package io.github.hdcharts.app.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A string resource with its arguments already formatted, so view models choose text without loading resources. */
data class UiText(
    val resource: StringResource,
    val args: List<Any> = emptyList(),
)

@Composable
fun UiText.resolve(): String = stringResource(resource, *args.toTypedArray())
