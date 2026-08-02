plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.chartsLine)
        }
    }
}
