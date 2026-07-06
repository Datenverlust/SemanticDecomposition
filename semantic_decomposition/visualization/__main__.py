from __future__ import annotations

import argparse
import logging


def _available_dictionaries() -> list:
    """Build list of available dictionary backends, best-coverage first."""
    dicts = []
    try:
        from ..dictionaries.wordnet_dictionary import WordnetDictionary
        dicts.append(("wordnet", WordnetDictionary))
    except ImportError:
        pass
    try:
        from ..dictionaries.wiktionary_dictionary import WiktionaryDictionary
        dicts.append(("wiktionary", WiktionaryDictionary))
    except ImportError:
        pass
    try:
        from ..dictionaries.wikidata_dictionary import WikidataDictionary
        dicts.append(("wikidata", WikidataDictionary))
    except ImportError:
        pass
    return dicts


def main() -> None:
    available = _available_dictionaries()
    dict_names = [name for name, _ in available]

    parser = argparse.ArgumentParser(
        prog="python -m semantic_decomposition.visualization",
        description="Interactive, incrementally-growing semantic decomposition graph.",
    )
    parser.add_argument("word", nargs="?", default="cat",
                        help="root word to decompose (default: cat)")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--no-browser", action="store_true",
                        help="do not open a browser automatically")
    parser.add_argument("--emit-delay", type=float, default=0.08,
                        help="seconds between streamed node/edge additions (animation pacing)")
    parser.add_argument(
        "--dictionaries", "-d",
        nargs="*",
        metavar="NAME",
        default=None,
        help=f"dictionary backends to use (available: {', '.join(dict_names) or 'none — install nltk for wordnet'}). "
             "Omit for all available; use 'demo' for the offline demo dictionary.",
    )
    parser.add_argument("--verbose", "-v", action="store_true",
                        help="enable debug logging")
    args = parser.parse_args()

    if args.verbose:
        logging.basicConfig(level=logging.DEBUG, format="%(name)s %(levelname)s: %(message)s")

    requested = args.dictionaries

    if requested is not None and "demo" in requested:
        from . import launch_demo
        launch_demo(
            word=args.word,
            host=args.host,
            port=args.port,
            open_browser=not args.no_browser,
            emit_delay=args.emit_delay,
        )
        return

    backend_map = dict(available)

    if requested is None:
        selected = available
    else:
        selected = []
        for name in requested:
            if name in backend_map:
                selected.append((name, backend_map[name]))
            else:
                parser.error(f"unknown dictionary '{name}' (available: {', '.join(dict_names)})")

    if not selected:
        print("No dictionary backends available. Falling back to offline demo.")
        print("Install nltk for WordNet:  pip install nltk")
        from . import launch_demo
        launch_demo(
            word=args.word,
            host=args.host,
            port=args.port,
            open_browser=not args.no_browser,
            emit_delay=args.emit_delay,
        )
        return

    instances = []
    for name, cls in selected:
        try:
            inst = cls()
            inst.init()
            instances.append(inst)
            print(f"  + {name}")
        except Exception as e:
            print(f"  - {name} (init failed: {e})")

    if not instances:
        print("All dictionary backends failed to init. Falling back to demo.")
        from . import launch_demo
        launch_demo(
            word=args.word,
            host=args.host,
            port=args.port,
            open_browser=not args.no_browser,
            emit_delay=args.emit_delay,
        )
        return

    from ..decomposition import Decomposition
    from . import launch

    Decomposition.init(instances)
    print(f"Decomposing with {len(instances)} backend(s)")
    launch(
        word=args.word,
        host=args.host,
        port=args.port,
        open_browser=not args.no_browser,
        emit_delay=args.emit_delay,
    )


if __name__ == "__main__":
    main()
