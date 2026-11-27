package com.example.ntm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ntm.activity.*
import com.example.ntm.data.Track
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.ui.viewmodel.PlayerViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
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
            PlaylistsScreen(navController, emptyList())
        }
        composable(Destination.Route.ARTISTS) {
            ArtistsScreen(navController, emptyList())
        }
    }
}