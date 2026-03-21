package io.github.dautovicharis.charts.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.BarChart
import io.github.dautovicharis.charts.HistogramChart
import io.github.dautovicharis.charts.LineChart
import io.github.dautovicharis.charts.PieChart
import io.github.dautovicharis.charts.RadarChart
import io.github.dautovicharis.charts.StackedAreaChart
import io.github.dautovicharis.charts.StackedBarChart
import io.github.dautovicharis.charts.model.toChartDataSet
import io.github.dautovicharis.charts.model.toMultiChartDataSet
import io.github.dautovicharis.charts.style.BarChartDefaults
import io.github.dautovicharis.charts.style.ChartViewDefaults
import io.github.dautovicharis.charts.style.ChartViewStyle
import io.github.dautovicharis.charts.style.HistogramChartDefaults
import io.github.dautovicharis.charts.style.LineChartDefaults
import io.github.dautovicharis.charts.style.PieChartDefaults
import io.github.dautovicharis.charts.style.RadarChartDefaults
import io.github.dautovicharis.charts.style.StackedAreaChartDefaults
import io.github.dautovicharis.charts.style.StackedBarChartDefaults

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
                .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
internal fun ChartPreview(
    destination: ChartDestination,
    previews: ChartGalleryPreviewState,
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
private fun PieChartPreview(values: List<Float>) {
    val dataSet =
        remember(values) {
            values.toChartDataSet(title = "")
        }
    PieChart(
        dataSet = dataSet,
        style = PieChartDefaults.style(chartViewStyle = previewChartViewStyle(), legendVisible = false),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun LineChartPreview(values: List<Float>) {
    val dataSet =
        remember(values) {
            values.toChartDataSet(title = "")
        }
    LineChart(
        dataSet = dataSet,
        style =
            LineChartDefaults.style(
                chartViewStyle = previewChartViewStyle(),
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
            ),
        interactionEnabled = false,
        animateOnStart = true,
    )
}

@Composable
private fun MultiLineChartPreview(series: List<Pair<String, List<Float>>>) {
    val dataSet =
        remember(series) {
            series.toMultiChartDataSet(title = "")
        }
    LineChart(
        dataSet = dataSet,
        style =
            LineChartDefaults.style(
                chartViewStyle = previewChartViewStyle(),
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
            ),
        interactionEnabled = false,
        animateOnStart = true,
    )
}

@Composable
private fun StackedAreaChartPreview(series: List<Pair<String, List<Float>>>) {
    val dataSet =
        remember(series) {
            series.toMultiChartDataSet(title = "")
        }
    StackedAreaChart(
        dataSet = dataSet,
        style =
            StackedAreaChartDefaults.style(
                chartViewStyle = previewChartViewStyle(),
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun BarChartPreview(values: List<Float>) {
    val dataSet =
        remember(values) {
            values.toChartDataSet(title = "")
        }
    BarChart(
        dataSet = dataSet,
        style =
            BarChartDefaults.style(
                minValue = 0f,
                maxValue = 100f,
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
                chartViewStyle = previewChartViewStyle(),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun HistogramChartPreview(values: List<Float>) {
    val labels =
        remember(values) {
            List(values.size) { index -> "B${index + 1}" }
        }
    val dataSet =
        remember(values, labels) {
            values.toChartDataSet(
                title = "",
                labels = labels,
            )
        }
    HistogramChart(
        dataSet = dataSet,
        style =
            HistogramChartDefaults.style(
                minValue = 0f,
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
                chartViewStyle = previewChartViewStyle(),
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun StackedBarChartPreview(series: List<Pair<String, List<Float>>>) {
    val dataSet =
        remember(series) {
            series.toMultiChartDataSet(title = "")
        }
    StackedBarChart(
        dataSet = dataSet,
        style =
            StackedBarChartDefaults.style(
                chartViewStyle = previewChartViewStyle(),
                xAxisLabelsVisible = false,
                yAxisLabelsVisible = false,
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun RadarChartPreview(series: List<Pair<String, List<Float>>>) {
    val categories =
        remember {
            listOf(
                "Performance",
                "Reliability",
                "Usability",
                "Security",
                "Scalability",
                "Observability",
            )
        }

    val previewSeries =
        remember(series) {
            if (series.isNotEmpty()) {
                series
            } else {
                listOf(
                    "Release 2.3" to listOf(86f, 82f, 78f, 89f, 84f, 77f),
                )
            }
        }

    val dataSet =
        remember(previewSeries) {
            previewSeries.toMultiChartDataSet(
                title = "",
                categories = categories,
            )
        }

    RadarChart(
        dataSet = dataSet,
        style =
            RadarChartDefaults.style(
                chartViewStyle = previewChartViewStyle(),
                categoryLegendVisible = false,
            ),
        interactionEnabled = false,
        animateOnStart = false,
    )
}

@Composable
private fun previewChartViewStyle(): ChartViewStyle =
    ChartViewDefaults.style(
        width = PreviewChartSize,
        outerPadding = 0.dp,
        innerPadding = 4.dp,
        cornerRadius = 14.dp,
        shadow = 0.dp,
        backgroundColor = Color.Transparent,
    )
