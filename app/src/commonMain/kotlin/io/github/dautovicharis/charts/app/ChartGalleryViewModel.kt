package io.github.dautovicharis.charts.app

import androidx.lifecycle.ViewModel
import io.github.dautovicharis.charts.app.data.ChartPreviewUseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ChartGalleryItemUiState(
    val destination: ChartDestination,
    val subtitle: String,
)

data class ChartGalleryPreviewState(
    val pieValues: List<Float>,
    val lineValues: List<Float>,
    val multiLineSeries: List<Pair<String, List<Float>>>,
    val stackedAreaSeries: List<Pair<String, List<Float>>>,
    val barValues: List<Float>,
    val histogramValues: List<Float>,
    val stackedSeries: List<Pair<String, List<Float>>>,
    val radarSeries: List<Pair<String, List<Float>>>,
)

data class ChartGalleryState(
    val items: List<ChartGalleryItemUiState>,
    val previews: ChartGalleryPreviewState,
)

class ChartGalleryViewModel(
    private val previewUseCase: ChartPreviewUseCase,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            ChartGalleryState(
                items = emptyList(),
                previews = previewUseCase.previewSeed(),
            ),
        )

    val state: StateFlow<ChartGalleryState> = _state.asStateFlow()
    private var isLoopRunning = false

    fun buildItems(menuItems: List<ChartDestination>): List<ChartGalleryItemUiState> =
        menuItems.map { screen ->
            ChartGalleryItemUiState(
                destination = screen,
                subtitle = subtitleFor(screen),
            )
        }

    fun setMenuItems(menuItems: List<ChartDestination>) {
        _state.update { state ->
            state.copy(items = buildItems(menuItems))
        }
    }

    suspend fun runPreviewLoop() {
        if (isLoopRunning) return
        isLoopRunning = true
        try {
            coroutineScope {
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS,
                        jitterMs = 350L,
                        update = { previews ->
                            previews.copy(pieValues = previewUseCase.nextPiePreview(previews.pieValues))
                        },
                    )
                }
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS + 150L,
                        jitterMs = 400L,
                        update = { previews ->
                            previews.copy(barValues = previewUseCase.nextBarPreview(previews.barValues))
                        },
                    )
                }
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS + 225L,
                        jitterMs = 400L,
                        update = { previews ->
                            previews.copy(
                                histogramValues = previewUseCase.nextHistogramPreview(previews.histogramValues),
                            )
                        },
                    )
                }
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS + 300L,
                        jitterMs = 450L,
                        update = { previews ->
                            previews.copy(stackedAreaSeries = previewUseCase.nextStackedAreaPreview())
                        },
                    )
                }
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS + 500L,
                        jitterMs = 500L,
                        update = { previews ->
                            previews.copy(stackedSeries = previewUseCase.nextStackedPreview())
                        },
                    )
                }
                launch {
                    previewLoop(
                        baseIntervalMs = LIVE_PREVIEW_INTERVAL_MS + 900L,
                        jitterMs = 600L,
                        update = { previews ->
                            previews.copy(radarSeries = previewUseCase.nextRadarPreview())
                        },
                    )
                }
            }
        } finally {
            isLoopRunning = false
        }
    }

    private suspend fun previewLoop(
        baseIntervalMs: Long,
        jitterMs: Long,
        update: (ChartGalleryPreviewState) -> ChartGalleryPreviewState,
    ) {
        while (true) {
            delay(randomizedInterval(baseIntervalMs, jitterMs))
            _state.update { state ->
                state.copy(previews = update(state.previews))
            }
        }
    }

    private fun subtitleFor(item: ChartDestination): String =
        when (item) {
            is ChartDestination.PieChartScreen -> "Composition at a glance."
            is ChartDestination.LineChartScreen -> "Trends over time."
            is ChartDestination.MultiLineChartScreen -> "Compare multiple series."
            is ChartDestination.StackedAreaChartScreen -> "Cumulative layers by category."
            is ChartDestination.BarChartScreen -> "Rank values quickly."
            is ChartDestination.HistogramChartScreen -> "Distribution across bins."
            is ChartDestination.StackedBarChartScreen -> "Segment composition by category."
            is ChartDestination.RadarChartScreen -> "Live radial signals."
        }

    private companion object {
        private const val LIVE_PREVIEW_INTERVAL_MS = 2000L
        private const val MIN_PREVIEW_INTERVAL_MS = 700L
    }

    private fun randomizedInterval(
        baseIntervalMs: Long,
        jitterMs: Long,
    ): Long {
        if (jitterMs <= 0) return baseIntervalMs.coerceAtLeast(MIN_PREVIEW_INTERVAL_MS)
        val delta = Random.nextLong(-jitterMs, jitterMs + 1)
        return (baseIntervalMs + delta).coerceAtLeast(MIN_PREVIEW_INTERVAL_MS)
    }
}
