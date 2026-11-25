# MVP architecture

Recorded **25 November 2026**. Approved scope: local SQLite journal, verified shulker selling, conservative recovery and read-only audits. MongoDB, outbox workers, distributed coordination and automatic reconciliation are not implemented. The historical proposal is retained separately in FUTURE_HYBRID_ARCHITECTURE.md.

## Authority and execution

| Stage | Authority | Action |
| --- | --- | --- |
| Capture, before intent acknowledgement | Live Treasury/shop, player inventory, Sophisticated canonical data, Numismatics account | Snapshot on server thread; no economic mutation authorised |
| Durable PREPARED | SQLite authorises one attempt; live game state still owns resources | Resume on server thread; revalidate before mutation |
| Apply/compensate | Actual verified in-memory resource states | Remove and verify every edit, then credit and verify; no yielding or SQL |
| Durable outcome | SQLite records observations, not cross-mod save confirmation | Release global admission and report result |
| Unclean restart or uncertain outcome | Local journal plus recovered resource data | Persistent breaker; manual investigation; never last-write-wins/replay |

A random UUID identifies each transaction globally; stable instance ID and per-start boot UUID identify its origin. No remote service participates. One active transaction globally avoids overlapping addon plans without a resource-lock framework. Other mod activity can happen while the writer runs, so continuation rechecks: active ID, ledger generation, connected player object, dimension, 30-second expiry, addon enabled, module/proximity/block identity/configured BUY mode, same shop object/template/price/lot, configured quantity limit, same account object/both balances and exact source images. Sophisticated edits recheck carried UUID uniqueness and original outer stack object. Stale preparation is recorded CANCELLED without economic mutation.

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

Mixin config is required and server-scoped; injections require one target. All use Mojang/mod runtime names with `remap=false`. Target incompatibility must fail rather than silently run an unprotected branch. Treasury's injection follows its module, distance, shop/block/config/mode checks; these checks are repeated after asynchronous intent persistence. Original exact `isSameItemSameComponents`, lot size and price remain authoritative. Disabled addon refuses Sell All. Other Treasury actions are not rewritten or made transactional by this addon.

Metadata pins reference versions, compileJava hashes all four supplied JARs, and asynchronous startup hashes the runtime Treasury/Numismatics files and installed Sophisticated pair before allowing players. See VERIFICATION.md for hashes. Hashing installed files rather than version strings addresses same-version rebuilds. Third-party transformations are not fingerprinted; test the full modpack. NeoForge/GameTest proved current injection application, not compatibility with all other Mixins.

No custom packets, registries, client assets or screen hooks. Treasury's Sell All button already sends the supported action without requiring loose inventory stock. Dedicated-server @Mod and server Mixin list keep addon code off clients; absent-addon real-client handshake remains an acceptance test. Sophisticated classes are reached only behind mod-presence checks. SQLite 3.51.3.0 is exact-version Jar-in-Jar; the development runs additionally require ModDevGradle additionalRuntimeClasspath, per its official documentation. Native JDBC loading was verified on this Mac; staging Linux architecture/native temp permissions need verification.

## Vanilla adapter

`VanillaSources` supports loose slots 0–35, plus the contents of vanilla-namespace shulker `BlockItem`s carried in vanilla inventory slots, including the offhand. Offhand/armor loose items are not sold. Boxes must have count one and no legacy BLOCK_ENTITY_DATA. Components larger than the vanilla 27-slot shulker layout are rejected rather than truncated.

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

Core's inspected oversized codec has the same id/component fields as Minecraft, with an unrestricted integer count. The addon decodes the component-bearing stack with Minecraft's public codec at count one, then restores the original positive integer count. This avoids the normal count bound while avoiding initialization of Core's unrelated client/container codecs. Candidate matching still uses the fully decoded item and components. Malformed IDs/components/counts abort preparation with no mutation.

Ordinary inventory handlers cap a single extraction at the normal item stack size and can invoke after-extract upgrade hooks. Repeatedly extracting would add work proportional to the number of normal stacks and could cause effects outside the rollback domain. Directly preparing canonical before/after images makes removal proportional to slot count and avoids processing hooks. To make that safe, **all non-stack upgrades are excluded**, and partition metadata is accepted only for one default finite partition starting at zero (or no serialized partition metadata). Creative/infinite, compression/compacting and other partitions are excluded.

Supported stack-upgrade IDs are the inspected standard tier 1, tier 1 plus, tiers 2–5 and omega. Upgrade items must also be instances of Core's StackUpgradeItem. Omega is a finite count multiplier, distinct from infinite/creative partitions. No upgrade slot is emptied or sold. Canonical inventories larger than 256 slots are skipped to bound parsing work. All serialized slots must be unique, in bounds and positive. Lists with unexpected entry types are rejected.

Before scanning supported shulkers, the adapter examines modern UUID components across the carried vanilla inventory. Every UUID with more than one occurrence is excluded completely; choosing the first alias could conceal a duplicate item. Linked endpoints are rejected **before** canonical resolution. Modern UUID-less empty boxes are ignored without creating saved records. Legacy custom/embedded storage is skipped rather than migrated during a commerce transaction; opening/placing it through Sophisticated Storage's normal flow is the migration route.

The full outer shulker stack and canonical compound must match the snapshot before writing, and the full canonical after-image must be observed before payment. `setStorageContents` marks SavedData dirty. An already existing StackStorageWrapper for the actual source stack is invalidated using `onContentsNbtUpdated()`; the addon never adds a wrapper cache entry or clears the global repository. Other aliases cannot obtain payment from an old snapshot: the addon always re-reads canonical data; Sophisticated's fromStack also reloads its canonical record.

## Containment, performance and limitations

The admission gate blocks all players while starting or recovering. Any live ledger/settlement emergency disconnects all players and denies re-entry, because a bulk-only pause would allow uncertain funds through other mods. The console remains available. No in-game breaker-reset, correction or refund command exists. See RECOVERY.md for offline handling.

Planning has a global 250 ms default admission interval, one active request, per-request slot/NBT/intent budgets, 256 Sophisticated inventory slots and at most 16 upgrade entries per source. Oversized requests abort before intent/mutation. Source processing is proportional to slots rather than total item count. Budgets also apply when no sale is possible; repeated failed attempts cannot bypass the cooldown. Busy notices are globally throttled to once/second.

NBT encoding, component decoding, full-image comparisons and serialisation still run on the server thread. A byte cap bounds accepted/scanned data, not the worst-case cost of an arbitrary third-party component codec. Moderator SQL queries are bounded to 25 summaries; inspect can read one capped intent and paginate it. No load benchmark was performed. Status reads only volatile memory. Disk space, WAL growth, journal latency and server MSPT require host monitoring; there is no statistics framework or automatic archival.

Cross-player UUID aliases are not globally scanned. Canonical data is re-read on each sale and full snapshots revalidated, preventing two addon sales from paying for one removal. The addon does not repair aliases or third-party cached-wrapper behaviour outside its supported caller. Arbitrary async third-party mutations, malicious extensions, fatal JVM errors, storage failure and independent world/bank restores exceed the synchronous compensation model. Conservative startup quarantine reduces exposure but does not turn separate resource persistence into one atomic transaction.
