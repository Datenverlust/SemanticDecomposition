"""WordNet dictionary backend via NLTK.

Provides synonyms, antonyms, hypernyms, hyponyms, meronyms, definitions, and
derivationally related forms from Princeton WordNet.
"""
from __future__ import annotations

import logging
from typing import List, Optional, TYPE_CHECKING

from .base_dictionary import BaseDictionary

if TYPE_CHECKING:
    from ..concept import Concept
    from ..definition import Definition

logger = logging.getLogger(__name__)

try:
    from nltk.corpus import wordnet as wn
    _WN_AVAILABLE = True
except ImportError:
    _WN_AVAILABLE = False


class WordnetDictionary(BaseDictionary):
    """Dictionary backed by Princeton WordNet (via NLTK)."""

    _initialized: bool = False

    @classmethod
    def init(cls) -> None:
        super().init()
        if cls._initialized:
            return
        if not _WN_AVAILABLE:
            logger.warning("NLTK wordnet not available — pip install nltk")
            cls._initialized = True
            return
        import nltk
        for resource in ("wordnet", "omw-1.4"):
            try:
                nltk.data.find(f"corpora/{resource}")
            except LookupError:
                nltk.download(resource, quiet=True)
        cls._initialized = True
        logger.debug("WordnetDictionary initialised")

    def _synsets(self, word: str) -> list:
        if not _WN_AVAILABLE:
            return []
        return wn.synsets(word)

    def get_synonyms(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        for ss in self._synsets(word):
            for lemma in ss.lemmas():
                name = lemma.name().replace("_", " ")
                if name.lower() != word.lower() and name.lower() not in seen:
                    seen.add(name.lower())
                    result.append(self._make_concept(name))
        return result

    def get_antonyms(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        for ss in self._synsets(word):
            for lemma in ss.lemmas():
                for ant in lemma.antonyms():
                    name = ant.name().replace("_", " ")
                    if name.lower() not in seen:
                        seen.add(name.lower())
                        result.append(self._make_concept(name))
        return result

    def get_hypernyms(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        for ss in self._synsets(word):
            for hyper in ss.hypernyms():
                for lemma in hyper.lemmas():
                    name = lemma.name().replace("_", " ")
                    if name.lower() not in seen:
                        seen.add(name.lower())
                        result.append(self._make_concept(name))
        return result

    def get_hyponyms(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        for ss in self._synsets(word):
            for hypo in ss.hyponyms():
                for lemma in hypo.lemmas():
                    name = lemma.name().replace("_", " ")
                    if name.lower() not in seen:
                        seen.add(name.lower())
                        result.append(self._make_concept(name))
        return result

    def get_meronyms(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        for ss in self._synsets(word):
            for mero in ss.part_meronyms() + ss.substance_meronyms() + ss.member_meronyms():
                for lemma in mero.lemmas():
                    name = lemma.name().replace("_", " ")
                    if name.lower() not in seen:
                        seen.add(name.lower())
                        result.append(self._make_concept(name))
        return result

    def get_definitions(self, word: str) -> List["Definition"]:
        from ..definition import Definition

        results: List["Definition"] = []
        for ss in self._synsets(word):
            defn_text = ss.definition()
            if not defn_text:
                continue
            tokens = self._tokenise_definition(defn_text)
            if tokens:
                results.append(Definition([self._make_concept(t) for t in tokens]))
        return results

    def fill_concept(self, concept: "Concept") -> "Concept":
        concept = self.set_lemma(concept)
        concept = self.set_pos(concept)
        concept = self.fill_related(concept)
        return concept

    def fill_related(self, concept: "Concept") -> "Concept":
        word = concept.litheral or ""
        concept.synonyms.extend(self.get_synonyms(word))
        concept.antonyms.extend(self.get_antonyms(word))
        concept.hypernyms.extend(self.get_hypernyms(word))
        concept.hyponyms.extend(self.get_hyponyms(word))
        concept.meronyms.extend(self.get_meronyms(word))
        concept.definitions.extend(self.get_definitions(word))
        concept.derivations.extend(self._get_derivations(word))
        return concept

    def get_lemma(self, word: str) -> str:
        if self._nlp is not None:
            doc = self._nlp(word)
            if doc:
                return doc[0].lemma_
        return word

    def get_concept(self, word: str) -> "Concept":
        from ..concept import Concept
        c = Concept()
        c.litheral = word
        return self.fill_concept(c)

    def set_pos(self, concept: "Concept") -> "Concept":
        if self._nlp is not None:
            doc = self._nlp(concept.litheral)
            if doc:
                concept.pos = doc[0].pos_
        return concept

    def fill_definition(self, definition: "Definition") -> "Definition":
        for c in definition.concepts:
            self.fill_concept(c)
        return definition

    # --- helpers ---

    def _get_derivations(self, word: str) -> List["Concept"]:
        seen: set[str] = set()
        result: List["Concept"] = []
        if not _WN_AVAILABLE:
            return result
        for ss in self._synsets(word):
            for lemma in ss.lemmas():
                for deriv in lemma.derivationally_related_forms():
                    name = deriv.name().replace("_", " ")
                    if name.lower() != word.lower() and name.lower() not in seen:
                        seen.add(name.lower())
                        result.append(self._make_concept(name))
        return result

    def _make_concept(self, litheral: str) -> "Concept":
        from ..concept import Concept
        from ..word_type import WordType
        c = Concept()
        c.litheral = litheral
        c.word_type = WordType.NN
        return c

    def _tokenise_definition(self, text: str) -> List[str]:
        stop = {"a", "an", "the", "is", "are", "of", "to", "in", "for",
                "and", "or", "that", "which", "with", "by", "on", "at",
                "as", "its", "it", "be", "been", "was", "were", "has",
                "have", "had", "not", "no", "but", "from", "this", "than"}
        if self._nlp is not None:
            doc = self._nlp(text)
            return [t.lemma_.lower() for t in doc
                    if t.is_alpha and t.lemma_.lower() not in stop and len(t.lemma_) > 1]
        return [w.lower() for w in text.split()
                if w.isalpha() and w.lower() not in stop and len(w) > 1]
