package com.miniappfactory.boomblocks.ui.modeselect

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.miniappfactory.boomblocks.ui.components.screenBodyTextColor
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.gameSurfaces
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 161 — MOD SECIM EKRANININ OLCU VE OKUNURLUK GUVENCESI.
 *
 * Uc hata sinifi burada kilitleniyor:
 *
 *  1. OLU BOSLUK. Kartlar `aspectRatio(1f)` ile kareydi ve kenar SADECE
 *     genislikten cikiyordu; cihazda olculdu: alt basligin altinda ~270px,
 *     izgaranin altinda ~250px bos kaliyordu. Artik kart hucrenin tamamini
 *     kapliyor — ama TASMADAN.
 *
 *  2. SESSIZ KIRPILMA. "MODALITÀ COMFORT" tam puntoda kart genisligine
 *     sigmiyor ve `maxLines = 1` yuzunden ucu kesiliyordu.
 *
 *  3. ZEMINE GOMULEN METIN. Alt baslik `accentText` (camgobegi) idi; ekran
 *     zemini de ayni renk ailesinden turuyor. Kullanici: "gozukmuyor bu
 *     sekilde". Metin rolu artik notr ve ZEMINDEN turuyor — 6 skin x
 *     acik/koyu icin burada taraniyor.
 */
class ModeSelectLayoutTest {

    // WCAG bagil parlaklik kontrast orani.
    private fun contrast(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        val hi = maxOf(la, lb)
        val lo = minOf(la, lb)
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun allCases(): List<Triple<String, BlastSkin, Boolean>> = buildList {
        add(Triple("DEFAULT-dark", BlastSkin.DEFAULT, true))
        add(Triple("DEFAULT-light", BlastSkin.DEFAULT, false))
        BlastSkin.entries.filter { it != BlastSkin.DEFAULT }.forEach {
            add(Triple("${it.name}-dark", it, true))
            add(Triple("${it.name}-light", it, false))
        }
    }

    // -----------------------------------------------------------------------
    // 1. Izgara olculeri
    // -----------------------------------------------------------------------

    // Butun ekran sekilleri. Tablet YATAY gercek bir senaryo: Android 16
    // tabletlerinde manifestteki portre kilidi yok sayiliyor.
    private fun shapes() = listOf(
        "dar telefon" to (292.dp to 380.dp),
        "telefon" to (332.dp to 480.dp),
        "kare" to (600.dp to 600.dp),
        "tablet portre" to (740.dp to 900.dp),
        "tablet yatay" to (900.dp to 260.dp),
        "asiri kisa" to (900.dp to 120.dp)
    )

    @Test
    fun `cards keep the target aspect ratio`() {
        // docs/UI_TARGET.md 5.3: kart 390x480 = 0.8125. KARE DEGIL.
        shapes().forEach { (name, size) ->
            val (w, h) = size
            val m = modeGridMetrics(w, h)
            val ratio = m.cardWidth.value / m.cardHeight.value
            assertTrue(
                "$name: kart orani hedeften sapti ($ratio, hedef $MODE_CARD_ASPECT)",
                kotlin.math.abs(ratio - MODE_CARD_ASPECT) < 0.02f
            )
        }
    }

    @Test
    fun `grid never overflows its box on any shape`() {
        // BAGLAYICI KISIT: bu ekran kaydirilmaz. Izgara her sekilde kutusuna
        // sigmak ZORUNDA — alt sira kirpilirsa kart tiklanamaz hale gelir.
        shapes().forEach { (name, size) ->
            val (w, h) = size
            val m = modeGridMetrics(w, h)
            assertTrue(
                "$name: izgara YATAYDA tasti",
                (m.cardWidth * 2 + ModeGridColumnGap).value <= w.value + 0.01f
            )
            assertTrue(
                "$name: izgara DIKEYDE tasti — alt sira kirpilirdi",
                (m.cardHeight * 2 + ModeGridRowGap).value <= h.value + 0.01f
            )
            assertTrue("$name: kart genisligi sifir", m.cardWidth.value > 0f)
            assertTrue("$name: kart yuksekligi sifir", m.cardHeight.value > 0f)
        }
    }

    @Test
    fun `phone portrait cards grow well past the old square layout`() {
        // ESKI DAVRANIS: kart `aspectRatio(1f)` ile KARE ve kenar yalnizca
        // genislikten cikiyordu; dikeyde artan her sey bosluga gidiyordu.
        // Yeni kart en az o kareyi kapsamali, ustelik daha uzun olmali.
        val w = 332.dp
        val h = 480.dp
        val m = modeGridMetrics(w, h)
        val oldSquareSide = (w - ModeGridColumnGap).value / 2f

        assertTrue(
            "kart genisligi kucultuldu (${m.cardWidth.value} < $oldSquareSide)",
            m.cardWidth.value >= oldSquareSide - 0.01f
        )
        assertTrue(
            "kart hala kare — dikey alan kullanilmiyor (${m.cardHeight.value})",
            m.cardHeight.value > oldSquareSide * 1.15f
        )
    }

    @Test
    fun `short windows shrink the card instead of clipping it`() {
        // Tablet yatay: yukseklik yetmiyor. Kart ORANI KORUYARAK kuculur,
        // kirpilmaz.
        val m = modeGridMetrics(900.dp, 260.dp)
        assertTrue(
            "kisa pencerede kart yuksekligi hucreyi asti",
            (m.cardHeight * 2 + ModeGridRowGap).value <= 260.01f
        )
        assertTrue(
            "kisa pencerede kart genisligi orana gore kuculmedi",
            m.cardWidth.value < (900.dp - ModeGridColumnGap).value / 2f
        )
    }

    // -----------------------------------------------------------------------
    // 2. Uzun ceviriler
    // -----------------------------------------------------------------------

    // Kart genisligi 163dp (SM-G950F portre) - 18dp ic dolgu.
    private val cardTextWidthDp = 160f - 18f

    // Olcum vekili: Black agirlikli baslik fontunda bir harf ~ puntonun 0.62
    // kati. Gercek cagrida bu isi `TextMeasurer` yapar; burada aranan sey
    // ARAMA ALGORITMASININ dogrulugu.
    private fun widthOf(text: String, sp: Float) = text.length * 0.62f * sp

    private fun fittedSize(titles: List<String>, maxSp: Float = 26f): Float =
        fitFontSize(maxSp, MODE_TITLE_MIN_SP) { candidate ->
            titles.all { widthOf(it, candidate) <= cardTextWidthDp }
        }

    @Test
    fun `mode titles shrink for the longest translation instead of clipping`() {
        val sets = mapOf(
            "TR" to listOf("SONSUZ", "KARİYER", "PRO MOD", "KOLAY MOD"),
            "EN" to listOf("ENDLESS", "CAREER", "PRO MODE", "COMFORT MODE"),
            "IT" to listOf("INFINITA", "CARRIERA", "MODALITÀ PRO", "MODALITÀ COMFORT"),
            // FRANSIZCA: cihazda "MODE CONF..." diye kirpilan set. 12 karakter
            // oldugu icin karakter BUTCESI onu kucultmemisti — gercek olcum
            // kucultuyor.
            "FR" to listOf("INFINI", "CARRIÈRE", "MODE PRO", "MODE CONFORT"),
            "ES" to listOf("INFINITO", "CARRERA", "MODO PRO", "MODO CONFORT")
        )
        sets.forEach { (name, titles) ->
            val sp = fittedSize(titles)
            assertTrue("$name: punto alt sinirin altina dustu ($sp)", sp >= MODE_TITLE_MIN_SP)
            // ASIL SART: hicbir ad kart genisligini asmiyor, yani kirpilmiyor.
            titles.forEach { title ->
                assertTrue(
                    "$name: \"$title\" karta sigmiyor — kirpilirdi " +
                        "(${widthOf(title, sp)}dp / ${cardTextWidthDp}dp)",
                    widthOf(title, sp) <= cardTextWidthDp + 0.01f
                )
            }
        }
    }

    @Test
    fun `longer translations get a smaller shared size than short ones`() {
        val tr = fittedSize(listOf("SONSUZ", "KARİYER", "PRO MOD", "KOLAY MOD"))
        val it = fittedSize(listOf("INFINITA", "CARRIERA", "MODALITÀ PRO", "MODALITÀ COMFORT"))
        assertTrue("uzun ceviri kucultulmemis (TR=$tr IT=$it)", it < tr)
    }

    @Test
    fun `title size search returns the largest size that fits`() {
        // Bir sonraki punto (bulunanin 1sp ustu) SIGMAMALI: arama en buyugu
        // buluyor, gereksiz kucultmuyor.
        val titles = listOf("INFINITA", "CARRIERA", "MODALITÀ PRO", "MODALITÀ COMFORT")
        val sp = fittedSize(titles)
        if (sp > MODE_TITLE_MIN_SP) {
            assertTrue(
                "arama gereginden fazla kucultmus ($sp)",
                titles.any { widthOf(it, sp + 1f) > cardTextWidthDp }
            )
        }
    }

    // -----------------------------------------------------------------------
    // 3. Metin rolu: okunan metin notr, vurgu rengi kimlik tasir
    // -----------------------------------------------------------------------

    @Test
    fun `screen body text stays readable on every skin sky`() {
        // Faz 159 dersi (pasif TOPLA): test yalnizca `accentText`i deniyordu,
        // gercek cagiran baska bir token gonderiyordu ve hata ancak CIHAZDA
        // goruldu. Bu yuzden burada test edilen sey GERCEK cagrilan fonksiyon.
        allCases().forEach { (name, skin, dark) ->
            val palette = blastPalette(skin, dark)
            val s = gameSurfaces(palette, skin.accentGradient)
            val body = screenBodyTextColor(s)

            // Alt baslik 15sp — kucuk metin sayilir, WCAG AA esigi 4.5.
            assertTrue(
                "$name: alt baslik gokyuzune gomuluyor " +
                    "(kontrast ${contrast(body, s.skyTop)})",
                contrast(body, s.skyTop) >= 4.5
            )
            // Gok gradyaninin ortasinda ve ufkunda da okunmali: metin
            // seridi tek bir tonun uzerinde durmuyor.
            assertTrue(
                "$name: alt baslik gok ortasinda okunmuyor",
                contrast(body, s.skyMid) >= 4.5
            )
        }
    }

    @Test
    fun `screen body text is neutral rather than the skin accent`() {
        // ILKE: vurgu rengi KIMLIK/DURUM tasir, okunan metin notr kalir.
        // Biri ileride buraya `accentText` geri yazarsa burada yakalanir.
        allCases().forEach { (name, skin, dark) ->
            val s = gameSurfaces(blastPalette(skin, dark), skin.accentGradient)
            val body = screenBodyTextColor(s)
            val spread = maxOf(body.red, body.green, body.blue) -
                minOf(body.red, body.green, body.blue)
            assertTrue(
                "$name: okunan metin rengi doygun bir vurgu rengi (spread=$spread)",
                spread < 0.12f
            )
            assertTrue(
                "$name: okunan metin skin accent'ine baglanmis",
                body != s.accentText && body != s.accentPrimary
            )
        }
    }

    @Test
    fun `stat label stays readable on the sunken well of every skin`() {
        // Kart icindeki "EN YÜKSEK SEVİYE" etiketi de OKUNAN metin rolunde.
        allCases().forEach { (name, skin, dark) ->
            val s = gameSurfaces(blastPalette(skin, dark), skin.accentGradient)
            val well = s.sunken
            val onWell = if (well.luminance() > 0.45f) {
                Color(0xFF12161F)
            } else {
                Color.White
            }
            assertTrue(
                "$name: istatistik etiketi oyuk zeminde okunmuyor " +
                    "(kontrast ${contrast(onWell, well)})",
                contrast(onWell, well) >= 4.5
            )
        }
    }

    // -----------------------------------------------------------------------
    // 4. Dekorasyon metnin uzerine gelmez
    // -----------------------------------------------------------------------

    @Test
    fun `header pieces stay in the corners and never reach the text column`() {
        // Cihazda yakalandi: turuncu tek kup "Bir oyun modu seç" yazisinin
        // icine giriyordu. Parcayi SILMEK hedeften uzaklastirirdi — hedef
        // gorselde ust koselerde de blok var. Bu yuzden ust serittekiler
        // kenara pimlendi ve gezinme yaricaplari kisildi.
        //
        // En dar desteklenen ekran 292dp: yaricap orada en buyuk ORANSAL
        // salinimi yapar, yani en kotu durum budur.
        val narrowestWidthDp = 292f
        assertTrue("ana menude hic parca kalmadi", ModeSelectWanderingPieces.isNotEmpty())

        ModeSelectWanderingPieces.filter { it.fy < HEADER_BAND_BOTTOM }.forEach { piece ->
            // Gezinme yorungesi bir elips; en kotu durumda yaricap kadar
            // yana kayar.
            val drift = piece.rangeDp / narrowestWidthDp
            // FAZ 162: CIZIM GENISLIGI DE HESABA KATILIYOR.
            //
            // Bu test onceden yalnizca parcanin MERKEZINI kontrol ediyordu.
            // Madde 3'te parcalar duz kareden 3B kup varligina cevrilince
            // cizim genisligi buyudu ve merkez sinirda kalmasina ragmen
            // parcanin KENARI metin sutununa giriyordu — yani test yesil
            // kalirken hata cihazda gorunur olacakti. Tam olarak Faz 161'de
            // yakalanan hatanin ayni sinifi.
            val halfWidth = piece.widthDp / 2f / narrowestWidthDp
            val left = piece.fx - drift - halfWidth
            val right = piece.fx + drift + halfWidth
            assertTrue(
                "ust serit parcasi metin sutununa giriyor " +
                    "(fx=${piece.fx}, salinim ${left}..${right})",
                right <= HEADER_TEXT_LEFT || left >= HEADER_TEXT_RIGHT
            )
        }
    }

    @Test
    fun `header corners still carry pieces like the target image`() {
        // Hedef gorselde ust koselerde blok VAR; ust seridi tamamen
        // bosaltmak hedeften uzaklasmak olurdu.
        assertTrue(
            "ust seritte hic parca kalmamis — hedefte koselerde blok var",
            ModeSelectWanderingPieces.any { it.fy < HEADER_BAND_BOTTOM }
        )
    }
}
