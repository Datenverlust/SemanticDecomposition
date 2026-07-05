from __future__ import annotations
from typing import Optional, TYPE_CHECKING

from .relation import Relation

if TYPE_CHECKING:
    from ..entity import Entity


class Role(Relation):
    def __init__(self, name: str = "") -> None:
        super().__init__(name)
        self.source: Optional["Entity"] = None
        self.target: Optional["Entity"] = None
