from __future__ import annotations
from typing import List, TYPE_CHECKING

if TYPE_CHECKING:
    from .concept import Concept


class Definition:
    """A definition expressed as a list of concepts (one per token)."""

    def __init__(self, concepts: List["Concept"] | None = None) -> None:
        self._concepts: List["Concept"] = concepts if concepts is not None else []

    @property
    def concepts(self) -> List["Concept"]:
        return self._concepts

    @concepts.setter
    def concepts(self, value: List["Concept"]) -> None:
        self._concepts = value

    def __repr__(self) -> str:
        return f"Definition({self._concepts!r})"
