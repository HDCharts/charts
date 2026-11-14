package io.github.hdcharts.core.internal.telemetry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalInspectionMode
import io.github.hdcharts.core.LocalChartsTelemetryEnabled
import io.github.hdcharts.core.internal.InternalChartsApi

/** Reports one use per composition lifetime, including entries that display validation errors. */
@InternalChartsApi
@Composable
fun TrackChartUsage(chart: String) {
    if (LocalInspectionMode.current) return
    val platform = rememberTelemetryPlatform() ?: return
    TrackChartDisplay(chart) { chartTelemetry.display(it, platform) }
}

@Composable
internal fun TrackChartDisplay(
    chart: String,
    report: (String) -> Unit,
) {
    if (!LocalChartsTelemetryEnabled.current) return
    LaunchedEffect(Unit) { report(chart) }
}

@Composable
internal expect fun rememberTelemetryPlatform(): TelemetryPlatform?
