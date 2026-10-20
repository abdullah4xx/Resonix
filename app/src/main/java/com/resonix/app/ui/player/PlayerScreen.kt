package com.resonix.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.R
import com.resonix.app.ui.PlayerViewModel
import com.resonix.app.ui.home.TrackArt
import com.resonix.app.ui.theme.LocalThemePreset
import com.resonix.app.ui.theme.ThemePreset
import com.resonix.app.ui.theme.themeCardShape
import com.resonix.app.ui.theme.themeUsesNeumorphism
import com.resonix.app.ui.theme.themeUsesOutline
import java.util.Locale

@Composable
fun PlayerScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val fx by vm.effects.state.collectAsStateWithLifecycle()
    val armed by vm.sleepTimer.armed.collectAsStateWithLifecycle()
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val current = tracks.firstOrNull { it.id.toString() == ui.mediaId }
    val snack = remember { SnackbarHostState() }
    val preset = LocalThemePreset.current

    LaunchedEffect(Unit) { vm.effects.notices.collect { snack.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.cd_close)) }
                Text(
                    stringResource(R.string.player_now_playing), Modifier.weight(1f),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.size(48.dp))
            }

            if (themeUsesNeumorphism(preset)) {
                NeumorphicPlayerLayout(
                    track = current,
                    title = ui.title,
                    artist = ui.artist,
                    isPlaying = ui.isPlaying,
                    isFavorite = current?.isFavorite == true,
                    quranProtected = fx.quranProtected,
                    waveform = ui.waveform,
                    positionMs = ui.positionMs,
                    durationMs = ui.durationMs,
                    onFavorite = { current?.let(vm::toggleFavorite) },
                    onPrevious = vm::previous,
                    onPlayPause = vm::togglePlay,
                    onNext = vm::next,
                    onSeek = vm::seekTo,
                )
            } else {
                val artCorner = when (preset) {
                    ThemePreset.VIOLET_CURRENT -> 999.dp
                    ThemePreset.OUTLINE_MONO -> 12.dp
                    ThemePreset.PETAL_PLAYER -> 40.dp
                    else -> 30.dp
                }
                val artShape = if (artCorner == 999.dp) CircleShape else RoundedCornerShape(artCorner)
                TrackArt(
                    current,
                    280.dp,
                    Modifier.align(Alignment.CenterHorizontally)
                        .shadow(if (themeUsesNeumorphism(preset)) 10.dp else 0.dp, artShape),
                    corner = artCorner,
                )

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ui.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                    Text(ui.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (fx.quranProtected) {
                        Text(
                            stringResource(R.string.quran_protection_active),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                // Time flows left-to-right even in Arabic, so the waveform and media controls stay LTR.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        InteractiveWaveformSeekbar(ui.waveform, ui.positionMs, ui.durationMs, onSeek = vm::seekTo)

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { current?.let(vm::toggleFavorite) }) {
                                Icon(
                                    if (current?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    stringResource(R.string.cd_favorite), tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(onClick = vm::previous) {
                                Icon(Icons.Default.SkipPrevious, stringResource(R.string.cd_previous), Modifier.size(36.dp))
                            }
                            IconButton(
                                onClick = vm::togglePlay,
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            ) {
                                Icon(
                                    if (ui.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    stringResource(R.string.cd_play_pause),
                                    Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                            IconButton(onClick = vm::next) {
                                Icon(Icons.Default.SkipNext, stringResource(R.string.cd_next), Modifier.size(36.dp))
                            }
                            Spacer(Modifier.size(48.dp))
                        }
                    }
                }
            }

            SettingRow(
                title = stringResource(R.string.sleep_timer),
                subtitle = stringResource(if (fx.quranProtected) R.string.sleep_timer_desc_surah else R.string.sleep_timer_desc_track),
                checked = armed,
                onChecked = { if (it) vm.sleepTimer.arm() else vm.sleepTimer.disarm() },
            )

            SettingRow(
                title = stringResource(R.string.mark_quran),
                subtitle = stringResource(R.string.mark_quran_desc),
                checked = fx.quranProtected,
                enabled = current != null,
                onChecked = { current?.let { t -> vm.setQuranFlag(t, it) } },
            )

            Card(
                Modifier.fillMaxWidth()
                    .then(
                        if (themeUsesOutline(preset)) {
                            Modifier.border(1.dp, MaterialTheme.colorScheme.outline, themeCardShape(preset))
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = fx.quranProtected) { vm.effects.notifyBlocked() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = themeCardShape(preset),
            ) {
                val q = fx.quranProtected
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.effects_title), style = MaterialTheme.typography.titleMedium)
                    if (q) {
                        Text(
                            stringResource(R.string.quran_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Text(
                        stringResource(R.string.speed_fmt, "%.2f".format(Locale.US, fx.speed)),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(fx.speed, { vm.effects.setSpeed(it) }, valueRange = 0.5f..2f, enabled = !q)

                    Text(
                        stringResource(R.string.pitch_fmt, "%.2f".format(Locale.US, fx.pitch)),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Slider(fx.pitch, { vm.effects.setPitch(it) }, valueRange = 0.5f..2f, enabled = !q)

                    Text(stringResource(R.string.equalizer), style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = fx.eqPreset < 0, enabled = !q,
                            onClick = { vm.effects.setEqPreset(-1) },
                            label = { Text(stringResource(R.string.eq_off)) },
                        )
                        fx.eqPresets.forEachIndexed { i, name ->
                            FilterChip(selected = fx.eqPreset == i, enabled = !q, onClick = { vm.effects.setEqPreset(i) }, label = { Text(name) })
                        }
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.ambient_noise), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Switch(fx.ambient, { vm.effects.setAmbient(it) }, enabled = !q)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NeumorphicPlayerLayout(
    track: com.resonix.app.data.TrackEntity?,
    title: String,
    artist: String,
    isPlaying: Boolean,
    isFavorite: Boolean,
    quranProtected: Boolean,
    waveform: FloatArray,
    positionMs: Long,
    durationMs: Long,
    onFavorite: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    val raisedSurface = MaterialTheme.colorScheme.surfaceVariant
    val edge = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 2)
            Text(artist, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (quranProtected) {
                Text(
                    stringResource(R.string.quran_protection_active),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            InteractiveWaveformSeekbar(waveform, positionMs, durationMs, onSeek = onSeek)
        }

        BoxWithConstraints(Modifier.fillMaxWidth().height(380.dp)) {
            val railShape = RoundedCornerShape(25.dp)
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                Column(
                    Modifier.width(72.dp).fillMaxHeight()
                        .shadow(
                            10.dp,
                            railShape,
                            ambientColor = surface.copy(alpha = 0.9f),
                            spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                        )
                        .clip(railShape)
                        .background(raisedSurface)
                        .border(2.dp, surface.copy(alpha = 0.9f), railShape)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconButton(onClick = onFavorite, modifier = Modifier.size(48.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            stringResource(R.string.cd_favorite),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipNext, stringResource(R.string.cd_next), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(52.dp).shadow(7.dp, CircleShape)
                            .clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            stringResource(R.string.cd_play_pause),
                            Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.SkipPrevious, stringResource(R.string.cd_previous), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.width(14.dp))
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(androidx.compose.ui.graphics.RectangleShape),
                ) {
                    Box(
                        Modifier.size(500.dp)
                            .shadow(
                                18.dp,
                                CircleShape,
                                ambientColor = raisedSurface.copy(alpha = 0.95f),
                                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(3.dp, surface, CircleShape),
                    )
                    TrackArt(
                        track,
                        300.dp,
                        Modifier.offset(x = 100.dp, y = 100.dp).border(3.dp, surface, CircleShape),
                        corner = 999.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit, enabled: Boolean = true) {
    val preset = LocalThemePreset.current
    val shape = themeCardShape(preset)
    Card(
        Modifier.fillMaxWidth()
            .then(
                if (themeUsesOutline(preset)) Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                else Modifier
            )
            .then(if (themeUsesNeumorphism(preset)) Modifier.shadow(5.dp, shape) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = shape,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked, onChecked, enabled = enabled)
        }
    }
}
