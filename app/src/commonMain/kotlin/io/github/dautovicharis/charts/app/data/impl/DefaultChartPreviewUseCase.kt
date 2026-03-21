package io.github.dautovicharis.charts.app.data.impl

import io.github.dautovicharis.charts.app.ChartGalleryPreviewState
import io.github.dautovicharis.charts.app.data.ChartPreviewUseCase
import kotlin.random.Random

class DefaultChartPreviewUseCase : ChartPreviewUseCase {
    private val previewPieValues = listOf(32f, 21f, 24f, 14f, 9f)
    private val previewLineValues = listOf(42f, 38f, 45f, 51f, 47f)
    private val previewMultiLineSeries =
        listOf(
            "Web Store" to listOf(12f, 14f, 13f, 16f, 18f),
            "Mobile App" to listOf(9f, 11f, 12f, 14f, 15f),
            "Partner Sales" to listOf(7f, 8f, 9f, 10f, 12f),
        )
    private val previewStackedAreaSeries =
        listOf(
            "Free Plan" to listOf(18f, 20f, 22f, 24f, 26f),
            "Standard Plan" to listOf(10f, 12f, 13f, 15f, 16f),
            "Premium Plan" to listOf(6f, 7f, 8f, 9f, 10f),
        )
    private val previewBarValues = listOf(18f, 32f, 26f, 48f, 36f, 28f, 54f)
    private val previewHistogramValues = listOf(4f, 7f, 12f, 16f, 14f, 9f, 5f)
    private val previewStackedSeries =
        listOf(
            "North America" to listOf(20f, 22f, 25f),
            "Europe" to listOf(14f, 16f, 18f),
            "Asia Pacific" to listOf(12f, 14f, 17f),
        )
    private val previewRadarSeries =
        listOf(
            "Release 2.3" to listOf(86f, 82f, 78f, 89f, 84f, 77f),
        )

    override fun previewSeed(): ChartGalleryPreviewState =
        ChartGalleryPreviewState(
            pieValues = previewPieValues,
            lineValues = previewLineValues,
            multiLineSeries = previewMultiLineSeries,
            stackedAreaSeries = previewStackedAreaSeries,
            barValues = previewBarValues,
            histogramValues = previewHistogramValues,
            stackedSeries = previewStackedSeries,
            radarSeries = previewRadarSeries,
        )

    override fun nextPiePreview(values: List<Float>): List<Float> =
        values.map { value ->
            jitter(value, from = -6, until = 6, min = 8f, max = 55f)
        }

    override fun nextLinePreview(values: List<Float>): List<Float> =
        values.map { value ->
            jitter(value, from = -6, until = 6, min = 6f, max = 28f)
        }

    override fun nextBarPreview(values: List<Float>): List<Float> =
        values.map { value ->
            jitter(value, from = -8, until = 9, min = 0f, max = 100f)
        }

    override fun nextHistogramPreview(values: List<Float>): List<Float> =
        values.map { value ->
            jitter(value, from = -4, until = 5, min = 0f, max = 60f)
        }

    override fun nextMultiLinePreview(): List<Pair<String, List<Float>>> =
        previewMultiLineSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -5, until = 6, min = 6f, max = 22f)
                }
        }

    override fun nextStackedAreaPreview(): List<Pair<String, List<Float>>> =
        previewStackedAreaSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -5, until = 6, min = 4f, max = 28f)
                }
        }

    override fun nextStackedPreview(): List<Pair<String, List<Float>>> =
        previewStackedSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -6, until = 6, min = 6f, max = 28f)
                }
        }

    override fun nextRadarPreview(): List<Pair<String, List<Float>>> =
        previewRadarSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -18, until = 18, min = 30f, max = 100f)
                }
        }

    private fun jitter(
        value: Float,
        from: Int,
        until: Int,
        min: Float,
        max: Float,
    ): Float {
        val delta = Random.nextInt(from, until)
        return (value + delta).coerceIn(min, max)
    }
}
