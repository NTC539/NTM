package com.example.ntm.db.dao

import androidx.room.*
import com.example.ntm.db.entity.ExcludedPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcludedPathDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(path: ExcludedPathEntity): Long

    @Query("DELETE FROM excluded_paths WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("SELECT * FROM excluded_paths")
    suspend fun getExcludedPaths(): List<ExcludedPathEntity>

    @Query("SELECT * FROM excluded_paths")
    fun getAllExcludedPaths(): Flow<List<ExcludedPathEntity>>
}