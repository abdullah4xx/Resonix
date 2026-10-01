package com.resonix.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
    val path: String,
    val folder: String,
    val uri: String,
    val durationMs: Long,
    val dateAdded: Long,
    val isVideo: Boolean = false,
    val isQuran: Boolean = false,
    /** 0 = auto-detect, 1 = user marked as Quran, -1 = user marked as not Quran */
    val quranOverride: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0L,
)
