from __future__ import annotations
import concurrent.futures
from typing import Dict, List, Optional, Set, TYPE_CHECKING

from .concept import Concept
from .definition import Definition
from .word_type import WordType
from .decomposition_config import DecompositionConfig
from .persistence.concept_cache import ConceptCache

if TYPE_CHECKING:
    from .dictionaries.dictionary import Dictionary


class Decomposition:
    """
    Main entry point for semantic decomposition.

    Manages a list of Dictionary backends and a ConceptCache.
    Provides single- and multi-threaded concept decomposition.
    """

    dictionaries: List["Dictionary"] = []
    concept_cache: ConceptCache = ConceptCache.get_instance()
    n_sm_primes: List[str] = []
    concepts_to_ignore: Set[str] = set()

    @classmethod
    def init(cls, dictionaries: List["Dictionary"]) -> None:
        cls.dictionaries = dictionaries
        for d in dictionaries:
            if hasattr(d, "init"):
                d.init()

    @classmethod
    def create_concept(cls, litheral: str, word_type: Optional[WordType] = None) -> Concept:
        concept = Concept()
        concept.litheral = litheral
        concept.word_type = word_type
        return concept

    @classmethod
    def decompose(cls, concept: Concept) -> Concept:
        cached = cls.concept_cache.get(concept.id)
        if cached is not None:
            return cached
        for dictionary in cls.dictionaries:
            try:
                concept = dictionary.fill_concept(concept)
            except Exception:
                continue
        cls.concept_cache.put(concept)
        return concept

    @classmethod
    def multi_threaded_decompose(cls, concepts: List[Concept]) -> List[Concept]:
        with concurrent.futures.ThreadPoolExecutor(
            max_workers=DecompositionConfig.thread_count
        ) as executor:
            futures = {executor.submit(cls.decompose, c): c for c in concepts}
            results = []
            for future in concurrent.futures.as_completed(futures):
                try:
                    results.append(future.result())
                except Exception:
                    results.append(futures[future])
        return results

    @classmethod
    def check_is_prime(cls, word: str) -> bool:
        return word in cls.n_sm_primes

    @classmethod
    def get_prime_of_concept(cls, concept: Concept) -> Optional[str]:
        if cls.check_is_prime(concept.litheral):
            return concept.litheral
        return None
