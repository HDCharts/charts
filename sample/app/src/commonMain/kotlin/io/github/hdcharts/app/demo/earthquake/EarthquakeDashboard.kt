package io.github.hdcharts.app.demo.earthquake

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.allStringResources
import hdcharts.app.generated.resources.earthquakes_attribution
import hdcharts.app.generated.resources.earthquakes_daily_counts_subtitle
import hdcharts.app.generated.resources.earthquakes_daily_counts_title
import hdcharts.app.generated.resources.earthquakes_load_failed
import hdcharts.app.generated.resources.earthquakes_magnitudes_subtitle
import hdcharts.app.generated.resources.earthquakes_magnitudes_title
import hdcharts.app.generated.resources.earthquakes_retry
import hdcharts.app.generated.resources.earthquakes_section_last_day
import hdcharts.app.generated.resources.earthquakes_section_last_month
import hdcharts.app.generated.resources.earthquakes_section_seismographs
import io.github.hdcharts.app.ui.UiText
import io.github.hdcharts.app.ui.composable.CardGrid
import io.github.hdcharts.app.ui.resolve
import io.github.hdcharts.core.model.rememberChartSelection
import io.github.hdcharts.core.style.AxesDefaults
import io.github.hdcharts.core.style.AxisLabelDefaults
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.GridDefaults
import io.github.hdcharts.histogram.HistogramChart
import io.github.hdcharts.line.LineChart
import io.github.hdcharts.line.LineChartDefaults
import io.github.hdcharts.line.LiveLineChart
import io.github.hdcharts.sampleshared.theme.Dimens
import io.github.hdcharts.stackedbar.StackedBarChart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val ThreeColumnBreakpoint = 940.dp
private val TwoColumnBreakpoint = 600.dp

// Inside a vertical scroll a chart would size itself square to its width, so every chart gets a fixed height.
private val SeismographHeight = 250.dp
private val LastDayChartHeight = 220.dp
private val DailyCountsHeight = 500.dp
private val MagnitudesHeight = 500.dp

// The daily counts have 30 bars to the histogram's 8, so they get the wider share of a shared row.
private const val MAGNITUDES_WEIGHT = 1f
private const val DAILY_COUNTS_WEIGHT = 2f

// Matches the line height of the small caption text beside it.
private val ArrivalIconSize = 14.dp
private val StatusDotSize = 10.dp
private val StatusRingWidth = 2.dp
private val SeismographLineWidth = 1.dp

private const val DASHBOARD_STRING_PREFIX = "earthquakes_"

@Composable
fun EarthquakeDashboard(viewModel: EarthquakeDashboardViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Collected from the start, so the stations connect while the earthquake history is still loading.
    val seismographs by viewModel.seismographs.collectAsStateWithLifecycle()
    val textLoaded = rememberDashboardTextLoaded()

    when (val state = uiState) {
        EarthquakeDashboardUiState.Failed ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.md, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(Res.string.earthquakes_load_failed))
                Button(onClick = viewModel::retry) {
                    Text(text = stringResource(Res.string.earthquakes_retry))
                }
            }

        is EarthquakeDashboardUiState.Ready if textLoaded ->
            EarthquakeDashboardContent(state = state, seismographs = seismographs)

        else ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
    }
}

/**
 * Loads every dashboard string behind the first spinner. On the web a string loads after the frame that first
 * asks for it, so labels would otherwise appear after their dots and spinners.
 */
@Composable
private fun rememberDashboardTextLoaded(): Boolean {
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        coroutineScope {
            Res.allStringResources
                .filterKeys { it.startsWith(DASHBOARD_STRING_PREFIX) }
                .values
                .map { async { getString(it) } }
                .awaitAll()
        }
        loaded = true
    }
    return loaded
}

@Composable
private fun EarthquakeDashboardContent(
    state: EarthquakeDashboardUiState.Ready,
    seismographs: SeismographsUiState,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns =
            when {
                maxWidth >= ThreeColumnBreakpoint -> 3
                maxWidth >= TwoColumnBreakpoint -> 2
                else -> 1
            }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.galleryPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardPadding),
        ) {
            FeedStatus(feed = state.feed)

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                SectionTitle(text = stringResource(Res.string.earthquakes_section_seismographs))
                StationStatusLegend()
            }
            CardGrid(items = seismographs.cards, columns = columns) { card, modifier ->
                SeismographCard(card = card, modifier = modifier.height(SeismographHeight))
            }

            SectionTitle(text = stringResource(Res.string.earthquakes_section_last_day))
            CardGrid(items = state.continents, columns = columns) { continent, modifier ->
                ContinentMagnitudes(continent = continent, modifier = modifier.height(LastDayChartHeight))
            }

            SectionTitle(text = stringResource(Res.string.earthquakes_section_last_month))
            // A Row, not a FlowRow: FlowRow asks charts for intrinsic sizes, which their BoxWithConstraints cannot give.
            if (columns == 1) {
                Magnitudes(
                    chart = state.magnitudes,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(MagnitudesHeight),
                )
                DailyCounts(
                    chart = state.dailyCounts,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(DailyCountsHeight),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.cardPadding),
                ) {
                    Magnitudes(
                        chart = state.magnitudes,
                        modifier =
                            Modifier
                                .weight(MAGNITUDES_WEIGHT)
                                .height(MagnitudesHeight),
                    )
                    DailyCounts(
                        chart = state.dailyCounts,
                        modifier =
                            Modifier
                                .weight(DAILY_COUNTS_WEIGHT)
                                .height(DailyCountsHeight),
                    )
                }
            }

            Attribution()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = Dimens.sm),
        style = MaterialTheme.typography.titleMedium,
    )
}

// Live seismographs move on their own, so they need no update time.
@Composable
private fun SeismographCard(
    card: SeismographCardUi,
    modifier: Modifier = Modifier,
) {
    val containerStyle = ChartContainerDefaults.style()
    val captionPadding =
        Modifier.padding(start = containerStyle.contentPadding, end = containerStyle.contentPadding)
    val subtitle = card.subtitle.resolve()

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = containerStyle.contentPadding)) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                if (card.placeholder != null) {
                    ChartPlaceholder(
                        title = card.title,
                        subtitle = subtitle,
                        message = card.placeholder.message.resolve(),
                        loading = card.placeholder.loading,
                    )
                } else {
                    LiveLineChart(
                        data = card.chart,
                        modifier = Modifier.fillMaxSize(),
                        title = card.title,
                        subtitle = subtitle,
                        style =
                            LineChartDefaults.style(
                                // A thin, straight trace between samples, as seismographs draw it; curves cost more per frame.
                                line = LineChartDefaults.line(strokeWidth = SeismographLineWidth, bezier = false),
                                // No grid, like the plain paper of a seismograph.
                                axes =
                                    AxesDefaults.style(
                                        grid = GridDefaults.style(visible = false),
                                        xLabels = AxisLabelDefaults.style(visible = false),
                                    ),
                                range =
                                    LineChartDefaults.range(
                                        min = card.range?.start,
                                        max = card.range?.endInclusive,
                                    ),
                            ),
                        shiftDuration = SeismographSlot,
                        animateOnStart = false,
                        axisValueFormatter = { it.roundToInt().toString() },
                    )
                }
            }
            Row(
                modifier = captionPadding,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    card.lastRecorded?.let { lastRecorded ->
                        Text(
                            text = lastRecorded.resolve(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    WaveArrivalLine(arrival = card.arrival)
                }
                card.status?.let { status ->
                    val label = stringResource(status.label)
                    StatusDot(status = status, modifier = Modifier.semantics { contentDescription = label })
                }
            }
        }
    }
}

@Composable
private fun WaveArrivalLine(arrival: WaveArrivalUi) {
    // An arrival still ahead of the playback stands out until the waves reach the screen.
    val color = if (arrival.upcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
    ) {
        Icon(
            imageVector = Icons.Filled.Waves,
            contentDescription = null,
            modifier = Modifier.size(ArrivalIconSize),
            tint = color,
        )
        Text(
            text = arrival.text.resolve(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A filled dot coloured by shaking, or a hollow ring when the station is unavailable. */
@Composable
private fun StatusDot(
    status: StationStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val color =
        when (status) {
            StationStatus.Quiet -> colors.outline
            StationStatus.Normal -> colors.primary
            StationStatus.Elevated -> colors.tertiary
            StationStatus.Strong -> colors.error
            StationStatus.Unavailable -> colors.error
        }
    Box(
        modifier =
            modifier
                .size(StatusDotSize)
                .then(
                    if (status == StationStatus.Unavailable) {
                        Modifier.border(width = StatusRingWidth, color = color, shape = CircleShape)
                    } else {
                        Modifier.background(color = color, shape = CircleShape)
                    },
                ),
    )
}

@Composable
private fun StationStatusLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        StationStatus.entries.forEach { status ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
            ) {
                StatusDot(status = status)
                Text(
                    text = stringResource(status.label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Keeps a card's title and subtitle in the chart's place while there is nothing to draw yet. */
@Composable
private fun ChartPlaceholder(
    title: String,
    subtitle: String,
    message: String,
    loading: Boolean = false,
) {
    val containerStyle = ChartContainerDefaults.style()
    Column(modifier = Modifier.padding(containerStyle.contentPadding)) {
        Text(text = title, style = containerStyle.styleTitle)
        Text(
            text = subtitle,
            modifier = Modifier.padding(top = Dimens.xs),
            style = containerStyle.styleSubtitle,
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Dimens.md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (loading) CircularProgressIndicator()
        }
    }
}

@Composable
private fun Attribution() {
    Text(
        text = stringResource(Res.string.earthquakes_attribution),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Magnitudes(
    chart: ChartCardUi,
    modifier: Modifier = Modifier,
) {
    LiveChartCard(updated = chart.updated, modifier = modifier) {
        HistogramChart(
            data = chart.data,
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.earthquakes_magnitudes_title),
            subtitle = stringResource(Res.string.earthquakes_magnitudes_subtitle),
            animateOnStart = false,
        )
    }
}

@Composable
private fun DailyCounts(
    chart: ChartCardUi,
    modifier: Modifier = Modifier,
) {
    LiveChartCard(updated = chart.updated, modifier = modifier) {
        StackedBarChart(
            data = chart.data,
            modifier = Modifier.fillMaxSize(),
            title = stringResource(Res.string.earthquakes_daily_counts_title),
            subtitle = stringResource(Res.string.earthquakes_daily_counts_subtitle),
            animateOnStart = false,
        )
    }
}

@Composable
private fun FeedStatus(feed: FeedStatusUi) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Icon(
                imageVector =
                    when (feed.status) {
                        ConnectionStatus.Connecting -> Icons.Filled.Sync
                        ConnectionStatus.Live -> Icons.Filled.Sensors
                        ConnectionStatus.Reconnecting -> Icons.Filled.CloudOff
                    },
                contentDescription = null,
                tint =
                    when (feed.status) {
                        ConnectionStatus.Reconnecting -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
            )
            Text(text = feed.text.resolve(), style = MaterialTheme.typography.titleSmall)
        }
        feed.latest?.let { latest ->
            Text(
                text = latest.resolve(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ContinentMagnitudes(
    continent: ContinentQuakesUi,
    modifier: Modifier = Modifier,
) {
    val quakes = continent.quakes
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selectedIndex = quakes.indexOfFirst { it.id == selectedId }.takeIf { it >= 0 }
    LaunchedEffect(quakes, selectedId) {
        if (selectedIndex == null) selectedId = null
    }
    // A new holder starts at the saved quake, so the chart's data-change reset cannot clear its identity.
    val selection =
        key(continent.chart, quakes) {
            rememberChartSelection(initialIndex = selectedIndex) { index ->
                selectedId = index?.let(quakes::getOrNull)?.id
            }
        }
    val selected = selection.selectedIndex?.let(quakes::getOrNull)
    val subtitle = continent.subtitle.resolve()

    LiveChartCard(
        updated = continent.updated,
        modifier = modifier,
        caption = selected?.caption?.resolve(),
    ) {
        if (continent.placeholder != null) {
            ChartPlaceholder(
                title = continent.title,
                subtitle = subtitle,
                message = continent.placeholder.message.resolve(),
                loading = continent.placeholder.loading,
            )
        } else {
            LineChart(
                data = continent.chart,
                modifier = Modifier.fillMaxSize(),
                title = continent.title,
                subtitle = subtitle,
                style =
                    LineChartDefaults.style(
                        line = LineChartDefaults.line(bezier = false),
                        axes = AxesDefaults.style(xLabels = AxisLabelDefaults.style(visible = false)),
                        range =
                            LineChartDefaults.range(
                                min = continent.range.start,
                                max = continent.range.endInclusive,
                            ),
                    ),
                selection = selection,
                animateOnStart = false,
            )
        }
    }
}

/** Shows when the chart last changed below it, after an optional [caption]. */
@Composable
private fun LiveChartCard(
    updated: UiText?,
    modifier: Modifier = Modifier,
    caption: String? = null,
    content: @Composable () -> Unit,
) {
    val containerStyle = ChartContainerDefaults.style()

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                content()
            }
            if (caption != null || updated != null) {
                Row(
                    modifier =
                        Modifier.padding(
                            start = containerStyle.contentPadding,
                            end = containerStyle.contentPadding,
                            bottom = containerStyle.contentPadding,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
                ) {
                    Text(
                        text = caption.orEmpty(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (updated != null) {
                        Text(
                            text = updated.resolve(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
