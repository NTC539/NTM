package com.example.ntm.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ntm.R
import com.example.ntm.data.Artist
import com.example.ntm.data.Playlist
import com.example.ntm.data.Track
import com.example.ntm.ui.theme.NtmTheme

@Composable
fun SearchScreen(searchQuery: String, tracks: List<Track>, artists: List<Artist>, playlists: List<Playlist>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        OutlinedTextField(
            value = searchQuery, onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent
            )
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            if (tracks.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.filter_tracks)) }
                items(tracks.size) { index -> SearchTrackRow(tracks[index]) }
            }
            if (artists.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.nav_artists)) }
                items(artists.size) { index -> SearchArtistRow(artists[index]) }
            }
            if (playlists.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.nav_playlists)) }
                items(playlists.size) { index -> SearchPlaylistRow(playlists[index]) }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
}

@Composable
fun SearchTrackRow(track: Track) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = track.cover), contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(track.title, color = MaterialTheme.colorScheme.onBackground)
            Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SearchArtistRow(artist: Artist) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = artist.avatar), contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(artist.name, color = MaterialTheme.colorScheme.onBackground)
            Text(artist.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SearchPlaylistRow(playlist: Playlist) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = playlist.cover), contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(playlist.name, color = MaterialTheme.colorScheme.onBackground)
            Text(pluralStringResource(R.plurals.track_count, playlist.trackCount, playlist.trackCount), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SearchScreenPreview() {
    val tracks = listOf(Track("Холода", "Mellsher, СТИНТ", "2:35", R.drawable.holoda))
    val artists = listOf(Artist("MellSher", "Российский исполнитель и стример", R.drawable.mellsher))
    val playlists = listOf(Playlist("Любимое", 25, R.drawable.holoda))
    NtmTheme { SearchScreen("Холода", tracks, artists, playlists) }
}