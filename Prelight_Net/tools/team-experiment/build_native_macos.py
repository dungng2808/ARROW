"""Build the locked tree-sitter sources for native macOS; no business logic."""
import hashlib
import json
import pathlib
import platform
import subprocess
import tarfile
import urllib.request

ROOT=pathlib.Path(__file__).resolve().parents[2]
if platform.system()!='Darwin' or platform.machine()!='arm64':
    raise SystemExit('Run from an ARM64 terminal on Apple Silicon macOS; no Windows build or Rosetta.')
WORK=ROOT/'.work/native-macos'; WORK.mkdir(parents=True,exist_ok=True)
lock=json.loads((ROOT/'tools/tree-sitter-native/tools.lock.json').read_text())
sources={}
for name,item in lock['sources'].items():
    archive=WORK/(name+'.tar.gz')
    if not archive.exists(): urllib.request.urlretrieve(item['url'],archive)
    if hashlib.sha256(archive.read_bytes()).hexdigest()!=item['sha256']:
        raise ValueError('Source checksum mismatch: '+name)
    with tarfile.open(archive) as tar:
        for member in tar:
            if not member.isfile():continue
            target=(WORK/member.name).resolve()
            if not target.is_relative_to(WORK.resolve()):raise ValueError('Unsafe archive member')
            target.parent.mkdir(parents=True,exist_ok=True)
            with tar.extractfile(member) as stream:target.write_bytes(stream.read())
    sources[name]=next(WORK.glob('tree*'+item['version']))
core=next(sources['tree-sitter'].rglob('lib/src/lib.c')).parent.parent
grammar=next(sources['tree-sitter-java'].rglob('src/parser.c')).parent
include=WORK/'include/tree_sitter';include.mkdir(parents=True,exist_ok=True)
(include/'parser.h').write_bytes((core/'src/parser.h').read_bytes())
output=WORK/'libtree_sitter_java_preflight.dylib'
command=['clang','-dynamiclib','-O2','-std=gnu11','-arch','arm64','-I'+str(include.parent),'-I'+str(core/'include'),'-I'+str(core/'src'),'-I'+str(grammar),str(core/'src/lib.c'),str(grammar/'parser.c'),'-o',str(output)]
subprocess.run(command,check=True)
metadata={'platform':'macos-arm64','sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'sources':lock['sources'],'command':command,'output':str(output)}
(WORK/'build.json').write_text(json.dumps(metadata,indent=2)+'\n')
print(json.dumps(metadata))
