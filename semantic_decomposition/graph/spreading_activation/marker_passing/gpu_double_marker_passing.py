from __future__ import annotations

from typing import Dict, List, Optional, TYPE_CHECKING

from marker_passing.gpu import GraphSerializer, CudaSpreadingAlgorithm
from ...semantic_net import SemanticNet

if TYPE_CHECKING:
    from ....concept import Concept
    from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig
    from marker_passing.node import Node


class GpuDoubleMarkerPassing:
    """
    GPU-accelerated version of DoubleMarkerPassing.

    Builds the same Node/Link concept graph on the CPU (identical to the
    CPU DoubleMarkerPassing), then serialises it to a sparse weight matrix
    and delegates the pulse loop to CudaSpreadingAlgorithm (SpMM on GPU).

    The object-oriented Node/Link/Marker abstractions are used only during
    graph construction. The spreading loop itself runs entirely on the GPU
    as a repeated sparse matrix multiplication.

    Limitations
    -----------
    * Only scalar float activation is supported.  DoubleMarkerWithOrigin
      markers are approximated as scalar weights on the edges — origins are
      not tracked per-pulse on the GPU.
    * execute_batched() is the preferred entry point: it evaluates K seed
      concepts simultaneously in one [N x K] SpMM per pulse.
    """

    def __init__(
        self,
        config: Optional["MarkerPassingConfig"] = None,
        device: Optional[str] = None,
    ) -> None:
        from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig
        self._config = config or MarkerPassingConfig()
        self._device = device

        self._graph: SemanticNet = SemanticNet()
        self._concept_to_node: Dict["Concept", "Node"] = {}
        self._node_to_concept: Dict["Node", "Concept"] = {}

        # populated after fill_nodes()
        self._serializer: Optional[GraphSerializer] = None
        self._cuda_algo: Optional[CudaSpreadingAlgorithm] = None

    # ------------------------------------------------------------------
    # graph construction  (same logic as CPU DoubleMarkerPassing)
    # ------------------------------------------------------------------

    def fill_nodes(self, concepts: List["Concept"]) -> None:
        """Build the concept graph and serialise it to GPU tensors."""
        for concept in concepts:
            self._add_concept_recursively(concept, self._config.decomposition_depth)

        nodes = list(self._concept_to_node.values())
        self._serializer = GraphSerializer(nodes)
        self._cuda_algo = CudaSpreadingAlgorithm(
            self._serializer,
            threshold=self._config.threshold,
            max_pulses=self._config.termination_pulse_count,
            device=self._device,
        )

    def _add_concept_recursively(self, concept: "Concept", depth: int) -> "Node":
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

        self._connect(concept, concept.synonyms,  "SynonymLink",  depth)
        self._connect(concept, concept.antonyms,   "AntonymLink",  depth)
        self._connect(concept, concept.hypernyms,  "HypernymLink", depth)
        self._connect(concept, concept.hyponyms,   "HyponymLink",  depth)
        self._connect(concept, concept.meronyms,   "WeightedLink", depth)
        for definition in concept.definitions:
            if hasattr(definition, "concepts"):
                self._connect(concept, definition.concepts, "DefinitionLink", depth)
        return node

    def _connect(
        self,
        source_concept: "Concept",
        related: List["Concept"],
        link_class_name: str,
        depth: int,
    ) -> None:
        from ....entities.links import (
            SynonymLink, AntonymLink, HypernymLink,
            HyponymLink, DefinitionLink, WeightedLink,
        )
        link_map = {
            "SynonymLink":   SynonymLink,
            "AntonymLink":   AntonymLink,
            "HypernymLink":  HypernymLink,
            "HyponymLink":   HyponymLink,
            "DefinitionLink": DefinitionLink,
            "WeightedLink":  WeightedLink,
        }
        LinkClass = link_map.get(link_class_name, WeightedLink)
        source_node = self._concept_to_node[source_concept]
        for target_concept in related:
            target_node = self._add_concept_recursively(target_concept, depth - 1)
            link = LinkClass(source=source_node, target=target_node, config=self._config)
            if link not in source_node.get_links():
                source_node.get_links().append(link)

    # ------------------------------------------------------------------
    # execution
    # ------------------------------------------------------------------

    def execute(self, seed_concepts: List["Concept"], start_activation: float = 1.0) -> int:
        """
        Run spreading activation from one or more seed concepts.
        Returns the number of pulses executed.
        """
        self._require_built()
        activations = {
            self._concept_to_node[c]: start_activation
            for c in seed_concepts
            if c in self._concept_to_node
        }
        self._cuda_algo.reset()
        self._cuda_algo.set_activations(activations)
        return self._cuda_algo.execute()

    def execute_batched(
        self,
        seed_concept_lists: List[List["Concept"]],
        start_activation: float = 1.0,
    ) -> List[Dict["Concept", float]]:
        """
        Evaluate K independent seed-concept sets simultaneously.

        Each entry in seed_concept_lists seeds a separate activation vector.
        All K vectors are propagated in a single [N x K] SpMM per pulse.

        Returns one Dict[Concept, float] per input seed set.
        """
        self._require_built()
        act_dicts = [
            {
                self._concept_to_node[c]: start_activation
                for c in seeds
                if c in self._concept_to_node
            }
            for seeds in seed_concept_lists
        ]
        node_results = self._cuda_algo.execute_batched(act_dicts)
        return [
            {
                self._node_to_concept[node]: act
                for node, act in node_dict.items()
                if node in self._node_to_concept
            }
            for node_dict in node_results
        ]

    def get_activation(self, concept: "Concept") -> float:
        """Read scalar activation for a concept after execute()."""
        self._require_built()
        node = self._concept_to_node.get(concept)
        if node is None:
            return 0.0
        return self._cuda_algo.get_activation(node)

    def get_all_activations(self) -> Dict["Concept", float]:
        """Return activation for every concept after execute()."""
        self._require_built()
        node_acts = self._cuda_algo.get_all_activations()
        return {
            self._node_to_concept[node]: act
            for node, act in node_acts.items()
            if node in self._node_to_concept
        }

    @property
    def pulses_run(self) -> int:
        return self._cuda_algo.pulses_run if self._cuda_algo else 0

    @property
    def device(self) -> str:
        return self._cuda_algo.device if self._cuda_algo else "uninitialized"

    def _require_built(self) -> None:
        if self._cuda_algo is None:
            raise RuntimeError("Call fill_nodes() before execute()")
