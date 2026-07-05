from __future__ import annotations
import copy


class DataExample:
    """Holds a measured result and an optional ground-truth value."""

    def __init__(self, result: float = 0.0, true_result: float = 0.0) -> None:
        self.result: float = result
        self.true_result: float = true_result

    def clone(self) -> "DataExample":
        return copy.copy(self)

    def __repr__(self) -> str:
        return f"DataExample(result={self.result}, true_result={self.true_result})"
