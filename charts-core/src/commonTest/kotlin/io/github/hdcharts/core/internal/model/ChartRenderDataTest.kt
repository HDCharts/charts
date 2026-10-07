package io.github.hdcharts.core.internal.model

import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.internal.ChartPolicy
import io.github.hdcharts.core.internal.ChartValidationInputs
import io.github.hdcharts.core.internal.ValidationErrors
import io.github.hdcharts.core.internal.errorsFor
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * The render model every chart below the seam draws from.
 *
 * It holds the caller's own [io.github.hdcharts.core.model.ChartData], so what matters here is that
 * nothing is copied, reshaped or dropped on the way in — and that the one chart with a different
 * shape, stacked bar, transposes the way the reader expects.
 *
 * The model assumes a validated input and does not check. [validation_rejectsWhatTheModelAssumes]
 * is where that assumption is actually enforced: `errorsFor` rejects the same inputs, so the seam
 * never builds a model from them.
 */
class ChartRenderDataTest {
    private fun model(
        vararg series: ChartSeries,
        categories: List<String> = emptyList(),
        title: String = "Title",
    ) = ChartRenderData(
        data = ChartData(categories = categories, series = series.toList()),
        title = title,
    )

    @Test
    fun minMax_returnsCorrectMinMaxValues() {
        val data =
            model(
                ChartSeries(name = "Label1", values = listOf(1.0, 2.0, 3.0)),
                ChartSeries(name = "Label2", values = listOf(0.5, 1.0, 1.5)),
                ChartSeries(name = "Label3", values = listOf(0.5, 1.0, 5.0)),
            )

        assertEquals(0.5 to 5.0, data.minMax())
    }

    @Test
    fun normalizeByMinMax_returnsNormalizedValues() {
        val data =
            model(
                ChartSeries(name = "Series1", values = listOf(0.0, 5.0)),
                ChartSeries(name = "Series2", values = listOf(2.5, 10.0)),
            )

        assertContentEquals(
            listOf(listOf(0f, 0.5f), listOf(0.25f, 1f)),
            data.normalizeByMinMax(data.minMax(), zeroRangeValue = 0f),
        )
    }

    @Test
    fun normalizeByMinMax_whenZeroRange_usesZeroRangeValue() {
        val data =
            model(
                ChartSeries(name = "Series1", values = listOf(3.0, 3.0)),
                ChartSeries(name = "Series2", values = listOf(3.0, 3.0)),
            )

        assertContentEquals(
            listOf(listOf(1f, 1f), listOf(1f, 1f)),
            data.normalizeByMinMax(data.minMax(), zeroRangeValue = 1f),
        )
    }

    @Test
    fun valueCount_returnsTheFirstSeriesCount() {
        val data =
            model(
                ChartSeries(name = "Label1", values = listOf(1.0, 2.0, 3.0)),
                ChartSeries(name = "Label2", values = listOf(0.5, 1.0, 1.5)),
            )

        assertEquals(3, data.valueCount())
    }

    @Test
    fun valueCount_withNoSeries_isZeroRatherThanThrowing() {
        assertEquals(0, model().valueCount())
    }

    @Test
    fun minMax_withNoSeries_isZeroRatherThanThrowing() {
        assertEquals(0.0 to 0.0, model().minMax())
    }

    @Test
    fun minMax_skipsAnEmptySeriesAndBoundsTheRest() {
        val data =
            model(
                ChartSeries(name = "Label1", values = emptyList()),
                ChartSeries(name = "Label2", values = listOf(2.0, 4.0)),
            )

        assertEquals(2.0 to 4.0, data.minMax())
    }

    @Test
    fun minMax_withNoValuesAtAll_isZeroRatherThanThrowing() {
        assertEquals(0.0 to 0.0, model(ChartSeries(name = "Label1", values = emptyList())).minMax())
    }

    @Test
    fun hasCategories_reportsWhetherTheDataCarriesThem() {
        assertFalse(model(ChartSeries(name = "One", values = listOf(1.0))).hasCategories())
        assertEquals(
            expected = true,
            actual =
                model(
                    ChartSeries(name = "One", values = listOf(1.0)),
                    categories = listOf("Jan"),
                ).hasCategories(),
        )
    }

    /** One total per series, in the caller's order, so a reader can index it like [series]. */
    @Test
    fun seriesTotals_sumsEachSeriesInOrder() {
        val data =
            model(
                ChartSeries(name = "Bar1", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Bar2", values = listOf(4.0)),
                ChartSeries(name = "Bar3", values = listOf(-1.0, 0.5)),
            )

        assertContentEquals(expected = listOf(3.0, 4.0, -0.5), actual = data.seriesTotals)
    }

    @Test
    fun seriesTotals_withNoSeries_isEmpty() {
        assertEquals(expected = emptyList(), actual = model().seriesTotals)
    }

    /**
     * Cached, so a reader in the draw loop cannot see the totals change under it, and a second read
     * costs nothing.
     */
    @Test
    fun seriesTotals_isTheSameListOnEveryRead() {
        val data = model(ChartSeries(name = "Bar1", values = listOf(1.0, 2.0)))

        assertEquals(expected = data.seriesTotals, actual = data.seriesTotals)
    }

    /** The model is the caller's data, not a copy of it, so nothing can drift between the two. */
    @Test
    fun seriesAndCategories_areTheCallersOwn() {
        val categories = listOf("Jan", "Feb")
        val series = listOf(ChartSeries(name = "One", values = listOf(1.0, 2.0)))
        val data = ChartRenderData(data = ChartData(categories = categories, series = series), title = "T")

        assertEquals(expected = listOf("One"), actual = data.series.map { it.name })
        assertContentEquals(expected = listOf(1.0, 2.0), actual = data.series.single().values)
        assertContentEquals(expected = categories, actual = data.categories)
        assertEquals(expected = "T", actual = data.title)
    }

    /**
     * Two models built from equal values are equal, so a `remember` keyed on one does not recompute
     * when the caller passes freshly allocated but identical data.
     */
    @Test
    fun modelsWithEqualContent_areEqual() {
        val first =
            model(
                ChartSeries(name = "Label1", values = listOf(1.0, 2.0)),
                categories = listOf("Jan", "Feb"),
            )
        val second =
            model(
                ChartSeries(name = "Label1", values = listOf(1.0, 2.0)),
                categories = listOf("Jan", "Feb"),
            )
        val different =
            model(
                ChartSeries(name = "Label1", values = listOf(1.0, 9.0)),
                categories = listOf("Jan", "Feb"),
            )

        assertEquals(expected = first, actual = second)
        assertEquals(expected = first.hashCode(), actual = second.hashCode())
        assertFalse(first == different)
    }

    /**
     * Stacked bar's model: one series per bar, each bar's values its segments, and the categories
     * holding the segment names. The two fields swap roles here, and this is the only place that
     * happens.
     */
    @Test
    fun transposeForStacking_readsTheSeriesMajorModelPerBar() {
        val barMajor =
            model(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
                categories = listOf("Jan", "Feb"),
            ).transposeForStacking()

        assertEquals(expected = listOf("Jan", "Feb"), actual = barMajor.series.map { it.name })
        assertContentEquals(expected = listOf("One", "Two"), actual = barMajor.categories)
        assertContentEquals(expected = listOf(1.0, 3.0), actual = barMajor.series[0].values)
        assertContentEquals(expected = listOf(2.0, 4.0), actual = barMajor.series[1].values)
        assertEquals(expected = "Title", actual = barMajor.title)
    }

    @Test
    fun transposeForStacking_isItsOwnInverse() {
        val seriesMajor =
            model(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                ChartSeries(name = "Two", values = listOf(3.0, 4.0)),
                categories = listOf("Jan", "Feb"),
            )

        assertEquals(
            expected = seriesMajor,
            actual = seriesMajor.transposeForStacking().transposeForStacking(),
        )
    }

    @Test
    fun transposeForStacking_withASingleSeries_givesOneSegmentPerBar() {
        val barMajor =
            model(
                ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                categories = listOf("Jan", "Feb"),
            ).transposeForStacking()

        assertEquals(expected = listOf("Jan", "Feb"), actual = barMajor.series.map { it.name })
        assertContentEquals(expected = listOf("One"), actual = barMajor.categories)
        assertContentEquals(expected = listOf(1.0), actual = barMajor.series[0].values)
    }

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
            stacksValues = false,
            singleSeries = false,
            hasAxis = false,
            hasFixedRange = false,
            colorsMatch = { data -> data.series.size },
        )

    /**
     * The model assumes validated data and does not check. These pin where that assumption is
     * enforced: `errorsFor` rejects the same inputs, so `ChartEntry` returns errors and never builds
     * a model from them. A style value out of range is clamped and drawn; invalid data is reported
     * and nothing is drawn.
     */
    @Test
    fun validation_rejectsWhatTheModelAssumes() {
        assertEquals(
            expected = listOf("At least one series is required."),
            actual =
                multiSeriesPolicy.errorsFor(
                    data = chartDataOf(categories = listOf("Jan")),
                    inputs = multiSeriesInputs,
                    density = Density(1f),
                ),
        )
        assertEquals(
            expected = listOf("Series 1 is not aligned with the first series."),
            actual =
                multiSeriesPolicy.errorsFor(
                    data =
                        chartDataOf(
                            categories = listOf("Jan", "Feb"),
                            ChartSeries(name = "One", values = listOf(1.0, 2.0)),
                            ChartSeries(name = "Two", values = listOf(3.0)),
                        ),
                    inputs = multiSeriesInputs,
                    density = Density(1f),
                ),
        )
        assertEquals(
            expected = listOf("Series 0 contains a non-finite value."),
            actual =
                multiSeriesPolicy.errorsFor(
                    data =
                        chartDataOf(
                            categories = listOf("Jan", "Feb"),
                            ChartSeries(name = "One", values = listOf(1.0, Double.NaN)),
                        ),
                    inputs = multiSeriesInputs,
                    density = Density(1f),
                ),
        )
    }

    /**
     * A category count that disagrees with the values is reported, not padded and not trimmed. The
     * model needs no fallback for it, which is the point: it can never see one.
     */
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
}
