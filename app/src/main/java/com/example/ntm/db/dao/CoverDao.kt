package com.example.ntm.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ntm.db.entity.CoverEntity

@Dao
interface CoverDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cover: CoverEntity): Long

    @Query("SELECT * FROM covers WHERE hash = :hash LIMIT 1")
    suspend fun getCoverByHash(hash: String): CoverEntity?
}