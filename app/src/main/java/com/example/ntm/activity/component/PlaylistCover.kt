package com.example.ntm.activity.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ntm.db.entity.PlaylistEntity
import com.example.ntm.db.entity.TrackWithCover
import java.io.File

@Composable
fun PlaylistCover(
    playlist: PlaylistEntity,
    tracks: List<TrackWithCover>,
    customCoverPath: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (playlist.cover_type) {
            0 -> { // Последний добавленный трек (берем первый из tracks, если он отсортирован по дате добавления)
                val lastTrack = tracks.lastOrNull()
                if (lastTrack?.cover?.path != null) {
                    AsyncImage(
                        model = File(lastTrack.cover.path),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DefaultIcon()
                }
            }
            1 -> { // Сетка 2x2
                val coverTracks = tracks.takeLast(4)
                Column(Modifier.fillMaxSize()) {
                    Row(Modifier.weight(1f)) {
                        CoverCell(coverTracks.getOrNull(0)?.cover?.path, Modifier.weight(1f).fillMaxHeight())
                        CoverCell(coverTracks.getOrNull(1)?.cover?.path, Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(Modifier.weight(1f)) {
                        CoverCell(coverTracks.getOrNull(2)?.cover?.path, Modifier.weight(1f).fillMaxHeight())
                        CoverCell(coverTracks.getOrNull(3)?.cover?.path, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
            2 -> { // Кастомная
                if (customCoverPath != null) {
                    AsyncImage(
                        model = File(customCoverPath),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    DefaultIcon()
                }
            }
        }
    }
}

@Composable
private fun CoverCell(path: String?, modifier: Modifier) {
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        if (path != null) {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun DefaultIcon() {
    Icon(
        Icons.Default.MusicNote,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
    )
}