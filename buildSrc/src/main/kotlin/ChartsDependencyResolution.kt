import org.gradle.api.Project

private const val CHARTS_GROUP_ID = "io.github.dautovicharis"
private const val CHARTS_DEPENDENCY_MODE_PROPERTY = "chartsDependencyMode"
private const val CHARTS_PUBLISHED_VERSION_PROPERTY = "chartsPublishedVersion"

enum class ChartsDependencyMode {
    LOCAL,
    MAVEN,
    SNAPSHOT,
}

data class ChartsDependencyResolution(
    val mode: ChartsDependencyMode,
    val publishedVersion: String?,
)

fun Project.resolveChartsDependencyResolution(): ChartsDependencyResolution {
    val modeRaw = providers.gradleProperty(CHARTS_DEPENDENCY_MODE_PROPERTY).orElse("local").get()
    val mode =
        when (modeRaw.lowercase()) {
            "local" -> ChartsDependencyMode.LOCAL
            "maven" -> ChartsDependencyMode.MAVEN
            "snapshot" -> ChartsDependencyMode.SNAPSHOT
            else ->
                error(
                    "Invalid $CHARTS_DEPENDENCY_MODE_PROPERTY='$modeRaw'. Supported values: local, maven, snapshot.",
                )
        }

    val publishedVersion =
        providers
            .gradleProperty(CHARTS_PUBLISHED_VERSION_PROPERTY)
            .orNull
            ?.trim()
            .orEmpty()
            .takeIf { it.isNotEmpty() }
    if ((mode == ChartsDependencyMode.MAVEN || mode == ChartsDependencyMode.SNAPSHOT) && publishedVersion == null) {
        error("$CHARTS_PUBLISHED_VERSION_PROPERTY is required when $CHARTS_DEPENDENCY_MODE_PROPERTY=$modeRaw.")
    }
    if (mode == ChartsDependencyMode.SNAPSHOT && publishedVersion != null && !publishedVersion.endsWith("-SNAPSHOT")) {
        error(
            "$CHARTS_PUBLISHED_VERSION_PROPERTY must end with '-SNAPSHOT' when $CHARTS_DEPENDENCY_MODE_PROPERTY=snapshot.",
        )
    }

    return ChartsDependencyResolution(
        mode = mode,
        publishedVersion = publishedVersion,
    )
}

fun ChartsDependencyResolution.module(
    projectDependency: Any,
    artifactId: String,
): Any =
    when (mode) {
        ChartsDependencyMode.LOCAL -> projectDependency
        ChartsDependencyMode.MAVEN -> "$CHARTS_GROUP_ID:$artifactId:${checkNotNull(publishedVersion)}"
        ChartsDependencyMode.SNAPSHOT -> "$CHARTS_GROUP_ID:$artifactId:${checkNotNull(publishedVersion)}"
    }
