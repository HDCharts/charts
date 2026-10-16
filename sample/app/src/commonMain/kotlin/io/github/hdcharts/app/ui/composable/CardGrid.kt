package io.github.hdcharts.app.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hdcharts.sampleshared.theme.Dimens

/** Rows of equal-width cells; a short last row keeps the cell width. */
@Composable
internal fun <T> CardGrid(
    items: List<T>,
    columns: Int,
    cell: @Composable (T, Modifier) -> Unit,
) {
    items.chunked(columns).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.cardPadding),
        ) {
            rowItems.forEach { cell(it, Modifier.weight(1f)) }
            repeat(columns - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
