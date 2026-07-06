"""Advanced batch processing for GPU-accelerated decomposition.

This module provides efficient batch processing strategies that adapt to
available GPU memory and optimize throughput for semantic decomposition.
"""
from __future__ import annotations

import logging
import time
from typing import Callable, List, Optional, Tuple

from .concept import Concept
from .gpu_memory_manager import GPUMemoryManager, get_memory_manager
from .statistics_cache import StatisticsCache, get_statistics_cache

logger = logging.getLogger(__name__)


class BatchProcessor:
    """Manages intelligent batch processing for decomposition.

    Provides:
    - Adaptive batch sizing based on available GPU memory
    - Progress tracking and statistics
    - Streaming processing for large datasets
    - Early termination support
    """

    def __init__(
        self,
        memory_manager: Optional[GPUMemoryManager] = None,
        statistics_cache: Optional[StatisticsCache] = None,
        enable_streaming: bool = True,
    ):
        """Initialize the batch processor.

        Parameters
        ----------
        memory_manager : GPUMemoryManager, optional
            Memory manager for adaptive sizing. Uses global if None.
        statistics_cache : StatisticsCache, optional
            Cache for statistics. Uses global if None.
        enable_streaming : bool, optional
            Enable streaming mode for large datasets. Default is True.
        """
        self.memory_manager = (
            memory_manager or get_memory_manager()
        )
        self.statistics_cache = (
            statistics_cache or get_statistics_cache()
        )
        self.enable_streaming = enable_streaming

        # Statistics
        self.total_processed: int = 0
        self.total_time: float = 0.0
        self.batches_processed: int = 0

    def process_batch(
        self,
        items: List[Concept],
        process_fn: Callable[[Concept], Concept],
        estimated_memory_per_item: int = 1024,
        show_progress: bool = True,
    ) -> Tuple[List[Concept], float]:
        """Process a batch of items with adaptive sizing.

        Parameters
        ----------
        items : list[Concept]
            Items to process.
        process_fn : callable
            Function to apply to each item.
        estimated_memory_per_item : int, optional
            Estimated memory per item in bytes. Default is 1024.
        show_progress : bool, optional
            Show progress information. Default is True.

        Returns
        -------
        tuple[list[Concept], float]
            (processed_items, elapsed_time)
        """
        start_time = time.perf_counter()

        if not items:
            return [], 0.0

        # Calculate optimal batch size
        optimal_batch_size = (
            self.memory_manager.calculate_optimal_batch_size(
                estimated_memory_per_item
            )
        )

        if show_progress:
            logger.debug(
                f"Processing {len(items)} items with "
                f"batch size {optimal_batch_size}"
            )

        results = []
        processed_count = 0

        # Process in adaptive batches
        for batch_start in range(0, len(items), optimal_batch_size):
            batch_end = min(batch_start + optimal_batch_size, len(items))
            batch = items[batch_start:batch_end]

            # Check cache for batch items
            cached_results = []
            uncached_items = []
            uncached_indices = []

            for i, item in enumerate(batch):
                cache_key = f"concept_{item.id}"
                cached = self.statistics_cache.get(cache_key)
                if cached is not None:
                    cached_results.append((i, cached))
                else:
                    uncached_items.append(item)
                    uncached_indices.append(i)

            # Process uncached items
            if uncached_items:
                batch_results = [
                    process_fn(item) for item in uncached_items
                ]

                # Cache results
                for item, result in zip(uncached_items, batch_results):
                    cache_key = f"concept_{item.id}"
                    self.statistics_cache.put(cache_key, result)

                # Merge with cached results
                for i, result in zip(uncached_indices, batch_results):
                    cached_results.append((i, result))

            # Sort by original order and extract results
            cached_results.sort(key=lambda x: x[0])
            for _, result in cached_results:
                results.append(result)

            processed_count += len(batch)
            self.total_processed += len(batch)

            if show_progress and processed_count % (
                optimal_batch_size * 10
            ) == 0:
                logger.debug(
                    f"Processed {processed_count}/{len(items)} items"
                )

        elapsed = time.perf_counter() - start_time
        self.total_time += elapsed
        self.batches_processed += 1

        if show_progress:
            throughput = len(items) / elapsed if elapsed > 0 else 0
            logger.debug(
                f"Batch processed: {len(items)} items in {elapsed:.2f}s "
                f"({throughput:.0f} items/sec)"
            )

        return results, elapsed

    def process_stream(
        self,
        items: List[Concept],
        process_fn: Callable[[Concept], Concept],
        batch_size: int = 32,
        max_items: Optional[int] = None,
        should_continue: Optional[Callable[[], bool]] = None,
    ) -> Tuple[List[Concept], float]:
        """Process items in streaming mode with early termination support.

        Parameters
        ----------
        items : list[Concept]
            Items to process.
        process_fn : callable
            Function to apply to each item.
        batch_size : int, optional
            Batch size for processing. Default is 32.
        max_items : int, optional
            Maximum items to process. Processes all if None.
        should_continue : callable, optional
            Function returning False to stop processing early.

        Returns
        -------
        tuple[list[Concept], float]
            (processed_items, elapsed_time)
        """
        start_time = time.perf_counter()
        results = []
        processed = 0

        for batch_start in range(0, len(items), batch_size):
            # Check early termination
            if should_continue is not None and not should_continue():
                logger.debug(
                    f"Early termination at {processed} items"
                )
                break

            # Check max items limit
            if max_items is not None and processed >= max_items:
                break

            batch_end = min(batch_start + batch_size, len(items))
            if max_items is not None:
                batch_end = min(batch_end, batch_start + max_items - processed)

            batch = items[batch_start:batch_end]

            # Process batch
            for item in batch:
                result = process_fn(item)
                results.append(result)
                processed += 1

            # Clear cache periodically to manage memory
            if processed % (batch_size * 100) == 0:
                self.memory_manager.clear_cache()

        elapsed = time.perf_counter() - start_time
        self.total_time += elapsed

        return results, elapsed

    def get_statistics(self) -> dict:
        """Get processing statistics.

        Returns
        -------
        dict
            Dictionary containing processing statistics.
        """
        avg_time_per_batch = (
            self.total_time / self.batches_processed
            if self.batches_processed > 0
            else 0.0
        )
        throughput = (
            self.total_processed / self.total_time
            if self.total_time > 0
            else 0.0
        )

        return {
            "total_processed": self.total_processed,
            "batches_processed": self.batches_processed,
            "total_time_seconds": self.total_time,
            "avg_batch_time_seconds": avg_time_per_batch,
            "throughput_items_per_sec": throughput,
            "cache_stats": self.statistics_cache.get_stats(),
            "memory_percent": self.memory_manager.get_memory_percent(),
        }

    def reset_statistics(self) -> None:
        """Reset processing statistics."""
        self.total_processed = 0
        self.total_time = 0.0
        self.batches_processed = 0


# Global batch processor instance
_batch_processor: Optional[BatchProcessor] = None


def get_batch_processor() -> BatchProcessor:
    """Get or create the global batch processor.

    Returns
    -------
    BatchProcessor
        The global batch processor.
    """
    global _batch_processor
    if _batch_processor is None:
        _batch_processor = BatchProcessor()
    return _batch_processor
