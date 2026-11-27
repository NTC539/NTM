package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listening_history",
    foreignKeys = [
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["track_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("track_id")]
)
data class ListeningHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val track_id: Long,
    val started_at: Long,
    val listened_ms: Long,
    val completed: Boolean,
    val context_type: String?, // "PLAYLIST", "ALBUM", "ARTIST", "QUEUE"
    val context_id: Long?
)
