package com.example.ntm.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.ntm.data.UiTrack
import com.example.ntm.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ВАЖНО: Класс PlayerState должен быть здесь
data class PlayerState(
    val currentTrack: UiTrack? = null,
    val queue: List<UiTrack> = emptyList(),
    val isPlaying: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    init {
        val sessionToken = SessionToken(
            application,
            ComponentName(application, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()

        // Используем ContextCompat.getMainExecutor для слушателя
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            setupControllerListener()
        }, ContextCompat.getMainExecutor(application))
    }

    private fun setupControllerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = controller?.currentMediaItemIndex ?: 0
                val track = _playerState.value.queue.getOrNull(index)
                _playerState.update { it.copy(currentTrack = track) }
            }
        })

        viewModelScope.launch {
            while (true) {
                controller?.let { _currentPosition.value = it.currentPosition }
                delay(500)
            }
        }
    }

    fun playTrack(track: UiTrack, allTracks: List<UiTrack>) {
        val startIndex = allTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        _playerState.update { it.copy(queue = allTracks, currentTrack = track) }

        controller?.apply {
            setMediaItems(allTracks.map { MediaItem.fromUri(it.path) })
            seekTo(startIndex, 0L)
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun playNext() { controller?.seekToNextMediaItem() }
    fun playPrevious() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun getDuration() = controller?.duration?.takeIf { it > 0 } ?: 0L

    fun stopAndClear() {
        controller?.stop()
        controller?.clearMediaItems()
        _playerState.update { PlayerState() }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}