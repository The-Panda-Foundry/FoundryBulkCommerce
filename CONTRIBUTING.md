# Contributing to Foundry: Bulk Commerce

Thank you for helping The Panda Foundry improve transaction safety and compatibility.

For ordinary defects and suggestions, use the issue templates. Include exact mod/loader versions, minimal reproduction steps, expected/actual results and redacted diagnostics. Follow SECURITY.md for exploitable issues. Do not submit production ledgers/worlds, secrets, third-party JARs or copied upstream implementation/assets without permission.

Source visibility does not make this an open-source project. Discuss proposed code changes and written contribution/licensing terms with the owner **before** submitting a patch or pull request. No contributor licence agreement or broad inbound rights transfer is implied by this document. Do not submit work you cannot license; third-party material must have identified provenance and suitable permissions.

Keep changes focused. Preserve hotbar eligibility, primary-only extraction, identity checks, exact matching, checked arithmetic, verified payment/compensation, the persistent ledger and conservative recovery. Obtain architectural approval before broad changes. No automatic reconciliation, remote/nested selling or background scans are authorized by an issue request.

Use Java21 and the checked-in Gradle wrapper. Follow README.md for legal acquisition of exact dependency references. Run `./gradlew build` and relevant actual-mod GameTests; describe unperformed live/host tests honestly. Test fixtures belong only in src/test or src/gameTest, never the production source set. Add meaningful regression coverage for new behaviour and document risks/configuration changes.

Disclose material AI assistance and independently review its output. Do not claim tests, authorship or compatibility that has not been established. Maintainers decide whether to accept, release or deploy changes; a contribution does not authorize publication or deployment.
