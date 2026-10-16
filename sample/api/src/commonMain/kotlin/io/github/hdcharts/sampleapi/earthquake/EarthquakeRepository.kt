package io.github.hdcharts.sampleapi.earthquake

import io.github.hdcharts.sampleapi.ApiUnavailableException
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

interface EarthquakeRepository {
    /**
     * Loads [history], merges live reports by id, and refreshes history on every connection.
     * Each collector owns its requests and socket; cancellation stops both.
     * Collect again after [EarthquakeFeedState.Failed] to retry the initial load.
     */
    fun observe(
        history: List<EarthquakeHistory>,
        clock: Clock = Clock.System,
    ): Flow<EarthquakeFeedState>

    /**
     * Reported earthquakes, newest first.
     *
     * @param limit The most events to read, across as many requests as that takes.
     * @param since Include earthquakes occurring at or after this time.
     * @throws ApiUnavailableException when the request fails; the caller decides how to show it.
     */
    suspend fun recent(
        limit: Int,
        since: Instant? = null,
        minMagnitude: Double? = null,
    ): List<Earthquake>

    /** Earthquakes as they are reported. Never completes; reconnects with backoff when the connection drops. */
    fun live(): Flow<EarthquakeFeedEvent>
}

/** One part of the history to retain; overlapping queries are combined by earthquake id. */
data class EarthquakeHistory(
    val span: Duration,
    val limit: Int,
    val minMagnitude: Double? = null,
)

/** Repository data and synchronization status, independent of any chart or screen. */
sealed interface EarthquakeFeedState {
    data object Loading : EarthquakeFeedState

    data object Failed : EarthquakeFeedState

    data class Ready(
        val quakes: List<Earthquake>,
        val connection: EarthquakeConnectionStatus,
    ) : EarthquakeFeedState
}

enum class EarthquakeConnectionStatus {
    Connecting,
    Live,
    Reconnecting,
}
