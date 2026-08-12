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

    val publishable = library + listOf(":charts-bom")
    val ciKmpCompile = library + listOf(SAMPLE_SHARED, ":app")
    val ciAndroidCompile = library + listOf(SAMPLE_SHARED, ":app")
}
