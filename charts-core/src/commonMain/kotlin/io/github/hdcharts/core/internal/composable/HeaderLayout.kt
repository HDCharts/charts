package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.hdcharts.core.internal.InternalChartsApi

@Composable
@InternalChartsApi
fun ChartHeaderLayout(
    title: String,
    titleTextStyle: TextStyle,
    showControls: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String?,
    subtitleTextStyle: TextStyle,
    controls: @Composable () -> Unit = {},
) {
    if (!subtitle.isNullOrBlank()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartHeading(
                title = title,
                titleTextStyle = titleTextStyle,
                subtitle = subtitle,
                subtitleTextStyle = subtitleTextStyle,
                modifier = Modifier.weight(1f),
            )
            if (showControls) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    controls()
                }
            }
        }
        return
    }
    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        ChartHeading(
            title = title,
            titleTextStyle = titleTextStyle,
            subtitle = subtitle,
            subtitleTextStyle = subtitleTextStyle,
            modifier = Modifier.align(Alignment.TopStart),
        )

        if (showControls) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                controls()
            }
        }
    }
}
