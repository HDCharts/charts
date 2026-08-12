package dev.hdcode.charts.sampleshared.data

import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.MultiChartDataSet

interface MultiLineSampleUseCase {
    fun initialMultiLineSample(): MultiLineSampleData

    fun multiLineRefreshRange(): IntRange

    fun multiLineSample(range: IntRange): MultiLineSampleData
}

interface StackedBarSampleUseCase {
    fun initialStackedBarSample(): StackedBarSampleData

    fun initialStackedBarNoCategoriesDataSet(): MultiChartDataSet

    fun stackedBarRefreshRange(): IntRange

    fun stackedBarSample(range: IntRange): StackedBarSampleData

    fun stackedBarSample(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData
}

interface StackedAreaSampleUseCase {
    fun initialStackedAreaSample(): StackedAreaSampleData

    fun initialStackedAreaNoCategoriesDataSet(): MultiChartDataSet

    fun stackedAreaRefreshRange(): IntRange

    fun stackedAreaSample(range: IntRange): StackedAreaSampleData

    fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData
}

interface RadarSampleUseCase {
    fun initialRadarSample(): RadarSampleData

    fun initialRadarDefaultDataSet(): ChartDataSet

    fun initialRadarEdgeDataSet(): ChartDataSet

    fun initialRadarMultiNoCategoriesDataSet(): MultiChartDataSet

    fun radarRefreshRange(): IntRange

    fun radarDefaultDataSet(range: IntRange): ChartDataSet

    fun radarBasicDataSet(range: IntRange): MultiChartDataSet

    fun radarCustomSample(range: IntRange): RadarCustomSampleData
}
