import org.gradle.api.Task
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.build.config) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.axion.release)
    id("charts.api-compatibility")
}

val versionCatalog =
    extensions
        .getByType(VersionCatalogsExtension::class.java)
        .named("libs")

scmVersion {
    tag {
        prefix.set("")
    }
    versionIncrementer(
        providers.gradleProperty("chartsVersionIncrementer").get(),
    )
}

val chartsReleaseVersion = providers.gradleProperty("chartsReleaseVersion").orNull?.takeIf { it.isNotBlank() }
val isCompositeIncludedBuild = gradle.parent != null
version =
    when {
        chartsReleaseVersion != null -> chartsReleaseVersion
        isCompositeIncludedBuild -> "dev-local"
        else -> scmVersion.version
    }

buildscript {
    val buildscriptVersionCatalog =
        project.extensions
            .getByType(VersionCatalogsExtension::class.java)
            .named("libs")

    // Force patched vulnerable transitives on the Gradle plugin classpath (AGP/UTP transitives).
    configurations.configureBuildscriptSecurityOverrides(buildscriptVersionCatalog)
}

// Keep Kotlin/JS transitive dependencies patched in kotlin-js-store/yarn.lock.
configureJsSecurityOverrides(versionCatalog)

// Root project only needs the ktlint/logback override; commons-lang3 is enforced in subprojects.
configurations.configureProjectSecurityOverrides(
    versionCatalog = versionCatalog,
)

fun Task.isGradleSignTask(): Boolean {
    var taskClass: Class<*>? = javaClass
    while (taskClass != null) {
        if (taskClass.name == "org.gradle.plugins.signing.Sign") return true
        taskClass = taskClass.superclass
    }
    return false
}

val verifySigningKey =
    tasks.register<Exec>("verifySigningKey") {
        group = "verification"
        description =
            "Verifies the in-memory PGP signing key before signing " +
            "(see .github/scripts/verify-signing-key.sh)."

        val verificationScript =
            layout.projectDirectory
                .file(".github/scripts/verify-signing-key.sh")
                .asFile

        commandLine("bash", verificationScript.absolutePath, "--required")
    }

@OptIn(KotlinNativeCacheApi::class)
subprojects {
    version = rootProject.version

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
            android.set(true)
            ignoreFailures.set(false)
        }
    }

    // Fail before any artifact is signed with an expired, revoked, invalid, or
    // missing in-memory key. Ordinary local builds are unaffected because they
    // do not schedule Gradle signing tasks.
    //
    // The `Sign` task type is not available on this script's compile classpath,
    // and Gradle decorates task implementations at runtime. Walk the runtime
    // class hierarchy so both `Sign` and generated `Sign_Decorated` tasks match.
    plugins.withId("signing") {
        tasks.configureEach {
            if (isGradleSignTask()) {
                dependsOn(verifySigningKey)
            }
        }
    }

    configurations.configureProjectSecurityOverrides(
        versionCatalog = versionCatalog,
        includeCommonsLang = true,
    )

    // Compose UI tests in a browser may intentionally wait up to three seconds for
    // a state change. Karma/Mocha's two-second default therefore aborts valid Wasm
    // tests before their own timeout can produce a useful result.
    tasks.withType<org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest>().configureEach {
        if (!name.startsWith("wasmJs")) return@configureEach

        onTestFrameworkSet {
            (
                this as? KotlinKarma
            )?.useConfigDirectory(
                rootProject.layout.projectDirectory
                    .dir("karma.config.d")
                    .asFile,
            )
        }
    }

    // Hosted macOS can supply Kotlin/Native dependency caches built with a newer
    // iOS Simulator SDK than this project's iOS deployment target. Linking those
    // caches then fails on symbols unavailable to the deployment target.
    //
    // Disable only simulator *test* binary caches; this does not change published
    // libraries or device targets. Re-enable caching after a Kotlin/Compose update
    // provides deployment-target-compatible caches (or after an intentional minimum
    // iOS version increase). The version marker makes that review mandatory on
    // Kotlin upgrades.
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        if (path !in ChartsModules.library) return@withId

        extensions.configure<KotlinMultiplatformExtension>("kotlin") {
            targets.withType<KotlinNativeTarget>().configureEach {
                if (name != "iosSimulatorArm64") return@configureEach

                binaries.withType<TestExecutable>().configureEach {
                    disableNativeCache(
                        version = DisableCacheInKotlinVersion.`2_4_10`,
                        reason =
                            "Hosted macOS caches can target a newer simulator SDK than the test deployment target.",
                    )
                }
            }
        }
    }
}

tasks.register("chartsTestJvm") {
    group = "verification"
    description = "Runs JVM tests for all chart modules"
    dependsOn(ChartsModules.library.map { "$it:jvmTest" })
}

tasks.register("chartsTestIos") {
    group = "verification"
    description = "Runs iOS simulator tests for all chart modules"
    dependsOn(ChartsModules.library.map { "$it:iosSimulatorArm64Test" })
}

tasks.register("chartsTestWasm") {
    group = "verification"
    description = "Runs Wasm tests for all chart modules"
    dependsOn(ChartsModules.library.map { "$it:wasmJsTest" })
}

tasks.register("chartsTestAndroid") {
    group = "verification"
    description = "Runs Android instrumented tests for chart modules"
    dependsOn(
        ChartsModules.library
            .filter { it != ":charts-core" }
            .map { "$it:connectedAndroidTest" },
    )
}

tasks.register("updateScreenshots") {
    group = "HDCharts"
    description = "Updates Android screenshot test baselines (debug variant)"
    dependsOn(":androidApp:updateDebugScreenshotTest")
}

tasks.register("chartsCheck") {
    group = "HDCharts"
    description = "Build and tests for the HDCharts project"
    dependsOn(getTasksByName("ktlintCheck", true))
    dependsOn("build")
    dependsOn("chartsTestJvm")

    tasks.findByName("build")?.mustRunAfter(getTasksByName("ktlintCheck", true))
    tasks.findByName("chartsTestJvm")?.mustRunAfter("build")
}

tasks.register<Exec>("buildSrcKtlintCheck") {
    group = "verification"
    description = "Runs ktlintCheck for buildSrc Kotlin code and scripts"
    commandLine("./gradlew", "-p", "buildSrc", "ktlintCheck")
}

tasks.register<Exec>("buildSrcKtlintFormat") {
    group = "formatting"
    description = "Runs ktlintFormat for buildSrc Kotlin code and scripts"
    commandLine("./gradlew", "-p", "buildSrc", "ktlintFormat")
}

tasks.named("ktlintCheck").configure {
    dependsOn("buildSrcKtlintCheck")
}

tasks.named("ktlintFormat").configure {
    dependsOn("buildSrcKtlintFormat")
}

tasks.register("publishChartsModules") {
    group = "publishing"
    description = "Publishes all HDCharts modules and BOM to the configured Maven repository"
    dependsOn(ChartsModules.publishable.map { "$it:publish" })
}

tasks.register("publishChartsModulesToMavenLocal") {
    group = "publishing"
    description = "Publishes all HDCharts modules and BOM to Maven Local"
    dependsOn(ChartsModules.publishable.map { "$it:publishToMavenLocal" })
}

tasks.register<Sync>("generateWebDemo") {
    group = "HDCharts"
    description = "Builds the Wasm web app and copies files to docs/static/demo/<target-version>"

    val docsVersionDir =
        providers.provider {
            if (project.version.toString().endsWith("-SNAPSHOT")) "snapshot" else project.version.toString()
        }

    // Only the leaf demo app distribution is needed for docs/static/demo.
    dependsOn(":app:wasmJsBrowserDistribution")
    from(layout.projectDirectory.dir("app/build/dist/wasmJs/productionExecutable"))
    into(docsVersionDir.map { layout.projectDirectory.dir("docs/static/demo/$it") })

    doLast {
        logger.lifecycle("✅ Wasm web demo updated (${project.version})")
    }
}

tasks.register("generateApiDocs") {
    group = "HDCharts"
    description = "Generate Dokka API reference to docs/static/api/<target-version>"

    dependsOn("charts:dokkaGenerate")
}

tasks.register("generateDocs") {
    group = "HDCharts"
    description = "Generate Dokka API docs and Wasm web demo to docs/static/"

    dependsOn("generateApiDocs")
    dependsOn("generateWebDemo")

    doLast {
        logger.lifecycle("✅ Docs updated (${project.version})")
    }
}

tasks.register("listDocsGifScenarios") {
    group = "HDCharts"
    description = "Lists available docs GIF scenarios discovered via @RecordGif in :androidApp"
    dependsOn(":androidApp:listGifScenarios")
}

tasks.register("recordDocsGif") {
    group = "HDCharts"
    description =
        "Records one docs GIF scenario to <gifContentRoot>/<gifDocsVersion>/wiki/assets (set -PgifScenario=<name>, defaults to first)"
    dependsOn(":androidApp:recordGifDebug")
}

tasks.register("recordDocsGifs") {
    group = "HDCharts"
    description =
        "Records all docs GIF scenarios to <gifContentRoot>/<gifDocsVersion>/wiki/assets (default version: snapshot)"
    dependsOn(":androidApp:recordGifsDebug")
}

tasks.register("validateDocsGifBaselines") {
    group = "HDCharts"
    description = "Validates Android docs GIF output against gif-baselines"
    dependsOn(":androidApp:validateGifBaselines")
}

// CI entry points for reusable workflows.
// The `chartsTest*` tasks are platform-specific commands for local use.
// The `ciTest*`, `ciCompile`, and `ciAssemble` tasks are CI entry points;
// they define the exact scope invoked by the reusable workflows.
// The `smoke-line` consumer compile belongs only to `ciCompile`, not to a test task.

tasks.register("ciTestJvm") {
    group = "CI"
    description = "CI entry point for JVM tests"
    dependsOn("chartsTestJvm")
}

tasks.register("ciTestAndroid") {
    group = "CI"
    description = "CI entry point for Android tests"
    dependsOn(":androidApp:validateDebugScreenshotTest")
    dependsOn("chartsTestAndroid")
}

tasks.register("ciTestWeb") {
    group = "CI"
    description = "CI entry point for Wasm browser tests"
    dependsOn("chartsTestWasm")
}

tasks.register("ciTestIos") {
    group = "CI"
    description = "CI entry point for iOS simulator tests"
    dependsOn("chartsTestIos")
}

tasks.register("ciCompile") {
    group = "CI"
    description = "Compiles all CI targets without packaging outputs"
    dependsOn(ChartsModules.ciKmpCompile.map { "$it:compileKotlinJvm" })
    dependsOn(ChartsModules.ciKmpCompile.map { "$it:compileKotlinWasmJs" })
    dependsOn(ChartsModules.ciAndroidCompile.map { "$it:compileAndroidMain" })
    dependsOn(":smoke-line:compileKotlinJvm")
}

tasks.register("ciAssemble") {
    group = "CI"
    description = "Assembles CI validation artifacts using dev/debug outputs"
    dependsOn(ChartsModules.library.map { "$it:jvmJar" })
    dependsOn(":charts-bom:assemble")
    dependsOn(ChartsModules.ciAndroidCompile.map { "$it:assembleAndroidMain" })
    dependsOn(":app:wasmJsBrowserDevelopmentExecutableDistribution")
    dependsOn(":smoke-line:assemble")
}
