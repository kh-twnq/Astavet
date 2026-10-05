# Rules — Database migrations (Liquibase/Flyway migration repos)

> Partly generic. The repo-specific detail for this workspace's migration repos has been removed
> during genericization, but the **staged-change patterns** in the second half apply to any
> Liquibase/Flyway-managed schema — keep them. Fill in your own migration repos at the top.

Migration repos are typically **batch jobs** (e.g. a Liquibase Kubernetes Job) that run **before**
their paired service deploys, then exit. They are not web servers.

## ⚠️ Keep each changeset in the right repo / database
If a workspace has more than one database, it usually has one migration repo **per database**. Record
the mapping (migration repo → target DB → paired service → changelog tables / default schema) and
**never add a changeset for one database to another database's migration repo.**

## Per-repo facts to record
- Stack (Spring Boot + Java version, Gradle, Liquibase/Flyway, PostgreSQL), entry class, changelog
  locations, test approach (e.g. Testcontainers).
- **How the target environment is selected** — by branch, or by which master changelog a branch's CI
  points at. A common trap: `staging` and `production` sharing one master file while `dev` has its own,
  so a changeset placed in a `dev/`-only folder **runs on dev only** and merging the branch forward
  does **not** make it run on staging/prod (silent no-op — deploy goes green, the DB keeps old data).
  Promoting requires adding a matching changeset under the shared/prod path, not just merging forward.
- Multi-schema / multi-tenant setups: whether one `SpringLiquibase` instance runs per schema, and the
  config classes that drive it.
- File conventions (formatted-SQL naming like `VYYYYMMDDHHMI__description.sql`; env-split changeset
  folders).

## Build / deploy
```bash
./gradlew clean build    # full build with tests
./gradlew test           # tests only
```
Deploy via `k8s/` + `Dockerfile` (per repo).

## Pitfalls / rules
- Match the repo's configured JDK.
- For any schema change, add a **new changeset** — never hand-edit the DB and never rely on `ddl-auto`
  in the services (should be `validate`; verify every profile — see "ddl-auto drift" below).
- Migrations run before the service deploys; a broken changeset blocks the deploy.

## Adding a NOT NULL column/constraint to an existing (non-empty) table — mandatory staged pattern

Applies to any Liquibase/Flyway-managed schema. Never collapse this into one changeset:
1. **Add the column nullable** (or add the constraint as `NOT VALID`/deferred), separate changeset.
2. **Backfill** existing rows in a separate changeset (`UPDATE ... SET col = <derived-or-default>
   WHERE col IS NULL`).
3. **Enforce `NOT NULL`** (or validate the constraint) only after the backfill changeset has run
   *and*, if any application code needs to write the new column first, after that code is deployed —
   document this ordering dependency explicitly in the changeset's `<comment>`, since Liquibase has
   no built-in way to block a changeset on "the paired service's new deploy has happened."
4. Only then, if a rename is also needed, do the `RENAME COLUMN` as its own final changeset.

Skipping straight to `ALTER COLUMN ... SET NOT NULL` on a table that already has rows is a
**destructive-risk changeset** — it will fail outright if any existing row is null, or silently
reject writes from an out-of-date service instance that hasn't been updated to populate the new
column yet.

## Preconditions — use them for environment/state-sensitive changesets, not just comments

When a changeset must only run in a specific schema, environment, or after a specific prior state,
prefer a Liquibase `<preConditions>` block (`sqlCheck`, `columnExists`, `tableExists`, `rowCount`,
etc.) over a comment-only warning — a precondition is tool-enforced; a comment narrating "run this
only after X" can be missed by a future author.

## Multi-schema/multi-tenant migration runners often have no partial-failure isolation

If a migration job iterates multiple schemas/tenants and applies changesets to each in a loop, check
whether a single try/catch wraps the *entire* loop rather than per-schema: if so, one bad schema
aborts the whole run, leaving later schemas unmigrated with no compensating rollback of the ones
already done. Recovery is usually "fix the problem and re-run" (safe for individual changeset
idempotency, since a properly-tracked changelog table skips already-applied changesets), but any
manually-staged multi-step rollout (see the NOT-NULL pattern above) must be **manually re-verified**
after a partial-failure re-run — the tool has no awareness of a staging sequence, only of per-changeset
idempotency.

## `ddl-auto`/`hbm2ddl.auto` drift risk across environment profiles

If a migration tool is the schema authority, every environment profile of the paired service should
have `ddl-auto`/`hbm2ddl.auto` set to `validate` (or unset) — never `update`/`create`. Check every
profile, not just the ones you'd normally deploy to; a dev-only profile hardcoding a more permissive
value is a real schema-authority conflict if that profile is ever pointed at a shared/long-lived
database, and it's easy to miss because production profiles look correct. If you find one, it's a
repo source-code fix — flag it rather than change it silently.

## Rollback coverage

New changesets should include a `<rollback>` block unless the change is a pure, already-idempotent
seed/data-correction script where a rollback wouldn't be meaningful — state clearly in the changeset
comment why one was omitted rather than silently skipping it. Note that having rollback SQL present
is not the same as it being verified: unless a test actually executes `liquibase rollback`, rollback
correctness is unverified regardless of how complete the coverage looks.
