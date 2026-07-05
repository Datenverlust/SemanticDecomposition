"""High-performance statistics cache with GPU support.

This module provides an LRU cache for corpus statistics with optional GPU
tensor caching for frequently accessed items.
"""
from __future__ import annotations

import logging
from collections import OrderedDict
from typing import Any, Dict, Optional, Tuple

logger = logging.getLogger(__name__)

try:
    import torch
    import numpy as np
    _TORCH_AVAILABLE = True
except ImportError:
    _TORCH_AVAILABLE = False


class StatisticsCache:
    """LRU cache for corpus statistics with GPU acceleration.

    This cache stores:
    - Frequency lookups
    - Co-occurrence counts
    - Similarity scores
    - Optionally, tensors for GPU operations

    The cache is thread-safe for reads but requires synchronization for writes.
    """

    def __init__(
        self,
        max_size: int = 10000,
        enable_gpu: bool = True,
        device: str = "cuda",
    ):
        """Initialize the statistics cache.

        Parameters
        ----------
        max_size : int, optional
            Maximum number of items to cache. Default is 10000.
        enable_gpu : bool, optional
            Whether to cache tensors on GPU. Default is True.
        device : str, optional
            Device for GPU tensors ('cuda' or 'mps'). Default is 'cuda'.
        """
        self.max_size = max_size
        self.enable_gpu = enable_gpu and _TORCH_AVAILABLE
        self.device = device

        # Main cache: key -> value
        self._cache: OrderedDict[str, Any] = OrderedDict()

        # Hit counter for eviction strategy
        self._hits: Dict[str, int] = {}

        # GPU tensor cache (optional)
        self._gpu_cache: Optional[Dict[str, torch.Tensor]] = (
            {} if self.enable_gpu else None
        )

        # Statistics
        self.hits: int = 0
        self.misses: int = 0

    def get(self, key: str) -> Any | None:
        """Get value from cache.

        Parameters
        ----------
        key : str
            Cache key.

        Returns
        -------
        Any | None
            The cached value, or None if not found.
        """
        if key in self._cache:
            # Move to end (most recently used)
            self._cache.move_to_end(key)
            self._hits[key] = self._hits.get(key, 0) + 1
            self.hits += 1
            return self._cache[key]

        self.misses += 1
        return None

    def put(self, key: str, value: Any) -> None:
        """Put value in cache.

        Parameters
        ----------
        key : str
            Cache key.
        value : Any
            Value to cache.
        """
        if key in self._cache:
            # Update existing entry and move to end
            self._cache[key] = value
            self._cache.move_to_end(key)
        else:
            # Add new entry and check size
            self._cache[key] = value
            self._hits[key] = 0

            if len(self._cache) > self.max_size:
                self._evict_least_used()

    def put_tensor(self, key: str, tensor: torch.Tensor) -> None:
        """Cache a tensor on GPU.

        Parameters
        ----------
        key : str
            Cache key.
        tensor : torch.Tensor
            Tensor to cache.
        """
        if not self.enable_gpu or self._gpu_cache is None:
            return

        try:
            self._gpu_cache[key] = tensor.to(self.device, non_blocking=True)
        except Exception as e:
            logger.warning(f"Failed to cache tensor: {e}")

    def get_tensor(self, key: str) -> torch.Tensor | None:
        """Get cached tensor from GPU.

        Parameters
        ----------
        key : str
            Cache key.

        Returns
        -------
        torch.Tensor | None
            The cached tensor, or None if not found.
        """
        if not self.enable_gpu or self._gpu_cache is None:
            return None

        return self._gpu_cache.get(key)

    def _evict_least_used(self) -> None:
        """Evict the least frequently used item.

        Uses hit count as the eviction metric. Falls back to FIFO if all
        items have the same hit count.
        """
        if not self._cache:
            return

        # Find item with lowest hit count
        min_hits = min(self._hits.values())
        for key in list(self._cache.keys()):
            if self._hits.get(key, 0) == min_hits:
                # Remove least recently used among items with same hits
                del self._cache[key]
                del self._hits[key]
                if self._gpu_cache and key in self._gpu_cache:
                    del self._gpu_cache[key]
                logger.debug(f"Evicted cache entry: {key}")
                break

    def clear(self) -> None:
        """Clear the entire cache."""
        self._cache.clear()
        self._hits.clear()
        if self._gpu_cache:
            self._gpu_cache.clear()
        self.hits = 0
        self.misses = 0

    def get_stats(self) -> Dict[str, Any]:
        """Get cache statistics.

        Returns
        -------
        dict[str, Any]
            Dictionary containing cache statistics.
        """
        total = self.hits + self.misses
        hit_rate = self.hits / total if total > 0 else 0.0

        return {
            "size": len(self._cache),
            "max_size": self.max_size,
            "hits": self.hits,
            "misses": self.misses,
            "hit_rate": hit_rate,
            "gpu_cache_size": len(self._gpu_cache) if self._gpu_cache else 0,
        }

    def batch_get(self, keys: list[str]) -> Tuple[list[Any], list[bool]]:
        """Get multiple values from cache.

        Parameters
        ----------
        keys : list[str]
            List of cache keys.

        Returns
        -------
        tuple[list[Any], list[bool]]
            Tuple of (values, found_flags) where values contains None for
            missing keys.
        """
        values = []
        found_flags = []

        for key in keys:
            value = self.get(key)
            values.append(value)
            found_flags.append(value is not None)

        return values, found_flags

    def batch_put(self, items: Dict[str, Any]) -> None:
        """Put multiple values in cache efficiently.

        Parameters
        ----------
        items : dict[str, Any]
            Dictionary of key-value pairs to cache.
        """
        for key, value in items.items():
            self.put(key, value)


# Global cache instance
_statistics_cache: Optional[StatisticsCache] = None


def get_statistics_cache(
    max_size: int = 10000,
    enable_gpu: bool = True,
    device: str = "cuda",
) -> StatisticsCache:
    """Get or create the global statistics cache.

    Parameters
    ----------
    max_size : int, optional
        Maximum cache size. Ignored if cache already initialized.
    enable_gpu : bool, optional
        Enable GPU caching. Ignored if cache already initialized.
    device : str, optional
        GPU device. Ignored if cache already initialized.

    Returns
    -------
    StatisticsCache
        The global statistics cache.
    """
    global _statistics_cache
    if _statistics_cache is None:
        _statistics_cache = StatisticsCache(
            max_size=max_size,
            enable_gpu=enable_gpu,
            device=device,
        )
    return _statistics_cache
