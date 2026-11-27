package com.example.ntm.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.ntm.db.dao.*
import com.example.ntm.db.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class MediaScanner(
    private val context: Context,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val coverDao: CoverDao,
    private val scanPathDao: ScanPathDao,
    private val excludedPathDao: ExcludedPathDao
) {

    suspend fun scanMediaStore(): Boolean = withContext(Dispatchers.IO) {
        val activePaths = scanPathDao.getActivePaths().map { it.path }
        // Если пути не заданы, сканируем стандартную папку Music
        val pathsToScan = if (activePaths.isEmpty()) {
            listOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath)
        } else activePaths

        val excludedPaths = excludedPathDao.getExcludedPaths().map { it.path }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val foundTrackIds = mutableSetOf<Long>()

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sysAlbumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol)

                    // Фильтрация путей
                    val isInScanPath = pathsToScan.any { path.startsWith(it) }
                    val isExcluded = excludedPaths.any { path.startsWith(it) }
                    if (!isInScanPath || isExcluded) continue

                    val title = cursor.getString(titleCol) ?: "Unknown"
                    val artistName = cursor.getString(artistCol) ?: "Unknown Artist"
                    val albumName = cursor.getString(albumCol) ?: "Unknown Album"
                    val mime = cursor.getString(mimeCol)
                    val dateAdded = cursor.getLong(dateAddedCol) * 1000
                    val dateMod = cursor.getLong(dateModCol) * 1000
                    val sysAlbumId = cursor.getLong(sysAlbumIdCol)

                    val existingTrack = trackDao.getTrackByPath(path)
                    if (existingTrack != null && existingTrack.file_modified_at == dateMod && !existingTrack.is_missing) {
                        foundTrackIds.add(existingTrack.id)
                        continue
                    }

                    val artistId = getOrCreateArtist(artistName)
                    val albumId = getOrCreateAlbum(albumName, artistId)
                    val coverId = extractAndSaveCover(path, sysAlbumId)

                    val track = TrackEntity(
                        id = existingTrack?.id ?: 0,
                        path = path,
                        title = title,
                        artist_id = artistId,
                        album_id = albumId,
                        mime_type = mime,
                        cover_id = coverId ?: existingTrack?.cover_id,
                        date_added = dateAdded,
                        file_modified_at = dateMod,
                        updated_at = System.currentTimeMillis(),
                        is_missing = false
                    )

                    if (existingTrack == null) {
                        val newId = trackDao.insert(track)
                        foundTrackIds.add(newId)
                    } else {
                        trackDao.update(track)
                        foundTrackIds.add(track.id)
                    }
                }
            }

            // Помечаем отсутствующие треки
            val allDbTracks = trackDao.getAllTracksList()
            for (dbTrack in allDbTracks) {
                if (!foundTrackIds.contains(dbTrack.id) && !dbTrack.is_missing) {
                    trackDao.setMissingStatus(dbTrack.id, true)
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    private suspend fun getOrCreateArtist(name: String): Long {
        return artistDao.getArtistByName(name)?.id ?: artistDao.insert(ArtistEntity(name = name))
    }

    private suspend fun getOrCreateAlbum(title: String, artistId: Long): Long {
        return albumDao.getAlbumByTitleAndArtist(title, artistId)?.id
            ?: albumDao.insert(AlbumEntity(title = title, artist_id = artistId))
    }

    private suspend fun extractAndSaveCover(filePath: String, sysAlbumId: Long): Long? {
        val file = File(filePath)
        val hash = generateHash(filePath)

        // Проверяем, есть ли уже такая обложка в БД
        coverDao.getCoverByHash(hash)?.let { return it.id }

        val coversDir = File(context.filesDir, "covers")
        if (!coversDir.exists()) coversDir.mkdirs()
        val coverFile = File(coversDir, "cover_$hash.jpg")

        var success = false
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(filePath)
            val picture = retriever.embeddedPicture

            // ВАЖНО: Проверяем, что массив байт не пустой
            if (picture != null && picture.isNotEmpty()) {
                FileOutputStream(coverFile).use { output ->
                    output.write(picture)
                }
                // Проверяем, что файл действительно записался и имеет размер
                if (coverFile.exists() && coverFile.length() > 0) {
                    success = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        return if (success) {
            val cover = CoverEntity(
                path = coverFile.absolutePath,
                mime_type = "image/jpeg",
                hash = hash
            )
            coverDao.insert(cover)
        } else null
    }

    private fun generateHash(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}