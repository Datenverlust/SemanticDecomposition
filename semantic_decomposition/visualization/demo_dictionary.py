from __future__ import annotations

from itertools import count
from typing import Dict, List, TYPE_CHECKING

if TYPE_CHECKING:
    from ..concept import Concept

# A small, connected, offline semantic neighbourhood so the visualiser is
# runnable with zero setup (no WordNet / Wiktionary / Wikidata required).
# Each entry: litheral -> {relation: [related litherals]}.
_DATA: Dict[str, Dict[str, List[str]]] = {
    "cat": {
        "synonyms": ["feline"],
        "hypernyms": ["mammal", "pet"],
        "hyponyms": ["kitten", "tabby"],
        "meronyms": ["claw", "whisker"],
        "definitions": ["small", "domesticated", "carnivore"],
    },
    "dog": {
        "synonyms": ["hound", "canine"],
        "hypernyms": ["mammal", "pet"],
        "hyponyms": ["puppy", "terrier"],
        "meronyms": ["paw", "tail"],
    },
    "pet": {
        "synonyms": ["companion"],
        "hypernyms": ["animal"],
        "hyponyms": ["cat", "dog"],
    },
    "feline": {
        "hypernyms": ["mammal"],
        "hyponyms": ["cat", "lion", "tiger"],
    },
    "mammal": {
        "hypernyms": ["animal", "vertebrate"],
        "hyponyms": ["cat", "dog", "whale"],
    },
    "animal": {
        "hypernyms": ["organism"],
        "hyponyms": ["mammal", "bird", "fish"],
        "antonyms": ["plant"],
    },
    "kitten": {"hypernyms": ["cat"], "synonyms": ["kitty"]},
    "puppy": {"hypernyms": ["dog"]},
    "tabby": {"hypernyms": ["cat"]},
    "terrier": {"hypernyms": ["dog"]},
    "tiger": {"hypernyms": ["feline"], "meronyms": ["stripe"]},
    "lion": {"hypernyms": ["feline"], "meronyms": ["mane"]},
    "whale": {"hypernyms": ["mammal", "animal"], "meronyms": ["fluke"]},
    "bird": {
        "hypernyms": ["animal"],
        "hyponyms": ["sparrow", "eagle"],
        "meronyms": ["wing", "beak"],
    },
    "fish": {"hypernyms": ["animal"], "meronyms": ["fin", "gill"]},
    "sparrow": {"hypernyms": ["bird"]},
    "eagle": {"hypernyms": ["bird"]},
    "plant": {
        "antonyms": ["animal"],
        "hypernyms": ["organism"],
        "hyponyms": ["tree", "flower"],
    },
    "tree": {"hypernyms": ["plant"], "meronyms": ["leaf", "root", "branch"]},
    "flower": {"hypernyms": ["plant"], "meronyms": ["petal", "stem"]},
    "vertebrate": {"hypernyms": ["organism"], "hyponyms": ["mammal", "bird", "fish"]},
    "organism": {"hyponyms": ["animal", "plant"]},
    "canine": {"synonyms": ["dog"], "hypernyms": ["mammal"]},
    "hound": {"hypernyms": ["dog"]},
    "companion": {"synonyms": ["pet", "friend"]},
}

_RELATION_ATTR = {
    "synonyms": "synonyms",
    "antonyms": "antonyms",
    "hypernyms": "hypernyms",
    "hyponyms": "hyponyms",
    "meronyms": "meronyms",
    "derivations": "derivations",
}


class DemoDictionary:
    """
    Zero-dependency, offline dictionary backend for demoing the visualiser.

    Duck-typed to the Decomposition backend contract: it only needs ``init()``
    and ``fill_concept(concept)``.  ``fill_concept`` looks the word up in the
    static table and attaches *shallow* related concepts (litheral only), so
    that each double-click / expand fetches the next hop on demand.
    """

    def __init__(self) -> None:
        self._ids = count(1)

    def init(self) -> None:  # noqa: D401 - matches backend contract
        pass

    def fill_concept(self, concept: "Concept") -> "Concept":
        word = (concept.litheral or "").strip().lower()
        entry = _DATA.get(word)
        if not entry:
            return concept

        for key, attr in _RELATION_ATTR.items():
            for litheral in entry.get(key, []):
                getattr(concept, attr).append(self._shallow(litheral))

        # definitions are modelled as a single Definition holding token concepts
        def_words = entry.get("definitions")
        if def_words:
            definition = self._make_definition([self._shallow(w) for w in def_words])
            concept.definitions.append(definition)

        return concept

    # --- helpers ---

    def _shallow(self, litheral: str) -> "Concept":
        from ..concept import Concept
        from ..word_type import WordType

        c = Concept()
        c.litheral = litheral
        c.word_type = WordType.NN
        c.id = next(self._ids)
        return c

    @staticmethod
    def _make_definition(concepts: List["Concept"]):
        try:
            from ..definition import Definition
            d = Definition()
            d.concepts = concepts
            return d
        except Exception:
            # fall back to a duck-typed stand-in with a .concepts attribute
            class _Def:
                pass
            d = _Def()
            d.concepts = concepts
            return d
