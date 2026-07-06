from __future__ import annotations

import json
import queue
import threading
import webbrowser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Optional, TYPE_CHECKING
from urllib.parse import urlparse

if TYPE_CHECKING:
    from .graph_visualizer import DecompositionGraphVisualizer

_STATIC_DIR = Path(__file__).parent / "static"


class _Server(ThreadingHTTPServer):
    daemon_threads = True
    allow_reuse_address = True


class VisualizationServer:
    """
    Tiny stdlib HTTP server that exposes a DecompositionGraphVisualizer to a
    browser.  No external dependencies.

    Endpoints
    ---------
    GET  /            -> the single-page frontend (static/index.html)
    GET  /graph       -> JSON snapshot {nodes, edges} (fallback for the client)
    GET  /events      -> Server-Sent Events stream of graph mutations
    POST /expand      -> body {"id": "<node_id>"}      expand a node one hop
    POST /decompose   -> body {"word": "...", "word_type": "NN"}  new root word

    ``/expand`` and ``/decompose`` return immediately (202) and run the
    (potentially slow) decomposition in a background thread; results arrive on
    the SSE stream.
    """

    def __init__(
        self,
        visualizer: "DecompositionGraphVisualizer",
        host: str = "127.0.0.1",
        port: int = 8765,
    ) -> None:
        self.visualizer = visualizer
        self.host = host
        self.port = port
        self._httpd: Optional[_Server] = None

    @property
    def url(self) -> str:
        return f"http://{self.host}:{self.port}/"

    def serve(self, open_browser: bool = True, block: bool = True) -> "VisualizationServer":
        handler = self._make_handler()
        self._httpd = _Server((self.host, self.port), handler)
        if open_browser:
            threading.Thread(
                target=lambda: webbrowser.open(self.url), daemon=True
            ).start()
        if block:
            try:
                self._httpd.serve_forever()
            except KeyboardInterrupt:
                pass
            finally:
                self._httpd.shutdown()
        else:
            threading.Thread(target=self._httpd.serve_forever, daemon=True).start()
        return self

    def stop(self) -> None:
        if self._httpd is not None:
            self._httpd.shutdown()

    # ------------------------------------------------------------------

    def _make_handler(self):
        visualizer = self.visualizer

        class Handler(BaseHTTPRequestHandler):
            protocol_version = "HTTP/1.1"

            def log_message(self, *args) -> None:  # silence default logging
                pass

            # --- helpers ---

            def _send_bytes(self, body: bytes, content_type: str, code: int = 200) -> None:
                self.send_response(code)
                self.send_header("Content-Type", content_type)
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)

            def _send_json(self, obj, code: int = 200) -> None:
                self._send_bytes(json.dumps(obj).encode("utf-8"), "application/json", code)

            def _read_json(self) -> dict:
                length = int(self.headers.get("Content-Length", 0) or 0)
                if not length:
                    return {}
                raw = self.rfile.read(length)
                try:
                    return json.loads(raw.decode("utf-8"))
                except (ValueError, UnicodeDecodeError):
                    return {}

            # --- routing ---

            def do_GET(self) -> None:
                path = urlparse(self.path).path
                if path == "/":
                    self._serve_index()
                elif path == "/graph":
                    self._send_json(visualizer.snapshot())
                elif path == "/events":
                    self._serve_events()
                else:
                    self._send_bytes(b"Not found", "text/plain", 404)

            def do_POST(self) -> None:
                path = urlparse(self.path).path
                if path == "/expand":
                    body = self._read_json()
                    node_id = body.get("id")
                    if node_id:
                        threading.Thread(
                            target=visualizer.expand, args=(node_id,), daemon=True
                        ).start()
                    self._send_json({"ok": True}, 202)
                elif path == "/decompose":
                    body = self._read_json()
                    word = (body.get("word") or "").strip()
                    if word:
                        wt = self._resolve_word_type(body.get("word_type"))
                        threading.Thread(
                            target=visualizer.start, args=(word, wt), daemon=True
                        ).start()
                    self._send_json({"ok": True}, 202)
                else:
                    self._send_bytes(b"Not found", "text/plain", 404)

            # --- endpoint implementations ---

            def _serve_index(self) -> None:
                index = _STATIC_DIR / "index.html"
                try:
                    self._send_bytes(index.read_bytes(), "text/html; charset=utf-8")
                except OSError:
                    self._send_bytes(b"index.html missing", "text/plain", 500)

            def _serve_events(self) -> None:
                self.send_response(200)
                self.send_header("Content-Type", "text/event-stream")
                self.send_header("Cache-Control", "no-cache")
                self.send_header("Connection", "keep-alive")
                self.send_header("X-Accel-Buffering", "no")
                self.end_headers()

                q = visualizer.subscribe()
                try:
                    while True:
                        try:
                            event = q.get(timeout=15)
                        except queue.Empty:
                            # keep-alive comment so proxies don't drop the stream
                            self.wfile.write(b": ping\n\n")
                            self.wfile.flush()
                            continue
                        payload = json.dumps(event).encode("utf-8")
                        self.wfile.write(b"data: " + payload + b"\n\n")
                        self.wfile.flush()
                except (BrokenPipeError, ConnectionResetError, OSError):
                    pass
                finally:
                    visualizer.unsubscribe(q)

            @staticmethod
            def _resolve_word_type(name: Optional[str]):
                if not name:
                    return None
                try:
                    from ..word_type import WordType
                    return WordType[name]
                except Exception:
                    return None

        return Handler
