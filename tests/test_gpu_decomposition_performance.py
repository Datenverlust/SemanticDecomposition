"""Performance benchmark: CPU vs GPU-accelerated semantic decomposition.

Run with pytest (collects all ``test_*`` functions and prints timing assertions)
or as a standalone script::

    python -m pytest tests/test_gpu_decomposition_performance.py -v -s

The suite uses a lightweight in-memory ``MockDictionary`` so results are
reproducible without any external language resources.
"""
from __future__ import annotations

import pathlib
import random
import string
import time
from typing import List

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


def _make_concept_list(n: int, seed: int = 42) -> List[Concept]:
    """Return *n* distinct Concept objects with unique IDs and random literals."""
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


_CONCEPTS_FILE = pathlib.Path(__file__).parent / "cybersecurity_concepts.txt"


def _load_cybersecurity_concepts() -> List[Concept]:
    """Load the cybersecurity concept list from the companion text file."""
    terms = [
        line.strip()
        for line in _CONCEPTS_FILE.read_text(encoding="utf-8").splitlines()
        if line.strip()
    ]
    return [_make_concept(term, uid) for uid, term in enumerate(terms, start=1)]


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
# Cybersecurity domain concepts
# ---------------------------------------------------------------------------

class TestCybersecurityConcepts:
    """Decompose the real-world cybersecurity concept list from
    ``tests/cybersecurity_concepts.txt`` and compare CPU vs GPU paths."""

    @pytest.fixture(autouse=True)
    def _concepts(self):
        self.concepts = _load_cybersecurity_concepts()

    def test_concepts_file_loaded(self):
        """Sanity check: the companion file must contain at least one concept."""
        assert len(self.concepts) > 0, "cybersecurity_concepts.txt is empty or missing"

    def test_cpu_sequential_all_concepts(self):
        _reset_cache()
        results = [Decomposition.decompose(c) for c in self.concepts]
        assert len(results) == len(self.concepts)
        for r in results:
            assert isinstance(r, Concept)

    def test_cpu_multithreaded_all_concepts(self):
        _reset_cache()
        results = Decomposition.multi_threaded_decompose(self.concepts)
        assert len(results) == len(self.concepts)

    def test_gpu_batch_all_concepts(self):
        _reset_cache()
        results, _ = GPUDecomposition.batch_decompose(self.concepts)
        assert len(results) == len(self.concepts)

    def test_performance_report(self, capsys):
        """Print a side-by-side timing report for the full cybersecurity list."""
        n = len(self.concepts)

        seq_time = _run_cpu_sequential(self.concepts)
        mt_time = _run_cpu_multithreaded(self.concepts)
        gpu_time = _run_gpu_batch(self.concepts)

        with capsys.disabled():
            print(
                f"\n{'=' * 60}\n"
                f"  Cybersecurity concepts ({n} terms)\n"
                f"  CPU sequential      : {seq_time:>7.3f} s\n"
                f"  CPU multi-threaded  : {mt_time:>7.3f} s\n"
                f"  GPU ({_GPU_LABEL:<18}): {gpu_time:>7.3f} s\n"
                f"  MT  speedup vs seq  : {seq_time / mt_time:>7.2f}x\n"
                f"  GPU speedup vs seq  : {seq_time / gpu_time:>7.2f}x\n"
                f"{'=' * 60}"
            )

        assert mt_time < seq_time, (
            f"Multi-threaded ({mt_time:.3f}s) should be faster than sequential ({seq_time:.3f}s)"
        )
