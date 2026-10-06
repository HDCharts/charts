package io.github.hdcharts.core.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.InternalChartsApi

/** Every chart style default in one place, so the same element looks the same in every chart.
 *
 * When adding a new default here, also update [StyleDefaultsTest.defaults_matchCurrentValues] with
 * an assertion for the new property. */
@InternalChartsApi
object StyleDefaults {
    // ===========================================================================
    // Shared by ALL charts
    // ===========================================================================

    /** Grid, axis, selection, and border lines. */
    val lineWidth: Dp = 1.dp

    /** Lines that draw a series, such as a line chart line or a radar polygon outline. */
    val seriesLineWidth: Dp = 2.dp

    /** Point radius. */
    val pointSize: Dp = 4.dp

    /** Points are hidden at rest; the selected point is drawn even when they are hidden. */
    val pointsVisible: Boolean = false

    /** The legend shows by default; it still hides itself without two items and a name. */
    val legendVisible: Boolean = true

    /** Radius of a point on the selected position, drawn larger than [pointSize] so it stands out. */
    val selectedPointSize: Dp = 5.dp

    val barSpacing: Dp = 10.dp
    val minBarWidth: Dp = 10.dp
    val axisLabelSize: TextUnit = 11.sp

    /** Gap between an axis and its labels. */
    val axisLabelPadding: Dp = 10.dp
    val seriesAlpha: Float = 1f
    val unselectedAlpha: Float = 0.7f
    val gridSteps: Int = 4
    val containerPadding: Dp = 15.dp
    val titleSize: TextUnit = 20.sp
    val titleWeight: FontWeight = FontWeight.ExtraBold

    val seriesColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.primary

    val pointColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.tertiary

    val gridColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outlineVariant

    val axisColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.outline

    val axisLabelColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurfaceVariant

    val selectionColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    val titleColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.onSurface

    // ===========================================================================
    // Histogram
    // ===========================================================================

    /** Histogram bins touch, so there is no spacing between them. */
    val histogramBarSpacing: Dp = 0.dp

    /** A bin count of zero renders at the baseline. */
    val histogramRangeMin: Double = 0.0

    // ===========================================================================
    // Line
    // ===========================================================================

    /** Radius of the marker that follows the touch along the line. */
    val lineSelectionMarkerSize: Dp = 3.dp

    // ===========================================================================
    // Pie
    // ===========================================================================

    val pieDonutHole: Float = 0f

    val pieBorderColor: Color
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface

    // ===========================================================================
    // Radar
    // ===========================================================================

    /** Radar polygons overlap each other and the grid, so a solid fill would hide the series behind it. */
    val radarFillAlpha: Float = 0.25f

    /** Lower than [unselectedAlpha], because translucent radar fills barely change at that value. */
    val radarUnfocusedSeriesAlpha: Float = 0.35f

    /** Each category name sits at the end of its axis, so the web shrinks to leave room for them. */
    val radarAxisLabelsVisible: Boolean = true

    /**
     * How far a radar label that does not fit stays from the canvas edge. It is not reserved from the
     * web: such a label moves inside the canvas instead, so only a too small chart pays for it.
     */
    val radarLabelEdgePadding: Dp = 6.dp
}
