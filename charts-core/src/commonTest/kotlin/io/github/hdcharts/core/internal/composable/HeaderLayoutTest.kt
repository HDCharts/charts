package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.style.StyleDefaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HeaderLayoutTest {
    @Test
    fun wrappedSubtitle_staysBelowTitleAndBesideControls() =
        runComposeUiTest {
            setContent {
                ChartHeaderLayout(
                    title = "Monthly totals",
                    titleTextStyle = TextStyle(fontSize = 20.sp),
                    subtitle = "A long explanation of the monthly totals that wraps across several lines",
                    subtitleTextStyle = TextStyle(fontSize = 14.sp),
                    showControls = true,
                    modifier = Modifier.width(240.dp),
                ) {
                    Box(modifier = Modifier.size(48.dp).testTag("controls"))
                }
            }

            val title = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode().boundsInRoot
            val subtitle = onNodeWithTag(TestTags.CHART_SUBTITLE).fetchSemanticsNode().boundsInRoot
            val controls = onNodeWithTag("controls").fetchSemanticsNode().boundsInRoot
            val subtitleLayout = mutableListOf<TextLayoutResult>()
            onNodeWithTag(TestTags.CHART_SUBTITLE)
                .fetchSemanticsNode()
                .config[SemanticsActions.GetTextLayoutResult]
                .action
                ?.invoke(subtitleLayout)
            assertTrue(subtitleLayout.single().lineCount > 1)
            val expectedGap = with(density) { StyleDefaults.subtitleSpacing.toPx() }
            assertEquals(expectedGap, subtitle.top - title.bottom, absoluteTolerance = 1f)
            assertTrue(subtitle.right <= controls.left)
            assertTrue(title.right <= controls.left)
        }
}
