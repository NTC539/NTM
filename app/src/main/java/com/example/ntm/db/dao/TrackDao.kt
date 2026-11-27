package com.example.ntm.db.dao

import androidx.room.*
import com.example.ntm.db.entity.TrackEntity
import com.example.ntm.db.entity.TrackWithCover
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: TrackEntity): Long

    @Update
    suspend fun update(track: TrackEntity)

    @Query("UPDATE tracks SET is_missing = :isMissing WHERE id = :trackId")
    suspend fun setMissingStatus(trackId: Long, isMissing: Boolean)

    @Delete
    suspend fun delete(track: TrackEntity)

    @Query("SELECT * FROM tracks WHERE is_missing = 0 ORDER BY title ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE is_missing = 0 AND (title LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%') ORDER BY title ASC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE path = :path LIMIT 1")
    suspend fun getTrackByPath(path: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE artist_id = :artistId AND is_missing = 0 ORDER BY album_id, title ASC")
    fun getTracksByArtist(artistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album_id = :albumId AND is_missing = 0 ORDER BY title ASC")
    fun getTracksByAlbum(albumId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksList(): List<TrackEntity>

    @Transaction
    @Query("SELECT * FROM tracks WHERE is_missing = 0 ORDER BY title ASC")
    fun getAllTracksWithCover(): Flow<List<TrackWithCover>>
}
