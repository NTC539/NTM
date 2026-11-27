package com.example.ntm.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ntm.db.AppDatabase
import com.example.ntm.db.entity.CoverEntity
import com.example.ntm.db.entity.PlaylistEntity
import com.example.ntm.db.entity.PlaylistTrackEntity
import com.example.ntm.db.entity.TrackWithCover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class PlaylistWithTracks(
    val playlist: PlaylistEntity,
    val tracks: List<TrackWithCover>
)

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val playlistDao = db.playlistDao()
    private val coverDao = db.coverDao()

    val allPlaylists = playlistDao.getAllPlaylists().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun getPlaylistDetails(playlistId: Long): StateFlow<PlaylistWithTracks?> {
        val playlistFlow = playlistDao.getPlaylistById(playlistId)
        val tracksFlow = playlistDao.getTracksForPlaylist(playlistId)

        // Объединяем данные плейлиста и его треков
        return combine(playlistFlow, tracksFlow) { playlist, tracks ->
            if (playlist != null) PlaylistWithTracks(playlist, tracks) else null
        }.stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.deletePlaylist(playlist)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val maxPos = playlistDao.getMaxPosition(playlistId) ?: 0
            playlistDao.addTrackToPlaylist(
                PlaylistTrackEntity(playlist_id = playlistId, track_id = trackId, position = maxPos + 1)
            )
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun updateCoverType(playlist: PlaylistEntity, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.updatePlaylist(playlist.copy(cover_type = type, cover_id = null))
        }
    }

    fun setCustomCover(playlist: PlaylistEntity, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val coversDir = File(context.filesDir, "custom_covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val coverFile = File(coversDir, "playlist_cover_${UUID.randomUUID()}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(coverFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val coverEntity = CoverEntity(
                    path = coverFile.absolutePath,
                    mime_type = "image/jpeg",
                    hash = "custom_${System.currentTimeMillis()}"
                )
                val coverId = coverDao.insert(coverEntity)

                playlistDao.updatePlaylist(playlist.copy(cover_type = 2, cover_id = coverId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}