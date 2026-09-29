package io.github.hdcharts.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.hdcharts.sampleshared.theme.Dimens
import org.jetbrains.compose.resources.stringResource

private val ExampleListMaxWidth = 640.dp
private val ExampleRowShape = RoundedCornerShape(16.dp)

@Composable
fun ChartExampleList(
    examples: List<ChartExample>,
    onExampleSelected: (ChartExample) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .widthIn(max = ExampleListMaxWidth)
                    .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimens.galleryPadding, vertical = Dimens.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            items(examples, key = { it.id }) { example ->
                ElevatedCard(
                    onClick = { onExampleSelected(example) },
                    shape = ExampleRowShape,
                ) {
                    ListItem(
                        headlineContent = { Text(stringResource(example.title)) },
                        supportingContent = { Text(stringResource(example.summary)) },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}
