package io.github.hdcharts.app.demo.earthquake

import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.earthquakes_arrival_none
import hdcharts.app.generated.resources.earthquakes_arrival_past
import hdcharts.app.generated.resources.earthquakes_arrival_upcoming
import hdcharts.app.generated.resources.earthquakes_last_day
import hdcharts.app.generated.resources.earthquakes_latest
import hdcharts.app.generated.resources.earthquakes_seismograph_last_recorded
import hdcharts.app.generated.resources.earthquakes_seismograph_station
import hdcharts.app.generated.resources.earthquakes_seismograph_unavailable
import hdcharts.app.generated.resources.earthquakes_seismograph_waiting
import hdcharts.app.generated.resources.earthquakes_selected
import hdcharts.app.generated.resources.earthquakes_status_connecting
import hdcharts.app.generated.resources.earthquakes_status_live
import hdcharts.app.generated.resources.earthquakes_status_reconnecting
import hdcharts.app.generated.resources.earthquakes_updated
import hdcharts.app.generated.resources.earthquakes_waiting
import io.github.hdcharts.app.ui.UiText
import io.github.hdcharts.core.model.ChartData
import io.github.hdcharts.core.model.ChartSeries
import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.Instant

// Top of the shared magnitude scale; a stronger quake raises it so its spike is not cut off.
private const val MAGNITUDE_SCALE_MAX = 7.0

internal fun DashboardState.toUi(): EarthquakeDashboardUiState =
    when (this) {
        DashboardState.Loading -> EarthquakeDashboardUiState.Loading
        DashboardState.Failed -> EarthquakeDashboardUiState.Failed
        is DashboardState.Ready ->
            EarthquakeDashboardUiState.Ready(
                feed = connection.toUi(latest = latest),
                continents = Continent.entries.map { lastDay.getValue(it).toUi(continent = it) },
                magnitudes =
                    ChartCardUi(
                        data = magnitudeBandsChart(magnitudeBands),
                        updated = updatedText(magnitudeBandsUpdatedAt),
                    ),
                dailyCounts = ChartCardUi(data = dailyCounts.toChartData(), updated = null),
            )
    }

private fun ConnectionStatus.toUi(latest: Earthquake?) =
    FeedStatusUi(
        status = this,
        text =
            when (this) {
                ConnectionStatus.Connecting -> UiText(Res.string.earthquakes_status_connecting)
                ConnectionStatus.Live -> UiText(Res.string.earthquakes_status_live)
                ConnectionStatus.Reconnecting -> UiText(Res.string.earthquakes_status_reconnecting)
            },
        latest = latest?.let { UiText(resource = Res.string.earthquakes_latest, args = it.summaryArgs()) },
    )

internal fun ContinentHistory.toUi(continent: Continent): ContinentQuakesUi {
    val magnitudes = quakes.map { it.magnitude }
    return ContinentQuakesUi(
        title = continent.displayName,
        subtitle = UiText(resource = Res.string.earthquakes_last_day, args = listOf(magnitudes.size)),
        // No x labels: points are evenly spaced per quake, so times would suggest a time scale.
        chart = ChartData(series = listOf(ChartSeries(name = "Magnitude", values = magnitudes))),
        // Same scale on every continent, so spike heights compare across them.
        range = 0.0..maxOf(MAGNITUDE_SCALE_MAX, ceil(magnitudes.maxOrNull() ?: 0.0)),
        quakes =
            quakes.map {
                QuakePointUi(
                    id = it.id,
                    caption = UiText(resource = Res.string.earthquakes_selected, args = it.summaryArgs()),
                )
            },
        updated = updatedText(updatedAt),
        // The line needs two points; a continent can be quiet for a while.
        placeholder =
            if (magnitudes.size >= 2) null else PlaceholderUi(UiText(Res.string.earthquakes_waiting), loading = false),
    )
}

// Both ends in each label, so "M2–3" does not read as exactly magnitude 2.
internal fun magnitudeBandsChart(counts: List<Int>): ChartData =
    ChartData(
        categories = List(counts.size) { if (it == counts.lastIndex) "M$it+" else "M$it–${it + 1}" },
        series = listOf(ChartSeries(name = "Quakes", values = counts.map(Int::toDouble))),
    )

internal fun SeismographPlayback.toUi(arrivals: Map<Continent, WaveArrival>): SeismographsUiState =
    SeismographsUiState(
        cards =
            Seismographs.map { (continent, seismograph) ->
                stations[continent].toUi(
                    continent = continent,
                    seismograph = seismograph,
                    // Always a line, so the chart keeps its height when an arrival shows up.
                    arrival = arrivals[continent]?.toUi(playhead = playhead) ?: NoArrival,
                )
            },
    )

private fun StationPlayback?.toUi(
    continent: Continent,
    seismograph: Seismograph,
    arrival: WaveArrivalUi,
): SeismographCardUi {
    val unavailable = this?.availability == StationAvailability.Unavailable
    val values = this?.values.orEmpty()
    return SeismographCardUi(
        title = continent.displayName,
        subtitle =
            UiText(
                resource = Res.string.earthquakes_seismograph_station,
                args = listOf(seismograph.place, seismograph.station.network, seismograph.station.code),
            ),
        chart = ChartData(series = listOf(ChartSeries(name = "Ground velocity", values = values))),
        range = this?.range,
        placeholder =
            when {
                values.isNotEmpty() -> null
                unavailable ->
                    PlaceholderUi(message = UiText(Res.string.earthquakes_seismograph_unavailable), loading = false)
                else -> PlaceholderUi(message = UiText(Res.string.earthquakes_seismograph_waiting), loading = true)
            },
        lastRecorded =
            this?.lastRecordedAt?.takeIf { unavailable }?.let {
                UiText(resource = Res.string.earthquakes_seismograph_last_recorded, args = listOf(it.utcClockLabel()))
            },
        arrival = arrival,
        status = if (unavailable) StationStatus.Unavailable else this?.level?.toStatus(),
    )
}

private fun ShakingLevel.toStatus(): StationStatus =
    when (this) {
        ShakingLevel.Quiet -> StationStatus.Quiet
        ShakingLevel.Normal -> StationStatus.Normal
        ShakingLevel.Elevated -> StationStatus.Elevated
        ShakingLevel.Strong -> StationStatus.Strong
    }

private val NoArrival = WaveArrivalUi(text = UiText(Res.string.earthquakes_arrival_none), upcoming = false)

/** Counts down to the estimated arrival at the playhead, then shows when the waves arrived. */
internal fun WaveArrival.toUi(playhead: Instant): WaveArrivalUi {
    val upcoming = arrivesAt > playhead
    return WaveArrivalUi(
        text =
            UiText(
                resource =
                    if (upcoming) Res.string.earthquakes_arrival_upcoming else Res.string.earthquakes_arrival_past,
                args =
                    listOf(
                        quake.magnitude.magnitudeLabel(),
                        quake.region,
                        distanceKm.roundToInt(),
                        if (upcoming) (arrivesAt - playhead).countdownLabel() else arrivesAt.utcClockLabel(),
                    ),
            ),
        upcoming = upcoming,
    )
}

internal fun DailyCounts.toChartData(): ChartData =
    ChartData(
        categories = List(days) { DayLabelFormat.format(firstDay.plus(it, DateTimeUnit.DAY)) },
        series =
            Continent.entries.map { continent ->
                ChartSeries(name = continent.displayName, values = counts.getValue(continent).map(Int::toDouble))
            },
    )

private fun updatedText(updatedAt: Instant) =
    UiText(resource = Res.string.earthquakes_updated, args = listOf(updatedAt.utcClockLabel()))

// Magnitude, region, and time, as the latest and selected quake captions show them.
private fun Earthquake.summaryArgs(): List<Any> = listOf(magnitude.magnitudeLabel(), region, time.utcTimeLabel())

internal val Continent.displayName: String
    get() =
        when (this) {
            Continent.Asia -> "Asia"
            Continent.Oceania -> "Oceania"
            Continent.NorthAmerica -> "North America"
            Continent.SouthAmerica -> "South America"
            Continent.Europe -> "Europe"
            Continent.Africa -> "Africa"
        }

private val DayLabelFormat =
    LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        day(padding = Padding.NONE)
    }

private val ClockLabelFormat =
    LocalTime.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
    }

private val TimeLabelFormat =
    LocalTime.Format {
        hour()
        char(':')
        minute()
    }

internal fun Instant.utcTimeLabel(): String = TimeLabelFormat.format(toLocalDateTime(TimeZone.UTC).time)

internal fun Instant.utcClockLabel(): String = ClockLabelFormat.format(toLocalDateTime(TimeZone.UTC).time)

/** `m:ss` left, rounded up so it shows 0:00 only once the time has passed. */
internal fun Duration.countdownLabel(): String {
    val seconds = ceil(coerceAtLeast(Duration.ZERO).toDouble(DurationUnit.SECONDS)).toLong()
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

/** One decimal, as agencies report magnitudes. */
internal fun Double.magnitudeLabel(): String = ((this * 10).roundToInt() / 10.0).toString()
