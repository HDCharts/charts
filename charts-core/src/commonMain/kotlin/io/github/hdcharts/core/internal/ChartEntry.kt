package io.github.hdcharts.core.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.hdcharts.core.internal.composable.ChartErrors
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.style.ChartContainerStyle
import kotlinx.collections.immutable.toImmutableList

/**
 * Runs the input stages every chart shares, then hands the result to [content]. Pipeline stages 2,
 * 3 and 4, and the chart answers all three through [spec]:
 *
 *  1. Stage 2, validation. [data] is checked against [ChartSpec.policy] and the style values
 *     [ChartSpec.validationInputs] returns. Errors render through [errorStyle] and nothing else
 *     runs — which is why clamping comes last rather than in stage order.
 *  2. Stage 4, conversion. The validated [data] becomes the chart's render model, once, through
 *     [ChartSpec.convert].
 *  3. Stage 3, clamping. [style] is clamped for drawing at the current density, through
 *     [ChartSpec.clamp].
 *
 * Stages that need layout size — domain, normalization, density, drawing — belong in the content
 * composable, below this seam, because they cannot be decided before constraints are known.
 *
 * All three run inside a `remember`, so none may capture state that changes without [spec],
 * [style], [data], or [title] changing.
 */
@InternalChartsApi
@Composable
fun <S : Any, M : Any> ChartEntry(
    spec: ChartSpec<S, M>,
    data: ChartData,
    style: S,
    errorStyle: ChartContainerStyle,
    content: @Composable (data: M, style: S) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val density = LocalDensity.current
    val inputs = spec.validationInputs(style)
    val errors =
        remember(data, spec, inputs, density) {
            spec.policy.errorsFor(data = data, inputs = inputs, density = density)
        }
    if (errors.isNotEmpty()) {
        ChartErrors(
            style = errorStyle,
            errors = errors.toImmutableList(),
            modifier = modifier,
        )
        return
    }
    val internalData = remember(data, title) { spec.convert(data, title) }
    val drawStyle = remember(style, density) { spec.clamp(style, density) }
    content(internalData, drawStyle)
}
