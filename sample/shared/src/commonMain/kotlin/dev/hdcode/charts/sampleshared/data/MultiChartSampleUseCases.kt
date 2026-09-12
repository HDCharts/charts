package dev.hdcode.charts.sampleshared.data

import io.github.dautovicharis.charts.model.ChartData

interface MultiLineSampleUseCase {
    fun initialMultiLineSample(): MultiLineSampleData

    fun multiLineRefreshRange(): IntRange

    fun multiLineSample(range: IntRange): MultiLineSampleData
}

interface StackedBarSampleUseCase {
    fun initialStackedBarSample(): StackedBarSampleData

    fun initialStackedBarNoCategoriesDataSet(): StackedBarSampleData

    fun stackedBarRefreshRange(): IntRange

    fun stackedBarSample(range: IntRange): StackedBarSampleData

    fun stackedBarSample(
        points: Int,
        range: IntRange,
    ): StackedBarSampleData
}

interface StackedAreaSampleUseCase {
    fun initialStackedAreaSample(): StackedAreaSampleData

    fun initialStackedAreaNoCategoriesData(): StackedAreaSampleData

    fun stackedAreaRefreshRange(): IntRange

    fun stackedAreaSample(range: IntRange): StackedAreaSampleData

    fun stackedAreaSample(
        points: Int,
        range: IntRange,
    ): StackedAreaSampleData
}

interface RadarSampleUseCase {
    fun initialRadarSample(): RadarSampleData

    fun initialRadarDefaultData(): ChartData

    fun initialRadarEdgeData(): ChartData

    fun initialRadarMultiNoCategoriesData(): ChartData

    fun radarRefreshRange(): IntRange

    fun radarDefaultData(range: IntRange): ChartData

    fun radarBasicData(range: IntRange): ChartData

    fun radarCustomSample(range: IntRange): RadarCustomSampleData
}
