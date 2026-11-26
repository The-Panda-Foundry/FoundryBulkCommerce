# Third-party notices

Recorded 25 November 2026.

The production addon embeds **Xerial SQLite JDBC 3.51.3.0** as the unmodified Maven artifact `org.xerial:sqlite-jdbc:3.51.3.0` in NeoForge Jar-in-Jar. SHA-256: `3684ac5e9bd893cbe68eb0ac1708343f66be829b1d2b5ab317e783d05acd5aef`.

- Project/source and license: https://github.com/xerial/sqlite-jdbc
- Maven artifact: https://repo.maven.apache.org/maven2/org/xerial/sqlite-jdbc/3.51.3.0/
- Xerial wrapper: Apache License 2.0 (see embedded artifact's META-INF licenses); SQLite engine: public domain. Native components and upstream notices are retained unmodified inside the artifact. The addon's All Rights Reserved license does not replace third-party terms.
- SQLite durability reference: https://www.sqlite.org/pragma.html#pragma_synchronous
- WAL requirements: https://www.sqlite.org/wal.html
- NeoForge development library handling: https://github.com/neoforged/ModDevGradle#external-dependencies-runs

The four user-supplied compatibility mod JARs and Create remain external dependencies and are not distributed. Minecraft/NeoForge are provided through the standard development toolchain, not repackaged into this addon. No MongoDB driver is bundled.


## Publication audit — 26 November 2026

The Foundry All Rights Reserved notice covers original Foundry material only. It does not supersede the following terms. This is an interoperability project, not an upstream fork; no upstream mod classes, JARs, textures or logos are included in the production addon. Download native dependencies from their official projects. Public source visibility does not authorize copying third-party implementations.

| Component | Evidence / treatment |
| --- | --- |
| Xerial SQLite JDBC3.51.3.0 | Only embedded third-party JAR. Its Apache2.0 licence and Zentus notice are retained inside the unchanged artifact and reproduced under licenses/. SQLite engine is public domain; upstream component notices still apply. |
| Gradle wrapper | Tooling JAR/scripts, not a dependency mod. Apache2.0 headers/embedded text retained; copy under licenses/. |
| Create: Numismatics1.1.0 | External API; exact JAR declares LGPL, upstream project identifies LGPL3.0. No Numismatics classes/assets are bundled. See review gate below. Source: https://github.com/Layers-of-Railways/CreateNumismatics |
| Curios9.5.1 | Optional external API; exact JAR declares LGPL3.0-or-later. No Curios classes/assets are bundled. Source: https://github.com/TheIllusiveC4/Curios |
| Treasury1.0.2 | Exact JAR declares All Rights Reserved. Both current Modrinth and CurseForge1.0.2 version pages display the project-level MIT licence, contradicting the local JAR and current repository ARR notice; which terms apply to the pinned artifact remains unresolved. Owner must confirm applicable interoperability/attribution terms before publication. Source declared by JAR: https://github.com/Yassounou/numismatics_treasury |
| Sophisticated Core/Storage/Backpacks | Exact native JARs declare All Rights Reserved. Inspected for API/canonical-schema interoperability, not redistributed. Source: https://github.com/P3pp3rF1y/SophisticatedCore , https://github.com/P3pp3rF1y/SophisticatedStorage , https://github.com/P3pp3rF1y/SophisticatedBackpacks |
| Accessories | External optional API; pinned JAR declares MIT. Not bundled. Source: https://github.com/wisp-forest/accessories |
| Create | External native dependency; exact JAR carries LICENSE.md_create-1.21.1. Not bundled; use upstream terms/source: https://github.com/Creators-of-Create/Create |
| Minecraft / NeoForge | Normal development/runtime dependencies; no game assets or loader implementation redistributed in this addon. Consult their own terms. |

The Sophisticated author's public response supports separate-mod API integrations without modifying upstream files, subject to the native licences: https://github.com/P3pp3rF1y/SophisticatedBackpacks/discussions/1755 . This is useful evidence, not a bespoke grant for copying upstream code. The small native inventory layout formula is explicitly attributed in CanonicalContents and the BC-004 architecture document. No upstream implementation files or foreign mod classes were found in the tracked repository/artifact inspection; this does not establish legal clearance for every interoperability technique. Minecraft structure empty.nbt is an addon-created109-byte test fixture, not imported artwork.

### Outstanding licensing gate

ARR for original Foundry work is not automatically incompatible with every external API licence. However, LGPL3 combined-work conditions include notices and users’ ability to modify/relink libraries and debug those modifications. Strict runtime SHA verification deliberately rejects modified library builds. Whether this particular separately loaded addon is a Combined Work and what compliant relinking path is required has not been legally established. The owner should obtain suitable licensing advice/clarification for Numismatics/Curios and Treasury before public binary/source release. Do not weaken safety fingerprints as a speculative licensing fix. The original-work licence explicitly preserves third-party LGPL rights, but that sentence alone does not resolve technical compliance. No legal clearance or upstream endorsement is claimed.

Authoritative reference: https://www.gnu.org/licenses/lgpl-3.0-standalone.html . Licence text presence alone is not a complete provenance or rights audit. Future branding screenshots/coin/shulker artwork require their own review; no such assets are currently tracked.

## Proposed licensing resolution,26 November2026

Complete GPL3/LGPL3 texts and the exact Curios copyright notice are prepared in licenses/. This does not relicense original Foundry work or establish a completed compliance route. See docs/licensing/PROPOSAL.md, PROPOSED_EXCEPTION.md and SOURCE_MATERIALS.json. Active LICENSE is unchanged pending approval. Exact Treasury1.0.2 version-page evidence and an unsent developer request are in docs/licensing/TREASURY_CLARIFICATION_REQUEST.md. Recovered prior review: docs/RECOVERED_FINAL_REVIEW_2026-11-26.md.

## Approved source-recombination route —26 November2026

The root LICENSE now adopts the approved ARR application exception; original BankAccountAccessor/injected accessor definitions are additionally LGPL3-or-later. Complete pinned library sources/build files, copyright/MIT notices, GPL3/LGPL3 texts, patches and recipient instructions accompany the compliance materials. Source-class rebuild/recombination passed2424 checks and13 runtime GameTests; two official-verifier negative tests failed closed as expected. Full native wrappers could not download uncached Gradle due DNS. See docs/licensing/VALIDATION_2026-11-26.md. Earlier proposed/draft references above are historical, not the current licence status. Required source/notice access must actually accompany any approved distribution. Treasury clarification remains unsent/unresolved.
