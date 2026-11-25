# Integrated-server investigation and focused correction

Recorded **25 November 2026**, before implementing the correction. Baseline: `716e97c`, v0.1.0.

## Verified cause

The mod entry point uses `@Mod(..., dist = Dist.DEDICATED_SERVER)`. NeoForge filters that constructor on the physical side, so a Prism CLIENT process never registers our config, lifecycle listeners or RegisterCommandsEvent handler, even when it later starts an integrated logical server. Independently, the Mixin config puts all four hooks in `server`, which Mixin selects only in a SERVER physical environment. Fixing the entry point alone would leave the transaction/accessor/admission/shutdown hooks absent in integrated play.

The supplied Prism log lists the addon at line 577 and nested SQLite at line 726, then starts IntegratedServer at line 4675. A whole-log search finds addon discovery/resource entries but no addon constructor or ledger messages. Discovery is therefore not evidence of runtime activation. The restrictions fully explain the absent commands and unextended selling path; no transaction was attempted by the addon in this run.

## Other paths checked

- RegisterCommandsEvent registration is inside the skipped constructor. Its command tree requires permission level 2; unprivileged players cannot see/use it. Single-player acceptance should use cheats enabled (or Open to LAN with cheats) and `/bulkcommerce status`, not the bare root. Permission behaviour is retained.
- Ledger initialization is a ServerStartingEvent listener inside the same skipped constructor. The inspected IntegratedServer calls NeoForge's normal starting hook, so that lifecycle route is valid once registered. SQLite discovery does not mean a connection was opened.
- IntegratedPlayerList delegates login checks to PlayerList for ordinary admission, so the existing recovery gate remains applicable, including to the single-player owner. No owner recovery bypass is added.
- Treasury 1.0.2 ServerShopScreen.initCustomer creates the BUY_FROM_PLAYER Sell All button independently of loose item count. Its sellAll method sends `shop_sell_all` with the shop position. ServerActionHandler dispatches this to tradeShop(..., true). The ordinary quantity action button has separate enablement logic; it does not disable Sell All. No client UI patch is needed for the inspected reference.

## Focused correction plan

Load the common entry point and the four common-class Mixins on both physical distributions, while retaining server lifecycle/ServerPlayer/server-thread execution checks. Add startup, compatibility and ledger readiness diagnostics; add opt-in bounded transaction-phase logging without snapshot payloads. Keep existing safety and permission rules. Increment to 0.1.1 and test dedicated and physical CLIENT/integrated activation. Multiplayer clients must remain able to connect without installing this addon. No production deployment.

References: NeoForge 1.21.1 sides documentation, https://docs.neoforged.net/docs/1.21.1/concepts/sides/ ; cached Mixin 0.8.7 MixinConfig.prepare source (common list followed by physical CLIENT/SERVER switch); supplied Treasury JAR and cached NeoForge/Minecraft IntegratedServer, IntegratedPlayerList and ServerLoginPacketListenerImpl source.

## Implemented v0.1.1 and validation

- Removed the physical @Mod restriction and moved all four Mixins to the common `mixins` list. Dependency-side metadata now validates both distributions when installed. No networking or client assets were introduced.
- Added normal startup/command/compatibility/ledger diagnostics and `debugTransactions=false`. Enabling it logs bounded IDs, phase categories, quantities and acknowledgements at INFO; it does not log snapshots. Startup health distinguishes ready, recovery-blocked and failed states.
- Preserved SQL worker execution, FULL durable pre-mutation intent/outcomes, main-thread revalidation, verified extraction/payment, exact rollback, persistent breaker, login containment and manual reconciliation. A stopped/older integrated-world opening callback is ignored.
- Gradle build passed. Five suites: transaction 2,327; reference/side compatibility 20; native SQLite 27; vanilla 16; Sophisticated schema 13 — **2,403 checks**. The transformed dedicated GameTest passed, including mixed 256-item/12-Spur settlement and a stale 64-item cancellation, with phase logging enabled in the test fixture.
- Actual physical CLIENT quick-play launch was attempted using a copied disposable world. The host rejected macOS graphical-session service access at the early-window stage, before addon loading. The attempt was interrupted. This is a testing blocker, not evidence of a fixed integrated runtime pass.
- The tested build/runtime is NeoForge 21.1.265. An exact 21.1.256 offline attempt could not resolve uncached NeoForge inputs. Use `-PneoForgeVersion=21.1.256` with network/cached inputs for exact-version development runs. The cause identified here is physical-side gating, not a demonstrated loader-version mismatch.

## Architect's focused Prism acceptance

1. Remove v0.1.0, install only the v0.1.1 addon JAR, fully restart Prism's Minecraft process. Use a disposable full-pack world on 21.1.256 with cheats enabled.
2. Check the five startup stages listed in README. `/bulkcommerce status` must show ready and unblocked. If cheats are disabled, the OP-level-two command tree is hidden; this is separate from addon initialization.
3. Enable debugTransactions in that world's serverconfig and restart the world. A 64-stone vanilla shulker, no loose stone, and a BUY_FROM_PLAYER shop at 64/3 Spurs must produce receipt → intent durable → outcome durable and exactly one payment. Preserve a nonmatching item/component. Repeat with upgraded Sophisticated storage.
4. Save/exit, reopen the same world, and switch to a second disposable world; check separate ledgers and healthy lifecycle diagnostics. Do not bypass recovery if a breaker is present. Recheck dedicated-server operation with an ordinary client that lacks the addon.

## Optional developer integrated test

`runIntegratedTestClient` is a development-only physical-client run. Provide the same four references and `-PcreateJar=...`. First copy a disposable generated `run-gametest/world` to `run-integrated-test/saves/acceptance`, excluding session.lock and its data/foundrybulkcommerce ledger directory, so histories are not cloned. Never use player/production worlds. Run `./gradlew ... runIntegratedTestClient` on a graphical macOS session (or another supported graphical host). A test-only CLIENT @Mod invokes `test run installedadapters` after a real IntegratedServer starts and reports `[BulkCommerce acceptance] physical CLIENT / integrated server result=PASS` or FAIL before closing the client. Inspect the actual result; Gradle success alone does not establish it. No test launcher/client classes or copied worlds are packaged into the production JAR.
