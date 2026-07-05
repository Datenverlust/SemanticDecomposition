from ...marker_passing.processing_step import ProcessingStep
from ...marker_passing.termination_condition import TerminationCondition


class CountTerminationCondition(ProcessingStep, TerminationCondition):
    """Counts pulses and terminates after a configurable maximum."""

    def __init__(self, max_pulses: int = 80) -> None:
        self._max_pulses = max_pulses
        self._count = 0

    @property
    def count(self) -> int:
        return self._count

    def execute(self) -> None:
        self._count += 1

    def compute(self) -> bool:
        return self._count >= self._max_pulses

    def reset(self) -> None:
        self._count = 0
