package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "excluded_paths",
    indices = [Index(value = ["path"], unique = true)]
)
data class ExcludedPathEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val created_at: Long = System.currentTimeMillis()
)
