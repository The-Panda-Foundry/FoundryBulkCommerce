# Sophisticated dependency assessment — 26 November 2026

Target confirmed by architect: local Prism instance The Panda Foundry 2.1.0. The versions initially listed in the brief are the old builds' displayed metadata versions; their JARs in The Panda Foundry2.0/Beta pack still match the previously validated hashes exactly. No change would have been needed for that old set. The confirmed target is genuinely different.

## Exact archive comparison

| Mod | Old full build / displayed | New full build / displayed | Old SHA-256 | New SHA-256 |
| --- | --- | --- | --- | --- |
| Core | 1.5.7.2381 /1.5.7 | 1.5.43.2504 /1.5.43 | 33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39 | 6273cfa446b338f165a9c799ddd78fcb320fad0b519a70d969165b7518d3bac8 |
| Storage | 1.6.2.2159 /1.6.2 | 1.6.22.2244 /1.6.22 | bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69 | e11e7bbb8c4e4acf8b34bb0133c82d95c4e9c5dd843c46c21c8a482d9e40d80f |
| Backpacks | 3.26.9.2195 /3.26.9 | 3.26.23.2263 /3.26.23 | de4a51eb3757ee273b56a41620d6598cd3af8578a5a02c4b5a784e42ab4bee2b | 8df6a8ebad2aebc71bc54e51eddd47fe931ed8bc338f7d1924e272ac72e38092 |

Sources were read-only original archives under Prism instances/The Panda Foundry2.0/minecraft/mods and The Panda Foundry2.1.0/minecraft/mods. Original Core/Storage references also exist under Developer/FoundryBulkCommerce-source/dependencies. Requested Developer/FoundryBulkCommerce does not exist; the two existing maintained Git checkouts in this task workspace are reused, not duplicated. The fixture repository is output/foundry-commerce-fixtures.

## Pre-fingerprint API and behaviour assessment

The exact selected classes were compared byte-for-byte, public signatures checked and changed methods decompiled read-only before adding replacement hashes. No dependency source/classes are redistributed.

- ModCoreDataComponents, ItemContentsStorage, StackStorageWrapper and BackpackStorage class bytes are unchanged. Modern UUID component, independent overworld repositories, full canonical get/set semantics and SavedData formats used by the addons are retained. Storage root remains storageWrapper.contents; Backpacks root remains direct inventory/upgradeInventory/settings. No ledger schema/data migration is indicated.
- InventoryHandler insertion/save/slot-capacity paths used by fixtures are retained. Changes add batch slot updates/listener removal and correct tracking. Its stack-based extractItem now routes through the ordinary extraction method; Bulk Commerce does not call that path, instead retaining verified bounded canonical before/after edits. No extraction/payment algorithm change is proposed.
- StackUpgradeItem.getStackSizeMultiplier/getInventorySlotLimit and finite capped item arithmetic remain; new compatibility checks include supported content types/minimum multipliers. Exact passive modifier class/wrapper remains multiplier-only for supported item layouts. All other/processing upgrades remain excluded by Bulk Commerce. Fixture insertion must still use native validation; tests must establish actual accepted defaults/tiers/counts.
- StorageWrapperRepository adds per-thread-group UUID caches and refresh/removal methods. Existing item-reference lookup APIs used for conservative cache invalidation remain. Backpacks onContentsNbtUpdated additionally deconstructs processing handlers, notifies callbacks and refreshes UUID wrappers. Supported passive-only edits still require full canonical after/rollback equality; runtime regression is required to verify callbacks do not alter evidence.
- BackpackWrapper retains all constructor/fromStack/fromExistingData/UUID/inventory/upgrade APIs used here. Additional detached-layout/fluid APIs, cache refresh and configured slot-number handling do not require addon execution changes. IBackpackWrapper removes a covariant settings declaration; neither addon relies on that removed declaration. Binary execution on both supported sets must be tested before retaining old compatibility.
- Native StorageContentsPayload and BackpackContentsPayload response codecs/classes are unchanged. Fixtures use these existing server-to-client responses. New tooltip request records add ItemStackReference and ownership/location validation; neither addon constructs those request packets. Clients should run Sophisticated dependencies matching the server's cohort; server-only addon compatibility does not promise old/new Sophisticated networking interoperability.
- Nested/inception, linked, fluid/tank/creative/processing layouts remain outside supported commerce. No new capability/remote traversal or native upgrade-rule bypass is authorized.

Static assessment: no substantial transaction/storage execution change identified as necessary. Candidate exact fingerprints may now be added for runtime testing; compatibility remains provisional until both cohorts pass regression. Any native callback/schema failure requiring substantial handling changes must be reported rather than silently refactored.

## Explicit maintenance boundaries

Retain exact old/new dependency cohorts, not arbitrary mixed builds or displayed-version-only acceptance. Keep original economy/Create/equipment libraries unchanged. Pure fingerprint/cohort tests must reject unknown hashes and unvalidated mixtures. Fresh bulkTransactionLimit default is updated to the explicitly requested230400; existing saved world settings are not rewritten. Preserve the actual existing key debugTransactions=false and showTransactionMessages=false; transactionDebug is a diagnostic label, not a configuration key. Do not rename/reset deployed config or ledger/instance identity.

The existing fixture utility has no separate vanilla-shulker creation command; it creates Sophisticated Netherite shulkers and selectable backpacks, while Bulk Commerce's existing tests cover vanilla shulkers. Maintenance will preserve that actual baseline rather than introduce a new fixture feature.

Runtime results, archive comparison, installation/rollback and outstanding validation are appended after testing. No deployment is authorized or performed.


## Unchanged companion references

All following archives in the old/new local instances are byte-for-byte identical; no other dependency was updated by these projects.

| Archive | SHA-256 (both instances) |
| --- | --- |
| create-1.21.1-6.0.10.jar | ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a |
| CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar | d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767 |
| numismatics_treasury-1.0.2.jar | fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2 |
| curios-neoforge-9.5.1+1.21.1.jar | a45df2125c26219974aba7507ffc9afe7b83acc941a386af3faacb1cc0056fde |
| accessories-neoforge-1.1.0-beta.53+1.21.1.jar | 10017a3da78ea63e9ece27a1ca32f8cf490362f348778cf8cb759e7282f3beb0 |
| owo-lib-neoforge-0.12.15.5-beta.1+1.21.jar | de6ed336bd80154b7241a7b3276694befc1c94550add8bcdfe7f82e5172fd13d |

## Completed automated validation

| Configuration | Bulk Commerce0.2.2 | Fixtures0.2.1 |
| --- | --- | --- |
| New complete cohort | Clean Java21 Gradle build;2424 executable checks;10 actual required NeoForge GameTests pass | Clean Java21 Gradle build;4 actual required GameTests pass;7356 explicitly counted native fixture/inspection assertions plus real-sale assertions |
| Old complete cohort | Same new-compiled classes/JAR, compilation excluded;2424 checks and10 required GameTests pass | Same new-compiled classes/JAR, compilation excluded;4 required GameTests pass |
| New cohort with Backpacks absent |10 harness cases pass:5 active base/notification cases,5 guarded/skipped optional cases; Curios/Accessories also absent |3 harness cases pass:2 active shulker/inspection cases (994 assertions), backpack matrix guarded/skipped; production integration excluded |
| Deliberately mixed compile inputs | Exact cohort task rejects before compile/mutation (expected negative result) | Exact cohort task rejects before compile (expected negative result) |

The38 compatibility checks include all8 old/new complete-family permutations (only2 homogeneous sets accepted), unknown same-version hashes, optional-family subsets and defaults230400/quiet false. Existing transaction2327, SQLite27, vanilla19 and schema13 checks remain. Existing matrices cover six shulker tiers/ten passive modifiers and six backpack tiers/nine passive modifiers, representative combinations, huge stock, partial remainder, protected positions, duplicate UUIDs, metadata, verified extraction/payment/compensation and persistent ledger/breaker behaviour.

The fixture integration loads the separately compiled Bulk Commerce0.2.2 JAR, not its source build. Each cohort executes two real asynchronous Treasury/SQLite sales from2,147,483,648 wheat: total460,800 removed,200Spurs paid,2,147,022,848 remaining; unique ledger outcomes and exact inspection/first-stack counts verified. Native payload codec and compressed SavedData disk round trips pass on both cohorts. Standalone optional-absent fixture execution does not load Bulk Commerce. No fixture code is added to the production addon.

The persistent test ledger was opened read-only after upgrade runs: schema1, clean_stop1, breaker0, and historical transactions from0.1.0/0.1.1/0.1.2/0.2.0/0.2.1 remained alongside0.2.2 records. No schema migration, history reset/delete or server-instance rewrite was introduced. This is test-world evidence, not inspection of production financial data.

## Binary scope verification

Against accepted0.2.1, only BulkConfig (explicitly requested fresh default), RuntimeCompatibility (exact cohorts), FoundryBulkCommerce (version), and BulkSales (inlined addonVersion evidence constant) class bytes differ. All transaction, wallet, storage adapter, ledger and Mixin classes are byte-identical. BulkSales source was not modified. SQLite nested bytes remain unchanged. Against fixtures0.2.0, all12 fixture class files are byte-identical: only mod metadata/build compatibility and version change. Neither release bundles original Sophisticated JARs; the production archive contains no Commerce Fixtures/GameTest classes.

## Runtime qualification and outstanding live tests

Automated tests ran actual Minecraft1.21.1 / NeoForge21.1.265 headless dedicated GameTestServer processes with Temurin21.0.12/macOS ARM64, Gradle9.2.1/ModDevGradle2.0.148. They execute actual mods, Mixins, native storage, native SQLite and command/notification paths; they are not human gameplay, remote-client or Linux hosting acceptance. The same binaries were exercised with both cohorts using standard Gradle runtime tasks and -x compileJava/compileTestJava/compileGameTestJava where stated; exact reference-verification tasks still ran. No fallback compiler/hand packaging was used.

The existing integrated-client harness was attempted against the new set in a separate copied disposable world under /tmp. macOS hiservices/LaunchServices failed during early-window initialization (Connection Invalid/_LSModifyNotification error-50); the attempt was interrupted before addon/game acceptance. This is an environment blocker, not a passed integrated test. No production world/client instance was changed by this attempt.

Remaining validation: target full-pack integrated and dedicated player connections without the addons installed on clients, matching new Sophisticated client/server networking, graphical native tooltip/UI after creation/sale, normal save/restart consistency, concurrent real players/MSPT/disk contention and disposable crash/IO/manual reconciliation. Old0.2.1 live acceptance reported by the architect is not automatically new-dependency acceptance. Core's existing int compacted-tooltip overflow remains independently reproduced; exact long info/canonical slots stay valid. Unknown equipment providers/mirrored bridges and out-of-scope layouts remain unverified.

No guarantee of atomic persistence across Minecraft inventories, Sophisticated repositories and Numismatics is added. The local durable intent/outcome ledger, conservative dirty-boot flags, persistent containment and manual-only reconciliation remain. No automatic replay/refund, remote synchronisation, additional integration, new command/interface or crash fix is implemented. No production deployment.
