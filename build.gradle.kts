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
    versionIncrementer("incrementMinor")
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

// Root project only needs ktlint/logback override; commons-lang3/guava are enforced in subprojects.
configurations.configureProjectSecurityOverrides(
    versionCatalog = versionCatalog,
)

subprojects {
    version = rootProject.version

    tasks.matching { it.name == "jsBrowserTest" }.configureEach {
        doFirst {
            rootProject.patchKarmaMinimatchCompatibility()
        }
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
        android.set(true)
        ignoreFailures.set(false)
    }

    configurations.configureProjectSecurityOverrides(
        versionCatalog = versionCatalog,
        includeCommonsAndGuava = true,
    )
}

tasks.register("chartsTest") {
    group = "Charts"
    description = "Relevant tests for the charts project"
    dependsOn("charts:jvmTest")
    dependsOn(project(":androidApp").tasks.named("validateDebugScreenshotTest"))
    dependsOn("chartsModulesTest")
}

tasks.register("chartsModulesTest") {
    group = "Charts"
    description = "Runs JVM tests for all modular chart artifacts and the umbrella module"
    dependsOn(ChartsModules.library.map { "$it:jvmTest" })
    dependsOn("smokeLineCompile")
}

tasks.register("smokeLineCompile") {
    group = "Charts"
    description = "Smoke compile of a module that depends only on charts-line"
    dependsOn(":smoke-line:compileKotlinJvm")
}

tasks.register("updateScreenshots") {
    group = "Charts"
    description = "Updates Android screenshot test baselines (debug variant)"
    dependsOn(":androidApp:updateDebugScreenshotTest")
}

tasks.register("chartsCheck") {
    group = "Charts"
    description = "Build and tests for the charts project"
    dependsOn(getTasksByName("ktlintCheck", true))
    dependsOn("build")
    dependsOn("chartsTest")

    tasks.findByName("build")?.mustRunAfter(getTasksByName("ktlintCheck", true))
    tasks.findByName("chartsTest")?.mustRunAfter("build")
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
    description = "Publishes all charts modules and BOM to the configured Maven repository"
    dependsOn(ChartsModules.publishable.map { "$it:publish" })
}

tasks.register("publishChartsModulesToMavenLocal") {
    group = "publishing"
    description = "Publishes all charts modules and BOM to Maven Local"
    dependsOn(ChartsModules.publishable.map { "$it:publishToMavenLocal" })
}

tasks.register<Sync>("generateJsDemo") {
    group = "Charts"
    description = "Builds the JS app and copies files to docs/static/demo/<target-version>"

    val docsVersionDir =
        providers.provider {
            if (project.version.toString().endsWith("-SNAPSHOT")) "snapshot" else project.version.toString()
        }

    // Only the demo app distribution is needed for docs/static/demo.
    // Depending on all jsBrowserDistribution tasks triggers unnecessary production JS builds
    // in every module and can make generateDocs appear to hang.
    dependsOn(":app:jsBrowserDistribution")
    from(layout.projectDirectory.dir("app/build/dist/js/productionExecutable"))
    into(docsVersionDir.map { layout.projectDirectory.dir("docs/static/demo/$it") })

    doLast {
        logger.lifecycle("✅ JS demo updated (${project.version})")
    }
}

tasks.register("generateDocs") {
    group = "Charts"
    description = "Generate Dokka API docs and JS demo to docs/static/"

    dependsOn("charts:dokkaGenerate")
    dependsOn("generateJsDemo")

    doLast {
        logger.lifecycle("✅ Docs updated (${project.version})")
    }
}

tasks.register("listDocsGifScenarios") {
    group = "Charts"
    description = "Lists available docs GIF scenarios discovered via @RecordGif in :androidApp"
    dependsOn(":androidApp:listGifScenarios")
}

tasks.register("recordDocsGif") {
    group = "Charts"
    description =
        "Records one docs GIF scenario to docs/content/<gifDocsVersion>/wiki/assets (set -PgifScenario=<name>, defaults to first)"
    dependsOn(":androidApp:recordGifDebug")
}

tasks.register("recordDocsGifs") {
    group = "Charts"
    description =
        "Records all docs GIF scenarios to docs/content/<gifDocsVersion>/wiki/assets (default version: snapshot)"
    dependsOn(":androidApp:recordGifsDebug")
}

// Fast PR/main signal: compile coverage across key targets without packaging outputs.
tasks.register("ciCompile") {
    group = "Charts"
    description = "CI-focused compile task set without packaging"
    dependsOn(ChartsModules.ciKmpCompile.map { "$it:compileKotlinJvm" })
    dependsOn(ChartsModules.ciKmpCompile.map { "$it:compileKotlinJs" })
    dependsOn(ChartsModules.ciAndroidCompile.map { "$it:compileAndroidMain" })
    dependsOn(":smoke-line:compileKotlinJvm")
}

// Output validation path used by CI, intentionally kept on dev/debug tasks where possible:
// - avoids expensive production JS pipelines
// - avoids duplicating compile-only checks already covered by ciCompile
tasks.register("ciAssemble") {
    group = "Charts"
    description = "CI-focused assemble task set using dev/debug outputs"
    dependsOn(ChartsModules.library.map { "$it:jvmJar" })
    dependsOn(":charts-bom:assemble")
    dependsOn(ChartsModules.ciAndroidCompile.map { "$it:assembleAndroidMain" })
    dependsOn(":app:jsBrowserDevelopmentExecutableDistribution")
    dependsOn(":smoke-line:assemble")
}
