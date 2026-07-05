from __future__ import annotations
from typing import Optional, TYPE_CHECKING

from .weighted_link import WeightedLink

if TYPE_CHECKING:
    from ...marker_passing.node import Node
    from ..spreading_activation.marker_passing_config import MarkerPassingConfig


class AntonymLink(WeightedLink):
    def __init__(
        self,
        source: Optional["Node"] = None,
        target: Optional["Node"] = None,
        config: Optional["MarkerPassingConfig"] = None,
    ) -> None:
        weight = config.get_antonym_link_weight() if config else -0.9
        super().__init__(source, target, weight, config)
