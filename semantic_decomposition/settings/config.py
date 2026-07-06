from __future__ import annotations
import configparser
import os
from enum import Enum
from pathlib import Path
from typing import List, Optional


class Language(Enum):
    EN = "EN"
    GER = "GER"


_CONFIG_DIR = Path.home() / ".decomposition"
_CONFIG_FILE = _CONFIG_DIR / "decomposition.cfg"


_ALL_DICTIONARIES = ["wordnet", "wiktionary", "wikidata"]


class Config:
    """Singleton reading from ~/.decomposition/decomposition.cfg."""

    _instance: Optional["Config"] = None

    def __init__(self) -> None:
        self.language: Language = Language.EN
        self.wiktionary_path: str = ""
        self.primes_dir: str = str(_CONFIG_DIR / "primes")
        self._stop_words: List[str] = []
        self.dictionaries: List[str] = list(_ALL_DICTIONARIES)
        self._load()

    @classmethod
    def get_instance(cls) -> "Config":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def _load(self) -> None:
        if not _CONFIG_FILE.exists():
            return
        parser = configparser.ConfigParser()
        parser.read(str(_CONFIG_FILE))
        section = "decomposition"
        if parser.has_section(section):
            lang = parser.get(section, "language", fallback="EN")
            self.language = Language[lang.upper()]
            self.wiktionary_path = parser.get(section, "wiktionary_path", fallback="")
            self.primes_dir = parser.get(
                section, "primes_dir", fallback=str(_CONFIG_DIR / "primes")
            )
            raw_stop = parser.get(section, "stop_words", fallback="")
            self._stop_words = [w.strip() for w in raw_stop.split(",") if w.strip()]
            raw_dicts = parser.get(section, "dictionaries", fallback="")
            if raw_dicts.strip():
                self.dictionaries = [d.strip().lower() for d in raw_dicts.split(",") if d.strip()]

    def primes_words(self) -> List[str]:
        path = Path(self.primes_dir) / "primes.txt"
        if not path.exists():
            return []
        return [line.strip() for line in path.read_text().splitlines() if line.strip()]

    def stop_words(self) -> List[str]:
        return self._stop_words

    def filter_primes(self, words: List[str]) -> List[str]:
        primes = set(self.primes_words())
        return [w for w in words if w not in primes]
