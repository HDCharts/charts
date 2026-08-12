import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
}

val chartsDependencies = resolveChartsDependencyResolution()

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    android {
        namespace = Config.SAMPLE_SHARED_NAMESPACE
        compileSdk =
            libs.versions.compile.sdk
                .get()
                .toInt()
        minSdk =
            libs.versions.min.sdk
                .get()
                .toInt()
        androidResources {
            enable = true
        }
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
        commonMain.dependencies {
            api(chartsDependencies.module(projects.charts, "charts"))
            api(chartsDependencies.module(projects.chartsCore, "charts-core"))
            api(libs.compose.mpp.runtime)
            api(libs.compose.mpp.foundation)
            api(libs.compose.mpp.material3)
            api(libs.compose.mpp.ui)
            api(libs.compose.mpp.resources)
            api(libs.kotlinx.collections.immutable)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
    }
}

compose.resources {
    publicResClass = true
}
