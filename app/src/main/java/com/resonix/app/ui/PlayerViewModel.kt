package com.resonix.app.ui

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.resonix.app.core.EXTRA_IS_QURAN
import com.resonix.app.core.EXTRA_PATH
import com.resonix.app.data.TrackEntity
import com.resonix.app.data.TrackRepository
import com.resonix.app.playback.ArtworkPalette
import com.resonix.app.playback.AudioEffectsManager
import com.resonix.app.playback.Media3Service
import com.resonix.app.playback.SleepTimerController
import com.resonix.app.playback.WaveformExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PlayerUiState(
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val waveform: FloatArray = FloatArray(0),
    val glow: Color? = null,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: TrackRepository,
    val effects: AudioEffectsManager,
    val sleepTimer: SleepTimerController,
) : ViewModel() {

    val tracks: StateFlow<List<TrackEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _ui = MutableStateFlow(PlayerUiState())
    val ui: StateFlow<PlayerUiState> = _ui.asStateFlow()

    private var controller: MediaController? = null
    private var visualsJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = sync()
        override fun onIsPlayingChanged(isPlaying: Boolean) = sync()
        override fun onPlaybackStateChanged(playbackState: Int) = sync()
    }

    init {
        val token = SessionToken(context, ComponentName(context, Media3Service::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            sync()
        }, ContextCompat.getMainExecutor(context))

        viewModelScope.launch {
            while (isActive) {
                controller?.let { c ->
                    _ui.update { it.copy(positionMs = c.currentPosition, durationMs = c.duration.coerceAtLeast(0)) }
                }
                delay(250)
            }
        }
    }

    fun scan() {
        viewModelScope.launch { repo.scanDevice() }
    }

    private fun sync() {
        val c = controller ?: return
        val item = c.currentMediaItem
        val id = item?.mediaId
        val changed = id != _ui.value.mediaId
        _ui.update {
            it.copy(
                mediaId = id,
                title = item?.mediaMetadata?.title?.toString().orEmpty(),
                artist = item?.mediaMetadata?.artist?.toString().orEmpty(),
                isPlaying = c.isPlaying,
            )
        }
        if (changed && item != null) {
            id?.toLongOrNull()?.let { viewModelScope.launch { repo.markPlayed(it) } }
            loadVisuals(item)
        }
    }

    private fun loadVisuals(item: MediaItem) {
        visualsJob?.cancel()
        visualsJob = viewModelScope.launch {
            _ui.update { it.copy(waveform = FloatArray(0), glow = null) }
            val uri: Uri = item.localConfiguration?.uri ?: return@launch
            val glow = withContext(Dispatchers.IO) { ArtworkPalette.dominant(context, uri) }
            _ui.update { it.copy(glow = glow?.let { g -> Color(g) }) }
            val wf = WaveformExtractor.extract(context, uri)
            _ui.update { it.copy(waveform = wf) }
        }
    }

    private fun TrackEntity.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(uri))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setExtras(Bundle().apply {
                    putBoolean(EXTRA_IS_QURAN, isQuran)
                    putString(EXTRA_PATH, path)
                })
                .build()
        )
        .build()

    fun play(list: List<TrackEntity>, startIndex: Int) {
        val c = controller ?: return
        if (list.isEmpty()) return
        c.shuffleModeEnabled = false
        c.setMediaItems(list.map { it.toMediaItem() }, startIndex, 0L)
        c.prepare()
        c.play()
    }

    fun shuffleAll(list: List<TrackEntity>) = play(list.shuffled(), 0)

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch { repo.setFavorite(track.id, !track.isFavorite) }
    }

    fun setQuranFlag(track: TrackEntity, quran: Boolean) {
        viewModelScope.launch { repo.setQuranOverride(track.id, quran) }
        effects.setOverride(track.id.toString(), quran)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()
        super.onCleared()
    }
}
