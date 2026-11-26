# Foundry: Bulk Commerce

Sell resources directly from hotbar storage through Numismatics Treasury’s existing **Sell All** button.

A server-side Minecraft 1.21.1 addon by **The Panda Foundry**. Current version: **0.2.3**. Multiplayer clients do not need Bulk Commerce installed. Integrated hosts install it locally so their logical server can run it.

## Features

- Vanilla shulkers, every validated Sophisticated shulker/backpack tier and supported stack upgrades including Omega.
- Deliberate container selection: all nine hotbar slots are eligible; main-inventory, offhand, armour and equipped storage contents are protected.
- Multiple complete shop bundles per request, with exact matching and the original shop prices/access rules. Excess stock remains in its source.
- Reviewed ordinary upgrades can stay installed. Their configuration, auxiliary inventories, fluids and energy are preserved and never sold.
- Verified extraction before payment, checked arithmetic, duplicate storage identity protection and verified compensation.
- Asynchronous local SQLite transaction ledger, unique references, read-only audit commands and persistent emergency containment.
- Event-driven bounded work; no continuous inventory scans or additional tick listeners.

## Requirements and compatibility

Minecraft **1.21.1**, **Java 21**, NeoForge **21.1.x**. Automated development runtime: 21.1.265; metadata minimum 21.1.242 does not certify every loader build.

| Component | Requirement |
| --- | --- |
| Numismatics Treasury | Required: 1.0.2, exact validated JAR |
| Create: Numismatics | Required: 1.1.0 NeoForge/MC1.21.1, exact validated JAR |
| Create | Install the economy mods’ normal dependencies; tested with 6.0.10 |
| Sophisticated Storage/Core | Optional integration; coherent old or new exact cohort below |
| Sophisticated Backpacks | Optional integration, independent of Storage; requires its native Core dependency |
| Curios / Accessories | Optional equipped identity checks; exact validated builds only |
| SQLite JDBC | 3.51.3.0 bundled through NeoForge Jar-in-Jar; not another mod to download |

| Validated cohort | Core | Storage | Backpacks |
| --- | --- | --- | --- |
| Original | 1.5.7.2381 | 1.6.2.2159 | 3.26.9.2195 |
| Updated | 1.5.43.2504 | 1.6.22.2244 | 3.26.23.2263 |

Installed family members must belong to one coherent cohort. Exact SHA-256 verification intentionally refuses unknown bytes, even if displayed versions match. Omitted optional integrations need not be installed. Ordinary clients still need compatible versions of their normal native mods.

[Exact dependency fingerprints](docs/DEPENDENCY_COMPATIBILITY_2026-11-26.md) · [Upgrade support and restrictions](docs/BC_004_UPGRADE_COMPATIBILITY.md) · [Tier/capacity matrix](docs/V0_2_0_COMPATIBILITY.md)

## Installation

1. Obtain the official production `foundrybulkcommerce-0.2.3.jar` when released and verify its published checksum. Preparation of this repository is not publication approval.
2. Back up world/player data, Sophisticated storage, Numismatics accounts, configuration and the complete ledger consistently after a normal shutdown.
3. Place exactly one Bulk Commerce production JAR in the host’s `mods` directory. Do not install the sources JAR, test tools or Commerce Fixtures. Dedicated-server clients do not need this addon.
4. Keep the normal economy dependencies and any optional integrations on their validated builds. Start with Java 21 and check initialization, exact compatibility and ledger-readiness messages.
5. Put eligible containers in hotbar slots 0–8, open an existing Treasury shop that buys the item and click **Sell All**. The selected hotbar slot does not matter.

No ledger migration/reset is required when upgrading from 0.2.2. Preserve configuration, transaction history and server instance identity. [Installation, consistent backups and rollback](docs/BC_004_INSTALLATION.md).

## Configuration and administration

NeoForge server configuration: `<world>/serverconfig/foundrybulkcommerce-server.toml`.

```toml
bulkTransactionLimit = 230400
debugTransactions = false
showTransactionMessages = false
```

The limit is the maximum quantity sold per request, not container capacity. Treasury’s shop bundle maximum remains 2304. Routine preparation/success chat is silent by default; meaningful failures and critical warnings remain visible. The actual logging key is `debugTransactions`, independently of chat notifications.

Moderator commands require permission level 2 or console: `/bulkcommerce status`, `recent [limit]`, `player <uuid> [limit]`, `unresolved` and `inspect <transaction-uuid> [page]`.

[All configuration options](docs/CONFIGURATION.md) · [Command reference](docs/ADMIN_COMMANDS.md) · [Manual recovery](docs/RECOVERY.md)

## Transaction and storage limitations

The ledger does **not** guarantee atomic disk persistence across Minecraft inventories, Sophisticated repositories and Numismatics accounts. APPLIED_MEMORY records verified memory, not coordinated resource saves. Uncertain/unclean outcomes require containment and manual reconciliation. There is no automatic replay, refund or crash reconciliation. Never delete/reset the ledger or remove the addon to bypass a breaker.

Nested, linked, remote and placed storage are excluded. Inception, infinity, compression, mob-catcher and unreviewed implementations remain restricted as detailed in the upgrade matrix. Native I/O filters govern automation rather than an owner’s manual primary-inventory sale. A native stock/configuration change during asynchronous intent preparation can cancel a stale plan.

One globally active transaction, cooldowns and scan/snapshot limits bound execution; busy requests may be refused. Unknown equipment providers and bridges mirroring one physical slot into several APIs are not certified. Backups/ledger evidence contain sensitive player/inventory/account information and must remain private.

No MongoDB, remote synchronization, new interfaces or bundled fixture utility. This addon does not address unrelated Numismatics sales-point crashes.

## Building and validation

Use the Java 21 Gradle wrapper. Obtain the original dependency JARs legally; none are distributed in this repository. Put the exact economy and optional compile references listed in build.gradle in `dependencies/`, or pass `-PdependencyDir`, `-PcoreJar`, `-PstorageJar`, `-PbackpacksJar`, `-PcuriosJar` and `-PaccessoriesJar` paths. All optional APIs are compile references; they are not all mandatory runtime integrations.

```sh
./gradlew build
./gradlew -PcreateJar=/path/to/create-1.21.1-6.0.10.jar runGameTestServer
# Optional equipped identity tests additionally require native runtime dependencies:
./gradlew -PcreateJar=/path/to/create.jar -PwithCurios -PwithAccessories \
  -PowoJar=/path/to/owo-lib.jar runGameTestServer
./gradlew -PcreateJar=/path/to/create.jar -PwithoutBackpacks runGameTestServer
```

`build` runs five executable regression harnesses through `check`; the existing suite reports 2,424 checks. Headless GameTests load actual mods, Mixins, canonical inventories, bank and SQLite. Test sources stay in `src/test` / `src/gameTest`; only `src/main` is packaged in the production JAR. Run worlds are disposable and must never use production data. A populated cache supports `--offline` builds.

[Verification history and qualifications](docs/VERIFICATION.md) · [Dedicated staging checklist](docs/BC_004_INSTALLATION.md) · [Architecture](docs/ARCHITECTURE.md) · [Changelog](CHANGELOG.md)

## Issues, security and development

Use the bug/feature templates for non-security reports. Do not attach complete worlds, raw ledgers, account dumps, credentials or unredacted logs. Report duplication, authorization bypass and other exploitable defects privately under [SECURITY.md](SECURITY.md).

[Contributing](CONTRIBUTING.md) · [AI development disclosure](AI_DEVELOPMENT.md)

## Licence and attribution

Original Foundry: Bulk Commerce work is **All Rights Reserved**; public source visibility is not an open-source grant. See [LICENSE](LICENSE). External APIs/assets and bundled SQLite retain their own terms; see [third-party notices](THIRD_PARTY_NOTICES.md). No upstream endorsement is implied. Publication remains subject to the owner’s licensing and privacy review.
