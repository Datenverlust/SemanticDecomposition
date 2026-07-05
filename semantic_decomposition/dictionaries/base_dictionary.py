from __future__ import annotations
from abc import abstractmethod
from typing import List, Optional, TYPE_CHECKING

from .dictionary import Dictionary
from ..settings.config import Config, Language

if TYPE_CHECKING:
    from ..concept import Concept
    from ..definition import Definition

try:
    import spacy
    _SPACY_AVAILABLE = True
except ImportError:
    _SPACY_AVAILABLE = False


class BaseDictionary(Dictionary):
    """Abstract base providing shared NLP pipeline (spaCy replacing StanfordCoreNLP)."""

    _nlp = None

    @classmethod
    def init(cls) -> None:
        if not _SPACY_AVAILABLE:
            return
        config = Config.get_instance()
        model = "en_core_web_sm" if config.language == Language.EN else "de_core_news_sm"
        try:
            cls._nlp = spacy.load(model)
        except OSError:
            cls._nlp = None

    def set_lemma(self, concept: "Concept") -> "Concept":
        if self._nlp is None:
            concept.lemma = concept.litheral
            return concept
        doc = self._nlp(concept.litheral)
        if doc:
            concept.lemma = doc[0].lemma_
        else:
            concept.lemma = concept.litheral
        return concept

    @abstractmethod
    def get_synonyms(self, word: str) -> List["Concept"]: ...

    @abstractmethod
    def get_antonyms(self, word: str) -> List["Concept"]: ...

    @abstractmethod
    def get_hypernyms(self, word: str) -> List["Concept"]: ...

    @abstractmethod
    def get_hyponyms(self, word: str) -> List["Concept"]: ...

    @abstractmethod
    def get_meronyms(self, word: str) -> List["Concept"]: ...

    @abstractmethod
    def get_definitions(self, word: str) -> List["Definition"]: ...

    @abstractmethod
    def fill_concept(self, concept: "Concept") -> "Concept": ...

    @abstractmethod
    def get_lemma(self, word: str) -> str: ...

    @abstractmethod
    def get_concept(self, word: str) -> "Concept": ...

    @abstractmethod
    def set_pos(self, concept: "Concept") -> "Concept": ...

    @abstractmethod
    def fill_definition(self, definition: "Definition") -> "Definition": ...

    @abstractmethod
    def fill_related(self, concept: "Concept") -> "Concept": ...
