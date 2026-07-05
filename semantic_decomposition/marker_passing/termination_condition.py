from abc import ABC, abstractmethod


class TerminationCondition(ABC):
    @abstractmethod
    def compute(self) -> bool: ...
