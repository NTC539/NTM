package com.example.ntm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.ntm.data.UiTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerState(
    val currentTrack: UiTrack? = null,
    val queue: List<UiTrack> = emptyList(),
    val isPlaying: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                val track = _playerState.value.queue.getOrNull(index)
                _playerState.update { it.copy(currentTrack = track) }
            }
        })

        // Постоянное обновление позиции для слайдера
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _currentPosition.value = exoPlayer.currentPosition
                }
                delay(500)
            }
        }
    }

    fun playTrack(track: UiTrack, allTracks: List<UiTrack>) {
        val startIndex = allTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        _playerState.update { it.copy(queue = allTracks, currentTrack = track) }

        // Загружаем треки в ExoPlayer. ExoPlayer умеет читать локальные пути (file://)
        exoPlayer.setMediaItems(allTracks.map { MediaItem.fromUri(it.path) })
        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun playNext() { if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNext() }
    fun playPrevious() { if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPrevious() }
    fun seekTo(positionMs: Long) { exoPlayer.seekTo(positionMs) }
    fun getDuration() = if (exoPlayer.duration > 0) exoPlayer.duration else 0L

    fun stopAndClear() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playerState.update { PlayerState() }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}