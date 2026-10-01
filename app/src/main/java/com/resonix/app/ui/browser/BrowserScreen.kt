package com.resonix.app.ui.browser

import android.content.Intent
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.resonix.app.R
import com.resonix.app.download.DownloadService
import com.resonix.app.download.DownloadStatus
import com.resonix.app.download.DownloadTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(val tracker: DownloadTracker) : ViewModel()

@Composable
fun BrowserScreen(modifier: Modifier = Modifier, dvm: DownloadViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val downloads by dvm.tracker.items.collectAsStateWithLifecycle()
    val defaultName = stringResource(R.string.dl_default_name)
    var input by remember { mutableStateOf("https://archive.org") }
    var currentUrl by remember { mutableStateOf("https://archive.org") }
    var web by remember { mutableStateOf<WebView?>(null) }
    var dialogOpen by remember { mutableStateOf(false) }
    var dlUrl by remember { mutableStateOf("") }
    var dlTitle by remember { mutableStateOf("") }
    var dlQuran by remember { mutableStateOf(false) }

    BackHandler(enabled = web?.canGoBack() == true) { web?.goBack() }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            singleLine = true,
            label = { Text(stringResource(R.string.browser_address)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { web?.loadUrl(normalizeUrl(input)) }),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        )

        downloads.takeLast(3).forEach { d ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    when (d.status) {
                        DownloadStatus.RUNNING -> stringResource(R.string.dl_downloading_fmt, d.title, d.progress)
                        DownloadStatus.DONE -> stringResource(R.string.dl_saved_fmt, d.title)
                        DownloadStatus.FAILED -> stringResource(R.string.dl_failed_fmt, d.title, d.error.orEmpty())
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                if (d.status == DownloadStatus.RUNNING) {
                    LinearProgressIndicator(progress = { d.progress / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { c ->
                    WebView(c).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                currentUrl = url
                                input = url
                            }
                        }
                        // Direct file downloads triggered by a page open the overlay automatically.
                        setDownloadListener { url, _, _, _, _ ->
                            dlUrl = url
                            dlTitle = url.substringBefore('?').substringAfterLast('/').substringBeforeLast('.').ifBlank { defaultName }
                            dialogOpen = true
                        }
                        loadUrl(currentUrl)
                        web = this
                    }
                },
            )
            ExtendedFloatingActionButton(
                onClick = {
                    dlUrl = currentUrl
                    dlTitle = currentUrl.substringBefore('?').substringAfterLast('/').substringBeforeLast('.').ifBlank { defaultName }
                    dialogOpen = true
                },
                icon = { Icon(Icons.Default.Download, null) },
                text = { Text(stringResource(R.string.dl_button)) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            title = { Text(stringResource(R.string.dl_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(dlUrl, { dlUrl = it }, label = { Text(stringResource(R.string.dl_url_label)) }, singleLine = true)
                    OutlinedTextField(dlTitle, { dlTitle = it }, label = { Text(stringResource(R.string.dl_name_label)) }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(!dlQuran, { dlQuran = false }, label = { Text(stringResource(R.string.dl_cat_music)) })
                        FilterChip(dlQuran, { dlQuran = true }, label = { Text(stringResource(R.string.dl_cat_quran)) })
                    }
                    Text(stringResource(R.string.dl_warning), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogOpen = false
                        ContextCompat.startForegroundService(
                            ctx,
                            Intent(ctx, DownloadService::class.java)
                                .putExtra(DownloadService.EXTRA_URL, dlUrl.trim())
                                .putExtra(DownloadService.EXTRA_TITLE, dlTitle.trim())
                                .putExtra(DownloadService.EXTRA_QURAN, dlQuran)
                        )
                    },
                    enabled = dlUrl.startsWith("http"),
                ) { Text(stringResource(R.string.dl_button)) }
            },
            dismissButton = { TextButton(onClick = { dialogOpen = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

private fun normalizeUrl(raw: String): String {
    val s = raw.trim()
    return when {
        s.startsWith("http://") || s.startsWith("https://") -> s
        s.contains(' ') || !s.contains('.') -> "https://www.google.com/search?q=" + android.net.Uri.encode(s)
        else -> "https://$s"
    }
}
