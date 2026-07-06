from __future__ import annotations

import queue
import threading
import time
from typing import Callable, Dict, List, Optional, Tuple, TYPE_CHECKING

if TYPE_CHECKING:
    from ..concept import Concept
    from ..word_type import WordType


# Relation name -> default marker-passing edge weight (from MarkerPassingConfig).
# Used only for display / edge labelling in the visualiser.
_RELATION_WEIGHTS: Dict[str, float] = {
    "synonym": 0.62,
    "antonym": -0.90,
    "hypernym": -0.02,
    "hyponym": 0.79,
    "meronym": 0.50,
    "definition": -0.78,
    "derivation": 0.0,
}


class DecompositionGraphVisualizer:
    """
    Live, incrementally-growing model of a semantic decomposition graph.

    The visualiser keeps an authoritative set of nodes and edges and pushes a
    stream of mutation *events* to any number of subscribers (the web frontend
    connects via Server-Sent Events).  As the decomposition progresses, new
    nodes and edges are emitted one at a time so the browser can animate them
    into the force-directed layout.

    Two entry points drive growth:

    * ``start(word)``       — decompose a root word and emit its first hop.
    * ``expand(node_id)``   — decompose an existing node one hop further.
                              This is what a double-click in the UI triggers.

    The class is decoupled from ``Decomposition``: it takes a ``decompose_fn``
    and a ``create_concept_fn`` (defaulting to the real ones) so it can be
    unit-tested with stubs.

    Thread-safety: graph state is guarded by a lock.  Slow dictionary calls
    happen *outside* the lock; only the fast state mutation + event fan-out is
    locked, so many expansions can run concurrently without blocking readers.
    """

    def __init__(
        self,
        decompose_fn: Optional[Callable[["Concept"], "Concept"]] = None,
        create_concept_fn: Optional[Callable[[str, Optional["WordType"]], "Concept"]] = None,
        emit_delay: float = 0.08,
    ) -> None:
        self._decompose_fn = decompose_fn
        self._create_concept_fn = create_concept_fn
        self._emit_delay = emit_delay

        self._lock = threading.RLock()
        self._nodes: Dict[str, dict] = {}
        self._edges: Dict[str, dict] = {}
        self._concept_by_node: Dict[str, "Concept"] = {}
        self._expanding: set[str] = set()
        self._subscribers: List["queue.Queue[dict]"] = []

    # ------------------------------------------------------------------
    # lazy defaults (resolved on first use to avoid import cycles)
    # ------------------------------------------------------------------

    def _decompose(self, concept: "Concept") -> "Concept":
        fn = self._decompose_fn
        if fn is None:
            from ..decomposition import Decomposition
            fn = Decomposition.decompose
            self._decompose_fn = fn
        try:
            return fn(concept)
        except Exception:
            # a failed dictionary lookup should not kill the whole graph
            return concept

    def _create_concept(self, litheral: str, word_type: Optional["WordType"]) -> "Concept":
        fn = self._create_concept_fn
        if fn is None:
            from ..decomposition import Decomposition
            fn = Decomposition.create_concept
            self._create_concept_fn = fn
        return fn(litheral, word_type)

    # ------------------------------------------------------------------
    # subscription (used by the SSE endpoint)
    # ------------------------------------------------------------------

    def subscribe(self) -> "queue.Queue[dict]":
        """
        Register a new event subscriber.  The current graph is replayed into
        the queue first so a late-joining client ends up fully consistent.
        """
        q: "queue.Queue[dict]" = queue.Queue()
        with self._lock:
            for node in self._nodes.values():
                q.put({"type": "add_node", "node": node})
            for edge in self._edges.values():
                q.put({"type": "add_edge", "edge": edge})
            for key, node in self._nodes.items():
                if node["expanded"]:
                    q.put({"type": "node_expanded", "id": key})
            self._subscribers.append(q)
        return q

    def unsubscribe(self, q: "queue.Queue[dict]") -> None:
        with self._lock:
            if q in self._subscribers:
                self._subscribers.remove(q)

    def _emit(self, event: dict) -> None:
        with self._lock:
            for q in self._subscribers:
                q.put(event)

    # ------------------------------------------------------------------
    # public snapshot
    # ------------------------------------------------------------------

    def snapshot(self) -> dict:
        with self._lock:
            return {
                "nodes": list(self._nodes.values()),
                "edges": list(self._edges.values()),
            }

    # ------------------------------------------------------------------
    # graph growth
    # ------------------------------------------------------------------

    def start(self, word: str, word_type: Optional["WordType"] = None) -> str:
        """Decompose a root word and emit its immediate relations."""
        if word_type is None:
            word_type = self._default_word_type()
        self._emit({"type": "status", "message": f"Decomposing '{word}' …"})

        concept = self._create_concept(word, word_type)
        concept = self._decompose(concept)

        root_id, _ = self._add_node(concept, root=True)
        self._emit_relations(root_id, concept)
        self._mark_expanded(root_id)
        self._emit({"type": "status", "message": f"Ready — double-click a node to expand it."})
        return root_id

    def expand(self, node_id: str) -> None:
        """
        Decompose the concept behind ``node_id`` one hop further, emitting any
        new nodes and edges.  Safe to call repeatedly / concurrently; a node is
        only expanded once.
        """
        with self._lock:
            if node_id not in self._concept_by_node:
                return
            if node_id in self._expanding or self._nodes.get(node_id, {}).get("expanded"):
                return
            self._expanding.add(node_id)
            concept = self._concept_by_node[node_id]
            label = self._nodes[node_id]["label"]

        self._emit({"type": "status", "message": f"Expanding '{label}' …"})
        concept = self._decompose(concept)
        added = self._emit_relations(node_id, concept)
        self._mark_expanded(node_id)

        with self._lock:
            self._expanding.discard(node_id)

        if added:
            self._emit({"type": "status", "message": f"Added {added} node(s) from '{label}'."})
        else:
            self._emit({"type": "status", "message": f"'{label}' has no further relations."})

    # ------------------------------------------------------------------
    # internals
    # ------------------------------------------------------------------

    def _emit_relations(self, source_id: str, concept: "Concept") -> int:
        """Add one edge (and target node) per related concept.  Returns count added."""
        added = 0
        for relation, related_concept in self._relations_of(concept):
            target_id, is_new = self._add_node(related_concept)
            edge_added = self._add_edge(source_id, target_id, relation)
            if is_new or edge_added:
                added += 1
                if self._emit_delay:
                    time.sleep(self._emit_delay)
        return added

    def _relations_of(self, concept: "Concept") -> List[Tuple[str, "Concept"]]:
        """Flatten every relation list on a concept into (relation, concept) pairs."""
        pairs: List[Tuple[str, "Concept"]] = []

        for relation, attr in (
            ("synonym", "synonyms"),
            ("antonym", "antonyms"),
            ("hypernym", "hypernyms"),
            ("hyponym", "hyponyms"),
            ("meronym", "meronyms"),
            ("derivation", "derivations"),
        ):
            for c in getattr(concept, attr, None) or []:
                pairs.append((relation, c))

        # definitions: each Definition holds a list of concepts (one per token)
        for definition in getattr(concept, "definitions", None) or []:
            for c in getattr(definition, "concepts", None) or []:
                pairs.append(("definition", c))

        # arbitrary_relations: {relation_name: [concepts]}
        arbitrary = getattr(concept, "arbitrary_relations", None) or {}
        for name, concepts in arbitrary.items():
            for c in concepts or []:
                pairs.append((name or "arbitrary", c))

        return pairs

    def _add_node(self, concept: "Concept", root: bool = False) -> Tuple[str, bool]:
        key = self._node_key(concept)
        with self._lock:
            if key in self._nodes:
                # keep the richer concept object if the new one has relations
                if not self._concept_has_relations(self._concept_by_node.get(key)) \
                        and self._concept_has_relations(concept):
                    self._concept_by_node[key] = concept
                return key, False
            node = {
                "id": key,
                "label": concept.litheral or "?",
                "word_type": self._word_type_name(concept),
                "root": root,
                "expanded": False,
            }
            self._nodes[key] = node
            self._concept_by_node[key] = concept
            for q in self._subscribers:
                q.put({"type": "add_node", "node": node})
        return key, True

    def _add_edge(self, source_id: str, target_id: str, relation: str) -> bool:
        if source_id == target_id:
            return False
        edge_id = f"{source_id}|{relation}|{target_id}"
        with self._lock:
            if edge_id in self._edges:
                return False
            edge = {
                "id": edge_id,
                "source": source_id,
                "target": target_id,
                "relation": relation,
                "weight": _RELATION_WEIGHTS.get(relation, 0.0),
            }
            self._edges[edge_id] = edge
            for q in self._subscribers:
                q.put({"type": "add_edge", "edge": edge})
        return True

    def _mark_expanded(self, node_id: str) -> None:
        with self._lock:
            node = self._nodes.get(node_id)
            if node is None or node["expanded"]:
                return
            node["expanded"] = True
            for q in self._subscribers:
                q.put({"type": "node_expanded", "id": node_id})

    # --- helpers ---

    @staticmethod
    def _node_key(concept: "Concept") -> str:
        litheral = (concept.litheral or "?").strip().lower()
        wt = DecompositionGraphVisualizer._word_type_name(concept)
        return f"{litheral}::{wt}"

    @staticmethod
    def _word_type_name(concept: "Concept") -> str:
        wt = getattr(concept, "word_type", None)
        if wt is None:
            return "?"
        return getattr(wt, "name", str(wt))

    @staticmethod
    def _concept_has_relations(concept: Optional["Concept"]) -> bool:
        if concept is None:
            return False
        for attr in ("synonyms", "antonyms", "hypernyms", "hyponyms",
                     "meronyms", "derivations", "definitions"):
            if getattr(concept, attr, None):
                return True
        return bool(getattr(concept, "arbitrary_relations", None))

    def _default_word_type(self) -> Optional["WordType"]:
        try:
            from ..word_type import WordType
            return WordType.NN
        except Exception:
            return None
