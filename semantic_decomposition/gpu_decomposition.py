"""GPU-accelerated semantic decomposition with corpus statistics.

This module extends :class:`~semantic_decomposition.decomposition.Decomposition`
with advanced batch-processing that leverages CUDA / MPS / CPU torch devices.

Features:
- Adaptive chunk sizing based on available GPU memory
- GPU memory management and monitoring
- High-performance statistics caching
- Support for corpus linguistic statistics
- Progress tracking and statistics

Typical usage::

    from semantic_decomposition.gpu_decomposition import GPUDecomposition
    from semantic_decomposition.dictionaries.dictionary import Dictionary

    GPUDecomposition.init(my_dictionaries)
    results, elapsed = GPUDecomposition.batch_decompose(concepts)
    stats = GPUDecomposition.get_statistics()
"""
from __future__ import annotations

import concurrent.futures
import logging
import time
from typing import Dict, List, Optional, Tuple

from .batch_processor import BatchProcessor, get_batch_processor
from .concept import Concept
from .decomposition import Decomposition
from .decomposition_config import DecompositionConfig
from .gpu_memory_manager import GPUMemoryManager, get_memory_manager
from .statistics_cache import StatisticsCache, get_statistics_cache

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
    """Advanced GPU-aware decomposition with memory management and caching.

    Enhancements over base Decomposition:
    * Adaptive chunk sizing based on GPU memory availability
    * High-performance statistics caching with GPU support
    * GPU memory monitoring and management
    * Progress tracking and performance statistics
    * Streaming mode for processing large datasets
    * Early termination support

    The GPU device is automatically detected (CUDA > MPS > CPU).
    """

    # Class-level managers
    _memory_manager: Optional[GPUMemoryManager] = None
    _statistics_cache: Optional[StatisticsCache] = None
    _batch_processor: Optional[BatchProcessor] = None

    @classmethod
    def init(cls, dictionaries: List["Dictionary"]) -> None:
        """Initialize with dictionaries and GPU resources.

        Parameters
        ----------
        dictionaries : list[Dictionary]
            Dictionary backends for decomposition.
        """
        super().init(dictionaries)

        # Initialize GPU management resources
        cls._memory_manager = GPUMemoryManager(device=_DEVICE)
        cls._statistics_cache = StatisticsCache(
            max_size=10000,
            enable_gpu=_TORCH_AVAILABLE and _DEVICE in ("cuda", "mps"),
            device=_DEVICE,
        )
        cls._batch_processor = BatchProcessor(
            memory_manager=cls._memory_manager,
            statistics_cache=cls._statistics_cache,
            enable_streaming=True,
        )

        logger.debug(
            f"GPUDecomposition initialized with device={_DEVICE}, "
            f"torch_available={_TORCH_AVAILABLE}"
        )

    # ------------------------------------------------------------------
    # Public API
    # ------------------------------------------------------------------

    @classmethod
    def batch_decompose(
        cls,
        concepts: List[Concept],
        *,
        chunk_size: Optional[int] = None,
        enable_streaming: bool = False,
        show_progress: bool = True,
    ) -> Tuple[List[Concept], float]:
        """Decompose concepts in parallel with adaptive batching.

        Parameters
        ----------
        concepts : list[Concept]
            Concepts to decompose.
        chunk_size : int, optional
            Override chunk size. Uses adaptive sizing if None.
        enable_streaming : bool, optional
            Use streaming mode for large datasets. Default is False.
        show_progress : bool, optional
            Show progress information. Default is True.

        Returns
        -------
        tuple[list[Concept], float]
            (decomposed_concepts, elapsed_seconds)
        """
        if not concepts:
            return [], 0.0

        if cls._batch_processor is None:
            # Fallback if not initialized
            return cls.multi_threaded_decompose(concepts)

        t0 = time.perf_counter()

        if enable_streaming:
            results, _ = cls._batch_processor.process_stream(
                concepts,
                cls.decompose,
                batch_size=chunk_size or 32,
                should_continue=None,
            )
        else:
            results, _ = cls._batch_processor.process_batch(
                concepts,
                cls.decompose,
                estimated_memory_per_item=1024,
                show_progress=show_progress,
            )

        elapsed = time.perf_counter() - t0

        if show_progress:
            logger.debug(
                f"Batch decomposition completed: {len(results)} concepts "
                f"in {elapsed:.2f}s ({len(results) / elapsed:.0f} items/sec)"
            )

        return results, elapsed

    @classmethod
    def stream_decompose(
        cls,
        concepts: List[Concept],
        *,
        batch_size: int = 32,
        max_items: Optional[int] = None,
        show_progress: bool = True,
    ) -> Tuple[List[Concept], float]:
        """Process concepts in streaming mode with limited memory footprint.

        Parameters
        ----------
        concepts : list[Concept]
            Concepts to decompose.
        batch_size : int, optional
            Batch size for streaming. Default is 32.
        max_items : int, optional
            Maximum items to process. All if None.
        show_progress : bool, optional
            Show progress information. Default is True.

        Returns
        -------
        tuple[list[Concept], float]
            (decomposed_concepts, elapsed_seconds)
        """
        if cls._batch_processor is None:
            return cls.multi_threaded_decompose(concepts[:max_items])

        t0 = time.perf_counter()

        results, _ = cls._batch_processor.process_stream(
            concepts,
            cls.decompose,
            batch_size=batch_size,
            max_items=max_items,
            should_continue=None,
        )

        elapsed = time.perf_counter() - t0

        if show_progress:
            logger.debug(
                f"Stream decomposition completed: {len(results)} concepts "
                f"in {elapsed:.2f}s"
            )

        return results, elapsed

    @classmethod
    def get_statistics(cls) -> Dict:
        """Get decomposition statistics.

        Returns
        -------
        dict
            Dictionary containing performance and cache statistics.
        """
        if cls._batch_processor is None:
            return {}

        return cls._batch_processor.get_statistics()

    @classmethod
    def get_memory_stats(cls) -> Dict:
        """Get GPU memory statistics.

        Returns
        -------
        dict
            Dictionary containing memory usage information.
        """
        if cls._memory_manager is None:
            return {}

        used, total = cls._memory_manager.get_memory_usage()
        return {
            "device": _DEVICE,
            "memory_used_bytes": used,
            "memory_total_bytes": total,
            "memory_percent": cls._memory_manager.get_memory_percent(),
            "available_bytes": cls._memory_manager.get_available_memory(),
        }

    @classmethod
    def clear_cache(cls) -> None:
        """Clear statistics cache and GPU memory."""
        if cls._statistics_cache:
            cls._statistics_cache.clear()
        if cls._memory_manager:
            cls._memory_manager.clear_cache()
        logger.debug("Cleared caches")

    @classmethod
    def reset_statistics(cls) -> None:
        """Reset performance statistics."""
        if cls._batch_processor:
            cls._batch_processor.reset_statistics()
        logger.debug("Reset statistics")

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
