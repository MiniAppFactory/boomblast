package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.readableOn
import androidx.compose.ui.platform.LocalDensity

// Faz 158 — paylasilan oyun arayuz kiti (2. parca).
//
// GameComponents.kt "malzeme"yi (baslik, panel, buton, kapsul) tanimliyor;
// bu dosya o malzemeden kurulan daha buyuk parcalari: neon kart, madalyon,
// sayfa noktalari, secilebilir satir, wordmark, ust bar tusu.
//
// Hepsi `GameSurfaces` uzerinden calisiyor — yani hicbiri skin'e ozel renk
// gommuyor, 6 skin de ayni malzemeyi kendi accentGradient'iyle aliyor.

// Parlayan kenarlikli kart. Kenarlik DURUM tasir: `accent` kartin konusunun
// rengi, `glow` ne kadar "aktif" oldugu. Sonuk kart ile parlayan kart
// arasindaki fark boylece renkten DEGIL, isik miktarindan da okunur —
// "renk tek ayrim kanali olamaz" kuralinin karsiligi.
@Composable
fun NeonCard(
    surfaces: GameSurfaces,
    accent: Color,
    modifier: Modifier = Modifier,
    glow: Float = 1f,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    // FAZ 162 — MADDE 4: KENARLIK HALESININ GUCU.
    //
    // 1f = mod secim kartlarinin MEVCUT gorunumu. O kartlarda kenarlik mod
    // rengini tasiyor ve kullanici begendi — varsayilan bu yuzden 1f ve bu
    // deger eski davranisla birebir ayni cikti verir.
    //
    // 1f uzeri: hale daha genise yayilir ve icte ikinci bir kontur belirir
    // ("cam tup" hissi). Tek renkli, buyuk kartlar (onboarding gibi) icin —
    // orada kenarlik durum degil KIMLIK tasidigi icin daha guclu olabilir.
    //
    // Kenarlik hala DURUM tasiyor: `glow` parametresi (aktif/pasif) her iki
    // hale gucunde de calismaya devam eder, bloom sadece tavani yukseltir.
    bloom: Float = 1f,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val g = glow.coerceIn(0f, 1f)
    val b = bloom.coerceIn(1f, 3f)
    Column(
        modifier = modifier
            // Faz 159: neon bloom — clip'ten ONCE, disariya tasabilmesi icin.
            .gameOuterGlow(
                accent = accent,
                cornerRadius = cornerRadius,
                intensity = if (surfaces.isLightSurface) 0f else 0.40f + g * 0.60f,
                // bloom 1f -> 3 katman x 2.2dp (ESKI DAVRANIS, birebir).
                // bloom 2f -> 12 katman x 1.1dp: ayni toplam yayilim
                // civari, ama konturlar ortustugu icin ayri halkalar
                // yerine surekli bir hale okunuyor (onboarding kartinda
                // cihazda goruldu, bkz. OnboardingScreen aciklamasi).
                layers = (3f + (b - 1f) * 9f).roundToInt().coerceIn(3, 21),
                spreadStepDp = 2.2f / b,
                coreAlpha = 0.30f * b
            )
            .clip(shape)
            // FAZ 162 — MADDE 2: TONLAMA KATSAYILARI HEDEFTEN ACIK KALIYORDU.
            //
            // CIHAZDA OLCULDU: Kariyer kartinin govdesi #163C58 idi —
            // DESIGN_SPEC'in verdigi #0E1835'ten hem daha acik hem de
            // camgobegine kaymis. Sebep gradyanin UST ucu: `bandEven` %24
            // oraninda mod accent'ine cekiliyordu, yani kartin en genis
            // yuzeyi zeminden cok accent'ten renk aliyordu.
            //
            // Katsayilar dusuruldu (0.24 -> 0.16, 0.08 -> 0.05). Kartin MOD
            // KIMLIGI kaybolmuyor: o kimligi zaten kenarlik, dis hale,
            // madalyon ve baslik tasiyor — govde ise spec'in istedigi derin
            // laciverde donuyor ve dort kart tek malzeme gibi okunuyor.
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(surfaces.bandEven, accent, (if (surfaces.isLightSurface) 0.10f else 0.16f) * g),
                        lerp(surfaces.panel, accent, (if (surfaces.isLightSurface) 0.04f else 0.05f) * g),
                        surfaces.panel
                    )
                )
            )
            .border(
                width = if (g > 0.5f) 2.dp else 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        lerp(surfaces.panelBorder, lerp(accent, Color.White, 0.45f), g),
                        lerp(surfaces.panelBorder, accent, g),
                        lerp(surfaces.panelBorder, accent.copy(alpha = 0.5f), g)
                    )
                ),
                shape = shape
            )
            // IC KONTUR — mockup'taki "cift cizgi" hissi: parlak dis
            // kenarligin hemen icinde daha ince, daha acik ikinci bir cizgi.
            // Kenarlik boylece bir CIZGI degil bir CERCEVE gibi okunuyor.
            // (GamePanel Faz 159'da ayni cozumu almisti; NeonCard'a simdi
            // geliyor.) YALNIZCA guclu halede — mod secim kartlarinin
            // gorunumu birebir korunsun diye.
            .then(
                if (b > 1f && !surfaces.isLightSurface) {
                    Modifier.drawBehind {
                        val inset = 3.5f * density
                        drawRoundRect(
                            color = lerp(accent, Color.White, 0.55f)
                                .copy(alpha = 0.14f + 0.24f * g),
                            topLeft = Offset(inset, inset),
                            size = Size(
                                size.width - inset * 2f,
                                size.height - inset * 2f
                            ),
                            cornerRadius = CornerRadius(cornerRadius.toPx() - inset),
                            style = Stroke(width = 1.2f * density)
                        )
                    }
                } else {
                    Modifier
                }
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content
    )
}

// Dairesel ikon madalyonu: parlayan halka + gradyan dolgu + sembol.
// Her kart kendi renginde madalyon tasir.
// Faz 159 — madalyon YASSI degil. Kullanicinin teshisi: "hedefte parlayan
// halka + gradyan disk, su an mat disk + gri halka" (ozellikle onboarding
// kartlarindaki turuncu/sari/yesil daireler).
//
// Uc duzeltme:
//   1. Disk gradyani artik MERKEZDEN degil UST-SOLDAN baslar — isik yukaridan
//      geliyor, disk kure gibi okunuyor.
//   2. Halka tek renk degil: ustte parlak, altta koyu (dikey gradyan kenarlik).
//   3. Halkanin disinda katmanli parlama (gameOuterGlow ile ayni yaklasim;
//      Modifier.blur API 31+ oldugu icin kullanilamiyor).
@Composable
fun IconMedallion(
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    dimmed: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    // Faz 166 -- BIRIM HATASI DUZELTMESI.
    //
    // Asagidaki radyal gradyan `size.value`yi (Dp'nin ham sayisi, ornegin 56)
    // dogrudan `Brush.radialGradient`e veriyordu; oysa oradaki `center` ve
    // `radius` PIKSEL bekler. 20 satir yukaridaki `drawBehind` blogu ayni
    // olcuyu `size.toPx()` ile DOGRU kullaniyor, yani hata ayni composable
    // icinde tutarsizdi.
    //
    // Sonuc: isik kaynagi cihaz yogunluguyla KAYIYORDU. Tasarimin yapildigi
    // xxhdpi'de (density 3) merkez dairenin ust-solunda; density 1.5'te ayni
    // ham sayilar merkezin SAGINA dusuyordu. Madalyon Gorevler ekraninda
    // kosulsuz ciziliyor, yani her kullanici goruyor.
    //
    // Carpanlar density 3'teki GORUNUMU birebir korumak icin secildi:
    //   0.9 / 3 = 0.30,  0.7 / 3 = 0.233,  2.2 / 3 = 0.733
    val medallionPx = with(LocalDensity.current) { size.toPx() }
    Box(
        modifier = modifier
            .size(size)
            // Dis parlama — clip'ten ONCE, disariya tasabilmesi icin.
            .drawBehind {
                if (dimmed) return@drawBehind
                val r = size.toPx() / 2f
                for (layer in 1..3) {
                    val spread = layer * 2.4f * density
                    drawCircle(
                        color = accent.copy(alpha = 0.26f / layer),
                        radius = r + spread,
                        style = Stroke(width = 2f * density)
                    )
                }
            }
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        lerp(accent, Color.White, if (dimmed) 0.10f else 0.42f)
                            .copy(alpha = if (dimmed) 0.16f else 0.72f),
                        accent.copy(alpha = if (dimmed) 0.10f else 0.42f),
                        lerp(accent, Color.Black, 0.45f)
                            .copy(alpha = if (dimmed) 0.05f else 0.30f)
                    ),
                    // Isik kaynagi ust-sol: dairenin merkezi degil.
                    center = Offset(medallionPx * 0.30f, medallionPx * 0.233f),
                    radius = medallionPx * 0.733f
                )
            )
            .border(
                width = 2.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        lerp(accent, Color.White, if (dimmed) 0.10f else 0.55f)
                            .copy(alpha = if (dimmed) 0.35f else 1f),
                        accent.copy(alpha = if (dimmed) 0.30f else 0.90f),
                        lerp(accent, Color.Black, 0.40f)
                            .copy(alpha = if (dimmed) 0.30f else 0.85f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// Sayfa noktalari — aktif olan daha buyuk ve parlak.
@Composable
fun PageDots(
    count: Int,
    selectedIndex: Int,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(if (active) 10.dp else 7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (active) surfaces.accentPrimary else surfaces.panelBorder)
            )
        }
    }
}

// Secilebilir satir: secili olan PARLAYAN kenarlik + onay ikonu alir,
// secili olmayan sonuk kalir. Onay ikonu ikinci ayrim kanali (sekil) —
// renk korlugunde de secim okunur.
@Composable
fun GameSelectableRow(
    selected: Boolean,
    onClick: () -> Unit,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    accent: Color = surfaces.accentPrimary,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(
                        listOf(
                            lerp(surfaces.bandEven, accent, if (surfaces.isLightSurface) 0.14f else 0.24f),
                            surfaces.panel
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(surfaces.bandEven, surfaces.panel))
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else surfaces.panelBorder,
                shape = shape
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            // TASMA: yukseklik sabit degil, alt sinir var. Uzun dil/tema adlari
            // satiri uzatir, kirpmaz. 48dp dokunma hedefi de buradan geliyor.
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(10.dp))
        }
        content()
        if (trailing != null) {
            trailing()
        }
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Faz 159 — wordmark IKI SATIR. Kullanicinin teyidi: "menudeki Kaboom Blocks
// yazisi iki satirdi, senin fontu daha iyiydi." Ust satir ALTIN "Kaboom",
// alt satir skin accent'inde "Blocks".
//
// Malzeme `GameEmblem` ile ORTAK: ekran basliklariyla (AYARLAR, HAFTALIK
// GOREVLER) birebir ayni kontur/gradyan/ic isik/golge receti. Fark yalnizca
// metin ve hizalama.
//
// Gorsel VARLIK DEGIL, metin: 5 dilde de ayni kalir, fontScale ile olceklenir.
//
// Faz 161 — WORDMARK TEK BLOK.
//
// Kullanicinin teshisi: "logoyu kucucuk birakmissin, Kaboom'un yazi
// renklerinin alakasi yok, aralarina kocaman bosluk birakmissin Kaboom ve
// Blocks yazisi arasinda."
//
// Ucu de ayni yerden cikiyordu: iki satir arasindaki bosluk (yazi tipinin
// ascent/descent payi) punto ile birlikte BUYUYOR. Puntoyu buyutmek bosluğu
// da buyuttu, blok parcalandi ve gradyan buyuk yuzeyde yikanmis gorundu.
//
// Cozum boyut degil ISCILIK: `tightLines` ile satir kutulari harfe oturuyor,
// iki satir tek logo blogu gibi okunuyor. Wordmark ust barda, uygulama
// ikonunun YANINDA, KOMPAKT duruyor — mockup'ta oyle.
@Composable
fun GameWordmark(
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    // Ust barda ikon/jeton kapsulu ile yan yana dururken parilti o yogun
    // satiri okunmaz hale getiriyordu; varsayilan kapali.
    sparkle: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    GameEmblem(
        text = "Kaboom Blocks",
        surfaces = surfaces,
        modifier = modifier,
        fontSize = fontSize,
        textAlign = textAlign,
        allowSplit = true,
        sparkle = sparkle,
        // Marka blogu HER EKRANDA sikisik: menude de, Sartlar ekraninda da
        // tek bir logo olarak okunmali.
        tightLines = true,
        // Hedef gorselde "Kaboom" 165'te bitip "Blocks" 165'te basliyor —
        // satirlar BITISIK. Yazi tipinin satir kutusu punto'nun ~1.5 kati
        // oldugu icin fazlalik buradan geri aliniyor. Punto ile olcekleniyor,
        // yani her boyutta ayni sikilik.
        lineSpacing = -(fontSize.value * 0.34f).dp
    )
}

/**
 * Faz 161 — EKRAN ZEMININDE OKUNMASI GEREKEN METIN.
 *
 * Kullanicinin teshisi: "'Bir oyun modu sec' yazisi beyaz degil mesela,
 * gozukmuyor bu sekilde." O metin `accentText` (camgobegi) idi; ekran zemini
 * de ayni renk ailesinden turuyor (`skyTop` accent'e dogru cekiliyor), yani
 * metin zemine gomuluyordu.
 *
 * ILKE: vurgu rengi KIMLIK ve DURUM tasir (mod rengi, secili hal, kenarlik).
 * OKUNMASI gereken metin notr ve yuksek kontrastli olur. Ikisi karistiginda
 * kaybeden hep metin tarafi oluyor.
 *
 * Sabit beyaz GOMULMUYOR: DEFAULT skin'in ACIK temasinda beyaz kaybolurdu.
 * Renk `readableOn` ile ZEMINDEN turuyor — koyu gokte beyaz, acik gokte
 * neredeyse siyah. 6 skin x acik/koyu icin `ModeSelectLayoutTest` tarar.
 */
fun screenBodyTextColor(surfaces: GameSurfaces, alpha: Float = 1f): Color =
    readableOn(surfaces.skyTop).copy(alpha = alpha)

// Ust bardaki ikon dugmesi: koyu yuvarlatilmis kare + ince accent kenarlik.
// Stok IconButton'un gorunmez dairesi yerine gorunur bir "tus".
@Composable
fun GameIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    accent: Color = surfaces.accentPrimary,
    size: Dp = 40.dp
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(lerp(surfaces.bandEven, accent, 0.18f), surfaces.panel)
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (surfaces.isLightSurface) {
                lerp(accent, Color.Black, 0.32f)
            } else {
                lerp(accent, Color.White, 0.35f)
            },
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * `GameIconButton`in GORSEL VARLIK alan hali.
 *
 * Tus govdesi (koyu yuvarlatilmis kare + accent kenarlik) BIREBIR ayni;
 * degisen yalnizca icerik: Material vektor yerine kendi hacmini ve
 * parlamasini tasiyan bir varlik.
 *
 * TINT YOK. Varliklar kendi golge/parlama katmanlarini tasiyor; `ColorFilter`
 * uygulamak onlari duz siluete cevirirdi.
 */
@Composable
fun GameImageIconButton(
    @androidx.annotation.DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    accent: Color = surfaces.accentPrimary,
    size: Dp = 40.dp
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(lerp(surfaces.bandEven, accent, 0.18f), surfaces.panel)
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

// Ekran basligi satiri: geri tusu + baslik + istege bagli sag aksiyonlar.
// Tek yerde tanimli oldugu icin Ayarlar/Gorevler/Loadout ayni ritmi paylasir.
@Composable
fun GameScreenHeader(
    title: String,
    surfaces: GameSurfaces,
    onBack: () -> Unit,
    backDescription: String,
    modifier: Modifier = Modifier,
    backTestTag: String = "",
    titleFontSize: TextUnit = 24.sp,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    // Faz 159 — BASLIK EKRANIN TAM ORTASINDA.
    //
    // CIHAZDA YAKALANAN HATA (Fransizca ekranda goruldu): baslik ortali
    // degildi, saga kaymisti. Sebep duzendi — baslik geri butonuyla AYNI
    // Row icindeydi ve `weight(1f)` ile KALAN alani aliyordu; yani ekranin
    // degil, geri butonundan ARTA KALAN bosluğun ortasina hizalaniyordu.
    // Kayma miktari tam olarak geri butonunun genisligi kadardi. Kisa
    // basliklarda goze batmiyor, "PARAMÈTRES" / "MISSIONS HEBDOMADAIRES"
    // gibi uzun karsiliklarda belli oluyordu.
    //
    // Cozum Row degil BOX: baslik tum genislige gore ortalanir, geri butonu
    // ve aksiyonlar onun USTUNE bindirilir — genislik TUKETMEZLER.
    //
    // Row + weight ile ya da saga gorunmez bosluk koyarak da "duzeltilebilirdi"
    // ama ikisi de kirilgan: saga ikinci bir buton eklendigi gun ortalama
    // yeniden bozulurdu.
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // BINDIRME CAKISMASI: baslik cok uzunsa geri butonunun altina
        // girebilir. Iki yana da ESIT pay birakiliyor (geri butonu 44dp +
        // 12dp bosluk) — esit olmasi sart, aksi halde ortalama yine bozulur.
        // Pay icinde kalan uzun basliklar `GameEmblem` tarafindan once iki
        // satira bolunur, sonra gerekirse punto kuculur; KESILMEZ.
        GameTitle(
            text = title,
            surfaces = surfaces,
            fontSize = titleFontSize,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp)
        )
        GameBackButton(
            onClick = onBack,
            surfaces = surfaces,
            contentDescription = backDescription,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .then(
                    if (backTestTag.isNotEmpty()) Modifier.testTag(backTestTag) else Modifier
                )
        )
        if (actions != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/**
 * FAZ 186 — CHROME VARLIKLARI ICIN ORTAK CIZICI.
 *
 * Harita ekranlari (Faz 174) jeton/kupa/ayarlar/geri ogelerini RASTER
 * varliklardan ciziyor; ana menu ve oyun ekrani ise ayni ogeleri Compose'da
 * uretiyordu. Kullanici ikisinin AYNI olmasini istedi, o yuzden cizim tek
 * yere alindi.
 *
 * Cagiran taraf GOVDE olcusunu verir; isima payi varliklarin normalize
 * edilmis govde/bitmap oranindan (0.806, bkz.
 * tools/wordart/normalize_chrome.py) eklenir. Boylece ayni `bodyWidth`
 * degeri her ekranda AYNI fiziksel boyu ve ayni dokunma hedefini uretir.
 */
const val CHROME_BODY_RATIO = 0.806f

@Composable
fun ChromeAssetButton(
    resId: Int,
    bodyWidth: Dp,
    aspect: Float,
    contentDescription: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val w = bodyWidth / CHROME_BODY_RATIO
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .width(w)
            .height(w * aspect)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}

