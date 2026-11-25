# v0.2.0 / v0.2.1 storage compatibility matrix

The v0.2.1 adapters are byte-identical to accepted v0.2.0; the matrix below is unchanged. Final release validation status is in RELEASE_0_2_1.md.

Verified **25 November 2026**, against the exact reference archives. Production uses namespace + actual ShulkerBoxItem/BackpackItem APIs, not a fixed tier allowlist. Capacities are API defaults from the test configuration; modern stored capacity components are supported within the existing work/byte limits. Changes to dependency bytes are rejected until reviewed.

## Containers

All rows require a single outer item in hotbar indices 0–8 and valid finite canonical data. Loose stock still follows Treasury's ordinary slots 0–35. Every other container location is protected, irrespective of selected hotbar slot.

| Integration | Item (integration namespace) | Default inventory slots | Default upgrade slots | Automated coverage |
| --- | --- | ---: | ---: | --- |
| Vanilla | All vanilla shulker colours | 27 | — | Component edits/preservation, all nine hotbar slots, protected locations |
| Storage | shulker_box | 27 | 1 | All ten passive modifiers |
| Storage | copper_shulker_box | 45 | 1 | All ten passive modifiers |
| Storage | iron_shulker_box | 54 | 2 | All ten passive modifiers |
| Storage | gold_shulker_box | 81 | 3 | All ten passive modifiers |
| Storage | diamond_shulker_box | 108 | 4 | All ten passive modifiers |
| Storage | netherite_shulker_box | 132 | 5 | All ten passive modifiers |
| Backpacks | backpack | 27 | 1 | All nine passive modifiers |
| Backpacks | copper_backpack | 45 | 1 | All nine passive modifiers |
| Backpacks | iron_backpack | 54 | 2 | All nine passive modifiers |
| Backpacks | gold_backpack | 81 | 3 | All nine passive modifiers |
| Backpacks | diamond_backpack | 108 | 5 | All nine passive modifiers |
| Backpacks | netherite_backpack | 120 | 7 | All nine passive modifiers |

The runtime matrices cover empty/full canonical storage, partial extraction, unrelated contents, metadata/upgrades, full-image compensation and duplicate identities. Representative multi-upgrade combinations and repeated Omega are tested where capacity permits. Existing Storage tests also exercise modern saved capacity of 300 slots. These are automated headless tests; they do not certify every custom server capacity setting or every modifier permutation.

## Passive modifiers

Accept only registered exact Core StackUpgradeItem instances in the corresponding integration namespace, count one, with a positive finite API multiplier. No subclass or arbitrary processing hooks are trusted. Multiple legitimate passive modifiers are supported within the API upgrade capacity; the addon preserves existing installation state and never installs/bypasses dependency upgrade rules.

| Item suffix | Storage multiplier | Backpacks multiplier |
| --- | ---: | ---: |
| stack_upgrade_starter_tier | — | 1.5 |
| stack_upgrade_tier_1 | 2 | 2 |
| stack_upgrade_tier_1_plus | 3 | — |
| stack_upgrade_tier_2 | 4 | 4 |
| stack_upgrade_tier_3 | 8 | 8 |
| stack_upgrade_tier_4 | 16 | 16 |
| stack_upgrade_tier_5 | 32 | — |
| stack_upgrade_omega_tier | 2,147,483,647 | 2,147,483,647 |
| stack_downgrade_tier_1 | 1/8 | 1/8 |
| stack_downgrade_tier_2 | 1/16 | 1/16 |
| stack_downgrade_tier_3 | 1/32 | 1/32 |

Omega is a finite modifier, distinct from infinite/creative partitions. A stored slot count is a positive signed int; combined source totals are checked longs. Runtime tests use Integer.MAX_VALUE wheat per slot and combined totals beyond that boundary. They exercise transaction limits without iterating individual wheat items. Canonical counts are not normalised through the vanilla stack codec: components decode at count one, then the original finite count is restored for exact matching.

## Protected and unsupported configurations

No extraction from main inventory outside hotbar, armour, offhand, equipped Curios/Accessories, cursor, nested containers, placed blocks or remote/linked storage. UUID inspection across supported external locations does not decode those containers' inventories. No recursive inspection is performed.

Reject stacked outer containers, custom/legacy embedded storage, missing/malformed canonical data, unsupported namespace/class, non-default partitions, all non-stack upgrades (including disabled ones), and Backpack loot/template state or nonzero COLUMNS_TAKEN. Tank/processing layouts that occupy columns therefore remain excluded. Upgrades introducing processing hooks cannot be made rollback-safe merely by disabling them. Conversion crafting items are not passive installed stack modifiers. Chest/barrel selling remains outside scope.

Backpack settings, upgrades and all unrelated canonical keys are copied and verified. A schema/layout not proven safe is rejected without migration. Protected unsafe containers do not prevent unrelated ordinary stock selling unless they introduce duplicate identities or exceed identity work budgets.

## Repository identities and optional equipment

Storage: overworld SavedData sophisticatedstorage, root storageWrapper.contents. Backpacks: independent overworld SavedData sophisticatedbackpacks, direct contents root. Both outer items use Core STORAGE_UUID. Identity keys are (repository, UUID); equal UUID values in different repositories are independent and tested. Any same-repository duplicate is rejected even when the other occurrence is outside the hotbar.

Supported identity locations: all 41 physical vanilla slots, current menu cursor, Curios active and cosmetic slots, Accessories active and cosmetic slots. Core-UUID items in unknown namespaces conservatively reserve the UUID in both repositories. Other equipment-provider extensions are unverified. Bridges mirroring a single physical slot through both APIs may cause conservative duplicate rejection; do not disable duplicate protection to work around an unsupported bridge.

Backpacks, Curios and Accessories are optional runtime integrations, each lazily accessed in separate guarded classes and exact-byte fingerprinted when installed. Backpacks absent with Curios/Accessories absent is runtime-tested; existing shulkers remain operational. Both equipment APIs present is also runtime-tested. Storage/Core absent together and Backpacks-only combinations are supported by guarded design but not exercised by this release's runtime suite.

## Diagnostics

Detailed rejection lines require debugTransactions=true. Codes remain UNSUPPORTED_STORAGE, DUPLICATE_UUID, UNSUPPORTED_UPGRADE, INVALID_DATA, SCAN_LIMIT, SNAPSHOT_LIMIT, INSUFFICIENT_STOCK and VALIDATION_FAILURE. Unsafe sources are excluded; aggregate work/serialization overflow aborts the whole preparation. Settlement uncertainty triggers persistent containment rather than an ordinary rejection. Do not infer a material-tier defect from a generic insufficient-stock result.

Historical BC-001 investigation and v0.1.2 matrix evidence remain in SHULKER_COMPATIBILITY.md. The architect reports substantial successful v0.1.2 integrated acceptance, including Omega and totals above int range. New v0.2.0 full-pack and dedicated acceptance remain outstanding.
