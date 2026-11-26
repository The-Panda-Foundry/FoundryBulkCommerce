# Security policy

## Report privately

Item/currency duplication, permission bypass, identity spoofing and transaction/ledger vulnerabilities must not be posted in public issues, discussions or comments.

Once enabled on the public repository, use **Security → Report a vulnerability** to submit a private GitHub vulnerability report to the maintainers. Include the addon/loader/dependency versions, a minimal disposable-world reproduction, expected/observed results and possible impact. Attach only the smallest redacted evidence needed; never upload production databases, complete inventories, passwords or account dumps. Do not exploit production servers or affect other players to demonstrate a report.

If that private-report button is unavailable, do not publish exploit details. Use an already-established private conversation with a project maintainer to request a secure reporting route before sharing evidence. No security email address is specified because an owner-approved mailbox has not been established. Enabling and testing GitHub private reporting is a publication prerequisite; adding this file does not enable that setting.

Maintainers assess reports privately, preserve evidence and coordinate remediation/disclosure. No response-time guarantee, paid bounty or blanket authorization for testing third-party servers is offered.

## Supported scope

Review targets the latest maintained release (currently0.2.3) on its exact validated dependency cohorts. Reports affecting older versions are welcome but do not imply continued fixes for every historical build. The addon’s ledger does not guarantee cross-resource disk atomicity. See docs/RECOVERY.md for containment and manual reconciliation; never clear evidence or automatically refund/replay uncertain transactions.
