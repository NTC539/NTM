package com.example.ntm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ntm.activity.*
import com.example.ntm.data.Track
import com.example.ntm.ui.viewmodel.MainViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home.route,
        modifier = modifier
    ) {
        composable(Destination.Home.route) {
            HomeScreen(navController, mainViewModel)
        }
        composable(Destination.Search.route) {
            SearchScreen("", emptyList(), emptyList(), emptyList())
        }
        composable(Destination.Settings.route) {
            SettingsScreen(mainViewModel)
        }
        composable(Destination.Route.PLAYER) {
            PlayerScreen(navController, Track("", "", "", 0))
        }
        composable(Destination.Route.QUEUE) {
            QueueScreen(navController, emptyList())
        }
        composable(Destination.Route.LYRICS) {
            LyricsScreen(navController, Track("", "", "", 0))
        }
        composable(Destination.Route.PLAYLISTS) {
            PlaylistsScreen(navController, emptyList())
        }
        composable(Destination.Route.ARTISTS) {
            ArtistsScreen(navController, emptyList())
        }
    }
}