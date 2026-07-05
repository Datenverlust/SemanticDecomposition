from __future__ import annotations
import copy
from .data_example import DataExample


class SimilarityPair(DataExample):
    """DataExample that also carries the two strings being compared."""

    def __init__(
        self,
        string1: str = "",
        string2: str = "",
        result: float = 0.0,
        true_result: float = 0.0,
    ) -> None:
        super().__init__(result, true_result)
        self.string1: str = string1
        self.string2: str = string2

    def clone(self) -> "SimilarityPair":
        return copy.copy(self)

    def __repr__(self) -> str:
        return (
            f"SimilarityPair({self.string1!r}, {self.string2!r}, "
            f"result={self.result}, true_result={self.true_result})"
        )
