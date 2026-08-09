<p align="center">
  <img
    src="./hdcharts-logo.svg"
    alt="HDCharts logo"
    align="center"
    width="300"
  />
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.dautovicharis/charts/overview">
    <img src="https://img.shields.io/maven-central/v/io.github.dautovicharis/charts.svg?label=Maven%20Central" />
  </a>
  <a href="https://central.sonatype.com/repository/maven-snapshots/io/github/dautovicharis/charts/maven-metadata.xml">
    <img src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fdautovicharis%2Fcharts%2Fmaven-metadata.xml&label=Snapshots&color=4285F4" />
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
    implementation("io.github.dautovicharis:charts:<version>")
}
```

### Individual chart modules

Use independent modules when you want only specific chart types and smaller dependency footprint.

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

### BOM

Use BOM for version alignment where Gradle platforms are supported.
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

## Example

```kotlin
@Composable
fun BasicLineChart() {
    val values = listOf(42f, 38f, 45f, 51f, 47f, 54f, 49f)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val dataSet = values.toChartDataSet(
        title = "Daily Support Tickets",
        labels = labels,
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
