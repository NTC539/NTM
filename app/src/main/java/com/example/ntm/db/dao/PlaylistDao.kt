package com.example.ntm.db.dao

import androidx.room.*
import com.example.ntm.db.entity.PlaylistEntity
import com.example.ntm.db.entity.PlaylistTrackEntity
import com.example.ntm.db.entity.TrackWithCover
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?>

    // Запрос на получение треков плейлиста с подтягиванием обложек и артистов
    @Transaction
    @Query("""
        SELECT tracks.* FROM tracks
        INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.track_id
        WHERE playlist_tracks.playlist_id = :playlistId
        ORDER BY playlist_tracks.position ASC
    """)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackWithCover>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlist_id = :playlistId AND track_id = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlist_id = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?
}