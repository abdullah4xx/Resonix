package com.resonix.app.playback

import androidx.media3.common.MediaItem
import com.resonix.app.core.EXTRA_IS_QURAN

/**
 * Single source of truth for the Quran protection rule.
 * Audio is flagged by metadata (title/artist/album/genre), by file path, or by the user's manual override.
 * While a Quran item is active, every effect (ambient noise, filters, pitch, speed, equalizer) is blocked.
 */
object QuranProtectionInterceptor {

    private val keywords = listOf(
        "quran", "qur'an", "koran", "surah", "surat ", "tilawa", "tajweed", "tartil", "mushaf",
        "قرآن", "القرآن", "قران", "سورة", "تلاوة", "تجويد", "ترتيل", "مصحف", "المصحف",
    )

    fun detect(vararg fields: String?): Boolean {
        val hay = fields.filterNotNull().joinToString(" ").lowercase()
        return keywords.any { hay.contains(it) }
    }

    fun isQuran(item: MediaItem?): Boolean =
        item?.mediaMetadata?.extras?.getBoolean(EXTRA_IS_QURAN, false) == true

    /** Runs [action] only when [quran] is false; otherwise calls [onBlocked] and returns false. */
    inline fun guard(quran: Boolean, onBlocked: () -> Unit, action: () -> Unit): Boolean {
        if (quran) {
            onBlocked()
            return false
        }
        action()
        return true
    }
}
