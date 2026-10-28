plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.japicmp.gradle.plugin)
    testImplementation(kotlin("test"))
}
