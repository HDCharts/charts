package dev.hdcode.charts.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_close_settings
import chartsproject.app.generated.resources.cd_select_theme
import chartsproject.app.generated.resources.dark_mode_off
import chartsproject.app.generated.resources.dark_mode_on
import chartsproject.app.generated.resources.dark_mode_system
import chartsproject.app.generated.resources.drawer_dark_mode_subtitle
import chartsproject.app.generated.resources.drawer_dynamic_colors_disable_hint
import chartsproject.app.generated.resources.drawer_dynamic_colors_subtitle
import chartsproject.app.generated.resources.drawer_github_subtitle
import chartsproject.app.generated.resources.drawer_header_subtitle
import chartsproject.app.generated.resources.drawer_header_title
import chartsproject.app.generated.resources.drawer_section_appearance
import chartsproject.app.generated.resources.drawer_section_links
import chartsproject.app.generated.resources.drawer_section_themes
import chartsproject.app.generated.resources.drawer_title_dark_mode
import chartsproject.app.generated.resources.drawer_title_dynamic_colors
import chartsproject.app.generated.resources.drawer_title_github
import chartsproject.app.generated.resources.github_url
import chartsproject.app.generated.resources.github_url_content_description
import chartsproject.sample_shared.generated.resources.ic_github
import dev.hdcode.charts.sampleshared.theme.LocalHasDynamicColorFeature
import dev.hdcode.charts.sampleshared.theme.Theme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import chartsproject.sample_shared.generated.resources.Res as SharedRes

@Composable
fun SettingsDrawerContent(
    themeState: ThemesState,
    onThemeSelected: (Theme) -> Unit,
    onDarkModeToggle: () -> Unit,
    onDynamicToggle: () -> Unit,
    onClose: () -> Unit,
    showCloseButton: Boolean = true,
) {
    val hasDynamicColors = LocalHasDynamicColorFeature.current
    val githubUrl = stringResource(Res.string.github_url)
    val uriHandler = LocalUriHandler.current
    val darkModeLabel =
        stringResource(
            when (themeState.darkMode) {
                DarkModeSettings.System -> Res.string.dark_mode_system
                DarkModeSettings.On -> Res.string.dark_mode_on
                DarkModeSettings.Off -> Res.string.dark_mode_off
            },
        )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        modifier = Modifier.width(320.dp),
    ) {
        DrawerHeader(
            onClose = onClose,
            showCloseButton = showCloseButton,
        )

        DrawerSectionTitle(text = stringResource(Res.string.drawer_section_appearance))
        DrawerSettingCard(
            title = stringResource(Res.string.drawer_title_dark_mode),
            subtitle = stringResource(Res.string.drawer_dark_mode_subtitle, darkModeLabel),
            leading = {
                Icon(
                    imageVector = Icons.Filled.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailing = {
                SettingBadge(text = darkModeLabel)
            },
            onClick = onDarkModeToggle,
        )

        if (hasDynamicColors) {
            DrawerToggleCard(
                title = stringResource(Res.string.drawer_title_dynamic_colors),
                subtitle = stringResource(Res.string.drawer_dynamic_colors_subtitle),
                checked = themeState.useDynamicColors,
                onCheckedChange = { onDynamicToggle() },
            )
        }

        DrawerSectionTitle(text = stringResource(Res.string.drawer_section_themes))
        if (themeState.useDynamicColors) {
            Text(
                text = stringResource(Res.string.drawer_dynamic_colors_disable_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        } else {
            ThemeSwatches(
                themeState = themeState,
                onThemeSelected = onThemeSelected,
            )
        }

        DrawerSectionTitle(text = stringResource(Res.string.drawer_section_links))
        DrawerSettingCard(
            title = stringResource(Res.string.drawer_title_github),
            subtitle = stringResource(Res.string.drawer_github_subtitle),
            leading = {
                Icon(
                    painter = painterResource(SharedRes.drawable.ic_github),
                    contentDescription = stringResource(Res.string.github_url_content_description),
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            onClick = { uriHandler.openUri(githubUrl) },
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DrawerHeader(
    onClose: () -> Unit,
    showCloseButton: Boolean,
) {
    val headerBrush =
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f),
            ),
        )
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(headerBrush)
                .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.drawer_header_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = stringResource(Res.string.drawer_header_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
            if (showCloseButton) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.cd_close_settings),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

@Composable
private fun DrawerSettingCard(
    title: String,
    subtitle: String,
    leading: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                leading()
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun DrawerToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun ThemeSwatches(
    themeState: ThemesState,
    onThemeSelected: (Theme) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(modifier = Modifier.width(16.dp)) }
        itemsIndexed(items = themeState.themes) { index, theme ->
            val themeContentDescription = stringResource(Res.string.cd_select_theme, index + 1)
            val isSelectedTheme = themeState.selectedTheme == theme
            FilledIconToggleButton(
                checked = isSelectedTheme,
                onCheckedChange = { onThemeSelected(theme) },
                modifier =
                    Modifier
                        .size(40.dp)
                        .semantics { contentDescription = themeContentDescription },
                shape = CircleShape,
                colors =
                    IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        checkedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .border(
                                width = if (isSelectedTheme) 2.dp else 1.dp,
                                color =
                                    if (isSelectedTheme) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    },
                                shape = CircleShape,
                            ).padding(2.dp)
                            .clip(CircleShape)
                            .background(theme.light.primary),
                )
            }
        }
        item { Spacer(modifier = Modifier.width(16.dp)) }
    }
}

@Composable
private fun SettingBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
