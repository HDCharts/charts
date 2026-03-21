package io.github.dautovicharis.charts.demoshared.data

import io.github.dautovicharis.charts.model.ChartDataSet

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
    fun initialLineDataSet(): ChartDataSet

    fun lineRefreshRange(): IntRange

    fun lineRefreshPointsCount(): Int

    fun lineDataSet(
        range: IntRange,
        numOfPoints: IntRange,
    ): ChartDataSet
}

interface BarSampleUseCase {
    fun initialBarDataSet(): ChartDataSet

    fun barDefaultPoints(): Int

    fun barDefaultRange(): IntRange

    fun barDataSet(
        points: Int,
        range: IntRange,
    ): ChartDataSet
}

interface HistogramSampleUseCase {
    fun initialHistogramDataSet(): ChartDataSet

    fun histogramDefaultPoints(): Int

    fun histogramDefaultRange(): IntRange

    fun histogramDataSet(
        points: Int,
        range: IntRange,
    ): ChartDataSet
}
