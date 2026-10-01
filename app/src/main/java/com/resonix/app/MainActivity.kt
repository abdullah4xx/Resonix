package com.resonix.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import com.resonix.app.core.LocaleManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import com.resonix.app.ui.search.SearchScreen
import com.resonix.app.ui.home.TrackArt
import com.resonix.app.data.TrackEntity
import androidx.compose.ui.unit.sp
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Explore
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.ui.PlayerUiState
import com.resonix.app.ui.PlayerViewModel
import com.resonix.app.ui.browser.BrowserScreen
import com.resonix.app.ui.home.HomeScreen
import com.resonix.app.ui.player.PlayerScreen
import com.resonix.app.ui.theme.ResonixTheme
import com.resonix.app.ui.theme.ThemeBuilderScreen
import com.resonix.app.ui.theme.ThemeRepository
import com.resonix.app.ui.theme.edgeGlow
import com.resonix.app.ui.theme.themedBackground
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private enum class Dest { BROWSER, LIBRARY, THEMES, SEARCH }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themeRepo: ThemeRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by themeRepo.settings.collectAsStateWithLifecycle()
            // Layout direction follows the language the user picked (Arabic = RTL, English = LTR).
            val rtl = LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL
            CompositionLocalProvider(
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                ResonixTheme(settings) { ResonixRoot(themeRepo) }
            }
        }
    }
}

@Composable
private fun ResonixRoot(themeRepo: ThemeRepository, vm: PlayerViewModel = hiltViewModel()) {
    val settings by themeRepo.settings.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val allTracks by vm.tracks.collectAsStateWithLifecycle()
    val current = allTracks.firstOrNull { it.id.toString() == ui.mediaId }
    var dest by rememberSaveable { mutableStateOf(Dest.LIBRARY) }
    var showPlayer by rememberSaveable { mutableStateOf(false) }

    PermissionGate(onMediaGranted = { vm.scan() })
    BackHandler(enabled = showPlayer) { showPlayer = false }

    Box(
        Modifier.fillMaxSize()
            .themedBackground(settings.preset, MaterialTheme.colorScheme.background)
            .edgeGlow(ui.glow)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    if (ui.mediaId != null) {
                        MiniPlayer(ui, current, onToggle = vm::togglePlay, onNext = vm::next, onOpen = { showPlayer = true })
                    }
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onBackground,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        NavigationBarItem(
                            dest == Dest.BROWSER, { dest = Dest.BROWSER },
                            { Icon(Icons.Default.Explore, null) },
                            label = { Text(stringResource(R.string.nav_browser)) },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            dest == Dest.LIBRARY, { dest = Dest.LIBRARY },
                            { Icon(Icons.Default.Headset, null) },
                            label = { Text(stringResource(R.string.nav_library)) },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            dest == Dest.THEMES, { dest = Dest.THEMES },
                            { Icon(Icons.Default.Palette, null) },
                            label = { Text(stringResource(R.string.nav_themes)) },
                            colors = navColors,
                        )
                        NavigationBarItem(
                            dest == Dest.SEARCH, { dest = Dest.SEARCH },
                            { Icon(Icons.Default.Search, null) },
                            label = { Text(stringResource(R.string.nav_search)) },
                            colors = navColors,
                        )
                    }
                }
            },
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (dest) {
                    Dest.LIBRARY -> HomeScreen(vm, onOpenSearch = { dest = Dest.SEARCH })
                    Dest.BROWSER -> BrowserScreen()
                    Dest.THEMES -> ThemeBuilderScreen(themeRepo, settings)
                    Dest.SEARCH -> SearchScreen(vm)
                }
            }
        }

        AnimatedVisibility(
            visible = showPlayer,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            PlayerScreen(vm, onClose = { showPlayer = false })
        }
    }
}

@Composable
private fun MiniPlayer(ui: PlayerUiState, track: TrackEntity?, onToggle: () -> Unit, onNext: () -> Unit, onOpen: () -> Unit) {
    val tint = ui.glow?.copy(alpha = 0.35f) ?: Color.Transparent
    Surface(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp).clickable(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            Modifier.background(tint).padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackArt(track, 44.dp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(ui.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    ui.artist, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (ui.durationMs > 0) (ui.positionMs.toFloat() / ui.durationMs).coerceIn(0f, 1f) else 0f },
                    modifier = Modifier.fillMaxSize().padding(3.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                )
                IconButton(onClick = onToggle, modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (ui.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        stringResource(R.string.cd_play_pause),
                    )
                }
            }
            IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, stringResource(R.string.cd_next)) }
        }
    }
}

private fun hasMediaPermission(ctx: Context): Boolean {
    val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
    return ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun PermissionGate(onMediaGranted: () -> Unit) {
    val ctx = LocalContext.current
    val perms = remember {
        if (Build.VERSION.SDK_INT >= 33) arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.POST_NOTIFICATIONS,
        ) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (hasMediaPermission(ctx)) onMediaGranted()
    }
    LaunchedEffect(Unit) {
        if (hasMediaPermission(ctx)) onMediaGranted()
        val missing = perms.filter { ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) launcher.launch(missing.toTypedArray())
    }
}
