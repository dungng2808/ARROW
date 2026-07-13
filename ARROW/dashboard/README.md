# ARROW Dashboard

Run from the repository root:

```powershell
python -m dashboard.server --host 127.0.0.1 --port 8765
```

Open:

```text
http://127.0.0.1:8765
```

The dashboard is a thin UI layer over the existing pipeline:

- reads `config/pipeline.yaml` for agents, prompts, and Adaptive Repair settings;
- lists all projects from `../classes2test/dataset`;
- launches `python -m src.run_pipeline` as a subprocess;
- writes per-run logs to `dashboard/logs`;
- writes per-run temporary configs to `dashboard/runtime_configs`;
- saves launcher run history to `dashboard/run_records` so rerun/resume remains available after restarting the dashboard;
- writes filtered rerun project lists to `dashboard/runtime_shards`;
- reads experiment results from `runs/**/reports/records/**/result.json`;
- reads Adaptive Repair checkpoints from `runs/**/<agent>/<prompt>/repair/checkpoints`.

`retry_mode: unlimited` is implemented as unlimited repair attempts with prompt-order cycling and a wall-clock guard.

Java selection order:

- `Override JAVA_HOME`: passes the user-provided path to `--java-home` for this run;
- `JDK version map`: after the repo is cloned, detects the Java version requested by Maven/Gradle/project files and uses the matching `build.java_homes` entry;
- `Java default`: uses `build.java_default` when no project Java version is detected or no matching map entry exists;
- system default: if the configured paths are empty/missing, Maven/Gradle use the process default.

`JDK version map` accepts one mapping per line:

```text
java-8: D:\Tools\jdk1.8.0_202
java-11: D:\Tools\jdk-11
java-17: D:\Tools\jdk-17
java-21: D:\Tools\jdk-21
```

To download local Temurin JDKs into the repository folder `Java version` and
generate a copy-ready map, run from the ARROW root:

```powershell
.\scripts\install-java-versions.ps1
```

The script installs `jdk-8`, `jdk-11`, `jdk-17`, and `jdk-21` by default and
writes `Java version\java-version-map.txt`.

This dashboard value is written to `build.java_homes` in the per-run runtime
config. The `Java default` field is written to `build.java_default`.

If a project Java version is detected but no matching configured JDK exists, the pipeline falls back to `build.java_default`; if that is also missing, it uses the system default Java.

When a launched run finishes, the dashboard polls run status and refreshes the experiment table automatically.

Running jobs can be interrupted with the `Stop` button in the run list. The dashboard stops the pipeline process tree, including child Maven/Gradle/Git processes on Windows.

After a shard has finished or been stopped, `Run again` appears with four choices:

- `Run all again`: run the whole selected shard from the beginning;
- `Run failed projects`: run only projects with at least one failed experiment in the latest run;
- `Continue stopped run`: include the project that was interrupted, then continue with every remaining project.
- `Retry failed, then continue`: first rerun failed projects from the stopped run, then include the interrupted project and every remaining project; duplicate project IDs are removed.

The filtered modes always reset `Start index` to `0` and `Limit` to `0` because they operate on a newly generated project list.

Pipeline logs include the current phase, selected Java behavior, Maven/Gradle command, verification result, and Adaptive Repair attempt decisions.

## Running four shards from the dashboard

The dashboard can launch the existing shard files in `shards/`.

Recommended setup for four people:

```text
person 00 -> repo_shard_00.txt, shard id person_00
person 01 -> repo_shard_01.txt, shard id person_01
person 02 -> repo_shard_02.txt, shard id person_02
person 03 -> repo_shard_03.txt, shard id person_03
```

In the launcher:

- set `Run scope` to `Shard batch`;
- choose the assigned `Shard file`;
- set `Input mode` to `Project`;
- set `Samples/project` to `1` for one sample per repository, or `all` for every sample in each repository;
- set `Start index` to `0`;
- set `Limit` to `0` to run all remaining inputs in that shard.

`Limit = 0` means all remaining inputs from the selected shard.
