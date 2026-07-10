# AGENTS.md - ARROW

This file is the local operating policy for the independent `ARROW`
pipeline. Follow it when modifying or running this pipeline.

## Scope

- `ARROW` is independent from `mini-agonetest`.
- Do not import or depend on code from `mini-agonetest`.
- `classes2test/dataset` is the input source for samples.
- Do not modify `classes2test/AgoneTest` as part of this pipeline.
- Prefer Vietnamese explanations for user-facing docs and answers.

## Output Layout

- Default output layout is repo/sample based:
  - `runs/<repo_folder>/<sample_id>/...`
- `repo_folder` is controlled by `config/pipeline.yaml`:
  - `repo_folder_name: repo_name` gives folders such as `runs/adbcj/...`
  - `repo_folder_name: project_id` gives folders such as `runs/13899/...`
  - `repo_folder_name: owner_repo` gives folders such as `runs/mheath-adbcj/...`
- Keep `run_id` and `shard_id` as report metadata, not as the default folder
  layout, unless `output.layout: run_shard` is explicitly configured.

## Report Policy

- JSON/JSONL is the primary report format.
- Per-experiment report:
  - `runs/<repo_folder>/<sample_id>/reports/records/<sample_id>/<agent>/<prompt>/result.json`
- Per-sample JSONL:
  - `runs/<repo_folder>/<sample_id>/reports/records/experiments.jsonl`
- CSV is an export/merge artifact, not the main per-run truth.
- Merge reports with:
  - `python -m src.run_pipeline --merge-reports --runs-dir runs --output-dir runs\merged`
- Paper-style fields must exist in `result.json`, JSONL, and merged class CSV:
  - `Generator(LLM)`
  - `Prompt_Technique`
  - `Compilation`
  - `Project_ID`
  - `Class_Under_Test`
  - `Branch_Coverage%`
  - `Line_Coverage%`
  - `Method_Coverage%`
  - `Mutation_Score%`
  - `NumberOfMethods`
  - all individual test smell columns.
- If coverage, mutation, or smell metrics are not executed, leave their fields
  empty rather than writing false zeroes.

## Cleanup Policy

- Repo clones are temporary execution resources.
- Default behavior:
  - clone to `repos/<project_id>/`;
  - copy to isolated workspace;
  - write report;
  - delete workspace and repo cache.
- `repo.delete_after_report: true` means repo cache should be removed after
  reports are written.
- `cleanup.delete_experiment_workspace_after_report: true` means experiment
  workspace should be removed after reports are written.
- Keep workspace/repo only when debugging with:
  - `--keep-workspace`
  - `--keep-repo-cache`

## LLM Policy

- All generation, repair, and regeneration calls must go through
  `src/llm_client.py`.
- Do not call Ollama, OpenAI, Groq, Anthropic, Google, or provider-specific
  clients directly from pipeline code.
- `--agent` should accept configured agent names and model aliases.
- For the current default config:
  - agent name: `qwen-coder-1.5b`
  - model: `ollama/qwen2.5-coder:1.5b`
  - accepted alias: `qwen2.5-coder:1.5b`
- LiteLLM metadata must be JSON-safe before writing files. Do not serialize raw
  LiteLLM response objects such as `Choices`.

## Prompt Policy

- Generation and repair prompts must clearly require:
  - exactly one Java compilation unit;
  - exact package from `package_name`;
  - exact public class name from `required_generated_class_name`;
  - Java source only;
  - no markdown fences;
  - no explanations;
  - no focal class rewrite;
  - no production code;
  - no build file, dependency, patch, or diff output.
- If LLM returns the focal class or any class name other than the required
  generated test class name, validation must reject it.
- Generated test class names should use:
  - `<FocalClassName>Test_<hash>`
- Validation may normalize a single safe generated-test naming variant such as
  `<FocalClassName>GeneratedTest_<hash>` or
  `<FocalClassName>AgoneGeneratedTest_<hash>` to the required
  `<FocalClassName>Test_<hash>` name before writing the file. Unrelated class
  names must still be rejected.

## Adaptive Repair Policy

- Adaptive Repair may modify only the generated test file.
- Never modify:
  - focal class;
  - production code;
  - existing human-written tests;
  - `pom.xml`;
  - `build.gradle`;
  - `settings.gradle`;
  - dependency configuration;
  - wrapper files.
- Repair decisions must be state-machine based.
- Do not use numeric error counts as a repair decision score.
- `compile_errors`, `test_failures`, and `test_errors` are telemetry only.
- Use failure states, normalized signatures, code hashes, real Maven/Gradle
  verification, checkpoints, rollback, prompt fallback, and bounded attempts.
- Best candidate means the latest verified progress/partial-progress/pass
  candidate, not the candidate with the lowest error count.
- Rollback must use filesystem copy from persisted best candidate or checkpoint,
  not Git reset/restore.

## Maven Multi-Module Policy

- Many dataset repos are Maven multi-module projects.
- Building only a module POM can fail when the module depends on sibling modules
  that are not installed locally.
- Default Maven strategy should be:
  - `build.maven.multi_module_strategy: root_with_pl_am`
  - `build.maven.use_also_make: true`
- This should produce commands shaped like:
  - `mvn -f <repo_root>/pom.xml -pl <module_path> -am ...`
- `-pl <module_path>` selects the module containing the focal/generated test.
- `-am` asks Maven to also build required upstream modules.
- For target-test runs with `-Dtest=<GeneratedTest>`, also pass:
  - `-Dsurefire.failIfNoSpecifiedTests=false`
- Reason: with `-am`, Maven may execute test phase in dependency modules before
  the target module. Those modules do not contain the generated test. Without
  `surefire.failIfNoSpecifiedTests=false`, Surefire fails with:
  - `No tests matching pattern "<GeneratedTest>" were executed!`
- Keep `-DfailIfNoTests=false` too. It handles modules with no tests at all.
- These two flags are different:
  - `-DfailIfNoTests=false`: do not fail when a module has no tests.
  - `-Dsurefire.failIfNoSpecifiedTests=false`: do not fail when `-Dtest=...`
    matches no test in a particular module.

## Baseline Failure Policy

- Always run baseline verification before adding generated test.
- If baseline fails due to project/build configuration, do not call LLM repair.
- Examples of baseline/build configuration failures:
  - missing sibling Maven artifact;
  - Java source/target too old for current JDK;
  - missing tool/dependency.
- For Java version failures such as:
  - `source option 5 is no longer supported`
  - `target option 5 is no longer supported`
  prefer running with an older configured `--java-home`, usually JDK 8, before
  blaming generated tests.

## Test and Verification Policy

- After code changes, run:
  - `python -m compileall src tests`
  - `python -m pytest tests`
- For smoke without real LLM, use:
  - `--mock-llm-smoke`
- For a real LLM run, ensure:
  - `python -m pip install -r requirements.txt`
  - `ollama serve`
  - `ollama pull qwen2.5-coder:1.5b`
- Do not claim end-to-end success when module tests fail, even if target test
  passes.
