# Staging acceptance checklist

Prepared **25 November 2026**. Architect/acceptance tester: Ben. **Nothing in this checklist authorizes a production deployment.** Record the actual execution date, modpack/loader versions, configuration and candidate JAR SHA-256 with results.

## Setup and launch gates

- [ ] Use an isolated dedicated staging server and a consistent backup containing world data, player data and Numismatics bank data.
- [ ] Confirm Java 21, Minecraft 1.21.1, the four exact reference JARs, Create 6.0.10 and its normal dependencies. Record the NeoForge build; candidate compilation used 21.1.265.
- [ ] Open the project in a writable environment and run `./gradlew --offline build` (or resolve missing inputs online). Resolve any Gradle task/configuration failures before treating the normal development build as validated.
- [ ] Add only the addon JAR to staging. Confirm no Mixin, accessor, dependency-version or config errors at startup.
- [ ] Join with the normal modpack client **without** Bulk Commerce. Confirm no handshake or menu failure.
- [ ] Confirm `foundrybulkcommerce-server.toml` defaults and record the limit. Keep `2304` for baseline tests, then raise deliberately.
- [ ] Keep one known Treasury BUY_FROM_PLAYER test shop and record template components, price and lot size.

## Baseline and exact matching

- [ ] Sell loose matching items; inventory loss equals quantity, payment equals complete lots × existing price.
- [ ] Sell with no matching stock: no payment or item mutation.
- [ ] Test ordinary stone, a differently named/component-bearing stack and a shop configured to a named/component-bearing template. Only exact matches sell.
- [ ] Confirm purchases, manual quantity sales, player shops and auctions retain their original behaviour.
- [ ] Confirm shop module disabled, unconfigured shop, wrong shop mode, wrong block/position, out-of-range player and malformed request cases fail through Treasury's original checks without payment.

## Vanilla shulkers

- [ ] Sell with zero loose items and all stock inside a vanilla shulker. The existing Sell All button should work.
- [ ] Mix loose stock with several colored/named shulkers; matching stacks span multiple inner slots.
- [ ] Verify all box names/colors/components and unrelated contents remain unchanged.
- [ ] Verify a box carried in the offhand is included; offhand loose stock is not.
- [ ] Incomplete lots remain untouched. Check one lot assembled from several boxes plus loose stock.
- [ ] Empty boxes and unsupported container templates issue no payment. Nested containers/backpacks are not opened or consumed as matching commodities.
- [ ] Reopen the shulker, move it, place/break it and reconnect. Confirm client/server counts agree.

## Sophisticated Storage (critical)

- [ ] Test actual wood/copper/iron/gold/diamond/netherite shulkers available in the pack, both without upgrades and with each supported standard stack tier/combination, including omega.
- [ ] Test real counts above 64, 128 and 2304; verify reductions at a slot boundary and inside a very large stack.
- [ ] Mix vanilla, Sophisticated and loose stock in one complete-lot sale.
- [ ] Confirm original UUID, tier, upgrades, slot settings, cosmetic components and unrelated items persist.
- [ ] Prime tooltip/stash wrapper state before selling, then stash/open/place/break the box after selling. Confirm stale wrapper contents cannot reappear.
- [ ] A shulker with any non-stack upgrade is skipped, even if that upgrade is disabled. Confirm zero mutation of that box.
- [ ] Linked/ender endpoint shulkers, creative/infinite/compacting partitions, packed other storage and backpacks are skipped.
- [ ] Modern UUID-less empty boxes do not acquire new UUIDs or saved records from a sale attempt.
- [ ] Legacy/embedded custom-data shulkers are skipped. Migrate via the mod's normal opening/placement flow, then retest if they become eligible.
- [ ] Inventories above 256 slots are skipped with clear feedback.

## UUID alias and repeated-request tests (throwaway data only)

- [ ] On an isolated test copy, create two carried Sophisticated items with the same STORAGE_UUID. Both aliases must be excluded; no duplicate counting/payment. Include another unique box to verify it remains independently usable.
- [ ] Put an alias in the offhand and repeat; repeat with an unsupported carried item sharing the UUID.
- [ ] Move an alias to a second player. Sell once through each player in sequence: the second can sell only the remaining **canonical** contents, never an old cached count.
- [ ] Test a carried alias while another copy is placed. Missing canonical contents must be skipped; verify no data resurrection.
- [ ] Rapidly confirm multiple Sell All requests. Every positive payment must correspond to another actual removal. Once complete lots are exhausted, payment stops.

## Bounds and balances

- [ ] Limit below lot size: no sale. At exactly one lot: exactly that lot. Nonmultiple limit: round down. Above limit: remainder retained.
- [ ] Raise limit above 2304, then test multiple complete 2304-item lots and large stack-upgraded counts. Reduce limit afterward.
- [ ] Overflow payout multiplication: reject before any item mutation.
- [ ] With a test account near the 1,073,741,823 visible-balance cap, sell across the threshold and inspect standard **plus** additional balance. Verify exact total increase.
- [ ] With a near-long-overflow test reserve, reject before mutation. Use only disposable bank data.

## Failure/recovery and persistence

- [ ] Run all executable test suites and review their failure-injection coverage before live acceptance.
- [ ] If live adapter/payment fault injection is required, use a separate development-only harness on a disposable world; the shipped addon intentionally exposes no failure command. Verify failed/partial item writes and failed credit return exact before-images where compensation succeeds.
- [ ] Verify uncertain payment compensation never refunds items, and uncertain item restoration never pays. Confirm bulk selling pauses and RECONCILE records retain the transaction/player/UUID/balance/snapshot evidence.
- [ ] Reconcile one failure once using the recorded images and actual state; do not restart merely to bypass the pause.
- [ ] Complete a successful mixed sale, perform an ordinary save/clean restart and verify player items, canonical Sophisticated contents and both account fields agree.
- [ ] Acknowledge the documented cross-file crash-durability limitation. If crash-atomic commerce is an acceptance requirement, stop and redesign persistence before approving this version.

## Performance and sign-off

- [ ] Profile the existing server without sales: confirm no addon periodic scans/tick listeners.
- [ ] Profile Sell All with one and many full shulkers, several players and large component-bearing stacks. Record request duration/MSPT and adjust the limit.
- [ ] Test `vanillaShulkers=false`, `sophisticatedShulkers=false` and `enabled=false`; note the last restores Treasury's original Sell All implementation.
- [ ] On a separate compatible minimal server, omit Sophisticated Storage/Core and verify vanilla-only startup and selling.
- [ ] Keep staging evidence, exact candidate hash and any unresolved limitations. Architect decides acceptance. Production deployment is a separate action.
