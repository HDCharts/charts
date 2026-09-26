package io.github.hdcharts.app.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_ANIMATE_ON_START
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.model.ChartValueFormatters
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.LineChartDefaults

/**
 * Chart image for the charts-docs landing page hero, drawn with the default
 * line chart style. The snapshot docs sync copies this reference to
 * charts-docs/docs-app/public/charts-hero-chart.png.
 */

private val heroData =
    listOf(
        "Web Store" to listOf(420.0, 510.0, 480.0, 530.0, 560.0, 590.0),
        "Mobile App" to listOf(360.0, 420.0, 410.0, 460.0, 500.0, 540.0),
        "Partner Sales" to listOf(280.0, 320.0, 340.0, 360.0, 390.0, 420.0),
    ).toChartData(categories = listOf("Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6"))

@PreviewTest
@Preview(
    name = "Hero chart",
    device = "spec:width=800dp,height=440dp,dpi=480",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
fun HeroChartPreview() {
    Box(modifier = Modifier.size(width = 720.dp, height = 390.dp)) {
        ScreenshotSurface {
            LineChart(
                data = heroData,
                modifier = Modifier.fillMaxSize(),
                title = "Weekly Revenue by Channel",
                animateOnStart = SCREENSHOT_ANIMATE_ON_START,
                valueFormatter = ChartValueFormatters.prefix("$"),
                axisValueFormatter = { value -> "$" + LineChartDefaults.axisValueFormatter.format(value) },
            )
        }
    }
}
