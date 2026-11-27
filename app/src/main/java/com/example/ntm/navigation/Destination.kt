package com.example.ntm.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ntm.R

sealed class Destination(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : Destination(Route.HOME, R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home)
    data object Search : Destination(Route.SEARCH, R.string.nav_search, Icons.Outlined.Search, Icons.Filled.Search)
    data object Settings : Destination(Route.SETTINGS, R.string.nav_settings, Icons.Outlined.Settings, Icons.Filled.Settings)

    object Route {
        const val HOME = "home"
        const val SEARCH = "search"
        const val SETTINGS = "settings"
        const val PLAYER = "player"
        const val QUEUE = "queue"
        const val LYRICS = "lyrics"
        const val PLAYLISTS = "playlists"
        const val ARTISTS = "artists"
        const val PLAYLIST_DETAIL = "playlist_detail"
    }
}