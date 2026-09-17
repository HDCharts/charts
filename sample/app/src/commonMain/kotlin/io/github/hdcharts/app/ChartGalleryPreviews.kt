package io.github.hdcharts.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.BarChart
import io.github.hdcharts.charts.HistogramChart
import io.github.hdcharts.charts.LineChart
import io.github.hdcharts.charts.PieChart
import io.github.hdcharts.charts.RadarChart
import io.github.hdcharts.charts.StackedAreaChart
import io.github.hdcharts.charts.StackedBarChart
import io.github.hdcharts.charts.model.PieSlice
import io.github.hdcharts.charts.model.toChartData
import io.github.hdcharts.charts.style.BarChartDefaults
import io.github.hdcharts.charts.style.ChartContainerDefaults
import io.github.hdcharts.charts.style.ChartContainerStyle
import io.github.hdcharts.charts.style.HistogramChartDefaults
import io.github.hdcharts.charts.style.LineChartDefaults
import io.github.hdcharts.charts.style.PieChartDefaults
import io.github.hdcharts.charts.style.RadarChartDefaults
import io.github.hdcharts.charts.style.StackedAreaChartDefaults
import io.github.hdcharts.charts.style.StackedBarChartDefaults
import io.github.hdcharts.sampleshared.data.ChartGalleryPreview
import io.github.hdcharts.sampleshared.theme.Dimens

private val PreviewShape = RoundedCornerShape(18.dp)
private val PreviewChartSize = 140.dp

@Composable
internal fun ChartPreviewFrame(
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val frameBrush =
        Brush.linearGradient(
            0f to accent.copy(alpha = 0.16f),
            1f to accent.copy(alpha = 0.04f),
        )
    val clickModifier =
        if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(PreviewShape)
                .then(clickModifier)
                .background(frameBrush)
                .border(1.dp, accent.copy(alpha = 0.18f), PreviewShape)
                .padding(Dimens.xs),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
internal fun ChartPreview(
    destination: ChartDestination,
    previews: ChartGalleryPreview,
) {
    when (destination) {
        is ChartDestination.PieChartScreen -> PieChartPreview(previews.pieValues)
        is ChartDestination.LineChartScreen -> LineChartPreview(previews.lineValues)
        is ChartDestination.MultiLineChartScreen ->
            MultiLineChartPreview(previews.multiLineSeries)
        is ChartDestination.StackedAreaChartScreen ->
            StackedAreaChartPreview(previews.stackedAreaSeries)
        is ChartDestination.BarChartScreen -> BarChartPreview(previews.barValues)
        is ChartDestination.HistogramChartScreen -> HistogramChartPreview(previews.histogramValues)
        is ChartDestination.StackedBarChartScreen -> StackedBarChartPreview(previews.stackedSeries)
        is ChartDestination.RadarChartScreen -> RadarChartPreview(previews.radarSeries)
    }
}

@Composable
private fun PieChartPreview(values: List<Double>) {
    val data =
        remember(values) {
            values.mapIndexed { index, value ->
                PieSlice(label = "Segment ${index + 1}", value = value)
            }
        }
    PieChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            PieChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
                legend = PieChartDefaults.legend(visible = false),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun LineChartPreview(values: List<Double>) {
    val data =
        remember(values) {
            values.toChartData()
        }
    LineChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            LineChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
                axis =
                    LineChartDefaults.axis(
                        xLabels = LineChartDefaults.xLabels(visible = false),
                        yLabels = LineChartDefaults.yLabels(visible = false),
                    ),
            ),
        interactionEnabled = false,
        animateOnStart = true,
    )
}

@Composable
private fun MultiLineChartPreview(series: List<Pair<String, List<Double>>>) {
    val data =
        remember(series) {
            series.toChartData()
        }
    LineChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            LineChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
                legend = LineChartDefaults.legend(visible = false),
                axis =
                    LineChartDefaults.axis(
                        xLabels = LineChartDefaults.xLabels(visible = false),
                        yLabels = LineChartDefaults.yLabels(visible = false),
                    ),
            ),
        interactionEnabled = false,
        animateOnStart = true,
    )
}

@Composable
private fun StackedAreaChartPreview(series: List<Pair<String, List<Double>>>) {
    val data =
        remember(series) {
            series.toChartData()
        }
    StackedAreaChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            StackedAreaChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
                axis =
                    StackedAreaChartDefaults.axis(
                        xLabels = StackedAreaChartDefaults.xLabels(visible = false),
                        yLabels = StackedAreaChartDefaults.yLabels(visible = false),
                    ),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun BarChartPreview(values: List<Double>) {
    val data =
        remember(values) {
            values.toChartData()
        }
    BarChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            BarChartDefaults.style(
                range =
                    BarChartDefaults.range(
                        min = 0.0,
                        max = 100.0,
                    ),
                axis =
                    BarChartDefaults.axis(
                        xLabels = BarChartDefaults.xLabels(visible = false),
                        yLabels = BarChartDefaults.yLabels(visible = false),
                    ),
                chartContainerStyle = previewChartContainerStyle(),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun HistogramChartPreview(values: List<Double>) {
    val data =
        remember(values) {
            values.toChartData()
        }
    HistogramChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            HistogramChartDefaults.style(
                range = BarChartDefaults.range(min = 0.0),
                axis =
                    BarChartDefaults.axis(
                        xLabels = BarChartDefaults.xLabels(visible = false),
                        yLabels = BarChartDefaults.yLabels(visible = false),
                    ),
                chartContainerStyle = previewChartContainerStyle(),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun StackedBarChartPreview(series: List<Pair<String, List<Double>>>) {
    val data =
        remember(series) {
            series.toChartData()
        }
    StackedBarChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            StackedBarChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
                axis =
                    StackedBarChartDefaults.axis(
                        xLabels = StackedBarChartDefaults.xLabels(visible = false),
                        yLabels = StackedBarChartDefaults.yLabels(visible = false),
                    ),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun RadarChartPreview(series: List<Pair<String, List<Double>>>) {
    val data =
        remember(series) { series.toChartData() }

    RadarChart(
        data = data,
        modifier = Modifier.size(PreviewChartSize),
        style =
            RadarChartDefaults.style(
                chartContainerStyle = previewChartContainerStyle(),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun previewChartContainerStyle(): ChartContainerStyle = ChartContainerDefaults.style(contentPadding = Dimens.xs)
