object ChartsModules {
    const val SAMPLE_SHARED = ":sample-shared"

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
            ":charts",
        )

    val sampleTested = listOf(SAMPLE_SHARED, ":app")
    val publishable = library + listOf(":charts-bom", ":charts-relocation")
    val ciKmpCompile = library + listOf(SAMPLE_SHARED, ":app")
    val ciAndroidCompile = library + listOf(SAMPLE_SHARED, ":app")
}
