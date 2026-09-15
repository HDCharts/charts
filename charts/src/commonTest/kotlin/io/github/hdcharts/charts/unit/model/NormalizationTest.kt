package io.github.hdcharts.charts.unit.model

import io.github.hdcharts.charts.internal.common.model.ChartDataItem
import io.github.hdcharts.charts.internal.common.model.MultiChartData
import io.github.hdcharts.charts.internal.common.model.normalizeBarValues
import io.github.hdcharts.charts.internal.common.model.normalizeStackedAreaValues
import io.github.hdcharts.charts.internal.common.model.normalizeStackedValues
import io.github.hdcharts.charts.internal.common.model.resolveBarRange
import io.github.hdcharts.charts.internal.common.model.toChartData
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class NormalizationTest {
    @Test
    fun normalizeBarValues_whenZeroRange_usesSign() {
        // Arrange
        val chartData = listOf(-2.0f, 0.0f, 3.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = 5.0,
                maxValue = 5.0,
                useFixedRange = true,
            )

        // Assert
        assertContentEquals(expected = listOf(-1f, 0f, 1f), actual = normalized)
    }

    @Test
    fun normalizeBarValues_clampsToRange() {
        // Arrange
        val chartData = listOf(-20.0f, -10.0f, 0.0f, 10.0f, 20.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = -10.0,
                maxValue = 10.0,
                useFixedRange = true,
            )

        // Assert
        assertContentEquals(
            expected = listOf(-0.5f, -0.5f, 0f, 0.5f, 0.5f),
            actual = normalized,
        )
    }

    @Test
    fun normalizeBarValues_whenAllPositive_andAutoRange_scalesByMax() {
        // Arrange
        val chartData = listOf(10.0f, 25.0f, 50.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = 10.0,
                maxValue = 50.0,
                useFixedRange = false,
            )

        // Assert
        assertContentEquals(expected = listOf(0.2f, 0.5f, 1f), actual = normalized)
    }

    @Test
    fun normalizeBarValues_whenAllPositive_clampsToFixedRange() {
        // Arrange
        val chartData = listOf(25.0f, 50.0f, 125.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = 50.0,
                maxValue = 100.0,
                useFixedRange = true,
            )

        // Assert
        assertContentEquals(expected = listOf(0f, 0f, 1f), actual = normalized)
    }

    @Test
    fun normalizeBarValues_whenAllPositive_andFixedRange_scalesByRange() {
        // Arrange
        val chartData = listOf(10.0f, 25.0f, 50.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = 10.0,
                maxValue = 50.0,
                useFixedRange = true,
            )

        // Assert
        assertContentEquals(expected = listOf(0f, 0.375f, 1f), actual = normalized)
    }

    @Test
    fun normalizeBarValues_whenAllNegative_andAutoRange_scalesByMinMagnitude() {
        // Arrange
        val chartData = listOf(-50.0f, -25.0f, -10.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = -50.0,
                maxValue = -10.0,
                useFixedRange = false,
            )

        // Assert
        assertContentEquals(expected = listOf(-1f, -0.5f, -0.2f), actual = normalized)
    }

    @Test
    fun normalizeBarValues_whenAllNegative_andFixedRange_scalesByRange() {
        // Arrange
        val chartData = listOf(-50.0f, -25.0f, -10.0f).toChartData()

        // Act
        val normalized =
            chartData.normalizeBarValues(
                minValue = -50.0,
                maxValue = -10.0,
                useFixedRange = true,
            )

        // Assert
        assertContentEquals(expected = listOf(-1f, -0.375f, 0f), actual = normalized)
    }

    @Test
    fun resolveBarRange_whenInvalidRange_fallsBackToDataRange() {
        // Arrange
        val chartData = listOf(1.0f, 5.0f, 3.0f).toChartData()

        // Act
        val (min, max) = chartData.resolveBarRange(minValue = 10f, maxValue = 2f)

        // Assert
        assertEquals(1.0, min)
        assertEquals(5.0, max)
    }

    @Test
    fun normalizeStackedValues_returnsNormalizedSums() {
        // Arrange
        val data =
            MultiChartData(
                items =
                    listOf(
                        ChartDataItem("A", listOf(1.0f, 1.0f).toChartData()),
                        ChartDataItem("B", listOf(1.0f, 3.0f).toChartData()),
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
                        ChartDataItem("A", listOf(0.0f, 0.0f).toChartData()),
                        ChartDataItem("B", listOf(0.0f, 0.0f).toChartData()),
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
                        ChartDataItem("A", listOf(1.0f, 2.0f, 3.0f).toChartData()),
                        ChartDataItem("B", listOf(2.0f, 1.0f, 1.0f).toChartData()),
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
                        ChartDataItem("A", listOf(0.0f, 0.0f, 0.0f).toChartData()),
                        ChartDataItem("B", listOf(0.0f, 0.0f, 0.0f).toChartData()),
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
                        ChartDataItem("A", listOf(4.0f, 8.0f).toChartData()),
                    ),
                title = "Title",
            )

        // Act
        val normalized = data.normalizeStackedAreaValues()

        // Assert
        assertContentEquals(expected = listOf(0.5f, 1f), actual = normalized.first())
    }
}
