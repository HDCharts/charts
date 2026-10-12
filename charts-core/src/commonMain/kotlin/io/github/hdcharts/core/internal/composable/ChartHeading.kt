package io.github.hdcharts.core.internal.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import io.github.hdcharts.core.internal.InternalChartsApi
import io.github.hdcharts.core.internal.TestTags
import io.github.hdcharts.core.style.StyleDefaults

/** Shared title and subtitle text, spacing, and visibility. */
@Composable
@InternalChartsApi
fun ChartHeading(
    title: String,
    titleTextStyle: TextStyle,
    subtitle: String?,
    subtitleTextStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    if (title.isBlank() && subtitle.isNullOrBlank()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(StyleDefaults.subtitleSpacing),
    ) {
        if (title.isNotBlank()) {
            Text(
                modifier = Modifier.testTag(TestTags.CHART_TITLE),
                text = title,
                style = titleTextStyle,
            )
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                modifier = Modifier.testTag(TestTags.CHART_SUBTITLE),
                text = subtitle,
                style = subtitleTextStyle,
            )
        }
    }
}
