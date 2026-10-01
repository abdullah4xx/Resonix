package com.resonix.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.R
import com.resonix.app.ui.PlayerViewModel
import java.util.Locale

@Composable
fun PlayerScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val fx by vm.effects.state.collectAsStateWithLifecycle()
    val armed by vm.sleepTimer.armed.collectAsStateWithLifecycle()
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val current = tracks.firstOrNull { it.id.toString() == ui.mediaId }
    val snack = remember { SnackbarHostState() }

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

            Box(
                Modifier.align(Alignment.CenterHorizontally).size(180.dp).clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (fx.quranProtected) Icons.Default.MenuBook else Icons.Default.MusicNote, null,
                    Modifier.size(84.dp), tint = MaterialTheme.colorScheme.primary,
                )
            }

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
                Modifier.fillMaxWidth().clickable(enabled = fx.quranProtected) { vm.effects.notifyBlocked() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
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
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit, enabled: Boolean = true) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
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
