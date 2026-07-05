from abc import ABC, abstractmethod
from ..i_concept import IConcept


class SemanticDistanceMeasureInterface(ABC):
    @abstractmethod
    def compare_concepts(self, c1: IConcept, c2: IConcept) -> float: ...
