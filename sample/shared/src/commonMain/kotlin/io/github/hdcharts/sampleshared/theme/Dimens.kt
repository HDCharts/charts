package io.github.hdcharts.sampleshared.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared spacing scale for sample/demo composables.
 *
 * Tokens are named by their layout intent rather than by literal dp value,
 * so call sites read as a layout decision (e.g. `Dimens.cardPadding`) rather
 * than a number. Reuse the same token across composables that need the same
 * visual relationship, even if the underlying value changes later.
 */
object Dimens {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val controlSpacing: Dp = 10.dp
    val md: Dp = 12.dp
    val cardPadding: Dp = 16.dp
    val galleryPadding: Dp = 18.dp
    val drawerPadding: Dp = 20.dp
    val xl: Dp = 24.dp
}
