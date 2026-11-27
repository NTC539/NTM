package com.example.ntm.activity

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ntm.activity.component.PlaylistCover
import com.example.ntm.data.toUiTrack
import com.example.ntm.db.entity.PlaylistEntity
import com.example.ntm.db.entity.TrackWithCover
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.ui.viewmodel.PlayerViewModel
import com.example.ntm.ui.viewmodel.PlaylistViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavHostController?,
    playlistViewModel: PlaylistViewModel,
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    playlistId: Long
) {
    val playlistData by playlistViewModel.getPlaylistDetails(playlistId).collectAsState()
    val allLibraryTracks by mainViewModel.tracksWithCovers.collectAsState()
    val context = LocalContext.current

    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showCoverOptions by remember { mutableStateOf(false) }

    // Лаунчер для выбора кастомной обложки
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            playlistData?.playlist?.let { pl -> playlistViewModel.setCustomCover(pl, it) }
        }
    }

    if (playlistData == null) return

    val playlist = playlistData!!.playlist
    val tracks = playlistData!!.tracks

    // Находим путь к кастомной обложке, если она есть
    val customCoverPath = if (playlist.cover_type == 2) {
        // В реальном проекте нужно сделать запрос к coverDao по id, но для простоты передадим null
        // или вытянем через отдельный запрос. Пока оставим так.
        null
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(120.dp).clickable { showCoverOptions = true },
                ) {
                    PlaylistCover(playlist = playlist, tracks = tracks, customCoverPath = customCoverPath, modifier = Modifier.fillMaxSize())
                    Icon(Icons.Default.Edit, "Edit Cover", modifier = Modifier.align(Alignment.BottomEnd).background(MaterialTheme.colorScheme.surface).padding(4.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(playlist.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${tracks.size} треков", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        if (tracks.isNotEmpty()) {
                            // Берем первый трек для старта, а весь список передаем как rawTracks
                            playerViewModel.playTrack(
                                selectedTrack = tracks.first().toUiTrack(),
                                rawTracks = tracks
                            )
                            navController?.navigate("player")
                        }
                    }) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Слушать")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Треки", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { showAddTrackDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Добавить трек", tint = MaterialTheme.colorScheme.primary)
                }
            }

            LazyColumn {
                items(tracks, key = { it.track.id }) { trackWithCover ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Обложка трека
                        if (trackWithCover.cover?.path != null) {
                            AsyncImage(model = File(trackWithCover.cover.path), contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)))
                        } else {
                            Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(trackWithCover.track.title, maxLines = 1)
                            Text(trackWithCover.artist?.name ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { playlistViewModel.removeTrackFromPlaylist(playlist.id, trackWithCover.track.id) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Диалог выбора обложки
    if (showCoverOptions) {
        AlertDialog(
            onDismissRequest = { showCoverOptions = false },
            title = { Text("Обложка плейлиста") },
            text = {
                Column {
                    TextButton(onClick = { playlistViewModel.updateCoverType(playlist, 0); showCoverOptions = false }) {
                        Text("Последний добавленный трек")
                    }
                    TextButton(onClick = { playlistViewModel.updateCoverType(playlist, 1); showCoverOptions = false }) {
                        Text("Сетка из 4 треков")
                    }
                    TextButton(onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)); showCoverOptions = false }) {
                        Text("Выбрать из галереи")
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Диалог добавления трека
    if (showAddTrackDialog) {
        AlertDialog(
            onDismissRequest = { showAddTrackDialog = false },
            title = { Text("Добавить трек в плейлист") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    // Фильтруем треки, которые уже есть в плейлисте
                    val existingIds = tracks.map { it.track.id }.toSet()
                    val availableTracks = allLibraryTracks.filter { it.track.id !in existingIds }

                    items(availableTracks, key = { it.track.id }) { track ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                playlistViewModel.addTrackToPlaylist(playlist.id, track.track.id)
                                showAddTrackDialog = false
                            }.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(track.track.title, maxLines = 1, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddTrackDialog = false }) { Text("Закрыть") } }
        )
    }
}