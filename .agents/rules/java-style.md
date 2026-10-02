# Java style

## Language/API requirements

Identifiers, imports and Javadoc constructs must be valid for the target JDK. A style guide does not redefine Java syntax or impose naming as a language requirement.

## Upstream recommendations

Google Java Style documents naming, imports, formatting and Javadoc for code following Google Style. Its exact import ordering, indentation and line limits apply only when adopted by the project. Checkstyle checks configurable source conventions; SpotBugs reports potential bytecode bug patterns, not formatting compliance or proof of correctness.

## User-selected conventions

Respect the existing formatter and repository conventions. Where no convention exists, prefer descriptive PascalCase type names, camelCase members and lowercase packages; agree on formatting if a new project needs a full style decision. Remove unused imports and keep imports understandable. Write useful Javadoc for exposed contracts, exceptional cases, nullability, ownership and thread safety; avoid comments that merely repeat a method name. Do not impose Google formatting or reformat unrelated files. Run configured checks only on appropriate scope; disclose pre-existing violations separately.

## Conditional framework rules

Preserve framework-required names, generated sources and code-generation boundaries. Framework annotations are not a reason to replace a repository formatter.

## Discover in the repository

Formatter and configuration, `.editorconfig`, existing naming/import patterns, generated directories, Checkstyle/SpotBugs configuration and suppressions, documentation linting, project adoption (or non-adoption) of Google Style.

Sources: [Style source records](SOURCES.md#java-style).
