package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.seismogram.GroundMotion
import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.SeismogramFeedEvent
import io.github.hdcharts.sampleapi.seismogram.SeismogramRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.produceIn
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** A station shown for a continent, with a place name a viewer would recognise and its coordinates. */
internal data class Seismograph(
    val station: SeismicStation,
    val place: String,
    val latitude: Double,
    val longitude: Double,
)

internal enum class ShakingLevel {
    Quiet,
    Normal,
    Elevated,
    Strong,
}

/**
 * One station's playback window in nm/s, and how the newest seconds compare with the rest.
 *
 * @param range The y range to draw, kept steady while the data stays inside it.
 */
internal data class StationPlayback(
    val values: List<Double>,
    val level: ShakingLevel?,
    val range: ClosedFloatingPointRange<Double>? = null,
    val availability: StationAvailability = StationAvailability.Loading,
    val lastRecordedAt: Instant? = null,
)

internal enum class StationAvailability {
    Loading,
    Available,
    Unavailable,
}

/** @param playhead The recorded moment shown at the right edge, [PlaybackDelay] behind the clock. */
internal data class SeismographPlayback(
    val playhead: Instant,
    val stations: Map<Continent, StationPlayback>,
)

// One station per continent from the global IU network, on the fastest channel served live: 40 vertical samples/s.
// Coordinates come from the FDSN station service.
internal val Seismographs: Map<Continent, Seismograph> =
    mapOf(
        Continent.Asia to
            Seismograph(
                broadband(code = "MAJO"),
                place = "Matsushiro, Japan",
                latitude = 36.54567,
                longitude = 138.20406,
            ),
        Continent.Oceania to
            Seismograph(
                broadband(code = "CTAO"),
                place = "Charters Towers, Australia",
                latitude = -20.08765,
                longitude = 146.24998,
            ),
        Continent.NorthAmerica to
            Seismograph(
                broadband(code = "ANMO"),
                place = "Albuquerque, USA",
                latitude = 34.94591,
                longitude = -106.4572,
            ),
        // The main LCO sensor records BHZ at only 20 samples/s; the second one records 40.
        Continent.SouthAmerica to
            Seismograph(
                broadband(code = "LCO", location = "10"),
                place = "Las Campanas, Chile",
                latitude = -29.011,
                longitude = -70.7005,
            ),
        Continent.Europe to
            Seismograph(broadband(code = "KONO"), place = "Kongsberg, Norway", latitude = 59.6521, longitude = 9.5946),
        Continent.Africa to
            Seismograph(
                broadband(code = "TSUM"),
                place = "Tsumeb, Namibia",
                latitude = -19.20227,
                longitude = 17.58376,
            ),
    )

private fun broadband(
    code: String,
    location: String = "00",
) = SeismicStation(network = "IU", code = code, location = location, channel = "BHZ")

// The newest data is usually 0 to 20 s old, so a minute behind always has something to play.
internal val PlaybackDelay = 1.minutes
internal val PlaybackWindow = 1.minutes

// One drawn point per slot; each slot keeps its strongest sample so spikes survive.
internal val SeismographSlot = 200.milliseconds

/** Plays streamed recordings one slot per [ticks], [PlaybackDelay] behind the clock. */
internal fun seismographFeed(
    repository: SeismogramRepository,
    clock: Clock,
    ticks: Flow<Unit>,
): Flow<SeismographPlayback> =
    flow {
        val startedAt = clock.now()
        val continents = Seismographs.entries.associate { (continent, seismograph) -> seismograph.station to continent }
        var stations = Seismographs.keys.associateWith { RecordedStation() }
        var playheadSlot = (startedAt - PlaybackDelay).slotIndex()
        var state = playbackWindows(stations, playheadSlot, previous = null, now = startedAt, startedAt = startedAt)
        emit(state)
        val received =
            repository.live(
                continents.keys.toList(),
                since = startedAt - PlaybackDelay - PlaybackWindow - BaselineSpan,
            )

        // A null is a clock tick. All recording and playback mutations belong to this collector.
        fun apply(update: SeismogramFeedEvent?) {
            if (update == null) {
                playheadSlot =
                    nextPlayheadSlot(current = playheadSlot, target = (clock.now() - PlaybackDelay).slotIndex())
            } else {
                val continent = continents[update.station] ?: return
                val recorded = stations.getValue(continent)
                val next =
                    when (update) {
                        is SeismogramFeedEvent.Unavailable ->
                            recorded.copy(
                                availability = StationAvailability.Unavailable,
                            )
                        is SeismogramFeedEvent.Recorded -> {
                            val motion = update.motion
                            if (motion.samples.isEmpty()) return
                            val keptSlots = ((PlaybackWindow + BaselineSpan) / SeismographSlot).toLong()
                            val hasWaveform =
                                state.stations
                                    .getValue(continent)
                                    .values
                                    .isNotEmpty()
                            recorded.copy(
                                countsPerMeterPerSecond = update.countsPerMeterPerSecond,
                                slots =
                                    recorded.slots.withMotion(
                                        motion = motion,
                                        oldestKept = playheadSlot - keptSlots,
                                        writableFrom = if (hasWaveform) playheadSlot + 1 else Long.MIN_VALUE,
                                        receivedThrough = recorded.receivedThrough,
                                    ),
                                receivedThrough = maxOf(recorded.receivedThrough ?: Instant.DISTANT_PAST, motion.end),
                                lastRecordedAt =
                                    maxOf(
                                        recorded.lastRecordedAt ?: Instant.DISTANT_PAST,
                                        motion.start + ((motion.samples.size - 1) / motion.samplesPerSecond).seconds,
                                    ),
                                availability = StationAvailability.Available,
                            )
                        }
                    }
                stations = stations + (continent to next)
            }
        }
        coroutineScope {
            val updates = merge(received, ticks.map { null }).produceIn(this)
            for (first in updates) {
                // Takes every update already waiting, so a replayed backlog builds the windows once, not per record.
                apply(first)
                var next = updates.tryReceive()
                while (next.isSuccess) {
                    apply(next.getOrNull())
                    next = updates.tryReceive()
                }
                state =
                    playbackWindows(stations, playheadSlot, previous = state, now = clock.now(), startedAt = startedAt)
                emit(state)
            }
        }
    }

// Playback this far behind the clock jumps to catch up instead of stepping; a hidden tab gets here quickly.
private val PlaybackResync = 2.seconds

/**
 * Moves playback one slot towards [target], so the chart always slides by exactly one point; a late tick costs a
 * slot of delay instead of a jump. Falling more than [PlaybackResync] behind jumps straight to [target].
 */
internal fun nextPlayheadSlot(
    current: Long,
    target: Long,
): Long =
    when {
        target <= current -> current
        target - current > PlaybackResync / SeismographSlot -> target
        else -> current + 1
    }

/** Raw counts grouped into slots, with the cursor used to discard records replayed after reconnecting. */
private data class RecordedStation(
    val countsPerMeterPerSecond: Double? = null,
    val slots: Map<Long, Slot> = emptyMap(),
    val receivedThrough: Instant? = null,
    val lastRecordedAt: Instant? = null,
    val availability: StationAvailability = StationAvailability.Loading,
)

/** The samples of one slot, kept as running totals so samples split across records still combine. */
internal data class Slot(
    val sum: Long,
    val count: Int,
    val min: Int,
    val max: Int,
) {
    operator fun plus(sample: Int) =
        Slot(sum = sum + sample, count = count + 1, min = minOf(min, sample), max = maxOf(max, sample))

    /** The sample furthest from the slot's mean, so a short spike is drawn at its full height. */
    val strongest: Int
        get() {
            val mean = sum.toDouble() / count
            return if (abs(max - mean) >= abs(mean - min)) max else min
        }

    companion object {
        fun of(sample: Int) = Slot(sum = sample.toLong(), count = 1, min = sample, max = sample)
    }
}

/** Adds [motion] to slots from [writableFrom] on, keeping only slots at or after [oldestKept]. */
internal fun Map<Long, Slot>.withMotion(
    motion: GroundMotion,
    oldestKept: Long,
    writableFrom: Long = Long.MIN_VALUE,
    receivedThrough: Instant? = null,
): Map<Long, Slot> {
    val updated = toMutableMap()
    motion.samples.forEachIndexed { index, sample ->
        val time = motion.start + (index / motion.samplesPerSecond).seconds
        if (receivedThrough?.let { time < it } == true) return@forEachIndexed
        val slot = time.slotIndex()
        if (slot >= writableFrom) updated[slot] = updated[slot]?.plus(sample) ?: Slot.of(sample)
    }
    return updated.filterKeys { it >= oldestKept }
}

/**
 * Ground velocity in nm/s for the [slots] up to [endSlot], centred on the station's average over the [baselineSlots]
 * before each point. A slot without data repeats the one before it.
 */
internal fun Map<Long, Slot>.playbackWindow(
    endSlot: Long,
    slots: Int,
    countsPerMeterPerSecond: Double,
    baselineSlots: Int = 0,
): List<Double> {
    val firstSlot = endSlot - slots + 1
    val firstNeeded = firstSlot - baselineSlots
    // Starts from the last slot before the span, so a gap is filled the same way wherever it sits.
    var held =
        (firstNeeded downTo firstNeeded - baselineSlots - slots).firstNotNullOfOrNull { this[it] }
            ?: (firstNeeded..endSlot).firstNotNullOfOrNull { this[it] }
            ?: return emptyList()
    if ((firstSlot..endSlot).none { this[it] != null }) return emptyList()
    val span = (endSlot - firstNeeded + 1).toInt()
    val strongest = IntArray(span)
    // Fixed-point means keep the running sum exact, so a point keeps its last bits and LiveLineChart never morphs it.
    val means = LongArray(span)
    for (index in 0 until span) {
        held = this[firstNeeded + index] ?: held
        strongest[index] = held.strongest
        means[index] = (held.sum * MEAN_SCALE).floorDiv(held.count)
    }
    var total = (0 until baselineSlots).sumOf { means[it] }
    return List(slots) { windowIndex ->
        val index = baselineSlots + windowIndex
        if (windowIndex > 0) total += means[index - 1] - means[windowIndex - 1]
        val baseline = if (baselineSlots == 0) 0.0 else total.toDouble() / MEAN_SCALE / baselineSlots
        (strongest[index] - baseline) / countsPerMeterPerSecond * NANOMETERS_PER_METER
    }
}

private const val NANOMETERS_PER_METER = 1e9

// Slot means are kept to 1/1024 of a count, far below what the chart can show.
private const val MEAN_SCALE = 1024L

internal fun Instant.slotIndex(): Long = toEpochMilliseconds().floorDiv(SeismographSlot.inWholeMilliseconds)

// Shaking is judged over chunks of this length: the newest one against the typical one.
private val ShakingChunk = 10.seconds

private fun playbackWindows(
    stations: Map<Continent, RecordedStation>,
    playheadSlot: Long,
    previous: SeismographPlayback?,
    now: Instant,
    startedAt: Instant,
): SeismographPlayback {
    val playhead = Instant.fromEpochMilliseconds(playheadSlot * SeismographSlot.inWholeMilliseconds)
    return SeismographPlayback(
        playhead = playhead,
        stations =
            stations.mapValues { (continent, recorded) ->
                val before = previous?.stations?.get(continent)
                val availability =
                    when {
                        recorded.availability == StationAvailability.Unavailable -> StationAvailability.Unavailable
                        recorded.lastRecordedAt != null && recorded.lastRecordedAt >= playhead ->
                            StationAvailability.Available
                        now - startedAt >= PlaybackDelay -> StationAvailability.Unavailable
                        else -> StationAvailability.Loading
                    }
                // A missing recording keeps the last waveform, with its age and unavailable status visible.
                val values =
                    if (availability != StationAvailability.Available) {
                        before?.values.orEmpty()
                    } else {
                        recorded.slots.playbackWindow(
                            endSlot = playheadSlot,
                            slots = (PlaybackWindow / SeismographSlot).toInt(),
                            countsPerMeterPerSecond = recorded.countsPerMeterPerSecond!!,
                            baselineSlots = (BaselineSpan / SeismographSlot).toInt(),
                        )
                    }
                StationPlayback(
                    values = values,
                    level =
                        if (availability == StationAvailability.Available) {
                            val firstSlot = playheadSlot - values.size + 1
                            val recordedValues =
                                values.mapIndexed { index, value ->
                                    value.takeIf { firstSlot + index in recorded.slots }
                                }
                            shakingLevel(values = recordedValues, chunk = (ShakingChunk / SeismographSlot).toInt())
                        } else {
                            null
                        },
                    range = steadyRange(current = before?.range, values = values),
                    availability = availability,
                    lastRecordedAt = recorded.lastRecordedAt,
                )
            },
    )
}

// The trace is centred on this much earlier motion, which removes each station's offset and slow drift.
internal val BaselineSpan = 30.seconds

// A fresh scale spans this many times the station's normal shaking, like the fixed gain of a real display.
private const val RANGE_NOISE_FACTOR = 4.0

// Room left above the largest swing whenever the scale has to grow.
private const val RANGE_HEADROOM = 1.2

// The scale shrinks only once the window needs less than this fraction of it, so normal shaking never rescales.
private const val RANGE_SHRINK_BELOW = 0.5

/**
 * A symmetric scale around zero that is wide enough for the station's normal shaking and the largest swing. It
 * widens as soon as a stronger swing arrives, and goes back to a fresh scale only once that swing has left the window.
 */
internal fun steadyRange(
    current: ClosedFloatingPointRange<Double>?,
    values: List<Double>,
): ClosedFloatingPointRange<Double>? {
    if (values.isEmpty()) return current
    val peak = values.maxOf { abs(it) }
    val fresh = maxOf(RANGE_NOISE_FACTOR * values.standardDeviation(), peak * RANGE_HEADROOM, 1.0)
    val half =
        when {
            current == null -> fresh
            peak > current.endInclusive -> peak * RANGE_HEADROOM
            fresh < current.endInclusive * RANGE_SHRINK_BELOW -> fresh
            else -> current.endInclusive
        }
    return -half..half
}

/**
 * How the spread of the newest [chunk] values compares with the typical spread of all chunks in [values]. A null
 * value is a slot held through a gap; chunks with one are skipped, since a held stretch is not quiet ground.
 */
internal fun shakingLevel(
    values: List<Double?>,
    chunk: Int,
): ShakingLevel? {
    val chunks = values.chunked(chunk).filter { it.size == chunk }
    if (chunks.isEmpty() || null in chunks.last()) return null
    val spreads = chunks.filterNot { null in it }.map { it.filterNotNull().standardDeviation() }
    if (spreads.size < 3) return null
    val typical = spreads.sorted()[spreads.size / 2]
    if (typical <= 0.0) return null
    val ratio = spreads.last() / typical
    return when {
        ratio < 0.7 -> ShakingLevel.Quiet
        ratio < 1.5 -> ShakingLevel.Normal
        ratio < 3.0 -> ShakingLevel.Elevated
        else -> ShakingLevel.Strong
    }
}

private fun List<Double>.standardDeviation(): Double {
    val mean = average()
    return sqrt(sumOf { (it - mean) * (it - mean) } / size)
}
