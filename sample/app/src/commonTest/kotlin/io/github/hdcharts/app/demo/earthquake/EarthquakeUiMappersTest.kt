package io.github.hdcharts.app.demo.earthquake

import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.earthquakes_arrival_none
import hdcharts.app.generated.resources.earthquakes_arrival_past
import hdcharts.app.generated.resources.earthquakes_arrival_upcoming
import hdcharts.app.generated.resources.earthquakes_seismograph_last_recorded
import hdcharts.app.generated.resources.earthquakes_seismograph_unavailable
import hdcharts.app.generated.resources.earthquakes_seismograph_waiting
import hdcharts.app.generated.resources.earthquakes_selected
import hdcharts.app.generated.resources.earthquakes_waiting
import io.github.hdcharts.app.ui.UiText
import io.github.hdcharts.sampleapi.earthquake.Continent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val Noon = Instant.parse("2026-10-12T12:00:00Z")

class EarthquakeUiMappersTest {
    @Test
    fun waveArrival_countsDownUntilThePlayheadThenShowsTheArrivalTime() {
        val arrival = WaveArrival(quake("a", ago = 1.minutes, magnitude = 4.24, now = Noon), 812.4, Noon + 42.seconds)

        assertEquals(
            WaveArrivalUi(
                text =
                    UiText(
                        resource = Res.string.earthquakes_arrival_upcoming,
                        args = listOf("4.2", "REGION a", 812, "0:42"),
                    ),
                upcoming = true,
            ),
            arrival.toUi(playhead = Noon),
        )
        assertEquals(
            WaveArrivalUi(
                text =
                    UiText(
                        resource = Res.string.earthquakes_arrival_past,
                        args = listOf("4.2", "REGION a", 812, "12:00:42"),
                    ),
                upcoming = false,
            ),
            arrival.toUi(playhead = Noon + 1.minutes),
        )
    }

    @Test
    fun seismographPlayback_waitsOnEveryStationBeforeTheFirstRecording() {
        val cards = SeismographPlayback(playhead = Noon, stations = emptyMap()).toUi(arrivals = emptyMap()).cards

        assertEquals(Continent.entries.map { it.displayName }, cards.map { it.title })
        cards.forEach { card ->
            assertEquals(
                PlaceholderUi(UiText(Res.string.earthquakes_seismograph_waiting), loading = true),
                card.placeholder,
            )
            assertNull(card.status)
            assertNull(card.lastRecorded)
        }
    }

    @Test
    fun seismographPlayback_keepsTheArrivalLineWithoutARecentQuake() {
        // An empty line would appear later and push the chart up.
        val cards = SeismographPlayback(playhead = Noon, stations = emptyMap()).toUi(arrivals = emptyMap()).cards

        cards.forEach { card ->
            assertEquals(WaveArrivalUi(UiText(Res.string.earthquakes_arrival_none), upcoming = false), card.arrival)
        }
    }

    @Test
    fun seismographPlayback_showsAnUnavailableStationWithItsLastRecording() {
        val asia =
            StationPlayback(
                values = emptyList(),
                level = null,
                availability = StationAvailability.Unavailable,
                lastRecordedAt = Noon - 90.seconds,
            )

        val card =
            SeismographPlayback(playhead = Noon, stations = mapOf(Continent.Asia to asia))
                .toUi(arrivals = emptyMap())
                .cards
                .first()

        assertEquals(
            PlaceholderUi(UiText(Res.string.earthquakes_seismograph_unavailable), loading = false),
            card.placeholder,
        )
        assertEquals(
            UiText(resource = Res.string.earthquakes_seismograph_last_recorded, args = listOf("11:58:30")),
            card.lastRecorded,
        )
        assertEquals(StationStatus.Unavailable, card.status)
    }

    @Test
    fun continentHistory_captionsEachPointAndSharesTheMagnitudeScale() {
        val quakes =
            listOf(quake("a", ago = 20.minutes, magnitude = 3.0), quake("b", ago = 10.minutes, magnitude = 7.4))

        val continent = history(quakes).toUi(continent = Continent.Asia)

        assertEquals(listOf("a", "b"), continent.quakes.map { it.id })
        assertEquals(
            listOf(3.0, 7.4),
            continent.chart.series
                .single()
                .values,
        )
        assertEquals(
            UiText(resource = Res.string.earthquakes_selected, args = listOf("7.4", "REGION b", "11:50")),
            continent.quakes.last().caption,
        )
        // A quake stronger than the shared M7 top raises the scale to fit it.
        assertEquals(0.0..8.0, continent.range)
        assertNull(continent.placeholder)
        assertEquals(0.0..7.0, history(quakes.take(1)).toUi(continent = Continent.Asia).range)
    }

    @Test
    fun continentHistory_waitsUntilTheLineHasTwoPoints() {
        val single = history(listOf(quake("a", ago = 20.minutes, magnitude = 3.0))).toUi(continent = Continent.Asia)

        assertEquals(PlaceholderUi(UiText(Res.string.earthquakes_waiting), loading = false), single.placeholder)
    }

    @Test
    fun dailyCounts_labelsEachDayAndNamesEachContinent() {
        val counts = Continent.entries.associateWith { listOf(0, 0, 0) } + (Continent.Asia to listOf(0, 1, 2))

        val data = DailyCounts(firstDay = LocalDate(2026, 10, 10), days = 3, counts = counts).toChartData()

        assertEquals(listOf("Oct 10", "Oct 11", "Oct 12"), data.categories)
        assertEquals(Continent.entries.map { it.displayName }, data.series.map { it.name })
        assertEquals(listOf(0.0, 1.0, 2.0), data.series.first().values)
    }

    @Test
    fun magnitudeBandsChart_labelsBothEndsOfEachBand() {
        val data = magnitudeBandsChart(listOf(1, 0, 2, 1, 0, 0, 0, 2))

        assertEquals(listOf("M0–1", "M1–2", "M2–3", "M3–4", "M4–5", "M5–6", "M6–7", "M7+"), data.categories)
        assertEquals(listOf(1.0, 0.0, 2.0, 1.0, 0.0, 0.0, 0.0, 2.0), data.series.single().values)
    }

    @Test
    fun magnitudeLabel_roundsToOneDecimal() {
        assertEquals(listOf("4.0", "3.1", "6.5"), listOf(4.0, 3.14, 6.46).map { it.magnitudeLabel() })
    }

    @Test
    fun countdownLabel_roundsUpToWholeSeconds() {
        assertEquals(
            listOf("12:00", "1:43", "0:59", "0:01", "0:00", "0:00"),
            listOf(12.minutes, 102.3.seconds, 59.seconds, 1.milliseconds, Duration.ZERO, (-1).seconds)
                .map { it.countdownLabel() },
        )
    }
}

private fun history(quakes: List<io.github.hdcharts.sampleapi.earthquake.Earthquake>) =
    ContinentHistory(quakes = quakes, updatedAt = Noon)
