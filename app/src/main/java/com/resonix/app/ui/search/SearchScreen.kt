package com.resonix.app.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.R
import com.resonix.app.ui.PlayerViewModel
import com.resonix.app.ui.home.TrackActions
import com.resonix.app.ui.home.TrackRow

@Composable
fun SearchScreen(vm: PlayerViewModel, modifier: Modifier = Modifier) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val results = remember(tracks, q) {
        if (q.isEmpty()) emptyList()
        else tracks.filter { it.title.contains(q, true) || it.artist.contains(q, true) || it.album.contains(q, true) }
    }
    val actions = remember(vm) { TrackActions(vm::play, vm::toggleFavorite, vm::setQuranFlag) }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, stringResource(R.string.cd_close)) }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )
        if (q.isNotEmpty() && results.isEmpty()) {
            Text(
                stringResource(R.string.search_empty),
                Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyColumn {
            itemsIndexed(results, key = { _, t -> t.id }) { i, t ->
                TrackRow(t, onClick = { vm.play(results, i) }, actions = actions)
            }
        }
    }
}
