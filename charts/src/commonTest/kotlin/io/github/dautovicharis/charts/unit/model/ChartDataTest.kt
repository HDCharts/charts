package io.github.dautovicharis.charts.unit.model

import io.github.dautovicharis.charts.internal.common.model.ChartData
import io.github.dautovicharis.charts.internal.common.model.ChartDataType
import io.github.dautovicharis.charts.mock.MockTest
import io.github.dautovicharis.charts.model.ChartDataSet
import io.github.dautovicharis.charts.model.toChartDataSet
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ChartDataTest {
    // DoubleList
    @Test
    fun chartDataSet_doubleList_correctDataReturned() {
        // Arrange
        val items = listOf(1.0, 2.0, 3.0)
        val itemsType = ChartDataType.DoubleData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = items)
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("1.0", "2.0", "3.0"))
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    @Test
    fun chartDataSet_doubleList_withHighPrecision_roundsDefaultLabels() {
        // Arrange
        val items = listOf(101.322397132296, 151.31476088115193)
        val itemsType = ChartDataType.DoubleData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = items)
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("101.32", "151.31"))
    }

    // FloatList
    @Test
    fun chartDataSet_floatList_correctDataReturned() {
        // Arrange
        val items = listOf(1.5f, 2.5f, 3.5f)
        val itemsType = ChartDataType.FloatData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = listOf(1.5, 2.5, 3.5))
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("1.5", "2.5", "3.5"))
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    // StringList - double
    @Test
    fun chartDataSet_stringDoubleList_correctDataWithPostfixReturned() {
        // Arrange
        val items = listOf("25.5", "40.0", "70.5")
        val itemsType = ChartDataType.StringData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE, postfix = " °C")

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = listOf(25.5, 40.0, 70.5))
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("25.5 °C", "40.0 °C", "70.5 °C"))
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    // StringList - int
    @Test
    fun chartDataSet_stringIntList_correctDataWithPrefixReturned() {
        // Arrange
        val items = listOf("1", "2", "3")
        val itemsType = ChartDataType.StringData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE, prefix = "$")

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = listOf(1.0, 2.0, 3.0))
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("$1", "$2", "$3"))
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    // StringList - float
    @Test
    fun chartDataSet_stringFloatList_correctDataReturned() {
        // Arrange
        val items = listOf("-1.0", "2.0", "3.0")
        val itemsType = ChartDataType.StringData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = listOf(-1.0, 2.0, 3.0))
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = items)
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    // Primary constructor - List<Pair<String, Double>>
    @Test
    fun toChartData_primaryConstructor_correctDataReturned() {
        val chartData = ChartData(listOf("50%" to 50.0, "150%" to 150.0))
        assertContentEquals(actual = chartData.points, expected = listOf(50.0, 150.0))
        assertContentEquals(actual = chartData.labels, expected = listOf("50%", "150%"))
    }

    // IntList
    @Test
    fun chartDataSet_intList_correctDataReturned() {
        // Arrange
        val items = listOf(-1, 2, 3)
        val itemsType = ChartDataType.IntData(items)

        // Act
        val inputDataSet = ChartDataSet(items = itemsType, title = MockTest.TITLE)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.points, expected = listOf(-1.0, 2.0, 3.0))
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = listOf("-1", "2", "3"))
        assertEquals(actual = inputDataSet.data.label, expected = MockTest.TITLE)
    }

    // Explicit labels
    @Test
    fun `chart data set with explicit labels uses provided labels`() {
        // Arrange
        val items = listOf(1f, 2f, 3f)
        val labels = listOf("One", "Two", "Three")

        // Act
        val inputDataSet = items.toChartDataSet(title = MockTest.TITLE, labels = labels)

        // Assert
        assertContentEquals(actual = inputDataSet.data.item.labels, expected = labels)
    }
}
