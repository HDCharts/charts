package io.github.dautovicharis.charts.app

import androidx.lifecycle.ViewModel
import io.github.dautovicharis.charts.demoshared.theme.Theme
import io.github.dautovicharis.charts.demoshared.theme.blueViolet
import io.github.dautovicharis.charts.demoshared.theme.citrusGrove
import io.github.dautovicharis.charts.demoshared.theme.deepOceanBlue
import io.github.dautovicharis.charts.demoshared.theme.deepRed
import io.github.dautovicharis.charts.demoshared.theme.docsSlate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MenuState(
    val menuItems: List<ChartDestination>,
    val selectedDestination: ChartDestination? = null,
)

enum class DarkModeSettings {
    System,
    On,
    Off,
}

data class ThemesState(
    val themes: List<Theme>,
    val selectedTheme: Theme,
    val darkMode: DarkModeSettings,
    val useDynamicColors: Boolean,
)

class MainViewModel : ViewModel() {
    private val _menuState =
        MutableStateFlow(
            MenuState(
                listOf(
                    ChartDestination.PieChartScreen,
                    ChartDestination.LineChartScreen,
                    ChartDestination.MultiLineChartScreen,
                    ChartDestination.StackedAreaChartScreen,
                    ChartDestination.BarChartScreen,
                    ChartDestination.HistogramChartScreen,
                    ChartDestination.StackedBarChartScreen,
                    ChartDestination.RadarChartScreen,
                ),
            ),
        )

    val menuState: StateFlow<MenuState> = _menuState.asStateFlow()

    private val _themeState =
        MutableStateFlow(
            ThemesState(
                themes =
                    listOf(
                        deepRed,
                        blueViolet,
                        deepOceanBlue,
                        citrusGrove,
                        docsSlate,
                    ),
                selectedTheme = docsSlate,
                darkMode = DarkModeSettings.System,
                useDynamicColors = false,
            ),
        )

    val themeState: StateFlow<ThemesState> = _themeState.asStateFlow()

    fun onThemeSelected(newTheme: Theme) {
        _themeState.update {
            it.copy(
                selectedTheme = newTheme,
            )
        }
    }

    fun toggleDarkMode() {
        _themeState.update {
            val newDarkMode =
                when (it.darkMode) {
                    DarkModeSettings.System -> DarkModeSettings.Off
                    DarkModeSettings.Off -> DarkModeSettings.On
                    DarkModeSettings.On -> DarkModeSettings.System
                }
            it.copy(
                darkMode = newDarkMode,
            )
        }
    }

    fun toggleDynamicColor() {
        _themeState.update {
            it.copy(
                useDynamicColors = !it.useDynamicColors,
            )
        }
    }

    fun onChartSelected(destination: ChartDestination) {
        _menuState.update {
            it.copy(selectedDestination = destination)
        }
    }

    fun onChartUnselected() {
        _menuState.update {
            it.copy(selectedDestination = null)
        }
    }

    fun resolveDarkTheme(isSystemInDark: Boolean): Boolean =
        when (themeState.value.darkMode) {
            DarkModeSettings.System -> isSystemInDark
            DarkModeSettings.On -> true
            DarkModeSettings.Off -> false
        }
}
