package io.github.hdcharts.app.demo.earthquake

import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.earthquakes_level_elevated
import hdcharts.app.generated.resources.earthquakes_level_normal
import hdcharts.app.generated.resources.earthquakes_level_quiet
import hdcharts.app.generated.resources.earthquakes_level_strong
import hdcharts.app.generated.resources.earthquakes_seismograph_unavailable
import io.github.hdcharts.app.ui.UiText
import io.github.hdcharts.core.model.ChartData
import org.jetbrains.compose.resources.StringResource

sealed interface EarthquakeDashboardUiState {
    data object Loading : EarthquakeDashboardUiState

    data object Failed : EarthquakeDashboardUiState

    /** @param continents One last-day card per continent, in grid order. */
    data class Ready(
        val feed: FeedStatusUi,
        val continents: List<ContinentQuakesUi>,
        val magnitudes: ChartCardUi,
        val dailyCounts: ChartCardUi,
    ) : EarthquakeDashboardUiState
}

enum class ConnectionStatus {
    Connecting,
    Live,
    Reconnecting,
}

data class FeedStatusUi(
    val status: ConnectionStatus,
    val text: UiText,
    val latest: UiText?,
)

/** @param updated When the data last changed; null for a chart that does not show it. */
data class ChartCardUi(
    val data: ChartData,
    val updated: UiText?,
)

/** Shown in a chart's place while there is nothing to draw. */
data class PlaceholderUi(
    val message: UiText,
    val loading: Boolean,
)

/** @param quakes One per chart point, in the same order, so a selected point names its quake. */
data class ContinentQuakesUi(
    val title: String,
    val subtitle: UiText,
    val chart: ChartData,
    val range: ClosedFloatingPointRange<Double>,
    val quakes: List<QuakePointUi>,
    val updated: UiText,
    val placeholder: PlaceholderUi?,
)

/** @param id Stays the same while indexes shift, so a selection can follow its quake. */
data class QuakePointUi(
    val id: String,
    val caption: UiText,
)

/** @param cards One seismograph per continent, in grid order. */
data class SeismographsUiState(
    val cards: List<SeismographCardUi>,
)

/**
 * @param range The y range to draw, kept steady while the data stays inside it.
 * @param lastRecorded Shown only while the station is unavailable.
 */
data class SeismographCardUi(
    val title: String,
    val subtitle: UiText,
    val chart: ChartData,
    val range: ClosedFloatingPointRange<Double>?,
    val placeholder: PlaceholderUi?,
    val lastRecorded: UiText?,
    val arrival: WaveArrivalUi,
    val status: StationStatus?,
)

/** @param upcoming The waves have not reached the playhead yet, which the card highlights. */
data class WaveArrivalUi(
    val text: UiText,
    val upcoming: Boolean,
)

/** What a seismograph card's dot stands for; the legend lists them in this order. */
enum class StationStatus(
    val label: StringResource,
) {
    Quiet(Res.string.earthquakes_level_quiet),
    Normal(Res.string.earthquakes_level_normal),
    Elevated(Res.string.earthquakes_level_elevated),
    Strong(Res.string.earthquakes_level_strong),
    Unavailable(Res.string.earthquakes_seismograph_unavailable),
}
