package io.github.hdcharts.sampleapi.earthquake

import kotlin.time.Duration
import kotlin.time.Instant

/**
 * One reported earthquake.
 *
 * @param id Stable across revisions, so a revised report replaces the earlier one.
 * @param region Seismic region name as the agency reports it, such as `"ANTOFAGASTA, CHILE"`.
 */
data class Earthquake(
    val id: String,
    val time: Instant,
    val magnitude: Double,
    val region: String,
    val continent: Continent,
    val latitude: Double,
    val longitude: Double,
)

/** Continent an earthquake is grouped under; offshore quakes go to the nearest one. */
enum class Continent {
    Asia,
    Oceania,
    NorthAmerica,
    SouthAmerica,
    Europe,
    Africa,
}

/** What the live earthquake feed reports while it is collected. */
sealed interface EarthquakeFeedEvent {
    /** The live connection is open. */
    data object Connected : EarthquakeFeedEvent

    /** A new earthquake, or a revision of one reported earlier. */
    data class Reported(
        val earthquake: Earthquake,
    ) : EarthquakeFeedEvent

    /** The connection dropped; the feed reconnects after [retryIn]. */
    data class Disconnected(
        val retryIn: Duration,
    ) : EarthquakeFeedEvent
}
