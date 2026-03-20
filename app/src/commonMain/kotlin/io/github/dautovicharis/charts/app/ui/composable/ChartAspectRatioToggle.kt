package io.github.dautovicharis.charts.app.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.chart_aspect_square
import chartsproject.app.generated.resources.chart_aspect_tall
import chartsproject.app.generated.resources.chart_aspect_wide
import org.jetbrains.compose.resources.stringResource

enum class ChartAspectRatioPreset(
    val ratio: Float,
) {
    Square(1f),
    Wide16x9(16f / 9f),
    Tall3x4(3f / 4f),
}

@Composable
fun ChartAspectRatioToggle(
    selectedPreset: ChartAspectRatioPreset,
    onPresetSelected: (ChartAspectRatioPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        AspectRatioItem(
            label = stringResource(Res.string.chart_aspect_square),
            selected = selectedPreset == ChartAspectRatioPreset.Square,
            onClick = { onPresetSelected(ChartAspectRatioPreset.Square) },
        )
        AspectRatioItem(
            label = stringResource(Res.string.chart_aspect_wide),
            selected = selectedPreset == ChartAspectRatioPreset.Wide16x9,
            onClick = { onPresetSelected(ChartAspectRatioPreset.Wide16x9) },
        )
        AspectRatioItem(
            label = stringResource(Res.string.chart_aspect_tall),
            selected = selectedPreset == ChartAspectRatioPreset.Tall3x4,
            onClick = { onPresetSelected(ChartAspectRatioPreset.Tall3x4) },
        )
    }
}

@Composable
private fun AspectRatioItem(
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
                .wrapContentSize()
                .clip(shape)
                .background(backgroundColor, shape)
                .clickable(onClick = onClick)
                .semantics { role = Role.Button }
                .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

fun ChartAspectRatioPreset.toChartModifier(): Modifier = Modifier.aspectRatio(ratio)
