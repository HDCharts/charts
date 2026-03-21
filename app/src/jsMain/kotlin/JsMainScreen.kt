import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.dark_mode_off
import chartsproject.app.generated.resources.dark_mode_on
import chartsproject.app.generated.resources.dark_mode_system
import chartsproject.app.generated.resources.drawer_dark_mode_subtitle
import chartsproject.app.generated.resources.drawer_dynamic_colors_disable_hint
import chartsproject.app.generated.resources.drawer_dynamic_colors_subtitle
import chartsproject.app.generated.resources.drawer_github_subtitle
import chartsproject.app.generated.resources.drawer_header_subtitle
import chartsproject.app.generated.resources.drawer_header_title
import chartsproject.app.generated.resources.drawer_section_appearance
import chartsproject.app.generated.resources.drawer_section_links
import chartsproject.app.generated.resources.drawer_section_themes
import chartsproject.app.generated.resources.drawer_title_dark_mode
import chartsproject.app.generated.resources.drawer_title_dynamic_colors
import chartsproject.app.generated.resources.drawer_title_github
import chartsproject.app.generated.resources.github_url
import chartsproject.charts_demo_shared.generated.resources.ic_github
import io.github.dautovicharis.charts.app.ChartDestination
import io.github.dautovicharis.charts.app.LocalChartGalleryColumns
import io.github.dautovicharis.charts.app.MainScreen
import io.github.dautovicharis.charts.app.ui.composable.LocalChartDemoMaxWidth
import io.github.dautovicharis.charts.demoshared.startup.ChartsStartupGate
import io.github.dautovicharis.charts.demoshared.startup.StartupResources
import io.github.dautovicharis.charts.demoshared.startup.rememberStartupResourcesReady
import io.github.dautovicharis.charts.demoshared.theme.AppTheme
import io.github.dautovicharis.charts.demoshared.theme.docsSlate
import chartsproject.app.generated.resources.Res as AppRes
import chartsproject.charts_demo_shared.generated.resources.Res as SharedRes

@Composable
internal fun JsMainScreen() {
    val startupReady = rememberJsDemoStartupResourcesReady()

    AppTheme(
        theme = docsSlate,
        useDynamicColors = false,
    ) {
        ChartsStartupGate(
            isContentReady = startupReady,
        ) {
            CompositionLocalProvider(
                LocalChartDemoMaxWidth provides 500.dp,
                LocalChartGalleryColumns provides 3,
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
private fun rememberJsDemoStartupResourcesReady(): Boolean {
    val galleryDestinations =
        remember {
            listOf(
                ChartDestination.PieChartScreen,
                ChartDestination.LineChartScreen,
                ChartDestination.MultiLineChartScreen,
                ChartDestination.StackedAreaChartScreen,
                ChartDestination.BarChartScreen,
                ChartDestination.HistogramChartScreen,
                ChartDestination.StackedBarChartScreen,
                ChartDestination.RadarChartScreen,
            )
        }
    val drawerStrings =
        remember {
            listOf(
                AppRes.string.github_url,
                AppRes.string.dark_mode_system,
                AppRes.string.dark_mode_on,
                AppRes.string.dark_mode_off,
                AppRes.string.drawer_header_title,
                AppRes.string.drawer_header_subtitle,
                AppRes.string.drawer_section_appearance,
                AppRes.string.drawer_title_dark_mode,
                AppRes.string.drawer_dark_mode_subtitle,
                AppRes.string.drawer_title_dynamic_colors,
                AppRes.string.drawer_dynamic_colors_subtitle,
                AppRes.string.drawer_section_themes,
                AppRes.string.drawer_dynamic_colors_disable_hint,
                AppRes.string.drawer_section_links,
                AppRes.string.drawer_title_github,
                AppRes.string.drawer_github_subtitle,
            )
        }

    val resources =
        remember(galleryDestinations, drawerStrings) {
            StartupResources(
                vectorDrawables = (galleryDestinations.map { it.icon } + SharedRes.drawable.ic_github).distinct(),
                strings = (galleryDestinations.map { it.title } + drawerStrings).distinct(),
            )
        }

    return rememberStartupResourcesReady(resources)
}
