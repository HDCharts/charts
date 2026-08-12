import org.gradle.api.tasks.testing.Test
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeScreenshot)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeGifRecorder)
    alias(libs.plugins.ktlint)
}

val chartsDependencies = resolveChartsDependencyResolution()

val localSigningProperties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use(::load)
        }
    }

fun Project.readSigningProperty(name: String): String? {
    val gradleProperty = providers.gradleProperty(name).orNull
    if (!gradleProperty.isNullOrBlank()) return gradleProperty

    val localProperty = localSigningProperties.getProperty(name)
    return localProperty?.takeIf { it.isNotBlank() }
}

fun Project.resolveSigningFile(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file else rootProject.file(path)
}

val releaseStoreFilePath = project.readSigningProperty("ANDROID_RELEASE_STORE_FILE")
val releaseStorePassword = project.readSigningProperty("ANDROID_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = project.readSigningProperty("ANDROID_RELEASE_KEY_ALIAS")
val releaseKeyPassword = project.readSigningProperty("ANDROID_RELEASE_KEY_PASSWORD")

val hasReleaseSigningConfig =
    listOf(
        releaseStoreFilePath,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    ).all { !it.isNullOrBlank() }

val gifDocsVersion = providers.gradleProperty("gifDocsVersion").orElse("snapshot")
val gifContentRoot =
    providers.gradleProperty("gifContentRoot").orElse(
        providers.provider {
            val migratedDocsContent =
                rootProject.layout.projectDirectory
                    .dir("../charts-docs/content")
                    .asFile
            if (migratedDocsContent.exists()) "../charts-docs/content" else "docs/content"
        },
    )
val gifOutputDir = providers.gradleProperty("gifOutputDir")
val compileSdkVersion =
    libs.versions.compile.sdk
        .get()
        .toInt()
val compileSdkMinorApiLevel =
    libs.versions.android.compile.sdk.minor
        .get()
        .toInt()
val protobufSecurityVersion =
    libs.versions.protobuf.security
        .get()
val httpClientSecurityVersion =
    libs.versions.httpclient.security
        .get()
val jose4jSecurityVersion =
    libs.versions.jose4j.security
        .get()

configurations.configureEach {
    if (name.startsWith("_internal-unified-test-platform")) {
        resolutionStrategy.eachDependency {
            if (requested.group == SecurityOverrides.PROTOBUF_GROUP &&
                requested.name in SecurityOverrides.PROTOBUF_ARTIFACTS
            ) {
                useVersion(protobufSecurityVersion)
                because(SecurityOverrides.PROTOBUF_REASON)
            }
            if (requested.group == SecurityOverrides.HTTP_COMPONENTS_GROUP &&
                requested.name == SecurityOverrides.HTTP_CLIENT_ARTIFACT
            ) {
                useVersion(httpClientSecurityVersion)
                because(SecurityOverrides.HTTP_CLIENT_REASON)
            }
            if (requested.group == SecurityOverrides.JOSE4J_GROUP &&
                requested.name == SecurityOverrides.JOSE4J_ARTIFACT
            ) {
                useVersion(jose4jSecurityVersion)
                because(SecurityOverrides.JOSE4J_REASON)
            }
        }
    }
}

android {
    namespace = Config.DEMO_NAMESPACE
    compileSdk {
        version =
            release(compileSdkVersion) {
                minorApiLevel = compileSdkMinorApiLevel
            }
    }

    defaultConfig {
        applicationId = Config.DEMO_NAMESPACE
        minSdk =
            libs.versions.min.sdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.target.sdk
                .get()
                .toInt()
        versionCode = Config.DEMO_VERSION_CODE
        versionName = Config.DEMO_VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = project.resolveSigningFile(checkNotNull(releaseStoreFilePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    kotlin {
        jvmToolchain(
            libs.versions.java
                .get()
                .toInt(),
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }

    buildFeatures {
        compose = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

gifRecorder {
    applicationId.set(Config.DEMO_NAMESPACE)
    outputDir.set(
        gifOutputDir
            .map { outputDir -> rootProject.layout.projectDirectory.dir(outputDir) }
            .orElse(
                gifContentRoot.zip(gifDocsVersion) { contentRoot, docsVersion ->
                    rootProject.layout.projectDirectory.dir("$contentRoot/$docsVersion/wiki/assets")
                },
            ),
    )
    baselineDir.set(rootProject.layout.projectDirectory.dir("gif-baselines"))
    // Matches the Material 3 colorScheme.background used by DocsGifScene.
    canvasBackgroundColor.set("0xFCFCFD")
}

dependencies {
    implementation(project(":app"))
    implementation(chartsDependencies.module(project(":charts"), "charts"))
    implementation(project(":sample-shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(project(":app"))
    androidTestImplementation(chartsDependencies.module(project(":charts"), "charts"))
    androidTestImplementation(libs.compose.ui.tooling.preview)

    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.compose.ui.tooling.preview)
    screenshotTestImplementation(libs.compose.ui.tooling)
    screenshotTestImplementation(chartsDependencies.module(project(":charts"), "charts"))
    screenshotTestImplementation(project(":app"))
    screenshotTestImplementation(project(":sample-shared"))
}

tasks.withType<Test>().configureEach {
    if (name.contains("ScreenshotTest")) {
        // Screenshot rendering is memory-heavy; keep fork count low and heap high to avoid OOM.
        maxParallelForks = 1
        maxHeapSize = "3g"
    }
}
