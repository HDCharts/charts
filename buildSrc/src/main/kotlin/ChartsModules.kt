object ChartsModules {
    const val DEMO_SHARED = ":charts-demo-shared"

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
    val ciKmpCompile = library + listOf(DEMO_SHARED, ":app")
    val ciAndroidCompile = library + listOf(DEMO_SHARED, ":app")
}
