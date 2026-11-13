import copy
import importlib.util
import pathlib
import random
import sqlite3
import contextlib
import io
import json
import tempfile
import types
import unittest
from unittest import mock

SPEC=importlib.util.spec_from_file_location('team',pathlib.Path(__file__).with_name('team.py'))
TEAM=importlib.util.module_from_spec(SPEC);SPEC.loader.exec_module(TEAM)

def task(i,repo='https://github.com/o/r'):
    return {'candidate':{'task_id':str(i),'project_id':'p','repo_url':repo,'class_path':f'src/main/java/C{i}.java','class_name':f'C{i}',
        'test_class_paths':[f'src/test/java/C{i}Test.java'],'source_json_paths':[f'p/{i}.json'],'source_json_sha256s':['a'*64]},
        'pinned_revision':{'checkout_sha':'b'*40},'extra':{'catlm.java_archive_sha256':'c'*64,'catlm.java_archive_bytes':'123'}}

class AssignmentTests(unittest.TestCase):
    def test_reallocation_is_exact_swap_of_quang_and_chinh(self):
        tasks=[task(i,f'https://github.com/o/r{i//3}') for i in range(240)]
        old_members={'dung':40,'ka':20,'quang':15,'han':15,'chinh':10}
        with mock.patch.object(TEAM,'MEMBERS',old_members):old=TEAM.allocate(tasks)
        new=TEAM.allocate(tasks)
        self.assertEqual(new['quang'],old['chinh'])
        self.assertEqual(new['chinh'],old['quang'])
        for member in ('dung','ka','han'):self.assertEqual(new[member],old[member])

    def shard_inputs(self,root):
        tasks=[task(1),task(2)]
        index=root/'state/class_index.sqlite';index.parent.mkdir(parents=True)
        with contextlib.closing(sqlite3.connect(index)) as db, db:
            db.execute('CREATE TABLE normalized_tasks(task_id TEXT,task_json TEXT)')
            db.executemany('INSERT INTO normalized_tasks VALUES(?,?)',[(t['candidate']['task_id'],json.dumps(t)) for t in tasks])
        plan=root/'plan.json'
        TEAM.write(plan,{'datasets':{'classes2test':{'pool_sha256':TEAM.pool_hash(tasks,'classes2test'),'assignments':{'quang':['1']}}}})
        return types.SimpleNamespace(plan=plan,dataset='classes2test',member='quang',index=index,output=root/'local-shard.json')

    def test_existing_shard_reused_only_when_assignment_is_unchanged(self):
        with tempfile.TemporaryDirectory() as temporary,contextlib.redirect_stdout(io.StringIO()):
            args=self.shard_inputs(pathlib.Path(temporary))
            TEAM.shard(args);before=args.output.read_bytes();TEAM.shard(args)
            self.assertEqual(before,args.output.read_bytes())
            plan=TEAM.read(args.plan);plan['datasets']['classes2test']['assignments']['quang']=['2'];TEAM.write(args.plan,plan)
            with self.assertRaisesRegex(ValueError,'another assignment'):TEAM.shard(args)
            self.assertEqual(before,args.output.read_bytes())

    def test_checkpoint_from_previous_assignment_is_not_resumed(self):
        with tempfile.TemporaryDirectory() as temporary,contextlib.redirect_stdout(io.StringIO()):
            args=self.shard_inputs(pathlib.Path(temporary))
            checkpoint=args.index.with_name('run_state.sqlite')
            with contextlib.closing(sqlite3.connect(checkpoint)) as db, db:
                db.execute('CREATE TABLE tasks(task_id TEXT)');db.execute("INSERT INTO tasks VALUES('2')")
            with self.assertRaisesRegex(ValueError,'Checkpoint belongs to a different assignment'):TEAM.shard(args)
            self.assertFalse(args.output.exists())

    def test_complete_disjoint_repo_assignment_and_deterministic_order(self):
        tasks=[task(i,f'https://github.com/o/r{i//3}') for i in range(240)]
        first=TEAM.allocate(tasks);random.Random(42).shuffle(tasks)
        self.assertEqual(first,TEAM.allocate(tasks))
        owner={i:m for m,ids in first.items() for i in ids}
        self.assertEqual(len(owner),240)
        for i in range(0,240,3):self.assertEqual(len({owner[str(j)] for j in range(i,i+3)}),1)
        self.assertGreater(len(first['dung']),len(first['ka']))
        self.assertGreater(len(first['ka']),len(first['chinh']))

    def test_catlm_local_paths_do_not_change_pool_but_commit_and_archive_do(self):
        original=task(1);relocated=copy.deepcopy(original)
        relocated['candidate']['source_json_paths']=['/Users/a/run/input.json']
        relocated['candidate']['source_json_sha256s']=['d'*64]
        self.assertEqual(TEAM.pool_hash([original],'catlm'),TEAM.pool_hash([relocated],'catlm'))
        relocated['pinned_revision']['checkout_sha']='e'*40
        self.assertNotEqual(TEAM.pool_hash([original],'catlm'),TEAM.pool_hash([relocated],'catlm'))
        relocated=copy.deepcopy(original);relocated['extra']['catlm.java_archive_sha256']='e'*64
        self.assertNotEqual(TEAM.pool_hash([original],'catlm'),TEAM.pool_hash([relocated],'catlm'))

    def test_c2t_evidence_change_and_gitbug_commit_change_rejected_by_pool(self):
        original=task(1);changed=copy.deepcopy(original)
        changed['candidate']['source_json_sha256s']=['e'*64]
        self.assertNotEqual(TEAM.pool_hash([original],'classes2test'),TEAM.pool_hash([changed],'classes2test'))
        changed=copy.deepcopy(original);changed['pinned_revision']['checkout_sha']='e'*40
        self.assertNotEqual(TEAM.pool_hash([original],'gitbug-java'),TEAM.pool_hash([changed],'gitbug-java'))

    def test_duplicate_ids_rejected(self):
        with self.assertRaises(ValueError):TEAM.pool_hash([task(1),task(1)],'classes2test')

    def test_plan_hash_ignores_json_whitespace_and_line_endings(self):
        plan={'datasets':{'catlm':{'total':119}},'weights':TEAM.MEMBERS}
        unix=json.dumps(plan,indent=2)+'\n'
        windows=unix.replace('\n','\r\n')
        self.assertEqual(TEAM.digest(json.loads(unix)),TEAM.digest(json.loads(windows)))

    def test_export_and_aggregate_validate_coverage_and_detect_changed_output(self):
        with tempfile.TemporaryDirectory() as temporary:
            root=pathlib.Path(temporary)
            tasks=[task(i,f'https://github.com/o/r{i}') for i in range(30)]
            assigned=TEAM.allocate(tasks)
            plan={'datasets':{d:{'total':30,'assignments':assigned,'pool_sha256':'f'*64} for d in TEAM.EXPECTED}}
            plan_path=root/'plan.json';TEAM.write(plan_path,plan)
            with mock.patch.object(TEAM,'ROOT',root),mock.patch.object(TEAM.subprocess,'check_output',return_value='123456\n'),contextlib.redirect_stdout(io.StringIO()):
                for dataset in TEAM.EXPECTED:
                    for member in TEAM.MEMBERS:
                        run=root/'runs'/member/dataset
                        TEAM.write(run/'provenance.json',{'run_status':'COMPLETED'})
                        TEAM.write(run/'reports/summary.json',{'total':len(assigned[member])})
                        records=[{'task_id':i,'technical_eligible':int(i)%2==0,'strict_eligible':False,'preflight_status':'ELIGIBLE' if int(i)%2==0 else 'EXCLUDED'} for i in assigned[member]]
                        (run/'reports/preflight_results.jsonl').write_text(''.join(json.dumps(r)+'\n' for r in records))
                        TEAM.export(types.SimpleNamespace(plan=plan_path,run=run,dataset=dataset,member=member))
                TEAM.aggregate(types.SimpleNamespace(plan=plan_path))
                result=TEAM.read(root/'results/team-r1/_aggregate/full-summary.json')
                self.assertEqual(result['total'],90)
                self.assertEqual(result['technical_pass'],45)
                def staged_unrelated(command,**kwargs):
                    value='experiment/team-r1-dung\n' if 'branch' in command else 'unrelated-user-file.txt\n'
                    return types.SimpleNamespace(stdout=value)
                with mock.patch.object(TEAM.subprocess,'run',side_effect=staged_unrelated) as git:
                    with self.assertRaisesRegex(ValueError,'Other staged changes'):
                        TEAM.publish(types.SimpleNamespace(plan=plan_path,member='dung'))
                    self.assertEqual(git.call_count,2)  # no add/commit/push on rejection
                target=next((root/'results/team-r1/dung/catlm').glob('preflight_results.part-*.jsonl'))
                target.write_bytes(target.read_bytes()+b'\n')
                with self.assertRaisesRegex(ValueError,'Changed export'):
                    TEAM.aggregate(types.SimpleNamespace(plan=plan_path))

if __name__=='__main__':unittest.main()
