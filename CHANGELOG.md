# Changelog

## 0.1.0 — 25 November 2026

Initial staging candidate.

- Integrate with the validated Treasury 1.0.2 server-shop Sell All path.
- Include matching loose items, carried vanilla shulkers and supported canonical Sophisticated Storage shulkers.
- Preserve full item/component matching and sell only complete lots at the existing shop price.
- Support oversized finite Sophisticated stack counts and standard stack upgrades.
- Add a configurable per-request item limit with checked arithmetic.
- Exclude duplicate carried UUID aliases, linked storage, backpacks, nested traversal, legacy embedded storage and non-stack upgrades.
- Verify source changes before payment, verify standard and overflow account totals, and compensate failures.
- Pause bulk transactions on uncertain recovery and log reconciliation details.
- Add Java 21/NeoForge Gradle project, offline cache build helper, integrity tests, compatibility checks and staging documentation.

Compiled with the local-cache Java 21 fallback. Full Gradle execution, live Mixins and complete modpack runtime acceptance remain pending.
