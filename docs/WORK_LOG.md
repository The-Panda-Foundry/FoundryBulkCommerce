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
