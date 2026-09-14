package io.github.hdcharts.charts.internal.common.interaction

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import io.github.hdcharts.charts.internal.InternalChartsApi
import kotlin.math.abs

private const val ZOOM_DELTA_EPSILON = 0.01f

@InternalChartsApi
fun buildTapGestureModifier(
    enabled: Boolean,
    vararg keys: Any?,
    onTap: PointerInputScope.(Offset) -> Unit,
    onDoubleTap: (PointerInputScope.() -> Unit)? = null,
): Modifier {
    if (!enabled) return Modifier
    return Modifier.pointerInput(*keys) {
        detectTapGestures(
            onTap = { offset ->
                onTap(offset)
            },
            onDoubleTap =
                if (onDoubleTap == null) {
                    null
                } else {
                    { onDoubleTap() }
                },
        )
    }
}

@InternalChartsApi
fun buildHorizontalDragGestureModifier(
    enabled: Boolean,
    vararg keys: Any?,
    onDragStart: PointerInputScope.(Offset) -> Unit,
    onHorizontalDrag: PointerInputScope.(Offset) -> Unit,
    onDragEnd: PointerInputScope.() -> Unit,
    onDragCancel: PointerInputScope.() -> Unit,
): Modifier {
    if (!enabled) return Modifier
    return Modifier.pointerInput(*keys) {
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                onDragStart(offset)
            },
            onHorizontalDrag = { change, _ ->
                onHorizontalDrag(change.position)
                change.consume()
            },
            onDragEnd = {
                onDragEnd()
            },
            onDragCancel = {
                onDragCancel()
            },
        )
    }
}

@InternalChartsApi
fun buildPinchZoomModifier(
    enabled: Boolean,
    zoomMin: Float,
    zoomMax: Float,
    getZoomScale: () -> Float,
    setZoomScale: (Float) -> Unit,
    vararg keys: Any?,
): Modifier {
    if (!enabled) return Modifier
    return Modifier.pointerInput(*keys) {
        awaitEachGesture {
            while (true) {
                val event = awaitPointerEvent()
                val pressedPointers = event.changes.count { it.pressed }
                if (pressedPointers == 0) break
                if (pressedPointers < 2) continue

                val zoomChange = event.calculateZoom()
                if (abs(zoomChange - 1f) > ZOOM_DELTA_EPSILON) {
                    val currentScale = getZoomScale()
                    val updatedScale = (currentScale * zoomChange).coerceIn(zoomMin, zoomMax)
                    if (abs(updatedScale - currentScale) <= ZOOM_DELTA_EPSILON) continue
                    setZoomScale(updatedScale)
                    event.changes.forEach { change ->
                        if (change.positionChanged()) {
                            change.consume()
                        }
                    }
                }
            }
        }
    }
}
