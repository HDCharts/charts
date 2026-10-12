package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hdcharts.core.internal.TestTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ChartHeadingTest {
    @Test
    fun heading_blankTitleOrSubtitle_takesNoSpace() =
        runComposeUiTest {
            val title = mutableStateOf("")
            val subtitle = mutableStateOf<String?>("Monthly totals")
            setContent {
                ChartHeading(
                    title = title.value,
                    titleTextStyle = TextStyle(fontSize = 20.sp),
                    subtitle = subtitle.value,
                    subtitleTextStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.width(240.dp).testTag("header"),
                )
            }

            onNodeWithTag(TestTags.CHART_TITLE).assertDoesNotExist()
            onNodeWithTag(TestTags.CHART_SUBTITLE).assertIsDisplayed().assertTextEquals("Monthly totals")
            val header = onNodeWithTag("header").fetchSemanticsNode().boundsInRoot
            val subtitleOnly = onNodeWithTag(TestTags.CHART_SUBTITLE).fetchSemanticsNode().boundsInRoot
            assertEquals(header.top, subtitleOnly.top)

            runOnIdle {
                title.value = "Selected category: 20"
                subtitle.value = "  "
            }
            onNodeWithTag(TestTags.CHART_SUBTITLE).assertDoesNotExist()
            val titleOnlyHeader = onNodeWithTag("header").fetchSemanticsNode().boundsInRoot
            val titleOnly = onNodeWithTag(TestTags.CHART_TITLE).fetchSemanticsNode().boundsInRoot
            assertEquals(titleOnly.height, titleOnlyHeader.height)

            runOnIdle { title.value = "" }
            onNodeWithTag("header").assertDoesNotExist()
        }
}
