from __future__ import annotations
from typing import Collection, List, Optional, TYPE_CHECKING

from .spreaded_markers import SpreadedMarkers

if TYPE_CHECKING:
    from .node import Node
    from .in_function import InFunction
    from .out_function import OutFunction
    from .select_firing_nodes_function import SelectFiringNodesFunction
    from .termination_condition import TerminationCondition
    from .processing_step import ProcessingStep


class SpreadingAlgorithm:
    """Main marker-passing algorithm. Runs pulses until termination condition is met."""

    def __init__(self) -> None:
        self._terminate: bool = False
        self._in: Optional["InFunction"] = None
        self._out: Optional["OutFunction"] = None
        self._select_firing_nodes: Optional["SelectFiringNodesFunction"] = None
        self._termination_condition: Optional["TerminationCondition"] = None
        self._preprocessing_steps: List["ProcessingStep"] = []
        self._postprocessing_steps: List["ProcessingStep"] = []
        self._firing_nodes: List["Node"] = []
        self._active_nodes: List["Node"] = []

    @property
    def terminate(self) -> bool:
        return self._terminate

    @terminate.setter
    def terminate(self, value: bool) -> None:
        self._terminate = value

    @property
    def in_function(self) -> Optional["InFunction"]:
        return self._in

    @in_function.setter
    def in_function(self, value: "InFunction") -> None:
        self._in = value

    @property
    def out_function(self) -> Optional["OutFunction"]:
        return self._out

    @out_function.setter
    def out_function(self, value: "OutFunction") -> None:
        self._out = value

    @property
    def select_firing_nodes(self) -> Optional["SelectFiringNodesFunction"]:
        return self._select_firing_nodes

    @select_firing_nodes.setter
    def select_firing_nodes(self, value: "SelectFiringNodesFunction") -> None:
        self._select_firing_nodes = value

    @property
    def termination_condition(self) -> Optional["TerminationCondition"]:
        return self._termination_condition

    @termination_condition.setter
    def termination_condition(self, value: "TerminationCondition") -> None:
        self._termination_condition = value

    @property
    def preprocessing_steps(self) -> List["ProcessingStep"]:
        return self._preprocessing_steps

    @preprocessing_steps.setter
    def preprocessing_steps(self, value: List["ProcessingStep"]) -> None:
        self._preprocessing_steps = value

    @property
    def postprocessing_steps(self) -> List["ProcessingStep"]:
        return self._postprocessing_steps

    @postprocessing_steps.setter
    def postprocessing_steps(self, value: List["ProcessingStep"]) -> None:
        self._postprocessing_steps = value

    @property
    def firing_nodes(self) -> List["Node"]:
        return self._firing_nodes

    @firing_nodes.setter
    def firing_nodes(self, value: List["Node"]) -> None:
        self._firing_nodes = value

    @property
    def active_nodes(self) -> List["Node"]:
        return self._active_nodes

    @active_nodes.setter
    def active_nodes(self, value: List["Node"]) -> None:
        self._active_nodes = value

    def execute(self) -> None:
        self._terminate = False
        while not self._terminate:
            self._pulse()

    def _pulse(self) -> Collection["Node"]:
        self._preprocess()
        self._firing_nodes = list(self._select_firing_nodes.compute(self._active_nodes))
        self._spread()
        self._postprocess()
        self._check_termination()
        return self._active_nodes

    def _preprocess(self) -> None:
        for step in self._preprocessing_steps:
            step.execute()

    def _spread(self) -> None:
        spreaded = SpreadedMarkers()
        for node in self._firing_nodes:
            spreaded.add_all(self._out.compute(node))
        self._firing_nodes.clear()
        for target in spreaded.get_target_nodes():
            self._in.compute(spreaded.get_input_for_target(target), target)
            self._active_nodes.append(target)

    def _postprocess(self) -> None:
        for step in self._postprocessing_steps:
            step.execute()

    def _check_termination(self) -> None:
        self._terminate = self._termination_condition.compute()
