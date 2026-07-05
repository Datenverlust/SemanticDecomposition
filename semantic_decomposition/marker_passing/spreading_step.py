from __future__ import annotations
from typing import List, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from .link import Link
    from .marker import Marker
    from .node import Node


class SpreadingStep:
    """One activation step within a pulse: a link traversal with markers."""

    def __init__(self) -> None:
        self._link: Optional["Link"] = None
        self._in_direction: bool = True
        self._markings: List["Marker"] = []

    @property
    def link(self) -> Optional["Link"]:
        return self._link

    @link.setter
    def link(self, value: Optional["Link"]) -> None:
        self._link = value

    @property
    def in_direction(self) -> bool:
        return self._in_direction

    @in_direction.setter
    def in_direction(self, value: bool) -> None:
        self._in_direction = value

    @property
    def markings(self) -> List["Marker"]:
        return self._markings

    @markings.setter
    def markings(self, value: List["Marker"]) -> None:
        self._markings = value

    def get_target_node(self) -> Optional["Node"]:
        if self._link is None:
            return None
        return self._link.target if self._in_direction else self._link.source
