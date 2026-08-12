package dev.hdcode.charts.app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.chart_custom
import chartsproject.app.generated.resources.chart_default
import org.jetbrains.compose.resources.stringResource

enum class ChartPreset {
    Default,
    Custom,
}

@Composable
fun ChartPresetToggle(
    selectedPreset: ChartPreset,
    onPresetSelected: (ChartPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PresetItem(
            label = stringResource(Res.string.chart_default),
            selected = selectedPreset == ChartPreset.Default,
            onClick = { onPresetSelected(ChartPreset.Default) },
        )
        PresetItem(
            label = stringResource(Res.string.chart_custom),
            selected = selectedPreset == ChartPreset.Custom,
            onClick = { onPresetSelected(ChartPreset.Custom) },
        )
    }
}

@Composable
private fun PresetItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        }
    val textColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        }

    Text(
        text = label,
        color = textColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        modifier =
            Modifier
                .clip(shape)
                .background(backgroundColor, shape)
                .clickable(onClick = onClick)
                .semantics { role = Role.Button }
                .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
