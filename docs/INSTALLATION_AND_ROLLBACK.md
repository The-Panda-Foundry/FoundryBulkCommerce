# v0.2.1 installation and rollback

Prepared **25 November 2026**. Instructions for an approved maintenance window or isolated staging. This delivery does not authorize or perform production deployment. Commerce Fixtures is not part of this release and must not be installed on production/public clients.

## Install or upgrade from accepted v0.2.0

1. Retain the accepted v0.2.0 JAR and configuration, Git tag baseline/v0.2.0 and a consistent backup of world/player files, Numismatics, both Sophisticated SavedData repositories and the complete ledger/sidecars. Do not copy only ledger.sqlite during active WAL writes. Schedule player access appropriately; let pending transactions finish, then stop normally and wait for the Java process/ledger worker to exit.
2. Verify the downloaded foundrybulkcommerce-0.2.1.jar against SHA256SUMS. On macOS use `shasum -a 256 -c SHA256SUMS`; on Linux use `sha256sum -c SHA256SUMS`, from the artifacts directory.
3. Replace the old addon JAR in the host's mods folder with exactly one foundrybulkcommerce-0.2.1.jar. Do not install the sources JAR, source project, Git bundle, test code or Commerce Fixtures. Leave exact dependency JARs, world data and ledger untouched. No schema migration or native item format change occurs.
4. Keep the existing `<world>/serverconfig/foundrybulkcommerce-server.toml`, serverInstanceId and approved bulkTransactionLimit/work budgets. The new root key defaults false when absent:

   ```toml
   showTransactionMessages = false
   debugTransactions = false
   ```

   The actual logging key remains debugTransactions, independently of the startup log's transactionDebug label. Do not reset an existing230,400 limit to the unchanged2,304 default by replacing the whole configuration. To opt into routine chat, set showTransactionMessages=true. Restart after edits for controlled acceptance.
5. Start with Java21 and the target Minecraft1.21.1/NeoForge modpack. Confirm v0.2.1 physical-side initialization, command registration, exact compatibility hashes, logical-server startup and healthy SQLite/admission diagnostics. If the breaker/ledger is blocked, retain evidence and follow RECOVERY.md; do not delete/reset it or remove the addon to bypass containment.
6. Dedicated hosts install the addon only on the server; remote clients use their original modpack without requiring Bulk Commerce. Integrated hosts install it in the local Prism instance. Verify one controlled sale, quiet preparation/success chat, exact balances/remainder, normal UI refresh and administrator recent/inspect evidence. Failures and critical warnings must remain visible. Follow RELEASE_0_2_1.md for final host gates.

SQLite needs persistent local disk and usable native-library/temp permissions on the host. Startup fingerprinting is strict even for same-version rebuilt dependencies. The automated runtime here was macOS/NeoForge21.1.265; exact21.1.256/Linux/native-channel qualification remains an explicit host test.

## Instance identity for staging copies

An existing ledger is permanently bound to its recorded instance ID. Upgrading or downgrading its addon must preserve that binding. A brand-new staging world/ledger should receive its own instance ID before first initialization. If copying an existing world plus ledger for isolated investigation, do not change serverInstanceId and expect it to open: perform an explicitly reviewed offline identity migration if a distinct copied identity is required. Never run two processes against the same ledger files; do not reuse cloned economic data as an independently authoritative production instance.

## Software rollback to v0.2.0

1. Stop normally and wait for process exit. Preserve the post-v0.2.1 world/economy/ledger/logs consistently before changing anything; retain unresolved evidence.
2. Replace only the addon with the accepted v0.2.0 JAR. Its known SHA-256 is `81847f9e3a2eb20b44c0c0951bdec4c83473c9520e9327b1116972a08728dbd0`. Recover the original compiled artifact from the accepted v0.2.0 package, or reproduce from its preserved Git baseline using the documented toolchain; never keep both versions installed.
3. Keep current game data, both canonical repositories, bank and ledger together. Schema remains1 and v0.2.1 added no resource format change, so notification-only software rollback does not require restoring old financial state. Preserve IDs and current transaction history. Keep the existing configuration backup; v0.2.0 does not support showTransactionMessages and will again show routine chat.
4. Restart isolated staging first and verify health, instance binding, native contents/bank and read-only audits. A software downgrade is not reconciliation and must not bypass an existing breaker.

If data must be restored, restore one **consistent complete backup** under the manual recovery process. Never roll back only player inventory, one Sophisticated repository, bank files or ledger: that can duplicate currency/items or erase recovery evidence. Account for every post-backup economic action and external restore before returning service. Do not blindly replay/refund pending transactions.

No deployment or automated rollback action is included in the project.
