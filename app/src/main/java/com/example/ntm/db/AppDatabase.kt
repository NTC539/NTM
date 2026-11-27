package com.example.ntm.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ntm.db.dao.*
import com.example.ntm.db.entity.*

@Database(
    entities = [
        ArtistEntity::class,
        CoverEntity::class,
        AlbumEntity::class,
        SourceEntity::class,
        TrackEntity::class,
        LyricEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        QueueItemEntity::class,
        ListeningHistoryEntity::class,
        UserSettingEntity::class,
        ExcludedPathEntity::class,
        ScanPathEntity::class
    ],
    version = 2, // Увеличили версию, так как изменили структуру
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackDao(): TrackDao
    abstract fun coverDao(): CoverDao
    abstract fun scanPathDao(): ScanPathDao
    abstract fun excludedPathDao(): ExcludedPathDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ntm_music_database"
                )
                    .fallbackToDestructiveMigration() // Пока используем это, чтобы не писать миграции
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}