package com.resonix.app.playback

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

object WaveformExtractor {
    private const val MAX_DECODE_US = 30L * 60L * 1_000_000L // longer files use the fallback shape

    suspend fun extract(context: Context, uri: Uri, bars: Int = 100): FloatArray =
        withContext(Dispatchers.Default) {
            val extractor = MediaExtractor()
            var codec: MediaCodec? = null
            try {
                extractor.setDataSource(context, uri, null)
                val idx = (0 until extractor.trackCount).firstOrNull {
                    extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
                } ?: return@withContext fallback(uri, bars)
                extractor.selectTrack(idx)
                val format = extractor.getTrackFormat(idx)
                val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L
                if (durationUs <= 0 || durationUs > MAX_DECODE_US) return@withContext fallback(uri, bars)

                codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
                codec.configure(format, null, null, 0)
                codec.start()

                val peaks = FloatArray(bars)
                val info = MediaCodec.BufferInfo()
                var inputDone = false
                var outputDone = false
                while (!outputDone) {
                    ensureActive()
                    if (!inputDone) {
                        val i = codec.dequeueInputBuffer(5000)
                        if (i >= 0) {
                            val buf = codec.getInputBuffer(i)!!
                            val size = extractor.readSampleData(buf, 0)
                            if (size < 0) {
                                codec.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            } else {
                                codec.queueInputBuffer(i, 0, size, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                    val o = codec.dequeueOutputBuffer(info, 5000)
                    if (o >= 0) {
                        if (info.size > 0) {
                            val out = codec.getOutputBuffer(o)!!
                            out.position(info.offset)
                            out.limit(info.offset + info.size)
                            val shorts = out.order(ByteOrder.nativeOrder()).asShortBuffer()
                            var peak = 0
                            while (shorts.hasRemaining()) {
                                val v = abs(shorts.get().toInt())
                                if (v > peak) peak = v
                            }
                            val bar = ((info.presentationTimeUs.toDouble() / durationUs) * bars).toInt().coerceIn(0, bars - 1)
                            peaks[bar] = max(peaks[bar], peak / 32768f)
                        }
                        codec.releaseOutputBuffer(o, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                }
                val top = peaks.max().takeIf { it > 0f } ?: return@withContext fallback(uri, bars)
                FloatArray(bars) { (peaks[it] / top).coerceIn(0.04f, 1f) }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                fallback(uri, bars)
            } finally {
                try { codec?.stop() } catch (_: Exception) {}
                codec?.release()
                extractor.release()
            }
        }

    /** Deterministic, smooth pseudo-waveform used when decoding isn't possible. */
    private fun fallback(uri: Uri, bars: Int): FloatArray {
        val rnd = Random(uri.toString().hashCode())
        var prev = 0.5f
        return FloatArray(bars) {
            prev = (prev * 0.6f + rnd.nextFloat() * 0.4f).coerceIn(0.1f, 1f)
            prev
        }
    }
}
