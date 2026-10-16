package io.github.hdcharts.sampleapi.earthquake.emsc

import io.github.hdcharts.sampleapi.http.SampleJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlin.time.Instant

internal interface EmscApi {
    suspend fun events(
        limit: Int,
        since: Instant?,
        minMagnitude: Double?,
        offset: Int = 0,
    ): List<EmscFeatureDto>

    suspend fun listen(
        onOpen: suspend () -> Unit,
        onMessage: suspend (EmscLiveMessageDto) -> Unit,
    )
}

/**
 * Remote data source for the EMSC seismic portal: an FDSN query endpoint for history and a
 * WebSocket that pushes each event as it is reported. Neither needs an API key.
 */
internal class EmscEarthquakeApi(
    private val client: HttpClient,
) : EmscApi {
    override suspend fun events(
        limit: Int,
        since: Instant?,
        minMagnitude: Double?,
        offset: Int,
    ): List<EmscFeatureDto> =
        withContext(Dispatchers.Default) {
            val response =
                client.get(QUERY_URL) {
                    parameter("format", "json")
                    parameter("orderby", "time")
                    parameter("limit", limit)
                    since?.let { parameter("starttime", it.toString()) }
                    minMagnitude?.let { parameter("minmag", it) }
                    // FDSN counts the first result as 1.
                    if (offset > 0) parameter("offset", offset + 1)
                    timeout { requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS }
                }
            // FDSN answers an empty match with 204 and no body.
            if (response.status == HttpStatusCode.NoContent) return@withContext emptyList()
            SampleJson.decodeFromString<EmscFeatureCollectionDto>(response.bodyAsText()).features
        }

    /** Suspends while the socket is open and returns when the server closes it. */
    override suspend fun listen(
        onOpen: suspend () -> Unit,
        onMessage: suspend (EmscLiveMessageDto) -> Unit,
    ) = withContext(Dispatchers.Default) {
        client.webSocket(LIVE_URL) {
            onOpen()
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                parseLiveMessage(frame.readText())?.let { onMessage(it) }
            }
        }
    }

    companion object {
        private const val QUERY_URL = "https://www.seismicportal.eu/fdsnws/event/1/query"
        private const val LIVE_URL = "wss://www.seismicportal.eu/standing_order/websocket"
        private const val REQUEST_TIMEOUT_MILLIS = 15_000L
    }
}

/** Null for a message that does not match the documented shape, so one bad message keeps the socket open. */
internal fun parseLiveMessage(text: String): EmscLiveMessageDto? =
    try {
        SampleJson.decodeFromString<EmscLiveMessageDto>(text)
    } catch (e: SerializationException) {
        null
    }
