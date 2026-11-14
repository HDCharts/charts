package io.github.hdcharts.core

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.hdcharts.core.internal.telemetry.chartTelemetry

/**
 * Controls chart usage reporting within a composition subtree. Defaults to `true`.
 *
 * Provide `false` around galleries or embedded previews to suppress their chart-display events.
 * Charts outside that scope keep their existing behavior. [ChartsTelemetry.enabled] must also be
 * `true` for reporting; this setting does not change the global preference or SDK opt-in state.
 */
val LocalChartsTelemetryEnabled: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { true }

/**
 * Global chart usage telemetry. Enabled by default.
 *
 * Set [enabled] to `false` before composing charts to prevent SDK initialization and reporting.
 * Set it to `false` later to stop new captures and call the SDK's opt-out API. Previously queued
 * events and requests already in flight can still be delivered by the SDK. Apply your saved
 * preference at each application startup.
 *
 * Events contain the chart type, installation identifier, platform, and HDCharts version.
 * Chart data, titles, labels, and person profiles are never included.
 */
object ChartsTelemetry {
    var enabled: Boolean
        get() = chartTelemetry.enabled
        set(value) {
            chartTelemetry.enabled = value
        }
}
