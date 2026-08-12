package dev.hdcode.charts.sampleshared.startup

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private const val LOADER_BAR_COUNT = 6
const val DEFAULT_LOADER_DURATION_MILLIS = 1700L

@Composable
fun ChartsStartupGate(
    isContentReady: Boolean,
    minimumVisibleMillis: Long = DEFAULT_LOADER_DURATION_MILLIS,
    loader: @Composable () -> Unit = { ChartsLoaderScreen() },
    content: @Composable () -> Unit,
) {
    var minimumVisibleElapsed by remember(minimumVisibleMillis) { mutableStateOf(false) }

    LaunchedEffect(minimumVisibleMillis) {
        delay(minimumVisibleMillis)
        minimumVisibleElapsed = true
    }

    val showLoader = !isContentReady || !minimumVisibleElapsed
    if (showLoader) {
        loader()
    } else {
        content()
    }
}

@Composable
fun ChartsLoaderScreen(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Box(
                modifier = Modifier.size(148.dp),
                contentAlignment = Alignment.Center,
            ) {
                ChartLoaderGlyph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ChartLoaderGlyph(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "charts_loader_transition")
    val progress by
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = DEFAULT_LOADER_DURATION_MILLIS.toInt(), easing = LinearEasing),
                ),
            label = "charts_loader_progress",
        )

    Canvas(modifier = modifier) {
        val left = size.width * 0.1f
        val right = size.width * 0.9f
        val top = size.height * 0.12f
        val baseline = size.height * 0.85f
        val chartWidth = right - left
        val chartHeight = baseline - top
        val gap = chartWidth * 0.03f
        val barWidth = (chartWidth - gap * (LOADER_BAR_COUNT - 1)) / LOADER_BAR_COUNT
        val strokeWidth = size.minDimension * 0.032f
        val barTops = ArrayList<Offset>(LOADER_BAR_COUNT)

        repeat(LOADER_BAR_COUNT) { index ->
            val phase = (progress + index * 0.14f) % 1f
            val wave = ((sin((phase * 2f * PI).toFloat() - (PI / 2f).toFloat()) + 1f) * 0.5f)
            val barHeight = chartHeight * (0.24f + 0.72f * wave)
            val x = left + index * (barWidth + gap)
            val y = baseline - barHeight

            drawRoundRect(
                color = loaderBarColor(index, wave, colorScheme),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(x = barWidth * 0.45f, y = barWidth * 0.45f),
            )
            barTops.add(Offset(x = x + (barWidth / 2f), y = y))
        }

        val lineOffsetY = chartHeight * 0.06f
        val linePath =
            Path().apply {
                val first = barTops.first()
                moveTo(first.x, first.y - lineOffsetY)
                for (index in 1 until barTops.size) {
                    val previous = barTops[index - 1]
                    val current = barTops[index]
                    val controlX = (previous.x + current.x) / 2f
                    cubicTo(
                        controlX,
                        previous.y - lineOffsetY,
                        controlX,
                        current.y - lineOffsetY,
                        current.x,
                        current.y - lineOffsetY,
                    )
                }
            }

        drawPath(
            path = linePath,
            color = colorScheme.outline.copy(alpha = 0.88f),
            style =
                Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
        )

        val dotSegment = progress * (barTops.size - 1)
        val lowerIndex = dotSegment.toInt().coerceIn(0, barTops.lastIndex)
        val upperIndex = (lowerIndex + 1).coerceAtMost(barTops.lastIndex)
        val segmentProgress = dotSegment - lowerIndex
        val lower = barTops[lowerIndex]
        val upper = barTops[upperIndex]
        val dotCenter =
            Offset(
                x = lower.x + (upper.x - lower.x) * segmentProgress,
                y = lower.y + (upper.y - lower.y) * segmentProgress - lineOffsetY,
            )

        drawCircle(
            color = colorScheme.primary.copy(alpha = 0.9f),
            radius = strokeWidth * 0.72f,
            center = dotCenter,
        )
        drawCircle(
            color = colorScheme.surface,
            radius = strokeWidth * 0.32f,
            center = dotCenter,
        )
    }
}

private fun loaderBarColor(
    index: Int,
    wave: Float,
    colorScheme: ColorScheme,
): Color {
    val blend =
        if (LOADER_BAR_COUNT <= 1) {
            0f
        } else {
            index.toFloat() / (LOADER_BAR_COUNT - 1).toFloat()
        }
    val base = lerp(colorScheme.surfaceVariant, colorScheme.outlineVariant, blend)
    val accent = colorScheme.primary.copy(alpha = 0.14f)
    return lerp(base, accent, 0.18f).copy(alpha = 0.46f + wave * 0.34f)
}
