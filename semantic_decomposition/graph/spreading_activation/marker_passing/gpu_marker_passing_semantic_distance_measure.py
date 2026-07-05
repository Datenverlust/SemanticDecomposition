from __future__ import annotations

from typing import List, Optional, TYPE_CHECKING

from ....i_concept import IConcept
from ....distance.semantic_distance_measure import SemanticDistanceMeasureInterface
from .gpu_double_marker_passing import GpuDoubleMarkerPassing

if TYPE_CHECKING:
    from ....concept import Concept
    from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig


class GpuMarkerPassingSemanticDistanceMeasure(SemanticDistanceMeasureInterface):
    """
    GPU-accelerated semantic similarity via spreading activation.

    Replaces the CPU MarkerPassingSemanticDistanceMeasure with a GPU SpMM
    loop.  The similarity formula is preserved:

        score = (||x_c1||₁ + ||x_c2||₁) / (2 × start_activation)

    where x_c1 and x_c2 are the final activation vectors seeded from each
    input concept.  This is the vectorised equivalent of the CPU formula
    which sums abs(activation) across all nodes.

    Single-pair usage
    -----------------
        measure = GpuMarkerPassingSemanticDistanceMeasure()
        score = measure.compare_concepts(cat, dog)

    Batch usage — evaluate many pairs in one GPU call
    -------------------------------------------------
    The key GPU advantage: all pairs share the same graph and are evaluated
    in a single [N x 2B] SpMM per pulse, where B is the number of pairs.

        measure = GpuMarkerPassingSemanticDistanceMeasure()
        scores = measure.compare_many([(cat, dog), (car, ship), (run, walk)])
        # scores is a list of floats, one per pair
    """

    def __init__(
        self,
        config: Optional["MarkerPassingConfig"] = None,
        device: Optional[str] = None,
    ) -> None:
        from ....entities.spreading_activation.marker_passing_config import MarkerPassingConfig
        self._config = config or MarkerPassingConfig()
        self._device = device

    def compare_concepts(self, c1: IConcept, c2: IConcept) -> float:
        from ....concept import Concept
        if not isinstance(c1, Concept) or not isinstance(c2, Concept):
            raise TypeError("Both arguments must be Concept instances")
        scores = self.compare_many([(c1, c2)])
        return scores[0]

    def compare_many(
        self,
        pairs: List[tuple["Concept", "Concept"]],
    ) -> List[float]:
        """
        Evaluate a batch of concept pairs in a single GPU pass.

        All concepts across all pairs are merged into one graph.
        Each pair contributes two columns to the [N x 2B] activation matrix.

        Parameters
        ----------
        pairs : list of (Concept, Concept)

        Returns
        -------
        list of float — one similarity score per pair, same order as input.
        """
        if not pairs:
            return []

        # Gather all concepts into one graph
        all_concepts: List["Concept"] = []
        seen = set()
        for c1, c2 in pairs:
            for c in (c1, c2):
                if id(c) not in seen:
                    all_concepts.append(c)
                    seen.add(id(c))

        algo = GpuDoubleMarkerPassing(config=self._config, device=self._device)
        algo.fill_nodes(all_concepts)

        start = self._config.start_activation

        # Each pair → two seed vectors (one per concept)
        seed_lists = [[c] for pair in pairs for c in pair]
        results = algo.execute_batched(seed_lists, start_activation=start)

        # Pair up results and compute similarity
        scores: List[float] = []
        for i in range(len(pairs)):
            acts_c1 = results[2 * i]
            acts_c2 = results[2 * i + 1]
            total = sum(abs(v) for v in acts_c1.values()) \
                  + sum(abs(v) for v in acts_c2.values())
            denominator = 2.0 * start
            scores.append(total / denominator if denominator else 0.0)

        return scores
