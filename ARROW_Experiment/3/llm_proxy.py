#!/usr/bin/env python3
"""Proxy cục bộ cho ChatUniTest trong TN3: ChatUniTest gọi mô hình qua đây, không gọi thẳng nhà cung cấp.
Chốt 26/11/2026: proxy do nhóm tự viết, KHÔNG dùng LiteLLM.

Vì sao cần (bắt buộc):
  - ChatUniTest 2.1.1 chỉ nhận một danh sách tên mô hình cố định (gpt-3.5-turbo, gpt-4o, gpt-4o-mini, code-llama, ...).
    ChatUniTest khai "gpt-4o" (ngân sách ngữ cảnh 122.880 token); proxy đổi sang mô hình thật của ARROW.
  - Giữ cùng tham số suy luận như ARROW: proxy chèn các tham số của mô hình trong config.json của arrow
    (temperature, seed, reasoning_effort, provider = ghim Amazon Bedrock cho Qwen, ...),
    TRỪ max_tokens: giữ giới hạn câu trả lời của ChatUniTest (maxResponseTokens mặc định 1.024; chốt 26/11).
  - Ghi mỗi lượt gọi một dòng (nhãn lớp, mô hình, token vào/ra, thời gian) để lập bảng chi phí ARROW – ChatUniTest.
  - Khóa API chỉ nằm ở proxy (đọc giống arrow: biến ARROW_SECRET_<REF>, rồi secrets/<ref>.key; Windows: DPAPI),
    không ghi vào pom.xml.

Đường dẫn ChatUniTest gọi:  POST http://127.0.0.1:<cổng>/<nhãn>/<mô hình arrow>/v1/chat/completions
    <nhãn>  ví dụ <task_id>__<mô hình>__<giờ>, để biết lượt gọi thuộc lần chạy nào
Chạy thử độc lập:  python3 llm_proxy.py --port 4001 --log calls.jsonl
"""
import argparse                       # tham số dòng lệnh (khi chạy riêng)
import json                           # đọc/ghi JSON
import os                             # đường dẫn, biến môi trường
import sys                            # sys.exit
import threading                      # chạy server ở luồng nền; khóa khi ghi log
import time                           # đo thời gian mỗi lượt gọi
import urllib.error                   # lỗi HTTP khi gọi nhà cung cấp
import urllib.request                 # gửi yêu cầu HTTP (không cần cài thư viện ngoài)
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer   # server HTTP nhiều luồng có sẵn của Python

SKIP_PARAMS = {"max_tokens"}          # tham số của arrow KHÔNG chèn: giữ max_tokens do ChatUniTest tự gửi (chốt 26/11)


def read_secret(home, ref):
    """Đọc khóa API tên `ref` theo đúng thứ tự arrow dùng:
    1) biến môi trường ARROW_SECRET_<REF>; 2) Windows: file secrets/<ref>.bin mã hóa DPAPI; 3) file secrets/<ref>.key."""
    env = "ARROW_SECRET_" + ref.upper().replace("-", "_")      # ví dụ openrouter → ARROW_SECRET_OPENROUTER
    if os.environ.get(env):
        return os.environ[env].strip()
    d = os.path.join(home, "secrets")
    if os.name == "nt":                                         # Windows
        path = os.path.join(d, ref + ".bin")
        if os.path.exists(path):
            return _dpapi_unprotect(open(path, "rb").read(), b"ARROW Developer CLI secret v1").decode("utf-8").strip()
    path = os.path.join(d, ref + ".key")                        # macOS / Linux
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            return f.read().strip()
    raise RuntimeError(f"không có khóa '{ref}' (đặt {env} hoặc arrow secrets set {ref})")


def _dpapi_unprotect(data, entropy):
    """Giải mã khóa arrow lưu trên Windows (DPAPI: Windows tự mã hóa theo tài khoản người dùng).
    `entropy` là chuỗi phụ arrow dùng khi mã hóa; phải đúng thì mới giải được."""
    import ctypes                                               # gọi hàm C của Windows
    from ctypes import wintypes

    class BLOB(ctypes.Structure):                               # cấu trúc DATA_BLOB của Windows: (độ dài, con trỏ dữ liệu)
        _fields_ = [("cbData", wintypes.DWORD), ("pbData", ctypes.POINTER(ctypes.c_char))]

    def blob(b):                                                # đóng gói bytes thành BLOB (giữ buf để không bị giải phóng)
        buf = ctypes.create_string_buffer(b, len(b))
        return BLOB(len(b), ctypes.cast(buf, ctypes.POINTER(ctypes.c_char))), buf
    bin_, k1 = blob(data)
    ent, k2 = blob(entropy)
    out = BLOB()
    if not ctypes.windll.crypt32.CryptUnprotectData(ctypes.byref(bin_), None, ctypes.byref(ent), None, None, 0, ctypes.byref(out)):
        raise RuntimeError("không giải mã được khóa (DPAPI)")
    try:
        return ctypes.string_at(out.pbData, out.cbData)         # chép kết quả ra bytes của Python
    finally:
        ctypes.windll.kernel32.LocalFree(out.pbData)            # trả vùng nhớ Windows đã cấp


class Upstreams:
    """Tra mô hình của arrow (llm.models trong config.json của home gốc) → (url gọi, model_id thật, tham số, khóa)."""

    def __init__(self, home):
        with open(os.path.join(home, "config.json"), encoding="utf-8") as f:
            cfg = json.load(f)
        self.models = cfg["llm"]["models"]          # {tên mô hình arrow: {provider, model_id, params, ...}}
        self.providers = cfg["llm"]["providers"]    # {tên nhà cung cấp: {base_url, secret_ref, ...}}
        self.home = home
        self._keys = {}                             # nhớ khóa đã đọc, khỏi đọc lại mỗi lượt gọi

    def get(self, name):
        m = self.models[name]                       # ví dụ "qwen3coder-or"
        p = self.providers[m["provider"]]           # ví dụ "openrouter"
        ref = p.get("secret_ref")
        if ref and ref not in self._keys:
            self._keys[ref] = read_secret(self.home, ref)
        # url = base_url + /chat/completions; params là bản chép để không sửa cấu hình gốc
        return p["base_url"].rstrip("/") + "/chat/completions", m["model_id"], dict(m.get("params") or {}), self._keys.get(ref)


def make_handler(up, log_path, lock):
    """Tạo lớp xử lý yêu cầu HTTP (mỗi yêu cầu POST của ChatUniTest = một lượt gọi mô hình)."""
    class H(BaseHTTPRequestHandler):
        protocol_version = "HTTP/1.1"

        def log_message(self, *a):                  # tắt log mặc định của http.server (in ra màn hình)
            pass

        def do_POST(self):
            parts = [x for x in self.path.split("?")[0].split("/") if x]   # "/tag/model/v1/chat/completions" → các phần
            t0 = time.time()
            rec = {"ts": time.strftime("%Y-%m-%dT%H:%M:%S"), "path": self.path}   # dòng log của lượt gọi này
            try:
                if len(parts) < 4 or parts[-2:] != ["chat", "completions"]:
                    raise ValueError("đường dẫn phải là /<nhãn>/<mô hình>/v1/chat/completions")
                tag, alias = parts[0], parts[1]                             # nhãn lần chạy, tên mô hình arrow
                rec.update(tag=tag, model=alias)
                body = json.loads(self.rfile.read(int(self.headers.get("Content-Length") or 0)) or b"{}")   # thân yêu cầu
                url, model_id, params, key = up.get(alias)
                rec["client_model"] = body.get("model")                     # tên ChatUniTest gửi (gpt-4o)
                body["model"] = model_id                                    # đổi sang mô hình thật
                for k, v in params.items():                                 # chèn tham số của ARROW
                    if k not in SKIP_PARAMS:                                # trừ max_tokens
                        body[k] = v
                body.pop("stream", None)                                    # không dùng chế độ trả từng phần
                rec["sent_params"] = {k: body.get(k) for k in ("temperature", "seed", "reasoning_effort", "max_tokens", "top_p")}
                req = urllib.request.Request(url, data=json.dumps(body).encode(), method="POST",
                                             headers={"Content-Type": "application/json",
                                                      **({"Authorization": "Bearer " + key} if key else {})})
                try:
                    with urllib.request.urlopen(req, timeout=900) as r:     # chờ tối đa 15 phút một lượt
                        status, data = r.status, r.read()
                except urllib.error.HTTPError as e:                        # lỗi 4xx/5xx vẫn đọc thân để chuyển lại cho ChatUniTest
                    status, data = e.code, e.read()
                rec["status"] = status
                try:
                    j = json.loads(data)
                    u = j.get("usage") or {}                                # số token nhà cung cấp báo
                    rec.update(prompt_tokens=u.get("prompt_tokens"), completion_tokens=u.get("completion_tokens"),
                               model_id=j.get("model"),
                               finish_reason=((j.get("choices") or [{}])[0] or {}).get("finish_reason"))
                    if status >= 400:
                        rec["error"] = json.dumps(j.get("error") or j)[:500]
                except ValueError:                                         # câu trả lời không phải JSON
                    rec["error"] = data[:300].decode("utf-8", "replace")
            except Exception as e:                       # noqa: BLE001   lỗi của chính proxy → trả 502 cho ChatUniTest
                status, data = 502, json.dumps({"error": {"message": f"proxy: {e}"}}).encode()
                rec.update(status=status, error=str(e)[:500])
            rec["seconds"] = round(time.time() - t0, 2)
            with lock:                                                     # nhiều luồng cùng ghi → khóa để không lẫn dòng
                with open(log_path, "a", encoding="utf-8") as f:
                    f.write(json.dumps(rec, ensure_ascii=False) + "\n")
            self.send_response(status)                                     # chuyển nguyên câu trả lời về ChatUniTest
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
    return H


def start(home, log_path, port=0):
    """Chạy proxy trong một luồng nền; trả về (server, cổng). Gọi server.shutdown() để tắt.
    port=0: hệ điều hành tự chọn cổng trống."""
    os.makedirs(os.path.dirname(os.path.abspath(log_path)), exist_ok=True)
    srv = ThreadingHTTPServer(("127.0.0.1", port), make_handler(Upstreams(home), log_path, threading.Lock()))
    threading.Thread(target=srv.serve_forever, daemon=True).start()       # daemon: tự tắt khi chương trình chính thoát
    return srv, srv.server_address[1]


def main():
    """Chạy proxy riêng (để thử tay)."""
    ap = argparse.ArgumentParser(description="Proxy mô hình cho ChatUniTest (TN3)")
    ap.add_argument("--home", default=os.environ.get("ARROW_HOME", os.path.expanduser("~/.arrow")))
    ap.add_argument("--port", type=int, default=4001)
    ap.add_argument("--log", default="proxy_calls.jsonl")
    a = ap.parse_args()
    srv, port = start(a.home, a.log, a.port)
    print(f"Proxy chạy ở http://127.0.0.1:{port}/<nhãn>/<mô hình>/v1/chat/completions (Ctrl+C để dừng)")
    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        srv.shutdown()
        sys.exit(0)


if __name__ == "__main__":
    main()
