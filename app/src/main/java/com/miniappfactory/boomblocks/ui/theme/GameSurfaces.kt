package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// Faz 158 — "menuler cok soguk ve siradan, hic oyun menusu gibi degil /
// biz sanki websitesi hissi veriyoruz" (kullanici, ekran goruntusuyle).
//
// TESHIS: menuler duz `palette.background` uzerinde duran, esit yukseklikli,
// esit araliktaki Material kartlardan olusuyordu. Bir DUNYA degil bir PANEL.
// Ustelik palet sabitlerinin 20'si `Kron` onekli — yani bu renk sistemi bir
// YARIS oyunundan (Kron Drive) miras alinmis, oyun-menusu diline hic
// donusturulmemisti.
//
// COZUM: skin'e ozel renk GOMMEDEN, her skin'in KENDI `accentGradient`inden
// turetilen tek bir "malzeme" katmani. 6 skinin hicbiri elle ele alinmiyor;
// hepsi ayni formulden geciyor, o yuzden yeni bir skin eklendiginde de
// kendiliginden dogru gorunur.
//
// ACIK/KOYU TEMA: tum kararlar `palette.background.luminance()` uzerinden
// veriliyor — bir `darkMode` bayragi uzerinden DEGIL. Boylece DEFAULT skin'in
// acik temasi da, kalici koyu 5 skin de ayni kodla dogru calisir.
//
// FAZ 146-147 TUZAGI (altin uzerine altin yazi okunmuyordu): `accentText`
// ve `readableOn()` bu sinifin icinde — vurgu rengi bir metne dogrudan
// verilmiyor, once zeminin parlakligina gore duzeltiliyor.
@Immutable
data class GameSurfaces(
    /** Zemin acik mi? Tum kontrast kararlari buna dayanir. */
    val isLightSurface: Boolean,
    val accentPrimary: Color,
    val accentSecondary: Color,
    /** Vurgu renginin ekran zemininde OKUNABILIR hali (baslik/etiket metni icin). */
    val accentText: Color,
    // Zemin katmanlari (GameScreenBackground)
    val skyTop: Color,
    val skyMid: Color,
    val horizon: Color,
    val groundTop: Color,
    val groundBottom: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    // Liste bantlamasi (donusumlu satir tonu)
    val bandEven: Color,
    val bandOdd: Color,
    /** Satir ayiricisi / ince ic cizgi. */
    val hairline: Color,
    /** Panel govdesi ve kenarligi (kart tekduzeligini kiran "grup" yuzeyi). */
    val panel: Color,
    val panelBorder: Color,
    /** Kabartma butonlarin alt gölgesi icin notr koyu ton. */
    val sunken: Color
)

/**
 * Verilen zemin uzerinde okunan metin/ikon rengi. Tek kural: parlak zeminde
 * koyu, koyu zeminde acik. Faz 146-147'de yasanan "NeonGold uzerine NeonGold"
 * hatasinin sistematik panzehiri — renk secen her bilesen bunu cagirir.
 */
fun readableOn(background: Color): Color =
    if (background.luminance() > 0.45f) Color(0xFF12161F) else Color.White

/**
 * Bir vurgu rengini verilen ZEMIN uzerinde metin olarak kullanilabilir hale
 * getirir: acik zeminde koyulastirir, koyu zeminde acar. Renk KIMLIGI (hue)
 * korunur, sadece deger degisir — skin'in karakteri kaybolmaz.
 */
fun accentTextOn(accent: Color, background: Color): Color =
    if (background.luminance() > 0.45f) lerp(accent, Color.Black, 0.42f)
    else lerp(accent, Color.White, 0.22f)

/**
 * Bir rengi HUE'SUNU BOZMADAN doygunlastirir ("derinlestirir").
 *
 * Her kanal, rengin kendi ortalamasindan uzaklastirilir: baskin kanal daha
 * baskin, zayif kanal daha zayif olur. Sonuc ayni renktir ama daha CANLI —
 * lacivert daha doygun bir laciverde doner, turkuaza kaymaz.
 *
 * `amount = 0f` rengi hic degistirmez; acik tema bu yolu kullanir, cunku acik
 * bir zeminde doygunlastirma renk lekesi yaratir.
 */
internal fun deepen(color: Color, amount: Float): Color {
    if (amount <= 0f) return color
    val mid = (color.red + color.green + color.blue) / 3f
    fun push(c: Float) = (mid + (c - mid) * (1f + amount)).coerceIn(0f, 1f)
    return Color(
        red = push(color.red),
        green = push(color.green),
        blue = push(color.blue),
        alpha = color.alpha
    )
}

/**
 * Bir rengin GRI (akromatik) bilesenini soker — hue'yu ve en guclu kanalin
 * parlakligini KORUYARAK doygunlastirir.
 *
 * `deepen` kanallari ortalamadan uzaklastirir; bu yeterli degildi. Bir rengin
 * "yikanmis" gorunmesinin sebebi uc kanalin ORTAK tabani, yani icindeki gri
 * paydir. Burada o taban dogrudan cikariliyor:
 *
 *     gri  = min(r, g, b) * amount
 *     yeni = (r - gri, g - gri, b - gri)
 *
 * OLCUM (2026-08-29, cihaz vs hedef mockup): zemin #253F6D (uc kanal
 * birbirine yakin = tanim geregi GRI) idi, hedef #001154 (kirmizi kanal
 * neredeyse sifir). Fark ton degil DOYGUNLUK'tu; `amount = 1f` gri payi
 * tamamen siler, ara degerler kontrollu doyurur.
 *
 * En guclu kanal degismedigi icin renk KARARMAZ, sadece kirlilikten arinir.
 * Acik temada CAGRILMAZ: acik bir zeminde min kanal buyuktur ve cikarma
 * zemini karartip lekelerdi (bkz. `purify` icindeki `light` dali).
 */
internal fun saturateDeep(color: Color, amount: Float): Color {
    if (amount <= 0f) return color
    val grey = minOf(color.red, color.green, color.blue) * amount.coerceIn(0f, 1f)
    return Color(
        red = (color.red - grey).coerceIn(0f, 1f),
        green = (color.green - grey).coerceIn(0f, 1f),
        blue = (color.blue - grey).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

fun gameSurfaces(palette: BlastPalette, accent: List<Color>): GameSurfaces {
    val bg = palette.background
    val a0 = accent.firstOrNull() ?: NeonCyan
    val a1 = accent.lastOrNull() ?: a0
    val light = bg.luminance() > 0.45f

    // Faz 159 — ZEMIN DERINLESTIRILDI.
    //
    // Kullanicinin teshisi: "arka plan solgun, turkuaz-gri, yikanmis
    // duruyor". Sebep tek bir satirdaydi: gok DOGRUDAN accent'e dogru
    // cekiliyordu (`lerp(bg, a0, 0.26)`). DEFAULT skin'de a0 = camgobegi,
    // yani lacivert zemin yukari dogru TURKUAZA donuyordu — mockup'taki
    // derin, doygun maviye degil.
    //
    // Yeni turetme iki adimli:
    //   1. once zeminin KENDI rengi doyurulur (siyaha dogru degil, kendi
    //      hue'sunda derinlestirilir),
    //   2. sonra accent yalnizca HAFIF bir tint olarak eklenir (0.26 -> 0.13).
    // Sonuc: renk kimligi accent'ten gelmeye devam eder ama zemin yikanmaz.
    // Tint icin accent'in TEK ucu degil, gradyanin ORTASI kullaniliyor.
    // DEFAULT skin'de a0 camgobegi (#06B6D4), a1 mor (#8B5CF6); ortalari
    // #4F84E7 — yani mockup'taki KRALIYET MAVISI. Tek basina a0 kullanmak
    // zemini turkuaza cekiyordu, kullanicinin "yikanmis duruyor" dedigi sey
    // tam olarak oydu. Formul skin'e ozel degil: ORMAN'da yesil, GUN BATIMI'nda
    // sicak turuncu-pembe, SEKER PEMBESI'nde pembe uretir.
    //
    // FAZ 162 — MADDE 1: DOYGUNLUK. Faz 159'daki iki adim dogru yondeydi ama
    // yetmedi. Cihazda olculdu (2026-08-29):
    //
    //   | katman     | hedef   | Faz 159 |
    //   |------------|---------|---------|
    //   | ust zemin  | #01267C | #253F6D |
    //   | kart ici   | #172662 | #294D69 |
    //   | kenar zemin| #0F1D65 | #1A2A4E |
    //
    // Hedefte kirmizi kanal ~0, bizimkinde uc kanal birbirine yakin — yani
    // TANIM GEREGI gri. Sebep `lerp(deepBg, skyTint, ...)`: accent tint'i
    // hue kimligini getiriyor ama beraberinde GRI PAY da getiriyor, cunku
    // Oklab lerp'i iki rengi karistirirken ortak bir taban birakiyor.
    //
    // Cozum sira degisikligi: once doyur, tint'i uygula, SONRA tekrar doyur.
    // Ikinci gecis tint'in getirdigi gri payi soker; hue kimligi (yani
    // skin'in karakteri) tint'ten gelmeye devam eder.
    val skyTint = lerp(a0, a1, 0.55f)
    // Acik tema bu islemlerin DISINDA: acik bir zeminde gri payi sokmek
    // zemini karartir ve renk lekesi yapar. DEFAULT skin'in acik temasi
    // GameSurfacesSkinTest'te de taraniyor.
    fun purify(c: Color, amount: Float): Color = if (light) c else saturateDeep(c, amount)

    //
    // TINT ARTIK DAHA GUCLU (0.34 -> 0.48). Faz 159 tint'i 0.26'ya DUSURMUSTU
    // cunku tint tek basina zemini yikiyordu; artik arkasindan ikinci bir
    // doyurma gecisi geldigi icin tint'i kismaya gerek yok. Tersine: hedefin
    // parlakligi (#01267C, mavi kanal 124) ancak bu kadar tint ile yakalaniyor.
    // Az tint + cok doyurma = dogru renk ama COK KOYU zemin (ilk denemede
    // #061B4B cikti, hedefin mavi kanalinin ucte ikisi).
    val deepBg = purify(deepen(bg, if (light) 0.0f else 0.28f), 0.78f)
    // CIHAZDA OLCULDU (ikinci gecis): 0.84 doyurmayla ust zemin #0F2D67
    // cikti — hedefin (#01267C) tonunu tutturdu ama kirmizi kanalda 15
    // birim artik gri kaldi. Kalintinin kaynagi zemin gradyani DEGIL,
    // uzerine cizilen ambient isima halkalari; onlari kismak yerine
    // doygunluk 0.90'a cekildi, cunku isimalar hacim hissini veriyor.
    val skyTop = purify(lerp(deepBg, skyTint, if (light) 0.10f else 0.56f), 0.90f)
    val skyMid = purify(lerp(deepBg, skyTint, if (light) 0.05f else 0.42f), 0.90f)
    // Ufuk da mavi tasiyor. Onceden `horizon = bg` idi; gradyanin ALT ucu
    // ham koyu zemine dusuyordu ve ekranin yarisi neredeyse siyah kaliyordu.
    // Mockup'ta mavi asagi dogru KOYULASIR ama kaybolmaz.
    val horizonColor = purify(lerp(deepBg, skyTint, if (light) 0.03f else 0.24f), 0.86f)

    // Zemin bandi: asagi dogru KOYULASIR. Acik temada siyaha cok az (0.10)
    // gidilir; yoksa acik temanin altinda kara bir serit olusurdu.
    val groundBase = purify(lerp(horizonColor, a1, if (light) 0.10f else 0.12f), 0.84f)
    val groundTop = lerp(groundBase, Color.Black, if (light) 0.05f else 0.12f)
    val groundBottom = lerp(groundBase, Color.Black, if (light) 0.13f else 0.34f)

    return GameSurfaces(
        isLightSurface = light,
        accentPrimary = a0,
        accentSecondary = a1,
        accentText = accentTextOn(a0, bg),
        skyTop = skyTop,
        skyMid = skyMid,
        horizon = horizonColor,
        groundTop = groundTop,
        groundBottom = groundBottom,
        glowPrimary = a0.copy(alpha = if (light) 0.10f else 0.17f),
        glowSecondary = a1.copy(alpha = if (light) 0.08f else 0.13f),
        // Bantlama: bir ton kartin kendisi, digeri cardAlt'a dogru kaydirilmis
        // hali. cardAlt koyu paletlerde daha ACIK, acik palette daha KOYU —
        // yani fark iki yonde de olusuyor, elle dallanmaya gerek yok.
        // FAZ 162 — MADDE 2: KART GOVDESI DE DOYGUN DEGILDI.
        //
        // DESIGN_SPEC kart govdesini #0E1835, ic istatistik panelini #0A1329
        // veriyor; ikisi de derin ve DOYGUN lacivert. Bizimki #294D69 /
        // #080D18 idi — birincisi gri-mavi, ikincisi neredeyse notr siyah.
        // Zemin doyurulup kart oldugu gibi birakilsaydi kart zeminin
        // uzerinde GRI bir dikdortgen olarak one cikardi; malzeme tek
        // parca okunmazdi. O yuzden ayni `purify` kart katmanlarina da
        // uygulaniyor, sadece daha yumusak katsayilarla: kart zeminden
        // AYRISMAYA devam etmek zorunda.
        bandEven = purify(palette.card, 0.58f),
        // BANTLAMA FARKI GARANTI EDILIYOR. `purify` iki tonun ORTAK gri
        // payini sokerken aralarindaki parlaklik farkini da daraltiyor:
        // ilk denemede SUNSET (0.0036) ve PURPLE_NIGHT (0.0023)
        // GameSurfacesSkinTest'in 0.004 esiginin ALTINA dustu. Fark artik
        // doyurmadan SONRA, paletten bagimsiz sabit bir aciklastirma ile
        // ekleniyor — hangi skin gelirse gelsin bant gorunur kaliyor.
        bandOdd = purify(lerp(palette.card, palette.cardAlt, 0.55f), 0.58f)
            .let { if (light) it else lerp(it, Color.White, 0.07f) },
        hairline = palette.cardBorder.copy(alpha = 0.55f),
        panel = purify(lerp(palette.card, bg, 0.25f), 0.66f),
        panelBorder = palette.cardBorder,
        // Ic istatistik paneli. Onceden HAM `bg`den turuyordu, yani zemin
        // doyurulunca geride notr kalirdi; artik doyurulmus `deepBg`den
        // turuyor ve siyaha daha az gidiyor (0.45 -> 0.30) — hedefteki
        // #0A1329 lacivert kaliyor, #080D18 gibi kömürlesmiyor.
        sunken = if (light) {
            lerp(bg, Color.Black, 0.22f)
        } else {
            // 0.30 siyah ilk denemede #000516 uretti — hedefteki #0A1329
            // lacivertin yerine neredeyse saf siyah. Kart ICINDEKI kutu
            // karttan koyu olmali ama RENGINI kaybetmemeli.
            lerp(deepBg, Color.Black, 0.10f)
        }
    )
}

/**
 * Ekranlarin kullandigi giris noktasi. `accentOverride` sadece mod kimligi
 * tasiyan ekranlar icindir (Kariyer camgobegi / Pro turuncu / Kolay nane) —
 * verilmezse skin'in kendi gradyani kullanilir.
 */
@Composable
fun rememberGameSurfaces(
    skin: BlastSkin,
    darkMode: Boolean,
    accentOverride: List<Color>? = null
): GameSurfaces {
    val palette = blastPalette(skin, darkMode)
    val accent = accentOverride ?: skin.accentGradient
    return remember(palette, accent) { gameSurfaces(palette, accent) }
}
