package com.miniappfactory.boomblocks.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Faz 105: Kariyer / Pro / Sonsuz modlarinda patlama titresimi.
//
// Faz 105'te kullanici "1 satirda olmasin" demisti; gerekce, her hamlede
// titreyen bir oyunun birkac dakikada bunaltmasi ve titresimin "bir sey
// basardin" anlamini oldurmesiydi.
//
// Faz 153: bu karar kullanici tarafindan TERSINE CEVRILDI — "titresim yapsin
// 1'de de hepsinde de". Esik kaldirildi, ama Faz 105'in kaygisi SIDDET
// KADEMESIYLE korunuyor: tek satir en alt kademede cok hafif bir dokunus
// (12ms / 70 genlik) aliyor, 2 satir eski en alt kademeyi (20ms / 110) aliyor,
// yukarisi hic degismedi. Yani "her patlama bir sey ifade ediyor" ile "kombo
// hala ozel" bir arada duruyor. Ayni yaklasim ekran sarsintisinda da
// uygulandi (bkz. SHAKE_AMPLITUDES_PX).
//
// Neden sure degil GENLIK + VURUS SAYISI ile siddet veriliyor: uzun titresim
// (100ms+) tok bir darbe gibi degil, bildirim vizildamasi gibi hissettirir.
// Oyunlarda darbe hissi kisa ve keskin tutulup siddet genlikle ayrilir; en
// ust kademede tek uzun titresim yerine cift vurus kullaniliyor.
//
// SoundManager ile ayni desen: durum tutmayan bir object, cagiran taraf
// ayardan gelen bayragi gecirir.
object HapticManager {

    private var vibrator: Vibrator? = null
    private var hasAmplitudeControl = false

    // Faz 160: kullanici Galaxy Tab S7'de "titresim ozelligi var ama oyun
    // titresimlerini ALGILAMIYOR" dedi — bildirim/cagri titresimleri calisiyor,
    // 3 satir patlatinca bile HIC gelmiyor. Sadece sure/genlik degil: `vibrate`
    // cagrisi bir KULLANIM NITELIGI (AudioAttributes) tasimiyordu, usage'i
    // "belirsiz" (USAGE_UNKNOWN) kaliyordu. Android 12+ / Samsung'da usage'i
    // tanimsiz uygulama titresimleri bastirilabiliyor; bildirim titresiminin
    // usage'i tanimli oldugu icin o calisiyor. Cozum: her vibrate cagrisina
    // "UI geri bildirimi/sonification" niteligi ver — bu, medya sesinden ve
    // dokunma-geri-bildirimi anahtarindan bagimsiz olarak onurlandirilir.
    private val hapticAttributes: AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

    fun init(context: Context) {
        if (vibrator != null) return
        val appContext = context.applicationContext
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        // Titresim donanimi olmayan cihaz (cogu tablet) — hic tutmuyoruz ki
        // asagidaki her cagri tek bir null kontrolunde erkenden cikabilsin.
        if (v == null || !v.hasVibrator()) return
        vibrator = v
        hasAmplitudeControl = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v.hasAmplitudeControl()
    }

    fun release() {
        vibrator = null
        hasAmplitudeControl = false
    }

    /**
     * Bir satir/sutun patladiginda calisir (Faz 153'ten beri TEK satirda da).
     *
     * @param totalLinesCleared bu hamlede temizlenen satir + sutun sayisi
     * @param comboCount art arda AYRI hamlelerde patlatma serisi (ses tarafiyla ayni kavram)
     */
    fun playClearHaptic(hapticsEnabled: Boolean, totalLinesCleared: Int, comboCount: Int) {
        if (!hapticsEnabled) return
        // Faz 153: "totalLinesCleared < 2 && comboCount < 2 -> return" esigi
        // KALDIRILDI (kullanici: "titresim yapsin 1'de de hepsinde de").
        // Ayrim artik "titrer / titremez" degil, NE KADAR titrer.
        val v = vibrator ?: return

        // Kademeler: 0 = tek satir, 1 = 2 satir, 2 = 3 satir, 3 = 4+ satir.
        // 3x ve uzeri kombo oyuncuyu bir kademe yukari tasir (ses tarafinda
        // playComboBlast da comboCount'a gore varyant seciyor, ikisi tutarli
        // kalsin) — yani seriyi ayakta tutan tek satir da guclenerek duyuluyor.
        // Faz 153 oncesi formulle karsilastirma: her kademe AYNEN bir basamak
        // yukari kaydi, yeni olan SADECE en alttaki "tek satir" basamagi;
        // 2+ satirin hissi degismedi.
        val tier = (totalLinesCleared - 1 + if (comboCount >= 3) 1 else 0).coerceIn(0, 3)

        try {
            when {
                // En iyi durum: genlik kontrollu cihaz (API 26+ ve donanim destekli).
                hasAmplitudeControl -> {
                    val effect = when (tier) {
                        // Faz 160: kullanici Galaxy Tab S7'de "titresim ozelligi
                        // var ama oyun titresimlerini algilamiyor" dedi (bildirim/
                        // cagri titresimleri calisiyor). Kok neden SURE: 22ms'lik
                        // kisa darbede tablet motoru (telefonunkinden yavas/gucsuz)
                        // tam donmeye firsat bulamadan bitiyor, hic hissedilmiyor;
                        // bildirim/cagri desenleri uzun oldugu icin hissediliyor.
                        // Cozum evrensel: SURELER uzatildi (sure her motorda
                        // calisir, genlik destegi olsun olmasin), genlikler de
                        // yukseltildi. Telefonda da daha net, tablette de tetiklenir.
                        //   tek satir 22ms/130 -> 42ms/200
                        0 -> VibrationEffect.createOneShot(42L, 200)
                        1 -> VibrationEffect.createOneShot(55L, 230)
                        2 -> VibrationEffect.createOneShot(70L, 255)
                        // Cift vurus: kisa on darbe + sessizlik + tam guclu asil
                        // darbe. Faz 160'ta sureler uzatildi (motorun donmesi icin).
                        else -> VibrationEffect.createWaveform(
                            longArrayOf(0L, 45L, 45L, 60L),
                            intArrayOf(0, 230, 0, 255),
                            -1
                        )
                    }
                    v.vibrate(effect, hapticAttributes)
                }
                // API 26+ ama genlik kontrolu yok: siddeti sadece sure/desen ayirir.
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    // Genlik ayarlanamiyor, siddet SADECE sureyle ayrilir.
                    // Faz 160: tablet motoru icin sureler uzatildi (bkz. yukarisi).
                    val effect = when (tier) {
                        0 -> VibrationEffect.createOneShot(42L, VibrationEffect.DEFAULT_AMPLITUDE)
                        1 -> VibrationEffect.createOneShot(55L, VibrationEffect.DEFAULT_AMPLITUDE)
                        2 -> VibrationEffect.createOneShot(70L, VibrationEffect.DEFAULT_AMPLITUDE)
                        else -> VibrationEffect.createWaveform(longArrayOf(0L, 45L, 45L, 60L), -1)
                    }
                    v.vibrate(effect, hapticAttributes)
                }
                // API 24-25 (minSdk 24): eski API. RetroSoundManager ile ayni desen.
                else -> {
                    // Faz 160: eski API'de de sureler uzatildi + AudioAttributes
                    // eklendi (vibrate(long, AudioAttributes) API 21'den beri var).
                    @Suppress("DEPRECATION")
                    when (tier) {
                        0 -> v.vibrate(42L, hapticAttributes)
                        1 -> v.vibrate(55L, hapticAttributes)
                        2 -> v.vibrate(70L, hapticAttributes)
                        else -> v.vibrate(longArrayOf(0L, 45L, 45L, 60L), -1, hapticAttributes)
                    }
                }
            }
        } catch (e: Exception) {
            // Titresim hicbir zaman oyunu bozacak bir sey degil — uretici
            // kaynakli istisnalar sessizce yutulur (RetroSoundManager ile ayni ilke).
        }
    }
}
