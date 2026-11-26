# BC-004: reviewed ordinary upgrades — v0.2.3

26 November 2026. Candidate only; no production deployment. Accepted source preserved as `baseline/v0.2.2`; development branch `fix/bc-004-upgrades-0.2.3`. Commerce Fixtures remains a separate, unchanged development-only project.

## Verified cause and focused correction

Both canonical adapters call `CanonicalContents.parse`. Its old `upgrade.getItem().getClass() != StackUpgradeItem.class` condition rejected every non-stack upgrade, regardless of whether it altered primary inventory semantics. BackpackSources separately rejected every nonzero `COLUMNS_TAKEN` value, excluding legitimate tank/battery layouts. These conditions reproduce the reported behaviour in both fingerprinted dependency cohorts.

`UpgradePolicy` now permits exact reviewed native item classes in the source integration's namespace, count one; finite positive exact StackUpgradeItem multipliers retain their existing validation. Unknown classes/subclasses, addon namespaces and malformed upgrades still fail closed. This is a security allowlist of implementations, not a tier/item-ID list. Registration discovery tests enumerate every native upgrade, so omissions are visible. No dependency fingerprints are relaxed.

Production still edits only the finite, canonical **primary item inventory**. No wrapper is constructed during planning, no processing/extraction/tick hook is invoked, and no nested, remote, linked or upgrade-owned inventory is scanned for sale. Native wrapper cache invalidation after a write remains in place; any resulting root mismatch fails verification. Existing primary inventory, UUID, outer-component, full canonical-root, duplicate-identity, hotbar, shop and bank revalidation remains unchanged. No new Mixin, command, configuration, worker, tick listener or network payload is added.

Recipe compacting and void upgrades can process items during normal native automation, but do not virtualise primary counts. Bulk Commerce deliberately performs an owner's manual primary-inventory sale without invoking those processors or I/O filter hooks. Filter ghost items are neither stock nor a permission mechanism for this sale. Shop permissions and exact item/component matching still apply. Native automation may change data while intent is pending; that makes the plan stale and cancels it. There is no automatic retry.

## Layout and preservation

Tank and battery upgrade items report two consumed inventory columns each through `IUpgradeItem.getInventoryColumnsTaken()`. Other reviewed implementations report zero. The backpack's saved columns must equal the checked sum of installed upgrade requirements. For the two exact supported cohorts the native formula is: 9 base columns for capacity at most81, otherwise12; rows=ceil(capacity/baseColumns); usable primary slots=capacity−rows×consumedColumns. Saved inventory size must fit that usable capacity; negative, excessive or inconsistent columns are rejected. New Core exposes InventoryLayout; old Core implements the same formula in its wrapper, so the small pinned formula preserves old-binary compatibility without migration or mutating capacity APIs. Tier/capacity defaults still come from the native item APIs and modern saved components. Shulkers reject column-consuming upgrades.

Full canonical before/after images retain every installed upgrade, configuration, filter prototype, auxiliary item, fluid, energy value and unrelated setting. Only matching primary stack counts change. Auxiliary inventories are copied as opaque state, never recursively traversed. Their bytes count toward existing snapshot limits. Changes while SQLite prepares intent cancel the plan; no stale snapshot overwrites those changes. Removal verification precedes payment; wallet-first compensation and verified source restoration remain unchanged.

## Explicit restrictions

| Family / upgrade | Reason |
| --- | --- |
| Backpacks inception | Proxies nested inventories and callbacks; recursive selling remains excluded, even if disabled |
| Storage compression | Virtual partitions and count conversion; primary counts cannot use the default finite model |
| Both infinity / survival infinity | Infinite or creative partition semantics; no finite extraction proof |
| Backpacks mob catcher / advanced mob catcher | Captured entities and blocked-slot lifecycle require a separate audit; excluded conservatively |
| New Storage1.6.22 pump / advanced pump / XP pump | Native Storage TankPump/TankXpPump implementations target fluid-tank storage, not the supported carried shulker primary-inventory domain; not whitelisted |
| Unknown classes/subclasses/namespaces, malformed upgrades/layouts | Not independently reviewed in exact pinned builds |
| Non-default partitions, linked/legacy/template/loot storage | Existing unsupported storage restrictions remain |

Backpack Core pump/XP-pump implementations, and Storage1.6.2’s corresponding Core implementations, are separately reviewed and permitted; their presence does not enable selling external inventories, fluid or experience. Restricted upgrades disqualify the container even when disabled. A container being skipped does not prevent a sale from other safe stock. Debug diagnostics retain UNSUPPORTED_UPGRADE / UNSUPPORTED_STORAGE / INVALID_DATA categories; routine messages remain suppressed by default, while meaningful failures and security notifications remain visible.

## Exact dependencies and API investigation

No original JAR was modified or redistributed. Both complete cohorts remain supported; mixtures/unknown bytes fail closed. [Exact fingerprints](DEPENDENCY_COMPATIBILITY_2026-11-26.md) are unchanged. Inspected actual upgrade implementations for insertion/extraction/slot-change hooks, processing inventory routes, wrapper refresh/deconstruction, partition/proxy behaviour and columns. Reviewed Core implementations cover pickup/filter/magnet/feeding, recipe compacting/voiding, manual/automatic cooking, crafting/stonecutting/jukebox/alchemy, tank/battery/pump/XP pump; Backpacks adds restock/deposit/refill/tool swapping/everlasting/anvil/smithing; Storage adds native recipe compacting and hopper. This is not a promise of compatibility with arbitrary configuration packs or future rebuilds.

## Regression evidence and limits

The native fixtures live only in `src/gameTest`, use real UUID allocation, `canAddUpgradeTo`, native upgrade-handler validation/insertion and native wrapper layout/resource APIs. No production fixture utility extension or fixture JAR is required. Restricted negative cases deliberately install unsafe test states and assert rejection; these are not presented as legitimate creation examples.

The upgrade matrix exercises both families without upgrades; every registered tier with a reviewed ordinary upgrade; every registered non-stack upgrade individually; three ordinary upgrades; fully populated ordinary upgrade slots; fully populated mixed Omega/ordinary slots; exact partial extraction, unrelated item preservation, root/outer/UUID equality, existing-wrapper reopening and wallet-failure compensation. It seeds filter configuration and prototypes, crafting wheat, cooking input/fuel, music discs, anvil/smithing inputs/configuration, water and energy through native APIs. Layout disagreements are rejected. Existing stack-only/tier/Omega, signed-int aggregate stock, protected locations, duplicate identities, vanilla, notification, arithmetic, rollback/breaker and ledger tests also run.

A real Treasury/Numismatics/SQLite test sells6912 shulker+223488 backpack wheat for100 Spurs, then76032 for33, leaving480. Protected main-inventory storage and upgrade-owned wheat/resources stay exact. A subsequent pending sale cancels after an auxiliary crafting-grid change; three distinct durable references contain two APPLIED_MEMORY and one CANCELLED outcomes. Injected wallet failure in the source matrix is test-only coordinator compensation, not a forced live-bank exception.

See VERIFICATION.md for final run counts and logs in the release package. These are actual headless NeoForge dedicated GameTests on macOS/Java21/21.1.265, not human GUI/full-modpack or remote-client acceptance. Target Linux host, unmodified-client connection, native automation under real player load, tooltip/UI persistence across restart and crash/manual-reconciliation gates remain mandatory staging tests. No atomic persistence across Minecraft inventories, native repositories and Numismatics bank files is guaranteed; APPLIED_MEMORY is not coordinated disk commit. Never automatically replay/refund a pending record.

The following registry matrix is generated from the completed native discovery run. PRIMARY_ONLY means the upgrade is permitted while only primary items are sold; RESTRICTED means the container is skipped. Stack modifiers retain the separately tested existing matrix.

| Native registry ID | New cohort | Old cohort |
| --- | --- | --- |
| `sophisticatedbackpacks:advanced_alchemy_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_compacting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_deposit_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_feeding_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_filter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_jukebox_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_magnet_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_mob_catcher_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedbackpacks:advanced_pickup_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_pump_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_refill_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_restock_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_tool_swapper_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:advanced_void_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:alchemy_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:anvil_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:auto_blasting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:auto_smelting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:auto_smoking_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:battery_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:blasting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:compacting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:crafting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:deposit_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:everlasting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:feeding_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:filter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:inception_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedbackpacks:infinity_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedbackpacks:jukebox_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:magnet_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:mob_catcher_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedbackpacks:pickup_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:pump_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:refill_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:restock_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:smelting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:smithing_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:smoking_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:stonecutter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:survival_infinity_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedbackpacks:tank_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:tool_swapper_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:void_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedbackpacks:xp_pump_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_alchemy_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_compacting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_feeding_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_filter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_hopper_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_jukebox_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_magnet_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_pickup_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_pump_upgrade` | RESTRICTED | PRIMARY_ONLY |
| `sophisticatedstorage:advanced_void_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:alchemy_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:auto_blasting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:auto_smelting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:auto_smoking_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:blasting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:compacting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:compression_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedstorage:crafting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:feeding_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:filter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:hopper_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:infinity_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedstorage:jukebox_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:magnet_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:pickup_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:pump_upgrade` | RESTRICTED | PRIMARY_ONLY |
| `sophisticatedstorage:smelting_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:smoking_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:stonecutter_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:survival_infinity_upgrade` | RESTRICTED | RESTRICTED |
| `sophisticatedstorage:void_upgrade` | PRIMARY_ONLY | PRIMARY_ONLY |
| `sophisticatedstorage:xp_pump_upgrade` | RESTRICTED | PRIMARY_ONLY |
