package io.github.hdcharts.sampleapi.seismogram.seedlink

import io.github.hdcharts.sampleapi.seismogram.GroundMotion
import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.fdsn.decodeMiniSeed
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Instant

/** SeedLink v3 over WebSocket: one station per connection, resuming from its recorded time. */
internal class SeedLinkApi(
    private val client: HttpClient,
) {
    suspend fun listen(
        station: SeismicStation,
        since: Instant,
        onRejected: suspend () -> Unit,
        onRecord: suspend (GroundMotion) -> Unit,
    ) = withContext(Dispatchers.Default) {
        client.webSocket(LIVE_URL) {
            val accepted = subscribe(station = station, since = since) { command(it) }
            if (!accepted) onRejected()
            check(accepted) { "SeedLink rejected every station" }
            send(Frame.Text("END\r\n"))
            while (true) {
                val frame =
                    withTimeoutOrNull(60_000L) { incoming.receive() }
                        ?: error("SeedLink stopped sending recordings")
                if (frame !is Frame.Binary) continue
                val (recorded, motion) = decodeSeedLinkPacket(frame.data) ?: continue
                if (recorded != station) continue
                motion.forEach { onRecord(it) }
            }
        }
    }

    companion object {
        private const val LIVE_URL = "wss://rtserve.earthscope.org/seedlink"
    }
}

/** True when the server accepts [command]; a missing reply still fails the connection. */
private suspend fun DefaultClientWebSocketSession.command(command: String): Boolean {
    send(Frame.Text("$command\r\n"))
    val reply =
        withTimeoutOrNull(15_000L) { incoming.receive() }
            ?: error("SeedLink did not acknowledge $command")
    return reply.data.decodeToString().trim() == "OK"
}

internal fun seedLinkTime(time: Instant): String =
    time
        .toString()
        .take(19)
        .replace('-', ',')
        .replace('T', ',')
        .replace(':', ',')

/** Sends the station's commands, stopping at the first rejection. */
internal suspend fun subscribe(
    station: SeismicStation,
    since: Instant,
    command: suspend (String) -> Boolean,
): Boolean =
    command("STATION ${station.code} ${station.network}") &&
        command("SELECT ${station.location}${station.channel}.D") &&
        command("TIME ${seedLinkTime(since)}")

/** Null for a frame that is not a SeedLink data packet, so the connection keeps reading. */
internal fun decodeSeedLinkPacket(packet: ByteArray): Pair<SeismicStation, List<GroundMotion>>? {
    if (packet.size < 8 + 48 || packet[0] != 'S'.code.toByte() || packet[1] != 'L'.code.toByte()) return null
    val record = packet.copyOfRange(8, packet.size)

    fun field(
        start: Int,
        end: Int,
    ) = record.copyOfRange(start, end).decodeToString().trim()
    val station =
        SeismicStation(network = field(18, 20), code = field(8, 13), location = field(13, 15), channel = field(15, 18))
    return station to decodeMiniSeed(record)
}
