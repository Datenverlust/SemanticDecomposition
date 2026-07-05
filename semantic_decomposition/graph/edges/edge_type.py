from enum import Enum


class EdgeType(Enum):
    Synonym = "Synonym"
    Antonym = "Antonym"
    Definition = "Definition"
    Hypernym = "Hypernym"
    Hyponym = "Hyponym"
    Meronym = "Meronym"
    Arbitrary = "Arbitrary"
    Unknown = "Unknown"
