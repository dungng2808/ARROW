"""Compare the full .NET Classes2Test index with the original Python index, read-only."""
import hashlib, itertools, json, pathlib, sqlite3
ROOT=pathlib.Path(__file__).resolve().parents[1]
old=ROOT.parent/'preflight_tool/runs/quang-shard-03-20260925T131230Z/state/class_index.sqlite'
new=ROOT/'.work/acceptance/c2t-parallel/state/class_index.sqlite'
report={}
with sqlite3.connect(old.as_uri()+'?mode=ro&immutable=1',uri=True) as a, sqlite3.connect(new.as_uri()+'?mode=ro',uri=True) as b:
    for table,order,expected in [('classes','task_id',85819),('evidence','task_id,source_json_path',362414),('input_rejections','source_json_path',0)]:
        count=0
        for left,right in itertools.zip_longest(a.execute(f'SELECT * FROM {table} ORDER BY {order}'),b.execute(f'SELECT * FROM {table} ORDER BY {order}')):
            assert left==right,(table,left,right); count+=1
        assert count==expected,(table,count)
        report[table]={'matched':count,'expected':expected}
for path in sorted((ROOT.parent/'shards-5').glob('shard-*.json')):
    counterpart=ROOT/'.work/acceptance/shards-full'/path.name
    assert path.read_bytes()==counterpart.read_bytes(),path.name
    report[path.name]={'byte_identical':True,'sha256':hashlib.sha256(counterpart.read_bytes()).hexdigest()}
(ROOT/'.work/verification/classes2test_full.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report))
