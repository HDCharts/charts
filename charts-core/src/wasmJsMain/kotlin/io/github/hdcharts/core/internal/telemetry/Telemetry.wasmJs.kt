@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.hdcharts.core.internal.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.posthog.kmp.PostHogContext

@Composable
internal actual fun rememberTelemetryPlatform(): TelemetryPlatform? =
    remember { TelemetryPlatform("wasm", PostHogContext()) }

internal actual fun configurePlatformTelemetry() {
    disableWebTracking(posthog)
}

private fun disableWebTracking(sdk: JsAny): Unit =
    js(
        """{
        sdk.set_config({
            disable_session_recording: true,
            capture_heatmaps: false,
            capture_performance: false,
            capture_dead_clicks: false,
            disable_surveys: true,
            disable_product_tours: true,
            disable_conversations: true,
            disable_external_dependency_loading: true,
            enable_recording_console_log: false,
            advanced_disable_decide: true,
            advanced_disable_flags: true,
            ip: false
        });
        sdk.stopSessionRecording();
    }""",
    )
