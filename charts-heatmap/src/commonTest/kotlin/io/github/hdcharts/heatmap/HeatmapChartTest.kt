package io.github.hdcharts.heatmap

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
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

private const val TITLE = "Activity"

// Rows and columns of the activity fixture.
private const val MON = 0
private const val TUE = 1
private const val MORNING = 0
private const val MIDDAY = 1
private const val EVENING = 2

// Touch positions within a cell, as fractions of the cell size.
private const val CELL_CENTER = 0.5f
private const val NEAR_RIGHT_EDGE = 0.9f

private val LABELED_CHART_WIDTH = 480.dp
private val LABELED_CHART_HEIGHT = 320.dp
private val PLAIN_CHART_WIDTH = 300.dp
private val PLAIN_CHART_HEIGHT = 240.dp

// One pixel per dp, so dp sizes are also pixel positions in captures.
private val ONE_PIXEL_PER_DP = Density(1f)
private const val PLAIN_CELL_SPACING_PX = 10
private const val CAPTURE_WIDTH_PX = 200
private const val CAPTURE_HEIGHT_PX = 100

// Captures use a black-to-white scale, so a pixel's gray level is its scale position.
private const val BLACK = 0f
private const val WHITE = 1f
private const val OPAQUE = 0.99f
private const val TRANSPARENT = 0f

// About eight 8-bit color steps, for antialiasing and gradient rounding between platforms.
private const val GRAY_TOLERANCE = 0.03f

// Two pixels either side of a continuous edge may differ by a few color steps of the field's slope.
private const val MAX_EDGE_STEP = 3f / 255f

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
    private val monMorning = activity.cellIndex(MON, MORNING)
    private val monMidday = activity.cellIndex(MON, MIDDAY)
    private val monEveningMissing = activity.cellIndex(MON, EVENING)
    private val tueMorning = activity.cellIndex(TUE, MORNING)
    private val tueMidday = activity.cellIndex(TUE, MIDDAY)
    private val tueEvening = activity.cellIndex(TUE, EVENING)
    private val pastLastCell = activity.rows.size * activity.columns.size

    @Test
    fun heatmapData_copiesInputsAndIndexesCellsRowMajor() {
        val original = 1.0
        val changedAfterCopy = 99.0
        val values = mutableListOf(mutableListOf<Double?>(original, null))
        val data = HeatmapData(columns = listOf("A", "B"), rows = listOf("R"), values = values)
        values[0][0] = changedAfterCopy

        assertEquals(expected = original, actual = data.values[0][0])
        assertEquals(expected = null, actual = data.values[0][1])
        assertEquals(expected = TUE * activity.columns.size + EVENING, actual = tueEvening)
        assertEquals(
            expected = data,
            actual = HeatmapData(listOf("A", "B"), listOf("R"), listOf(listOf(original, null))),
        )
    }

    @Test
    fun heatmapChart_defaultsToCellsAndShowsLabelsLegendAndTitle() =
        runComposeUiTest {
            // The legend labels the lowest and highest activity values.
            val lowestLabel = "1"
            val highestLabel = "9"
            lateinit var style: HeatmapChartStyle
            setContent {
                style = HeatmapChartDefaults.style()
                HeatmapChart(
                    data = activity,
                    modifier = Modifier.size(LABELED_CHART_WIDTH, LABELED_CHART_HEIGHT),
                    title = TITLE,
                )
            }

            runOnIdle { assertEquals(expected = HeatmapRenderMode.Cells, actual = style.renderMode) }
            onNodeWithTag(HEATMAP_CHART_TAG).assertIsDisplayed()
            onNodeWithText(TITLE).assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_X_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_Y_AXIS_LABELS_TAG).assertIsDisplayed()
            onNodeWithText("Tue").assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_LEGEND_TAG).assertIsDisplayed()
            onNodeWithText(lowestLabel).assertIsDisplayed()
            onNodeWithText(highestLabel).assertIsDisplayed()
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

    // On Wasm and iOS, runComposeUiTest returns an asynchronous TestResult, so each test returns exactly
    // one runComposeUiTest and switches modes or data through state inside it.

    @Test
    fun heatmapChart_tappingACellSelectsItAndShowsItsSourceValueInBothModes() =
        runComposeUiTest {
            val selection = ChartSelection()
            var mode by mutableStateOf(HeatmapRenderMode.Cells)
            setContent { TestHeatmap(activity, selection, mode) }

            HeatmapRenderMode.entries.forEach { nextMode ->
                runOnIdle {
                    selection.clear()
                    mode = nextMode
                }
                // Near the right edge of Tue/Morning, where the interpolated color is blended toward Midday.
                onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput {
                    click(cellPoint(row = TUE, column = MORNING, x = NEAR_RIGHT_EDGE))
                }

                runOnIdle {
                    assertEquals(
                        expected = tueMorning,
                        actual = selection.selectedIndex,
                        message = "$nextMode",
                    )
                }
                onNodeWithText("$TITLE · Tue, Morning: 7").assertIsDisplayed()
            }
        }

    @Test
    fun heatmapChart_tappingAMissingCellClearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection()
            var mode by mutableStateOf(HeatmapRenderMode.Cells)
            setContent { TestHeatmap(activity, selection, mode) }

            HeatmapRenderMode.entries.forEach { nextMode ->
                runOnIdle {
                    mode = nextMode
                    selection.select(monMorning)
                }
                onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput { click(cellPoint(row = MON, column = EVENING)) }

                runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex, message = "$nextMode") }
                onNodeWithText(TITLE).assertIsDisplayed()
            }
        }

    @Test
    fun heatmapChart_draggingAcrossCellsUpdatesSelectionAndHeading() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Interpolated) }

            val plot = onNodeWithTag(HEATMAP_CHART_TAG)
            plot.performTouchInput {
                down(cellPoint(row = TUE, column = MORNING))
                moveTo(cellPoint(row = TUE, column = MIDDAY))
            }
            runOnIdle { assertEquals(expected = tueMidday, actual = selection.selectedIndex) }
            onNodeWithText("$TITLE · Tue, Midday: 2").assertIsDisplayed()

            plot.performTouchInput { moveTo(cellPoint(row = TUE, column = EVENING)) }
            runOnIdle { assertEquals(expected = tueEvening, actual = selection.selectedIndex) }
            onNodeWithText("$TITLE · Tue, Evening: 9").assertIsDisplayed()

            plot.performTouchInput {
                moveTo(cellPoint(row = MON, column = EVENING))
                up()
            }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_disabledInteractionIgnoresTapsButShowsProgrammaticSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = monMidday)
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Cells, interactionEnabled = false) }

            onNodeWithText("$TITLE · Mon, Midday: 4").assertIsDisplayed()
            onNodeWithTag(HEATMAP_CHART_TAG).performTouchInput { click(cellPoint(row = TUE, column = EVENING)) }

            runOnIdle { assertEquals(expected = monMidday, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_changedDataClearsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = tueMorning)
            val data = mutableStateOf(activity)
            setContent { TestHeatmap(data.value, selection, HeatmapRenderMode.Cells) }
            runOnIdle { assertEquals(expected = tueMorning, actual = selection.selectedIndex) }

            runOnIdle { data.value = HeatmapData(activity.columns, listOf("Wed", "Thu"), activity.values) }

            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }
            onNodeWithText(TITLE).assertIsDisplayed()
        }

    @Test
    fun heatmapChart_equalDataKeepsSelection() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = tueMorning)
            val data = mutableStateOf(activity)
            setContent { TestHeatmap(data.value, selection, HeatmapRenderMode.Cells) }

            runOnIdle {
                data.value =
                    HeatmapData(activity.columns.toList(), activity.rows.toList(), activity.values.map { it.toList() })
            }

            runOnIdle { assertEquals(expected = tueMorning, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_programmaticSelectionOfAMissingOrOutOfRangeCellIsCleared() =
        runComposeUiTest {
            val selection = ChartSelection()
            setContent { TestHeatmap(activity, selection, HeatmapRenderMode.Cells) }

            runOnIdle { selection.select(monEveningMissing) }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }

            runOnIdle { selection.select(pastLastCell) }
            runOnIdle { assertEquals(expected = null, actual = selection.selectedIndex) }

            runOnIdle { selection.select(tueEvening) }
            runOnIdle { assertEquals(expected = tueEvening, actual = selection.selectedIndex) }
        }

    @Test
    fun heatmapChart_customFormatterFormatsSelectionAndLegend() =
        runComposeUiTest {
            val selection = ChartSelection(initialIndex = tueEvening)
            setContent {
                HeatmapChart(
                    data = activity,
                    modifier = Modifier.size(LABELED_CHART_WIDTH, LABELED_CHART_HEIGHT),
                    title = TITLE,
                    selection = selection,
                    valueFormatter = { "${it.toInt()} visits" },
                )
            }

            onNodeWithText("$TITLE · Tue, Evening: 9 visits").assertIsDisplayed()
            onNodeWithText("1 visits").assertIsDisplayed()
        }

    @Test
    fun heatmapChart_cellsAndInterpolatedModesDrawDifferentColorsFromTheSameValues() =
        runComposeUiTest {
            val low = 0.0
            val high = 10.0
            val data = HeatmapData(columns = listOf("A", "B"), rows = listOf("R"), values = listOf(listOf(low, high)))
            var mode by mutableStateOf(HeatmapRenderMode.Cells)
            val capture = setCapturedHeatmap(data) { mode }
            val middleLine = CAPTURE_HEIGHT_PX / 2
            val cellWidth = CAPTURE_WIDTH_PX / data.columns.size
            val quarterWay = 0.25f
            val halfWay = 0.5f

            val cells = capture().line(y = middleLine)
            runOnIdle { mode = HeatmapRenderMode.Interpolated }
            val interpolated = capture().line(y = middleLine)

            // Cells: flat colors and a gap at the shared edge.
            assertGray(expected = BLACK, actual = cells(cellWidth / 2), message = "cells, first center")
            assertGray(
                expected = BLACK,
                actual = cells(cellWidth - PLAIN_CELL_SPACING_PX),
                message = "cells, near the edge",
            )
            assertEquals(expected = TRANSPARENT, actual = cells(cellWidth).alpha, message = "cells, gap")
            assertGray(expected = WHITE, actual = cells(cellWidth * 3 / 2), message = "cells, second center")
            // Interpolated: the field blends from low at the first center to high at the second, with no gap.
            assertGray(expected = BLACK, actual = interpolated(cellWidth / 4), message = "interpolated, outer edge")
            assertGray(
                expected = quarterWay,
                actual = interpolated(cellWidth * 3 / 4),
                message = "interpolated, quarter way",
            )
            assertGray(expected = halfWay, actual = interpolated(cellWidth), message = "interpolated, shared edge")
            assertGray(expected = WHITE, actual = interpolated(cellWidth * 7 / 4), message = "interpolated, outer edge")
        }

    @Test
    fun heatmapChart_missingCellsDrawEmptyInBothModes() =
        runComposeUiTest {
            val data =
                HeatmapData(
                    columns = listOf("A", "B", "C"),
                    rows = listOf("R"),
                    values = listOf(listOf(0.0, null, 10.0)),
                )
            // The plot keeps its 2.5 aspect ratio, so 250 pixels across fill the 100 pixel capture height.
            val width = 250
            val cellWidth = width / data.columns.size
            val leftOfGap = cellWidth * 3 / 4
            var mode by mutableStateOf(HeatmapRenderMode.Cells)
            val capture = setCapturedHeatmap(data, width = width) { mode }

            HeatmapRenderMode.entries.forEach { nextMode ->
                runOnIdle { mode = nextMode }
                val pixels = capture().line(y = CAPTURE_HEIGHT_PX / 2)
                assertEquals(
                    expected = TRANSPARENT,
                    actual = pixels(width / 2).alpha,
                    message = "$nextMode, missing center",
                )
                assertGray(expected = BLACK, actual = pixels(leftOfGap), message = "$nextMode, left of the gap")
                assertGray(
                    expected = WHITE,
                    actual = pixels(width - leftOfGap),
                    message = "$nextMode, right of the gap",
                )
            }
        }

    @Test
    fun heatmapChart_interpolatedFieldHasNoStepAtCellEdgesBesideAMissingCell() =
        runComposeUiTest {
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
            val size = 300
            val cellSize = size / data.columns.size
            // Low in the first row, close to the missing center cell.
            val nearGap = cellSize * 4 / 5

            val pixel =
                setCapturedHeatmap(
                    data,
                    width = size,
                    height = size,
                ) { HeatmapRenderMode.Interpolated }().line(y = nearGap)

            // Either side of the edges between the first-row cells. Both pixels must be drawn, so an empty
            // capture cannot pass.
            listOf(cellSize, cellSize * 2).forEach { edge ->
                val left = pixel(edge - 1)
                val right = pixel(edge)
                assertTrue(left.alpha > OPAQUE && right.alpha > OPAQUE, "left $left, right $right")
                assertTrue(abs(left.red - right.red) <= MAX_EDGE_STEP, "left $left, right $right")
            }
        }

    @Test
    fun heatmapChart_reportsEveryInvalidInput() =
        runComposeUiTest {
            val collapsedBound = 5.0
            val negativeLabelSize = (-1).sp
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
                            colorScale =
                                HeatmapColorScale(
                                    listOf(Color.Red),
                                    min = collapsedBound,
                                    max = collapsedBound,
                                ),
                            xLabels = AxisLabelDefaults.style(size = negativeLabelSize),
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
    fun heatmapChart_reportsEmptyAndMisalignedMatrices() =
        runComposeUiTest {
            val cases =
                listOf(
                    HeatmapData(emptyList(), emptyList(), emptyList()) to
                        listOf("At least one column is required.", "At least one row is required."),
                    HeatmapData(listOf("A", "B"), listOf("R"), listOf(listOf(1.0))) to
                        listOf("Every value row must have one value per column label."),
                    HeatmapData(listOf("A"), listOf("R"), listOf(listOf(null))) to
                        listOf("At least one cell must have a value."),
                )
            var data by mutableStateOf(cases.first().first)
            setContent { HeatmapChart(data = data) }

            cases.forEach { (nextData, messages) ->
                runOnIdle { data = nextData }
                messages.forEach { message -> onNodeWithText(message, substring = true).assertExists() }
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
            val onlyValue = 3.0
            val onlyValueLabel = "3"
            val negativeSpacing = (-4).dp
            setContent {
                HeatmapChart(
                    data = HeatmapData(listOf("Only"), listOf("Row"), listOf(listOf(onlyValue))),
                    style =
                        HeatmapChartDefaults.style(
                            cellSpacing = negativeSpacing,
                            renderMode = HeatmapRenderMode.Interpolated,
                        ),
                )
            }

            onNodeWithTag(HEATMAP_CHART_TAG).assertIsDisplayed()
            onNodeWithText(onlyValueLabel).assertIsDisplayed()
        }

    @Test
    fun heatmapChart_tinyContainerWithASelectionDrawsWithoutCrashing() =
        runComposeUiTest {
            val tinySize = 1.dp
            setContent {
                HeatmapChart(
                    data = activity,
                    modifier = Modifier.size(width = tinySize, height = tinySize),
                    title = TITLE,
                    selection = ChartSelection(initialIndex = tueEvening),
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
        modifier: Modifier = Modifier.size(width = PLAIN_CHART_WIDTH, height = PLAIN_CHART_HEIGHT),
    ) {
        CompositionLocalProvider(LocalDensity provides ONE_PIXEL_PER_DP) {
            HeatmapChart(
                data = data,
                modifier = modifier,
                style = plainStyle(mode),
                title = TITLE,
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
            cellSpacing = PLAIN_CELL_SPACING_PX.dp,
            xLabels = AxisLabelDefaults.style(visible = false),
            yLabels = AxisLabelDefaults.style(visible = false),
            legend = LegendDefaults.style(visible = false),
        )

    /** A point in the activity grid, [x] of the way across the cell and vertically centered. */
    private fun TouchInjectionScope.cellPoint(
        row: Int,
        column: Int,
        x: Float = CELL_CENTER,
    ): Offset =
        Offset(
            x = width * (column + x) / activity.columns.size,
            y = height * (row + CELL_CENTER) / activity.rows.size,
        )

    /**
     * Draws [data] [width] by [height] pixels in the mode [mode] returns, and returns a function that
     * captures the current frame. Read [mode] from state to compare modes in one test.
     */
    private fun ComposeUiTest.setCapturedHeatmap(
        data: HeatmapData,
        width: Int = CAPTURE_WIDTH_PX,
        height: Int = CAPTURE_HEIGHT_PX,
        mode: () -> HeatmapRenderMode,
    ): () -> PixelMap =
        setCapturedContent { captureModifier ->
            CompositionLocalProvider(LocalDensity provides ONE_PIXEL_PER_DP) {
                HeatmapChart(
                    data = data,
                    modifier = Modifier.size(width.dp, height.dp).then(captureModifier),
                    style = plainStyle(mode()),
                    animateOnStart = false,
                )
            }
        }

    private fun PixelMap.line(y: Int): (Int) -> Color = { x -> this[x, y] }

    private fun assertGray(
        expected: Float,
        actual: Color,
        message: String,
    ) {
        assertTrue(
            actual.alpha > OPAQUE &&
                abs(actual.red - expected) <= GRAY_TOLERANCE &&
                abs(actual.green - expected) <= GRAY_TOLERANCE &&
                abs(actual.blue - expected) <= GRAY_TOLERANCE,
            "$message: expected gray $expected, actual $actual",
        )
    }
}
