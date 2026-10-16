package io.github.hdcharts.sampleapi.seismogram

import io.github.hdcharts.sampleapi.ApiUnavailableException
import io.github.hdcharts.sampleapi.http.networkCall
import io.github.hdcharts.sampleapi.http.reconnectDelay
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal class EarthScopeSeismogramRepository(
    private val sensitivity: suspend (SeismicStation) -> Double?,
    private val listen: suspend (
        SeismicStation,
        Instant,
        suspend () -> Unit,
        suspend (GroundMotion) -> Unit,
    ) -> Unit,
) : SeismogramRepository {
    override fun live(
        stations: List<SeismicStation>,
        since: Instant,
    ): Flow<SeismogramFeedEvent> =
        channelFlow {
            // Each station has its own connection, so its data shows as soon as it arrives, whatever the others do.
            stations.forEach { station -> launch { stream(station, since) } }
        }

    private suspend fun ProducerScope<SeismogramFeedEvent>.stream(
        station: SeismicStation,
        since: Instant,
    ) = coroutineScope {
        // Loaded beside the connection; records wait for it instead of being dropped.
        val scale = async { loadScale(station) }
        var cursor = since
        var failedAttempts = 0
        while (isActive) {
            try {
                networkCall {
                    listen(
                        station,
                        cursor,
                        { send(SeismogramFeedEvent.Unavailable(station)) },
                    ) { motion ->
                        failedAttempts = 0
                        send(SeismogramFeedEvent.Recorded(station, motion, scale.await()))
                        cursor = maxOf(cursor, motion.end)
                    }
                }
            } catch (e: ApiUnavailableException) {
                // Retry a dropped connection and resume from the last record.
            }
            send(SeismogramFeedEvent.Unavailable(station))
            delay(reconnectDelay(failedAttempts++))
        }
    }

    private suspend fun ProducerScope<SeismogramFeedEvent>.loadScale(station: SeismicStation): Double {
        while (true) {
            val scale =
                try {
                    networkCall { sensitivity(station) }
                } catch (e: ApiUnavailableException) {
                    null
                }
            if (scale != null) return scale
            send(SeismogramFeedEvent.Unavailable(station))
            delay(METADATA_RETRY_DELAY)
        }
    }

    private companion object {
        val METADATA_RETRY_DELAY = 10.seconds
    }
}
