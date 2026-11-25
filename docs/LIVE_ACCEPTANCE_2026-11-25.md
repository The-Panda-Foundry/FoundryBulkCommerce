# v0.2.0 integrated functional acceptance and final validation plan

Recorded **25 November 2026**, from the architect's live acceptance feedback. This report records user-observed gameplay; it is separate from automated development tests and does not establish complete release acceptance.

## Preserved implementation

- Tested candidate: v0.2.0-rc1, source commit d25249a2ebfe6ed99e43142fc15685912e12f890.
- Immutable checkpoint tag: acceptance/v0.2.0-integrated-functional-2026-11-25 (annotated).
- Production JAR SHA-256: 81847f9e3a2eb20b44c0c0951bdec4c83473c9520e9327b1116972a08728dbd0.
- No implementation, configuration defaults, dependency archives or production JAR changed/recompiled for this feedback. Follow-up Git changes are documentation only.

## Confirmed live observations

| Scenario | Architect-reported result |
| --- | --- |
| All standard Sophisticated Backpack tiers | Passed |
| Multiple backpacks and shulkers in one transaction | Passed |
| Netherite shulker with Omega and 300,000 wheat | Passed |
| Hotbar-only container extraction | Passed |
| Main-inventory backpacks and shulkers | Protected; untouched |
| Curios-equipped backpacks | Protected; untouched |
| Partial extraction and retained excess stock | Passed |
| Mixed-container sale | Completed; 1,120 wheat remained in one eligible backpack; protected containers untouched |

No live log, transaction UUID, exact starting mixed-container total, shop lot/price, payment delta, current loader fingerprint or complete upgrade permutation record accompanied this feedback. Do not infer these values or infer that Omega backpacks were tested from the Omega shulker result.

## Final validation gates, in order

| Gate | Work remaining | Evidence to retain |
| --- | --- | --- |
| 1. Upgraded backpack regression | All safely supported backpack modifiers including Omega; representative combinations; mixed/protected containers; duplicate identities; totals beyond int and transaction ceiling; repeated partial sales; stale movement/alias rejection | Exact versions/configuration; before/after counts, upgrades/settings, both bank fields; transaction IDs; debug rejection lines where needed |
| 2. Persistence | Wrapper reopen, orderly save/restart and world reopen; vanilla components, both canonical repositories, unrelated metadata, bank and ledger consistency | Consistent backup, before/after snapshots and ledger rows; clean shutdown/readiness logs |
| 3. Dedicated compatibility | Java21/NeoForge21.1.256 full-pack Linux staging; SQLite native loading and hashes; connect normal clients without Bulk Commerce; existing Treasury UI; optional Backpacks absence | Startup and connection logs, exact mod/JAR hashes, successful transaction IDs; permission/command checks |
| 4. Concurrency and performance | Two then several real players, repeated clicks, mixed upgraded containers, world saves; busy requests must not mutate/pay; measure MSPT/latency/GC/commit contention | Requests/results/references, before/after stock/bank, timings and host metrics |
| 5. Conservative crash containment | Disposable fault environment: pending intent, extraction/payment/outcome boundary kills, IO failure, dirty reboot and manual reconciliation | Full incident backup, retained intent/outcome evidence, breaker/login containment across restart, documented human decision; no automatic replay/refund |

Use INTEGRATED_ACCEPTANCE.md for detailed functional regressions, STAGING_CHECKLIST.md for dedicated steps, RECOVERY.md for manual containment, V0_2_0_COMPATIBILITY.md for supported configurations and PERFORMANCE.md for qualified development measurements. Mark individual checkboxes only when that exact scenario is performed. Previously reported manual functional passes should not mark dedicated or crash gates complete.

Existing automated evidence remains unchanged: 2,406 executable checks; nine required GameTests with integrations; four base runtime tests with five guarded optional skips when integrations are absent. Automated runtime was NeoForge21.1.265; exact21.1.256 artifacts were unavailable offline. This documentation-only checkpoint does not rerun or upgrade that evidence.

## Staging preparation

Use an isolated consistent copy and distinct instance/ledger identity with persistent local disk and console access. Preserve the accepted JAR and backups. Keep approved limits unchanged and debug logging off except bounded diagnostic sessions. Never point development fixtures or fault tests at production. Do not bypass containment by deleting the ledger or removing the addon. No new features or refactoring are planned; any discovered defect should receive a separately reviewed focused correction and regression test.

**Status: integrated functional acceptance reported successful; final validation pending. No production deployment.**
