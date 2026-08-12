package dev.hdcode.charts.app.screenshot.shared

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

internal const val PREVIEW_DEVICE: String = "spec:width=411dp,height=891dp,dpi=420"
internal const val PREVIEW_DEVICE_TABLET: String = "spec:width=834dp,height=1194dp,dpi=264"
internal const val PREVIEW_DEVICE_TABLET_LANDSCAPE: String = "spec:width=1194dp,height=834dp,dpi=264"

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = PREVIEW_DEVICE,
)
@Preview(
    name = "Light Tablet",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET,
)
@Preview(
    name = "Light Tablet Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = PREVIEW_DEVICE_TABLET_LANDSCAPE,
)
internal annotation class ScreenshotPreview
