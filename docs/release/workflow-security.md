# Workflow Security

The release workflows use four GitHub environments:

| Environment | Allowed branch | Purpose |
| --- | --- | --- |
| `Release Approval` | `main` | One approval gate before a release starts publishing |
| `Production` | `main` | Release publishing and docs promotion |
| `Snapshot` | `main` | Unattended nightly snapshot publication |
| `Automation` | `main` | Post-merge API baseline updates |

All environments disallow administrator bypass. `Release Approval` allows
`dautovicharis` or `hdcodedev` to review. Self-approval is currently allowed
because both accounts are operated by the same person.

## Secret Migration

Repository and organization secrets remain available to a modified workflow
outside an environment. Copy the current values into the environments below,
verify one release and snapshot, and then remove the repository/organization
copies.

### Production

- `AWS_ROLE_ARN`
- `CHARTS_WORKFLOW_TOKEN`
- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY_ID`
- `SIGNING_PASSWORD`
- `SIGNING_IN_MEMORY_KEY`
- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_STORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`

### Snapshot

- `AWS_ROLE_ARN`
- `CHARTS_WORKFLOW_TOKEN`
- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY_ID`
- `SIGNING_PASSWORD`
- `SIGNING_IN_MEMORY_KEY`
- `ANDROID_RELEASE_KEYSTORE_BASE64`
- `ANDROID_RELEASE_STORE_PASSWORD`
- `ANDROID_RELEASE_KEY_ALIAS`
- `ANDROID_RELEASE_KEY_PASSWORD`

### Automation

- `CHARTS_WORKFLOW_TOKEN`

The AWS trust policy should accept tokens only for the `Production` and
`Snapshot` environment subjects. The cross-repository token should eventually
be replaced with a short-lived GitHub App installation token.

## Repository Policy

`.github/CODEOWNERS` identifies the default repository owners, but code-owner
approval is not required. The default-branch ruleset requires changes to use a
pull request, resolves review conversations, allows squash merging only, and
requires all five PR validation jobs. It does not require an approving review
or approval after the last push.

External actions use major-version tags. Dependabot keeps those action versions
current through reviewed pull requests.
