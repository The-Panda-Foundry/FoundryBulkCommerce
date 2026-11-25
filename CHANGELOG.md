# Changelog

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
