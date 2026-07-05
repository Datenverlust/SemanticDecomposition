from __future__ import annotations
from typing import List, TYPE_CHECKING

if TYPE_CHECKING:
    from .relations.relation import Relation


class Entity:
    def __init__(self, name: str = "") -> None:
        self.name: str = name
        self.relations: List["Relation"] = []

    def is_relationship(self) -> bool:
        return False

    def __repr__(self) -> str:
        return f"{self.__class__.__name__}({self.name!r})"

    def __hash__(self) -> int:
        return hash(self.name)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Entity):
            return NotImplemented
        return self.name == other.name
