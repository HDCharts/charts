package io.github.hdcharts.core.internal.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.hdcharts.core.style.ChartContainerDefaults
import io.github.hdcharts.core.style.LegendStyle
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LegendTest {
    @Test
    fun legend_namedItemsWithValues_showsNameAndValue() =
        runComposeUiTest {
            setContent { TestLegend(names = listOf("A", "B"), values = listOf("1", "2")) }

            onNodeWithText("A - 1").assertIsDisplayed()
            onNodeWithText("B - 2").assertIsDisplayed()
        }

    @Test
    fun legend_blankName_showsValueOnly() =
        runComposeUiTest {
            setContent { TestLegend(names = listOf("A", ""), values = listOf("1", "2")) }

            onNodeWithText("2").assertIsDisplayed()
        }

    @Test
    fun legend_oneItem_isHidden() =
        runComposeUiTest {
            setContent { TestLegend(names = listOf("A")) }

            onAllNodesWithText("A").assertCountEquals(0)
        }

    @Test
    fun legend_allBlankNames_isHidden() =
        runComposeUiTest {
            setContent { TestLegend(names = listOf("", " "), values = listOf("1", "2")) }

            onAllNodesWithText("1").assertCountEquals(0)
        }

    @Test
    fun legend_notVisible_isHidden() =
        runComposeUiTest {
            setContent { TestLegend(names = listOf("A", "B"), visible = false) }

            onAllNodesWithText("A").assertCountEquals(0)
        }
}

@Composable
private fun TestLegend(
    names: List<String>,
    values: List<String> = emptyList(),
    visible: Boolean = true,
) {
    Legend(
        chartContainerStyle = ChartContainerDefaults.style(),
        style = LegendStyle(visible = visible),
        legend = names.toImmutableList(),
        colors = persistentListOf(Color.Red, Color.Blue),
        labels = values.toImmutableList(),
    )
}
