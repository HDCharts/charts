package io.github.hdcharts.app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.cd_pause_live_updates
import hdcharts.app.generated.resources.cd_play_live_updates
import hdcharts.app.generated.resources.cd_refresh_data
import hdcharts.app.generated.resources.cd_regenerate_chart
import hdcharts.app.generated.resources.ic_replay
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Caps the chart width in the stacked layout; the side panel layout uses [SidePanelChartMaxWidth].
internal val LocalChartDemoMaxWidth = staticCompositionLocalOf { Dp.Infinity }

// Set by MainScreen when it shows the wide layout, so chart pages switch together with the settings rail.
internal val LocalChartDemoSidePanel = staticCompositionLocalOf { false }

private val SidePanelChartMaxWidth = 1200.dp

@Composable
fun ChartDemo(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshVisible: Boolean = true,
    extraButtons: @Composable RowScope.() -> Unit = {},
    controlsContent: @Composable () -> Unit = {},
    chartItem: @Composable () -> Unit,
) {
    var chartRefreshKey by remember { mutableIntStateOf(0) }
    val currentChartItem by rememberUpdatedState(chartItem)
    val currentControlsContent by rememberUpdatedState(controlsContent)
    // Movable so the chart and sliders keep their state when the layout switches.
    val chart =
        remember {
            movableContentOf {
                key(chartRefreshKey) {
                    currentChartItem()
                }
            }
        }
    val controls = remember { movableContentOf { currentControlsContent() } }
    val actions: @Composable () -> Unit = {
        ChartDemoActions(
            onRegenerate = { chartRefreshKey += 1 },
            onRefresh = onRefresh,
            refreshVisible = refreshVisible,
            extraButtons = extraButtons,
        )
    }

    if (LocalChartDemoSidePanel.current) {
        SidePanelChartDemo(
            actions = actions,
            controls = controls,
            chart = chart,
            modifier = modifier,
        )
    } else {
        StackedChartDemo(
            actions = actions,
            controls = controls,
            chart = chart,
            modifier = modifier,
        )
    }
}

@Composable
private fun StackedChartDemo(
    actions: @Composable () -> Unit,
    controls: @Composable () -> Unit,
    chart: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            DrawerGestureLockContainer(
                modifier = Modifier.widthIn(max = LocalChartDemoMaxWidth.current),
            ) {
                chart()
            }
        }

        actions()
        controls()
    }
}

// Chart fills the height left after the top bar; the panel mirrors the settings rail on the left.
@Composable
private fun SidePanelChartDemo(
    actions: @Composable () -> Unit,
    controls: @Composable () -> Unit,
    chart: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(Dimens.cardPadding),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.widthIn(max = SidePanelChartMaxWidth)) {
                chart()
            }
        }

        Column(
            modifier =
                Modifier
                    .width(Dimens.sideRailWidth)
                    .fillMaxHeight()
                    .shadow(Dimens.sideRailElevation)
                    .background(MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.drawerPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            actions()
            controls()
        }
    }
}

@Composable
private fun ChartDemoActions(
    onRegenerate: () -> Unit,
    onRefresh: () -> Unit,
    refreshVisible: Boolean,
    extraButtons: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = onRegenerate) {
            Icon(
                painter = painterResource(Res.drawable.ic_replay),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = stringResource(Res.string.cd_regenerate_chart),
            )
        }

        extraButtons()

        if (refreshVisible) {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(Res.string.cd_refresh_data),
                )
            }
        }
    }
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription =
                stringResource(
                    if (isPlaying) {
                        Res.string.cd_pause_live_updates
                    } else {
                        Res.string.cd_play_live_updates
                    },
                ),
        )
    }
}
