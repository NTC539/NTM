package com.example.ntm.activity

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ntm.R
import com.example.ntm.data.HomeCategory
import com.example.ntm.data.toUiTrack
import com.example.ntm.db.entity.TrackWithCover
import com.example.ntm.navigation.Destination
import com.example.ntm.ui.viewmodel.MainViewModel
import com.example.ntm.ui.viewmodel.PlayerViewModel
import com.example.ntm.utils.PermissionUtils
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    navController: NavHostController? = null,
    viewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tracks by viewModel.tracksWithCovers.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.scanMusic()
        else Toast.makeText(context, "Без разрешения на чтение музыки сканирование невозможно", Toast.LENGTH_LONG).show()
    }

    LaunchedEffect(Unit) {
        if (PermissionUtils.hasAudioPermission(context)) {
            if (viewModel.isLibraryEmpty()) viewModel.scanMusic()
        } else {
            permissionLauncher.launch(PermissionUtils.getAudioPermission())
        }
    }

    val onRefreshClick = {
        if (PermissionUtils.hasAudioPermission(context)) viewModel.scanMusic()
        else permissionLauncher.launch(PermissionUtils.getAudioPermission())
    }

    val categories = listOf(
        HomeCategory(R.string.category_recent, Icons.Outlined.History),
        HomeCategory(R.string.category_playlists, Icons.Outlined.QueueMusic),
        HomeCategory(R.string.category_albums, Icons.Outlined.Album),
        HomeCategory(R.string.category_artists, Icons.Outlined.Person)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.my_tracks), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        items(categories) { category ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (category.titleResId == R.string.category_playlists) navController?.navigate(Destination.Route.PLAYLISTS)
                    if (category.titleResId == R.string.category_artists) navController?.navigate(Destination.Route.ARTISTS)
                }.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(stringResource(category.titleResId), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (tracks.isEmpty() && !isScanning) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.MusicNote, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Музыка не найдена", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text("Нажмите на кнопку обновления сверху, чтобы просканировать устройство.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        // ВАЖНО: Добавлен key = { it.track.id } для оптимизации
        items(tracks, key = { it.track.id }) { trackWithCover ->
            TrackItem(trackWithCover) {
                playerViewModel.playTrack(
                    track = trackWithCover.toUiTrack(),
                    allTracks = tracks.map { it.toUiTrack() }
                )
                navController?.navigate(Destination.Route.PLAYER)
            }
        }
    }
}



@Composable
fun TrackItem(trackWithCover: TrackWithCover, onClick: () -> Unit) {
    val context = LocalContext.current
    val track = trackWithCover.track
    val artistName = trackWithCover.artist?.name ?: "Неизвестный исполнитель"
    val coverPath = trackWithCover.cover?.path

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (coverPath != null) {
                AsyncImage(
                    // ИСПОЛЬЗУЕМ ImageRequest с указанием размера
                    model = ImageRequest.Builder(context)
                        .data(coverPath)
                        .size(128.dp.value.toInt()) // Уменьшаем картинку при загрузке
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(artistName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}