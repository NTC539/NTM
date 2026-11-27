package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "covers",
    indices = [Index(value = ["path"], unique = true), Index(value = ["hash"])]
)
data class CoverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val mime_type: String,
    val hash: String,
    val updated_at: Long = System.currentTimeMillis()
)
