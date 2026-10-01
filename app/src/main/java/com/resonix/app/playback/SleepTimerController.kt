package com.resonix.app.playback

import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Smart sleep timer: when armed, it does nothing until the CURRENT track/Surah is about to finish,
 * then fades the volume to zero over the last [FADE_MS] and pauses exactly at the end.
 */
@Singleton
class SleepTimerController @Inject constructor() {
    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val _armed = MutableStateFlow(false)
    val armed: StateFlow<Boolean> = _armed.asStateFlow()

    private val tick = object : Runnable {
        override fun run() {
            val p = player ?: return
            if (!_armed.value) return
            val duration = p.duration
            if (duration != C.TIME_UNSET && p.isPlaying) {
                val remaining = duration - p.currentPosition
                if (remaining <= FADE_MS) p.volume = (remaining.coerceAtLeast(0) / FADE_MS.toFloat())
                if (remaining <= 250) {
                    p.pause()
                    p.volume = 1f
                    if (p.hasNextMediaItem()) p.seekToNextMediaItem() else p.seekTo(0)
                    p.pause()
                    _armed.value = false
                    return
                }
            }
            handler.postDelayed(this, 100)
        }
    }

    fun bind(p: ExoPlayer) { player = p }

    fun arm() {
        if (_armed.value) return
        _armed.value = true
        handler.post(tick)
    }

    fun disarm() {
        _armed.value = false
        handler.removeCallbacks(tick)
        player?.volume = 1f
    }

    fun release() {
        disarm()
        player = null
    }

    private companion object { const val FADE_MS = 4000L }
}
