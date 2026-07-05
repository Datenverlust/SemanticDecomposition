"""Corpus-based linguistic statistics dictionary implementation.

This module provides a dictionary backend that leverages corpus linguistic
statistics to enrich semantic decomposition with frequency data, co-occurrence
metrics, and similarity scores.
"""
from __future__ import annotations

import logging
from pathlib import Path
from typing import Dict, List, Optional, Set, Tuple, TYPE_CHECKING

from .base_dictionary import BaseDictionary
from .dictionary import Dictionary
from ..concept import Concept
from ..definition import Definition

if TYPE_CHECKING:
    pass

logger = logging.getLogger(__name__)

try:
    import numpy as np
    _NUMPY_AVAILABLE = True
except ImportError:
    _NUMPY_AVAILABLE = False


class CorpusStatistics:
    """Container for corpus linguistic statistics."""

    def __init__(self):
        """Initialize corpus statistics container."""
        # Word frequency: word -> count
        self.frequencies: Dict[str, int] = {}

        # Co-occurrence: (word1, word2) -> count
        self.cooccurrences: Dict[Tuple[str, str], int] = {}

        # Semantic similarity: (word1, word2) -> similarity_score (0-1)
        self.similarities: Dict[Tuple[str, str], float] = {}

        # Word vectors: word -> numpy array
        self.word_vectors: Dict[str, np.ndarray] = {} if _NUMPY_AVAILABLE else {}

        # Context window size used
        self.context_window: int = 5

        # Total corpus tokens
        self.total_tokens: int = 0

        # Unique words
        self.vocabulary_size: int = 0

    def get_frequency(self, word: str) -> int:
        """Get word frequency (count in corpus).

        Parameters
        ----------
        word : str
            The word to look up.

        Returns
        -------
        int
            The frequency count, or 0 if not found.
        """
        return self.frequencies.get(word.lower(), 0)

    def get_relative_frequency(self, word: str) -> float:
        """Get relative word frequency (frequency / total tokens).

        Parameters
        ----------
        word : str
            The word to look up.

        Returns
        -------
        float
            The relative frequency (0-1), or 0 if not found.
        """
        if self.total_tokens == 0:
            return 0.0
        return self.get_frequency(word) / self.total_tokens

    def get_cooccurrence_count(self, word1: str, word2: str) -> int:
        """Get co-occurrence count between two words.

        Parameters
        ----------
        word1 : str
            First word.
        word2 : str
            Second word.

        Returns
        -------
        int
            The co-occurrence count, or 0 if not found.
        """
        w1, w2 = word1.lower(), word2.lower()
        key1 = (w1, w2)
        key2 = (w2, w1)
        return max(
            self.cooccurrences.get(key1, 0),
            self.cooccurrences.get(key2, 0),
        )

    def get_similarity(self, word1: str, word2: str) -> float:
        """Get semantic similarity between two words.

        Parameters
        ----------
        word1 : str
            First word.
        word2 : str
            Second word.

        Returns
        -------
        float
            Similarity score (0-1), or 0 if not found.
        """
        w1, w2 = word1.lower(), word2.lower()
        if w1 == w2:
            return 1.0
        key1 = (w1, w2)
        key2 = (w2, w1)
        return max(
            self.similarities.get(key1, 0.0),
            self.similarities.get(key2, 0.0),
        )


class CorpusLinguisticStatisticsDictionary(BaseDictionary):
    """Dictionary backed by corpus linguistic statistics.

    This dictionary provides:
    - Word frequency information
    - Co-occurrence statistics
    - Semantic similarity metrics
    - Context-based concept enrichment

    The corpus is expected to be pre-processed and indexed for efficient lookup.
    """

    _statistics: Optional[CorpusStatistics] = None
    _index_path: Optional[Path] = None
    _initialized: bool = False

    @classmethod
    def init(cls, statistics_path: Optional[str | Path] = None) -> None:
        """Initialize the corpus statistics dictionary.

        Parameters
        ----------
        statistics_path : str | Path, optional
            Path to the corpus statistics index. If None, will attempt to load
            from default locations or initialize empty.
        """
        super().init()

        if cls._initialized:
            return

        if statistics_path is not None:
            cls._index_path = Path(statistics_path)

        cls._statistics = CorpusStatistics()

        if cls._index_path and cls._index_path.exists():
            cls._load_statistics(cls._index_path)

        cls._initialized = True
        logger.debug(f"Initialized CorpusLinguisticStatisticsDictionary with {
            cls._statistics.vocabulary_size} unique words")

    @classmethod
    def _load_statistics(cls, path: Path) -> None:
        """Load corpus statistics from file.

        Parameters
        ----------
        path : Path
            Path to the statistics file (currently supports pickle/JSON).
        """
        if not cls._statistics:
            return

        try:
            import json

            if path.suffix == ".json":
                with open(path) as f:
                    data = json.load(f)
                    cls._statistics.frequencies = data.get("frequencies", {})
                    cls._statistics.cooccurrences = {
                        tuple(k.split("|")): v
                        for k, v in data.get("cooccurrences", {}).items()
                    }
                    cls._statistics.similarities = {
                        tuple(k.split("|")): v
                        for k, v in data.get("similarities", {}).items()
                    }
                    cls._statistics.total_tokens = data.get("total_tokens", 0)
                    cls._statistics.vocabulary_size = len(
                        cls._statistics.frequencies
                    )
                logger.debug(f"Loaded corpus statistics from {path}")
        except Exception as e:
            logger.warning(f"Failed to load statistics from {path}: {e}")

    def get_synonyms(self, word: str) -> List[Concept]:
        """Get synonyms based on co-occurrence and similarity.

        Parameters
        ----------
        word : str
            The word to find synonyms for.

        Returns
        -------
        list[Concept]
            List of synonym concepts, ranked by similarity.
        """
        if not self._statistics:
            return []

        synonyms = []
        word_lower = word.lower()

        # Find words with high co-occurrence or similarity
        for candidate, similarity in self._statistics.similarities.items():
            if word_lower in candidate and similarity > 0.7:
                other_word = (
                    candidate[1] if candidate[0] == word_lower else candidate[0]
                )
                concept = Concept()
                concept.litheral = other_word
                concept.confidence = similarity
                synonyms.append(concept)

        return sorted(synonyms, key=lambda c: c.confidence, reverse=True)

    def get_antonyms(self, word: str) -> List[Concept]:
        """Get antonyms if available in corpus.

        This is a stub implementation - full antonym detection would require
        additional semantic analysis.

        Parameters
        ----------
        word : str
            The word to find antonyms for.

        Returns
        -------
        list[Concept]
            Empty list (antonym detection not yet implemented).
        """
        return []

    def get_hypernyms(self, word: str) -> List[Concept]:
        """Get hypernyms (more general concepts).

        This is a stub implementation - full hypernym detection would require
        additional semantic analysis.

        Parameters
        ----------
        word : str
            The word to find hypernyms for.

        Returns
        -------
        list[Concept]
            Empty list (hypernym detection not yet implemented).
        """
        return []

    def get_hyponyms(self, word: str) -> List[Concept]:
        """Get hyponyms (more specific concepts).

        This is a stub implementation - full hyponym detection would require
        additional semantic analysis.

        Parameters
        ----------
        word : str
            The word to find hyponyms for.

        Returns
        -------
        list[Concept]
            Empty list (hyponym detection not yet implemented).
        """
        return []

    def get_meronyms(self, word: str) -> List[Concept]:
        """Get meronyms (part-of relationships).

        This is a stub implementation.

        Parameters
        ----------
        word : str
            The word to find meronyms for.

        Returns
        -------
        list[Concept]
            Empty list (meronym detection not yet implemented).
        """
        return []

    def get_definitions(self, word: str) -> List[Definition]:
        """Get definitions based on context in corpus.

        Parameters
        ----------
        word : str
            The word to get definitions for.

        Returns
        -------
        list[Definition]
            List of definition concepts based on co-occurrence.
        """
        if not self._statistics:
            return []

        definitions = []
        word_lower = word.lower()
        definition_concepts = []

        # Build definition from high co-occurring words
        for (w1, w2), count in sorted(
            self._statistics.cooccurrences.items(),
            key=lambda x: x[1],
            reverse=True,
        )[:10]:
            if w1 == word_lower:
                concept = Concept()
                concept.litheral = w2
                concept.confidence = min(count / max(1, self._statistics.get_frequency(word)), 1.0)
                definition_concepts.append(concept)
            elif w2 == word_lower:
                concept = Concept()
                concept.litheral = w1
                concept.confidence = min(count / max(1, self._statistics.get_frequency(word)), 1.0)
                definition_concepts.append(concept)

        if definition_concepts:
            definitions.append(Definition(definition_concepts))

        return definitions

    def fill_concept(self, concept: Concept) -> Concept:
        """Fill a concept with corpus statistics data.

        Parameters
        ----------
        concept : Concept
            The concept to enrich.

        Returns
        -------
        Concept
            The enriched concept.
        """
        concept = self.set_lemma(concept)
        concept = self.set_pos(concept)

        # Add frequency information
        if self._statistics:
            frequency = self._statistics.get_frequency(concept.litheral)
            rel_freq = self._statistics.get_relative_frequency(
                concept.litheral
            )

            if frequency > 0:
                concept.frequency = frequency
                concept.relative_frequency = rel_freq

        concept = self.fill_related(concept)
        return concept

    def get_lemma(self, word: str) -> str:
        """Get lemma for a word.

        Parameters
        ----------
        word : str
            The word to lemmatize.

        Returns
        -------
        str
            The lemma.
        """
        if self._nlp is not None:
            doc = self._nlp(word)
            if doc:
                return doc[0].lemma_
        return word

    def get_concept(self, word: str) -> Concept:
        """Get a concept for a word.

        Parameters
        ----------
        word : str
            The word.

        Returns
        -------
        Concept
            A concept for the word.
        """
        concept = Concept()
        concept.litheral = word
        return self.fill_concept(concept)

    def set_pos(self, concept: Concept) -> Concept:
        """Set part-of-speech for a concept.

        Parameters
        ----------
        concept : Concept
            The concept to enrich.

        Returns
        -------
        Concept
            The enriched concept.
        """
        if self._nlp is not None:
            doc = self._nlp(concept.litheral)
            if doc:
                concept.pos = doc[0].pos_
        return concept

    def fill_definition(self, definition: Definition) -> Definition:
        """Enrich a definition with additional concepts.

        Parameters
        ----------
        definition : Definition
            The definition to enrich.

        Returns
        -------
        Definition
            The enriched definition.
        """
        for concept in definition.concepts:
            self.fill_concept(concept)
        return definition

    def fill_related(self, concept: Concept) -> Concept:
        """Fill in related concepts (synonyms, etc.).

        Parameters
        ----------
        concept : Concept
            The concept to enrich.

        Returns
        -------
        Concept
            The enriched concept.
        """
        concept.synonyms = self.get_synonyms(concept.litheral)
        concept.definitions = self.get_definitions(concept.litheral)
        return concept
