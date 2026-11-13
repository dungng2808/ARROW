"""Build DLL độc lập từ đúng sdist Python; mọi download/cache nằm trong Prelight_Net."""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tarfile
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT / ".work/native"
WORK.mkdir(parents=True, exist_ok=True)
os.environ["PIP_CACHE_DIR"] = str(WORK / "pip-cache")
os.environ["ZIG_GLOBAL_CACHE_DIR"] = str(WORK / "zig-cache")
os.environ["ZIG_LOCAL_CACHE_DIR"] = str(WORK / "zig-local-cache")
os.environ["TEMP"] = os.environ["TMP"] = str(WORK)
venv = WORK / "venv"
python = venv / "Scripts/python.exe"
if not python.exists(): subprocess.run([sys.executable, "-B", "-m", "venv", str(venv)], check=True)
subprocess.run([str(python), "-B", "-m", "pip", "install", "ziglang==0.13.0", "--disable-pip-version-check"], check=True)
sources, lock = {}, {"compiler": "ziglang==0.13.0", "sources": {}}
for name, version in (("tree-sitter", "0.24.0"), ("tree-sitter-java", "0.23.5")):
    with urllib.request.urlopen(f"https://pypi.org/pypi/{name}/{version}/json") as response: metadata = json.load(response)
    entry = next(item for item in metadata["urls"] if item["packagetype"] == "sdist")
    archive = WORK / entry["filename"]
    if not archive.exists(): urllib.request.urlretrieve(entry["url"], archive)
    digest = hashlib.sha256(archive.read_bytes()).hexdigest()
    if digest != entry["digests"]["sha256"]: raise ValueError("sdist checksum mismatch")
    with tarfile.open(archive) as tar:
        for member in tar:
            if not member.isfile(): continue
            target = (WORK / member.name).resolve()
            if not target.is_relative_to(WORK.resolve()): raise ValueError("unsafe sdist")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(tar.extractfile(member).read())
    sources[name] = WORK / entry["filename"].removesuffix(".tar.gz")
    lock["sources"][name] = {"version": version, "url": entry["url"], "sha256": digest}
core = next(sources["tree-sitter"].rglob("lib/src/lib.c")).parent.parent
grammar = next(sources["tree-sitter-java"].rglob("src/parser.c")).parent
output = ROOT / "tools/tree-sitter-native/bin/tree_sitter_java_preflight.dll"
output.parent.mkdir(parents=True, exist_ok=True)
headers = WORK / "include/tree_sitter"
headers.mkdir(parents=True, exist_ok=True)
(headers / "parser.h").write_bytes((core / "src/parser.h").read_bytes())
exports = WORK / "exports.def"
exports.write_text("EXPORTS\n" + "\n".join(("tree_sitter_java", "ts_parser_new", "ts_parser_set_language", "ts_parser_parse_string", "ts_tree_root_node", "ts_node_type", "ts_node_start_byte", "ts_node_end_byte", "ts_node_named_child_count", "ts_node_named_child", "ts_node_child_by_field_name", "ts_node_parent", "ts_node_eq", "ts_tree_delete", "ts_parser_delete")) + "\n", encoding="ascii")
subprocess.run([str(python), "-B", "-m", "ziglang", "cc", "-shared", "-O2", "-I" + str(WORK / "include"), "-I" + str(core / "include"), "-I" + str(core / "src"), "-I" + str(grammar), str(core / "src/lib.c"), str(grammar / "parser.c"), str(exports), "-o", str(output)], check=True)
lock["dll_sha256"] = hashlib.sha256(output.read_bytes()).hexdigest()
(ROOT / "tools/tree-sitter-native/tools.lock.json").write_text(json.dumps(lock, indent=2) + "\n", encoding="utf-8")
print(json.dumps({"output": str(output), "sha256": lock["dll_sha256"]}))
