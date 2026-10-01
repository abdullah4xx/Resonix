package com.resonix.app.playback

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.palette.graphics.Palette

object ArtworkPalette {
    /** Returns an ARGB color from the embedded cover art, or null if there is none. */
    fun dominant(context: Context, uri: Uri): Int? {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(context, uri)
            val bytes = r.embeddedPicture ?: return null
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val p = Palette.from(bmp).generate()
            p.getVibrantColor(p.getDominantColor(0)).takeIf { it != 0 }
        } catch (_: Exception) {
            null
        } finally {
            r.release()
        }
    }
}
