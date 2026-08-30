package com.miniappfactory.boomblocks.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.readableOn

// Faz 158 — oyun menusu "malzemesi".
//
// Kullanicinin teshisi cok islevseldi: "kurumsal bir sirketin pptx'i gibi",
// "biz sanki websitesi hissi veriyoruz". Kok sebep RENK degil MALZEME idi:
// her satir ayni yukseklikte duz bir Material Card, butonlar duz dikdortgen,
// ikonlar isletim sistemi emojisi.
//
// Bu dosya o malzemeyi tek yerde tanimliyor:
//   GameTitle       — konturlu + gradyan dolgulu baslik (metin tabanli, 5 dil)
//   GamePanel       — kabartmali panel (ust ic isik cizgisi + durum kenarligi)
//   GameIconTile    — gradyan dolgulu yuvarlatilmis ikon kutucugu
//   GameButton      — kalin govde + kenarlik + KATI alt golge + basinca cokme
//   GameCoinPill    — icon_coin + sayi (ciplak sayi/emoji YERINE)
//   GameStatPill    — ikonlu genel deger kapsulu
//   GameScrollHint  — "asagida devami var" chevron'u
//
// TASMA KURALI: bu dosyadaki HICBIR bilesende sabit yukseklik yok. Her yerde
// `defaultMinSize(minHeight = ...)` var — yani TR/IT gibi uzun ceviriler
// kirpilmak yerine bileseni UZATIR. Dokunma hedefi de bu alt sinirla
// garanti altinda (>= 48dp).

// ---------------------------------------------------------------------------
// Baslik
// ---------------------------------------------------------------------------

// Faz 159 — baslik artik `GameEmblem` ile AYNI malzemeden. Kullanicinin
// istegi: "burada da Kaboom Blocks yazisi gibi golgeli, sari mavi vs guzel
// yaz, ayni sekilde AYARLAR yazisi da."
//
// Onceki hal ince konturlu (yazi boyutunun %11'i) tek renk gradyandi ve
// cihazda YASSI cikiyordu. Simdi kontur %22, ustune ic isik + dus golgesi +
// dis isima + parilti geliyor; hepsi GameEmblem.kt'de tanimli.
//
// Bolme kurali: `maxLines >= 2` ise cok kelimeli baslik iki satira boluner
// (ust ALTIN, alt skin accent'i). `maxLines == 1` cagiranlar (LoadoutScreen'de
// "SEVIYE 3") tek satirda kalir — davranislari degismedi.
@Composable
fun GameTitle(
    text: String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
    fillColors: List<Color>? = null,
    maxLines: Int = 2,
    textAlign: TextAlign = TextAlign.Center
) {
    GameEmblem(
        text = text,
        surfaces = surfaces,
        modifier = modifier,
        fontSize = fontSize,
        textAlign = textAlign,
        allowSplit = maxLines >= 2,
        sparkle = true
    )
}

// ---------------------------------------------------------------------------
// Panel / kart
// ---------------------------------------------------------------------------

/**
 * Faz 159 — DIS PARLAMA (neon bloom).
 *
 * Kullanicinin teshisi: "kart kenarliklari sonuk, hedefte parlak neon ve cift
 * cizgi hissi veren bir parlama var". Onceki halde kenarlik tek bir 1.5dp
 * cizgiydi.
 *
 * `Modifier.blur` KULLANILAMAZ: API 31+ istiyor, bu projenin minSdk'si 24 —
 * eski cihazlarda sessizce hicbir sey yapmazdi. Onun yerine parlama, gittikce
 * genisleyen ve sonen KATMANLI konturlarla yaklastiriliyor; sonuc gozle ayni,
 * maliyet birkac cizgi.
 *
 * `.clip()` ZINCIRDEN ONCE cagrilmali — aksi halde parlama kirpilir ve
 * disariya tasamaz.
 */
internal fun Modifier.gameOuterGlow(
    accent: Color,
    cornerRadius: Dp,
    intensity: Float,
    layers: Int = 3,
    // FAZ 162 — MADDE 4. Kullanici: "dil seciminden sonraki 3 ekranin kenar
    // cizgileri daha neon efekti alabilir, cok tekduze hissediliyor."
    //
    // Cihazda goruldu: o kartin kenarligi TEK, sabit parlaklikta ince bir
    // cizgi — cam tup degil, cizilmis bir cerceve. Katman sayisi ve
    // basamak araligi artik disaridan verilebiliyor ki ayni malzeme hem
    // sonuk (mod karti) hem guclu (onboarding karti) halede kullanilabilsin.
    //
    // VARSAYILANLAR MEVCUT GORUNUMU AYNEN KORUR (3 katman / 2.2dp / 0.30):
    // mod secim kartlari kullanicinin begendigi haliyle kaliyor, cagiran
    // taraf acikca istemedikce hicbir sey degismiyor.
    spreadStepDp: Float = 2.2f,
    coreAlpha: Float = 0.30f
): Modifier = this.drawBehind {
    val i = intensity.coerceIn(0f, 1f)
    if (i <= 0.01f) return@drawBehind
    val r = cornerRadius.toPx()
    for (layer in 1..layers) {
        val spread = layer * spreadStepDp * density
        // 1/layer sonumu: disa dogru hizla zayiflayan bir hale — gercek
        // blur'un ucuz yaklasimi. Kok yerine dogrudan bolme kullaniliyor,
        // cunku daha yavas sonum "kalin ikinci bir cerceve" gibi okunuyor.
        val alpha = coreAlpha * i / layer
        drawRoundRect(
            color = accent.copy(alpha = alpha),
            topLeft = Offset(-spread, -spread),
            size = Size(size.width + spread * 2f, size.height + spread * 2f),
            cornerRadius = CornerRadius(r + spread),
            style = Stroke(width = 2f * density)
        )
    }
}

// Kabartmali panel: ust kenarda ince ACIK ic cizgi (isik yukaridan),
// disinda durum tasiyan kenarlik. `emphasis` 0..1 — kart ne kadar "aktif"
// ise kenarligi o kadar parlak. Hedef mockup'ta ilerlemis gorev karti
// parlak, sonuk gorev karti mat: kenarlik BILGI tasiyor.
@Composable
fun GamePanel(
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    emphasis: Float = 0f,
    accent: Color? = null,
    cornerRadius: Dp = 18.dp,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val glowAccent = accent ?: surfaces.accentPrimary
    val e = emphasis.coerceIn(0f, 1f)
    // Faz 159: CIHAZDA GORULEN — vurgusuz panellerin kenarligi ham
    // `panelBorder` (#3B4E70, koyu gri-mavi) idi ve zeminden hic ayrilmiyordu
    // ("kart kenarliklari sonuk"). Taban kenarlik artik accent'e dogru %38
    // cekiliyor; vurgu ARTTIKCA fark aciliyor, yani kenarlik hala BILGI
    // tasiyor, sadece taban degeri gorunur hale geldi.
    val baseBorder = lerp(surfaces.panelBorder, glowAccent, if (surfaces.isLightSurface) 0.20f else 0.38f)
    val borderTop = lerp(
        baseBorder,
        lerp(glowAccent, Color.White, 0.30f),
        e * 0.85f
    )
    val borderBottom = lerp(
        baseBorder,
        glowAccent.copy(alpha = 0.5f),
        e * 0.4f
    )
    Column(
        modifier = modifier
            // Faz 159: parlama clip'ten ONCE — disariya tasabilmesi icin.
            .gameOuterGlow(
                accent = glowAccent,
                cornerRadius = cornerRadius,
                // Sonuk panel de artik tamamen mat degil: taban parlama 0.45,
                // vurgulu panelde 1.0'a cikiyor. Kenarlik hala BILGI tasiyor.
                intensity = if (surfaces.isLightSurface) 0f else 0.45f + emphasis.coerceIn(0f, 1f) * 0.55f
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(surfaces.bandEven, Color.White, if (surfaces.isLightSurface) 0.35f else 0.045f),
                        surfaces.panel
                    )
                )
            )
            .border(
                width = if (emphasis > 0.5f) 2.dp else 1.5.dp,
                brush = Brush.verticalGradient(listOf(borderTop, borderBottom)),
                shape = shape
            )
            .drawBehind {
                val inset = 3f * density
                // Ust ic isik cizgisi — panelin "kabarik" gorunmesini saglayan detay.
                drawLine(
                    color = Color.White.copy(alpha = if (surfaces.isLightSurface) 0.6f else 0.14f),
                    start = Offset(cornerRadius.toPx() * 0.7f, inset),
                    end = Offset(size.width - cornerRadius.toPx() * 0.7f, inset),
                    strokeWidth = 1.2f * density
                )
                // Faz 159 — IC KONTUR. Mockup'taki "cift cizgi" hissi buradan
                // geliyor: parlak dis kenarligin hemen icinde daha koyu, ince
                // bir cizgi. Kenarlik boylece bir CIZGI degil bir CERCEVE gibi
                // okunuyor.
                if (!surfaces.isLightSurface) {
                    val gap = 2.6f * density
                    drawRoundRect(
                        color = Color(0xFF060B22).copy(alpha = 0.45f),
                        topLeft = Offset(gap, gap),
                        size = Size(size.width - gap * 2f, size.height - gap * 2f),
                        cornerRadius = CornerRadius(cornerRadius.toPx() - gap),
                        style = Stroke(width = 1.4f * density)
                    )
                }
            }
            .padding(contentPadding),
        content = content
    )
}

// Grup basligi — "kart tekduzeligini kir" isteginin ritim ayagi. Ayni
// yukseklikteki satirlar arasina nefes ve anlam koyar.
@Composable
fun GameSectionHeader(
    text: String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(surfaces.accentPrimary, surfaces.accentSecondary)
                    )
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = surfaces.accentText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Ikon kutucugu
// ---------------------------------------------------------------------------

// Emoji YERINE: gradyan dolgulu yuvarlatilmis kare + ustunde parlaklik +
// icinde Material vektor glifi. `material-icons-extended` zaten bagimlilikta.
@Composable
fun GameIconTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String? = null
) {
    GameIconTile(tint = tint, modifier = modifier, size = size) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = readableOn(tint),
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

// Boyali/cizilmis ikon varligi icin: ayni kutucuk malzemesi, icinde tint
// UYGULANMAYAN bir gorsel. Cizilmis ikon seti geldikce cagiran taraf sadece
// bu asiri yuklemeye geciyor, kutucugun kendisi degismiyor.
@Composable
fun GameIconTile(
    @androidx.annotation.DrawableRes iconRes: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    contentDescription: String? = null
) {
    GameIconTile(tint = tint, modifier = modifier, size = size) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.66f)
        )
    }
}

// Ortak govde: gradyan dolgu + kenarlik + ust parlaklik.
@Composable
fun GameIconTile(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(size * 0.30f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(lerp(tint, Color.White, 0.28f), lerp(tint, Color.Black, 0.18f))
                )
            )
            .border(1.dp, lerp(tint, Color.White, 0.5f).copy(alpha = 0.7f), shape),
        contentAlignment = Alignment.Center
    ) {
        // Ust yarida ince parlaklik — kutucuk "camsi" gorunur.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size * 0.45f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
        )
        content()
    }
}

// ---------------------------------------------------------------------------
// Buton
// ---------------------------------------------------------------------------

// FAZ 168: basma yolunun `depth`e orani. 1.0 iken buton yuzu govdenin uzerine
// TAM oturuyordu ve hareket "basma" degil "kayma" olarak okunuyordu (kullanici
// geri bildirimi). 0.5'te govde basiliyken de gorunur kaliyor.
//
// Bu carpan KALINLIGI degil YOLU belirler: `depth` (varsayilan 5dp) aynen
// duruyor, yani butonun 3B govdesi hic incelmedi.
private const val PRESS_TRAVEL_FRACTION = 0.5f

@Immutable
data class GameButtonColors(
    val top: Color,
    val bottom: Color,
    val rim: Color,
    val shade: Color,
    val content: Color
)

// Renk hiyerarsisi buradan geliyor: her aksiyon KENDI renginden bir govde
// aliyor (birincil = skin accent, topla = yesil, satin al = accent, ikincil =
// notr). Ayni tonda bir yigin buton yerine renk BILGI tasiyor.
fun gameButtonColors(base: Color): GameButtonColors {
    // Oyun butonu dili: dolgu PARLAK, yazi KOYU (hedef mockup'taki altin ve
    // cyan butonlar da boyle). Koyu bir accent (ornegin PURPLE_NIGHT'in
    // #A78BFA'si ya da DEFAULT'un camgobegi) dogrudan dolgu olarak
    // kullanilirsa uzerine ne beyaz ne siyah yazi yeterli kontrast verir —
    // ikisinin de orta parlaklikta kaldigi "olu bolge" olusur.
    //
    // Cozum: dolgu once bir esigin uzerine CIKARILIYOR, sonra yazi rengi
    // dolgunun EN KOYU noktasina (bottom) gore seciliyor. Boylece butonun
    // her yerinde ayni yazi rengi okunur. Faz 146-147'deki "altin uzerine
    // altin" hatasinin sistematik panzehiri budur.
    //
    // Dolgu, EN KOYU noktasi (bottom) esigin uzerine cikana kadar
    // acilir. Dongü sabit adimli ve sinirli (en fazla 6 adim), yani
    // deterministik: ayni accent her zaman ayni butonu verir.
    // FAZ 159 — YON DEGISTI. Onceki dongu dolguyu, uzerine KOYU yazi okunacak
    // kadar ACIYORDU. Bu, esigi zar zor gecen renklerde (turuncu #FF6B35,
    // camgobegi #06B6D4) dolguyu uc-dort kez parlatip neredeyse BEYAZA
    // ceviriyordu — cihazda "SATIN AL" ve "BAŞLA" krem rengi dumduz
    // dikdortgenler olarak cikiyordu, mockup'taki doygun butonlara hic
    // benzemiyordu. (Ayni tuzak geri tusunu de beyazlatmisti; bkz.
    // `gameIconButtonColors`.)
    //
    // Yeni kural: once YAZI RENGI dolguya gore secilir, sonra dolgu o renkten
    // gerektigi KADAR uzaklastirilir. Boylece:
    //   - yesil/altin gibi zaten parlak renkler doygun kalir + koyu yazi
    //     (mockup'taki "KABUL ET" ve altin butonlar tam olarak boyle),
    //   - turuncu/camgobegi gibi orta renkler biraz KOYULASIR + beyaz yazi,
    //     yani rengini korur.
    //
    // Dongu sabit adimli ve en fazla 6 adim — deterministik.
    var fill = base
    val content = readableOn(base)
    val away = if (content == Color.White) Color.Black else Color.White
    var guard = 0
    while (contrastRatio(content, fill) < 3.2f && guard < 6) {
        fill = lerp(fill, away, 0.14f)
        guard++
    }
    return GameButtonColors(
        top = lerp(fill, Color.White, 0.18f),
        bottom = lerp(fill, Color.Black, 0.12f),
        // Kenarlik ve golge dolgudan turuyor: skin'in kimligi kenarda ve
        // altta korunuyor.
        rim = lerp(fill, Color.White, 0.55f),
        shade = lerp(base, Color.Black, 0.48f),
        content = content
    )
}

// Birincil aksiyon: skin'in accentGradient'inin iki ucunun karisimi. Renk
// hiyerarsisinin en ust basamagi — ekrandaki en agir buton bunu kullanir.
fun primaryGameButtonColors(surfaces: GameSurfaces): GameButtonColors =
    gameButtonColors(lerp(surfaces.accentPrimary, surfaces.accentSecondary, 0.35f))

// Ikincil/pasif govde: geri cekilir, ama YOK OLMAZ — kullanici neye
// basamadigini gorebilmeli (booster "karsilanamiyor" hali gibi).
/** WCAG bagil parlaklik kontrast orani (GameSurfacesSkinTest ile ayni formul). */
private fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val hi = maxOf(la, lb)
    val lo = minOf(la, lb)
    return (hi + 0.05f) / (lo + 0.05f)
}

/**
 * Istenen metin rengini dolgu uzerinde OKUNUR hale getirir.
 *
 * Faz 159'da cihazda yakalanan hata: `MissionsScreen` pasif "TOPLA" butonunun
 * yazi rengi olarak `surfaces.hairline` gonderiyordu — o bir KENARLIK tonu
 * (yari saydam koyu gri), metin rengi degil. Buton govdesi okunur hale
 * getirilince yazi govdenin icinde kayboldu.
 *
 * Cagiran tarafin niyeti korunuyor (sonuk bir yazi istiyor), ama esigin
 * altina duserse renk govdeye gore yeniden turetiliyor. Boylece HANGI renk
 * gonderilirse gonderilsin yazi okunur kalir.
 */
private fun legibleOn(fill: Color, requested: Color): Color {
    if (contrastRatio(requested, fill) >= 2.6f) return requested
    // Sonuk kalsin ama okunsun: tam beyaz/siyah degil, %62 yolda.
    return lerp(fill, readableOn(fill), 0.62f)
}

fun mutedGameButtonColors(surfaces: GameSurfaces, textColor: Color): GameButtonColors =
    GameButtonColors(
        // Faz 159: CIHAZDA GORULEN — "TOPLA" butonu neredeyse gorunmez bir
        // ana hatta donusuyordu. Hedef mockup'ta kilitli TOPLA hala acikca
        // bir BUTON: kabartmali, kenarligi belli, gri-mavi bir govde.
        //
        // Bu ayni zamanda bir UX kurali: karsilanamayan secenek GIZLENMEZ,
        // sebebiyle gosterilir. Gorunmeyen bir buton o kurali cigniyordu.
        top = lerp(surfaces.bandOdd, Color.White, if (surfaces.isLightSurface) 0f else 0.10f),
        bottom = lerp(surfaces.bandOdd, Color.Black, if (surfaces.isLightSurface) 0.06f else 0.12f),
        rim = lerp(surfaces.panelBorder, Color.White, if (surfaces.isLightSurface) 0f else 0.18f),
        shade = surfaces.sunken,
        content = legibleOn(
            lerp(surfaces.bandOdd, Color.White, if (surfaces.isLightSurface) 0f else 0.10f),
            textColor
        )
    )

// Kalin govde + kenarlik + KATI alt golge + basinca cokme.
//
// Golge Material'in yumusak elevation'i DEGIL: govdenin altinda duran katı
// bir dilim. Basinca govde o dilimin uzerine "oturuyor" (sink == depth), yani
// dokunma fiziksel bir tepki veriyor. Oyun butonu dili budur.
@Composable
fun GameButton(
    onClick: () -> Unit,
    colors: GameButtonColors,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    depth: Dp = 5.dp,
    cornerRadius: Dp = 16.dp,
    minHeight: Dp = 52.dp,
    horizontalPadding: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // Faz 160 — DOKUNMA HEDEFI SIGDIRMADAN MUAF.
    //
    // `FitToHeight` icerigi sigdirmak icin `LocalDensity`yi kucultur; bu
    // bosluk/dolgu/punto icin ISTENEN sey, ama dokunma hedefi icin DEGIL:
    // 52dp'lik bir buton %85 olcekte 44.2dp'ye duser ve 48dp kuralini
    // cignerdi. minHeight carpana BOLUNerek fiziksel yukseklik sabit
    // tutuluyor. `FitToHeight` disinda carpan 1f, yani bu satir etkisiz.
    val fitScale = LocalFitScale.current.coerceIn(0.5f, 1f)
    // FAZ 168 — BASMA HISSI YUMUSATILDI.
    //
    // Kullanici: "Basla tusunda basma hissini vermek icin yazi ve kutusuna
    // asagi kayma efekti vermissin, guzel olmus ama cok olmus. Basma hissi
    // degil ASAGI KAYMA hissi cok net, orayi biraz yumusatmak lazim."
    //
    // Teshis: yol `depth`in TAMAMI kadardi. Yani basinca butonun yuzu 3B
    // govdenin uzerine TAM oturuyor, govde tamamen kayboluyordu. Goz bunu
    // "bastim" degil "asagi kaydi" diye okuyor, cunku gercek bir tusta govde
    // hicbir zaman tamamen kapanmaz -- biraz cokup DIRENIR.
    //
    // Iki degisiklik:
    //   1. Yol `depth`in yarisi. Govde her zaman gorunur kaliyor, yani buton
    //      basiliyken de bir NESNE; kalinlik (depth) hic degismedi, sadece
    //      hareket kisaldi.
    //   2. Zamanlama `tween(60)` duz/dogrusaldi -- ani basla, ani dur, yani
    //      "kayma". Cikisi yumusayan bir egri temas hissi veriyor. Birakista
    //      biraz daha uzun: gercek bir tus geri gelirken yavaslar.
    val pressTravel = depth * PRESS_TRAVEL_FRACTION
    val sink by animateDpAsState(
        targetValue = if (pressed && enabled) pressTravel else 0.dp,
        animationSpec = tween(
            durationMillis = if (pressed && enabled) 70 else 110,
            easing = FastOutSlowInEasing
        ),
        label = "gameButtonSink"
    )
    // Faz 159: 0.6 cok saydamdi — kilitli buton zeminde eriyordu. 0.85 hala
    // "pasif" okunuyor ama buton bir NESNE olarak duruyor.
    val alpha = if (enabled) 1f else 0.85f

    Box(
        modifier = modifier
            // Faz 159: butonun cevresine dis parlama. Mockup'ta butonlar
            // zeminden "isik sacarak" ayriliyor; duz dolgu bunu vermiyordu.
            .gameOuterGlow(
                accent = colors.rim,
                cornerRadius = cornerRadius,
                // Pasif butonda da cok hafif bir kenar isigi kaliyor: nesne
                // gorunur olsun ama dikkat cekmesin.
                intensity = if (enabled) 0.75f else 0.18f,
                layers = 2
            )
            // Katı alt golge: TUM govdeyi kaplayan yuvarlak dikdortgen. Ustune
            // opak govde bindigi icin sadece alt serit gorunur; basinca govde
            // asagi inip seridi kapatir = cokme.
            .drawBehind {
                drawRoundRect(
                    color = colors.shade.copy(alpha = alpha),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 🔴 URETIMDE COKME (2026-08-29, vc19/1.2.0, S22 Ultra):
                //   java.lang.IllegalArgumentException: Padding must be non-negative
                //   at androidx.compose.foundation.layout.PaddingElement.<init>
                //
                // `sink` bir ANIMASYON degeri (animateDpAsState), `depth` ise
                // composable PARAMETRESI. Ikisi ayri kaynaklardan geliyor ve
                // ayni karede senkron olmak zorunda degil: buton basiliyken
                // yeniden bestelenip `depth` kuculurse (ya da ayni yuvada
                // farkli `depth`li bir butona donusurse), `sink` bir sure eski
                // BUYUK degerini tasir ve `depth - sink` NEGATIF olur.
                // Compose negatif padding'i istisna ile reddediyor.
                //
                // Tetikleyen senaryo: yeterli jetonla SATIN AL'a basmak —
                // satin alma aninda buton pasiflesiyor ve satir yeniden
                // besteleniyor.
                //
                // Duzeltme: iki degeri de sinirla. Gorsel davranis degismiyor
                // (normal akista zaten 0..depth araliginda), yalnizca imkansiz
                // durum guvenli hale geliyor.
                .padding(
                    // `sink` artik en fazla depth/2, yani `depth - sink` zaten
                    // pozitif. Kelepceler YINE DE duruyor: vc19'daki uretim
                    // cokmesi (`Padding must be non-negative`) tam olarak bu
                    // farkin negatife dusmesinden cikmisti ve orada da "olamaz"
                    // sanilmisti -- `sink` bir ANIMASYON degeri, `depth` bir
                    // PARAMETRE; ikisi ayri kaynaktan geliyor ve kare arasinda
                    // ayrisabiliyor.
                    top = sink.coerceIn(0.dp, depth),
                    bottom = (depth - sink).coerceAtLeast(0.dp)
                )
                // TASMA: sabit yukseklik YOK. Uzun IT/TR metni butonu uzatir,
                // kirpmaz. Alt sinir 52dp — dokunma hedefi >= 48dp garantili.
                .defaultMinSize(minHeight = minHeight / fitScale)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            lerp(colors.top, Color.White, 0.12f).copy(alpha = alpha),
                            colors.top.copy(alpha = alpha),
                            colors.bottom.copy(alpha = alpha)
                        )
                    )
                )
                .border(2.dp, colors.rim.copy(alpha = alpha * 0.9f), shape)
                // Faz 159 — UST IC PARLAKLIK SERIDI. Mockup'taki butonlarin
                // ust yarisinda cam gibi bir parlama var; duz gradyan bunu
                // vermiyordu. Serit govdenin ust ~%42'sini kapliyor ve
                // asagi dogru tamamen soniyor.
                .drawBehind {
                    val inset = 3f * density
                    val bandH = size.height * 0.42f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f * alpha),
                                Color.White.copy(alpha = 0.0f)
                            ),
                            startY = inset,
                            endY = inset + bandH
                        ),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2f, bandH),
                        cornerRadius = CornerRadius(cornerRadius.toPx() - inset)
                    )
                }
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = horizontalPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// Metin + istege bagli sol ikon iceren kisayol. Metin maxLines = 2 ve
// ortalanmis: "PREPARA IL TUO EQUIPAGGIAMENTO" gibi uzun ceviriler ikinci
// satira taser, butonu uzatir — kirpilmaz.
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    colors: GameButtonColors,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    leadingIcon: ImageVector? = null,
    depth: Dp = 5.dp,
    cornerRadius: Dp = 16.dp,
    minHeight: Dp = 52.dp,
    horizontalPadding: Dp = 16.dp,
    maxLines: Int = 2
) {
    GameButton(
        onClick = onClick,
        colors = colors,
        modifier = modifier,
        enabled = enabled,
        depth = depth,
        cornerRadius = cornerRadius,
        minHeight = minHeight,
        horizontalPadding = horizontalPadding
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = TextStyle(
                fontFamily = AppFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = colors.content,
                textAlign = TextAlign.Center
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Faz 159 — IKON butonlarinin govde rengi.
 *
 * CIHAZDA GORULEN HATA: geri tusu mockup'taki kabartmali MOR kare yerine
 * neredeyse BEYAZ cikiyordu. Sebep `gameButtonColors`'un parlatma dongusu:
 * o dongu METIN butonlari icin var — dolguyu, uzerine KOYU yazi okunacak
 * kadar acar. DEFAULT skin'in camgobegi+mor karisimi esigi gecene kadar
 * dort kez acilinca beyaza yaklasiyordu.
 *
 * Ikon butonunda o kisitlama gereksiz: ikon tek renk ve `readableOn` ile
 * dolguya gore SECILIYOR, yani koyu bir govde uzerine beyaz ikon tamamen
 * gecerli. Bu yuzden govde HAM accent'ten turuyor ve doygun kaliyor.
 */
fun gameIconButtonColors(base: Color): GameButtonColors {
    // "OLU BOLGE" TUZAGI (bu dosyada daha once de yasandi): camgobegi gibi
    // ORTA parlaklikta bir renk uzerinde ne beyaz ne siyah ikon 3.0 kontrasta
    // ulasir. `gameButtonColors` bunu dolguyu ACARAK cozer, ama o yol ikon
    // butonunu beyaza dondurup mockup'taki mor kimligi yok ediyordu.
    //
    // Buradaki cozum ters yonde: once ikon rengi secilir, sonra DOLGU o
    // renkten UZAKLASTIRILIR. Yani doygunluk korunur, sadece deger kayar.
    // Dongu sabit adimli ve en fazla 6 adim — deterministik: ayni accent her
    // zaman ayni butonu verir.
    var fill = base
    val content = readableOn(base)
    val away = if (content == Color.White) Color.Black else Color.White
    var guard = 0
    while (contrastRatio(content, fill) < 3.2f && guard < 6) {
        fill = lerp(fill, away, 0.16f)
        guard++
    }
    return GameButtonColors(
        top = lerp(fill, Color.White, 0.16f),
        bottom = lerp(fill, Color.Black, 0.14f),
        rim = lerp(fill, Color.White, 0.55f),
        shade = lerp(fill, Color.Black, 0.52f),
        content = content
    )
}

// Faz 160 — GERI TUSU HER EKRANDA AYNI MOR.
//
// Kullanicinin cihazdan teshisi: "geri tusu her ekranda farkli, hepsini mor
// yapabiliriz, ayarlar menusundeki gibi." Onceki hal govdeyi
// `surfaces.accentSecondary`den aliyordu, yani buton BULUNDUGU EKRANIN
// vurgu rengine buruniyordu: Kariyer'de turkuaz, Loadout'ta mor, Ayarlar'da
// mor.
//
// Gerekce sadece estetik degil: geri tusu bir GEZINME kontrolu, ekranin
// KONUSUNA gore anlam degistirmemeli. Sabit kalmasi onu ogrenilebilir
// yapar — oyuncu her ekranda ayni yerde AYNI seyi arar. Renk burada bilgi
// tasimiyordu, yalnizca gurultu uretiyordu.
//
// RISK — mor zeminde mor buton: sabit rengi hicbir kontrol olmadan gommek
// PURPLE_NIGHT skininde butonu zemine gomerdi. Bu yuzden renk sabit
// baslar ama zeminden AYRISANA kadar itilir (`backButtonBase`).
val BackButtonAccent = Color(0xFF8B5CF6)

/**
 * Geri tusunun govde rengi: sabit mor, ama ekran zemininden ayrisacak
 * kadar itilmis hali.
 *
 * Skin'e gore RENK degismez (kimlik sabit), yalnizca DEGER kayar — ve
 * yalnizca gerekiyorsa. `GameSurfacesSkinTest` bunu 6 skinde de dogruluyor.
 * Dongu sabit adimli ve en fazla 6 adim: deterministik.
 */
fun backButtonBase(surfaces: GameSurfaces): Color {
    val bg = surfaces.skyTop
    var c = BackButtonAccent
    val away = if (bg.luminance() > c.luminance()) Color.Black else Color.White
    var guard = 0
    // 1.9: buton bir METIN degil, parlak kenarligi ve dis parlamasi olan
    // buyuk bir NESNE — metin esigi (3.0) burada gereksiz agir olurdu.
    while (contrastRatio(c, bg) < 1.9f && guard < 6) {
        c = lerp(c, away, 0.12f)
        guard++
    }
    return c
}

@Composable
fun GameBackButton(
    onClick: () -> Unit,
    surfaces: GameSurfaces,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val colors = gameIconButtonColors(backButtonBase(surfaces))
    GameButton(
        onClick = onClick,
        colors = colors,
        modifier = modifier.width(size),
        depth = 4.dp,
        cornerRadius = 14.dp,
        minHeight = size,
        horizontalPadding = 0.dp
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = contentDescription,
            tint = colors.content,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Kapsuller (pill)
// ---------------------------------------------------------------------------

// Ciplak sayi ya da "20 🪙" gibi emoji yerine: gercek jeton varligi
// (icon_coin.webp) + kalin altin sayi, kenarlikli kapsul icinde.
@Composable
fun GameCoinPill(
    amount: String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    accent: Color = GoldPillAccent,
    iconSize: Dp = 18.dp,
    fontSize: TextUnit = 15.sp,
    prefix: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    GamePill(
        surfaces = surfaces,
        accent = accent,
        modifier = modifier,
        leading = {
            Image(
                painter = painterResource(R.drawable.icon_coin),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        },
        trailing = trailing
    ) {
        Text(
            text = if (prefix != null) "$prefix$amount" else amount,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.45f) else accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Genel ikonlu deger kapsulu (kupa + skor, yildiz + seviye, ...).
@Composable
fun GameStatPill(
    value: String,
    icon: ImageVector,
    accent: Color,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp
) {
    GamePill(
        surfaces = surfaces,
        accent = accent,
        modifier = modifier,
        leading = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.35f) else accent,
                modifier = Modifier.size(16.dp)
            )
        }
    ) {
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.45f) else accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GamePill(
    surfaces: GameSurfaces,
    accent: Color,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(surfaces.bandEven, accent, if (surfaces.isLightSurface) 0.14f else 0.20f),
                        lerp(surfaces.panel, accent, if (surfaces.isLightSurface) 0.06f else 0.08f)
                    )
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.55f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            // Dokunulabilir kapsullerde 36dp alt sinir + cevresindeki 8dp
            // bosluklar ile komsu hedeflerden ayrisiyor.
            .defaultMinSize(minHeight = if (onClick != null) 36.dp else 28.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(6.dp))
        }
        content()
        if (trailing != null) {
            Spacer(modifier = Modifier.width(6.dp))
            trailing()
        }
    }
}

// Satir ikonlarinin rengi: rolun kendi hue'su (ses mor, titresim yesil, ...)
// KORUNUR ama skin'in accent'ine dogru cekilir. Boylece ikonografi karakter
// kazanir (hepsi tek tonda degil) ama 6 skinin hicbirinde yabanci durmaz —
// skin'e ozel elle renk gomulmus olmaz.
fun roleTint(role: Color, surfaces: GameSurfaces): Color =
    lerp(role, surfaces.accentPrimary, 0.35f)

// Altin kapsullerin ortak vurgusu — NeonGold ile ayni aile ama bu dosya
// kendi kendine yeter, tema sabitine bagli degil.
val GoldPillAccent = Color(0xFFFACC15)

// ---------------------------------------------------------------------------
// Kaydirma ipucu
// ---------------------------------------------------------------------------

// Kullanici daha once "kaydirilabilir oldugunu anlayamadim" demisti. Liste
// dibinde yumusakca yanip sonen chevron, ek bir metin/ceviri gerektirmeden
// "asagida devami var" der.
@Composable
fun BoxScope.GameScrollHint(
    visible: Boolean,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(),
        label = "scrollHintAlpha"
    )
    if (alpha <= 0.01f) return
    Box(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 2.dp)
            .size(34.dp)
            .clip(RoundedCornerShape(50))
            .background(surfaces.panel.copy(alpha = 0.85f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = surfaces.accentText.copy(alpha = alpha),
            modifier = Modifier.size(24.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Liste bantlamasi
// ---------------------------------------------------------------------------

// Rakipte liste satirlari donusumlu krem/beyaz bantlar halinde. Ayni fikrin
// koyu zeminli karsiligi: cift satirlar kart tonu, tek satirlar cardAlt'a
// dogru kaydirilmis ton. cardAlt koyu paletlerde daha ACIK, acik palette daha
// KOYU — fark iki yonde de olusuyor, elle dallanmaya gerek yok.
fun Modifier.gameBanding(index: Int, surfaces: GameSurfaces): Modifier =
    this.background(if (index % 2 == 0) surfaces.bandEven else surfaces.bandOdd)

// Satir ayiricisi.
@Composable
fun GameDivider(surfaces: GameSurfaces, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, surfaces.hairline, Color.Transparent)
                )
            )
    )
}

// Panel yuzeyine ince ic golge cizen yardimci (raylar, oyuk alanlar icin).
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawInnerWell(
    color: Color,
    cornerRadius: Float,
    borderColor: Color
) {
    drawRoundRect(
        brush = SolidColor(color),
        cornerRadius = CornerRadius(cornerRadius),
        size = Size(size.width, size.height)
    )
    drawRoundRect(
        color = borderColor,
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 1f * density)
    )
}
