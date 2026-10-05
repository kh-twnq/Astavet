# Rules — Git workflow & CI/CD

Each repo is an **independent git repository** (see `workspace.md`). Run all git commands inside the
specific repo folder. Derive the details below from each repo's CI config (e.g. `.gitlab-ci.yml` /
`.github/workflows/`), its git hooks (e.g. `.husky/`), and `git remote -v`; mark anything inferred
as **Unknown / needs confirmation**.

## Hosting
- Hosted on your Git host, e.g. GitLab via `glab` (or GitHub via `gh`):
  `<git-host>/<group>/<repo>.git`. Code review happens via Merge/Pull Requests.

## Branch model (environment branches)
- Long-lived branches commonly map to environments and drive deploys (read the repo's CI rules to
  confirm), e.g.:
  - `develop` — default branch → deploys to **dev**
  - `staging` → deploys to **stg**
  - `production` → deploys to **prd**
- Work happens on short-lived branches merged via MR/PR.
- **Do not assume the promotion path is the classic gitflow ladder.** A branch deploys to its
  environment when code lands on it; the *promotion path between* environments is a per-project
  decision — confirm it (see next section).

## Release target-branch flow (confirm per release)
**Follow your project's branch/release model, and confirm the deploy target per release.** Some
projects deviate from standard gitflow (`develop → staging → production`) temporarily — do not
"correct" a deliberate deviation to gitflow. Verify the current promotion direction from the
authoritative source (a change-request / RFC doc and the actual MR `source → target` on the Git
host) before each release, because it can change over time.

Common variations to check for:
- A repo whose changelog/config is environment-specific may promote differently from the others.
- Library repos may **publish from a specific branch/tag** (e.g. a `release/*` branch, or `develop`)
  rather than promote through env branches — merging there can be a public release, not a dev deploy.
- Tag-based repos release by **pushing a version tag**, not by merging an env branch; their env
  branches may be stale — base new work off the active default branch.
- Some repos have **extra deploy targets** (e.g. a separate tenant line) — check before branching.

## Branch base (confirm per fix — never auto-default)
Two different questions, do not conflate them:
- **Base of a working branch** (where a new `feature/*`/`bugfix/*` starts): the correct base differs
  per fix — **confirm the base before branching for each task; reuse confirmation already supplied**. Do not hardcode a default (not
  `develop`, not `staging`, not `production`). The deploy-promotion direction does **not** dictate
  where your working branch starts.
- **Target of a deploy MR** (env → env promotion at release time): follows the *Release target-branch
  flow* above, re-verified from the latest release doc. That is a release step, owned by `$ship-task`'s
  release path — not something you pick when opening a normal feature/bugfix branch.

## Branch naming (recommended)
- `feature/<user>/<short-desc>` and `bugfix/<user>/<short-desc>`, often including a ticket key:
  - e.g. `bugfix/<user>/PROJ-1234-inactive-user-session-destroyed`
  - e.g. `feature/<user>/cors-config`
- Ticket project key used in branches/tickets: **`PROJ`** (replace with your project's key).
- Prefer the `feature|bugfix/<user>/<key>-<desc>` form for new work.

## Frontend git hooks (if present, e.g. `.husky/`)
- `pre-commit` → `lint-staged` (ESLint + Prettier on staged files)
- `pre-push` → type-check must pass before push (e.g. `yarn tsc`)
- `commit-msg` → may enforce a commit-message convention. *(Confirm the configured commitlint/rule
  before relying on a specific format — Unknown / needs confirmation per repo.)*

## CI/CD pipelines (read each repo's CI config)
Typical shapes to expect (confirm per repo from its CI file):
- **Service repos**: `build → sast → deploy` (and sometimes `test`), branch-based on
  `develop`/`staging`/`production`; build a Docker image, tag it, push to the registry, and deploy
  by applying `k8s/` manifests via a provided kubeconfig.
- **Migration repos**: `build → deploy`, branch-based.
- **Library repos**: build → **package publish** (e.g. PyPI/npm), from a specific publish branch.
- **Tag-based repos**: run only on version tags (e.g. matching a `vX.Y.Z(.devN|.preN)?` pattern),
  build a wheel/package and publish. See the repo's `RELEASE.md` for the bump procedure.

## Merge Request format (title + description)
- **Title** = a short description of the work done (the main task) — **not** a conventional-commit
  prefix and **not** the ticket key.
- **Description** = just the ticket link, in the form `Task: <jira-base-url>/browse/<TICKET-KEY>`.
  No changelog/checklist body.
- Ticket base URL: `<jira-base-url>/browse/<TICKET-KEY>`.
- Tooling: use your Git host's CLI (e.g. `glab` for GitLab, `gh` for GitHub). Edit an existing MR
  with e.g. `glab mr update <iid> --title "…" --description "Task: <url>"` (run inside the repo).
- Opening an MR via push options can set these too, e.g.
  `-o merge_request.title=… -o merge_request.description="Task: <url>"`
  (push-option values **cannot contain newlines**).

## Conventions & tooling
- Commit/MR/ticket flow uses your Git host + issue tracker. Relevant available skills: `commit`
  (conventional commits), `$start-task`, `$ship-task`, `mr-feedback`.
- **Only commit or push when explicitly asked.** Never push directly to
  `develop`/`staging`/`production`; open an MR from a `feature/`/`bugfix/` branch.
- Don't commit secrets or `.env` files (see `workspace.md`).
