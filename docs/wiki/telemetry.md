---
title: Telemetry
order: 3
---

# Telemetry

HDCharts reports chart usage through PostHog. Reporting is disabled by default in development
(CI and local builds). It is enabled for snapshot and production releases.

## Collected data

| Information | Description |
|---|---|
| Chart type | The chart used, such as line, bar, or pie. |
| Installation identifier | PostHog's persistent anonymous identifier for an installation. |
| Platform | Android, iOS, JVM, or Wasm. |
| HDCharts version | The library version in use. |
| SDK metadata | PostHog SDK name and version, event timestamp, and event ID. |

App/package IDs, hostnames, chart data, titles, labels, and application user identities are excluded.

## When telemetry runs

Telemetry is controlled by the `development` Gradle property, which defaults to `true`:

| Build | `development` | Telemetry |
|---|---|---|
| CI / local dev | `true` (default) | Disabled |
| Snapshot release | `false` | Enabled |
| Production release | `false` | Enabled |

Override with `-Pdevelopment=false` to enable telemetry in a development build, or
`-PchartsTelemetryEnabled=false` to disable it in a release build.

## Disable telemetry

To disable chart usage telemetry at runtime, set this before displaying any charts:

```kotlin
import io.github.hdcharts.core.ChartsTelemetry

ChartsTelemetry.enabled = false
```

## Enable telemetry in development

To enable telemetry in a development build, pass the Gradle property:

```
-Pdevelopment=false
```

Or enable it at runtime before displaying charts:

```kotlin
import io.github.hdcharts.core.ChartsTelemetry

ChartsTelemetry.enabled = true
```
