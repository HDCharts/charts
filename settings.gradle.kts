rootProject.name = "ChartsProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val chartsDependencyMode =
    providers
        .gradleProperty("chartsDependencyMode")
        .orElse("local")
        .get()
        .lowercase()
val isSnapshotMode = chartsDependencyMode == "snapshot"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // https://github.com/gradle/foojay-toolchains
    // Automatically download required JDK
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

buildscript {
    configurations.classpath {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-core") {
                val versionCatalog =
                    settings.extensions
                        .getByType(org.gradle.api.artifacts.VersionCatalogsExtension::class.java)
                        .named("libs")
                val jacksonCoreSecurityVersion =
                    versionCatalog.findVersion("jackson-core-security").get().requiredVersion
                useVersion(jacksonCoreSecurityVersion)
                because("Mitigate GHSA-72hv-8253-57qq in settings classpath")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        if (isSnapshotMode) {
            exclusiveContent {
                forRepository {
                    maven(url = uri("https://central.sonatype.com/repository/maven-snapshots/")) {
                        mavenContent {
                            snapshotsOnly()
                        }
                    }
                }
                filter {
                    includeGroup("io.github.dautovicharis")
                }
            }
        }
    }
}

include(":app")
include(":androidApp")
include(":charts")
include(":charts-demo-shared")
include(":charts-core")
include(":charts-bar")
include(":charts-histogram")
include(":charts-line")
include(":charts-pie")
include(":charts-radar")
include(":charts-stacked-bar")
include(":charts-stacked-area")
include(":charts-bom")
include(":smoke-line")
