package io.github.hdcharts.core.internal

import androidx.compose.ui.unit.Density
import io.github.hdcharts.core.model.ChartData

/**
 * Everything a chart declares about its input, in one place: which checks apply, how its style
 * answers them, how the style is clamped for drawing, and how public data becomes its render model.
 *
 * [ChartEntry] runs the four in a fixed order and knows nothing about what a chart looks like. One
 * object per chart, so the values that have to change together cannot drift apart: a chart that
 * grows a style field the color check reads updates one function, and a chart with two public
 * composables shares one declaration.
 *
 * @param S The chart's style type.
 * @param M The chart's internal render model.
 */
@InternalChartsApi
interface ChartSpec<S : Any, M : Any> {
    /** Which input checks apply to this chart. */
    val policy: ChartPolicy

    /**
     * The values [policy] reads out of [style]. Every field of [ChartValidationInputs] is stated
     * here; none has a default, so a skipped check and a forgotten mapping do not look alike.
     */
    fun validationInputs(style: S): ChartValidationInputs

    /**
     * The style at [density], with every drawable value inside its range.
     *
     * The rules live with the style, as a `clamp(density)` extension next to the style class, so
     * this delegates rather than writing them a second time. A chart that draws more than one style
     * — histogram draws bar's — maps between them here.
     */
    fun clamp(
        style: S,
        density: Density,
    ): S

    /**
     * The validated [data] as this chart's render model. Runs once per data or title change, and
     * only after validation passes, so it may assume the data holds what [policy] checked for.
     *
     * Most charts call the shared `toRenderModel(data, title, labels)` in `charts-core` and declare
     * only where their point labels come from. Two build their own: bar and histogram share
     * `toBarRenderData`, which has no categories, and stacked bar transposes a per-bar model.
     */
    fun convert(
        data: ChartData,
        title: String?,
    ): M
}
