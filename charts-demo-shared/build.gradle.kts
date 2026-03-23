plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val chartsDependencies = resolveChartsDependencyResolution()

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    android {
        namespace = Config.CHARTS_DEMO_SHARED_NAMESPACE
        compileSdk = Config.COMPILE_SDK
        minSdk = Config.MIN_SDK
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(
                org.jetbrains.kotlin.gradle.dsl.JvmTarget
                    .fromTarget(libs.versions.java.get()),
            )
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js(IR) {
        browser()
        binaries.executable()
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
