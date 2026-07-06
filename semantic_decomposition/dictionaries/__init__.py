from .dictionary import Dictionary
from .base_dictionary import BaseDictionary

__all__ = ["Dictionary", "BaseDictionary"]

try:
    from .wordnet_dictionary import WordnetDictionary
    __all__.append("WordnetDictionary")
except ImportError:
    pass

try:
    from .wiktionary_dictionary import WiktionaryDictionary
    __all__.append("WiktionaryDictionary")
except ImportError:
    pass

try:
    from .wikidata_dictionary import WikidataDictionary
    __all__.append("WikidataDictionary")
except ImportError:
    pass

try:
    from .corpus_statistics_dictionary import CorpusLinguisticStatisticsDictionary
    __all__.append("CorpusLinguisticStatisticsDictionary")
except ImportError:
    pass
