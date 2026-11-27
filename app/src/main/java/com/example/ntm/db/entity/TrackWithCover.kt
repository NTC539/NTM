package com.example.ntm.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TrackWithCover(
    @Embedded val track: TrackEntity,
    @Relation(
        parentColumn = "cover_id",
        entityColumn = "id"
    )
    val cover: CoverEntity?,

    @Relation(
        parentColumn = "artist_id",
        entityColumn = "id"
    )
    val artist: ArtistEntity?
)