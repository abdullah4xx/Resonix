package com.resonix.app.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
    ORGANIC_LIGHT(R.string.preset_organic),
    OUTLINE_MONO(R.string.preset_outline),
    VIOLET_CURRENT(R.string.preset_violet),
    PETAL_PLAYER(R.string.preset_petal),
    NEUMORPHIC_BLUE(R.string.preset_neumorphic),
}

data class ThemeSettings(
    val preset: ThemePreset = ThemePreset.ORGANIC_LIGHT,
    val primary: Int = 0xFF4FD1C5.toInt(),
    val secondary: Int = 0xFFB794F4.toInt(),
    val isDark: Boolean = false,
)

@Singleton
class ThemeRepository @Inject constructor(@ApplicationContext ctx: Context) {
    private val prefs = ctx.getSharedPreferences("resonix_theme", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()

    private fun load(): ThemeSettings {
        val storedPreset = prefs.getInt("preset", -1)
        val preset = when (storedPreset) {
            ThemePreset.DEFAULT.ordinal -> ThemePreset.ORGANIC_LIGHT
            ThemePreset.OLED.ordinal -> ThemePreset.NEUMORPHIC_BLUE
            ThemePreset.GLASS.ordinal -> ThemePreset.VIOLET_CURRENT
            ThemePreset.CUSTOM.ordinal -> ThemePreset.CUSTOM
            in ThemePreset.ORGANIC_LIGHT.ordinal until ThemePreset.values().size ->
                ThemePreset.values()[storedPreset]
            else -> ThemeSettings().preset
        }
        val wasLegacyPreset = storedPreset in ThemePreset.DEFAULT.ordinal..ThemePreset.CUSTOM.ordinal
        return ThemeSettings(
            preset = preset,
            primary = prefs.getInt("primary", ThemeSettings().primary),
            secondary = prefs.getInt("secondary", ThemeSettings().secondary),
            isDark = prefs.getBoolean("dark_mode", wasLegacyPreset),
        )
    }

    private fun save(s: ThemeSettings) {
        prefs.edit()
            .putInt("preset", s.preset.ordinal)
            .putInt("primary", s.primary)
            .putInt("secondary", s.secondary)
            .putBoolean("dark_mode", s.isDark)
            .apply()
        _settings.value = s
    }

    fun applyPreset(p: ThemePreset) {
        save(_settings.value.copy(preset = p))
    }

    fun setCustomColors(primary: Int? = null, secondary: Int? = null) {
        val c = _settings.value
        save(c.copy(preset = ThemePreset.CUSTOM, primary = primary ?: c.primary, secondary = secondary ?: c.secondary))
    }

    fun setDarkMode(isDark: Boolean) {
        save(_settings.value.copy(isDark = isDark))
    }
}

private fun onColor(c: Color) = if (c.luminance() > 0.5f) Color.Black else Color.White

private data class ThemePalette(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val secondary: Color,
    val outline: Color,
)

private fun palette(preset: ThemePreset, isDark: Boolean, settings: ThemeSettings): ThemePalette {
    if (preset == ThemePreset.CUSTOM) {
        val accent = Color(settings.primary)
        return ThemePalette(
            background = if (isDark) Color(0xFF171614) else Color(0xFFF3F2EE),
            surface = if (isDark) Color(0xFF211F1B) else Color(0xFFFAF9F6),
            surfaceAlt = if (isDark) Color(0xFF2B2823) else Color(0xFFEBE9E3),
            text = if (isDark) Color(0xFFF5F2EB) else Color(0xFF161615),
            muted = if (isDark) Color(0xFFBBB5AA) else Color(0xFF77756F),
            accent = accent,
            onAccent = onColor(accent),
            secondary = Color(settings.secondary),
            outline = if (isDark) Color(0xFF45413A) else Color(0xFFD8D6CF),
        )
    }

    val isLight = !isDark
    return when (preset) {
        ThemePreset.DEFAULT, ThemePreset.ORGANIC_LIGHT -> if (isLight) ThemePalette(
            Color(0xFFF3F2EE), Color(0xFFFAF9F6), Color(0xFFEBE9E3), Color(0xFF161615),
            Color(0xFF77756F), Color(0xFF111110), Color.White, Color(0xFF698B58), Color(0xFFD8D6CF),
        ) else ThemePalette(
            Color(0xFF171614), Color(0xFF211F1B), Color(0xFF2B2823), Color(0xFFF5F2EB),
            Color(0xFFBBB5AA), Color(0xFFF5F2EB), Color(0xFF151412), Color(0xFF9AAB87), Color(0xFF45413A),
        )
        ThemePreset.OUTLINE_MONO -> if (isLight) ThemePalette(
            Color(0xFFEFEFEF), Color.White, Color(0xFFF5F5F5), Color(0xFF111111),
            Color(0xFF646464), Color(0xFF111111), Color.White, Color(0xFF444444), Color(0xFF171717),
        ) else ThemePalette(
            Color(0xFF111111), Color(0xFF1A1A1A), Color(0xFF242424), Color(0xFFF7F7F7),
            Color(0xFFB7B7B7), Color(0xFFF7F7F7), Color(0xFF111111), Color(0xFFCECECE), Color(0xFFE5E5E5),
        )
        ThemePreset.VIOLET_CURRENT, ThemePreset.GLASS -> if (isLight) ThemePalette(
            Color(0xFFF1EAFA), Color.White, Color(0xFFE9DEF5), Color(0xFF211535),
            Color(0xFF6F6280), Color(0xFFEF821B), Color(0xFF24172B), Color(0xFF7544BE), Color(0xFFD5C5E8),
        ) else ThemePalette(
            Color(0xFF11104C), Color(0xFF100E18), Color(0xFF201939), Color(0xFFFBF9FF),
            Color(0xFFB3A9C8), Color(0xFFFF8B20), Color(0xFF17111F), Color(0xFF6514B9), Color(0xFF3A2B58),
        )
        ThemePreset.PETAL_PLAYER -> if (isLight) ThemePalette(
            Color(0xFFEFEDF1), Color.White, Color(0xFFFAF7FB), Color(0xFF17151A),
            Color(0xFF77717A), Color(0xFFEC65B5), Color.White, Color(0xFFF7D9F2), Color(0xFFDED9DF),
        ) else ThemePalette(
            Color(0xFF19151A), Color(0xFF211C22), Color(0xFF2D2530), Color(0xFFF9F4FA),
            Color(0xFFBDB2C0), Color(0xFFFF91D2), Color(0xFF251522), Color(0xFF4D3047), Color(0xFF443846),
        )
        ThemePreset.NEUMORPHIC_BLUE, ThemePreset.OLED -> if (isLight) ThemePalette(
            Color(0xFFB3E5FC), Color(0xFFD5EAF5), Color(0xFF9CD3FF), Color(0xFF173456),
            Color(0xFF426987), Color(0xFF438FBE), Color.White, Color(0xFF5BA9D8), Color(0xFFC8E4F1),
        ) else ThemePalette(
            Color(0xFF20374D), Color(0xFF29455E), Color(0xFF315674), Color(0xFFEDF7FF),
            Color(0xFFC3D9E9), Color(0xFF91CCEF), Color(0xFF173047), Color(0xFF438FBE), Color(0xFF3E5F7A),
        )
        ThemePreset.CUSTOM -> error("Custom palette handled above")
    }
}

fun buildColorScheme(s: ThemeSettings): ColorScheme {
    val p = palette(s.preset, s.isDark, s)
    return if (s.isDark) darkColorScheme(
        primary = p.accent,
        onPrimary = p.onAccent,
        secondary = p.secondary,
        onSecondary = onColor(p.secondary),
        background = p.background,
        onBackground = p.text,
        surface = p.surface,
        onSurface = p.text,
        surfaceVariant = p.surfaceAlt,
        onSurfaceVariant = p.muted,
        outline = p.outline,
        primaryContainer = p.accent.copy(alpha = 0.25f),
        onPrimaryContainer = p.text,
    ) else lightColorScheme(
        primary = p.accent,
        onPrimary = p.onAccent,
        secondary = p.secondary,
        onSecondary = onColor(p.secondary),
        background = p.background,
        onBackground = p.text,
        surface = p.surface,
        onSurface = p.text,
        surfaceVariant = p.surfaceAlt,
        onSurfaceVariant = p.muted,
        outline = p.outline,
        primaryContainer = p.accent.copy(alpha = 0.14f),
        onPrimaryContainer = p.text,
    )
}

val LocalThemePreset = staticCompositionLocalOf { ThemePreset.ORGANIC_LIGHT }

fun themeCardShape(preset: ThemePreset): Shape = when (preset) {
    ThemePreset.ORGANIC_LIGHT, ThemePreset.DEFAULT, ThemePreset.CUSTOM -> RoundedCornerShape(16.dp)
    ThemePreset.OUTLINE_MONO -> RoundedCornerShape(6.dp)
    ThemePreset.VIOLET_CURRENT, ThemePreset.GLASS -> RoundedCornerShape(14.dp)
    ThemePreset.PETAL_PLAYER -> RoundedCornerShape(24.dp)
    ThemePreset.NEUMORPHIC_BLUE, ThemePreset.OLED -> RoundedCornerShape(22.dp)
}

fun themeUsesOutline(preset: ThemePreset) =
    preset == ThemePreset.OUTLINE_MONO

fun themeUsesNeumorphism(preset: ThemePreset) =
    preset == ThemePreset.NEUMORPHIC_BLUE || preset == ThemePreset.OLED

@Composable
fun ResonixTheme(settings: ThemeSettings, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalThemePreset provides settings.preset) {
        MaterialTheme(colorScheme = buildColorScheme(settings), content = content)
    }
}

fun Modifier.themedBackground(base: Color): Modifier = background(base)

@Composable
fun ThemeBuilderScreen(repo: ThemeRepository, settings: ThemeSettings, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.theme_picker_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.theme_appearance), style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !settings.isDark,
                onClick = { repo.setDarkMode(false) },
                label = { Text(stringResource(R.string.theme_light)) },
            )
            FilterChip(
                selected = settings.isDark,
                onClick = { repo.setDarkMode(true) },
                label = { Text(stringResource(R.string.theme_dark)) },
            )
        }

        listOf(
            ThemePreset.ORGANIC_LIGHT,
            ThemePreset.OUTLINE_MONO,
            ThemePreset.VIOLET_CURRENT,
            ThemePreset.PETAL_PLAYER,
            ThemePreset.NEUMORPHIC_BLUE,
        ).forEach { preset ->
            ThemePresetCard(
                preset = preset,
                selected = settings.preset == preset,
                isDark = settings.isDark,
                onClick = { repo.applyPreset(preset) },
            )
        }

        FilterChip(
            selected = settings.preset == ThemePreset.CUSTOM,
            onClick = { repo.applyPreset(ThemePreset.CUSTOM) },
            label = { Text(stringResource(R.string.preset_custom)) },
        )

        if (settings.preset == ThemePreset.CUSTOM) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = themeCardShape(settings.preset),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.theme_builder), style = MaterialTheme.typography.titleMedium)
                    HsvPicker(stringResource(R.string.primary_color), settings.primary) { repo.setCustomColors(primary = it) }
                    HsvPicker(stringResource(R.string.secondary_color), settings.secondary) { repo.setCustomColors(secondary = it) }
                }
            }
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
private fun ThemePresetCard(preset: ThemePreset, selected: Boolean, isDark: Boolean, onClick: () -> Unit) {
    val preview = palette(preset, isDark, ThemeSettings(preset = preset, isDark = isDark))
    val shape = themeCardShape(preset)
    Column(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ThemeMiniPreview(preset, preview)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(preset.labelRes), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        when (preset) {
                            ThemePreset.ORGANIC_LIGHT -> R.string.theme_organic_description
                            ThemePreset.OUTLINE_MONO -> R.string.theme_outline_description
                            ThemePreset.VIOLET_CURRENT -> R.string.theme_violet_description
                            ThemePreset.PETAL_PLAYER -> R.string.theme_petal_description
                            else -> R.string.theme_neumorphic_description
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.theme_selected), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ThemeMiniPreview(preset: ThemePreset, colors: ThemePalette) {
    val frameShape = RoundedCornerShape(11.dp)
    Column(
        Modifier.fillMaxWidth().height(144.dp).clip(frameShape).background(colors.background).padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (preset == ThemePreset.OUTLINE_MONO) R.string.theme_discover else R.string.nav_library),
                Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = colors.text,
                fontWeight = FontWeight.Bold,
            )
            Box(
                Modifier.size(19.dp).clip(CircleShape)
                    .background(if (preset == ThemePreset.VIOLET_CURRENT) colors.accent else colors.surfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (preset == ThemePreset.VIOLET_CURRENT) Icons.Default.PlayArrow else Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = if (preset == ThemePreset.VIOLET_CURRENT) colors.onAccent else colors.text,
                )
            }
        }

        when (preset) {
            ThemePreset.ORGANIC_LIGHT -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(3) { index ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (index == 1) colors.surfaceAlt else colors.surface),
                            )
                            MiniTextLine(colors.text, Modifier.fillMaxWidth(.78f))
                        }
                    }
                }
                MiniTrackLine(colors, preset, widthFraction = .82f)
                MiniNavigation(colors, selectedIndex = 1)
            }
            ThemePreset.OUTLINE_MONO -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(3) {
                        Box(
                            Modifier.weight(1f).height(43.dp).background(colors.surface)
                                .border(1.dp, colors.outline, RoundedCornerShape(5.dp)),
                        )
                    }
                }
                MiniTrackLine(colors, preset)
                MiniNavigation(colors, selectedIndex = 0)
            }
            ThemePreset.VIOLET_CURRENT -> {
                Box(
                    Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(8.dp))
                        .background(colors.accent),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(colors.surfaceAlt))
                        Spacer(Modifier.width(9.dp))
                        MiniTextLine(colors.onAccent, Modifier.fillMaxWidth(.35f))
                    }
                }
                MiniTrackLine(colors, preset)
                MiniNavigation(colors, selectedIndex = 1)
            }
            ThemePreset.PETAL_PLAYER -> {
                Box(
                    Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(17.dp))
                        .background(colors.secondary.copy(alpha = .55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(35.dp).clip(CircleShape).background(colors.surface))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { MiniTrackLine(colors, preset, Modifier.weight(1f)) }
                }
                MiniNavigation(colors, selectedIndex = 2)
            }
            ThemePreset.NEUMORPHIC_BLUE -> {
                Row(Modifier.fillMaxWidth().height(68.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier.width(28.dp).height(62.dp).clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceAlt).border(2.dp, colors.surface, RoundedCornerShape(10.dp))
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        repeat(4) {
                            Box(Modifier.size(7.dp).clip(CircleShape).background(colors.accent))
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(5.dp))) {
                        Box(
                            Modifier.size(132.dp).offset(x = 36.dp, y = 4.dp)
                                .clip(CircleShape).background(colors.surfaceAlt)
                                .border(2.dp, colors.surface, CircleShape),
                        )
                        Box(
                            Modifier.size(78.dp).offset(x = 64.dp, y = 30.dp)
                                .clip(CircleShape).background(colors.secondary)
                                .border(2.dp, colors.surface, CircleShape),
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun MiniTrackLine(colors: ThemePalette, preset: ThemePreset, modifier: Modifier = Modifier, widthFraction: Float = 1f) {
    val outline = preset == ThemePreset.OUTLINE_MONO
    Row(
        modifier.fillMaxWidth(widthFraction)
            .height(27.dp)
            .clip(if (outline) RoundedCornerShape(4.dp) else RoundedCornerShape(8.dp))
            .background(colors.surface)
            .then(if (outline) Modifier.border(1.dp, colors.outline, RoundedCornerShape(4.dp)) else Modifier)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(15.dp).clip(if (preset == ThemePreset.NEUMORPHIC_BLUE) CircleShape else RoundedCornerShape(4.dp))
                .background(colors.secondary),
        )
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            MiniTextLine(colors.text, Modifier.fillMaxWidth(.7f))
            MiniTextLine(colors.muted, Modifier.fillMaxWidth(.42f))
        }
        Box(Modifier.size(5.dp).clip(CircleShape).background(colors.accent))
    }
}

@Composable
private fun MiniTextLine(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.height(4.dp).clip(CircleShape).background(color.copy(alpha = .78f)))
}

@Composable
private fun MiniNavigation(colors: ThemePalette, selectedIndex: Int) {
    Row(
        Modifier.fillMaxWidth().height(17.dp).clip(RoundedCornerShape(6.dp))
            .background(colors.surface).padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(4) { index ->
            Box(
                Modifier.width(if (index == selectedIndex) 22.dp else 12.dp)
                    .height(4.dp).clip(CircleShape)
                    .background(if (index == selectedIndex) colors.accent else colors.muted.copy(alpha = .35f)),
            )
        }
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
