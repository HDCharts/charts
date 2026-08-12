package dev.hdcode.charts.sampleshared.data

import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet

data class PieSampleData(
    val dataSet: ChartDataSet,
    val segmentKeys: List<String>,
)

data class MultiLineSampleData(
    val dataSet: MultiChartDataSet,
    val seriesKeys: List<String>,
)

data class StackedBarSampleData(
    val dataSet: MultiChartDataSet,
    val segmentKeys: List<String>,
)

data class StackedAreaSampleData(
    val dataSet: MultiChartDataSet,
    val seriesKeys: List<String>,
)

data class RadarSampleData(
    val basicDataSet: MultiChartDataSet,
    val customDataSet: MultiChartDataSet,
    val seriesKeys: List<String>,
)

data class RadarCustomSampleData(
    val dataSet: MultiChartDataSet,
    val seriesKeys: List<String>,
)
