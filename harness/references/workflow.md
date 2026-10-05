# Task ledger contract

Use the scripts from the target repository root. Task IDs allow letters, digits,
underscores, dots and hyphens, start with a letter or digit, and have at most 80
characters. State and verification logs live in git-ignored `harness/state/`.

```sh
python3 harness/scripts/harness.py start TASK-123 --objective "Fix parsing" --base main
python3 harness/scripts/harness.py phase TASK-123 implement
git add -- <intended-files>
python3 harness/scripts/harness.py verify TASK-123
python3 harness/scripts/harness.py review TASK-123 --verdict pass --summary "Checked parser callers and failure paths"
python3 harness/scripts/harness.py ready TASK-123
python3 harness/scripts/harness.py handoff TASK-123 --summary "Ready; shipping was not requested"
```

`start` records the base but never creates a branch. The agent handles branch
selection using the user's intent and existing authorization. State does not
create tickets, log invented hours, or imply authorization to push.

Phases: `start -> implement -> verify -> ready_to_ship -> shipped`.
`blocked` is a local task outcome, not Codex's goal status. Verification failures
set blocked; fix and rerun verify. Tests and secret scanning must pass, a passing
review must match the fingerprint, and HEAD/branch/content must be unchanged for
`ready` to succeed. Review is a declared agent/human verdict, not an automated
code review. Record it only after examining the actual change.

Stage the intended changes before verification. `ready` requires the staged
index to match the complete verified working snapshot; unrelated dirty work
must be isolated first, never automatically staged. Staging after verification
invalidates evidence. Before commit, run `ready` immediately. After
commit, `ship TASK --url URL` checks that the new HEAD's parent matches the
verified HEAD, the new tree matches the verified working snapshot, the branch
is unchanged, and no remaining changes exist. This records an already completed
ship; it does not commit, push, open a PR, or verify the remote URL.

`shipped` means the supplied review URL was recorded after local checks. It does
not mean merged, deployed, or independently confirmed by a human.

The fingerprint includes all non-ignored untracked files. Ledger files are
ignored. Verification stores only exit status and stdout/stderr hashes;
command output is captured and hashed, not printed or persisted. To diagnose a
failed command, rerun its argv in the target repo. Secret
findings contain paths, line numbers, and categories, never token values.

A command timeout or a tracked/untracked change during verification fails the
run. Empty command lists are rejected unless `allow_no_tests: true`; this is for
explicitly configured docs-only projects. Commands use argv arrays, not a shell.
Runtime scripts require Python 3.9+; `doctor` checks TOML with Python 3.11+.

The ledger is single-writer. Do not have parallel agents mutate the same task
record. Git submodules and repositories with checkout/clean filters or CRLF
conversion are outside this MVP's snapshot-to-commit support.

For the full ticket/bugfix/release workflow, initialize graph.py and follow
[graph dependencies](../graph/README.md). Multi-repo coordination is described
in [installation](installation.md); each repo retains its own verifier evidence.
