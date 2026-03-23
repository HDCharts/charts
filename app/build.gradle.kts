import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.build.config)
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
        namespace = Config.DEMO_LIBRARY_NAMESPACE
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
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "app"
            isStatic = true
        }
    }
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "Charts.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy()
            }
            binaries.executable()
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.mpp.runtime)
            implementation(libs.compose.mpp.foundation)
            implementation(libs.compose.mpp.material3)
            implementation(libs.compose.mpp.ui)
            implementation(libs.compose.mpp.preview)
            implementation(libs.compose.mpp.resources)
            implementation(libs.compose.mpp.material.icons.extended)

            implementation(chartsDependencies.module(project(":charts"), "charts"))
            implementation(project(":charts-demo-shared"))
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.collections.immutable)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
        }
    }
}

// Required for Desktop
compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = Config.DEMO_NAMESPACE
            packageVersion = Config.DEMO_VERSION_NAME
        }
    }
}

// Shared BuildConfig
buildConfig {
    packageName(Config.DEMO_LIBRARY_NAMESPACE)
    buildConfigField("CHARTS_VERSION", project.version.toString())
    useKotlinOutput()
}

compose.resources {
    publicResClass = true
}
