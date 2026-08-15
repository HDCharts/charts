package dev.hdcode.charts.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import chartsproject.app.generated.resources.Res
import chartsproject.app.generated.resources.cd_navigate_back
import chartsproject.app.generated.resources.cd_open_settings
import dev.hdcode.charts.app.library.BuildConfig
import dev.hdcode.charts.app.ui.composable.InteractiveSurfaceCallbacks
import dev.hdcode.charts.app.ui.composable.LocalInteractiveSurfaceCallbacks
import dev.hdcode.charts.sampleshared.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val WideLayoutBreakpoint = 980.dp
private val SettingsRailWidth = 320.dp
private val SettingsEdgeOpenZoneWidth = 20.dp
private val SettingsEdgeOpenDragThreshold = 28.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val menuState by viewModel.menuState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val canNavigateBack = currentBackStackEntry?.destination?.route != ChartDestination.MainScreen.ROUTE
    val isMainRoute = currentRoute == ChartDestination.MainScreen.ROUTE
    val topBarTitleByRoute = remember(menuState.menuItems) { menuState.menuItems.topBarTitleByRoute() }
    val topBarTitle =
        if (isMainRoute) {
            null
        } else {
            topBarTitleByRoute[currentRoute]?.let { stringResource(it) }
        }
    var activeInteractiveSurfaces by remember { mutableIntStateOf(0) }
    val interactionCallbacks =
        remember {
            InteractiveSurfaceCallbacks(
                onInteractionStart = {
                    activeInteractiveSurfaces += 1
                },
                onInteractionEnd = {
                    activeInteractiveSurfaces = (activeInteractiveSurfaces - 1).coerceAtLeast(0)
                },
            )
        }

    AppTheme(
        theme = themeState.selectedTheme,
        useDynamicColors = themeState.useDynamicColors,
        darkTheme = viewModel.resolveDarkTheme(isSystemInDarkTheme()),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            CompositionLocalProvider(LocalInteractiveSurfaceCallbacks provides interactionCallbacks) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useWideLayout = maxWidth >= WideLayoutBreakpoint

                    LaunchedEffect(useWideLayout) {
                        if (useWideLayout && drawerState.currentValue == DrawerValue.Open) {
                            drawerState.close()
                        }
                    }

                    if (useWideLayout) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier =
                                    Modifier
                                        .width(SettingsRailWidth)
                                        .fillMaxHeight(),
                            ) {
                                SettingsDrawerContent(
                                    themeState = themeState,
                                    onThemeSelected = viewModel::onThemeSelected,
                                    onDarkModeToggle = viewModel::toggleDarkMode,
                                    onDynamicToggle = viewModel::toggleDynamicColor,
                                    onClose = {},
                                    showCloseButton = false,
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            )

                            MainScaffold(
                                canNavigateBack = canNavigateBack,
                                topBarTitle = topBarTitle,
                                showSettingsButton = false,
                                onNavigateBack = { navController.popBackStack() },
                                onOpenSettings = {},
                                content = { innerPadding ->
                                    Navigation(
                                        navController = navController,
                                        menuState = menuState,
                                        onChartSelected = viewModel::onChartSelected,
                                        modifier = Modifier.padding(innerPadding),
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        val canOpenFromEdge =
                            drawerState.currentValue == DrawerValue.Closed && activeInteractiveSurfaces == 0

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = false,
                            drawerContent = {
                                SettingsDrawerContent(
                                    themeState = themeState,
                                    onThemeSelected = viewModel::onThemeSelected,
                                    onDarkModeToggle = viewModel::toggleDarkMode,
                                    onDynamicToggle = viewModel::toggleDynamicColor,
                                    onClose = { scope.launch { drawerState.close() } },
                                )
                            },
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                MainScaffold(
                                    canNavigateBack = canNavigateBack,
                                    topBarTitle = topBarTitle,
                                    showSettingsButton = true,
                                    onNavigateBack = { navController.popBackStack() },
                                    onOpenSettings = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    },
                                    content = { innerPadding ->
                                        Navigation(
                                            navController = navController,
                                            menuState = menuState,
                                            onChartSelected = viewModel::onChartSelected,
                                            modifier = Modifier.padding(innerPadding),
                                        )
                                    },
                                )

                                SettingsEdgeSwipeOpenZone(
                                    visible = canOpenFromEdge,
                                    onOpen = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(menuState.selectedDestination) {
        menuState.selectedDestination?.let {
            viewModel.onChartUnselected()
            navController.navigate(it.route)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    canNavigateBack: Boolean,
    topBarTitle: String?,
    showSettingsButton: Boolean,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable (innerPadding: androidx.compose.foundation.layout.PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (!topBarTitle.isNullOrBlank()) {
                        Text(text = topBarTitle)
                    }
                },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.cd_navigate_back),
                            )
                        }
                    }
                },
                actions = {
                    if (showSettingsButton) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(Res.string.cd_open_settings),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

private fun List<ChartDestination>.topBarTitleByRoute(): Map<String, StringResource> =
    buildMap {
        this@topBarTitleByRoute.forEach { destination ->
            put(destination.route, destination.title)
        }
    }

@Composable
private fun BoxScope.SettingsEdgeSwipeOpenZone(
    visible: Boolean,
    onOpen: () -> Unit,
) {
    if (!visible) return

    val dragThresholdPx = with(LocalDensity.current) { SettingsEdgeOpenDragThreshold.toPx() }

    Box(
        modifier =
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(SettingsEdgeOpenZoneWidth)
                .pointerInput(onOpen) {
                    var consumedDistance = 0f
                    detectHorizontalDragGestures(
                        onDragStart = {
                            consumedDistance = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            if (dragAmount <= 0f) return@detectHorizontalDragGestures

                            consumedDistance += dragAmount
                            if (consumedDistance >= dragThresholdPx) {
                                change.consume()
                                consumedDistance = 0f
                                onOpen()
                            }
                        },
                        onDragEnd = {
                            consumedDistance = 0f
                        },
                        onDragCancel = {
                            consumedDistance = 0f
                        },
                    )
                },
    )
}

@Composable
fun MainScreenContent(
    menuState: MenuState,
    onChartSelected: (selected: ChartDestination) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        ChartGallery(
            menuState = menuState,
            onChartSelected = onChartSelected,
            versionLabel = "HDCharts: ${BuildConfig.CHARTS_VERSION}",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
        )
    }
}
