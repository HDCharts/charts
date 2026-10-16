package io.github.hdcharts.sampleapi.http

import io.ktor.client.plugins.websocket.WebSocketCapability
import kotlin.test.Test
import kotlin.test.assertContains

class SampleHttpClientTest {
    @Test
    fun defaultEngine_supportsWebSockets() {
        val client = sampleHttpClient()
        try {
            assertContains(client.engine.supportedCapabilities, WebSocketCapability)
        } finally {
            client.close()
        }
    }
}
