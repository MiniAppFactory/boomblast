package com.miniappfactory.boomblocks.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density

// ---------------------------------------------------------------------------
// Faz 160 — OTOMATIK SIGDIRMA (auto shrink)
// ---------------------------------------------------------------------------
//
// CIHAZDA YAKALANAN HATA: Loadout ekraninda icerik kullanilabilir yuksekligi
// BIRKAC PIKSEL asiyordu. Sonuc orantisiz bicimde cirkin: kaydirma devreye
// giriyor, kaydirma ipucu chevron'u "REKLAM IZLE" butonunun UZERINE biniyor
// ve butonu kismen kapatiyordu.
//
// NEDEN ELLE BOSLUK KISMAK YANLIS: S8'de 12dp kirpmak yalnizca S8'i cozer.
// Baska bir ekran orani, gezinme cubugu olan bir cihaz, daha buyuk sistem
// yazi tipi ya da daha uzun bir ceviri geldigi anda tasma geri gelir. Bu
// sarmalayici sorunu SINIFI olarak cozuyor: icerik kullanilabilir yukseklige
// KENDINI sigdiriyor.
//
// CALISMA SEKLI (SubcomposeLayout, iki gecis):
//   1. OLCUM gecisi — icerik dogal boyutunda, SINIRSIZ yukseklikle olculur.
//   2. Gereken yukseklik kullanilabilirden buyukse kucultme carpani
//      hesaplanir (kullanilabilir / gereken) ve icerik, `LocalDensity`
//      olceklenerek YENIDEN yerlestirilir. Density'yi olceklemek dp ve sp'yi
//      birlikte kucultur; yani bosluklar, dolgular, ikonlar ve punto
//      ORANTILI kuculur — tek tek deger elle degistirilmez.
//
// GUVENLIK PAYLARI (bunlar olmadan ozellik zararlidir):
//
//   a) ALT SINIR (`minScale`, varsayilan 0.85). Sonsuz kucultme yok. Alt
//      sinirda hala sigmiyorsa `fits = false` doner ve cagiran taraf
//      KAYDIRMAYA geri doner. Yani ozellik "sigdiramazsa" duruma zarar
//      vermez, eski davranisa duser.
//
//   b) DOKUNMA HEDEFLERI KUCULMEZ. Bu, alt sinirdan daha baglayici bir
//      kisit: 48dp'lik bir buton %85'e inseydi 40.8dp olurdu ve dokunma
//      hedefi kuralini cignerdi. Cozum `LocalFitScale`: olceklenmis density
//      icinde `GameButton` kendi `minHeight`ini carpana BOLER, boylece
//      FIZIKSEL yuksekligi sabit kalir. Kisacasi kuculen sey bosluk,
//      dolgu ve punto; TIKLANAN ALAN degil.
//
//   c) ERISILEBILIRLIK EZILMEZ. Kullanici sistemde buyuk yazi sectiyse
//      sigdirma ugruna bunu sifirlamak yanlis olurdu. Metin, duzenin
//      YARISI kadar kuculur: duzen %85'e inerken metin %92.5'te kalir
//      (`textScale = (1 + scale) / 2`). fontScale DUSURULUR ama
//      SIFIRLANMAZ — kullanicinin 1.3'luk ayari 1.20 olarak yasar.
//
// Bu turda YALNIZCA Loadout'ta kullaniliyor. Once burada cihazda dogru
// calistigi gorulsun, diger ekranlara sonra yayilsin.

/**
 * Aktif sigdirma carpani. 1f = kucultme yok.
 *
 * `GameButton` bunu okuyup dokunma hedefini geri buyutur (yukaridaki (b)
 * maddesi). `FitToHeight` disinda her zaman 1f oldugu icin diger ekranlarda
 * hicbir etkisi yoktur.
 */
val LocalFitScale = compositionLocalOf { 1f }

private enum class FitSlot { Probe, ScaledProbe, MinProbe, Content }

// Sigan en buyuk olcegi bulmak icin ikili arama adimi. 4 adim, [0.85, 1.0]
// araliginda ~0.01 cozunurluk demek -- gozle ayirt edilemeyecek kadar ince,
// ama her adim bir olcum gecisi oldugu icin daha fazlasi bosuna maliyet.
private const val SCALE_SEARCH_STEPS = 4

/**
 * Icerigi kullanilabilir yuksekliğe sigdirir; sigdiramazsa kaydirmaya birakir.
 *
 * @param minScale kucultmenin alt siniri. Bu sinirda da sigmazsa `fits = false`.
 * @param content `fits` parametresi ile cagrilir: `true` ise icerik TAM sigdi
 *   (kaydirma ve chevron GEREKSIZ, hic cizilmemeli), `false` ise cagiran
 *   taraf kaydirilabilir bir duzen kurmalidir.
 */
@Composable
fun FitToHeight(
    modifier: Modifier = Modifier,
    minScale: Float = 0.85f,
    content: @Composable (fits: Boolean) -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val available = constraints.maxHeight

        // Yukseklik sinirsizsa sigdirilacak bir sey yok: dogal boyut.
        if (!constraints.hasBoundedHeight) {
            val placeables = subcompose(FitSlot.Content) { content(true) }
                .map { it.measure(constraints) }
            val w = placeables.maxOfOrNull { it.width } ?: 0
            val h = placeables.maxOfOrNull { it.height } ?: 0
            return@SubcomposeLayout layout(w, h) {
                placeables.forEach { it.place(0, 0) }
            }
        }

        // --- 1. GECIS: dogal yukseklik ---
        // Kaydirmasiz varyant (fits = true) sinirsiz yukseklikle olculur;
        // boylece icerigin GERCEKTEN istedigi yukseklik ogrenilir.
        val probe = subcompose(FitSlot.Probe) { content(true) }
            .map {
                it.measure(
                    Constraints(
                        minWidth = 0,
                        maxWidth = constraints.maxWidth,
                        minHeight = 0,
                        maxHeight = Constraints.Infinity
                    )
                )
            }
        val natural = probe.maxOfOrNull { it.height } ?: 0

        // --- 2. GECIS: gerekiyorsa olcekle ---
        val rawScale = if (natural <= available || natural == 0) {
            1f
        } else {
            available.toFloat() / natural.toFloat()
        }
        val scale = rawScale.coerceIn(minScale, 1f)

        // Faz 166 — TAHMIN YERINE OLCUM (ve Faz 166e ile duzeltilmis hali).
        //
        // SORUN: burada `fits = natural * scale <= available` yaziyordu, yani
        // sigip sigmadigi TAHMIN ediliyordu ve tahmin sistematik olarak
        // iyimserdi:
        //
        //   duzen `scale` kadar kuculuyor,
        //   metin ise `textScale = (1 + scale) / 2` kadar (erisilebilirlik payi),
        //   ve textScale > scale.
        //
        // Yani metin duzenden DAHA AZ kuculuyor; yuksekligi cogunlukla metin
        // belirleyen bir icerikte gercek yukseklik `natural * scale`i asiyor.
        //
        // ILK DENEME YANLISTI: "olc, sigmiyorsa kaydirmaya dus" yapmistim.
        // Kullanici bunu aninda yakaladi -- Loadout'ta uc kart da sigarken
        // ekran kaydirmaya dusuyor ve "REKLAM IZLE" chevron'un altinda
        // kaliyordu ("burasi bozulmus, 3'u de gozukuyordu, simdi reklam izle
        // icin scroll gerekiyor"). Tasma ~birkac pikseldi; ona karsilik
        // kaydirma + 34dp chevron seridi acmak kuru bir zarardi.
        //
        // DOGRUSU: kaydirmaya dusmek degil, OLCULEN tasma kadar biraz daha
        // KUCULTMEK. Tahminin iyimserligi zaten oranseldi, o yuzden tek bir
        // duzeltme adimi yetiyor: yeni olcek = olcek * (available / olculen).
        // Kaydirma yalnizca `minScale` alt sinirinda BILE sigmiyorsa devreye
        // giriyor -- dosyanin bastan beri vaat ettigi davranis.
        fun densityFor(sc: Float) = Density(
            density = density * sc,
            fontScale = fontScale * (((1f + sc) / 2f) / sc)
        )

        // Verilen olcekte icerigin GERCEK yuksekligi. `slotId` her cagriya ayri
        // bir subcompose yuvasi verir; Compose yuvalari anahtara gore yeniden
        // kullandigi icin olcum gecisleri arasinda maliyet amorti oluyor.
        fun measureAt(slotId: Any, sc: Float): Int =
            subcompose(slotId) {
                CompositionLocalProvider(
                    LocalDensity provides densityFor(sc),
                    LocalFitScale provides sc
                ) {
                    content(true)
                }
            }.map {
                it.measure(
                    Constraints(
                        minWidth = 0,
                        maxWidth = constraints.maxWidth,
                        minHeight = 0,
                        maxHeight = Constraints.Infinity
                    )
                )
            }.maxOfOrNull { it.height } ?: 0

        var effectiveScale = scale
        var fits = natural <= available

        if (scale < 1f && available > 0) {
            // NEDEN TEK BIR DUZELTME ADIMI YETMIYOR: metin `(1 + s) / 2` kadar
            // kuculuyor, yani olcek 0.85'e inse bile metin ancak 0.925'e
            // iniyor. Yukseklik metin agirlikliysa "olculen tasma kadar
            // kucult" formulu hedefe yakinsiyor ama USTTEN, ve birkac adimda
            // bile esigi gecmeyebiliyor. Bu yuzden dogrudan ARANIYOR.
            if (measureAt(FitSlot.ScaledProbe, scale) > available) {
                if (measureAt(FitSlot.MinProbe, minScale) > available) {
                    // Alt sinirda BILE sigmiyor -> eski davranis: kaydirma.
                    effectiveScale = minScale
                    fits = false
                } else {
                    // `lo` her zaman SIGAN, `hi` her zaman SIGMAYAN olcek.
                    // Amac sigan olceklerin EN BUYUGU: metin olabildigince
                    // buyuk kalsin, kaydirma da acilmasin.
                    var lo = minScale
                    var hi = scale
                    repeat(SCALE_SEARCH_STEPS) { step ->
                        val mid = (lo + hi) / 2f
                        if (measureAt(step, mid) <= available) lo = mid else hi = mid
                    }
                    effectiveScale = lo
                    fits = true
                }
            } else {
                fits = true
            }
        }

        val placeables = if (effectiveScale >= 1f) {
            subcompose(FitSlot.Content) { content(fits) }
                .map { it.measure(constraints) }
        } else {
            // Olcumle cizim AYNI yogunlugu kullaniyor — ikisinin ayrisabilmesi
            // zaten yukaridaki hatanin kaynagiydi.
            subcompose(FitSlot.Content) {
                CompositionLocalProvider(
                    LocalDensity provides densityFor(effectiveScale),
                    LocalFitScale provides effectiveScale
                ) {
                    content(fits)
                }
            }.map { it.measure(constraints) }
        }

        layout(constraints.maxWidth, available) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}
