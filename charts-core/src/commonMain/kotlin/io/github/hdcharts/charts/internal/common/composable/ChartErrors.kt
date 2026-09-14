package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import io.github.hdcharts.charts.internal.TestTags
import io.github.hdcharts.charts.style.ChartContainerStyle
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ChartErrors(
    style: ChartContainerStyle,
    errors: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.then(style.modifierMain)) {
        Column(modifier = Modifier.padding(style.innerPadding).testTag(TestTags.CHART_ERROR)) {
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
    }
}
