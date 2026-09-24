package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffectsHelper {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    fun playClick() {
        scope.launch {
            try {
                val durationMs = 80
                val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(totalSamples)
                val fundamentalFreq = 330f // E4 - warm tone
                val overtoneFreq = 660f
                
                for (i in 0 until totalSamples) {
                    val progress = i.toFloat() / totalSamples
                    
                    val angleFund = 2.0 * Math.PI * fundamentalFreq * i / SAMPLE_RATE
                    val angleOvertone = 2.0 * Math.PI * overtoneFreq * i / SAMPLE_RATE
                    
                    // Soft attack (5ms) and quick exponential decay
                    val attackDuration = (SAMPLE_RATE * 0.005f).toInt()
                    val envelope = if (i < attackDuration) {
                        i.toFloat() / attackDuration
                    } else {
                        kotlin.math.exp(-6.0 * progress).toFloat()
                    }
                    
                    // Gentle volume blend (peak ~10000, perfectly comfortable)
                    val wave = (sin(angleFund) * 8000 + sin(angleOvertone) * 2000) * envelope
                    buffer[i] = wave.toInt().toShort()
                }
                playPcm(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNext() {
        scope.launch {
            try {
                // A rising soft interval (E5 to G5)
                val part1 = generateSineWave(659.25f, 60, fadeOut = true)
                val part2 = generateSineWave(783.99f, 90, fadeOut = true)
                
                // Mix with small overlap
                val overlap = 150
                val totalSamples = part1.size + part2.size - overlap
                val buffer = ShortArray(totalSamples)
                System.arraycopy(part1, 0, buffer, 0, part1.size)
                for (i in 0 until part2.size) {
                    val idx = part1.size - overlap + i
                    if (idx < totalSamples) {
                        buffer[idx] = (buffer[idx] + part2[i]).coerceIn(-10000, 10000).toShort()
                    }
                }
                playPcm(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playPrevious() {
        scope.launch {
            try {
                // A descending soft interval (G5 to E5)
                val part1 = generateSineWave(783.99f, 60, fadeOut = true)
                val part2 = generateSineWave(659.25f, 90, fadeOut = true)
                
                // Mix with small overlap
                val overlap = 150
                val totalSamples = part1.size + part2.size - overlap
                val buffer = ShortArray(totalSamples)
                System.arraycopy(part1, 0, buffer, 0, part1.size)
                for (i in 0 until part2.size) {
                    val idx = part1.size - overlap + i
                    if (idx < totalSamples) {
                        buffer[idx] = (buffer[idx] + part2[i]).coerceIn(-10000, 10000).toShort()
                    }
                }
                playPcm(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playSuccess() {
        scope.launch {
            try {
                val totalDurationMs = 900
                val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000f)).toInt()
                val mixBuffer = FloatArray(totalSamples) // Mix with float for high-quality accumulation
                
                // Beautiful pentatonic magical arpeggio (C5, E5, G5, A5, C6, E6)
                val notes = listOf(
                    Pair(523.25f, 0),      // C5
                    Pair(659.25f, 80),     // E5
                    Pair(783.99f, 160),    // G5
                    Pair(880.00f, 240),    // A5
                    Pair(1046.50f, 320),   // C6
                    Pair(1318.51f, 400)    // E6
                )
                
                val noteDurationMs = 450
                val noteSamplesCount = (SAMPLE_RATE * (noteDurationMs / 1000f)).toInt()
                
                for ((freq, startMs) in notes) {
                    val startSample = (SAMPLE_RATE * (startMs / 1000f)).toInt()
                    for (i in 0 until noteSamplesCount) {
                        val mixIndex = startSample + i
                        if (mixIndex >= totalSamples) break
                        
                        val progress = i.toFloat() / noteSamplesCount
                        val angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE
                        
                        // 15ms soft attack to prevent clicking, with a gentle exponential decay
                        val attackSamples = (SAMPLE_RATE * 0.015f).toInt()
                        val envelope = if (i < attackSamples) {
                            i.toFloat() / attackSamples
                        } else {
                            kotlin.math.exp(-3.5 * progress).toFloat()
                        }
                        
                        // Extremely delicate and blended volume
                        val noteAmplitude = 4500f
                        mixBuffer[mixIndex] += (sin(angle) * noteAmplitude * envelope).toFloat()
                    }
                }
                
                // Convert to ShortArray with clipping protection and comfortable master volume
                val finalBuffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    finalBuffer[i] = mixBuffer[i].coerceIn(-13000f, 13000f).toInt().toShort()
                }
                
                playPcm(finalBuffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playFailure() {
        scope.launch {
            try {
                val totalDurationMs = 350
                val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000f)).toInt()
                val buffer = ShortArray(totalSamples)
                
                val p1Duration = (SAMPLE_RATE * 0.15f).toInt()
                val p2Duration = (SAMPLE_RATE * 0.20f).toInt()
                
                // First part of descending gentle tone
                for (i in 0 until p1Duration) {
                    val progress = i.toFloat() / p1Duration
                    val currentFreq = 330f - (330f - 262f) * progress
                    val angle = 2.0 * Math.PI * currentFreq * i / SAMPLE_RATE
                    val envelope = if (progress < 0.1f) progress / 0.1f else (1f - progress)
                    buffer[i] = (sin(angle) * 7000 * envelope).toInt().toShort()
                }
                
                // Second part of descending gentle tone with a soft overlap
                val p2Start = (SAMPLE_RATE * 0.10f).toInt()
                for (i in 0 until p2Duration) {
                    val progress = i.toFloat() / p2Duration
                    val currentFreq = 262f - (262f - 220f) * progress
                    val angle = 2.0 * Math.PI * currentFreq * i / SAMPLE_RATE
                    val envelope = if (progress < 0.1f) progress / 0.1f else (1f - progress)
                    
                    val mixIndex = p2Start + i
                    if (mixIndex < totalSamples) {
                        val sampleVal = (sin(angle) * 7000 * envelope).toInt()
                        buffer[mixIndex] = (buffer[mixIndex] + sampleVal).coerceIn(-10000, 10000).toShort()
                    }
                }
                
                playPcm(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playBubbleSound() {
        scope.launch {
            try {
                val durationMs = 150
                val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val progress = i.toFloat() / totalSamples
                    // Slide pitch rapidly upwards to make a bubbly water/game pop sound
                    val currentFreq = 350f + (900f - 350f) * progress
                    val angle = 2.0 * Math.PI * currentFreq * i / SAMPLE_RATE
                    
                    // 8ms soft attack and smooth fade out
                    val attackDuration = (SAMPLE_RATE * 0.008f).toInt()
                    val envelope = if (i < attackDuration) {
                        i.toFloat() / attackDuration
                    } else {
                        (1f - progress)
                    }
                    
                    buffer[i] = (sin(angle) * 10000 * envelope).toInt().toShort()
                }
                playPcm(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun generateSineWave(frequency: Float, durationMs: Int, fadeOut: Boolean = true): ShortArray {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt()
        val buffer = ShortArray(totalSamples)
        val attackSamples = (SAMPLE_RATE * 0.005f).toInt() // 5ms soft attack to prevent pop clicks
        for (i in 0 until totalSamples) {
            val angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE
            var sample = sin(angle) * 10000 // Ultra comfortable 30% master volume
            
            val progress = i.toFloat() / totalSamples
            val attackEnvelope = if (i < attackSamples) {
                i.toFloat() / attackSamples
            } else {
                1f
            }
            
            val decayEnvelope = if (fadeOut && progress > 0.5f) {
                (1f - progress) / 0.5f
            } else {
                1f
            }
            
            buffer[i] = (sample * attackEnvelope * decayEnvelope).toInt().toShort()
        }
        return buffer
    }

    private fun playPcm(buffer: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackSize = if (minBufferSize > buffer.size * 2) minBufferSize else buffer.size * 2

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        
        scope.launch(Dispatchers.IO) {
            val durationMs = (buffer.size * 1000L) / SAMPLE_RATE
            kotlinx.coroutines.delay(durationMs + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
