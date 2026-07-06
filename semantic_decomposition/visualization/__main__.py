from __future__ import annotations

import argparse
import logging


_BACKEND_REGISTRY = {
    "wordnet": "semantic_decomposition.dictionaries.wordnet_dictionary.WordnetDictionary",
    "wiktionary": "semantic_decomposition.dictionaries.wiktionary_dictionary.WiktionaryDictionary",
    "wikidata": "semantic_decomposition.dictionaries.wikidata_dictionary.WikidataDictionary",
}


def _load_backend(name: str):
    """Import and return a dictionary class by registry name, or None."""
    qualname = _BACKEND_REGISTRY.get(name)
    if qualname is None:
        return None
    module_path, cls_name = qualname.rsplit(".", 1)
    try:
        import importlib
        mod = importlib.import_module(module_path)
        return getattr(mod, cls_name)
    except (ImportError, AttributeError):
        return None


def main() -> None:
    from ..settings.config import Config
    config = Config.get_instance()

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
        help=f"dictionary backends to use (available: {', '.join(_BACKEND_REGISTRY)}). "
             "Default: from config file, or all available. Use 'demo' for the offline demo.",
    )
    parser.add_argument("--verbose", "-v", action="store_true",
                        help="enable debug logging")
    args = parser.parse_args()

    if args.verbose:
        logging.basicConfig(level=logging.DEBUG, format="%(name)s %(levelname)s: %(message)s")

    if args.dictionaries is not None and "demo" in args.dictionaries:
        from . import launch_demo
        launch_demo(
            word=args.word,
            host=args.host,
            port=args.port,
            open_browser=not args.no_browser,
            emit_delay=args.emit_delay,
        )
        return

    wanted = args.dictionaries if args.dictionaries is not None else config.dictionaries

    instances = []
    for name in wanted:
        cls = _load_backend(name)
        if cls is None:
            print(f"  - {name} (not installed)")
            continue
        try:
            inst = cls()
            inst.init()
            instances.append(inst)
            print(f"  + {name}")
        except Exception as e:
            print(f"  - {name} (init failed: {e})")

    if not instances:
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
