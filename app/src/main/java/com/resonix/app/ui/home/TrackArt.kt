package com.resonix.app.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.resonix.app.data.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads cover art / video thumbnails without any extra library. */
object ArtLoader {
    private val cache = LruCache<String, Bitmap>(64)

    suspend fun load(ctx: Context, uri: String, path: String, isVideo: Boolean, px: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            cache.get(uri)?.let { return@withContext it }
            var bmp: Bitmap? = null
            if (Build.VERSION.SDK_INT >= 29) {
                bmp = runCatching { ctx.contentResolver.loadThumbnail(Uri.parse(uri), Size(px, px), null) }.getOrNull()
            }
            if (bmp == null) {
                val r = MediaMetadataRetriever()
                try {
                    if (uri.startsWith("file:")) r.setDataSource(path) else r.setDataSource(ctx, Uri.parse(uri))
                    val bytes = r.embeddedPicture
                    bmp = when {
                        bytes != null -> decodeScaled(bytes, px)
                        isVideo -> r.getFrameAtTime(0)
                        else -> null
                    }
                } catch (_: Exception) {
                } finally {
                    r.release()
                }
            }
            bmp?.also { cache.put(uri, it) }
        }

    private fun decodeScaled(bytes: ByteArray, px: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= px && bounds.outHeight / (sample * 2) >= px) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}

@Composable
fun TrackArt(track: TrackEntity?, size: Dp, modifier: Modifier = Modifier, corner: Dp = 10.dp) {
    val ctx = LocalContext.current
    val px = with(LocalDensity.current) { size.roundToPx() }
    val bmp by produceState<Bitmap?>(null, track?.uri) {
        value = track?.let { ArtLoader.load(ctx, it.uri, it.path, it.isVideo, px) }
    }
    Box(
        modifier.size(size).clip(RoundedCornerShape(corner)).background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val b = bmp
        if (b != null) {
            Image(b.asImageBitmap(), null, Modifier.size(size), contentScale = ContentScale.Crop)
        } else {
            Icon(
                if (track?.isQuran == true) Icons.Default.MenuBook else Icons.Default.MusicNote,
                null,
                Modifier.size(size / 2),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            )
        }
    }
}
