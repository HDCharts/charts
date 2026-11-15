---
title: Telemetry
order: 3
---

# Telemetry

HDCharts reports chart usage through PostHog. Reporting is enabled by default on Android, iOS,
JVM, and Wasm.

## Collected data

| Information | Description |
|---|---|
| Chart type | The chart used, such as line, bar, or pie. |
| Installation identifier | PostHog's persistent anonymous identifier for an installation. |
| Platform | Android, iOS, JVM, or Wasm. |
| HDCharts version | The library version in use. |
| SDK metadata | PostHog SDK name and version, event timestamp, and event ID. |

App/package IDs, hostnames, chart data, titles, labels, and application user identities are excluded.

## Disable telemetry

To disable chart usage telemetry, set this at each application startup before displaying any charts:

```kotlin
import io.github.hdcharts.core.ChartsTelemetry

ChartsTelemetry.enabled = false
```
