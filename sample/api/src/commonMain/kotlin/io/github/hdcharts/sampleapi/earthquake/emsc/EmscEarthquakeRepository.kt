package io.github.hdcharts.sampleapi.earthquake.emsc

import io.github.hdcharts.sampleapi.ApiUnavailableException
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import io.github.hdcharts.sampleapi.earthquake.EarthquakeConnectionStatus
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedEvent
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedState
import io.github.hdcharts.sampleapi.earthquake.EarthquakeHistory
import io.github.hdcharts.sampleapi.earthquake.EarthquakeRepository
import io.github.hdcharts.sampleapi.http.networkCall
import io.github.hdcharts.sampleapi.http.reconnectDelay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class EmscEarthquakeRepository(
    private val api: EmscApi,
) : EarthquakeRepository {
    override suspend fun recent(
        limit: Int,
        since: Instant?,
        minMagnitude: Double?,
    ): List<Earthquake> =
        networkCall {
            val quakes = mutableListOf<Earthquake>()
            var read = 0
            do {
                val requested = minOf(PAGE_SIZE, limit - read)
                // Paged by events read, since events without a magnitude yet are dropped below.
                val page = api.events(limit = requested, since = since, minMagnitude = minMagnitude, offset = read)
                read += page.size
                page.mapNotNullTo(quakes) { it.properties.toEarthquake() }
            } while (page.size == requested && read < limit)
            quakes
        }

    override fun live(): Flow<EarthquakeFeedEvent> =
        channelFlow {
            var failedAttempts = 0
            while (true) {
                try {
                    networkCall {
                        api.listen(
                            onOpen = {
                                failedAttempts = 0
                                send(EarthquakeFeedEvent.Connected)
                            },
                            onMessage = { message ->
                                message.data.properties
                                    .toEarthquake()
                                    ?.let { send(EarthquakeFeedEvent.Reported(it)) }
                            },
                        )
                    }
                } catch (e: ApiUnavailableException) {
                    // A dropped connection; retry below like a server close.
                }
                val retryIn = reconnectDelay(failedAttempts++)
                send(EarthquakeFeedEvent.Disconnected(retryIn))
                delay(retryIn)
            }
        }

    override fun observe(
        history: List<EarthquakeHistory>,
        clock: Clock,
    ): Flow<EarthquakeFeedState> =
        flow {
            require(history.isNotEmpty())
            require(history.all { it.span.isPositive() && it.limit > 0 })
            emit(EarthquakeFeedState.Loading)
            val initial =
                try {
                    loadHistory(history, clock)
                } catch (e: ApiUnavailableException) {
                    emit(EarthquakeFeedState.Failed)
                    return@flow
                }
            emitAll(observeReports(initial, history, clock))
        }

    private fun observeReports(
        initial: List<Earthquake>,
        history: List<EarthquakeHistory>,
        clock: Clock,
    ): Flow<EarthquakeFeedState.Ready> =
        flow {
            val retainedSpan = history.maxOf { it.span }
            var quakes = mergeReports(emptyMap(), initial, clock.now() - retainedSpan)
            var connection = EarthquakeConnectionStatus.Connecting
            emit(EarthquakeFeedState.Ready(quakes.values.toList(), connection))

            val refreshRequests = MutableStateFlow<RefreshRequest?>(null)
            var refreshGeneration = 0
            var reportedDuringRefresh = emptyMap<String, Earthquake>()
            merge(
                live().map { HistoryUpdate.Feed(it) },
                historyRefreshes(refreshRequests, history, clock),
            ).collect { update ->
                when (update) {
                    is HistoryUpdate.Feed ->
                        when (val event = update.event) {
                            EarthquakeFeedEvent.Connected -> {
                                // Late reports and revisions can have occurrence times before the initial load.
                                connection = EarthquakeConnectionStatus.Connecting
                                reportedDuringRefresh = emptyMap()
                                refreshRequests.value = RefreshRequest(++refreshGeneration)
                            }
                            is EarthquakeFeedEvent.Disconnected -> {
                                refreshRequests.value = null
                                connection = EarthquakeConnectionStatus.Reconnecting
                            }
                            is EarthquakeFeedEvent.Reported -> {
                                quakes = mergeReports(quakes, listOf(event.earthquake), clock.now() - retainedSpan)
                                if (refreshRequests.value != null) {
                                    reportedDuringRefresh =
                                        reportedDuringRefresh + (event.earthquake.id to event.earthquake)
                                }
                            }
                        }
                    is HistoryUpdate.Refreshed -> {
                        if (update.request != refreshRequests.value) return@collect
                        refreshRequests.value = null
                        val cutoff = clock.now() - retainedSpan
                        // Reports received during HTTP loading take precedence over its snapshot.
                        val refreshed = mergeReports(emptyMap(), update.quakes, cutoff)
                        quakes = mergeReports(refreshed, reportedDuringRefresh.values, cutoff)
                        reportedDuringRefresh = emptyMap()
                        connection = EarthquakeConnectionStatus.Live
                    }
                    is HistoryUpdate.RefreshFailed -> {
                        if (update.request != refreshRequests.value) return@collect
                        connection = EarthquakeConnectionStatus.Reconnecting
                    }
                }
                emit(EarthquakeFeedState.Ready(quakes.values.toList(), connection))
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun historyRefreshes(
        requests: StateFlow<RefreshRequest?>,
        history: List<EarthquakeHistory>,
        clock: Clock,
    ): Flow<HistoryUpdate> =
        requests.flatMapLatest { request ->
            if (request == null) {
                emptyFlow()
            } else {
                flow {
                    while (true) {
                        val refreshed =
                            try {
                                loadHistory(history, clock)
                            } catch (e: ApiUnavailableException) {
                                emit(HistoryUpdate.RefreshFailed(request))
                                delay(5.seconds)
                                continue
                            }
                        emit(HistoryUpdate.Refreshed(request, refreshed))
                        break
                    }
                }
            }
        }

    private suspend fun loadHistory(
        history: List<EarthquakeHistory>,
        clock: Clock,
    ): List<Earthquake> =
        coroutineScope {
            val now = clock.now()
            history
                .map { query ->
                    async {
                        recent(limit = query.limit, since = now - query.span, minMagnitude = query.minMagnitude)
                    }
                }.awaitAll()
                .flatten()
        }

    private fun mergeReports(
        known: Map<String, Earthquake>,
        reported: Collection<Earthquake>,
        oldestKept: Instant,
    ): Map<String, Earthquake> = (known + reported.associateBy { it.id }).filterValues { it.time >= oldestKept }

    private companion object {
        const val PAGE_SIZE = 2_000
    }
}

private data class RefreshRequest(
    val generation: Int,
)

private sealed interface HistoryUpdate {
    data class Feed(
        val event: EarthquakeFeedEvent,
    ) : HistoryUpdate

    data class Refreshed(
        val request: RefreshRequest,
        val quakes: List<Earthquake>,
    ) : HistoryUpdate

    data class RefreshFailed(
        val request: RefreshRequest,
    ) : HistoryUpdate
}
