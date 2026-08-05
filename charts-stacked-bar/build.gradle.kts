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
        namespace = Config.CHARTS_STACKED_BAR_NAMESPACE
        compileSdk =
            libs.versions.compile.sdk
                .get()
                .toInt()
        minSdk =
            libs.versions.min.sdk
                .get()
                .toInt()
        androidResources.enable = true
        compilerOptions {
            jvmTarget.set(
                JvmTarget
                    .fromTarget(libs.versions.java.get()),
            )
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
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
            api(projects.chartsCore)
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
        artifactId = Config.ARTIFACT_STACKED_BAR_ID,
        version = project.version.toString(),
    )

    pom {
        ChartsPublishing.configurePom(
            pom = this,
            moduleName = "Stacked Bar Chart",
            moduleDescription = "Stacked bar chart module for Charts.",
        )
    }
}
