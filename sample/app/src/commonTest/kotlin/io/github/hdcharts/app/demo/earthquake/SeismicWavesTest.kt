package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Continent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val Now = Instant.parse("2026-10-12T12:00:00Z")

class SeismicWavesTest {
    @Test
    fun arcDegrees_measuresTheGreatCircle() {
        assertEquals(0.0, arcDegrees(10.0, 20.0, 10.0, 20.0), absoluteTolerance = 1e-9)
        assertEquals(90.0, arcDegrees(0.0, 0.0, 0.0, 90.0), absoluteTolerance = 1e-9)
        assertEquals(180.0, arcDegrees(0.0, 0.0, 0.0, 180.0), absoluteTolerance = 1e-9)
        // Crossing the antimeridian takes the short way round.
        assertEquals(2.0, arcDegrees(0.0, 179.0, 0.0, -179.0), absoluteTolerance = 1e-9)
    }

    @Test
    fun pWaveTravelTime_followsTheTableAndInterpolatesBetweenRows() {
        assertEquals(0.seconds, pWaveTravelTime(0.0))
        assertEquals(142.seconds, pWaveTravelTime(10.0))
        assertEquals(209.seconds, pWaveTravelTime(15.0))
        assertEquals(784.seconds, pWaveTravelTime(90.0))
    }

    @Test
    fun likelyRecorded_needsStrongerQuakesFurtherAway() {
        assertTrue(likelyRecorded(magnitude = 2.5, arcDegrees = 1.0))
        assertFalse(likelyRecorded(magnitude = 3.5, arcDegrees = 10.0))
        assertTrue(likelyRecorded(magnitude = 4.0, arcDegrees = 10.0))
        assertFalse(likelyRecorded(magnitude = 5.0, arcDegrees = 60.0))
        assertTrue(likelyRecorded(magnitude = 5.5, arcDegrees = 60.0))
    }

    @Test
    fun latestArrival_picksTheNewestArrivalAmongRecordableQuakes() {
        val quakes =
            listOf(
                // 10 degrees away, strong enough: arrives 142 s after it happened.
                quake(id = "near", ago = 20.minutes, magnitude = 4.5, now = Now, latitude = 0.0, longitude = 10.0),
                // 90 degrees away but too weak to register.
                quake(id = "weak far", ago = 1.minutes, magnitude = 4.5, now = Now, latitude = 0.0, longitude = 90.0),
                // 90 degrees away and strong: arrives 784 s after it happened, the newest arrival.
                quake(
                    id = "strong far",
                    ago = 10.minutes,
                    magnitude = 6.0,
                    now = Now,
                    latitude = 0.0,
                    longitude = 90.0,
                ),
                quake(id = "too old", ago = 2.hours, magnitude = 7.0, now = Now, latitude = 0.0, longitude = 1.0),
            )

        val arrival = latestArrival(quakes = quakes, latitude = 0.0, longitude = 0.0, since = Now - 1.hours)

        assertEquals("strong far", arrival?.quake?.id)
        assertEquals(Now - 10.minutes + 784.seconds, arrival?.arrivesAt)
        assertEquals(10_007.0, arrival!!.distanceKm, absoluteTolerance = 1.0)
    }

    @Test
    fun latestArrival_isNullWithoutARecordableQuake() {
        val quakes =
            listOf(quake(id = "tiny", ago = 1.minutes, magnitude = 1.0, continent = Continent.Europe, now = Now))

        assertNull(latestArrival(quakes = quakes, latitude = 45.0, longitude = 45.0, since = Now - 1.hours))
    }
}
