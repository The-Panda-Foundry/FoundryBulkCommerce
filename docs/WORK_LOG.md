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

## v0.1.1 integrated acceptance correction — 25 November 2026

- Read the supplied Prism acceptance log. Confirmed addon/SQLite discovery and actual integrated-world startup, with no addon initialization/ledger messages in the whole-log search.
- Established root cause before editing production code: physically dedicated @Mod plus physically server-only Mixin config. Inspected NeoForge side semantics, MixinConfig side selection and IntegratedServer lifecycle/login delegation.
- Inspected Treasury's actual client screen and action dispatcher: Sell All sends the existing request without a loose-inventory guard; no client interface change required. Commands retain permission level two and their status subcommand.
- Changed only activation/dependency-side validation, startup diagnostics, optional phase logging and stale integrated-world startup callback protection; bumped version to 0.1.1. All economic/recovery mechanisms remain intact.
- Added four structural regressions (entry point, common Mixins, all hooks, client request path). Standard Gradle build and dedicated transformed GameTest pass; all 2,403 executable regression checks pass.
- Added a test-only physical CLIENT quick-play harness using a disposable copy of the prior test world. Attempted actual client launch; macOS graphical-session services failed before mod loading. Interrupted the blocked attempt. No integrated runtime pass is claimed.
- Tried an offline 21.1.256 build: required NeoForge artifacts are not cached, so that version could not be tested here. Final build/runtime use 21.1.265; a Gradle version override is supplied for exact-version retesting with available dependencies.
- Original references unchanged, no source changes in the external project, no production deployment. Full Prism integrated acceptance and dedicated client-without-addon handshake remain explicit acceptance gates.

## v0.1.2 BC-001 and acceptance continuation — 25 November 2026

- Preserved v0.1.1 f3a5560 as baseline/v0.1.1 before production edits. Created a separate focused development branch.
- Read the acceptance log and inspected the exact supplied registry/handler implementations. Baseline actual-runtime reproduction accepted all six full tiers, including Netherite 132 slots. The original live rejection is not reproducible as a tier-specific defect; old debug-off logs cannot identify the exact guard. This limitation is explicit.
- Inspected local acceptance SavedData read-only: a canonical 132-slot/8,448-wheat record exists, but cannot prove the historical outer inventory/alias/request state. No user world or dependency changes.
- Implemented API capacity validation, passive modifier-class discovery/validation, single-pass decoding with reusable matching plan, and precise rejection categories. Preserved all transaction/identity/access/ledger safeguards and default workload values.
- Runtime discovery found six shulkers, seven stack upgrades and three passive downgrades. Matrix covers each modifier/tier pair and representative mixed/repeated combinations, full/empty/mixed contents, upgrades/metadata, aliases, exact rollback and an API-promoted 300-slot capacity.
- Added real Treasury/SQLite Netherite tests for 1/2/3/4 boxes (3/7/11/14 Spurs), configured-limit remainders, repeated requests, concurrent admission refusal, all-tier+vanilla+loose mixed transaction, insufficient stock and payout/account overflow. BC-002 calculation remains unchanged.
- Added local warm-JVM capture/plan/evidence and thread-allocation benchmark against the exact v0.1.1 addon. No timing-dependent pass threshold; report sample sizes/environment and outstanding full-pack host profiling.
- First expanded matrix exposed three legitimate passive downgrade items omitted by the prior ID list; the API-class rule includes them. Corrected test fixtures to respect their smaller per-slot capacities. Test timeouts increased to allow native SQLite startup under fast headless GameTest ticks; no production waits/tick processing added.
- Updated complete compatibility/configuration matrix, diagnostics, performance report and dedicated staging checklist. Exact live original rejection, .2 full-pack integrated behaviour, Linux native loading/absent-addon handshake and real crash/host load acceptance remain pending. No production deployment.


## 25 November 2026 — v0.2.0 hotbar and optional Backpacks

Preserved accepted v0.1.2 commit 25505d80e5d3621eb96d02bf3448d8fbd908f35a with baseline/v0.1.2 before edits. Inspected exact Backpacks 3.26.9.2195, Core, Storage, Curios and Accessories API bytecode/decompiled implementation read-only. Established independent canonical UUID repositories, native Backpacks root/merge setter, wrapper invalidation and official equipment integrations. Original JARs/world data were not modified.

Implemented hotbar-only extraction, optional canonical Backpacks source, shared narrow finite parser and separate bounded identity-only equipment helpers. Retained original economic calculation, shop checks, transaction limits, exact matching, verified extraction/payment/compensation, SQLite schema, persistent containment and common Mixins. No speculative frameworks, remote persistence, ticks or client API added. Existing separate development fixture utility was not changed; runtime stress fixtures are test-only and not shipped in production.

Added full registry-tier/passive modifier matrix, actual equipped/cosmetic aliases, separate repository identities, protected/nested contents, partial huge-stack extraction, repeated actual Treasury/SQLite settlement and pending-plan movement/alias cancellations. Clean normal Java21 Gradle build and all nine present-integration GameTests passed; all optional integrations absent also passed the four real shulker tests with five explicitly skipped-by-guard optional cases. Test discovery needed isolated installed helpers because synthetic lambda signatures referenced optional APIs; production guarded adapters were unaffected. 2,406 executable check assertions pass. Exact 21.1.256 remains uncached and its offline artifact request failed; runtime evidence is 21.1.265.

Measured native Netherite Omega backpack capture/partial planning on this Mac (40 warmed samples each), with bounded inspections and data sizes; details/limits in PERFORMANCE.md. No claim of full-pack/Linux/concurrent network MSPT or crash atomicity. Updated configuration, compatibility, architecture, recovery boundaries and separate integrated/dedicated acceptance checklists. Future hybrid proposal retained. No production deployment.

Final build-input check explicitly limits development runtime fileTree inputs to the original four archives; optional integration flags now remain effective even when all compile references share dependencies/. Absent-integration build/runtime was rerun successfully after that Gradle change. Archive validation confirms Java21, 45 addon classes, no test/foreign mod classes and unchanged nested SQLite.
