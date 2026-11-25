# Work log

## 25 November 2026

- Read all four user-supplied local JARs; ZIP CRC verification passed for every entry.
- Inspected compiled implementations with local Vineflower and javap; reference source remains temporary and is not included in deliverables.
- Java 21.0.12, Gradle 9.2.1, ModDevGradle 2.0.148 and NeoForge 21.1.265 are cached.
- Requested project directory is readable but not writable under this task's sandbox. Development is isolated in the task workspace; original dependencies remain untouched.
- No production access, deployment, original dependency modification or redistribution.
- Treasury validates module, distance, block identity, configured shop and BUY_FROM_PLAYER before Sell All's first countMatching invocation.
- Treasury 1.0.2 ignores removeMatching's boolean and caps quantity at 2304; addon replaces only the validated Sell All execution branch.
- Sophisticated storage uses UUID-indexed SavedData and oversized stack counts. Stack extraction is capped at one normal stack per operation.
- Numismatics accounts hold standard and overflow balances separately; both must be observed for payment verification and compensation.

- Initialised a nested Git repository with dependency JARs, build outputs, runtime data and local settings ignored.
- Created Java 21 / NeoForge 21.1.265 Gradle configuration, pinned wrapper/plugin, server metadata and required server-only Mixin configuration.
- Implemented vanilla support first, then bounded canonical Sophisticated support and verified standard/overflow bank settlement.
- Changed the initial extraction concept to canonical copy/edit/compare for supported Sophisticated layouts after confirming extraction hooks and count caps. Only stack upgrades/default finite partitions are admitted; no runtime extraction hook is invoked.
- Gradle failed before project configuration because the sandbox denied its local file-lock socket. A writable temporary cache copy was attempted; the originals were not modified. The final build helper reads exact original cached inputs directly and uses javac/JAR packaging without sockets.
- Used the cached Minecraft manifest and NeoForge userdev libraries for a coherent offline test classpath; Minecraft resources are read only for isolated bootstrap tests.
- Added four executable suites: transaction fault injection, original-JAR structural compatibility, real vanilla item/component adapter tests and canonical Sophisticated schema/UUID tests.
- Completed README, architecture/Mixin/dependency/configuration notes, explicit durability boundaries, changelog and staging acceptance checklist. Full modpack runtime/Gradle acceptance remains pending; nothing deployed.
- Final local-cache build passed 2,327 transaction checks, 16 original-JAR structural checks, 11 vanilla adapter checks and 13 canonical-schema/UUID checks. Compiled output is a staging candidate; live Mixin/modpack execution remains unverified.

## Approved local-ledger MVP — 25 November 2026

- Implementing the explicitly approved SQLite-only MVP. MongoDB, replication and distributed coordination remain deferred.
- Standard Gradle now executes past the previous sandbox socket blocker. It revealed an undeclared localRuntime configuration; replaced that use with runtimeOnly.
- Development remains in the existing task-workspace Git repository; external project/reference directories are read-only inputs.
- Planned containment: durable pre-mutation intent, durable outcomes, a persistent breaker, startup recovery checks, read-only audit commands and bounded request work. No automatic recovery/refunds.

- Implemented SQLite-only schema, one bounded native-JDBC writer, pre-mutation durable intent and observed outcomes. All SQL/fingerprinting is off the Minecraft thread.
- Added global admission cooldown, slot/NBT/intent bounds, asynchronous state revalidation and UUID-alias revalidation, with verified synchronous economic settlement.
- Added persistent breaker, dirty-boot recovery, normal-loop/save completion markers and global login/disconnection containment; manual reconciliation remains offline and explicit.
- Added permission-level-two read-only audit commands. No remote workers, MongoDB dependencies, refunds or automatic crash reconciliation.
- Removed overbroad EntityBlock item exclusion; added ordinary-item, vanilla serialization and budget regressions. Startup hashes installed reference builds, including after config reload.
- Fixed the Gradle runtimeOnly configuration and development library classpath; removed superseded hand-packaging fallback. SQLite downloaded from official Maven and SHA-256 verified, provided through a temporary local repository because JVM network resolution was restricted.
- Standard Gradle clean build passed. Final build reran all five suites: 2,399 regression checks passed. Actual NeoForge GameTest passed with real Mixins, bank overflow, upgraded storage, alias invalidation, compensation and serialization.
- Extended GameTest through Treasury's actual Sell All handler: mixed loose/vanilla/upgraded contents paid 12 Spurs for 256 items; a stale second plan cancelled without mutations. Durable rows and clean-stop metadata verified after exit.
- Retained the original full H1 hybrid proposal as historical/deferred documentation. Updated approved architecture, schema/authority, dependency notices, config/commands, recovery procedure and staging acceptance checklist.
- Original four dependency JARs are unchanged and excluded from source/Git/distribution. No production connection or deployment. Full-client, complete-modpack, host-native loading, real crash/fault and performance acceptance remain explicitly pending.
