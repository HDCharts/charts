package io.github.hdcharts.sampleshared.data.impl

import io.github.hdcharts.sampleshared.data.ChartGalleryPreview
import io.github.hdcharts.sampleshared.data.ChartPreviewUseCase
import kotlin.random.Random

class DefaultChartPreviewUseCase : ChartPreviewUseCase {
    private val previewPieValues = listOf(32.0, 21.0, 24.0, 14.0, 9.0)
    private val previewLineValues = listOf(42.0, 38.0, 45.0, 51.0, 47.0)
    private val previewMultiLineSeries =
        listOf(
            "Web Store" to listOf(12.0, 14.0, 13.0, 16.0, 18.0),
            "Mobile App" to listOf(9.0, 11.0, 12.0, 14.0, 15.0),
            "Partner Sales" to listOf(7.0, 8.0, 9.0, 10.0, 12.0),
        )
    private val previewStackedAreaSeries =
        listOf(
            "Free Plan" to listOf(18.0, 20.0, 22.0, 24.0, 26.0),
            "Standard Plan" to listOf(10.0, 12.0, 13.0, 15.0, 16.0),
            "Premium Plan" to listOf(6.0, 7.0, 8.0, 9.0, 10.0),
        )
    private val previewBarValues = listOf(18.0, 32.0, 26.0, 48.0, 36.0, 28.0, 54.0)
    private val previewHistogramValues = listOf(4.0, 7.0, 12.0, 16.0, 14.0, 9.0, 5.0)
    private val previewStackedSeries =
        listOf(
            "North America" to listOf(20.0, 22.0, 25.0),
            "Europe" to listOf(14.0, 16.0, 18.0),
            "Asia Pacific" to listOf(12.0, 14.0, 17.0),
        )
    private val previewRadarSeries =
        listOf(
            "Release 2.3" to listOf(86.0, 82.0, 78.0, 89.0, 84.0, 77.0),
        )

    override fun previewSeed(): ChartGalleryPreview =
        ChartGalleryPreview(
            pieValues = previewPieValues,
            lineValues = previewLineValues,
            multiLineSeries = previewMultiLineSeries,
            stackedAreaSeries = previewStackedAreaSeries,
            barValues = previewBarValues,
            histogramValues = previewHistogramValues,
            stackedSeries = previewStackedSeries,
            radarSeries = previewRadarSeries,
        )

    override fun nextPiePreview(values: List<Double>): List<Double> =
        values.map { value ->
            jitter(value, from = -6, until = 6, min = 8.0, max = 55.0)
        }

    override fun nextLinePreview(values: List<Double>): List<Double> =
        values.map { value ->
            jitter(value, from = -6, until = 6, min = 6.0, max = 28.0)
        }

    override fun nextBarPreview(values: List<Double>): List<Double> =
        values.map { value ->
            jitter(value, from = -8, until = 9, min = 0.0, max = 100.0)
        }

    override fun nextHistogramPreview(values: List<Double>): List<Double> =
        values.map { value ->
            jitter(value, from = -4, until = 5, min = 0.0, max = 60.0)
        }

    override fun nextMultiLinePreview(): List<Pair<String, List<Double>>> =
        previewMultiLineSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -5, until = 6, min = 6.0, max = 22.0)
                }
        }

    override fun nextStackedAreaPreview(): List<Pair<String, List<Double>>> =
        previewStackedAreaSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -5, until = 6, min = 4.0, max = 28.0)
                }
        }

    override fun nextStackedPreview(): List<Pair<String, List<Double>>> =
        previewStackedSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -6, until = 6, min = 6.0, max = 28.0)
                }
        }

    override fun nextRadarPreview(): List<Pair<String, List<Double>>> =
        previewRadarSeries.map { (label, values) ->
            label to
                values.map { value ->
                    jitter(value, from = -18, until = 18, min = 30.0, max = 100.0)
                }
        }

    private fun jitter(
        value: Double,
        from: Int,
        until: Int,
        min: Double,
        max: Double,
    ): Double {
        val delta = Random.nextInt(from, until)
        return (value + delta).coerceIn(min, max)
    }
}
