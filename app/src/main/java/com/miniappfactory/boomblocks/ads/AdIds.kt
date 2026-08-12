package com.miniappfactory.boomblocks.ads

import com.miniappfactory.boomblocks.BuildConfig

// Gercek AdMob App ID + ad unit ID'leri (2026-08-08, kullanicinin kendi
// AdMob konsolunda bu oyun icin olusturuldu). Debug build'lerde hala
// Google'in herkese acik TEST ID'leriyle calisir (Whats-This'teki
// hardcoded-const + BuildConfig.DEBUG deseniyle birebir ayni), sadece
// release build gercek ID'leri kullanir.
object AdIds {
    private const val PRODUCTION_BANNER_AD_UNIT_ID = "ca-app-pub-8582550349019790/5454388648"
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    private const val PRODUCTION_REWARDED_AD_UNIT_ID = "ca-app-pub-8582550349019790/5534279692"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Faz 39/51: Seviye Modu'nda her 2 bolumde bir zorunlu gecis (interstitial)
    // reklami icin. Faz 51: kullanici kendi AdMob konsolunda gercek bir
    // "Interstitial" reklam birimi olusturdu — artik release build'de test
    // ID'si degil, gercek production ID kullaniliyor.
    private const val PRODUCTION_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8582550349019790/4062199744"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun bannerAdUnitId(): String = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PRODUCTION_BANNER_AD_UNIT_ID
    fun rewardedAdUnitId(): String = if (BuildConfig.DEBUG) TEST_REWARDED_AD_UNIT_ID else PRODUCTION_REWARDED_AD_UNIT_ID
    fun interstitialAdUnitId(): String = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else PRODUCTION_INTERSTITIAL_AD_UNIT_ID

    // Faz 38: gelistiricinin/ekibin kendi cihazlarinda RELEASE build (gercek
    // prodüksiyon reklam ID'leri) test edilirken, AdMob'un "kendi reklamini
    // izleme/tiklama" (invalid traffic) politikasi ihlal edilmesin diye —
    // buradaki cihazlara HER ZAMAN Google'in guvenli test/house reklamlari
    // gosterilir, gercek kullanicilar etkilenmez. Her yeni test cihazinin
    // ID'si, o cihazda ilk reklam istegi yapildiginda logcat'te "Ads" etiketiyle
    // otomatik yazdirilir (ornek: "Use RequestConfiguration.Builder()
    // .setTestDeviceIds(Arrays.asList(\"XXXX\")) to get test ads on this device.").
    val developerTestDeviceIds = listOf(
        "4EC2D32786F16937AF9963145EA0E233" // Samsung Galaxy S8 (proje test cihazi)
        // S22 Ultra (kullanicinin kendi telefonu) buraya eklenecek — ID'si
        // henuz alinamadi (cihaz USB baglantisi kesildi), tekrar baglaninca
        // logcat'ten okunup eklenmeli.
    )
}
