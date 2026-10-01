package com.resonix.app.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.Equalizer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.resonix.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class EffectsState(
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val eqPreset: Int = -1,
    val eqPresets: List<String> = emptyList(),
    val ambient: Boolean = false,
    val quranProtected: Boolean = false,
)

@Singleton
class AudioEffectsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var player: ExoPlayer? = null
    private var equalizer: Equalizer? = null
    private val ambient = AmbientNoiseMixer()
    private val overrides = ConcurrentHashMap<String, Boolean>()
    private var currentId: String? = null

    private val _state = MutableStateFlow(EffectsState())
    val state: StateFlow<EffectsState> = _state.asStateFlow()

    private val _notices = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val notices: SharedFlow<String> = _notices.asSharedFlow()

    fun bind(p: ExoPlayer) {
        player = p
        attach(p.audioSessionId)
    }

    fun attach(sessionId: Int) {
        try {
            equalizer?.release()
            val eq = Equalizer(0, sessionId).apply { enabled = false }
            equalizer = eq
            val names = (0 until eq.numberOfPresets.toInt()).map { eq.getPresetName(it.toShort()) }
            _state.update { it.copy(eqPresets = names) }
            val s = _state.value
            if (s.eqPreset >= 0 && !s.quranProtected) applyEq(s.eqPreset)
        } catch (_: Exception) {
            equalizer = null
        }
    }

    /** Called by the service on every item transition. */
    fun onTrackChanged(item: MediaItem?) {
        currentId = item?.mediaId
        val override = currentId?.let { overrides[it] }
        applyQuranState(override ?: QuranProtectionInterceptor.isQuran(item))
    }

    /** Called when the user manually marks the current item as Quran / not Quran. */
    fun setOverride(mediaId: String, quran: Boolean) {
        overrides[mediaId] = quran
        if (mediaId == currentId) applyQuranState(quran)
    }

    private fun applyQuranState(quran: Boolean) {
        if (quran) resetAll()
        _state.update { it.copy(quranProtected = quran) }
    }

    private fun resetAll() {
        player?.playbackParameters = PlaybackParameters(1f, 1f)
        equalizer?.enabled = false
        ambient.stop()
        _state.update { it.copy(speed = 1f, pitch = 1f, eqPreset = -1, ambient = false) }
    }

    fun notifyBlocked() {
        _notices.tryEmit(context.getString(R.string.quran_notice))
    }

    private fun guarded(action: () -> Unit) {
        QuranProtectionInterceptor.guard(_state.value.quranProtected, { notifyBlocked() }, action)
    }

    fun setSpeed(v: Float) = guarded {
        _state.update { it.copy(speed = v) }
        player?.playbackParameters = PlaybackParameters(v, _state.value.pitch)
    }

    fun setPitch(v: Float) = guarded {
        _state.update { it.copy(pitch = v) }
        player?.playbackParameters = PlaybackParameters(_state.value.speed, v)
    }

    fun setEqPreset(index: Int) = guarded {
        _state.update { it.copy(eqPreset = index) }
        applyEq(index)
    }

    private fun applyEq(index: Int) {
        try {
            val eq = equalizer ?: return
            if (index < 0) eq.enabled = false else {
                eq.usePreset(index.toShort())
                eq.enabled = true
            }
        } catch (_: Exception) {
        }
    }

    fun setAmbient(on: Boolean) = guarded {
        _state.update { it.copy(ambient = on) }
        if (on) ambient.start() else ambient.stop()
    }

    fun release() {
        ambient.stop()
        equalizer?.release()
        equalizer = null
        player = null
    }
}

/** Soft brown-noise generator streamed through its own AudioTrack, mixed by the system with the music. */
private class AmbientNoiseMixer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch {
            val rate = 22050
            val minBuf = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(rate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track.setVolume(0.12f)
            track.play()
            val buf = ShortArray(2048)
            var last = 0f
            try {
                while (isActive) {
                    for (i in buf.indices) {
                        val white = Random.nextFloat() * 2f - 1f
                        last = (last + 0.02f * white) / 1.02f
                        buf[i] = (last * 3.5f * 16000f).toInt().coerceIn(-32768, 32767).toShort()
                    }
                    track.write(buf, 0, buf.size)
                }
            } finally {
                track.stop()
                track.release()
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
