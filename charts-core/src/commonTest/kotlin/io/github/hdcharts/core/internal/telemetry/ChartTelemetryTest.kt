@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.github.hdcharts.core.internal.telemetry

import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChartTelemetryTest {
    private val platform = TelemetryPlatform("jvm")

    @Test
    fun disabledBeforeFirstDisplay_preventsSetupAndCapture() =
        runTest {
            val client = RecordingClient()
            val telemetry = ChartTelemetry(client, backgroundScope)
            assertTrue(telemetry.enabled)

            telemetry.enabled = false
            telemetry.display("line", platform)
            runCurrent()

            assertFalse(telemetry.enabled)
            assertEquals(0, client.setups)
            assertTrue(client.charts.isEmpty())
            assertTrue(client.consentChanges.isEmpty())
        }

    @Test
    fun multipleCharts_initializeOnceAndCaptureEachDisplay() =
        runTest {
            val client = RecordingClient()
            val telemetry = ChartTelemetry(client, backgroundScope)

            telemetry.display("line", platform)
            telemetry.display("pie", platform)
            telemetry.display("line", platform)
            runCurrent()

            assertEquals(1, client.setups)
            assertEquals(listOf("line", "pie", "line"), client.charts)
        }

    @Test
    fun optOutBeforeWorkerRuns_dropsPendingDisplaysEvenAfterReenable() =
        runTest {
            val client = RecordingClient()
            val telemetry = ChartTelemetry(client, backgroundScope)

            telemetry.display("line", platform)
            telemetry.enabled = false
            telemetry.enabled = true
            telemetry.display("pie", platform)
            runCurrent()

            assertEquals(listOf("pie"), client.charts)
        }

    @Test
    fun runtimeOptOut_updatesSdkAndPreventsCaptureUntilReenabled() =
        runTest {
            val client = RecordingClient()
            val telemetry = ChartTelemetry(client, backgroundScope)
            telemetry.display("line", platform)
            runCurrent()

            telemetry.enabled = false
            telemetry.display("pie", platform)
            runCurrent()
            assertEquals(listOf(true, false), client.consentChanges)
            assertEquals(listOf("line"), client.charts)

            telemetry.enabled = true
            telemetry.display("bar", platform)
            runCurrent()
            assertEquals(listOf(true, false, true), client.consentChanges)
            assertEquals(listOf("line", "bar"), client.charts)
            assertEquals(1, client.setups)
        }

    @Test
    fun optOutDuringSetup_preventsFirstCapture() =
        runTest {
            lateinit var telemetry: ChartTelemetry
            val client = RecordingClient(onSetup = { telemetry.enabled = false })
            telemetry = ChartTelemetry(client, backgroundScope)

            telemetry.display("line", platform)
            runCurrent()

            assertEquals(1, client.setups)
            assertTrue(client.charts.isEmpty())
            assertTrue(client.consentChanges.all { !it })
        }

    @Test
    fun setupFailure_stopsTelemetryWithoutThrowingOrRetryingEveryDisplay() =
        runTest {
            val client = RecordingClient(onSetup = { error("SDK unavailable") })
            val telemetry = ChartTelemetry(client, backgroundScope)

            repeat(3) { telemetry.display("line", platform) }
            runCurrent()

            assertEquals(1, client.setups)
            assertTrue(client.charts.isEmpty())
        }

    @Test
    fun captureFailure_stopsTelemetryWithoutFailingCaller() =
        runTest {
            val client = RecordingClient(onCapture = { error("storage unavailable") })
            val telemetry = ChartTelemetry(client, backgroundScope)

            repeat(3) { telemetry.display("line", platform) }
            runCurrent()

            assertEquals(1, client.setups)
            assertEquals(listOf("line"), client.charts)
        }

    private class RecordingClient(
        val onSetup: () -> Unit = {},
        val onCapture: () -> Unit = {},
    ) : TelemetryClient {
        var setups = 0
        val charts = mutableListOf<String>()
        val consentChanges = mutableListOf<Boolean>()

        override fun setup(platform: TelemetryPlatform) {
            setups++
            onSetup()
        }

        override fun capture(
            chart: String,
            platform: TelemetryPlatform,
        ) {
            charts += chart
            onCapture()
        }

        override fun setEnabled(enabled: Boolean) {
            consentChanges += enabled
        }
    }
}
