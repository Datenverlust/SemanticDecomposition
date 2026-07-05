from __future__ import annotations
from typing import Collection, Dict, List, Optional, TYPE_CHECKING

from ...marker_passing.node import Node
from ...marker_passing.spreading_step import SpreadingStep

if TYPE_CHECKING:
    from ...marker_passing.link import Link
    from ...marker_passing.marker import Marker
    from ...concept import Concept
    from ..markers.typed_marker import TypedMarker


_C_T: float = 100_000.0


class TypedNode(Node):
    """Node for typed marker-passing; tracks per-(concept, type) activation."""

    def __init__(
        self,
        litheral: str = "",
        concept: Optional["Concept"] = None,
    ) -> None:
        self.litheral: str = litheral
        self.concept: Optional["Concept"] = concept
        self._links: List["Link"] = []
        self._markers: List["Marker"] = []
        # Map[concept -> TypedMarker]
        self.activation: Dict["Concept", "TypedMarker"] = {}
        self.pulse_count: int = 0

    def get_links(self) -> List["Link"]:
        return self._links

    def get_markers(self) -> List["Marker"]:
        return self._markers

    def check_thresholds(self, marker_classes: object = None) -> bool:
        return bool(self.activation)

    def get_cumulative_activation(self) -> float:
        return sum(m.value for m in self.activation.values())

    def get_cumulative_weighted_activation(self) -> float:
        total = 0.0
        for concept, marker in self.activation.items():
            total += marker.value / _C_T
        return total

    def in_function(self, input_steps: Collection[SpreadingStep]) -> None:
        from ..markers.typed_marker import TypedMarker
        for step in input_steps:
            for marker in step.markings:
                if isinstance(marker, TypedMarker) and marker.origin is not None:
                    existing = self.activation.get(marker.origin)
                    if existing is None:
                        self.activation[marker.origin] = TypedMarker(
                            value=marker.value, origin=marker.origin, marker_type=marker.type
                        )
                    else:
                        existing.value += marker.value

    def out_function(self) -> Collection[SpreadingStep]:
        from ..markers.typed_marker import TypedMarker
        steps: List[SpreadingStep] = []
        for link in self._links:
            from ..links.weighted_link import WeightedLink
            if not isinstance(link, WeightedLink):
                continue
            for concept, marker in self.activation.items():
                new_val = marker.value * link.weight
                new_marker = TypedMarker(value=new_val, origin=concept, marker_type=marker.type)
                step = SpreadingStep()
                step.link = link
                step.in_direction = True
                step.markings = [new_marker]
                steps.append(step)
        return steps

    def __repr__(self) -> str:
        return f"TypedNode({self.litheral!r})"

    def __hash__(self) -> int:
        return hash(self.litheral)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, TypedNode):
            return NotImplemented
        return self.litheral == other.litheral
