package io.github.hdcharts.app.demo.earthquake

import hdcharts.app.generated.resources.Res
import hdcharts.app.generated.resources.earthquakes_arrival_none
import hdcharts.app.generated.resources.earthquakes_latest
import hdcharts.app.generated.resources.earthquakes_status_connecting
import hdcharts.app.generated.resources.earthquakes_status_live
import hdcharts.app.generated.resources.earthquakes_status_reconnecting
import hdcharts.app.generated.resources.earthquakes_updated
import io.github.hdcharts.app.ui.UiText
import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import io.github.hdcharts.sampleapi.earthquake.EarthquakeConnectionStatus
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedEvent
import io.github.hdcharts.sampleapi.earthquake.EarthquakeFeedState
import io.github.hdcharts.sampleapi.earthquake.EarthquakeHistory
import io.github.hdcharts.sampleapi.earthquake.EarthquakeRepository
import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.SeismogramFeedEvent
import io.github.hdcharts.sampleapi.seismogram.SeismogramRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EarthquakeDashboardViewModelTest {
    private val now = Instant.parse("2026-10-12T12:00:00Z")
    private var clockNow = now
    private val clock =
        object : Clock {
            override fun now(): Instant = clockNow
        }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_showsFailedWhenHistoryFails_andLoadsAfterRetry() =
        runTest {
            val repository = FakeEarthquakeRepository(failHistory = true)
            val viewModel = viewModel(repository)

            assertIs<EarthquakeDashboardUiState.Failed>(viewModel.uiState.value)

            repository.failHistory = false
            viewModel.retry()
            runCurrent()

            assertIs<EarthquakeDashboardUiState.Ready>(viewModel.uiState.value)
        }

    @Test
    fun uiState_keepsEveryQuakeOfABusyDay() =
        runTest {
            // More quakes than one request returns, as in a strong aftershock sequence.
            val busyDay = List(4_500) { quake(id = "q$it", ago = (it * 15).seconds, magnitude = 3.0, now = now) }
            val viewModel = viewModel(FakeEarthquakeRepository(history = busyDay))

            val state = assertIs<EarthquakeDashboardUiState.Ready>(viewModel.uiState.value)
            assertEquals(
                4_500,
                state.continent(Continent.Asia).quakes.size,
            )
        }

    @Test
    fun uiState_mapsANewSnapshotToItsContinent() =
        runTest {
            val repository =
                FakeEarthquakeRepository(
                    history = listOf(asia("a", 20), asia("b", 10)),
                )
            val viewModel = viewModel(repository)

            repository.states.value =
                EarthquakeFeedState.Ready(
                    quakes = listOf(asia("a", 20), asia("b", 10), asia("pushed", 1, magnitude = 5.5)),
                    connection = EarthquakeConnectionStatus.Live,
                )
            runCurrent()

            val state = assertIs<EarthquakeDashboardUiState.Ready>(viewModel.uiState.value)
            assertEquals(
                UiText(resource = Res.string.earthquakes_latest, args = listOf("5.5", "REGION pushed", "11:59")),
                state.feed.latest,
            )
            assertEquals(
                listOf(3.0, 3.0, 5.5),
                state.continent(Continent.Asia).magnitudes(),
            )
        }

    @Test
    fun uiState_showsOnlyTheLastDayOnContinentLines() =
        runTest {
            val repository =
                FakeEarthquakeRepository(
                    history = listOf(asia("yesterday", 25 * 60), asia("a", 20), asia("b", 10)),
                )
            val viewModel = viewModel(repository)

            assertEquals(
                2,
                viewModel
                    .readyState()
                    .continent(Continent.Asia)
                    .magnitudes()
                    .size,
            )
        }

    @Test
    fun uiState_expiresQuakesAndArrivalsWithoutFetchingMoreHistory() =
        runTest {
            val repository =
                FakeEarthquakeRepository(
                    history =
                        listOf(
                            asia("expiring", 24 * 60 - 1, magnitude = 4.5),
                            asia("arrival", 59, magnitude = 6.0),
                        ),
                )
            val viewModel = viewModel(repository)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.seismographs.collect {} }
            runCurrent()
            val requests = repository.requests.toList()
            val noArrival = UiText(Res.string.earthquakes_arrival_none)
            assertEquals(
                2,
                viewModel
                    .readyState()
                    .continent(Continent.Asia)
                    .quakes.size,
            )
            assertNotEquals(
                noArrival,
                viewModel.seismographs.value.cards
                    .first()
                    .arrival.text,
            )

            clockNow += 2.minutes
            advanceTimeBy(2.minutes)
            runCurrent()

            assertEquals(
                listOf("arrival"),
                viewModel
                    .readyState()
                    .continent(Continent.Asia)
                    .quakes
                    .map { it.id },
            )
            assertEquals(
                1.0,
                viewModel
                    .readyState()
                    .magnitudes.data.series
                    .single()
                    .values
                    .sum(),
            )
            assertEquals(
                noArrival,
                viewModel.seismographs.value.cards
                    .first()
                    .arrival.text,
            )
            assertEquals(requests, repository.requests)
        }

    @Test
    fun uiState_advancesDailyCountsAtUtcMidnightWithoutFeedEvents() =
        runTest {
            clockNow = Instant.parse("2026-10-12T23:59:30Z")
            val repository =
                FakeEarthquakeRepository(
                    history = listOf(quake("a", ago = 1.hours, magnitude = 5.0, now = clockNow)),
                )
            val viewModel = viewModel(repository)
            val requests = repository.requests.toList()
            assertEquals(
                "Oct 12",
                viewModel
                    .readyState()
                    .dailyCounts.data.categories
                    .last(),
            )
            assertEquals(
                1.0,
                viewModel
                    .readyState()
                    .dailyCounts.data.series
                    .first()
                    .values
                    .last(),
            )

            clockNow += 30.seconds
            advanceTimeBy(30.seconds)
            runCurrent()

            val daily = viewModel.readyState().dailyCounts.data
            assertEquals("Oct 13", daily.categories.last())
            assertEquals(
                listOf(1.0, 0.0),
                daily.series
                    .first()
                    .values
                    .takeLast(2),
            )
            assertEquals(requests, repository.requests)
        }

    @Test
    fun uiState_mapsARevisedSnapshot() =
        runTest {
            val repository = FakeEarthquakeRepository(history = listOf(asia("a", 20), asia("b", 10)))
            val viewModel = viewModel(repository)

            repository.states.value =
                EarthquakeFeedState.Ready(
                    quakes = listOf(asia("a", 20), asia("b", 10, magnitude = 4.2)),
                    connection = EarthquakeConnectionStatus.Live,
                )
            runCurrent()

            val state = assertIs<EarthquakeDashboardUiState.Ready>(viewModel.uiState.value)
            assertEquals(
                listOf(3.0, 4.2),
                state.continent(Continent.Asia).magnitudes(),
            )
        }

    @Test
    fun uiState_movesTheUpdateTimeOnlyForChartsWhoseDataChanged() =
        runTest {
            val repository = FakeEarthquakeRepository(history = listOf(asia("a", 20), asia("b", 10)))
            val viewModel = viewModel(repository)

            clockNow = now + 5.minutes
            repository.states.value =
                EarthquakeFeedState.Ready(
                    quakes = listOf(asia("a", 20), asia("b", 10), asia("pushed", 0, magnitude = 3.0)),
                    connection = EarthquakeConnectionStatus.Live,
                )
            runCurrent()

            val state = assertIs<EarthquakeDashboardUiState.Ready>(viewModel.uiState.value)
            assertEquals(updatedText(clockNow), state.continent(Continent.Asia).updated)
            assertEquals(updatedText(now), state.continent(Continent.Europe).updated)
        }

    @Test
    fun uiState_followsTheConnection() =
        runTest {
            val repository = FakeEarthquakeRepository()
            val viewModel = viewModel(repository)

            assertEquals(UiText(Res.string.earthquakes_status_connecting), viewModel.readyConnection())

            repository.states.value = EarthquakeFeedState.Ready(emptyList(), EarthquakeConnectionStatus.Live)
            runCurrent()
            assertEquals(UiText(Res.string.earthquakes_status_live), viewModel.readyConnection())

            repository.states.value = EarthquakeFeedState.Ready(emptyList(), EarthquakeConnectionStatus.Reconnecting)
            runCurrent()
            assertEquals(ReconnectingText, viewModel.readyConnection())
        }

    @Test
    fun uiState_showsConnectingWhileTheReconnectRefreshRuns() =
        runTest {
            val repository = FakeEarthquakeRepository()
            val viewModel = viewModel(repository)
            repository.states.value = EarthquakeFeedState.Ready(emptyList(), EarthquakeConnectionStatus.Reconnecting)
            repository.states.value = EarthquakeFeedState.Ready(emptyList(), EarthquakeConnectionStatus.Connecting)
            runCurrent()

            assertEquals(UiText(Res.string.earthquakes_status_connecting), viewModel.readyConnection())
        }

    @Test
    fun uiState_expiresTheLastRetainedQuakeWithoutRepositoryUpdates() =
        runTest {
            val repository =
                FakeEarthquakeRepository(
                    history = listOf(quake("expiring", ago = 30.days - 1.minutes, magnitude = 5.0, now = now)),
                )
            val viewModel = viewModel(repository)
            assertNotEquals(null, viewModel.readyState().feed.latest)
            val requests = repository.requests.toList()

            clockNow += 2.minutes
            advanceTimeBy(2.minutes)
            runCurrent()

            assertNull(viewModel.readyState().feed.latest)
            assertEquals(requests, repository.requests)
        }

    @Test
    fun retry_cancelsThePreviousRepositoryObservation() =
        runTest {
            val repository = FakeEarthquakeRepository()
            val viewModel = viewModel(repository)
            assertEquals(1, repository.activeCollectors)

            viewModel.retry()
            runCurrent()

            assertEquals(2, repository.requests.size)
            assertEquals(1, repository.activeCollectors)
        }

    @Test
    fun subscriptions_shareAndStopTheRepositoryObservation() =
        runTest {
            val repository = FakeEarthquakeRepository()
            val viewModel = EarthquakeDashboardViewModel(repository, NoSeismograms, clock, emptyFlow())
            val ui = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
            val seismic =
                backgroundScope.launch(
                    UnconfinedTestDispatcher(testScheduler),
                ) { viewModel.seismographs.collect {} }
            runCurrent()
            assertEquals(1, repository.requests.size)
            assertEquals(1, repository.activeCollectors)

            ui.cancel()
            seismic.cancel()
            advanceTimeBy(10.seconds)
            runCurrent()
            assertEquals(0, repository.activeCollectors)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
            runCurrent()
            assertEquals(2, repository.requests.size)
            assertEquals(1, repository.activeCollectors)
        }

    private fun TestScope.viewModel(repository: FakeEarthquakeRepository): EarthquakeDashboardViewModel {
        val viewModel =
            EarthquakeDashboardViewModel(
                repository = repository,
                seismograms = NoSeismograms,
                clock = clock,
                ticks = emptyFlow(),
            )
        // stateIn only runs while someone collects.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        runCurrent()
        return viewModel
    }

    private fun EarthquakeDashboardViewModel.readyState(): EarthquakeDashboardUiState.Ready =
        assertIs<EarthquakeDashboardUiState.Ready>(uiState.value)

    private fun EarthquakeDashboardViewModel.readyConnection(): UiText = readyState().feed.text

    private fun asia(
        id: String,
        minutesAgo: Int,
        magnitude: Double = 3.0,
    ): Earthquake = quake(id = id, ago = minutesAgo.minutes, magnitude = magnitude, now = now)
}

private fun EarthquakeDashboardUiState.Ready.continent(continent: Continent): ContinentQuakesUi =
    continents.single { it.title == continent.displayName }

private fun ContinentQuakesUi.magnitudes(): List<Double> = chart.series.single().values

private fun updatedText(time: Instant) =
    UiText(resource = Res.string.earthquakes_updated, args = listOf(time.utcClockLabel()))

private val ReconnectingText = UiText(Res.string.earthquakes_status_reconnecting)

private object NoSeismograms : SeismogramRepository {
    override fun live(
        stations: List<SeismicStation>,
        since: Instant,
    ): Flow<SeismogramFeedEvent> = emptyFlow()
}

private class FakeEarthquakeRepository(
    var failHistory: Boolean = false,
    history: List<Earthquake> = emptyList(),
) : EarthquakeRepository {
    val states =
        MutableStateFlow<EarthquakeFeedState>(EarthquakeFeedState.Ready(history, EarthquakeConnectionStatus.Connecting))
    val requests = mutableListOf<List<EarthquakeHistory>>()
    var activeCollectors = 0

    override fun observe(
        history: List<EarthquakeHistory>,
        clock: Clock,
    ): Flow<EarthquakeFeedState> =
        flow {
            requests += history
            emit(EarthquakeFeedState.Loading)
            if (failHistory) {
                emit(EarthquakeFeedState.Failed)
            } else {
                activeCollectors++
                try {
                    emitAll(states)
                } finally {
                    activeCollectors--
                }
            }
        }

    override suspend fun recent(
        limit: Int,
        since: Instant?,
        minMagnitude: Double?,
    ): List<Earthquake> = error("The view model should observe repository snapshots")

    override fun live(): Flow<EarthquakeFeedEvent> = error("The view model should observe repository snapshots")
}
