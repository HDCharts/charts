package io.github.hdcharts.core.internal.model

import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import kotlin.test.Test
import kotlin.test.assertContentEquals

class NormalizationTest {
    private fun model(vararg values: List<Double>) =
        MultiChartData(
            data =
                ChartData(
                    categories = emptyList(),
                    series = values.mapIndexed { index, series -> ChartSeries(name = "S$index", values = series) },
                ),
            title = "Title",
        )

    @Test
    fun normalizeStackedValues_returnsNormalizedSums() {
        // Arrange
        val data = model(listOf(1.0, 1.0), listOf(1.0, 3.0))

        // Act
        val normalized = data.normalizeStackedValues()

        // Assert
        assertContentEquals(expected = listOf(0.5f, 1f), actual = normalized)
    }

    @Test
    fun normalizeStackedValues_whenAllZero_returnsZeros() {
        // Arrange
        val data = model(listOf(0.0, 0.0), listOf(0.0, 0.0))

        // Act
        val normalized = data.normalizeStackedValues()

        // Assert
        assertContentEquals(expected = listOf(0f, 0f), actual = normalized)
    }

    @Test
    fun normalizeStackedAreaValues_returnsCumulativeNormalizedBounds() {
        // Arrange
        val data = model(listOf(1.0, 2.0, 3.0), listOf(2.0, 1.0, 1.0))

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(
            expected = listOf(0.25f, 0.5f, 0.75f),
            actual = normalized[0],
        )
        assertContentEquals(
            expected = listOf(0.75f, 0.75f, 1f),
            actual = normalized[1],
        )
    }

    @Test
    fun normalizeStackedAreaValues_whenAllZero_returnsZeros() {
        // Arrange
        val data = model(listOf(0.0, 0.0, 0.0), listOf(0.0, 0.0, 0.0))

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(expected = listOf(0f, 0f, 0f), actual = normalized[0])
        assertContentEquals(expected = listOf(0f, 0f, 0f), actual = normalized[1])
    }

    @Test
    fun normalizeStackedAreaValues_withSingleSeries_scalesByGlobalMaxTotal() {
        // Arrange
        val data = model(listOf(4.0, 8.0))

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(expected = listOf(0.5f, 1f), actual = normalized.first())
    }
}
