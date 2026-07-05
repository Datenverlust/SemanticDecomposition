from __future__ import annotations
from typing import Dict, List, Optional
from .i_concept import IConcept
from .word_type import WordType


class Concept(IConcept):
    """Core data type representing a word/concept with all its semantic relations."""

    def __init__(self) -> None:
        self.litheral: str = ""
        self.word_type: Optional[WordType] = None
        self.id: int = 0
        self.synonyms: List["Concept"] = []
        self.antonyms: List["Concept"] = []
        self.hypernyms: List["Concept"] = []
        self.hyponyms: List["Concept"] = []
        self.meronyms: List["Concept"] = []
        self.definitions: List[object] = []
        self.derivations: List["Concept"] = []
        self.arbitrary_relations: Dict[str, List["Concept"]] = {}
        self.lemma: str = ""
        self.ner: str = ""
        self.originated_relation_name: str = ""

    def __repr__(self) -> str:
        return f"Concept({self.litheral!r})"

    def __hash__(self) -> int:
        return hash(self.id)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Concept):
            return NotImplemented
        return self.id == other.id
