"""Low-level CUDA operations for batch processing.

This module provides efficient GPU kernels for common operations in semantic
decomposition, including distance calculations, similarity metrics, and
vectorization operations.
"""
from __future__ import annotations

import logging
from typing import Optional, Tuple

logger = logging.getLogger(__name__)

try:
    import torch
    import torch.nn.functional as F
    _TORCH_AVAILABLE = True
except ImportError:
    _TORCH_AVAILABLE = False


class CUDAOperations:
    """Collection of GPU-accelerated operations."""

    def __init__(self, device: str = "cuda"):
        """Initialize CUDA operations.

        Parameters
        ----------
        device : str, optional
            Device to use ('cuda', 'mps', or 'cpu'). Default is 'cuda'.
        """
        self.device = device
        self.available = _TORCH_AVAILABLE and device in ("cuda", "mps")

    # Distance and Similarity Metrics

    def cosine_similarity(
        self,
        a: torch.Tensor,
        b: torch.Tensor,
        dim: int = 1,
    ) -> torch.Tensor:
        """Compute cosine similarity between vectors.

        Parameters
        ----------
        a : torch.Tensor
            First tensor of shape (batch, features) or (features,).
        b : torch.Tensor
            Second tensor of shape (batch, features) or (features,).
        dim : int, optional
            Dimension along which to compute similarity. Default is 1.

        Returns
        -------
        torch.Tensor
            Similarity scores in range [-1, 1].
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return F.cosine_similarity(a, b, dim=dim)

    def euclidean_distance(
        self,
        a: torch.Tensor,
        b: torch.Tensor,
    ) -> torch.Tensor:
        """Compute Euclidean distance between vectors.

        Parameters
        ----------
        a : torch.Tensor
            First tensor of shape (batch, features) or (features,).
        b : torch.Tensor
            Second tensor of shape (batch, features) or (features,).

        Returns
        -------
        torch.Tensor
            Distance values.
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return torch.cdist(a, b, p=2.0)

    def manhattan_distance(
        self,
        a: torch.Tensor,
        b: torch.Tensor,
    ) -> torch.Tensor:
        """Compute Manhattan distance between vectors.

        Parameters
        ----------
        a : torch.Tensor
            First tensor.
        b : torch.Tensor
            Second tensor.

        Returns
        -------
        torch.Tensor
            Distance values.
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return torch.cdist(a, b, p=1.0)

    # Batch Operations

    def batch_normalize(
        self,
        tensors: torch.Tensor,
        dim: int = 1,
        eps: float = 1e-8,
    ) -> torch.Tensor:
        """Normalize batch of vectors.

        Parameters
        ----------
        tensors : torch.Tensor
            Batch of tensors of shape (batch, features).
        dim : int, optional
            Dimension to normalize. Default is 1.
        eps : float, optional
            Epsilon for numerical stability. Default is 1e-8.

        Returns
        -------
        torch.Tensor
            Normalized tensors.
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return F.normalize(tensors, p=2.0, dim=dim, eps=eps)

    def batch_mean_pooling(
        self,
        embeddings: torch.Tensor,
        attention_mask: Optional[torch.Tensor] = None,
    ) -> torch.Tensor:
        """Compute mean pooling over embeddings.

        Parameters
        ----------
        embeddings : torch.Tensor
            Embeddings of shape (batch, seq_len, features).
        attention_mask : torch.Tensor, optional
            Attention mask of shape (batch, seq_len).

        Returns
        -------
        torch.Tensor
            Pooled embeddings of shape (batch, features).
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        if attention_mask is not None:
            # Mask out padding tokens
            embeddings = embeddings * attention_mask.unsqueeze(-1)
            sum_embeddings = torch.sum(embeddings, dim=1)
            sum_mask = torch.clamp(
                torch.sum(attention_mask, dim=1, keepdims=True), min=1e-9
            )
            return sum_embeddings / sum_mask
        else:
            return torch.mean(embeddings, dim=1)

    def batch_max_pooling(
        self,
        embeddings: torch.Tensor,
    ) -> torch.Tensor:
        """Compute max pooling over embeddings.

        Parameters
        ----------
        embeddings : torch.Tensor
            Embeddings of shape (batch, seq_len, features).

        Returns
        -------
        torch.Tensor
            Pooled embeddings of shape (batch, features).
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return torch.max(embeddings, dim=1)[0]

    # Frequency Operations

    def compute_log_frequency(
        self,
        frequencies: torch.Tensor,
        smooth: float = 1.0,
    ) -> torch.Tensor:
        """Compute log-transformed frequencies with smoothing.

        Parameters
        ----------
        frequencies : torch.Tensor
            Raw frequency counts.
        smooth : float, optional
            Smoothing factor. Default is 1.0.

        Returns
        -------
        torch.Tensor
            Log-transformed frequencies.
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        return torch.log(frequencies + smooth)

    def compute_relative_frequencies(
        self,
        frequencies: torch.Tensor,
    ) -> torch.Tensor:
        """Normalize frequencies to relative frequencies.

        Parameters
        ----------
        frequencies : torch.Tensor
            Raw frequency counts of shape (batch,).

        Returns
        -------
        torch.Tensor
            Relative frequencies (normalized to [0, 1]).
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        total = torch.sum(frequencies)
        return frequencies / torch.clamp(total, min=1)

    # Attention Operations

    def scaled_dot_product_attention(
        self,
        query: torch.Tensor,
        key: torch.Tensor,
        value: torch.Tensor,
        mask: Optional[torch.Tensor] = None,
        dropout_p: float = 0.0,
    ) -> Tuple[torch.Tensor, torch.Tensor]:
        """Compute scaled dot-product attention.

        Parameters
        ----------
        query : torch.Tensor
            Query tensor of shape (batch, seq_len, features).
        key : torch.Tensor
            Key tensor of shape (batch, seq_len, features).
        value : torch.Tensor
            Value tensor of shape (batch, seq_len, features).
        mask : torch.Tensor, optional
            Attention mask.
        dropout_p : float, optional
            Dropout probability. Default is 0.0.

        Returns
        -------
        tuple[torch.Tensor, torch.Tensor]
            Attention output and attention weights.
        """
        if not _TORCH_AVAILABLE:
            raise RuntimeError("PyTorch not available")

        d_k = query.size(-1)
        scores = torch.matmul(query, key.transpose(-2, -1)) / (d_k ** 0.5)

        if mask is not None:
            scores = scores.masked_fill(mask == 0, float("-inf"))

        attn_weights = F.softmax(scores, dim=-1)
        attn_weights = F.dropout(attn_weights, p=dropout_p, training=True)

        output = torch.matmul(attn_weights, value)
        return output, attn_weights

    # Helper Methods

    def to_device(self, tensor: torch.Tensor) -> torch.Tensor:
        """Move tensor to the configured device.

        Parameters
        ----------
        tensor : torch.Tensor
            Tensor to move.

        Returns
        -------
        torch.Tensor
            Tensor on the target device.
        """
        if self.available:
            return tensor.to(self.device)
        return tensor

    def create_tensor(
        self,
        data: list | tuple,
        dtype: torch.dtype = torch.float32,
    ) -> torch.Tensor:
        """Create a tensor on the configured device.

        Parameters
        ----------
        data : list | tuple
            Data to convert to tensor.
        dtype : torch.dtype, optional
            Data type. Default is torch.float32.

        Returns
        -------
        torch.Tensor
            Tensor on the target device.
        """
        if self.available:
            return torch.tensor(data, dtype=dtype, device=self.device)
        return torch.tensor(data, dtype=dtype)

    def create_empty(
        self,
        shape: Tuple[int, ...],
        dtype: torch.dtype = torch.float32,
    ) -> torch.Tensor:
        """Create an empty tensor on the configured device.

        Parameters
        ----------
        shape : tuple[int, ...]
            Shape of the tensor.
        dtype : torch.dtype, optional
            Data type. Default is torch.float32.

        Returns
        -------
        torch.Tensor
            Empty tensor on the target device.
        """
        if self.available:
            return torch.empty(shape, dtype=dtype, device=self.device)
        return torch.empty(shape, dtype=dtype)
