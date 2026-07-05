from __future__ import annotations
from typing import Any, Dict, Optional

from .edge_type import EdgeType


class WeightedEdge:
    """Graph edge carrying a type, weight, and arbitrary attributes."""

    def __init__(
        self,
        source: Any = None,
        target: Any = None,
        edge_type: EdgeType = EdgeType.Unknown,
        weight: float = 1.0,
    ) -> None:
        self._source = source
        self._target = target
        self.edge_type: EdgeType = edge_type
        self._weight: float = weight
        self.attributes: Dict[str, Any] = {}

    @property
    def source(self) -> Any:
        return self._source

    @property
    def target(self) -> Any:
        return self._target

    def get_edge_weight(self) -> float:
        return self._weight

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, WeightedEdge):
            return NotImplemented
        return self._source == other._source and self._target == other._target

    def __hash__(self) -> int:
        return hash((self._source, self._target))

    def __repr__(self) -> str:
        return f"WeightedEdge({self._source!r} -[{self.edge_type.value}]-> {self._target!r})"
