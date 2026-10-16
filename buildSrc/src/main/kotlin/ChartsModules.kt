object ChartsModules {
    const val SAMPLE_SHARED = ":sample-shared"
    const val SAMPLE_API = ":sample-api"

    val library =
        listOf(
            ":charts-core",
            ":charts-line",
            ":charts-pie",
            ":charts-bar",
            ":charts-histogram",
            ":charts-stacked-bar",
            ":charts-stacked-area",
            ":charts-radar",
            ":charts-gauge",
            ":charts",
        )

    val sampleTested = listOf(SAMPLE_SHARED, SAMPLE_API, ":app")
    val publishable = library + listOf(":charts-bom", ":charts-relocation")
    val ciKmpCompile = library + listOf(SAMPLE_SHARED, SAMPLE_API, ":app")
    val ciAndroidCompile = library + listOf(SAMPLE_SHARED, SAMPLE_API, ":app")
}
