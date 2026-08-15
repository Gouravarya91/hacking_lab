package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Web Audio API style Sound Manager & Synthesizer for CYBER_LAB_PRO.
 * Generates custom multi-waveform sound effects (sine, square, sawtooth, noise)
 * with precise envelope shaping, pitch sweeps, and harmonics for an authentic cyber aesthetic.
 */
object CyberAudioEngine {
    var isMuted: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 44100
    private var vibrator: Vibrator? = null

    fun initialize(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun triggerVibration(type: SoundType) {
        if (isMuted || vibrator?.hasVibrator() != true) return
        try {
            val effect = when (type) {
                SoundType.KEY_CLACK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    } else {
                        VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                SoundType.TAB_SWITCH, SoundType.BUTTON_CLICK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    } else {
                        VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                SoundType.EXECUTE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else {
                        VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                SoundType.SUCCESS, SoundType.CTF_FLAG_SOLVED -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    } else {
                        val timings = longArrayOf(0, 15, 30, 20)
                        VibrationEffect.createWaveform(timings, -1)
                    }
                }
                SoundType.ERROR -> {
                    val timings = longArrayOf(0, 40, 40, 40)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        VibrationEffect.createWaveform(timings, -1)
                    } else {
                        VibrationEffect.createWaveform(timings, -1)
                    }
                }
                else -> null
            }
            effect?.let { vibrator?.vibrate(it) }
        } catch (e: Exception) {
            // Ignore vibration exceptions
        }
    }

    enum class SoundType {
        KEY_CLACK,
        EXECUTE,
        SUCCESS,
        ERROR,
        TAB_SWITCH,
        BUTTON_CLICK,
        ALERT_SIREN,
        SCAN_PING,
        CTF_FLAG_SOLVED,
        MATRIX_CASCADE,
        AI_CHIRP
    }

    enum class Waveform {
        SINE,
        SQUARE,
        SAWTOOTH,
        TRIANGLE,
        NOISE
    }

    // Cache pre-generated short sound buffers for zero-latency execution
    private val bufferCache = ConcurrentHashMap<SoundType, ShortArray>()

    init {
        // Pre-warm buffer generation in background
        scope.launch {
            try {
                bufferCache[SoundType.BUTTON_CLICK] = synthesizeTone(
                    startFreq = 1050f,
                    endFreq = 1200f,
                    durationMs = 25,
                    waveform = Waveform.SINE,
                    volume = 0.35f
                )
                bufferCache[SoundType.TAB_SWITCH] = synthesizeTone(
                    startFreq = 850f,
                    endFreq = 1100f,
                    durationMs = 30,
                    waveform = Waveform.TRIANGLE,
                    volume = 0.3f
                )
                bufferCache[SoundType.SCAN_PING] = synthesizeSonarPing()
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }
    }

    fun playSound(type: SoundType) {
        if (isMuted) return
        triggerVibration(type)
        
        scope.launch {
            try {
                when (type) {
                    SoundType.KEY_CLACK -> playKeyClack()
                    SoundType.EXECUTE -> playCommandExecute()
                    SoundType.SUCCESS -> playCommandSuccess()
                    SoundType.ERROR -> playCommandError()
                    SoundType.TAB_SWITCH -> playFromCacheOrSynthesize(SoundType.TAB_SWITCH) {
                        synthesizeTone(850f, 1100f, 30, Waveform.TRIANGLE, 0.3f)
                    }
                    SoundType.BUTTON_CLICK -> playFromCacheOrSynthesize(SoundType.BUTTON_CLICK) {
                        synthesizeTone(1050f, 1200f, 25, Waveform.SINE, 0.35f)
                    }
                    SoundType.ALERT_SIREN -> playAlertSiren()
                    SoundType.SCAN_PING -> playFromCacheOrSynthesize(SoundType.SCAN_PING) {
                        synthesizeSonarPing()
                    }
                    SoundType.CTF_FLAG_SOLVED -> playCtfFlagSolved()
                    SoundType.MATRIX_CASCADE -> playMatrixCascade()
                    SoundType.AI_CHIRP -> playAiChirp()
                }
            } catch (e: Exception) {
                // Ignore audio errors gracefully on restricted audio devices
            }
        }
    }

    /**
     * Mechanical Cyber Terminal Keyboard typing sound.
     * Features randomized micro-pitch shift and subtle noise impulse for realism.
     */
    private fun playKeyClack() {
        val randomFreq = Random.nextInt(1150, 1450).toFloat()
        val durationMs = Random.nextInt(14, 20)
        val buffer = synthesizeMechanicalKey(randomFreq, durationMs)
        playBuffer(buffer)
    }

    /**
     * Futuristic ascending laser execution blip.
     */
    private fun playCommandExecute() {
        val buffer = synthesizeTone(
            startFreq = 700f,
            endFreq = 1750f,
            durationMs = 70,
            waveform = Waveform.SAWTOOTH,
            volume = 0.45f
        )
        playBuffer(buffer)
    }

    /**
     * Uplifting harmonic major cyber chord arpeggio for successful command runs.
     */
    private fun playCommandSuccess() {
        // Arpeggiate C5 (523Hz) -> E5 (659Hz) -> G5 (784Hz) -> C6 (1046Hz)
        val freqs = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val buffer = synthesizeArpeggio(freqs, noteDurationMs = 35, waveform = Waveform.SINE, volume = 0.4f)
        playBuffer(buffer)
    }

    /**
     * Low glitch dual-tone buzz alarm for command errors.
     */
    private fun playCommandError() {
        val buffer = synthesizeDualTone(
            freq1 = 220f,
            freq2 = 145f,
            durationMs = 120,
            waveform = Waveform.SAWTOOTH,
            volume = 0.5f
        )
        playBuffer(buffer)
    }

    /**
     * Warbling emergency alarm siren.
     */
    private fun playAlertSiren() {
        val b1 = synthesizeTone(1500f, 1100f, 60, Waveform.SQUARE, 0.4f)
        val b2 = synthesizeTone(1100f, 1600f, 60, Waveform.SQUARE, 0.4f)
        playBuffer(b1)
        playBuffer(b2)
    }

    /**
     * Epic CTF triumph fanfare.
     */
    private fun playCtfFlagSolved() {
        val freqs = listOf(440f, 554.37f, 659.25f, 880f, 1108.73f)
        val buffer = synthesizeArpeggio(freqs, noteDurationMs = 50, waveform = Waveform.TRIANGLE, volume = 0.5f)
        playBuffer(buffer)
    }

    /**
     * High-speed data stream cascade tone.
     */
    private fun playMatrixCascade() {
        val freqs = listOf(1400f, 1200f, 1600f, 900f, 1800f, 2100f)
        val buffer = synthesizeArpeggio(freqs, noteDurationMs = 20, waveform = Waveform.SINE, volume = 0.35f)
        playBuffer(buffer)
    }

    /**
     * Soft friendly AI chirp.
     */
    private fun playAiChirp() {
        val buffer = synthesizeTone(
            startFreq = 880f,
            endFreq = 1320f,
            durationMs = 45,
            waveform = Waveform.SINE,
            volume = 0.35f
        )
        playBuffer(buffer)
    }

    private fun playFromCacheOrSynthesize(type: SoundType, fallback: () -> ShortArray) {
        val cached = bufferCache[type]
        if (cached != null) {
            playBuffer(cached)
        } else {
            val generated = fallback()
            bufferCache[type] = generated
            playBuffer(generated)
        }
    }

    // =========================================================================
    // Core Web Audio Style Synthesizer Functions
    // =========================================================================

    /**
     * Synthesizes mechanical keyboard click with dual layer: pitch tone + noise texture
     */
    private fun synthesizeMechanicalKey(frequency: Float, durationMs: Int): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            // Fast exponential decay envelope
            val decay = (1.0 - (i.toDouble() / numSamples))
            val envelope = decay * decay

            // 85% Sine + 15% Noise texture for realistic mechanical tactile sound
            val sineVal = sin(2.0 * PI * frequency * time)
            val noiseVal = (Random.nextDouble() * 2.0 - 1.0)
            val mixed = (sineVal * 0.85 + noiseVal * 0.15) * Short.MAX_VALUE * 0.32 * envelope

            buffer[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Synthesizes a tone with optional frequency sweep (pitch ramp) & ADSR envelope.
     */
    private fun synthesizeTone(
        startFreq: Float,
        endFreq: Float,
        durationMs: Int,
        waveform: Waveform,
        volume: Float = 0.5f
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress

            // Attack & Release Envelope
            val envelope = when {
                progress < 0.1 -> progress / 0.1
                progress > 0.7 -> (1.0 - progress) / 0.3
                else -> 1.0
            }

            phase += 2.0 * PI * currentFreq / SAMPLE_RATE
            if (phase > 2.0 * PI) phase -= 2.0 * PI

            val sampleVal = when (waveform) {
                Waveform.SINE -> sin(phase)
                Waveform.SQUARE -> if (sin(phase) >= 0) 1.0 else -1.0
                Waveform.SAWTOOTH -> (phase / PI) - 1.0
                Waveform.TRIANGLE -> (2.0 / PI) * Math.asin(sin(phase).coerceIn(-1.0, 1.0))
                Waveform.NOISE -> Random.nextDouble() * 2.0 - 1.0
            }

            val finalSample = (sampleVal * Short.MAX_VALUE * volume * envelope).toInt()
            buffer[i] = finalSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Synthesizes dual-tone harmonic dissonance (used for errors/glitches).
     */
    private fun synthesizeDualTone(
        freq1: Float,
        freq2: Float,
        durationMs: Int,
        waveform: Waveform,
        volume: Float = 0.5f
    ): ShortArray {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val envelope = (1.0 - progress)

            phase1 += 2.0 * PI * freq1 / SAMPLE_RATE
            phase2 += 2.0 * PI * freq2 / SAMPLE_RATE

            val v1 = if (waveform == Waveform.SAWTOOTH) (phase1 % (2.0 * PI) / PI) - 1.0 else sin(phase1)
            val v2 = if (waveform == Waveform.SAWTOOTH) (phase2 % (2.0 * PI) / PI) - 1.0 else sin(phase2)

            val mixed = ((v1 * 0.6 + v2 * 0.4) * Short.MAX_VALUE * volume * envelope).toInt()
            buffer[i] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Synthesizes sonar ping with ringing decay.
     */
    private fun synthesizeSonarPing(): ShortArray {
        val durationMs = 80
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val time = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / numSamples
            val decay = Math.exp(-progress * 5.0)

            val sample = sin(2.0 * PI * 1760.0 * time) * Short.MAX_VALUE * 0.4 * decay
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    /**
     * Synthesizes consecutive arpeggio notes into a single concatenated audio buffer.
     */
    private fun synthesizeArpeggio(
        frequencies: List<Float>,
        noteDurationMs: Int,
        waveform: Waveform,
        volume: Float
    ): ShortArray {
        val noteSamples = (SAMPLE_RATE * (noteDurationMs / 1000.0)).toInt()
        val totalSamples = noteSamples * frequencies.size
        val buffer = ShortArray(totalSamples)

        frequencies.forEachIndexed { noteIndex, freq ->
            val startIdx = noteIndex * noteSamples
            var phase = 0.0

            for (i in 0 until noteSamples) {
                val progress = i.toDouble() / noteSamples
                val envelope = when {
                    progress < 0.1 -> progress / 0.1
                    progress > 0.8 -> (1.0 - progress) / 0.2
                    else -> 1.0
                }

                phase += 2.0 * PI * freq / SAMPLE_RATE
                if (phase > 2.0 * PI) phase -= 2.0 * PI

                val raw = when (waveform) {
                    Waveform.SINE -> sin(phase)
                    Waveform.TRIANGLE -> (2.0 / PI) * Math.asin(sin(phase).coerceIn(-1.0, 1.0))
                    else -> sin(phase)
                }

                val sample = (raw * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[startIdx + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
        return buffer
    }

    /**
     * Plays raw 16-bit PCM ShortArray using Android AudioTrack static mode.
     */
    private fun playBuffer(buffer: ShortArray) {
        if (buffer.isEmpty()) return
        try {
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
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            val durationMs = (buffer.size.toDouble() / SAMPLE_RATE * 1000).toLong() + 15
            Thread.sleep(durationMs)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            // AudioTrack may fail gracefully in headless or restricted container environments
        }
    }
}
