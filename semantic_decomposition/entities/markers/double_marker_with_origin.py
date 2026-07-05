from __future__ import annotations
from typing import List, Optional, TYPE_CHECKING

from ...marker_passing.marker import Marker

if TYPE_CHECKING:
    from ..links.weighted_link import WeightedLink
    from ...concept import Concept


class DoubleMarkerWithOrigin(Marker):
    """Marker tracking activation, its origin concept, and the traversal path."""

    def __init__(
        self,
        activation: float = 0.0,
        origin: Optional["Concept"] = None,
        link_type: str = "",
    ) -> None:
        self.activation: float = activation
        self.origin: Optional["Concept"] = origin
        self.link_type: str = link_type
        self.visited_concepts: List["Concept"] = []
        self.visited_links: List["WeightedLink"] = []
        self.answers: List["Concept"] = []

    def __repr__(self) -> str:
        return f"DoubleMarkerWithOrigin(activation={self.activation}, origin={self.origin})"
