package io.github.hdcharts.app

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

/**
 * One demo screen listed under a chart type.
 *
 * @param id Route segment, unique within its chart type.
 * @param summary One line on what the example shows, displayed under [title] in the list.
 */
class ChartExample(
    val id: String,
    val title: StringResource,
    val summary: StringResource,
    val content: @Composable () -> Unit,
)
