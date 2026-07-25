# Versioning

Release versions are resolved by Axion from Git tags.

The root Gradle build currently uses:

```kotlin
scmVersion {
    tag {
        prefix.set("")
    }
    versionIncrementer("incrementMinor")
}
```

This means the normal development line increments the minor version after a
release tag.

Example:

1. Before release, `./gradlew -q currentVersion` resolves `2.3.0-SNAPSHOT`.
2. `Release` publishes and tags `2.3.0`.
3. After the `2.3.0` tag exists, Axion resolves the next development version as
   `2.4.0-SNAPSHOT`.
4. `Sync Version File` opens a PR to update `.version` to that new snapshot
   version.

Manual release workflows resolve the version from Axion at runtime.
Release-note synchronization reads `.version` as the checked-in target snapshot
marker.

## Patch Releases

Patch releases are not currently selectable from the release workflow.

With `incrementMinor`, a release from `2.3.0-SNAPSHOT` produces `2.3.0`, and the
next development version becomes `2.4.0-SNAPSHOT`. It does not automatically
create `2.3.1-SNAPSHOT`.

Before cutting patch or hotfix releases, choose and document the intended
strategy. Common options are:

- Change the project default to `versionIncrementer("incrementPatch")`.
- Add an explicit Gradle property override for the incrementer and wire release
  workflows to pass it.
- Use a dedicated hotfix branch policy where patch releases are prepared from a
  release branch with its own versioning rules.

Until one of those strategies is implemented, treat the release workflow as
minor-line release automation.
