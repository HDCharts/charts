package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Earthquake
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** A quake whose first waves reach a station at [arrivesAt], estimated from the distance between them. */
internal data class WaveArrival(
    val quake: Earthquake,
    val distanceKm: Double,
    val arrivesAt: Instant,
)

private const val KM_PER_DEGREE = 111.19

// First P-wave travel time for a surface quake by distance in degrees, rounded from the IASP91 Earth model.
private val PWaveTravelSeconds =
    listOf(
        0.0 to 0.0,
        1.0 to 19.0,
        2.0 to 33.0,
        5.0 to 75.0,
        10.0 to 142.0,
        20.0 to 276.0,
        30.0 to 372.0,
        40.0 to 460.0,
        50.0 to 539.0,
        60.0 to 608.0,
        70.0 to 672.0,
        80.0 to 731.0,
        90.0 to 784.0,
        100.0 to 828.0,
        180.0 to 1_200.0,
    )

/** Great-circle distance between two points, in degrees of arc. */
internal fun arcDegrees(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double,
): Double {
    val fromPhi = fromLatitude.radians()
    val toPhi = toLatitude.radians()
    val halfDeltaPhi = (toPhi - fromPhi) / 2
    val halfDeltaLambda = (toLongitude - fromLongitude).radians() / 2
    val haversine =
        sin(halfDeltaPhi) * sin(halfDeltaPhi) + cos(fromPhi) * cos(toPhi) * sin(halfDeltaLambda) * sin(halfDeltaLambda)
    return 2 * asin(min(1.0, sqrt(haversine))) * 180 / PI
}

/** Estimated time for the first P wave to travel [arcDegrees]. */
internal fun pWaveTravelTime(arcDegrees: Double): Duration {
    val distance = arcDegrees.coerceIn(0.0, 180.0)
    val upper = PWaveTravelSeconds.indexOfFirst { (degrees, _) -> degrees >= distance }.coerceAtLeast(1)
    val (fromDegrees, fromSeconds) = PWaveTravelSeconds[upper - 1]
    val (toDegrees, toSeconds) = PWaveTravelSeconds[upper]
    return (fromSeconds + (toSeconds - fromSeconds) * (distance - fromDegrees) / (toDegrees - fromDegrees)).seconds
}

// Rough rule for a broadband station: small quakes register only nearby, large ones anywhere.
internal fun likelyRecorded(
    magnitude: Double,
    arcDegrees: Double,
): Boolean =
    when {
        arcDegrees < 2 -> magnitude >= 2.5
        arcDegrees < 20 -> magnitude >= 4.0
        else -> magnitude >= 5.5
    }

/** The latest arrival at the given place among quakes since [since] that it would likely record. */
internal fun latestArrival(
    quakes: Collection<Earthquake>,
    latitude: Double,
    longitude: Double,
    since: Instant,
): WaveArrival? =
    quakes
        .asSequence()
        .filter { it.time >= since }
        .mapNotNull { quake ->
            val degrees = arcDegrees(quake.latitude, quake.longitude, latitude, longitude)
            if (!likelyRecorded(quake.magnitude, degrees)) return@mapNotNull null
            WaveArrival(
                quake = quake,
                distanceKm = degrees * KM_PER_DEGREE,
                arrivesAt = quake.time + pWaveTravelTime(degrees),
            )
        }.maxByOrNull { it.arrivesAt }

private fun Double.radians(): Double = this * PI / 180
