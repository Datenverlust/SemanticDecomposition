# SemanticDecomposition

Automatic construction of **semantic concept graphs** from natural language words,
available in both **Java** (original) and **Python** (`python-rebuild` branch).

Given a word, the framework queries a set of dictionary backends (WordNet,
Wiktionary, Wikidata, …) to collect synonyms, antonyms, hypernyms, hyponyms,
meronyms, and textual definitions, then assembles them into a weighted graph
that downstream reasoning algorithms (marker passing, similarity measures) can
operate on.

```
@InProceedings{Faehndrich2018,
  author    = {Fähndrich, Johannes and Weber, Sabine and Kanthak, Hannes},
  title     = {A Marker Passing Approach to Winograd Schemas},
  booktitle = {Semantic Technology},
  year      = {2018},
  publisher = {Springer International Publishing},
  pages     = {165--181},
  doi       = {10.1007/978-3-030-04284-4_11}
}
```

---

## Python package (`python-rebuild` branch)

### Requirements

- Python 3.9+
- `spacy` *(optional)* — lemmatisation in `BaseDictionary`; install the model
  matching your language (`en_core_web_sm` / `de_core_news_sm`)

```bash
pip install spacy
python -m spacy download en_core_web_sm
```

### Installation

```bash
git clone https://github.com/Datenverlust/SemanticDecomposition.git
cd SemanticDecomposition
git checkout python-rebuild

# also needs the MarkerPassing package on PYTHONPATH:
export PYTHONPATH="/path/to/MarkerPassingAlgorithm:$PYTHONPATH"

# or install both as editable packages:
pip install -e ../MarkerPassingAlgorithm
pip install -e .
```

### Package structure

```
semantic_decomposition/
├── __init__.py                     # Top-level exports
├── i_concept.py                    # IConcept          — marker interface
├── concept.py                      # Concept           — core semantic entity
├── definition.py                   # Definition        — list of concepts (one per token)
├── word_type.py                    # WordType          — Penn Treebank POS enum
├── decomposition.py                # Decomposition     — main entry point
├── decomposition_config.py         # DecompositionConfig
│
├── exceptions/
│   └── dictionary_does_not_contain_concept_exception.py
│
├── settings/
│   └── config.py                   # Config            — singleton, reads ~/.decomposition/
│
├── persistence/
│   └── concept_cache.py            # ConceptCache      — singleton, pickle-backed LRU
│
├── dictionaries/
│   ├── dictionary.py               # Dictionary        — ABC for all backends
│   └── base_dictionary.py          # BaseDictionary    — NLP pipeline (spaCy)
│
├── entities/
│   ├── entity.py / prime.py
│   ├── markers/                    # DoubleMarker, DoubleMarkerWithOrigin, TypedMarker
│   ├── links/                      # WeightedLink + Synonym/Antonym/Hypernym/…Link
│   ├── nodes/                      # DoubleNode, DoubleNodeWithMultipleThresholds, TypedNode
│   ├── relations/                  # Relation, Role, Synonym
│   └── spreading_activation/       # MarkerPassingConfig, TypedMarkerPassingConfig
│
├── graph/
│   ├── semantic_net.py             # SemanticNet       — replaces JGraphT
│   ├── graph_util.py               # GraphUtil         — build/cache/merge graphs
│   └── edges/                      # EdgeType enum, WeightedEdge
│   └── spreading_activation/
│       ├── count_termination_condition.py
│       ├── double_spreading_activation.py
│       └── marker_passing/
│           ├── double_marker_passing.py
│           ├── typed_marker_passing.py
│           └── marker_passing_semantic_distance_measure.py
│
├── distance/
│   ├── data_example.py
│   ├── similarity_pair.py
│   └── semantic_distance_measure.py  # SemanticDistanceMeasureInterface ABC
│
└── marker_passing/                 # Self-contained copy of the SpreadingAlgorithm base
```

### Quick start

```python
from semantic_decomposition import Concept, Definition, WordType, Decomposition
from semantic_decomposition.dictionaries.base_dictionary import BaseDictionary

# 1. Implement a Dictionary backend (e.g. wrapping NLTK WordNet)
class MyDictionary(BaseDictionary):
    def get_synonyms(self, word):   return []
    def get_antonyms(self, word):   return []
    def get_hypernyms(self, word):  return []
    def get_hyponyms(self, word):   return []
    def get_meronyms(self, word):   return []
    def get_definitions(self, word):return []
    def fill_concept(self, concept):return concept
    def get_lemma(self, word):      return word
    def get_concept(self, word):
        c = Concept()
        c.litheral = word
        return c
    def set_pos(self, concept):     return concept
    def fill_definition(self, d):   return d
    def fill_related(self, c):      return c

# 2. Initialise Decomposition with your backend(s)
Decomposition.init([MyDictionary()])

# 3. Decompose a word into a Concept
cat = Decomposition.create_concept("cat", WordType.NN)
cat = Decomposition.decompose(cat)

# 4. Measure semantic distance
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    MarkerPassingSemanticDistanceMeasure,
)

dog = Decomposition.create_concept("dog", WordType.NN)
dog = Decomposition.decompose(dog)

measure = MarkerPassingSemanticDistanceMeasure()
similarity = measure.compare_concepts(cat, dog)
print(f"cat ↔ dog similarity: {similarity:.4f}")
```

### Configuration

Create `~/.decomposition/decomposition.cfg`:

```ini
[decomposition]
language = EN          # EN or GER
primes_dir = ~/.decomposition/primes
stop_words = the, a, an, of
```

---

## Java package (original)

### Requirements

- Java 8, Maven, ≥ 8 GB RAM (16 GB recommended for full Wikidata)

### Build

```bash
# 1. Install MarkerPassingAlgorithm
git clone https://github.com/Datenverlust/MarkerPassingAlgorithm.git
cd MarkerPassingAlgorithm && mvn clean install && cd ..

# 2. Install JWKTL
git clone https://github.com/dkpro/dkpro-jwktl.git
cd dkpro-jwktl && mvn clean install && cd ..

# 3. Build SemanticDecomposition
git clone https://github.com/Datenverlust/SemanticDecomposition.git
cd SemanticDecomposition && mvn clean install
```

> **First run note:** WordNet, Wiktionary, and Wikidata are downloaded and
> converted into local databases on first use. This can take a long time and
> significant disk space (Wikidata extracted: ~250 GB).

---

## Key translation decisions (Java → Python)

| Java | Python |
|---|---|
| JGraphT `Graph` | Custom `SemanticNet` (dict-backed adjacency) |
| Guava `LoadingCache` | `dict`-based `ConceptCache` (pickle persistence) |
| Guava `BiMap` | Two mirrored `dict`s |
| `StanfordCoreNLP` | Optional `spaCy` pipeline |
| `ExecutorService` | `concurrent.futures.ThreadPoolExecutor` |
| Java `enum` | `enum.Enum` |
| Singleton `getInstance()` | Class-level `_instance` pattern |

---

## Related projects

- **MarkerPassingAlgorithm** — spreading-activation engine used internally
- **SemanticDecompositionExperiments** — NLP experiments built on top of this library

---

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)

Contact: datenverlust@gmail.com
