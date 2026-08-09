package io.github.dautovicharis.charts

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import io.github.dautovicharis.charts.internal.common.composable.Chart
import io.github.dautovicharis.charts.internal.common.composable.ChartErrors
import io.github.dautovicharis.charts.internal.common.theme.ChartsDefaultTheme
import io.github.dautovicharis.charts.style.ChartViewDefaults
import kotlinx.collections.immutable.persistentListOf

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(
    name = "Light",
    group = "Theme",
    uiMode = UI_MODE_NIGHT_NO or UI_MODE_TYPE_NORMAL,
)
@Preview(
    name = "Dark",
    group = "Theme",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
annotation class ChartsPreviewLightDark

@Composable
fun ChartsPreviewTheme(content: @Composable () -> Unit) {
    ChartsDefaultTheme(
        darkTheme = isSystemInDarkTheme(),
        dynamicColor = false,
        content = content,
    )
}

@ChartsPreviewLightDark
@Composable
private fun ChartsCoreContainerPreview() {
    ChartsPreviewTheme {
        Chart(chartViewsStyle = ChartViewDefaults.style()) {
            Text("HDCharts Core Container")
        }
    }
}

@ChartsPreviewLightDark
@Composable
private fun ChartsCoreErrorsPreview() {
    ChartsPreviewTheme {
        ChartErrors(
            style = ChartViewDefaults.style(),
            errors =
                persistentListOf(
                    "Error: at least two values are required.",
                    "Error: labels and values must have matching sizes.",
                ),
        )
    }
}
