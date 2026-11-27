package com.example.ntm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ntm.activity.*
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.ui.viewmodel.PlayerViewModel
import com.example.ntm.ui.viewmodel.PlaylistViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            HomeScreen(navController, mainViewModel, playerViewModel)
        }
        composable(Destination.Search.route) {
            SearchScreen("", emptyList(), emptyList(), emptyList())
        }
        composable(Destination.Settings.route) {
            SettingsScreen(mainViewModel)
        }
        composable(Destination.Route.PLAYER) {
            PlayerScreen(navController, playerViewModel)
        }
        composable(Destination.Route.QUEUE) {
            QueueScreen(navController, playerViewModel)
        }
        composable(Destination.Route.LYRICS) {
            LyricsScreen(navController, playerViewModel)
        }
        composable(Destination.Route.PLAYLISTS) {
            PlaylistsScreen(navController, playlistViewModel)
        }
        composable(Destination.Route.ARTISTS) {
            ArtistsScreen(navController, emptyList())
        }

        composable(Destination.Route.PLAYLISTS) {
            PlaylistsScreen(navController, playlistViewModel)
        }

        composable("${Destination.Route.PLAYLIST_DETAIL}/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
            PlaylistDetailScreen(navController, playlistViewModel, mainViewModel, playerViewModel, playlistId)
        }
    }
}