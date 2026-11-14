package io.github.hdcharts.core.internal.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.posthog.kmp.PostHogContext

@Composable
internal actual fun rememberTelemetryPlatform(): TelemetryPlatform? =
    remember {
        TelemetryPlatform("ios", PostHogContext())
    }

internal actual fun configurePlatformTelemetry() = Unit
