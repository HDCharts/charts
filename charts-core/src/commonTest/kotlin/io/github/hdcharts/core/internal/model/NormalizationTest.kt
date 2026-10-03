package io.github.hdcharts.core.internal.model

import kotlin.test.Test
import kotlin.test.assertContentEquals

class NormalizationTest {
    @Test
    fun normalizeStackedValues_returnsNormalizedSums() {
        // Arrange
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(1.0, 1.0).toChartData()),
                        ChartDataItem("B", listOf(1.0, 3.0).toChartData()),
                    ),
                title = "Title",
            )

        // Act
        val normalized = data.normalizeStackedValues()

        // Assert
        assertContentEquals(expected = listOf(0.5f, 1f), actual = normalized)
    }

    @Test
    fun normalizeStackedValues_whenAllZero_returnsZeros() {
        // Arrange
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(0.0, 0.0).toChartData()),
                        ChartDataItem("B", listOf(0.0, 0.0).toChartData()),
                    ),
                title = "Title",
            )

        // Act
        val normalized = data.normalizeStackedValues()

        // Assert
        assertContentEquals(expected = listOf(0f, 0f), actual = normalized)
    }

    @Test
    fun normalizeStackedAreaValues_returnsCumulativeNormalizedBounds() {
        // Arrange
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(1.0, 2.0, 3.0).toChartData()),
                        ChartDataItem("B", listOf(2.0, 1.0, 1.0).toChartData()),
                    ),
                title = "Title",
            )

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
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(0.0, 0.0, 0.0).toChartData()),
                        ChartDataItem("B", listOf(0.0, 0.0, 0.0).toChartData()),
                    ),
                title = "Title",
            )

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(expected = listOf(0f, 0f, 0f), actual = normalized[0])
        assertContentEquals(expected = listOf(0f, 0f, 0f), actual = normalized[1])
    }

    @Test
    fun normalizeStackedAreaValues_withSingleSeries_scalesByGlobalMaxTotal() {
        // Arrange
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(4.0, 8.0).toChartData()),
                    ),
                title = "Title",
            )

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(expected = listOf(0.5f, 1f), actual = normalized.first())
    }
}
