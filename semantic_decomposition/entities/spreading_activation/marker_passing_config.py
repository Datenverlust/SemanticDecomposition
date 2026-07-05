class MarkerPassingConfig:
    """Static configuration for link weights and spreading activation parameters."""

    start_activation: float = 1.0
    threshold: float = 0.064
    termination_pulse_count: int = 80
    double_activation_limit: int = 20
    decomposition_depth: int = 1

    _definition_link_weight: float = -0.78
    _synonym_link_weight: float = 0.62
    _hypernym_link_weight: float = -0.02
    _hyponym_link_weight: float = 0.79
    _antonym_link_weight: float = -0.9
    _arbitrary_relation_link_weight: float = 0.0

    @classmethod
    def get_definition_link_weight(cls) -> float:
        return cls._definition_link_weight

    @classmethod
    def get_synonym_link_weight(cls) -> float:
        return cls._synonym_link_weight

    @classmethod
    def get_hypernym_link_weight(cls) -> float:
        return cls._hypernym_link_weight

    @classmethod
    def get_hyponym_link_weight(cls) -> float:
        return cls._hyponym_link_weight

    @classmethod
    def get_antonym_link_weight(cls) -> float:
        return cls._antonym_link_weight

    @classmethod
    def get_arbitrary_relation_link_weight(cls) -> float:
        return cls._arbitrary_relation_link_weight
