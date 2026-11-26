# Changelog

## 0.2.3 — 26 November 2026 (BC-004)

- Replace shared stack-only eligibility with explicit reviewed-class validation for ordinary Sophisticated upgrades. Preserve primary-only extraction; never sell upgrade items, ghost filters, crafting/cooking contents, fluids or energy.
- Validate legitimate backpack tank/battery column layouts against installed upgrade APIs. Keep nested, virtual, infinite and unreviewed storage restricted.
- Add native multi-upgrade, auxiliary-state, compensation and real Treasury/SQLite regressions. Existing transaction, ledger, identity, hotbar, notification and dependency safeguards remain unchanged.
- No schema migration, new configuration, production deployment or Commerce Fixtures inclusion. See docs/BC_004_UPGRADE_COMPATIBILITY.md and docs/BC_004_INSTALLATION.md.

## 0.2.2 — 26 November 2026 — dependency maintenance

- Validate confirmed new Core1.5.43.2504, Storage1.6.22.2244 and Backpacks3.26.23.2263 exact JARs; retain independently retested old complete cohort.
- Update compile inputs and precise metadata alternatives; strict off-thread runtime hashes accept complete verified cohorts and reject unknown/mixed builds. Economy/Create/equipment libraries unchanged.
- Preserve transaction/storage/ledger/Mixin class bytes and all existing features. No schema/history/instance migration, notification behaviour change or production deployment.
- Set explicitly requested fresh bulkTransactionLimit default230400 while retaining existing saved values, debugTransactions=false and showTransactionMessages=false; no config key rename.
- Document complete comparison/API review, both-cohort/optional-absent regression, native request-network change, integrated launch environment blocker and coherent rollback requirements.

## 0.2.1 — 25 November 2026 — final release candidate

- Add showTransactionMessages=false, independently of debugTransactions. Suppress only routine preparation/success chat and their references by default; retain failure/rejection/security/circuit-breaker messages, ledger evidence and administrator commands.
- Keep transaction execution, asynchronous intent/outcome handling, matching/extraction/payment/compensation, schema, compatibility pins and UI refresh unchanged.
- Add actual server-message capture tests for quiet/visible sales, debug independence, pending-intent option changes, visible stale/insufficient-stock/admission rejections, exact bank/stock and unique durable ledger records. Correct the existing performance harness's current-version label.
- Preserve accepted v0.2.0 in Git; document architect-reported integrated/initial dedicated acceptance, focused final review, deployment gates, installation/rollback and explicit cross-resource durability limits.
- No storage integrations/features/refactoring, Commerce Fixtures packaging, unrelated Numismatics crash fix or production deployment.

## 0.2.0 — 25 November 2026

- Optional Sophisticated Backpacks 3.26.9.2195 integration: all six registered tiers and nine passive stack modifiers, including Omega, through the existing canonical storage API.
- Restrict vanilla shulker, Sophisticated shulker and backpack extraction to hotbar slots 0–8. Preserve ordinary loose-item inventory behaviour. Revalidate original eligible position/object before mutation.
- Separate repository-scoped storage identities from extraction eligibility; inspect wider vanilla inventory/cursor and supported Curios/Accessories active/cosmetic equipment without decoding protected contents.
- Preserve complete canonical settings/upgrades/metadata, exact extraction verification, compensation, checked arithmetic, transaction limits, ledger and persistent recovery containment. No schema migration or new Mixin/network interface.
- Pin optional integration references by SHA-256; preserve accepted v0.1.2 under baseline/v0.1.2.
- Add actual runtime backpack/tier/modifier, equipment-alias, mixed async transaction and large-count performance regressions; isolate optional test API signatures from GameTest discovery.
- Document mandatory eligibility change, compatibility matrix, limits, test evidence and integrated/dedicated acceptance gates. No production deployment; no automatic crash reconciliation.

## 0.1.2 release candidate — 25 November 2026

- Preserve working v0.1.1 at tag baseline/v0.1.1. Baseline runtime reproduction accepts all six shulker tiers, including Netherite; the historical generic rejection cannot establish its exact cause.
- Validate registered Sophisticated shulkers/capacities with non-mutating dependency APIs and modern saved components. Support legitimate larger saved capacities within existing workload limits.
- Replace individual upgrade IDs with the exact inspected passive StackUpgradeItem class/namespace/finite-multiplier checks, covering all seven upgrades and three downgrades. Other processing hooks remain excluded.
- Decode canonical entries once and reuse the immutable exact-match plan for removal; preserve complete before/after verification, rollback and metadata.
- Add typed, bounded rejection categories and debug-only source/work details. No protective default/range is raised.
- Add installed-registry matrix, Netherite/mixed/repeated/overflow/ledger runtime regressions and reproducible preparation/heap-allocation comparison with v0.1.1.
- BC-002: no change to the working quantity calculation or Treasury bundle maximum; larger transactions remain configured through bulkTransactionLimit.
- No ledger migration, new networking, automatic recovery, scope expansion or production deployment.

## 0.1.1 — 25 November 2026

- Correct physical-side restrictions: common entry point and common-class Mixins now activate for integrated and dedicated servers. Economic execution remains on the logical server thread.
- Validate required dependency metadata on both physical distributions when installed, preserving dedicated-server use without requiring the addon on multiplayer clients.
- Add explicit initialization, command permission, compatibility fingerprint, ledger readiness/blocking and emergency diagnostics.
- Add `debugTransactions` (default false) for bounded INFO transaction phases, IDs, quantities and outcomes without inventory snapshots.
- Guard old integrated-world opening callbacks against affecting a subsequent world.
- Add physical-side regression assertions and Treasury client request-path check; retain all transaction safety tests and add a test-only integrated-client launch fixture.
- Standard Gradle build and physical dedicated GameTest pass. Graphical integrated runtime is blocked by the execution environment and remains a Prism retest; NeoForge 21.1.256 inputs are not cached here.
- No ledger schema change, automatic reconciliation, remote integration or production deployment.

## 0.1.0 MVP revision — 25 November 2026

- Add native SQLite local ledger with FULL WAL commits, immutable transaction UUID/origin IDs, full pre-mutation evidence and durable observed outcomes.
- Move every database operation off the server thread; revalidate game state after intent acknowledgement and verify removal before payment.
- Persist breaker and dirty-boot recovery obligations; contain uncertain resources by denying login/disconnecting players. No automatic replay/refunds.
- Add read-only operator/console status, recent, player, inspection and unresolved commands, plus offline manual reconciliation guidance.
- Bound request admission, concurrent work, scans and intent bytes; recheck UUID aliases introduced while preparation is pending.
- Allow ordinary non-container block-entity items; add item persistence and budget regressions.
- Verify exact runtime dependency fingerprints, including same-version builds.
- Repair standard ModDevGradle configuration and development SQLite classpath. Bundle only SQLite JDBC, never the four reference mod JARs.
- Add native database fault/restart tests and an actual NeoForge GameTest exercising installed Mixins, bank and Sophisticated Storage.
- Retain the hybrid proposal as explicitly deferred documentation. No MongoDB or synchronisation workers added.

## 0.1.0 initial staging candidate — 25 November 2026

- Integrate Treasury Sell All with matching loose items and carried vanilla/canonical Sophisticated shulkers, complete lots, stack upgrades and checked arithmetic.
- Exclude nested/linked/backpack storage and duplicate carried UUIDs; verify and compensate in-memory mutations.
- Supply initial isolated tests and documentation. The initial fallback-packaged candidate and process-only breaker are superseded by the MVP revision.
