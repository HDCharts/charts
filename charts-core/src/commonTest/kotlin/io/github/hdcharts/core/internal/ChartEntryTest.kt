package io.github.hdcharts.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.core.model.chartDataOf
import io.github.hdcharts.core.style.ChartContainerDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
                        spec = TestSpec(onClamp = { density -> clampedAtDensity = density.density }),
                    )
                }
            }

            assertEquals(expected = 3f, actual = clampedAtDensity)
        }

    @Test
    fun changingSpec_recomputesConversionAndClamping() =
        runComposeUiTest {
            var clampedBy: String? = null
            val activeSpec =
                androidx.compose.runtime.mutableStateOf(
                    TestSpec(conversionTitle = "First", onClamp = { clampedBy = "First" }),
                )
            var convertedTitle: String? = null

            setContent {
                entry(
                    spec = activeSpec.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                    content = { converted, _ -> SideEffect { convertedTitle = converted.title } },
                )
            }

            assertEquals(expected = "First", actual = convertedTitle)
            assertEquals(expected = "First", actual = clampedBy)

            runOnIdle {
                activeSpec.value = TestSpec(conversionTitle = "Second", onClamp = { clampedBy = "Second" })
            }
            waitForIdle()

            assertEquals(expected = "Second", actual = convertedTitle)
            assertEquals(expected = "Second", actual = clampedBy)
        }

    @Test
    fun changingSpec_recomputesValidation() =
        runComposeUiTest {
            val activeSpec =
                androidx.compose.runtime.mutableStateOf(
                    TestSpec(minValues = ValidationErrors.MIN_VALUES),
                )

            setContent {
                entry(
                    spec = activeSpec.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0))),
                    title = "Title",
                )
            }

            onNodeWithText("At least 2 values are required.", substring = true).assertIsDisplayed()

            runOnIdle {
                activeSpec.value = TestSpec(minValues = 3)
            }
            waitForIdle()

            onNodeWithText("At least 3 values are required.", substring = true).assertIsDisplayed()
        }

    @Test
    fun changingStyle_recomputesValidation() =
        runComposeUiTest {
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    TestEntryStyle(size = 8.dp, colorCount = 0),
                )

            setContent {
                entry(
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                )
            }

            runOnIdle {
                activeStyle.value = TestEntryStyle(size = 8.dp, colorCount = 3)
            }
            waitForIdle()

            onNodeWithText("Color count (3) must match series count (1).", substring = true).assertIsDisplayed()
        }

    @Test
    fun equivalentValidationInputs_compareEqualAndDoNotRepeatValidation() =
        runComposeUiTest {
            var validationRuns = 0
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })
            val firstStyle =
                TestEntryStyle(
                    size = 8.dp,
                    axisLabelColor = Color.Black,
                    axisLabelVisible = true,
                    axisLabelSize = 12.sp,
                    axisLabelMaxCount = 5,
                )
            val equivalentStyle =
                TestEntryStyle(
                    size = 9.dp,
                    axisLabelColor = Color.Red,
                    axisLabelVisible = false,
                    axisLabelSize = 12.sp,
                    axisLabelMaxCount = 5,
                )
            assertEquals(expected = spec.policy.snapshot(firstStyle), actual = spec.policy.snapshot(equivalentStyle))
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    firstStyle,
                )

            setContent {
                entry(
                    spec = spec,
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                )
            }

            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeStyle.value = equivalentStyle
            }
            waitForIdle()

            assertEquals(expected = 1, actual = validationRuns)
        }

    @Test
    fun changingAxisLabelColor_reusesValidationAndUpdatesContent() =
        runComposeUiTest {
            var validationRuns = 0
            var renderedColor = Color.Unspecified
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    TestEntryStyle(size = 8.dp, axisLabelColor = Color.Black),
                )
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })

            setContent {
                entry(
                    spec = spec,
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                    content = { _, drawStyle -> SideEffect { renderedColor = drawStyle.axisLabelColor } },
                )
            }

            runOnIdle {
                assertEquals(expected = Color.Black, actual = renderedColor)
                assertEquals(expected = 1, actual = validationRuns)
                activeStyle.value = TestEntryStyle(size = 8.dp, axisLabelColor = Color.Red)
            }

            waitForIdle()

            runOnIdle {
                assertEquals(expected = Color.Red, actual = renderedColor)
                assertEquals(expected = 1, actual = validationRuns)
            }
        }

    @Test
    fun changingAxisLabelVisibility_reusesValidation() =
        runComposeUiTest {
            var validationRuns = 0
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    TestEntryStyle(size = 8.dp, axisLabelVisible = true),
                )
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })

            setContent {
                entry(
                    spec = spec,
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                )
            }

            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeStyle.value = TestEntryStyle(size = 8.dp, axisLabelVisible = false)
            }
            waitForIdle()

            assertEquals(expected = 1, actual = validationRuns)
        }

    @Test
    fun changingAxisLabelSize_recomputesValidation() =
        runComposeUiTest {
            var validationRuns = 0
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    TestEntryStyle(size = 8.dp, axisLabelSize = 11.sp),
                )
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })

            setContent {
                entry(
                    spec = spec,
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                )
            }

            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeStyle.value = TestEntryStyle(size = 8.dp, axisLabelSize = 20_000.sp)
            }
            waitForIdle()

            assertEquals(expected = 2, actual = validationRuns)
            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
        }

    @Test
    fun changingAxisLabelMaxCount_recomputesValidation() =
        runComposeUiTest {
            var validationRuns = 0
            val activeStyle =
                androidx.compose.runtime.mutableStateOf(
                    TestEntryStyle(size = 8.dp, axisLabelMaxCount = null),
                )
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })

            setContent {
                entry(
                    spec = spec,
                    style = activeStyle.value,
                    data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                    title = "Title",
                )
            }

            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeStyle.value = TestEntryStyle(size = 8.dp, axisLabelMaxCount = 1)
            }
            waitForIdle()

            assertEquals(expected = 2, actual = validationRuns)
            onNodeWithText("X-axis label max count must be in 2..1000.", substring = true)
                .assertIsDisplayed()
        }

    @Test
    fun changingData_recomputesValidation() =
        runComposeUiTest {
            var validationRuns = 0
            val activeData =
                androidx.compose.runtime.mutableStateOf(
                    chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                )
            val spec = TestSpec(onExpectedColors = { validationRuns++ })

            setContent {
                entry(
                    spec = spec,
                    data = activeData.value,
                    title = "Title",
                )
            }

            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeData.value = chartData(ChartSeries(name = "One", values = listOf(1.0)))
            }
            waitForIdle()

            assertEquals(expected = 2, actual = validationRuns)
            onNodeWithText("At least 2 values are required.", substring = true).assertIsDisplayed()
        }

    @Test
    fun changingDensity_recomputesDensitySensitiveValidation() =
        runComposeUiTest {
            var validationRuns = 0
            var contentRan = false
            val activeDensity = androidx.compose.runtime.mutableStateOf(Density(1f))
            val style = TestEntryStyle(size = 8.dp, axisLabelSize = 20_000.sp)
            val spec = TestSpec(includeAxes = true, onExpectedColors = { validationRuns++ })

            setContent {
                CompositionLocalProvider(LocalDensity provides activeDensity.value) {
                    entry(
                        spec = spec,
                        style = style,
                        data = chartData(ChartSeries(name = "One", values = listOf(1.0, 2.0))),
                        title = "Title",
                        content = { _, _ -> SideEffect { contentRan = true } },
                    )
                }
            }

            onNodeWithText("X-axis label size must resolve to at most 16384 pixels.", substring = true)
                .assertIsDisplayed()
            assertEquals(expected = 1, actual = validationRuns)

            runOnIdle {
                activeDensity.value = Density(0.5f)
            }
            waitForIdle()

            assertEquals(expected = 2, actual = validationRuns)
            assertTrue(contentRan)
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

    private class TestEntryStyle(
        val size: Dp,
        val colorCount: Int = 0,
        val axisLabelColor: Color = Color.Unspecified,
        val axisLabelVisible: Boolean = true,
        val axisLabelSize: TextUnit = 11.sp,
        val axisLabelMaxCount: Int? = null,
    )

    /** A minimal chart: the checks every chart shares, without range or axis inputs. */
    private class TestSpec(
        private val minValues: Int = ValidationErrors.MIN_VALUES,
        private val conversionTitle: String? = null,
        private val onClamp: (density: Density) -> Unit = {},
        private val includeAxes: Boolean = false,
        private val onExpectedColors: () -> Unit = {},
    ) : ChartSpec<TestEntryStyle> {
        override val policy =
            ChartPolicy<TestEntryStyle>(
                series =
                    SeriesRule.Multiple(
                        minValues = minValues,
                        allowNegative = true,
                        stacked = false,
                        expectedColors = { data ->
                            onExpectedColors()
                            data.series.size
                        },
                    ),
                colorCount = { it.colorCount },
                range = null,
                axes =
                    if (includeAxes) {
                        { style ->
                            val labels =
                                AxisLabelValidationValues(
                                    size = style.axisLabelSize,
                                    maxCount = style.axisLabelMaxCount,
                                )
                            AxisInputs(xLabels = labels, yLabels = labels)
                        }
                    } else {
                        null
                    },
            )

        override fun clamp(
            style: TestEntryStyle,
            density: Density,
        ): TestEntryStyle {
            onClamp(density)
            return TestEntryStyle(
                size = style.size.clampSize(fallback = FALLBACK_SIZE, density = density),
                colorCount = style.colorCount,
                axisLabelColor = style.axisLabelColor,
                axisLabelVisible = style.axisLabelVisible,
                axisLabelSize = style.axisLabelSize,
                axisLabelMaxCount = style.axisLabelMaxCount,
            )
        }

        /** Marks the conversion so the test can tell the seam's output from the caller's input. */
        override fun convert(
            data: ChartData,
            title: String?,
        ): ChartRenderData =
            ChartRenderData(
                data = data,
                title = conversionTitle ?: "$title:${data.series.size}",
            )
    }

    private companion object {
        val FALLBACK_SIZE = 4.dp

        fun chartData(vararg series: ChartSeries) = chartDataOf(series = series)
    }
}
