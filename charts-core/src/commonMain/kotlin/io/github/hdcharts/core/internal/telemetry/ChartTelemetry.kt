@file:OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)

package io.github.hdcharts.core.internal.telemetry

import com.posthog.kmp.PostHogContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt

internal data class TelemetryPlatform(
    val name: String,
    val context: PostHogContext? = null,
)

internal interface TelemetryClient {
    fun setup(platform: TelemetryPlatform)

    fun capture(
        chart: String,
        platform: TelemetryPlatform,
    )

    fun setEnabled(enabled: Boolean)
}

internal val chartTelemetry =
    ChartTelemetry(
        PostHogTelemetryClient(),
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default,
        ),
        initiallyEnabled = TelemetryBuildConfig.TELEMETRY_ENABLED,
    )

/** Serializes SDK setup and capture; the SDK owns queues, persistence, retries, and delivery. */
internal class ChartTelemetry(
    private val client: TelemetryClient,
    private val scope: CoroutineScope,
    initiallyEnabled: Boolean = true,
) {
    private val consent = AtomicBoolean(initiallyEnabled)
    private val generation = AtomicInt(0)
    private val mutex = Mutex()
    private var initialized = false
    private var failed = false

    var enabled: Boolean
        get() = consent.load()
        set(value) {
            if (consent.exchange(value) == value) return
            generation.addAndFetch(1)
            scope.launch {
                mutex.withLock {
                    if (initialized) safely { client.setEnabled(enabled) }
                }
            }
        }

    fun display(
        chart: String,
        platform: TelemetryPlatform,
    ) {
        if (!enabled) return
        val displayGeneration = generation.load()
        scope.launch {
            mutex.withLock {
                if (!enabled || failed || displayGeneration != generation.load()) return@withLock
                safely {
                    if (!initialized) {
                        client.setup(platform)
                        initialized = true
                        client.setEnabled(enabled)
                    }
                    // Consent can change while a native SDK initializes on this worker.
                    if (enabled && displayGeneration == generation.load()) client.capture(chart, platform)
                }
            }
        }
    }

    private inline fun safely(action: () -> Unit) {
        try {
            action()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            // A broken SDK stays disabled for this process; charts keep working.
            failed = true
        }
    }
}
