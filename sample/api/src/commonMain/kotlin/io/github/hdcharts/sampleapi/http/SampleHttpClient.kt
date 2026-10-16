package io.github.hdcharts.sampleapi.http

import io.github.hdcharts.sampleapi.ApiUnavailableException
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json

// Decoded here rather than through Ktor content negotiation, which pulls YAML and OpenAPI libraries into the bundle.
internal val SampleJson = Json { ignoreUnknownKeys = true }

/**
 * The one HTTP client every sample API shares.
 *
 * @param engine Leave null to use the platform engine; tests pass a mock engine.
 */
internal fun sampleHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(HttpTimeout)
        install(WebSockets)
    }
    return if (engine == null) HttpClient(config) else HttpClient(engine, config)
}

// Ktor's browser engine reports network failures as Error, not Exception, so both are caught here.
internal suspend inline fun <T> networkCall(block: () -> T): T =
    try {
        block()
    } catch (e: CancellationException) {
        // A dropped socket cancels its own channels; only the caller's cancellation should end a retry loop.
        currentCoroutineContext().ensureActive()
        throw ApiUnavailableException(e)
    } catch (e: Throwable) {
        throw ApiUnavailableException(e)
    }
