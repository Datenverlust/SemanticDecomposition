class DictionaryDoesNotContainConceptException(Exception):
    def __init__(self, concept_name: str) -> None:
        super().__init__(f"Dictionary does not contain concept: {concept_name}")
        self.concept_name = concept_name
