---
title: Versioning
order: 7
---

# Versioning

Axion resolves the version from Git tags (`scmVersion` in `build.gradle.kts`). The
`chartsVersionIncrementer` property in `gradle.properties` picks how the next version grows:
`incrementMajor`, `incrementMinor`, or `incrementPatch`.

Axion reads only the tags already in the clone. The build turns off its `unshallowRepoOnCI` fetch,
so a CI job that needs the version checks out with `fetch-depth: 0`. A shallow clone has no tags and
builds as `0.1.0-SNAPSHOT`.

After a release tag `X.Y.Z`, the next development version is:

| Incrementer | Next version |
|---|---|
| `incrementMajor` | `(X+1).0.0-SNAPSHOT` |
| `incrementMinor` | `X.(Y+1).0-SNAPSHOT` |
| `incrementPatch` | `X.Y.(Z+1)-SNAPSHOT` |

The pending version also depends on the incrementer. Change it only after the release tag is
pushed, or the release ships under a different version.

`./gradlew -q currentVersion` prints the pending version. **Release** publishes it without the
`-SNAPSHOT` suffix, and release notes use the same number.

## Patch Releases

There is no patch release process yet. **Release** runs only from `main`, so a patch line from a
hotfix branch needs changes to `release.yml` and to the environment branch rules.
