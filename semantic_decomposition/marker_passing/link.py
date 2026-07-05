from __future__ import annotations
from abc import ABC, abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .node import Node


class Link(ABC):
    """Directed edge with a source and a target node."""

    @property
    @abstractmethod
    def source(self) -> "Node": ...

    @source.setter
    @abstractmethod
    def source(self, value: "Node") -> None: ...

    @property
    @abstractmethod
    def target(self) -> "Node": ...

    @target.setter
    @abstractmethod
    def target(self, value: "Node") -> None: ...
