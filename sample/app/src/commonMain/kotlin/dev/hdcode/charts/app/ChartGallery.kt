package dev.hdcode.charts.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val GalleryCardShape = RoundedCornerShape(24.dp)
private val GalleryTwoColumnBreakpoint = 760.dp
private val GalleryThreeColumnBreakpoint = 940.dp
internal val LocalChartGalleryColumns = staticCompositionLocalOf { 2 }

@Composable
fun ChartGallery(
    menuState: MenuState,
    onChartSelected: (ChartDestination) -> Unit,
    versionLabel: String,
    modifier: Modifier = Modifier,
    viewModel: ChartGalleryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val menuItems = menuState.menuItems
    val items = state.items.ifEmpty { viewModel.buildItems(menuItems) }

    LaunchedEffect(menuItems) {
        viewModel.setMenuItems(menuItems)
    }

    LaunchedEffect(viewModel) {
        viewModel.runPreviewLoop()
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val preferredColumns = LocalChartGalleryColumns.current.coerceIn(1, 3)
            val columns =
                when {
                    preferredColumns >= 3 && maxWidth >= GalleryThreeColumnBreakpoint -> 3
                    preferredColumns >= 2 && maxWidth >= GalleryTwoColumnBreakpoint -> 2
                    else -> 1
                }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items.chunked(columns).forEach { rowItems ->
                    val hasUnevenLastItem = columns > 1 && rowItems.size == 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        rowItems.forEach { item ->
                            val accent = MaterialTheme.colorScheme.primary

                            ChartGalleryCard(
                                item = item,
                                accent = accent,
                                onOpen = {
                                    onChartSelected(item.destination)
                                },
                                preview = {
                                    ChartPreview(
                                        destination = item.destination,
                                        previews = state.previews,
                                    )
                                },
                                modifier =
                                    if (hasUnevenLastItem) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.weight(1f)
                                    },
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = versionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun ChartGalleryCard(
    item: ChartGalleryItemUiState,
    accent: Color,
    onOpen: () -> Unit,
    preview: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        shape = GalleryCardShape,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(item.destination.icon),
                        contentDescription = stringResource(item.destination.title),
                        tint = accent,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(item.destination.title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ChartPreviewFrame(
                        accent = accent,
                        onClick = onOpen,
                    ) {
                        preview()
                    }
                }
            }
        }
    }
}
