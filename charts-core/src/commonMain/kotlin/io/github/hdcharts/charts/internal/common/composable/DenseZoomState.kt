package io.github.hdcharts.charts.internal.common.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.hdcharts.charts.internal.InternalChartsApi

@Composable
@InternalChartsApi
fun rememberDenseExpandedState(isDenseModeAvailable: Boolean): MutableState<Boolean> {
    val denseExpanded = remember { mutableStateOf(false) }
    LaunchedEffect(isDenseModeAvailable) {
        if (!isDenseModeAvailable) denseExpanded.value = false
    }
    return denseExpanded
}

@Composable
@InternalChartsApi
fun rememberZoomScaleState(
    isZoomActive: Boolean,
    minZoom: Float,
    maxZoom: Float,
    initialZoom: Float = minZoom,
): MutableFloatState {
    val zoomScale = remember { mutableFloatStateOf(initialZoom) }
    LaunchedEffect(isZoomActive, minZoom, maxZoom) {
        zoomScale.floatValue =
            when {
                !isZoomActive -> minZoom
                else -> zoomScale.floatValue.coerceIn(minZoom, maxZoom)
            }
    }
    return zoomScale
}

@InternalChartsApi
fun zoomOutScale(
    zoomScale: Float,
    zoomStep: Float,
    minZoom: Float,
    maxZoom: Float,
): Float = (zoomScale / zoomStep).coerceIn(minZoom, maxZoom)

@InternalChartsApi
fun zoomInScale(
    zoomScale: Float,
    zoomStep: Float,
    minZoom: Float,
    maxZoom: Float,
): Float = (zoomScale * zoomStep).coerceIn(minZoom, maxZoom)
