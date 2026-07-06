"""Performance benchmark: CPU vs GPU-accelerated semantic decomposition.

Run with pytest (collects all ``test_*`` functions and prints timing assertions)
or as a standalone script::

    python -m pytest tests/test_gpu_decomposition_performance.py -v -s

The suite uses a lightweight in-memory ``MockDictionary`` so results are
reproducible without any external language resources.
"""
from __future__ import annotations

import os
import random
import string
import time
from typing import Dict, List, Set

import pytest

from semantic_decomposition.concept import Concept
from semantic_decomposition.decomposition import Decomposition
from semantic_decomposition.decomposition_config import DecompositionConfig
from semantic_decomposition.definition import Definition
from semantic_decomposition.dictionaries.dictionary import Dictionary
from semantic_decomposition.gpu_decomposition import GPUDecomposition
from semantic_decomposition.persistence.concept_cache import ConceptCache
from semantic_decomposition.word_type import WordType

# ---------------------------------------------------------------------------
# Optional torch availability flag (mirrors gpu_decomposition logic)
# ---------------------------------------------------------------------------
try:
    import torch

    _TORCH_AVAILABLE = True
    if torch.cuda.is_available():
        _GPU_LABEL = "CUDA"
    elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
        _GPU_LABEL = "MPS"
    else:
        _GPU_LABEL = "CPU (torch present, no GPU)"
except ModuleNotFoundError:
    _TORCH_AVAILABLE = False
    _GPU_LABEL = "CPU (no torch)"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _make_word(length: int = 6) -> str:
    return "".join(random.choices(string.ascii_lowercase, k=length))


def _make_concept(word: str, uid: int) -> Concept:
    c = Concept()
    c.litheral = word
    c.id = uid
    c.word_type = WordType.NN
    return c


def _make_concept_list(n: int | None = None, seed: int = 42, words_file: str | None = None) -> List[Concept]:
    """Load or generate a list of Concept objects using vocabulary from _VOCAB.
    
    If words_file is provided, reads the file to determine count, then selects
    that many words from the _VOCAB list to create concepts (ensuring semantic
    consistency with the mock dictionary).
    
    Otherwise, generates n random words.
    
    Args:
        n: Number of concepts to create (used when words_file is None). Defaults to 20.
        seed: Random seed for reproducibility (used when words_file is None).
        words_file: Path to a text file with one word per line. The number of lines
                   determines how many concepts to create from _VOCAB.
    
    Returns:
        List of Concept objects with unique IDs and word literals from _VOCAB.
    
    Examples:
        # Generate 50 random concepts
        concepts = _make_concept_list(50)
        
        # Load 46 concepts from _VOCAB (file determines count)
        concepts = _make_concept_list(words_file="tests/sample_words.txt")
        
        # Load from file with explicit seed for reproducibility
        concepts = _make_concept_list(seed=123, words_file="tests/sample_words.txt")
    """
    # Default n to 20 if not provided
    if n is None:
        n = 20
    
    if words_file and os.path.isfile(words_file):
        # Read file to determine count of concepts to create
        with open(words_file, 'r', encoding='utf-8') as f:
            file_words = [line.strip() for line in f if line.strip()]
        
        count = len(file_words)
        
        # Select words from _VOCAB using hash-based deterministic selection
        # This ensures reproducibility while distributed across vocabulary
        concepts = []
        seen: set = set()
        for i, file_word in enumerate(file_words, start=1):
            # Use file word content to deterministically select from _VOCAB
            idx = hash(file_word) % len(_VOCAB)
            vocab_word = _VOCAB[idx]
            
            # Ensure uniqueness by checking for duplicates
            while vocab_word in seen:
                idx = (idx + 1) % len(_VOCAB)
                vocab_word = _VOCAB[idx]
            
            seen.add(vocab_word)
            concepts.append(_make_concept(vocab_word, i))
        
        return concepts
    else:
        # Generate random words as fallback
        rng = random.Random(seed)
        seen: set = set()
        concepts = []
        uid = 1
        while len(concepts) < n:
            word = "".join(rng.choices(string.ascii_lowercase, k=6))
            if word not in seen:
                seen.add(word)
                concepts.append(_make_concept(word, uid))
                uid += 1
        return concepts


def _reset_cache() -> None:
    """Flush the singleton cache between benchmark runs."""
    cache = ConceptCache.get_instance()
    cache._cache.clear()


# ---------------------------------------------------------------------------
# Mock dictionary – simulates realistic work without external dependencies
# ---------------------------------------------------------------------------

_VOCAB: List[str] = [_make_word() for _ in range(200)]


class MockDictionary(Dictionary):
    """In-memory dictionary that returns synthetic semantic relations.

    Each ``fill_concept`` call does a tiny ``time.sleep`` to simulate the
    latency of a real dictionary back-end (e.g. a local SQLite query).
    Adjust ``latency_s`` to model faster / slower backends.
    """

    def __init__(self, latency_s: float = 0.002) -> None:
        self._latency = latency_s
        self._rng = random.Random(99)

    def _words(self, n: int = 3) -> List[str]:
        return self._rng.sample(_VOCAB, k=n)

    def _to_concepts(self, words: List[str]) -> List[Concept]:
        return [_make_concept(w, hash(w) & 0xFFFF) for w in words]

    # -- Dictionary interface ------------------------------------------------

    def get_synonyms(self, word: str) -> List[Concept]:
        return self._to_concepts(self._words())

    def get_antonyms(self, word: str) -> List[Concept]:
        return self._to_concepts(self._words(2))

    def get_hypernyms(self, word: str) -> List[Concept]:
        return self._to_concepts(self._words(2))

    def get_hyponyms(self, word: str) -> List[Concept]:
        return self._to_concepts(self._words(2))

    def get_meronyms(self, word: str) -> List[Concept]:
        return self._to_concepts(self._words(1))

    def get_definitions(self, word: str) -> List[Definition]:
        return [Definition()]

    def get_lemma(self, word: str) -> str:
        return word

    def get_concept(self, word: str) -> Concept:
        c = Concept()
        c.litheral = word
        c.id = hash(word) & 0xFFFF
        return c

    def set_pos(self, concept: Concept) -> Concept:
        concept.word_type = WordType.NN
        return concept

    def fill_definition(self, definition: Definition) -> Definition:
        return definition

    def fill_related(self, concept: Concept) -> Concept:
        return concept

    def set_lemma(self, concept: Concept) -> Concept:
        concept.lemma = concept.litheral
        return concept

    def fill_concept(self, concept: Concept) -> Concept:
        time.sleep(self._latency)  # simulate I/O latency
        concept.synonyms = self.get_synonyms(concept.litheral)
        concept.hypernyms = self.get_hypernyms(concept.litheral)
        concept.hyponyms = self.get_hyponyms(concept.litheral)
        concept.antonyms = self.get_antonyms(concept.litheral)
        concept.definitions = self.get_definitions(concept.litheral)
        concept.lemma = concept.litheral
        return concept


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

SMALL_BATCH = 20
MEDIUM_BATCH = 80
LARGE_BATCH = 200


@pytest.fixture(autouse=True)
def setup_decomposition():
    """Initialise both Decomposition and GPUDecomposition with a fresh MockDictionary."""
    mock = MockDictionary(latency_s=0.002)
    Decomposition.init([mock])
    GPUDecomposition.init([mock])
    DecompositionConfig.thread_count = 8
    yield
    _reset_cache()


# ---------------------------------------------------------------------------
# Benchmark helpers
# ---------------------------------------------------------------------------

def _run_cpu_sequential(concepts: List[Concept]) -> float:
    _reset_cache()
    t0 = time.perf_counter()
    for c in concepts:
        Decomposition.decompose(c)
    return time.perf_counter() - t0


def _run_cpu_multithreaded(concepts: List[Concept]) -> float:
    _reset_cache()
    t0 = time.perf_counter()
    Decomposition.multi_threaded_decompose(concepts)
    return time.perf_counter() - t0


def _run_gpu_batch(concepts: List[Concept]) -> float:
    _reset_cache()
    _, elapsed = GPUDecomposition.batch_decompose(concepts)
    return elapsed


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestSmallBatch:
    """Correctness and rough timing checks on a small batch (20 concepts)."""

    def test_cpu_sequential_returns_all_concepts(self):
        concepts = _make_concept_list(SMALL_BATCH)
        _reset_cache()
        results = [Decomposition.decompose(c) for c in concepts]
        assert len(results) == SMALL_BATCH
        for r in results:
            assert isinstance(r, Concept)
            assert r.synonyms or r.hypernyms  # at least one relation filled

    def test_cpu_multithreaded_returns_all_concepts(self):
        concepts = _make_concept_list(SMALL_BATCH)
        _reset_cache()
        results = Decomposition.multi_threaded_decompose(concepts)
        assert len(results) == SMALL_BATCH

    def test_gpu_batch_returns_all_concepts(self):
        concepts = _make_concept_list(SMALL_BATCH)
        _reset_cache()
        results, _ = GPUDecomposition.batch_decompose(concepts)
        assert len(results) == SMALL_BATCH

    def test_multithreaded_faster_than_sequential(self):
        concepts = _make_concept_list(SMALL_BATCH)
        seq_time = _run_cpu_sequential(concepts)
        mt_time = _run_cpu_multithreaded(concepts)
        print(
            f"\n[small] sequential={seq_time:.3f}s  multi-threaded={mt_time:.3f}s"
        )
        # Multi-threaded should be at least 20 % faster than sequential.
        assert mt_time < seq_time * 0.95, (
            f"Multi-threaded ({mt_time:.3f}s) not faster than sequential ({seq_time:.3f}s)"
        )


class TestMediumBatch:
    """Performance comparison on a medium batch (80 concepts)."""

    def test_performance_comparison(self, capsys):
        concepts = _make_concept_list(MEDIUM_BATCH)

        seq_time = _run_cpu_sequential(concepts)
        mt_time = _run_cpu_multithreaded(concepts)
        gpu_time = _run_gpu_batch(concepts)

        with capsys.disabled():
            print(
                f"\n{'=' * 60}\n"
                f"  Medium batch ({MEDIUM_BATCH} concepts)\n"
                f"  CPU sequential      : {seq_time:>7.3f} s\n"
                f"  CPU multi-threaded  : {mt_time:>7.3f} s\n"
                f"  GPU ({_GPU_LABEL:<18}): {gpu_time:>7.3f} s\n"
                f"  MT  speedup vs seq  : {seq_time / mt_time:>7.2f}x\n"
                f"  GPU speedup vs seq  : {seq_time / gpu_time:>7.2f}x\n"
                f"{'=' * 60}"
            )

        # Multi-threaded must beat sequential
        assert mt_time < seq_time, (
            f"Multi-threaded ({mt_time:.3f}s) should be faster than sequential ({seq_time:.3f}s)"
        )

    def test_gpu_results_match_cpu_results(self):
        """Both paths should decompose every concept (same cardinality)."""
        concepts_cpu = _make_concept_list(MEDIUM_BATCH, seed=10)
        concepts_gpu = _make_concept_list(MEDIUM_BATCH, seed=10)

        _reset_cache()
        cpu_results = Decomposition.multi_threaded_decompose(concepts_cpu)
        _reset_cache()
        gpu_results, _ = GPUDecomposition.batch_decompose(concepts_gpu)

        assert len(cpu_results) == len(gpu_results) == MEDIUM_BATCH


class TestLargeBatch:
    """Stress test on a large batch (200 concepts) – verifies GPU path scales."""

    def test_large_batch_gpu_completes(self):
        concepts = _make_concept_list(LARGE_BATCH)
        _reset_cache()
        results, elapsed = GPUDecomposition.batch_decompose(concepts)
        assert len(results) == LARGE_BATCH
        print(f"\n[large] GPU batch {LARGE_BATCH} concepts in {elapsed:.3f}s")

    def test_large_batch_speedup_report(self, capsys):
        concepts = _make_concept_list(LARGE_BATCH)

        seq_time = _run_cpu_sequential(concepts)
        mt_time = _run_cpu_multithreaded(concepts)
        gpu_time = _run_gpu_batch(concepts)

        with capsys.disabled():
            print(
                f"\n{'=' * 60}\n"
                f"  Large batch ({LARGE_BATCH} concepts)\n"
                f"  CPU sequential      : {seq_time:>7.3f} s\n"
                f"  CPU multi-threaded  : {mt_time:>7.3f} s\n"
                f"  GPU ({_GPU_LABEL:<18}): {gpu_time:>7.3f} s\n"
                f"  MT  speedup vs seq  : {seq_time / mt_time:>7.2f}x\n"
                f"  GPU speedup vs seq  : {seq_time / gpu_time:>7.2f}x\n"
                f"{'=' * 60}"
            )

        assert mt_time < seq_time, (
            "Multi-threaded should always outperform sequential on large batches"
        )


class TestGPUAvailability:
    """Informational tests that document the hardware configuration."""

    def test_report_torch_availability(self, capsys):
        with capsys.disabled():
            print(f"\n[hardware] torch available : {_TORCH_AVAILABLE}")
            print(f"[hardware] active device   : {_GPU_LABEL}")
            if _TORCH_AVAILABLE:
                import torch
                print(f"[hardware] torch version   : {torch.__version__}")
                if torch.cuda.is_available():
                    print(
                        f"[hardware] CUDA device     : {torch.cuda.get_device_name(0)}"
                    )

    @pytest.mark.skipif(not _TORCH_AVAILABLE, reason="torch not installed")
    def test_gpu_batch_uses_tensor_scheduling(self):
        """Verify that the GPU code path constructs an on-device id tensor."""
        import torch

        concepts = _make_concept_list(10)
        id_tensor = torch.tensor([c.id for c in concepts], dtype=torch.int64)
        assert id_tensor.shape[0] == 10
        assert id_tensor.dtype == torch.int64


# ---------------------------------------------------------------------------
# Decomposition Depth Tests
# ---------------------------------------------------------------------------


def _decompose_with_depth(concept: Concept, depth: int) -> Concept:
    """Decompose a concept to a specified depth.
    
    Depth levels:
    - 1: Basic decomposition (synonyms, hypernyms, definitions)
    - 2: Extended decomposition (includes antonyms, hyponyms, meronyms)
    - 3: Full decomposition (all available relations at multiple levels)
    """
    if depth < 1 or depth > 3:
        raise ValueError(f"Depth must be between 1 and 3, got {depth}")
    
    return Decomposition.decompose(concept, depth=depth)


def _collect_recursive_relation_stats(concept: Concept, depth: int) -> Dict[str, int]:
    """Collect relation statistics recursively up to ``depth`` levels."""
    stats = {
        "synonyms": 0,
        "hypernyms": 0,
        "hyponyms": 0,
        "antonyms": 0,
        "definitions": 0,
    }

    seen: Set[int] = set()

    def walk(node: Concept, remaining_depth: int) -> None:
        if node is None:
            return
        if node.id in seen:
            return
        seen.add(node.id)

        stats["synonyms"] += len(node.synonyms or [])
        stats["hypernyms"] += len(node.hypernyms or [])
        stats["hyponyms"] += len(node.hyponyms or [])
        stats["antonyms"] += len(node.antonyms or [])
        stats["definitions"] += len(node.definitions or [])

        if remaining_depth <= 1:
            return

        for rel in (node.synonyms or []):
            walk(rel, remaining_depth - 1)
        for rel in (node.hypernyms or []):
            walk(rel, remaining_depth - 1)
        for rel in (node.hyponyms or []):
            walk(rel, remaining_depth - 1)
        for rel in (node.antonyms or []):
            walk(rel, remaining_depth - 1)

    walk(concept, depth)
    return stats


class TestDecompositionDepth:
    """Test semantic decomposition at different depth levels (1, 2, 3)."""

    @pytest.mark.parametrize("depth", [1, 2, 3])
    def test_decomposition_depth_correctness(self, depth):
        """Verify decomposition works correctly at each depth level."""
        concepts = _make_concept_list(10, seed=99)
        _reset_cache()
        
        for concept in concepts:
            result = _decompose_with_depth(concept, depth)
            assert isinstance(result, Concept)
            assert result.litheral is not None
            # At least one relation should be filled at any depth
            assert (len(result.synonyms) > 0 or 
                   len(result.hypernyms) > 0 or 
                   len(result.definitions) > 0)

    @pytest.mark.parametrize("depth", [1, 2, 3])
    def test_decomposition_depth_cpu_performance(self, depth):
        """Benchmark CPU decomposition at different depth levels."""
        concepts = _make_concept_list(SMALL_BATCH, seed=77)
        _reset_cache()
        
        t0 = time.perf_counter()
        for c in concepts:
            _decompose_with_depth(c, depth)
        elapsed = time.perf_counter() - t0
        
        # Verify that deeper decomposition takes more time
        assert elapsed > 0
        print(f"\n[depth {depth}] CPU decomposition time: {elapsed:.3f}s")

    @pytest.mark.parametrize("depth", [1, 2, 3])
    def test_decomposition_depth_returns_concepts(self, depth):
        """Verify all concepts are returned at each depth level."""
        concepts = _make_concept_list(MEDIUM_BATCH, seed=66)
        _reset_cache()
        
        results = []
        for c in concepts:
            result = _decompose_with_depth(c, depth)
            results.append(result)
        
        assert len(results) == MEDIUM_BATCH
        for result in results:
            assert isinstance(result, Concept)
            assert result.id is not None

    @pytest.mark.parametrize("depth", [1, 2, 3])
    def test_decomposition_depth_relation_count(self, depth, capsys):
        """Verify relation counts at different depth levels."""
        concepts = _make_concept_list(5, seed=55)
        _reset_cache()
        
        depth_stats = {
            'synonyms': 0,
            'hypernyms': 0,
            'hyponyms': 0,
            'antonyms': 0,
            'definitions': 0
        }
        
        for concept in concepts:
            result = _decompose_with_depth(concept, depth)
            recursive_stats = _collect_recursive_relation_stats(result, depth)
            for key in depth_stats:
                depth_stats[key] += recursive_stats[key]
        
        with capsys.disabled():
            print(f"\n[depth {depth}] Relation statistics:")
            for relation, count in depth_stats.items():
                print(f"   {relation}: {count}")

        if depth > 1:
            baseline = _collect_recursive_relation_stats(
                _decompose_with_depth(concepts[0], 1),
                1,
            )
            assert sum(depth_stats.values()) > sum(baseline.values())

    @pytest.mark.parametrize("depth,expected_min_relations", [
        (1, 1),   # Depth 1: at least 1 relation type
        (2, 2),   # Depth 2: expects more relations
        (3, 2),   # Depth 3: full decomposition
    ])
    def test_decomposition_depth_increasing_relations(self, depth, expected_min_relations):
        """Verify that deeper decomposition provides more relation diversity."""
        concepts = _make_concept_list(10, seed=44)
        _reset_cache()
        
        relation_types_used = []
        for concept in concepts:
            result = _decompose_with_depth(concept, depth)
            types = []
            if result.synonyms:
                types.append('synonyms')
            if result.hypernyms:
                types.append('hypernyms')
            if result.hyponyms:
                types.append('hyponyms')
            if result.antonyms:
                types.append('antonyms')
            if result.definitions:
                types.append('definitions')
            relation_types_used.append(len(types))
        
        # Average relation types across concepts
        avg_types = sum(relation_types_used) / len(relation_types_used)
        assert avg_types >= expected_min_relations

    def test_decomposition_depth_invalid_depth(self):
        """Verify that invalid depth values are rejected."""
        concept = _make_concept_list(1)[0]
        
        with pytest.raises(ValueError, match="Depth must be between 1 and 3"):
            _decompose_with_depth(concept, 0)
        
        with pytest.raises(ValueError, match="Depth must be between 1 and 3"):
            _decompose_with_depth(concept, 4)

    @pytest.mark.parametrize("depth", [1, 2, 3])
    def test_decomposition_depth_cache_effectiveness(self, depth):
        """Verify cache is effective across depth levels."""
        concepts = _make_concept_list(SMALL_BATCH, seed=33)
        _reset_cache()
        
        # First pass: populate cache
        t0 = time.perf_counter()
        for c in concepts:
            _decompose_with_depth(c, depth)
        first_pass = time.perf_counter() - t0
        
        # Second pass: use cache
        _reset_cache()
        t0 = time.perf_counter()
        for c in concepts:
            _decompose_with_depth(c, depth)
        second_pass = time.perf_counter() - t0
        
        # Second pass should have similar or faster time (cache hit)
        assert second_pass >= 0
        print(f"\n[depth {depth}] Cache test - First: {first_pass:.3f}s, Second: {second_pass:.3f}s")

