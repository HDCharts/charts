package io.github.hdcharts.heatmap

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.model.ChartSelection
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.LegendDefaults
import io.github.hdcharts.heatmap.internal.HEATMAP_CHART_LEGEND_TAG
import io.github.hdcharts.heatmap.internal.HEATMAP_CHART_TAG
import io.github.hdcharts.heatmap.internal.HEATMAP_CHART_X_AXIS_LABELS_TAG
import io.github.hdcharts.heatmap.internal.HEATMAP_CHART_Y_AXIS_LABELS_TAG
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HeatmapChartTest {
    private val activity =
        HeatmapData(
            columns = listOf("Morning", "Midday", "Evening"),
            rows = listOf("Mon", "Tue"),
            values =
                listOf(
                    listOf(1.0, 4.0, null),
                    listOf(7.0, 2.0, 9.0),
                ),
        )

    @Test
    fun heatmapData_copiesInputsAndIndexesCellsRowMajor() {
        val values = mutableListOf(mutableListOf<Double?>(1.0, null))
        val data = HeatmapData(columns = listOf("A", "B"), rows = listOf("R"), values = values)
        values[0][0] = 99.0

        assertEquals(expected = 1.0, actual = data.values[0][0])
        assertEquals(expected = null, actual = data.values[0][1])
        assertEquals(expected = 5, actual = activity.cellIndex(row = 1, column = 2))
        assertEquals(expected = data, actual = HeatmapData(listOf("A", "B"), listOf("R"), listOf(listOf(1.0, null))))
    }

    @Test
    fun heatmapChart_defaultsToCellsAndShowsLabelsLegendAndTitle() =
        runComposeUiTest {
            lateinit var style: HeatmapChartStyle
            setContent {
                style = HeatmapChartDefaults.style()
                HeatmapChart(data = activity, modifier = Modifier.size(480.dp, 320.dp), title = "Activity")
            }

            runOnIdle { assertEquals(expected = HeatmapRenderMode.Cells, actual = style.renderMode) }
            onNodeWithTag(HEATMAP_CHART_TAG).assertIsDisplayed()
            onNodeWithText("Activity").assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_X_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_Y_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithText("Tue").assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_LEGEND_TAG).assertIsDisplayed()
            onNodeWithText("1").assertIsDisplayed()
            onNodeWithText("9").assertIsDisplayed()
        }

    @Test
    fun heatmapChart_hiddenLegendIsNotShown() =
        runComposeUiTest {
            setContent {
                HeatmapChart(
                    data = activity,
                    style = HeatmapChartDefaults.style(legend = LegendDefaults.style(visible = false)),
                )
            }

            onNodeWithTag(HEATMAP_CHART_TAG).assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_LEGEND_TAG).assertDoesNotExist()
        }

    @Test
    fun heatmapChart_tappingACellSelectsItAndShowsItsSourceValueInBothModes() {
        HeatmapRenderMode.entries.forEach { mode ->
            runComposeUiTest {
                val selection = ChartSelection()
                setContent { TestHeatmap(activity, selection, mode) }

                // Near the right edge of Tue/Morning, where the interpolated color is blended toward 2.
                onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput { click(cellPoint(row = 1, column = 0, x = 0.9f)) }

                runOnIdle { assertEquals(expected = 3, actual = selection.selectedIndex, message = "$mode") }
                onNodeWithText("Activity · Tue, Morning: 7").assertIsDisplayed()
            }
        }
    }

    @Test
    fun heatmapChart_tappingAMissingCellClearsSelection() {
        HeatmapRenderMode.entries.forEach { mode ->
            runComposeUiTest {
                val selection = ChartSelection(initialIndex = 0)
                setContent { TestHeatmap(activity, selection, mode) }

                onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput { click(cellPoint(row = 0, column = 2)) }

                runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex, message = "$mode") }
                onNodeWithText("Activity").assertIsDisplayed()
            }
        }
    }

    @Test
    fun heatmapChart_draggingAcrossCellsUpdatesSelectionAndHeading() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Interpolated) }

            val plot = onNodeWithTag(HEATMAP_CHART_TAG)
            plot.performTouchInput {
                down(cellPoint(row = 1, column = 0))
                moveTo(cellPoint(row = 1, column = 1))
            }
            runOnIdle { assertEquals(expected = 4, actual = selection.selectedIndex) }
            onNodeWithText("Activity · Tue, Midday: 2").assertIsDisplayed()

            plot.performTouchInput { moveTo(cellPoint(row = 1, column = 2)) }
            runOnIdle { assertEquals(expected = 5, actual = selection.selectedIndex) }
            onNodeWithText("Activity · Tue, Evening: 9").assertIsDisplayed()

            plot.performTouchInput {
                moveTo(cellPoint(row = 0, column = 2))
                up()
            }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_disabledInteractionIgnoresTapsButShowsProgrammaticSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 1)
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Cells, interactionEnabled = false) }

            onNodeWithText("Activity · Mon, Midday: 4").assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput { click(cellPoint(row = 1, column = 2)) }

            runOnIdle { assertEquals(expected = 1, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_changedDataClearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 3)
            val data = mutableStateOf(activity)
            setContent { TestHeatmap(data.value, selection, HeatmapRenderMode.Cells) }
            runOnIdle { assertEquals(expected = 3, actual = selection.selectedIndex) }

            runOnIdle { data.value = HeatmapData(activity.columns, listOf("Wed", "Thu"), activity.values) }

            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }
            onNodeWithText("Activity").assertIsDisplayed()
        }

    @Test
    fun heatmapChart_equalDataKeepsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 3)
            val data = mutableStateOf(activity)
            setContent { TestHeatmap(data.value, selection, HeatmapRenderMode.Cells) }

            runOnIdle {
                data.value =
                    HeatmapData(activity.columns.toList(), activity.rows.toList(), activity.values.map { it.toList() })
            }

            runOnIdle { assertEquals(expected = 3, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_programmaticSelectionOfAMissingOrOutOfRangeCellIsCleared() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Cells) }

            runOnIdle { selection.select(2) }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }

            runOnIdle { selection.select(6) }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }

            runOnIdle { selection.select(5) }
            runOnIdle { assertEquals(expected = 5, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_customFormatterFormatsSelectionAndLegend() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = 5)
            setContent {
                HeatmapChart(
                    data = activity,
                    modifier = Modifier.size(480.dp, 320.dp),
                    title = "Activity",
                    selection = selection,
                    valueFormatter = { "${it.toInt()} visits" },
                )
            }

            onNodeWithText("Activity · Tue, Evening: 9 visits").assertIsDisplayed()
            onNodeWithText("1 visits").assertIsDisplayed()
        }

    @Test
    fun heatmapChart_cellsAndInterpolatedModesDrawDifferentColorsFromTheSameValues() {
        val data = HeatmapData(columns = listOf("A", "B"), rows = listOf("R"), values = listOf(listOf(0.0, 10.0)))

        val cells = captureRow(data, HeatmapRenderMode.Cells)
        val interpolated = captureRow(data, HeatmapRenderMode.Interpolated)

        // Cells: flat colors and a gap at the shared edge.
        assertGray(expected = 0f, actual = cells(50), message = "cells, first center")
        assertGray(expected = 0f, actual = cells(90), message = "cells, near the edge")
        assertEquals(expected = 0f, actual = cells(100).alpha, message = "cells, gap")
        assertGray(expected = 1f, actual = cells(150), message = "cells, second center")
        // Interpolated: the field blends from 0 at the first center to 10 at the second, with no gap.
        assertGray(expected = 0f, actual = interpolated(25), message = "interpolated, outer edge")
        assertGray(expected = 0.25f, actual = interpolated(75), message = "interpolated, quarter way")
        assertGray(expected = 0.5f, actual = interpolated(100), message = "interpolated, shared edge")
        assertGray(expected = 1f, actual = interpolated(175), message = "interpolated, outer edge")
    }

    @Test
    fun heatmapChart_missingCellsDrawEmptyInBothModes() {
        val data =
            HeatmapData(columns = listOf("A", "B", "C"), rows = listOf("R"), values = listOf(listOf(0.0, null, 10.0)))

        HeatmapRenderMode.entries.forEach { mode ->
            // Three columns over 250 pixels, so each cell is about 83 pixels wide.
            val pixels = captureRow(data, mode, width = 250)
            assertEquals(expected = 0f, actual = pixels(125).alpha, message = "$mode, missing center")
            assertGray(expected = 0f, actual = pixels(60), message = "$mode, beside the gap")
            assertGray(expected = 1f, actual = pixels(190), message = "$mode, beside the gap")
        }
    }

    @Test
    fun heatmapChart_interpolatedFieldHasNoStepAtCellEdgesBesideAMissingCell() {
        // A plane with its center cell missing. Every other cell borders the gap.
        val data =
            HeatmapData(
                columns = listOf("A", "B", "C"),
                rows = listOf("1", "2", "3"),
                values =
                    listOf(
                        listOf(0.0, 10.0, 20.0),
                        listOf(30.0, null, 50.0),
                        listOf(60.0, 70.0, 80.0),
                    ),
            )

        val pixel = captureRow(data, HeatmapRenderMode.Interpolated, width = 300, height = 300, y = 80)

        // Row A, close to the gap: either side of the edge between the first two cells.
        assertTrue(abs(pixel(99).red - pixel(100).red) <= 3f / 255f, "left ${pixel(99)}, right ${pixel(100)}")
        assertTrue(abs(pixel(199).red - pixel(200).red) <= 3f / 255f, "left ${pixel(199)}, right ${pixel(200)}")
    }

    @Test
    fun heatmapChart_reportsEveryInvalidInput() =
        runComposeUiTest {
            setContent {
                HeatmapChart(
                    data =
                        HeatmapData(
                            columns = listOf("A", "B"),
                            rows = listOf("R1", "R2"),
                            values = listOf(listOf(1.0, Double.NaN)),
                        ),
                    style =
                        HeatmapChartDefaults.style(
                            colorScale = HeatmapColorScale(listOf(Color.Red), min = 5.0, max = 5.0),
                            xLabels = AxisLabelDefaults.style(size = (-1).sp),
                        ),
                )
            }

            onNodeWithTag(TestTags.CHART_ERROR).assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_TAG).assertDoesNotExist()
            onNodeWithText("The value matrix must have one value row per row label.", substring = true).assertExists()
            onNodeWithText("Cell values must be finite.", substring = true).assertExists()
            onNodeWithText("The color scale requires at least two colors.", substring = true).assertExists()
            onNodeWithText("The color scale minimum must be less than its maximum.", substring = true).assertExists()
            onNodeWithText("X-axis label size must be a finite, positive sp value.", substring = true).assertExists()
        }

    @Test
    fun heatmapChart_reportsEmptyAndMisalignedMatrices() {
        val cases =
            listOf(
                HeatmapData(emptyList(), emptyList(), emptyList()) to
                    listOf("At least one column is required.", "At least one row is required."),
                HeatmapData(listOf("A", "B"), listOf("R"), listOf(listOf(1.0))) to
                    listOf("Every value row must have one value per column label."),
                HeatmapData(listOf("A"), listOf("R"), listOf(listOf(null))) to
                    listOf("At least one cell must have a value."),
            )
        cases.forEach { (data, messages) ->
            runComposeUiTest {
                setContent { HeatmapChart(data = data) }
                messages.forEach { message -> onNodeWithText(message, substring = true).assertExists() }
            }
        }
    }

    @Test
    fun heatmapChart_reportsNonFiniteColorScaleBounds() =
        runComposeUiTest {
            setContent {
                HeatmapChart(
                    data = activity,
                    style =
                        HeatmapChartDefaults.style(
                            colorScale = HeatmapChartDefaults.colorScale(max = Double.POSITIVE_INFINITY),
                        ),
                )
            }

            onNodeWithText("Color scale bounds must be finite.", substring = true).assertExists()
        }

    @Test
    fun heatmapChart_drawsSingleCellsAndClampsInvalidSpacing() =
        runComposeUiTest {
            setContent {
                HeatmapChart(
                    data = HeatmapData(listOf("Only"), listOf("Row"), listOf(listOf(3.0))),
                    style =
                        HeatmapChartDefaults.style(
                            cellSpacing = (-4).dp,
                            renderMode = HeatmapRenderMode.Interpolated,
                        ),
                )
            }

            onNodeWithTag(HEATMAP_CHART_TAG).assertIsDisplayed()
            onNodeWithText("3").assertIsDisplayed()
        }

    @Test
    fun heatmapChart_tinyContainerWithASelectionDrawsWithoutCrashing() =
        runComposeUiTest {
            setContent {
                HeatmapChart(
                    data = activity,
                    modifier = Modifier.size(width = 1.dp, height = 1.dp),
                    title = "Activity",
                    selection = ChartSelection(initialIndex = 5),
                    animateOnStart = false,
                )
            }

            waitForIdle()
        }

    @Composable
    private fun TestHeatmap(
        data: HeatmapData,
        selection: ChartSelection,
        mode: HeatmapRenderMode,
        interactionEnabled: Boolean = true,
        modifier: Modifier = Modifier.size(width = 300.dp, height = 240.dp),
    ) {
        CompositionLocalProvider(LocalDensity provides Density(1f)) {
            HeatmapChart(
                data = data,
                modifier = modifier,
                style = plainStyle(mode),
                title = "Activity",
                selection = selection,
                interactionEnabled = interactionEnabled,
                animateOnStart = false,
            )
        }
    }

    /** No padding, labels or legend, so the cell grid fills the plot canvas. */
    @Composable
    private fun plainStyle(mode: HeatmapRenderMode) =
        HeatmapChartDefaults.style(
            chartContainerStyle = ChartContainerDefaults.style(contentPadding = 0.dp),
            renderMode = mode,
            colorScale = HeatmapColorScale(listOf(Color.Black, Color.White)),
            cellSpacing = 10.dp,
            xLabels = AxisLabelDefaults.style(visible = false),
            yLabels = AxisLabelDefaults.style(visible = false),
            legend = LegendDefaults.style(visible = false),
        )

    private fun androidx.compose.ui.test.TouchInjectionScope.cellPoint(
        row: Int,
        column: Int,
        x: Float = 0.5f,
    ): Offset = Offset(width * (column + x) / 3f, height * (row + 0.5f) / 2f)

    /** Draws [data] [width] by [height] pixels and returns a reader of the pixel line at [y]. */
    private fun captureRow(
        data: HeatmapData,
        mode: HeatmapRenderMode,
        width: Int = 200,
        height: Int = 100,
        y: Int = height / 2,
    ): (Int) -> Color {
        var reader: (Int) -> Color = { Color.Unspecified }
        runComposeUiTest {
            val capture =
                setCapturedContent { captureModifier ->
                    CompositionLocalProvider(LocalDensity provides Density(1f)) {
                        HeatmapChart(
                            data = data,
                            modifier = Modifier.size(width.dp, height.dp).then(captureModifier),
                            style = plainStyle(mode),
                            animateOnStart = false,
                        )
                    }
                }
            val pixels = capture()
            reader = { x -> pixels[x, y] }
        }
        return reader
    }

    private fun assertGray(
        expected: Float,
        actual: Color,
        message: String,
    ) {
        val tolerance = 0.03f
        assertTrue(
            actual.alpha > 0.99f &&
                abs(actual.red - expected) <= tolerance &&
                abs(actual.green - expected) <= tolerance &&
                abs(actual.blue - expected) <= tolerance,
            "$message: expected gray $expected, actual $actual",
        )
    }
}
