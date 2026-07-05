from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Collection, TYPE_CHECKING

if TYPE_CHECKING:
    from .node import Node
    from .spreading_step import SpreadingStep


class OutFunction(ABC):
    @abstractmethod
    def compute(self, node: "Node") -> Collection["SpreadingStep"]: ...
