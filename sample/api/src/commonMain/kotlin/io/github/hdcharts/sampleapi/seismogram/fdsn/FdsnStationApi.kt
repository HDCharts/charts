package io.github.hdcharts.sampleapi.seismogram.fdsn

import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/** Velocity sensitivity published by the station's operator, in sensor counts per m/s. */
internal class FdsnStationApi(
    private val client: HttpClient,
) {
    suspend fun sensitivity(station: SeismicStation): Double? =
        withContext(Dispatchers.Default) {
            velocitySensitivity(
                client
                    .get(STATION_URL) {
                        parameter("net", station.network)
                        parameter("sta", station.code)
                        parameter("loc", station.location)
                        parameter("cha", station.channel)
                        parameter("level", "channel")
                        parameter("format", "text")
                        parameter("endafter", Clock.System.now().toString())
                        timeout { requestTimeoutMillis = 15_000L }
                    }.bodyAsText(),
            )
        }

    companion object {
        private const val STATION_URL = "https://service.earthscope.org/fdsnws/station/1/query"
    }
}

private const val SCALE_COLUMN = 11
private const val SCALE_UNITS_COLUMN = 13

internal fun velocitySensitivity(channelText: String): Double? =
    channelText
        .lineSequence()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .map { it.split("|") }
        .lastOrNull { it.getOrNull(SCALE_UNITS_COLUMN).equals("m/s", ignoreCase = true) }
        ?.getOrNull(SCALE_COLUMN)
        ?.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it > 0 }
