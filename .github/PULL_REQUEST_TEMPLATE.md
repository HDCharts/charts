## Summary

<!-- Describe what this PR changes and why. -->

-

## Breaking Change

<!-- Describe the breaking change and required migration, or remove this section. -->

## Validation

<!-- Mark each local gradlew command run for this change. -->

- [ ] `./gradlew ktlintCheck`
- [ ] `./gradlew :charts-<module>:jvmTest`
- [ ] `./gradlew chartsTestJvm`
- [ ] `./gradlew ciCompile`
- [ ] `./gradlew chartsCheck`
- [ ] `./gradlew apiCompatibilityCheck`
- [ ] `./gradlew :androidApp:validateDebugScreenshotTest`
- [ ] `./gradlew updateScreenshots`

<!-- CI handles these — do not run locally; they are machine-dependent and slow:

     ./gradlew validateDocsGifBaselines
     ./gradlew chartsTestAndroid
     ./gradlew chartsTestIos
     ./gradlew chartsTestWasm -->
