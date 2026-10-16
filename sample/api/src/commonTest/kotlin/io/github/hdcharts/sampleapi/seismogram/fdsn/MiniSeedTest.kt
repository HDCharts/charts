package io.github.hdcharts.sampleapi.seismogram.fdsn

import io.github.hdcharts.sampleapi.http.sampleHttpClient
import io.github.hdcharts.sampleapi.seismogram.SeismicStation
import io.github.hdcharts.sampleapi.seismogram.fdsn.MiniSeedFixtures.ANMO_RECORD
import io.github.hdcharts.sampleapi.seismogram.fdsn.MiniSeedFixtures.STATION_TEXT
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class MiniSeedTest {
    @Test
    fun decodeMiniSeed_readsARealSteim2Record() {
        val motion = decodeMiniSeed(ANMO_RECORD.hexToByteArray()).single()

        assertEquals(Instant.parse("2026-10-12T16:45:19.0695Z"), motion.start)
        assertEquals(1.0, motion.samplesPerSecond)
        assertEquals(260, motion.samples.size)
        assertEquals(listOf(244, 567, 511, 260, 150, 218), motion.samples.take(6))
        // The record stores its last sample too, so a wrong difference anywhere would show here.
        assertEquals(426, motion.samples.last())
    }

    @Test
    fun decodeMiniSeed_readsEveryRecordInAResponse() {
        val twoRecords = (ANMO_RECORD + ANMO_RECORD).hexToByteArray()

        assertEquals(2, decodeMiniSeed(twoRecords).size)
    }

    @Test
    fun decodeMiniSeed_returnsNothingForAnEmptyResponse() {
        assertTrue(decodeMiniSeed(ByteArray(0)).isEmpty())
    }

    @Test
    fun decodeMiniSeed_stopsAtAnImpossibleRecordLength() {
        // Blockette 1000 stores the record length as a power of two; 2^31 does not fit a record.
        val record = ANMO_RECORD.hexToByteArray().also { it[54] = 31 }

        assertTrue(decodeMiniSeed(record).isEmpty())
    }

    @Test
    fun decodeMiniSeed_stopsAtABlocketteThatPointsBackToItself() {
        // The header and blockette 1001 both point at blockette 1001, so blockette 1000 is never reached.
        val record = ANMO_RECORD.hexToByteArray().also { it[47] = 56 }.also { it[59] = 56 }

        assertTrue(decodeMiniSeed(record).isEmpty())
    }

    @Test
    fun velocitySensitivity_readsTheScaleOfAVelocityChannel() {
        assertEquals(1.97536E9, velocitySensitivity(STATION_TEXT))
    }

    @Test
    fun velocitySensitivity_isNullWithoutAVelocityChannel() {
        assertEquals(null, velocitySensitivity(STATION_TEXT.replace("|m/s|", "|M/S**2|")))
        assertEquals(null, velocitySensitivity(""))
    }

    @Test
    fun sensitivity_requestsTheChannelMetadata() =
        runTest {
            val requests = mutableListOf<HttpRequestData>()
            val repository = repositoryRespondingWith(requests = requests) { respond(content = STATION_TEXT) }

            val station = SeismicStation(network = "IU", code = "ANMO", location = "00", channel = "BHZ")
            assertEquals(1.97536E9, repository.sensitivity(station))
            val request = requests.single()
            assertEquals("/fdsnws/station/1/query", request.url.encodedPath)
            assertEquals("channel", request.url.parameters["level"])
            assertEquals("text", request.url.parameters["format"])
        }

    private fun repositoryRespondingWith(
        requests: MutableList<HttpRequestData> = mutableListOf(),
        respond:
            suspend io.ktor.client.engine.mock.MockRequestHandleScope.() -> io.ktor.client.request.HttpResponseData,
    ): FdsnStationApi {
        val engine =
            MockEngine { request ->
                requests += request
                respond()
            }
        return FdsnStationApi(sampleHttpClient(engine))
    }
}

internal object MiniSeedFixtures {
    // Trimmed from a real station service response.
    const val STATION_TEXT =
        "#Network | Station | Location | Channel | Latitude | Longitude | Elevation | Depth | Azimuth | Dip | " +
            "SensorDescription | Scale | ScaleFreq | ScaleUnits | SampleRate | StartTime | EndTime\n" +
            "IU|ANMO|00|LHZ|34.94591|-106.4572|1632.7|188.0|0.0|-90.0|Streckeisen STS-6A VBB Seismometer|" +
            "1.97536E9|0.02|m/s|1.0|2018-07-09T20:45:00.0000|\n"

    // One 512-byte LHZ record from IU.ANMO (Albuquerque), fetched from EarthScope on 2026-10-12.
    const val ANMO_RECORD =
        "3236333436394420414e4d4f2030304c485a495507ea011d102d130002b7010400010001002000020000000000400030" +
            "03e800380b01090003e9000064274607029aaaaa000000f4000001aaca050fc8f05e48446bd8d516c0728c93f89cbf6e" +
            "c1c15cc4cf4a5e67dde71243bef8805dd372b098c36abe69805a82358063fe812aaaaaaabeedff8ade567f62eb8f0125" +
            "c438d3d9df031eb5e9c26d60c6ab8e5e802d021ed24bf25bf210f8e1c311c117ff0aea4c800a027580b3ff4dbeeafe48" +
            "2aaaaaaad056e113bff1fdc2bf390130811500eff06a807ad01b16d6cd11f401c680c7c4f75fd8e8ff791fe4810a0133" +
            "bfa5fddabf4000de8115012ced09737f2aaaaaaa808d826180017dd3e76d4934814e8204bf59fb9dbf21823b818d01b3" +
            "bf0a7d77ca54a076c14a3e4cbff782528136ff66e41d8f6dfa735949cafdf2fc2aaaaaaaf59f3057c0f31d97ecb81d5d" +
            "d66a12f7ccfd6b86817981ddbee7fd37bf988191811980aabf757dbebf5a810281220287bfe47bd6beac81ed81aa81f3" +
            "2aaaaaaabfbc7cd8be6280288187828380767f12be63fe40da97a485edfb80a9d0afc2cffc4608edbf57fd72bfd48319" +
            "81697f19be91fdd6bfd90202d8e2a9222aaaaaaabf7b7db8f890bc3ad1456feee618402781900199bf2f7d75bf660236" +
            "81087f4af4cd4795d021276bc6604aaef7b579a0c65c0ac8f8efd372bff101f7"
}
