package io.github.hdcharts.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.internal.model.ChartRenderData
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartContainerStyle
import kotlinx.collections.immutable.toImmutableList

/**
 * Prepares indexed chart input through [spec], then hands the result to [content].
 *
 * Validates [data] and the policy's style inputs, converts valid data through [ChartSpec.convert],
 * then clamps [style] at the current density through [ChartSpec.clamp]. Errors render through
 * [errorStyle] and return before conversion or clamping. [additionalErrors] joins the policy errors.
 *
 * Content owns layout, animation, and drawing. Work that needs a measured size runs there.
 *
 * Validation extracts a value-based snapshot from [style] and remembers errors against [spec],
 * [data], the snapshot, density and [additionalErrors]. Conversion is remembered against [spec],
 * [data] and [title].
 * Clamping is remembered against [spec], [style] and density.
 */
@InternalChartsApi
@Composable
fun <S : Any> ChartEntry(
    spec: ChartSpec<S>,
    data: ChartData,
    style: S,
    errorStyle: ChartContainerStyle,
    content: @Composable (data: ChartRenderData, style: S) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    additionalErrors: List<String> = emptyList(),
) {
    val density = LocalDensity.current
    val policy = spec.policy
    val snapshot = policy.snapshot(style)
    val errors =
        remember(data, spec, snapshot, density, additionalErrors) {
            policy.errorsFor(data = data, inputs = snapshot, density = density) + additionalErrors
        }
    if (errors.isNotEmpty()) {
        ChartErrors(
            style = errorStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
        return
    }
    // Convert to internal data
    val internalData = remember(spec, data, title) { spec.convert(data, title) }
    // Clamp unsafe styles
    val drawStyle = remember(spec, style, density) { spec.clamp(style, density) }
    content(internalData, drawStyle)
}
