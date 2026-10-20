package com.resonix.app.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.R
import com.resonix.app.data.TrackEntity
import com.resonix.app.ui.LanguageSwitcher
import com.resonix.app.ui.PlayerViewModel
import com.resonix.app.ui.theme.LocalThemePreset
import com.resonix.app.ui.theme.themeCardShape
import com.resonix.app.ui.theme.ThemePreset
import com.resonix.app.ui.theme.themeUsesNeumorphism
import com.resonix.app.ui.theme.themeUsesOutline
import com.resonix.app.ui.theme5.NeuPlaylistTile
import com.resonix.app.ui.theme5.NeuSectionHeader
import com.resonix.app.ui.theme5.NeuWideCard

enum class HomeTab(@StringRes val label: Int) {
    TRACKS(R.string.tab_tracks),
    ALBUMS(R.string.tab_albums),
    PLAYLISTS(R.string.tab_playlists),
    ARTISTS(R.string.tab_artists),
    FOLDERS(R.string.tab_folders),
}

enum class QuickFilter { NONE, QURAN, FAVORITES, VIDEOS, RECENT }

enum class SortOrder(@StringRes val label: Int) {
    NEWEST(R.string.sort_newest),
    TITLE(R.string.sort_title),
    ARTIST(R.string.sort_artist),
}

private data class QuickItem(val filter: QuickFilter, val label: String, val icon: ImageVector)

@Composable
fun HomeScreen(vm: PlayerViewModel, onOpenSearch: () -> Unit, modifier: Modifier = Modifier) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val preset = LocalThemePreset.current
    var tab by rememberSaveable { mutableStateOf(HomeTab.TRACKS) }
    var quick by rememberSaveable { mutableStateOf(QuickFilter.NONE) }
    var sort by rememberSaveable { mutableStateOf(SortOrder.NEWEST) }
    var sortMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(setOf<String>()) }

    val audio = tracks.filter { !it.isVideo }
    val visible = when (quick) {
        QuickFilter.NONE -> audio
        QuickFilter.QURAN -> audio.filter { it.isQuran }
        QuickFilter.FAVORITES -> audio.filter { it.isFavorite }
        QuickFilter.RECENT -> audio.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed }.take(50)
        QuickFilter.VIDEOS -> tracks.filter { it.isVideo }
    }
    val sorted = if (quick == QuickFilter.RECENT) visible else when (sort) {
        SortOrder.NEWEST -> visible.sortedByDescending { it.dateAdded }
        SortOrder.TITLE -> visible.sortedBy { it.title.lowercase() }
        SortOrder.ARTIST -> visible.sortedBy { it.artist.lowercase() }
    }
    val onToggle: (String) -> Unit = { k -> expanded = if (k in expanded) expanded - k else expanded + k }
    val actions = remember(vm) { TrackActions(vm::play, vm::toggleFavorite, vm::setQuranFlag) }

    val favLabel = stringResource(R.string.quick_favorites)
    val quickItems = listOf(
        QuickItem(QuickFilter.QURAN, stringResource(R.string.quick_quran), Icons.Default.MenuBook),
        QuickItem(QuickFilter.FAVORITES, favLabel, Icons.Default.Favorite),
        QuickItem(QuickFilter.VIDEOS, stringResource(R.string.quick_videos), Icons.Default.VideoLibrary),
        QuickItem(QuickFilter.RECENT, stringResource(R.string.quick_recent), Icons.Default.History),
    )
    val quranName = stringResource(R.string.playlist_quran)
    val musicName = stringResource(R.string.playlist_music)
    val recentPlayedName = stringResource(R.string.playlist_recent)

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item(key = "title") {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.nav_library),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onOpenSearch) { Icon(Icons.Default.Search, stringResource(R.string.nav_search)) }
                LanguageSwitcher()
            }
        }
        item(key = "theme-showcase") {
            ThemeShowcase(preset, sorted, onOpenTab = { tab = it }) { selected ->
                val index = sorted.indexOf(selected)
                if (index >= 0) vm.play(sorted, index)
            }
        }
        item(key = "quick") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(quickItems) { q ->
                    QuickCard(q, quick == q.filter) {
                        quick = if (quick == q.filter) QuickFilter.NONE else q.filter
                    }
                }
            }
        }
        item(key = "tabs") {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            ) {
                HomeTab.values().forEach { t -> TabLabel(stringResource(t.label), tab == t) { tab = t } }
            }
        }
        item(key = "shuffle") {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val shuffleColor = MaterialTheme.colorScheme.primary
                Box(
                    Modifier.size(width = 44.dp, height = 32.dp).clip(RoundedCornerShape(10.dp))
                        .background(shuffleColor)
                        .clickable(enabled = sorted.isNotEmpty()) { vm.shuffleAll(sorted) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        stringResource(R.string.shuffle),
                        Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.shuffle), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.tracks_count, sorted.size),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(onClick = { sortMenu = true }) { Icon(Icons.Default.Sort, stringResource(R.string.cd_sort)) }
                    DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                        SortOrder.values().forEach { s ->
                            DropdownMenuItem(
                                text = { Text(stringResource(s.label)) },
                                onClick = { sort = s; sortMenu = false },
                                trailingIcon = { if (sort == s) Icon(Icons.Default.Check, null) },
                            )
                        }
                    }
                }
            }
        }

        when (tab) {
            HomeTab.TRACKS -> itemsIndexed(sorted, key = { _, t -> "t-${t.id}" }) { i, t ->
                TrackRow(t, onClick = { vm.play(sorted, i) }, actions = actions)
            }
            HomeTab.ALBUMS -> groupedSection(
                "a", sorted.groupBy { it.album }.toSortedMap(String.CASE_INSENSITIVE_ORDER), expanded, onToggle, actions,
            )
            HomeTab.ARTISTS -> groupedSection(
                "r", sorted.groupBy { it.artist }.toSortedMap(String.CASE_INSENSITIVE_ORDER), expanded, onToggle, actions,
            )
            HomeTab.FOLDERS -> groupedSection(
                "f", sorted.groupBy { it.folder }.toSortedMap(String.CASE_INSENSITIVE_ORDER), expanded, onToggle, actions,
            )
            HomeTab.PLAYLISTS -> {
                // Smart playlists, generated automatically from your library.
                val smart = linkedMapOf(
                    quranName to sorted.filter { it.isQuran },
                    musicName to sorted.filter { !it.isQuran && !it.isVideo },
                    favLabel to sorted.filter { it.isFavorite },
                    recentPlayedName to sorted.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed },
                ).filterValues { it.isNotEmpty() }
                groupedSection("p", smart, expanded, onToggle, actions)
            }
        }
        if (tracks.isEmpty()) {
            item(key = "empty") {
                Text(
                    stringResource(R.string.empty_library),
                    Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ThemeShowcase(
    preset: ThemePreset,
    tracks: List<TrackEntity>,
    onOpenTab: (HomeTab) -> Unit,
    onPlay: (TrackEntity) -> Unit,
) {
    if (tracks.isEmpty()) return
    val featured = tracks.first()
    val accent = MaterialTheme.colorScheme.primary
    val cardShape = themeCardShape(preset)

    when (preset) {
        ThemePreset.ORGANIC_LIGHT, ThemePreset.DEFAULT, ThemePreset.CUSTOM -> Unit
        ThemePreset.OUTLINE_MONO -> {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    stringResource(R.string.theme_discover),
                    Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(tracks.distinctBy { it.artist }.take(5), key = { "discover-${it.id}" }) { track ->
                        Column(
                            Modifier.width(90.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onPlay(track) }
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            TrackArt(track, 76.dp, corner = 5.dp)
                            Text(track.artist, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        ThemePreset.VIOLET_CURRENT -> {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    stringResource(R.string.theme_featured_track),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(cardShape)
                        .background(accent)
                        .clickable { onPlay(featured) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TrackArt(featured, 78.dp, corner = 999.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(featured.title, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(featured.artist, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.PlayArrow, stringResource(R.string.cd_play_pause), tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        ThemePreset.PETAL_PLAYER -> {
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    stringResource(R.string.tab_playlists),
                    Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(tracks.take(6), key = { "petal-${it.id}" }) { track ->
                        Column(
                            Modifier.width(132.dp).clip(cardShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { onPlay(track) }
                                .padding(8.dp),
                        ) {
                            TrackArt(track, 116.dp, corner = 18.dp)
                            Spacer(Modifier.height(7.dp))
                            Text(
                                track.title,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
        ThemePreset.NEUMORPHIC_BLUE, ThemePreset.OLED -> {
            val artists = tracks.filter { it.artist.isNotBlank() }.distinctBy { it.artist }.take(6)
            val collections = listOf(
                stringResource(R.string.playlist_music) to tracks.filter { !it.isQuran && !it.isVideo },
                stringResource(R.string.playlist_quran) to tracks.filter { it.isQuran && !it.isVideo },
            ).filter { it.second.isNotEmpty() }
            Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                NeuSectionHeader(
                    stringResource(R.string.tab_artists),
                    stringResource(R.string.cd_more),
                    Modifier.padding(horizontal = 20.dp),
                    onMore = { onOpenTab(HomeTab.ARTISTS) },
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(artists, key = { "neumo-artist-${it.id}" }) { track ->
                        NeumorphicArtist(track, onClick = { onPlay(track) })
                    }
                }

                NeuSectionHeader(
                    stringResource(R.string.tab_playlists),
                    stringResource(R.string.cd_more),
                    Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    onMore = { onOpenTab(HomeTab.PLAYLISTS) },
                )
                collections.firstOrNull()?.let { (name, collection) ->
                    NeuWideCard(
                        name = name,
                        count = stringResource(R.string.tracks_count, collection.size),
                        cover = {
                            TrackArt(collection.firstOrNull(), 340.dp, Modifier.fillMaxSize(), artHeight = 188.dp)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = { onPlay(collection.first()) },
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(collections, key = { it.first }) { (name, collection) ->
                        NeumorphicPlaylistCard(name, collection, onClick = { onPlay(collection.first()) })
                    }
                }

                NeuSectionHeader(
                    stringResource(R.string.tab_tracks),
                    stringResource(R.string.cd_more),
                    Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
                    onMore = { onOpenTab(HomeTab.TRACKS) },
                )
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    tracks.take(3).forEach { track ->
                        NeumorphicRecentTrack(track) { onPlay(track) }
                    }
                }
            }
        }
        else -> Unit
    }
}

@Composable
private fun NeumorphicArtist(track: TrackEntity, onClick: () -> Unit) {
    val surface = MaterialTheme.colorScheme.surface
    val raised = MaterialTheme.colorScheme.surfaceVariant
    Column(
        Modifier.width(78.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(Modifier.size(78.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(72.dp)
                    .shadow(9.dp, CircleShape, ambientColor = surface, spotColor = raised)
                    .clip(CircleShape)
                    .background(surface)
                    .border(3.dp, surface.copy(alpha = 0.9f), CircleShape),
            )
            TrackArt(track, 54.dp, corner = 999.dp)
        }
        Text(track.artist, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NeumorphicPlaylistCard(
    title: String,
    tracks: List<TrackEntity>,
    onClick: () -> Unit,
) {
    NeuPlaylistTile(
        name = title,
        count = stringResource(R.string.tracks_count, tracks.size),
        cover = { TrackArt(tracks.firstOrNull(), 86.dp, artHeight = 167.dp) },
        onClick = onClick,
    )
}

@Composable
private fun NeumorphicRecentTrack(track: TrackEntity, onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    val surface = MaterialTheme.colorScheme.surface
    val raised = MaterialTheme.colorScheme.surfaceVariant
    Row(
        Modifier.fillMaxWidth()
            .shadow(7.dp, shape, ambientColor = surface, spotColor = raised)
            .clip(shape)
            .background(raised)
            .border(2.dp, surface.copy(alpha = 0.85f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrackArt(track, 42.dp, corner = 999.dp)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                track.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(track.artist, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.PlayArrow, stringResource(R.string.cd_play_pause), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
    }
}

private fun LazyListScope.groupedSection(
    prefix: String,
    groups: Map<String, List<TrackEntity>>,
    expanded: Set<String>,
    onToggle: (String) -> Unit,
    actions: TrackActions,
) {
    groups.forEach { (name, list) ->
        item(key = "$prefix-$name") { GroupHeader(name, list.size, name in expanded) { onToggle(name) } }
        if (name in expanded) {
            itemsIndexed(list, key = { _, t -> "$prefix-$name-${t.id}" }) { i, t ->
                TrackRow(t, onClick = { actions.play(list, i) }, actions = actions)
            }
        }
    }
}

@Composable
private fun TabLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier.width(22.dp).height(3.dp).clip(RoundedCornerShape(2.dp))
                .background(if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent),
        )
    }
}

@Composable
private fun QuickCard(item: QuickItem, selected: Boolean, onClick: () -> Unit) {
    val preset = LocalThemePreset.current
    val shape = themeCardShape(preset)
    val accent = if (item.filter == QuickFilter.FAVORITES || item.filter == QuickFilter.RECENT) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }
    Column(
        Modifier.width(84.dp)
            .shadow(if (themeUsesNeumorphism(preset)) 5.dp else 0.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                when {
                    selected -> Modifier.border(1.5.dp, accent, shape)
                    themeUsesOutline(preset) -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                    else -> Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, null, Modifier.size(22.dp), tint = accent)
        }
        Text(item.label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GroupHeader(name: String, count: Int, open: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.tracks_count, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
    }
}
