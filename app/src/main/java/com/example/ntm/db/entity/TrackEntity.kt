package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(entity = ArtistEntity::class, parentColumns = ["id"], childColumns = ["artist_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = AlbumEntity::class, parentColumns = ["id"], childColumns = ["album_id"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = CoverEntity::class, parentColumns = ["id"], childColumns = ["cover_id"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index(value = ["path"], unique = true),
        Index("artist_id"),
        Index("album_id"),
        Index("title")
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val title: String,
    val artist_id: Long? = null,
    val album_id: Long? = null,
    val year: Int? = null,
    val genre: String? = null,
    val mime_type: String? = null,
    val cover_id: Long? = null,
    val date_added: Long = System.currentTimeMillis(),
    val file_modified_at: Long,
    val updated_at: Long = System.currentTimeMillis(),
    val is_missing: Boolean = false
)