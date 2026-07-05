from ...marker_passing.marker import Marker


class DoubleMarker(Marker):
    """Marker carrying a single floating-point activation value."""

    def __init__(self, activation: float = 0.0) -> None:
        self.activation: float = activation

    def __repr__(self) -> str:
        return f"DoubleMarker({self.activation})"
