# Demonstrated recipient rebuild procedure

26 November2026. Approved application-recombination permission is in LICENSE. Official fingerprints and transaction code remain unchanged. This document records a **source-class rebuild and runtime recombination**, not a successful clean full-native-project Gradle build.

## Source and inputs

Use the accompanying exact-source archives for Numismatics b4cbb96f5cd2fbec80fdbb2f6f3cc3abcd4f6bf5 and Curios6b122c8a7e2b514fd94197459ade11f19a0bfb09, plus the matching complete Foundry source archive. Every777 Numismatics and248 Curios tracked file is included; archive bytes are checked against Git blob IDs. Native build scripts, wrappers, source, resources, copyright and supplied licence files are retained. Archives are pristine; modifications are separate patches. SHA256/version manifests identify the binaries, sources and test changes. No production database, secret or signing credential is required.

Obtain Java21 and the original pinned native binaries/compile references lawfully, including Create6.0.10 and the coherent Sophisticated cohort. Original non-LGPL dependency JARs are not redistributed. Use the original reference filenames in the recipient dependency directory to match the existing build selection. Use normal Gradle repositories/caches; no proprietary build key is required.

## 1. Establish the Java21 classpath

From a disposable copy of the Foundry source, run its Gradle9.2.1 wrapper with the existing documented dependency path properties and runtime test dependencies, adding:

```sh
./gradlew -I docs/licensing/RECIPIENT_CLASSPATH.init.gradle \
  -PcreateJar=/path/to/create-1.21.1-6.0.10.jar \
  -PwithCurios -PwithAccessories -PowoJar=/path/to/owo-lib.jar \
  writeRecipientClasspaths
```

This writes build/recipient/compile-classpath.txt. As for the normal build, supply -PdependencyDir, -PcoreJar, -PstorageJar, -PbackpacksJar, -PcuriosJar and -PaccessoriesJar when references are not under dependencies/. Retain their original exact digests while preparing the initial classpath. Add the normal Create embedded Ponder/Catnip library to the classpath: extract META-INF/jarjar/ponder-neoforge-1.0.82+mc1.21.1.jar from the lawful Create6.0.10 JAR into a temporary directory. The recorded run also supplied its embedded Flywheel/Registrate JARs. These are external native build inputs, not supplied redistribution payloads. The exported path list plus these entries is the `RECIPIENT_CP` below; use the platform path separator (colon on macOS/Linux). No server-thread/database operation participates.

## 2. Apply and compile actual authored source changes

Apply patches/numismatics-recipient-marker.patch from the Numismatics source root and patches/curios-recipient-marker.patch from the Curios root (`patch -p0 < /path/to/the.patch`). Each adds only a public marker method. The Numismatics patch also maps the common-source Fabric Environment/EnvType annotation to the matching NeoForge OnlyIn/Dist annotation, preserving client-only scope as in the native NeoForge artifact. These are genuine authored upstream sources, not decompiled or fabricated API stubs. Preserve upstream headers and the modification/date comments in the patches.

```sh
javac --release 21 -proc:none -encoding UTF-8 -classpath "$RECIPIENT_CP" \
  -d /path/to/numismatics-classes \
  /path/to/numismatics-source/common/src/main/java/dev/ithundxr/createnumismatics/content/backend/BankAccount.java
javac --release 21 -proc:none -encoding UTF-8 -classpath "$RECIPIENT_CP" \
  -d /path/to/curios-classes \
  /path/to/curios-source/neoforge/src/main/java/top/theillusivec4/curios/api/CuriosApi.java
python3 docs/licensing/recipient_overlay.py /path/to/original-numismatics.jar \
  /path/to/numismatics-classes /path/to/recipient-dependencies/CreateNumismatics-1.1.0+neoforge-mc1.21.1.jar
python3 docs/licensing/recipient_overlay.py /path/to/original-curios.jar \
  /path/to/curios-classes /path/to/recipient-dependencies/curios-neoforge-9.5.1+1.21.1.jar
```

The overlay retains every unaffected archive entry, including native metadata and notices, and replaces the source-compiled class family. The original JAR is never overwritten. Copy the unchanged Treasury reference into the recipient dependency directory. This scoped demonstration does not rebuild all other native classes. For a full native rebuild use the complete upstream projects and their wrappers/tasks, rather than claiming this overlay is such a build. Source modification rights are not limited to these marker examples.

## 3. Rebuild the recipient application with exact replacement references

Compute each new JAR's SHA256. Change the Numismatics and Curios expected digests only in **your copy** of build.gradle and RuntimeCompatibility.java. The supplied recipient-application-exact-digests.patch records the demonstrated output hashes, not a universal hash for other compiler outputs: substitute your actual hashes if they differ. Recompression/timestamps and other build inputs can change digests without changing API semantics. Retain all unrelated pins, arithmetic, hotbar, identity, extraction, ledger and compensation checks. There is no official wildcard/bypass option.

Run Java21 `./gradlew build runGameTestServer` with the same exact dependency properties, now selecting recipient dependencies and the modified Curios JAR. To verify the added source methods loaded, copy patches/RecipientRebuildGameTest.java.example into the recipient src/gameTest package as RecipientRebuildGameTest.java. The recorded run passed2424 executable checks and13 required actual GameTests, including both marker checks, native equipment/containers, transaction payment and ledger cases. It ran on macOS/NeoForge21.1.265 in a disposable world, not a live server. Keep logs/modification records and provide adopted permissions/source materials when conveying recombinations. Identify forks accurately, without implying upstream/Foundry endorsement. Do not impose an official-approval requirement on these rights.

## 4. Negative verification and upstream wrapper limit

In the disposable recipient copy only, restored original RuntimeCompatibility source was tested first with modified Numismatics, then with original Numismatics plus modified Curios. Each isolated one-test headless server rejected the respective digest, left ledger/player/transaction admission closed, and passed the failure-containment assertion. These are two independent runtime checks, not merely calculated hash inequalities. No unknown library was accepted by the official verifier.

Full upstream wrappers were retried26 November2026: Curios requires Gradle8.8 and Numismatics8.12; both stopped at UnknownHostException for services.gradle.org because these distributions are uncached. No full native Gradle build or bit-identical native artifact reproduction is claimed. On a normally connected machine, use the complete supplied native wrapper projects and their documented :neoforge:build tasks; no publish/upload task is necessary. Native toolchain versions differ from the addon test runtime and are retained in their build files. This environmental limitation does not erase the demonstrated source-class/recombination result.

The final materials package includes pristine upstream/application source, source patches, complete licences, manifests and validation evidence. Beside any approved binary download, publish equivalent no-charge source access directions and maintain availability. Treasury clarification remains pending; no new official binary or publication is authorized by this procedure.
