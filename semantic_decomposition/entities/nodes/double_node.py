from __future__ import annotations
from typing import Collection, List, Optional, TYPE_CHECKING

from ...marker_passing.node import Node
from ...marker_passing.spreading_step import SpreadingStep

if TYPE_CHECKING:
    from ...marker_passing.link import Link
    from ...marker_passing.marker import Marker
    from ..markers.double_marker import DoubleMarker
    from ...concept import Concept
    from ..spreading_activation.marker_passing_config import MarkerPassingConfig


class DoubleNode(Node):
    """Node accumulating a single double activation value for marker passing."""

    def __init__(
        self,
        litheral: str = "",
        concept: Optional["Concept"] = None,
        config: Optional["MarkerPassingConfig"] = None,
    ) -> None:
        self.litheral: str = litheral
        self.concept: Optional["Concept"] = concept
        self._links: List["Link"] = []
        self._markers: List["Marker"] = []
        self.threshold: float = config.threshold if config else 0.064
        self.activation: float = 0.0
        self._config = config

    def get_links(self) -> List["Link"]:
        return self._links

    def get_markers(self) -> List["Marker"]:
        return self._markers

    def check_thresholds(self, marker_classes: object = None) -> bool:
        return self.activation >= self.threshold

    def in_function(self, input_steps: Collection[SpreadingStep]) -> None:
        from ..markers.double_marker import DoubleMarker
        for step in input_steps:
            for marker in step.markings:
                if isinstance(marker, DoubleMarker):
                    self.activation += marker.activation
        self._markers.clear()
        if self.activation != 0.0:
            self._markers.append(DoubleMarker(self.activation))

    def out_function(self) -> Collection[SpreadingStep]:
        from ..markers.double_marker import DoubleMarker
        steps: List[SpreadingStep] = []
        for link in self._links:
            from ..links.weighted_link import WeightedLink
            if isinstance(link, WeightedLink):
                activation = self.activation * link.weight
                step = SpreadingStep()
                step.link = link
                step.in_direction = True
                step.markings = [DoubleMarker(activation)]
                steps.append(step)
        return steps

    def __repr__(self) -> str:
        return f"DoubleNode({self.litheral!r}, activation={self.activation})"

    def __hash__(self) -> int:
        return hash(self.litheral)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, DoubleNode):
            return NotImplemented
        return self.litheral == other.litheral
