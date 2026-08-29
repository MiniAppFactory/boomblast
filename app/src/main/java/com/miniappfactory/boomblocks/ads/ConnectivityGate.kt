package com.miniappfactory.boomblocks.ads

/**
 * Faz 166: odullu reklam sunulup sunulmayacaginin SAF karari.
 *
 * NEDEN VAR — kullanicinin tespit ettigi istismar:
 *
 *   "Reklam cikmasa da devam et demistik. O zaman mobil veriyi kapatip o tusa
 *    basarsa hic reklam izlemeden gecer."
 *
 * Zincir soyle isliyordu:
 *   ucak modu -> RewardedAd.load basarisiz -> onFailure -> onDenied
 *   -> BoomBlocksGame'de `onRequestContinueAd(proceed, proceed)` (Faz 95c/98)
 *   -> oyuncu tahtayi KORUYARAK bedava devam ediyor.
 *
 * Faz 95c/98'deki comertlik BILINCLI bir karardi ve DOGRU: gercek bir no-fill
 * oyuncunun hatasi degil, oyuncu diyalogda kilitli kalmamali. Sorun comertlik
 * degil, comertligin ISTEYEREK tetiklenebilmesi. Ayrim su:
 *
 *   ag VAR  + reklam gelmedi  -> gercek no-fill, oyuncuyu cezalandirma (comert kal)
 *   ag YOK                    -> reklam hic istenemez, teklif de edilmemeli
 *
 * Kullanicinin kabul ettigi takas:
 *   "Wi-Fi olmayan yerde oynayan kisi o zaman reklam izle devam et veya odul
 *    reklam al diyemez, bu da dogru bir trade."
 *
 * Yani cevrimdisi oyuncu oyunu oynamaya devam eder — yalnizca ODULLU reklam
 * yuzeyleri kapanir. Oynanis, skor, bolum ilerlemesi etkilenmez.
 *
 * Bu nesne Android'e BAGIMSIZ tutuldu (`RetryAdPolicy`,
 * `InterstitialFrequencyPolicy`, `AllClearRules` ile ayni disiplin) — karar
 * JVM testinde dogrulanabilsin diye. Agin GERCEKTEN var olup olmadigini olcmek
 * `NetworkReachability.kt`'nin isi.
 */
object ConnectivityGate {

    /**
     * Odullu reklam yuzeyi (buton/cip) oyuncuya SUNULSUN mu?
     *
     * @param hasNetwork dogrulanmis internet erisimi var mi (bkz. NetworkReachability)
     * @param consentGiven UMP rizasi reklam istemeye izin veriyor mu (AdsConsent)
     */
    fun canOfferRewardedAd(hasNetwork: Boolean, consentGiven: Boolean): Boolean =
        hasNetwork && consentGiven

    /**
     * SDK'ya gercekten yukleme istegi gonderilsin mi?
     *
     * `canOfferRewardedAd` ile ayni kosullara ek olarak in-flight kontrolu icerir:
     * ayni anda ikinci bir yukleme baslatmak arka arkaya iki tam ekran reklam
     * demek ve bu Play'in "disruptive ads" politikasinin dogrudan ihlali.
     */
    fun shouldAttemptLoad(
        hasNetwork: Boolean,
        consentGiven: Boolean,
        loadInFlight: Boolean
    ): Boolean = canOfferRewardedAd(hasNetwork, consentGiven) && !loadInFlight
}
