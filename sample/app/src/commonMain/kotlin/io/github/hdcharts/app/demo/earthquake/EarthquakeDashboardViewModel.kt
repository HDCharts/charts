package io.github.hdcharts.app.demo.earthquake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import io.github.hdcharts.sampleapi.earthquake.EarthquakeConnectionStatus
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedState
import io.github.hdcharts.sampleapi.earthquake.EarthquakeHistory
import io.github.hdcharts.sampleapi.earthquake.EarthquakeRepository
import io.github.hdcharts.sampleapi.seismogram.SeismogramRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** What the view model tracks; [toUi] turns it into what the screen shows. */
internal sealed interface DashboardState {
    data object Loading : DashboardState

    data object Failed : DashboardState

    data class Ready(
        val connection: ConnectionStatus,
        val latest: Earthquake?,
        val dailyCounts: DailyCounts,
        val lastDay: Map<Continent, ContinentHistory>,
        val magnitudeBands: List<Int>,
        val magnitudeBandsUpdatedAt: Instant,
        val arrivals: Map<Continent, WaveArrival>,
    ) : DashboardState
}

/** @param updatedAt When the magnitudes last changed, so each chart can show its own freshness. */
internal data class ContinentHistory(
    val quakes: List<Earthquake>,
    val updatedAt: Instant,
)

/**
 * Loads the last [HISTORY_DAYS] of quakes and refreshes them after connecting. Separately plays
 * back live seismographs. Both reach the screen as UI models built by `toUi()`.
 *
 * @param ticks Moves seismograph playback on by a slot; tests pass their own.
 */
class EarthquakeDashboardViewModel(
    private val repository: EarthquakeRepository,
    seismograms: SeismogramRepository,
    private val clock: Clock = Clock.System,
    ticks: Flow<Unit> = slotTicks(clock = clock, slotLength = SeismographSlot),
) : ViewModel() {
    private val loadAttempts = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dashboard: StateFlow<DashboardState> =
        loadAttempts
            .flatMapLatest { dashboardUpdates() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS),
                initialValue = DashboardState.Loading,
            )

    val uiState: StateFlow<EarthquakeDashboardUiState> =
        dashboard
            .map { it.toUi() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS),
                initialValue = EarthquakeDashboardUiState.Loading,
            )

    // Arrival lines count down against the playhead, so they are mapped with each playback step.
    val seismographs: StateFlow<SeismographsUiState> =
        combine(
            seismographFeed(repository = seismograms, clock = clock, ticks = ticks),
            dashboard.map { (it as? DashboardState.Ready)?.arrivals.orEmpty() }.distinctUntilChanged(),
        ) { playback, arrivals -> playback.toUi(arrivals = arrivals) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS),
                initialValue =
                    SeismographPlayback(playhead = clock.now() - PlaybackDelay, stations = emptyMap())
                        .toUi(arrivals = emptyMap()),
            )

    fun retry() {
        loadAttempts.update { it + 1 }
    }

    private fun dashboardUpdates(): Flow<DashboardState> =
        combine(
            repository.observe(
                history =
                    listOf(
                        EarthquakeHistory(
                            span = HISTORY_DAYS.days,
                            limit = DAILY_COUNTS_LIMIT,
                            minMagnitude = DAILY_COUNTS_MIN_MAGNITUDE,
                        ),
                        EarthquakeHistory(span = CONTINENT_SPAN, limit = LAST_DAY_MAX),
                    ),
                clock = clock,
            ),
            slotTicks(clock = clock, slotLength = 1.minutes).onStart { emit(Unit) },
        ) { feed, _ -> feed }
            .runningFold<EarthquakeFeedState, DashboardState>(DashboardState.Loading) { previous, feed ->
                when (feed) {
                    EarthquakeFeedState.Loading -> DashboardState.Loading
                    EarthquakeFeedState.Failed -> DashboardState.Failed
                    is EarthquakeFeedState.Ready ->
                        readyState(
                            quakes = feed.quakes,
                            connection =
                                when (feed.connection) {
                                    EarthquakeConnectionStatus.Connecting -> ConnectionStatus.Connecting
                                    EarthquakeConnectionStatus.Live -> ConnectionStatus.Live
                                    EarthquakeConnectionStatus.Reconnecting -> ConnectionStatus.Reconnecting
                                },
                            previous = previous as? DashboardState.Ready,
                        )
                }
            }

    private fun readyState(
        quakes: List<Earthquake>,
        connection: ConnectionStatus,
        previous: DashboardState.Ready?,
    ): DashboardState.Ready {
        val now = clock.now()
        val cutoff = now - HISTORY_DAYS.days
        val retained = quakes.filter { it.time >= cutoff }
        val dailyCounts =
            dailyCountsByContinent(
                quakes = retained,
                now = now,
                days = HISTORY_DAYS,
                minMagnitude = DAILY_COUNTS_MIN_MAGNITUDE,
            )
        val lastDayQuakes = recentByContinent(quakes = retained, since = now - CONTINENT_SPAN)
        val magnitudeBands = quakesByMagnitude(lastDayQuakes.values.flatten())
        return DashboardState.Ready(
            connection = connection,
            latest = retained.maxByOrNull { it.time },
            dailyCounts = dailyCounts,
            // A chart keeps its earlier update time when its values did not change, e.g. after a quake elsewhere.
            lastDay =
                lastDayQuakes.mapValues { (continent, recent) ->
                    val before = previous?.lastDay?.get(continent)
                    val unchanged = before?.quakes?.map { it.magnitude } == recent.map { it.magnitude }
                    ContinentHistory(quakes = recent, updatedAt = if (unchanged) before.updatedAt else now)
                },
            magnitudeBands = magnitudeBands,
            magnitudeBandsUpdatedAt =
                previous?.takeIf { it.magnitudeBands == magnitudeBands }?.magnitudeBandsUpdatedAt ?: now,
            arrivals =
                buildMap {
                    Seismographs.forEach { (continent, seismograph) ->
                        latestArrival(
                            quakes = retained,
                            latitude = seismograph.latitude,
                            longitude = seismograph.longitude,
                            since = now - ARRIVAL_LOOKBACK,
                        )?.let { put(continent, it) }
                    }
                },
        )
    }

    companion object {
        private const val HISTORY_DAYS = 30
        private const val DAILY_COUNTS_MIN_MAGNITUDE = 4.0
        private val CONTINENT_SPAN = 24.hours

        // Waves cross the whole planet in about 20 minutes, so older quakes have long been recorded.
        private val ARRIVAL_LOOKBACK = 1.hours
        private const val DAILY_COUNTS_LIMIT = 5_000

        // About 400 quakes a day are reported worldwide; the cap stops paging a server that ignores the offset.
        private const val LAST_DAY_MAX = 20_000
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
