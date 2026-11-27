package com.example.ntm.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ntm.navigation.AppNavHost
import com.example.ntm.navigation.Destination
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.ui.viewmodel.PlayerViewModel
import com.example.ntm.ui.viewmodel.PlaylistViewModel

@Composable
fun MusicMainScreen() {
    val mainViewModel: MainViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isPlayerScreen = currentRoute == Destination.Route.PLAYER ||
            currentRoute == Destination.Route.QUEUE ||
            currentRoute == Destination.Route.LYRICS

    val playerState by playerViewModel.playerState.collectAsState()

    MusicMainScreenContent(
        currentRoute = currentRoute,
        showBottomBar = !isPlayerScreen,
        currentTrack = playerState.currentTrack,
        isPlaying = playerState.isPlaying,
        onMiniPlayerClick = { navController.navigate(Destination.Route.PLAYER) },
        onPlayPauseClick = { playerViewModel.togglePlayPause() },
        onCloseClick = { playerViewModel.stopAndClear() },
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            mainViewModel = mainViewModel,
            playerViewModel = playerViewModel,
            playlistViewModel = playlistViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MusicMainScreenContent(
    currentRoute: String?,
    showBottomBar: Boolean,
    currentTrack: com.example.ntm.data.UiTrack?, // Изменили тип
    isPlaying: Boolean,
    onMiniPlayerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onCloseClick: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(Destination.Home, Destination.Search, Destination.Settings)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if (currentTrack != null) {
                        MiniPlayer(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            onClick = onMiniPlayerClick,
                            onPlayPauseClick = onPlayPauseClick,
                            onCloseClick = onCloseClick
                        )
                    }
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp
                    ) {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (currentRoute == item.route) item.selectedIcon else item.icon,
                                        contentDescription = stringResource(item.titleResId)
                                    )
                                },
                                label = { Text(stringResource(item.titleResId)) },
                                selected = currentRoute == item.route,
                                onClick = { onNavigate(item.route) }
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        content(innerPadding)
    }
}