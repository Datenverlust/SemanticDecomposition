from __future__ import annotations
from typing import Optional, TYPE_CHECKING

from ...marker_passing.link import Link

if TYPE_CHECKING:
    from ...marker_passing.node import Node
    from ..spreading_activation.marker_passing_config import MarkerPassingConfig


class WeightedLink(Link):
    """Directed link between two nodes carrying a numeric weight."""

    def __init__(
        self,
        source: Optional["Node"] = None,
        target: Optional["Node"] = None,
        weight: float = 0.0,
        config: Optional["MarkerPassingConfig"] = None,
    ) -> None:
        self._source = source
        self._target = target
        self._weight = weight
        self._config = config

    @property
    def source(self) -> Optional["Node"]:
        return self._source

    @source.setter
    def source(self, value: "Node") -> None:
        self._source = value

    @property
    def target(self) -> Optional["Node"]:
        return self._target

    @target.setter
    def target(self, value: "Node") -> None:
        self._target = value

    @property
    def weight(self) -> float:
        return self._weight

    @property
    def marker_passing_config(self) -> Optional["MarkerPassingConfig"]:
        return self._config

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, WeightedLink):
            return NotImplemented
        return self._source == other._source and self._target == other._target

    def __hash__(self) -> int:
        return hash((id(self._source), id(self._target)))
