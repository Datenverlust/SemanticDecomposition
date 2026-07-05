from __future__ import annotations
from typing import Collection, Dict, List, Optional, TYPE_CHECKING

from ...marker_passing.spreading_algorithm import SpreadingAlgorithm
from ...marker_passing.spreading_step import SpreadingStep
from ...marker_passing.in_function import InFunction
from ...marker_passing.out_function import OutFunction
from ...marker_passing.select_firing_nodes_function import SelectFiringNodesFunction
from ...marker_passing.node import Node
from ..semantic_net import SemanticNet
from .count_termination_condition import CountTerminationCondition

if TYPE_CHECKING:
    from ...concept import Concept
    from ...entities.spreading_activation.marker_passing_config import MarkerPassingConfig


class _AllNodesSelect(SelectFiringNodesFunction):
    def compute(self, active_nodes: Collection[Node]) -> Collection[Node]:
        return active_nodes


class _DoubleIn(InFunction):
    def compute(self, input_steps: Collection[SpreadingStep], node: Node) -> None:
        if hasattr(node, "in_function"):
            node.in_function(input_steps)


class _DoubleOut(OutFunction):
    def compute(self, node: Node) -> Collection[SpreadingStep]:
        if hasattr(node, "out_function"):
            return node.out_function()
        return []


class DoubleSpreadingActivation(SpreadingAlgorithm):
    """Spreading activation over a SemanticNet using DoubleNode nodes."""

    def __init__(
        self,
        graph: SemanticNet,
        config: Optional["MarkerPassingConfig"] = None,
    ) -> None:
        super().__init__()
        self._graph = graph
        self._config = config
        termination = CountTerminationCondition(
            config.termination_pulse_count if config else 80
        )
        self.termination_condition = termination
        self.preprocessing_steps = [termination]
        self.in_function = _DoubleIn()
        self.out_function = _DoubleOut()
        self.select_firing_nodes = _AllNodesSelect()

    def do_initial_marking(self, concepts: List["Concept"]) -> None:
        from ...entities.nodes.double_node import DoubleNode
        from ...entities.markers.double_marker import DoubleMarker
        from ...entities.spreading_activation.marker_passing_config import MarkerPassingConfig

        start = (
            self._config.start_activation
            if self._config
            else MarkerPassingConfig.start_activation
        )
        for node in self._active_nodes:
            if isinstance(node, DoubleNode) and node.concept in concepts:
                node.activation = start
                node.add_marker(DoubleMarker(start))
