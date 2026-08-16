package com.miniappfactory.boomblocks.ads

/**
 * Faz 114: gecis reklami SIKLIK SINIRI.
 *
 * Sorun (uc denetimden ve TODO madde 1'den): interstitial'in zaman bazli hicbir
 * ust siniri yoktu. Pro Mod'da ust uste kaybeden bir oyuncu "YENIDEN BASLA" /
 * "HARITAYA DON" dongusunde ~30-60 saniyede bir tam ekran reklam goruyordu ve
 * bunun tavani yoktu. Faz 108'in yeniden-giris korumasi bunu COZMEZ: o yalnizca
 * ucusta olan TEK bir gosterimi ikilemeyi engelliyor, arka arkaya gelen ayri
 * gosterimlere hic dokunmuyor.
 *
 * Bu, AdMob degil **Play** tarafinin "disruptive ads" politikasinin yaptirim
 * alani — yani sonucu uygulama askiya alinmasi olabilir, sadece gelir kaybi
 * degil.
 *
 * Bu nesne Android'e hic bagimli DEGILDIR (SystemClock disaridan verilir), bu
 * yuzden dogrudan JVM unit testiyle kilitlenebilir.
 *
 * ILKE: sinir devreye girdiginde oyuncu ASLA bloklanmaz — reklam atlanir ve
 * akis aynen surer. Sinir bir "bekleme" degil, bir "vazgecme"dir.
 */
object InterstitialFrequencyPolicy {

    /**
     * Iki gercek gosterim arasindaki en kisa sure.
     *
     * 60 sn secildi: sikayete konu olan "30-60 saniyede bir" hizli-ates
     * dongusunu keser, ama normal oynanista bir bolum genellikle 60 sn'den uzun
     * surdugu icin bolum-sonu reklamina pratikte hic dokunmaz — yani planlanan
     * gelir korunur, yalnizca kotuye giden dongu budanir.
     */
    const val MIN_INTERVAL_MS = 60_000L

    /**
     * Uygulama acildiktan sonra gecis reklaminin hic gosterilmedigi sure.
     *
     * Ilk oturum tamamen korumasizdi: yeni kurulan uygulamada oyuncu daha ne
     * oynadigini anlamadan tam ekran reklam gorebiliyordu — hem ilk izlenim hem
     * de tutunma acisindan en pahali yer. 45 sn, oyuncunun en az bir bolumu
     * bitirmesine yetiyor.
     */
    const val SESSION_GRACE_MS = 45_000L

    /**
     * @param nowMs             su anki monotonik zaman (SystemClock.elapsedRealtime)
     * @param lastShownAtMs     en son GERCEKTEN gosterilip kapatilan reklamin zamani,
     *                          hic gosterilmediyse 0
     * @param sessionStartMs    surecin/oturumun basladigi monotonik zaman
     *
     * @return reklam istenebilir mi
     */
    fun allow(
        nowMs: Long,
        lastShownAtMs: Long,
        sessionStartMs: Long
    ): Boolean {
        if (nowMs - sessionStartMs < SESSION_GRACE_MS) return false
        // lastShownAtMs == 0 => bu oturumda hic gosterilmedi, sadece grace gecerli.
        if (lastShownAtMs != 0L && nowMs - lastShownAtMs < MIN_INTERVAL_MS) return false
        return true
    }
}
