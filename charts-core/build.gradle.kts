import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    `maven-publish`
    signing
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.build.config)
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    android {
        namespace = Config.CHARTS_CORE_NAMESPACE
        compileSdk =
            libs.versions.compile.sdk
                .get()
                .toInt()
        minSdk =
            libs.versions.min.sdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget.set(
                JvmTarget
                    .fromTarget(libs.versions.java.get()),
            )
        }
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    jvm()

    sourceSets {
        all {
            languageSettings.optIn("io.github.hdcharts.core.internal.InternalChartsApi")
        }

        commonMain.dependencies {
            api(libs.compose.mpp.runtime)
            api(libs.compose.mpp.foundation)
            api(libs.compose.mpp.material3)
            api(libs.compose.mpp.ui)
            api(libs.compose.mpp.preview)
            implementation(libs.compose.mpp.resources)
            api(libs.kotlinx.collections.immutable)
            implementation(libs.posthog.kmp)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.compose.mpp.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit"))
        }
    }
}

buildConfig {
    packageName("io.github.hdcharts.core.internal.telemetry")
    className("TelemetryBuildConfig")
    useKotlinOutput { internalVisibility = true }
    buildConfigField("CHARTS_VERSION", project.version.toString())
    buildConfigField("POSTHOG_HOST", "https://edge.hdcode.dev")

    val development = providers.gradleProperty("development").map { it.toBoolean() }.orElse(true)
    val chartsTelemetryEnabled = providers.gradleProperty("chartsTelemetryEnabled").map(String::toBoolean).orElse(true)

    val postHogProjectToken = providers.environmentVariable("POSTHOG_PROJECT_TOKEN").map(String::trim).orElse("")

    buildConfigField("POSTHOG_PROJECT_TOKEN", postHogProjectToken)

    val telemetryEnabled =
        providers.provider {
            val enabled = !development.get() && chartsTelemetryEnabled.get()
            check(!enabled || postHogProjectToken.get().isNotBlank()) {
                "Set POSTHOG_PROJECT_TOKEN for telemetry-enabled builds, or use -PchartsTelemetryEnabled=false."
            }
            enabled
        }

    buildConfigField(
        "TELEMETRY_ENABLED",
        telemetryEnabled,
    )
    buildConfigField(
        "DEVELOPMENT_MODE",
        development,
    )
}

mavenPublishing {
    coordinates(
        groupId = Config.GROUP_ID,
        artifactId = Config.ARTIFACT_CORE_ID,
        version = project.version.toString(),
    )

    pom {
        ChartsPublishing.configurePom(
            pom = this,
            moduleName = "HDCharts Core",
            moduleDescription = "Core components for HDCharts.",
        )
    }
}
