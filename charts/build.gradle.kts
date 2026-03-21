plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    android {
        namespace = Config.CHARTS_NAMESPACE
        compileSdk = Config.COMPILE_SDK
        minSdk = Config.MIN_SDK
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
        all {
            languageSettings.optIn("io.github.dautovicharis.charts.internal.InternalChartsApi")
        }

        commonMain.dependencies {
            api(projects.chartsCore)
            api(projects.chartsLine)
            api(projects.chartsPie)
            api(projects.chartsBar)
            api(projects.chartsHistogram)
            api(projects.chartsStackedBar)
            api(projects.chartsStackedArea)
            api(projects.chartsRadar)
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

private val apiSourceRoots =
    listOf(
        project.rootDir.resolve("charts-core/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-line/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-pie/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-bar/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-histogram/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-stacked-bar/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-stacked-area/src/commonMain/kotlin/io/github/dautovicharis/charts"),
        project.rootDir.resolve("charts-radar/src/commonMain/kotlin/io/github/dautovicharis/charts"),
    )

dokka {
    val isSnapshotVersion = project.version.toString().endsWith("-SNAPSHOT")
    val snapshotOutputDir = file(project.rootDir.resolve("docs/static/api/snapshot"))
    val versionOutputDir = file(project.rootDir.resolve("docs/static/api/${project.version}"))
    val primaryOutputDir = if (isSnapshotVersion) snapshotOutputDir else versionOutputDir

    dokkaSourceSets.commonMain {
        sourceLink {
            sourceRoots.setFrom(emptyList<File>())
            apiSourceRoots.filter { it.exists() }.forEach { sourceRoots.from(it) }
            remoteUrl("https://github.com/dautovicharis/charts/tree/${project.version}")
            remoteLineSuffix.set("#L")
        }

        skipDeprecated.set(false)
        skipEmptyPackages.set(true)

        perPackageOption {
            matchingRegex.set("io\\.github\\.dautovicharis\\.charts\\.internal(\\..*)?")
            suppress.set(true)
        }
    }

    dokkaPublications.html {
        outputDirectory.set(primaryOutputDir)
    }

    pluginsConfiguration {
        versioning {
            version.set(project.version.toString())
        }
    }
}

// https://github.com/Kotlin/dokka/issues/3988
tasks.register("dokkaHtml") {
    dependsOn("dokkaGenerate")
}

mavenPublishing {
    coordinates(
        groupId = Config.GROUP_ID,
        artifactId = Config.ARTIFACT_ID,
        version = project.version.toString(),
    )

    pom {
        ChartsPublishing.configurePom(
            pom = this,
            moduleName = "Charts",
            moduleDescription = "Charts.",
        )
    }
}

dependencies {
    dokkaHtmlPlugin(libs.dokka.versions)
    dokkaHtmlPlugin(libs.dokka.doc)
}
