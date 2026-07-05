from __future__ import annotations
from typing import Collection, Dict, List, Optional, Set, TYPE_CHECKING

from ....marker_passing.spreading_algorithm import SpreadingAlgorithm
from ....marker_passing.spreading_step import SpreadingStep
from ....marker_passing.in_function import InFunction
from ....marker_passing.out_function import OutFunction
from ....marker_passing.select_firing_nodes_function import SelectFiringNodesFunction
from ....marker_passing.node import Node
from ...semantic_net import SemanticNet
from ..count_termination_condition import CountTerminationCondition

if TYPE_CHECKING:
    from ....concept import Concept
    from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig


class _FireThresholdNodes(SelectFiringNodesFunction):
    def __init__(self, config: Optional["MarkerPassingConfig"]) -> None:
        self._config = config

    def compute(self, active_nodes: Collection[Node]) -> Collection[Node]:
        from ....entities.nodes.double_node_with_multiple_thresholds import (
            DoubleNodeWithMultipleThresholds,
        )
        result = []
        for node in active_nodes:
            if isinstance(node, DoubleNodeWithMultipleThresholds):
                if node.check_thresholds():
                    result.append(node)
        return result


class _DoubleMultiIn(InFunction):
    def compute(self, input_steps: Collection[SpreadingStep], node: Node) -> None:
        if hasattr(node, "in_function"):
            node.in_function(input_steps)


class _DoubleMultiOut(OutFunction):
    def compute(self, node: Node) -> Collection[SpreadingStep]:
        if hasattr(node, "out_function"):
            return node.out_function()
        return []


class DoubleMarkerPassing(SpreadingAlgorithm):
    """
    Marker-passing algorithm using DoubleNodeWithMultipleThresholds nodes.

    Replaces the JGraphT-backed Java implementation; uses SemanticNet instead.
    """

    def __init__(
        self,
        config: Optional["MarkerPassingConfig"] = None,
    ) -> None:
        super().__init__()
        from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig
        self._config = config or MarkerPassingConfig()
        self._graph: SemanticNet = SemanticNet()
        # Bidirectional map: Concept <-> Node
        self._concept_to_node: Dict["Concept", Node] = {}
        self._node_to_concept: Dict[Node, "Concept"] = {}

        termination = CountTerminationCondition(self._config.termination_pulse_count)
        self.termination_condition = termination
        self.preprocessing_steps = [termination]
        self.in_function = _DoubleMultiIn()
        self.out_function = _DoubleMultiOut()
        self.select_firing_nodes = _FireThresholdNodes(self._config)

    def fill_nodes(self, concepts: List["Concept"]) -> None:
        for concept in concepts:
            self._add_concept_recursively(concept, self._config.decomposition_depth)
        self._active_nodes = list(self._concept_to_node.values())

    def _add_concept_recursively(self, concept: "Concept", depth: int) -> Node:
        if concept in self._concept_to_node:
            return self._concept_to_node[concept]

        from ....entities.nodes.double_node_with_multiple_thresholds import (
            DoubleNodeWithMultipleThresholds,
        )
        node = DoubleNodeWithMultipleThresholds(
            litheral=concept.litheral, concept=concept, config=self._config
        )
        self._concept_to_node[concept] = node
        self._node_to_concept[node] = concept
        self._graph.add_vertex(node)

        if depth <= 0:
            return node

        self._connect_related(concept, concept.synonyms, "SynonymLink", depth)
        self._connect_related(concept, concept.antonyms, "AntonymLink", depth)
        self._connect_related(concept, concept.hypernyms, "HypernymLink", depth)
        self._connect_related(concept, concept.hyponyms, "HyponymLink", depth)
        self._connect_related(concept, concept.meronyms, "WeightedLink", depth)
        for definition in concept.definitions:
            if hasattr(definition, "concepts"):
                self._connect_related(concept, definition.concepts, "DefinitionLink", depth)
        return node

    def _connect_related(
        self,
        source_concept: "Concept",
        related: List["Concept"],
        link_class: str,
        depth: int,
    ) -> None:
        from ....entities.links import (
            SynonymLink, AntonymLink, HypernymLink, HyponymLink, DefinitionLink, WeightedLink,
        )
        link_map = {
            "SynonymLink": SynonymLink,
            "AntonymLink": AntonymLink,
            "HypernymLink": HypernymLink,
            "HyponymLink": HyponymLink,
            "DefinitionLink": DefinitionLink,
            "WeightedLink": WeightedLink,
        }
        LinkClass = link_map.get(link_class, WeightedLink)
        source_node = self._concept_to_node[source_concept]
        for target_concept in related:
            target_node = self._add_concept_recursively(target_concept, depth - 1)
            link = LinkClass(source=source_node, target=target_node, config=self._config)
            if link not in source_node.get_links():
                source_node.get_links().append(link)

    @staticmethod
    def do_initial_marking(
        nodes: Dict["Concept", Node],
        concepts: List["Concept"],
        start_activation: float,
    ) -> None:
        from ....entities.markers.double_marker_with_origin import DoubleMarkerWithOrigin
        for concept in concepts:
            node = nodes.get(concept)
            if node is not None:
                marker = DoubleMarkerWithOrigin(
                    activation=start_activation, origin=concept
                )
                node.add_marker(marker)
                from ....entities.nodes.double_node_with_multiple_thresholds import (
                    DoubleNodeWithMultipleThresholds,
                )
                if isinstance(node, DoubleNodeWithMultipleThresholds):
                    node.activation[concept][start_activation].append(concept)
