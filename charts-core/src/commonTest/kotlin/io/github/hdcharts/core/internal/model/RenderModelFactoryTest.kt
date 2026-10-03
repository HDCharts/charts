package io.github.hdcharts.core.internal.model

import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.errorsFor
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * One factory builds the render model for every chart that draws series against categories. What
 * differs between charts is only [PointLabels], so that is what these pin.
 */
class RenderModelFactoryTest {
    private val oneSeries =
        chartDataOf(
            categories = listOf("Jan", "Feb"),
            ChartSeries(name = "One", values = listOf(1.0, 2.0)),
        )

    private val multiSeriesInputs =
        ChartValidationInputs(
            colorCount = 0,
            rangeMin = null,
            rangeMax = null,
            xLabels = null,
            yLabels = null,
        )

    /** Takes any number of aligned series, no axis and no fixed range, so only data checks fire. */
    private val multiSeriesPolicy =
        ChartPolicy(
            minValues = ValidationErrors.MIN_VALUES,
            allowNegative = true,
            singleSeries = false,
            hasAxis = false,
            hasFixedRange = false,
            colorsMatch = { data -> data.series.size },
        )

    private val twoSeries =
        chartDataOf(
            categories = listOf("Jan", "Feb"),
            ChartSeries(name = "One", values = listOf(1.0, 2.0)),
            ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
        )

    @Test
    fun categories_labelsEveryPointAndKeepsTheCategories() {
        val model = toRenderModel(data = oneSeries, title = "T", labels = PointLabels.CATEGORIES)

        assertContentEquals(
            expected = listOf("Jan", "Feb"),
            actual =
                model.items
                    .single()
                    .item.labels,
        )
        assertContentEquals(expected = listOf("Jan", "Feb"), actual = model.categories)
        assertEquals(expected = "T", actual = model.title)
    }

    @Test
    fun categories_keepsEverySeriesAsItsOwnItem() {
        val model = toRenderModel(data = twoSeries, title = null, labels = PointLabels.CATEGORIES)

        assertEquals(expected = listOf("One", "Two"), actual = model.items.map { it.label })
        assertContentEquals(expected = listOf(1.0, 2.0), actual = model.items[0].item.points)
        assertContentEquals(expected = listOf(3.0, 4.0), actual = model.items[1].item.points)
        assertEquals(expected = "", actual = model.title)
    }

    @Test
    fun categories_whenSingleSeries_labelsPointsAndDropsTheCategories() {
        val model =
            toRenderModel(data = oneSeries, title = "T", labels = PointLabels.CATEGORIES_WHEN_SINGLE_SERIES)

        assertContentEquals(
            expected = listOf("Jan", "Feb"),
            actual =
                model.items
                    .single()
                    .item.labels,
        )
        assertTrue(model.categories.isEmpty())
    }

    @Test
    fun categoriesWhenSingleSeries_whenSeveralSeries_keepsCategoriesAndDropsPointLabels() {
        val model =
            toRenderModel(data = twoSeries, title = "T", labels = PointLabels.CATEGORIES_WHEN_SINGLE_SERIES)

        assertContentEquals(expected = listOf("", ""), actual = model.items[0].item.labels)
        assertContentEquals(expected = listOf("", ""), actual = model.items[1].item.labels)
        assertContentEquals(expected = listOf("Jan", "Feb"), actual = model.categories)
    }

    @Test
    fun none_labelsNothing() {
        val model = toRenderModel(data = oneSeries, title = "T", labels = PointLabels.NONE)

        assertContentEquals(
            expected = listOf("", ""),
            actual =
                model.items
                    .single()
                    .item.labels,
        )
        assertTrue(model.categories.isEmpty())
    }

    /** A short category list blanks the tail rather than dropping the points. */
    @Test
    fun categories_withFewerCategoriesThanValues_keepsEveryPoint() {
        val data =
            chartDataOf(
                categories = listOf("Jan"),
                ChartSeries(name = "One", values = listOf(1.0, 2.0, 3.0)),
            )

        val model = toRenderModel(data = data, title = "T", labels = PointLabels.CATEGORIES)

        assertContentEquals(
            expected = listOf("Jan", "", ""),
            actual =
                model.items
                    .single()
                    .item.labels,
        )
        assertContentEquals(
            expected = listOf(1.0, 2.0, 3.0),
            actual =
                model.items
                    .single()
                    .item.points,
        )
    }

    @Test
    fun categories_withNoCategories_leavesEveryPointUnlabelled() {
        val data =
            chartDataOf(
                categories = emptyList(),
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
            )

        val model = toRenderModel(data = data, title = "T", labels = PointLabels.CATEGORIES)

        assertContentEquals(
            expected = listOf("", ""),
            actual =
                model.items
                    .single()
                    .item.labels,
        )
    }

    @Test
    fun modelsWithEqualContent_areEqual() {
        val first = toRenderModel(data = twoSeries, title = "T", labels = PointLabels.CATEGORIES)
        val second = toRenderModel(data = twoSeries, title = "T", labels = PointLabels.CATEGORIES)

        assertEquals(expected = first, actual = second)
        assertEquals(expected = first.hashCode(), actual = second.hashCode())
    }

    /**
     * The factory assumes a validated model and does not check. These pin where that assumption is
     * actually enforced: `errorsFor` rejects the same inputs, so the seam never calls the factory
     * with them. A style value out of range would be clamped and drawn; data is reported here and
     * nothing is drawn.
     */
    @Test
    fun validation_rejectsTheDataTheFactoryAssumesAway() {
        val policy = multiSeriesPolicy
        val inputs = multiSeriesInputs

        assertEquals(
            expected = listOf("At least one series is required."),
            actual =
                policy.errorsFor(
                    data = chartDataOf(categories = listOf("Jan")),
                    inputs = inputs,
                    density = Density(1f),
                ),
        )
        assertEquals(
            expected = listOf("Series 1 is not aligned with the first series."),
            actual =
                policy.errorsFor(
                    data =
                        chartDataOf(
                            categories = listOf("Jan", "Feb"),
                            ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                            ChartSeries(name = "Two", values = listOf(3.0)),
                        ),
                    inputs = inputs,
                    density = Density(1f),
                ),
        )
        assertEquals(
            expected = listOf("Series 0 contains a non-finite value."),
            actual =
                policy.errorsFor(
                    data =
                        chartDataOf(
                            categories = listOf("Jan", "Feb"),
                            ChartSeries(name = "One", values = listOf(1.0, Double.NaN)),
                        ),
                    inputs = inputs,
                    density = Density(1f),
                ),
        )
    }

    /** A category count that disagrees with the values is reported, not silently padded or trimmed. */
    @Test
    fun validation_rejectsAMismatchedCategoryCount() {
        val mismatched =
            chartDataOf(
                categories = listOf("Jan"),
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
            )

        assertEquals(
            expected = listOf("Category count (1) must match value count (2)."),
            actual =
                multiSeriesPolicy.errorsFor(
                    data = mismatched,
                    inputs = multiSeriesInputs,
                    density = Density(1f),
                ),
        )
    }

    /** Stacked bar's model: one item per bar, each bar's points its segments, categories the names. */
    @Test
    fun transposeForStackedBar_readsTheSeriesMajorModelPerBar() {
        val barMajor =
            toRenderModel(
                data = twoSeries,
                title = "T",
                labels = PointLabels.CATEGORIES,
            ).transposeForStacking()

        assertEquals(expected = listOf("Jan", "Feb"), actual = barMajor.items.map { it.label })
        assertContentEquals(expected = listOf("One", "Two"), actual = barMajor.categories)
        assertContentEquals(expected = listOf(1.0, 3.0), actual = barMajor.items[0].item.points)
        assertContentEquals(expected = listOf(2.0, 4.0), actual = barMajor.items[1].item.points)
        assertContentEquals(expected = listOf("One", "Two"), actual = barMajor.items[0].item.labels)
        assertEquals(expected = "T", actual = barMajor.title)
    }

    @Test
    fun transposeForStackedBar_isItsOwnInverse() {
        val seriesMajor = toRenderModel(data = twoSeries, title = "T", labels = PointLabels.CATEGORIES)

        assertEquals(
            expected = seriesMajor,
            actual = seriesMajor.transposeForStacking().transposeForStacking(),
        )
    }

    @Test
    fun transposeForStackedBar_withASingleSeries_givesOneSegmentPerBar() {
        val barMajor =
            toRenderModel(data = oneSeries, title = "T", labels = PointLabels.CATEGORIES).transposeForStacking()

        assertEquals(expected = listOf("Jan", "Feb"), actual = barMajor.items.map { it.label })
        assertContentEquals(expected = listOf("One"), actual = barMajor.categories)
        assertContentEquals(expected = listOf(1.0), actual = barMajor.items[0].item.points)
    }
}
