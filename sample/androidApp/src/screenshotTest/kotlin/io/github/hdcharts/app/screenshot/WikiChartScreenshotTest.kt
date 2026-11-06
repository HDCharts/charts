package io.github.hdcharts.app.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import io.github.hdcharts.app.docs.CalorieProgress
import io.github.hdcharts.app.docs.ComparePlatformReleases
import io.github.hdcharts.app.docs.DailyCashFlow
import io.github.hdcharts.app.docs.DailyCashFlowGradient
import io.github.hdcharts.app.docs.DailyMeasurement
import io.github.hdcharts.app.docs.EnergyUsage
import io.github.hdcharts.app.docs.ReadinessMetric
import io.github.hdcharts.app.docs.ScatterBasicExample
import io.github.hdcharts.app.docs.SelectSessionBlock
import io.github.hdcharts.app.docs.SelectTemperature
import io.github.hdcharts.app.docs.ShowDailyTemperatures
import io.github.hdcharts.app.docs.ShowInterpolatedCpuLoad
import io.github.hdcharts.app.docs.ShowMonthlyCashBalance
import io.github.hdcharts.app.docs.ShowMonthlySubscribers
import io.github.hdcharts.app.docs.ShowPie
import io.github.hdcharts.app.docs.ShowPieWithRoundedShare
import io.github.hdcharts.app.docs.ShowPlatformReadiness
import io.github.hdcharts.app.docs.ShowQuarterlyRevenue
import io.github.hdcharts.app.docs.ShowReadinessOnFixedScale
import io.github.hdcharts.app.docs.ShowRegionalCpuLoad
import io.github.hdcharts.app.docs.ShowRequestDurationDistribution
import io.github.hdcharts.app.docs.ShowTeamProgress
import io.github.hdcharts.app.docs.ShowWeeklySessions
import io.github.hdcharts.app.docs.regionalCpuLoad
import io.github.hdcharts.app.docs.weeklySessions
import io.github.hdcharts.app.screenshot.shared.PREVIEW_DEVICE_TABLET_LANDSCAPE
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface
import io.github.hdcharts.core.model.rememberChartSelection
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

    WikiScreenshotSurface {
        DailyCashFlowGradient(data)
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun PieWikiPreview() {
    WikiScreenshotSurface {
        ShowPie()
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun PieSelectedShareWikiPreview() {
    WikiScreenshotSurface {
        ShowPieWithRoundedShare(
            EnergyUsage.sample.toChartData(
                value = EnergyUsage::consumption,
                category = EnergyUsage::category,
            ),
            selection = rememberChartSelection(initialIndex = 4),
        )
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun LineWikiPreview() {
    WikiScreenshotSurface {
        ShowDailyTemperatures(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun LineSelectionWikiPreview() {
    WikiScreenshotSurface {
        SelectTemperature(
            data =
                DailyMeasurement.sample.toChartData(
                    value = DailyMeasurement::temperatureCelsius,
                    category = DailyMeasurement::day,
                    seriesName = DailyMeasurement.SERIES_NAME,
                ),
            modifier = Modifier.fillMaxSize(),
            selection = rememberChartSelection(initialIndex = 3),
        )
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun HistogramWikiPreview() {
    WikiScreenshotSurface {
        ShowRequestDurationDistribution(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun StackedBarWikiPreview() {
    WikiScreenshotSurface {
        ShowQuarterlyRevenue(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun StackedAreaWikiPreview() {
    WikiScreenshotSurface {
        ShowMonthlySubscribers(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun RadarWikiPreview() {
    WikiScreenshotSurface {
        ShowPlatformReadiness(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun ScatterWikiPreview() {
    WikiScreenshotSurface {
        ScatterBasicExample()
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun HeatmapWikiPreview() {
    WikiScreenshotSurface {
        ShowWeeklySessions()
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun HeatmapSequentialWikiPreview() {
    WikiScreenshotSurface {
        ShowRegionalCpuLoad()
    }
}

/** The Heatmap Interpolated wiki example rendered with the data from the Sequential Color Scale example. */
@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun HeatmapInterpolatedWikiPreview() {
    WikiScreenshotSurface {
        ShowInterpolatedCpuLoad(data = regionalCpuLoad)
    }
}

/** The Heatmap Selection wiki example rendered with the data from Heatmap Basic Usage. */
@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun HeatmapSelectionWikiPreview() {
    WikiScreenshotSurface {
        SelectSessionBlock(data = weeklySessions)
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun RadarMultipleSeriesWikiPreview() {
    WikiScreenshotSurface {
        ComparePlatformReleases(modifier = Modifier.fillMaxSize())
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun RadarFixedRangeWikiPreview() {
    WikiScreenshotSurface {
        ShowReadinessOnFixedScale(
            data =
                ReadinessMetric.sample.toChartData(
                    value = ReadinessMetric::score,
                    category = ReadinessMetric::category,
                    seriesName = ReadinessMetric.SERIES_NAME,
                ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun RingGaugeWikiPreview() {
    WikiScreenshotSurface {
        ShowTeamProgress()
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun SegmentedRingGaugeWikiPreview() {
    WikiScreenshotSurface {
        CalorieProgress()
    }
}

@PreviewTest
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
@Composable
fun WaterfallWikiPreview() {
    WikiScreenshotSurface { ShowMonthlyCashBalance(modifier = Modifier.fillMaxSize()) }
}

@Composable
private fun WikiScreenshotSurface(content: @Composable () -> Unit) {
    ScreenshotSurface {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
