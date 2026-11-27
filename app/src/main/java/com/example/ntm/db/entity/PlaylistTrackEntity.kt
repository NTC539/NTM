package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlist_id", "track_id"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlist_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["track_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("track_id")]
)
data class PlaylistTrackEntity(
    val playlist_id: Long,
    val track_id: Long,
    val position: Int,
    val added_at: Long = System.currentTimeMillis()
)
