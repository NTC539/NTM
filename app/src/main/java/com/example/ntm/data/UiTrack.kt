package com.example.ntm.data

import com.example.ntm.db.entity.TrackWithCover

data class UiTrack(
    val id: Long,
    val title: String,
    val artistName: String,
    val path: String,
    val coverPath: String?
)

fun TrackWithCover.toUiTrack(): UiTrack {
    return UiTrack(
        id = track.id,
        title = track.title,
        artistName = artist?.name ?: "Неизвестный исполнитель",
        path = track.path,
        coverPath = cover?.path
    )
}
