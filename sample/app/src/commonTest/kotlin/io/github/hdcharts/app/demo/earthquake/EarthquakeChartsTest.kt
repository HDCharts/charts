package io.github.hdcharts.app.demo.earthquake

import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val Now = Instant.parse("2026-10-12T12:00:00Z")

class EarthquakeChartsTest {
    @Test
    fun dailyCountsByContinent_countsStrongQuakesPerDayAndContinent() {
        val quakes =
            listOf(
                quake(id = "a", ago = 1.hours, magnitude = 4.5, continent = Continent.Asia),
                quake(id = "b", ago = 2.hours, magnitude = 5.1, continent = Continent.Asia),
                quake(id = "c", ago = 1.days, magnitude = 4.0, continent = Continent.Europe),
                quake(id = "weak", ago = 1.hours, magnitude = 3.9, continent = Continent.Asia),
                quake(id = "too old", ago = 3.days, magnitude = 6.0, continent = Continent.Asia),
            )

        val daily = dailyCountsByContinent(quakes = quakes, now = Now, days = 3, minMagnitude = 4.0)

        assertEquals(LocalDate(2026, 10, 10), daily.firstDay)
        assertEquals(listOf(0, 0, 2), daily.counts.getValue(Continent.Asia))
        assertEquals(listOf(0, 1, 0), daily.counts.getValue(Continent.Europe))
        assertEquals(listOf(0, 0, 0), daily.counts.getValue(Continent.Africa))
    }

    @Test
    fun recentByContinent_keepsQuakesSinceTheCutoffOldestFirst() {
        val quakes =
            listOf(
                quake(id = "newest", ago = 1.minutes, magnitude = 3.0, continent = Continent.Oceania),
                quake(id = "oldest", ago = 23.hours, magnitude = 1.0, continent = Continent.Oceania),
                quake(id = "middle", ago = 10.minutes, magnitude = 2.0, continent = Continent.Oceania),
                quake(id = "too old", ago = 25.hours, magnitude = 9.0, continent = Continent.Oceania),
            )

        val recent = recentByContinent(quakes = quakes, since = Now - 24.hours)

        assertEquals(listOf("oldest", "middle", "newest"), recent.getValue(Continent.Oceania).map { it.id })
        assertEquals(emptyList(), recent.getValue(Continent.Africa))
    }

    @Test
    fun quakesByMagnitude_countsEachWholeMagnitudeAndGroupsStrongOnes() {
        val quakes =
            listOf(0.4, 2.0, 2.9, 3.1, 7.2, 8.4).mapIndexed {
                i,
                m,
                ->
                quake(id = "$i", ago = 1.hours, magnitude = m)
            }

        assertEquals(listOf(1, 0, 2, 1, 0, 0, 0, 2), quakesByMagnitude(quakes))
    }
}

internal fun quake(
    id: String,
    ago: Duration,
    magnitude: Double,
    continent: Continent = Continent.Asia,
    now: Instant = Now,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
) = Earthquake(
    id = id,
    time = now - ago,
    magnitude = magnitude,
    region = "REGION $id",
    continent = continent,
    latitude = latitude,
    longitude = longitude,
)
