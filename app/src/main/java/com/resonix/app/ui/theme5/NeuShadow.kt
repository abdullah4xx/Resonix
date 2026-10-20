package com.resonix.app.ui.theme5

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

data class NeuShadow(
    val dx: Dp,
    val dy: Dp,
    val blur: Dp,
    val color: Color,
    val spread: Dp = Dp.Zero,
)

fun Modifier.neuShadow(shape: Shape, shadows: List<NeuShadow>): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    if (shadows.isEmpty() || w < 1f || h < 1f) {
        return@drawWithCache onDrawBehind { }
    }

    val pad = ceil(
        shadows.maxOf { max(abs(it.dx.toPx()), abs(it.dy.toPx())) + it.blur.toPx() + it.spread.toPx() },
    ).toInt() + 2
    val bitmap = Bitmap.createBitmap(
        (w + pad * 2).toInt().coerceAtLeast(1),
        (h + pad * 2).toInt().coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = AndroidCanvas(bitmap)
    canvas.translate(pad.toFloat(), pad.toFloat())
    val path = shape.createOutline(size, layoutDirection, this).toPath().asAndroidPath()

    for (shadow in shadows) {
        val sigma = shadow.blur.toPx() / 2f
        val radius = max(0.01f, (sigma - 0.5f) / 0.57735f)
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.TRANSPARENT
            setShadowLayer(radius, shadow.dx.toPx(), shadow.dy.toPx(), shadow.color.toArgb())
            if (shadow.spread.toPx() != 0f) {
                style = AndroidPaint.Style.FILL_AND_STROKE
                strokeWidth = shadow.spread.toPx() * 2f
            }
        }
        canvas.drawPath(path, paint)
    }

    val image = bitmap.asImageBitmap()
    onDrawBehind {
        drawIntoCanvas {
            it.drawImage(image, Offset(-pad.toFloat(), -pad.toFloat()), androidx.compose.ui.graphics.Paint())
        }
    }
}

private fun Outline.toPath(): Path = when (this) {
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
    is Outline.Generic -> path
}
