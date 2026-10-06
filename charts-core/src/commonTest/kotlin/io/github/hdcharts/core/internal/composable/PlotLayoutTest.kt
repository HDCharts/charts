package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.InternalChartsApi
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PLOT_TAG = "plot"
private const val HALF_CIRCLE_ASPECT_RATIO = 2f

@OptIn(InternalChartsApi::class, ExperimentalTestApi::class)
class PlotLayoutTest {
    /** A wide plot in a box too short for it keeps its shape and centers, as a gauge's half circle does. */
    @Test
    fun plotLayout_wideAspectRatioInShortBox_keepsShapeAndCenters() =
        runComposeUiTest {
            setContent {
                ChartPlotLayout(
                    plotAspectRatio = HALF_CIRCLE_ASPECT_RATIO,
                    modifier = Modifier.size(width = 600.dp, height = 200.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize().testTag(PLOT_TAG))
                }
            }

            val plotBounds = onNodeWithTag(PLOT_TAG).fetchSemanticsNode().boundsInRoot

            assertEquals(expected = 300f, actual = plotBounds.center.x, absoluteTolerance = 1.5f)
            assertEquals(expected = 400f, actual = plotBounds.width, absoluteTolerance = 1.5f)
            assertEquals(expected = 200f, actual = plotBounds.height, absoluteTolerance = 1.5f)
        }

    /** In a box with room to spare, a wide plot takes the full width and only the height its shape needs. */
    @Test
    fun plotLayout_wideAspectRatioInTallBox_takesFullWidth() =
        runComposeUiTest {
            setContent {
                ChartPlotLayout(
                    plotAspectRatio = HALF_CIRCLE_ASPECT_RATIO,
                    modifier = Modifier.size(width = 400.dp, height = 400.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize().testTag(PLOT_TAG))
                }
            }

            val plotBounds = onNodeWithTag(PLOT_TAG).fetchSemanticsNode().boundsInRoot

            assertEquals(expected = 400f, actual = plotBounds.width, absoluteTolerance = 1.5f)
            assertEquals(expected = 200f, actual = plotBounds.height, absoluteTolerance = 1.5f)
        }
}
