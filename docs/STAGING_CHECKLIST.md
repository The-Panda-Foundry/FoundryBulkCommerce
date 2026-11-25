# v0.1.2 dedicated-server staging acceptance

Prepared **25 November 2026**. Use a disposable consistent copy and keep console access. No production deployment. Automated results and the architect's v0.1.1 integrated tests are documented separately; all dedicated boxes below remain acceptance work.

## Preparation and connection

- [ ] Back up world/player data, Numismatics, Sophisticated canonical storage and ledger consistently. Retain v0.1.1/tag baseline/v0.1.1 for comparison. Install only the v0.1.2 addon JAR; leave original four dependencies unchanged.
- [ ] Use Java 21, Minecraft 1.21.1 and staging NeoForge 21.1.256 with Create dependencies/full modpack. Confirm initialization, exact dependency hashes, Linux SQLite native loading and healthy ledger diagnostics. Full automated runtime here used 21.1.265; exact staging loader still needs this check.
- [ ] Connect an ordinary full-pack client **without Bulk Commerce installed**. Open the unchanged Treasury interface, execute Sell All and inspect outcome. Also retest integrated hosting with the candidate installed locally.
- [ ] Give the staging world a distinct instance/ledger identity before first use; do not casually clone/reset a production journal. Verify level-two/console audit commands and denial to unprivileged players.

## Transaction acceptance

- [ ] Use a BUY_FROM_PLAYER shop at 2,304 wheat / 1 Spur. For these test cases set bulkTransactionLimit=65536, keeping maxScannedSlots=4096 and maxSnapshotBytes=1048576. Restore approved operational values afterward.

| Netherite boxes | Wheat before | Expected Spurs | Wheat after | Passed / references |
| --- | ---: | ---: | ---: | --- |
| 1 | 8,448 | 3 | 1,536 | |
| 2 | 16,896 | 7 | 768 | |
| 3 | 25,344 | 11 | 0 | |
| 4 | 33,792 | 14 | 1,536 | |

Each row must be one Sell All request and one successful ledger transaction. Verify both bank balance fields, all remaining stock and unique transaction IDs.

- [ ] Test base/copper/iron/gold/diamond/Netherite, empty/partial/full, every available passive upgrade tier and representative allowed combinations. Preserve unrelated items, names/colors/components and installed upgrade bytes. See SHULKER_COMPATIBILITY.md for the complete matrix; do not assume non-stack upgrades are supported.
- [ ] Combine different tiers, multiple same-tier boxes, vanilla boxes (including offhand) and loose stock. Confirm exact item/component matching and unchanged shop bundle configuration/prices.
- [ ] With a lower bulkTransactionLimit, sell only the maximum complete bundles, repeat Sell All and verify the remainder remains. When no complete bundle fits, no payment or mutation should occur. Check a configured limit smaller than one bundle and payout/account overflow.
- [ ] Open/close existing Sophisticated wrappers, place/break boxes, stop/restart and verify canonical contents, vanilla component persistence, bank state and ledger history. Do not count a dirty flag as proof of cross-resource crash atomicity.

## Rejections, contention and recovery

- [ ] Enable debugTransactions temporarily. Reproduce the original Netherite rejection if possible and capture the exact reason/reference, outer item metadata/identity state and legitimate capacity. Baseline testing did not reproduce a tier-specific failure; its historical exact cause remains unknown.
- [ ] Test intentional UUID aliases: every duplicate carried identity must be rejected. Confirm stale canonical plans cannot pay twice when two players hold aliases. Never disable this protection to make a test pass.
- [ ] Confirm explicit unsupported-storage/upgrade, invalid-data, scan-limit, snapshot-limit, insufficient-stock and validation categories. Unsafe sources remain excluded; workload failures cause no partial sale. Disable debug logging after diagnosis.
- [ ] Attempt concurrent requests from two and several real players, repeated clicks, stale source/price/dimension/account changes while intent is pending, and disconnects. Verify one active transaction, no late stale mutation, duplicate payment or unexpected inventory change.
- [ ] Profile full-box/component-heavy work during normal world activity and saves. Record MSPT, response latency, heap/GC behaviour, free disk/WAL size and commit contention before increasing protective limits. Local microbenchmark results are not production measurements.
- [ ] In a disposable fault test, exercise failed intent/outcome writes, disk full/corrupt/locked ledger and kills at removal/payment/outcome boundaries. Verify persistent breaker/login containment survives restart and no automatic refund/replay occurs.
- [ ] Complete one offline manual reconciliation using RECOVERY.md; preserve evidence and actor/reason. Normal orderly restart must not erase unresolved flags. Never bypass a breaker by deleting the journal or removing the addon.

Record loader/mod versions, quantity/payment before and after, transaction references, timings and deviations. Accept for production only after architect review of these staging results; no deployment is part of this delivery.
