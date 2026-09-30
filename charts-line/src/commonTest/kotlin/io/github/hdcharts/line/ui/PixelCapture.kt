package io.github.hdcharts.line.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Sets [content] with a modifier that records its drawing; the returned function captures the current frame. */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.setCapturedContent(
    content: @Composable (captureModifier: Modifier) -> Unit,
): () -> PixelMap {
    lateinit var captureLayer: GraphicsLayer
    lateinit var captureScope: CoroutineScope
    val capturedPixels = mutableStateOf<PixelMap?>(null)
    setContent {
        captureLayer = rememberGraphicsLayer()
        captureScope = rememberCoroutineScope()
        content(
            Modifier.drawWithContent {
                captureLayer.record { this@drawWithContent.drawContent() }
                drawLayer(captureLayer)
            },
        )
    }
    return {
        capturedPixels.value = null
        runOnIdle { captureScope.launch { capturedPixels.value = captureLayer.toImageBitmap().toPixelMap() } }
        waitUntil(timeoutMillis = 3_000L) { capturedPixels.value != null }
        checkNotNull(capturedPixels.value)
    }
}
