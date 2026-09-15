package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.charts.style.ChartContainerStyle

@Composable
fun Chart(
    chartContainerStyle: ChartContainerStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.then(chartContainerStyle.modifierMain),
    ) {
        Column(
            modifier =
                Modifier
                    .wrapContentSize(),
        ) {
            content()
        }
    }
}
