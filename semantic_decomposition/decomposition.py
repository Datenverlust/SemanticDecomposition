from __future__ import annotations
import hashlib
import concurrent.futures
from typing import Iterable, List, Optional, Set, TYPE_CHECKING

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
        cls._ensure_concept_id(concept)
        return concept

    @classmethod
    def decompose(
        cls,
        concept: Concept,
        depth: Optional[int] = None,
        _visited_ids: Optional[Set[int]] = None,
    ) -> Concept:
        target_depth = depth if depth is not None else DecompositionConfig.decomposition_depth
        if target_depth < 1:
            raise ValueError(f"Depth must be >= 1, got {target_depth}")

        cls._ensure_concept_id(concept)

        if _visited_ids is None:
            _visited_ids = set()
        if concept.id in _visited_ids:
            return concept

        visited_next = set(_visited_ids)
        visited_next.add(concept.id)

        cached = cls.concept_cache.get(concept.id)
        if cached is not None:
            concept = cached
        else:
            for dictionary in cls.dictionaries:
                try:
                    concept = dictionary.fill_concept(concept)
                except Exception:
                    continue
            cls.concept_cache.put(concept)

        if target_depth == 1:
            return concept

        # Depth > 1: iteratively decompose all concepts from the previous level.
        concept.synonyms = cls._decompose_relation_list(
            concept.synonyms, target_depth - 1, visited_next
        )
        concept.antonyms = cls._decompose_relation_list(
            concept.antonyms, target_depth - 1, visited_next
        )
        concept.hypernyms = cls._decompose_relation_list(
            concept.hypernyms, target_depth - 1, visited_next
        )
        concept.hyponyms = cls._decompose_relation_list(
            concept.hyponyms, target_depth - 1, visited_next
        )
        concept.meronyms = cls._decompose_relation_list(
            concept.meronyms, target_depth - 1, visited_next
        )
        concept.derivations = cls._decompose_relation_list(
            concept.derivations, target_depth - 1, visited_next
        )

        for relation_name, relation_concepts in list(concept.arbitrary_relations.items()):
            concept.arbitrary_relations[relation_name] = cls._decompose_relation_list(
                relation_concepts, target_depth - 1, visited_next
            )

        for definition in concept.definitions:
            if isinstance(definition, Definition):
                definition.concepts = cls._decompose_relation_list(
                    definition.concepts, target_depth - 1, visited_next
                )

        return concept

    @classmethod
    def _decompose_relation_list(
        cls,
        concepts: Optional[Iterable[Concept]],
        depth: int,
        visited_ids: Set[int],
    ) -> List[Concept]:
        if not concepts:
            return []

        result: List[Concept] = []
        for related in concepts:
            cls._ensure_concept_id(related)
            result.append(cls.decompose(related, depth=depth, _visited_ids=visited_ids))
        return result

    @classmethod
    def _ensure_concept_id(cls, concept: Concept) -> None:
        if getattr(concept, "id", 0):
            return

        literal = (concept.litheral or "").strip().lower()
        word_type = (
            concept.word_type.name
            if isinstance(concept.word_type, WordType)
            else str(concept.word_type or "")
        )
        raw = f"{literal}|{word_type}"
        digest = hashlib.blake2b(raw.encode("utf-8"), digest_size=8).digest()
        concept.id = int.from_bytes(digest, byteorder="big", signed=False)

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
