package io.github.hdcharts.core.internal.telemetry

import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogBeforeSend
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext
import com.sun.net.httpserver.HttpServer
import io.github.hdcharts.core.ChartsTelemetry
import java.io.File
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Exercises the published SDK's real persistence and networking against a loopback server. */
class PostHogSdkTest {
    private val requests = LinkedBlockingQueue<Request>()
    private lateinit var server: HttpServer
    private lateinit var storage: File
    private var previousHome: String? = null
    private val platform = TelemetryPlatform("jvm")

    @BeforeTest
    fun setup() {
        previousHome = System.getProperty("user.home")
        storage = Files.createTempDirectory("hdcharts-posthog-test").toFile()
        System.setProperty("user.home", storage.absolutePath)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val input =
                if (exchange.requestHeaders.getFirst("Content-Encoding") == "gzip") {
                    GZIPInputStream(exchange.requestBody)
                } else {
                    exchange.requestBody
                }
            requests.add(Request(exchange.requestURI.path, input.bufferedReader().use { it.readText() }))
            val response = """{"status":1,"flags":{},"featureFlags":{}}""".toByteArray()
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
        server.start()
    }

    @AfterTest
    fun teardown() {
        PostHog.close()
        server.stop(0)
        if (previousHome == null) System.clearProperty("user.home") else System.setProperty("user.home", previousHome)
        storage.deleteRecursively()
    }

    @Test
    fun sdkOptOutStopsNewCapturesButPreviouslyQueuedEventsCanStillBeDelivered() {
        PostHog.setup(
            loopbackConfig(flushAt = 1000).copy(
                beforeSend = listOf(PostHogBeforeSend { filterTelemetryEvent(it, platform, enabled = true) }),
            ),
            PostHogContext(),
        )
        PostHog.capture(CHART_DISPLAYED, mapOf("chart_type" to "line"))
        awaitPersistedEvent()

        PostHog.optOut()
        PostHog.capture(CHART_DISPLAYED, mapOf("chart_type" to "pie"))
        PostHog.flush()
        val batch = awaitBatch()

        assertTrue(batch.contains("line"), batch)
        assertFalse(batch.contains("pie"), batch)
    }

    @Test
    fun hostIdentificationPreservesFilteredDeliveryAndOwnedConsent() {
        // The production filter reads global consent even in builds with telemetry disabled.
        val previousConsent = ChartsTelemetry.enabled
        ChartsTelemetry.enabled = true
        try {
            val client = PostHogTelemetryClient { loopbackConfig(flushAt = 1) }
            client.setup(platform)
            val installationId = assertNotNull(PostHog.getAnonymousId())
            PostHog.identify("private-host-user")

            client.capture("line", platform)
            val ownedBatch = awaitBatch()
            assertTrue(ownedBatch.contains("\"chart_type\":\"line\""), ownedBatch)

            PostHog.capture(
                CHART_DISPLAYED,
                mapOf(
                    "chart_type" to "pie",
                    "title" to "private-title",
                    "application_id" to "dev.example.private",
                    "\$app_namespace" to "dev.example.private",
                ),
            )
            val filteredBatch = awaitBatch()
            for (batch in listOf(ownedBatch, filteredBatch)) {
                assertTrue(batch.contains("\"installation_id\":\"$installationId\""), batch)
                listOf(
                    "private-host-user",
                    "private-title",
                    "application_id",
                    "dev.example.private",
                    "\$app_namespace",
                    "\$session_id",
                    "\$os_name",
                    "\$locale",
                    "\$timezone",
                ).forEach {
                    assertFalse(batch.contains(it), batch)
                }
            }

            client.setEnabled(false)
            assertTrue(PostHog.isOptedOut())
        } finally {
            ChartsTelemetry.enabled = previousConsent
        }
    }

    private fun loopbackConfig(flushAt: Int): PostHogConfig =
        baseTelemetryConfig(platform).copy(
            projectToken = "phc_hdcharts_loopback_test",
            host = "http://127.0.0.1:${server.address.port}",
            flushAt = flushAt,
            flushIntervalSeconds = 3600,
        )

    private fun awaitBatch(): String {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (System.nanoTime() < deadline) {
            val request = requests.poll(100, TimeUnit.MILLISECONDS) ?: continue
            if (request.path.contains("batch")) return request.body
        }
        error("The SDK did not deliver an event batch to the loopback server.")
    }

    private fun awaitPersistedEvent() {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
        while (System.nanoTime() < deadline) {
            if (storage.walkTopDown().any { it.isFile && it.extension == "event" }) return
            Thread.sleep(10)
        }
        error("The SDK did not persist an event.")
    }

    private data class Request(
        val path: String,
        val body: String,
    )
}
