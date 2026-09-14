package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.internal.TestTags

@Composable
@InternalChartsApi
fun ChartHeaderLayout(
    title: String,
    titleTextStyle: TextStyle,
    showControls: Boolean,
    modifier: Modifier = Modifier,
    controls: @Composable () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        if (title.isNotBlank()) {
            Text(
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .testTag(TestTags.CHART_TITLE),
                text = title,
                style = titleTextStyle,
            )
        }

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
