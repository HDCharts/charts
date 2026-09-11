package dev.hdcode.charts.sampleshared.data

import io.github.dautovicharis.charts.model.ChartData

interface PieSampleUseCase {
    fun initialPieSample(): PieSampleData

    fun initialPieCustomSample(): PieSampleData

    fun pieRefreshRange(): IntRange

    fun pieSample(
        range: IntRange,
        numOfPoints: IntRange,
    ): PieSampleData

    fun pieCustomSample(range: IntRange): PieSampleData
}

interface LineSampleUseCase {
    fun initialLineDataSet(): ChartData

    fun lineRefreshRange(): IntRange

    fun lineRefreshPointsCount(): Int

    fun lineDataSet(
        range: IntRange,
        numOfPoints: IntRange,
    ): ChartData
}

interface BarSampleUseCase {
    fun initialBarDataSet(): ChartData

    fun barDefaultPoints(): Int

    fun barDefaultRange(): IntRange

    fun barDataSet(
        points: Int,
        range: IntRange,
    ): ChartData
}

interface HistogramSampleUseCase {
    fun initialHistogramDataSet(): ChartData

    fun histogramDefaultPoints(): Int

    fun histogramDefaultRange(): IntRange

    fun histogramDataSet(
        points: Int,
        range: IntRange,
    ): ChartData
}
