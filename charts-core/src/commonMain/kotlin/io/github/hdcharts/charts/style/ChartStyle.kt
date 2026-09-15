package io.github.hdcharts.charts.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val DEFAULT_CHART_CONTENT_PADDING_DP = 15

private fun defaultChartContentPadding(): Dp = DEFAULT_CHART_CONTENT_PADDING_DP.dp

/**
 * Presentation-only container style shared by every chart.
 *
 * The caller controls the surrounding chrome (background, shadow, shape, and outer padding) and
 * the chart's size through the chart `modifier`. The style supplies the chart-internal content
 * spacing and the title text style.
 *
 * @property styleTitle The text style applied to chart titles.
 * @property contentPadding The internal spacing used for axes, title, legend, and plot content.
 */
@Immutable
class ChartContainerStyle(
    val styleTitle: TextStyle,
    val contentPadding: Dp,
)

/**
 * An object that provides default styles for a chart container.
 */
object ChartContainerDefaults {
    /**
     * Returns a [ChartContainerStyle] with the provided parameters or their default values.
     *
     * The container applies only chart content spacing. Use the chart composable's
     * `modifier` for width, height, placement, and surrounding padding. Wrap the chart in a
     * `Surface`, `Card`, or `Modifier.background()` to add chrome when desired.
     *
     * @param contentPadding The spacing reserved inside the chart for axes and supporting content.
     */
    @Composable
    fun style(contentPadding: Dp = defaultChartContentPadding()): ChartContainerStyle {
        val titleStyle =
            TextStyle(
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold,
            )
        return ChartContainerStyle(
            styleTitle = titleStyle,
            contentPadding = contentPadding,
        )
    }
}
