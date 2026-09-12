package dev.hdcode.charts.sampleshared.data

import io.github.dautovicharis.charts.model.ChartData
import io.github.dautovicharis.charts.model.PieSlice

data class PieSampleData(
    val slices: List<PieSlice>,
    val title: String,
)

data class MultiLineSampleData(
    val dataSet: ChartData,
    val seriesKeys: List<String>,
    val title: String,
)

data class StackedBarSampleData(
    val dataSet: ChartData,
    val segmentKeys: List<String>,
    val title: String,
)

data class StackedAreaSampleData(
    val data: ChartData,
    val seriesKeys: List<String>,
    val title: String,
)

data class RadarSampleData(
    val basicData: ChartData,
    val customData: ChartData,
    val seriesKeys: List<String>,
    val title: String,
)

data class RadarCustomSampleData(
    val data: ChartData,
    val seriesKeys: List<String>,
)
