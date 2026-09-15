package io.github.hdcharts.sampleshared.data

interface ChartPreviewUseCase {
    fun previewSeed(): ChartGalleryPreview

    fun nextPiePreview(values: List<Double>): List<Double>

    fun nextLinePreview(values: List<Double>): List<Double>

    fun nextBarPreview(values: List<Double>): List<Double>

    fun nextHistogramPreview(values: List<Double>): List<Double>

    fun nextMultiLinePreview(): List<Pair<String, List<Double>>>

    fun nextStackedAreaPreview(): List<Pair<String, List<Double>>>

    fun nextStackedPreview(): List<Pair<String, List<Double>>>

    fun nextRadarPreview(): List<Pair<String, List<Double>>>
}
