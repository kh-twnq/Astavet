# Workspace Rules (cross-cutting)

Conventions that apply across **all** repos in this workspace. Repo-specific detail lives in the
sibling `harness/rules/*.md` files; the high-level map is in the root `AGENTS.md`.

## Multi-repo workspace (may not be a monorepo)
- This workspace can hold several independent repositories (e.g. a VS Code multi-root workspace),
  **not** necessarily a single git monorepo. Treat each subfolder as its own git repository unless
  the root clearly is one.
- When it is not a monorepo there is **no** root-level build, no shared lockfile, and no cross-repo
  package linking on disk — repos integrate at **runtime over HTTP** (see `AGENTS.md` → Cross-Repo
  Interaction).
- **Don't** run a build at the workspace root, and **don't** `cd` across repos in one command. Run
  `git` / the build tool / the package manager **inside the specific repo folder**.

## JDK / toolchain per repo (mismatch fails confusingly)
- Repos may pin **different JDK versions** (and different Node/Python toolchains). A JDK mismatch
  fails confusingly. **Match each repo's configured JDK/toolchain** (check its `build.gradle` /
  `pom.xml` / `.tool-versions` / IDE workspace config) when building or testing from a terminal.
- Do NOT assume one JDK across the whole workspace — confirm per repo.

## Naming
- Keep the product/package naming consistent with each repo's existing convention. A product may
  have historical/alternate names that refer to the same thing — treat them as one product.

## Shared Java-service conventions (apply to every Spring/JPA service here)
- **Strict layering**: Controller(DTO) → Service(domain Model) → Repository(Model) →
  RepositoryImpl(Entity↔Model via a mapper such as MapStruct) → JPA(Entity). **JPA entities never
  leave the repository layer.**
- Interfaces in `service/` / `repository/`; implementations in `*/impl/` with an `Impl` suffix.
- **Naming**: entities `<Name>Entity`; DTOs `<Action>Request` / `<Action>Response`; repos
  `<Entity>Repository` → `<Entity>RepositoryImpl` → `Jpa<Entity>Repository`; constants in `constants/`.
- **API versioning** via `controller/v1/` packages.
- Build/run with the Gradle **wrapper** (`./gradlew …`), never a system Gradle.

## Development workflow
1. Open the workspace so every repo's JDK/toolchain is wired up.
2. Work **inside one repo at a time** — each has its own branch/history (see `git-workflow.md`).
3. For a feature touching multiple tiers, change them in dependency order and **keep the HTTP/DTO
   contract in sync on both sides** — no codegen will do it for you (see "No generated client" below).
4. Use each repo's configured package manager (e.g. `yarn` vs `npm`) — don't switch tools.
5. For any IDE extension repo, follow its own build/reinstall steps after a source change.

## Environment / secrets (general)
- Java services are typically profile-driven; repo-specific variables are documented in each repo's
  rules file.
- **Never commit real secrets.** `.env` files may exist in some repos — treat them as local-only.
- Prefer a secrets manager / env-var driven config over hard-coded credentials.

## Cross-cutting pitfalls
- **Multiple databases.** A workspace may own more than one database, each with its own migration
  repo. Put each schema changeset in the **correct** migration repo — never mix them.
- **Multiple frontends may consume one backend.** More than one UI (e.g. a user app and an
  admin/IMS app) can call the same backend. When a UI is **outside** the cross-repo contract group,
  route/DTO changes there need a **manual** grep — don't rely on tooling to catch it.
- **Some logic may live in an external service.** Validation/enforcement can live in a separate
  service rather than the backend. Fix the defect where the logic actually is, even when the symptom
  shows up elsewhere.
- **No generated cross-repo client.** A backend/service endpoint or DTO change won't propagate to a
  consumer automatically — update the consumer by hand. *(Verify: no generated/shared API client is
  usually checked in; `build/generated/` is typically only mapper/annotation-processor output.)*
- **Version drift in docs.** Verify versions in `build.gradle` / `package.json` before trusting
  prose — trust the build file over any doc.
- **Empty/placeholder READMEs**: some repos have minimal/empty READMEs — rely on the code and these
  rules.
- **Watch for symlinked agent docs.** In some repos alternative agent instruction files may be symlinks to a
  single `AGENTS.md` — edit the real source, not the symlink.

## General editing rules
1. **Do not modify source unless asked.** When asked, change only the relevant repo; keep edits scoped.
2. Follow each repo's existing conventions and match surrounding code style — don't introduce new
   patterns or new build tools.
3. Keep cross-tier contracts in sync (frontend ↔ backend ↔ any service) when you touch a DTO/route.
4. Don't weaken auth (JWT/OAuth2, any service authorization) or rate limiting; don't commit secrets.
5. For schema changes, add a migration changeset in the **correct** migration repo — never hand-edit
   the DB or rely on `ddl-auto` (keep it at `validate`).
6. Run the repo's lint/tests before declaring done; report real results (see `testing.md`).
7. When something is genuinely unclear, mark it **"Unknown / needs confirmation"** rather than
   inventing behavior.

## Navigation — important file paths
- Use each repo's own layout. Common entry points to look for:
  - Backend Spring service: `src/main/java/<base-package>/…Application.java`, config in
    `src/main/resources/application*.yml`, controllers under `controller/<domain>/`, DTOs under
    `dto/<domain>/`.
  - Web frontend: routing/config (e.g. `config/routes.ts`, `config/config.ts`), HTTP clients under
    `src/services/`, feature code under `src/pages/`.
  - Migration repo: changelogs under `src/main/resources/migration/`.

## Code navigation — knowledge graph first (if available)
If the workspace has a code knowledge-graph index (e.g. a per-repo GitNexus `.gitnexus/`), prefer
its graph tools over blind grep for "where does X live", "what calls this", "what breaks if I change
it" — see the relevant docs rule for the tool-per-phase table, freshness rules, and any cross-repo
contract registry (route↔consumer lookups). Anything the registry doesn't cover — dynamic URLs,
WS/STOMP/SSE, non-linked consumers — stays a manual sync check.

## Recommended first files to read for a new task
- **Backend/platform**: the repo's rules file → the `…Application.java` entry → relevant
  `controller/<domain>/` → matching `service/`, `dto/<domain>/`, `application-<concern>.yml`.
- **Frontend/UI**: `config/routes.ts` → the feature's `src/` code → `src/services/` → the env config.
- **A service (AI/codegen/validation/etc.)**: its rules file → its entry point → its handlers/providers.
- **Database/schema work**: the relevant migration repo's changelogs (+ that repo's rules/AGENTS.md).
