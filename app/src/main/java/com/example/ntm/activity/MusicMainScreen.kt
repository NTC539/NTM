package com.example.ntm.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ntm.R
import com.example.ntm.data.HomeCategory
import com.example.ntm.data.Track
import com.example.ntm.navigation.AppNavHost
import com.example.ntm.navigation.Destination
import com.example.ntm.ui.viewmodel.MainViewModel

@Composable
fun MusicMainScreen() {
    // 1. Инициализируем ViewModel
    val mainViewModel: MainViewModel = viewModel()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isPlayerScreen = currentRoute == Destination.Route.PLAYER ||
            currentRoute == Destination.Route.QUEUE ||
            currentRoute == Destination.Route.LYRICS

    // В будущем: эти данные будут браться из ViewModel (состояние ExoPlayer)
    var currentTrack by remember { mutableStateOf<Track?>(Track("Километры", "Wildways", "03:23", R.drawable.kosmos)) }
    var isPlaying by remember { mutableStateOf(true) }

    MusicMainScreenContent(
        currentRoute = currentRoute,
        showBottomBar = !isPlayerScreen,
        currentTrack = currentTrack,
        isPlaying = isPlaying,
        onMiniPlayerClick = { navController.navigate(Destination.Route.PLAYER) },
        onPlayPauseClick = { isPlaying = !isPlaying }, // Теперь кнопка работает
        onCloseClick = { currentTrack = null },        // Закрываем мини-плеер
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
            mainViewModel = mainViewModel, // 2. Передаем ViewModel в навигацию
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun MusicMainScreenContent(
    currentRoute: String?,
    showBottomBar: Boolean,
    currentTrack: Track?,
    isPlaying: Boolean,
    onMiniPlayerClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onCloseClick: () -> Unit, // 3. Добавлен параметр
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

// --- Моки для Preview оставлены ---
val mockCategories = listOf(
    HomeCategory(R.string.category_recent, Icons.Outlined.History),
    HomeCategory(R.string.category_playlists, Icons.Outlined.QueueMusic),
    HomeCategory(R.string.category_albums, Icons.Outlined.Album),
    HomeCategory(R.string.category_artists, Icons.Outlined.Person),
)
val mockTracks = listOf(
    Track("Невыносимого нет", "ЖЩ", "3:18", R.drawable.nevinosimgonet),
    Track("Хуже, чем вчера", "ЖЩ", "4:11", R.drawable.pustota),
    Track("Жить по-другому", "ЖЩ", "3:25", R.drawable.pustota),
    Track("космос", "наше последнее лето", "2:17", R.drawable.kosmos),
    Track("увидимся снова", "наше последнее лето", "1:58", R.drawable.uvidimsasnova),
    Track("Холода", "Mellsher, СТИНТ", "2:35", R.drawable.holoda),
    Track("X", "Mellsher", "2:30", R.drawable.x)
)
private val mockMiniTrack = Track("космос", "наше последнее лето", "03:23", R.drawable.kosmos)