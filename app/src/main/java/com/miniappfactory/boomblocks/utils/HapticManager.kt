package com.miniappfactory.boomblocks.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Faz 105: Kariyer / Pro / Sonsuz modlarinda COKLU patlama titresimi.
//
// Neden sadece 2+ satir: kullanici acik istedi ("1 satirda olmasin"). Her
// hamlede titreyen bir oyun birkac dakikada bunaltir ve titresimin "bir sey
// basardin" anlamini tamamen oldurur — tek satir zaten patlamanin varsayilan
// hali, ozel bir an degil.
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
     * Tek hamlede birden fazla satir/sutun patlayinca calisir.
     *
     * @param totalLinesCleared bu hamlede temizlenen satir + sutun sayisi
     * @param comboCount art arda AYRI hamlelerde patlatma serisi (ses tarafiyla ayni kavram)
     */
    fun playClearHaptic(hapticsEnabled: Boolean, totalLinesCleared: Int, comboCount: Int) {
        if (!hapticsEnabled) return
        // Kullanici karari: tek satirda titresim YOK. Kombo serisi ne kadar
        // uzun olursa olsun bu kural bozulmuyor — kombo sadece asagida
        // siddeti bir kademe artirabiliyor.
        if (totalLinesCleared < 2) return
        val v = vibrator ?: return

        // 0 = 2 satir, 1 = 3 satir, 2 = 4+ satir. 3x ve uzeri kombo seride
        // olan oyuncuyu bir kademe yukari tasir (ses tarafinda playComboBlast
        // da comboCount'a gore varyant seciyor, ikisi tutarli kalsin).
        val tier = (totalLinesCleared - 2 + if (comboCount >= 3) 1 else 0).coerceIn(0, 2)

        try {
            when {
                // En iyi durum: genlik kontrollu cihaz (API 26+ ve donanim destekli).
                hasAmplitudeControl -> {
                    val effect = when (tier) {
                        0 -> VibrationEffect.createOneShot(20L, 110)
                        1 -> VibrationEffect.createOneShot(35L, 180)
                        // Cift vurus: kisa on darbe + 40ms sessizlik + tam guclu
                        // asil darbe. Toplam 95ms, tek uzun titresimden cok daha
                        // "patlama" gibi hissettiriyor.
                        else -> VibrationEffect.createWaveform(
                            longArrayOf(0L, 25L, 40L, 30L),
                            intArrayOf(0, 160, 0, 255),
                            -1
                        )
                    }
                    v.vibrate(effect)
                }
                // API 26+ ama genlik kontrolu yok: siddeti sadece sure/desen ayirir.
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    val effect = when (tier) {
                        0 -> VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE)
                        1 -> VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
                        else -> VibrationEffect.createWaveform(longArrayOf(0L, 25L, 40L, 30L), -1)
                    }
                    v.vibrate(effect)
                }
                // API 24-25 (minSdk 24): eski API. RetroSoundManager ile ayni desen.
                else -> {
                    @Suppress("DEPRECATION")
                    when (tier) {
                        0 -> v.vibrate(20L)
                        1 -> v.vibrate(35L)
                        else -> v.vibrate(longArrayOf(0L, 25L, 40L, 30L), -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Titresim hicbir zaman oyunu bozacak bir sey degil — uretici
            // kaynakli istisnalar sessizce yutulur (RetroSoundManager ile ayni ilke).
        }
    }
}
