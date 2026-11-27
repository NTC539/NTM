package com.example.ntm.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import com.example.ntm.db.entity.TrackWithCover
import com.example.ntm.data.toUiTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

            // Отслеживаем смену трека (как ручную, так и автоматическую)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateCurrentTrack()
            }

            // На всякий случай дублируем проверку при любых событиях плеера
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) || events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                    updateCurrentTrack()
                }
            }
        })

        // Постоянное обновление позиции
        viewModelScope.launch {
            while (true) {
                controller?.let { _currentPosition.value = it.currentPosition }
                delay(500)
            }
        }
    }

    private fun updateCurrentTrack() {
        val index = controller?.currentMediaItemIndex ?: 0
        val track = _playerState.value.queue.getOrNull(index)
        if (track != null && track.id != _playerState.value.currentTrack?.id) {
            _playerState.update { it.copy(currentTrack = track) }
        }
    }

    fun playTrack(selectedTrack: UiTrack, rawTracks: List<TrackWithCover>) {
        viewModelScope.launch(Dispatchers.Default) { // Уходим в фоновый поток
            val allUiTracks = rawTracks.map { it.toUiTrack() }
            val startIndex = allUiTracks.indexOfFirst { it.id == selectedTrack.id }.coerceAtLeast(0)

            _playerState.update { it.copy(queue = allUiTracks, currentTrack = selectedTrack) }

            withContext(Dispatchers.Main) {
                val mediaItems = allUiTracks.map { uiTrack ->
                    val metadataBuilder = MediaMetadata.Builder()
                        .setTitle(uiTrack.title)
                        .setArtist(uiTrack.artistName)
                        .setIsPlayable(true)
                        .setIsBrowsable(false)

                    uiTrack.coverPath?.let { path ->
                        metadataBuilder.setArtworkUri(Uri.fromFile(File(path)))
                    }

                    MediaItem.Builder()
                        .setUri(uiTrack.path)
                        .setMediaId(uiTrack.id.toString())
                        .setMediaMetadata(metadataBuilder.build())
                        .build()
                }

                controller?.apply {
                    setMediaItems(mediaItems)
                    seekTo(startIndex, 0L)
                    prepare()
                    play()
                }
            }
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