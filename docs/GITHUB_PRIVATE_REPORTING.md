# Private reporting setup — owner instructions only

26 November2026. No GitHub settings or reports changed.

Use the intended repository with owner/admin/security rights. In Settings → Security and quality → Advanced Security, enable Private vulnerability reporting. With a separate non-admin account, confirm Security (Security and quality) → Advisories → Report a vulnerability shows the private form and SECURITY.md. Submit a harmless “private reporting test — no vulnerability”, check maintainer notification/private reply, and verify an unrelated account or signed-out session cannot access it. Ensure it did not create a public issue. Close/dismiss the test privately; do not publish an advisory, create a CVE or publish a fork.

This procedure sends a notification and is for the owner/authorised tester to perform later. GitHub documents the route for public repositories; do not assume it can be enabled/tested while the repository is private. At an approved publication stage, use an approved non-sensitive public placeholder/policy repository to activate/test before uploading mod source/binaries, or establish a real monitored private contact before visibility changes. No placeholder/publication is authorized here. No security mailbox has been invented. If no private route exists, request a contact without revealing exploit details.

References: https://docs.github.com/en/code-security/how-tos/report-and-fix-vulnerabilities/configure-vulnerability-reporting/configure-for-a-repository ; https://docs.github.com/en/code-security/how-tos/report-and-fix-vulnerabilities/report-privately . Activation and access checks remain unperformed.
