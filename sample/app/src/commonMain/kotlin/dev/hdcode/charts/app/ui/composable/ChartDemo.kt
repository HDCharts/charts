package dev.hdcode.charts.app.ui.composable

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier

@Composable
fun ChartDemo(
    styleItems: StyleItems,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshVisible: Boolean = true,
    onStyleItemsChanged: (StyleItems?) -> Unit = {},
    extraButtons: @Composable RowScope.() -> Unit = {},
    presetContent: @Composable () -> Unit = {},
    controlsContent: @Composable () -> Unit = {},
    chartItem: @Composable () -> Unit,
) {
    SideEffect {
        onStyleItemsChanged(styleItems)
    }

    DisposableEffect(onStyleItemsChanged) {
        onDispose {
            onStyleItemsChanged(null)
        }
    }

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
