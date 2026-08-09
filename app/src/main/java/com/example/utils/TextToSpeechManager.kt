package com.example.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

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

    // excitementLevel: 0 (en dusuk kombo) .. 4+ (en yuksek kombo) — pitch/hiz
    // buna gore olceklenir, boylece "İNANILMAZ!/AMAZING!" gibi en ust seviye
    // kelimeler kulakta gercekten daha coskulu hissettiriyor.
    fun speakPraise(text: String, isTr: Boolean, excitementLevel: Int) {
        val engine = tts ?: return
        if (!isReady) return
        try {
            val locale = if (isTr && trAvailable) Locale("tr", "TR") else Locale.US
            engine.language = locale
            val level = excitementLevel.coerceIn(0, 4)
            engine.setPitch(1.15f + level * 0.06f)
            engine.setSpeechRate(1.05f + level * 0.05f)
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
