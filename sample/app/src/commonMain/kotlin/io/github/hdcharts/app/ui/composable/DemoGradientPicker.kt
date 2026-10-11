package io.github.hdcharts.app.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.gradient_fade
import hdcharts.app.generated.resources.gradient_fade_older
import hdcharts.app.generated.resources.gradient_per_bar
import hdcharts.app.generated.resources.gradient_plot_horizontal
import hdcharts.app.generated.resources.gradient_plot_vertical
import io.github.hdcharts.core.style.ChartGradient
import io.github.hdcharts.core.style.ChartGradients
import io.github.hdcharts.core.style.GradientPoint
import io.github.hdcharts.core.style.GradientSpan
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DemoGradientColors = listOf(Color(0xFF1E89E6), Color(0xFF9B4EDD), Color(0xFFEA7A2E))

enum class DemoGradient(
    val label: StringResource,
    val gradient: ChartGradient,
) {
    PerBar(
        label = Res.string.gradient_per_bar,
        gradient = ChartGradients.vertical(colors = DemoGradientColors, span = GradientSpan.Shape),
    ),
    PlotHorizontal(
        label = Res.string.gradient_plot_horizontal,
        gradient = ChartGradients.horizontal(colors = DemoGradientColors, span = GradientSpan.Plot),
    ),
    PlotVertical(
        label = Res.string.gradient_plot_vertical,
        gradient = ChartGradients.vertical(colors = DemoGradientColors, span = GradientSpan.Plot),
    ),
    Fade(
        label = Res.string.gradient_fade,
        gradient = ChartGradients.fade(endAlpha = 0.35f),
    ),
    FadeOlder(
        label = Res.string.gradient_fade_older,
        // Fades from the line's color at the last point to 35% at the first.
        gradient =
            ChartGradients.fade(endAlpha = 0.35f, span = GradientSpan.Plot).copy(
                start = GradientPoint(x = 1f, y = 0.5f),
                end = GradientPoint(x = 0f, y = 0.5f),
            ),
    ),
    ;

    companion object {
        val barOptions = listOf(PerBar, PlotHorizontal, PlotVertical, Fade)
        val lineOptions = listOf(PlotVertical, PlotHorizontal, FadeOlder)
        val liveLineOptions = lineOptions + Fade
    }
}

@Composable
fun DemoGradientPicker(
    options: List<DemoGradient>,
    selected: DemoGradient,
    onSelected: (DemoGradient) -> Unit,
    modifier: Modifier = Modifier,
    perShapeLabel: StringResource = Res.string.gradient_per_bar,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = {
                    val label = if (option == DemoGradient.PerBar) perShapeLabel else option.label
                    Text(stringResource(label))
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
            )
        }
    }
}
