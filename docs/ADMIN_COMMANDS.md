# v0.2.1 administrative reference

Verified **25 November 2026** against LedgerCommands. All commands are **read-only** and require vanilla permission level2 or console. There is no separate permission node or writable correction command. Ordinary selling does not require moderator command permission. The bare /bulkcommerce root has no action.

| Command | Behaviour |
| --- | --- |
| `/bulkcommerce status` | Memory-only ledger health/readiness/blocker/instance/queue; no SQL wait |
| `/bulkcommerce recent [limit]` | Latest10 summaries by default; explicit1–25 |
| `/bulkcommerce player <player-uuid> [limit]` | Exact UUID history; latest10 by default,1–25 |
| `/bulkcommerce unresolved` | Latest25 PREPARED or recovery-flagged records; not proof of total unresolved count |
| `/bulkcommerce inspect <transaction-uuid>` | Summary, instance/boot, intent hash, observed outcome and pagination instruction |
| `/bulkcommerce inspect <transaction-uuid> <page>` | Full intent evidence in2,000-character pages, split into500-character lines;1-based page argument bounded1–10,000; actual data is snapshot-capped |

SQL queries use the existing bounded ledger worker and reply on the server thread. They remain visible with showTransactionMessages=false and debugTransactions=false. Quiet successful-sale UUIDs can be retrieved through recent/player and then inspect. A rejected preparation may have a diagnostic reference without a ledger row because economic intent was never admitted; this behaviour is unchanged.

States: PREPARED (durable intent; effects may have been attempted if no outcome survived), CANCELLED (no economic attempt), APPLIED_MEMORY (verified in-memory removal/payment), ROLLED_BACK_MEMORY (verified in-memory compensation), RECOVERY_REQUIRED (uncertainty requiring manual investigation). Never interpret APPLIED_MEMORY as an atomic disk-save receipt. Recovery flags can apply to apparently successful memory outcomes after a dirty boot.

There is no replay, refund, correction, reconciliation or breaker-reset command. Audit query failure itself triggers conservative containment. Console audit remains usable when SQL can be opened; a corrupt/unopenable ledger needs offline investigation. For incidents exceeding the25-row view, inspect a consistent backed-up database with read-only SQLite tooling. Follow RECOVERY.md before any explicit offline reconciliation/clearance. Protect snapshots/history/backups as sensitive inventory and balance evidence.
