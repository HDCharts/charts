package io.github.hdcharts.sampleapi.earthquake.emsc

import io.github.hdcharts.sampleapi.earthquake.EarthquakeConnectionStatus
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedState
import io.github.hdcharts.sampleapi.earthquake.EarthquakeHistory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EmscEarthquakeRepositoryTest {
    private val now = Instant.parse("2026-10-12T12:00:00Z")
    private var clockNow = now
    private val clock =
        object : Clock {
            override fun now(): Instant = clockNow
        }
    private val history = listOf(EarthquakeHistory(span = 1.days, limit = 100))

    @Test
    fun observe_backfillsLateReportsAndRevisionsOnTheFirstConnection() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 5.0), feature("older", 4.5, now - (25 * 60).minutes)))
            api.beforeOpen = {
                api.history =
                    listOf(
                        feature("known", 4.2),
                        feature("late", 3.0),
                        feature("older", 3.5, now - (25 * 60).minutes),
                        feature("strong", 5.5, now - (26 * 60).minutes),
                    )
            }
            val states =
                collect(
                    this,
                    EmscEarthquakeRepository(api).observe(
                        history = listOf(EarthquakeHistory(30.days, 100, 4.0)) + history,
                        clock = clock,
                    ),
                )

            assertEquals(EarthquakeFeedState.Loading, states.first())
            assertEquals(mapOf("known" to 4.2, "late" to 3.0, "strong" to 5.5), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Live, ready(states).connection)
        }

    @Test
    fun observe_emitsFailureAndCanBeCollectedAgainAfterAnInitialFailure() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            api.failHistory = true
            val observed = EmscEarthquakeRepository(api).observe(history, clock)
            assertEquals(listOf(EarthquakeFeedState.Loading, EarthquakeFeedState.Failed), observed.toList())

            api.failHistory = false
            val states = collect(this, observed)
            assertEquals(mapOf("known" to 4.0), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Live, ready(states).connection)
        }

    @Test
    fun observe_replacesDuplicateAndRevisedReportsById() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))

            api.report(feature("known", 5.5))
            api.report(feature("known", 5.5))
            runCurrent()

            assertEquals(mapOf("known" to 5.5), magnitudes(states))
            assertEquals(1, ready(states).quakes.size)
        }

    @Test
    fun observe_preservesLiveReportsDuringAReconnectRefresh() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))
            val gate = CompletableDeferred<Unit>()
            api.historyGate = gate
            api.drop()
            runCurrent()
            assertEquals(EarthquakeConnectionStatus.Reconnecting, ready(states).connection)
            advanceTimeBy(2.seconds)
            runCurrent()
            assertEquals(EarthquakeConnectionStatus.Connecting, ready(states).connection)

            api.report(feature("known", 5.5))
            api.report(feature("new", 3.0))
            runCurrent()
            gate.complete(Unit)
            runCurrent()

            assertEquals(mapOf("known" to 5.5, "new" to 3.0), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Live, ready(states).connection)
        }

    @Test
    fun observe_cancelsAnOldRefreshAcrossRapidReconnects() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))
            val oldGate = CompletableDeferred<Unit>()
            api.historyGate = oldGate
            api.drop()
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()
            api.drop()
            runCurrent()
            assertEquals(1, api.cancelledRequests)

            val newGate = CompletableDeferred<Unit>()
            api.historyGate = newGate
            api.history = listOf(feature("known", 5.0))
            advanceTimeBy(2.seconds)
            runCurrent()
            oldGate.complete(Unit)
            runCurrent()
            assertEquals(EarthquakeConnectionStatus.Connecting, ready(states).connection)
            api.report(feature("known", 5.5))
            runCurrent()
            newGate.complete(Unit)
            runCurrent()

            assertEquals(mapOf("known" to 5.5), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Live, ready(states).connection)
        }

    @Test
    fun observe_retriesHttpFailuresAndCancelsRetriesWhileDisconnected() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))
            api.failHistory = true
            api.drop()
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()
            val failedRequests = api.requests.size
            advanceTimeBy(15.seconds)
            runCurrent()
            assertEquals(failedRequests + 3, api.requests.size)
            assertEquals(mapOf("known" to 4.0), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Reconnecting, ready(states).connection)

            api.openGate = CompletableDeferred()
            api.drop()
            runCurrent()
            val stoppedRequests = api.requests.size
            advanceTimeBy(20.seconds)
            runCurrent()
            assertEquals(stoppedRequests, api.requests.size)
            api.failHistory = false
            api.history = listOf(feature("recovered", 4.5))
            api.openGate?.complete(Unit)
            runCurrent()

            assertEquals(mapOf("recovered" to 4.5), magnitudes(states))
            assertEquals(EarthquakeConnectionStatus.Live, ready(states).connection)
        }

    @Test
    fun observe_cancelsInitialLoadingWithoutEmittingFailure() =
        runTest {
            val api = FakeEmscApi(emptyList())
            api.historyGate = CompletableDeferred()
            val states = mutableListOf<EarthquakeFeedState>()
            val collector =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    EmscEarthquakeRepository(api).observe(history, clock).toList(states)
                }
            runCurrent()
            collector.cancel()
            runCurrent()

            assertEquals(listOf<EarthquakeFeedState>(EarthquakeFeedState.Loading), states)
            assertEquals(1, api.cancelledRequests)
            assertEquals(0, api.activeSockets)
        }

    @Test
    fun observe_isColdAndCancelsItsSocketAndRefreshWhenCollectionStops() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val observed = EmscEarthquakeRepository(api).observe(history, clock)
            assertEquals(0, api.requests.size)
            assertEquals(0, api.activeSockets)
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { observed.collect {} }
            runCurrent()
            assertEquals(1, api.activeSockets)
            api.historyGate = CompletableDeferred()
            api.drop()
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()
            collector.cancel()
            runCurrent()

            assertEquals(0, api.activeSockets)
            assertEquals(1, api.cancelledRequests)
            val stoppedRequests = api.requests.size
            advanceTimeBy(10.seconds)
            runCurrent()
            assertEquals(stoppedRequests, api.requests.size)

            api.historyGate = null
            api.history = listOf(feature("fresh", 5.0))
            val restarted = collect(this, observed)
            assertEquals(mapOf("fresh" to 5.0), magnitudes(restarted))
            assertEquals(1, api.activeSockets)
        }

    @Test
    fun observe_keepsTheBoundaryAndPrunesExpiredReportsAndRevisions() =
        runTest {
            val api = FakeEmscApi(listOf(feature("boundary", 4.0, now - 1.days)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))
            assertEquals(mapOf("boundary" to 4.0), magnitudes(states))

            clockNow += 1.minutes
            api.report(feature("new", 5.0))
            runCurrent()
            assertEquals(mapOf("new" to 5.0), magnitudes(states))
            api.report(feature("new", 5.0, now - 2.days))
            runCurrent()
            assertEquals(emptyMap(), magnitudes(states))
        }

    @Test
    fun observe_recalculatesHistoryWindowsOnReconnect() =
        runTest {
            val api = FakeEmscApi(listOf(feature("known", 4.0)))
            val states = collect(this, EmscEarthquakeRepository(api).observe(history, clock))
            clockNow += 1.days
            api.history = listOf(feature("new", 5.0, clockNow))
            api.drop()
            runCurrent()
            advanceTimeBy(2.seconds)
            runCurrent()

            assertEquals(clockNow - 1.days, api.requests.last())
            assertEquals(mapOf("new" to 5.0), magnitudes(states))
        }

    private fun collect(
        scope: TestScope,
        observed: Flow<EarthquakeFeedState>,
    ): List<EarthquakeFeedState> {
        val states = mutableListOf<EarthquakeFeedState>()
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { observed.toList(states) }
        scope.runCurrent()
        return states
    }

    private fun ready(states: List<EarthquakeFeedState>): EarthquakeFeedState.Ready = assertIs(states.last())

    private fun magnitudes(states: List<EarthquakeFeedState>): Map<String, Double> =
        ready(states).quakes.associate { it.id to it.magnitude }

    private fun feature(
        id: String,
        magnitude: Double,
        time: Instant = now - 20.minutes,
    ) = EmscFeatureDto(EmscPropertiesDto(unid = id, time = time.toString(), lat = 50.1, lon = 19.3, mag = magnitude))
}

private class FakeEmscApi(
    var history: List<EmscFeatureDto>,
) : EmscApi {
    var failHistory = false
    var beforeOpen: suspend () -> Unit = {}
    var historyGate: CompletableDeferred<Unit>? = null
    var openGate: CompletableDeferred<Unit>? = null
    var cancelledRequests = 0
    var activeSockets = 0
    val requests = mutableListOf<Instant?>()
    private val reports = Channel<EmscFeatureDto?>(Channel.UNLIMITED)

    override suspend fun events(
        limit: Int,
        since: Instant?,
        minMagnitude: Double?,
        offset: Int,
    ): List<EmscFeatureDto> {
        requests += since
        if (failHistory) error("HTTP unavailable")
        val snapshot =
            history
                .filter {
                    (since == null || Instant.parse(it.properties.time) >= since) &&
                        (minMagnitude == null || (it.properties.mag ?: Double.NEGATIVE_INFINITY) >= minMagnitude)
                }.drop(offset)
                .take(limit)
        try {
            historyGate?.await()
        } catch (e: CancellationException) {
            cancelledRequests++
            throw e
        }
        return snapshot
    }

    override suspend fun listen(
        onOpen: suspend () -> Unit,
        onMessage: suspend (EmscLiveMessageDto) -> Unit,
    ) {
        activeSockets++
        try {
            openGate?.await()
            beforeOpen()
            onOpen()
            for (report in reports) {
                if (report == null) error("Socket disconnected")
                onMessage(EmscLiveMessageDto(action = "update", data = report))
            }
        } finally {
            activeSockets--
        }
    }

    suspend fun report(feature: EmscFeatureDto) {
        reports.send(feature)
    }

    suspend fun drop() {
        reports.send(null)
    }
}
