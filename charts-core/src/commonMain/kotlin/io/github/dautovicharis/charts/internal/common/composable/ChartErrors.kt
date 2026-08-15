package io.github.dautovicharis.charts.internal.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.dautovicharis.charts.internal.TestTags
import io.github.dautovicharis.charts.internal.common.theme.ChartsDefaultTheme
import io.github.dautovicharis.charts.style.ChartContainerStyle
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ChartErrors(
    style: ChartContainerStyle,
    errors: ImmutableList<String>,
) {
    ChartsDefaultTheme(content = {
        Column(
            modifier =
                style.modifierMain
                    .padding(15.dp)
                    .testTag(TestTags.CHART_ERROR),
        ) {
            errors.forEach { error ->
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(5.dp),
                            ).padding(5.dp),
                    text = "$error\n",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    })
}
