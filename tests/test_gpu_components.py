"""Unit tests for GPU memory management and batch processing.

Tests for:
- GPUMemoryManager: memory monitoring and adaptive sizing
- StatisticsCache: LRU caching with GPU support
- BatchProcessor: batch processing and streaming
- CorpusLinguisticStatisticsDictionary: corpus-based dictionary
"""
from __future__ import annotations

import logging
from typing import List

import pytest

from semantic_decomposition.batch_processor import BatchProcessor, get_batch_processor
from semantic_decomposition.concept import Concept
from semantic_decomposition.cuda_operations import CUDAOperations
from semantic_decomposition.dictionaries.corpus_statistics_dictionary import (
    CorpusLinguisticStatisticsDictionary,
    CorpusStatistics,
)
from semantic_decomposition.gpu_decomposition import GPUDecomposition
from semantic_decomposition.gpu_memory_manager import (
    GPUMemoryConfig,
    GPUMemoryManager,
    get_memory_manager,
)
from semantic_decomposition.statistics_cache import StatisticsCache, get_statistics_cache

logger = logging.getLogger(__name__)

# Check for torch availability
try:
    import torch
    _TORCH_AVAILABLE = True
except ImportError:
    _TORCH_AVAILABLE = False


class TestGPUMemoryManager:
    """Test suite for GPUMemoryManager."""

    def test_init_cpu(self):
        """Test initialization on CPU."""
        manager = GPUMemoryManager(device="cpu")
        assert manager.device == "cpu"
        assert not manager.available

    def test_init_auto_detect(self):
        """Test automatic device detection."""
        manager = GPUMemoryManager()
        assert manager.device in ("cpu", "cuda", "mps")

    def test_get_available_memory(self):
        """Test memory availability reporting."""
        manager = GPUMemoryManager(device="cpu")
        memory = manager.get_available_memory()
        assert memory > 0
        assert memory == int(1e12)  # CPU fallback

    def test_get_memory_usage(self):
        """Test memory usage tracking."""
        manager = GPUMemoryManager(device="cpu")
        used, total = manager.get_memory_usage()
        assert used >= 0
        assert total > 0

    def test_get_memory_percent(self):
        """Test memory percentage calculation."""
        manager = GPUMemoryManager(device="cpu")
        percent = manager.get_memory_percent()
        assert 0.0 <= percent <= 100.0

    def test_calculate_optimal_batch_size(self):
        """Test batch size calculation."""
        manager = GPUMemoryManager(device="cpu")

        # With 1KB per item and 1TB available, should get large batch size
        size = manager.calculate_optimal_batch_size(
            estimated_memory_per_item=1024
        )
        assert size >= GPUMemoryConfig.min_batch_size
        assert size <= GPUMemoryConfig.max_batch_size

    def test_batch_size_respects_limits(self):
        """Test that batch size respects configured limits."""
        manager = GPUMemoryManager(device="cpu")

        # With 0 memory per item, should still return valid size
        size = manager.calculate_optimal_batch_size(
            estimated_memory_per_item=0
        )
        assert size == 32  # Falls back to current size

        # Very large items should reduce batch size
        size = manager.calculate_optimal_batch_size(
            estimated_memory_per_item=int(1e9),  # 1GB per item
            current_batch_size=1000,
        )
        assert size >= GPUMemoryConfig.min_batch_size

    def test_clear_cache(self):
        """Test cache clearing."""
        manager = GPUMemoryManager(device="cpu")
        # Should not raise
        manager.clear_cache()

    def test_synchronize(self):
        """Test synchronization."""
        manager = GPUMemoryManager(device="cpu")
        # Should not raise
        manager.synchronize()

    def test_global_instance(self):
        """Test global memory manager singleton."""
        m1 = get_memory_manager()
        m2 = get_memory_manager()
        assert m1 is m2


class TestStatisticsCache:
    """Test suite for StatisticsCache."""

    def test_init(self):
        """Test cache initialization."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)
        assert cache.max_size == 100
        assert cache.hits == 0
        assert cache.misses == 0

    def test_put_and_get(self):
        """Test basic put/get operations."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)

        cache.put("key1", "value1")
        assert cache.get("key1") == "value1"
        assert cache.hits == 1

    def test_miss(self):
        """Test cache miss."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)
        result = cache.get("nonexistent")
        assert result is None
        assert cache.misses == 1

    def test_lru_eviction(self):
        """Test LRU eviction when cache is full."""
        cache = StatisticsCache(max_size=3, enable_gpu=False)

        # Fill cache
        cache.put("a", 1)
        cache.put("b", 2)
        cache.put("c", 3)

        # Access 'a' to mark as recently used
        cache.get("a")

        # Add new item, should evict 'b' (least recently used of 'b' and 'c')
        cache.put("d", 4)

        # 'a' should still be there
        assert cache.get("a") is not None

    def test_batch_get(self):
        """Test batch get operation."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)

        cache.put("a", 1)
        cache.put("b", 2)

        values, found = cache.batch_get(["a", "b", "c"])
        assert values == [1, 2, None]
        assert found == [True, True, False]

    def test_batch_put(self):
        """Test batch put operation."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)

        cache.batch_put({"x": 10, "y": 20, "z": 30})

        assert cache.get("x") == 10
        assert cache.get("y") == 20
        assert cache.get("z") == 30

    def test_clear(self):
        """Test cache clearing."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)

        cache.put("a", 1)
        cache.get("a")
        cache.get("a")

        cache.clear()

        assert cache.get("a") is None
        # After clear, the new get will increment misses
        assert cache.hits == 0
        assert cache.misses == 1

    def test_get_stats(self):
        """Test statistics reporting."""
        cache = StatisticsCache(max_size=100, enable_gpu=False)

        cache.put("a", 1)
        cache.get("a")
        cache.get("b")

        stats = cache.get_stats()
        assert stats["size"] == 1
        assert stats["hits"] == 1
        assert stats["misses"] == 1
        assert stats["hit_rate"] == 0.5

    def test_global_instance(self):
        """Test global cache singleton."""
        c1 = get_statistics_cache()
        c2 = get_statistics_cache()
        assert c1 is c2


class TestBatchProcessor:
    """Test suite for BatchProcessor."""

    def test_init(self):
        """Test batch processor initialization."""
        processor = BatchProcessor(enable_streaming=True)
        assert processor.enable_streaming
        assert processor.total_processed == 0

    def test_process_batch_empty(self):
        """Test processing empty batch."""
        processor = BatchProcessor()

        def dummy_process(item):
            return item

        results, elapsed = processor.process_batch(
            [],
            dummy_process,
            show_progress=False,
        )

        assert results == []
        assert elapsed >= 0

    def test_process_batch_simple(self):
        """Test processing simple batch."""
        processor = BatchProcessor()

        concepts = [Concept() for _ in range(10)]
        for i, c in enumerate(concepts):
            c.id = i
            c.litheral = f"word{i}"

        def dummy_process(item):
            item.litheral = item.litheral.upper()
            return item

        results, elapsed = processor.process_batch(
            concepts,
            dummy_process,
            show_progress=False,
        )

        assert len(results) == 10
        assert all(c.litheral.isupper() for c in results)
        assert processor.total_processed == 10

    def test_stream_decompose(self):
        """Test streaming decomposition."""
        processor = BatchProcessor()

        concepts = [Concept() for _ in range(20)]
        for i, c in enumerate(concepts):
            c.id = i
            c.litheral = f"word{i}"

        def dummy_process(item):
            return item

        results, elapsed = processor.process_stream(
            concepts,
            dummy_process,
            batch_size=5,
        )

        assert len(results) == 20

    def test_stream_max_items(self):
        """Test streaming with max items limit."""
        processor = BatchProcessor()

        concepts = [Concept() for _ in range(20)]
        for i, c in enumerate(concepts):
            c.id = i
            c.litheral = f"word{i}"

        def dummy_process(item):
            return item

        results, elapsed = processor.process_stream(
            concepts,
            dummy_process,
            batch_size=5,
            max_items=10,
        )

        assert len(results) == 10

    def test_get_statistics(self):
        """Test statistics reporting."""
        processor = BatchProcessor()

        concepts = [Concept() for _ in range(5)]
        for i, c in enumerate(concepts):
            c.id = i
            c.litheral = f"word{i}"

        def dummy_process(item):
            return item

        processor.process_batch(concepts, dummy_process, show_progress=False)

        stats = processor.get_statistics()
        assert "total_processed" in stats
        assert "throughput_items_per_sec" in stats
        assert stats["total_processed"] == 5

    def test_reset_statistics(self):
        """Test statistics reset."""
        processor = BatchProcessor()

        concepts = [Concept() for _ in range(5)]
        for i, c in enumerate(concepts):
            c.id = i
            c.litheral = f"word{i}"

        def dummy_process(item):
            return item

        processor.process_batch(concepts, dummy_process, show_progress=False)
        processor.reset_statistics()

        assert processor.total_processed == 0
        assert processor.total_time == 0.0
        assert processor.batches_processed == 0


class TestCorpusStatistics:
    """Test suite for CorpusStatistics."""

    def test_init(self):
        """Test corpus statistics initialization."""
        stats = CorpusStatistics()
        assert stats.context_window == 5
        assert stats.vocabulary_size == 0

    def test_frequencies(self):
        """Test frequency tracking."""
        stats = CorpusStatistics()
        stats.frequencies["the"] = 100
        stats.frequencies["cat"] = 50
        stats.total_tokens = 1000

        assert stats.get_frequency("the") == 100
        assert stats.get_frequency("cat") == 50
        assert stats.get_frequency("missing") == 0

    def test_relative_frequency(self):
        """Test relative frequency calculation."""
        stats = CorpusStatistics()
        stats.frequencies["the"] = 100
        stats.total_tokens = 1000

        rel_freq = stats.get_relative_frequency("the")
        assert rel_freq == 0.1

    def test_cooccurrence(self):
        """Test co-occurrence tracking."""
        stats = CorpusStatistics()
        stats.cooccurrences[("cat", "dog")] = 25

        assert stats.get_cooccurrence_count("cat", "dog") == 25
        assert stats.get_cooccurrence_count("dog", "cat") == 25  # Symmetric
        assert stats.get_cooccurrence_count("cat", "bird") == 0

    def test_similarity(self):
        """Test similarity scoring."""
        stats = CorpusStatistics()
        stats.similarities[("cat", "dog")] = 0.8

        assert stats.get_similarity("cat", "dog") == 0.8
        assert stats.get_similarity("dog", "cat") == 0.8  # Symmetric
        assert stats.get_similarity("cat", "cat") == 1.0  # Self similarity


class TestCorpusLinguisticStatisticsDictionary:
    """Test suite for CorpusLinguisticStatisticsDictionary."""

    def test_init(self):
        """Test dictionary initialization."""
        CorpusLinguisticStatisticsDictionary.init()
        assert CorpusLinguisticStatisticsDictionary._initialized

    def test_get_synonyms_empty(self):
        """Test synonym retrieval with empty statistics."""
        CorpusLinguisticStatisticsDictionary.init()
        dictionary = CorpusLinguisticStatisticsDictionary()

        synonyms = dictionary.get_synonyms("test")
        assert synonyms == []

    def test_get_definitions_empty(self):
        """Test definition retrieval with empty statistics."""
        CorpusLinguisticStatisticsDictionary.init()
        dictionary = CorpusLinguisticStatisticsDictionary()

        definitions = dictionary.get_definitions("test")
        assert definitions == []

    def test_get_concept(self):
        """Test concept retrieval."""
        CorpusLinguisticStatisticsDictionary.init()
        dictionary = CorpusLinguisticStatisticsDictionary()

        concept = dictionary.get_concept("test")
        assert concept is not None
        assert concept.litheral == "test"

    def test_fill_concept(self):
        """Test concept enrichment."""
        CorpusLinguisticStatisticsDictionary.init()
        dictionary = CorpusLinguisticStatisticsDictionary()

        concept = Concept()
        concept.litheral = "test"

        enriched = dictionary.fill_concept(concept)
        assert enriched.litheral == "test"


class TestCUDAOperations:
    """Test suite for CUDA operations."""

    @pytest.mark.skipif(not _TORCH_AVAILABLE, reason="torch not available")
    def test_init(self):
        """Test CUDA operations initialization."""
        ops = CUDAOperations(device="cpu")
        assert ops.device == "cpu"

    @pytest.mark.skipif(not _TORCH_AVAILABLE, reason="torch not available")
    def test_normalize(self):
        """Test vector normalization."""
        ops = CUDAOperations(device="cpu")
        tensor = torch.randn(3, 10)
        normalized = ops.batch_normalize(tensor, dim=1)

        assert normalized.shape == tensor.shape
        # Check that vectors are normalized
        norms = torch.norm(normalized, p=2, dim=1)
        assert torch.allclose(norms, torch.ones(3), atol=1e-6)

    @pytest.mark.skipif(not _TORCH_AVAILABLE, reason="torch not available")
    def test_mean_pooling(self):
        """Test mean pooling."""
        ops = CUDAOperations(device="cpu")
        embeddings = torch.randn(2, 5, 10)
        pooled = ops.batch_mean_pooling(embeddings)

        assert pooled.shape == (2, 10)


if __name__ == "__main__":
    pytest.main([__file__, "-v", "-s"])
