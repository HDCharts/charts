package io.github.hdcharts.sampleshared.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos

/**
 * Shows [content] once [resources] are loaded, then removes the `startup-loader` element from index.html.
 *
 * Until then nothing is composed, so the HTML loader stays on screen during the wasm download and the preload.
 */
@Composable
fun StartupGate(
    resources: StartupResources,
    content: @Composable () -> Unit,
) {
    if (!rememberStartupResourcesReady(resources)) return

    content()

    LaunchedEffect(Unit) {
        // Wait for the first frame with content so the page never shows empty between loader and app.
        withFrameNanos { }
        removeStartupLoader()
    }
}

private fun removeStartupLoader(): Unit = js("document.getElementById('startup-loader')?.remove()")
