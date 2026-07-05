from __future__ import annotations
import os
import pickle
from pathlib import Path
from typing import Dict, Optional, Tuple, TYPE_CHECKING

from .semantic_net import SemanticNet
from .edges.weighted_edge import WeightedEdge
from .edges.edge_type import EdgeType

if TYPE_CHECKING:
    from ..concept import Concept
    from ..word_type import WordType

_GRAPH_CACHE: Dict[Tuple[str, str, int], SemanticNet] = {}
_GRAPH_STORE_DIR = Path.home() / ".decomposition" / "graphs"


class GraphUtil:
    """Static utility for building and merging SemanticNet graphs from concepts."""

    @staticmethod
    def create_graph(concept: "Concept") -> SemanticNet:
        graph = SemanticNet()
        graph.add_vertex(concept)
        GraphUtil._add_relation_edges(graph, concept, concept.synonyms, EdgeType.Synonym)
        GraphUtil._add_relation_edges(graph, concept, concept.antonyms, EdgeType.Antonym)
        GraphUtil._add_relation_edges(graph, concept, concept.hypernyms, EdgeType.Hypernym)
        GraphUtil._add_relation_edges(graph, concept, concept.hyponyms, EdgeType.Hyponym)
        GraphUtil._add_relation_edges(graph, concept, concept.meronyms, EdgeType.Meronym)
        for rel_name, related in concept.arbitrary_relations.items():
            GraphUtil._add_relation_edges(graph, concept, related, EdgeType.Arbitrary)
        for definition in concept.definitions:
            if hasattr(definition, "concepts"):
                for def_concept in definition.concepts:
                    edge = WeightedEdge(concept, def_concept, EdgeType.Definition)
                    graph.add_edge(concept, def_concept, edge)
        return graph

    @staticmethod
    def _add_relation_edges(
        graph: SemanticNet,
        source: "Concept",
        targets: list,
        edge_type: EdgeType,
    ) -> None:
        for target in targets:
            edge = WeightedEdge(source, target, edge_type)
            graph.add_edge(source, target, edge)

    @staticmethod
    def merge_graph(g1: SemanticNet, g2: SemanticNet) -> SemanticNet:
        merged = SemanticNet()
        for node in g1.vertex_set():
            merged.add_vertex(node)
        for node in g2.vertex_set():
            merged.add_vertex(node)
        for edge in g1.edge_set():
            merged.add_edge(edge.source, edge.target, edge)
        for edge in g2.edge_set():
            if not merged.contains_edge(edge):
                merged.add_edge(edge.source, edge.target, edge)
        return merged

    @staticmethod
    def get_graph(word: str, word_type: "WordType", depth: int) -> SemanticNet:
        key = (word, str(word_type), depth)
        if key in _GRAPH_CACHE:
            return _GRAPH_CACHE[key]
        loaded = GraphUtil._load_graph(word, word_type, depth)
        if loaded is not None:
            _GRAPH_CACHE[key] = loaded
            return loaded
        return SemanticNet()

    @staticmethod
    def save_graph(graph: SemanticNet, word: str, word_type: "WordType", depth: int) -> None:
        _GRAPH_STORE_DIR.mkdir(parents=True, exist_ok=True)
        path = _GRAPH_STORE_DIR / f"{word}_{word_type}_{depth}.pkl"
        with open(path, "wb") as f:
            pickle.dump(graph, f)
        key = (word, str(word_type), depth)
        _GRAPH_CACHE[key] = graph

    @staticmethod
    def _load_graph(word: str, word_type: "WordType", depth: int) -> Optional[SemanticNet]:
        path = _GRAPH_STORE_DIR / f"{word}_{word_type}_{depth}.pkl"
        if not path.exists():
            return None
        try:
            with open(path, "rb") as f:
                return pickle.load(f)
        except Exception:
            return None
