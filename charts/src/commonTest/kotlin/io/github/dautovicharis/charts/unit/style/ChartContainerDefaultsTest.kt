package io.github.dautovicharis.charts.unit.style

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.style.ChartContainerDefaults
import kotlin.test.Test

class ChartContainerDefaultsTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartViewDefaults_withExplicitWidth_constrainsChartWidth() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style(width = 250.dp, modifierChart = Modifier)
                    Box(modifier = style.modifierMain.semantics { testTag = "box" })
                }
            }

            onNodeWithTag("box").assertWidthIsEqualTo(250.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartViewDefaults_withInfiniteWidth_wrapsContent() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style =
                        ChartContainerDefaults.style(width = Dp.Infinity, modifierChart = Modifier)
                    Box(
                        modifier =
                            style.modifierMain.semantics { testTag = "box" },
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.width(180.dp),
                        )
                    }
                }
            }

            onNodeWithTag("box").assertWidthIsEqualTo(180.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartViewDefaults_defaultModifierChart_squareChartArea() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style(width = 200.dp)
                    Box(
                        modifier =
                            Modifier
                                .width(200.dp)
                                .then(style.modifierChart)
                                .fillMaxSize()
                                .semantics { testTag = "chart" },
                    )
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(200.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(200.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartViewDefaults_customModifierChart_overridesAspectRatio() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style =
                        ChartContainerDefaults.style(
                            width = 200.dp,
                            modifierChart = Modifier.aspectRatio(2f),
                        )
                    Box(
                        modifier =
                            Modifier
                                .width(200.dp)
                                .then(style.modifierChart)
                                .fillMaxSize()
                                .semantics { testTag = "chart" },
                    )
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(200.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(100.dp)
        }
}
