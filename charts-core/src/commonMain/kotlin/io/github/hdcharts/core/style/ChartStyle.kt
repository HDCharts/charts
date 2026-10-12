package io.github.hdcharts.core.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

/**
 * Presentation-only container style shared by every chart.
 *
 * The caller controls the surrounding chrome (background, shadow, shape, and outer padding) and
 * the chart's size through the chart `modifier`. The style supplies the chart-internal content
 * spacing and the title and subtitle text styles.
 *
 * @property styleTitle The text style applied to chart titles.
 * @property contentPadding The internal spacing used for axes, title, legend, and plot content.
 * @property styleSubtitle The text style applied to chart subtitles.
 */
@Immutable
class ChartContainerStyle(
    val styleTitle: TextStyle,
    val contentPadding: Dp,
    val styleSubtitle: TextStyle =
        TextStyle(
            fontSize = StyleDefaults.subtitleSize,
            fontWeight = StyleDefaults.subtitleWeight,
            textAlign = TextAlign.Start,
        ),
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
     * @param styleTitle The text style applied to the title.
     * @param styleSubtitle The text style applied to the subtitle.
     */
    @Composable
    fun style(
        contentPadding: Dp = StyleDefaults.containerPadding,
        styleTitle: TextStyle =
            TextStyle(
                fontSize = StyleDefaults.titleSize,
                color = StyleDefaults.titleColor,
                textAlign = TextAlign.Start,
                fontWeight = StyleDefaults.titleWeight,
            ),
        styleSubtitle: TextStyle =
            TextStyle(
                fontSize = StyleDefaults.subtitleSize,
                color = StyleDefaults.subtitleColor,
                textAlign = TextAlign.Start,
                fontWeight = StyleDefaults.subtitleWeight,
            ),
    ): ChartContainerStyle =
        ChartContainerStyle(
            styleTitle = styleTitle,
            contentPadding = contentPadding,
            styleSubtitle = styleSubtitle,
        )
}
