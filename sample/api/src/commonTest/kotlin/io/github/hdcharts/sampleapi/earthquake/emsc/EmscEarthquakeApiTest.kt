package io.github.hdcharts.sampleapi.earthquake.emsc

import io.github.hdcharts.sampleapi.ApiUnavailableException
import io.github.hdcharts.sampleapi.earthquake.Continent
import io.github.hdcharts.sampleapi.earthquake.Earthquake
import io.github.hdcharts.sampleapi.http.sampleHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class EmscEarthquakeApiTest {
    @Test
    fun recent_mapsEachReportedEventToAnEarthquake() =
        runTest {
            val repository = repositoryRespondingWith(content = TWO_EVENTS_JSON)

            val earthquakes = repository.recent(limit = 2)

            assertEquals(
                expected =
                    listOf(
                        Earthquake(
                            id = "20261012_0000194",
                            time = Instant.parse("2026-10-12T10:50:07.0Z"),
                            magnitude = 2.9,
                            region = "ANTOFAGASTA, CHILE",
                            continent = Continent.SouthAmerica,
                            latitude = -24.01,
                            longitude = -67.47,
                        ),
                        Earthquake(
                            id = "20261012_0000193",
                            time = Instant.parse("2026-10-12T10:40:48.78Z"),
                            magnitude = 2.2,
                            region = "ISLAND OF HAWAII, HAWAII",
                            continent = Continent.NorthAmerica,
                            latitude = 19.2972,
                            longitude = -155.3737,
                        ),
                    ),
                actual = earthquakes,
            )
        }

    @Test
    fun recent_sendsTheFilterAsQueryParameters() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val repository = repositoryRespondingWith(content = TWO_EVENTS_JSON, requests = requests)

            repository.recent(
                limit = 1500,
                since = Instant.parse("2026-09-12T00:00:00Z"),
                minMagnitude = 4.0,
            )

            val parameters = requests.single().url.parameters
            assertEquals("json", parameters["format"])
            assertEquals("1500", parameters["limit"])
            assertEquals("2026-09-12T00:00:00Z", parameters["starttime"])
            assertEquals("4.0", parameters["minmag"])
        }

    @Test
    fun recent_pagesByTheEventsReadIncludingThoseWithoutMagnitude() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val engine =
                MockEngine { request ->
                    requests += request
                    // A full first page with one event that has no magnitude yet, then the last event.
                    val content =
                        when (request.url.parameters["offset"]) {
                            null -> featuresJson(List(1_999) { MAGNITUDE_PROPERTIES } + NO_MAGNITUDE_PROPERTIES)
                            else -> featuresJson(listOf(MAGNITUDE_PROPERTIES))
                        }
                    respond(content = content, headers = headersOf(HttpHeaders.ContentType, "application/json"))
                }
            val repository = EmscEarthquakeRepository(EmscEarthquakeApi(sampleHttpClient(engine)))

            val earthquakes = repository.recent(limit = 20_000)

            assertEquals(2_000, earthquakes.size)
            // FDSN counts the first result as 1.
            assertEquals("2001", requests[1].url.parameters["offset"])
        }

    @Test
    fun recent_returnsNothingWhenNoEventMatches() =
        runTest {
            val repository = repositoryRespondingWith(content = "", status = HttpStatusCode.NoContent)

            assertTrue(repository.recent(limit = 10).isEmpty())
        }

    @Test
    fun recent_failsOnServerError() =
        runTest {
            val repository = repositoryRespondingWith(content = "", status = HttpStatusCode.ServiceUnavailable)

            val failure = assertFailsWith<ApiUnavailableException> { repository.recent(limit = 10) }
            assertIs<ServerResponseException>(failure.cause)
        }

    @Test
    fun recent_failsWithAnExceptionWhenTheBrowserCannotReachTheServer() =
        runTest {
            // Ktor's browser engine reports an offline fetch as an Error, not an Exception.
            val engine = MockEngine { throw Error("Fail to fetch") }
            val repository = EmscEarthquakeRepository(EmscEarthquakeApi(sampleHttpClient(engine)))

            assertFailsWith<ApiUnavailableException> { repository.recent(limit = 10) }
        }

    @Test
    fun recent_skipsEventsWithoutMagnitude() =
        runTest {
            val repository = repositoryRespondingWith(content = NO_MAGNITUDE_JSON)

            assertTrue(repository.recent(limit = 10).isEmpty())
        }

    @Test
    fun parseLiveMessage_readsTheActionAndEvent() {
        val message = parseLiveMessage(LIVE_UPDATE_JSON)

        assertEquals("update", message?.action)
        assertEquals("POLAND", message?.data?.properties?.region)
    }

    @Test
    fun parseLiveMessage_returnsNullForAnUnexpectedMessage() {
        assertNull(parseLiveMessage("""{"status":"ok"}"""))
    }

    private fun repositoryRespondingWith(
        content: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        requests: MutableList<HttpRequestData> = mutableListOf(),
    ): EmscEarthquakeRepository {
        val engine =
            MockEngine { request ->
                requests += request
                respond(
                    content = content,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        return EmscEarthquakeRepository(EmscEarthquakeApi(sampleHttpClient(engine)))
    }

    private companion object {
        // Trimmed from a real query response.
        const val TWO_EVENTS_JSON =
            """{"type":"FeatureCollection","metadata":{"count":2},"features":[
            {"type":"Feature","geometry":{"type":"Point","coordinates":[-67.47,-24.01,-219.1]},
            "id":"20261012_0000194","properties":{"source_id":"2073448","source_catalog":"EMSC-RTS",
            "lastupdate":"2026-10-12T10:54:30.654459Z","time":"2026-10-12T10:50:07.0Z",
            "flynn_region":"ANTOFAGASTA, CHILE","lat":-24.01,"lon":-67.47,"depth":219.1,"evtype":"ke",
            "auth":"CSN","mag":2.9,"magtype":"ml","unid":"20261012_0000194"}},
            {"type":"Feature","geometry":{"type":"Point","coordinates":[-155.3737,19.2972,-29.5]},
            "id":"20261012_0000193","properties":{"source_id":"2073446","source_catalog":"EMSC-RTS",
            "lastupdate":"2026-10-12T10:44:27.462577Z","time":"2026-10-12T10:40:48.78Z",
            "flynn_region":"ISLAND OF HAWAII, HAWAII","lat":19.2972,"lon":-155.3737,"depth":29.5,
            "evtype":"ke","auth":"HV","mag":2.2,"magtype":"md","unid":"20261012_0000193"}}]}"""

        const val NO_MAGNITUDE_JSON =
            """{"type":"FeatureCollection","features":[{"type":"Feature","properties":{
            "time":"2026-10-12T10:50:07.0Z","flynn_region":"ANTOFAGASTA, CHILE","lat":-24.01,
            "lon":-67.47,"unid":"20261012_0000194"}}]}"""

        const val MAGNITUDE_PROPERTIES =
            """{"time":"2026-10-12T10:50:07.0Z","lat":-24.01,"lon":-67.47,"mag":2.9,"unid":"20261012_0000194"}"""

        const val NO_MAGNITUDE_PROPERTIES =
            """{"time":"2026-10-12T10:50:07.0Z","lat":-24.01,"lon":-67.47,"unid":"20261012_0000195"}"""

        fun featuresJson(properties: List<String>): String =
            properties.joinToString(prefix = """{"features":[""", postfix = "]}") { """{"properties":$it}""" }

        // Shape of a message received from the live WebSocket.
        const val LIVE_UPDATE_JSON =
            """{"action":"update","data":{"type":"Feature","geometry":{"type":"Point",
            "coordinates":[19.3557,50.0992,-0.0]},"id":"20261012_0000180","properties":{
            "time":"2026-10-12T09:48:17.39Z","flynn_region":"POLAND","lat":50.0992,"lon":19.3557,
            "mag":2.4,"magtype":"ml","unid":"20261012_0000180"}}}"""
    }
}
