from __future__ import annotations
from typing import Optional, TYPE_CHECKING

from .weighted_link import WeightedLink

if TYPE_CHECKING:
    from ...marker_passing.node import Node
    from ..spreading_activation.marker_passing_config import MarkerPassingConfig


class ArbitraryRelationLink(WeightedLink):
    def __init__(
        self,
        source: Optional["Node"] = None,
        target: Optional["Node"] = None,
        config: Optional["MarkerPassingConfig"] = None,
        relation_name: str = "",
    ) -> None:
        weight = config.get_arbitrary_relation_link_weight() if config else 0.0
        super().__init__(source, target, weight, config)
        self.relation_name: str = relation_name
