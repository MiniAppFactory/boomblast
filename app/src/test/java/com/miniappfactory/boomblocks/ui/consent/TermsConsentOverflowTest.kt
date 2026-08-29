package com.miniappfactory.boomblocks.ui.consent

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import com.miniappfactory.boomblocks.data.AppLanguage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Faz 160 — RIZA EKRANI GOVDE METNI, 5 DILDE SATIR SAYISI.
 *
 * NEDEN BU TEST VAR: Faz 159'da FR govde metni %26 kisaltildi (121 -> 89
 * karakter) ama iddia CIHAZDA DOGRULANAMADI. Sebep yapisal: bu ekran dil
 * seciminden ONCE render ediliyor, yani uygulama icinden Fransizcaya gecip
 * bakmak MUMKUN DEGIL — cihazin sistem dilini degistirmek gerekiyor. O yuzden
 * "iki satira indi" kimse tarafindan gorulmemisti.
 *
 * Bu test o bosluğu kapatiyor: gercek `TermsAcceptScreen` render edilip
 * govde metninin GERCEK satir sayisi olculuyor. Metinler test icinde
 * KOPYALANMIYOR — ekranin kendisi render edildigi icin string'ler
 * kaynaktan geliyor ve ceviri degisirse test onu yakalar (kopyalasaydik
 * test kendi kopyasini dogrular, gercegi degil).
 *
 * En kotu senaryo:
 *   - 360dp genislik (S8 / 1080px @ density 480 — kullanicinin cihazi),
 *   - govdeye kalan genislik: 360 - 2*24 (kart kenar boslugu)
 *     - 2*28 (kart ici dolgu) = 256dp,
 *   - fontScale 1.0 ve 1.3 (kullanicinin buyuttugu yazi tipi).
 *
 * !!! BU TESTIN SINIRI — CIHAZ YERINE GECMEZ !!!
 *
 * Robolectric uygulamanin kendi yazi tipini (`AppFontFamily`) degil bir
 * IKAME yazi tipi kullanir ve o ikame DAHA DAR. Olculen deger cihazdakinden
 * sistematik olarak KUCUK cikar: bu metin burada 2 satir raporlanirken
 * cihazda (SM-G950F, 1080x2220) 3 SATIR render ediliyor — ekran goruntusuyle
 * dogrulandi.
 *
 * Bu yuzden sinirlar cihaz gercegine gore gevsek secildi (3 / 4). Testin isi
 * "cihazda kac satir" sertifikasi vermek DEGIL; metin ya da duzen ileride
 * BUYURSE bunu yakalamak. Kesin satir sayisi yalnizca cihazda gorulerek
 * dogrulanir — Faz 159'un "iki satira indi" hatasi tam olarak bu ayrimi
 * atladigi icin olustu.
 *
 * NOT: `GraphicsMode.NATIVE` sart. Varsayilan LEGACY modda sahte glyph
 * genislikleri yuzunden metin HIC sarmalanmaz ve test her dilde "1 satir"
 * raporlayarak YANLIS YERE yesil yanar (bu tuzaga bir kez dusuldu).
 *
 * HUKUKI SINIR: bu test yalnizca SATIR SAYISINI olcer. Metnin icerigi
 * kisaltilirken korunmasi gereken uc unsur (gizlilik politikasi + kullanim
 * sartlarina ACIK atif, calisan BAGLANTI, acik KABUL eylemi) ayri
 * testlerin/incelemenin konusu — satir sayisi ugruna onlar atilamaz.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h740dp-mdpi")
// GERCEK yazi tipi metrikleri sart: varsayilan (LEGACY) grafik modunda
// Robolectric sahte glyph genislikleri kullanir ve metin HIC sarmalanmaz —
// test her dilde "1 satir" raporlayip YANLIS YERE yesil yanar.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TermsConsentOverflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun lineCountOf(tag: String): Int {
        val node = composeRule.onNodeWithTag(tag).fetchSemanticsNode()
        val results = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        assertTrue("$tag icin metin duzeni okunamadi", results.isNotEmpty())
        return results.first().lineCount
    }

    private fun assertBodyFits(maxLines: Int, fontScale: Float) {
        val languageState = mutableStateOf(AppLanguage.TR)
        // Sonsuz animasyon (`WanderingPiecesBackground`) saatin kendiliginden
        // ilerlemesiyle `waitForIdle`i asla bitirmezdi.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val language by languageState
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = fontScale)
            ) {
                TermsAcceptScreen(
                    language = language,
                    darkMode = true,
                    onAccept = {}
                )
            }
        }

        AppLanguage.entries.forEach { lang ->
            languageState.value = lang
            composeRule.mainClock.advanceTimeByFrame()
            val lines = lineCountOf("terms_body")
            assertTrue(
                "[$lang / fontScale=$fontScale] riza govde metni $lines satir — " +
                    "en fazla $maxLines olmali (360dp ekranda govdeye 256dp kaliyor)",
                lines <= maxLines
            )
            println("RIZA GOVDESI  $lang  fontScale=$fontScale  ->  $lines satir")
        }
    }

    @Test
    fun `consent body stays bounded in every language`() {
        assertBodyFits(maxLines = 3, fontScale = 1f)
    }

    /**
     * Kullanici sistem yazi tipini buyuttugunde metin uzar. Burada sinir
     * 3 satir: amac "asla buyumesin" degil, KONTROLSUZ buyumesin — kart
     * yuksekligi ve KABUL ET butonunun gorunurlugu bu sinirla korunuyor.
     */
    @Test
    fun `consent body stays bounded with enlarged system font`() {
        assertBodyFits(maxLines = 4, fontScale = 1.3f)
    }
}
