# Source register

Checked: **2026-09-30**. Every successful entry below was opened and its relevant content consulted, not inferred from a search snippet. URLs with `current`, an unversioned reference root or a repository branch are moving targets. Version scope records what the page displayed when checked; it is not a requirement to use or upgrade to that version. At invocation, use primary documentation matching the actual project. Unversioned educational pages are not language specifications.

The rules use short original summaries and user-selected operating conventions. No upstream code, skill body or substantial documentation passage was copied or adapted. Attribution is provided here. Example repositories supply context only, not normative rules; no dependencies or example packages were installed.

## Java core

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://dev.java/learn/ | Getting to Know the Language; Mastering the API; References for the latest release | 2026-09-30 | Java learning portal; unversioned educational guidance |
| https://dev.java/learn/language/oop/ | Object Oriented Programming; Classes and Objects; Using Records to Model Immutable Data | 2026-09-30 | Educational topic index, not a specification; records require a supporting target JDK |
| https://dev.java/learn/language/annotations-exceptions/ | Annotations and Exceptions | 2026-09-30 | Educational topic index |
| https://dev.java/learn/language/annotations-exceptions/exceptions/ | Exceptions; Catching and Handling Exceptions; Throwing Exceptions | 2026-09-30 | Educational topic index |
| https://dev.java/learn/language/annotations-exceptions/exceptions/catching-handling/ | The try-with-resources Statement; Opening Several Resources; Suppressed Exceptions | 2026-09-30 | Java tutorial; resource syntax compatibility must be checked against target JDK |
| https://docs.oracle.com/en/java/ | Java Platform, Standard Edition (Java SE); Java SE Documentation | 2026-09-30 | Oracle documentation portal; select target release |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Object.html | hashCode; equals | 2026-09-30 | Java SE 21 API contracts; consulted baseline, not a minimum project JDK |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html | Map contract, mutable keys | 2026-09-30 | Java SE 21 API |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html | Class BigDecimal; divide; rounding modes; equals/compareTo | 2026-09-30 | Java SE 21 API |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/package-summary.html | Package java.time; dates/times; class descriptions | 2026-09-30 | Java SE 21 API; java.time introduced in Java 8 |

Null conventions, business units/rounding, API ownership and error taxonomy remain repository decisions. The portal is a navigation source, not evidence for an arbitrary chosen JDK.

## Java style

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://google.github.io/styleguide/javaguide.html | 3 Source file structure / 3.3 Imports; 4 Formatting; 5 Naming; 7 Javadoc | 2026-09-30 | Google's unversioned Java style guide; applies when adopted, not a Java mandate |
| https://checkstyle.org/ | Automate Java coding standards with Checkstyle; Style Configurations | 2026-09-30 | Checkstyle project documentation; page title 14.3.0; configurable checks |
| https://spotbugs.github.io/ | Find bugs in Java Programs; Bug Descriptions; Using SpotBugs | 2026-09-30 | SpotBugs project documentation; landing page not release-pinned; static analysis |

No Google formatter, indentation/column limit or analysis tool is mandated by this collection.

## Java architecture

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://docs.oracle.com/javase/specs/jls/se21/html/jls-7.html | 7 Packages and Modules; 7.4 Package Declarations; 7.7 Module Declarations | 2026-09-30 | Java SE 21 language specification |
| https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html | Structuring Your Code; Using the “default” Package; Locating the Main Application Class | 2026-09-30 | Spring Boot 4.1.1 displayed; upstream recommendations, no required layout |
| https://www.archunit.org/userguide/html/000_Index.html | 5.3 Library; 8.1 Architectures / 8.1.1 Layered Architecture; 8.2 Slices | 2026-09-30 | ArchUnit project user guide; unversioned URL; match installed library for syntax |
| https://github.com/spring-projects/spring-petclinic | README / Run Petclinic locally; sample application and build alternatives | 2026-09-30 | Spring project sample, moving default branch; example rather than specification |

The controller/service/repository package split and dependency direction are explicitly the user's preference. They are not derived as mandates from Spring Boot, Petclinic or ArchUnit.

## Spring components and API

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html | Dependency Injection; Constructor-based Dependency Injection; Constructor-based or setter-based DI? | 2026-09-30 | Spring Framework 7.0.9 displayed; recommendation for required dependencies |
| https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html | Bean Scopes; The Singleton Scope; The Prototype Scope; Singleton Beans with Prototype-bean Dependencies | 2026-09-30 | Spring Framework 7.0.9 displayed; scope/lifetime contracts |
| https://docs.spring.io/spring-framework/reference/web/webmvc.html | Spring Web MVC | 2026-09-30 | Spring Framework 7.0.9 displayed; Servlet MVC, separate from WebFlux |
| https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html | Validation; method versus argument validation; @Valid semantics | 2026-09-30 | Spring Framework 7.0.9 displayed; behavior differs across versions |
| https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html | Exceptions; @ExceptionHandler; exception matching | 2026-09-30 | Spring Framework 7.0.9 displayed; MVC only |

DTO choice and response shape are user/repository decisions. This collection does not claim that MVC mechanisms implement reactive WebFlux behavior; read reactive docs at invocation if needed.

## Persistence and transactions

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html | Using @Transactional; proxy/self-invocation note; @Transactional Settings; rollback defaults and configurable settings | 2026-09-30 | Spring Framework 7.0.9 displayed; default proxy behavior, subject to configuration |
| https://docs.spring.io/spring-data/jpa/reference/ | Spring Data JPA; JPA and JPA Repositories; version navigation | 2026-09-30 | Spring Data JPA 4.1.1 stable reference displayed |
| https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html | Transactionality; Transactional query methods; readOnly note; unit-of-work boundary recommendation | 2026-09-30 | Spring Data JPA 4.1.1 displayed; provider/driver effects are conditional |
| https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html | JPA Query Methods; Configuring Fetch- and LoadGraphs; ad hoc entity graphs | 2026-09-30 | Spring Data JPA 4.1.1 displayed; installed provider/query behavior must be verified |
| https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html | Locking; lock metadata on query and redeclared CRUD methods | 2026-09-30 | Spring Data JPA 4.1.1 displayed; not a universal locking recommendation |

Schema migrations, selected constraints, isolation/locking, query-count targets and idempotency contracts must be discovered. No particular database, ORM, migration tool or lock strategy is mandated.

## Java security

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://www.oracle.com/java/technologies/javase/seccodeguide.html | 1 Denial of Service; 2 Confidential Information (2-2 logging); 3 Injection and Inclusion; 5 Input Validation (5-1); 6 Mutability; 8 Serialization and Deserialization; 9 Access Control | 2026-09-30 | Oracle Java SE security recommendations; unversioned document, verify version-specific mechanisms |
| https://docs.spring.io/spring-security/reference/ | Spring Security; authentication; authorization; protection against common attacks; Servlet/reactive entry points | 2026-09-30 | Spring Security 7.1.1 displayed |
| https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html | Cross Site Request Forgery (CSRF); Using Spring Security CSRF Protection; Persisting the CsrfToken | 2026-09-30 | Spring Security Servlet reference; defaults can be overridden by application configuration |

Keeping CSRF, TLS verification and authorization intact during routine fixes is a user-selected safety convention. Threat models, exemptions and effective authorization policy remain project decisions. Old security mechanisms mentioned in general Java guidance must not be assumed available in the target runtime.

## Java testing

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://docs.spring.io/spring-boot/reference/testing/index.html | Testing; test modules and starter support | 2026-09-30 | Spring Boot 4.1.1 displayed; do not infer a project's test versions from this page |
| https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html | Testing Spring Boot Applications; Auto-configured Tests; focused slices | 2026-09-30 | Spring Boot 4.1.1 displayed; annotations/packages are version-sensitive |
| https://docs.junit.org/ | Overview; What is JUnit? | 2026-09-30 | Redirected to https://docs.junit.org/6.1.3/overview.html; JUnit 6.1.3 observed |
| https://docs.junit.org/6.1.3/writing-tests/test-instance-lifecycle.html | Test Instance Lifecycle; per-method default | 2026-09-30 | JUnit Jupiter 6.1.3 documentation |
| https://java.testcontainers.org/ | Testcontainers for Java; data access layer integration tests | 2026-09-30 | Testcontainers project documentation; unversioned site |
| https://java.testcontainers.org/test_framework_integration/junit_5/ | Jupiter / JUnit 5; shared and singleton containers | 2026-09-30 | Testcontainers Jupiter integration page; check actual engine/library compatibility |
| https://github.com/spring-ai-community/spring-testing-skills | README / Skills Included; License | 2026-09-30 | Community example, not official Spring specifications; README targets Boot 4.x / Framework 7.x, with migration notes |

The community example advertises Apache License 2.0. No example skill text/code was adapted, and its dependencies were not added. Destructive-fixture safeguards, behavior-driven selection and honest check-status reporting are user-selected conventions, not claims about a single test framework.

## Java build and dependencies

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://maven.apache.org/guides/ | Maven Build Config Fundamentals; Getting Started with Maven; Guides / Using Toolchains | 2026-09-30 | Apache Maven documentation index; not a pinned runtime |
| https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html | Build Lifecycle Basics; default/clean/site lifecycles | 2026-09-30 | Apache Maven guide; verify actual lifecycle/plugin bindings |
| https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html | Transitive Dependencies; Dependency Management | 2026-09-30 | Apache Maven guide; effective management and plugin dependencies differ |
| https://docs.gradle.org/current/userguide/userguide.html | Gradle User Manual; Gradle Fundamentals; JVM builds | 2026-09-30 | Gradle 9.8.0 displayed; moving current URL |
| https://docs.gradle.org/current/userguide/gradle_wrapper.html | Gradle Wrapper; Using the Gradle Wrapper; wrapper integrity | 2026-09-30 | Gradle 9.8.0 manual; use project wrapper version |
| https://docs.gradle.org/current/userguide/toolchains.html | Toolchains for JVM projects; Java toolchains; The --release flag; Source and Target compatibility | 2026-09-30 | Gradle 9.8.0 manual; build-runtime versus project compiler compatibility |

See [Java style](#java-style) for Checkstyle/SpotBugs and [Java architecture](#java-architecture) for ArchUnit. Running only existing tools, preserving dependency controls and avoiding automatic upgrades/installations are user-selected conventions.

## Concurrency and performance

| Exact source URL | Relevant consulted sections | Date checked | Version scope / authority |
| --- | --- | --- | --- |
| https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html | 17 Threads and Locks; 17.4 Memory Model; 17.4.5 Happens-before Order | 2026-09-30 | Java SE 21 language specification |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html | Executors; Concurrent Collections; Memory Consistency Properties | 2026-09-30 | Java SE 21 API |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html | shutdown; shutdownNow; awaitTermination; close | 2026-09-30 | Java SE 21 API; close/AutoCloseable support must be checked for older targets |
| https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html | Virtual threads; interrupt; interrupted; virtual-thread APIs | 2026-09-30 | Java SE 21 API; virtual-thread API since 21 |
| https://dev.java/learn/api/virtual-threads/ | Why Virtual Threads?; Creating Virtual Threads | 2026-09-30 | Dev.java educational article by Cay Horstmann, UPL noted on page; no text/code adapted |
| https://dev.java/learn/jvm/monitoring-troubleshooting/ | JFR, Monitoring and Troubleshooting; monitoring/troubleshooting/JFR topic links | 2026-09-30 | Educational index; choose runtime-compatible tools |
| https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html | Why Use Virtual Threads?; Scheduling Virtual Threads and Pinned Virtual Threads; Virtual Threads: An Adoption Guide; Represent Every Concurrent Task as a Virtual Thread; Never Pool Virtual Threads | 2026-09-30 | Oracle JDK 25 guide; newer pinning discussion is not interchangeable with JDK 21 |

Measurement targets, executor ownership and cancellation contracts require repository/workload evidence. No performance improvement is promised by these references.

## Codex format, instructions and discovery

| Requested exact URL | Retrieved URL | Consulted sections | Date checked / scope |
| --- | --- | --- | --- |
| https://developers.openai.com/codex/skills | https://learn.chatgpt.com/docs/build-skills | Create a skill; Where Codex loads local skills; Enable or disable local Codex skills | 2026-09-30; current official documentation, supports repository .agents/skills, user ~/.agents/skills and symlinked skill folders |
| https://developers.openai.com/codex/guides/agents-md | https://learn.chatgpt.com/docs/agent-configuration/agents-md | How Codex discovers guidance; Create global guidance; Layer project instructions | 2026-09-30; current official documentation, CODEX_HOME and global override precedence |

Local authoring instructions consulted: the installed `skill-creator/SKILL.md` and `openai-docs/SKILL.md` in `~/.codex/skills/.system/`. They supplement the official pages; the collection contains no username-specific source paths. The source register records the original personal setup research; the collection has since moved into a standalone collection repository within the workspace. Runtime discovery of the reorganized collection was not tested. `/skills` verification is a user follow-up after restarting, not a setup result.

## Retrieval limitations

All **22 user-requested source URLs** were retrieved (the two Codex pages and JUnit root redirected as recorded above). Additional attempted URLs that could not supply usable page content:

- https://dev.java/learn/exceptions/ — empty retrieval. Used the actual linked `/learn/language/annotations-exceptions/exceptions/` route instead.
- https://dev.java/learn/objects/ — retrieval error. Used the actual linked `/learn/language/oop/` route instead.
- https://dev.java/learn/virtual-threads/ — retrieval error. Used the actual linked `/learn/api/virtual-threads/` route instead.
- https://openjdk.org/jeps/444 — page retrieval failed, including a 403 retry; a search result was available but was not used as a successfully read citation. Used the Java SE 21 API and Oracle JDK 25 guide for the collection's virtual-thread statements.
- https://openjdk.org/jeps/491 — page retrieval failed. No release-specific claim is attributed to this unread page; consult matching runtime documentation if pinning matters.

These are retrieval observations, not claims that the sources no longer exist. No inaccessible page is treated as evidence.
