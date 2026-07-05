from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Collection, TYPE_CHECKING

if TYPE_CHECKING:
    from .spreading_step import SpreadingStep
    from .node import Node


class InFunction(ABC):
    @abstractmethod
    def compute(self, input_steps: Collection["SpreadingStep"], node: "Node") -> None: ...
