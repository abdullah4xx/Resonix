package com.resonix.app.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resonix.app.R
import com.resonix.app.core.DEVELOPER_AUTHOR
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemePreset(@StringRes val labelRes: Int) {
    DEFAULT(R.string.preset_default),
    OLED(R.string.preset_oled),
    GLASS(R.string.preset_glass),
    CUSTOM(R.string.preset_custom),
}

data class ThemeSettings(
    val preset: ThemePreset = ThemePreset.OLED,
    val primary: Int = 0xFF4FD1C5.toInt(),
    val secondary: Int = 0xFFB794F4.toInt(),
)

@Singleton
class ThemeRepository @Inject constructor(@ApplicationContext ctx: Context) {
    private val prefs = ctx.getSharedPreferences("resonix_theme", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()

    private fun load() = ThemeSettings(
        preset = ThemePreset.values().getOrElse(prefs.getInt("preset", ThemePreset.OLED.ordinal)) { ThemePreset.OLED },
        primary = prefs.getInt("primary", ThemeSettings().primary),
        secondary = prefs.getInt("secondary", ThemeSettings().secondary),
    )

    private fun save(s: ThemeSettings) {
        prefs.edit().putInt("preset", s.preset.ordinal).putInt("primary", s.primary).putInt("secondary", s.secondary).apply()
        _settings.value = s
    }

    fun applyPreset(p: ThemePreset) = save(
        when (p) {
            ThemePreset.DEFAULT -> ThemeSettings(p, 0xFF6C5CE7.toInt(), 0xFF00CEC9.toInt())
            ThemePreset.OLED -> ThemeSettings(p, 0xFF4FD1C5.toInt(), 0xFFB794F4.toInt())
            ThemePreset.GLASS -> ThemeSettings(p, 0xFF8AB4FF.toInt(), 0xFFC58AFF.toInt())
            ThemePreset.CUSTOM -> _settings.value.copy(preset = p)
        }
    )

    fun setCustomColors(primary: Int? = null, secondary: Int? = null) {
        val c = _settings.value
        save(c.copy(preset = ThemePreset.CUSTOM, primary = primary ?: c.primary, secondary = secondary ?: c.secondary))
    }
}

private fun onColor(c: Color) = if (c.luminance() > 0.5f) Color.Black else Color.White

fun buildColorScheme(s: ThemeSettings): ColorScheme {
    val p = Color(s.primary)
    val sec = Color(s.secondary)
    return when (s.preset) {
        ThemePreset.OLED -> darkColorScheme(
            primary = p, secondary = sec, onPrimary = onColor(p), onSecondary = onColor(sec),
            background = Color.Black, surface = Color.Black, surfaceVariant = Color(0xFF1C1C1E),
            onBackground = Color(0xFFEDEDED), onSurface = Color(0xFFEDEDED), onSurfaceVariant = Color(0xFFB0B0B0),
            primaryContainer = p.copy(alpha = 0.25f),
        )
        ThemePreset.GLASS -> darkColorScheme(
            primary = p, secondary = sec, onPrimary = onColor(p), onSecondary = onColor(sec),
            background = Color(0xFF0B1020), surface = Color.White.copy(alpha = 0.06f),
            surfaceVariant = Color.White.copy(alpha = 0.12f),
            onBackground = Color(0xFFF2F4FF), onSurface = Color(0xFFF2F4FF), onSurfaceVariant = Color(0xFFC7CCE6),
            primaryContainer = p.copy(alpha = 0.28f),
        )
        else -> darkColorScheme(
            primary = p, secondary = sec, onPrimary = onColor(p), onSecondary = onColor(sec),
            background = Color(0xFF101114), surface = Color(0xFF181A1F), surfaceVariant = Color(0xFF22252C),
            onBackground = Color(0xFFEDEDF2), onSurface = Color(0xFFEDEDF2), onSurfaceVariant = Color(0xFFB4B8C4),
            primaryContainer = p.copy(alpha = 0.28f),
        )
    }
}

@Composable
fun ResonixTheme(settings: ThemeSettings, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = buildColorScheme(settings), content = content)
}

fun Modifier.themedBackground(preset: ThemePreset, base: Color): Modifier = when (preset) {
    ThemePreset.GLASS -> background(
        Brush.linearGradient(listOf(Color(0xFF1B2A6B), Color(0xFF0B1020), Color(0xFF3A1B5C)))
    )
    ThemePreset.OLED -> background(Color.Black)
    else -> background(base)
}

/** Soft glow along the left/right screen edges, tinted by the artwork's Palette color. */
fun Modifier.edgeGlow(color: Color?): Modifier {
    if (color == null) return this
    return drawBehind {
        val w = 28.dp.toPx()
        drawRect(
            Brush.horizontalGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), startX = 0f, endX = w),
            size = Size(w, size.height),
        )
        drawRect(
            Brush.horizontalGradient(listOf(Color.Transparent, color.copy(alpha = 0.55f)), startX = size.width - w, endX = size.width),
            topLeft = Offset(size.width - w, 0f),
            size = Size(w, size.height),
        )
    }
}

@Composable
fun ThemeBuilderScreen(repo: ThemeRepository, settings: ThemeSettings, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Text(stringResource(R.string.theme_presets), style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemePreset.values().forEach { p ->
                FilterChip(
                    selected = settings.preset == p,
                    onClick = { repo.applyPreset(p) },
                    label = { Text(stringResource(p.labelRes), maxLines = 1) },
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.theme_builder), style = MaterialTheme.typography.titleMedium)
                HsvPicker(stringResource(R.string.primary_color), settings.primary) { repo.setCustomColors(primary = it) }
                HsvPicker(stringResource(R.string.secondary_color), settings.secondary) { repo.setCustomColors(secondary = it) }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(settings.primary)))
            Box(Modifier.size(48.dp).clip(CircleShape).background(Color(settings.secondary)))
            Text(stringResource(R.string.theme_preview), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.crafted_by, DEVELOPER_AUTHOR),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HsvPicker(label: String, argb: Int, onChange: (Int) -> Unit) {
    val hsv = FloatArray(3).also { android.graphics.Color.colorToHSV(argb, it) }
    fun emit(h: Float = hsv[0], s: Float = hsv[1], v: Float = hsv[2]) =
        onChange(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v)))

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(20.dp).clip(CircleShape).background(Color(argb)))
            Text("  $label", style = MaterialTheme.typography.labelLarge)
        }
        Text(stringResource(R.string.hue), style = MaterialTheme.typography.labelSmall)
        Slider(hsv[0], { emit(h = it) }, valueRange = 0f..360f)
        Text(stringResource(R.string.saturation), style = MaterialTheme.typography.labelSmall)
        Slider(hsv[1], { emit(s = it) }, valueRange = 0f..1f)
        Text(stringResource(R.string.brightness), style = MaterialTheme.typography.labelSmall)
        Slider(hsv[2], { emit(v = it) }, valueRange = 0.2f..1f)
    }
}
