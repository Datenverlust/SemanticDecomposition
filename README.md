# SemanticDecomposition

Automatic construction of **semantic concept graphs** from natural language words.

Given a word, the framework queries a set of pluggable dictionary backends
(WordNet, Wiktionary, Wikidata, …) to collect synonyms, antonyms, hypernyms,
hyponyms, meronyms, and textual definitions, then assembles them into a weighted
graph. Downstream reasoning algorithms (marker passing, semantic similarity) can
operate on these graphs directly.

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

## Requirements

| Dependency | CPU (`python` branch) | GPU (`python_gpu` branch) |
|---|---|---|
| Python 3.9+ | ✓ | ✓ |
| `spacy` *(optional)* | lemmatisation | lemmatisation |
| `torch` (PyTorch ≥ 2.0) | — | ✓ required |
| CUDA toolkit | — | optional (falls back to CPU tensors) |
| `MarkerPassingAlgorithm` `python_gpu` branch | — | ✓ required |

```bash
# GPU branch only
pip install torch
# with CUDA (example for CUDA 12.1):
pip install torch --index-url https://download.pytorch.org/whl/cu121
```

## Installation

```bash
git clone https://github.com/Datenverlust/SemanticDecomposition.git
cd SemanticDecomposition
git checkout python_gpu
pip install -e .

# SemanticDecomposition depends on MarkerPassingAlgorithm:
git clone https://github.com/Datenverlust/MarkerPassingAlgorithm.git
cd MarkerPassingAlgorithm && git checkout python && pip install -e . && cd ..
```

---

## Package structure

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
│   └── config.py                   # Config singleton  — reads ~/.decomposition/
│
├── persistence/
│   └── concept_cache.py            # ConceptCache      — singleton, pickle-backed LRU
│
├── dictionaries/
│   ├── dictionary.py               # Dictionary ABC    — implement one per backend
│   └── base_dictionary.py          # BaseDictionary    — shared NLP pipeline (spaCy)
│
├── entities/
│   ├── entity.py / prime.py
│   ├── markers/
│   │   ├── double_marker.py        # DoubleMarker      — float activation
│   │   ├── double_marker_with_origin.py  # + origin concept
│   │   └── typed_marker.py         # TypedMarker       — typed activation
│   ├── links/
│   │   ├── weighted_link.py        # WeightedLink      — base link with weight
│   │   ├── synonym_link.py         #   weight  0.62
│   │   ├── antonym_link.py         #   weight -0.90
│   │   ├── hypernym_link.py        #   weight -0.02
│   │   ├── hyponym_link.py         #   weight  0.79
│   │   ├── definition_link.py      #   weight -0.78
│   │   └── arbitrary_relation_link.py  # weight 0.00
│   ├── nodes/
│   │   ├── double_node.py          # DoubleNode        — basic float activation node
│   │   ├── double_node_with_multiple_thresholds.py  # per-concept activation
│   │   └── typed_node.py           # TypedNode         — typed spreading activation
│   ├── relations/
│   │   ├── relation.py / role.py / synonym.py
│   └── spreading_activation/
│       ├── marker_passing_config.py       # MarkerPassingConfig — link weights + params
│       └── typed_marker_passing_config.py # TypedMarkerPassingConfig — GA-tunable params
│
├── graph/
│   ├── semantic_net.py             # SemanticNet       — dict-backed adjacency graph
│   ├── graph_util.py               # GraphUtil         — build / cache / merge graphs
│   └── edges/
│       ├── edge_type.py            # EdgeType enum
│       └── weighted_edge.py
│   └── spreading_activation/
│       ├── count_termination_condition.py
│       ├── double_spreading_activation.py
│       └── marker_passing/
│           ├── double_marker_passing.py           # DoubleMarkerPassing
│           ├── typed_marker_passing.py            # TypedMarkerPassing
│           └── marker_passing_semantic_distance_measure.py
│
├── distance/
│   ├── data_example.py
│   ├── similarity_pair.py
│   └── semantic_distance_measure.py  # SemanticDistanceMeasureInterface ABC
│
└── marker_passing/                 # Self-contained copy of the spreading-activation base
```

---

## Core concepts

### Concept

The fundamental semantic unit. Each `Concept` stores a word along with all its
semantic relations collected from dictionary backends.

| Attribute | Type | Description |
|---|---|---|
| `litheral` | `str` | The word string |
| `word_type` | `WordType` | Penn Treebank POS tag |
| `id` | `str` | Unique key (`litheral + "_" + word_type`) |
| `synonyms` | `List[Concept]` | Synonymous concepts |
| `antonyms` | `List[Concept]` | Antonymous concepts |
| `hypernyms` | `List[Concept]` | More general concepts |
| `hyponyms` | `List[Concept]` | More specific concepts |
| `meronyms` | `List[Concept]` | Part-of concepts |
| `definitions` | `List[Definition]` | Textual definitions as concept lists |
| `lemma` | `str` | Lemmatised form |
| `ner` | `str` | Named-entity type (if any) |

Two `Concept` objects are equal (and hash-equal) if they share the same `id`.

### WordType

`WordType` is an `Enum` wrapping the full Penn Treebank POS tag set.

```python
from semantic_decomposition import WordType

WordType.NN    # noun, singular
WordType.NNS   # noun, plural
WordType.VB    # verb, base form
WordType.VBD   # verb, past tense
WordType.JJ    # adjective
WordType.RB    # adverb
```

`word_type.type()` returns a broad category string (`"noun"`, `"verb"`,
`"adjective"`, `"adverb"`, or `"unknown"`).

### Dictionary (backend)

Implement `Dictionary` (or subclass `BaseDictionary`) to connect any lexical
resource. Every abstract method receives a concept or word string and should
return the appropriate relation list.

```python
from semantic_decomposition.dictionaries.base_dictionary import BaseDictionary
from semantic_decomposition import Concept

class MyDictionary(BaseDictionary):
    def get_synonyms(self, word: str) -> list:   return []
    def get_antonyms(self, word: str) -> list:   return []
    def get_hypernyms(self, word: str) -> list:  return []
    def get_hyponyms(self, word: str) -> list:   return []
    def get_meronyms(self, word: str) -> list:   return []
    def get_definitions(self, word: str) -> list:return []
    def fill_concept(self, concept: Concept) -> Concept: return concept
    def get_lemma(self, word: str) -> str:       return word
    def get_concept(self, word: str) -> Concept:
        c = Concept()
        c.litheral = word
        return c
    def set_pos(self, concept: Concept) -> Concept:   return concept
    def fill_definition(self, d) -> object:      return d
    def fill_related(self, c: Concept) -> Concept: return c
```

### Decomposition

`Decomposition` is the main entry point. It is a class with only class-level
state — no instantiation needed.

| Method | Description |
|---|---|
| `Decomposition.init(dictionaries)` | Register backends; call `init()` on each |
| `Decomposition.create_concept(word, word_type)` | Create an empty `Concept` |
| `Decomposition.decompose(concept)` | Fill a concept from all backends; result is cached |
| `Decomposition.multi_threaded_decompose(concepts)` | Decompose a list in parallel |
| `Decomposition.check_is_prime(word)` | Test whether a word is an NSM prime |
| `Decomposition.get_prime_of_concept(concept)` | Return the prime label or `None` |

### MarkerPassingConfig

Static configuration class for the spreading-activation parameters.

| Field | Default | Description |
|---|---|---|
| `start_activation` | `1.0` | Initial activation placed on seed concepts |
| `threshold` | `0.064` | Minimum activation for a node to fire |
| `termination_pulse_count` | `80` | Maximum number of pulses |
| `double_activation_limit` | `20` | Maximum markers a node can hold |
| `decomposition_depth` | `1` | Recursive expansion depth during graph construction |
| `_synonym_link_weight` | `0.62` | Weight on synonym edges |
| `_antonym_link_weight` | `-0.90` | Weight on antonym edges |
| `_hypernym_link_weight` | `-0.02` | Weight on hypernym edges |
| `_hyponym_link_weight` | `0.79` | Weight on hyponym edges |
| `_definition_link_weight` | `-0.78` | Weight on definition edges |
| `_arbitrary_relation_link_weight` | `0.0` | Weight on arbitrary edges |

### SemanticNet

Replaces JGraphT. A lightweight dict-backed directed graph.

```python
from semantic_decomposition.graph.semantic_net import SemanticNet

g = SemanticNet()
g.add_vertex(node_a)
g.add_vertex(node_b)
g.add_edge(edge)           # edge must expose .source and .target
g.vertex_set()             # all vertices
g.edge_set()               # all edges
g.out_edges_of(node_a)     # edges leaving node_a
g.in_edges_of(node_b)      # edges entering node_b
```

---

## Usage examples

### Example 1 — Decompose a word

```python
from semantic_decomposition import Decomposition, WordType
from my_package import MyDictionary  # your Dictionary implementation

Decomposition.init([MyDictionary()])

cat = Decomposition.create_concept("cat", WordType.NN)
cat = Decomposition.decompose(cat)

print(cat.litheral)         # "cat"
print(cat.synonyms)         # [Concept("feline"), …]
print(cat.hypernyms)        # [Concept("animal"), …]
print(len(cat.definitions)) # number of textual definitions
```

### Example 2 — Parallel decomposition of a sentence

```python
from semantic_decomposition import Decomposition, WordType

words = ["the", "cat", "sat", "on", "the", "mat"]
concepts = [Decomposition.create_concept(w, WordType.NN) for w in words]

# decompose all words in parallel using a thread pool
filled = Decomposition.multi_threaded_decompose(concepts)

for c in filled:
    print(c.litheral, "→", [s.litheral for s in c.synonyms])
```

### Example 3 — Semantic similarity with marker passing

`MarkerPassingSemanticDistanceMeasure` builds a merged concept graph for two
words, runs `DoubleMarkerPassing`, and returns a similarity score in `[0, ∞)`.

```python
from semantic_decomposition import Decomposition, WordType
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    MarkerPassingSemanticDistanceMeasure,
)

Decomposition.init([MyDictionary()])

cat = Decomposition.decompose(Decomposition.create_concept("cat", WordType.NN))
dog = Decomposition.decompose(Decomposition.create_concept("dog", WordType.NN))

measure = MarkerPassingSemanticDistanceMeasure()
score = measure.compare_concepts(cat, dog)
print(f"cat ↔ dog similarity: {score:.4f}")
```

The similarity formula is:

```
score = Σ |activation_at_node| / (2 × start_activation)
```

Nodes where markers from both seed concepts overlap contribute the most.

### Example 4 — Custom link weights

Override `MarkerPassingConfig` class attributes before running the algorithm:

```python
from semantic_decomposition.entities.spreading_activation.marker_passing_config import (
    MarkerPassingConfig,
)

MarkerPassingConfig._synonym_link_weight = 0.8
MarkerPassingConfig._antonym_link_weight = -0.5
MarkerPassingConfig.threshold = 0.1
MarkerPassingConfig.termination_pulse_count = 50
```

Or pass a modified config instance directly:

```python
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    DoubleMarkerPassing,
)

class MyConfig(MarkerPassingConfig):
    _synonym_link_weight = 0.9
    threshold = 0.05

algo = DoubleMarkerPassing(config=MyConfig())
algo.fill_nodes([cat, dog])
DoubleMarkerPassing.do_initial_marking(algo._concept_to_node, [cat, dog], 1.0)
algo.execute()
```

### Example 5 — Build and inspect a concept graph

```python
from semantic_decomposition.graph.graph_util import GraphUtil

graph = GraphUtil.create_graph(cat)   # builds a SemanticNet for "cat"
print(f"Vertices: {len(graph.vertex_set())}")
print(f"Edges:    {len(graph.edge_set())}")

for edge in graph.out_edges_of(cat_node):
    print(f"  {edge.source.litheral} --[{type(edge).__name__}]--> {edge.target.litheral}")
```

### Example 6 — Persist and reload a concept cache

`ConceptCache` is a singleton that serialises decomposed concepts to
`~/.decomposition/ConceptCache/cache.pkl` automatically.

```python
from semantic_decomposition.persistence.concept_cache import ConceptCache

cache = ConceptCache.get_instance()

# First run: decompose and cache
cat = Decomposition.decompose(Decomposition.create_concept("cat", WordType.NN))

# Second run: fetched from disk — no dictionary call needed
cat_cached = cache.get(cat.id)
print(cat_cached.litheral)   # "cat"
```

---

## GPU acceleration (`python_gpu` branch)

The `python_gpu` branch adds two GPU-native classes that replace the CPU
spreading-activation loop with **sparse matrix multiplication on the GPU**
via PyTorch / cuSPARSE.  Graph construction (dictionary lookup, decomposition,
node/link wiring) still runs on the CPU — only the pulse loop moves to the GPU.

### `GpuDoubleMarkerPassing`

Drop-in replacement for `DoubleMarkerPassing`.  Builds the identical concept
graph on the CPU, serialises it to a sparse weight matrix, then runs the
spreading loop as repeated SpMM on the GPU.

```python
from semantic_decomposition import Decomposition, WordType
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    GpuDoubleMarkerPassing,
)

Decomposition.init([MyDictionary()])

cat = Decomposition.decompose(Decomposition.create_concept("cat", WordType.NN))
dog = Decomposition.decompose(Decomposition.create_concept("dog", WordType.NN))

algo = GpuDoubleMarkerPassing(device="cuda")   # or "cpu" for testing
algo.fill_nodes([cat, dog])

# single activation vector — both seeds in one run
pulses = algo.execute([cat, dog], start_activation=1.0)
print(f"Converged in {pulses} pulses on {algo.device}")

activations = algo.get_all_activations()   # Dict[Concept, float]
for concept, act in sorted(activations.items(), key=lambda x: -abs(x[1]))[:5]:
    print(f"  {concept.litheral}: {act:.4f}")
```

### `GpuMarkerPassingSemanticDistanceMeasure`

GPU-accelerated semantic similarity.  Each `compare_concepts()` call evaluates
two seed vectors simultaneously in one `[N × 2]` SpMM per pulse.

```python
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    GpuMarkerPassingSemanticDistanceMeasure,
)

measure = GpuMarkerPassingSemanticDistanceMeasure(device="cuda")
score = measure.compare_concepts(cat, dog)
print(f"cat ↔ dog similarity: {score:.4f}")
```

### Batch evaluation — the primary GPU win

`compare_many()` evaluates an entire dataset in **one GPU call**: all pairs
are stacked into a single `[N × 2B]` activation matrix and spread simultaneously.

```python
from semantic_decomposition import Decomposition, WordType
from semantic_decomposition.graph.spreading_activation.marker_passing import (
    GpuMarkerPassingSemanticDistanceMeasure,
)

pairs_words = [("cat", "dog"), ("car", "ship"), ("run", "walk"), ("cold", "hot")]

pairs = [
    (
        Decomposition.decompose(Decomposition.create_concept(w1, WordType.NN)),
        Decomposition.decompose(Decomposition.create_concept(w2, WordType.NN)),
    )
    for w1, w2 in pairs_words
]

measure = GpuMarkerPassingSemanticDistanceMeasure(device="cuda")
scores = measure.compare_many(pairs)   # single GPU pass for all 4 pairs

for (w1, w2), score in zip(pairs_words, scores):
    print(f"{w1:10} ↔ {w2:10}  {score:.4f}")
```

Throughput scales near-linearly with the number of pairs — evaluating all 353
WordSim-353 pairs costs roughly the same GPU time as evaluating a single pair.

### CPU fallback

Both classes fall back to CPU tensors transparently when CUDA is not available.
Use `device="cpu"` explicitly to force CPU mode (useful for testing without a GPU):

```python
algo   = GpuDoubleMarkerPassing(device="cpu")
measure = GpuMarkerPassingSemanticDistanceMeasure(device="cpu")
```

### Package location

```
semantic_decomposition/graph/spreading_activation/marker_passing/
├── double_marker_passing.py                     # CPU original
├── marker_passing_semantic_distance_measure.py  # CPU original
├── gpu_double_marker_passing.py                 # GPU — GpuDoubleMarkerPassing
└── gpu_marker_passing_semantic_distance_measure.py  # GPU — GpuMarkerPassingSemanticDistanceMeasure
```

---

## Interactive graph visualisation

The `semantic_decomposition.visualization` package renders a decomposition graph
**live in the browser** and lets you explore it interactively.  As a word is
decomposed, new nodes and edges stream in over Server-Sent Events and animate
into a force-directed layout.  **Double-clicking any node decomposes that concept
one hop further**, so you can grow the semantic neighbourhood on demand.

The server is pure Python standard library — no Flask, no websockets, no build
step.  The frontend loads `vis-network` from a CDN.

### Zero-setup demo

An offline `DemoDictionary` ships with a small connected vocabulary, so you can
try the UI without WordNet / Wiktionary / Wikidata:

```bash
python -m semantic_decomposition.visualization cat
# then double-click nodes in the browser to expand them
```

Options:

```bash
python -m semantic_decomposition.visualization dog --port 9000 --no-browser
python -m semantic_decomposition.visualization animal --emit-delay 0.15   # slower animation
```

### With your own dictionary backends

```python
from semantic_decomposition import Decomposition
from semantic_decomposition.visualization import launch

Decomposition.init([MyDictionary()])   # real WordNet / Wiktionary / … backends
launch("cat")                          # opens a browser, blocks until Ctrl-C
```

### How it works

```
                    GET  /            → single-page frontend (vis-network)
Browser  ◀── SSE ── GET  /events      → stream of add_node / add_edge / node_expanded
         ── POST ─▶ POST /expand      → decompose one node further  (double-click)
         ── POST ─▶ POST /decompose   → start a new root word       (search box)
```

`DecompositionGraphVisualizer` holds the authoritative graph and fans mutation
events out to every connected browser.  Slow dictionary calls run off the request
thread, so nodes appear incrementally as each relation is resolved.

| Class | Role |
|---|---|
| `DecompositionGraphVisualizer` | Live graph model + incremental event stream; `start()` / `expand()` |
| `VisualizationServer` | Stdlib HTTP server: static page, SSE, `/expand`, `/decompose` |
| `DemoDictionary` | Offline sample vocabulary for a zero-setup demo |
| `launch()` / `launch_demo()` | One-call entry points |

Edges are colour-coded by relation (synonym, antonym, hypernym, hyponym,
meronym, definition, derivation); root, expanded, and not-yet-expanded nodes are
styled distinctly.  Drag to rearrange, scroll to zoom, **Fit** to recentre.

### Package location

```
semantic_decomposition/visualization/
├── __init__.py            # launch() / launch_demo()
├── __main__.py            # python -m semantic_decomposition.visualization <word>
├── graph_visualizer.py    # DecompositionGraphVisualizer
├── server.py              # VisualizationServer (stdlib http.server + SSE)
├── demo_dictionary.py     # DemoDictionary (offline vocabulary)
└── static/
    └── index.html         # force-directed frontend (vis-network)
```

---

## Configuration file

Create `~/.decomposition/decomposition.cfg` to configure language and primes:

```ini
[decomposition]
language = EN                          # EN or GER
primes_dir = ~/.decomposition/primes   # directory with NSM prime word lists
stop_words = the, a, an, of, to       # comma-separated stop words
```

Read at runtime via `Config.get_instance()`:

```python
from semantic_decomposition.settings.config import Config

cfg = Config.get_instance()
print(cfg.language)       # Language.EN
print(cfg.stop_words())   # ['the', 'a', 'an', …]
```

---

## API reference

### `Decomposition`

```python
Decomposition.init(dictionaries: List[Dictionary]) -> None
Decomposition.create_concept(litheral: str, word_type: WordType = None) -> Concept
Decomposition.decompose(concept: Concept) -> Concept
Decomposition.multi_threaded_decompose(concepts: List[Concept]) -> List[Concept]
Decomposition.check_is_prime(word: str) -> bool
Decomposition.get_prime_of_concept(concept: Concept) -> Optional[str]
```

### `MarkerPassingSemanticDistanceMeasure`

```python
measure = MarkerPassingSemanticDistanceMeasure(config=None)
score: float = measure.compare_concepts(c1: Concept, c2: Concept)
score: float = measure.pass_marker(c1: Concept, c2: Concept)
```

### `DoubleMarkerPassing`

```python
algo = DoubleMarkerPassing(config=None)
algo.fill_nodes(concepts: List[Concept]) -> None
DoubleMarkerPassing.do_initial_marking(
    nodes: Dict[Concept, Node],
    concepts: List[Concept],
    start_activation: float,
) -> None
algo.execute() -> None   # inherited from SpreadingAlgorithm
```

### `SemanticNet`

```python
g = SemanticNet()
g.add_vertex(node) -> None
g.add_edge(edge) -> None
g.vertex_set() -> Set
g.edge_set() -> Set
g.out_edges_of(node) -> List
g.in_edges_of(node) -> List
```

---

## Related projects

- **MarkerPassingAlgorithm** — spreading-activation engine used internally
- **SemanticDecompositionExperiments** — NLP experiments built on top of this library

---

## License

[GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)

Contact: datenverlust@gmail.com
