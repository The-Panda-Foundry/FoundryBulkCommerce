# MVP verification and review disposition

Date: **25 November 2026**. Based on reviewed source commit `ca0b30f`; final MVP commit is recorded in the delivery receipt/Git history. Nothing deployed to production; original four JARs remain unmodified and excluded from distribution.

## Executed results

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

## Compiled artifact

`foundrybulkcommerce-0.1.0.jar` SHA-256: `ecb76347d61cc27418aaf3986ed147300f98762d67a15ea97215c7551b09c336`.

SQLite nested artifact SHA-256: `3684ac5e9bd893cbe68eb0ac1708343f66be829b1d2b5ab317e783d05acd5aef`. Embedded driver is exact-version Jar-in-Jar; inspect THIRD_PARTY_NOTICES.md.

## Reference fingerprints

| Original filename | SHA-256 |
| --- | --- |
| CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar | d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767 |
| numismatics_treasury-1.0.2.jar | fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2 |
| sophisticatedcore-1.21.1-1.5.7.2381.jar | 33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39 |
| sophisticatedstorage-1.21.1-1.6.2.2159.jar | bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69 |
