from __future__ import annotations

import argparse


def main() -> None:
    parser = argparse.ArgumentParser(
        prog="python -m semantic_decomposition.visualization",
        description="Interactive, incrementally-growing semantic decomposition graph. "
                    "Runs an offline demo dictionary unless a backend is wired up in code.",
    )
    parser.add_argument("word", nargs="?", default="cat",
                        help="root word to decompose (default: cat)")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--no-browser", action="store_true",
                        help="do not open a browser automatically")
    parser.add_argument("--emit-delay", type=float, default=0.08,
                        help="seconds between streamed node/edge additions (animation pacing)")
    args = parser.parse_args()

    from . import launch_demo

    launch_demo(
        word=args.word,
        host=args.host,
        port=args.port,
        open_browser=not args.no_browser,
        emit_delay=args.emit_delay,
    )


if __name__ == "__main__":
    main()
