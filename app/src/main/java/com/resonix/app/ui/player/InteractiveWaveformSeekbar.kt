package com.resonix.app.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.resonix.app.core.formatTime
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun InteractiveWaveformSeekbar(
    waveform: FloatArray,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    playedColor: Color = MaterialTheme.colorScheme.primary,
    unplayedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
) {
    var drag by remember { mutableStateOf<Float?>(null) }
    var widthPx by remember { mutableIntStateOf(1) }
    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val shown = drag ?: progress
    val bars = if (waveform.isEmpty()) FloatArray(80) { 0.08f } else waveform

    Column(modifier.fillMaxWidth()) {
        // Tooltip lane
        Box(Modifier.fillMaxWidth().height(32.dp)) {
            drag?.let { f ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.offset {
                        val tipW = 56.dp.toPx()
                        IntOffset((f * widthPx - tipW / 2).roundToInt().coerceIn(0, max(0, (widthPx - tipW).roundToInt())), 0)
                    },
                ) {
                    Text(
                        formatTime((f * durationMs).toLong()),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .onSizeChanged { widthPx = it.width.coerceAtLeast(1) }
                .pointerInput(durationMs) {
                    detectTapGestures { o -> onSeek((o.x / size.width * durationMs).toLong()) }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { drag = (it.x / size.width).coerceIn(0f, 1f) },
                        onDragEnd = {
                            drag?.let { f -> onSeek((f * durationMs).toLong()) }
                            drag = null
                        },
                        onDragCancel = { drag = null },
                    ) { change, _ ->
                        change.consume()
                        drag = (change.position.x / size.width).coerceIn(0f, 1f)
                    }
                }
        ) {
            val n = bars.size
            val barW = size.width / n
            val cy = size.height / 2
            for (i in 0 until n) {
                val h = max(bars[i], 0.05f) * size.height * 0.92f
                val x = i * barW + barW / 2
                drawLine(
                    color = if ((i + 0.5f) / n <= shown) playedColor else unplayedColor,
                    start = Offset(x, cy - h / 2),
                    end = Offset(x, cy + h / 2),
                    strokeWidth = barW * 0.6f,
                    cap = StrokeCap.Round,
                )
            }
            drawLine(
                color = playedColor,
                start = Offset(shown * size.width, 0f),
                end = Offset(shown * size.width, size.height),
                strokeWidth = 2.dp.toPx(),
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime((shown * durationMs).toLong()), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall)
        }
    }
}
