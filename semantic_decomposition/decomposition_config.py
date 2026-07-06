class DecompositionConfig:
    """Global configuration knobs for the decomposition algorithm."""

    thread_count: int = 4
    cache_size: int = 100
    decomposition_depth: int = 1
