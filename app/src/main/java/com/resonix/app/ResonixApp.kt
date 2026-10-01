package com.resonix.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.resonix.app.core.LocaleManager
import com.resonix.app.core.NOTIF_CHANNEL_DOWNLOADS
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ResonixApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                NOTIF_CHANNEL_DOWNLOADS,
                getString(R.string.notif_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
    }
}
