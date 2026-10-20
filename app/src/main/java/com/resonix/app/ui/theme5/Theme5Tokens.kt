package com.resonix.app.ui.theme5

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

object Neu {
    private val isDark: Boolean
        @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val Background: Color
        @Composable get() = if (isDark) Color(0xFF14283A) else Color(0xFFB3E5FC)
    val Surface: Color
        @Composable get() = if (isDark) Color(0xFF203A50) else Color(0xFF9CD3FF)
    val Icon: Color
        @Composable get() = if (isDark) Color(0xFFB7DDF5) else Color(0xFF72A8D5)
    val ShadowDark: Color
        @Composable get() = if (isDark) Color(0xFF0A1825) else Color(0xFF90B8D9)
    val TitleStart: Color
        @Composable get() = if (isDark) Color(0xFFE1F4FF) else Color(0xFF002369)
    val TitleEnd: Color
        @Composable get() = if (isDark) Color(0xFF96CAEB) else Color(0xFF000000)
    val TextSecondary: Color
        @Composable get() = if (isDark) Color(0xFFB4D0E3) else Color(0xFF64B5F6)
    val SectionTitle: Color
        @Composable get() = if (isDark) Color(0xFFC2E2F5) else Color(0xFF6FABD3)
    val Pill: Color
        @Composable get() = if (isDark) Color(0xFF2A465D) else Color(0xFF9ECCE4)
    val PillHighlight: Color
        @Composable get() = if (isDark) Color(0x40385A73) else Color(0xB9FFFFFF)
    val Tile: Color
        @Composable get() = if (isDark) Color(0xFF1E384F) else Color(0xFFD5EAF5)
    val TileBorder: Color
        @Composable get() = if (isDark) Color(0xFF2D4A61) else Color(0xFFC8E4F1)
    val SoftShadow: Color
        @Composable get() = if (isDark) Color(0xFF0D2030) else Color(0xFFA6CEE0)
    val DeepShadow: Color
        @Composable get() = if (isDark) Color(0xFF081624) else Color(0xFF76B4CD)
    val Highlight: Color
        @Composable get() = if (isDark) Color(0xFF385A73) else Color.White

    val ControlPillShadow: List<NeuShadow>
        @Composable get() = listOf(NeuShadow(10.dp, 0.dp, 20.dp, ShadowDark))
    val BigCircleShadow: List<NeuShadow>
        @Composable get() = listOf(NeuShadow(2.dp, 20.dp, 20.dp, ShadowDark, spread = 10.dp))
    val TileShadows: List<NeuShadow>
        @Composable get() = listOf(
            NeuShadow((-5).dp, 0.dp, 2.dp, Highlight),
            NeuShadow(4.dp, 4.dp, 4.dp, SoftShadow),
            NeuShadow(10.dp, 10.dp, 22.dp, DeepShadow),
            NeuShadow((-10).dp, (-10).dp, 22.dp, Highlight),
        )
    val WideCardShadows: List<NeuShadow>
        @Composable get() = listOf(
            NeuShadow(4.dp, 4.dp, 4.dp, SoftShadow),
            NeuShadow(10.dp, 10.dp, 22.dp, DeepShadow),
            NeuShadow((-5).dp, 0.dp, 2.dp, Highlight),
        )
    val PillRimShadow: List<NeuShadow>
        @Composable get() = listOf(NeuShadow(0.dp, (-1).dp, 1.dp, PillHighlight))
}
