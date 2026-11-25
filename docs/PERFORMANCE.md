# Local v0.1.2 performance measurements

Recorded **25 November 2026** on macOS ARM64 / Apple M4 Pro, Temurin Java 21.0.12, Minecraft 1.21.1 and NeoForge 21.1.265, with Create 6.0.10 and the exact four compatibility references. These are automated headless development measurements, **not full Panda Foundry load/MSPT or hosted Linux results**.

## Capture / plan / evidence benchmark

Real registered Netherite shulkers and dependency wrappers build canonical finite storage. Each timed operation collects eligible sources, computes complete lots within 65,536 items and constructs removal before/after evidence using the actual adapter. No edit is applied or paid in this benchmark. It excludes shop/wallet validation, vanilla-source collection, final intent JSON encoding, SQLite IO and settlement. Both versions use the same 4,096-inspection / 1-MiB snapshot settings. The 32x case has 132 occupied slots per box, each holding 2,048 wheat; four such boxes contain 1,081,344 wheat, above the tested transaction limit, without per-item loops.

The baseline is the unmodified compiled v0.1.1 addon (commit f3a5560, SHA-256 `372c9e8e623b521b9041553c81208cecea3b73cc6b8af131600576019454e1ea`). A test-only child classloader loads only its Sophisticated adapter classes; Minecraft/Core/Storage and SaleSource/Transaction types are shared. The new ScanBudget instrumentation is shared; its slot/NBT operations used by the old adapter retain their meaning. After 12 warmups per scenario/version, 40 samples are collected; sorted index 20 is the reported middle sample, index 37 is the approximately 95th-percentile sample. Baseline runs first, so JIT/order noise remains a limitation. No timing threshold controls test pass/fail.

Thread allocation uses the JVM ThreadMXBean allocation counter. These are cumulative **bytes allocated per operation**, not retained heap or peak memory and not a leak proof. Input fixtures and classloading warmup are outside timing.

| Scenario | Adapter | Middle sample ms | Approx. p95 ms | Middle allocated bytes | Sophisticated inspections |
| --- | --- | ---: | ---: | ---: | ---: |
| 1-full-netherite | v0.1.1 | 0.4679 | 0.5191 | 1,304,272 | 346 |
| 1-full-netherite | v0.1.2 | 0.2345 | 0.2751 | 784,096 | 214 |
| 4-full-netherite | v0.1.1 | 1.3916 | 1.6018 | 4,996,040 | 1138 |
| 4-full-netherite | v0.1.2 | 0.6424 | 0.6884 | 2,853,800 | 610 |
| 4-full-netherite-32x | v0.1.1 | 0.8895 | 0.9560 | 3,099,944 | 1142 |
| 4-full-netherite-32x | v0.1.2 | 0.5741 | 0.6188 | 1,895,176 | 614 |

Raw scanned canonical NBT was 6,215 bytes for one full box; 24,860 for four ordinary boxes; 25,324 for four upgraded boxes. Each measured request is well below default byte/inspection budgets. Source before/after evidence sizes were 6,613 / 23,503 / 10,363 characters respectively, unchanged between versions. Final JSON has its own byte cap too.

The reduction comes from decoding each stored item once and reusing match indexes/counts, plus avoiding duplicate nested copies in removal planning. Work depends on slots/data, not the number of individual wheat items. Full before/after verification and all safety controls remain. Large component codecs can still consume main-thread CPU before a byte-cap rejection; there is no hard millisecond deadline or guarantee from this microbenchmark.

## Real async handler / concurrent admission

The Netherite acceptance test invokes Treasury's real transformed handler and native SQLite, waits for the durable outcome and verifies the bank/remaining contents. End-to-end headless durations (milliseconds):

- 1 box(es): 2.2557 ms; 3 Spurs; 1,536 wheat left.
- 2 box(es): 1.9168 ms; 7 Spurs; 768 wheat left.
- 3 box(es): 2.3544 ms; 11 Spurs; 0 wheat left.
- 4 box(es): 2.5840 ms; 14 Spurs; 1,536 wheat left.

These include asynchronous intent/outcome acknowledgements and fast GameTest scheduling, but exclude fixture filling. A normal 20-TPS server adds tick-scheduling latency; these are not player-visible latency promises. SQLite FULL commit costs depend on the actual host filesystem and can contend with world saves.

A second registered FakePlayer submits a valid, fully stocked request while the first intent is pending. Admission is refused without stock/payment mutation. Recorded call times were 283.166, 93.417, 109.791, 114.042 microseconds (debug logging enabled). This exercises two interleaved players on the real server thread. It is not a many-player network/load test. The one-active-request policy and default global 250-ms interval remain unchanged; requests are refused while busy rather than queued for later economic replay.

## Bounds and remaining staging measurements

No default/range was raised. Per-request source/diagnostic lists are bounded by vanilla inventory size, matched entries by inspection budgets, canonical copies by the scanned byte budget, intent by its own byte cap and outstanding plans by one active transaction. The SQL writer remains a bounded queue of 64 with no server-thread database wait. Encoding/temporary allocation can exceed serialized-byte counts by a multiple, as the allocation measurements show; this is not a strict process-heap cap.

On dedicated staging, record server-thread preparation/settlement time and normal MSPT while 1, 2 and several real players submit requests; include the full modpack, world saves, upgraded boxes and component-heavy nonmatches. Measure journal latency/disk pressure on Linux, verify client connection without this addon, and choose limits from those results. Keep default scan/snapshot safeguards until evidence justifies changes.

## Reproduction

Run the standard Gradle gameTestServer task with Java 21, original dependencyDir, Create JAR and `-PbaselineJar=/path/to/the/original/0.1.1.jar`. The optional baseline path is used only by test code and is never packaged in the addon. See VERIFICATION.md for the temporary local Maven repository override needed in this task environment. Read actual `[BC001 benchmark]`, `[BC001 performance]`, `[BC001 acceptance]` and required-test result lines, not just Gradle's exit code.
