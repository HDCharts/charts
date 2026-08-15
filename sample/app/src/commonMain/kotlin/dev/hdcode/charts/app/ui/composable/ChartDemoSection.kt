package dev.hdcode.charts.app.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_refresh_data
import chartsproject.app.generated.resources.cd_regenerate_chart
import chartsproject.app.generated.resources.ic_replay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChartDemoSection(
    modifier: Modifier = Modifier,
    buttonsVisibility: Boolean = true,
    refreshVisible: Boolean = true,
    extraButtons: @Composable RowScope.() -> Unit = {},
    presetContent: @Composable () -> Unit = {},
    controlsContent: @Composable () -> Unit = {},
    chartContent: @Composable () -> Unit,
    onRefresh: () -> Unit,
) {
    val contentPadding = 16.dp
    var chartRefreshKey by remember { mutableIntStateOf(0) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            presetContent()
        }

        ChartPreviewSection(chartContent = chartContent, refreshKey = chartRefreshKey)

        if (buttonsVisibility) {
            ChartActionRow(
                onRegenerateChart = { chartRefreshKey += 1 },
                extraButtons = extraButtons,
                onRefresh = onRefresh,
                refreshVisible = refreshVisible,
            )
        }

        controlsContent()
    }
}

@Composable
private fun ChartPreviewSection(
    chartContent: @Composable () -> Unit,
    refreshKey: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        key(refreshKey) {
            DrawerGestureLockContainer(
                modifier = Modifier.widthIn(max = LocalChartDemoMaxWidth.current),
            ) {
                chartContent()
            }
        }
    }
}

@Composable
private fun ChartActionRow(
    onRegenerateChart: () -> Unit,
    extraButtons: @Composable RowScope.() -> Unit,
    onRefresh: () -> Unit,
    refreshVisible: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onClick = onRegenerateChart,
        ) {
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
