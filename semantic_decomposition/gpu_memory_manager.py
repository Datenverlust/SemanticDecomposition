"""GPU memory management utilities for efficient batch processing.

This module provides tools to monitor and manage GPU memory usage during
semantic decomposition. It includes adaptive chunk sizing based on available
memory and graceful fallback to CPU when needed.
"""
from __future__ import annotations

import logging
from typing import Optional, Tuple

logger = logging.getLogger(__name__)

try:
    import torch
    _TORCH_AVAILABLE = True
except ImportError:
    _TORCH_AVAILABLE = False


class GPUMemoryConfig:
    """Configuration for GPU memory management."""

    # Maximum percentage of GPU memory to use (0-100)
    max_memory_percent: float = 80.0

    # Minimum batch size to maintain throughput
    min_batch_size: int = 1

    # Maximum batch size to try
    max_batch_size: int = 10000

    # Safety margin (percent) below max memory threshold
    safety_margin_percent: float = 5.0

    # Enable adaptive chunk sizing
    enable_adaptive_sizing: bool = True


class GPUMemoryManager:
    """Manages GPU memory allocation and monitoring.

    Provides utilities to:
    - Monitor current GPU memory usage
    - Calculate optimal batch sizes based on available memory
    - Handle out-of-memory scenarios gracefully
    - Track memory usage statistics
    """

    def __init__(self, device: Optional[str] = None):
        """Initialize the memory manager.

        Parameters
        ----------
        device : str, optional
            Device to manage ('cuda', 'mps', or 'cpu'). Auto-detects if None.
        """
        if not _TORCH_AVAILABLE:
            self.device = "cpu"
            self.available = False
            return

        if device is None:
            if torch.cuda.is_available():
                self.device = "cuda"
            elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
                self.device = "mps"
            else:
                self.device = "cpu"
        else:
            self.device = device

        self.available = self.device in ("cuda", "mps")

    def get_available_memory(self) -> int:
        """Get available GPU memory in bytes.

        Returns
        -------
        int
            Available memory in bytes. Returns sys.maxsize for CPU.
        """
        if not self.available or not _TORCH_AVAILABLE:
            return int(1e12)  # Assume ~1TB on CPU

        try:
            if self.device == "cuda":
                return torch.cuda.get_device_properties(0).total_memory - (
                    torch.cuda.memory_reserved(0)
                )
            else:
                return int(1e12)  # Fallback for MPS
        except Exception as e:
            logger.warning(f"Failed to get GPU memory: {e}")
            return int(1e12)

    def get_memory_usage(self) -> Tuple[int, int]:
        """Get current GPU memory usage.

        Returns
        -------
        tuple[int, int]
            (used_bytes, total_bytes)
        """
        if not self.available or not _TORCH_AVAILABLE:
            return (0, int(1e12))

        try:
            if self.device == "cuda":
                used = torch.cuda.memory_allocated(0)
                total = torch.cuda.get_device_properties(0).total_memory
                return (used, total)
            else:
                return (0, int(1e12))
        except Exception as e:
            logger.warning(f"Failed to get memory usage: {e}")
            return (0, int(1e12))

    def get_memory_percent(self) -> float:
        """Get percentage of GPU memory currently in use.

        Returns
        -------
        float
            Percentage (0-100). Returns 0 for CPU.
        """
        if not self.available:
            return 0.0

        used, total = self.get_memory_usage()
        if total == 0:
            return 0.0
        return 100.0 * used / total

    def calculate_optimal_batch_size(
        self,
        estimated_memory_per_item: int,
        current_batch_size: int = 32,
    ) -> int:
        """Calculate optimal batch size based on available GPU memory.

        Parameters
        ----------
        estimated_memory_per_item : int
            Estimated memory required per item in bytes.
        current_batch_size : int, optional
            Current batch size to adjust. Default is 32.

        Returns
        -------
        int
            Recommended batch size.
        """
        if not self.available or not _TORCH_AVAILABLE:
            return current_batch_size

        try:
            available_mem = self.get_available_memory()

            # Calculate how much memory we can use
            max_usable = int(
                available_mem
                * (GPUMemoryConfig.max_memory_percent / 100.0)
                * (
                    (100.0 - GPUMemoryConfig.safety_margin_percent)
                    / 100.0
                )
            )

            # Calculate optimal batch size
            if estimated_memory_per_item <= 0:
                return current_batch_size

            optimal_size = int(max_usable / estimated_memory_per_item)

            # Clamp to configured limits
            optimal_size = max(
                GPUMemoryConfig.min_batch_size,
                min(optimal_size, GPUMemoryConfig.max_batch_size),
            )

            logger.debug(
                f"Calculated optimal batch size: {optimal_size} "
                f"(available: {available_mem / 1e9:.1f} GB, "
                f"per-item: {estimated_memory_per_item / 1e6:.1f} MB)"
            )

            return optimal_size

        except Exception as e:
            logger.warning(f"Error calculating batch size: {e}")
            return current_batch_size

    def clear_cache(self) -> None:
        """Clear GPU cache to free memory."""
        if not self.available or not _TORCH_AVAILABLE:
            return

        try:
            if self.device == "cuda":
                torch.cuda.empty_cache()
                logger.debug("Cleared CUDA cache")
            elif self.device == "mps":
                torch.mps.empty_cache()
                logger.debug("Cleared MPS cache")
        except Exception as e:
            logger.warning(f"Failed to clear cache: {e}")

    def synchronize(self) -> None:
        """Synchronize GPU operations."""
        if not self.available or not _TORCH_AVAILABLE:
            return

        try:
            if self.device == "cuda":
                torch.cuda.synchronize()
            elif self.device == "mps":
                torch.mps.synchronize()
        except Exception:
            pass


# Global instance for easy access
_memory_manager: Optional[GPUMemoryManager] = None


def get_memory_manager() -> GPUMemoryManager:
    """Get or create the global memory manager instance.

    Returns
    -------
    GPUMemoryManager
        The global memory manager.
    """
    global _memory_manager
    if _memory_manager is None:
        _memory_manager = GPUMemoryManager()
    return _memory_manager
