package com.example.ntm.activity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ntm.R
import com.example.ntm.navigation.Destination

@Composable
fun PlayerBottomBar(
    navController: NavHostController?,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Destination.Route.PLAYER,
            onClick = {},
            icon = {
                Icon(
                    imageVector = if (currentRoute == Destination.Route.PLAYER) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.player_tab_player)) }
        )
        NavigationBarItem(
            selected = currentRoute == Destination.Route.QUEUE,
            onClick = {},
            icon = {
                Icon(
                    imageVector = if (currentRoute == Destination.Route.QUEUE) Icons.AutoMirrored.Filled.QueueMusic else Icons.AutoMirrored.Outlined.QueueMusic,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.player_tab_queue)) }
        )
        NavigationBarItem(
            selected = currentRoute == Destination.Route.LYRICS,
            onClick = {},
            icon = {
                Icon(
                    imageVector = if (currentRoute == Destination.Route.LYRICS) Icons.Filled.Subject else Icons.Outlined.Subject,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.player_tab_lyrics)) }
        )
    }
}