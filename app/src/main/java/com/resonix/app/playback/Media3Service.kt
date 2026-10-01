package com.resonix.app.playback

import android.media.audiofx.AudioEffect
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Media3Service : MediaSessionService() {

    @Inject lateinit var effects: AudioEffectsManager
    @Inject lateinit var sleepTimer: SleepTimerController

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                effects.onTrackChanged(mediaItem)
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != AudioEffect.ERROR_BAD_VALUE && audioSessionId != 0) {
                    effects.attach(audioSessionId)
                }
            }
        })

        effects.bind(player)
        sleepTimer.bind(player)
        session = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        sleepTimer.release()
        effects.release()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
