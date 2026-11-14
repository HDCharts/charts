package io.github.hdcharts.core.internal.telemetry

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.posthog.kmp.PostHogContext

@Composable
internal actual fun rememberTelemetryPlatform(): TelemetryPlatform? {
    val application = LocalContext.current.applicationContext as? Application ?: return null
    return remember(application) {
        TelemetryPlatform("android", PostHogContext(application))
    }
}

internal actual fun configurePlatformTelemetry() = Unit
