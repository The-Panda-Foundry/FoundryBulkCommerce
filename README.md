# Foundry: Bulk Commerce

**v0.2.0 release candidate — 25 November 2026.** Architect-reported integrated functional acceptance has passed for the manually tested scenarios; upgraded backpacks, persistence, concurrency, crash recovery and dedicated compatibility remain pending. See [live acceptance and final validation plan](docs/LIVE_ACCEPTANCE_2026-11-25.md). Minecraft 1.21.1, Java 21, NeoForge; integrated and dedicated logical servers. Extends Treasury's existing BUY_FROM_PLAYER server-shop **Sell All**. No production deployment has occurred.

Put eligible containers in **any of hotbar slots 0–8**, open the existing Treasury interface and click Sell All. The selected slot does not matter. Containers in the remaining inventory, offhand, armour, Curios and Accessories are protected. Loose matching items retain Treasury's ordinary main-inventory behaviour (slots 0–35). Nested containers are never traversed or sold as commodities.

This deliberately changes v0.1.2 container eligibility. Preserve your existing transaction-limit settings: the default remains 2,304; the accepted stress-test setting was 230,400. Treasury's maximum configurable shop bundle remains 2,304. A sale can contain multiple complete bundles, up to the Bulk Commerce limit; excess stock remains in its source.

## Compatibility

| Source | Extraction |
| --- | --- |
| Vanilla shulker boxes | Hotbar only, all colours, 27 slots |
| Sophisticated Storage 1.6.2.2159 | All six shulker tiers; supported passive stack modifiers |
| Sophisticated Backpacks 3.26.9.2195 | Optional; all six backpack tiers; nine passive stack modifiers including Omega |
| Equipped, offhand, main-inventory containers outside hotbar | Identity checks only; contents never decoded for sale |
| Linked/remote, placed, nested, legacy embedded storage | Excluded |
| Non-stack processing upgrades, creative/infinite partitions, unverified layouts | Excluded safely |

[Complete shulker/backpack capacity and upgrade matrix](docs/V0_2_0_COMPATIBILITY.md). Uses the actual canonical Sophisticated repositories, API capacities and modern saved components. No hardcoded production tier/item allowlist. Full identity/upgrade/settings/metadata preservation is verified before payment. Unknown dependency builds fail closed by exact SHA-256, even when their version strings match.

Storage and Backpacks use independent SavedData repositories. Identities are **repository + UUID**. Duplicate occurrences within a repository exclude every alias. Identity inspection covers vanilla inventory/armour/offhand, cursor and installed supported Curios/Accessories active/cosmetic slots; it is independent of extraction eligibility and repeated before mutation. Unknown third-party equipment providers and recursive nested identities are not inspected. Third-party equipment bridges mirroring one physical slot into both APIs are unverified and may conservatively reject it as a duplicate.

## Installation and configuration

Dedicated server: replace the old addon with **only** `foundrybulkcommerce-0.2.0.jar` and restart. Multiplayer clients need their normal modpack, **without needing Bulk Commerce**. Single-player hosts install the addon locally; its logical server handles transactions. No new screens, packets or client registry entries are introduced. Real absent-addon client handshake remains a staging gate; integrated gameplay is confirmed only for the reported manual scenarios.

Required economy references: Treasury 1.0.2, Create Numismatics 1.1.0 and their usual Create dependencies. Optional integrations: exact Storage/Core, Backpacks, Curios 9.5.1 and Accessories 1.1.0-beta.53 reference builds. The addon does not supply those mods. SQLite JDBC 3.51.3.0 is bundled via NeoForge Jar-in-Jar; see [notices](THIRD_PARTY_NOTICES.md).

Configuration: `<world>/serverconfig/foundrybulkcommerce-server.toml`. Existing settings are retained; new `sophisticatedBackpacks=true` enables the adapter only when that mod is installed. Hotbar eligibility is mandatory. Defaults: `bulkTransactionLimit=2304`, `maxScannedSlots=4096`, `maxSnapshotBytes=1048576`, `requestIntervalMillis=250`, `debugTransactions=false`. [All settings and distinct limits](docs/CONFIGURATION.md).

One globally active transaction and a cooldown bound admission; work is per slot/stack, never per individual item. No continuous scanning, tick listener or persistent inventory cache is added. Protected containers receive lightweight UUID inspection only. Database operations and fingerprint IO run on the bounded ledger worker; server-thread continuations revalidate positions, identities, shop access, prices, inventory and account state before mutation.

## Ledger and operations

Local ledger: `<world>/data/foundrybulkcommerce/ledger.sqlite` with WAL/SHM and process lock. Use persistent local disk, a unique server instance ID and a separate staging world/ledger. Durable intent precedes economic mutation; every removal is verified before credit. Checked arithmetic, exact component matching, compensation verification, persistent breaker and conservative crash containment remain unchanged. Backpack sources are explicitly identified in transaction evidence.

| Read-only command (OP level 2 or console) | Result |
| --- | --- |
| `/bulkcommerce status` | Readiness, breaker, instance and queue |
| `/bulkcommerce recent [limit]` | Latest 10; maximum 25 |
| `/bulkcommerce player <uuid> [limit]` | Player history |
| `/bulkcommerce unresolved` | Latest 25 unresolved records |
| `/bulkcommerce inspect <transaction-uuid> [page]` | Outcome and paginated evidence |

Startup logs confirm physical side, command registration, logical server, exact dependency fingerprints and ledger readiness. Enable transaction debug logging for bounded rejection/phase diagnostics; full inventory snapshots are not printed. Sell All does not require moderator permission.

**The ledger does not provide atomic persistence across Minecraft, either Sophisticated repository and Numismatics.** Outcomes such as APPLIED_MEMORY describe verified memory, not coordinated resource saves. Unclean boots/uncertain outcomes trigger persistent manual-recovery containment. Admission is closed until ready; live emergencies disconnect players and block new logins. No automatic replay/refund or breaker-reset command exists. Never bypass recovery by deleting the ledger/removing the addon. [Architecture](docs/ARCHITECTURE.md) · [manual recovery](docs/RECOVERY.md).

No automatic retention: monitor free disk/WAL growth, unresolved records and host latency. Retain unresolved evidence; archive resolved records offline only after consistent backup and review. Ledger/backups contain sensitive inventory/account evidence. At 48 KiB/record, 1,000 transactions/day is about 1.37 GiB/month before indexes/WAL; large snapshots can cost more. The future hybrid SQLite/MongoDB proposal remains [documented and deferred](docs/FUTURE_HYBRID_ARCHITECTURE.md).

## Build and test

Use Java 21 and the Gradle 9.2.1 wrapper. Default ModDevGradle 2.0.148 runtime is NeoForge **21.1.265**, the cached version actually tested here. Override with `-PneoForgeVersion=21.1.256` when those dependencies are available. Metadata minimum 21.1.242 is not a claim of testing every loader version.

Place the original four reference JARs plus the three optional-integration compile references in `dependencies/`:

```text
CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar
numismatics_treasury-1.0.2.jar
sophisticatedcore-1.21.1-1.5.7.2381.jar
sophisticatedstorage-1.21.1-1.6.2.2159.jar
sophisticatedbackpacks-1.21.1-3.26.9.2195.jar
curios-neoforge-9.5.1+1.21.1.jar
accessories-neoforge-1.1.0-beta.53+1.21.1.jar
```

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
./gradlew -PcreateJar=/absolute/path/create-1.21.1-6.0.10.jar runGameTestServer
# Equipped integrations additionally need their normal runtime dependencies:
./gradlew -PcreateJar=/absolute/path/create.jar -PwithCurios -PwithAccessories \
  -PowoJar=/absolute/path/owo-lib-neoforge-0.12.15.5-beta.1+1.21.jar runGameTestServer
# Same compiled source, without Backpacks/Curios/Accessories at runtime:
./gradlew -PcreateJar=/absolute/path/create.jar -PwithoutBackpacks runGameTestServer
```

Override paths with `-PdependencyDir=...` (original four), `-PbackpacksJar=...`, `-PcuriosJar=...`, `-PaccessoriesJar=...`. Optional APIs are required to compile their adapters, not to run the production addon. Optional runtime flags remain effective when all compile references share dependencies/. `--offline` reuses a populated Gradle cache. No originals are modified, tracked or redistributed.

`build` invokes five executable JavaExec suites through `check` (2,406 checks), rather than JUnit tests. GameTests additionally load actual mods/Mixins, bank, canonical repositories and native SQLite. Read actual required-test success lines, not just Gradle's exit. Output: `build/libs/foundrybulkcommerce-0.2.0.jar` and sources JAR. Test-only fixtures are never packaged in the production addon. Development run worlds are disposable; never use production data.

[Verification and limitations](docs/VERIFICATION.md) · [performance](docs/PERFORMANCE.md) · [integrated acceptance](docs/INTEGRATED_ACCEPTANCE.md) · [dedicated staging](docs/STAGING_CHECKLIST.md). v0.1.2's accepted baseline is preserved by tag `baseline/v0.1.2`. Dedicated full-pack, client handshake, upgraded-backpack validation, persistence, concurrency, host load and crash-boundary testing remain outstanding.
