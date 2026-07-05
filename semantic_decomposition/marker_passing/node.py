from __future__ import annotations
from abc import ABC, abstractmethod
from typing import List, TYPE_CHECKING

if TYPE_CHECKING:
    from .link import Link
    from .marker import Marker


class Node(ABC):
    """Vertex in the graph that holds markers and connects via links."""

    def add_link(self, link: "Link") -> None:
        self.get_links().append(link)

    def remove_link(self, link: "Link") -> None:
        self.get_links().remove(link)

    @abstractmethod
    def get_links(self) -> List["Link"]: ...

    def add_marker(self, marker: "Marker") -> None:
        self.get_markers().append(marker)

    def remove_marker(self, marker: "Marker") -> None:
        self.get_markers().remove(marker)

    @abstractmethod
    def get_markers(self) -> List["Marker"]: ...

    @abstractmethod
    def check_thresholds(self, marker_classes: object) -> bool: ...
