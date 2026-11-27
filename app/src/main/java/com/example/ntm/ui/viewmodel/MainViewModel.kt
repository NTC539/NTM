package com.example.ntm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ntm.data.MediaScanner
import com.example.ntm.db.AppDatabase
import com.example.ntm.db.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)

    private val mediaScanner = MediaScanner(
        application, db.trackDao(), db.artistDao(), db.albumDao(),
        db.coverDao(), db.scanPathDao(), db.excludedPathDao()
    )

    val tracks = db.trackDao().getAllTracks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val tracksWithCovers = db.trackDao().getAllTracksWithCover()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val scanPaths = db.scanPathDao().getAllPaths().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val excludedPaths = db.excludedPathDao().getAllExcludedPaths().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    fun scanMusic() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            mediaScanner.scanMediaStore()
            _isScanning.value = false
        }
    }

    fun addScanPath(path: String) {
        if (path.isBlank()) return
        viewModelScope.launch {
            db.scanPathDao().insert(ScanPathEntity(path = path))
        }
    }

    fun removeScanPath(path: String) {
        viewModelScope.launch { db.scanPathDao().deleteByPath(path) }
    }

    fun addExcludedPath(path: String) {
        if (path.isBlank()) return
        viewModelScope.launch {
            db.excludedPathDao().insert(ExcludedPathEntity(path = path))
        }
    }

    fun removeExcludedPath(path: String) {
        viewModelScope.launch { db.excludedPathDao().deleteByPath(path) }
    }

    suspend fun isLibraryEmpty(): Boolean {
        return db.trackDao().getAllTracksList().isEmpty()
    }

    fun clearMediaLibrary() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Очищаем базу данных
                db.clearAllTables()

                // 2. Удаляем файлы кэша обложек
                val coversDir = java.io.File(getApplication<Application>().filesDir, "covers")
                if (coversDir.exists()) {
                    coversDir.deleteRecursively()
                }
            }
        }
    }
}