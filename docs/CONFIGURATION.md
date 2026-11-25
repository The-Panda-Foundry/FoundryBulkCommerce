# Configuration and the six distinct limits

Version 0.2.0, **25 November 2026**. NeoForge file: `<world>/serverconfig/foundrybulkcommerce-server.toml`. Existing values are retained; v0.2.0 retains existing defaults/ranges and adds an optional backpack adapter. Restart the world/server after edits for a repeatable acceptance run.

| Concept | Owner / value | Effect |
| --- | --- | --- |
| Shop bundle size | Treasury, 1–2,304 items | Items priced as one lot; unchanged |
| Container inventory capacity | Sophisticated API + modern saved capacity | Number of slots, not quantity sold; validated against canonical layout |
| Stack capacity | Sophisticated passive modifiers and canonical finite counts | How many items a slot can hold; never treated as transaction quantity |
| Inventory work budget | maxScannedSlots | Bounds outer scans and serialized inventory/upgrade inspections; also caps one container's inventory size |
| Serialization budget | maxSnapshotBytes | Separately caps total scanned binary NBT and final UTF-8 ledger intent; not a count of items |
| Transaction quantity | bulkTransactionLimit | Maximum individual items sold in one request, rounded down to complete shop bundles |

| Setting | Default | Allowed values |
| --- | --- | --- |
| enabled | true | false refuses Sell All, without bypassing safety |
| vanillaShulkers | true | Include hotbar vanilla shulkers |
| sophisticatedShulkers | true | Include eligible hotbar Sophisticated shulkers |
| sophisticatedBackpacks | true | Include supported hotbar backpacks when installed |
| debugTransactions | false | INFO phases, per-source rejection categories and work counters when true |
| bulkTransactionLimit | 2304 | 1–1,048,576 items |
| serverInstanceId | empty | Generate/persist UUID, or initial explicit 1–64 letters/digits/_.:- |
| requestIntervalMillis | 250 | 50–60,000 ms; global admission interval |
| maxScannedSlots | 4096 | 64–32,768 inspections and per-container inventory-capacity ceiling |
| maxSnapshotBytes | 1,048,576 | 16,384–4,194,304 bytes |

BC-002 is resolved by existing configuration. For the four Netherite examples (up to 33,792 wheat), a test bulkTransactionLimit of **65,536** permits all complete bundles in one request. At 2,304 wheat per Spur, outcomes are 3/7/11/14 Spurs with 1,536/768/0/1,536 left. That is an acceptance-test setting, not a measured recommendation for production throughput. The default remains 2,304. Any remainder stays in the original containers. A bundle larger than the transaction limit cannot be sold and is diagnosed as validation failure.

Changing the transaction limit does not change prices, shop bundle configuration, storage slots, UUID eligibility, scanning budgets or snapshot budgets. Do not increase scan/serialization limits to mask an unknown rejection. First enable debugTransactions, capture the reference and exact category, then compare actual legitimate capacity/work with the configured budget. Keep the one-active-transaction policy, cooldown and host load measurements in mind.

Typed limit failures happen before durable intent and economic mutation. Integer payout multiplication and total-account arithmetic remain checked. User feedback and ledger outcomes are not guarantees of cross-resource crash atomicity. No automatic reconciliation or in-game breaker reset exists.

## v0.2.0 eligibility and identity work

Containers are eligible only in hotbar indices 0–8; this is standard behaviour with no configuration bypass. Loose matching inventory stacks still use slots 0–35. Offhand, armour, equipped and main-inventory containers outside the hotbar are protected. Turning sophisticatedBackpacks off does not disable wider UUID checks for shulker transactions, and does not bypass compatibility fingerprint verification.

maxScannedSlots includes bounded identity inspections across vanilla inventory/cursor and supported Curios/Accessories active/cosmetic slots, plus hotbar and serialized entry work. Each adapter performs its own bounded identity capture; mutation revalidation uses fresh bounded identity scans. No container content is decoded merely to inspect its UUID outside the hotbar. An excessive equipment layout fails safely rather than triggering an unbounded provider walk. Bounds apply per validation pass; they are not one cumulative lifetime transaction counter or hard CPU deadline. Full image comparisons and final evidence remain bounded by accepted data size.

Keep existing world settings, including a deliberate 230,400 limit, when upgrading. The addon does not rewrite that value. Default workload limits are unchanged; do not increase them solely because quantities exceed the item limit. See V0_2_0_COMPATIBILITY.md for supported layouts and exclusions.
