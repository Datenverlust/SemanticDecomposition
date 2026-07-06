"""Wiktionary dictionary backend via the MediaWiki API.

Parses Wiktionary pages to extract definitions, synonyms, antonyms, hypernyms,
hyponyms, meronyms, and derivations.  Uses the public REST API — no API key
required, but rate-limited.
"""
from __future__ import annotations

import json
import logging
import re
import urllib.request
import urllib.parse
from typing import Dict, List, Optional, Set, TYPE_CHECKING

from .base_dictionary import BaseDictionary

if TYPE_CHECKING:
    from ..concept import Concept
    from ..definition import Definition

logger = logging.getLogger(__name__)

_API_URL = "https://en.wiktionary.org/api/rest_v1/page/definition/{word}"
_PARSE_URL = "https://en.wiktionary.org/w/api.php"


class WiktionaryDictionary(BaseDictionary):
    """Dictionary backed by English Wiktionary (online, REST API)."""

    _initialized: bool = False
    _cache: Dict[str, dict] = {}

    @classmethod
    def init(cls) -> None:
        super().init()
        if cls._initialized:
            return
        cls._cache = {}
        cls._initialized = True
        logger.debug("WiktionaryDictionary initialised")

    def _fetch(self, word: str) -> dict:
        word_lower = word.strip().lower()
        if word_lower in self._cache:
            return self._cache[word_lower]

        result: dict = {"definitions": [], "synonyms": [], "antonyms": [],
                        "hypernyms": [], "hyponyms": [], "meronyms": [],
                        "derivations": []}

        try:
            data = self._fetch_definitions(word_lower)
            if data:
                result["definitions"] = data.get("definitions", [])
        except Exception as e:
            logger.debug(f"Wiktionary definition fetch failed for '{word}': {e}")

        try:
            relations = self._fetch_relations(word_lower)
            for key in ("synonyms", "antonyms", "hypernyms", "hyponyms",
                        "meronyms", "derivations"):
                result[key] = relations.get(key, [])
        except Exception as e:
            logger.debug(f"Wiktionary relation fetch failed for '{word}': {e}")

        self._cache[word_lower] = result
        return result

    def _fetch_definitions(self, word: str) -> dict:
        url = _API_URL.format(word=urllib.parse.quote(word))
        req = urllib.request.Request(url, headers={"User-Agent": "SemanticDecomposition/0.1"})
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except Exception:
            return {}

        definitions: List[str] = []
        for lang_entry in data.get("en", []):
            for defn in lang_entry.get("definitions", []):
                text = self._strip_html(defn.get("definition", ""))
                if text and len(text) > 3:
                    definitions.append(text)
        return {"definitions": definitions}

    def _fetch_relations(self, word: str) -> Dict[str, List[str]]:
        params = urllib.parse.urlencode({
            "action": "parse",
            "page": word,
            "prop": "wikitext",
            "format": "json",
        })
        url = f"{_PARSE_URL}?{params}"
        req = urllib.request.Request(url, headers={"User-Agent": "SemanticDecomposition/0.1"})
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except Exception:
            return {}

        wikitext = data.get("parse", {}).get("wikitext", {}).get("*", "")
        if not wikitext:
            return {}

        return self._parse_wikitext_relations(wikitext)

    def _parse_wikitext_relations(self, wikitext: str) -> Dict[str, List[str]]:
        relations: Dict[str, List[str]] = {
            "synonyms": [], "antonyms": [], "hypernyms": [], "hyponyms": [],
            "meronyms": [], "derivations": [],
        }

        section_map = {
            "synonyms": "synonyms", "synonym": "synonyms",
            "antonyms": "antonyms", "antonym": "antonyms",
            "hypernyms": "hypernyms", "hypernym": "hypernyms",
            "hyponyms": "hyponyms", "hyponym": "hyponyms",
            "meronyms": "meronyms", "meronym": "meronyms",
            "holonyms": "meronyms",
            "derived terms": "derivations", "related terms": "derivations",
        }

        inline_tpl_map = {
            "synonyms": "synonyms", "syn": "synonyms", "synonym of": "synonyms",
            "antonyms": "antonyms", "ant": "antonyms",
            "hypernyms": "hypernyms", "hyper": "hypernyms",
            "hyponyms": "hyponyms", "hypo": "hyponyms",
            "meronyms": "meronyms", "mero": "meronyms",
            "holonyms": "meronyms", "holo": "meronyms",
        }

        english_text = self._extract_english_section(wikitext)
        if not english_text:
            return relations

        for tpl_name, target in inline_tpl_map.items():
            for m in re.finditer(
                r"\{\{" + re.escape(tpl_name) + r"\|en\|([^}]+)\}\}",
                english_text,
            ):
                for w in self._extract_template_args(m.group(1)):
                    if w not in relations[target]:
                        relations[target].append(w)

        current_section: Optional[str] = None
        for line in english_text.split("\n"):
            header_match = re.match(r"^(={3,6})\s*(.+?)\s*\1$", line)
            if header_match:
                heading = header_match.group(2).lower().strip()
                current_section = section_map.get(heading)
                continue

            if current_section is None:
                continue

            for m in re.finditer(r"\{\{l\|en\|([^|}]+)", line):
                word = m.group(1).strip()
                if word and word not in relations[current_section]:
                    relations[current_section].append(word)

            for m in re.finditer(r"\[\[([^\]|#]+?)(?:\|[^\]]+)?\]\]", line):
                word = m.group(1).strip()
                if (word and word not in relations[current_section]
                        and not word.startswith(("Category:", "w:", "Appendix:", "Thesaurus:"))
                        and len(word) > 1 and word[0].isalpha()):
                    relations[current_section].append(word)

        for section_key in relations:
            for m in re.finditer(
                r"\{\{col\d?\|en[\s\S]*?\}\}", english_text
            ):
                self._extract_col_words(m.group(0), relations, section_key, english_text, m.start())

        return relations

    @staticmethod
    def _extract_english_section(wikitext: str) -> str:
        m = re.search(r"^==\s*English\s*==$", wikitext, re.MULTILINE)
        if not m:
            return ""
        start = m.end()
        end_match = re.search(r"^==\s*[^=]", wikitext[start:], re.MULTILINE)
        if end_match:
            return wikitext[start:start + end_match.start()]
        return wikitext[start:]

    @staticmethod
    def _extract_template_args(args_str: str) -> List[str]:
        words: List[str] = []
        for part in args_str.split("|"):
            part = part.strip()
            if not part or "=" in part or part.startswith(("Thesaurus:", "q:", "sense")):
                continue
            clean = re.sub(r"<[^>]*>", "", part).strip()
            if clean and len(clean) > 1 and clean[0].isalpha():
                words.append(clean)
        return words

    def _extract_col_words(self, template: str, relations: Dict[str, List[str]],
                           section_key: str, full_text: str, template_pos: int) -> None:
        preceding = full_text[:template_pos]
        headers = list(re.finditer(r"^(={3,6})\s*(.+?)\s*\1$", preceding, re.MULTILINE))
        if not headers:
            return
        last_heading = headers[-1].group(2).lower().strip()
        section_map = {
            "synonyms": "synonyms", "synonym": "synonyms",
            "antonyms": "antonyms", "antonym": "antonyms",
            "hypernyms": "hypernyms", "hypernym": "hypernyms",
            "hyponyms": "hyponyms", "hyponym": "hyponyms",
            "meronyms": "meronyms", "meronym": "meronyms",
            "holonyms": "meronyms",
            "derived terms": "derivations", "related terms": "derivations",
        }
        detected_section = section_map.get(last_heading)
        if detected_section != section_key:
            return

        for part in template.split("|"):
            part = part.strip().strip("{}")
            if not part or "=" in part or part in ("en",) or part.startswith("col"):
                continue
            clean = re.sub(r"\{\{[^}]*\}\}", "", part)
            clean = re.sub(r"\[\[([^\]|]+?)(?:\|[^\]]+)?\]\]", r"\1", clean)
            clean = re.sub(r"<[^>]*>", "", clean).strip()
            if clean and len(clean) > 1 and clean[0].isalpha() and clean not in relations[section_key]:
                relations[section_key].append(clean)

    @staticmethod
    def _strip_html(text: str) -> str:
        text = re.sub(r"<[^>]+>", "", text)
        text = re.sub(r"\s+", " ", text).strip()
        return text

    # --- Dictionary interface ---

    def get_synonyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("synonyms", [])]

    def get_antonyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("antonyms", [])]

    def get_hypernyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("hypernyms", [])]

    def get_hyponyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("hyponyms", [])]

    def get_meronyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("meronyms", [])]

    def get_definitions(self, word: str) -> List["Definition"]:
        from ..definition import Definition

        results: List["Definition"] = []
        stop = {"a", "an", "the", "is", "are", "of", "to", "in", "for",
                "and", "or", "that", "which", "with", "by", "on", "at",
                "as", "its", "it", "be", "been", "was", "were", "has",
                "have", "had", "not", "no", "but", "from", "this", "than"}
        for defn_text in self._fetch(word).get("definitions", []):
            tokens = [w.lower() for w in re.findall(r"[a-zA-Z]+", defn_text)
                      if w.lower() not in stop and len(w) > 1
                      and w.lower() != word.lower()]
            if tokens:
                results.append(Definition([self._make_concept(t) for t in tokens]))
        return results

    def fill_concept(self, concept: "Concept") -> "Concept":
        concept = self.set_lemma(concept)
        concept = self.set_pos(concept)
        concept = self.fill_related(concept)
        return concept

    def fill_related(self, concept: "Concept") -> "Concept":
        word = concept.litheral or ""
        concept.synonyms.extend(self.get_synonyms(word))
        concept.antonyms.extend(self.get_antonyms(word))
        concept.hypernyms.extend(self.get_hypernyms(word))
        concept.hyponyms.extend(self.get_hyponyms(word))
        concept.meronyms.extend(self.get_meronyms(word))
        concept.definitions.extend(self.get_definitions(word))
        concept.derivations.extend(self._get_derivations(word))
        return concept

    def _get_derivations(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("derivations", [])]

    def get_lemma(self, word: str) -> str:
        if self._nlp is not None:
            doc = self._nlp(word)
            if doc:
                return doc[0].lemma_
        return word

    def get_concept(self, word: str) -> "Concept":
        from ..concept import Concept
        c = Concept()
        c.litheral = word
        return self.fill_concept(c)

    def set_pos(self, concept: "Concept") -> "Concept":
        if self._nlp is not None:
            doc = self._nlp(concept.litheral)
            if doc:
                concept.pos = doc[0].pos_
        return concept

    def fill_definition(self, definition: "Definition") -> "Definition":
        for c in definition.concepts:
            self.fill_concept(c)
        return definition

    def _make_concept(self, litheral: str) -> "Concept":
        from ..concept import Concept
        from ..word_type import WordType
        c = Concept()
        c.litheral = litheral
        c.word_type = WordType.NN
        return c
