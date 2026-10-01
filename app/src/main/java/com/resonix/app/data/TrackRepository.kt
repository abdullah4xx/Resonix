package com.resonix.app.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.resonix.app.R
import com.resonix.app.core.VIDEO_ID_OFFSET
import com.resonix.app.playback.QuranProtectionInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import android.provider.MediaStore.Audio.Media as A
import android.provider.MediaStore.Video.Media as V

@Singleton
class TrackRepository @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val dao: TrackDao,
) {
    fun observeAll(): Flow<List<TrackEntity>> = dao.observeAll()
    suspend fun setFavorite(id: Long, fav: Boolean) = dao.setFavorite(id, fav)
    suspend fun markPlayed(id: Long) = dao.markPlayed(id, System.currentTimeMillis())
    suspend fun setQuranOverride(id: Long, quran: Boolean) =
        dao.setQuran(id, if (quran) 1 else -1, quran)

    suspend fun scanDevice() = withContext(Dispatchers.IO) {
        val existing = dao.getAll().associateBy { it.id }
        val scanned = scanAudio() + scanVideo()
        val merged = scanned.map { n ->
            val old = existing[n.id] ?: return@map n
            n.copy(
                isFavorite = old.isFavorite,
                lastPlayed = old.lastPlayed,
                quranOverride = old.quranOverride,
                isQuran = when (old.quranOverride) { 1 -> true; -1 -> false; else -> n.isQuran },
            )
        }
        dao.upsertAll(merged)
        val seen = scanned.map { it.id }.toHashSet()
        existing.values.filter { it.id > 0 && it.id !in seen }.map { it.id }
            .chunked(500).forEach { dao.delete(it) }
    }

    /** Registers a file saved by the download service. */
    suspend fun addDownloadedFile(file: File, asQuran: Boolean) = withContext(Dispatchers.IO) {
        val r = MediaMetadataRetriever()
        var title = file.nameWithoutExtension
        var artist = ctx.getString(R.string.unknown_artist)
        var album = ctx.getString(if (asQuran) R.string.quran_album else R.string.downloads_album)
        var genre = ""
        var duration = 0L
        try {
            r.setDataSource(file.absolutePath)
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { it.isNotBlank() }?.let { title = it }
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.takeIf { it.isNotBlank() }?.let { artist = it }
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.takeIf { it.isNotBlank() }?.let { album = it }
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)?.let { genre = it }
            duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
        } finally {
            r.release()
        }
        val quran = asQuran || QuranProtectionInterceptor.detect(title, artist, album, genre, file.absolutePath)
        dao.upsertOne(
            TrackEntity(
                id = -System.currentTimeMillis(),
                title = title, artist = artist, album = album, genre = genre,
                path = file.absolutePath,
                folder = file.parentFile?.name ?: ctx.getString(R.string.downloads_album),
                uri = Uri.fromFile(file).toString(),
                durationMs = duration, dateAdded = System.currentTimeMillis() / 1000,
                isQuran = quran, quranOverride = if (asQuran) 1 else 0,
            )
        )
    }

    private fun scanAudio(): List<TrackEntity> {
        val collection = A.EXTERNAL_CONTENT_URI
        val cols = arrayOf(A._ID, A.TITLE, A.ARTIST, A.ALBUM, A.DATA, A.DURATION, A.DATE_ADDED) +
            (if (Build.VERSION.SDK_INT >= 30) arrayOf("genre") else emptyArray<String>())
        val selection = "${A.DURATION} > 1000 AND ${A.IS_RINGTONE} = 0 AND " +
            "${A.IS_NOTIFICATION} = 0 AND ${A.IS_ALARM} = 0"
        val unknownArtist = ctx.getString(R.string.unknown_artist)
        val unknownAlbum = ctx.getString(R.string.unknown_album)
        val unknownFolder = ctx.getString(R.string.unknown_folder)
        val out = mutableListOf<TrackEntity>()
        ctx.contentResolver.query(collection, cols, selection, null, null)?.use { c ->
            val iId = c.getColumnIndexOrThrow(A._ID)
            val iTitle = c.getColumnIndexOrThrow(A.TITLE)
            val iArtist = c.getColumnIndexOrThrow(A.ARTIST)
            val iAlbum = c.getColumnIndexOrThrow(A.ALBUM)
            val iData = c.getColumnIndexOrThrow(A.DATA)
            val iDur = c.getColumnIndexOrThrow(A.DURATION)
            val iAdded = c.getColumnIndexOrThrow(A.DATE_ADDED)
            val iGenre = c.getColumnIndex("genre")
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                val path = c.getString(iData).orEmpty()
                val title = c.getString(iTitle) ?: File(path).nameWithoutExtension
                val artist = c.getString(iArtist)?.takeUnless { it == "<unknown>" } ?: unknownArtist
                val album = c.getString(iAlbum) ?: unknownAlbum
                val genre = if (iGenre >= 0) c.getString(iGenre).orEmpty() else ""
                out += TrackEntity(
                    id = id, title = title, artist = artist, album = album, genre = genre,
                    path = path, folder = File(path).parentFile?.name ?: unknownFolder,
                    uri = ContentUris.withAppendedId(collection, id).toString(),
                    durationMs = c.getLong(iDur), dateAdded = c.getLong(iAdded),
                    isQuran = QuranProtectionInterceptor.detect(title, artist, album, genre, path),
                )
            }
        }
        return out
    }

    private fun scanVideo(): List<TrackEntity> {
        val collection = V.EXTERNAL_CONTENT_URI
        val cols = arrayOf(V._ID, V.TITLE, V.DATA, V.DURATION, V.DATE_ADDED)
        val videoLabel = ctx.getString(R.string.video_label)
        val videosAlbum = ctx.getString(R.string.videos_album)
        val unknownFolder = ctx.getString(R.string.unknown_folder)
        val out = mutableListOf<TrackEntity>()
        try {
            ctx.contentResolver.query(collection, cols, null, null, null)?.use { c ->
                val iId = c.getColumnIndexOrThrow(V._ID)
                val iTitle = c.getColumnIndexOrThrow(V.TITLE)
                val iData = c.getColumnIndexOrThrow(V.DATA)
                val iDur = c.getColumnIndexOrThrow(V.DURATION)
                val iAdded = c.getColumnIndexOrThrow(V.DATE_ADDED)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    val path = c.getString(iData).orEmpty()
                    out += TrackEntity(
                        id = VIDEO_ID_OFFSET + id,
                        title = c.getString(iTitle) ?: File(path).nameWithoutExtension,
                        artist = videoLabel, album = videosAlbum, genre = "", path = path,
                        folder = File(path).parentFile?.name ?: unknownFolder,
                        uri = ContentUris.withAppendedId(collection, id).toString(),
                        durationMs = c.getLong(iDur), dateAdded = c.getLong(iAdded), isVideo = true,
                    )
                }
            }
        } catch (_: SecurityException) {
            // READ_MEDIA_VIDEO not granted: the Videos card simply stays empty.
        }
        return out
    }
}
