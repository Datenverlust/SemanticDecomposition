from __future__ import annotations
from typing import List, TYPE_CHECKING

from ....i_concept import IConcept
from ....distance.semantic_distance_measure import SemanticDistanceMeasureInterface
from .double_marker_passing import DoubleMarkerPassing

if TYPE_CHECKING:
    from ....concept import Concept
    from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig


class MarkerPassingSemanticDistanceMeasure(SemanticDistanceMeasureInterface):
    """
    Computes semantic similarity between two words using DoubleMarkerPassing.

    Algorithm:
    1. Decompose both words into concept graphs.
    2. Merge the two graphs.
    3. Run DoubleMarkerPassing with the two initial concepts marked.
    4. Return summed double activation / (2 * start_activation).
    """

    def __init__(
        self,
        config: "MarkerPassingConfig | None" = None,
    ) -> None:
        from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig
        self._config = config or MarkerPassingConfig()

    def compare_concepts(self, c1: IConcept, c2: IConcept) -> float:
        from ....concept import Concept
        if not isinstance(c1, Concept) or not isinstance(c2, Concept):
            raise TypeError("Both arguments must be Concept instances")
        return self.pass_marker(c1, c2)

    def pass_marker(self, c1: "Concept", c2: "Concept") -> float:
        algorithm = DoubleMarkerPassing(config=self._config)
        algorithm.fill_nodes([c1, c2])

        start = self._config.start_activation
        DoubleMarkerPassing.do_initial_marking(
            algorithm._concept_to_node, [c1, c2], start
        )

        algorithm.execute()

        return self._compute_similarity(algorithm, start)

    def _compute_similarity(
        self,
        algorithm: DoubleMarkerPassing,
        start_activation: float,
    ) -> float:
        from ....entities.nodes.double_node_with_multiple_thresholds import (
            DoubleNodeWithMultipleThresholds,
        )
        total = 0.0
        for node in algorithm.active_nodes:
            if isinstance(node, DoubleNodeWithMultipleThresholds):
                for act_map in node.activation.values():
                    for act_val in act_map:
                        total += abs(act_val)
        denominator = 2.0 * start_activation
        return total / denominator if denominator != 0 else 0.0
