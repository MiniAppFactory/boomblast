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

    // Faz 32: onceki tasarim IKI SAF SINUS tonunu (1400Hz + 180Hz) TAM AYNI sure
    // boyunca birlikte calıyordu — kullanici bunun "iğrenç bir akordiyon sesi
    // gibi" oldugunu belirtti (iki sabit perdenin birlikte cinlemesi tam olarak
    // bir akor/nefesli calgi tinisi verir). Muzikal perde YOK — gurultu tabanli,
    // cok kisa bir "tik" (mekanik klik) + neredeyse hissedilir olmayan, TEK
    // darbelik bir alcak "thud" (saglam oturma hissi, surdurulmus ton degil).
    private fun buildLockTrack(): AudioTrack {
        val sampleRate = 44100
        val durationMs = 55
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(3)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples

            // Tik: genis bantli gurultu, cok hizli sonuyor — perde hissi yok,
            // sadece mekanik bir "klik".
            val tickEnvelope = exp(-t * 70f)
            val tick = (random.nextFloat() * 2f - 1f) * tickEnvelope

            // Thud: tek bir kisa alcak-frekans darbesi, SURDURULMUS bir ton
            // DEGIL — sadece birkac donguluk bir "oturma" hissi.
            val thudEnvelope = exp(-t * 30f)
            val thud = sin(2.0 * Math.PI * 130.0 * i / sampleRate).toFloat() * thudEnvelope

            val mixed = tick * 0.7f + thud * 0.55f
            val sample = mixed * Short.MAX_VALUE * 0.95f
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

    // Faz 32: Faz 28'deki tasarim kullaniciya "kağıt yırtılmasına benziyor" geldi
    // — kok neden, "crack" katmaninin tek-kutuplu alcak-gecirgen FILTRELENMIS
    // gurultu olmasiydi (bu, ses tasariminda klasik olarak TAM OLARAK kağıt/
    // kırışma dokusu sentezlemek icin kullanilan teknik). Filtrelenmis gurultu
    // tamamen kaldirildi — yerine HAM (filtresiz), cok hizli sonen bir "punch"
    // (gercek bir patlamanin sert ilk darbesi), guclu bir "boom" (agirlik) ve
    // aralarinda kopru gorevi goren kisa bir gövde katmani kullanildi.
    private fun buildBlastTrack(): AudioTrack {
        val sampleRate = 44100
        val durationMs = 260
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(11)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples

            // Punch: HAM (filtresiz) gurultu, cok hizli sonuyor — "crinkle/kağıt"
            // dokusu veren filtrelemeden kacinilarak gercek bir "snap/crack" verir.
            val punchEnvelope = exp(-t * 45f)
            val punch = (random.nextFloat() * 2f - 1f) * punchEnvelope

            // Boom: 170Hz'den 50Hz'e hizla dusen, agirlikli bir sub-thump —
            // patlamanin asil gucu/agirligi burada.
            val boomFreq = 170.0 - 120.0 * (1.0 - exp(-t * 5.0))
            val boomEnvelope = exp(-t * 3.2f)
            val boomPhase = 2.0 * Math.PI * boomFreq * i / sampleRate
            val boom = sin(boomPhase).toFloat() * boomEnvelope

            // Body: punch ile boom arasinda kopruleme yapan, orta sureli bir
            // gurultu govdesi — "ince/havasiz" degil, dolgun bir patlama hissi.
            val bodyEnvelope = exp(-t * 10f)
            val body = (random.nextFloat() * 2f - 1f) * 0.5f * bodyEnvelope

            val mixed = punch * 0.5f + boom * 0.85f + body * 0.35f
            // Faz 29: yumusak dogrusal-olmayan doyum (tanh) — RMS enerjisini
            // (algilanan yuksekligi) tepe genligi ASMADAN artirir, gercek patlama
            // kayitlarindaki hafif "gritty" karakterin de bir kismini verir —
            // profesyonel oyun SFX masterlamada standart bir "loudness" teknigi.
            val drive = 1.5f
            val saturated = tanh(mixed * drive) / tanh(drive)
            val sample = saturated * Short.MAX_VALUE * 0.98f
            samples[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        return buildStaticTrack(sampleRate, samples)
    }
}
