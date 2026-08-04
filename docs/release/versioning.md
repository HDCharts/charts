# Versioning

Release versions are resolved by Axion from Git tags.

The root Gradle build currently uses:

```kotlin
scmVersion {
    tag {
        prefix.set("")
    }
    versionIncrementer(providers.gradleProperty("chartsVersionIncrementer").get())
}
```

The increment strategy comes from the `chartsVersionIncrementer` property in
`gradle.properties` (`incrementMajor`, `incrementMinor`, or `incrementPatch`).
The build fails if the property is missing. This means the normal development
line increments the minor version after a release tag.

Example:

1. Before release, `./gradlew -q currentVersion` resolves `2.3.0-SNAPSHOT`.
2. `Release` publishes and tags `2.3.0`.
3. After the `2.3.0` tag exists, Axion resolves the next development version as
   `2.4.0-SNAPSHOT`.

Manual release workflows resolve the version from Axion at runtime
(`./gradlew -q currentVersion`). Release-note synchronization uses that
Axion-resolved snapshot version as the target release marker.

## Patch Releases

Patch releases are not currently selectable from the release workflow.

With `incrementMinor`, a release from `2.3.0-SNAPSHOT` produces `2.3.0`, and the
next development version becomes `2.4.0-SNAPSHOT`. It does not automatically
create `2.3.1-SNAPSHOT`.

Before cutting patch or hotfix releases, choose and document the intended
strategy. The incrementer is already configurable via the `chartsVersionIncrementer`
property in `gradle.properties`; setting it to `incrementPatch` selects a patch
line. Other options are:

- Keep `incrementMinor` and cut patches from a dedicated hotfix branch with its
  own versioning rules.
- Use a dedicated hotfix branch policy where patch releases are prepared from a
  release branch with its own versioning rules.

Until a patch/hotfix policy is documented, treat the release workflow as
minor-line release automation.
