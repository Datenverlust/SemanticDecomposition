from __future__ import annotations
from typing import Optional, TYPE_CHECKING

from ...marker_passing.marker import Marker

if TYPE_CHECKING:
    from ...concept import Concept


class TypedMarker(Marker):
    """Marker with a numeric value, an origin concept, and a type identifier."""

    def __init__(
        self,
        value: float = 0.0,
        origin: Optional["Concept"] = None,
        marker_type: int = 0,
    ) -> None:
        self.value: float = value
        self.origin: Optional["Concept"] = origin
        self.type: int = marker_type

    def __repr__(self) -> str:
        return f"TypedMarker(value={self.value}, type={self.type})"
