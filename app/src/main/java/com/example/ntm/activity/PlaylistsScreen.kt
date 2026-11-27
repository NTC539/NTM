package com.example.ntm.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ntm.R
import com.example.ntm.db.entity.PlaylistEntity
import com.example.ntm.navigation.Destination
import com.example.ntm.ui.viewmodel.PlaylistViewModel

@Composable
fun PlaylistsScreen(navController: NavHostController? = null, viewModel: PlaylistViewModel) {
    val playlists by viewModel.allPlaylists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать плейлист")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text("Плейлисты", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            LazyColumn {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            navController?.navigate("${Destination.Route.PLAYLIST_DETAIL}/${playlist.id}")
                        }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Временная заглушка, так как для списка мы пока не тянем все треки
                        // В идеале здесь нужен запрос, который считает количество треков для плейлиста
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // PlaylistCover(...)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Новый плейлист") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }) },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) viewModel.createPlaylist(name)
                    showCreateDialog = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Отмена") } }
        )
    }
}