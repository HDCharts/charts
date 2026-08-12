import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.hdcode.charts.app.MainScreen
import dev.hdcode.charts.app.di.initKoin
import dev.hdcode.charts.app.ui.composable.LocalChartDemoMaxWidth

fun main() =
    application {
        initKoin()
        Window(
            title = "HDCharts Desktop Demo",
            state = rememberWindowState(width = 600.dp, height = 800.dp),
            onCloseRequest = ::exitApplication,
        ) {
            CompositionLocalProvider(LocalChartDemoMaxWidth provides 300.dp) {
                MainScreen()
            }
        }
    }
