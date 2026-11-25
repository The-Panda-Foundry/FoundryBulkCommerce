# v0.2.1 final release review and validation gates

Recorded **25 November 2026**. Candidate for architect acceptance; **no production deployment**. The architect reports extensive integrated-server v0.2.0 testing and initial dedicated functional success. Exact dedicated loader/host/client-install state and detailed persistence/concurrency/crash evidence were not supplied in this brief, so these are not inferred or marked passed. v0.2.1 itself has automated evidence below and still needs notification acceptance on the target runtime.

## Focused change and review conclusion

The production change is one default-false config option and two guards around preparation/success chat, plus version metadata. No storage adapter, transaction coordinator, wallet, ledger schema/SQL, Mixin, compatibility hash, admission/containment or networking logic changes. Inventory/Treasury refresh stays outside the success-message guard. Tests capture real ServerPlayer system messages through actual Treasury Sell All, async native SQLite and settlement.

No new verified release-blocking implementation defect was identified in the reviewed paths. This is a focused final review and regression pass, not a complete new independent security audit or proof that every third-party interaction is safe. Existing residual durability risks and uncompleted deployment qualification remain material. No further feature/refactor is proposed. The unrelated Numismatics sales-point crash is out of scope and remains an external issue; an unclean crash can legitimately activate this addon's existing recovery containment.

| Reviewed concern | Current evidence / remaining boundary |
| --- | --- |
| Notification scope | Only routine preparation and APPLIED_MEMORY success gated; non-success feedback, critical ledger paths and audit commands untouched |
| Async sequencing | Durable prepare before mutation; fresh logical-server continuation validates current shop, source position/identity, wallet and config; no SQL wait on server thread |
| Extraction/payment/compensation | Exact snapshots and verified removal precede payment; checked arithmetic; uncertain wallet compensation never returns items blindly; existing fault suite passes |
| Hotbar/storage identity | All container sources remain0–8; wider bounded repository-scoped identity checks and canonical revalidation unchanged; supported equipment APIs retained |
| Ledger/recovery | Schema1, unique IDs, instance/boot, FULL SQLite commits, persistent breaker/dirty-boot flags, read-only audit and manual reconciliation unchanged |
| Packaging/compatibility | Exact dependency pins and common server-class Mixins unchanged; no Commerce Fixtures, new client requirement or custom protocol; archive checks required below |
| Performance | No new scanning/worker/tick/cache or increased budgets; two O(1) config reads; actual host/network/disk timings still required |

## Automated evidence

- Normal Java21 Gradle clean build: **2,406 executable checks pass** (transaction2327, compatibility20, SQLite27, vanilla19, schema13).
- **Ten required actual NeoForge GameTests pass** with Backpacks, Curios and Accessories installed. Existing Storage443 and Backpacks224 matrix assertions remain passing, plus full mixed/large-count/overflow/repeated/concurrent-admission/compensation coverage.
- New notification runtime: five applied64-item/3-Spur sales and one stale cancellation yield exactly six unique durable rows. Tests cover default false, explicit true, debug true with quiet chat, true→false and false→true while intent is pending, matching references when visible, exact stock/unrelated-item/bank state, visible cancellation/insufficient-stock/admission warnings and retained audit command registration.
- Optional Backpacks/Curios/Accessories absent: ten harness cases pass, comprising **five active base/notification tests and five explicitly guarded/skipped optional tests**. This is not backpack coverage in the absence configuration.
- Toolchain: Temurin21.0.12/macOS ARM64, Minecraft1.21.1, Gradle9.2.1, ModDevGradle2.0.148, NeoForge **21.1.265**. Exact21.1.256 offline artifact resolution failed (uncached). No fallback compiler/packager. Full-pack graphical, Linux native and real-client checks remain staging gates.
- Test-only performance label now reports current addon version; it does not change the benchmark or production. Results are qualified microbenchmarks in PERFORMANCE.md/logs, not hosted MSPT guarantees.

## Final host validation protocol

Use an isolated consistent staging copy, the exact target modpack, console access and persistent local disk. Record loader/mod hashes, config, starting/remaining inventory and both bank fields, transaction IDs, timings and discrepancies. Use INSTALLATION_AND_ROLLBACK.md and the full STAGING_CHECKLIST.md. Do not install Commerce Fixtures in production; manual fixtures/test stock must stay isolated.

| Gate | Procedure | Required evidence / pass condition |
| --- | --- | --- |
| Notification acceptance | Default showTransactionMessages=false and debugTransactions=false: valid sale, rejection, duplicate/unsupported source warning; then opt in; separately enable debug with chat off | Quiet successful/preparation chat; normal UI/account updates; visible failures/security; unchanged diagnostic policy; exact recent/player/inspect rows and IDs |
| Dedicated connection | Start Java21 target NeoForge/full pack; connect ordinary client without Bulk Commerce; use existing Treasury UI; repeat relevant hotbar/protected and optional-mod cases | Healthy native SQLite/startup hashes, no addon-required client channel/registry, correct extraction/payment/references; actual target versions recorded |
| Clean persistence | Record a mixed vanilla/Storage/Backpacks sale, partial huge stack and metadata; stop normally, wait for process exit, back up consistently, restart and reopen containers | Exact player component data, both canonical repository+UUID roots, upgrades/settings/unrelated items, both bank balances and ledger outcomes survive; no unresolved rows/breaker unexpectedly cleared |
| Concurrency | Two then several real players submit simultaneous/repeated requests while world activity/saves continue; include aliases and source/price/account changes during preparation | One global active sale; busy requests cause no mutation and no queued replay; each accepted reference accounts for exactly one extraction/payment; stale plans cancel; repeat safely |
| Host performance | Compare baseline with large upgraded/mixed9-hotbar inventories, protected inventories, component-heavy nonmatches and repeated/concurrent requests | Record MSPT, main-thread preparation/settlement, latency p95/p99, heap/GC, free disk/WAL and FULL-commit/world-save contention; agree host budget before increasing limits; no hard deadline is guaranteed by byte/slot caps |
| Crash/failure containment | Disposable clone only: controlled JVM/process kills at durable intent, removal, payment and outcome boundaries; IO/locked/full/corrupt ledger tests | Retained pending/dirty-boot evidence, persistent breaker and login containment across restart, no late economic replay/refund; preserve logs/data for every boundary |
| Manual reconciliation | Follow RECOVERY.md on one disposable incident; inspect actual recovered resources against full intent/outcome evidence | Human evidence/operator/decision retained; no blind snapshot restoration; all obligations resolved before explicit offline clearance; reopening cannot erase unresolved history |

The OS boundary fault tests require staging-only external instrumentation or debugger controls if exact timing is necessary; no production fault hooks are added by this release. Existing simulated native SQLite/transaction faults are automated, but they are not a substitute for power-loss/disk/kill tests.

## Explicit transaction durability limitation

SQLite is authoritative for durable intent and observed outcome/audit. Live Minecraft inventories, each Sophisticated canonical repository and the Numismatics account remain authoritative for their actual resource state. SQLite does not own or atomically commit their files. Durable PREPARED authorizes at most the existing one attempt; it is never an instruction to replay automatically.

APPLIED_MEMORY means removal and payment were verified in memory; ROLLED_BACK_MEMORY means synchronous compensation was verified in memory. Neither proves those separate mods' subsequent saves all reached durable storage. A crash can persist removed items without payment, payment without removed items, or a mixture of old/new resources. FULL journal commits do not coordinate Minecraft player files, sophisticatedstorage, sophisticatedbackpacks and bank SavedData. Normal clean shutdown is an operational boundary, not proof of coordinated physical fsync. External partial restores, hidden third-party save failures or power loss can produce disagreements that a clean marker does not identify.

On dirty boot, prior potentially mutating records are conservatively flagged, including successful/rolled-back memory outcomes. A persistent breaker or unresolved record blocks admission. Live settlement/ledger uncertainty disconnects players and closes login admission so ambiguous funds cannot be spent via other mods. No automatic refund, replay, crash reconciliation or in-game breaker reset exists. Compare recovered repository-specific resources and subsequent activity with preserved evidence; if ownership cannot be established, use a consistent backup under manual review. Never resolve disagreement with blind last-write-wins or an isolated ledger restore.

## Other known limits

Unknown equipment providers/mirrored Curios–Accessories bridges are not certified; duplicate detection is bounded and scoped to supported locations, not a global online-player scan. Nested/remote/linked/equipped extraction and non-passive/creative layouts remain excluded. Arbitrary off-thread third-party mutation and pathological component codecs are outside guaranteed compensation/CPU limits. Commands use OP2/console, not a custom permission node; history queries are bounded, no automatic pruning/archival engine exists. Native tooltip aggregate formatting may be inaccurate at above-int totals; this release does not patch dependency UI. See compatibility/recovery docs for full boundaries.

**Release status: feature-complete candidate; focused automated validation passed. Final host validation and explicit architect approval remain required before any production deployment.**
