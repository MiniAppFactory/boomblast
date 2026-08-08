package com.example.ads

import com.example.BuildConfig

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

    fun bannerAdUnitId(): String = if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else PRODUCTION_BANNER_AD_UNIT_ID
    fun rewardedAdUnitId(): String = if (BuildConfig.DEBUG) TEST_REWARDED_AD_UNIT_ID else PRODUCTION_REWARDED_AD_UNIT_ID
}
