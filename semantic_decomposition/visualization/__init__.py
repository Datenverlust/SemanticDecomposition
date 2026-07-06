"""
Interactive, incrementally-growing visualisation of a semantic decomposition
graph.

The graph is rendered in the browser with a force-directed layout.  As a word
is decomposed, new nodes and edges stream in live over Server-Sent Events.
Double-clicking a node decomposes that concept one hop further, so the user can
explore the semantic neighbourhood interactively.

Quick start (offline demo, no dictionaries required)::

    python -m semantic_decomposition.visualization cat

With real dictionary backends (WordNet, Wiktionary, Wikidata)::

    python -m semantic_decomposition.visualization cat -d wordnet wiktionary wikidata

All available backends auto-detected::

    python -m semantic_decomposition.visualization cat -d

Programmatic use with your own dictionary backends::

    from semantic_decomposition import Decomposition
    from semantic_decomposition.visualization import launch

    Decomposition.init([MyDictionary()])
    launch("cat")            # opens a browser, blocks until Ctrl-C
"""
from __future__ import annotations

from typing import List, Optional, TYPE_CHECKING

from .graph_visualizer import DecompositionGraphVisualizer
from .server import VisualizationServer

if TYPE_CHECKING:
    from ..word_type import WordType

__all__ = [
    "DecompositionGraphVisualizer",
    "VisualizationServer",
    "launch",
    "launch_demo",
    "launch_with_all",
]


def launch(
    word: str,
    word_type: Optional["WordType"] = None,
    host: str = "127.0.0.1",
    port: int = 8765,
    open_browser: bool = True,
    emit_delay: float = 0.08,
    block: bool = True,
) -> VisualizationServer:
    """
    Start the visualisation server and begin decomposing ``word``.

    Assumes ``Decomposition.init([...])`` has already been called with your
    dictionary backends.  For a zero-setup demo use :func:`launch_demo`.
    """
    import threading
    import time

    visualizer = DecompositionGraphVisualizer(emit_delay=emit_delay)
    server = VisualizationServer(visualizer, host=host, port=port)

    def _kickoff() -> None:
        time.sleep(1.2)
        visualizer.start(word, word_type)

    threading.Thread(target=_kickoff, daemon=True).start()

    if block:
        print(f"Serving decomposition graph at {server.url}  (Ctrl-C to stop)")
    return server.serve(open_browser=open_browser, block=block)


def launch_demo(
    word: str = "cat",
    dictionaries: Optional[List[object]] = None,
    **kwargs,
) -> VisualizationServer:
    """
    Launch the visualiser backed by the offline :class:`DemoDictionary`, so it
    runs with no external lexical resources.  Handy for trying the UI.
    """
    from ..decomposition import Decomposition
    from .demo_dictionary import DemoDictionary

    Decomposition.init(dictionaries if dictionaries is not None else [DemoDictionary()])
    return launch(word, **kwargs)


def launch_with_all(
    word: str = "cat",
    **kwargs,
) -> VisualizationServer:
    """
    Launch the visualiser with all available dictionary backends
    (WordNet, Wiktionary, Wikidata).  Falls back to DemoDictionary if none
    are available.
    """
    from ..decomposition import Decomposition

    dicts: List[object] = []

    try:
        from ..dictionaries.wordnet_dictionary import WordnetDictionary
        d = WordnetDictionary()
        d.init()
        dicts.append(d)
    except Exception:
        pass

    try:
        from ..dictionaries.wiktionary_dictionary import WiktionaryDictionary
        d = WiktionaryDictionary()
        d.init()
        dicts.append(d)
    except Exception:
        pass

    try:
        from ..dictionaries.wikidata_dictionary import WikidataDictionary
        d = WikidataDictionary()
        d.init()
        dicts.append(d)
    except Exception:
        pass

    if not dicts:
        return launch_demo(word, **kwargs)

    Decomposition.init(dicts)
    return launch(word, **kwargs)
