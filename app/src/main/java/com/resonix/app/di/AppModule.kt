package com.resonix.app.di

import android.content.Context
import androidx.room.Room
import com.resonix.app.data.ResonixDatabase
import com.resonix.app.data.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ResonixDatabase =
        Room.databaseBuilder(context, ResonixDatabase::class.java, "resonix.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackDao(db: ResonixDatabase): TrackDao = db.trackDao()
}
