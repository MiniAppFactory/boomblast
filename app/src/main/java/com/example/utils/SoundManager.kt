package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlin.math.sin
import kotlin.random.Random

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var blastTrack: AudioTrack? = null
    private var pickupTrack: AudioTrack? = null
    private var lockTrack: AudioTrack? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to init ToneGenerator", e)
        }
        try {
            blastTrack = buildBlastTrack()
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to build blast AudioTrack", e)
        }
        try {
            pickupTrack = buildPickupTrack()
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to build pickup AudioTrack", e)
        }
        try {
            lockTrack = buildLockTrack()
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to build lock AudioTrack", e)
        }
    }

    fun playBeep(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    fun playSuccess(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 100)
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    fun playHit(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    // Faz 5: harici ses dosyasi gerektirmeyen, AudioTrack ile sentezlenmis
    // dolgun bir "patlama" efekti — satir/kombo temizlenince calinir.
    // ToneGenerator'in ince bipinden cok daha "juice"li.
    fun playBlast(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            blastTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    // Faz 11: parca alirken "swipe/whoosh" ve yerlestirirken "lock/click" sesleri —
    // onceden ikisi de ayni jenerik ToneGenerator bipini kullaniyordu, kullanici
    // geri bildirimi: "sesler cok kotu, sürükleyince swipe, birakinca lock sesi olmali".
    fun playPickup(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            pickupTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    fun playLock(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            lockTrack?.let { track ->
                track.stop()
                track.reloadStaticData()
                track.play()
            }
        } catch (e: Exception) {
            // Ignore audio exceptions gracefully
        }
    }

    private fun buildPickupTrack(): AudioTrack {
        // Kisa, yukselen frekansli bir "whoosh" — parmakla parcayi tepsiden
        // kaldirma hissi. Frekans zamanla artan bir sinüs + hafif gurultu.
        val sampleRate = 44100
        val durationMs = 70
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(7)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = sin(Math.PI * t).toFloat() // yumusak giris/cikis
            val freq = 260.0 + 620.0 * t // 260Hz -> 880Hz yukselen swipe
            phase += 2.0 * Math.PI * freq / sampleRate
            val tone = sin(phase).toFloat()
            val hiss = (random.nextFloat() * 2f - 1f) * 0.18f
            val sample = ((tone * 0.8f + hiss) * envelope * Short.MAX_VALUE * 0.55f)
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buildStaticTrack(sampleRate, samples)
    }

    private fun buildLockTrack(): AudioTrack {
        // Kisa, sert bir "click/lock" — parcanin ızgaraya oturdugu hissi.
        // Yuksek frekansli kisa bir tik + hafif dusuk frekansli thump, hizli sonum.
        val sampleRate = 44100
        val durationMs = 65
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = (1f - t).let { it * it * it } // cok hizli sonen kubik zarf
            val click = sin(2.0 * Math.PI * 1400.0 * i / sampleRate).toFloat()
            val thump = sin(2.0 * Math.PI * 180.0 * i / sampleRate).toFloat()
            val sample = ((click * 0.5f + thump * 0.5f) * envelope * Short.MAX_VALUE * 0.7f)
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buildStaticTrack(sampleRate, samples)
    }

    private fun buildStaticTrack(sampleRate: Int, samples: ShortArray): AudioTrack {
        val bufferSizeBytes = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }

    private fun buildBlastTrack(): AudioTrack {
        val sampleRate = 44100
        val durationMs = 180
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(42)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val envelope = (1f - t) * (1f - t) // hizli sonen kuadratik zarf
            val noise = random.nextFloat() * 2f - 1f
            val thump = sin(2.0 * Math.PI * 150.0 * i / sampleRate).toFloat()
            val sample = ((noise * 0.55f + thump * 0.45f) * envelope * Short.MAX_VALUE * 0.75f)
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val bufferSizeBytes = numSamples * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }
}
