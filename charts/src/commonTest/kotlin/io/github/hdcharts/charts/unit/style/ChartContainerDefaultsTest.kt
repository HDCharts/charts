package io.github.hdcharts.charts.unit.style

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.github.hdcharts.charts.internal.InternalChartsApi
import io.github.hdcharts.charts.internal.common.layout.fillMaxSizeChartModifier
import io.github.hdcharts.charts.style.ChartContainerDefaults
import kotlin.test.Test

@OptIn(InternalChartsApi::class)
class ChartContainerDefaultsTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartContainerDefaults_boundedRectangle_fillsAvailableSize() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style()
                    Box(modifier = Modifier.size(width = 280.dp, height = 180.dp)) {
                        Box(
                            modifier =
                                style
                                    .let { fillMaxSizeChartModifier(it, contentPadding = 0.dp) }
                                    .semantics { testTag = "chart" },
                        )
                    }
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(280.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(180.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartContainerDefaults_unboundedHeight_usesWidthAsFiniteFallback() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style()
                    Column(
                        modifier =
                            Modifier
                                .width(200.dp)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        Box(
                            modifier =
                                style
                                    .let { fillMaxSizeChartModifier(it, contentPadding = 0.dp) }
                                    .semantics { testTag = "chart" },
                        )
                    }
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(200.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(200.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartContainerDefaults_unboundedWidth_usesHeightAsFiniteFallback() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style()
                    Row(
                        modifier =
                            Modifier
                                .height(180.dp)
                                .horizontalScroll(rememberScrollState()),
                    ) {
                        Box(
                            modifier =
                                style
                                    .let { fillMaxSizeChartModifier(it, contentPadding = 0.dp) }
                                    .semantics { testTag = "chart" },
                        )
                    }
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(180.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(180.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartContainerDefaults_unboundedSize_usesDefaultFiniteFallback() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style()
                    Box(
                        modifier =
                            Modifier
                                .horizontalScroll(rememberScrollState())
                                .verticalScroll(rememberScrollState()),
                    ) {
                        Box(
                            modifier =
                                style
                                    .let { fillMaxSizeChartModifier(it, contentPadding = 0.dp) }
                                    .semantics { testTag = "chart" },
                        )
                    }
                }
            }

            onNodeWithTag("chart").assertWidthIsEqualTo(200.dp)
            onNodeWithTag("chart").assertHeightIsEqualTo(200.dp)
        }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun chartContainerDefaults_unboundedSize_respectsMinimumConstraints() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    val style = ChartContainerDefaults.style()
                    Layout(
                        content = {
                            Box(modifier = fillMaxSizeChartModifier(style, contentPadding = 0.dp)) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .semantics { testTag = "chart-content" },
                                )
                            }
                        },
                    ) { measurables, _ ->
                        val placeable =
                            measurables.single().measure(
                                Constraints(
                                    minWidth = 280.dp.roundToPx(),
                                    maxWidth = Constraints.Infinity,
                                    minHeight = 260.dp.roundToPx(),
                                    maxHeight = Constraints.Infinity,
                                ),
                            )
                        layout(placeable.width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }
                }
            }

            onNodeWithTag("chart-content").assertWidthIsEqualTo(280.dp)
            onNodeWithTag("chart-content").assertHeightIsEqualTo(260.dp)
        }
}
