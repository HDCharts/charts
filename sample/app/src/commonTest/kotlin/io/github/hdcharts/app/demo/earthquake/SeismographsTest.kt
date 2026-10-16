package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.seismogram.GroundMotion
import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.SeismogramFeedEvent
import io.github.hdcharts.sampleapi.seismogram.SeismogramRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val Noon = Instant.parse("2026-10-12T12:00:00Z")

// Slots from the start of the fetched history to 10 s past Noon, so recordings reach the playhead.
private val RecordedSlots = ((PlaybackDelay + PlaybackWindow + BaselineSpan) / SeismographSlot).toInt() + 50

@OptIn(ExperimentalCoroutinesApi::class)
class SeismographsTest {
    @Test
    fun withMotion_groupsSamplesIntoSlotsAndDropsOldSlots() {
        // Four samples per slot, whatever the slot length.
        val samplesPerSecond = 4 * 1000.0 / SeismographSlot.inWholeMilliseconds
        val motion = GroundMotion(start = Noon, samplesPerSecond = samplesPerSecond, samples = listOf(1, 2, 3, 4, 5))
        val old = (Noon - 1.minutes).slotIndex()

        val slots = mapOf(old to Slot.of(9)).withMotion(motion, oldestKept = Noon.slotIndex())

        assertEquals(setOf(Noon.slotIndex(), Noon.slotIndex() + 1), slots.keys)
        assertEquals(Slot(sum = 10, count = 4, min = 1, max = 4), slots.getValue(Noon.slotIndex()))
    }

    @Test
    fun withMotion_leavesSlotsBeforeWritableFromAsTheyWere() {
        // One sample per slot, whatever the slot length.
        val motion =
            GroundMotion(
                start = Noon,
                samplesPerSecond = 1000.0 / SeismographSlot.inWholeMilliseconds,
                samples = listOf(1, 2, 3),
            )
        val shown = mapOf(Noon.slotIndex() to Slot.of(7))

        val slots = shown.withMotion(motion, oldestKept = Long.MIN_VALUE, writableFrom = Noon.slotIndex() + 1)

        assertEquals(Slot.of(7), slots.getValue(Noon.slotIndex()))
        assertEquals(Slot.of(2), slots.getValue(Noon.slotIndex() + 1))
    }

    @Test
    fun nextPlayheadSlot_stepsOneSlotPerTick() {
        // On time, early, and late ticks all move playback by at most one slot.
        assertEquals(11L, nextPlayheadSlot(current = 10, target = 11))
        assertEquals(10L, nextPlayheadSlot(current = 10, target = 10))
        assertEquals(11L, nextPlayheadSlot(current = 10, target = 13))
        // Far behind, as after a hidden tab, it jumps to catch up.
        assertEquals(100L, nextPlayheadSlot(current = 10, target = 100))
    }

    @Test
    fun slot_drawsTheSampleFurthestFromItsMean() {
        assertEquals(-40, (Slot.of(0) + 1 + 2 + -40).strongest)
        assertEquals(30, (Slot.of(0) + 1 + 30).strongest)
    }

    @Test
    fun playbackWindow_holdsTheLastValueThroughAGap() {
        val start = Noon.slotIndex()
        val slots = mapOf(start + 1 to Slot.of(4), start + 3 to Slot.of(9))

        // 1e9 counts per m/s makes one count one nm/s, so values read as counts.
        val window = slots.playbackWindow(endSlot = start + 4, slots = 5, countsPerMeterPerSecond = 1e9)

        // Before the first sample the line starts at it; after a sample it holds until the next one.
        assertEquals(listOf(4.0, 4.0, 4.0, 9.0, 9.0), window)
    }

    @Test
    fun playbackWindow_turnsCountsIntoNanometersPerSecond() {
        val window =
            mapOf(Noon.slotIndex() to Slot.of(3_950))
                .playbackWindow(endSlot = Noon.slotIndex(), slots = 1, countsPerMeterPerSecond = 1.975e9)

        assertEquals(
            2000.0,
            window
                .single(),
            absoluteTolerance = 1.0,
        )
    }

    @Test
    fun playbackWindow_isEmptyWithoutAnyDataInTheWindow() {
        assertEquals(
            emptyList(),
            emptyMap<Long, Slot>().playbackWindow(endSlot = Noon.slotIndex(), slots = 5, countsPerMeterPerSecond = 1e9),
        )
    }

    @Test
    fun steadyRange_startsWideEnoughForNormalShakingAndCentresOnZero() {
        // Normal shaking of about 100 nm/s either way: 4 times its spread beats 1.2 times its peak.
        val range = steadyRange(current = null, values = List(20) { if (it % 2 == 0) 100.0 else -100.0 })!!

        assertEquals(-400.0, range.start, absoluteTolerance = 1.0)
        assertEquals(400.0, range.endInclusive, absoluteTolerance = 1.0)
    }

    @Test
    fun steadyRange_growsForAStrongerSwingAndShrinksBackOnceItHasPassed() {
        val calm = steadyRange(current = null, values = List(20) { if (it % 2 == 0) 100.0 else -100.0 })!!

        assertEquals(calm, steadyRange(current = calm, values = listOf(-300.0, 250.0)))
        val quake = steadyRange(current = calm, values = listOf(-100.0, 900.0))!!
        assertEquals(1080.0, quake.endInclusive, absoluteTolerance = 1.0)
        assertEquals(-1080.0, quake.start, absoluteTolerance = 1.0)
        // A slightly quieter window keeps the scale, so normal shaking never rescales the chart.
        assertEquals(quake, steadyRange(current = quake, values = listOf(-600.0, 600.0)))
        // Once the swing has left the window, the scale goes back to fitting normal shaking.
        assertEquals(calm, steadyRange(current = quake, values = listOf(-100.0, 100.0)))
    }

    @Test
    fun playbackWindow_centresAConstantOffsetOnZero() {
        val start = Noon.slotIndex()
        val offset = (start - 20..start + 4).associateWith { Slot.of(5_000) }

        val window =
            offset.playbackWindow(
                endSlot = start + 4,
                slots = 5,
                countsPerMeterPerSecond = 1e9,
                baselineSlots = 10,
            )

        assertEquals(List(5) { 0.0 }, window)
    }

    @Test
    fun playbackWindow_keepsEachCentredPointAsTheWindowMoves() {
        // Each point's baseline uses only earlier slots, so a point keeps its value and the chart slides.
        val start = Noon.slotIndex()
        val wandering = (start - 30..start + 30).associateWith { Slot.of(((it - start) * 37 % 23).toInt()) }

        val before =
            wandering.playbackWindow(
                endSlot = start + 10,
                slots = 8,
                countsPerMeterPerSecond = 1e9,
                baselineSlots = 10,
            )
        val after =
            wandering.playbackWindow(
                endSlot = start + 11,
                slots = 8,
                countsPerMeterPerSecond = 1e9,
                baselineSlots = 10,
            )

        assertEquals(
            before
                .drop(1),
            after
                .dropLast(1),
        )
    }

    @Test
    fun shakingLevel_comparesTheNewestChunkWithTheTypicalOne() {
        val steady = List(40) { if (it % 2 == 0) 10.0 else -10.0 }
        val burst = steady.dropLast(10) + List(10) { if (it % 2 == 0) 80.0 else -80.0 }
        val calm = steady.dropLast(10) + List(10) { if (it % 2 == 0) 2.0 else -2.0 }

        assertEquals(ShakingLevel.Normal, shakingLevel(steady, chunk = 10))
        assertEquals(ShakingLevel.Strong, shakingLevel(burst, chunk = 10))
        assertEquals(ShakingLevel.Quiet, shakingLevel(calm, chunk = 10))
    }

    @Test
    fun shakingLevel_isUnknownWithoutEnoughMotion() {
        assertEquals(null, shakingLevel(List(40) { 5.0 }, chunk = 10))
        assertEquals(null, shakingLevel(List(15) { it.toDouble() }, chunk = 10))
    }

    @Test
    fun playbackWindow_keepsFractionalBaselinesExactlyAsTheWindowSlides() {
        val slots =
            (0L..1000L).associateWith { index ->
                val count = if (index % 43 == 0L) 7 else 8
                val min = (index * 37 % 101).toInt() - 50
                Slot(sum = min.toLong() * count + 1, count = count, min = min, max = min + 1)
            }
        val before = slots.playbackWindow(600, 300, countsPerMeterPerSecond = 1.97536e9, baselineSlots = 150)
        val after = slots.playbackWindow(601, 300, countsPerMeterPerSecond = 1.97536e9, baselineSlots = 150)

        assertEquals(
            before
                .drop(1),
            after
                .dropLast(1),
        )
    }

    @Test
    fun withMotion_discardsReplayedSamplesAndCompletesAPartialSlot() {
        val first = GroundMotion(Noon, 40.0, listOf(10, 20, 30, 40, 50))
        val replay = GroundMotion(Noon, 40.0, listOf(10, 20, 30, 40, 50, 60, 70, 80))
        val before = emptyMap<Long, Slot>().withMotion(first, oldestKept = Long.MIN_VALUE)
        val after =
            before.withMotion(
                replay,
                oldestKept = Long.MIN_VALUE,
                receivedThrough =
                    Noon + 125.milliseconds,
            )

        assertEquals(Slot(sum = 360, count = 8, min = 10, max = 80), after.getValue(Noon.slotIndex()))
    }

    @Test
    fun seismographFeed_slidesOnePointPerTickWithFractionalSlotMeans() =
        runTest {
            var now = Noon
            val clock =
                object : Clock {
                    override fun now(): Instant = now
                }
            val repository = StreamedSeismograms()
            val ticks = MutableSharedFlow<Unit>()
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                seismographFeed(repository, clock, ticks).collect { emitted += it }
            }
            val start = Noon - PlaybackDelay - PlaybackWindow - BaselineSpan
            // Seven samples in each slot leave a fractional mean; the next slot starts after a short gap.
            repeat(RecordedSlots) { index ->
                val sample = index * 37 % 101
                repository.events.emit(
                    recorded(GroundMotion(start + SeismographSlot * index, 40.0, List(7) { sample + it })),
                )
            }
            val initial =
                emitted
                    .last()
                    .asia()
                    .values
            now += SeismographSlot * 3
            ticks.emit(Unit)
            runCurrent()
            val next =
                emitted
                    .last()
                    .asia()
                    .values

            assertEquals(initial.drop(1), next.dropLast(1))
        }

    @Test
    fun seismographFeed_buildsTheWindowsOnceForABurstOfRecords() =
        runTest {
            val clock =
                object : Clock {
                    override fun now(): Instant = Noon
                }
            // Records replayed after connecting arrive together; building the windows once per record froze the page.
            val backlog = Channel<SeismogramFeedEvent>(Channel.UNLIMITED)
            val start = Noon - PlaybackDelay - PlaybackWindow - BaselineSpan
            repeat(10) { index ->
                backlog.trySend(recorded(GroundMotion(start + SeismographSlot * index, 40.0, List(8) { index })))
            }
            val repository =
                object : SeismogramRepository {
                    override fun live(
                        stations: List<SeismicStation>,
                        since: Instant,
                    ): Flow<SeismogramFeedEvent> = backlog.receiveAsFlow()
                }
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch { seismographFeed(repository, clock, emptyFlow()).collect { emitted += it } }
            runCurrent()

            // The state before listening, then one state for the whole burst, with every record in it.
            assertEquals(2, emitted.size)
            assertTrue(emitted.last().asia().lastRecordedAt!! >= start + SeismographSlot * 9)
        }

    @Test
    fun seismographFeed_keepsTheWaveformAndRecordedTimeWhenUnavailable() =
        runTest {
            var now = Noon
            val clock =
                object : Clock {
                    override fun now(): Instant = now
                }
            val repository = StreamedSeismograms()
            val ticks = MutableSharedFlow<Unit>()
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                seismographFeed(repository, clock, ticks).collect { emitted += it }
            }
            val motion = recording()
            repository.events.emit(recorded(motion))
            val before = emitted.last().asia()
            repository.events.emit(SeismogramFeedEvent.Unavailable(Seismographs.getValue(Continent.Asia).station))
            now += 30.seconds
            ticks.emit(Unit)
            val failed = emitted.last().asia()

            assertEquals(StationAvailability.Unavailable, failed.availability)
            assertEquals(before.values, failed.values)
            assertEquals(before.lastRecordedAt, failed.lastRecordedAt)
            assertTrue(failed.values.isNotEmpty())

            repository.events.emit(recorded(recording(start = motion.start + 30.seconds)))
            assertEquals(StationAvailability.Available, emitted.last().asia().availability)
            assertTrue(emitted.last().asia().lastRecordedAt!! > failed.lastRecordedAt!!)
        }

    @Test
    fun seismographFeed_judgesShakingOnlyFromRecordedMotionAfterAGap() =
        runTest {
            val clock =
                object : Clock {
                    override fun now(): Instant = Noon
                }
            val repository = StreamedSeismograms()
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                seismographFeed(repository, clock, emptyFlow()).collect { emitted += it }
            }
            // Back after an outage: only the newest 15 s of the window were recorded, all of it steady.
            val playhead = Noon - PlaybackDelay
            repository.events.emit(
                SeismogramFeedEvent.Recorded(
                    Seismographs.getValue(Continent.Asia).station,
                    GroundMotion(
                        start = playhead - 15.seconds,
                        samplesPerSecond = 40.0,
                        samples = List(640) { (it * 37) % 101 },
                    ),
                    countsPerMeterPerSecond = 1.7e9,
                ),
            )

            assertEquals(StationAvailability.Available, emitted.last().asia().availability)
            // The held stretch is not quiet ground, so too little motion remains to judge.
            assertEquals(null, emitted.last().asia().level)
        }

    @Test
    fun seismographFeed_marksAStationUnavailableWhenItsStreamFallsBehindPlayback() =
        runTest {
            var now = Noon
            val clock =
                object : Clock {
                    override fun now(): Instant = now
                }
            val repository = StreamedSeismograms()
            val ticks = MutableSharedFlow<Unit>()
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                seismographFeed(repository, clock, ticks).collect { emitted += it }
            }
            repository.events.emit(recorded(recording()))
            val before = emitted.last().asia()
            now += 2.minutes
            ticks.emit(Unit)

            assertEquals(StationAvailability.Unavailable, emitted.last().asia().availability)
            assertEquals(before.values, emitted.last().asia().values)
        }

    @Test
    fun seismographFeed_stopsWaitingWhenAStationNeverSendsData() =
        runTest {
            var now = Noon
            val clock =
                object : Clock {
                    override fun now(): Instant = now
                }
            val ticks = MutableSharedFlow<Unit>()
            val emitted = mutableListOf<SeismographPlayback>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                seismographFeed(StreamedSeismograms(), clock, ticks).collect { emitted += it }
            }
            assertEquals(StationAvailability.Loading, emitted.last().asia().availability)
            now += PlaybackDelay
            ticks.emit(Unit)
            assertEquals(StationAvailability.Unavailable, emitted.last().asia().availability)
        }
}

private fun SeismographPlayback.asia(): StationPlayback = stations.getValue(Continent.Asia)

private fun recording(start: Instant = Noon - PlaybackDelay - PlaybackWindow - BaselineSpan) =
    GroundMotion(
        start,
        samplesPerSecond = 1.seconds / SeismographSlot,
        samples = List(RecordedSlots) { (it * 37) % 101 },
    )

private fun recorded(motion: GroundMotion) =
    SeismogramFeedEvent.Recorded(Seismographs.getValue(Continent.Asia).station, motion, countsPerMeterPerSecond = 1e9)

private class StreamedSeismograms : SeismogramRepository {
    val events = MutableSharedFlow<SeismogramFeedEvent>()

    override fun live(
        stations: List<SeismicStation>,
        since: Instant,
    ): Flow<SeismogramFeedEvent> = events
}
