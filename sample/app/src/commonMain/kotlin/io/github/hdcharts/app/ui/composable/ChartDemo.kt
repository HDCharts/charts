package io.github.hdcharts.app.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.cd_refresh_data
import hdcharts.app.generated.resources.cd_regenerate_chart
import hdcharts.app.generated.resources.ic_replay
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

internal val LocalChartDemoMaxWidth = staticCompositionLocalOf { Dp.Infinity }

@Composable
fun ChartDemo(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshVisible: Boolean = true,
    extraButtons: @Composable RowScope.() -> Unit = {},
    presetContent: @Composable () -> Unit = {},
    controlsContent: @Composable () -> Unit = {},
    chartItem: @Composable () -> Unit,
) {
    var chartRefreshKey by remember { mutableIntStateOf(0) }

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
            presetContent()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            key(chartRefreshKey) {
                DrawerGestureLockContainer(
                    modifier = Modifier.widthIn(max = LocalChartDemoMaxWidth.current),
                ) {
                    chartItem()
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = { chartRefreshKey += 1 }) {
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

        controlsContent()
    }
}
