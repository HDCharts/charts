package io.github.hdcharts.app.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hdcharts.app.screenshot.shared.ScreenshotSurface

internal const val GRADIENT_GALLERY_END_ALPHA = 0.35f

private val GalleryCardShape = RoundedCornerShape(12.dp)

@Composable
internal fun GradientGallerySurface(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ScreenshotSurface {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
internal fun ColumnScope.GradientGalleryRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
internal fun GradientGalleryExample(
    title: String,
    description: String,
    modifier: Modifier,
    chart: @Composable (Modifier) -> Unit,
) {
    Card(
        modifier = modifier.padding(vertical = 6.dp),
        shape = GalleryCardShape,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                chart(Modifier.fillMaxSize())
            }
        }
    }
}
