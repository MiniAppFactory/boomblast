package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces

// Faz 159 — TEK BASLIK MALZEMESI.
//
// Kullanicinin sozu: "burada da Kaboom Blocks yazisi gibi golgeli, sari mavi
// vs guzel yaz, ayni sekilde AYARLAR yazisi da."
//
// Onceki halde `GameTitle` ile `GameWordmark` AYRI davraniyordu: baslik ince
// konturlu duz lavanta metin, wordmark ise iki renkli tek satir. Cihaz
// ekran goruntusunde ikisi de YASSI cikiyordu — cunku kontur yazi boyutunun
// %11'i kadardi ve ic isik katmani hic yoktu.
//
// Bu dosya ikisinin de bestlendigi TEK recete. Fark yalnizca:
//   - renk semasi (altin / skin accent'i),
//   - satir bolunmesi (tek kelime tek satir, cok kelime iki satir).
// Kontur kalinligi, gradyan, ic isik, golge ve parilti AYNI.
//
// KATMAN SIRASI (alttan uste) — 3B kabartma hissi tam olarak bundan cikiyor:
//   1. dis isima  : accent renginde bulanik golge, harflerin cevresine tasar
//   2. dus golgesi: koyu, asagi kaymis — harfi zeminden KOPARIR
//   3. kontur     : kalin koyu lacivert Stroke (yazi boyutunun %22'si)
//   4. dolgu      : dikey gradyan (acik ust -> doygun alt)
//   5. ic isik    : ustten asagi sonen beyaz gradyan — "cam/kabartma" parlakligi
//
// METIN TABANLI: 5 dil var, gorsel wordmark surdurulemez. Her sey Text +
// drawStyle/brush ile ciziliyor, hicbir varlik yok.

// ---------------------------------------------------------------------------
// Renk semasi
// ---------------------------------------------------------------------------

@Immutable
data class EmblemColors(
    val fillTop: Color,
    val fillBottom: Color,
    val outline: Color,
    val glow: Color,
    // 3B GOVDE (ekstruzyon) rengi. Harfin YUZUNUN altinda kalan yan yuzey.
    // Logodaki "Kaboom" yazisinin hacimli durmasinin sebebi bu katman:
    // yuz sari->turuncu gradyan, govde ondan DAHA KOYU turuncu, ve ikisi
    // birlikte "alttan turuncu isik vurmus" izlenimini veriyor.
    // Verilmezse (null) duz cizim yapilir, yani eski davranis.
    val extrude: Color? = null
)

// Marka altini — mockup'taki ust satir. Tum skinlerde AYNI kalir (marka
// rengidir), sadece acik zeminde okunurluk icin koyulastirilir.
// FAZ 162 — kullanici: "yazinin rengi logodaki Kaboom yazisindaki sari ile
// tutmamis." Tahmin etmek yerine logo varliginin kendisinden ORNEKLENDI
// (docs/ui_mockups/assetpack/kaboom_blocks_logo.png, "Kaboom" satiri):
// en yaygin tonlar #FCE400 (ust isik) ve #FCB400 (alt golge), ve ikisinde de
// MAVI KANAL SIFIR — yani tam doygun.
//
// Eski degerler: #FFE066 (mavi 0x66 -> yikanmis sari) ve #F08A15 (fazla
// turuncu). Baslik logonun yaninda hep bir tik solgun kaliyordu.
private val EmblemGoldTop = Color(0xFFFFE400)
// ALT TON: cihaz olcumu (2026-08-29) logoda #FAA000 gibi KOYU TURUNCU
// tonlarin da bulundugunu, basliktaysa her seyin sari araliginda sikistigini
// gosterdi — tepe tonlar birebir ayni (#FADC00, %100 doygunluk), fark
// GRADYANIN DERINLIGINDE. #FCB400 sariya fazla yakindi ve yaziyi yassi
// gosteriyordu; alt ton logonun derin turuncusuna cekildi.
private val EmblemGoldBottom = Color(0xFFFA9200)
// KONTUR: logodan orneklendi (2026-08-29). "Kaboom" harflerinin cevresindeki
// kontur mat lacivert DEGIL, derin MENEKSE — olculen baskin tonlar #300090 ve
// #3C0090. Eski deger (#121A45) doygunlugu dusuk bir laciverttı ve yazi
// logonun yaninda hep bir tik "soguk" duruyordu. Ayni kontur logoda hem altin
// "Kaboom" hem mavi "Blocks" satirinda kullaniliyor, yani her iki sema icin de
// dogru.
private val EmblemDarkOutline = Color(0xFF33128C)
private val EmblemLightOutline = Color(0xFFFFFFFF)

/** Baslik/wordmark'in ALTIN yarisi. */
fun goldEmblemColors(isLightSurface: Boolean): EmblemColors =
    if (isLightSurface) {
        EmblemColors(
            fillTop = lerp(EmblemGoldTop, Color(0xFF7A4A00), 0.30f),
            fillBottom = lerp(EmblemGoldBottom, Color(0xFF7A2E00), 0.45f),
            outline = EmblemLightOutline,
            glow = EmblemGoldBottom.copy(alpha = 0.0f)
        )
    } else {
        EmblemColors(
            fillTop = EmblemGoldTop,
            fillBottom = EmblemGoldBottom,
            // Govde: alt tondan belirgin daha koyu turuncu-kahve. Logodan
            // orneklenen derin ton (#F09C00) ile ayni eksende ama daha koyu,
            // cunku govde isik ALMAYAN yuzey.
            extrude = Color(0xFFB35A00),
            outline = EmblemDarkOutline,
            glow = Color(0xFFFFB020).copy(alpha = 0.55f)
        )
    }

/**
 * Baslik/wordmark'in ACCENT yarisi (mockup'ta mavi). Skin'e ozel renk
 * GOMULMUYOR: her skin kendi `accentGradient`inden kendi ciftini alir —
 * DEFAULT'ta camgobegi, ORMAN'da yesil, SEKER PEMBESI'nde pembe.
 */
fun accentEmblemColors(
    accentPrimary: Color,
    accentSecondary: Color,
    isLightSurface: Boolean
): EmblemColors =
    if (isLightSurface) {
        EmblemColors(
            fillTop = lerp(accentPrimary, Color.Black, 0.30f),
            fillBottom = lerp(accentSecondary, Color.Black, 0.52f),
            outline = EmblemLightOutline,
            glow = accentPrimary.copy(alpha = 0f)
        )
    } else {
        EmblemColors(
            // Ust ucu beyaza dogru aciliyor: gradyanin "isik alan" tarafi.
            fillTop = lerp(accentPrimary, Color.White, 0.55f),
            fillBottom = lerp(accentSecondary, Color(0xFF0B3C8A), 0.25f),
            outline = EmblemDarkOutline,
            glow = accentPrimary.copy(alpha = 0.55f)
        )
    }

// ---------------------------------------------------------------------------
// Satir bolme
// ---------------------------------------------------------------------------

/**
 * Basligi mockup'taki gibi iki satira boler. Dile GOMULU elle bolme YOK —
 * karar kelime sayisindan cikar, o yuzden 5 dilin hepsinde ayni kodla dogru
 * calisir:
 *
 *   AYARLAR / SETTINGS / IMPOSTAZIONI / PARAMETRES / AJUSTES  -> tek kelime,
 *     tek satir (altin).
 *   HAFTALIK GOREVLER / WEEKLY MISSIONS / MISSIONI SETTIMANALI /
 *     MISSIONS HEBDOMADAIRES / MISIONES SEMANALES -> iki kelime, iki satir
 *     (ust altin, alt accent).
 *
 * Uc ve daha fazla kelimede bolme noktasi KARAKTER SAYISINA gore en dengeli
 * yerden secilir; boylece bir satir digerinin iki kati uzunlugunda kalmaz.
 */
internal fun splitEmblemLines(text: String): List<String> {
    val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.size <= 1) return listOf(text.trim())
    if (words.size == 2) return words

    var bestIndex = 1
    var bestDiff = Int.MAX_VALUE
    for (i in 1 until words.size) {
        val head = words.take(i).sumOf { it.length + 1 } - 1
        val tail = words.drop(i).sumOf { it.length + 1 } - 1
        val diff = kotlin.math.abs(head - tail)
        if (diff < bestDiff) {
            bestDiff = diff
            bestIndex = i
        }
    }
    return listOf(
        words.take(bestIndex).joinToString(" "),
        words.drop(bestIndex).joinToString(" ")
    )
}

/**
 * TASMA GUVENCESI. Iki satira bolunmus baslikta genisligi belirleyen sey en
 * UZUN satirdir ("HEBDOMADAIRES" = 13 karakter). Sabit bir yazi boyutu dar
 * ekranda (320dp) + buyutulmus yazi tipinde (fontScale 1.3) o satiri
 * kirptirirdi.
 *
 * Cozum: karakter sayisi bir butceyi asinca yazi boyutu ORANTILI kuculur.
 * Deterministik — ayni metin her zaman ayni boyutu verir — ve alt sinir
 * %68, yani baslik hicbir dilde okunamayacak kadar kucuk olmaz.
 */
internal fun emblemFontScale(lines: List<String>, budget: Int = 11): Float {
    val longest = lines.maxOfOrNull { it.length } ?: 0
    if (longest <= budget) return 1f
    return (budget.toFloat() / longest.toFloat()).coerceAtLeast(0.68f)
}

// ---------------------------------------------------------------------------
// Malzeme
// ---------------------------------------------------------------------------

/**
 * Tek satirlik amblem metni — 5 katmanin tamami. Hem `GameTitle` hem
 * `GameWordmark` bunu cagirir, o yuzden ikisi BIREBIR ayni malzemeyi tasir.
 */
@Composable
fun GameEmblemLine(
    text: String,
    colors: EmblemColors,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    maxLines: Int = 1,
    textAlign: TextAlign = TextAlign.Center,
    // Faz 161 — SATIR KUTUSUNU HARFE OTURT.
    //
    // Kullanicinin sozu: "Kaboom ve Blocks yazisi arasina kocaman bosluk
    // birakmissin." Bosluk ELLE KONULMAMISTI: satir kutusu yazi tipinin
    // kendi ascent/descent payini tasiyor ve iki satir alt alta gelince o
    // pay iki kez sayiliyordu. Punto buyudukce bosluk da buyuyor.
    //
    // `tightLines` satir yuksekligini punto ile esitler ve fazlaligi KIRPAR
    // (Trim.Both) — harfler kesilmez, yalnizca cevrelerindeki bos pay gider.
    // Wordmark tek bir logo blogu olarak okunmali; ekran BASLIKLARINDA
    // (GameTitle) varsayilan davranis korunuyor.
    tightLines: Boolean = false
) {
    val density = LocalDensity.current
    // Kontur kalinligi yazi boyutuyla olcekleniyor: fontScale buyudugunde de
    // orani korunur. 0.11 -> 0.22, yani onceki halin iki kati.
    val strokePx = with(density) { (fontSize.value * 0.22f).dp.toPx() }

    val base = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        letterSpacing = (fontSize.value * 0.035f).sp,
        textAlign = textAlign,
        lineHeight = if (tightLines) fontSize else TextUnit.Unspecified,
        platformStyle = if (tightLines) PlatformTextStyle(includeFontPadding = false) else null,
        lineHeightStyle = if (tightLines) {
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        } else {
            null
        }
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1+2) Kontur + dis isima + dus golgesi tek gecişte.
        //      StrokeJoin.Round: Black agirlikta koseli birlesimler sivri
        //      "diken"ler uretiyordu, yuvarlak birlesim onu keser.
        Text(
            text = text,
            style = base.copy(
                drawStyle = Stroke(width = strokePx, join = StrokeJoin.Round),
                color = colors.outline,
                shadow = Shadow(
                    color = colors.glow,
                    offset = Offset(0f, 0f),
                    blurRadius = strokePx * 2.6f
                )
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        // Dus golgesi: koyu, ASAGI kaymis. Harfi zeminden koparan katman.
        Text(
            text = text,
            style = base.copy(
                drawStyle = Stroke(width = strokePx * 0.9f, join = StrokeJoin.Round),
                color = Color(0xFF060B22).copy(alpha = 0.55f),
                shadow = Shadow(
                    color = Color(0xFF060B22).copy(alpha = 0.75f),
                    offset = Offset(0f, strokePx * 0.85f),
                    blurRadius = strokePx * 1.2f
                )
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        // Konturu golgenin uzerine bir kez daha koy: golge konturu
        // kirletmesin, kenar NET kalsin.
        Text(
            text = text,
            style = base.copy(
                drawStyle = Stroke(width = strokePx, join = StrokeJoin.Round),
                color = colors.outline
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        // 3.5) 3B GOVDE (ekstruzyon).
        //
        // Kullanicinin teshisi (2026-08-29): "Kaboom yazisi 3D efektli,
        // gradyan alttan turuncu isik vurmus gibi. Sen bunu yapamiyorsun."
        // Dogruydu — o ana kadar butun katmanlar DUZ metindi; renk araligini
        // genisletmek yassiligi cozmuyor, cunku eksik olan sey HACIM'di.
        //
        // Cozum: yuzun altina, asagi dogru kayan birkac dolu kopya. Her kopya
        // govde renginde; ustlerini bir sonraki kopya ortuyor, en ustte de yuz
        // duruyor. Geriye harfin ALT kenarinda gorunen bir yan yuzey kaliyor.
        // Adim sayisi puntoyla oranli (kucuk baslikta 2-3, buyukte 5-6).
        if (colors.extrude != null) {
            // 0.62 -> 0.38: ilk denemede kabartma FAZLA cikti (kullanici
            // cihazda gordu). Logodaki govde ince bir yan yuzey; bizimki
            // harfin altinda ikinci bir yazi gibi duruyordu.
            val depthPx = strokePx * 0.38f
            val steps = depthPx.toInt().coerceIn(2, 7)
            for (i in steps downTo 1) {
                val dy = with(density) { (depthPx * i / steps).toDp() }
                Text(
                    text = text,
                    style = base.copy(color = colors.extrude),
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = dy)
                )
            }
        }
        // 4) Dolgu: dikey gradyan.
        Text(
            text = text,
            style = base.copy(
                brush = Brush.verticalGradient(
                    // GECIS NOKTASI 0.52 -> 0.26 (2026-08-29, cihaz olcumu).
                    // Ust renk yarıya kadar SABIT tutuluyordu; kucuk puntoda
                    // harf yuksekligi zaten az oldugu icin alt ton neredeyse
                    // hic gorunmuyor, yazi YASSI okunuyordu. Cihazdan olculen
                    // tonlarin tamami #FADC00-#FAE600 arasinda sikisikti,
                    // logodaki derin turuncu (#FAA000) hic cikmiyordu.
                    // Gecis erkene alininca gradyan harfin icinde gelisiyor.
                    0f to colors.fillTop,
                    0.26f to colors.fillTop,
                    1f to colors.fillBottom
                )
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
        // 5) Ic isik: ustten asagi SONEN beyaz. Harfin ust yuzunde parlayan
        //    ince serit — kabartma hissini veren asil katman.
        Text(
            text = text,
            style = base.copy(
                // CIHAZDA GORULEN: ic isik cok genis ve cok parlakti; kucuk
                // punto wordmark'ta ALTIN neredeyse beyaza donuyordu. Serit
                // daraltildi (0.34 -> 0.24) ve alfasi dusuruldu (0.85 -> 0.58)
                // — kabartma hissi kaliyor, renk kimligi kaybolmuyor.
                brush = Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.58f),
                    0.24f to Color.White.copy(alpha = 0.08f),
                    0.40f to Color.Transparent,
                    1f to Color.Transparent
                )
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Parilti konumlari: amblem kutusunun kose bolgelerinde, metnin ORTASINA
// girmeyecek sekilde. x/y orani + yaricap orani (kutu yuksekligine gore).
private val EMBLEM_SPARKS = listOf(
    Triple(0.045f, 0.20f, 0.115f),
    Triple(0.135f, 0.60f, 0.070f),
    Triple(0.915f, 0.22f, 0.130f),
    Triple(0.965f, 0.66f, 0.075f),
    Triple(0.30f, 0.055f, 0.060f)
)

/**
 * Amblemin cevresindeki parildamalar (mockup'taki dort kollu yildizlar).
 * Metnin ARKASINA degil, kutunun kenar bolgelerine cizilir — okunurlugu
 * dusurmez. Animasyon YOK: bir kez cizilir, FPS/pil etkisi sifir.
 */
private fun DrawScope.drawEmblemSparks(tint: Color, alpha: Float) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    for ((fx, fy, fr) in EMBLEM_SPARKS) {
        val c = Offset(w * fx, h * fy)
        val r = h * fr
        // Yumusak hale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tint.copy(alpha = 0.55f * alpha), Color.Transparent),
                center = c,
                radius = r * 1.9f
            ),
            radius = r * 1.9f,
            center = c
        )
        // Dort kollu yildiz govdesi
        val star = Path().apply {
            moveTo(c.x, c.y - r)
            quadraticBezierTo(c.x + r * 0.16f, c.y - r * 0.16f, c.x + r, c.y)
            quadraticBezierTo(c.x + r * 0.16f, c.y + r * 0.16f, c.x, c.y + r)
            quadraticBezierTo(c.x - r * 0.16f, c.y + r * 0.16f, c.x - r, c.y)
            quadraticBezierTo(c.x - r * 0.16f, c.y - r * 0.16f, c.x, c.y - r)
            close()
        }
        drawPath(path = star, color = Color.White.copy(alpha = 0.92f * alpha))
    }
}

/**
 * Iki satirli, iki renkli amblem — mockup'taki "HAFTALIK / GOREVLER" ve
 * "Kaboom / Blocks" duzeni. Tek kelimelik metinlerde tek satir kalir.
 *
 * @param sparkle kenar parildamalari cizilsin mi (baslikta evet, dar
 *   yerlestirmelerde kapatilabilir).
 */
@Composable
fun GameEmblem(
    text: String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    textAlign: TextAlign = TextAlign.Center,
    allowSplit: Boolean = true,
    sparkle: Boolean = true,
    /** Iki satiri tek blok halinde sikistirir (bkz. `GameEmblemLine`). */
    tightLines: Boolean = false,
    // Faz 161 — SATIR ARASI ACIKLIK.
    //
    // Kullanicinin sozu: "aralarina kocaman bosluk birakmissin Kaboom ve
    // Blocks yazisi arasinda." Hedef gorselde iki satir neredeyse BITISIK
    // (Kaboom 95-165, Blocks 165-240 — ayni pikselde bitip basliyorlar).
    //
    // Bosluk elle konulmamisti: yazi tipinin ascent/descent payi satir
    // kutusunu punto'nun ~1.5 katina cikariyor ve iki satirda iki kez
    // sayiliyor. `lineHeight` + `Trim` bunu COZMEDI (cihazda denendi:
    // dogal metrik zaten lineHeight'tan buyuk oldugu icin kirpacak "fazla"
    // bulamiyor). Bu yuzden aciklik dogrudan Column'dan NEGATIF veriliyor —
    // ayni yontem kart istatistik kutusunda da kullaniliyor.
    //
    // Varsayilan 0.dp: ekran BASLIKLARI (GameTitle) etkilenmez.
    lineSpacing: Dp = 0.dp
) {
    val lines = if (allowSplit) splitEmblemLines(text) else listOf(text.trim())
    val scale = emblemFontScale(lines)
    val effectiveSize = (fontSize.value * scale).sp

    val gold = goldEmblemColors(surfaces.isLightSurface)
    val accent = accentEmblemColors(
        surfaces.accentPrimary,
        surfaces.accentSecondary,
        surfaces.isLightSurface
    )
    val sparkTint = if (surfaces.isLightSurface) surfaces.accentPrimary else Color(0xFFBFE9FF)

    // Dis kutu cagiranin verdigi alani kaplar (baslikta `weight(1f)`), IC
    // kutu ise metne gore DARALIR. Parildamalar ic kutuya gore ciziliyor:
    // aksi halde genis bir ust barda yildizlar metinden kopup ekranin
    // kenarlarina dagilirdi.
    Box(
        modifier = modifier,
        contentAlignment = when (textAlign) {
            TextAlign.Start -> Alignment.CenterStart
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.Center
        }
    ) {
        Box {
            if (sparkle && !surfaces.isLightSurface) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawEmblemSparks(sparkTint, alpha = 1f)
                }
            }
            Column(
                modifier = Modifier
                    // Kalin kontur harf kutusunun disina tasar; bu bosluk
                    // olmadan ust/alt satirda kirpilirdi.
                    .padding(
                        vertical = (effectiveSize.value * 0.10f).dp,
                        horizontal = (effectiveSize.value * 0.14f).dp
                    ),
                verticalArrangement = Arrangement.spacedBy(lineSpacing),
                // SATIRLAR HER ZAMAN ORTAK EKSENDE ORTALANIR — `textAlign`
                // yalnizca BLOGUN BUTUNUNUN nereye oturacagini belirler (ust
                // barda sola, baslikta ortaya), satirlarin birbirine gore
                // hizasini DEGIL.
                //
                // Kullanicinin sozu: "Kaboom ve Blocks ayni yerden basliyor
                // olmaz, ikisi de ayri ayri centered olmali." Sola hizalandiginda
                // kisa satirin sagi bos kaliyor ve blok dengesiz gorunuyordu;
                // ayni sorun "MISSIONS / HEBDOMADAIRES" gibi genislik farki
                // buyuk dillerde daha da belirgin.
                //
                // Her satir kendi genisliginde kaliyor (wrap-content), yani
                // kontur cizimi ve olcum bozulmuyor — hizalama Column'a ait.
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                lines.forEachIndexed { index, line ->
                    GameEmblemLine(
                        text = line,
                        // Ust satir ALTIN, alt satir skin accent'i. Tek
                        // satirsa altin (mockup'taki "AYARLAR").
                        colors = if (index == 0) gold else accent,
                        fontSize = effectiveSize,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        tightLines = tightLines
                    )
                }
            }
        }
    }
}
