package io.github.dautovicharis.charts.app.data

import io.github.dautovicharis.charts.app.ChartGalleryPreviewState

interface ChartPreviewUseCase {
    fun previewSeed(): ChartGalleryPreviewState

    fun nextPiePreview(values: List<Float>): List<Float>

    fun nextLinePreview(values: List<Float>): List<Float>

    fun nextBarPreview(values: List<Float>): List<Float>

    fun nextHistogramPreview(values: List<Float>): List<Float>

    fun nextMultiLinePreview(): List<Pair<String, List<Float>>>

    fun nextStackedAreaPreview(): List<Pair<String, List<Float>>>

    fun nextStackedPreview(): List<Pair<String, List<Float>>>

    fun nextRadarPreview(): List<Pair<String, List<Float>>>
}
