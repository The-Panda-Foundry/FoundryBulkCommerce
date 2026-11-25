# v0.2.1 dedicated-server final validation

Prepared **25 November 2026**. No production deployment is authorized or performed. The architect reports initial v0.2.0 dedicated functional success; full host persistence/concurrency/performance/crash qualification and v0.2.1 notification acceptance remain outstanding. Automated headless tests are not substitutes for these checks.

## Preparation and client compatibility

- [ ] Back up world/player files, both Sophisticated SavedData repositories, Numismatics and ledger consistently. Retain the accepted v0.2.0 JAR and Git tag baseline/v0.2.0. Use separate staging files on persistent local disk with console access. Preserve an existing ledger binding; see INSTALLATION_AND_ROLLBACK.md before assigning a new ID to a copied ledger.
- [ ] Use Java 21, Minecraft 1.21.1 and NeoForge 21.1.256/full modpack. Install only foundrybulkcommerce-0.2.1.jar on the server alongside the unchanged exact references. Check Linux native SQLite loading, startup hashes, commands and healthy admission. Development runtime here was 21.1.265; exact 21.1.256 must be verified.
- [ ] Connect a normal modpack client WITHOUT Bulk Commerce. No missing-registry/channel/config negotiation should occur. Use the existing Treasury Sell All UI and confirm the server handles it. Also connect with the addon on a client if desired; no client data is trusted for eligibility.
- [ ] Check ordinary player access to permitted shops, denial for prohibited shops, and OP-level-two/console read-only commands. Verify instance ID and recent/player/inspect/unresolved output.

## Notification acceptance

- [ ] With showTransactionMessages=false and debugTransactions=false, preparation/success and their references do not appear in chat; exact extraction/payment, ledger rows and UI refresh still occur.
- [ ] Cancellation, insufficient stock, unsupported/duplicate-container warnings, busy/disabled/recovery and critical containment remain visible. Quiet mode is not a blanket feedback mute.
- [ ] Enable showTransactionMessages=true to verify opt-in references; independently enable debugTransactions while chat remains off. Read-only recent/player/inspect must remain usable in all combinations.

## Economic and eligibility checks

- [ ] Execute the full INTEGRATED_ACCEPTANCE.md matrix on the dedicated server: all nine hotbar slots, six Storage tiers, six Backpacks tiers, vanilla colours, supported modifiers including Omega, same/mixed tiers, unrelated metadata and nested-item preservation.
- [ ] Place identical matching resources inside hotbar, main inventory, offhand and equipped containers. Sell All removes only hotbar contents plus ordinary loose stock. Main/offhand/armour/equipped/cosmetic container contents remain byte-for-byte unchanged.
- [ ] Exercise same-repository UUID aliases between hotbar and every protected location (Curios/Accessories active and cosmetic, cursor). Test independent equal UUID values across the two repositories. Do not weaken duplicate rejection.
- [ ] At 2,304 wheat / 1 Spur verify the 8,448-wheat Netherite example outcomes (1–4 shulkers: 3/7/11/14 Spurs and 1,536/768/0/1,536 wheat remaining) in one request each. Stock above the 230,400 configured ceiling and above signed-int totals must sell only complete lots within that ceiling; repeat and inspect exact remainder/reference.
- [ ] Verify insufficient stock, bundle larger than limit, exact item/components, checked payment/account overflow, unsupported upgrades/linked/legacy layouts, scan/snapshot refusal and no late payment after rejection. Defaults/work limits must stay enforced.
- [ ] Move/replace hotbar containers and introduce protected aliases while intent is pending; expect cancellation without extraction/payment. Retest shop price/permissions/account/dimension changes and disconnects. Use staging-only latency/fault controls if required.
- [ ] Open/reopen wrappers, place/break shulkers, orderly restart, then verify player data, both canonical repositories, bank fields and ledger records remain consistent. Confirm backpacks/settings/upgrades survive independently of the addon cache.
- [ ] Repeat with Backpacks absent; repeat with optional equipment APIs absent. Test the actual installed equipment bridge/configuration; unknown providers or mirrored bridges are not certified.

## Load and recovery gates

- [ ] Interleave two and several real players' Sell All requests, repeated clicks and other normal modpack activity. One global transaction remains active; busy requests must cause no mutation/payment and must not be automatically replayed.
- [ ] Measure normal/full-load MSPT, preparation and settlement duration, request latency p95/p99, heap/GC and SQLite FULL-commit latency/disk/WAL growth. Include component-heavy nonmatches, nine eligible containers, protected inventories, saves and Omega stacks. Record measurements before changing limits; local wheat microbenchmarks are not host capacity estimates.
- [ ] Disposable fault tests: intent/outcome SQL failure, locked/full/corrupt disk, JVM kills before/after intent, extraction, payment and outcome. Confirm persistent breaker/login containment survives restart and no refund/replay occurs. These tests remain unverified at OS/power-loss boundaries.
- [ ] Perform one conservative offline manual reconciliation from RECOVERY.md with evidence/operator/reason. Ensure ordinary restart cannot erase unresolved flags. Do not bypass containment by deleting the ledger/removing the addon.

Record exact versions/hashes, before/after quantities and both bank fields, ledger references, timings and any deviations. Architect acceptance is required before any later production deployment.

Use RELEASE_0_2_1.md for the ordered final persistence/concurrency/performance/crash protocol and INSTALLATION_AND_ROLLBACK.md before replacing or restoring any files. No release artifact authorizes production deployment.
