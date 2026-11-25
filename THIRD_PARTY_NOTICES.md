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
