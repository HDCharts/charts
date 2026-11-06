package io.github.hdcharts.app.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.demo.waterfall.BusinessWaterfall
import io.github.hdcharts.app.screenshot.shared.ScreenshotChartSurface
import io.github.hdcharts.app.screenshot.shared.ScreenshotPreview
import io.github.hdcharts.core.model.staticChartSelection
import io.github.hdcharts.sampleshared.data.WaterfallSamples
import io.github.hdcharts.waterfall.WaterfallChart
import io.github.hdcharts.waterfall.WaterfallData
import io.github.hdcharts.waterfall.WaterfallEntry.Change
import io.github.hdcharts.waterfall.WaterfallEntry.Total

@PreviewTest
@ScreenshotPreview
@Composable
fun WaterfallBusinessPreview() {
    ScreenshotChartSurface {
        BusinessWaterfall(selection = staticChartSelection(5), interactionEnabled = false, animateOnStart = false)
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun WaterfallCrossingZeroPreview() {
    ScreenshotChartSurface {
        WaterfallChart(WaterfallSamples.crossingZero, title = "Cash balance", animateOnStart = false)
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun WaterfallZeroChangesPreview() {
    ScreenshotChartSurface {
        WaterfallChart(
            WaterfallSamples.zeroChanges,
            title = "Consecutive totals and zero change",
            animateOnStart = false,
        )
    }
}

@PreviewTest
@ScreenshotPreview
@Composable
fun WaterfallNegativePreview() {
    val expenses = WaterfallData(listOf(Change("Materials", -40.0), Change("Payroll", -30.0), Total("Expenses")))
    ScreenshotChartSurface {
        WaterfallChart(expenses, title = "Operating expenses", animateOnStart = false)
    }
}
