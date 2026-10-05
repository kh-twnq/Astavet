# Installation

Keep this repository as the harness source. Install into an existing target Git
root (the installer refuses subdirectories and symlink destinations):

```sh
python3 harness/scripts/install.py /absolute/path/to/target
python3 harness/scripts/install.py /absolute/path/to/target --apply
```

Default is dry-run. All destinations are checked before any write. Existing
identical files are skipped; conflicting files stop the install. There is no
force-overwrite option. Merge existing AGENTS.md/config manually before using
this installer. It copies the required skills, config, scripts, schemas,
references, and templates; tests and CI stay in this source repository. All active skills, rules, component
docs, graph contracts, native roles and runtime scripts are included.

Installed `default.json` starts with no test command and will fail verify until
configured. Set real argv commands in `harness/profiles/local.json` (ignored),
or edit/commit `default.json` for team-wide defaults. Example:

```json
{"name":"service","verification":{"commands":[{"name":"tests","argv":["./gradlew","test"],"timeout_seconds":600}],"allow_no_tests":false}}
```

The generated AGENTS.md points at the target profile; update its repo map and
conventions. The installer appends an ignore block for local state/profile to
`.gitignore`. It never modifies your global `~/.codex` configuration, trusts a
project, creates branches, commits, or publishes.

Open the target Git root in Codex and trust its project configuration if the
client asks. Restart the session after installing skills/config. Run `doctor`,
then test hooks with the synthetic JSON examples in the source tests. Hook
commands resolve the Git root so opening a subdirectory works.

Local command hooks depend on client support and trust. Cloud-orchestrated Work
sessions do not load these local command hooks. Do not rely on them as a sandbox.
Native command `.rules` are optional and distinct from Markdown coding rules.

Official references, checked 2026-10-05:
- [AGENTS.md](https://learn.chatgpt.com/docs/agent-configuration/agents-md)
- [Skills](https://learn.chatgpt.com/docs/build-skills)
- [Configuration](https://learn.chatgpt.com/docs/config-file/config-basic)
- [Hooks](https://learn.chatgpt.com/docs/hooks)
- [Subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents)

For multi-repo work, configure workspace.repos (name, path, optional toolchain)
and contracts (name, producer, consumers, validation) in the profile. Resolve
relative paths against the coordinating Git root, not the session subdirectory.
Install and configure each target separately; each task has per-repo evidence.
Source `check_parity.py` checks all mapped artifacts; after installation use
`check_parity.py --installed` to omit source-only tests, CI and release docs.

Source verification now uses `harness/source-inventory.json`, an independent
inventory of the Claude working-tree snapshot, to detect missing mappings and
changed source digests. Run `python3 harness/scripts/check_parity.py --source /absolute/path/to/claude` to also compare current tracked source files and hashes.
Without `--source`, the bundled snapshot is checked; it is not a live source scan.
Review inventory updates against the source checkout rather than regenerating
it from parity mappings. Coverage still does not prove semantic equivalence.

The installer honors source Git ignore rules and excludes local-only config,
profiles, env files and Python caches even when accidentally tracked. It ships
`harness/examples/AGENTS.md` as the target's root conventions template, including
Repository Map, contracts, toolchains and project boundaries. Configure that
map before using project-specific workflows.

Run `python3 harness/scripts/check_native.py` with the Codex CLI installed to
check repo skill discovery and whether project config actually loaded. It uses
app-server initialize/config-read/skills-list without requesting a model turn
or changing user configuration. Untrusted project config is a failed check;
review and trust the installed project in the client, then rerun. It does not
claim live tool-hook dispatch or agent behavior has been tested.
Protocol reference: [Codex App Server](https://learn.chatgpt.com/docs/app-server).
