---
title: Setup
---

# Manual Setup

This guide will help you integrate the HDCharts library into your Kotlin Multiplatform project.

## Installation

[![Release](https://img.shields.io/maven-central/v/io.github.hdcharts/charts.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.hdcharts/charts/overview)

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
    implementation("io.github.hdcharts:charts:{{artifactVersion}}")
}
```

### Modular Dependencies (Pick What You Need)
```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:line:{{artifactVersion}}")
    implementation("io.github.hdcharts:pie:{{artifactVersion}}")
    implementation("io.github.hdcharts:bar:{{artifactVersion}}")
    implementation("io.github.hdcharts:histogram:{{artifactVersion}}")
    implementation("io.github.hdcharts:stacked-bar:{{artifactVersion}}")
    implementation("io.github.hdcharts:stacked-area:{{artifactVersion}}")
    implementation("io.github.hdcharts:radar:{{artifactVersion}}")
    implementation("io.github.hdcharts:gauge:{{artifactVersion}}")
    implementation("io.github.hdcharts:scatter:{{artifactVersion}}")
    implementation("io.github.hdcharts:heatmap:{{artifactVersion}}")
}
```

### BOM (Optional Version Alignment)
Use BOM where Gradle platforms are supported (for example JVM/Android module dependencies).  
For KMP `commonMain`, keep explicit versions as shown above.

```kotlin
dependencies {
    implementation(platform("io.github.hdcharts:bom:{{artifactVersion}}"))
    implementation("io.github.hdcharts:line")
    implementation("io.github.hdcharts:pie")
    implementation("io.github.hdcharts:bar")
    implementation("io.github.hdcharts:histogram")
    implementation("io.github.hdcharts:stacked-bar")
    implementation("io.github.hdcharts:stacked-area")
    implementation("io.github.hdcharts:radar")
    implementation("io.github.hdcharts:gauge")
    implementation("io.github.hdcharts:scatter")
    implementation("io.github.hdcharts:heatmap")
}
```

### Snapshot Builds [![Snapshots](https://img.shields.io/static/v1?label=Snapshots&message={{snapshotVersion}}&color=4285F4)](https://central.sonatype.com/repository/maven-snapshots/io/github/hdcharts/charts/maven-metadata.xml)
Access the latest pre-release builds through the Sonatype snapshots repository. Snapshots contain the most recent features and fixes that haven't been officially released yet, allowing you to test upcoming functionality.
```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:charts:<snapshot-version>")
}

dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}
```
