package io.github.hdcharts.core.internal.telemetry

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.hdcharts.core.LocalChartsTelemetryEnabled
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TrackChartUsageTest {
    @Test
    fun suppressedGallery_doesNotReportOnReentryOrAffectChartsOutsideScope() =
        runComposeUiTest {
            val visible = mutableStateOf(true)
            val reports = mutableListOf<String>()

            setContent {
                if (visible.value) {
                    CompositionLocalProvider(LocalChartsTelemetryEnabled provides false) {
                        TrackChartDisplay("line") { reports += it }
                        TrackChartDisplay("bar") { reports += it }
                    }
                    TrackChartDisplay("pie") { reports += it }
                }
            }
            waitForIdle()
            assertEquals(listOf("pie"), reports)

            runOnIdle { visible.value = false }
            waitForIdle()
            runOnIdle { visible.value = true }
            waitForIdle()
            assertEquals(listOf("pie", "pie"), reports)
        }

    @Test
    fun recompositionAndDataChanges_countOnceUntilChartLeavesComposition() =
        runComposeUiTest {
            val updates = mutableStateOf(0)
            val visible = mutableStateOf(true)
            val reports = mutableListOf<String>()

            setContent {
                if (visible.value) {
                    val update = updates.value
                    TrackChartDisplay("line") { reports += it }
                    Text("Update $update")
                }
            }
            waitForIdle()
            assertEquals(listOf("line"), reports)

            runOnIdle { updates.value++ }
            onNodeWithText("Update 1").assertExists()
            assertEquals(listOf("line"), reports)

            runOnIdle { visible.value = false }
            waitForIdle()
            runOnIdle { visible.value = true }
            waitForIdle()
            assertEquals(listOf("line", "line"), reports)
        }

    @Test
    fun separateInstancesOfSameChart_countSeparately() =
        runComposeUiTest {
            val reports = mutableListOf<String>()
            setContent {
                TrackChartDisplay("bar") { reports += it }
                TrackChartDisplay("bar") { reports += it }
            }
            waitForIdle()

            assertEquals(listOf("bar", "bar"), reports)
        }
}
