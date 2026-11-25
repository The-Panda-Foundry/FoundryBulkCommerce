# v0.1.2 shulker compatibility and BC-001 investigation

Recorded **25 November 2026**. Authoritative dependencies: Storage 1.6.2.2159 and Core 1.5.7.2381, exact existing SHA-256 pins. Baseline `f3a5560` is preserved by Git tag `baseline/v0.1.1`.

## What was established before production changes

A new runtime regression executed the **unchanged v0.1.1 adapter** against all six registered shulker items. All six full default containers were accepted, including Netherite's 132 slots / 8,448 wheat. Netherite therefore has no reproduced intrinsic tier rejection. Its default size is below the old 256-slot guard. The supplied live log had debugTransactions=false and only the generic preparation-rejected message, which cannot establish the original rejection category. Duplicate aliases were deliberately tested successfully and are not reclassified as a defect.

A read-only check of the local acceptance world's canonical SavedData also found a valid 132-slot / 8,448-wheat record with default finite partition and no upgrades. That record alone cannot establish the outer item/UUID aliases or request state at the time of the failed click. No world data or dependency JAR was changed. **The exact original live rejection remains unproven.** Earlier statements that Netherite itself was unsupported were too strong.

v0.1.2 improves API-based eligibility, complete modifier coverage, diagnostics and bounded preparation. It does not claim a demonstrated Netherite-specific bug fix. Its four requested Netherite sale examples are now independently verified through the actual Treasury handler, verified settlement and SQLite ledger in an automated NeoForge runtime.

## Complete registered shulker matrix

The runtime test discovers the actual item registry by `instanceof ShulkerBoxItem` and namespace, rather than enumerating a production tier allowlist. Default capacities below are measured from the supplied mod APIs/configuration, not addon constants.

| Registered item in sophisticatedstorage | Default inventory slots | Default upgrade slots | Wheat at 64/slot | Automated status |
| --- | ---: | ---: | ---: | --- |
| shulker_box | 27 | 1 | 1,728 | Passed |
| copper_shulker_box | 45 | 1 | 2,880 | Passed |
| iron_shulker_box | 54 | 2 | 3,456 | Passed |
| gold_shulker_box | 81 | 3 | 5,184 | Passed |
| diamond_shulker_box | 108 | 4 | 6,912 | Passed |
| netherite_shulker_box | 132 | 5 | 8,448 | Passed |

Every row covers UUID-less empty, canonical empty, full contents, partial removal, exact rollback, duplicate identity diagnostics, every registered passive modifier, nonmatching item preservation, upgrade/metadata preservation and malformed capacity rejection. Different tiers are also settled together with loose wheat and a vanilla shulker in one real asynchronous transaction. A modern saved 300-slot capacity is tested through the dependency wrapper and addon, exceeding the old arbitrary 256 ceiling without raising any workload setting.

## Complete passive stack modifier matrix

All of the following are registered instances of the **exact** Core `StackUpgradeItem` class in the pinned dependency. Its inspected implementation only changes finite slot multipliers. No item-ID allowlist is needed. Subclasses, unknown classes, other namespaces, nonpositive/nonfinite multipliers and non-stack processing upgrades remain excluded.

| Registered item in sophisticatedstorage | API multiplier | Automated coverage |
| --- | ---: | --- |
| stack_upgrade_tier_1 | 2 | All six shulker tiers |
| stack_upgrade_tier_1_plus | 3 | All six shulker tiers |
| stack_upgrade_tier_2 | 4 | All six shulker tiers |
| stack_upgrade_tier_3 | 8 | All six shulker tiers |
| stack_upgrade_tier_4 | 16 | All six shulker tiers |
| stack_upgrade_tier_5 | 32 | All six shulker tiers |
| stack_upgrade_omega_tier | 2,147,483,647 | All six shulker tiers |
| stack_downgrade_tier_1 | 1/8 | All six shulker tiers |
| stack_downgrade_tier_2 | 1/16 | All six shulker tiers |
| stack_downgrade_tier_3 | 1/32 | All six shulker tiers |

Core saturates finite item slot counts at Integer.MAX_VALUE where required; Omega is distinct from infinite/creative inventory partitions. Canonical counts remain positive ints and source totals are checked longs. The addon never installs upgrades or changes dependency installation rules. Existing legitimate combinations of these passive modifiers are accepted within the actual upgrade slot capacity. Mixed and repeated combinations are tested on tiers with multiple slots; every possible permutation is not enumerated. All ten single-modifier cases are tested on every tier. Downgrade cases use appropriately reduced stack counts. Non-stack upgrades (including disabled ones), non-default/infinite partitions, linked/legacy/custom-data containers, backpacks, chests and barrels remain outside supported scope.

## Capacity and workload validation

Use non-mutating `StorageBlockItem.getDefaultNumberOfInventorySlots` / `getDefaultNumberOfUpgradeSlots` plus modern stored slot-count components. The dependency's getNumberOf... APIs write components and are deliberately not called during addon planning. Canonical inventory/upgrade sizes and entry slots must fit these API capacities. Historical smaller serialized inventories can remain valid when defaults have increased. A modern saved capacity larger than the tier default is supported, within configured scan/snapshot budgets.

Each serialized inventory item is decoded once to validate and identify exact item/component matches. The immutable matching plan records entry indexes/counts, which are reused when constructing the copied after-image. No per-item extraction loop or processing upgrade hook is invoked. Full outer/canonical before and after checks, existing-wrapper invalidation, aliases, compensation, ledger and admission controls are retained.

The fixed 256-inventory-slot and 16-upgrade-entry assumptions have been replaced by API capacity validation plus the existing aggregate scan/byte budgets. Defaults/ranges of protective settings are unchanged. A container exceeding the configured scan-capacity ceiling or a full request exceeding inspection/byte budgets is refused before any economic mutation. It is not labelled an unsupported material tier. More items than bulkTransactionLimit alone never rejects an otherwise eligible box: only complete bundles within the limit are removed.

## Rejection diagnosis

`debugTransactions=false` remains the default. Detailed per-source lines appear only when enabled; they include transaction reference, vanilla inventory slot, item registry ID and reason, never full snapshots. Player preparation failures receive a bounded category and reference even with debug off.

| Code | Meaning |
| --- | --- |
| UNSUPPORTED_STORAGE | Non-shulker, unverified namespace, stacked outer container, linked/legacy/custom-data storage, non-default/infinite partition |
| DUPLICATE_UUID | Multiple carried items share the same storage identity; every alias excluded |
| UNSUPPORTED_UPGRADE | Other processing upgrade/class or invalid passive multiplier |
| INVALID_DATA | Missing/malformed canonical data, invalid item/count/list/slot, API capacity mismatch or duplicate serialized slot |
| SCAN_LIMIT | One container's inventory capacity or aggregate outer/entry inspections exceed maxScannedSlots |
| SNAPSHOT_LIMIT | Scanned binary NBT or final serialized intent exceeds maxSnapshotBytes |
| INSUFFICIENT_STOCK | Eligible matching stock does not make a complete shop bundle |
| VALIDATION_FAILURE | Transaction/bundle-limit mismatch, checked payout/account arithmetic or other preparation validation |

Unsafe sources are skipped; remaining eligible stock can sell. Scan/snapshot budget failures abort the whole preparation. A later insufficient-stock message may follow detailed source-specific rejection lines when every source was excluded. Settlement uncertainty still triggers the persistent breaker/containment; these diagnostics never turn it into an ordinary harmless rejection.

## Acceptance boundaries

Automated tests use actual registered dependency items, wrappers, transformed Mixins, Treasury and native SQLite in a headless NeoForge 21.1.265 runtime. They are not a full-pack human gameplay test. The architect reported successful v0.1.1 integrated-server acceptance on 21.1.256, including 6,912/3 and 18,432/8 single transactions. v0.1.2 itself still needs full-pack integrated regression, Linux dedicated staging, ordinary clients without this addon, restart persistence and host-load profiling. No production deployment occurred.
