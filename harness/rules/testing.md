# Rules — Testing

Run the relevant tests **inside the repo** before declaring work done, and report real results — never
claim green if you didn't run them.

## Per-repo commands (confirm each repo's own runner)
Read each repo's build/test config to find its real commands. Typical shapes by stack:

| Stack | Unit | Integration / more |
|-------|------|--------------------|
| Java/Spring service (Gradle) | `./gradlew test` | a separate task such as `./gradlew integrationTest` (Testcontainers, **needs Docker**); confirm whether integration tests are split out of the plain `test` task |
| Web frontend (Node) | `yarn test` / `npm test` (Jest) | `yarn test:coverage`; `yarn lint` / `yarn tsc`. Some FE repos have **no** e2e/Playwright suite — confirm |
| IDE extension (Python + TS) | `jlpm test` / `npm test` (Jest) + `pytest -vv -r ap --cov <pkg>` | Playwright/Galata in `ui-tests/` if present |
| Migration repo (Gradle) | `./gradlew test` (may use Testcontainers) | — |
| Python library | `python -m pytest tests/` | — (watch for repos with **no committed test modules** — a green run then proves nothing) |
| Validation/FastAPI service | `python -m pytest` | — |
| Template/asset catalog | often no test runner — verify each artifact builds (e.g. via its `Dockerfile`) | — |

## Notes
- A Spring service may **split** JPA/controller tests into a separate `integrationTest` task; plain
  `./gradlew test` then excludes them. Run both when touching repositories or controllers.
- Integration tests that use **Testcontainers** require a running Docker daemon.
- A frontend repo may have only Jest unit tests + `tsc --noEmit` (via `yarn lint` / a `pre-push`
  hook) and **no** browser/e2e suite — don't assume one exists.
- An IDE-extension repo may be the only one with Playwright/Galata UI tests (`ui-tests/`), plus Jest
  and pytest. Use the repo's configured TypeScript project/typecheck command (for example `tsc --noEmit -p tsconfig.json`); passing a file directly can bypass tsconfig options. `python -m py_compile <file>.py` is a syntax check, not server behavior coverage.
- Match the repo's JDK/toolchain for test runs (see `workspace.md`).

## Frontend test-writing exception (overrides the global "every change needs tests" default)
**Applies to all FE/UI source in the workspace, not one repo** — any repo/module whose source is
frontend/UI (TS/React or similar client code). Where an IDE-extension repo mixes a **TS/React
frontend** with a **Python server extension**, only the frontend half is in scope; the Python server
extension is backend code and **keeps** its mandatory tests, and any Playwright/Galata (`ui-tests/`)
suite keeps its expectations (this exception is about *unit* tests specifically).

- **FE source in scope does not require new unit tests.** This is a deliberate, project-level override
  of the global engineering-principle default ("every implementation suggests its tests") — scoped to
  frontend/UI code only. Root cause: FE repos here typically have no e2e/Playwright suite at all, and
  the team convention is not to author new Jest tests during implementation unless the user explicitly
  asks.
- This exception applies everywhere test effort is normally assumed, not just at write-time:
  - **Implementation** (`change-implementation`, ad-hoc coding): no new Jest/spec files for in-scope FE
    changes unless the user explicitly asks for them.
  - **`solution-planning` estimates**: do not add a test-writing work item, or pad the estimate for FE
    test effort, for work scoped to in-scope FE source. Backend/service/migration work — and an IDE
    extension's **Python** server-extension work — in the same ticket still gets its mandatory test
    line per `harness/rules/java.md` (Java) or the extension's pytest expectations.
  - **`completion-audit` scoring**: implemented-but-untested in-scope FE code scores **✅ Done**, not
    🟡 Partial — the untested-code-is-Partial rule does not apply there.
- **Still run the existing suite for verification** — `yarn test` / `jlpm test` (+ `tsc --noEmit` /
  `yarn lint`) before declaring FE work done, per the table above. The exception is about *authoring new
  tests*, not about skipping verification that nothing pre-existing broke.
- Every other repo/module keeps the mandatory-test rule unchanged: Java/Spring services and migration
  repos via `harness/rules/java.md` Phase 3; an IDE extension's **Python** server extension keeps pytest, and
  its **Playwright/Galata** UI suite keeps its expectations.

## Native verification profile

The table gives examples, not executable defaults. Set the actual per-repo argv/timeouts in `harness/profiles/local.json` or shared `default.json`. Stage only intended changes before final native verification, and rerun verify/review after content or index changes. `allow_no_tests` is an explicit docs-only setting, not the frontend exception: existing UI checks still run.
