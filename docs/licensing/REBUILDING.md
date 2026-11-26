# Recipient rebuild procedure — proposed, not executed

26 November2026. **Conditional on adoption of the proposed permission and provision of complete corresponding materials.** This is an engineering recipe for owner review; it is not a verified end-to-end modified-library build or permission currently granted by the active ARR text. No production files should be used.

## 1. Inputs

Obtain the exact release source revision and corresponding-source package, Java21, Gradle9.2.1 wrapper, ModDevGradle2.0.148, Minecraft1.21.1 mappings/runtime, NeoForge21.1.265 for the recorded test environment, and all native compile references listed in build.gradle. Keep untouched copies of official JARs and data. Download non-redistributed dependencies from their lawful sources; do not infer rights to publish Sophisticated/Treasury JARs from the Foundry exception. Optional integrations are runtime optional but their API references are compile inputs in this source tree.

For an unchanged-source reproduction, use the original exact cohort and economy/equipment references. `./gradlew build` invokes the executable regression harnesses. `./gradlew -PcreateJar=/path/to/create-1.21.1-6.0.10.jar -PwithCurios -PwithAccessories -PowoJar=/path/to/owo-lib.jar runGameTestServer` exercises the actual integrations. Put exact compile references in dependencies/ or supply the documented -P paths. A populated cache permits --offline; a clean machine needs lawful network access to the recorded repositories. The previous private /tmp SQLite Maven override is an environmental cache workaround, not a required undisclosed source input: standard build.gradle resolves org.xerial:sqlite-jdbc:3.51.3.0 from Maven Central. Clean-machine dependency resolution remains to be demonstrated. Verify Gradle/plugin/artifact provenance; do not require private credentials or local absolute paths.

## 2. Build the modified LGPL library

Pinned candidate source acquisition (instructions only; repositories/builds were not cloned/executed by this task):

```sh
git clone https://github.com/Layers-of-Railways/CreateNumismatics.git numismatics-source
git -C numismatics-source checkout --detach b4cbb96f5cd2fbec80fdbb2f6f3cc3abcd4f6bf5
git clone https://github.com/TheIllusiveC4/Curios.git curios-source
git -C curios-source checkout --detach 6b122c8a7e2b514fd94197459ade11f19a0bfb09
```

Preserve all required submodules/generated-input provenance and packaging changes. At those candidates Numismatics selects NeoForge21.1.228/Create6.0.10-280 and Curios selects NeoForge21.1.60; these native build toolchains differ from the addon test runtime21.1.265 and must be reproduced rather than silently replaced. Expected native build entry points are the projects' Gradle wrappers (Numismatics' NeoForge module, Curios' root build); verify their actual documented tasks/configuration before recording a successful recipe. Do not substitute latest branch heads. Candidate version metadata is evidence, not proof that the distributed JAR has no additional build inputs.


Start from the pinned native source commit/archive with its own build scripts/resources and licence; record your modification and its date. Build for MC1.21.1/NeoForge and the expected public interfaces. Source completeness/native reproduction must be confirmed before this procedure can be labelled reproducible. An API-compatible metadata-only modification is a useful first test, followed by a small genuine library source change. General library-modification rights are not limited to those examples.

## 3. Recombine your application copy

Compute the replacement JAR's complete SHA256 (`shasum -a 256 /path/to/replacement.jar` or `sha256sum`). In a **separate recipient source copy**, update only the exact expected references for the LGPL components you intentionally replaced:

| Component | Compile-time check | Runtime check |
| --- | --- | --- |
| Numismatics | build.gradle referenceHashes entry for CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar | RuntimeCompatibility.verifier fixed numismatics digest |
| Curios | build.gradle verifyIntegrationReferences Curios digest | RuntimeCompatibility.verifier fixed curios digest |

Preserve every unrelated fingerprint/cohort check. Keep replacement interfaces/version metadata compatible or explicitly adapt metadata/API bindings in the recipient copy; do not merely suppress failing Mixin/accessor tests. Retaining original dependency filenames is the simplest path through existing file selection; changing them requires updating the relevant build file selection and test inputs too. Replacement library version strings must satisfy metadata ranges in META-INF/neoforge.mods.toml or be deliberately adapted in that copy. A digest match alone is not compatibility proof.

No script or auto-patch changing these files is installed in the official repository by this proposal. The recipient recompiles their copy; the official binary remains strict. Leave quantity/payment/extraction/ledger/identity safeguards intact when making a compatibility-only fork. If a lawful library change needs a broader application adaptation, the proposed permission covers what is necessary rather than forcing a no-change API assumption.

## 4. Build, test and install in disposable staging

Run Java21 `./gradlew clean build` and relevant actual-mod GameTests using the replacement library. Preserve exact API/guard tests, rollback and ledger checks. Exercise integrated and dedicated startup, actual replacement class loading, hotbar/protected contents, aliases, partial/Omega extraction, payment, native storage persistence and manual-recovery containment. Do not mark skipped optional tests as active coverage. A deliberately unknown second digest must still be rejected. Save the modification diff, toolchain/dependency manifest, compile/test logs and resulting hashes. Install only the recipient addon and compatible replacement dependencies in a disposable world; never run different authoritative copies against a shared production ledger.

No host signature/secret is currently required to load an independently compiled addon. Installation information for a supplied User Product, if applicable, must be sufficient; public build instructions alone cannot substitute for a required unavailable key. Identify recipient builds as modified/unofficial, retain relevant notices and provide required materials/permissions to subsequent recipients. No new official release tag or binary is created by this guide.

## Acceptance before claiming a working section4(d)(0) route

A fresh recipient must be able to acquire all necessary materials, build the library and addon, load the recombined runtime, exercise transactions and retain downstream rights without asking for per-build permission. The proposal has not performed that demonstration. Current legal/source-material obstacles are in PROPOSAL.md and SOURCE_MATERIALS.json.
