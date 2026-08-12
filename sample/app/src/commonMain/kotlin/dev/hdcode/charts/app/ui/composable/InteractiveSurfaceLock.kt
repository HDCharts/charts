package dev.hdcode.charts.app.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

@Stable
data class InteractiveSurfaceCallbacks(
    val onInteractionStart: () -> Unit = {},
    val onInteractionEnd: () -> Unit = {},
)

val LocalInteractiveSurfaceCallbacks =
    staticCompositionLocalOf { InteractiveSurfaceCallbacks() }

@Composable
fun DrawerGestureLockContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val callbacks = LocalInteractiveSurfaceCallbacks.current
    var isInteracting by remember { mutableStateOf(false) }

    DisposableEffect(callbacks) {
        onDispose {
            if (isInteracting) {
                callbacks.onInteractionEnd()
            }
        }
    }

    Box(
        modifier =
            modifier.pointerInput(callbacks) {
                awaitPointerEventScope {
                    var wasPressed = false
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val isPressed = event.changes.any { it.pressed }

                        if (!wasPressed && isPressed) {
                            callbacks.onInteractionStart()
                            isInteracting = true
                        } else if (wasPressed && !isPressed) {
                            callbacks.onInteractionEnd()
                            isInteracting = false
                        }

                        wasPressed = isPressed
                    }
                }
            },
    ) {
        content()
    }
}
