package com.example.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.audiofx.LoudnessEnhancer
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
    // Faz 33: kullanicinin kendi hazirlayip yolladigi referans WAV'lara ve
    // Kotlin taslagina dayanarak eklendi — kombo (2+ satir) icin yukselen
    // pentatonik arpejli, coskulu bir "kombo patlamasi" (2x/3x/4x/5x+ ayri
    // tonlarda, seviye arttikca perde yukseliyor).
    private var comboBlastTracks: Array<AudioTrack?> = arrayOfNulls(4)

    // Faz 33: PCM ornekler zaten dijital tavana (Short.MAX_VALUE'a) kadar
    // cikarilmis durumda — bu noktadan sonra "daha da yukselt" demek sadece
    // dijital kirpilma (clipping/distortion) demek, gercek bir kazanc degil.
    // LoudnessEnhancer, Android'in TAM OLARAK bu senaryo icin (zaten mastered
    // edilmis/tavana yakin icerigi 0dBFS UZERINE cikarmak) sagladigi resmi bir
    // AudioEffect — dinamik aralik sikistirmasi uygulayarak algilanan sesi
    // saf kazanctan farkli olarak asiri kirpilma yaratmadan artirir. Her
    // AudioTrack'in kendi audio session'ina baglaniyor, referanslar GC'ye
    // gitmesin diye burada tutuluyor.
    private val loudnessEnhancers = mutableListOf<LoudnessEnhancer>()

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
        try {
            for (level in 0 until 4) comboBlastTracks[level] = buildComboBlastTrack(level)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to build combo blast AudioTracks", e)
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

    // Faz 33: 2+ satir/sutun ayni anda temizlenince (gercek kombo) playBlast
    // YERINE bu cagrilir — tekli patlamadan bilincli olarak daha coskulu ve
    // uzun (yukselen arpej + parıltı riser), boylece kombo aninin "buyuk bir
    // sey oldu" hissi net ayirt edilebiliyor.
    fun playComboBlast(soundEnabled: Boolean, comboCount: Int) {
        if (!soundEnabled) return
        try {
            val index = (comboCount - 2).coerceIn(0, 3)
            comboBlastTracks[index]?.let { track ->
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

    // Faz 33: Faz 32'deki sabit-130Hz "thud" hala kullanicinin kendi hazirladigi
    // referans tasarimda "surdurulmus/muzikal" riskine yakin bulundu — sabit
    // perde YERINE 190Hz'den 120Hz'e HIZLA dusen bir "thock" kullanildi, hicbir
    // an sabit bir perdede kalmiyor, bu yuzden akor/nefesli calgi hissi olusmuyor.
    private fun buildLockTrack(): AudioTrack {
        val sampleRate = 44100
        val durationMs = 60
        val numSamples = sampleRate * durationMs / 1000
        val samples = ShortArray(numSamples)
        val random = Random(3)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples

            // Tik: genis bantli gurultu, cok hizli sonuyor — perde hissi yok,
            // sadece mekanik bir "klik".
            val tickEnvelope = exp(-t * 70f)
            val tick = (random.nextFloat() * 2f - 1f) * tickEnvelope

            // Thock: 190Hz -> 120Hz hizla dusen tek darbe, sabit perdede kalmaz.
            val freq = 190.0 - 70.0 * (1.0 - exp(-t * 6.0))
            phase += 2.0 * Math.PI * freq / sampleRate
            val thock = sin(phase).toFloat() * exp(-t * 26f)

            val mixed = tick * 0.6f + thock * 0.7f
            val drive = 1.5f
            val saturated = tanh(mixed * drive) / tanh(drive)
            val sample = saturated * Short.MAX_VALUE * 0.95f
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
        try {
            val enhancer = LoudnessEnhancer(track.audioSessionId)
            // +20dB algisal kazanc — LoudnessEnhancer bunu duz gain degil,
            // dinamik aralik sikistirmasiyla uyguluyor, bu yuzden zaten tavana
            // yakin bir sinyalde bile kaba kirpilma yerine gercek bir "daha
            // yuksek/dolgun" his verir (profesyonel "loudness maximizer"
            // eklentilerinin kullandigi teknikle ayni prensip).
            enhancer.setTargetGain(2000)
            enhancer.enabled = true
            loudnessEnhancers.add(enhancer)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to attach LoudnessEnhancer", e)
        }
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
        var popPhase = 0.0

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

            // Faz 33: kullanicinin referans tasariminda eklenen "pop" — 900Hz'den
            // 500Hz'e hizla dusen, cok kisa parlak bir tepe katmani, patlamaya
            // "cıtırtılı/pırıltılı" bir ust-frekans karakteri katiyor.
            val popFreq = 900.0 - 400.0 * (1.0 - exp(-t * 8.0))
            popPhase += 2.0 * Math.PI * popFreq / sampleRate
            val pop = sin(popPhase).toFloat() * exp(-t * 18f) * 0.35f

            val mixed = punch * 0.5f + boom * 0.85f + body * 0.35f + pop
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

    // Faz 33: kullanicinin kendi hazirlayip yolladigi referans WAV/kod tasarimina
    // dayanarak eklendi — 2+ satir/sutun ayni anda temizlenince tekli patlamadan
    // bilincli olarak daha uzun/coskulu bir "kombo patlamasi": patlama govdesi +
    // yukselen pentatonik arpej (kombo seviyesi arttikca perde de yukseliyor) +
    // parlak bir "sweep" riser + kapanista kisa bir parıltı (sparkle) darbesi.
    private fun buildComboBlastTrack(comboIndex: Int): AudioTrack {
        val sampleRate = 44100
        val durationMs = 1100
        val numSamples = sampleRate * durationMs / 1000
        val samples = FloatArray(numSamples)
        val random = Random(11)

        var boomPhase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val punch = (random.nextFloat() * 2f - 1f) * exp(-t * 38f)
            val boomFreq = 160.0 - 115.0 * (1.0 - exp(-t * 4.5))
            boomPhase += 2.0 * Math.PI * boomFreq / sampleRate
            val boom = sin(boomPhase).toFloat() * exp(-t * 3.0f)
            val body = (random.nextFloat() * 2f - 1f) * 0.45f * exp(-t * 9f)
            samples[i] = punch * 0.42f + boom * 0.85f + body * 0.28f
        }

        val baseFreq = 293.66 * Math.pow(2.0, comboIndex * 2.0 / 12.0)
        val steps = floatArrayOf(1f, 1.125f, 1.25f, 1.5f, 1.6667f, 2f, 2.25f, 2.5f, 3f, 3.3334f, 4f)
        var noteTime = 0.10
        var gap = 0.085
        var lastNoteStart = 0
        for (k in steps.indices) {
            val startSample = (sampleRate * noteTime).toInt()
            if (startSample >= numSamples) break
            lastNoteStart = startSample
            val freq = baseFreq * steps[k]
            val amp = 0.30f + 0.030f * k
            for (i in startSample until numSamples) {
                val tk = (i - startSample).toFloat() / sampleRate
                var ping = sin(2.0 * Math.PI * freq * tk).toFloat() * exp(-tk * 9f) * amp
                ping += sin(2.0 * Math.PI * freq * 2.0 * tk).toFloat() * exp(-tk * 12f) * amp * 0.35f
                ping += sin(2.0 * Math.PI * freq * 3.0 * tk).toFloat() * exp(-tk * 16f) * amp * 0.12f
                samples[i] += ping
            }
            noteTime += gap
            gap *= 0.93
        }

        var sweepPhase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / numSamples
            val sweepFreq = 1500.0 + 2500.0 * Math.pow(t.toDouble(), 1.6)
            sweepPhase += 2.0 * Math.PI * sweepFreq / sampleRate
            val tone = sin(sweepPhase).toFloat() * 0.5f + (random.nextFloat() * 2f - 1f) * 0.5f
            val env = t * t * exp(-((t - 0.85f) * (t - 0.85f)) / 0.03f)
            samples[i] += tone * env * 0.30f
        }
        for (i in lastNoteStart until numSamples) {
            val tk = (i - lastNoteStart).toFloat() / sampleRate
            samples[i] += sin(2.0 * Math.PI * baseFreq * 4.0 * tk).toFloat() * exp(-tk * 10f) * 0.35f
        }

        val out = ShortArray(numSamples)
        val drive = 1.5f
        for (i in 0 until numSamples) {
            val saturated = tanh(samples[i] * drive) / tanh(drive)
            out[i] = (saturated * Short.MAX_VALUE * 0.98f).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buildStaticTrack(sampleRate, out)
    }
}
