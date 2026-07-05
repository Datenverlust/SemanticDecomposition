from __future__ import annotations
import os
import pickle
from pathlib import Path
from typing import Dict, List, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from ..concept import Concept

_CACHE_DIR = Path.home() / ".decomposition" / "ConceptCache"


class ConceptCache:
    """Singleton LRU-style in-memory cache backed by a file-based store."""

    _instance: Optional["ConceptCache"] = None

    def __init__(self, max_size: int = 100) -> None:
        self._max_size = max_size
        self._cache: Dict[int, "Concept"] = {}
        _CACHE_DIR.mkdir(parents=True, exist_ok=True)
        self._cache_file = _CACHE_DIR / "cache.pkl"
        self._load()

    @classmethod
    def get_instance(cls) -> "ConceptCache":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def get(self, key: int) -> Optional["Concept"]:
        return self._cache.get(key)

    def put(self, concept: "Concept") -> None:
        if len(self._cache) >= self._max_size:
            oldest = next(iter(self._cache))
            del self._cache[oldest]
        self._cache[concept.id] = concept

    def put_all(self, concepts: List["Concept"]) -> None:
        for concept in concepts:
            self.put(concept)

    def clean_up(self) -> None:
        self._save()

    def _load(self) -> None:
        if self._cache_file.exists():
            try:
                with open(self._cache_file, "rb") as f:
                    self._cache = pickle.load(f)
            except Exception:
                self._cache = {}

    def _save(self) -> None:
        with open(self._cache_file, "wb") as f:
            pickle.dump(self._cache, f)
