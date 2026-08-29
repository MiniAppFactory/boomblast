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

private enum class FitSlot { Probe, Content }

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
        // Alt sinirda bile sigmiyorsa kaydirmaya geri donulur.
        val fits = natural * scale <= available + 0.5f

        val placeables = if (scale >= 1f) {
            subcompose(FitSlot.Content) { content(fits) }
                .map { it.measure(constraints) }
        } else {
            // Metin duzenin yarisi kadar kuculur (erisilebilirlik payi).
            val textScale = (1f + scale) / 2f
            val baseDensity = density
            val baseFontScale = fontScale
            subcompose(FitSlot.Content) {
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = baseDensity * scale,
                        // sp = density * fontScale oldugu icin, density zaten
                        // `scale` kadar kucuktu; fontScale'i telafi carpaniyla
                        // buyuterek metnin yalnizca `textScale` kadar
                        // kuculmesini sagliyoruz.
                        fontScale = baseFontScale * (textScale / scale)
                    ),
                    LocalFitScale provides scale
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
