package io.github.hdcharts.sampleshared.data

import io.github.hdcharts.charts.model.ChartData
import io.github.hdcharts.charts.model.PieSlice

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

data class ChartGalleryPreview(
    val pieValues: List<Double>,
    val lineValues: List<Double>,
    val multiLineSeries: List<Pair<String, List<Double>>>,
    val stackedAreaSeries: List<Pair<String, List<Double>>>,
    val barValues: List<Double>,
    val histogramValues: List<Double>,
    val stackedSeries: List<Pair<String, List<Double>>>,
    val radarSeries: List<Pair<String, List<Double>>>,
)
