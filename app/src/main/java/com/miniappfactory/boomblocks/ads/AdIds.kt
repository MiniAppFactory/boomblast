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
    // Faz 100: Galaxy S8 listeden CIKARILDI — artik bir gelistirici cihazi
    // degil, gercek bir testcinin kendi hesabiyla closed testing'e katilacagi
    // normal bir cihaz. ID'si listede kaldigi surece o cihaza release build'de
    // bile her zaman test reklami gosterilirdi; testci gercek reklam akisini
    // goremezdi.
    //
    // Faz 102: S22 Ultra (proje sahibinin GUNLUK KULLANDIGI ve oyunu gercekten
    // OYNADIGI telefon) EKLENDI — Faz 38'den beri eklenmesi planlaniyordu ama
    // ID'si alinamamisti, bu yuzden release build'de bugune kadar hep GERCEK
    // reklam gosteriyordu. Sahibin kendi oyununda rewarded reklam izleyip odul
    // almasi AdMob'un "self-serving traffic / invalid traffic" politikasi
    // kapsamina girer (tipik sonuc: hesabin "ad serving limited" durumuna
    // dusmesi). Artik bu cihaza her zaman Google'in guvenli test/house
    // reklamlari gosteriliyor, gercek kullanicilar etkilenmiyor.
    //
    // Yeni bir gelistirici cihazi eklenecekse ID'si buraya yazilir: o cihazda
    // ilk reklam istegi yapildiginda logcat'e "Ads" etiketiyle otomatik
    // yazdirilir ("Use RequestConfiguration.Builder().setTestDeviceIds(
    // Arrays.asList(\"XXXX\")) to get test ads on this device.").
    //
    // DIKKAT: bu kimlik UYGULAMA BAZLIDIR, cihaz bazli DEGIL. Ayni S22
    // Ultra'da olculdu (2026-08-13): Kaboom "F83812BB...", Kron Drive
    // "B5BD61FF..." bildirdi. Sebebi buyuk olasilikla cihazda reklam
    // kimliginin silinmis/sifirlanmis olmasi — bu durumda SDK uygulama basina
    // ayri bir deger uretiyor. Yani her uygulamanin kendi listesine, O
    // UYGULAMADAN okunan kimlik yazilmali; baska bir uygulamadan alinan
    // kimlik hicbir ise yaramaz. Kimlik uygulama yeniden baslatildiginda
    // degismiyor (dogrulandi).
    val developerTestDeviceIds = listOf(
        // Samsung Galaxy S22 Ultra (SM-S908E) — proje sahibinin gunluk telefonu.
        // Kaboom'dan okundu; Kron Drive icin AYRI kimlik gerekir (B5BD61FF...).
        "F83812BBF9B13BF4F83079B72057D5AC"
    )
}
