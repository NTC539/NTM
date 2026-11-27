package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(entity = TrackEntity::class, parentColumns = ["id"], childColumns = ["track_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SourceEntity::class, parentColumns = ["id"], childColumns = ["source_id"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index(value = ["track_id"], unique = true), Index("source_id")]
)
data class LyricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val track_id: Long,
    val source_id: Long?,
    val text: String?,
    val source_url: String?,
    val external_id: String?,
    val status: String, // например: "LOADED", "ERROR", "NOT_FOUND"
    val fetched_at: Long?,
    val updated_at: Long = System.currentTimeMillis()
)
