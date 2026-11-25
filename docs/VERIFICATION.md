# Verification report

Date: **25 November 2026**.

## Passed

- Read each supplied local JAR, SHA-256 hashed its bytes and CRC-tested every ZIP entry. Originals were not modified.
- Inspected Treasury action handling, screen behaviour, bank services, Numismatics standard/overflow balances, Sophisticated canonical storage, wrapper invalidation, oversized stack encoding and partition/upgrade behaviour.
- Compiled all addon and test sources using **Temurin javac 21.0.12**, `--release 21`, against prepared **Minecraft 1.21.1 / NeoForge 21.1.265** classes and the four original JARs.
- Transaction suite: **2,327 checks**, including complete-lot bounds, partial writes, payment failure before/after credit, compensation order, stale plans and uncertain rollback.
- Original-JAR structural compatibility suite: **16 checks**, including the exact Treasury method/invocation descriptor, guard order before the injection, account fields and canonical storage APIs.
- Vanilla adapter suite: **11 checks** on actual Minecraft item stacks/inventory/components. Includes loose plus boxed contents, exact component matching, slot-spanning removal, name preservation, untouched nonmatches, offhand boxes, configuration switch, exact rollback and oversized-layout rejection.
- Sophisticated canonical-schema suite: **13 checks** on actual Minecraft registries/NBT and the production adapter planner. Includes oversized integer counts, copy-only planning, exact cross-slot reductions, nonmatching-entry preservation, UUID alias rejection, slot validity, partition exclusion, scan bound, non-stack-upgrade rejection and underflow without mutation.
- Generated addon and source archives contain only this project's code/resources; no original dependency code or JARs.

The Minecraft-item suites bootstrap an empty LoadingModList to provide the loader state required by the cached NeoForge classes. This is an isolated test harness, not a running modpack or world. The Sophisticated suite does not instantiate a live ItemContentsStorage world or register real stack-upgrade items; its positive oversized-stack/schema tests and conservative rejection tests must be complemented by the real upgrade and persistence acceptance cases.

## Not established

- **Full Gradle build:** Gradle 9.2.1 is locally cached, but the sandbox denied its local file-lock socket before configuring the project. The candidate JAR was packaged by the documented Java 21 fallback. The Gradle tasks/settings are supplied for normal development and must be run in the writable project environment.
- **Live Mixin application and full dedicated-server startup.** Structural compatibility checks are not a substitute.
- **Real Sophisticated storage/stack upgrades, cross-player aliases and restart persistence.** These require the staging checklist.
- **Network handshake with clients that lack this addon**, full-modpack compatibility, throughput/MSPT under load and production suitability.

## Workspace limitation

The supplied project/dependencies path is readable. A temporary write probe in the project root failed with `Operation not permitted`; no file was created there. Implementation and Git initialisation therefore took place in the task workspace, without changing the original directory. Extract the source package into the intended project once it is opened as a writable workspace. Dependency files remain at the user-supplied location and are excluded from source archives/Git.

## Reference fingerprints

| Original filename | SHA-256 |
| --- | --- |
| CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar | d33a33b46f923462ae6806a024851afaefa473ced0e95bf405f8ebc13abce767 |
| numismatics_treasury-1.0.2.jar | fde8264ff0e17792b6160e6c41c6ea0b19e5de1343ddddcdf13b5a0bddba46e2 |
| sophisticatedcore-1.21.1-1.5.7.2381.jar | 33ff373cd62e2bef84ca5dd18ffea1c7508842a3156d021f1a0101063f69fb39 |
| sophisticatedstorage-1.21.1-1.6.2.2159.jar | bd6cfe04f5062e1ac860f0e8e858185f037b3a20ef3ee387c8f895839781ac69 |

Re-run `scripts/build_offline.py` for a machine-readable `build/verification.json` receipt with the candidate JAR's hash.
