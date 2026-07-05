from __future__ import annotations
from collections import defaultdict
from typing import Collection, Dict, List, Optional, Tuple, TYPE_CHECKING

from ...marker_passing.node import Node
from ...marker_passing.spreading_step import SpreadingStep

if TYPE_CHECKING:
    from ...marker_passing.link import Link
    from ...marker_passing.marker import Marker
    from ...concept import Concept
    from ..spreading_activation.marker_passing_config import MarkerPassingConfig


class DoubleNodeWithMultipleThresholds(Node):
    """Node that tracks separate activation histories per originating concept."""

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
        self._config = config
        # Map[origin_concept -> Map[activation -> List[arriving_concept]]]
        self.activation: Dict["Concept", Dict[float, List["Concept"]]] = defaultdict(
            lambda: defaultdict(list)
        )
        self.activation_history: Dict["Concept", List[float]] = defaultdict(list)
        self._threshold_per_concept: Dict["Concept", float] = {}

    def get_threshold_for(self, concept: "Concept") -> float:
        from ..spreading_activation.marker_passing_config import MarkerPassingConfig
        return self._threshold_per_concept.get(
            concept,
            self._config.threshold if self._config else MarkerPassingConfig.threshold,
        )

    def get_links(self) -> List["Link"]:
        return self._links

    def get_markers(self) -> List["Marker"]:
        return self._markers

    def check_thresholds(self, marker_classes: object = None) -> bool:
        for origin, act_map in self.activation.items():
            for act_val in act_map:
                if act_val >= self.get_threshold_for(origin):
                    return True
        return False

    def in_function(self, input_steps: Collection[SpreadingStep]) -> None:
        from ..markers.double_marker_with_origin import DoubleMarkerWithOrigin
        for step in input_steps:
            for marker in step.markings:
                if isinstance(marker, DoubleMarkerWithOrigin) and marker.origin is not None:
                    self.activation[marker.origin][marker.activation].append(marker.origin)
                    self.activation_history[marker.origin].append(marker.activation)

    def out_function(self) -> Collection[SpreadingStep]:
        from ..markers.double_marker_with_origin import DoubleMarkerWithOrigin
        steps: List[SpreadingStep] = []
        for link in self._links:
            from ..links.weighted_link import WeightedLink
            if not isinstance(link, WeightedLink):
                continue
            for origin, act_map in self.activation.items():
                for act_val in act_map:
                    new_act = act_val * link.weight
                    marker = DoubleMarkerWithOrigin(
                        activation=new_act, origin=origin
                    )
                    step = SpreadingStep()
                    step.link = link
                    step.in_direction = True
                    step.markings = [marker]
                    steps.append(step)
        return steps

    def __repr__(self) -> str:
        return f"DoubleNodeWithMultipleThresholds({self.litheral!r})"

    def __hash__(self) -> int:
        return hash(self.litheral)

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, DoubleNodeWithMultipleThresholds):
            return NotImplemented
        return self.litheral == other.litheral
