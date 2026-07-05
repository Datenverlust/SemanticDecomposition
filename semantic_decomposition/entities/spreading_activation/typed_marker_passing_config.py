from __future__ import annotations
from typing import Any


class TypedMarkerPassingConfig:
    """All hyper-parameters for the typed marker-passing algorithm."""

    NUMBER_OF_PARAMETERS: int = 12

    def __init__(self) -> None:
        self.termination_pulse_count: int = 20
        self.initial_marker_amount: float = 1000.0
        self.decomposition_depth: int = 2

        self.synonym_weight: float = 0.62
        self.antonym_weight: float = -0.9
        self.definition_weight: float = -0.78
        self.hypernym_weight: float = -0.02
        self.hyponym_weight: float = 0.79
        self.meronym_weight: float = 0.5

        self.synonym_threshold: float = 0.064
        self.antonym_threshold: float = 0.064
        self.definition_threshold: float = 0.064

    def swap(self) -> "TypedMarkerPassingConfig":
        swapped = TypedMarkerPassingConfig()
        swapped.synonym_weight = self.antonym_weight
        swapped.antonym_weight = self.synonym_weight
        return swapped

    def get(self, index: int) -> Any:
        params = [
            self.termination_pulse_count,
            self.initial_marker_amount,
            self.decomposition_depth,
            self.synonym_weight,
            self.antonym_weight,
            self.definition_weight,
            self.hypernym_weight,
            self.hyponym_weight,
            self.meronym_weight,
            self.synonym_threshold,
            self.antonym_threshold,
            self.definition_threshold,
        ]
        return params[index]

    def get_number_of_parameters(self) -> int:
        return self.NUMBER_OF_PARAMETERS
