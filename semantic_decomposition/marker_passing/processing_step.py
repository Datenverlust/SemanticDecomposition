from abc import ABC, abstractmethod


class ProcessingStep(ABC):
    @abstractmethod
    def execute(self) -> None: ...
