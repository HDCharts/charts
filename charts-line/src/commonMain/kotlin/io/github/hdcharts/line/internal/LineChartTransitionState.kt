package io.github.hdcharts.line.internal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.hdcharts.core.internal.ANIMATION_TARGET
import io.github.hdcharts.core.internal.animation.ChartMorphState

/**
 * Everything a data update moves: the values a morph blends, the live window shift, and the window
 * the chart drew before it.
 *
 * A frame asks this state what to draw through [morph], [activeShift] and [shiftProgress], and the
 * update effect calls [update] to move it. Keeping both sides together is what lets a frame tell a
 * settled chart from a shifting one without the composable tracking each piece on its own.
 */
internal class LineChartTransitionState(
    initialMorphValues: List<List<Float>>,
) {
    /** The values an update that does not slide blends between. */
    val morph = ChartMorphState(initialMorphValues)

    private val shiftData = mutableStateOf<LineChartTimelineShiftData?>(null)
    private val shiftProgressValue = Animatable(ANIMATION_TARGET)
    private var previousRawSeries: List<List<Double>>? = null
    private var hasInitialized = false

    /** Points the live window has dropped for the window the line currently draws. */
    var drawnTimelinePoints by mutableLongStateOf(0L)
        private set

    /** How far the live shift has run: 0 at its start, [ANIMATION_TARGET] once it has settled. */
    val shiftProgress: Float get() = shiftProgressValue.value.coerceIn(0f, ANIMATION_TARGET)

    /** Whether a live shift is under way. The X-axis labels read this to stay on their points. */
    val isShifting: Boolean get() = shiftData.value != null

    /**
     * The live shift running right now, or null once it has settled.
     *
     * A settled shift holds its progress at [ANIMATION_TARGET], so the window it was built from is
     * no longer drawn: only a shift part-way through changes what the line shows.
     */
    val activeShift: LineChartTimelineShiftData?
        get() = shiftData.value?.takeIf { shiftProgress < ANIMATION_TARGET }

    /**
     * Moves the chart onto [currentRawSeries], drawn against [targetNormalized].
     *
     * The first update, a preview, and any update that changes the number of series or points draw
     * the new data straight away, because there is no earlier state to move from. Every other update
     * either slides the previous window or blends the two value sets, whichever
     * [decideLineChartUpdate] asks for.
     */
    suspend fun update(
        currentRawSeries: List<List<Double>>,
        currentMinMax: Pair<Double, Double>,
        targetNormalized: List<List<Float>>,
        renderMode: LineChartRenderMode,
        animationSpec: TweenSpec<Float>,
        droppedTimelinePoints: Long,
        isPreview: Boolean,
    ) {
        val previous = previousRawSeries
        previousRawSeries = currentRawSeries
        drawnTimelinePoints = droppedTimelinePoints

        val hasStructureChanged =
            previous != null &&
                !hasSameSeriesStructure(
                    previous = previous,
                    current = currentRawSeries,
                )
        if (hasStructureChanged || isPreview || !hasInitialized) {
            settle()
            morph.snapTo(targetNormalized)
            hasInitialized = true
            return
        }

        val transitionMode =
            decideLineChartUpdate(
                previousRawSeries = previous,
                currentRawSeries = currentRawSeries,
                currentMinMax = currentMinMax,
                renderMode = renderMode,
            )

        settle()
        when (transitionMode) {
            // The shift slides the previous window onto the current one, so the line draws the
            // transition values until it finishes rather than morphing between the two windows.
            is LineChartTransitionMode.Timeline -> {
                morph.snapTo(targetNormalized)
                morph.progress.snapTo(ANIMATION_TARGET)
                shiftData.value = transitionMode.shiftData

                // Each shift always covers one full step. Progress left over from a shift this
                // window interrupted belongs to the window before it, so reusing it would move the
                // line a step it has not travelled yet.
                shiftProgressValue.snapTo(0f)
                shiftProgressValue.animateTo(
                    targetValue = ANIMATION_TARGET,
                    animationSpec = animationSpec,
                )
                shiftData.value = null
            }

            LineChartTransitionMode.Morph ->
                morph.animateTo(
                    targets = targetNormalized,
                    animationSpec = animationSpec,
                )
        }
    }

    /**
     * Returns the chart to its pre-display state, so the entry reveal runs again from the data when
     * the chart comes back.
     */
    suspend fun resetForHidden(
        seriesCount: Int,
        pointsCount: Int,
    ) {
        settle()
        morph.snapTo(List(seriesCount) { List(pointsCount) { 0f } })
        morph.progress.snapTo(0f)
        hasInitialized = false
        previousRawSeries = null
    }

    /** Ends any live shift and returns the morph to the values it is drawing. */
    private suspend fun settle() {
        shiftData.value = null
        shiftProgressValue.snapTo(ANIMATION_TARGET)
    }
}
