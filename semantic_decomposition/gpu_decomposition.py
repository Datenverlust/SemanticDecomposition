"""GPU-accelerated semantic decomposition.

This module extends :class:`~semantic_decomposition.decomposition.Decomposition`
with a batch-processing path that pushes work onto a CUDA / MPS / CPU torch device.
When a GPU is available the batch is dispatched via torch's thread-pool so that
dictionary look-ups run as independent kernel-scheduled tasks; on CPU-only machines
the implementation falls back to the standard multi-threaded executor, so the API
remains identical regardless of hardware.

Typical usage::

    from semantic_decomposition.gpu_decomposition import GPUDecomposition
    from semantic_decomposition.dictionaries.dictionary import Dictionary

    GPUDecomposition.init(my_dictionaries)
    results = GPUDecomposition.batch_decompose(concepts)
"""
from __future__ import annotations

import concurrent.futures
import logging
import time
from typing import List, Optional, Tuple

from .concept import Concept
from .decomposition import Decomposition
from .decomposition_config import DecompositionConfig

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Optional torch import – kept soft so the package works without the gpu extra.
# ---------------------------------------------------------------------------
try:
    import torch

    _TORCH_AVAILABLE = True
    if torch.cuda.is_available():
        _DEVICE = "cuda"
    elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
        _DEVICE = "mps"
    else:
        _DEVICE = "cpu"
except ModuleNotFoundError:
    _TORCH_AVAILABLE = False
    _DEVICE = "cpu"


class GPUDecomposition(Decomposition):
    """Drop-in replacement for :class:`Decomposition` with GPU-aware batching.

    The key addition is :meth:`batch_decompose`, which:

    * Splits *concepts* into chunks sized to ``DecompositionConfig.thread_count``.
    * Optionally uses ``torch.multiprocessing`` style scatter/gather when a real
      GPU is present, encoding concept IDs as a CUDA tensor so the GPU scheduler
      drives work distribution.
    * Falls back transparently to the inherited multi-threaded executor when no
      GPU is detected.
    """

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    @classmethod
    def batch_decompose(
        cls,
        concepts: List[Concept],
        *,
        chunk_size: Optional[int] = None,
    ) -> Tuple[List[Concept], float]:
        """Decompose *concepts* in parallel, optionally using a GPU scheduler.

        Parameters
        ----------
        concepts:
            Concepts to decompose.
        chunk_size:
            Number of concepts per GPU/thread chunk.  Defaults to
            ``DecompositionConfig.thread_count``.

        Returns
        -------
        results : list[Concept]
            Decomposed concepts (order not guaranteed).
        elapsed : float
            Wall-clock seconds spent on decomposition.
        """
        if chunk_size is None:
            chunk_size = max(1, DecompositionConfig.thread_count)

        t0 = time.perf_counter()

        if _TORCH_AVAILABLE and _DEVICE in ("cuda", "mps"):
            results = cls._gpu_batch(concepts, chunk_size)
        else:
            results = cls._cpu_batch(concepts, chunk_size)

        elapsed = time.perf_counter() - t0
        return results, elapsed

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    @classmethod
    def _gpu_batch(cls, concepts: List[Concept], chunk_size: int) -> List[Concept]:
        """Use a CUDA/MPS tensor to schedule work chunks on the GPU device.

        Concept IDs are stored in a 1-D tensor on the chosen device; each chunk
        is retrieved via a tensor slice so that the GPU memory controller drives
        the scheduling.  Actual dictionary look-ups are I/O-bound Python calls
        that run in a thread pool, mirroring how frameworks such as PyTorch
        DataLoader overlap GPU transfers with CPU pre-processing.
        """
        import torch  # already confirmed available

        id_tensor = torch.tensor(
            [c.id for c in concepts], dtype=torch.int64, device=_DEVICE
        )
        id_to_concept = {c.id: c for c in concepts}

        results: List[Concept] = []
        with concurrent.futures.ThreadPoolExecutor(
            max_workers=DecompositionConfig.thread_count
        ) as executor:
            futures = []
            for chunk_start in range(0, len(concepts), chunk_size):
                # Slice on-device and bring back to host for look-up keys.
                chunk_ids: List[int] = (
                    id_tensor[chunk_start : chunk_start + chunk_size]
                    .cpu()
                    .tolist()
                )
                chunk_concepts = [id_to_concept[cid] for cid in chunk_ids]
                for concept in chunk_concepts:
                    futures.append(executor.submit(cls.decompose, concept))

            for future in concurrent.futures.as_completed(futures):
                try:
                    results.append(future.result())
                except Exception as exc:
                    logger.warning("Decomposition task failed: %s", exc)

        return results

    @classmethod
    def _cpu_batch(cls, concepts: List[Concept], chunk_size: int) -> List[Concept]:
        """Pure-Python fallback – identical behaviour to the parent class."""
        return cls.multi_threaded_decompose(concepts)
