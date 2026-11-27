package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CoverEntity::class,
            parentColumns = ["id"],
            childColumns = ["cover_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("artist_id"), Index("cover_id"), Index(value = ["title", "artist_id"], unique = true)]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist_id: Long? = null,
    val cover_id: Long? = null,
    val year: Int? = null,
    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)