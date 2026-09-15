<p align="center">
  <img
    src="./readme-assets/hdcharts-logo-light.svg#gh-light-mode-only"
    alt="HDCharts logo"
    align="center"
    width="300"
  />
  <img
    src="./readme-assets/hdcharts-logo-dark.svg#gh-dark-mode-only"
    alt="HDCharts logo"
    align="center"
    width="300"
  />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.hdcharts/charts/overview">
    <img src="https://img.shields.io/maven-central/v/io.github.hdcharts/charts.svg?label=Maven%20Central" />
  </a>
  <a href="https://central.sonatype.com/repository/maven-snapshots/io/github/hdcharts/charts/maven-metadata.xml">
    <img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fhdcharts%2Fcharts%2Fmaven-metadata.xml&label=Snapshots&color=4285F4" />
  </a>
  <img src="https://img.shields.io/badge/Compose_Multiplatform-1.11.1-4285F4?logo=jetpackcompose" />
  <img src="https://img.shields.io/badge/Kotlin_Multiplatform-2.4.10-0095D5?logo=kotlin" />
  <img src="https://img.shields.io/badge/AGP-9.3.1-2E7D32?logo=android" />
</p>

<p align="center">
  A Kotlin Multiplatform chart library built with Jetpack Compose.
</p>

<p align="center">
  <img width="893" alt="demo-light" src="https://github.com/user-attachments/assets/9bd7fc79-93c2-434a-a70c-d3a3a6dc9db4" />
</p>

---

## 📚 Documentation
https://charts.hdcode.dev/

## 🟢 Production Demo
https://charts.hdcode.dev/demo

## ✨ Snapshot Demo
https://charts.hdcode.dev/demo/snapshot/

## 🏀 Playground
https://charts.hdcode.dev/playground

## Get Started

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```


### All chart types

Use the umbrella artifact when you want all chart types with the simplest setup.

```kotlin
commonMain.dependencies {
    implementation("io.github.hdcharts:charts:<version>")
}
```

### Individual chart modules

Use independent modules when you want only specific chart types and smaller dependency footprint.

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

### BOM

Use BOM for version alignment where Gradle platforms are supported.
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

## Example

```kotlin
@Composable
fun BasicLineChart() {
    val values = listOf(42.0, 38.0, 45.0, 51.0, 47.0, 54.0, 49.0)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val dataSet = values.toChartData(
        categories = labels,
    )

    LineChart(dataSet)
}
```

<p align="center">
  <img src="docs/content/snapshot/wiki/assets/line_default.gif" alt="Basic line chart example" width="400" />
</p>

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## License
[MIT](LICENSE)
