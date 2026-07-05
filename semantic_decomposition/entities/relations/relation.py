from __future__ import annotations
from typing import List, TYPE_CHECKING

from ..entity import Entity

if TYPE_CHECKING:
    from .role import Role


class Relation(Entity):
    def __init__(self, name: str = "") -> None:
        super().__init__(name)
        self.roles: List["Role"] = []

    def is_relationship(self) -> bool:
        return True
