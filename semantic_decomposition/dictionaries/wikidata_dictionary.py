"""Wikidata dictionary backend via the SPARQL endpoint.

Extracts semantic relations from the Wikidata knowledge graph using the public
query service at query.wikidata.org.  No API key required.
"""
from __future__ import annotations

import json
import logging
import urllib.request
import urllib.parse
from typing import Dict, List, Optional, TYPE_CHECKING

from .base_dictionary import BaseDictionary

if TYPE_CHECKING:
    from ..concept import Concept
    from ..definition import Definition

logger = logging.getLogger(__name__)

_SPARQL_URL = "https://query.wikidata.org/sparql"
_WIKIDATA_API = "https://www.wikidata.org/w/api.php"


class WikidataDictionary(BaseDictionary):
    """Dictionary backed by Wikidata knowledge graph (SPARQL endpoint)."""

    _initialized: bool = False
    _cache: Dict[str, dict] = {}

    @classmethod
    def init(cls) -> None:
        super().init()
        if cls._initialized:
            return
        cls._cache = {}
        cls._initialized = True
        logger.debug("WikidataDictionary initialised")

    def _fetch(self, word: str) -> dict:
        word_lower = word.strip().lower()
        if word_lower in self._cache:
            return self._cache[word_lower]

        result: dict = {
            "description": "",
            "aliases": [],
            "instance_of": [],
            "subclass_of": [],
            "has_parts": [],
            "part_of": [],
            "said_to_be_same_as": [],
            "opposite_of": [],
        }

        try:
            qid = self._resolve_qid(word_lower)
            if qid:
                data = self._fetch_entity(qid)
                result = self._extract_relations(data, word_lower)
        except Exception as e:
            logger.debug(f"Wikidata fetch failed for '{word}': {e}")

        self._cache[word_lower] = result
        return result

    def _resolve_qid(self, word: str) -> Optional[str]:
        params = urllib.parse.urlencode({
            "action": "wbsearchentities",
            "search": word,
            "language": "en",
            "type": "item",
            "limit": "10",
            "format": "json",
        })
        url = f"{_WIKIDATA_API}?{params}"
        req = urllib.request.Request(url, headers={
            "User-Agent": "SemanticDecomposition/0.1",
            "Accept": "application/json",
        })
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        results = data.get("search", [])
        if not results:
            return None

        skip_descriptions = {"wikimedia", "category", "template", "disambiguation",
                             "module", "user", "help page"}
        word_lower = word.lower()
        for r in results:
            label = r.get("label", "").lower()
            desc = (r.get("description") or "").lower()
            if any(s in desc for s in skip_descriptions):
                continue
            if label == word_lower:
                return r.get("id")

        for r in results:
            desc = (r.get("description") or "").lower()
            if any(s in desc for s in skip_descriptions):
                continue
            return r.get("id")

        return results[0].get("id")

    def _fetch_entity(self, qid: str) -> dict:
        params = urllib.parse.urlencode({
            "action": "wbgetentities",
            "ids": qid,
            "props": "labels|descriptions|aliases|claims",
            "languages": "en",
            "format": "json",
        })
        url = f"{_WIKIDATA_API}?{params}"
        req = urllib.request.Request(url, headers={
            "User-Agent": "SemanticDecomposition/0.1",
            "Accept": "application/json",
        })
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        entities = data.get("entities", {})
        return entities.get(qid, {})

    def _extract_relations(self, entity: dict, original_word: str) -> dict:
        result: dict = {
            "description": "",
            "aliases": [],
            "instance_of": [],
            "subclass_of": [],
            "has_parts": [],
            "part_of": [],
            "said_to_be_same_as": [],
            "opposite_of": [],
        }

        desc = entity.get("descriptions", {}).get("en", {}).get("value", "")
        result["description"] = desc

        for alias in entity.get("aliases", {}).get("en", []):
            val = alias.get("value", "")
            if val.lower() != original_word:
                result["aliases"].append(val)

        prop_map = {
            "P31": "instance_of",      # instance of → hypernym-like
            "P279": "subclass_of",     # subclass of → hypernym
            "P527": "has_parts",       # has part(s) → hyponym/meronym
            "P361": "part_of",         # part of → holonym
            "P460": "said_to_be_same_as",  # said to be the same as → synonym
            "P461": "opposite_of",     # opposite of → antonym
        }

        claims = entity.get("claims", {})
        qids_to_resolve: List[str] = []
        qid_target_map: Dict[str, str] = {}

        for prop_id, target_key in prop_map.items():
            for claim in claims.get(prop_id, []):
                mainsnak = claim.get("mainsnak", {})
                datavalue = mainsnak.get("datavalue", {})
                if datavalue.get("type") == "wikibase-entityid":
                    target_qid = datavalue["value"].get("id")
                    if target_qid:
                        qids_to_resolve.append(target_qid)
                        qid_target_map[target_qid] = target_key

        if qids_to_resolve:
            labels = self._resolve_labels(qids_to_resolve)
            for qid, label in labels.items():
                target_key = qid_target_map.get(qid)
                if target_key and label.lower() != original_word:
                    result[target_key].append(label)

        return result

    def _resolve_labels(self, qids: List[str]) -> Dict[str, str]:
        labels: Dict[str, str] = {}
        for batch_start in range(0, len(qids), 50):
            batch = qids[batch_start:batch_start + 50]
            params = urllib.parse.urlencode({
                "action": "wbgetentities",
                "ids": "|".join(batch),
                "props": "labels",
                "languages": "en",
                "format": "json",
            })
            url = f"{_WIKIDATA_API}?{params}"
            req = urllib.request.Request(url, headers={
                "User-Agent": "SemanticDecomposition/0.1",
                "Accept": "application/json",
            })
            try:
                with urllib.request.urlopen(req, timeout=10) as resp:
                    data = json.loads(resp.read().decode("utf-8"))
                for qid, entity in data.get("entities", {}).items():
                    label = entity.get("labels", {}).get("en", {}).get("value")
                    if label:
                        labels[qid] = label
            except Exception as e:
                logger.debug(f"Wikidata label resolution failed: {e}")
        return labels

    # --- Dictionary interface ---

    def get_synonyms(self, word: str) -> List["Concept"]:
        data = self._fetch(word)
        words = data.get("aliases", []) + data.get("said_to_be_same_as", [])
        return [self._make_concept(w) for w in words]

    def get_antonyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("opposite_of", [])]

    def get_hypernyms(self, word: str) -> List["Concept"]:
        data = self._fetch(word)
        words = data.get("instance_of", []) + data.get("subclass_of", [])
        return [self._make_concept(w) for w in words]

    def get_hyponyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("has_parts", [])]

    def get_meronyms(self, word: str) -> List["Concept"]:
        return [self._make_concept(w) for w in self._fetch(word).get("part_of", [])]

    def get_definitions(self, word: str) -> List["Definition"]:
        from ..definition import Definition
        desc = self._fetch(word).get("description", "")
        if not desc or len(desc) < 3:
            return []
        stop = {"a", "an", "the", "is", "are", "of", "to", "in", "for",
                "and", "or", "that", "which", "with", "by", "on", "at",
                "as", "its", "it", "be", "been", "was", "were", "has",
                "have", "had", "not", "no", "but", "from", "this", "than"}
        tokens = [w.lower() for w in desc.split()
                  if w.isalpha() and w.lower() not in stop and len(w) > 1
                  and w.lower() != word.lower()]
        if tokens:
            return [Definition([self._make_concept(t) for t in tokens])]
        return []

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
        return concept

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
