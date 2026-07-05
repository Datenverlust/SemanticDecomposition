from __future__ import annotations
from typing import Any, Dict, List, Optional, Set, Tuple

from .edges.weighted_edge import WeightedEdge
from .edges.edge_type import EdgeType


class SemanticNet:
    """
    Directed graph representing a semantic network.

    Replaces JGraphT DefaultListenableGraph<Entity, Relation>.
    Nodes can be any hashable object (typically Entity / Concept).
    Edges are WeightedEdge instances.
    """

    def __init__(self) -> None:
        self._nodes: Set[Any] = set()
        self._edges: List[WeightedEdge] = []
        self._out_edges: Dict[Any, List[WeightedEdge]] = {}
        self._in_edges: Dict[Any, List[WeightedEdge]] = {}

    def add_vertex(self, node: Any) -> None:
        self._nodes.add(node)
        self._out_edges.setdefault(node, [])
        self._in_edges.setdefault(node, [])

    def add_edge(self, source: Any, target: Any, edge: WeightedEdge) -> None:
        self.add_vertex(source)
        self.add_vertex(target)
        self._edges.append(edge)
        self._out_edges[source].append(edge)
        self._in_edges[target].append(edge)

    def contains_vertex(self, node: Any) -> bool:
        return node in self._nodes

    def contains_edge(self, edge: WeightedEdge) -> bool:
        return edge in self._edges

    def vertex_set(self) -> Set[Any]:
        return set(self._nodes)

    def edge_set(self) -> List[WeightedEdge]:
        return list(self._edges)

    def edges_of(self, node: Any) -> List[WeightedEdge]:
        return self._out_edges.get(node, []) + self._in_edges.get(node, [])

    def out_edges_of(self, node: Any) -> List[WeightedEdge]:
        return self._out_edges.get(node, [])

    def get_edge_source(self, edge: WeightedEdge) -> Any:
        return edge.source

    def get_edge_target(self, edge: WeightedEdge) -> Any:
        return edge.target

    def get_edge_weight(self, edge: WeightedEdge) -> float:
        return edge.get_edge_weight()
