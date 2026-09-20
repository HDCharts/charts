---
title: Setup
---

# Manual Setup

This guide will help you integrate the HDCharts library into your Kotlin Multiplatform project.

## Installation

Replace `<version>` with the latest release: [![Release](https://img.shields.io/maven-central/v/io.github.dautovicharis/charts.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.dautovicharis/charts/overview)

### Repository
```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

### Umbrella Dependency
```kotlin
commonMain.dependencies {
    implementation("io.github.dautovicharis:charts:<version>")
}
```

### Modular Dependencies (Pick What You Need)
```kotlin
commonMain.dependencies {
    implementation("io.github.dautovicharis:charts-line:<version>")
    implementation("io.github.dautovicharis:charts-pie:<version>")
    implementation("io.github.dautovicharis:charts-bar:<version>")
    implementation("io.github.dautovicharis:charts-histogram:<version>")
    implementation("io.github.dautovicharis:charts-stacked-bar:<version>")
    implementation("io.github.dautovicharis:charts-stacked-area:<version>")
    implementation("io.github.dautovicharis:charts-radar:<version>")
}
```

### BOM (Optional Version Alignment)
Use BOM where Gradle platforms are supported (for example JVM/Android module dependencies).  
For KMP `commonMain`, keep explicit versions as shown above.

```kotlin
dependencies {
    implementation(platform("io.github.dautovicharis:charts-bom:<version>"))
    implementation("io.github.dautovicharis:charts-line")
    implementation("io.github.dautovicharis:charts-pie")
    implementation("io.github.dautovicharis:charts-bar")
    implementation("io.github.dautovicharis:charts-histogram")
    implementation("io.github.dautovicharis:charts-stacked-bar")
    implementation("io.github.dautovicharis:charts-stacked-area")
    implementation("io.github.dautovicharis:charts-radar")
}
```

### Snapshot Builds [![Snapshots](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fdautovicharis%2Fcharts%2Fmaven-metadata.xml&label=Snapshots&color=4285F4)](https://central.sonatype.com/repository/maven-snapshots/io/github/dautovicharis/charts/maven-metadata.xml)
Access the latest pre-release builds through the Sonatype snapshots repository. Snapshots contain the most recent features and fixes that haven't been officially released yet, allowing you to test upcoming functionality.
```kotlin
commonMain.dependencies {
    implementation("io.github.dautovicharis:charts:<snapshot-version>")
}

dependencyResolutionManagement {
    repositories {
        // Sonatype Central Portal Snapshots (replaces old s01.oss.sonatype.org)
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
```
