package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * FAZ 170 — SONUC DIYALOGU METINLERI HICBIR DILDE VE HICBIR GENISLIKTE
 * KIRPILMAMALI.
 *
 * NEDEN VAR: bu hatayi kullanici cihazda buldu, IKI KEZ.
 *
 *   "bu olmamis, sigmiyor SEVIYE TAMAMLANDI yazisi"
 *   "reklam izle devam et yazisi sigmamis burada da... APK yapip bana test
 *    yaptirmak yerine APK yapmadan once render et, ben senin testcin degilim"
 *
 * Hakliydi, ve ikisinde de ayni koke basmistim: PUNTOYU TAHMIN ETMEK.
 *   - Baslik SABIT 38sp'ydi; yorumumda "GameEmblemLine kendi kuculuyor"
 *     yaziyordu ama yanlisti -- `emblemFontScale` yalnizca `GameEmblem`de
 *     cagriliyor, `GameEmblemLine`da degil.
 *   - Buton etiketi KARAKTER SAYISINA gore seciliyordu
 *     (`length <= 18 -> 14.sp`). Faz 169'da sol ikonu ekleyince metne kalan
 *     genislik 38dp daraldi; karakter sayisi degismedigi icin formul bunu
 *     goremezdi.
 *
 * Kendi render'imi TEK bir genislikte (411dp) aldigim icin ikisini de
 * kacirdim. Bu test o bosluğu kapatiyor: dar genisliklerde ve bes dilde metin
 * gercekten olculuyor. Tasma olursa BUILD DUSER -- kullanici degil test bulur.
 *
 * Genislikler diyalog KARTININ IC genisligi (ekranin degil): kart ekranin
 * %85'i, eksi 16dp dis + 20dp ic dolgu. Dar bir telefonda bu ~200dp'ye iniyor,
 * o yuzden en zor senaryo bilerek 190dp.
 *
 * NOT: `rule.setContent` bir kural basina YALNIZCA BIR KEZ cagrilabilir. Ilk
 * yazdigimda dongunun ICINDE cagirmistim; iki test de "tasma var" gibi gorunup
 * dusuyordu, oysa hata testin kendisindeydi. Tum kombinasyonlar artik TEK
 * bestede, her biri kendi sabit genisligindeki sutunda.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
// Yazi olcumu GERCEK olsun diye native grafik: varsayilan (legacy) modda
// Robolectric font metrikleri sahte ve hasVisualOverflow her yerde true
// donuyordu -- 300dp genislikte "RETRY" bile tasiyor gorunuyordu.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResultDialogOverflowTest {

    @get:Rule
    val rule = createComposeRule()

    private val widths = listOf(190.dp, 210.dp, 240.dp, 300.dp)

    private fun buttonLabels(l: AppLanguage) = listOf(
        "watch_ad" to l.pick(
            tr = "REKLAM İZLE, DEVAM ET", en = "WATCH AD, CONTINUE",
            it = "GUARDA ANNUNCIO, CONTINUA", fr = "REGARDER PUB, CONTINUER",
            es = "VER ANUNCIO, CONTINUAR"
        ),
        "retry" to l.pick(
            tr = "TEKRAR DENE", en = "RETRY", it = "RIPROVA",
            fr = "RÉESSAYER", es = "REINTENTAR"
        ),
        "restart" to l.pick(
            tr = "YENİDEN BAŞLAT", en = "RESTART", it = "RICOMINCIA",
            fr = "RECOMMENCER", es = "REINICIAR"
        ),
        "back_to_map" to l.pick(
            tr = "HARİTAYA DÖN", en = "BACK TO MAP", it = "TORNA ALLA MAPPA",
            fr = "RETOUR À LA CARTE", es = "VOLVER AL MAPA"
        ),
        "continue" to l.pick(
            tr = "DEVAM ET", en = "CONTINUE", it = "CONTINUA",
            fr = "CONTINUER", es = "CONTINUAR"
        )
    )

    @Test
    fun `buton etiketleri hicbir dilde ve genislikte kirpilmaz`() {
        val overflowed = mutableSetOf<String>()

        rule.setContent {
            // Kaydirma sarmali SART: yuzlerce vaka tek sutuna dizilince ekran
            // yuksekligi tukeniyor, kalan cocuklara maxHeight = 0 geliyor ve
            // metinler GENISLIKTEN degil YUKSEKLIKTEN tasiyor. Ilk kosumda
            // 300dp genislikte "RETRY" bile kirpiliyor gorundu; tek vaka
            // izole edilince tasma yoktu. Kaydirma cocuklara sinirsiz yukseklik
            // verir, yani olcum yalnizca GENISLIGI sinar.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (language in AppLanguage.entries) {
                    for (width in widths) {
                        for ((key, label) in buttonLabels(language)) {
                            val id = "$language / ${width.value.toInt()}dp / $key: $label"
                            Column(Modifier.width(width)) {
                                ResultActionButton(
                                    text = label,
                                    onClick = {},
                                    colors = resultButtonColors(ResultPrimaryBlue, ResultOnPrimary),
                                    // En kotu durum bilerek seciliyor: IKONLU buton.
                                    // Ikon 28dp + 10dp aralik goturur, yani metne
                                    // kalan en dar alan.
                                    iconRes = R.drawable.kb_btn_watchad,
                                    modifier = Modifier.fillMaxWidth(),
                                    onTextLayout = { if (it.hasVisualOverflow) overflowed += id }
                                )
                            }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        assertTrue(
            "Su etiketler kirpiliyor:\n" + overflowed.sorted().joinToString("\n"),
            overflowed.isEmpty()
        )
    }

    /**
     * Baslik iki satirli; ikisi de kirpilmamali.
     *
     * `GameEmblemLine` kendi `onTextLayout`unu disari vermiyor, o yuzden
     * `ResultDialogTitle`in SECTIGI puntoya bakiliyor: taban degere (15sp)
     * DAYANMISSA metin sigmamis demektir, cunku arama yalnizca sigmadigi
     * surece kucultuyor.
     */
    @Test
    fun `baslik hicbir dilde tabana dayanmaz`() {
        val failures = mutableSetOf<String>()

        rule.setContent {
            // Kaydirma sarmali SART: yuzlerce vaka tek sutuna dizilince ekran
            // yuksekligi tukeniyor, kalan cocuklara maxHeight = 0 geliyor ve
            // metinler GENISLIKTEN degil YUKSEKLIKTEN tasiyor. Ilk kosumda
            // 300dp genislikte "RETRY" bile kirpiliyor gorundu; tek vaka
            // izole edilince tasma yoktu. Kaydirma cocuklara sinirsiz yukseklik
            // verir, yani olcum yalnizca GENISLIGI sinar.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (language in AppLanguage.entries) {
                    val top = language.pick(
                        tr = "SEVİYE", en = "LEVEL", it = "LIVELLO", fr = "NIVEAU", es = "NIVEL"
                    )
                    val bottoms = listOf(
                        language.pick(tr = "TAMAMLANDI!", en = "COMPLETE!", it = "COMPLETATO!", fr = "TERMINÉ!", es = "COMPLETADO!"),
                        language.pick(tr = "BAŞARISIZ", en = "FAILED", it = "FALLITO", fr = "ÉCHOUÉ", es = "FALLIDO"),
                        language.pick(tr = "BİTTİ", en = "OVER", it = "FINITO", fr = "TERMINÉE", es = "TERMINADO")
                    )
                    for (bottom in bottoms) {
                        for (width in widths) {
                            val id = "${width.value.toInt()}dp / $bottom"
                            Column(Modifier.width(width)) {
                                ResultDialogTitle(
                                    topLine = top,
                                    bottomLine = bottom,
                                    colors = resultEmblemColors(ResultFailAccent),
                                    onBottomSizeChosen = { if (it <= 15.01f) failures += id }
                                )
                            }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()

        assertTrue(
            "Baslik tabana dayandi (kirpilma riski):\n" + failures.sorted().joinToString("\n"),
            failures.isEmpty()
        )
    }
}
