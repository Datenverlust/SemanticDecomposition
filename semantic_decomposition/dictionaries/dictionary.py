from __future__ import annotations
from abc import ABC, abstractmethod
from typing import List, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from ..concept import Concept
    from ..definition import Definition
    from ..word_type import WordType


class Dictionary(ABC):
    """Interface every semantic dictionary backend must implement."""

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

    @abstractmethod
    def set_lemma(self, concept: "Concept") -> "Concept": ...
