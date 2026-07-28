package io.github.dautovicharis.charts.demoshared.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.preloadImageBitmap
import org.jetbrains.compose.resources.preloadImageVector

@Stable
data class StartupResources(
    val bitmapDrawables: List<DrawableResource> = emptyList(),
    val vectorDrawables: List<DrawableResource> = emptyList(),
    val strings: List<StringResource> = emptyList(),
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberStartupResourcesReady(resources: StartupResources): Boolean {
    val bitmapResources = remember(resources.bitmapDrawables) { resources.bitmapDrawables.distinct() }
    val vectorResources = remember(resources.vectorDrawables) { resources.vectorDrawables.distinct() }
    val stringResources = remember(resources.strings) { resources.strings.distinct() }

    var bitmapsReady = true
    bitmapResources.forEach { resource ->
        if (preloadImageBitmap(resource).value == null) {
            bitmapsReady = false
        }
    }

    var vectorsReady = true
    vectorResources.forEach { resource ->
        if (preloadImageVector(resource).value == null) {
            vectorsReady = false
        }
    }

    var stringsReady by remember(stringResources) { mutableStateOf(stringResources.isEmpty()) }
    LaunchedEffect(stringResources) {
        if (stringResources.isEmpty()) {
            stringsReady = true
            return@LaunchedEffect
        }
        stringResources.forEach { resource ->
            getString(resource)
        }
        stringsReady = true
    }

    return bitmapsReady && vectorsReady && stringsReady
}
