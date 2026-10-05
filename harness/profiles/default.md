# AstaVet project profile

Independent repository for a single Spring Boot commerce application with a same-origin browser client. Java 21, Spring Boot 4.1.1, Gradle wrapper 9.7.1 and PostgreSQL. No tracker or remote repository is configured.

`src/main/java` owns API and domain behavior; `src/main/resources/static` consumes the handwritten API; `src/main/resources/db/migration` owns this application's schema. No external consumers or migration repositories exist yet. Read `AGENTS.md` and `harness/rules` for policies.

Use feature branches. The initial request approved `main` as the base and `feature/astavet-mvp` as the working branch, with all work uncommitted. The new repository has no commits; harness ledger/graph initialization requires a committed base and is currently unavailable. Do not create a commit to work around that restriction.

Keep the retained no-comment policy and Vietnamese planning. Planning estimates, when requested, use 1d = 8h and 1 SP = 4h. Role labels are Frontend/Backend/BA/QAQC. Sub-tasks and tracker write-back require authorized scope and an available integration. Never fabricate tracker IDs, external approvals or graph evidence.
