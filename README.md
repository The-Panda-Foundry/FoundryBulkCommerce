# Foundry: Bulk Commerce

Version **0.1.0**, prepared **25 November 2026**. Minecraft **1.21.1**, NeoForge, Java **21**.

**Staging candidate.** The source compiles and the offline tests pass. A full dedicated-server launch with these mods and a live Mixin application has **not** been performed. Acceptance testing is required before considering production use.

Foundry: Bulk Commerce extends **Numismatics Treasury's server-shop Sell All button** to sell matching items from carried vanilla and supported Sophisticated Storage shulker boxes. Shop configuration, prices, complete lot sizes, Treasury's access checks and exact item/component matching remain authoritative.

## Usage

Carry the items/shulkers, open a Treasury server shop in **BUY_FROM_PLAYER** mode and confirm **Sell All**. One request sells complete lots up to the configured item limit. Repeat the action for additional batches. The addon uses the existing Treasury action payload and screen: clients need Treasury and its normal dependencies, but do not need Bulk Commerce.

Only the existing **Sell All** path is extended. Manual quantity sales, purchases, player shops, auctions and bank transfers remain Treasury's original implementations. This addon does not repair transaction handling in those other paths.

## Supported storage

| Source | Initial support |
| --- | --- |
| Loose main-inventory/hotbar items | Yes; same slots and component comparison as Treasury |
| Vanilla shulker boxes | Main inventory, hotbar or offhand; exactly one box per source |
| Sophisticated Storage shulkers | Canonical modern UUID-backed, finite storage; up to 256 inventory slots |
| Sophisticated stack upgrades | Standard tiers 1, 1+, 2–5 and omega, including oversized integer counts |
| Other Sophisticated upgrades | Excluded conservatively, even if disabled |
| Nested containers | Not traversed; container items are excluded as sale candidates |
| Backpacks, linked/ender storage, placed containers | Excluded |
| Armor/offhand loose items, Curios and other mod inventories | Not sold; only carried shulker items in vanilla inventory slots are considered |

Duplicate carried storage UUIDs exclude **every** matching alias from that request. Canonical contents are re-read and compared before any commit; the addon never pays from a cached item-count estimate.

The initial sale scope is ordinary commodities. Items with container/bundle/block-entity components, block-entity block items, items that disallow container nesting, Sophisticated items and backpack-named items are rejected as sale templates/candidates. This intentionally excludes some non-container block-entity items too. It avoids destroying hidden contents; unsupported items can still be handled through Treasury's unchanged manual sale path, subject to Treasury's own behaviour.

## Dependencies and building

Authoritative compatibility references (not bundled):

| Mod | Reference version |
| --- | --- |
| Numismatics Treasury | 1.0.2 |
| Create: Numismatics | 1.1.0+neoforge-mc1.21.1 |
| Sophisticated Storage | File build 1.6.2.2159; metadata version 1.6.2 |
| Sophisticated Core | File build 1.5.7.2381; metadata version 1.5.7 |

NeoForge **21.1.265** is the pinned build target. Declared minimum **21.1.242** follows Treasury; other loader builds need staging verification. Numismatics also requires **Create 6.0.10 to below 6.1.0**, with Create's normal dependencies. Sophisticated Storage/Core are optional at runtime but their supplied JARs are required for this source project's compilation and compatibility checks. Optional installed versions are constrained to the inspected metadata versions. File-build SHA-256 pins provide the stricter build-time reference check.

Place your own four reference JARs in `dependencies/`, retaining their original filenames. Nothing from that directory is committed or distributed. Java 21, Gradle wrapper 9.2.1 and ModDevGradle 2.0.148 are configured.

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew --offline build
```

Use `./gradlew build` if the required Gradle, plugin or NeoForge inputs are not already cached and network access is available. The Gradle build includes hash verification and the four executable test suites. The Gradle build itself has not been validated in the task sandbox: Gradle was blocked before project configuration when creating its local file-lock socket.

To keep dependencies at another path:

```sh
./gradlew --offline -PdependencyDir=/absolute/path/to/dependencies build
```

An offline fallback compiles using Java 21 and an existing **prepared NeoForge 21.1.265** cache, resolves exact libraries from the cached Minecraft manifest and NeoForge userdev configuration, runs the checks and packages only addon classes/resources:

```sh
python3 scripts/build_offline.py --dependencies /absolute/path/to/dependencies
```

Pass `--java-home` and `--gradle-home` when needed. The helper reads but never changes the original caches or dependency JARs. It requires the already prepared merged classes; it cannot replace a missing NeoForge setup. Its classpath includes the cached Minecraft client archive only to supply bootstrap resources to item tests. No Minecraft or dependency classes/resources are included in the addon.

The resulting files are `build/libs/foundrybulkcommerce-0.1.0.jar`, `build/libs/foundrybulkcommerce-0.1.0-sources.jar` and `build/verification.json`. `scripts/build_offline.py` creates deterministic archives.

## Server configuration

NeoForge writes `<world>/serverconfig/foundrybulkcommerce-server.toml`. The first run supplies defaults; use `defaultconfigs/` for defaults in new worlds if desired. The options are read per request. Restart the staging server after edits for a repeatable acceptance test.

| Option | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Enable the addon Sell All transaction path; false restores Treasury's original path |
| `vanillaShulkers` | `true` | Include carried vanilla shulker contents |
| `sophisticatedShulkers` | `true` | Include the supported Sophisticated adapter when both mods are installed |
| `bulkTransactionLimit` | `2304` | Individual items per request, including loose items; allowed range 1–1,048,576 |

The limit rounds **down** to complete lots. If it is smaller than the shop's lot size, that shop cannot complete a bulk sale. Price multiplication uses checked integer arithmetic: a payout larger than a positive signed 32-bit amount is refused before mutation. Account total/overflow capacity is checked with exact long arithmetic too. The default retains Treasury's original 2304-item upper bound; raise it deliberately after performance testing.

## Integrity and performance

Transactions run synchronously on Treasury's existing server-thread request. Planning operates on snapshots. Before mutation, all selected sources and the account must still match. Every applied change is compared against the complete expected result, followed by a final verification of every edited source. Only then is the account credited, and both standard and overflow balances are checked.

Removal or payment failures trigger compensation. A payment that might have happened is restored and verified **before** any items are returned. Item snapshots restore in reverse order. If compensation cannot be verified, bulk selling pauses for the lifetime of the dedicated-server process and logs a transaction reference, balances and source before/after data for administrator reconciliation. Do not simply restart to clear that pause: follow the recovery notes in `docs/ARCHITECTURE.md`.

No tick listeners, background scans, asynchronous inventory operations, addon persistent storage, new packets or persistent count caches are used. Work is limited to an explicit Sell All request, the vanilla inventory slots and supported shulkers' bounded slot lists. The only retained addon state is a server-thread reentrancy flag and an emergency circuit-breaker flag.

**Durability boundary:** this is a verified in-memory transaction, not a cross-file crash-atomic database transaction. Minecraft player data, Sophisticated SavedData and Numismatics balances save independently. A process/power/storage failure between writes can require restoring a consistent server backup. This version does not implement a write-ahead journal or automated crash reconciliation.

## Validation and next steps

Read `docs/VERIFICATION.md` for what was actually run. Use `docs/STAGING_CHECKLIST.md` for acceptance tests with the complete mod stack, including positive tests of real stack-upgrade items, UUID alias handling and save/restart persistence. `docs/ARCHITECTURE.md` describes the two Mixins, canonical storage schema and failure model. Original dependencies and production files have not been modified or deployed.
