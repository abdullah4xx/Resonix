package com.resonix.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAll(): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOne(item: TrackEntity)

    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("UPDATE tracks SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE tracks SET lastPlayed = :time WHERE id = :id")
    suspend fun markPlayed(id: Long, time: Long)

    @Query("UPDATE tracks SET quranOverride = :override, isQuran = :quran WHERE id = :id")
    suspend fun setQuran(id: Long, override: Int, quran: Boolean)
}
