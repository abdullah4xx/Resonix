package com.resonix.app.core

import java.util.Locale

internal const val DEVELOPER_AUTHOR = "Abdullah"

const val EXTRA_IS_QURAN = "resonix.is_quran"
const val EXTRA_PATH = "resonix.path"
const val NOTIF_CHANNEL_DOWNLOADS = "resonix_downloads"
const val VIDEO_ID_OFFSET = 1_000_000_000_000L

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%d:%02d", m, s)
}
