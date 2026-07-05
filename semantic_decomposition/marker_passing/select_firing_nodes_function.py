from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Collection, TYPE_CHECKING

if TYPE_CHECKING:
    from .node import Node


class SelectFiringNodesFunction(ABC):
    @abstractmethod
    def compute(self, active_nodes: Collection["Node"]) -> Collection["Node"]: ...
