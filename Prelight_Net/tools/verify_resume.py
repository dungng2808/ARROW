"""Hard-kill the .NET CLI during a synthetic local build, then verify durable resume."""
import ctypes, hashlib, json, os, pathlib, sqlite3, subprocess, time
ROOT = pathlib.Path(__file__).resolve().parents[1]
CLI = ROOT / '.work/cli/preflight-net.dll'
WORK = ROOT / '.work/acceptance' / ('hard-kill-' + time.strftime('%Y%m%d%H%M%S'))
WORK.mkdir(parents=True)
def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True); path.write_text(text, encoding='utf-8', newline='\n')
def call(args, **kw):
    return subprocess.run(args, cwd=ROOT, check=True, capture_output=True, text=True, creationflags=subprocess.CREATE_NO_WINDOW, **kw)
def cli(*args): return call(['dotnet', str(CLI), *map(str,args)])
repo = WORK / 'repository'; repo.mkdir()
write(repo / 'src/main/java/p/X.java', 'package p; public class X { public void work(){} }')
write(repo / 'src/test/java/p/XTest.java', 'package p; public class XTest {}')
write(repo / 'pom.xml', '<project><properties><maven.compiler.target>17</maven.compiler.target></properties></project>')
marker, release = WORK / 'building.pid', WORK / 'release'
ps = WORK / 'wait.ps1'
write(ps, f"[System.IO.File]::WriteAllText('{marker}', [string]$PID)\nStart-Sleep -Seconds 60\n")
write(repo / 'mvnw.cmd', f'@echo off\nif "%~1"=="--version" (echo Maven fixture& exit /b 0)\nif exist "{release}" exit /b 0\npowershell -NoProfile -ExecutionPolicy Bypass -File "{ps}"\nexit /b 0\n')
git = ['git','-c','safe.directory=' + repo.as_posix(),'-C',str(repo)]
for args in (['init','-b','main'],['add','.'],['-c','user.name=fixture','-c','user.email=fixture@example.test','commit','-m','fixture']): call(git+args)
record = {'repository':{'url':str(repo)},'focal_class':{'identifier':'X','file':'src/main/java/p/X.java'},'test_class':{'file':'src/test/java/p/XTest.java'}}
write(WORK / 'input/1/1.json', json.dumps(record))
config = {'run':{'workers':1,'offline':True,'build_timeout_seconds':120},'java':{'default_home':str(ROOT.parent/'Java-version/runtime/jdk-17')},'disk':{'min_free_gb':0,'resume_free_gb':0}}
write(WORK/'config.json', json.dumps(config))
run = WORK/'run'
cli('normalize','--dataset','classes2test','--config',WORK/'config.json','--input-root',WORK/'input','--output-dir',run)
with (WORK/'initial.log').open('w',encoding='utf-8') as log:
    child = subprocess.Popen(['dotnet',str(CLI),'run','--output-dir',str(run),'--fast'],cwd=ROOT,stdout=log,stderr=log,creationflags=subprocess.CREATE_NO_WINDOW)
    try:
        deadline = time.monotonic()+30
        while not marker.exists() and child.poll() is None and time.monotonic()<deadline: time.sleep(.1)
        assert marker.exists(), 'Build did not reach blocking fixture'
        helper_pid = int(marker.read_text())
        call(['taskkill','/PID',str(child.pid),'/F']); child.wait(timeout=10)
        time.sleep(.5)
        handle = ctypes.windll.kernel32.OpenProcess(0x1000,False,helper_pid)
        if handle:
            code = ctypes.c_ulong(); ctypes.windll.kernel32.GetExitCodeProcess(handle,ctypes.byref(code)); ctypes.windll.kernel32.CloseHandle(handle)
            assert code.value != 259, 'Build child survived parent hard kill'
    finally:
        if child.poll() is None: child.kill(); child.wait()
write(release, 'continue')
result = cli('resume','--output-dir',run,'--workers',2)
with sqlite3.connect(run/'state/run_state.sqlite') as db:
    attempts = db.execute('select attempt_number,status from task_attempts order by attempt_number').fetchall()
    sessions = db.execute('select status from sessions order by session_id').fetchall()
    assert attempts == [(1,'ABANDONED'),(2,'COMPLETED')], attempts
    assert sessions == [('ABANDONED',),('COMPLETED',)], sessions
    assert db.execute("select count(*) from tasks where status='COMPLETED'").fetchone()[0] == 1
rows = [json.loads(line) for line in (run/'reports/preflight_results.jsonl').read_text().splitlines()]
assert len(rows)==1 and rows[0]['preflight_status']=='PRECHECKED'
cli('summarize','--run-dir',run,'--output-dir',WORK/'summary')
report = {'passed':True,'parent_hard_kill':True,'build_child_terminated':True,'attempts':attempts,'sessions':sessions,'result_count':len(rows),'run':str(run)}
write(ROOT/'.work/verification/hard_kill_resume.json',json.dumps(report,indent=2)+'\n')
print(json.dumps(report))
