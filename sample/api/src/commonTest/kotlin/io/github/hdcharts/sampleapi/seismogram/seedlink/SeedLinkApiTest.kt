package io.github.hdcharts.sampleapi.seismogram.seedlink

import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.fdsn.MiniSeedFixtures.ANMO_RECORD
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SeedLinkApiTest {
    @Test
    fun decodeSeedLinkPacket_routesARealRecordToItsExactStationChannel() {
        val packet = "SL000123".encodeToByteArray() + ANMO_RECORD.hexToByteArray()

        val (station, motion) = decodeSeedLinkPacket(packet)!!

        assertEquals(SeismicStation("IU", "ANMO", location = "00", channel = "LHZ"), station)
        assertEquals(260, motion.single().samples.size)
    }

    @Test
    fun decodeSeedLinkPacket_skipsUnreadablePackets() {
        assertNull(decodeSeedLinkPacket("ERROR".encodeToByteArray()))
        assertNull(decodeSeedLinkPacket(ByteArray(520)))
    }

    @Test
    fun subscribe_stopsAtARejectedCommandAndAcceptsAnotherStation() =
        runTest {
            val anmo = SeismicStation("IU", "ANMO", location = "00", channel = "BHZ")
            val lco = SeismicStation("IU", "LCO", location = "10", channel = "BHZ")
            val since = Instant.parse("2026-10-12T12:34:56Z")
            val sent = mutableListOf<String>()

            val rejected =
                subscribe(station = lco, since = since) { command ->
                    sent += command
                    command != "STATION LCO IU"
                }
            val accepted =
                subscribe(station = anmo, since = since) { command ->
                    sent += command
                    true
                }

            assertEquals(false, rejected)
            assertEquals(true, accepted)
            assertEquals(
                listOf("STATION LCO IU", "STATION ANMO IU", "SELECT 00BHZ.D", "TIME 2026,10,12,12,34,56"),
                sent,
            )
        }

    @Test
    fun seedLinkTime_formatsTheResumeTimeForTheProtocol() {
        assertEquals("2026,10,12,12,34,56", seedLinkTime(Instant.parse("2026-10-12T12:34:56.125Z")))
    }
}
