package io.github.hdcharts.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.style.ChartContainerDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * The seam validates first, then converts and clamps once, then hands both to the content. A chart
 * that routes its entry through it cannot skip a stage, run one twice, or draw on invalid data.
 */
@OptIn(InternalChartsApi::class, ExperimentalTestApi::class)
class ChartEntryTest {
    @Test
    fun validData_handsTheConvertedDataAndClampedStyleToContent() =
        runComposeUiTest {
            var received: Pair<String, Dp>? = null

            setContent {
                entry(
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                    style = TestEntryStyle(size = Dp.Unspecified),
                    content = { converted, drawStyle ->
                        SideEffect { received = converted.title to drawStyle.size }
                    },
                )
            }

            assertEquals(expected = "Title:1", actual = received?.first)
            assertEquals(expected = FALLBACK_SIZE, actual = received?.second)
        }

    @Test
    fun invalidData_rendersTheErrorsAndNeverReachesContent() =
        runComposeUiTest {
            var contentRan = false

            setContent {
                entry(
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0))),
                    title = "Title",
                    content = { _, _ -> contentRan = true },
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithText(
                "At least 2 values are required.",
                substring = true,
            ).assertIsDisplayed()
            assertFalse(contentRan, "content must not run for invalid data")
        }

    /** Clamping is a density-dependent stage, so it has to run against the density in scope. */
    @Test
    fun clamping_runsAtTheAmbientDensity() =
        runComposeUiTest {
            var clampedAtDensity: Float? = null

            setContent {
                CompositionLocalProvider(LocalDensity provides Density(density = 3f)) {
                    entry(
                        data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                        title = "Title",
                        spec = TestSpec { density -> clampedAtDensity = density.density },
                    )
                }
            }

            assertEquals(expected = 3f, actual = clampedAtDensity)
        }

    @Composable
    private fun entry(
        data: ChartData,
        title: String?,
        style: TestEntryStyle = TestEntryStyle(size = 8.dp),
        spec: TestSpec = TestSpec(),
        content: @Composable (data: ChartRenderData, style: TestEntryStyle) -> Unit = { _, _ -> },
    ) {
        ChartEntry(
            spec = spec,
            data = data,
            style = style,
            errorStyle = ChartContainerDefaults.style(),
            content = content,
            title = title,
        )
    }

    private data class TestEntryStyle(
        val size: Dp,
    )

    /** A minimal chart: the checks every chart shares, minus the two this fake style cannot supply. */
    private class TestSpec(
        private val onClamp: (density: Density) -> Unit = {},
    ) : ChartSpec<TestEntryStyle> {
        override val policy =
            ChartPolicy(
                minValues = ValidationErrors.MIN_VALUES,
                allowNegative = true,
                stacksValues = false,
                singleSeries = false,
                hasAxis = false,
                hasFixedRange = false,
                colorsMatch = { data -> data.series.size },
            )

        override fun validationInputs(style: TestEntryStyle): ChartValidationInputs =
            ChartValidationInputs(
                colorCount = 0,
                rangeMin = null,
                rangeMax = null,
                xLabels = null,
                yLabels = null,
            )

        override fun clamp(
            style: TestEntryStyle,
            density: Density,
        ): TestEntryStyle {
            onClamp(density)
            return style.copy(size = style.size.clampSize(fallback = FALLBACK_SIZE, density = density))
        }

        /** Marks the conversion so the test can tell the seam's output from the caller's input. */
        override fun convert(
            data: ChartData,
            title: String?,
        ): ChartRenderData =
            ChartRenderData(
                data = data,
                title = "$title:${data.series.size}",
            )
    }

    private companion object {
        val FALLBACK_SIZE = 4.dp

        fun chartData(vararg series: ChartSeries) = chartDataOf(series = series)
    }
}
