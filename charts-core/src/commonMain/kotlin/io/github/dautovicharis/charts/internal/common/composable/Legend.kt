package io.github.dautovicharis.charts.internal.common.composable

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.style.ChartContainerStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun Legend(
    chartContainerStyle: ChartContainerStyle,
    legend: ImmutableList<String>,
    colors: ImmutableList<Color>,
    labels: ImmutableList<String> = persistentListOf(),
) {
    LegendItems(
        items = legend,
        colors = colors,
        fallbackColor = MaterialTheme.colorScheme.primary,
        itemPadding = chartContainerStyle.innerPadding,
        modifier =
            chartContainerStyle.modifierLegend.animateContentSize(
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing,
                    ),
            ),
        label = { index, legendItem -> legendLabel(legendItem = legendItem, labels = labels, index = index) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegendItems(
    items: ImmutableList<String>,
    colors: ImmutableList<Color>?,
    fallbackColor: Color,
    itemPadding: Dp,
    modifier: Modifier = Modifier,
    label: (index: Int, item: String) -> String = { _, item -> item },
) {
    FlowRow(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val color = (colors?.getOrNull(index) ?: fallbackColor).copy(alpha = 1f)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.padding(
                        end = itemPadding,
                        bottom = itemPadding / 2f,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(color, shape = CircleShape),
                )

                Text(
                    text = label(index, item),
                    color = color,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

private fun legendLabel(
    legendItem: String,
    labels: ImmutableList<String>,
    index: Int,
): String {
    if (labels.isEmpty()) return legendItem
    return labels.getOrNull(index)?.let { "$legendItem - $it" } ?: legendItem
}
