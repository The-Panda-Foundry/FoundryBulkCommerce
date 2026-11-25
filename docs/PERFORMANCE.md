# v0.2.1 performance qualification — 25 November 2026

Production storage/transaction/ledger class bytes are unchanged from accepted0.2.0. The new option adds two bounded cached config reads at notification sites; it introduces no scans or IO. Existing benchmark GameTests reran in the all-equipped0.2.1 runtime (40 samples after12 warmups). These capture/plan/evidence results exclude SQLite/final intent encoding/settlement and remain host/JIT/order sensitive:

| Native Netherite Omega backpacks | Middle ms | Approx. p95 ms | Inspections | Binary NBT bytes |
| --- | ---: | ---: | ---: | ---: |
| 1 | 0.266125 | 0.300708 | 212 | 5,755 |
| 4 | 0.617000 | 0.699042 | 575 | 23,020 |

The unchanged shulker capture benchmark also reran; full logs contain its timings/allocations and current version label. These are macOS/NeoForge21.1.265 headless measurements, not a like-for-like throughput comparison to prior runs, an end-to-end latency guarantee or hosted-server MSPT qualification. Real simultaneous-player admission, world-save/SQLite FULL-commit contention and component-heavy workloads must be profiled on the actual host using RELEASE_0_2_1.md. No budget/default was increased. The following earlier sections retain their explicitly versioned measurement history.

---

# v0.2.0 performance evidence

Recorded **25 November 2026**. macOS ARM64 / Apple M4 Pro, Temurin Java 21.0.12, NeoForge 21.1.265, actual reference mods including Backpacks, Curios, Accessories and its normal Owo dependency. These are development measurements, not hosted Linux/full-modpack MSPT results.

## Backpack capture, partial planning and evidence

BackpackPerformanceGameTest fills actual 120-slot Netherite backpacks via native wrappers. Every slot holds Integer.MAX_VALUE wheat with Omega installed. One backpack contains 257,698,037,640 wheat; four contain 1,030,792,150,560. Each operation captures sources, counts stock with long arithmetic and prepares partial removal/evidence for a 230,400-item ceiling. The timed section does not apply edits, mutate/pay, perform SQL or encode the final combined intent JSON. Identity inspection includes installed equipment APIs. Twelve warmups precede 40 timed samples per scenario. Middle sorted sample and approximately p95 are reported; no pass/fail timing threshold.

| Hotbar backpacks | Middle ms | Approx. p95 ms | Planning inspections | Scanned binary NBT bytes |
| --- | ---: | ---: | ---: | ---: |
| 1 Netherite + Omega | 0.245416 | 0.295084 | 212 | 5,755 |
| 4 Netherite + Omega | 0.512041 | 0.568834 | 575 | 23,020 |

These values are from the final all-equipped rerun after test-only optional-discovery isolation. JIT/order/host noise remains. Byte counts describe serialized input, not peak/retained heap or all temporary allocations. No claims of zero allocation or guaranteed submillisecond server cost are made.

HotbarTransactionsGameTest separately exercises real Treasury/Mixins, native SQLite intent/outcome and bank settlement for mixed backpack/Storage/vanilla/loose sources. Two repeated successful requests each remove 230,400 items, then two asynchronous stale plans cancel (movement and newly introduced alias). Protected and nested contents remain unchanged. Existing NetheriteTransactionsGameTest also interleaves a second player's request while the first is pending; admission rejects it with no economic mutation. Neither is a real concurrent-player network/load profile.

Default work/byte budgets, one globally active transaction, bounded SQL queue and 250-ms admission interval remain unchanged. No per-item loops, persistent inventory cache, ticks or equipment polling are introduced. Each mutation validation performs fresh bounded identity checks; timings above cover planning only, so actual settlement adds full canonical comparisons, per-edit identity checks and account validation. SQL/FULL durability operations stay off the server thread, but disk contention and callback tick scheduling affect total response time.

Measure actual full-pack MSPT, p95/p99 request latency, main-thread preparation/settlement, heap/GC, local commit latency, disk/WAL pressure and concurrent player admission on staging. Component-heavy stored items can be more expensive than wheat. Scan/byte budgets bound accepted work/data, not arbitrary third-party component codec execution time. Do not raise protective limits from these microbenchmarks alone.

## Reproduction

Run runGameTestServer using README's exact integration properties; inspect [BC020 performance] lines plus required-test success. The benchmark fixtures live only in src/gameTest and are excluded from the addon. No production fixture command or utility was introduced. Measurements below are retained historical v0.1.2 evidence, not a like-for-like v0.2.0 comparison (eligibility and identity work changed).

---

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
