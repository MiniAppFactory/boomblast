package com.miniappfactory.boomblocks.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

// Faz 39: RewardedAdManager ile ayni fire-and-forget desen. Odul yok — bolumler
// arasi zorunlu gecis reklami, kullanici sadece kapatinca (izleyip izlememesi
// fark etmeksizin) onProceed cagrilir; reklam yuklenemezse (no-fill/ag hatasi)
// oyun akisi ASLA bloklanmaz, onProceed yine de cagrilir.
object InterstitialAdManager {

    // Faz 108: yeniden-giris korumasi.
    //
    // Sorun: load() ile show() arasinda 1-3 saniye sessizlik var — hicbir
    // spinner yok. Oyuncunun bu sirada butona TEKRAR basmasi dogal bir tepki
    // (ayni desen Faz 43 ve 48'de rewarded tarafinda zaten kullanici
    // sikayeti olarak gelmisti). Her dokunus ayri bir load() basliyordu:
    // iki tam ekran reklam PES PESE aciliyor ve onProceed iki kez calisip
    // cift popBackStack / cift resetGame yapiyordu. Pes pese interstitial
    // dogrudan bir AdMob yerlesim politikasi ihlalidir.
    //
    // Cozum: ucusta bir gosterim varken gelen cagri SESSIZCE DUSURULUR —
    // onProceed cagrilmaz, cunku birinci cagrinin onProceed'i akisi zaten
    // surdurecek; ikisini de cagirmak cift navigasyon demek olurdu.
    @Volatile
    private var inFlightSince = 0L

    // Guvenlik supabi: SDK callback'i hic gelmezse (uretici hatasi, surec
    // olumu) bayrak sonsuza kadar takili kalir ve TUM ilerleme butonlari
    // sessizce olur — oyuncuyu kilitlememe ilkesinin tam tersi. Bu sure
    // sonunda bayrak bayat sayilir ve yeni cagri normal isler.
    private const val STALE_AFTER_MS = 15_000L

    // Faz 109: ON YUKLEME.
    //
    // Onceki desen "yukle-sonra-goster"di: oyuncu ilerleme butonuna basiyor,
    // SONRA `InterstitialAd.load()` basliyordu. Iki maliyeti vardi:
    //   1. 1-3 saniyelik sessiz bekleme (Faz 108'in cift-dokunma korumasi
    //      zaten bu bekleyisin yan etkisiydi),
    //   2. yuklemenin ilk anindaki sinif yukleme / WebView isi tam oyuncunun
    //      dokundugu kareye dusuyordu.
    //
    // Artik reklam ihtiyactan cok once hazirlanir; gosterme ani yalnizca
    // `show()` cagrisidir. Hazir degilse ESKI yol (yukle-sonra-goster) aynen
    // devrededir — yani reklam geliri hicbir kosulda AZALMAZ, sadece daha
    // sik ve daha hizli gosterilir.
    //
    // AdMob kurali: yuklenmis bir interstitial 1 saat sonra gecersiz sayilir.
    // Guvenli tarafta kalmak icin 50 dakika.
    private const val CACHE_TTL_MS = 50 * 60 * 1000L

    @Volatile
    private var cached: InterstitialAd? = null

    @Volatile
    private var cachedAt = 0L

    @Volatile
    private var loading = false

    // Faz 114: siklik siniri icin iki zaman damgasi.
    //
    // sessionStart: bu object ilk dokunuldugunda, yani pratikte surec
    // basladiginda sabitlenir. Oyuncu uygulamayi oldurup yeniden acarsa yeni bir
    // grace penceresi hak eder — bu kasitli: uzun bir aradan sonra donen oyuncu
    // yine "ilk izlenim" konumundadir.
    private val sessionStart = SystemClock.elapsedRealtime()

    // SADECE gercekten gosterilip kapatilan reklam icin guncellenir. No-fill ya
    // da "gosterilemedi" durumu cooldown'i YAKMAZ — yoksa bir dizi basarisiz
    // yukleme, gercek gosterimleri sessizce kilitlerdi.
    @Volatile
    private var lastShownAt = 0L

    private fun freshCached(): InterstitialAd? {
        val ad = cached ?: return null
        if (SystemClock.elapsedRealtime() - cachedAt > CACHE_TTL_MS) {
            cached = null
            return null
        }
        return ad
    }

    /**
     * Bir sonraki gecis reklamini onceden hazirlar. Idempotent: elde taze bir
     * reklam varken veya zaten bir yukleme surerken hicbir sey yapmaz.
     *
     * MainActivity ilk kareden sonra bir kez, ayrica her gosterimden sonra
     * otomatik olarak cagrilir.
     */
    fun preload(context: Context) {
        if (!AdsConsent.canRequestAds) return
        if (loading || freshCached() != null) return
        loading = true
        InterstitialAd.load(
            context.applicationContext,
            AdIds.interstitialAdUnitId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    cached = interstitialAd
                    cachedAt = SystemClock.elapsedRealtime()
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    // No-fill normaldir (bu projede olculmus 36x NO_FILL).
                    // Sessizce vazgecilir; bir sonraki preload() tekrar dener.
                    cached = null
                    loading = false
                }
            }
        )
    }

    fun loadAndShow(
        context: Context,
        activity: Activity,
        onProceed: () -> Unit
    ) {
        val now = SystemClock.elapsedRealtime()
        val busy = inFlightSince != 0L && (now - inFlightSince) < STALE_AFTER_MS
        if (busy) return

        // Faz 108: riza kapisi. canRequestAds() false iken (AB'de riza
        // alinmamis/geri cekilmis) reklam ISTENMEZ — ama oyuncu yine de
        // ilerler. Reklamsiz akis her zaman calisan akistir.
        if (!AdsConsent.canRequestAds) {
            onProceed()
            return
        }

        // Faz 114: siklik siniri. Sinir devredeyse reklam ISTENMEZ ve oyuncu
        // hicbir gecikme yasamadan devam eder. Reklamsiz akis her zaman calisan
        // akistir — burada da oyle.
        if (!InterstitialFrequencyPolicy.allow(now, lastShownAt, sessionStart)) {
            // Sinir yuzunden atlanan gosterim, bir sonrakini hazir tutmak icin
            // iyi bir firsat: preload idempotent.
            preload(context.applicationContext)
            onProceed()
            return
        }

        inFlightSince = now
        var finished = false
        val appContext = context.applicationContext
        // adWasShown: cooldown SADECE gercek bir tam ekran gosterimden sonra
        // baslar (bkz. lastShownAt aciklamasi).
        val finish = { adWasShown: Boolean ->
            if (!finished) {
                finished = true
                inFlightSince = 0L
                if (adWasShown) lastShownAt = SystemClock.elapsedRealtime()
                // Bir sonraki gosterim beklemesin.
                preload(appContext)
                onProceed()
            }
        }

        val attachAndShow = { ad: InterstitialAd ->
            ad.fullScreenContentCallback = object :
                com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() = finish(true)
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) = finish(false)
            }
            ad.show(activity)
        }

        // Faz 109: HIZLI YOL — reklam zaten elimizde, bekleme yok.
        val ready = freshCached()
        if (ready != null) {
            cached = null
            attachAndShow(ready)
            return
        }

        // YAVAS YOL — Faz 108'e kadarki davranisin AYNISI. Hazir reklam yoksa
        // gelir kaybetmemek icin yine de yuklenip gosterilir; sadece bu sefer
        // beklemek gerekir.
        InterstitialAd.load(
            appContext,
            AdIds.interstitialAdUnitId(),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    attachAndShow(interstitialAd)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    finish(false)
                }
            }
        )
    }
}
