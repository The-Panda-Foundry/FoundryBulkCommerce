# Verification and review disposition

## Current v0.2.0 release candidate — 25 November 2026

- Accepted v0.1.2 baseline preserved before edits: `25505d80e5d3621eb96d02bf3448d8fbd908f35a`, tag `baseline/v0.1.2`. Original dependency archives remain unmodified and excluded from source/distribution.
- Standard Java 21 / ModDevGradle clean build passes: **2,406 executable checks** (2,327 transaction + 20 compatibility + 27 native SQLite + 19 vanilla + 13 schema). The Gradle JUnit discovery task intentionally has no JUnit tests.
- **Nine required actual NeoForge GameTests pass** with Backpacks, Curios and Accessories loaded: the existing four adapter/shulker matrix/performance/real transaction tests, plus backpack matrix, Curios aliases, Accessories aliases, mixed hotbar async transactions and backpack performance. Storage matrix: 443 explicit assertions; Backpacks: 224, all six tiers × nine modifiers plus combinations/preservation/rejections.
- Mixed actual Treasury/SQLite transactions remove 230,400 items twice from stock above signed-int aggregate range; two subsequent pending plans cancel for hotbar movement and a newly introduced protected alias. Protected main/offhand/nested contents and installed metadata remain unchanged; durable rows/references and exact bank deltas are checked.
- Runtime without Backpacks, Curios or Accessories also passes. Four base tests exercise actual shulker functionality; five optional tests return early rather than claiming backpack/equipment coverage. GameTest discovery originally exposed optional synthetic-lambda signatures; test-only installed helpers now isolate them, verified in this absent run. Production optional adapters already use separate guarded classes.
- Both equipment APIs loaded: real active/cosmetic aliases block eligible backpack sources; equipped-only collection decodes zero content bytes. Equal UUID values across independent Storage/Backpacks repositories are accepted, within-repository aliases remain excluded. Unknown providers and third-party mirrored equipment bridges are unverified.
- Runtime toolchain: Temurin 21.0.12/macOS ARM64, Minecraft 1.21.1, NeoForge **21.1.265**, Gradle 9.2.1, ModDevGradle 2.0.148. An exact 21.1.256 offline artifact attempt failed because that version is absent from the cache; this is a documented environmental blocker, not a successful exact-loader test. Existing Gradle cache and the temporary local SQLite Maven repository were reused; no fallback compiler/packager.
- No OS kill/power-loss/disk-full/full-modpack/client handshake test is claimed. Full integrated v0.2.0, dedicated Linux 21.1.256, native SQLite host behaviour, real-player contention/MSPT and crash/manual-reconciliation gates remain outstanding. v0.1.2 integrated stress acceptance is user-reported; it does not certify new paths.
- The four common Mixins, bank/ledger schema and safety mechanisms are unchanged. No new networking, client class requirement, tick processing or persistent inventory cache. See V0_2_0_COMPATIBILITY.md and PERFORMANCE.md. Source commits and final artifact SHA-256 are in the release receipt/Git history. Production source: `050d6c7`; regression source: `3603ff7`. Production JAR SHA-256: `81847f9e3a2eb20b44c0c0951bdec4c83473c9520e9327b1116972a08728dbd0`; Java class major 65. Archive inspection verifies 45 addon classes, no foreign mod/test classes, and unchanged exact nested SQLite.

### New exact reference fingerprints

| Archive | SHA-256 |
| --- | --- |
| sophisticatedbackpacks-1.21.1-3.26.9.2195.jar | de4a51eb3757ee273b56a41620d6598cd3af8578a5a02c4b5a784e42ab4bee2b |
| curios-neoforge-9.5.1+1.21.1.jar | a45df2125c26219974aba7507ffc9afe7b83acc941a386af3faacb1cc0056fde |
| accessories-neoforge-1.1.0-beta.53+1.21.1.jar | 10017a3da78ea63e9ece27a1ca32f8cf490362f348778cf8cb759e7282f3beb0 |

All three are required compile references and individually optional at runtime; installed copies are hashed off-thread at startup regardless of adapter enable flags. Their normal dependent mods still apply (Accessories runtime tests used Owo 0.12.15.5-beta.1+1.21). README documents property overrides and present/absent commands; the original four hashes below remain unchanged. Runtime inputs explicitly select the original four; optional integrations use the documented flags even when compile archives share one directory.

The remaining sections preserve historical evidence; their versions, artifact hashes and outstanding-at-delivery statements are not current v0.2.0 results.


Date: **25 November 2026**. Based on reviewed source commit `ca0b30f`; final MVP commit is recorded in the delivery receipt/Git history. Nothing deployed to production; original four JARs remain unmodified and excluded from distribution.

## Current v0.1.2 release candidate — 25 November 2026

- Working baseline f3a5560 preserved by tag baseline/v0.1.1 before edits. Unmodified-baseline real runtime accepted all six shulker tiers including full Netherite. The original debug-off live rejection's exact cause is **not established**; see SHULKER_COMPATIBILITY.md.
- Standard Java 21 / ModDevGradle build passes with **2,403 executable checks**: transaction 2,327; compatibility 20; native SQLite 27; vanilla 16; canonical schema 13. Production quantity calculation and Treasury's maximum configured bundle are unchanged.
- **Four required actual NeoForge GameTests pass:** installed adapters/Mixins + async stale cancellation; complete registry matrix (431 explicit matrix assertions); preparation benchmark/scan-byte refusal; Netherite/mixed/repeated/concurrent/overflow/ledger transactions. Tests use actual mods, wrappers, bank, Treasury handler and SQLite, not only schema mocks. See complete tables in SHULKER_COMPATIBILITY.md and PERFORMANCE.md.
- All requested Netherite outcomes match: 1/2/3/4 boxes pay 3/7/11/14 Spurs with 1,536/768/0/1,536 wheat left. Each is one asynchronous Sell All and one durable ledger outcome. Six mixed Sophisticated tiers plus vanilla/loose stock settle in another single request. Counter/history checks verify unique references and no records/payments from rejected preparations.
- New production diagnostics use bounded categories/source details behind debugTransactions. Scan/snapshot defaults remain 4,096/1 MiB, including an API-validated per-container scan-capacity ceiling. A dependency-promoted 300-slot saved capacity is exercised; excessive data/work still fails closed.
- Local timing/allocation comparison to the exact v0.1.1 JAR shows reduced work/allocations for the tested scenarios. It is not an MSPT/load/production result. Test timeouts were increased for fast headless ticking/native startup; no database wait or periodic scan was added to production.
- Tested runtime remains NeoForge 21.1.265 on this Mac. The architect independently confirmed v0.1.1 integrated gameplay on 21.1.256. v0.1.2 full-pack integrated regression, exact 21.1.256 dedicated Linux staging, absent-addon real-client handshake, disk/kill boundaries and real host load remain acceptance gates. All original crash-atomicity limits remain.
- Existing normal Gradle cache and temporary local SQLite Maven mirror were reused as described below. Original reference hashes still pass; no original dependency JAR was modified or distributed. No production deployment.

Current artifact: `foundrybulkcommerce-0.1.2.jar`, SHA-256 `92ac4f47740a2e722d3dea0ddd152003d2fa90aef67ce72f435fee884201cbea`; Java class major 65. Archive inspection confirms the four common Mixins, exact unmodified nested SQLite and no original mod/dependency classes, GameTest or acceptance-launcher classes.

Functional source commit: `d3d68b615e61a1de688498334647377bc75a18cc`. The delivery receipt records the final documentation commit. After the last runtime pass, only configuration comments and an isolated schema-test fixture correction changed; the standard build and that schema suite were rerun successfully.

The remaining versioned sections preserve earlier evidence and artifact hashes as history.

## Historical v0.1.1 correction — 25 November 2026

Root cause and implementation: INTEGRATED_SERVER_FIX.md. Standard Gradle build passed, with **2,403 executable checks** (2,327 transaction + 20 structural/physical-side/request-path + 27 native SQLite + 16 vanilla + 13 Sophisticated). Actual NeoForge **physical DEDICATED_SERVER GameTest passed** with common Mixins and mixed async selling/stale cancellation. Diagnostics and opt-in phase logs were observed. Java 21, NeoForge 21.1.265; SQLite supplied via the same temporary local Maven override described below. Production JAR inspection confirms Java 21 class version, v0.1.1 metadata, common four-Mixin list, nested SQLite, and no client-test/GameTest classes.

**Not established:** actual physical CLIENT/integrated runtime. Its graphical launch was attempted but macOS service access failed before addon loading; the attempt was interrupted. A 21.1.256 offline build was also attempted but its NeoForge artifacts are uncached. Neither attempt is reported as a successful runtime/build. Full-pack Prism 21.1.256, real-client Sell All, world switching and absent-addon multiplayer handshake require acceptance. All prior crash atomicity and host performance limits remain.

Current artifact `foundrybulkcommerce-0.1.1.jar` SHA-256: `372c9e8e623b521b9041553c81208cecea3b73cc6b8af131600576019454e1ea`. SQLite schema remains 1 and all safety mechanisms are retained. The remaining sections preserve the original v0.1.0 baseline evidence; their artifact hash/counts are historical.

## Historical v0.1.0 baseline results

| Check | Result |
| --- | --- |
| Standard NeoForge Gradle build | PASS; real compileJava, jarJar, jar, sourcesJar, check/build tasks |
| Transaction fault/bounds suite | 2,327 checks passed |
| Original-JAR structural suite | 16 checks passed |
| Native SQLite ledger suite | 27 checks passed |
| Vanilla item/component suite | 16 checks passed |
| Sophisticated canonical schema suite | 13 checks passed |
| Total executable regression checks | **2,399 passed** |
| Actual NeoForge GameTest | **1 required test passed**, including complete asynchronous Treasury Sell All and stale cancellation |
| Production archive inspection | Java class major 65; only addon classes/resources plus unmodified nested SQLite; no game-test classes or original mod JARs |

Toolchain: Temurin Java 21.0.12, Gradle 9.2.1, ModDevGradle 2.0.148, Minecraft 1.21.1, NeoForge 21.1.265. The earlier hand-packaged fallback is superseded and removed. `runtimeOnly` fixes the undeclared localRuntime configuration. Gradle's test task is deliberately configured for no JUnit discoveries because the actual five suites are JavaExec check dependencies. Runtime testing also caught/fixed the missing development `additionalRuntimeClasspath` for SQLite.

Native ledger tests use actual SQLite JDBC/native code and temporary databases. They verify off-caller IO, durable intent/evidence/hash, quoted SQL values, bounded/player-filtered history, normal reopen, unclean-success recovery, preservation of older clean-boot results, pending intents, cancelled plans, persistent breaker, duplicate UUID refusal, exclusive ownership, instance mismatch and failed compatibility startup. An injected SQL trigger fails the outcome update and proves PREPARED survives with blocking state. These are simulated close/failure boundaries, not OS kill/power-loss or actual disk-full tests.

The actual GameTest loads Create 6.0.10 and the four exact reference mods through NeoForge's transformer. It verifies applied Treasury/bank/admission/shutdown Mixins, audit command permission level 2, real bank standard/reserve overflow and deduction, registered stack upgrade items, UUID-backed live ItemContentsStorage, duplicate carried aliases and aliases introduced after preparation, canonical edit/rollback plus existing-wrapper invalidation, and SavedData save/load serialization.

It then invokes Treasury's real private tradeShop method with Sell All=true using a test-only registered FakePlayer. The real Mixin and BulkSales async continuation remove 64 loose + 64 vanilla-box + 128 upgraded-storage items, then pay exactly **12 Spurs** at 3 per 64. After durable acknowledgement, a second 64-item request changes its source before acknowledgement; it cancels with unchanged currency and replacement item. A worker query verifies APPLIED_MEMORY and CANCELLED records. After server exit a read-only SQLite query verified `(256,12,APPLIED_MEMORY)` and `(64,3,CANCELLED)`, breaker 0 and clean_stop 1. This is a headless handler test; it does not simulate a networked player's UI.

## Original review findings

| Finding | Disposition and evidence | Remaining acceptance |
| --- | --- | --- |
| H1: independent saves/crash atomicity | **Mitigated to the approved MVP scope, not made atomic.** Durable pre-mutation intent, durable observed outcomes, persistent breaker/dirty-boot flags, login containment and manual recovery procedure | Real crash boundaries, hosting disk behaviour and manual incident exercise; no automatic replay/refunds |
| H2: actual bank/live storage/applied Mixin coverage missing | Actual transformed GameTest covers all three plus full async Sell All and stale-plan cancellation | Real clients, full modpack, restart/OS-failure matrix remain staging tests |
| M1: overbroad item restrictions | Removed blanket EntityBlock/Sophisticated-namespace exclusion; signs/enchanting-table regressions pass | Container/backpack/storage identity exclusions remain intentional approved scope |
| M2: quantity limit does not bound scanning/repeated requests | Global cooldown, one active sale, scan/byte budgets, capped queues/queries and source limits | Encoding/comparison can still be expensive; actual MSPT/storage contention unmeasured |
| L1: runtime cannot identify exact Storage/Core builds | Startup verifies exact installed reference SHA-256, including same-metadata rebuilds, off-thread before admission | Full-modpack transformations/native library resolution still need host acceptance |

## Build reproduction and environment qualification

Normal supported invocation: `./gradlew build` with Java 21 and the four local references; offline works with a populated cache. GameTest additionally needs `-PcreateJar=/path/to/create-1.21.1-6.0.10.jar`.

This execution reused a writable copy of the existing Gradle cache at `/tmp/fbc-gradle-home`. JVM DNS/network resolution could not fetch Maven dependencies normally. SQLite was downloaded using the available proxy transport from Maven Central and checked against its published SHA-256, then supplied to Gradle by a temporary local Maven repository init script. **No replacement compiler/hand packager was used.** The temporary repository only provides `org.xerial:sqlite-jdbc:3.51.3.0`, with artifact metadata, and does not change project source or original dependencies.

Executed command shape (actual final combined build/GameTest completed successfully in 13 seconds):

```sh
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home \
GRADLE_USER_HOME=/tmp/fbc-gradle-home \
./gradlew --offline --no-daemon --no-watch-fs \
  -I /tmp/fbc-local-repository.init.gradle \
  -PdependencyDir=/Users/benatkinson/Developer/FoundryBulkCommerce-source/dependencies \
  '-PcreateJar=/Users/benatkinson/Library/Application Support/PrismLauncher/instances/The Panda Foundry 2.0/minecraft/mods/create-1.21.1-6.0.10.jar' \
  build runGameTestServer --console=plain
```

A clean build/GameTest also passed before the final revalidation/test additions; final changes were compiled and all suites rerun. An ordinary networked first-time Maven resolution without the environment override was **not established here**. External update/auth services emitted network warnings in the isolated run; the addon ledger opened and all required tests passed. Inspect actual Minecraft logs, since Gradle can exit successfully even after Minecraft mod-loading failure.

## Remaining limitations

- No real-client absent-addon handshake/UI test, complete production-modpack run or Linux native SQLite load test yet.
- No measured MSPT benchmark, real disk-full/corruption test, JVM kill-at-every-boundary or independent-resource restore test.
- No proof of crash atomicity across game resources. Normal clean shutdown records a lifecycle boundary, not coordinated physical fsync. Arbitrary off-thread third-party mutations are outside compensation guarantees.
- No MongoDB, outbox, remote sync, distributed ownership, automatic reconciliation, retention worker or writable moderator command.
- No standalone moderator permission node; commands use OP level 2/console.
- Original external source directory remains read-only. The complete implementation and Git history are in the delivered source project; import into a writable local checkout.

## Historical v0.1.0 compiled artifact

`foundrybulkcommerce-0.1.0.jar` SHA-256: `ecb76347d61cc27418aaf3986ed147300f98762d67a15ea97215c7551b09c336`.

SQLite nested artifact SHA-256: `3684ac5e9bd893cbe68eb0ac1708343f66be829b1d2b5ab317e783d05acd5aef`. Embedded driver is exact-version Jar-in-Jar; inspect THIRD_PARTY_NOTICES.md.

## Reference fingerprints

| Original filename | SHA-256 |
| --- | --- |
| CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar | d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767 |
| numismatics_treasury-1.0.2.jar | fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2 |
| sophisticatedcore-1.21.1-1.5.7.2381.jar | 33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39 |
| sophisticatedstorage-1.21.1-1.6.2.2159.jar | bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69 |
