# v0.2.0 integrated-server acceptance

Prepared **25 November 2026**. All boxes below require live acceptance; automated results are in VERIFICATION.md. Use a disposable copy and the full Panda Foundry modpack, Java 21 / NeoForge 21.1.256. Do not test failure boundaries in a valuable world.

- [ ] Preserve the accepted v0.1.2 JAR and consistent world/ledger backup. Replace only the addon with v0.2.0. Confirm one addon JAR and the exact reference mods; do not modify dependency JARs.
- [ ] Open a staging copy with a distinct serverInstanceId/ledger identity. Confirm physical CLIENT initialization, logical IntegratedServer, dependency hashes, command registration and healthy SQLite admission. With cheats/OP level 2, run /bulkcommerce status.
- [ ] Verify all nine hotbar slots are eligible regardless of selection. With matching stock only inside main-inventory, offhand, armour and equipped backpacks, Sell All must leave it untouched. Loose matching items in ordinary main inventory must still sell.
- [ ] Test every row and passive modifier in V0_2_0_COMPATIBILITY.md. Cover empty/partial/full, different and same tiers, mixtures of vanilla/Storage/Backpacks and loose items, exact component differences and unsupported upgrades. Preserve names, colours, settings, upgrades and unrelated/nested contents.
- [ ] At 2,304 wheat / 1 Spur and a limit permitting it, verify 1/2/3/4 Storage Netherite shulkers holding 8,448 wheat each pay 3/7/11/14 Spurs with 1,536/768/0/1,536 left. Keep shop bundle rules unchanged.
- [ ] Set bulkTransactionLimit=230400. Use legitimate Netherite Omega backpacks with aggregate stock above Integer.MAX_VALUE. For 2,304 wheat / 1 Spur, each full-ceiling request removes 230,400 wheat and pays 100 Spurs. Repeated requests must preserve exact remainder; quantities alone must not reject the source.
- [ ] Intentionally duplicate a same-repository UUID between hotbar and protected main/offhand/equipped locations, including cosmetic equipment. Every eligible alias must be excluded; no protected contents may be extracted. Equal UUIDs in independent Storage/Backpacks repositories are distinct identities, not same-repository aliases.
- [ ] Move a selected container outside the hotbar or replace it while preparation is pending. Introduce a protected alias during preparation. Each stale transaction must cancel with no extraction/payment and a durable reference. Native async tests prove these paths; a real click test may require a deliberate staging-only IO delay, never a production modification.
- [ ] Verify level-two/console read-only audits, denial to unprivileged players, accurate backpack source evidence and unique transaction IDs. Temporarily enable debugTransactions for rejection categories, then turn it off.
- [ ] Open existing wrappers after sale; save/close/reopen, place/break shulkers and check bank, canonical backpack/shulker contents and vanilla components against ledger outcomes.
- [ ] Start a separate staging copy without Backpacks (and its dependent mods as appropriate); vanilla and Storage selling must continue. Remove optional equipment mods in a separate copy as well.
- [ ] Verify clean world switching/shutdown and persistent emergency containment using RECOVERY.md. Dirty-boot/kill tests remain separate fault acceptance; never delete a journal or automatically replay/refund to clear a breaker.

Record exact mod/loader hashes, transaction references, balances, before/after counts and deviations. Successful v0.1.2 gameplay does not automatically certify the new v0.2.0 equipment/eligibility paths.
