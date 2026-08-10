package com.example.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R
import kotlin.random.Random

// Faz 49: onceden TUM sesler AudioTrack ile elle sentezleniyordu (sinüs +
// gurultu + zarf matematigi) — kullanici defalarca (Faz 27-33 arasi da dahil)
// "patlama kağıt yırtılması gibi", "kilit sesi sünük" diye bildirdi. Elle
// yazilan dalga formulleriyle gercekci bir patlama/tikirti sesi yakalamanin
// bir tavani var — gercek, profesyonelce tasarlanmis SFX dosyalarina
// (Kenney.nl "Impact Sounds" + "Interface Sounds", CC0/telifsiz) gecildi.
// AudioTrack yerine SoundPool kullaniliyor — kisa SFX'ler icin Android'in
// standart, dusuk gecikmeli cozumu.
object SoundManager {
    private var soundPool: SoundPool? = null
    private var pickupId = 0
    private var lockId = 0
    private var blastId = 0
    private val comboIds = IntArray(4)
    private var chimeId = 0
    private var successId = 0
    private var successId2 = 0

    // Faz 27/28: dogrudan kaydirici degeri = kazanc kullanimi "artırınca
    // artmıyor" hissi veriyordu — %50 mutedil bir referans noktasi, ustu
    // kalan payi kapliyor. SoundPool.play() de 0f-1f volume aliyor, ayni
    // egri burada da gecerli.
    @Volatile
    private var volume: Float = 0.5f
    private const val REFERENCE_GAIN = 0.42f

    fun setVolume(value: Float) {
        val v = value.coerceIn(0f, 1f)
        volume = if (v <= 0.5f) {
            (v / 0.5f) * REFERENCE_GAIN
        } else {
            REFERENCE_GAIN + ((v - 0.5f) / 0.5f) * (1f - REFERENCE_GAIN)
        }
    }

    fun init(context: Context) {
        if (soundPool != null) return
        // Faz 30: USAGE_GAME, Samsung Game Booster/Game Launcher'in bu akisa
        // ozel ses kisitlamasi uygulamasina yol acmisti (kullanici geri
        // bildirimi: sesler cok kisikti) — ayni dersi burada da uyguluyoruz,
        // herhangi bir muzik/medya oynaticinin kullandigi standart kombinasyon.
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(attributes)
            .build()
        soundPool = pool
        pickupId = pool.load(context, R.raw.sfx_pickup, 1)
        lockId = pool.load(context, R.raw.sfx_lock, 1)
        blastId = pool.load(context, R.raw.sfx_blast, 1)
        comboIds[0] = pool.load(context, R.raw.sfx_combo_0, 1)
        comboIds[1] = pool.load(context, R.raw.sfx_combo_1, 1)
        comboIds[2] = pool.load(context, R.raw.sfx_combo_2, 1)
        comboIds[3] = pool.load(context, R.raw.sfx_combo_3, 1)
        chimeId = pool.load(context, R.raw.sfx_chime, 1)
        successId = pool.load(context, R.raw.sfx_success, 1)
        successId2 = pool.load(context, R.raw.sfx_success_2, 1)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }

    private fun play(id: Int) {
        if (id == 0) return
        soundPool?.play(id, volume, volume, 1, 0, 1f)
    }

    // Faz 11: parca alirken "swipe" ve yerlestirirken "lock" sesleri ayri
    // olmali (kullanici geri bildirimi). Faz 49: artik gercek SFX dosyalari.
    fun playPickup(soundEnabled: Boolean) {
        if (soundEnabled) play(pickupId)
    }

    fun playLock(soundEnabled: Boolean) {
        if (soundEnabled) play(lockId)
    }

    // Tema secimi gibi kucuk UI tiklamalari icin de ayni kisa "pickup" tikini
    // kullaniyoruz — ayri bir dosya gerektirmeyecek kadar kucuk bir etkilesim.
    fun playBeep(soundEnabled: Boolean) {
        if (soundEnabled) play(pickupId)
    }

    // Faz 52: kullanici Level Complete icin iki ayri zafer sesi gonderdi
    // (orkestral "swell" + kalabalik alkis/tezahurat) — ikisi arasinda
    // rastgele seciliyor, boylece seviye tamamlanirken cesitlilik olur.
    fun playSuccess(soundEnabled: Boolean) {
        if (!soundEnabled) return
        play(if (Random.nextBoolean()) successId else successId2)
    }

    fun playBlast(soundEnabled: Boolean) {
        if (soundEnabled) play(blastId)
    }

    // Faz 33: 2+ satir/sutun ayni anda temizlenince playBlast YERINE bu
    // cagrilir. Faz 49: gercek "punch" darbesi (kombo seviyesine gore 4
    // farkli varyant) + ustune bindirilmis kisa bir odul "chime"i — SoundPool
    // iki sesi ayni anda calabildigi icin gercek bir mix dosyasi gerekmiyor.
    fun playComboBlast(soundEnabled: Boolean, comboCount: Int) {
        if (!soundEnabled) return
        val index = (comboCount - 2).coerceIn(0, 3)
        play(comboIds[index])
        play(chimeId)
    }
}
