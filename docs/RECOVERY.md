# Conservative recovery and manual reconciliation

Procedure recorded **25 November 2026**. Use on isolated staging first. There are no automatic refunds or crash replay.

## When the breaker is closed

`/bulkcommerce status` reports readiness/blocking. `/bulkcommerce unresolved` lists the latest 25 pending/flagged rows; inspect the full database for larger incidents. A startup error, mismatch, corrupt/locked ledger or unresolved boot denies player admission. During a live emergency all online players are disconnected. Console read-only audit remains available when SQL is readable.

Do not remove the addon, disable its Mixins, clear the database, delete a PREPARED row or restart repeatedly to get players online. An outcome labelled APPLIED_MEMORY records an observation; it does not prove both items and payment survived a crash.

## Investigation

1. Keep player access contained; stop the server normally. Preserve logs and the entire incident data before any correction. Take a consistent backup of the world/player files, Numismatics data, Sophisticated Storage ItemContentsStorage, Backpacks BackpackStorage (when installed) and ledger together. Do not copy only ledger.sqlite while an active WAL exists. Use SQLite's backup mechanism or copy the closed database with any sidecars.
2. Read with a read-only SQLite client. Record instance, boot, transaction ID, intent hash, shop/template/quantity/price, both balance before values and source before/after images. Commands show full intent in pages; the SQLite rows are easier for large evidence.
3. Compare the actual recovered player inventory/vanilla container components, canonical Sophisticated repository + UUID contents and Numismatics standard **and additional** balances with the evidence. Include later transactions and player activity; matching one balance is not proof of a particular payment. Never overwrite later legitimate contents from an old snapshot.
4. Establish one defensible resolution. Prefer restoration of a consistent pre-incident backup when ownership/payment cannot be established. If a targeted correction is warranted, apply it once offline using the authoritative mod data tooling and preserve the evidence. A human decision is required; this addon supplies no economic correction tooling.
5. In one explicit, backed-up offline SQLite transaction, append one `reconciliations` row per reviewed transaction (new UUID, transaction ID, UTC epoch milliseconds, operator and detailed evidence/action). Clear that row's `recovery_required`; change an abandoned PREPARED to an explicit manual state such as MANUALLY_RECONCILED so it remains history but is not pending. Do not replace the original intent/outcome payloads. The schema permits manual state labels; runtime emits only its five documented states.
6. Verify **zero** rows satisfy `recovery_required=1 OR state='PREPARED'`. Only after every recovery obligation and root cause is resolved may the offline administrator set metadata breaker to `0` and record its reason/resolution. Preserve original boot/instance metadata; do not falsify clean-stop evidence. There is deliberately no blanket reset SQL supplied to bypass these checks.
7. Restart staging, inspect status/unresolved/recent and verify affected resources before inviting players back. A configured instance mismatch requires an explicit identity migration, not silently replacing the database. Record any migration/restore alongside backups and retain distinct staging/production identities.

A breaker without pending rows still needs root-cause investigation: e.g. incompatible reference, disk failure or failed read. Correct the cause before clearing its persistent metadata. If the database cannot open, restore a verified consistent backup; do not manufacture a blank ledger over uncertain game data.

## Backup, retention and monitoring

Use persistent local storage and host disk alerts. POSIX ledger directory is 0700 and file 0600; protect the containing backups too. Inspect host SQLite/WAL bytes, free space, unresolved records, database open errors and status. There is no automatic pruning or configurable archival engine in this MVP. Archive only reviewed/resolved records after a verified backup; never delete pending or unresolved evidence to recover space. SQLite FULL commits can contend with world saves even though they run off-thread. Measure this on the actual host before increasing transaction rate/limits.


## v0.2.0 backpack evidence

Backpacks and Storage are independent canonical SavedData repositories (sophisticatedbackpacks and sophisticatedstorage). A UUID alone cannot identify which resource to reconcile. Source descriptions identify backpack versus shulker; inspect the matching repository and preserve its full settings/upgrades/root metadata. Equal UUID values across repositories do not establish an alias. Recovered item positions may differ from the original hotbar snapshot: never infer extraction authorization or ownership from position alone after a crash. The hotbar rule and additional canonical API do not provide coordinated crash persistence; manual evidence-based containment remains mandatory.


## v0.2.1 notification setting

showTransactionMessages only controls routine preparation/success chat; it does not remove ledger records, warnings, persistent breaker/login containment or manual recovery obligations. Silent success still means APPLIED_MEMORY rather than cross-resource disk atomicity. Notification-only software rollback to accepted v0.2.0 requires no schema migration; never treat a downgrade as permission to clear/replay unresolved outcomes. See INSTALLATION_AND_ROLLBACK.md.
