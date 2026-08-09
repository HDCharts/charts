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
    }

    jvm()

    sourceSets {
        all {
            languageSettings.optIn("io.github.dautovicharis.charts.internal.InternalChartsApi")
        }

        commonMain.dependencies {
            api(libs.compose.mpp.runtime)
            api(libs.compose.mpp.foundation)
            api(libs.compose.mpp.material3)
            api(libs.compose.mpp.ui)
            api(libs.compose.mpp.preview)
            implementation(libs.compose.mpp.resources)
            api(libs.kotlinx.collections.immutable)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.compose.mpp.ui.test)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
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
