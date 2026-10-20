package com.resonix.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonix.app.R
import com.resonix.app.data.TrackEntity
import com.resonix.app.ui.theme.LocalThemePreset
import com.resonix.app.ui.theme.ThemePreset
import com.resonix.app.ui.theme.themeCardShape
import com.resonix.app.ui.theme.themeUsesNeumorphism
import com.resonix.app.ui.theme.themeUsesOutline
import com.resonix.app.ui.theme5.Neu
import com.resonix.app.ui.theme5.neuShadow

class TrackActions(
    val play: (List<TrackEntity>, Int) -> Unit,
    val favorite: (TrackEntity) -> Unit,
    val quran: (TrackEntity, Boolean) -> Unit,
)

@Composable
fun TrackRow(track: TrackEntity, onClick: () -> Unit, actions: TrackActions) {
    var menu by remember { mutableStateOf(false) }
    val preset = LocalThemePreset.current
    val shape = themeCardShape(preset)
    val isEditorial = preset == ThemePreset.ORGANIC_LIGHT
    if (preset == ThemePreset.NEUMORPHIC_BLUE) {
        val neuShape = RoundedCornerShape(20.dp)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .neuShadow(neuShape, Neu.TileShadows)
                .background(Neu.Tile, neuShape)
                .border(4.dp, Neu.TileBorder, neuShape)
                .clip(neuShape).clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackArt(
                track,
                52.dp,
                corner = 10.dp,
                artHeight = 52.dp,
                artShape = RoundedCornerShape(
                    topStart = 30.dp,
                    topEnd = 30.dp,
                    bottomEnd = 10.dp,
                    bottomStart = 10.dp,
                ),
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Neu.TitleStart,
                )
                Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = Neu.TextSecondary)
            }
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.cd_more), tint = Neu.Icon)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(if (track.isFavorite) R.string.menu_remove_fav else R.string.menu_add_fav)) },
                    leadingIcon = { Icon(if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) },
                    onClick = { menu = false; actions.favorite(track) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(if (track.isQuran) R.string.menu_unmark_quran else R.string.menu_mark_quran)) },
                    leadingIcon = { Icon(Icons.Default.MenuBook, null) },
                    onClick = { menu = false; actions.quran(track, !track.isQuran) },
                )
            }
        }
        return
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = if (isEditorial) 20.dp else 12.dp, vertical = if (isEditorial) 0.dp else 4.dp)
                .shadow(if (themeUsesNeumorphism(preset)) 5.dp else 0.dp, shape)
                .clip(shape)
                .background(
                    when (preset) {
                        ThemePreset.VIOLET_CURRENT -> MaterialTheme.colorScheme.surfaceVariant
                        ThemePreset.PETAL_PLAYER -> MaterialTheme.colorScheme.surface
                        else -> if (isEditorial) Color.Transparent else MaterialTheme.colorScheme.surface
                    }
                )
                .then(
                    if (themeUsesOutline(preset)) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, shape)
                    else Modifier
                )
                .clickable(onClick = onClick)
                .padding(
                    horizontal = if (isEditorial) 0.dp else 12.dp,
                    vertical = if (preset == ThemePreset.PETAL_PLAYER) 12.dp else 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackArt(track, if (preset == ThemePreset.PETAL_PLAYER) 56.dp else 52.dp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (track.isQuran) Icons.Default.MenuBook else Icons.Default.PhoneAndroid, null,
                        Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.cd_more), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(if (track.isFavorite) R.string.menu_remove_fav else R.string.menu_add_fav)) },
                        leadingIcon = { Icon(if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) },
                        onClick = { menu = false; actions.favorite(track) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(if (track.isQuran) R.string.menu_unmark_quran else R.string.menu_mark_quran)) },
                        leadingIcon = { Icon(Icons.Default.MenuBook, null) },
                        onClick = { menu = false; actions.quran(track, !track.isQuran) },
                    )
                }
            }
        }
        if (isEditorial) {
            Box(
                Modifier.fillMaxWidth().padding(start = 84.dp, end = 20.dp)
                    .height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
            )
        }
    }
}
