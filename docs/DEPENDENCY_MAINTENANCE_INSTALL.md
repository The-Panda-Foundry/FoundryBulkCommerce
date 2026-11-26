# Dependency maintenance installation / rollback — 26 November 2026

Candidates: **Bulk Commerce0.2.2** (production addon) and **Commerce Fixtures0.2.1** (development-only separate utility). No deployment is performed or authorized by this delivery. Production currently runs0.2.1 according to the architect; retain that accepted artifact/configuration and its complete native dependency cohort.

## Supported exact sets

| Set | Core | Storage | Backpacks (if installed) |
| --- | --- | --- | --- |
| Previously validated | 1.5.7.2381 | 1.6.2.2159 | 3.26.9.2195 |
| Newly validated | 1.5.43.2504 | 1.6.22.2244 | 3.26.23.2263 |

Complete fingerprints and API/native-network assessment are in DEPENDENCY_COMPATIBILITY_2026-11-26.md. Optional mods may be absent; present family members must belong to the same validated cohort. Do not mix old/new builds or substitute same-display-version rebuilds. Bulk Commerce checks actual runtime bytes before admission; fixture compile references remain exactly verified as before. Metadata permits only the two exact displayed versions. No additional libraries are updated/bundled by this work.

## Staging installation

1. Use a disposable consistent copy first. Stop normally and wait for the Java process/ledger worker to exit. Back up world/player files, Numismatics, both Sophisticated repositories, configuration and the full ledger/sidecars consistently. Retain the accepted addon/dependency set together, and preserve instance binding/history.
2. Check SHA256SUMS. Replace only the Bulk Commerce addon with foundrybulkcommerce-0.2.2.jar and install one matching Sophisticated cohort. Keep Create6.0.10, Numismatics1.1.0, Treasury1.0.2 and the normal equipment libraries unchanged. Remove duplicate old JARs; do not modify original dependency archives. Supplied production ZIP contains no Commerce Fixtures utility.
3. Keep the existing serverconfig file, ledger and serverInstanceId. Do not replace the whole TOML, reset a journal or create a new instance identity for an existing database. Fresh/missing-key defaults now use bulkTransactionLimit=230400; saved values remain authoritative. Actual keys debugTransactions=false and showTransactionMessages=false remain unchanged; transactionDebug is not a renamed/added key.
4. Ordinary clients do not need Bulk Commerce, but do need the matching updated Sophisticated/modpack versions. Native tooltip request packets changed shape across cohorts; old/new Sophisticated client/server interoperability is not certified. Test a normal client without the addon in staging.
5. Confirm addon version, native dependency hashes, ledger healthy/admission open, instance identity and administrator recent/player/inspect/unresolved history. Quiet routine chat, meaningful rejection/security feedback, exact quantities/balances and native hotbar/protected/duplicate behaviour must match baseline. Keep existing recovery containment; no automatic replay/refund exists.
6. Complete new-cohort full-pack integrated/dedicated client UI, orderly persistence, concurrent real players, performance/disk pressure and disposable fault/manual-reconciliation gates from the existing staging/recovery docs. Headless tests passed, but the graphical integrated harness was blocked by macOS window services before acceptance. No Linux/remote-client gameplay result is inferred.

Install foundrycommercefixtures-dev-0.2.1.jar **only in isolated development/staging**, if needed, with the same native cohort; remove its old version. It remains OP4, empty-hand, zero-entity and separate from the production addon. Do not add it to the public modpack or production server. Existing fixture commands and generated modern native storage identities/data are unchanged; no vanilla fixture command is added.

## Rollback

- Preserve a new consistent post-test/incident backup before any rollback. Stop normally. A JAR downgrade does not resolve a persistent breaker or uncertain financial outcome.
- Accepted Bulk Commerce0.2.1 verifies only the old native hashes. To return to that addon, restore the **complete matching old addon/native modpack cohort**, including clients. Never downgrade only the addon while leaving newly validated dependencies installed.
- Candidate Bulk Commerce0.2.2 and Fixtures0.2.1 also support the independently retested old native set; they can be retained for an isolated old-cohort comparison without changing addon format. This is not authorization to downgrade arbitrary native-mod data/features introduced elsewhere in the pack.
- No addon schema, transaction history or instance-ID migration is introduced. Do not restore an old ledger alone or delete new rows during a software rollback. If resource data must be restored, use one consistent complete backup and manual reconciliation, accounting for later activity; never independently roll back player inventories, a single canonical repository, bank files or journal.
- Native SavedData classes used here are byte-identical and modern fixture disk round trips passed on both cohorts. That does not certify every new Sophisticated feature or legacy migration for general modpack downgrade; retain coherent backups and validate the actual world's data before returning service.

Existing cross-resource durability limits remain: intent/outcome durability is not atomic persistence across Minecraft inventories, Sophisticated storage and Numismatics. Dirty boots/uncertainty require conservative manual containment. No production deployment occurs in this task.
