# Architecture and compatibility decisions

Recorded 25 November 2026.

## Ownership boundaries

Treasury remains the authority for shop access, mode, template, lot size and price. Numismatics owns the seller's player account. Vanilla owns inventory/component persistence; Sophisticated Storage owns canonical shulker SavedData. Bulk Commerce coordinates a bounded, synchronous Sell All operation across those owners.

No user-supplied item identifiers, quantities or prices are trusted. The request's position is already checked by Treasury; the addon re-reads the block entity and its configuration. No new command or alternate unguarded sale entry point is registered.

## Treasury Mixin

`TreasurySellAllMixin` targets the private static method:

```text
dev.yassou.numismaticstreasury.network.ServerActionHandler
tradeShop(ServerPlayer, JsonObject, boolean): void
```

It injects immediately before ordinal **0** of:

```text
InventoryUtil.countMatching(Player, ItemStack): int
```

In the supplied JAR that invocation is evaluated only for Sell All, after checking the serverShop module, player proximity, expected server-shop block identity, the shop block entity, configured status and BUY_FROM_PLAYER mode. The original `near` threshold is therefore retained without reimplementing it. The hook also checks `sellAll`. It cancels before executing the replacement transaction; addon failures never fall through into Treasury's original payment branch. `enabled=false` leaves the original method untouched.

The original loose-item comparison is `ItemStack.isSameItemSameComponents`; the addon uses that same comparison for all supported commodities. It does not use tags, names, fuzzy NBT matching or registry identity alone. Complete lots and original price-per-lot are retained. The original count limit is replaced by the bounded addon configuration for this branch only.

The original manual sell path ignores `removeMatching`'s return value. Globally mixing into InventoryUtil would affect auctions/player shops without their own transactional integration, so that approach was deliberately avoided.

The inspected client `ServerShopScreen.initCustomer` creates an active Sell All button independent of loose inventory count. Its `sellAll` sends the existing `shop_sell_all` action and position. The addon needs no client screen hook, custom packet, asset or registry entry. It is enabled only on dedicated servers; integrated singleplayer is outside this version's scope.

## Numismatics accessor Mixin

`BankAccountAccessor` targets private fields `balance:I` and `additionalBalance:J`, exposing paired readers/writers. `BankService.account(player)` resolves the ordinary player account before committing. Successful payment uses Treasury's `BankService.credit(UUID, int)`, which calls Numismatics `deposit(int)`.

Both balance fields are required: Numismatics caps its visible balance at **1,073,741,823** and stores more funds separately. Verifying only `getBalance()` could incorrectly report a valid payment as missing. Checked long arithmetic prevents overflow before payment. Restoration sets both captured values and calls `markDirty()`; `setBalance()` alone would move funds between the two fields and would not restore the exact snapshot. The account object identity is also checked; a replacement account is treated as an unresolved recovery rather than silently overwritten.

Both Mixins use `remap=false`: the mod methods/fields are not obfuscated Minecraft targets. All Minecraft descriptors are the 1.21.1 Mojang names used by NeoForge. No refmap is needed for these targets. The Mixin config is required and its injection has `require=1`; incompatible target changes should fail at startup instead of silently losing the integrity hook. Metadata pins the inspected mod versions; build-time fingerprints pin the original reference bytes.

## Transaction phases

1. Collect eligible source snapshots on the server thread. No wrappers/UUIDs are allocated and no live inventory is changed while planning.
2. Calculate the bounded quantity, rounding down to whole lots, and check payout/account arithmetic.
3. Prepare independent before/after edits on copies. A shulker's unrelated items, names, colors and components are preserved.
4. Revalidate every selected source and the account.
5. Register each edit as attempted **before** calling its write, then compare the full resulting source against the planned after-image.
6. Verify all edited sources again, then credit the account and verify its exact total.
7. On failure, compensate attempted payment first. Only after confirming that compensation, restore attempted item edits in reverse order and verify the snapshots.
8. Refresh inventory/shop views outside settlement; a screen refresh failure never reverses a completed sale.

Repeated/replayed requests must remove another real set of items to receive another payment. The per-request limit can leave additional eligible stock for subsequent clicks. No request-count estimate is reused between requests.

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

## Failure and recovery limits

Ordinary RuntimeExceptions in writes or payment are compensated synchronously. The transaction coordinator's injected-failure tests cover a write that throws after partial change, silent failed writes, failure before and after credit, missing credit, stale plans, failed payment compensation and failed item rollback.

An uncertain account compensation never returns items. An uncertain item rollback never issues a payment. Either sets a global emergency circuit breaker for addon Sell All. The log contains `RECONCILE <UUID>` records with the player, planned quantity/payment, original and current balances and before/after source data. Loose/vanilla before-images use registry-aware item SNBT; Sophisticated records use canonical SNBT plus their UUID/slot. These records can be large and should be retained with the server logs.

For recovery: stop player activity on staging, preserve logs and a consistent copy of all affected world/player/bank data, compare the actual item state with both images and compare both account balance fields, then reconcile once. Prefer restoring one consistent pre-test backup when the outcome cannot be established. Do not blindly refund items or deposit money, and do not repeatedly replay the sale. Do not clear the pause by restarting until reconciliation is complete. Disabling the addon delegates to Treasury's original Sell All and is not a recovery procedure.

There is no cross-file fsync/atomic-save guarantee, write-ahead journal, process-crash replay or protection against arbitrary third-party mods rewriting inventory/bank state off the server thread. Fatal JVM errors, disk failures and power loss are outside the recoverable RuntimeException model. This limitation is explicit; a stronger durability guarantee would require a separate persistence design and acceptance review.

## Performance

No tick/event scanning or count cache. A request scans at most the vanilla inventory slots and at most 256 entries per supported shulker, plus a maximum 16 upgrade entries. Duplicate UUID detection is local to that request. Counts use long arithmetic; removal/payment use checked positive ints. A single compound write replaces a supported canonical shulker record rather than looping normal-stack extractions. Staging profiling must include many full boxes and large component payloads; a quantity limit does not itself bound the byte size of existing item components.
