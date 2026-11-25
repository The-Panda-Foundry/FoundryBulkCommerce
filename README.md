# Foundry: Bulk Commerce

**0.1.1 focused correction — 25 November 2026.** Minecraft 1.21.1, integrated or dedicated NeoForge server, Java 21. Standard Gradle build and a real NeoForge GameTest have passed. Real-player acceptance in the complete server modpack is still required. Nothing has been deployed to production.

Extends **Numismatics Treasury's existing server-shop Sell All** to matching items in carried vanilla and supported Sophisticated Storage shulkers. Treasury retains shop prices, complete lots, access checks and exact item/component matching. Only Sell All is replaced; manual sales, purchases, auctions and transfers retain their original implementation.

Carry your items/boxes, open a BUY_FROM_PLAYER server shop and click Sell All. Each request sells complete lots up to the configured item limit. Preparation and outcome acknowledgement are asynchronous; wait for the transaction reference/result before retrying. Clients need their normal modpack but no Bulk Commerce installation. Install the addon in the local client modpack when hosting single-player; it operates on that world's logical server. Multiplayer clients do not need this addon when it is installed on the dedicated server.

## Supported sources

| Source | Support |
| --- | --- |
| Loose main inventory/hotbar | Same slots and comparison as Treasury |
| Vanilla shulkers | Vanilla inventory, including offhand; one box per slot |
| Sophisticated Storage shulkers | Modern UUID-backed canonical finite storage; at most 256 slots |
| Stack upgrades | Standard tiers 1, 1+, 2–5 and omega; oversized positive integer counts |
| Other upgrades/partitions | Excluded, including disabled non-stack upgrades and infinite/creative partitions |
| Linked storage, backpacks, placed containers | Excluded |
| Nested containers | Never traversed or sold as commodities |
| Loose offhand/armor, Curios, other inventories | Not sold |

Every carried alias of a duplicate storage UUID is excluded. UUID uniqueness is checked again before mutation. Legacy embedded Sophisticated storage is skipped rather than migrated. Sale templates with container/bundle/block-entity data, storage identities, backpack names, or items that cannot fit inside containers are excluded. Ordinary signs and enchanting tables are supported; block-entity block types alone are no longer rejected.

## Build

Use Java 21 and the included Gradle 9.2.1 wrapper. ModDevGradle 2.0.148 targets NeoForge **21.1.265** (override for testing with `-PneoForgeVersion=21.1.256`; dependencies must be available). Minimum metadata version 21.1.242 is not a claim of testing every loader version.

Place the original four JARs in `dependencies/`, with these names:

- `CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar`
- `numismatics_treasury-1.0.2.jar`
- `sophisticatedcore-1.21.1-1.5.7.2381.jar`
- `sophisticatedstorage-1.21.1-1.6.2.2159.jar`

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
# Existing populated cache:
./gradlew --offline -PdependencyDir=/absolute/path/to/dependencies build
```

`build` runs reference hashes, compilation and five executable regression suites through `check`. Gradle's JUnit discovery task has no JUnit tests; the named JavaExec suites are the tests. Output: `build/libs/foundrybulkcommerce-0.1.1.jar` and `-sources.jar`. Original dependency JARs are excluded from Git and distribution. SQLite JDBC **3.51.3.0** is bundled through NeoForge Jar-in-Jar; see THIRD_PARTY_NOTICES.md. No MongoDB dependency is added.

Runtime requires Treasury 1.0.2, Numismatics 1.1.0 and Create 6.0.10–below 6.1.0 with its normal dependencies. Sophisticated Storage/Core are optional at runtime, required for source compilation/reference checks. Exact supplied reference bytes are checked at startup before admitting players (including the Sophisticated pair when installed). Unexpected builds fail closed even when their metadata version is identical.

For the disposable runtime integration test, also supply your own Create JAR:

```sh
./gradlew -PcreateJar=/absolute/path/to/create-1.21.1-6.0.10.jar runGameTestServer
```

A test-only `runIntegratedTestClient` configuration also exists for a copied disposable world; see docs/INTEGRATED_SERVER_FIX.md. It is not included in the production JAR.

Read the game test result log: a successful Gradle exit alone does not prove Minecraft loaded successfully. The test uses a disposable `run-gametest/` world, actual mods and actual Mixins. Do not point development runs at production data. The obsolete hand-packaging fallback was removed; Gradle is the supported build process. Environment-specific offline cache provisioning is documented in docs/VERIFICATION.md.

## Configuration

NeoForge generates `<world>/serverconfig/foundrybulkcommerce-server.toml`. Restart after edits for repeatable staging tests.

| Key | Default | Range/meaning |
| --- | --- | --- |
| `debugTransactions` | false | Opt-in INFO transaction phases, IDs, counts and outcome categories; no snapshots |
| `enabled` | true | False **refuses Sell All**; it never bypasses the ledger |
| `vanillaShulkers` | true | Include vanilla shulkers |
| `sophisticatedShulkers` | true | Include supported Sophisticated sources |
| `bulkTransactionLimit` | 2304 | Individual items including loose stock; 1–1,048,576; whole lots only |
| `serverInstanceId` | empty | Generate/persist a UUID; or set 1–64 letters/digits/`_.:-` before first use |
| `requestIntervalMillis` | 250 | Global admission interval; 50–60,000 ms |
| `maxScannedSlots` | 4096 | Planning slot inspections; 64–32,768 |
| `maxSnapshotBytes` | 1,048,576 | Scanned NBT budget and final UTF-8 intent cap, separately enforced; 16,384–4,194,304 |

One transaction globally remains active through durable outcome acknowledgement. Preparations expire after 30 seconds. Prices use checked int multiplication; combined standard/reserve funds use checked long arithmetic. A limit below a shop lot results in no sale. Large component encoding is still server-thread work: byte/slot bounds and cooldowns reduce exposure but are not a hard millisecond deadline.

Instance ID is bound to the ledger at first creation; changing it later fails closed. Give staging an independent world/ledger and distinct ID. Do not run cloned ledgers under the same identity on multiple servers.

## Ledger and operator commands

Local database: `<world>/data/foundrybulkcommerce/ledger.sqlite`, with SQLite WAL/SHM sidecars and `ledger.lock`. Use persistent **local** storage, not a shared network filesystem. A single bounded worker performs all database operations, including FULL durability commits. The Minecraft thread never waits for SQL; it revalidates shop, player, account, identities and exact source snapshots before mutation.

OP permission level **2**, or console, can use these read-only commands:

| Command | Result |
| --- | --- |
| `/bulkcommerce status` | Memory-only readiness, breaker, instance and queued work |
| `/bulkcommerce recent [limit]` | Latest 10, at most 25 |
| `/bulkcommerce player <uuid> [limit]` | History for exact player UUID |
| `/bulkcommerce unresolved` | Latest 25 pending/recovery records |
| `/bulkcommerce inspect <transaction-uuid> [page]` | Summary/outcome; pages 1+ show full intent evidence |

No refund, replay, reconciliation or breaker-reset command exists. Moderator roles without OP level 2 need a separately reviewed permission integration; do not grant OP merely for this addon. Larger investigations use an offline/read-only SQLite client.

**Containment affects the whole server:** startup denies player login until the ledger is ready. An unresolved restart or emergency blocks all new logins. A live ledger/settlement emergency disconnects all online players, keeping uncertain funds unavailable through other mods. Console audit commands remain available when the database can be opened. A read-query failure also trips containment. Disabling/removing the addon is not a recovery procedure.

## Recovery limits and operations

Intent is durable before any item removal or payment. Successful extraction is verified before payment; payment and ordinary rollback are verified. Outcomes distinguish `APPLIED_MEMORY` from durable game saves. An unclean restart flags the previous boot's potentially mutating records, including apparently successful or rolled-back ones. The persistent breaker survives restarts. **No automatic replay or refund occurs.**

The ledger cannot make Minecraft player files, Sophisticated SavedData and Numismatics atomic. Their saves can disagree after a crash. Ordinary clean shutdown is an operational boundary, not proof of power-loss atomicity or physical persistence across every mod. Before/after snapshots are evidence, never unconditional restoration instructions. Follow docs/RECOVERY.md; preserve one consistent server backup.

There is no automatic ledger retention. Monitor disk/WAL size, unresolved records and status; retain unresolved evidence indefinitely. At 48 KiB per transaction, 1,000/day is roughly 1.37 GiB per 30 days before indexes/WAL; actual snapshots may be much larger. Resolved records may be archived offline after backup and review. Protect the database/backups as sensitive inventory and balance records. POSIX directory/file permissions are restricted; Windows needs hosting-account ACLs.

See docs/ARCHITECTURE.md for authority, schema and Mixins; docs/VERIFICATION.md for evidence and remaining gaps; docs/STAGING_CHECKLIST.md for acceptance. The original hybrid proposal is retained, explicitly deferred, in docs/FUTURE_HYBRID_ARCHITECTURE.md.

## v0.1.1 integrated-server retest

Replace v0.1.0 with **only** `foundrybulkcommerce-0.1.1.jar` and fully restart Minecraft so the common Mixins can apply. Use a disposable world with cheats enabled (or Open to LAN with cheats), then run **`/bulkcommerce status`**. The bare root has no executor; all subcommands require permission level 2. The normal Sell All button does not require that moderator permission.

Expected log stages, under `[BulkCommerce]`:

1. `0.1.1 initialised on physical CLIENT` in Prism (`DEDICATED_SERVER` on a server process).
2. `Registered /bulkcommerce ... (permission level 2; read-only)` when server commands are built.
3. `Starting logical server type=IntegratedServer` (or DedicatedServer).
4. Four `Compatibility SHA-256 verified` lines when Sophisticated is installed.
5. `SQLite ledger healthy and ready ... admission=OPEN`, or an explicit recovery/error diagnostic.

Set `debugTransactions = true` in the world's serverconfig TOML and restart the world to see request receipt, planning counts, durable intent, revalidation and durable outcome phases. It defaults false and does not print inventory snapshots. On a multiplayer client without a local world, initialization registers handlers but opens no ledger and performs no transactions locally.

For a controlled first test, configure the server shop to BUY_FROM_PLAYER, stone, **64 items for 3 Spurs**. Carry one vanilla shulker containing exactly 64 stone plus a nonmatching item; keep loose stone out of the inventory. Confirm Sell All, verify 64 stone removed, the other item preserved and exactly 3 Spurs added. Inspect the reference using `/bulkcommerce recent` and `/bulkcommerce inspect <uuid>`. Then save/reload and check persistence. The circuit breaker and all recovery restrictions remain active in single-player too; the owner has no bypass.

The correction and dedicated runtime regression pass. An actual physical-client/integrated runtime launch was attempted but blocked before mod loading by this task environment's macOS graphical-session restrictions. NeoForge 21.1.256/full-pack Prism acceptance therefore remains to be rerun by the architect; see the investigation report for the evidence and boundaries.
