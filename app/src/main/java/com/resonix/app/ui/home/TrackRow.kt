package com.resonix.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonix.app.R
import com.resonix.app.data.TrackEntity

class TrackActions(
    val play: (List<TrackEntity>, Int) -> Unit,
    val favorite: (TrackEntity) -> Unit,
    val quran: (TrackEntity, Boolean) -> Unit,
)

@Composable
fun TrackRow(track: TrackEntity, onClick: () -> Unit, actions: TrackActions) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrackArt(track, 52.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium, fontSize = 15.sp)
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
}
