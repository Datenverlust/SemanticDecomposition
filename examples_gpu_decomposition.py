"""Example: GPU-Accelerated Semantic Decomposition with Corpus Statistics.

This example demonstrates the complete GPU-accelerated semantic decomposition
system with corpus linguistic statistics integration.
"""
from __future__ import annotations

import logging
from typing import List

from semantic_decomposition.batch_processor import get_batch_processor
from semantic_decomposition.concept import Concept
from semantic_decomposition.dictionaries.corpus_statistics_dictionary import (
    CorpusLinguisticStatisticsDictionary,
    CorpusStatistics,
)
from semantic_decomposition.gpu_decomposition import GPUDecomposition
from semantic_decomposition.gpu_memory_manager import get_memory_manager
from semantic_decomposition.statistics_cache import get_statistics_cache

# Configure logging
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


def create_sample_concepts(count: int = 100) -> List[Concept]:
    """Create sample concepts for demonstration.

    Parameters
    ----------
    count : int, optional
        Number of concepts to create. Default is 100.

    Returns
    -------
    list[Concept]
        List of sample concepts.
    """
    concepts = []
    words = [
        "computer",
        "algorithm",
        "network",
        "data",
        "processing",
        "system",
        "memory",
        "storage",
        "communication",
        "computation",
    ]

    for i in range(count):
        concept = Concept()
        concept.id = i
        concept.litheral = f"{words[i % len(words)]}{i // len(words)}"
        concepts.append(concept)

    return concepts


def create_sample_corpus_statistics() -> CorpusStatistics:
    """Create sample corpus statistics.

    Returns
    -------
    CorpusStatistics
        Sample corpus statistics.
    """
    stats = CorpusStatistics()

    # Add sample frequencies
    stats.frequencies = {
        "the": 1000,
        "computer": 500,
        "algorithm": 300,
        "network": 250,
        "data": 450,
        "processing": 200,
        "system": 350,
        "memory": 150,
        "storage": 120,
        "communication": 100,
    }

    # Add co-occurrence statistics
    stats.cooccurrences = {
        ("computer", "algorithm"): 150,
        ("algorithm", "data"): 140,
        ("network", "communication"): 95,
        ("system", "memory"): 80,
        ("data", "processing"): 120,
        ("computer", "memory"): 110,
    }

    # Add similarity scores
    stats.similarities = {
        ("computer", "system"): 0.85,
        ("algorithm", "processing"): 0.78,
        ("network", "communication"): 0.82,
        ("data", "information"): 0.88,
    }

    stats.total_tokens = 5000
    stats.vocabulary_size = len(stats.frequencies)

    return stats


def example_basic_decomposition():
    """Example: Basic GPU-accelerated decomposition."""
    logger.info("=" * 70)
    logger.info("Example 1: Basic GPU-Accelerated Decomposition")
    logger.info("=" * 70)

    # Initialize GPU memory manager
    memory_manager = get_memory_manager()
    logger.info(f"Memory manager initialized: {memory_manager.device}")
    logger.info(f"  Available memory: {memory_manager.get_available_memory() / 1e9:.1f} GB")

    # Initialize corpus statistics dictionary
    CorpusLinguisticStatisticsDictionary.init()

    # Initialize decomposition with the dictionary
    GPUDecomposition.init([CorpusLinguisticStatisticsDictionary()])

    # Create sample concepts
    concepts = create_sample_concepts(50)
    logger.info(f"Created {len(concepts)} sample concepts")

    # Perform batch decomposition
    logger.info("Starting batch decomposition...")
    results, elapsed = GPUDecomposition.batch_decompose(
        concepts,
        show_progress=True,
    )

    logger.info(f"Decomposition completed:")
    logger.info(f"  Concepts processed: {len(results)}")
    logger.info(f"  Time elapsed: {elapsed:.3f} seconds")
    logger.info(f"  Throughput: {len(results) / elapsed:.0f} items/sec")

    # Print statistics
    stats = GPUDecomposition.get_statistics()
    logger.info(f"Performance statistics:")
    logger.info(f"  Hit rate: {stats.get('cache_stats', {}).get('hit_rate', 0):.1%}")
    logger.info(f"  Memory usage: {stats.get('memory_percent', 0):.1f}%")


def example_streaming_decomposition():
    """Example: Streaming decomposition for large datasets."""
    logger.info("=" * 70)
    logger.info("Example 2: Streaming Decomposition for Large Datasets")
    logger.info("=" * 70)

    # Initialize
    CorpusLinguisticStatisticsDictionary.init()
    GPUDecomposition.init([CorpusLinguisticStatisticsDictionary()])

    # Create a large batch
    concepts = create_sample_concepts(1000)
    logger.info(f"Created {len(concepts)} concepts for streaming")

    # Process in streaming mode
    logger.info("Starting streaming decomposition...")
    results, elapsed = GPUDecomposition.stream_decompose(
        concepts,
        batch_size=100,
        max_items=500,  # Process only first 500
    )

    logger.info(f"Streaming completed:")
    logger.info(f"  Concepts processed: {len(results)}")
    logger.info(f"  Time elapsed: {elapsed:.3f} seconds")


def example_with_corpus_statistics():
    """Example: Using corpus statistics for enrichment."""
    logger.info("=" * 70)
    logger.info("Example 3: Corpus Statistics Integration")
    logger.info("=" * 70)

    # Create and set corpus statistics
    stats = create_sample_corpus_statistics()
    logger.info(f"Created corpus statistics:")
    logger.info(f"  Vocabulary size: {stats.vocabulary_size}")
    logger.info(f"  Total tokens: {stats.total_tokens}")
    logger.info(f"  Co-occurrence pairs: {len(stats.cooccurrences)}")

    # Initialize dictionary with statistics
    CorpusLinguisticStatisticsDictionary.init()
    dictionary = CorpusLinguisticStatisticsDictionary()

    # Manually set statistics for demonstration
    if dictionary.__class__._statistics:
        dictionary.__class__._statistics.frequencies = stats.frequencies
        dictionary.__class__._statistics.cooccurrences = stats.cooccurrences
        dictionary.__class__._statistics.similarities = stats.similarities
        dictionary.__class__._statistics.total_tokens = stats.total_tokens

    # Test concept enrichment
    concept = Concept()
    concept.litheral = "computer"
    enriched = dictionary.fill_concept(concept)

    logger.info(f"Enriched concept 'computer':")
    logger.info(f"  Litheral: {enriched.litheral}")
    logger.info(f"  Frequency: {enriched.frequency}")
    logger.info(f"  Relative frequency: {enriched.relative_frequency:.4f}")
    logger.info(f"  Synonyms found: {len(enriched.synonyms)}")


def example_performance_monitoring():
    """Example: Performance monitoring and statistics."""
    logger.info("=" * 70)
    logger.info("Example 4: Performance Monitoring")
    logger.info("=" * 70)

    # Initialize
    memory_manager = get_memory_manager()
    statistics_cache = get_statistics_cache()
    batch_processor = get_batch_processor()

    CorpusLinguisticStatisticsDictionary.init()
    GPUDecomposition.init([CorpusLinguisticStatisticsDictionary()])

    # Process concepts
    concepts = create_sample_concepts(100)
    results, _ = GPUDecomposition.batch_decompose(concepts, show_progress=False)

    # Gather statistics
    memory_stats = GPUDecomposition.get_memory_stats()
    decomposition_stats = GPUDecomposition.get_statistics()

    logger.info(f"Memory Statistics:")
    logger.info(f"  Device: {memory_stats.get('device', 'N/A')}")
    logger.info(f"  Memory usage: {memory_stats.get('memory_percent', 0):.1f}%")

    logger.info(f"Decomposition Statistics:")
    logger.info(f"  Total processed: {decomposition_stats.get('total_processed', 0)}")
    logger.info(f"  Throughput: {decomposition_stats.get('throughput_items_per_sec', 0):.0f} items/sec")

    logger.info(f"Cache Statistics:")
    cache_stats = decomposition_stats.get("cache_stats", {})
    logger.info(f"  Cache size: {cache_stats.get('size', 0)}/{cache_stats.get('max_size', 0)}")
    logger.info(f"  Hit rate: {cache_stats.get('hit_rate', 0):.1%}")
    logger.info(f"  GPU cache size: {cache_stats.get('gpu_cache_size', 0)}")


def example_batching_strategies():
    """Example: Different batching strategies."""
    logger.info("=" * 70)
    logger.info("Example 5: Batching Strategies")
    logger.info("=" * 70)

    CorpusLinguisticStatisticsDictionary.init()
    GPUDecomposition.init([CorpusLinguisticStatisticsDictionary()])

    concepts = create_sample_concepts(200)

    # Strategy 1: Adaptive batching
    logger.info("Strategy 1: Adaptive batching (auto-sized)")
    results1, time1 = GPUDecomposition.batch_decompose(concepts, show_progress=False)
    logger.info(f"  Concepts: {len(results1)}, Time: {time1:.3f}s")

    # Strategy 2: Fixed batch size
    logger.info("Strategy 2: Fixed batch size (32 items)")
    GPUDecomposition.reset_statistics()
    results2, time2 = GPUDecomposition.batch_decompose(
        concepts,
        chunk_size=32,
        show_progress=False,
    )
    logger.info(f"  Concepts: {len(results2)}, Time: {time2:.3f}s")

    # Strategy 3: Streaming with max items
    logger.info("Strategy 3: Streaming with limit (100 items)")
    GPUDecomposition.reset_statistics()
    results3, time3 = GPUDecomposition.stream_decompose(
        concepts,
        batch_size=50,
        max_items=100,
        show_progress=False,
    )
    logger.info(f"  Concepts: {len(results3)}, Time: {time3:.3f}s")


if __name__ == "__main__":
    # Run all examples
    example_basic_decomposition()
    example_streaming_decomposition()
    example_with_corpus_statistics()
    example_performance_monitoring()
    example_batching_strategies()

    logger.info("=" * 70)
    logger.info("All examples completed successfully!")
    logger.info("=" * 70)
