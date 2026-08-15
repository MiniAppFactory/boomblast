package com.miniappfactory.boomblocks.ads

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Faz 109: TEK banner AdView — surec omru boyunca bir tane, ekranlar arasi
// TASINIR.
//
// OLCUM (S22 Ultra, release, 2026-08-15):
//
//   * Soguk acilis, HIC dokunmadan, 9 saniye: 6 kare cizildi, biri 200 ms
//     biri 65 ms. GPU histogrami en fazla 5 ms -> sicrama %100 UI thread.
//   * Bir oyun oturumundan sonra `dumpsys meminfo`: **WebViews: 7**.
//     Bu uygulamada WebView'in TEK kaynagi AdMob AdView'idir. Yani yedi
//     AdView yaratilmis, hicbiri yok edilmemisti.
//   * `AdView.destroy()` / `pause()` / `resume()` kod tabaninda HIC
//     cagrilmiyordu; buna karsilik 14 ayri `BannerAdView()` yerlesim
//     noktasi vardi.
//   * Logcat, tek bir soguk aciliste: 17:18:38.910 -> 17:19:38.910, tam
//     60.000 ms arayla ayni banner istegi. Yani her AdView 60 saniyede bir
//     KENDILIGINDEN yenileniyor. Yedi sizmis AdView = ekranda gorunmeyen
//     yedi paralel yenileme dongusu (ana thread + ag + pil), ve gorunmeyen
//     yerde harcanan yedi kat reklam istegi.
//
// Kok neden: eski `BannerAdView` her ekran girisinde `AndroidView`'in
// `factory`'sinde YENI bir `AdView` kuruyordu. `factory` kompozisyon
// sirasinda, UI thread'inde, KARENIN ICINDE calisir. Ana menu -> harita ->
// loadout -> oyun yolu dort ayri AdView demekti; sonuncusu tam 64 hucrelik
// izgaranin ilk kompozisyonuyla ayni kareye dusuyordu. Kullanicinin
// "level'e ilk girince hafif yavaslama" dedigi sey budur.
//
// Cozum: AdView bir kez yaratilir (isinma sirasinda, hicbir sey animasyon
// yapmazken), sonra her ekranda yalnizca EBEVEYN DEGISTIRIR. Ekran gecisi
// artik yeni WebView, yeni `loadAd()` ve yeni sinif yuklemesi tetiklemez.
//
// Reklam geliri: azalmaz, artar. Bugun her ekran sifirdan istek yapiyor ve
// olculmus NO_FILL orani yuzunden cogu bos donuyor; havuzdaki AdView zaten
// DOLU gelir ve aninda gorunur. Ustelik gorunmeyen sizmis AdView'lerin
// urettigi "viewable olmayan" gosterimler ortadan kalkar.
object BannerAdPool {

    /**
     * Ekran yerlestikten sonra banner'in baglanmasi icin beklenen sure.
     *
     * Kullanicinin acik onceligi: *"gerekirse oyun acilirken ceyrek saniye
     * gec gelsin ama gelince takilma hissi olmamali."* Banner'in yuksekligi
     * zaten Faz 108'de ONCEDEN rezerve edildigi icin bu gecikme HICBIR duzen
     * kaymasi yaratmaz — bos ve dogru boyutlu alan bastan oradadir, icine
     * reklam biraz sonra oturur.
     *
     * Neden 250 degil de 400: Compose Navigation'in varsayilan destinasyon
     * gecisi ~700 ms surer ve bu sirada IKI ekran birden kompoze olur. 250 ms
     * banner'i gecisin TAM ORTASINA baglardi. 400 ms, addView + olcum/yerlesim
     * isini gecis yatistiktan sonraya birakir. Reklam geliri etkilenmez:
     * gorunurluk (viewability) esigi saniyeler mertebesindedir, banner ise
     * ekran boyunca ayakta kalir.
     */
    const val ATTACH_DELAY_MS = 400L

    private var adView: AdView? = null
    private var adSizeKey: String? = null
    private var owner: FrameLayout? = null
    private var resumed = true

    private fun key(size: AdSize) = "${size.width}x${size.height}"

    /**
     * AdView'i (ve dolayisiyla surecin ILK WebView'ini) sakin bir anda kurar.
     *
     * MainActivity ilk kare cizildikten SONRA cagirir. Boylece WebView
     * saglayicisinin yuklenmesi — olculen en pahali tek is — ne acilis
     * karesine ne de bir seviye girisine denk gelir.
     */
    @Synchronized
    fun warmUp(context: Context, size: AdSize) {
        ensure(context, size)
    }

    @Synchronized
    private fun ensure(context: Context, size: AdSize): AdView? {
        // Riza kapisi: AdsConsent tek kaynak (bkz. AdsConsent). Riza yoksa
        // hicbir AdView kurulmaz — ekran yine acilir, sadece banner bos kalir.
        if (!AdsConsent.canRequestAds) return null

        val wanted = key(size)
        adView?.let { existing ->
            if (adSizeKey == wanted) return existing
            // Boyut degistiyse (farkli genislik) eskisi artik gecersiz.
            detachFromParent(existing)
            existing.destroy()
            adView = null
            owner = null
        }

        val created = AdView(context).apply {
            setAdSize(size)
            adUnitId = AdIds.bannerAdUnitId()
            loadAd(AdRequest.Builder().build())
        }
        adView = created
        adSizeKey = wanted
        return created
    }

    /**
     * Banner'i [container]'a tasir. Ekran gecisi sirasinda kisa bir sure iki
     * ekran birden kompoze olur; bu durumda banner'i SON isteyen alir, cunku
     * o kullanicinin gordugu ekrandir. Ikinci bir AdView yaratilmaz.
     */
    @Synchronized
    fun attach(container: FrameLayout, context: Context, size: AdSize) {
        val view = ensure(context, size) ?: return
        if (view.parent === container) return
        detachFromParent(view)
        container.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )
        owner = container
        if (resumed) view.resume()
    }

    /**
     * Composable ekrandan cikarken cagrilir. AdView YOK EDILMEZ — bir sonraki
     * ekran onu dolu haliyle devralsin diye yalnizca ebeveyninden ayrilir.
     */
    @Synchronized
    fun release(container: FrameLayout) {
        // Banner'i bu arada baska bir ekran devraldiysa dokunma.
        if (owner !== container) return
        adView?.let { detachFromParent(it) }
        owner = null
    }

    /** Activity onPause: gorunmeyen banner ag/ana-thread isi uretmesin. */
    @Synchronized
    fun pause() {
        resumed = false
        adView?.pause()
    }

    /** Activity onResume. */
    @Synchronized
    fun resume() {
        resumed = true
        if (owner != null) adView?.resume()
    }

    /**
     * Activity onDestroy. Olculen WebView sizintisinin kapandigi yer —
     * bugune kadar bu cagri kod tabaninda HIC yoktu.
     */
    @Synchronized
    fun destroy() {
        adView?.let {
            detachFromParent(it)
            it.destroy()
        }
        adView = null
        adSizeKey = null
        owner = null
    }

    private fun detachFromParent(view: AdView) {
        (view.parent as? ViewGroup)?.removeView(view)
    }
}
