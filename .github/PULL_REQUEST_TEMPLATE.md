## Summary

<!-- Describe what this PR changes and why. -->

-

## Local Validation

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
     ./gradlew chartsTestAndroidInstrumented
     ./gradlew chartsTestIos -->

<!-- Wasm browser tests are manual. When explicitly requested, use
     ./gradlew chartsTestWasm
     or ./gradlew ciTestWeb -PciTestModules=:charts-line -->
