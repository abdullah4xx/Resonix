package com.resonix.app.ui.theme5

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeuGradientText(text: String, fontSize: TextUnit, fontWeight: FontWeight, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            brush = Brush.horizontalGradient(listOf(Neu.TitleStart, Neu.TitleEnd)),
            fontSize = fontSize,
            fontWeight = fontWeight,
        ),
    )
}

@Composable
private fun NeuIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.requiredSize(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, Modifier.size(30.dp), tint = Neu.Icon)
    }
}

@Composable
fun NeuControlPill(
    playing: Boolean,
    onRepeat: () -> Unit,
    onNext: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    descRepeat: String,
    descNext: String,
    descToggle: String,
    descPrevious: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(25.dp)
    Column(
        modifier.size(width = 70.dp, height = 417.dp)
            .neuShadow(shape, Neu.ControlPillShadow)
            .background(Neu.Surface, shape)
            .border(1.dp, Neu.Highlight, shape)
            .padding(21.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeuIconButton(Icons.Default.Replay, descRepeat, onRepeat)
        NeuIconButton(Icons.Default.SkipNext, descNext, onNext)
        NeuIconButton(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, descToggle, onToggle)
        NeuIconButton(Icons.Default.SkipPrevious, descPrevious, onPrevious)
    }
}

@Composable
fun Theme5PlayerLayout(
    title: String,
    artist: String,
    device: String,
    playing: Boolean,
    onRepeat: () -> Unit,
    onNext: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    descRepeat: String,
    descNext: String,
    descToggle: String,
    descPrevious: String,
    cover: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(modifier.fillMaxSize().background(Neu.Background)) {
            Spacer(Modifier.height(80.dp))
            Column(Modifier.padding(start = 20.dp, end = 20.dp)) {
                NeuGradientText(title, 32.sp, FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                NeuGradientText(artist, 18.sp, FontWeight.Normal)
                Spacer(Modifier.height(5.dp))
                Text(device, color = Neu.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Spacer(Modifier.height(50.dp))
            Row(Modifier.fillMaxWidth().clipToBounds()) {
                Spacer(Modifier.width(20.dp))
                NeuControlPill(
                    playing, onRepeat, onNext, onToggle, onPrevious,
                    descRepeat, descNext, descToggle, descPrevious,
                )
                Spacer(Modifier.width(50.dp))
                Box(
                    Modifier.requiredSize(500.dp)
                        .neuShadow(CircleShape, Neu.BigCircleShadow)
                        .background(Neu.Surface, CircleShape)
                        .border(3.dp, Neu.Highlight, CircleShape),
                ) {
                    Box(
                        Modifier.offset(100.dp, 100.dp).size(300.dp)
                            .clip(CircleShape).border(3.dp, Neu.Highlight, CircleShape),
                    ) { cover() }
                }
            }
        }
    }
}

@Composable
fun NeuPlaylistTile(
    name: String,
    count: String,
    cover: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.size(width = 134.dp, height = 252.dp)
            .neuShadow(shape, Neu.TileShadows)
            .background(Neu.Tile, shape)
            .border(4.dp, Neu.TileBorder, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth().height(167.dp)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomEnd = 10.dp, bottomStart = 10.dp)),
        ) { cover() }
        Spacer(Modifier.height(10.dp))
        NeuGradientText(name, 12.sp, FontWeight.Normal)
        Text(count, color = Neu.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun NeuWideCard(
    name: String,
    count: String,
    cover: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier.fillMaxWidth().height(188.dp)
            .neuShadow(shape, Neu.WideCardShadows)
            .border(4.dp, Neu.Highlight, shape)
            .clip(shape)
            .clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize()) { cover() }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 15.dp, bottom = 12.dp)) {
            Text(name, color = Color.White, fontSize = 18.sp, maxLines = 1)
            Text(count, color = Color.White, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
fun NeuSectionHeader(
    title: String,
    moreDescription: String,
    modifier: Modifier = Modifier,
    onMore: () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, color = Neu.SectionTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        val shape = RoundedCornerShape(20.dp)
        Box(
            Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clip(shape).clickable(role = Role.Button, onClick = onMore),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(width = 35.dp, height = 22.dp)
                    .neuShadow(shape, Neu.PillRimShadow)
                    .background(Neu.Pill, shape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.MoreHoriz, moreDescription, Modifier.size(20.dp), tint = Color.White)
            }
        }
    }
}
