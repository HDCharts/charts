package io.github.dautovicharis.charts.internal.radarchart

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.dautovicharis.charts.internal.common.composable.LegendItems
import io.github.dautovicharis.charts.style.ChartContainerStyle
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun RadarLegend(
    chartContainerStyle: ChartContainerStyle,
    series: ImmutableList<String>,
    seriesColors: ImmutableList<Color>,
    seriesLabels: ImmutableList<String>,
    categories: ImmutableList<String>,
    categoryColors: ImmutableList<Color>,
) {
    Column(
        modifier =
            chartContainerStyle.modifierLegend.animateContentSize(
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing,
                    ),
            ),
    ) {
        if (series.isNotEmpty()) {
            LegendTitle("Series")
            Spacer(modifier = Modifier.height(chartContainerStyle.innerPadding / 2f))
            LegendItems(
                items = series,
                colors = seriesColors,
                fallbackColor = MaterialTheme.colorScheme.primary,
                itemPadding = chartContainerStyle.innerPadding,
                label = { index, seriesName ->
                    seriesLegendLabel(
                        seriesName = seriesName,
                        labels = seriesLabels,
                        index = index,
                    )
                },
            )
        }

        if (categories.isNotEmpty()) {
            if (series.isNotEmpty()) {
                Spacer(modifier = Modifier.height(chartContainerStyle.innerPadding / 2f))
            }
            LegendTitle("Categories")
            Spacer(modifier = Modifier.height(chartContainerStyle.innerPadding / 2f))
            val neutral = MaterialTheme.colorScheme.onSurfaceVariant
            LegendItems(
                items = categories,
                colors = categoryColors,
                fallbackColor = neutral,
                itemPadding = chartContainerStyle.innerPadding,
            )
        }
    }
}

private fun seriesLegendLabel(
    seriesName: String,
    labels: ImmutableList<String>,
    index: Int,
): String {
    if (labels.isEmpty()) return seriesName
    val value = labels.getOrNull(index)
    return if (value.isNullOrBlank()) seriesName else "$seriesName - $value"
}

@Composable
private fun LegendTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
