from __future__ import annotations
from typing import List, Optional, TYPE_CHECKING

from .relation import Relation

if TYPE_CHECKING:
    from ..entity import Entity


class Synonym(Relation):
    def __init__(self, name: str = "") -> None:
        super().__init__(name)
        self.entity: Optional["Entity"] = None
        self.sememe: List["Entity"] = []
