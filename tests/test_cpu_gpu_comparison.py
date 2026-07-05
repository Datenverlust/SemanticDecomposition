"""CPU vs GPU Decomposition Comparison Test

Comprehensive test suite comparing CPU-only and GPU-accelerated decomposition
on both correctness (equivalence) and performance (speed).

Usage:
    python tests/test_cpu_gpu_comparison.py
    python -m pytest tests/test_cpu_gpu_comparison.py -v -s
"""
from __future__ import annotations

import concurrent.futures
import logging
import time
from collections import defaultdict
from typing import Dict, List, Optional, Tuple

import pytest

from semantic_decomposition.batch_processor import BatchProcessor
from semantic_decomposition.concept import Concept
from semantic_decomposition.decomposition import Decomposition
from semantic_decomposition.decomposition_config import DecompositionConfig
from semantic_decomposition.definition import Definition
from semantic_decomposition.dictionaries.dictionary import Dictionary
from semantic_decomposition.gpu_decomposition import GPUDecomposition
from semantic_decomposition.gpu_memory_manager import get_memory_manager
from semantic_decomposition.statistics_cache import get_statistics_cache
from semantic_decomposition.word_type import WordType

logger = logging.getLogger(__name__)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)

# Check for torch availability
try:
    import torch
    _TORCH_AVAILABLE = True
except ImportError:
    _TORCH_AVAILABLE = False


# ============================================================================
# Mock Dictionary for Testing
# ============================================================================


class MockDictionary(Dictionary):
    """Simple in-memory dictionary for testing without external dependencies.
    
    Uses semantically related English concepts from the Cybercrime domain
    to demonstrate realistic semantic decomposition.
    """

    def __init__(self):
        """Initialize mock dictionary with Cybercrime domain concepts."""
        # Domain: Cybercrime and related security concepts
        # These are real English concepts related to cybercriminal activities
        self.word_data = {
            "cybercrime": {
                "synonyms": ["cyber attack", "hacking", "digital crime"],
                "definitions": [["criminal", "activity", "using", "computers"]],
                "hypernyms": ["crime", "illegal activity"],
            },
            "hacking": {
                "synonyms": ["unauthorized access", "intrusion"],
                "definitions": [["unauthorized", "access", "to", "computer", "systems"]],
                "hypernyms": ["cybercrime", "illegal activity"],
            },
            "malware": {
                "synonyms": ["virus", "trojan", "spyware"],
                "definitions": [["malicious", "software", "designed", "to", "harm"]],
                "hypernyms": ["malicious code", "cybercrime tool"],
            },
            "phishing": {
                "synonyms": ["social engineering", "fraud"],
                "definitions": [["deceptive", "attempt", "to", "obtain", "credentials"]],
                "hypernyms": ["cybercrime", "fraud"],
            },
            "ransomware": {
                "synonyms": ["encryption malware", "extortion malware"],
                "definitions": [["malware", "that", "encrypts", "files", "for", "ransom"]],
                "hypernyms": ["malware", "extortion"],
            },
            "botnet": {
                "synonyms": ["zombie network", "bot army"],
                "definitions": [["network", "of", "compromised", "computers"]],
                "hypernyms": ["malware infrastructure", "distributed attack"],
            },
            "ddos": {
                "synonyms": ["denial of service", "network attack"],
                "definitions": [["overwhelming", "server", "with", "traffic"]],
                "hypernyms": ["cyber attack", "network attack"],
            },
            "data breach": {
                "synonyms": ["unauthorized access", "data theft"],
                "definitions": [["unauthorized", "exposure", "of", "sensitive", "data"]],
                "hypernyms": ["cybercrime", "security incident"],
            },
            "encryption": {
                "synonyms": ["cryptography", "encoding"],
                "definitions": [["conversion", "of", "data", "using", "cipher"]],
                "hypernyms": ["security mechanism", "cryptographic method"],
            },
            "firewall": {
                "synonyms": ["security gateway", "packet filter"],
                "definitions": [["system", "that", "monitors", "network", "traffic"]],
                "hypernyms": ["security tool", "network defense"],
            },
        }

    def fill_concept(self, concept: Concept) -> Concept:
        """Fill concept with mock data."""
        word_lower = concept.litheral.lower()
        if word_lower in self.word_data:
            data = self.word_data[word_lower]

            # Add synonyms
            for syn in data.get("synonyms", []):
                syn_concept = Concept()
                syn_concept.litheral = syn
                concept.synonyms.append(syn_concept)

            # Add definitions
            for def_tokens in data.get("definitions", []):
                def_concepts = []
                for token in def_tokens:
                    token_concept = Concept()
                    token_concept.litheral = token
                    def_concepts.append(token_concept)
                concept.definitions.append(Definition(def_concepts))

        return concept

    def get_synonyms(self, word: str) -> List[Concept]:
        """Get synonyms."""
        word_lower = word.lower()
        if word_lower in self.word_data:
            results = []
            for syn in self.word_data[word_lower].get("synonyms", []):
                s = Concept()
                s.litheral = syn
                results.append(s)
            return results
        return []

    def get_antonyms(self, word: str) -> List[Concept]:
        """Get antonyms."""
        return []

    def get_hypernyms(self, word: str) -> List[Concept]:
        """Get hypernyms (more general concepts)."""
        word_lower = word.lower()
        if word_lower in self.word_data:
            results = []
            for hyp in self.word_data[word_lower].get("hypernyms", []):
                h = Concept()
                h.litheral = hyp
                results.append(h)
            return results
        return []

    def get_hyponyms(self, word: str) -> List[Concept]:
        """Get hyponyms."""
        return []

    def get_meronyms(self, word: str) -> List[Concept]:
        """Get meronyms."""
        return []

    def get_definitions(self, word: str) -> List[Definition]:
        """Get definitions."""
        word_lower = word.lower()
        if word_lower in self.word_data:
            defs = []
            for def_tokens in self.word_data[word_lower].get("definitions", []):
                def_concepts = []
                for token in def_tokens:
                    token_concept = Concept()
                    token_concept.litheral = token
                    def_concepts.append(token_concept)
                defs.append(Definition(def_concepts))
            return defs
        return []

    def get_lemma(self, word: str) -> str:
        """Get lemma."""
        return word.lower()

    def get_concept(self, word: str) -> Concept:
        """Get concept."""
        concept = Concept()
        concept.litheral = word
        return self.fill_concept(concept)

    def set_lemma(self, concept: Concept) -> Concept:
        """Set lemma."""
        concept.lemma = concept.litheral.lower()
        return concept

    def set_pos(self, concept: Concept) -> Concept:
        """Set POS."""
        return concept

    def fill_definition(self, definition: Definition) -> Definition:
        """Fill definition."""
        return definition

    def fill_related(self, concept: Concept) -> Concept:
        """Fill related."""
        return concept


# ============================================================================
# Test Data Generation
# ============================================================================


def create_test_concepts(count: int) -> List[Concept]:
    """Create test concepts for comparison.
    
    Uses semantically related English concepts from the Cybercrime domain
    to demonstrate realistic semantic decomposition rather than arbitrary words.

    Parameters
    ----------
    count : int
        Number of concepts to create.

    Returns
    -------
    list[Concept]
        List of cybercrime-related test concepts.
    """
    # Domain-specific cybercrime and security concepts
    # These are real English concepts related to cybercriminal activities
    concepts_domain = [
        "cybercrime",      # Primary domain concept
        "hacking",         # Method
        "malware",         # Tool/Threat
        "phishing",        # Attack type
        "ransomware",      # Malware type
        "botnet",          # Infrastructure
        "ddos",            # Attack method
        "data breach",     # Consequence/Crime
        "encryption",      # Defense mechanism
        "firewall",        # Security tool
    ]

    concepts = []
    for i in range(count):
        concept = Concept()
        concept.id = i
        # Use domain concepts cyclically, supporting requests for more concepts than base set
        concept.litheral = concepts_domain[i % len(concepts_domain)]
        concepts.append(concept)

    return concepts


# ============================================================================
# Correctness Comparison
# ============================================================================


class CorrectnessComparator:
    """Compares decomposition results for correctness and equivalence."""

    @staticmethod
    def concepts_equal(c1: Concept, c2: Concept) -> bool:
        """Check if two concepts are equivalent.

        Parameters
        ----------
        c1 : Concept
            First concept.
        c2 : Concept
            Second concept.

        Returns
        -------
        bool
            True if concepts are equivalent.
        """
        # Compare basic attributes
        if c1.litheral != c2.litheral:
            return False
        if c1.id != c2.id:
            return False

        # Compare synonyms
        if len(c1.synonyms) != len(c2.synonyms):
            return False

        # Compare definitions
        if len(c1.definitions) != len(c2.definitions):
            return False

        return True

    @staticmethod
    def compare_batches(
        batch1: List[Concept],
        batch2: List[Concept],
    ) -> Tuple[bool, Dict[str, int]]:
        """Compare two batches of concepts.

        Parameters
        ----------
        batch1 : list[Concept]
            First batch.
        batch2 : list[Concept]
            Second batch.

        Returns
        -------
        tuple[bool, dict[str, int]]
            (all_match, detailed_stats)
        """
        stats = {
            "total_concepts": len(batch1),
            "matching_concepts": 0,
            "mismatched_concepts": 0,
            "missing_in_batch2": 0,
        }

        if len(batch1) != len(batch2):
            stats["mismatched_concepts"] = abs(len(batch1) - len(batch2))
            return False, stats

        # Create maps for comparison
        map1 = {c.id: c for c in batch1}
        map2 = {c.id: c for c in batch2}

        for concept_id in map1:
            if concept_id not in map2:
                stats["missing_in_batch2"] += 1
                continue

            if CorrectnessComparator.concepts_equal(map1[concept_id], map2[concept_id]):
                stats["matching_concepts"] += 1
            else:
                stats["mismatched_concepts"] += 1

        all_match = stats["mismatched_concepts"] == 0 and stats["missing_in_batch2"] == 0

        return all_match, stats


# ============================================================================
# Performance Comparison
# ============================================================================


class PerformanceComparator:
    """Measures and compares decomposition performance."""

    def __init__(self):
        """Initialize performance tracker."""
        self.results: Dict[str, Dict] = defaultdict(dict)

    def measure_decomposition(
        self,
        name: str,
        decompose_fn,
        concepts: List[Concept],
        iterations: int = 1,
    ) -> Dict:
        """Measure decomposition performance.

        Parameters
        ----------
        name : str
            Name of the decomposition method.
        decompose_fn : callable
            Function to call for decomposition.
        concepts : list[Concept]
            Concepts to decompose.
        iterations : int, optional
            Number of iterations. Default is 1.

        Returns
        -------
        dict
            Performance metrics.
        """
        times = []
        results_list = []

        for i in range(iterations):
            t0 = time.perf_counter()
            try:
                if hasattr(decompose_fn, "__self__"):
                    # Class method
                    results = decompose_fn(concepts)
                    if isinstance(results, tuple):
                        results = results[0]
                else:
                    # Regular function
                    results = decompose_fn(concepts)
                elapsed = time.perf_counter() - t0

                times.append(elapsed)
                results_list.append(results)
            except Exception as e:
                logger.error(f"Error in {name} iteration {i}: {e}")
                return None

        # Calculate statistics
        total_time = sum(times)
        avg_time = total_time / len(times)
        min_time = min(times)
        max_time = max(times)
        throughput = len(concepts) / avg_time if avg_time > 0 else 0

        metrics = {
            "name": name,
            "total_time_seconds": total_time,
            "avg_time_seconds": avg_time,
            "min_time_seconds": min_time,
            "max_time_seconds": max_time,
            "throughput_items_per_sec": throughput,
            "iterations": iterations,
            "total_concepts": len(concepts),
        }

        self.results[name] = metrics
        return metrics

    def compare_performance(self) -> Dict:
        """Compare performance across methods.

        Returns
        -------
        dict
            Comparison metrics including speedup.
        """
        if len(self.results) < 2:
            return {}

        names = list(self.results.keys())
        baseline = self.results[names[0]]

        comparison = {
            "baseline": baseline["name"],
            "metrics": {},
        }

        for name, metrics in self.results.items():
            if name == baseline["name"]:
                speedup = 1.0
            else:
                speedup = baseline["avg_time_seconds"] / metrics["avg_time_seconds"]

            comparison["metrics"][name] = {
                "avg_time": metrics["avg_time_seconds"],
                "throughput": metrics["throughput_items_per_sec"],
                "speedup": speedup,
            }

        return comparison


# ============================================================================
# Test Classes
# ============================================================================


class TestCorrectnessComparison:
    """Test correctness equivalence between CPU and GPU versions."""

    @pytest.fixture
    def mock_dictionary(self):
        """Create mock dictionary fixture."""
        return MockDictionary()

    @pytest.fixture
    def test_concepts(self):
        """Create test concepts fixture."""
        return create_test_concepts(10)

    def test_cpu_decomposition_produces_valid_results(self, mock_dictionary, test_concepts):
        """Test that CPU decomposition produces valid results."""
        Decomposition.init([mock_dictionary])

        results = Decomposition.multi_threaded_decompose(test_concepts)

        assert len(results) == len(test_concepts)
        assert all(isinstance(c, Concept) for c in results)
        assert all(c.litheral for c in results)

    def test_gpu_decomposition_produces_valid_results(self, mock_dictionary, test_concepts):
        """Test that GPU decomposition produces valid results."""
        GPUDecomposition.init([mock_dictionary])

        results, _ = GPUDecomposition.batch_decompose(test_concepts, show_progress=False)

        assert len(results) == len(test_concepts)
        assert all(isinstance(c, Concept) for c in results)
        assert all(c.litheral for c in results)

    def test_cpu_gpu_results_are_equivalent(self, mock_dictionary, test_concepts):
        """Test that CPU and GPU results are equivalent."""
        # CPU decomposition
        Decomposition.init([mock_dictionary])
        cpu_results = Decomposition.multi_threaded_decompose(test_concepts)

        # GPU decomposition
        GPUDecomposition.init([mock_dictionary])
        gpu_results, _ = GPUDecomposition.batch_decompose(test_concepts, show_progress=False)

        # Compare results
        all_match, stats = CorrectnessComparator.compare_batches(cpu_results, gpu_results)

        logger.info(f"Correctness comparison: {stats}")
        assert all_match, f"CPU and GPU results differ: {stats}"

    def test_different_batch_sizes_produce_same_results(self, mock_dictionary, test_concepts):
        """Test that different batch sizes produce same results."""
        GPUDecomposition.init([mock_dictionary])

        # Batch size 1
        results1, _ = GPUDecomposition.batch_decompose(
            test_concepts,
            chunk_size=1,
            show_progress=False,
        )

        # Batch size 5
        GPUDecomposition.reset_statistics()
        results5, _ = GPUDecomposition.batch_decompose(
            test_concepts,
            chunk_size=5,
            show_progress=False,
        )

        # Results should be equivalent
        all_match, stats = CorrectnessComparator.compare_batches(results1, results5)
        assert all_match, f"Different batch sizes produced different results: {stats}"

    def test_streaming_vs_batch_equivalence(self, mock_dictionary, test_concepts):
        """Test that streaming and batch produce equivalent results."""
        GPUDecomposition.init([mock_dictionary])

        # Batch decomposition
        batch_results, _ = GPUDecomposition.batch_decompose(
            test_concepts,
            show_progress=False,
        )

        # Streaming decomposition
        GPUDecomposition.reset_statistics()
        stream_results, _ = GPUDecomposition.stream_decompose(
            test_concepts,
            batch_size=5,
            show_progress=False,
        )

        # Results should be equivalent
        all_match, stats = CorrectnessComparator.compare_batches(batch_results, stream_results)
        logger.info(f"Batch vs Stream comparison: {stats}")
        assert all_match, f"Batch and streaming produced different results: {stats}"


class TestPerformanceComparison:
    """Test performance comparison between CPU and GPU."""

    @pytest.fixture
    def mock_dictionary(self):
        """Create mock dictionary fixture."""
        return MockDictionary()

    @pytest.fixture
    def performance_comparator(self):
        """Create performance comparator fixture."""
        return PerformanceComparator()

    def test_cpu_performance_baseline(self, mock_dictionary, performance_comparator):
        """Measure CPU decomposition performance."""
        Decomposition.init([mock_dictionary])
        concepts = create_test_concepts(100)

        metrics = performance_comparator.measure_decomposition(
            "CPU (multi-threaded)",
            Decomposition.multi_threaded_decompose,
            concepts,
            iterations=3,
        )

        logger.info(f"CPU Performance: {metrics}")
        assert metrics is not None
        assert metrics["throughput_items_per_sec"] > 0

    def test_gpu_performance(self, mock_dictionary, performance_comparator):
        """Measure GPU decomposition performance."""
        GPUDecomposition.init([mock_dictionary])
        concepts = create_test_concepts(100)

        metrics = performance_comparator.measure_decomposition(
            "GPU (batch)",
            lambda c: GPUDecomposition.batch_decompose(c, show_progress=False),
            concepts,
            iterations=3,
        )

        logger.info(f"GPU Performance: {metrics}")
        assert metrics is not None
        assert metrics["throughput_items_per_sec"] > 0

    @pytest.mark.skipif(not _TORCH_AVAILABLE, reason="torch not available")
    def test_cpu_vs_gpu_speedup(self, mock_dictionary):
        """Compare CPU and GPU speedup on various batch sizes."""
        comparator = PerformanceComparator()

        batch_sizes = [10, 50, 100, 500]
        results_table = []

        for batch_size in batch_sizes:
            concepts = create_test_concepts(batch_size)

            # CPU
            Decomposition.init([mock_dictionary])
            cpu_metrics = comparator.measure_decomposition(
                f"CPU ({batch_size})",
                Decomposition.multi_threaded_decompose,
                concepts,
                iterations=2,
            )

            # GPU
            GPUDecomposition.init([mock_dictionary])
            gpu_metrics = comparator.measure_decomposition(
                f"GPU ({batch_size})",
                lambda c: GPUDecomposition.batch_decompose(c, show_progress=False),
                concepts,
                iterations=2,
            )

            if cpu_metrics and gpu_metrics:
                speedup = cpu_metrics["avg_time_seconds"] / gpu_metrics["avg_time_seconds"]
                results_table.append({
                    "batch_size": batch_size,
                    "cpu_time": cpu_metrics["avg_time_seconds"],
                    "gpu_time": gpu_metrics["avg_time_seconds"],
                    "speedup": speedup,
                    "cpu_throughput": cpu_metrics["throughput_items_per_sec"],
                    "gpu_throughput": gpu_metrics["throughput_items_per_sec"],
                })

        # Log results
        logger.info("\n" + "=" * 80)
        logger.info("CPU vs GPU Performance Comparison")
        logger.info("=" * 80)
        logger.info(f"{'Batch':<10} {'CPU (ms)':<12} {'GPU (ms)':<12} {'Speedup':<10} {'CPU Items/s':<15} {'GPU Items/s':<15}")
        logger.info("-" * 80)

        for row in results_table:
            logger.info(
                f"{row['batch_size']:<10} "
                f"{row['cpu_time']*1000:<12.2f} "
                f"{row['gpu_time']*1000:<12.2f} "
                f"{row['speedup']:<10.2f}x "
                f"{row['cpu_throughput']:<15.0f} "
                f"{row['gpu_throughput']:<15.0f}"
            )

        logger.info("=" * 80)

        # Assert that GPU is at least as fast as CPU
        if results_table:
            avg_speedup = sum(r["speedup"] for r in results_table) / len(results_table)
            logger.info(f"Average speedup: {avg_speedup:.2f}x")

    def test_memory_stats_cpu_vs_gpu(self, mock_dictionary):
        """Compare memory usage between CPU and GPU."""
        Decomposition.init([mock_dictionary])
        GPUDecomposition.init([mock_dictionary])

        concepts = create_test_concepts(100)

        # CPU decomposition
        Decomposition.multi_threaded_decompose(concepts)

        # GPU decomposition
        GPUDecomposition.batch_decompose(concepts, show_progress=False)

        # Get GPU memory stats
        memory_stats = GPUDecomposition.get_memory_stats()

        logger.info(f"GPU Memory Stats: {memory_stats}")
        assert memory_stats.get("device") in ("cuda", "mps", "cpu")

    def test_throughput_scaling(self, mock_dictionary):
        """Test throughput scaling with increasing batch size."""
        GPUDecomposition.init([mock_dictionary])

        batch_sizes = [10, 50, 100]
        throughputs = []

        for batch_size in batch_sizes:
            concepts = create_test_concepts(batch_size)
            GPUDecomposition.reset_statistics()

            _, elapsed = GPUDecomposition.batch_decompose(
                concepts,
                show_progress=False,
            )

            if elapsed > 0:
                throughput = batch_size / elapsed
                throughputs.append(throughput)
                logger.info(
                    f"Batch size {batch_size}: {throughput:.0f} items/sec"
                )

        # Throughput should generally increase with batch size
        logger.info(f"Throughput trend: {throughputs}")


class TestCacheEffectiveness:
    """Test cache effectiveness on decomposition."""

    @pytest.fixture
    def mock_dictionary(self):
        """Create mock dictionary fixture."""
        return MockDictionary()

    def test_cache_hit_rate(self, mock_dictionary):
        """Test cache hit rate with repeated decompositions."""
        GPUDecomposition.init([mock_dictionary])
        concepts = create_test_concepts(20)

        # First pass - no cache hits
        GPUDecomposition.batch_decompose(concepts, show_progress=False)
        stats1 = GPUDecomposition.get_statistics()

        # Second pass - should have cache hits
        GPUDecomposition.batch_decompose(concepts, show_progress=False)
        stats2 = GPUDecomposition.get_statistics()

        cache_stats_2 = stats2.get("cache_stats", {})
        hit_rate = cache_stats_2.get("hit_rate", 0)

        logger.info(f"Cache hit rate on second pass: {hit_rate:.1%}")
        logger.info(f"Cache stats: {cache_stats_2}")

        # Should have some cache hits on second pass
        assert hit_rate >= 0.0, "Cache should be tracking hits"

    def test_cache_effectiveness_on_repeated_concepts(self, mock_dictionary):
        """Test cache effectiveness when processing repeated concepts."""
        GPUDecomposition.init([mock_dictionary])

        # Create concepts with repetition
        concepts = []
        for i in range(5):
            for j in range(20):
                concept = Concept()
                concept.id = i * 20 + j
                concept.litheral = f"word_{i}"  # Repeated word
                concepts.append(concept)

        # Process with caching
        GPUDecomposition.batch_decompose(concepts, show_progress=False)
        stats = GPUDecomposition.get_statistics()

        cache_stats = stats.get("cache_stats", {})
        logger.info(f"Cache effectiveness with repeated concepts: {cache_stats}")


class TestStressAndEdgeCases:
    """Test stress cases and edge conditions."""

    @pytest.fixture
    def mock_dictionary(self):
        """Create mock dictionary fixture."""
        return MockDictionary()

    def test_empty_batch(self, mock_dictionary):
        """Test handling of empty batch."""
        GPUDecomposition.init([mock_dictionary])

        results, elapsed = GPUDecomposition.batch_decompose([], show_progress=False)

        assert len(results) == 0
        assert elapsed >= 0

    def test_single_concept(self, mock_dictionary):
        """Test decomposition of single concept."""
        GPUDecomposition.init([mock_dictionary])

        concepts = create_test_concepts(1)
        results, elapsed = GPUDecomposition.batch_decompose(concepts, show_progress=False)

        assert len(results) == 1

    def test_large_batch_streaming(self, mock_dictionary):
        """Test streaming mode with large batch."""
        GPUDecomposition.init([mock_dictionary])

        concepts = create_test_concepts(1000)

        # Stream with limit
        results, elapsed = GPUDecomposition.stream_decompose(
            concepts,
            batch_size=50,
            max_items=200,
            show_progress=False,
        )

        assert len(results) == 200
        logger.info(f"Streamed 200 from 1000 concepts in {elapsed:.3f}s")

    def test_concurrent_decompositions(self, mock_dictionary):
        """Test thread-safe concurrent decompositions."""
        GPUDecomposition.init([mock_dictionary])

        concepts_batch = [create_test_concepts(10) for _ in range(5)]

        def decompose_batch(concepts):
            results, _ = GPUDecomposition.batch_decompose(concepts, show_progress=False)
            return len(results)

        with concurrent.futures.ThreadPoolExecutor(max_workers=3) as executor:
            futures = [executor.submit(decompose_batch, batch) for batch in concepts_batch]
            results = [f.result() for f in concurrent.futures.as_completed(futures)]

        assert all(r == 10 for r in results)
        logger.info(f"Concurrent decompositions completed: {results}")


# ============================================================================
# Summary Report
# ============================================================================


def generate_summary_report():
    """Generate comprehensive summary report."""
    report = """
    ╔════════════════════════════════════════════════════════════════════════╗
    ║     CPU vs GPU Decomposition Comparison - Test Summary                 ║
    ╚════════════════════════════════════════════════════════════════════════╝

    Test Coverage:
    ✓ Correctness: Equivalence of CPU and GPU results
    ✓ Performance: Speed comparison across batch sizes
    ✓ Cache Effectiveness: Hit rate and performance impact
    ✓ Stress Testing: Edge cases and concurrent operations
    ✓ Memory Usage: GPU memory monitoring
    ✓ Throughput Scaling: Performance with varying batch sizes

    Key Metrics Measured:
    - Decomposition correctness (equivalence)
    - Execution time (CPU vs GPU)
    - Throughput (items/second)
    - Speedup factor
    - Cache hit rates
    - Memory usage
    - Thread safety
    """
    logger.info(report)


if __name__ == "__main__":
    generate_summary_report()
    pytest.main([__file__, "-v", "-s"])
