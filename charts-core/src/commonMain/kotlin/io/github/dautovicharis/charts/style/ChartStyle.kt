package io.github.dautovicharis.charts.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A class that defines the style for a Chart View.
 *
 * @property modifierMain The main modifier to be applied to the chart view.
 * @property styleTitle The style to be applied to the title of the chart view.
 * @property modifierTopTitle The modifier to be applied to the top title of the chart view.
 * @property modifierLegend The modifier to be applied to the legend of the chart view.
 * @property innerPadding The inner padding of the chart view.
 * @property width The width of the chart view.
 * @property backgroundColor The background color of the chart view.
 * @property modifierChart The modifier applied to chart drawing content.
 */
@Immutable
class ChartViewStyle(
    val modifierMain: Modifier,
    val styleTitle: TextStyle,
    val modifierTopTitle: Modifier,
    val modifierLegend: Modifier,
    val innerPadding: Dp,
    val width: Dp,
    val backgroundColor: Color,
    val modifierChart: Modifier,
) {
    fun wrapContentChartModifier(contentPadding: Dp = innerPadding): Modifier =
        Modifier
            .wrapContentSize()
            .padding(contentPadding)
            .then(modifierChart)

    fun fillMaxSizeChartModifier(contentPadding: Dp = innerPadding): Modifier =
        Modifier
            .padding(contentPadding)
            .then(modifierChart)
            .fillMaxSize()
}

/**
 * An object that provides default styles for a Chart View.
 */
object ChartViewDefaults {
    /**
     * Returns a ChartViewStyle with the provided parameters or their default values.
     *
     * @param width The width of the chart view. Defaults to Dp.Infinity.
     * @param outerPadding The outer padding of the chart view. Defaults to 20.dp.
     * @param innerPadding The inner padding of the chart view. Defaults to 15.dp.
     * @param cornerRadius The corner radius of the chart view. Defaults to 20.dp.
     * @param shadow The shadow of the chart view. Defaults to 2.dp.
     * @param backgroundColor The background color of the chart view. Defaults to a subtle blend of surface and primaryContainer that adapts to dark mode.
     * @param modifierChart The modifier applied to chart drawing content. Defaults to a square aspect ratio.
     */
    @Composable
    fun style(
        width: Dp = Dp.Infinity,
        outerPadding: Dp = 20.dp,
        innerPadding: Dp = 15.dp,
        cornerRadius: Dp = 20.dp,
        shadow: Dp = 1.dp,
        backgroundColor: Color = defaultChartBackgroundColor(),
        modifierChart: Modifier = Modifier.aspectRatio(1f),
    ): ChartViewStyle {
        val modifierTitle: Modifier = Modifier.padding(top = innerPadding, start = innerPadding)
        val modifierLegend: Modifier =
            Modifier
                .wrapContentSize()
                .padding(start = innerPadding, end = innerPadding, bottom = innerPadding)

        val modifierMain: Modifier =
            Modifier
                .wrapContentHeight()
                .padding(outerPadding)
                .shadow(elevation = shadow, shape = RoundedCornerShape(cornerRadius))
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(cornerRadius),
                )

        val updatedModifierMain =
            when (width) {
                // TODO: Double check this logic
                Dp.Infinity -> modifierMain.wrapContentWidth()
                else -> modifierMain.width(width)
            }

        val titleStyle =
            TextStyle(
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold,
            )

        return ChartViewStyle(
            modifierMain = updatedModifierMain,
            styleTitle = titleStyle,
            modifierTopTitle = modifierTitle,
            modifierLegend = modifierLegend,
            innerPadding = innerPadding,
            width = width,
            backgroundColor = backgroundColor,
            modifierChart = modifierChart,
        )
    }
}
