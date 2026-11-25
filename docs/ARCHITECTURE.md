# MVP architecture

Recorded **25 November 2026**. Approved scope: local SQLite journal, verified hotbar shulker/backpack selling, conservative recovery and read-only audits. MongoDB, outbox workers, distributed coordination and automatic reconciliation are not implemented. The historical proposal is retained separately in FUTURE_HYBRID_ARCHITECTURE.md.

## Authority and execution

| Stage | Authority | Action |
| --- | --- | --- |
| Capture, before intent acknowledgement | Live Treasury/shop, player inventory, Sophisticated canonical data, Numismatics account | Snapshot on server thread; no economic mutation authorised |
| Durable PREPARED | SQLite authorises one attempt; live game state still owns resources | Resume on server thread; revalidate before mutation |
| Apply/compensate | Actual verified in-memory resource states | Remove and verify every edit, then credit and verify; no yielding or SQL |
| Durable outcome | SQLite records observations, not cross-mod save confirmation | Release global admission and report result |
| Unclean restart or uncertain outcome | Local journal plus recovered resource data | Persistent breaker; manual investigation; never last-write-wins/replay |

A random UUID identifies each transaction globally; stable instance ID and per-start boot UUID identify its origin. No remote service participates. One active transaction globally avoids overlapping addon plans without a resource-lock framework. Other mod activity can happen while the writer runs, so continuation rechecks: active ID, ledger generation, connected player object, dimension, 30-second expiry, addon enabled, module/proximity/block identity/configured BUY mode, same shop object/template/price/lot, configured quantity limit, same account object/both balances and exact source images. All container edits recheck their original eligible hotbar position and object/snapshot. Sophisticated edits also recheck repository-scoped UUID uniqueness across supported identity locations. Stale preparation is recorded CANCELLED without economic mutation.

The worker receives only immutable IDs, numbers and serialized strings. No worker reads inventories, wrappers or accounts. Database callbacks schedule server-thread continuations. Shutdown can drop a callback; PREPARED remains a recovery obligation. No tick scanning, background inventory scanning or persistent count cache exists.

## Local schema and durability

`<world>/data/foundrybulkcommerce/ledger.sqlite`, schema `PRAGMA user_version=1`:

| Table | Contents |
| --- | --- |
| `metadata` | Stable instance ID, boot ID, clean-stop marker, breaker and reason |
| `transactions` | UUID primary key; instance/boot; timestamp; player UUID/name; quantity/payment; state; recovery flag; full intent/outcome JSON; SHA-256 of intent |
| `reconciliations` | UUID, linked transaction ID, timestamp, operator, decision/evidence notes; reserved for explicit offline manual entries |

Indexes cover time, player/time and recovery/state. JSON includes schema/addon versions, shop dimension/position/template/price/lot, both original balance fields and source before/after images (item or canonical SNBT). Outcomes include observed balances and source matches. SHA-256 detects accidental payload disagreement; it is not tamper-proof authentication. UUIDs and schema-versioned JSON can be exported later without shipping MongoDB machinery now.

One platform-thread executor, queue capacity 64, owns the connection. WAL, `synchronous=FULL`, busy timeout 2,500 ms, integrity quick-check and exclusive process lock are established before readiness. All SQL/file hash/commit operations run there. Queue rejection never authorises mutation. Local filesystem and host durability guarantees still matter; FULL cannot rescue failed hardware. SQLite's default WAL autocheckpoint remains in use, on the writer. There is no custom checkpoint or retention worker.

A normal sale needs two durable transaction commits: PREPARED and outcome. No ARMED stage is needed for this conservative MVP: PREPARED itself means effects may have been attempted if no outcome survived. Intent commit uncertainty means no mutations are attempted; an outcome acknowledgement failure after mutation quarantines the server. No retries perform economic mutations. The global active flag persists until outcome acknowledgement or containment.

States: PREPARED, CANCELLED, APPLIED_MEMORY, ROLLED_BACK_MEMORY, RECOVERY_REQUIRED. CANCELLED establishes no economic attempt. Other outcomes describe memory observations. RECOVERY_REQUIRED persists its recovery flag and breaker in the same SQL commit. Failed IO sets the in-memory blocker immediately and best-effort persists it; a surviving PREPARED or dirty-boot marker provides recovery evidence if the later breaker write fails.

On startup, a previous boot without clean-stop confirmation flags its PREPARED/APPLIED_MEMORY/ROLLED_BACK_MEMORY/RECOVERY_REQUIRED records for manual review. Any PREPARED or recovery-flagged row, or stored breaker, keeps admission closed. A later clean empty boot cannot erase these flags. Cancelled preparations alone do not require resource reconciliation.

Only normal server-loop exit plus a returned stopServer save sequence, no active transaction, no emergency and no pending/recovery row permits a clean-stop marker. Crash-finally ServerStoppedEvent alone is insufficient. The non-daemon worker closes asynchronously after the lifecycle event. This is conservative operational containment, not independently verified fsync for every game's files. Clean-shutdown save failures hidden by another mod, inconsistent external restores, and power loss after separately persisted resources remain risks.

## Settlement and compensation

The synchronous coordinator registers an edit as attempted before writing. It checks each full after-image immediately, rechecks all edits, then credits through Treasury BankService and checks exact combined balance. A failure after possible credit restores and verifies the wallet first. Only after that verification are attempted item edits restored in reverse order and checked. Uncertain wallet compensation leaves items removed; uncertain item rollback issues no new payment. Either requires manual reconciliation. A refresh/UI exception never reverses a settled sale.

Numismatics has `balance:int` capped at 1,073,741,823 plus `additionalBalance:long`. Both fields and account identity are captured. Payout multiplication and combined totals use exact checked arithmetic. Compensation restores both exact fields and marks the account dirty, avoiding setBalance's overflow redistribution semantics.

## Mixins and compatibility

| Mixin | Target / reason |
| --- | --- |
| TreasurySellAllMixin | `ServerActionHandler.tradeShop(ServerPlayer, JsonObject, boolean)` before ordinal 0 `InventoryUtil.countMatching(Player, ItemStack):int`; only validated Sell All; cancel original branch before addon call |
| BankAccountAccessor | Numismatics private `balance:I` and `additionalBalance:J` readers/writers for exact verification and compensation |
| PlayerAdmissionMixin | `PlayerList.canPlayerLogin(SocketAddress,GameProfile)` HEAD; deny admission until ready or during containment |
| ServerShutdownMixin | `MinecraftServer.runServer` after handleServerStopping for normal exit; `stopServer` RETURN for save-sequence completion |

Mixin config is required and common to both physical distributions; injections require one target. All use Mojang/mod runtime names with `remap=false`. Target incompatibility must fail rather than silently run an unprotected branch. Treasury's injection follows its module, distance, shop/block/config/mode checks; these checks are repeated after asynchronous intent persistence. Original exact `isSameItemSameComponents`, lot size and price remain authoritative. Disabled addon refuses Sell All. Other Treasury actions are not rewritten or made transactional by this addon.

Dependency metadata applies on BOTH physical distributions when the addon is installed, so integrated hosts validate required mods too. This does not require remote clients to install the addon. Metadata pins reference versions, compileJava hashes the four supplied JARs and three optional compile references, and asynchronous startup hashes the runtime Treasury/Numismatics files and installed Sophisticated, Curios and Accessories integrations before allowing players. See VERIFICATION.md for hashes. Hashing installed files rather than version strings addresses same-version rebuilds. Third-party transformations are not fingerprinted; test the full modpack. NeoForge/GameTest proved current injection application, not compatibility with all other Mixins.

No custom packets, registries, client assets or screen hooks. Treasury's Sell All button already sends the supported action without requiring loose inventory stock. The common @Mod entry point registers handlers on both physical distributions; all four Mixins target common server/account classes. Actual transactions remain restricted to server lifecycle events and ServerPlayer/server-thread calls. This enables integrated servers inside physical clients without making multiplayer clients install the addon. Absent-addon real-client handshake remains an acceptance test. Sophisticated classes are reached only behind mod-presence checks. SQLite 3.51.3.0 is exact-version Jar-in-Jar; the development runs additionally require ModDevGradle additionalRuntimeClasspath, per its official documentation. Native JDBC loading was verified on this Mac; staging Linux architecture/native temp permissions need verification.

## Vanilla adapter

`VanillaSources` supports loose slots 0–35, plus the contents of vanilla-namespace shulker `BlockItem`s in hotbar slots 0–8 only. Offhand/armor loose items are not sold. Boxes must have count one and no legacy BLOCK_ENTITY_DATA. Components larger than the vanilla 27-slot shulker layout are rejected rather than truncated.

Vanilla `ItemContainerContents` is copied, edited and reconstructed; the outer ItemStack is replaced as one edit. The full outer stack, including every unrelated component and nested item, is the before/after verification unit. No nested container is opened or emptied.

## Sophisticated adapter

The reference stores carried shulker contents under a modern `STORAGE_UUID` in overworld-backed `ItemContentsStorage` SavedData. The canonical record includes:

```text
storageWrapper.contents.inventory.Size
storageWrapper.contents.inventory.Items[]
  Slot: int
  id: item registry identifier
  count: int, default 1
  components: optional data component patch
storageWrapper.contents.upgradeInventory.Items[]
storageWrapper.contents.partitioner
```

This is a narrow **version-pinned schema adapter**, not a generic capability walker. It uses official `ItemContentsStorage.getStorageContents` and `setStorageContents` methods. Full compounds are copied; only matching item counts/entries change, preserving all other canonical data.

Core's inspected oversized codec has the same id/component fields as Minecraft, with an unrestricted integer count. The addon decodes the component-bearing stack with Minecraft's public codec at count one, then restores the original positive integer count. This avoids the normal count bound while avoiding initialization of Core's unrelated client/container codecs. Candidate matching still uses the fully decoded item and components. Malformed IDs/components/counts mark that source INVALID_DATA without mutation; aggregate workload/serialization limits abort the entire preparation.

Ordinary inventory handlers cap a single extraction at the normal item stack size and can invoke after-extract upgrade hooks. Repeatedly extracting would add work proportional to the number of normal stacks and could cause effects outside the rollback domain. Directly preparing canonical before/after images makes removal proportional to slot count and avoids processing hooks. To make that safe, **all non-stack upgrades are excluded**, and partition metadata is accepted only for one default finite partition starting at zero (or no serialized partition metadata). Creative/infinite, compression/compacting and other partitions are excluded.

All registered exact-class Core StackUpgradeItem modifiers in the Sophisticated Storage namespace are supported: seven upgrades and three downgrades, each a positive finite multiplier. Subclasses/other processing hooks remain excluded. Omega is a finite count multiplier, distinct from infinite/creative partitions. No upgrade slot is emptied or sold. Inventory/upgrade capacities come from non-mutating dependency defaults and modern saved components. The old fixed 256/16 assumptions are replaced by API bounds plus the existing scan/snapshot budgets. All serialized slots must be unique, in bounds and positive. Lists with unexpected entry types are rejected.

Before scanning supported hotbar shulkers, the adapter examines modern UUID components across vanilla inventory/cursor and supported equipment. Every repository-scoped UUID with more than one occurrence is excluded completely; choosing the first alias could conceal a duplicate item. Linked endpoints are rejected **before** canonical resolution. Modern UUID-less empty boxes are ignored without creating saved records. Legacy custom/embedded storage is skipped rather than migrated during a commerce transaction; opening/placing it through Sophisticated Storage's normal flow is the migration route.

The full outer shulker stack and canonical compound must match the snapshot before writing, and the full canonical after-image must be observed before payment. `setStorageContents` marks SavedData dirty. An already existing StackStorageWrapper for the actual source stack is invalidated using `onContentsNbtUpdated()`; the addon never adds a wrapper cache entry or clears the global repository. Other aliases cannot obtain payment from an old snapshot: the addon always re-reads canonical data; Sophisticated's fromStack also reloads its canonical record.

## Containment, performance and limitations

The admission gate blocks all players while starting or recovering. Any live ledger/settlement emergency disconnects all players and denies re-entry, because a bulk-only pause would allow uncertain funds through other mods. The console remains available. No in-game breaker-reset, correction or refund command exists. See RECOVERY.md for offline handling.

Planning has a global 250 ms default admission interval, one active request, per-request slot/NBT/intent budgets and API-validated inventory/upgrade capacities, with one container's inventory capacity also bounded by maxScannedSlots. Oversized requests abort before intent/mutation. Source processing is proportional to slots rather than total item count. Budgets also apply when no sale is possible; repeated failed attempts cannot bypass the cooldown. Busy notices are globally throttled to once/second.

NBT encoding, component decoding, full-image comparisons and serialisation still run on the server thread. A byte cap bounds accepted/scanned data, not the worst-case cost of an arbitrary third-party component codec. Moderator SQL queries are bounded to 25 summaries; inspect can read one capped intent and paginate it. Local warm-JVM source preparation and admission/ledger timings are recorded in PERFORMANCE.md; full-pack host MSPT/load benchmarking is still pending. Status reads only volatile memory. Disk space, WAL growth, journal latency and server MSPT require host monitoring; there is no statistics framework or automatic archival.

Cross-player UUID aliases are not globally scanned. Canonical data is re-read on each sale and full snapshots revalidated, preventing two addon sales from paying for one removal. The addon does not repair aliases or third-party cached-wrapper behaviour outside its supported caller. Arbitrary async third-party mutations, malicious extensions, fatal JVM errors, storage failure and independent world/bank restores exceed the synchronous compensation model. Conservative startup quarantine reduces exposure but does not turn separate resource persistence into one atomic transaction.

## v0.1.1 activation correction

See INTEGRATED_SERVER_FIX.md. v0.1.0's physical DEDICATED_SERVER entry point and `server` Mixin list excluded the integrated server. v0.1.1 uses the common constructor and `mixins` list. No client-only imports were added to production code. Optional debugTransactions logs bounded phase metadata at INFO, default off. Constructor/command registration and compatibility/ledger readiness diagnostics are always visible at INFO (blocked health at WARN; failures/containment at ERROR). No SQL, network wait, refund or recovery bypass was added. A stale opening callback from a closed integrated world cannot change the next world's admission state.

## v0.1.2 focused changes

Parse each canonical stored item once into exact matches and entry indexes/counts. Reuse this immutable plan when copying the after-image, eliminating redundant validation/count/removal decoding. The full before/after verification and wallet compensation sequence is unchanged. No game object crosses to the SQLite worker. No periodic task/cache, remote dependency, repeated Treasury transaction or client interface was added. Defaults, UUID safeguards, Mixin targets and SQLite schema remain unchanged.

Detailed skipped-source reasons are bounded by the carried inventory size and emitted only with transaction debugging. Scan/snapshot failures abort before mutations; unknown settlement state retains persistent recovery containment. See SHULKER_COMPATIBILITY.md for the baseline investigation, exact current matrix, and why the historical Netherite rejection is not claimed to be solved by an identified tier-specific patch.

## v0.2.0 focused adapters and hotbar policy

BackpackSources uses the inspected public BackpackStorage getBackpackContents/setBackpackContents API. Its root contains inventory/upgradeInventory/settings directly; Storage's root contains storageWrapper.contents. They are independent overworld SavedData repositories named sophisticatedbackpacks and sophisticatedstorage, even though both outer items use Core's STORAGE_UUID component. Identity keys therefore include repository. Equal UUID values across repositories are independent; aliases within a repository are excluded, including protected main inventory, offhand, armour, cursor and supported active/cosmetic equipment occurrences.

StorageIdentities performs only component inspection outside the hotbar. Optional CuriosIdentities and AccessoriesIdentities are separate classes reached after ModList presence checks. It avoids Backpacks' extensible PlayerInventoryProvider because an opaque third-party provider could enumerate unbounded locations and only return backpacks, concealing shulker aliases. Unknown third-party equipment providers are not covered. A bridge exposing the same physical equipment through both APIs is unverified and can conservatively cause a duplicate rejection; no heuristic deduplication weakens protection.

CanonicalContents shares the existing finite inventory parser between the two adapters. Production still uses narrow exact-version schema validation, actual API default capacities plus modern saved components, positive int slot counts, checked long totals and stack-level count changes. Only exact passive Core StackUpgradeItem instances registered in the source mod namespace are accepted. Every other upgrade, non-default partition, linked/legacy storage, Backpacks loot/template state and nonzero columns-taken layout is rejected. Installed upgrade/setting/root data is preserved in full snapshots. See V0_2_0_COMPATIBILITY.md.

Planning never calls BackpackWrapper.fromStack, which can allocate a UUID, migrate legacy data or populate caches. Native BackpackStorage returns the existing canonical record. Its setter merges all supplied top-level entries into the existing compound; it does not remove absent keys. The addon supplies complete before/after snapshots with the same top-level key set, changes only inventory entries and checks full root equality after writes. Newly introduced/unexpected data or callback effects cannot be silently accepted as verified rollback. Only an already-existing BackpackWrapper is invalidated with onContentsNbtUpdated; no new wrapper/global cache is created by production planning.

Vanilla StackEdit requires the captured outer object plus exact image before initial mutation; a verified restored snapshot may be a replacement object. Sophisticated edits require the same outer object, exact components, hotbar slot, canonical image and current unique identity. Source movement, replacement or aliases introduced during asynchronous preparation cancel before mutation/payment. No yield occurs during extraction, verification, payment or compensation. Audit descriptions distinguish Sophisticated backpack and shulker sources; ledger schema, states, common Mixins, bank arithmetic and compensation order are unchanged.

No Backpacks networking, equipment selling, nested traversal, remote storage, automatic crash recovery or future replication infrastructure is added. Actual clientless multiplayer compatibility and independent-resource crash behaviour remain staging gates.
