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
import io.github.hdcharts.app.screenshot.shared.SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.core.model.ChartValueFormatters
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults

/**
 * Chart image for the charts-docs landing page hero, drawn with the default
 * line chart style. The snapshot docs sync copies this reference to
 * charts-docs/docs-app/public/charts-hero-chart.png.
 */

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
            val sample = SCREENSHOT_MULTI_LINE_SAMPLE_USE_CASE.hero()
            LineChart(
                data = sample.dataSet,
                modifier = Modifier.fillMaxSize(),
                title = sample.title,
                animateOnStart = SCREENSHOT_ANIMATE_ON_START,
                valueFormatter = ChartValueFormatters.prefix("$"),
                axisValueFormatter = { value -> "$" + LineChartDefaults.axisValueFormatter.format(value) },
            )
        }
    }
}
