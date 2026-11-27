package com.example.ntm.db.dao

import androidx.room.*
import com.example.ntm.db.entity.ScanPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanPathDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(path: ScanPathEntity): Long

    @Query("DELETE FROM scan_paths WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("SELECT * FROM scan_paths WHERE enabled = 1")
    suspend fun getActivePaths(): List<ScanPathEntity>

    @Query("SELECT * FROM scan_paths")
    fun getAllPaths(): Flow<List<ScanPathEntity>>
}