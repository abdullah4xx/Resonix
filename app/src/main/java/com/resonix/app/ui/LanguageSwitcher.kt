package com.resonix.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.resonix.app.R
import com.resonix.app.core.LocaleManager
import com.resonix.app.core.findActivity

@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf(false) }
    val current = LocaleManager.current(ctx)
    val arabic = stringResource(R.string.lang_arabic)
    val english = stringResource(R.string.lang_english)

    fun pick(code: String) {
        open = false
        ctx.findActivity()?.let { LocaleManager.setLanguage(it, code) }
    }

    Box(modifier) {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Default.Language, stringResource(R.string.lang_title))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(arabic) },
                onClick = { pick("ar") },
                trailingIcon = { if (current == "ar") Icon(Icons.Default.Check, null) },
            )
            DropdownMenuItem(
                text = { Text(english) },
                onClick = { pick("en") },
                trailingIcon = { if (current == "en") Icon(Icons.Default.Check, null) },
            )
        }
    }
}
