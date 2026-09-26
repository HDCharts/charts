---
title: Setup
---

# Manual Setup

This guide will help you integrate the HDCharts library into your Kotlin Multiplatform project.

## Installation

Replace `<version>` with the latest release: [![Release](https://img.shields.io/maven-central/v/io.github.hdcharts/charts.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.hdcharts/charts/overview)

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
    implementation("io.github.hdcharts:charts:<version>")
}
```

### Modular Dependencies (Pick What You Need)
```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:line:<version>")
    implementation("io.github.hdcharts:pie:<version>")
    implementation("io.github.hdcharts:bar:<version>")
    implementation("io.github.hdcharts:histogram:<version>")
    implementation("io.github.hdcharts:stacked-bar:<version>")
    implementation("io.github.hdcharts:stacked-area:<version>")
    implementation("io.github.hdcharts:radar:<version>")
}
```

### BOM (Optional Version Alignment)
Use BOM where Gradle platforms are supported (for example JVM/Android module dependencies).  
For KMP `commonMain`, keep explicit versions as shown above.

```kotlin
dependencies {
    implementation(platform("io.github.hdcharts:bom:<version>"))
    implementation("io.github.hdcharts:line")
    implementation("io.github.hdcharts:pie")
    implementation("io.github.hdcharts:bar")
    implementation("io.github.hdcharts:histogram")
    implementation("io.github.hdcharts:stacked-bar")
    implementation("io.github.hdcharts:stacked-area")
    implementation("io.github.hdcharts:radar")
}
```

### Snapshot Builds [![Snapshots](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fhdcharts%2Fcharts%2Fmaven-metadata.xml&label=Snapshots&color=4285F4)](https://central.sonatype.com/repository/maven-snapshots/io/github/hdcharts/charts/maven-metadata.xml)
Access the latest pre-release builds through the Sonatype snapshots repository. Snapshots contain the most recent features and fixes that haven't been officially released yet, allowing you to test upcoming functionality.
```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:charts:<snapshot-version>")
}

dependencyResolutionManagement {
    repositories {
        // Sonatype Central Portal Snapshots (replaces old s01.oss.sonatype.org)
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
```
