package com.resonix.app.download

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.resonix.app.R
import com.resonix.app.core.NOTIF_CHANNEL_DOWNLOADS
import com.resonix.app.data.TrackRepository
import com.resonix.app.core.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

enum class DownloadStatus { RUNNING, DONE, FAILED }

data class DownloadItem(
    val id: Long,
    val title: String,
    val progress: Int,
    val status: DownloadStatus,
    val error: String? = null,
)

@Singleton
class DownloadTracker @Inject constructor() {
    private val ids = AtomicLong(1)
    private val _items = MutableStateFlow<List<DownloadItem>>(emptyList())
    val items: StateFlow<List<DownloadItem>> = _items.asStateFlow()

    fun add(title: String): Long {
        val id = ids.getAndIncrement()
        _items.update { it + DownloadItem(id, title, 0, DownloadStatus.RUNNING) }
        return id
    }

    fun progress(id: Long, p: Int) = _items.update { l -> l.map { if (it.id == id) it.copy(progress = p) else it } }
    fun finish(id: Long) = _items.update { l -> l.map { if (it.id == id) it.copy(progress = 100, status = DownloadStatus.DONE) else it } }
    fun fail(id: Long, msg: String?) = _items.update { l -> l.map { if (it.id == id) it.copy(status = DownloadStatus.FAILED, error = msg) else it } }
    fun clearFinished() = _items.update { l -> l.filter { it.status == DownloadStatus.RUNNING } }
}

/**
 * Background download manager for DIRECT audio file URLs the user has the right to save.
 * It does not extract streams from YouTube or any streaming service.
 */
@AndroidEntryPoint
class DownloadService : Service() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrap(base))
    }

    @Inject lateinit var repository: TrackRepository
    @Inject lateinit var tracker: DownloadTracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val active = AtomicInteger(0)
    private val notifications by lazy { getSystemService(NotificationManager::class.java) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            if (active.get() == 0) stopSelf()
            return START_NOT_STICKY
        }
        val title = intent.let { it.getStringExtra(EXTRA_TITLE) }?.ifBlank { null }
            ?: getString(R.string.dl_default_name)
        val quran = intent.getBooleanExtra(EXTRA_QURAN, false)

        val type = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
        ServiceCompat.startForeground(this, NOTIF_ID, notification(getString(R.string.notif_starting), 0, true), type)

        active.incrementAndGet()
        scope.launch { download(url, title, quran) }
        return START_NOT_STICKY
    }

    private suspend fun download(url: String, title: String, quran: Boolean) {
        val id = tracker.add(title)
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Resonix/1.0")
            }
            conn.connect()
            if (conn.responseCode !in 200..299) error(getString(R.string.err_http, conn.responseCode))
            val type = conn.contentType?.lowercase().orEmpty()
            if (type.startsWith("text/") || type.contains("html")) error(getString(R.string.err_not_audio))

            val dir = File(
                getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                if (quran) "Quran" else "Music",
            ).apply { mkdirs() }
            val file = uniqueFile(dir, sanitize(title), extensionFor(type, url))
            val total = conn.contentLengthLong

            conn.inputStream.use { input ->
                file.outputStream().use { out ->
                    val buf = ByteArray(16 * 1024)
                    var read = 0L
                    var lastUpdate = 0L
                    var n: Int
                    while (input.read(buf).also { n = it } >= 0) {
                        coroutineContext.ensureActive()
                        out.write(buf, 0, n)
                        read += n
                        val now = System.currentTimeMillis()
                        if (total > 0 && now - lastUpdate > 500) {
                            lastUpdate = now
                            val p = (read * 100 / total).toInt()
                            tracker.progress(id, p)
                            notifications.notify(NOTIF_ID, notification(title, p, false))
                        }
                    }
                }
            }
            repository.addDownloadedFile(file, quran)
            tracker.finish(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            tracker.fail(id, e.message ?: getString(R.string.err_generic))
        } finally {
            if (active.decrementAndGet() == 0) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun notification(text: String, progress: Int, indeterminate: Boolean): Notification =
        NotificationCompat.Builder(this, NOTIF_CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(text)
            .setProgress(100, progress, indeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    private fun sanitize(name: String) =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().take(80).ifBlank { "track" }

    private fun uniqueFile(dir: File, base: String, ext: String): File {
        var f = File(dir, "$base.$ext")
        var i = 1
        while (f.exists()) f = File(dir, "$base ($i).$ext").also { i++ }
        return f
    }

    private fun extensionFor(type: String, url: String): String = when {
        type.contains("mpeg") || type.contains("mp3") -> "mp3"
        type.contains("mp4") || type.contains("m4a") || type.contains("aac") -> "m4a"
        type.contains("ogg") || type.contains("opus") -> "ogg"
        type.contains("wav") -> "wav"
        type.contains("flac") -> "flac"
        else -> url.substringBefore('?').substringAfterLast('.', "mp3").lowercase().takeIf { it.length in 2..4 } ?: "mp3"
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_QURAN = "quran"
        private const val NOTIF_ID = 4201
    }
}
