# First public publication checklist

Prepared26 November2026. Current mod version0.2.3. This checklist is not publication/deployment approval.

## Owner decisions before public visibility

- [ ] Review the private pre-publication audit, including author identities and local paths in existing history. Sanitizing current documentation does not remove prior commits. No history rewrite was performed. Approve retaining existing history or separately authorize a specific remediation plan.
- [ ] Resolve Treasury1.0.2 applicable licence/interop terms; the exact historical JAR says ARR despite later MIT project listings.
- [ ] Resolve the LGPL combined-work/relinking assessment for Numismatics/Curios alongside exact runtime fingerprints. Review original-work LICENSE and third-party notices with suitable advice; no automatic legal clearance is claimed.
- [ ] Confirm the organisation/repository name, desired visibility, binary download/modpack permissions and contribution policy. Review AI_DEVELOPMENT.md.
- [ ] Enable GitHub private vulnerability reporting and test a private submission/maintainer notification before advertising SECURITY.md. Establish an owner-approved fallback private contact if desired; no fictional security mailbox is supplied.

## Technical publication checks

- [ ] Publish from the reviewed branch/commit; the historical main branch is older. Select which branches/tags are public deliberately. Do not mirror every ref or upload a full workspace/history bundle by default.
- [ ] Review the Git-tracked contents and latest audit before any push. Keep dependencies, builds, run worlds, raw audit/test logs, ledgers, credentials and private delivery bundles out of Git. Use GitHub secret scanning when available as an additional check, not proof of absence.
- [ ] Keep src/test and src/gameTest as reproducible source tests, separate from src/main. Do not publish the separate Commerce Fixtures project/JAR as part of this addon. Synthetic test helpers are not production tools.
- [ ] Verify official production0.2.3 SHA-256:2f082e085e8cf20e3b9e1fd55fe1a4589a66dcb2615a0d52c419a68e1dd9f7a8. The only embedded dependency is unchanged SQLite JDBC3.51.3.0. Supply third-party notices/licence texts alongside release distributions.
- [ ] Normal Java21 Gradle regression build and actual-mod GameTests must pass; distinguish these from target-host GUI, remote-client, load and crash acceptance. Consult existing staging/manual-recovery documents.
- [ ] Approve the final README, changelog, SECURITY, CONTRIBUTING, templates and ARR licence. Public source is not represented as open source. No copied upstream assets/implementation may be added without review.
- [ ] Only after owner approval, configure the actual GitHub remote and carry out a separately authorized push/publication. No push, repository creation, settings change, release tag or deployment is performed by this preparation task.

Licensing proposal and recipient-build/source-material checklist: licensing/PROPOSAL.md. No exception is adopted until owner approval. Private-report setup, including public-repository prerequisite: GITHUB_PRIVATE_REPORTING.md. Original history remains retained pending final owner approval.
