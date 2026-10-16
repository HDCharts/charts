package io.github.hdcharts.sampleapi.seismogram

import io.github.hdcharts.sampleapi.ApiUnavailableException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EarthScopeSeismogramRepositoryTest {
    private val station = SeismicStation("IU", "ANMO", location = "00", channel = "BHZ")
    private val since = Instant.parse("2026-10-12T12:00:00Z")
    private val motion = GroundMotion(since, 40.0, listOf(1, 2, 3))

    @Test
    fun live_resumesFromTheLastRecordAndReusesTheStationSensitivity() =
        runTest {
            var connections = 0
            var sensitivities = 0
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = {
                        sensitivities++
                        1e9
                    },
                    listen = { recorded, start, _, onRecord ->
                        connections++
                        assertEquals(station, recorded)
                        if (connections == 1) {
                            assertEquals(since, start)
                            onRecord(motion)
                            throw ApiUnavailableException(Error("Offline"))
                        }
                        assertEquals(since + 75.milliseconds, start)
                        onRecord(motion.copy(start = since + 75.milliseconds))
                        awaitCancellation()
                    },
                )

            val events = repository.live(listOf(station), since).take(3).toList()

            assertIs<SeismogramFeedEvent.Recorded>(events[0])
            assertEquals(SeismogramFeedEvent.Unavailable(station), events[1])
            assertIs<SeismogramFeedEvent.Recorded>(events[2])
            assertEquals(2, connections)
            assertEquals(1, sensitivities)
        }

    @Test
    fun live_retriesFailedMetadataWithoutClosingTheOtherStationStreams() =
        runTest {
            var attempts = 0
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { if (++attempts == 1) throw Error("Fail to fetch") else 1e9 },
                    listen = { _, _, _, onRecord ->
                        onRecord(motion)
                        onRecord(motion)
                        delay(11.seconds)
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val events = repository.live(listOf(station), since).take(2).toList()

            assertEquals(SeismogramFeedEvent.Unavailable(station), events[0])
            assertIs<SeismogramFeedEvent.Recorded>(events[1])
            assertEquals(2, attempts)
        }

    @Test
    fun live_reportsEveryStationUnavailableWhenTheBrowserCannotOpenTheSocket() =
        runTest {
            val other = station.copy(code = "MAJO")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { _, _, _, _ -> throw Error("Fail to connect") },
                )

            assertEquals(
                listOf(SeismogramFeedEvent.Unavailable(station), SeismogramFeedEvent.Unavailable(other)),
                repository.live(listOf(station, other), since).take(2).toList(),
            )
        }

    @Test
    fun live_reportsARejectedStationAndStreamsTheOthers() =
        runTest {
            val rejected = station.copy(code = "LCO", location = "10")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { recorded, _, onRejected, onRecord ->
                        if (recorded == rejected) {
                            onRejected()
                            awaitCancellation()
                        }
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val events = repository.live(listOf(station, rejected), since).take(2).toList()

            assertEquals(SeismogramFeedEvent.Unavailable(rejected), events[0])
            assertIs<SeismogramFeedEvent.Recorded>(events[1])
        }

    @Test
    fun live_streamsAStationWithoutWaitingForAnotherStationsConnection() =
        runTest {
            val slow = station.copy(code = "MAJO")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { recorded, _, _, onRecord ->
                        if (recorded == slow) delay(15.seconds)
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val received = mutableListOf<Pair<Long, SeismicStation>>()
            repository.live(listOf(station, slow), since).take(2).collect {
                received += testScheduler.currentTime to it.station
            }

            assertEquals(listOf(0L to station, 15_000L to slow), received)
        }

    @Test
    fun live_keepsStreamingAStationWhileAnotherStationsConnectionDrops() =
        runTest {
            val dropping = station.copy(code = "MAJO")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { recorded, _, _, onRecord ->
                        if (recorded == dropping) throw Error("Socket closed")
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val events = repository.live(listOf(station, dropping), since).take(3).toList()

            assertEquals(listOf(station), events.filterIsInstance<SeismogramFeedEvent.Recorded>().map { it.station })
            assertEquals(
                listOf(dropping, dropping),
                events.filterIsInstance<SeismogramFeedEvent.Unavailable>().map { it.station },
            )
        }

    @Test
    fun live_loadsEveryStationSensitivityAtOnce() =
        runTest {
            val other = station.copy(code = "MAJO")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = {
                        delay(15.seconds)
                        1e9
                    },
                    listen = { _, _, _, onRecord ->
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            repository.live(listOf(station, other), since).take(2).toList()

            assertEquals(15_000L, testScheduler.currentTime)
        }

    @Test
    fun live_streamsEachStationWithoutWaitingForTheOthersSensitivity() =
        runTest {
            val slow = station.copy(code = "MAJO")
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = {
                        if (it == slow) delay(15.seconds)
                        1e9
                    },
                    listen = { _, _, _, onRecord ->
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val received = mutableListOf<Pair<Long, SeismicStation>>()
            repository.live(listOf(station, slow), since).take(2).collect {
                assertIs<SeismogramFeedEvent.Recorded>(it)
                received += testScheduler.currentTime to it.station
            }

            // The slow station's record is held until its sensitivity arrives, not dropped.
            assertEquals(listOf(0L to station, 15_000L to slow), received)
        }

    @Test
    fun live_reconnectsAfterTheSocketCancelsItsOwnChannels() =
        runTest {
            var connections = 0
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { _, _, _, onRecord ->
                        connections++
                        // Ktor's browser socket cancels its outgoing channel when the connection drops.
                        if (connections == 1) throw CancellationException("Socket closed")
                        onRecord(motion)
                        awaitCancellation()
                    },
                )

            val events = repository.live(listOf(station), since).take(2).toList()

            assertEquals(SeismogramFeedEvent.Unavailable(station), events[0])
            assertIs<SeismogramFeedEvent.Recorded>(events[1])
        }

    @Test
    fun live_backsOffBetweenFailedConnections() =
        runTest {
            val connectedAt = mutableListOf<Long>()
            val repository =
                EarthScopeSeismogramRepository(
                    sensitivity = { 1e9 },
                    listen = { _, _, _, _ ->
                        connectedAt += testScheduler.currentTime
                        throw Error("Fail to connect")
                    },
                )

            repository.live(listOf(station), since).take(3).toList()

            assertEquals(listOf(0L, 2_000L, 6_000L), connectedAt)
        }
}
