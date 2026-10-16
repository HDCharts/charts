package io.github.hdcharts.sampleapi.seismogram

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** One seismometer channel, named the FDSN way: network, station code, location, and channel, e.g. IU.ANMO.00.BHZ. */
data class SeismicStation(
    val network: String,
    val code: String,
    val location: String,
    val channel: String,
)

/** Evenly spaced ground-motion samples in raw sensor counts, starting at [start]. */
data class GroundMotion(
    val start: Instant,
    val samplesPerSecond: Double,
    val samples: List<Int>,
) {
    /** When the sample after the last one is due, where the next record continues. */
    val end: Instant get() = start + (samples.size / samplesPerSecond).seconds
}

sealed interface SeismogramFeedEvent {
    val station: SeismicStation

    data class Recorded(
        override val station: SeismicStation,
        val motion: GroundMotion,
        val countsPerMeterPerSecond: Double,
    ) : SeismogramFeedEvent

    data class Unavailable(
        override val station: SeismicStation,
    ) : SeismogramFeedEvent
}

interface SeismogramRepository {
    /** One live connection per station, each resuming after a reconnect. History depends on the server's buffer. */
    fun live(
        stations: List<SeismicStation>,
        since: Instant,
    ): Flow<SeismogramFeedEvent>
}
