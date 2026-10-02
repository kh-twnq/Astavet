# Java builds and dependencies

## Language/API requirements

Distinguish the JDK running the build tool, compiler/test toolchains, bytecode target and runtime. Compiler source/target settings alone may not restrict newer API usage; inspect `--release` or equivalent compatibility controls. Build/plugin/runtime compatibility depends on their actual versions.

## Upstream recommendations

Gradle recommends its wrapper for a controlled distribution. Maven documents lifecycle phases and dependency management; effective dependency/plugin versions can differ from direct declarations. Gradle provides toolchains and compatibility controls with distinct purposes.

## User-selected conventions

Discover the actual build system, wrappers, modules and toolchains. Use commands verified from repository documentation, CI and configured tasks/goals; record working directory and selectors. Do not assume `test` includes integration tests or that a Java project uses Maven/Gradle. Inspect relevant dependency resolution when conflicts are evidenced. Respect BOMs, parent POMs, version catalogs, locks, verification metadata and existing version controls. Do not upgrade dependencies, regenerate wrappers, install analysis tools or change repository configuration automatically.

Diagnose the first causal failure, separating toolchain, compilation, dependency resolution, test discovery and environmental problems. Avoid destructive cache cleaning as a default. Redact credentials from settings, repository URLs, environment and debug output. Do not upload build scans without authorization.

## Conditional framework rules

For Maven, inspect effective profiles, parent management and Surefire/Failsafe bindings where relevant. For Gradle, inspect settings, source sets, task wiring and wrapper configuration before selecting commands. If ArchUnit, Checkstyle or SpotBugs is configured, run the applicable existing task; these tools provide scoped evidence rather than universal architecture/security guarantees.

## Discover in the repository

Build files/wrapper distributions, module graph, JDK release/toolchains, resolved framework/test/plugin versions, CI, dependency management and repositories, analysis configuration, offline/network constraints and output reports.

Sources: [Build source records](SOURCES.md#java-build-and-dependencies).
