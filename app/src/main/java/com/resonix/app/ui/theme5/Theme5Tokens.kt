package com.resonix.app.ui.theme5

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Neu {
    val Background = Color(0xFFB3E5FC)
    val Surface = Color(0xFF9CD3FF)
    val Icon = Color(0xFF72A8D5)
    val ShadowDark = Color(0xFF90B8D9)
    val TitleStart = Color(0xFF002369)
    val TitleEnd = Color(0xFF000000)
    val TextSecondary = Color(0xFF64B5F6)
    val SectionTitle = Color(0xFF6FABD3)
    val Pill = Color(0xFF9ECCE4)
    val PillHighlight = Color(0xB9FFFFFF)
    val Tile = Color(0xFFD5EAF5)
    val TileBorder = Color(0xFFC8E4F1)
    val SoftShadow = Color(0xFFA6CEE0)
    val DeepShadow = Color(0xFF76B4CD)
    val Highlight = Color.White

    val ControlPillShadow = listOf(NeuShadow(10.dp, 0.dp, 20.dp, ShadowDark))
    val BigCircleShadow = listOf(NeuShadow(2.dp, 20.dp, 20.dp, ShadowDark, spread = 10.dp))
    val TileShadows = listOf(
        NeuShadow((-5).dp, 0.dp, 2.dp, Highlight),
        NeuShadow(4.dp, 4.dp, 4.dp, SoftShadow),
        NeuShadow(10.dp, 10.dp, 22.dp, DeepShadow),
        NeuShadow((-10).dp, (-10).dp, 22.dp, Highlight),
    )
    val WideCardShadows = listOf(
        NeuShadow(4.dp, 4.dp, 4.dp, SoftShadow),
        NeuShadow(10.dp, 10.dp, 22.dp, DeepShadow),
        NeuShadow((-5).dp, 0.dp, 2.dp, Highlight),
    )
    val PillRimShadow = listOf(NeuShadow(0.dp, (-1).dp, 1.dp, PillHighlight))
}
