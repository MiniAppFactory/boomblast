package com.miniappfactory.boomblocks.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlin.random.Random

// Faz 34: kullanicinin orijinal "Block Blast" oyununu izleyip fark ettigi
// detay — kombo kelimeleri (Good!/Great!/Unstoppable! vb.) sadece gorsel
// degil, sesli de soyleniyor. Bizde zaten kombo buyuklugune gore artan bir
// ovgu kelimesi sistemi vardi (bkz. BlastTheBlocksGame.kt praiseWord) —
// eksik olan SES tarafiydi. Harici ses dosyasi/kayitli insan seslendirmesi
// GEREKMEDEN, cihazin kendi yerlesik TextToSpeech motoruyla bu kelimeler
// aninda seslendiriliyor. Gercek "duygusal" seslendirme (insan aktor
// tonlamasi) TTS motorlarinda yok — bunun yerine, kombo buyudukce pitch
// (perde) ve konusma hizini kademeli artirarak daha "heyecanli/tiz" bir
// ton elde ediliyor (guvenilir, olculebilir bir teknik).
object TextToSpeechManager {
    private var tts: TextToSpeech? = null

    @Volatile
    private var isReady = false

    @Volatile
    private var trAvailable = false

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = status == TextToSpeech.SUCCESS
            if (isReady) {
                val trResult = tts?.isLanguageAvailable(Locale("tr", "TR"))
                trAvailable = trResult != null &&
                    trResult >= TextToSpeech.LANG_AVAILABLE
            }
        }
    }

    // excitementLevel: 0 (en dusuk, "PATLAMA!/BLAST!") .. 5+ (en yuksek,
    // "İNANILMAZ!/AMAZING!") — pitch/hiz buna gore olceklenir. Faz 34'un ilk
    // suru "hep ayni robotik tonlamayla SÜPER diyor" seklinde geri bildirim
    // aldi — iki duzeltme birden yapildi: (1) cagiran taraf artik gercek bir
    // "cosku puani" gonderiyor (comboCount tek basina yeterince cesitlenmiyordu),
    // (2) buradaki pitch/hiz araligi belirgin sekilde genisletildi VE her
    // cagrida kucuk rastgele bir sapma (jitter) eklendi — TAMAMEN ayni pitch/
    // hizla soylenen kelime, tekrar tekrar duyulunca "makine gibi" hissi
    // veriyordu, ufak dogal degiskenlik bunu kirar.
    fun speakPraise(text: String, isTr: Boolean, excitementLevel: Int) {
        val engine = tts ?: return
        if (!isReady) return
        try {
            val locale = if (isTr && trAvailable) Locale("tr", "TR") else Locale.US
            engine.language = locale
            val level = excitementLevel.coerceIn(0, 5)
            val pitchJitter = (Random.nextFloat() - 0.5f) * 0.08f
            val rateJitter = (Random.nextFloat() - 0.5f) * 0.06f
            // Faz 51: kullanici en dusuk seviyede bile ("Good!") "cok duz
            // okuyor" dedi — eski taban (1.2 pitch / 1.05 hiz) normal konusmaya
            // cok yakindi, coskulu hissettirmiyordu. Taban degerler VE
            // seviye-basi artis miktari yukseltildi, boylece en dusuk seviye
            // bile belirgin sekilde "heyecanli" baslar, en yuksek seviye de
            // daha da tiz/hizli kalir.
            engine.setPitch((1.35f + level * 0.14f + pitchJitter).coerceAtLeast(0.5f))
            engine.setSpeechRate((1.15f + level * 0.1f + rateJitter).coerceAtLeast(0.5f))
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "combo_praise")
        } catch (e: Exception) {
            Log.e("TextToSpeechManager", "speakPraise failed", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TextToSpeechManager", "shutdown failed", e)
        }
        tts = null
        isReady = false
    }
}
