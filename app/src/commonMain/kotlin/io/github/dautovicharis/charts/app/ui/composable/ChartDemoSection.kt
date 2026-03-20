package io.github.dautovicharis.charts.app.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_refresh_data
import chartsproject.app.generated.resources.cd_regenerate_chart
import chartsproject.app.generated.resources.ic_replay
import chartsproject.app.generated.resources.style_status_count
import chartsproject.app.generated.resources.style_status_default
import chartsproject.app.generated.resources.style_status_modified
import chartsproject.app.generated.resources.style_status_unused
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
fun StyleDetailsTable(
    styleItems: StyleItems,
    modifier: Modifier = Modifier,
) {
    val columnWeight = 0.5f
    StyleDetailsTableContent(
        items = styleItems.items,
        columnWeight = columnWeight,
        modifier = modifier,
    )
}

@Composable
private fun StyleDetailsTableContent(
    items: List<StyleItem>,
    columnWeight: Float,
    modifier: Modifier = Modifier,
) {
    val modifiedItems = items.filter { it.status == StyleItemStatus.Modified }
    val defaultItems = items.filter { it.status == StyleItemStatus.Default }
    val unusedItems = items.filter { it.status == StyleItemStatus.Unused }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (modifiedItems.isNotEmpty()) {
            StyleDetailsSectionTitle(
                status = StyleItemStatus.Modified,
                count = modifiedItems.size,
            )
            modifiedItems.forEach { item ->
                StyleDetailsTableRow(item = item, columnWeight = columnWeight)
            }
        }

        if (defaultItems.isNotEmpty()) {
            StyleDetailsSectionTitle(
                status = StyleItemStatus.Default,
                count = defaultItems.size,
            )
            defaultItems.forEach { item ->
                StyleDetailsTableRow(item = item, columnWeight = columnWeight)
            }
        }

        if (unusedItems.isNotEmpty()) {
            StyleDetailsSectionTitle(
                status = StyleItemStatus.Unused,
                count = unusedItems.size,
            )
            unusedItems.forEach { item ->
                StyleDetailsTableRow(item = item, columnWeight = columnWeight)
            }
        }
    }
}

@Composable
private fun StyleDetailsSectionTitle(
    status: StyleItemStatus,
    count: Int,
) {
    Row(
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StyleStatusChip(status = status)
        Text(
            text = stringResource(Res.string.style_status_count, count),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StyleDetailsTableRow(
    item: StyleItem,
    columnWeight: Float,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface),
                shape = MaterialTheme.shapes.small,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            StyleDetailsTableCell(text = item.name, weight = columnWeight)
            Spacer(modifier = Modifier.width(1.dp))
            StyleDetailsTableCell(
                text = item.value,
                weight = columnWeight,
                color = item.color,
                colorChips = item.colors,
            )
        }

        if (!item.extraInfo.isNullOrBlank() || !item.unusedReason.isNullOrBlank()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!item.extraInfo.isNullOrBlank()) {
                    Text(
                        text = item.extraInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!item.unusedReason.isNullOrBlank()) {
                    Text(
                        text = item.unusedReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.StyleDetailsTableCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color? = null,
    colorChips: List<Color> = emptyList(),
) {
    Column(
        modifier =
            Modifier
                .weight(weight)
                .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (color != null) {
                StyleColorBadge(color = color)
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = fontWeight,
            )
        }
        if (colorChips.isNotEmpty()) {
            StyleColorChips(colors = colorChips)
        }
    }
}

@Composable
private fun StyleColorBadge(color: Color) {
    Box(
        modifier =
            Modifier
                .size(11.dp)
                .background(color, CircleShape)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape,
                ),
    )
}

@Composable
private fun StyleColorChips(colors: List<Color>) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        colors.forEach { chipColor ->
            Box(
                modifier =
                    Modifier
                        .size(11.dp)
                        .background(chipColor, CircleShape)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        ),
            )
        }
    }
}

@Composable
private fun StyleStatusChip(status: StyleItemStatus) {
    val (label, backgroundColor, textColor) =
        when (status) {
            StyleItemStatus.Modified -> {
                Triple(
                    stringResource(Res.string.style_status_modified),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    MaterialTheme.colorScheme.primary,
                )
            }

            StyleItemStatus.Default -> {
                Triple(
                    stringResource(Res.string.style_status_default),
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            StyleItemStatus.Unused -> {
                Triple(
                    stringResource(Res.string.style_status_unused),
                    MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    MaterialTheme.colorScheme.error,
                )
            }
        }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
        )
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
