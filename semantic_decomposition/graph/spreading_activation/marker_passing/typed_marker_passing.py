from __future__ import annotations
from typing import Collection, Dict, List, Optional, TYPE_CHECKING

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
    from ....entities.spreading_activation.typed_marker_passing_config import (
        TypedMarkerPassingConfig,
    )


class _AllActiveSelect(SelectFiringNodesFunction):
    def compute(self, active_nodes: Collection[Node]) -> Collection[Node]:
        return [n for n in active_nodes if n.check_thresholds()]


class _TypedIn(InFunction):
    def compute(self, input_steps: Collection[SpreadingStep], node: Node) -> None:
        if hasattr(node, "in_function"):
            node.in_function(input_steps)


class _TypedOut(OutFunction):
    def compute(self, node: Node) -> Collection[SpreadingStep]:
        if hasattr(node, "out_function"):
            return node.out_function()
        return []


class TypedMarkerPassing(SpreadingAlgorithm):
    """Marker-passing algorithm using TypedNode nodes and TypedMarkerPassingConfig."""

    def __init__(
        self,
        config: Optional["TypedMarkerPassingConfig"] = None,
    ) -> None:
        super().__init__()
        from ....entities.spreading_activation.typed_marker_passing_config import (
            TypedMarkerPassingConfig,
        )
        self._config = config or TypedMarkerPassingConfig()
        self._graph: SemanticNet = SemanticNet()
        self._concept_to_node: Dict["Concept", Node] = {}

        termination = CountTerminationCondition(self._config.termination_pulse_count)
        self.termination_condition = termination
        self.preprocessing_steps = [termination]
        self.in_function = _TypedIn()
        self.out_function = _TypedOut()
        self.select_firing_nodes = _AllActiveSelect()

    def set_initial_concepts(self, concepts: List["Concept"]) -> None:
        for concept in concepts:
            self._add_concept_recursively(concept, self._config.decomposition_depth)
        self._active_nodes = list(self._concept_to_node.values())

    def _add_concept_recursively(self, concept: "Concept", depth: int) -> Node:
        if concept in self._concept_to_node:
            return self._concept_to_node[concept]
        from ....entities.nodes.typed_node import TypedNode
        node = TypedNode(litheral=concept.litheral, concept=concept)
        self._concept_to_node[concept] = node
        self._graph.add_vertex(node)
        if depth > 0:
            self._connect_related(concept, concept.synonyms, self._config.synonym_weight, depth)
            self._connect_related(concept, concept.antonyms, self._config.antonym_weight, depth)
            self._connect_related(concept, concept.hypernyms, self._config.hypernym_weight, depth)
            self._connect_related(concept, concept.hyponyms, self._config.hyponym_weight, depth)
            self._connect_related(concept, concept.meronyms, self._config.meronym_weight, depth)
        return node

    def _connect_related(
        self,
        source_concept: "Concept",
        related: List["Concept"],
        weight: float,
        depth: int,
    ) -> None:
        from ....entities.links.weighted_link import WeightedLink
        source_node = self._concept_to_node[source_concept]
        for target_concept in related:
            target_node = self._add_concept_recursively(target_concept, depth - 1)
            link = WeightedLink(source=source_node, target=target_node, weight=weight)
            if link not in source_node.get_links():
                source_node.get_links().append(link)

    def get_definition_of_initial_concepts(self) -> List["Concept"]:
        result: List["Concept"] = []
        for concept in self._concept_to_node:
            for definition in concept.definitions:
                if hasattr(definition, "concepts"):
                    result.extend(definition.concepts)
        return result

    def start(self, initial_concepts: List["Concept"]) -> None:
        from ....entities.markers.typed_marker import TypedMarker
        for concept in initial_concepts:
            node = self._concept_to_node.get(concept)
            if node is not None:
                marker = TypedMarker(
                    value=self._config.initial_marker_amount, origin=concept, marker_type=0
                )
                node.add_marker(marker)
                from ....entities.nodes.typed_node import TypedNode
                if isinstance(node, TypedNode):
                    node.activation[concept] = marker
        self.execute()
