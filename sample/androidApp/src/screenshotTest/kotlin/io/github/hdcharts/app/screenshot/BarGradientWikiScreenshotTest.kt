package io.github.hdcharts.app.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.docs.DailyCashFlow
import io.github.hdcharts.app.docs.DailyCashFlowGradient
import io.github.hdcharts.app.screenshot.shared.PREVIEW_DEVICE_TABLET_LANDSCAPE
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.core.model.toChartData

/** The Bar Gradient wiki example rendered with the data from Bar Basic Usage. */
@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun BarGradientWikiPreview() {
    val data =
        DailyCashFlow.sample.toChartData(
            value = DailyCashFlow::amount,
            category = DailyCashFlow::day,
            seriesName = DailyCashFlow.SERIES_NAME,
        )

    ScreenshotSurface {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            DailyCashFlowGradient(data)
        }
    }
}
