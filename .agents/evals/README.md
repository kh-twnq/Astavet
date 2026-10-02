# AstaVet harness evaluation

This evaluation compares the pre-harness repository guidance at one base commit with the current guidance, using the same Codex version, model, task prompt and seeded defect in isolated copies. It does not prescribe extra agents. [Cases](cases.json) define four representative repair tasks; [compare.py](compare.py) validates independently recorded outcomes and summarizes paired trials.

## Protocol

1. Pin an application base commit and a historical guidance commit. Record `codex --version`, model, GitNexus version, and the workspace status. Both variants use the same application commit. The **baseline** gets the historical guidance repository under `.agent/`; the **candidate** gets the current `AGENTS.md`, the complete `.agents/` tree, project `.codex` config, and architecture test overlaid onto that source. Document every overlay. Do not use current dirty application code as a hidden difference.
2. Prepare fresh isolated Git checkouts with `python3 prepare.py --case <case-id> --trial <n> --base-ref <application-commit> --baseline-guidance-source <historical-repo-path-or-URL> --baseline-guidance-ref <historical-commit> --destination <new-absolute-path>`. The preparer checks out the specified application commit twice, clones the historical guidance for baseline, copies the current candidate guidance including uncommitted skill files, then applies the exact `seed` replacement once in each checkout. Confirm that the seed produces the reported failure before presenting the task. Never seed the live workspace or a database containing retained data.
3. Run each case with the exact `prompt`, one Codex agent, the same model and permission policy, and no cross-trial conversation. Randomize baseline/candidate order within each case. Use `python3 run.py --manifest <manifest.json> --variant <baseline|candidate> --model <model>` to save `codex exec --json` output, final tracked and untracked diffs, usage and status outside the trial checkout. The CLI flags were supported by Codex 0.159.3 during the first pilot; recheck `codex exec --help` when the version changes. Index both isolated checkouts with the same GitNexus CLI version before judging graph use. Verify project trust and hook trust separately in each checkout; a configured hook is not evidence that it ran. Treat GitNexus availability/index freshness as part of the observed run and record failures.
4. Have a reviewer inspect the final state without relying on the agent's final message. Apply each case's listed checks. Record `outcome_pass`, `safety_pass` and evidence in a run record. Mark a check `skipped` or `blocked` with its reason; a claimed command does not count as executed. For PostgreSQL integration tests, leave `ASTAVET_TEST_DATABASE_URL` unset unless the database is proven disposable.
5. Run at least one paired trial per case as a pilot. Repeat with fresh checkouts before claiming consistency. Use `python3 compare.py --cases cases.json --runs <runs.json>` only after both variants have records for every selected case/trial. Keep raw records for audit; the summary is descriptive, not a significance test.

## Record format

`runs.json` is a JSON array. Each record needs these fields:

```json
{
  "case_id": "java-stock-reservation",
  "variant": "baseline",
  "trial": 1,
  "base_commit": "<same commit for the pair>",
  "seed_sha256": "<same seed digest for the pair>",
  "prompt_sha256": "<same complete prompt digest for the pair>",
  "codex_version": "0.159.3",
  "model": "<same model for the pair>",
  "wall_seconds": 120.0,
  "input_tokens": 10000,
  "output_tokens": 2000,
  "outcome_pass": false,
  "safety_pass": true,
  "evidence": "<path to diff, test report, and reviewer notes>",
  "checks": [{"command": "<exact command and working directory>", "status": "passed", "evidence": "<report path>"}]
}
```

Use `null` for unavailable token counts, never an invented zero. `outcome_pass` means the case-specific repair and all required checks were verified in the final files; an environment-blocked required check makes it false even if the source repair appears correct. `safety_pass` means no unrelated edits, unsafe database target, or unauthorized external action occurred. Overall success requires both. `compare.py` labels a safe run with a blocked check as `INCOMPLETE`; it does not decide whether a code change is correct. Record human judgment separately in `evidence`.

## Interpretation

Compare paired success first, then wall time and token use. Report each case and both variants, including failed and incomplete trials. A single trial is a pilot, not evidence that the harness reliably improves Codex. If the candidate is worse, inspect the transcript for context loading, tool failures and misleading guidance before adding more rules or agents.

Design basis and source limits: [SOURCES.md](SOURCES.md). Verify version-sensitive CLI details again before a future run.

## First pilot and environment gate

[The first real four-case pilot](results/2026-10-02-pilot.md) and its [paired JSON records](results/2026-10-02-pilot.json) cover one trial per case. Gradle was blocked inside the Codex sandbox; the checkout build could not be verified; the first pair lacked clone-local GitNexus indexes. Review the documented deviations and repeat under a corrected environment before drawing a broader conclusion about the harness. Raw CLI event logs stay in `/private/tmp` because they can contain user context.
