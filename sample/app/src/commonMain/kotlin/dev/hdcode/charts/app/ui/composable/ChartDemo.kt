package dev.hdcode.charts.app.ui.composable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ChartDemo(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    buttonsVisibility: Boolean = true,
    refreshVisible: Boolean = true,
    extraButtons: @Composable RowScope.() -> Unit = {},
    presetContent: @Composable () -> Unit = {},
    controlsContent: @Composable () -> Unit = {},
    chartItem: @Composable () -> Unit,
) {
    ChartDemoSection(
        modifier = modifier.verticalScroll(rememberScrollState()),
        extraButtons = extraButtons,
        presetContent = presetContent,
        controlsContent = controlsContent,
        onRefresh = onRefresh,
        refreshVisible = refreshVisible,
        chartContent = chartItem,
    )
}
