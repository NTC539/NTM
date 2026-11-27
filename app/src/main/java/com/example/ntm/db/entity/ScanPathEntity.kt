package com.example.ntm.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan_paths",
    indices = [Index(value = ["path"], unique = true)]
)
data class ScanPathEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val enabled: Boolean = true,
    val last_scanned_at: Long? = null
)
