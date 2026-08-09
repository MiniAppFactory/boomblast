package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var blastTrack: AudioTrack? = null
    private var pickupTrack: AudioTrack? = null
    private var lockTrack: AudioTrack? = null

    // Faz 27: sesler telefonun sesi sonuna kadar acik olsa bile cok kisik
    // kaliyordu (kullanici geri bildirimi) — kok neden, sentezlenen PCM
    // ornekleklerin dijital tavanin sadece %55-70'ini kullanmasiydi. Genlik
    // tavana cikarildi, kalan "yukseklik" artik bu Volume ile calisma anda
    // ayarlanabiliyor. Varsayilan 0.5f, ONCEKI sabit ses seviyesine yakin —
    // Ayarlar'daki kaydirici ile kullanici bunu 0'dan (kisik degil, dusuk) yeni,
    // daha yuksek tavana (1.0f) kadar yukseltebiliyor.
    @Volatile
    private var volume: Float = 0.5f

    // Faz 28: duz lineer gain (kaydirici degeri = ses gain'i) kullanici geri
    // bildiriminde "artırınca artmıyor" olarak yasandi — kok neden, %50'nin
    // ONCEKI sabit ses seviyesine denk gelmesi icin gain'i zaten tavana yakin
    // (~0.5-0.6) tutmak gerekiyordu, bu da %50-%100 arasinda gercekte cok az
    // fark kalmasina yol aciyordu (dijital tavan 1.0'i asamiyoruz). Artik
    // %50 daha MUTEDIL bir referans noktasi (REFERENCE_GAIN) ve %50-%100 araligi
    // KALAN TUM gain payini kapliyor — kaydiricinin ust yarisini hareket
    // ettirmek artik gercekten, belirgin sekilde daha yuksek ses veriyor.
    private const val REFERENCE_GAIN = 0.42f

    fun setVolume(value: Float) {
        val v = value.coerceIn(0f, 1f)
        volume = if (v <= 0.5f) {
            (v / 0.5f) * REFERENCE_GAIN
        } else {
            REFERENCE_GAIN + ((v - 0.5f) / 0.5f) * (1f - REFERENCE_GAIN)
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
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
                track.setVolume(volume)
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
                track.setVolume(volume)
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
                track.setVolume(volume)
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
            val sample = ((tone * 0.8f + hiss) * envelope * Short.MAX_VALUE * 0.9f)
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
            val sample = ((click * 0.5f + thump * 0.5f) * envelope * Short.MAX_VALUE * 0.95f)
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buildStaticTrack(sampleRate, samples)
    }

    private fun buildStaticTrack(sampleRate: Int, samples: ShortArray): AudioTrack {
        val bufferSizeBytes = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    // Faz 30: Faz 29'daki USAGE_GAME + CONTENT_TYPE_MUSIC denemesi
                    // kullanicida sesi DAHA DA kistı (hipotez yanlis cikti — muhtemelen
                    // USAGE_GAME, Samsung Game Booster/Game Launcher'in bu akisa
                    // ozel ses isleme/sinirlama uygulamasina yol aciyordu). USAGE_MEDIA
                    // + CONTENT_TYPE_MUSIC — herhangi bir muzik/medya oynaticinin
                    // kullandigi, ozel oyun-modu ses isleme almayan standart kombinasyona
                    // gecildi.
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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

    // Faz 28: onceki 3-notali muzikal akor kullanici tarafindan "mekanik cirkin
    // bir ses, patlama sesi gibi degil, balon patlaması gibi de degil" olarak
    // reddedildi. Tasarim BASTAN yapildi — muzikal nota YOK, bunun yerine gercek
    // oyun SFX kutuphanelerindeki standart "patlama/pop" yapisi kullanildi:
    //   1) "crack": genis bantli, hizli sonen filtrelenmis gurultu (patlamanin
    //      sert, kirilma hissi veren ilk anı)
    //   2) "boom": pitch'i hizla dusen alcak frekansli bir sub-thump (agirlik/guc)
    //   3) "click": cok kisa, yuksek frekansli bir tik (atagi sertlestirir, "pop")
    // Ucu birlikte, hizli atak + eksponansiyel sonumle karisiyor.
    private fun buildBlastTrack(): AudioTrack {
        val sampleRate = 44100
        val durationMs = 210
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(11)

        var lowPassState = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples

            // Crack: beyaz gurultu, hafif alcak-gecirgen filtrelenip sertligi
            // biraz yumusatiliyor, cok hizli sonuyor (patlamanin "kirilma" ani).
            val crackEnvelope = exp(-t * 13f)
            val noise = random.nextFloat() * 2f - 1f
            lowPassState += (noise - lowPassState) * 0.55f
            val crack = lowPassState * crackEnvelope

            // Boom: 190Hz'den 60Hz'e hizla dusen bir sub-thump — patlamaya
            // "agirlik" ve "guc" katan alcak frekansli katman.
            val boomFreq = 190.0 - 130.0 * (1.0 - exp(-t * 6.0))
            val boomEnvelope = exp(-t * 4.5f)
            val boomPhase = 2.0 * Math.PI * boomFreq * i / sampleRate
            val boom = sin(boomPhase).toFloat() * boomEnvelope

            // Click: cok kisa, yuksek frekansli bir tik — atagi sertlestirip
            // "pop" hissi katıyor, ilk birkaç milisaniyede kayboluyor.
            val clickEnvelope = exp(-t * 55f)
            val click = sin(2.0 * Math.PI * 2600.0 * i / sampleRate).toFloat() * clickEnvelope

            val mixed = crack * 0.6f + boom * 0.8f + click * 0.22f
            // Faz 29: yumusak dogrusal-olmayan doyum (tanh) — RMS enerjisini
            // (algilanan yuksekligi) tepe genligi ASMADAN artirir, gercek patlama
            // kayitlarindaki hafif "gritty" karakterin de bir kismini verir —
            // profesyonel oyun SFX masterlamada standart bir "loudness" teknigi.
            val drive = 1.6f
            val saturated = tanh(mixed * drive) / tanh(drive)
            val sample = saturated * Short.MAX_VALUE * 0.98f
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return buildStaticTrack(sampleRate, samples)
    }
}
