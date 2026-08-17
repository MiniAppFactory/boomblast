package com.miniappfactory.boomblocks.ui.games.boomblocks

import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.painterResource
import com.miniappfactory.boomblocks.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.miniappfactory.boomblocks.ui.settings.SettingsScreen
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.BlockBlue
import com.miniappfactory.boomblocks.ui.theme.BlockGreen
import com.miniappfactory.boomblocks.ui.theme.BlockOrange
import com.miniappfactory.boomblocks.ui.theme.BlockPink
import com.miniappfactory.boomblocks.ui.theme.BlockPurple
import com.miniappfactory.boomblocks.ui.theme.BlockYellow
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonMagenta
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.BoosterType
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.data.EffectIntensity
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.utils.HapticManager
import com.miniappfactory.boomblocks.utils.SoundManager
import com.miniappfactory.boomblocks.utils.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import android.view.Choreographer

data class BlockShape(
    val id: Int,
    val pattern: List<List<Boolean>>,
    val colorIndex: Int
)

// Sonsuz Mod'da "reklamla devam et" icin — tahtayi ve skoru bir onceki
// hamleye geri saracak sekilde saklanan anlik goruntu.
data class GameSnapshot(
    val board: List<Int>,
    val score: Int
)

// Faz 107: patlama parcaciklari. Onceden `data class` + `List.map` ile her
// patlamada YENIDEN uretiliyordu ve hepsi TEK bir `progress` paylasiyordu —
// yani hepsi ayni anda dogup ayni anda oluyordu, duz cizgide, yercekimsiz.
// Artik: sabit boyutlu bir HAVUZ (kare basi tahsis yok), parcacik basina
// dogum gecikmesi/omur/boyut/donme, ve yukari yonelimli basit fizik.
//
// KRITIK BIRIM NOTU: tum konum/hiz/boyut degerleri HUCRE birimindedir
// (1.0 = bir grid hucresi), ham piksel DEGIL. Eski kod `radius = 4f + ...`
// yaziyordu; DrawScope'ta bu ham pikseldir, S8'in 3.0 yogunlugunda ~1.3dp
// eder — parcaciklar fiilen gorunmuyordu. Hucre birimi kullanmak ayrica
// tablet/telefon ve (banner reklam yuklenince kuculen) grid'de ayni hissi verir.
const val MAX_BLAST_PARTICLES = 96
// Parcacik penceresi: en gec dogan (300ms) + en uzun omur (600ms) = 900ms.
const val PARTICLE_WINDOW_MS = 950
const val PARTICLE_WINDOW_S = PARTICLE_WINDOW_MS / 1000f
// Hucre/saniye^2. Parcaciklar yukari firlar, tepe noktasina ~0.44sn'de varir
// ve dusmeye baslar — referanstaki "kucuk kupler huzme icinde yukari suzuluyor".
const val PARTICLE_GRAVITY = 8f

const val PK_DIAMOND = 0
const val PK_CIRCLE = 1
const val PK_STREAK = 2
const val PK_MOTE = 3
const val PK_STAR = 4

class BlastParticle {
    var kind = PK_DIAMOND
    var x = 0f          // hucre birimi
    var y = 0f
    var vx = 0f         // hucre / saniye
    var vy = 0f
    var rot = 0f        // derece
    var spin = 0f       // derece / saniye
    var size = 0f       // hucre birimi
    var delay = 0f      // saniye
    var life = 0f       // saniye
    var color = Color.White
    var active = false  // Faz 5: ParticlePool'da aktiflik durumu
}

// Faz 107: karakterli egriler. Duz `FastOutSlowInEasing` her asamayi ayni
// hissettiriyordu; asagidakiler olculmus referans degerleri.
// easeOutBack = %10 overshoot.
val EaseOutBackBoom = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f)
val EaseInCubicBoom = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)
val EaseOutCubicBoom = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
val EaseOutQuartBoom = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1f)

// Faz 50: patlama noktasinda yukari suzulup solan "+N" puan yazisi (Block
// Blast referansindan — kullanici "kayittan izle" dedi, orada her patlamada
// tam o hucrede kucuk bir sayi beliriyor, bizde SADECE merkezi tek bir
// banner metni vardi).
data class ScorePopup(
    val row: Float,
    val col: Float,
    val text: String,
    val color: Color
)

// ---------------------------------------------------------------------------
// Faz 1 (performans): alpha'si CIZIM FAZINDA okunan yuvarlak kenarlik.
//
// Sorun: `Modifier.border(w, NeonGold.copy(alpha = pulse), shape)` yazildiginda
// `pulse` KOMPOZISYON fazinda okunur. Deger `infiniteRepeatable` ile saniyede 60
// kez degistigi icin bu modifier zincirini iceren en yakin restart scope da
// saniyede 60 kez yeniden derlenir. Bu dosyada o scope grid Card'inin content
// lambda'siydi -> tahta boşken bile 64 hucrelik alt agac + overlay Canvas'lar +
// kombo banner her karede yeniden derleniyordu (bkz. docs/PERF_BASELINE.md,
// "Slow issue draw commands 704").
//
// `Modifier.border` bir `Brush` alir ama `Brush` **sealed** oldugu icin (Compose
// 1.7) kendi "gec baglanan" Brush'imizi yazamiyoruz. Bu yuzden kenarligi kendimiz
// ciziyoruz — ancak GEOMETRIYI uydurmadan: asagidaki hesap, Compose Foundation
// 1.7.2 `BorderModifierNode.drawWithCacheModifierNode` + `drawRoundRectBorder`
// bytecode'undan birebir cikarildi:
//
//   strokeWidthPx = min(ceil(width.toPx()), ceil(size.minDimension / 2f))
//   fillArea      = strokeWidthPx * 2 > size.minDimension
//   halfStroke    = strokeWidthPx / 2
//   topLeft       = Offset(halfStroke, halfStroke)
//   borderSize    = Size(width - strokeWidthPx, height - strokeWidthPx)
//   cornerRadius  = max(0f, r - halfStroke)            // BorderKt.shrink
//   style         = Stroke(strokeWidthPx)
//
// Cizim de `border` ile ayni sirada: once `drawContent()`, sonra stroke.
// Rengi `SolidColor` brush + `alpha` parametresi ureter; `SolidColor.applyTo`
// (bytecode) `p.color = value.copy(alpha = value.alpha * alpha)` yaptigi icin
// opak bir taban renkle sonuc `color.copy(alpha = a)` ile BIT-BIREBIR aynidir.
//
// Compose'un ucuncu bir dali daha var (cornerRadius.x < halfStroke -> clipRect
// difference + dolu roundRect). Bu dosyadaki tum cagrilar icin ulasilamaz:
// r = 6dp veya 12dp, halfStroke <= (2*d + 1)/2 = d + 0.5 piksel, ve
// 6*d >= d + 0.5 esitsizligi d >= 0.1 icin her zaman dogru.
private fun Modifier.drawPhaseRoundedBorder(
    width: Dp,
    radius: Dp,
    brush: Brush,
    alpha: () -> Float
): Modifier = this.drawWithCache {
    val strokeWidthPx = min(ceil(width.toPx()), ceil(size.minDimension / 2f))
    val r = radius.toPx()
    when {
        strokeWidthPx <= 0f || size.minDimension <= 0f -> onDrawWithContent { drawContent() }
        strokeWidthPx * 2 > size.minDimension -> {
            val corner = androidx.compose.ui.geometry.CornerRadius(r, r)
            onDrawWithContent {
                drawContent()
                drawRoundRect(brush = brush, cornerRadius = corner, alpha = alpha())
            }
        }
        else -> {
            val halfStroke = strokeWidthPx / 2f
            val topLeft = Offset(halfStroke, halfStroke)
            val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
            val shrunk = (r - halfStroke).coerceAtLeast(0f)
            val corner = androidx.compose.ui.geometry.CornerRadius(shrunk, shrunk)
            val stroke = Stroke(strokeWidthPx)
            onDrawWithContent {
                drawContent()
                drawRoundRect(
                    brush = brush,
                    topLeft = topLeft,
                    size = borderSize,
                    cornerRadius = corner,
                    alpha = alpha(),
                    style = stroke
                )
            }
        }
    }
}

// Faz 22: seviye tamamlanma anina ozel, tam ekran konfeti — grid'e gore konumlanan
// BlastParticle'dan farkli olarak ekran genisligine (0f..1f) gore konumlanir.
data class ConfettiPiece(
    val xFraction: Float,
    val startDelay: Float,
    val drift: Float,
    val rotationSpeed: Float,
    val color: Color
)

val BLOCK_COLORS = listOf(
    BlockOrange,
    BlockBlue,
    BlockGreen,
    BlockPink,
    BlockYellow,
    BlockPurple
)

val SHAPE_PATTERNS = listOf(
    // 1x1 Single
    listOf(listOf(true)),
    // 2x1 Line
    listOf(listOf(true, true)),
    // 1x2 Line
    listOf(listOf(true), listOf(true)),
    // 3x1 Line
    listOf(listOf(true, true, true)),
    // 1x3 Line
    listOf(listOf(true), listOf(true), listOf(true)),
    // 2x2 Square
    listOf(listOf(true, true), listOf(true, true)),
    // --- Kose ailesi (3 hucre, 2x2 kutu) — 4 yonun TAMAMI (Faz 107) ---
    // L Shape 1
    listOf(listOf(true, false), listOf(true, true)),
    // L Shape 2
    listOf(listOf(true, true), listOf(false, true)),
    // Faz 107: kose parcasinin 4 donme varyantindan yalnizca 2'si tanimliydi;
    // oyunda dondurme olmadigi icin diger 2 yon HIC cikmiyordu.
    // Kose 3 (L1'in 90°'si)
    listOf(listOf(true, true), listOf(true, false)),
    // Kose 4 (L2'nin 90°'si)
    listOf(listOf(false, true), listOf(true, true)),
    // --- T ailesi — 4 yonun TAMAMI (Faz 107) ---
    // T Shape
    listOf(listOf(true, true, true), listOf(false, true, false)),
    // T 90° (sag)
    listOf(listOf(false, true), listOf(true, true), listOf(false, true)),
    // T 180° (ters T)
    listOf(listOf(false, true, false), listOf(true, true, true)),
    // T 270° (sol)
    listOf(listOf(true, false), listOf(true, true), listOf(true, false)),
    // Faz 61: 3x3 buyuk kare kullanici istegiyle TAMAMEN KALDIRILDI (tek
    // hamlede tahtanin buyuk bir kismini kaplayip manevra alanini asiri
    // daraltiyordu). SHAPE_PATTERNS/SHAPE_WEIGHTS/SHAPE_WEIGHTS_ENDLESS
    // ucunden de kaldirildi, indeksler kaymadi cunku bu ucu de listenin AYNI
    // sirasinda birlikte tutuluyor.
    // Faz 46: kullanici "4'lü de olmali yoksa 4'lü kombo olmaz" dedi — en uzun
    // duz parca onceden 3'tu, coklu-satir kombolarini kurmayi zorlastiriyordu.
    // 4x1 Line
    listOf(listOf(true, true, true, true)),
    // 1x4 Line
    listOf(listOf(true), listOf(true), listOf(true), listOf(true)),
    // Faz 48: kullanici "büyük L de yok (3 yanyana + 2 tane altına devam),
    // 2x3'lü küp de yok" dedi — L1/L2 zaten vardi ama sadece kucuk (2x2) kose
    // parcalariydi, kullanicinin tarif ettigi BUYUK L (5 hucreli, Tetris-L
    // benzeri) ve dikdortgen bloklar eksikti.
    // --- Buyuk L ailesi (5 hucre, 3x3 kutu) — 4 yonun TAMAMI (Faz 107) ---
    // Big L (5 cells): ustte 3 yanyana, sol sutunda 2 tane daha asagi
    listOf(listOf(true, true, true), listOf(true, false, false), listOf(true, false, false)),
    // Buyuk L 90°
    listOf(listOf(true, true, true), listOf(false, false, true), listOf(false, false, true)),
    // Buyuk L 180°
    listOf(listOf(false, false, true), listOf(false, false, true), listOf(true, true, true)),
    // Buyuk L 270°
    listOf(listOf(true, false, false), listOf(true, false, false), listOf(true, true, true)),
    // 2x3 Rectangle
    listOf(listOf(true, true, true), listOf(true, true, true)),
    // 3x2 Rectangle
    listOf(listOf(true, true), listOf(true, true), listOf(true, true)),
    // Faz 48: kullanici "block blast ile mekanik olarak karsilastir" dedi —
    // arastirma (Wikipedia Tetromino, tetris.wiki) standart 7 parcalik tetromino
    // setini (I/O/T/L/J/S/Z) dogruladi. Bizde S/Z (capraz/skew) hic yoktu, L/J
    // de sadece kucuk 2x2 kose parcasi olarak vardi — standart 4 hucreli
    // (3-uzunlugunda + 1 dik) L/J eksikti. Hepsi eklendi.
    // --- S ailesi — 2 yonun TAMAMI (Faz 107; S/Z'nin yalnizca 2 farkli donusu var) ---
    // S-tetromino
    listOf(listOf(false, true, true), listOf(true, true, false)),
    // S-tetromino dikey
    listOf(listOf(true, false), listOf(true, true), listOf(false, true)),
    // --- Z ailesi — 2 yonun TAMAMI (Faz 107) ---
    // Z-tetromino
    listOf(listOf(true, true, false), listOf(false, true, true)),
    // Z-tetromino dikey
    listOf(listOf(false, true), listOf(true, true), listOf(true, false)),
    // --- L-tetromino ailesi — 4 yonun TAMAMI (Faz 107) ---
    // Standart L-tetromino (4 hucre)
    listOf(listOf(true, false), listOf(true, false), listOf(true, true)),
    // L-tetromino 90°
    listOf(listOf(true, true, true), listOf(true, false, false)),
    // L-tetromino 180°
    listOf(listOf(true, true), listOf(false, true), listOf(false, true)),
    // L-tetromino 270°
    listOf(listOf(false, false, true), listOf(true, true, true)),
    // --- J-tetromino ailesi — 4 yonun TAMAMI (Faz 107) ---
    // Standart J-tetromino (4 hucre)
    listOf(listOf(false, true), listOf(false, true), listOf(true, true)),
    // J-tetromino 90°
    listOf(listOf(true, false, false), listOf(true, true, true)),
    // J-tetromino 180°
    listOf(listOf(true, true), listOf(true, false), listOf(true, false)),
    // J-tetromino 270°
    listOf(listOf(true, true, true), listOf(false, false, true))
)

// SHAPE_PATTERNS ile ayni sirada agirliklar — duz uniform-random secim, 1x1/2'li
// parcalarin (listede ayri ayri 3 madde olarak sayildiklari icin) orantisiz sik
// gelmesine yol aciyordu (kullanici geri bildirimi: "çok fazla tek ve ikili
// geliyor, bu kolaylaştırıyor"). Kucuk parcalar hala cikabilir (stratejik bosluk
// doldurma icin gerekli) ama artik daha seyrek, buyuk/karmasik sekiller daha sik.
// Faz 55: kullanici "3x3'un ve tumunun acilmasi cok fena, bir anda manevra
// alani kalmiyor" dedi — T-shape'ten itibaren (seviye 9+ havuzu) TUM parcalar
// kucuk parcalarla (agirlik 1-3) ayni buyuklukte agirlik (2-3) tasiyordu.
// Havuz 8'den 19'a cikinca (Faz 55'in kademeli-acilim duzeltmesiyle bile)
// bu buyuk/karmasik parcalarin toplam cekilis payi >%50'ye firliyordu. Artik
// tumu (T-shape dahil) agirlik 1 — hala cikabilirler ama NADIR, kucuk/kolay
// parcalar oranı korunuyor, oyuncunun her zaman "manevra alani" kalıyor.
// Faz 107: eksik donme yonleri eklenince (18 -> 34 parca) agirliklar TAMAMEN
// yeniden olceklendi. Naif yaklasim — her yeni yone kardesiyle ayni agirligi
// vermek — zor parcalarin toplam payini %33'ten %48'e cikarirdi: T ailesi 1
// yonden 4'e cikinca payi 4 KATINA firlar, Buyuk L ve L/J tetrominolari da
// oyle. Bu tam olarak Faz 55'te duzeltilen "seviye 9'da bir anda zorlasiyor"
// sikayetinin geri gelmesi demekti.
//
// Cozum: tum agirliklar x4 olceklendi ve her AILENIN toplam payi Faz 55'teki
// haliyle birebir korunup aile ICINDE yonlere esit bolundu. Ornek: T ailesi
// once 1/30 pay aliyordu, simdi 4 yon x agirlik 1 = 4/120 — pay ayni, ama
// artik 4 farkli yonle geliyor. Yani cesitlilik artti, zorluk sabit kaldi.
// Toplam: 120 (eski 30'un tam 4 kati).
val SHAPE_WEIGHTS = listOf(
    4,  // 1x1                      | aile 4
    8,  // 2x1                      \
    8,  // 1x2                      / aile 16
    12, // 3x1                      \
    12, // 1x3                      / aile 24
    12, // 2x2                      | aile 12
    6,  // Kose L1                  \
    6,  // Kose L2                   \
    6,  // Kose 3   (Faz 107)        / aile 24
    6,  // Kose 4   (Faz 107)       /
    1,  // T                        \
    1,  // T  90°   (Faz 107)        \
    1,  // T 180°   (Faz 107)        / aile 4
    1,  // T 270°   (Faz 107)       /
    4,  // 4x1                      \
    4,  // 1x4                      / aile 8
    1,  // Buyuk L                  \
    1,  // Buyuk L  90° (Faz 107)    \
    1,  // Buyuk L 180° (Faz 107)    / aile 4
    1,  // Buyuk L 270° (Faz 107)   /
    4,  // 2x3                      \
    4,  // 3x2                      / aile 8
    2,  // S                        \
    2,  // S dikey  (Faz 107)       / aile 4
    2,  // Z                        \
    2,  // Z dikey  (Faz 107)       / aile 4
    1,  // L-tetromino              \
    1,  // L-tet  90°   (Faz 107)    \
    1,  // L-tet 180°   (Faz 107)    / aile 4
    1,  // L-tet 270°   (Faz 107)   /
    1,  // J-tetromino              \
    1,  // J-tet  90°   (Faz 107)    \
    1,  // J-tet 180°   (Faz 107)    / aile 4
    1   // J-tet 270°   (Faz 107)   /
)

// Faz 57: kullanici "sonsuz modda hic zorlasmiyor, hep en fazla 2x2 geliyor"
// dedi. Kok neden: Sonsuz Mod, Seviyeli Mod ile AYNI SHAPE_WEIGHTS tablosunu
// kullaniyordu — Faz 55'te buyuk parcalarin agirligi (T-shape dahil) bilerek
// 1'e dusurulmustu (Seviyeli Mod'da "level 9 aniden zorlasiyor" sikayetini
// cozmek icin), ama bu nadirlik yanlislikla Sonsuz Mod'u da etkiledi. Sonsuz
// Mod hedefsiz/surekli oldugu icin (Seviyeli Mod'daki gibi sabit bir hedefe
// yetismesi gerekmiyor) buyuk parcalarin daha sik gorunmesi sorun degil —
// ayri bir tablo ile buyuk parcalarin agirligi 2'ye cikarildi (kucuk
// parcalarla ayni degil, ama Seviyeli Mod'daki 1'in iki kati).
// Faz 107: SHAPE_WEIGHTS ile ayni x4 olcekleme ve aile-payi koruma mantigi
// (bkz. yukaridaki not). Toplam: 160 (eski 40'in tam 4 kati).
val SHAPE_WEIGHTS_ENDLESS = listOf(
    4,  // 1x1                      | aile 4
    8, 8,      // 2x1, 1x2          | aile 16
    12, 12,    // 3x1, 1x3          | aile 24
    12,        // 2x2               | aile 12
    6, 6, 6, 6, // Kose x4          | aile 24
    2, 2, 2, 2, // T x4             | aile 8
    8, 8,      // 4x1, 1x4          | aile 16
    2, 2, 2, 2, // Buyuk L x4       | aile 8
    8, 8,      // 2x3, 3x2          | aile 16
    4, 4,      // S x2              | aile 8
    4, 4,      // Z x2              | aile 8
    2, 2, 2, 2, // L-tetromino x4   | aile 8
    2, 2, 2, 2  // J-tetromino x4   | aile 8
)

// Faz 79: Pro Mode — kullanici "puan degil parca zorlugu yuksek olmali,
// 1x1 hic olmamali" dedi. 1x1'in agirligi 0 (matematiksel olarak asla
// secilmez, availableCount kapsaminda olsa bile), buyuk/karmasik parcalar
// Sonsuz Mod'dan da agir.
// Faz 107: SHAPE_WEIGHTS ile ayni x4 olcekleme ve aile-payi koruma mantigi
// (bkz. yukaridaki not). Toplam: 168 (eski 42'nin tam 4 kati). 1x1 yine 0.
val SHAPE_WEIGHTS_CHALLENGE = listOf(
    0,  // 1x1 — ASLA gelmez       | aile 0
    4, 4,      // 2x1, 1x2          | aile 8
    8, 8,      // 3x1, 1x3          | aile 16
    8,         // 2x2               | aile 8
    4, 4, 4, 4, // Kose x4          | aile 16
    3, 3, 3, 3, // T x4             | aile 12
    12, 12,    // 4x1, 1x4          | aile 24
    3, 3, 3, 3, // Buyuk L x4       | aile 12
    12, 12,    // 2x3, 3x2          | aile 24
    6, 6,      // S x2              | aile 12
    6, 6,      // Z x2              | aile 12
    3, 3, 3, 3, // L-tetromino x4   | aile 12
    3, 3, 3, 3  // J-tetromino x4   | aile 12
)

// Faz 107: havuz "SHAPE_PATTERNS'in ilk N parcasi" olarak aciliyor. Yonler
// eklenince N'in bir AILE SINIRINDA durmasi sart oldu — aksi halde oyuncuya
// ornegin T'nin 4 yonunden 2'si acilirdi, ki bu tamamen keyfi bir durum olurdu
// ("neden ters T geliyor ama yan T gelmiyor?"). Asagidaki kumulatif sinirlar
// listenin aile sonlarina denk gelir:
//   1 · 3 · 5 · 6 · 10 · 14 · 16 · 20 · 22 · 24 · 26 · 30 · 34
// Tum acilim tablolari bu degerlerden secilir; ara bir sayi KULLANILMAZ.
private val CAREER_POOL_STEPS = listOf(14, 16, 20, 22, 24, 26, 30, 34)
private val PRO_POOL_STEPS = listOf(10, 16, 22, 26, 30)
private val ENDLESS_POOL_STEPS = listOf(6, 10, 16, 22, 26, 30)

data class BlockThemeOption(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val titleIt: String,
    val titleFr: String,
    val titleEs: String,
    val icon: String,
    val descriptionTr: String,
    val descriptionEn: String,
    val descriptionIt: String,
    val descriptionFr: String,
    val descriptionEs: String
) {
    fun title(language: AppLanguage): String =
        language.pick(tr = titleTr, en = titleEn, it = titleIt, fr = titleFr, es = titleEs)

    fun description(language: AppLanguage): String =
        language.pick(tr = descriptionTr, en = descriptionEn, it = descriptionIt, fr = descriptionFr, es = descriptionEs)
}

val BLOCK_THEMES = listOf(
    BlockThemeOption(
        id = "CLASSIC",
        titleTr = "Klasik 3D Kabartma",
        titleEn = "Classic 3D Bevel",
        titleIt = "Rilievo 3D Classico",
        titleFr = "Relief 3D Classique",
        titleEs = "Relieve 3D Clásico",
        icon = "🧊",
        descriptionTr = "3D kabartmalı kristal bloklar",
        descriptionEn = "3D embossed crystal blocks",
        descriptionIt = "Blocchi di cristallo 3D in rilievo",
        descriptionFr = "Blocs de cristal 3D en relief",
        descriptionEs = "Bloques de cristal 3D en relieve"
    ),
    BlockThemeOption(
        id = "FRUIT",
        titleTr = "Meyve Küpleri",
        titleEn = "Fruit Cubes",
        titleIt = "Cubi di Frutta",
        titleFr = "Cubes de Fruits",
        titleEs = "Cubos de Fruta",
        icon = "🍉",
        descriptionTr = "Karpuz, peynir, çilek ve portakal",
        descriptionEn = "Watermelon, cheese, strawberry, orange",
        descriptionIt = "Anguria, formaggio, fragola e arancia",
        descriptionFr = "Pastèque, fromage, fraise et orange",
        descriptionEs = "Sandía, queso, fresa y naranja"
    ),
    BlockThemeOption(
        id = "SWEETS",
        titleTr = "Şekerleme & Tatlı",
        titleEn = "Sweets & Donuts",
        titleIt = "Dolci & Ciambelle",
        titleFr = "Bonbons & Donuts",
        titleEs = "Dulces y Donuts",
        icon = "🍩",
        descriptionTr = "Donut, çikolata, bisküvi ve şeker",
        descriptionEn = "Donut, chocolate, cookie, candy",
        descriptionIt = "Donut, cioccolato, biscotto e caramella",
        descriptionFr = "Donut, chocolat, biscuit et bonbon",
        descriptionEs = "Donut, chocolate, galleta y caramelo"
    )
    // Not: "MIXED" (zar) temasi kaldirildi — Unicode zar yuzu karakterleri
    // (⚀-⚅) bazi cihazlarda/fontlarda hic render olmuyordu (kullanici geri
    // bildirimi: "hiç gözükmüyor"). EmbossedBlockCell'deki "MIXED" case'i de
    // kaldirildi, DataStore'da hala "MIXED" kayitli olan cihazlar guvenli
    // sekilde duz/klasik renkli kupe (emoji'siz) dusuyor.
)

// Faz 34/35: kombo kademesi basina TEK sabit kelime yerine kucuk bir esanlamli
// havuzdan rastgele secim yapmak icin — ayni kombo seviyesinde bile her
// seferinde ayni kelime gelmesin diye. Faz 35'te 5 dile cikarildi.
private fun praiseFrom(
    language: AppLanguage,
    trPool: List<String>,
    enPool: List<String>,
    itPool: List<String>,
    frPool: List<String>,
    esPool: List<String>
): String = when (language) {
    AppLanguage.TR -> trPool.random()
    AppLanguage.EN -> enPool.random()
    AppLanguage.IT -> itPool.random()
    AppLanguage.FR -> frPool.random()
    AppLanguage.ES -> esPool.random()
}

@Composable
fun BlastTheBlocksGame(
    levelNumber: Int,
    targetScore: Int,
    shapePoolTier: Int = 3,
    // Faz 77: Pro Mode "daha yuksek puan carpani" — 1f=degisiklik yok
    // (Level/Sonsuz Mod), Pro Mode LevelDefinition.scoreMultiplier'i buraya
    // gecirir.
    scoreMultiplier: Float = 1f,
    // Faz 79: Pro Mode — parca havuzu farkli (daha zor, 1x1 yok) ve oyun-bitti
    // modalinda ekstra bir "YENIDEN BAŞLAT" secenegi var. Faz 96: can sistemi
    // (bypass edilebiliyordu) kaldirildi — "Yeniden Başlat" artik esikli
    // interstitial reklama bagli (onProModeRestart).
    isChallengeMode: Boolean = false,
    // Faz 96: "Yeniden Başlat" (Pro Mode) ve rewarded-hakki-tukenmis "TEKRAR
    // DENE" (Level+Pro ortak) artik interstitial reklam gosterip ardindan
    // onProceed() cagiran bir sarmalayicidan geciyor — reklam basarisiz olsa
    // (no-fill) bile onProceed HER ZAMAN cagrilir (InterstitialAdManager'in
    // mevcut davranisi), oyuncu asla kilitlenmez.
    onProModeRestart: (onProceed: () -> Unit) -> Unit = { it() },
    onRetryInterstitial: (onProceed: () -> Unit) -> Unit = { it() },
    currentTheme: String = "CLASSIC",
    language: AppLanguage = AppLanguage.TR,
    soundEnabled: Boolean = true,
    // Faz 105: coklu patlama titresimi. Bu composable'i Kariyer, Pro ve Sonsuz
    // modlarinin UCU de kullaniyor — tek baglanti noktasi ucunu birden kapsiyor.
    hapticsEnabled: Boolean = true,
    darkMode: Boolean = true,
    isEndless: Boolean = false,
    bestScore: Int = 0,
    initialBoosterCounts: Map<BoosterType, Int> = emptyMap(),
    onSelectTheme: (String) -> Unit = {},
    onUseBooster: (BoosterType) -> Unit = {},
    // Faz 94: sadece Sonsuz Mod'da dolu geçirilir — coin'den bagimsiz, reklam
    // izleyerek anlik +1 booster alma. onGranted, bu composable'in local
    // availableBoosterCounts state'ini ANINDA gunceller (kalici DataStore
    // yazma islemi cagiran tarafta/AppNavigation'da ayrica yapılır).
    onWatchAdForBooster: ((BoosterType, onGranted: () -> Unit) -> Unit)? = null,
    onLinesCleared: (count: Int) -> Unit = {},
    // Faz 73: tek hamlede 2+ satir/sutun patlatinca ("Coklu Patlama" haftalik
    // gorevi icin) tetiklenir — onLinesCleared'dan AYRI, cunku onLinesCleared
    // 1 satir patlasa bile cagriliyor, bu ise sadece coklu patlamada.
    onMultiClear: () -> Unit = {},
    onBack: () -> Unit,
    // Faz 115e: SKOR 0 iken bedelsiz cikis.
    //
    // Kullanicinin koydugu kural: "eger henuz score sifirsa bu dogru yaklasim
    // ama sifirdan buyukse reklam girmeli". Yani oyuncu daha hicbir sey
    // oynamadan cikiyorsa reklam gostermek hem gereksiz hem rahatsiz edici;
    // ama oynanmis bir tahtayi terk edip taze baslamak "bedava reset"tir ve
    // bedeli olmalidir.
    //
    // Verilmezse onBack'e duser, yani eski davranis (her cikista reklam).
    onBackWithoutAd: (() -> Unit)? = null,
    // Ayarlar navController.navigate() ile ayri bir hedefe gitmiyor — Navigation
    // Compose ekrandan ayrilinca bu composable'i komposizyondan atip geri donunce
    // yeniden olusturuyordu, bu da tum remember durumunu (tahta/skor/tepsi)
    // sifirliyordu (kullanici geri bildirimi: "renk/dil degistirirsen oyun sifirlar").
    // Ayarlar artik AYNI composable icinde bir overlay olarak gosteriliyor, oyun
    // ekranindan hic ayrilinmiyor.
    musicEnabled: Boolean = true,
    soundVolume: Float = 0.5f,
    onToggleSound: (Boolean) -> Unit = {},
    onSoundVolumeChange: (Float) -> Unit = {},
    onToggleMusic: (Boolean) -> Unit = {},
    onToggleHaptics: (Boolean) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit = {},
    uiSkin: BlastSkin = BlastSkin.DEFAULT,
    onSelectSkin: (BlastSkin) -> Unit = {},
    notificationsEnabled: Boolean = true,
    onToggleNotifications: (Boolean) -> Unit = {},
    onLevelComplete: (score: Int, stars: Int) -> Unit = { _, _ -> },
    // Faz 39: "DEVAM ET" butonuna basinca cagrilir — geri tepe uc bardaki/fiziksel
    // geri tusundaki onBack'ten AYRI tutuluyor ki AppNavigation araya (her 2
    // bolumde bir) zorunlu interstitial reklami sokabilsin, oyun ici geri
    // tusuna dokunmadan.
    onLevelCompleteContinue: () -> Unit = onBack,
    onLevelFailed: (score: Int) -> Unit = {},
    onEndlessGameOver: (score: Int) -> Unit = {},
    onRequestContinueAd: (onGranted: () -> Unit, onDenied: () -> Unit) -> Unit = { _, onDenied -> onDenied() },
    // Faz 22: ilk gercek hamleden once bir kez gosterilen rehber ipucu — bu bayrak
    // kalici (DataStore) oldugu icin varsayilan deger `true` (ipucu KAPALI), aksi
    // halde onizleme/test cagrilarinda yanlislikla her zaman gosterilir.
    hasMadeFirstMove: Boolean = true,
    onFirstMoveMade: () -> Unit = {},
    // Faz 4: Ambient toz + radyal isin etki yogunlugu (Düşük/Normal/Yüksek).
    // Yuksek yogunlukta daha cok parca, daha sering yenileme; zayif cihazlar icin opsiyonel.
    effectIntensity: EffectIntensity = EffectIntensity.NORMAL
) {
    val palette = blastPalette(uiSkin, darkMode)
    // Faz 22: skin/koyu-mod degisiminde renkler ONCEDEN tek karede sertce
    // degisiyordu (Ayarlar overlay'inden secim yapinca aninda). En gorunur iki
    // yuzeyde (kok zemin + ana kart) yumusak bir crossfade uygulanir.
    val animatedBackground by animateColorAsState(palette.background, animationSpec = tween(400), label = "bgCrossfade")
    val animatedCard by animateColorAsState(palette.card, animationSpec = tween(400), label = "cardCrossfade")
    // Kullanici geri bildirimi: "oyun duz siyah ekranda, hic cezbedici degil" —
    // kok zemin artik TEK DUZ RENK degil, mevcut skin'in accentGradient
    // renklerine dogru cok hafif (yaklasik %12-%18) karisan yumusak bir dikey
    // gradyan. lerp() DUZ RENK uretir (alfa/seffaflik yok), boylece hem koyu
    // hem acik modda (BlastLightPalette/BlastDarkPalette) board/tray okunabilirligi
    // bozulmadan zemine skin'in rengini hissettiren bir derinlik katilir.
    val backgroundAccentTop = uiSkin.accentGradient[0]
    val backgroundAccentBottom = uiSkin.accentGradient.getOrElse(1) { uiSkin.accentGradient[0] }
    val animatedBackgroundBrush = Brush.verticalGradient(
        colors = listOf(
            lerp(animatedBackground, backgroundAccentTop, 0.16f),
            animatedBackground,
            lerp(animatedBackground, backgroundAccentBottom, 0.12f)
        )
    )
    val gridSize = 8
    val board = remember { mutableStateListOf<Int>().apply { repeat(gridSize * gridSize) { add(0) } } }
    var score by remember { mutableIntStateOf(0) }
    // Sonsuz Mod'da "reklamla devam et" icin son birkac hamlenin anlik goruntusu —
    // en fazla MAX_MOVE_HISTORY kadar tutulur, "3 hamle geri al" bunu kullanir.
    val moveHistory = remember { mutableStateListOf<GameSnapshot>() }
    val MAX_MOVE_HISTORY = 5
    // Faz 101: Sonsuz Mod'da parca havuzunun genisligi skordan turetiliyor
    // (bkz. generateNewTray). "Reklam izle, devam et" 3 hamle geri alirken
    // skoru da geri aliyor (undoMovesForContinue) — oyuncu 100'luk bandin
    // altina duserse havuz GERCEKTEN 2 parca daraliyordu, yani reklam
    // izlemenin cezasi oyunun basitlesmesi oluyordu. Bu deger oturumda
    // ULASILAN EN YUKSEK skoru tutar ve asla azalmaz; havuz genisligi
    // bundan hesaplanir, boylece geri alma havuzu kucultmez.
    var peakScore by remember { mutableIntStateOf(0) }
    var comboCount by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isLevelComplete by remember { mutableStateOf(false) }
    // Faz 107b: kullanici bildirdi — "son patlamayi goremeden ekrana Level Complete
    // cikiyor". Kok neden: `isLevelComplete` placeShape icinde SENKRON set ediliyor
    // (t=0), oysa patlama ritueli (beklenti 400-620ms + flash + huzme) async bir
    // coroutine'de daha yeni basliyor. Yani bolumu bitiren patlama HIC gorulmuyordu.
    // Cozum: mantik aynen kaliyor (ilerleme aninda kaydediliyor), sadece DIYALOGUN
    // gorunurlugu ritual bitene kadar geciktiriliyor.
    var levelCompleteRevealed by remember { mutableStateOf(false) }
    var lastClearedText by remember { mutableStateOf("") }
    var lastClearWasCelebration by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    // Faz 38: kullanici "sonsuz oyunda geri tusuna basinca hemen ana menuye
    // cikmasin, teyit alsin" dedi — Sonsuz Mod'da hem sistem geri tusu/jesti
    // hem oyun ici geri oku artik dogrudan cikmiyor, once bu onay ekranini aciyor.
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var recentlyClearedCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val clearFlashAlpha = remember { Animatable(0f) }
    // Faz 21: cizgi tamamlanir tamamlanmaz, hucreler HALA DOLUYKEN kisa bir
    // "patlamaya kilitlendi" glow'u gosteriliyor (Block Blast referansi: neon
    // renkli kenarlik + sparkle) — gercek temizleme bu glow'dan hemen sonra
    // gerceklesiyor, boylece oyuncu "bu satirlar patlayacak" anini gorebiliyor.
    var glowingClearCells by remember { mutableStateOf<Set<Int>>(emptySet()) }
    // Faz 50: kullanicinin istegiyle (gercek Block Blast'in kaydini izleyip
    // karsilastirdik) — onceden glow SADECE hucre-hucre uygulaniyordu, rakipte
    // tum satir/sutunun etrafinda TEK bir parlak cerceve var. Ayrica
    // rakipte her patlamada tam o noktada yuzen bir "+N" puan yazisi cikiyor,
    // bizde skor bonusu sadece merkezi tek bir banner metniydi.
    var glowingRows by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var glowingCols by remember { mutableStateOf<Set<Int>>(emptySet()) }
    // Faz 106 (TODO 12): patlamayi TETIKLEYEN parcanin rengi. Onceden patlama
    // hep sabit NeonGold + beyaz + gokkusagi sweep ile ciziliyordu, yani tahtada
    // hangi renk parca oynanirsa oynansin patlama hep ayni goruluyordu. Artik
    // satir/sutun patlarken karisik renklerinden cikip TEK renge — o hamlede
    // yerlestirilen parcanin rengine — donuyor: "bunu ben patlattim" baglantisi
    // gorsel olarak kuruluyor (Block Blast referansi, kullanici istegi).
    var clearAccentColor by remember { mutableStateOf(NeonGold) }
    // Faz 106 (TODO 12): patlama aninda satir/sutun boyunca disa acilip sonen
    // sok dalgasi. glowingRows/Cols patlamadan HEMEN once bosaltildigi icin ayri
    // durum tutuluyor — dalga patlamadan SONRA devam ediyor.
    var shockRows by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var shockCols by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val shockProgress = remember { Animatable(0f) }
    // Faz 107: hucreler yok olduktan SONRA yerinde kalan isik huzmesi. Referans
    // olcumu: huzme ~690ms yasiyor ve patlamanin asil GOVDESI o — hucrelerin
    // yok olusu (~110ms) sadece tetik. Iki asamali: once dar/keskin/beyaz
    // cekirdek, sonra genisleyen yumusak altin bant.
    var beamRows by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var beamCols by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val beamProgress = remember { Animatable(0f) }
    var scorePopups by remember { mutableStateOf<List<ScorePopup>>(emptyList()) }
    val popupProgress = remember { Animatable(0f) }

    // Faz 115c — HATA DUZELTMESI (kullanici cihazda buldu: "bir satir patlayinca
    // aldigin puan ekranda takili kaliyor bir sonraki patlamaya kadar").
    //
    // Kok sebep: `popupProgress` yaratiliyordu ve cizim tarafinda okunuyordu, ama
    // ONU SUREN HICBIR KOD YOKTU — tum dosyada yalnizca iki kullanimi vardi:
    // tanim (burasi) ve `riseFraction = popupProgress.value` okumasi. `animateTo`
    // hic cagrilmiyordu. Sonuc: deger sonsuza kadar 0f, dolayisiyla
    //     riseFraction = 0  ->  popupAlpha = 1f - 0 = 1f
    // yani "+12" yazisi TAM OPAKLIKTA, hic yukselmeden ve hic solmadan asili
    // kaliyordu; ancak bir sonraki patlama `scorePopups` listesini degistirince
    // yer degistiriyordu. Animasyonu suren kod bir noktada kaldirilmis, cizim
    // yolu yerinde birakilmis.
    //
    // Duzeltme: liste doldugunda animasyonu bastan baslat, bitince listeyi
    // TEMIZLE. Listenin temizlenmesi onemli — yoksa yazi %0 alfa ile de olsa
    // kompozisyonda kalirdi.
    LaunchedEffect(scorePopups) {
        if (scorePopups.isNotEmpty()) {
            popupProgress.snapTo(0f)
            popupProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
            scorePopups = emptyList()
        }
    }

    // Faz 4/110c: Radyal isin flash KAPATILDI (havaifişek lazer ışınları)
    // Kullanıcı isteği: animation ağırlığı seçeneği kaldırıldı, Effect Intensity yaşamadı
    var radialFlash by remember { mutableStateOf<RadialFlashState?>(null) }
    val radialFlashDurationMs = 350
    val radialBeamCount = 0  // Hep kapalı

    // Faz 38: sistem geri tusu/jesti artik dogrudan menuye cikmiyor — once
    // "cikmak istedigine emin misin" onay ekranini aciyor (kullanici geri
    // bildirimi: yanlislikla cikip skoru kaybetme riski). Onceden SADECE
    // Sonsuz Mod'da vardi (kullanicinin ilk istegi acikca "sonsuz oyunda"
    // diyordu) — Faz 47'de kullanici "level modunda geri tuşu direkt atıyor,
    // çıkmak istiyor musunuz diye sormuyor" dedi, Seviyeli Mod'a da acildi.
    BackHandler(enabled = !isGameOver && !isLevelComplete) {
        showExitConfirmDialog = true
    }

    val glowPulse = remember { Animatable(0f) }
    // Faz 22: skor degisince anlik ziplama yerine eski degerden yeniye SAYARAK
    // artiyor + kisa bir "pop" (kullanicinin degerlendirmesi: rakip oyunlarda
    // standart olan bu geri bildirim bizde yoktu).
    val animatedScore by animateIntAsState(targetValue = score, animationSpec = tween(450), label = "scoreCountUp")
    val scoreScale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        scoreScale.snapTo(1.2f)
        scoreScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    // Faz 22: paylasilan, TEK infiniteTransition kaynaklari — her hucreye/karta
    // AYRI bir animasyon dongusu acmak (64 hucre + N kart) performans sorunu
    // yaratir, bunun yerine birkac paylasilan faz degeri hesaplanip asagi aktarilir.
    // Faz 1 (performans): bu ucu de `infiniteRepeatable`, yani HIC durmuyorlar.
    // Artik `by` ile degil, State nesnesi olarak tutuluyorlar — degerleri sadece
    // cizim fazinda (Canvas draw lambda'si, Brush.applyTo, graphicsLayer bloğu)
    // `.value` ile okunur. Kompozisyon fazinda okunurlarsa en yakin restart scope
    // saniyede 60 kez yeniden derlenir; bu dosyada o scope grid Card'inin content
    // lambda'siydi (64 hucre + overlay'ler).
    val sharedPulseState = rememberInfiniteTransition(label = "sharedPulse").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sharedPulseAnim"
    )
    val ambientPhaseState = rememberInfiniteTransition(label = "ambientPhase").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Restart),
        label = "ambientPhaseAnim"
    )
    val shimmerPhaseState = rememberInfiniteTransition(label = "shimmerPhase").animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerPhaseAnim"
    )

    // Faz 1 (performans): `drawPhaseRoundedBorder` icin SABIT taban brush'lar.
    // Renk/gradyan degismiyor — sadece alpha animasyonlu, o da cizim fazinda
    // uygulaniyor. `remember` ediliyorlar ki modifier element esitligi bozulmasin
    // (aksi halde her kompozisyonda drawWithCache onbellegi bosuna yenilenir) ve
    // gradyan shader'i bir kez kurulsun (eskiden her karede yeni bir
    // `Brush.linearGradient` nesnesi uretiliyordu).
    val nearMissBorderBrush = remember { SolidColor(NeonGold) }
    val streakCardBorderBrush = remember {
        Brush.linearGradient(listOf(Color(0xFFFF6B35), NeonGold))
    }
    val clearGlowBorderBrush = remember { SolidColor(Color.White) }

    // Faz 22: bir satir/sutunda TAM 1 hucre bos kalinca, o satirdaki dolu
    // hucreler hafifce nabiz atarak "bir hamle kaldi" hissi verir (Woodoku/
    // Blockudoku referansi).
    val nearMissCells = remember(board.toList()) {
        val cells = mutableSetOf<Int>()
        for (r in 0 until gridSize) {
            var filled = 0
            for (c in 0 until gridSize) if (board[r * gridSize + c] != 0) filled++
            if (filled == gridSize - 1) {
                for (c in 0 until gridSize) if (board[r * gridSize + c] != 0) cells.add(r * gridSize + c)
            }
        }
        for (c in 0 until gridSize) {
            var filled = 0
            for (r in 0 until gridSize) if (board[r * gridSize + c] != 0) filled++
            if (filled == gridSize - 1) {
                for (r in 0 until gridSize) if (board[r * gridSize + c] != 0) cells.add(r * gridSize + c)
            }
        }
        cells
    }

    // Faz 22: oturum ici basari rozetleri — kalici degil (DataStore'a yazilmiyor),
    // sadece bu oyun oturumu icinde bir kez gosterilir. Kalici bir basari sistemi
    // (yeni DataStore alanlari + tum ekranlara akis) kapsam disi birakildi.
    var sessionLinesCleared by remember { mutableIntStateOf(0) }
    val unlockedSessionAchievements = remember { mutableStateListOf<String>() }
    var activeAchievementText by remember { mutableStateOf<String?>(null) }
    fun maybeUnlockAchievement(id: String, text: String) {
        if (!unlockedSessionAchievements.contains(id)) {
            unlockedSessionAchievements.add(id)
            activeAchievementText = text
        }
    }

    // Faz 22: ilk gercek hamleden once tepsinin altinda kisa bir rehber ipucu —
    // ilk basarili yerlestirmede kalici olarak kapatilir (onFirstMoveMade).
    var showFirstMoveHint by remember { mutableStateOf(!hasMadeFirstMove) }

    var showContinueDialog by remember { mutableStateOf(false) }
    // Faz 97: kullanici "reklam izle devam et 1 kez değil 3 kez olsun, rewarded
    // admob gelir modeli daha iyi" dedi — Seviyeli+Pro Mode'daki bu hak artik
    // Sonsuz Mod'daki endlessContinuesUsed/maxEndlessContinues ile AYNI desende
    // bir sayac (eskiden tek kullanimlik bir boolean'di).
    var continuesUsedInAttempt by remember { mutableStateOf(0) }
    val maxRetryContinues = 3
    var isRequestingContinueAd by remember { mutableStateOf(false) }
    // Faz 90: kullanici Sonsuz Mod'da "reklam izle devam et" hakkinin oturum
    // basina SADECE 1 degil 4 kez sunulmasini istedi. Sonsuz Mod'a ozel ayri
    // bir sayac (yukaridaki continuesUsedInAttempt Seviyeli/Pro Mode'a ozel).
    var endlessContinuesUsed by remember { mutableStateOf(0) }
    val maxEndlessContinues = 4
    val shakeOffset = remember { Animatable(0f) }
    val comboTextScale = remember { Animatable(1f) }
    val comboTextAlpha = remember { Animatable(1f) }

    // Faz 115j — HATA DUZELTMESI (kullanici cihazda buldu, ekran goruntusuyle
    // dogruladi: "GÜZEL! +12" yazisi ekranda takili kaliyor).
    //
    // Bu, `scorePopups`/`popupProgress` (Faz 115c'de duzeltilen, satirin
    // KENDI uzerinde yuzen "+N" yazisi) ile KARISTIRILMAMALI — tamamen ayri
    // bir mekanizma: `lastClearedText` ("Combo Text Banner", tahtanin USTUNDE
    // ortalanmis "GÜZEL!"/"SÜPER!"/"PATLAMA!" + puan). Kok sebep FARKLI: bu
    // metnin HICBIR zaman-tabanli temizleme kodu yoktu — yalnizca (a) tam
    // sifirlamada veya (b) OYUNCU PATLAMASIZ BIR HAMLE YAPINCA ("else" dali,
    // asagida `lastClearedText = ""`) temizleniyordu. Yani patlamadan sonra
    // oyuncu bosta bir hamle yapmadigi surece yazi SONSUZA KADAR asili
    // kaliyordu — ekran goruntusundeki "SKOR 211" altindaki "GÜZEL! +12"
    // tam bu.
    //
    // Duzeltme: banner gorununce bir sure sonra KENDILIGINDEN solup kaybolur,
    // oyuncunun ayrica bosta hamle yapmasina gerek kalmaz. `lastClearedText`
    // her degistiginde (yeni bir patlama METNI degismis demektir, ayni yazi
    // tekrar atansa bile State esitligi sayesinde LaunchedEffect yeniden
    // TETIKLENMEZ — bu KASITLI, ayni metin ust uste gelirse suresi uzamaz).
    LaunchedEffect(lastClearedText) {
        if (lastClearedText.isNotEmpty()) {
            comboTextAlpha.snapTo(1f)
            delay(900)
            comboTextAlpha.animateTo(0f, animationSpec = tween(300, easing = LinearEasing))
            lastClearedText = ""
        }
    }
    // Faz 107: sabit boyutlu parcacik HAVUZU — patlama basina `List.map` ile
    // yeni nesne uretmek yerine ayni nesneler yeniden doldurulur (GC duraklamasi
    // Faz 5: ParticlePool obje havuzuna geçiş. Eski: Array + activeParticles sayacı.
    // Yeni: ParticlePool sınıfı tüm durumu yönetiyor.
    val particlePool = remember { ParticlePool() }

    // Faz 5: Kullanılmayan (Choreographer callback'inde local değişken)
    // var activeParticles by remember { mutableIntStateOf(0) }
    val particleProgress = remember { Animatable(0f) }
    // 4 uclu yildiz — birim koordinatlarda BIR KEZ kurulur, her parcacik icin
    // sadece transform uygulanir (sicak dongude Path tahsisi yok).
    val sparkStarPath = remember {
        Path().apply {
            moveTo(0f, -1f); lineTo(0.24f, -0.24f); lineTo(1f, 0f); lineTo(0.24f, 0.24f)
            lineTo(0f, 1f); lineTo(-0.24f, 0.24f); lineTo(-1f, 0f); lineTo(-0.24f, -0.24f)
            close()
        }
    }
    // Faz 22: seviye tamamlanma kutlamasi — onceden bu an gorsel olarak sadece
    // skor/yildiz sayilarindan ibaretti (tasarim onerisi: rakip oyunlarda en
    // yuksek motivasyon aninin ozel bir gorseli olmaliydi).
    var confettiPieces by remember { mutableStateOf<List<ConfettiPiece>>(emptyList()) }
    val confettiProgress = remember { Animatable(0f) }
    var armedBooster by remember { mutableStateOf<BoosterType?>(null) }
    val availableBoosterCounts = remember {
        mutableStateMapOf<BoosterType, Int>().apply { putAll(initialBoosterCounts) }
    }

    val trayShapes = remember { mutableStateListOf<BlockShape?>() }

    // Her yeni parcaya GERCEKTEN benzersiz bir id vermek icin sayac — sadece tepsi
    // pozisyonunu (0/1/2) kullanmak KRITIK bir bug'a yol aciyordu: pointerInput(shape?.id)
    // yalnizca id DEGISTIGINDE surukleme algilayicisini yeniden baslatir, ama id her
    // zaman ayni slot pozisyonuna esitse (id=index), tepsi yenilenince ayni id tekrar
    // kullanilir ve Compose bunu "degisiklik yok" sanip ESKI/BAYAT parca referansiyla
    // calismaya devam eder — kullanici "3'lu koydum 9'lu ciktu" gibi somut, tekrarlanabilir
    // hatalar bildirdi. Artik her BlockShape'e global, hic tekrar etmeyen bir id veriliyor.
    var nextShapeId by remember { mutableIntStateOf(0) }

    // Sürükle-bırak durumu: tepsideki hangi parça sürükleniyor, parmağın ekran üzerindeki
    // mutlak konumu ve ızgaranın piksel koordinatları — hedef hücreyi hesaplamak için gerekli.
    var draggedTrayIndex by remember { mutableIntStateOf(-1) }
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var dragPointerStartGlobal by remember { mutableStateOf(Offset.Zero) }
    var rootOriginPx by remember { mutableStateOf(Offset.Zero) }
    var gridOriginPx by remember { mutableStateOf(Offset.Zero) }
    var cellSizePx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val dragCoroutineScope = rememberCoroutineScope()
    // Parcayi parmagin biraz ustune kaldirir ki parmak parcayi tam kapatmasin —
    // 90dp cok fazla hissettiriyordu (Faz 15, kullanici geri bildirimi), 45dp'ye
    // dusuruldu. Faz 95: 45dp yetersiz kaliyordu, 70dp'ye cikarildi — kullanici
    // cihazda deneyip bunun da yetersiz kaldigini bildirdi, 85dp'ye cikarildi.
    val dragLiftDp = 85.dp

    fun activeDragShape(): BlockShape? =
        if (draggedTrayIndex in trayShapes.indices) trayShapes[draggedTrayIndex] else null

    fun currentHoverCell(): Pair<Int, Int>? {
        val cellSize = cellSizePx
        val shape = activeDragShape() ?: return null
        if (cellSize <= 0f) return null
        val liftPx = with(density) { dragLiftDp.toPx() }
        val pointerAbsolutePos = dragPointerStartGlobal + dragOffset.value
        // Ghost onizleme X'te (yatay) parmagin MERKEZINE, Y'de (dikey) ise parmaktan
        // sabit liftPx mesafede duran ALT KENARINA gore ciziliyor (bkz. asagidaki
        // ghost Box'in "left = pointer - width/2", "top = pointer - lift - height"
        // hesabi, Faz 95c) — hover hucresi de AYNI ofseti uygulamali, yoksa
        // kullanicinin gordugu onizleme ile gercek yerlesim/temizleme mantiginin
        // kullandigi hucre birbirini tutmaz (buyuk sekillerde parca gorunenden
        // kaymis yere yerlesir, beklenmedik satir/sutun temizlenir).
        val halfWidthCells = shape.pattern[0].size / 2f
        val heightCells = shape.pattern.size
        val localX = pointerAbsolutePos.x - gridOriginPx.x
        val localY = (pointerAbsolutePos.y - liftPx) - gridOriginPx.y
        // Düz floor() her zaman AŞAĞI/SOLA yuvarlar, en yakın hücreye degil — bu da
        // istatistiksel olarak yaklasik yarı zamanlarda tam bir hücre sola/yukarı
        // sistematik kaymaya yol aciyordu (kullanici geri bildirimi: "tam bir kare
        // kadar kayma var, sola ve yukarı"). +0.5f ekleyerek en yakın hücreye
        // yuvarlaniyor (floor(x+0.5) == round(x)).
        return floor((localY / cellSize) - heightCells + 0.5f).toInt() to
            floor((localX / cellSize) - halfWidthCells + 0.5f).toInt()
    }

    fun canPlaceShape(shape: BlockShape, startRow: Int, startCol: Int): Boolean {
        for (r in shape.pattern.indices) {
            for (c in shape.pattern[r].indices) {
                if (shape.pattern[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    if (targetR !in 0 until gridSize || targetC !in 0 until gridSize) return false
                    if (board[targetR * gridSize + targetC] != 0) return false
                }
            }
        }
        return true
    }

    fun isCurrentDropValid(): Boolean {
        val shape = activeDragShape() ?: return false
        val (r, c) = currentHoverCell() ?: return false
        return canPlaceShape(shape, r, c)
    }

    // Faz 51: kullanici "sürüklerken, bırakınca 3'lü patlayacak olan blok
    // önden glow oluyordu, Block Blast'ta böyleydi, bizimki öyle olmuyor"
    // dedi. Su an surukleniyorsa ve gecerli bir konuma denk geliyorsa, o
    // parca ORAYA birakilirsa hangi satir/sutunlarin tamamlanacagini
    // ONCEDEN hesaplayip donduruyor — render tarafinda Faz 50'deki ayni
    // satir/sutun cerceve katmaniyla (farkli renk/pulse ile) gosteriliyor.
    fun previewClearedLines(): Pair<Set<Int>, Set<Int>> {
        val shape = activeDragShape() ?: return emptySet<Int>() to emptySet()
        val (r, c) = currentHoverCell() ?: return emptySet<Int>() to emptySet()
        if (!canPlaceShape(shape, r, c)) return emptySet<Int>() to emptySet()
        val simulated = board.toMutableList()
        for (pr in shape.pattern.indices) {
            for (pc in shape.pattern[pr].indices) {
                if (shape.pattern[pr][pc]) {
                    simulated[(r + pr) * gridSize + (c + pc)] = 1
                }
            }
        }
        val rows = (0 until gridSize).filterTo(mutableSetOf()) { row ->
            (0 until gridSize).all { col -> simulated[row * gridSize + col] != 0 }
        }
        val cols = (0 until gridSize).filterTo(mutableSetOf()) { col ->
            (0 until gridSize).all { row -> simulated[row * gridSize + col] != 0 }
        }
        return rows to cols
    }

    fun isCellInDragFootprint(row: Int, col: Int): Boolean {
        val shape = activeDragShape() ?: return false
        val (hr, hc) = currentHoverCell() ?: return false
        val pr = row - hr
        val pc = col - hc
        return pr in shape.pattern.indices && pc in shape.pattern[pr].indices && shape.pattern[pr][pc]
    }

    val levelProgress = (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)

    fun computeStars(finalScore: Int, target: Int): Int = when {
        finalScore >= target * 2 -> 3
        finalScore >= (target * 1.5f).toInt() -> 2
        else -> 1
    }

    fun generateNewTray() {
        trayShapes.clear()
        // Sonsuz modda zorluk skora gore kademeli artar (eski endless mantigi);
        // level modda gercek seviye numarasi kullanilir.
        // Faz 45: puanlama 10x kucultuldugu icin (bkz. placeShape) bu esik de
        // orantili kuculdu (300->30), aksi halde Sonsuz Mod'da zorluk artik
        // ONCEKINE gore 10 kat daha YAVAS yukselirdi.
        //
        // Faz 55: kullanici "seviye 9'da bir anda cok zorlasiyor" dedi — eskiden
        // tier2 (8 parca) -> tier3 (TUM 19 parca, SHAPE_PATTERNS.size) gecisi
        // TEK seviyede oluyordu, 11 yeni (ve bircogu buyuk/karmasik) parca
        // birden havuza giriyordu. Artik seviye 9'dan itibaren havuz her
        // seviyede +2 parca ile KADEMELI genisliyor (9'da 10 parca, ~14'te tam
        // 19'a ulasiyor) — ayni parcalar sonunda geliyor ama sok etkisi yok.
        // Faz 57: Sonsuz Mod'da esik boluci /30 -> /15 (buyuk parcalar iki kati
        // hizla aciliyor, ortalama bir sonsuz oturumda gorulme sansi artiyor).
        // Faz 64: Seviyeli Mod'da ise TERS yonde — kullanici "çok bölüm geçsin,
        // challenge değil başarma isteğiyle oynatalım" dedi, hedef puanlar da
        // (bkz. LevelGenerator) duz +5/seviyeye indirildi. Parca zorlugu ayni
        // hizda artmaya devam etseydi, oyuncu hedefe kolay ulassa bile tahtada
        // yer kalmama riski (gercek "basarisizlik" kaynagi) degismezdi. Gercek
        // seviye numarasi yerine YARISI kullanilarak (levelNumber/2+1) tier
        // esikleri 2 KATINA cikariliyor — tier3 artik seviye 9 yerine 17'de
        // baslıyor, oyuncu cok daha uzun sure sadece kolay/basit parcalarla
        // oynuyor. Sonsuz Mod'a DOKUNULMADI.
        // Faz 79: Pro Mode kendi egrisi — kullanici "zorluk puandan degil
        // parca havuzundan gelsin" dedi. Seviyeli Mod'un YARISI hizi (levelNumber/2+1)
        // YERINE TAM levelNumber kullanilir, kolay-6'lik tier (1x1 dahil) baastan
        // ATLANIR (8'den, yani L sekilleri dahil, baslar), tam havuza (19 parca)
        // sadece birkac seviyede ulasilir. 1x1'in kendisi zaten SHAPE_WEIGHTS_CHALLENGE'da
        // agirlik 0 oldugu icin (asagida) availableCount kapsaminda olsa bile hic gelmez.
        val progressLevel = when {
            isChallengeMode -> levelNumber
            else -> (levelNumber / 2) + 1
        }
        val availableCount = if (isChallengeMode) {
            // Faz 107: havuz 18 -> 34 oldugu icin esikler aile sinirlarina tasindi
            // (bkz. PRO_POOL_STEPS). Kolay-6 tier yine atlaniyor, havuz yine hizli
            // aciliyor; tam havuz seviye 5 yerine 6'da tamamlaniyor — parca sayisi
            // neredeyse iki katina ciktigi icin son adimi tek seferde 8 parca
            // acmak yerine bir seviyeye daha yaymak daha yumusak.
            PRO_POOL_STEPS.getOrElse(progressLevel - 1) { SHAPE_PATTERNS.size }
        } else if (isEndless) {
            // Faz 92/93: kullanici once "score/15 ile 200 puanda TUM havuz
            // cok erken aciliyor" dedi, birkac egri denendi (kare, ozel
            // esikler) — sonunda kullanici "zaten oyun ici bomba/satir-sil
            // alma ekliyoruz, o yuzden havuz sabit 100'er puan bantlarinda
            // duz artsin" karariyla basitlestirdi. Her 100 puanda +2 parca,
            // 6 parcadan (0-99) baslar, 600+ puanda TAM havuza (18) ulasir.
            // Faz 101: duz `score` yerine `maxOf(score, peakScore)` — "reklam
            // izle devam et" skoru geri aldiginda havuz daralmasin diye.
            // Normal oyunda peakScore bayat/0 olabilir ama score her zaman
            // ondan buyuk oldugu icin sonuc degismez; SADECE geri alma
            // sonrasinda peakScore devreye girer (bkz. undoMovesForContinue).
            // Faz 107: adimlar aile sinirlarina tasindi (bkz. ENDLESS_POOL_STEPS).
            // Bant genisligi yine 100 puan ve tam havuz yine 600 puanda aciliyor —
            // yani acilim HIZI degismedi, sadece her adimda daha cok yon geliyor.
            ENDLESS_POOL_STEPS.getOrElse(maxOf(score, peakScore) / 100) { SHAPE_PATTERNS.size }
        } else {
            when {
                progressLevel <= 3 -> 6 // 1x1, 2x1, 1x2, 3x1, 1x3, 2x2 — degismedi
                // Faz 107: bu kademe 8 -> 10 oldu; kose ailesinin 4 yonu de burada
                // birlikte aciliyor (onceden yalnizca 2'si vardi).
                progressLevel <= 8 -> 10
                // Faz 107: duz "+2 parca/kademe" formulu yerine aile sinirlari
                // (bkz. CAREER_POOL_STEPS). Tam havuz seviye 24 yerine ~30'da
                // tamamlaniyor — seviyeler parametrik/sonsuz oldugu icin ust sinir
                // sorunu yok (bkz. LevelGenerator).
                else -> CAREER_POOL_STEPS.getOrElse(progressLevel - 9) { SHAPE_PATTERNS.size }
            }
        }
        val weights = when {
            isChallengeMode -> SHAPE_WEIGHTS_CHALLENGE
            isEndless -> SHAPE_WEIGHTS_ENDLESS
            else -> SHAPE_WEIGHTS
        }
        val totalWeight = (0 until availableCount).sumOf { weights[it] }
        repeat(3) {
            // Duz uniform secim yerine agirlikli secim — bkz. SHAPE_WEIGHTS notu.
            var roll = Random.nextInt(totalWeight)
            var chosenIndex = availableCount - 1
            for (idx in 0 until availableCount) {
                val w = weights[idx]
                if (roll < w) {
                    chosenIndex = idx
                    break
                }
                roll -= w
            }
            val randomColor = Random.nextInt(1, BLOCK_COLORS.size + 1)
            trayShapes.add(BlockShape(id = nextShapeId++, pattern = SHAPE_PATTERNS[chosenIndex], colorIndex = randomColor))
        }
    }

    fun canPlaceAnywhere(shape: BlockShape): Boolean {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (canPlaceShape(shape, r, c)) return true
            }
        }
        return false
    }

    fun checkGameOver() {
        val remainingShapes = trayShapes.filterNotNull()
        if (remainingShapes.isNotEmpty()) {
            val valid = remainingShapes.any { canPlaceAnywhere(it) }
            if (!valid) {
                if (isEndless && endlessContinuesUsed < maxEndlessContinues) {
                    showContinueDialog = true
                } else {
                    isGameOver = true
                    if (isEndless) {
                        onEndlessGameOver(score)
                    } else {
                        onLevelFailed(score)
                    }
                }
            }
        }
    }

    // Faz 101: tahtada en dolu (ama tamamen dolu olmayan sartsiz) satiri
    // bosaltir. "Reklamla devam" sonrasi yer acmak icin — normal bir satir
    // temizleme DEGIL: puan vermez, kombo saymaz, patlama animasyonu
    // tetiklemez; sadece yer acar. Bosaltilacak dolu satir kalmadiysa
    // false doner (sonsuz donguye karsi guvence).
    fun clearDensestRowForContinue(): Boolean {
        var bestRow = -1
        var bestFilled = 0
        for (r in 0 until gridSize) {
            val filled = (0 until gridSize).count { c -> board[r * gridSize + c] != 0 }
            if (filled > bestFilled) {
                bestFilled = filled
                bestRow = r
            }
        }
        if (bestRow < 0) return false
        for (c in 0 until gridSize) board[bestRow * gridSize + c] = 0
        return true
    }

    // Sonsuz modda oturum basina bir kez: son 3 hamleyi geri alip devam etme sansi.
    // Onceden alt 2 satiri korusuzca temizliyorduk, ama bu MEVCUT tepsideki
    // parcalar icin yer acmayabiliyordu — kullanici "izlese de sifirda basliyor"
    // diye bildirdi (fiilen oynanamaz kaliyordu). Gercek bir gecmis hamleye
    // (tahta + skor) donup USTUNE yeni bir tepsi vermek cok daha guvenilir:
    // o an tahta zaten oynanabilirdi (oyun devam ediyordu), yeni tepsi de
    // eski tikanmayi tekrarlama ihtimalini dusurur.
    fun undoMovesForContinue(count: Int) {
        val targetIndex = (moveHistory.size - count).coerceAtLeast(0)
        val snapshot = moveHistory.getOrNull(targetIndex) ?: return
        // Faz 101: skor birazdan geri alinacak — havuz genisligi hesabinin
        // (generateNewTray) daralmamasi icin ulasilan tepe deger saklanir.
        peakScore = maxOf(peakScore, score)
        for (i in board.indices) board[i] = snapshot.board[i]
        score = snapshot.score
        while (moveHistory.size > targetIndex) moveHistory.removeAt(moveHistory.size - 1)
        // Faz 59: kullanici "reklam izledim ama 3 hamleyi geri almadi, direkt
        // game over'a dustu" dedi. Kok neden: geri alinan tahta gercekten
        // oynanabilir bir gecmis durumdu, AMA generateNewTray() tamamen
        // RASTGELE yeni 3 parca uretiyordu — bu yeni parcalarin o tahtaya
        // sigacaginin hicbir garantisi yoktu. Artik en az yeterli sayida
        // parcanin sigdigi bir tepsi bulunana kadar (en fazla 12 deneme,
        // pratikte 1-2 denemede bulunur) yeniden uretiliyor.
        // Faz 69: kullanici cihazda test edip "sadece 1 tanesi sigiyor,
        // yerlestirince patlama bile olmuyor, reklam izlemek anlamsiz kaliyor"
        // dedi — SADECE 1 parcanin sigmasi garantisi yetersizdi, oyuncu
        // yerlestirir yerlestirmez tekrar sikisip "reklam bosa gitti" hissi
        // yasiyordu. Esik 1'den 2'ye cikarildi: en az 2 parca sigana kadar
        // yeniden uretiliyor — reklam sonrasi gercekten birkac hamlelik bir
        // nefes alani kaliyor, tek hamlelik sahte bir "devam" olmuyor.
        //
        // Faz 101: kullanici "reklam izle devam et deyince parca havuzu
        // sifirlaniyormus gibi cok basit parcalar geliyor" dedi. Kok neden
        // BULUNDU ve bu bir "havuz sifirlanmasi" degil, YUKARIDAKI dongunun
        // istatistiksel yan etkisiydi: tepsi, "en az 2 parca sigana kadar"
        // 12 kez BASTAN uretiliyordu. Geri alinan tahta hala dolu oldugu icin
        // dolu tahtada yalnizca KUCUK parcalar sigar — dolayisiyla buyuk/
        // karmasik parca iceren her tepsi reddedilip 1x1/2x1/1x2 agirlikli
        // tepsiler kabul ediliyordu (rejection sampling bias). Parca dagilimi
        // fiilen bozuluyordu.
        //
        // Cozum yon degistirdi: tepsiyi tahtaya UYDURMAK yerine tahtayi
        // tepsiye uyduruyoruz. Tepsi TEK SEFER, gercek agirlik tablosuyla
        // uretilir (dagilim hic bozulmaz), sonra o tepsiden en az 2 parca
        // sigana kadar tahtadan satir bosaltilir. Bu hem yanliligi tamamen
        // ortadan kaldirir hem de reklamin odulunu somutlastirir: oyuncu
        // sahte bir "kolay parca" yerine gercek bir nefes alani gorur.
        // Dongu sonlanmasi garantili — tahta bosaldiginda her parca sigar.
        generateNewTray()
        var guard = 0
        while (trayShapes.filterNotNull().count { canPlaceAnywhere(it) } < 2 && guard < gridSize) {
            if (!clearDensestRowForContinue()) break
            guard++
        }
    }

    // Faz 95c: kullanici "reklam izle devam et dediginde reklam yoksa oyunu
    // dogrudan bitiriyordu, reklam gelmediyse de butona basinca devam etsin"
    // dedi — onDenied (reklam basarisiz/reddedildi) artik onGranted ile AYNI
    // sekilde davraniyor (3 hamle geri al + devam), sadece "devam" hakkinin
    // (endlessContinuesUsed, 4 hak siniri) tuketilmesi korunuyor.
    fun handleContinueWithAd() {
        if (isRequestingContinueAd) return
        isRequestingContinueAd = true
        val proceedWithContinue = {
            isRequestingContinueAd = false
            showContinueDialog = false
            endlessContinuesUsed++
            undoMovesForContinue(3)
            checkGameOver()
        }
        onRequestContinueAd(proceedWithContinue, proceedWithContinue)
    }

    fun handleEndSession() {
        showContinueDialog = false
        isGameOver = true
        onEndlessGameOver(score)
    }


    fun applyBoosterAt(row: Int, col: Int) {
        val type = armedBooster ?: return
        val owned = availableBoosterCounts[type] ?: 0
        if (owned <= 0) {
            armedBooster = null
            return
        }
        when (type) {
            BoosterType.BOMB -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val tr = row + dr
                        val tc = col + dc
                        if (tr in 0 until gridSize && tc in 0 until gridSize) {
                            board[tr * gridSize + tc] = 0
                        }
                    }
                }
            }
            BoosterType.LINE_CLEAR -> {
                for (c2 in 0 until gridSize) {
                    board[row * gridSize + c2] = 0
                }
                onLinesCleared(1)
            }
        }
        SoundManager.playSuccess(soundEnabled)
        availableBoosterCounts[type] = owned - 1
        onUseBooster(type)
        armedBooster = null
        checkGameOver()
    }

    // Faz 115e: cikis tek noktadan gecer — geri tusu, cikis onayi ve
    // level-failed "HARITAYA DON" ayni kurali paylassin diye.
    //
    // Kural (kullanici, 2026-08-16): skor 0 ise oyuncu daha hicbir sey
    // oynamamistir, bedelsiz cikar. Skor > 0 ise oynanmis bir tahta terk
    // ediliyor demektir; bu bir "bedava reset"tir ve reklam gosterilir.
    fun exitGame() {
        val free = onBackWithoutAd
        if (score <= 0 && free != null) free() else onBack()
    }

    fun resetGame() {
        board.clear()
        repeat(gridSize * gridSize) { board.add(0) }
        score = 0
        peakScore = 0 // Faz 101: yeni oturum, havuz genisligi bastan hesaplansin
        moveHistory.clear()
        comboCount = 0
        draggedTrayIndex = -1
        armedBooster = null
        isGameOver = false
        isLevelComplete = false
        // Faz 107b: gorunurluk bayragi da sifirlanmali, aksi halde yeniden
        // baslatildiginda diyalog ekranda asili kalir.
        levelCompleteRevealed = false
        showContinueDialog = false
        continuesUsedInAttempt = 0
        endlessContinuesUsed = 0
        lastClearedText = ""
        generateNewTray()
    }

    // Faz 55: kullanici "level bitti retry deyince reklam izlemesi gerekmez
    // mi" dedi — onceden Seviyeli Mod'da TEKRAR DENE tamamen ucretsiz/aninda
    // idi. Artik Sonsuz Mod'daki "reklamla devam et" ile AYNI generic
    // onRequestContinueAd kancasi kullanilarak zorunlu hale getirildi.
    // Faz 61: kullanici iki konu bildirdi — (1) reklam izleyince Sonsuz
    // Mod'daki GIBI 3 hamle geri alip devam etsin istedi (tam seviye
    // sifirlama YERINE), bomba hediyesi istemedi (kendi onerisini geri
    // cekti); (2) bu buton "hala reklam cagiramiyor" dedi — kok neden
    // bulundu: `AppNavigation.kt`'deki Routes.GAME (Seviyeli Mod) rotasi
    // `onRequestContinueAd`'i HIC BAGLAMAMISTI, sadece Routes.ENDLESS_GAME
    // bagliyordu — varsayilan deger aninda onDenied() cagiriyordu, yani
    // Seviyeli Mod'da GERCEK bir reklam hicbir zaman yuklenmiyordu (ayri
    // duzeltildi, bkz. AppNavigation.kt). Faz 97: kullanici "reklam izle
    // devam et 1 kez değil 3 kez olsun" dedi — ayni seviye denemesinde artik
    // maxRetryContinues (3) kez reklamli devam sunuluyor (Endless'teki 4 hak
    // ile AYNI desen); hak tukenince buton "TEKRAR DENE"ye (zorunlu
    // interstitial'a sarili, bkz. onRetryInterstitial) donuyor.
    // Faz 98: kullanici "seviyelide yandim, reklam izle dedim reklam cikmadi
    // ama baslatmadi da, birkac kez denemek zorunda kaldim" dedi. Kok neden:
    // onDenied dali SADECE spinner'i kapatiyordu, oyunu devam ettirmiyordu —
    // reklam no-fill oldugunda oyuncu game-over dialogunda kilitli kaliyordu.
    // Faz 95c'de bu duzeltme SADECE Sonsuz Mod'a (handleContinueWithAd)
    // uygulanmis, ayni sorunu yasayan bu akis atlanmisti. Artik Sonsuz Mod ile
    // BIREBIR ayni desen: onGranted ve onDenied AYNI lambda — reklam gelmese
    // de "izlemis sayilir" (oyuncunun hatasi degil), sadece hak tuketilir.
    fun handleRetryWithAd() {
        if (isRequestingContinueAd) return
        if (continuesUsedInAttempt >= maxRetryContinues) {
            onRetryInterstitial { resetGame() }
            return
        }
        isRequestingContinueAd = true
        val proceedWithContinue = {
            isRequestingContinueAd = false
            continuesUsedInAttempt++
            isGameOver = false
            undoMovesForContinue(3)
            checkGameOver()
        }
        onRequestContinueAd(proceedWithContinue, proceedWithContinue)
    }

    LaunchedEffect(Unit) {
        if (trayShapes.isEmpty()) {
            generateNewTray()
        }
    }

    // Faz 5: withFrameNanos — her karede parçacık physics + floating score update.
    // Choreographer callback'i, kare zamanını doğru şekilde yakalıyor.
    LaunchedEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        var lastFrameTimeNanos = System.nanoTime()

        choreographer.postFrameCallback(object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                val deltaTimeNanos = frameTimeNanos - lastFrameTimeNanos
                val deltaTimeMs = deltaTimeNanos / 1_000_000f
                lastFrameTimeNanos = frameTimeNanos

                // Faz 5: Physics güncelleme
                particlePool.update(deltaTimeMs)

                // Faz 4: Radyal flash'i guncelle (progress += delta)
                if (radialFlash != null) {
                    val flash = radialFlash!!
                    val newProgress = flash.progress + deltaTimeMs / radialFlashDurationMs
                    if (newProgress >= 1f) {
                        radialFlash = null  // Sifirlanir
                    } else {
                        radialFlash = flash.copy(progress = newProgress)
                    }
                }

                // Sonraki frame için tekrar kayıt
                choreographer.postFrameCallback(this)
            }
        })
    }

    // Basari rozeti kisa bir sure gosterilip kendiliginden kapanir.
    LaunchedEffect(activeAchievementText) {
        if (activeAchievementText != null) {
            delay(2200)
            activeAchievementText = null
        }
    }

    // Seviye tamamlaninca bir kerelik konfeti patlamasi.
    LaunchedEffect(isLevelComplete) {
        if (isLevelComplete) {
            // Faz 107b: bolumu bitiren patlama izlensin diye bekle. Ritual:
            // beklenti (tek satir 400ms / kombo 620ms) + flash 110ms + huzme 360ms.
            // En uzun hâli ~1090ms; kombo olmayan durumda biraz erken gelmesi
            // sorun degil, tersi (patlamayi kacirmak) sorun.
            // Faz 107c: ritual kisaldi (beklenti 480 + flash 110 + huzme 280 = ~870),
            // bekleme de ona gore indirildi. Bu deger ritual sureleriyle birlikte
            // guncellenmezse diyalog gereksiz gec gelir.
            delay(860)
            levelCompleteRevealed = true
            // Zafer sesi de burada calar — onceden placeShape icinde t=0'da
            // caliyordu ve patlama sesinin uzerine biniyordu.
            SoundManager.playSuccess(soundEnabled)
            confettiPieces = List(36) {
                ConfettiPiece(
                    xFraction = Random.nextFloat(),
                    startDelay = Random.nextFloat() * 0.3f,
                    drift = (Random.nextFloat() - 0.5f) * 60f,
                    rotationSpeed = Random.nextFloat() * 6f + 2f,
                    color = BLOCK_COLORS[Random.nextInt(BLOCK_COLORS.size)]
                )
            }
            confettiProgress.snapTo(0f)
            confettiProgress.animateTo(1f, animationSpec = tween(1600, easing = FastOutSlowInEasing))
        }
    }

    fun placeShape(shapeIndex: Int, shape: BlockShape, startRow: Int, startCol: Int) {
        if (!canPlaceShape(shape, startRow, startCol)) return

        if (showFirstMoveHint) {
            showFirstMoveHint = false
            onFirstMoveMade()
        }

        // Faz 61: onceden SADECE Sonsuz Mod'da tutuluyordu. Seviyeli Mod'daki
        // "reklam izle, devam et" de artik ayni geri-alma mekanizmasini
        // kullandigi icin (bkz. handleRetryWithAd) her iki modda da kayit
        // tutuluyor.
        moveHistory.add(GameSnapshot(board = board.toList(), score = score))
        while (moveHistory.size > MAX_MOVE_HISTORY) moveHistory.removeAt(0)

        SoundManager.playLock(soundEnabled)

        var placedBlocks = 0
        for (r in shape.pattern.indices) {
            for (c in shape.pattern[r].indices) {
                if (shape.pattern[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    board[targetR * gridSize + targetC] = shape.colorIndex
                    placedBlocks++
                }
            }
        }

        // Faz 45: onceden hucre basina 10 puan verilirdi — kullanici "puanlar kare
        // sayısı kadar olmalı" dedi (hakli: eski sistemde HİÇ satir/sutun temizlemeden
        // sadece 50 hucre doldurarak 500'luk bir hedefe ulasilabiliyordu, mantiksizdi).
        // Artik 1 hucre = 1 puan; asagidaki satir temizleme bonusu ve hedef puanlari
        // (bkz. LevelGenerator) bu YENİ 10x kucuk olceğe gore yeniden ayarlandi.
        // Faz 77: Pro Mode "daha yuksek puan carpani" — varsayilan 1f (Level/
        // Sonsuz Mod degismez), Pro Mode'da >1f verilip TUM puan artislarina
        // uygulanir.
        val placeBonus = (placedBlocks * scoreMultiplier).roundToInt()
        score += placeBonus

        // Check full rows & columns
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until gridSize) {
            var fullRow = true
            for (c in 0 until gridSize) {
                if (board[r * gridSize + c] == 0) {
                    fullRow = false
                    break
                }
            }
            if (fullRow) rowsToClear.add(r)
        }

        for (c in 0 until gridSize) {
            var fullCol = true
            for (r in 0 until gridSize) {
                if (board[r * gridSize + c] == 0) {
                    fullCol = false
                    break
                }
            }
            if (fullCol) colsToClear.add(c)
        }

        val totalLinesCleared = rowsToClear.size + colsToClear.size
        if (totalLinesCleared > 0) {
            onLinesCleared(totalLinesCleared)
            if (totalLinesCleared >= 2) onMultiClear()
            comboCount++
            // Faz 45: onceden sabit 100 puan/satir idi (yeni 1puan/hucre olcegine
            // gore orantisizca buyuktu). Artik satir basina bonus, o satirin gercek
            // uzunluguna (gridSize hucre) esit — "bir satiri temizlemek, o satiri
            // YENIDEN doldurmaya esdeger" gibi mantikli/orantili bir taban.
            //
            // Faz 46: kullanici "block blast'ın ödüllendirme mekaniğine bak" dedi —
            // arastirma (blockblast.free/wiki/scoring-and-combos) iki ayri kavrami
            // netlestirdi: "Streak" (ART ARDA hamlelerde patlatma — bizim comboCount,
            // zaten dogrusal carpandi, degismedi) ve "Combo" (TEK hamlede BIRDEN
            // FAZLA satir/sutun patlatma — bizim totalLinesCleared). Referansta 2.
            // kavram DOGRUSAL DEGIL: "3 satiri BIRDEN patlatmak, 3 satiri AYRI AYRI
            // patlatmaktan 3-4 kat daha degerli." Onceden totalLinesCleared sadece
            // dogrusal carpiyordu (2 satir = tam 2 kat, fazlasi yoktu) — artik ek bir
            // multiLineMultiplier var (1 satir icin carpan yok, her ekstra ESZAMANLI
            // satir +%50 ekliyor: 2 satir=1.5x, 3 satir=2x, 4 satir=2.5x).
            val multiLineMultiplier = 1f + (totalLinesCleared - 1) * 0.5f
            val lineBonus = (totalLinesCleared * gridSize * comboCount * multiLineMultiplier * scoreMultiplier).roundToInt()
            score += lineBonus
            // Kombo yukseldikce ovgu kelimesi de yukseliyor (Block Blast!'daki
            // "Excellent!"/thumbs-up tarzi geri bildirime benzer, kullanici istegi).
            // Coklu satir/sutun temizleme HER ZAMAN iyi bir sey — ilk kombo adiminda
            // bile ("ÇOKLU PATLAMA!") kirmizimsi renkle gosterilmesi "kotu bir sey oldu"
            // hissi veriyordu (kullanici geri bildirimi) — artik daha cosku dolu.
            //
            // Faz 34 duzeltmesi: sadece comboCount'a (art arda AYRI hamlelerde
            // temizleme) bakmak kelime cesitliligini neredeyse yok ediyordu —
            // comboCount>=2'ye ulasmak nadir, ama TEK hamlede 2-3-4 satir birden
            // temizlemek (totalLinesCleared>1) cok daha sik. Sonuc: kullanici
            // pratikte HEP "SÜPER!" duyuyordu, ust kademe kelimeler neredeyse
            // hic tetiklenmiyordu. Artik ikisi birlesik bir "cosku puani" ile
            // olculuyor, boylece buyuk TEK hamlelik patlamalar da hak ettigi
            // ust kademe kelimeyi tetikliyor.
            val excitement = (comboCount - 1) + (totalLinesCleared - 1)
            // Faz 34: kullanici "hep aynısı geliyor" dedi — her kademede TEK
            // sabit kelime yerine kucuk bir esanlamli havuzdan rastgele seciliyor,
            // boylece ayni kombo seviyesinde bile degisken tepki alınıyor.
            // Faz 34c: kullanici "yazi secili dilde kalsin ama SES her zaman
            // Ingilizce olsun, Turkce TTS sesleri pek güzel değil" dedi — bu
            // yuzden ekranda gosterilen kelime (language'a gore) ile TTS'e
            // gonderilen kelime artik BAGIMSIZ: ikisi de ayni kademenin
            // havuzundan geliyor ama SES her zaman enPool'dan seciliyor.
            // Faz 35: 5 dile cikarildi.
            data class PraisePools(val tr: List<String>, val en: List<String>, val it: List<String>, val fr: List<String>, val es: List<String>)
            val pools = when {
                excitement >= 5 -> PraisePools(
                    listOf("İNANILMAZ!", "EFSANE!", "DURDURULAMAZ!"),
                    listOf("AMAZING!", "LEGENDARY!", "UNSTOPPABLE!"),
                    listOf("INCREDIBILE!", "LEGGENDARIO!", "INARRESTABILE!"),
                    listOf("INCROYABLE!", "LÉGENDAIRE!", "IMPARABLE!"),
                    listOf("INCREÍBLE!", "LEGENDARIO!", "IMPARABLE!")
                )
                excitement == 4 -> PraisePools(
                    listOf("MÜKEMMEL!", "MUHTEŞEM!", "OLAĞANÜSTÜ!"),
                    listOf("EXCELLENT!", "FANTASTIC!", "INCREDIBLE!"),
                    listOf("ECCELLENTE!", "FANTASTICO!", "STRAORDINARIO!"),
                    listOf("EXCELLENT!", "FANTASTIQUE!", "EXTRAORDINAIRE!"),
                    listOf("EXCELENTE!", "FANTÁSTICO!", "EXTRAORDINARIO!")
                )
                excitement == 3 -> PraisePools(
                    listOf("HARİKA!", "ÇOK İYİ!", "BAŞARILI!"),
                    listOf("GREAT!", "AWESOME!", "SMOOTH!"),
                    listOf("FANTASTICO!", "GRANDIOSO!", "PERFETTO!"),
                    listOf("GÉNIAL!", "IMPRESSIONNANT!", "PARFAIT!"),
                    listOf("GENIAL!", "IMPRESIONANTE!", "PERFECTO!")
                )
                excitement == 2 -> PraisePools(
                    listOf("GÜZEL!", "İYİ İŞ!", "AFERİN!"),
                    listOf("NICE!", "GOOD!", "SWEET!"),
                    listOf("BELLO!", "BRAVO!", "OTTIMO!"),
                    listOf("BIEN!", "BRAVO!", "SUPER!"),
                    listOf("BIEN!", "BUEN TRABAJO!", "GENIAL!")
                )
                excitement == 1 -> PraisePools(
                    listOf("SÜPER!", "GÜZEL!", "İYİ!"),
                    listOf("SUPER!", "NICE!", "GOOD!"),
                    listOf("SUPER!", "BELLO!", "BENE!"),
                    listOf("SUPER!", "BIEN!", "JOLI!"),
                    listOf("SÚPER!", "BIEN!", "BUENO!")
                )
                else -> PraisePools(
                    listOf("PATLAMA!", "GÜZEL!"),
                    listOf("BLAST!", "NICE!"),
                    listOf("BOTTO!", "BELLO!"),
                    listOf("BOUM!", "BIEN!"),
                    listOf("BUM!", "BIEN!")
                )
            }
            val praiseWord = praiseFrom(language, pools.tr, pools.en, pools.it, pools.fr, pools.es)
            val praiseEmoji = when {
                excitement >= 5 -> " 🔥"
                excitement >= 2 -> " 👍"
                excitement == 1 -> " 🎉"
                else -> ""
            }
            lastClearedText = "$praiseWord$praiseEmoji +$lineBonus"
            lastClearWasCelebration = comboCount >= 2 || totalLinesCleared > 1

            // Faz 34: orijinal oyunda ovgu kelimeleri sesle de soyleniyordu
            // (kullanici gozlemi) — sadece gercek bir kutlama aninda (kucuk
            // her tekli patlamada degil) seslendiriliyor. Faz 93: kullanicinin
            // gonderdigi 4 gercek ses kaydiyla (good/great/amazing/incredible)
            // cihaz TTS motorunun (robotik) yerini aldi.
            if (lastClearWasCelebration && soundEnabled) {
                SoundManager.playPraise(soundEnabled, excitement)
            }

            sessionLinesCleared += totalLinesCleared
            when {
                comboCount == 3 -> maybeUnlockAchievement("combo_3", language.pick(tr = "🔥 3x Kombo!", en = "🔥 3x Combo!", it = "🔥 3x Combo!", fr = "🔥 3x Combo!", es = "🔥 3x Combo!"))
                comboCount == 5 -> maybeUnlockAchievement("combo_5", language.pick(tr = "🔥 5x Kombo — İnanılmaz!", en = "🔥 5x Combo — Amazing!", it = "🔥 5x Combo — Incredibile!", fr = "🔥 5x Combo — Incroyable!", es = "🔥 5x Combo — Increíble!"))
            }
            when {
                sessionLinesCleared >= 10 && sessionLinesCleared - totalLinesCleared < 10 ->
                    maybeUnlockAchievement("lines_10", language.pick(tr = "🏅 10 satır temizledin!", en = "🏅 10 lines cleared!", it = "🏅 10 linee eliminate!", fr = "🏅 10 lignes effacées!", es = "🏅 ¡10 líneas eliminadas!"))
                sessionLinesCleared >= 25 && sessionLinesCleared - totalLinesCleared < 25 ->
                    maybeUnlockAchievement("lines_25", language.pick(tr = "🏅 25 satır temizledin!", en = "🏅 25 lines cleared!", it = "🏅 25 linee eliminate!", fr = "🏅 25 lignes effacées!", es = "🏅 ¡25 líneas eliminadas!"))
                sessionLinesCleared >= 50 && sessionLinesCleared - totalLinesCleared < 50 ->
                    maybeUnlockAchievement("lines_50", language.pick(tr = "🏅 50 satır — harikasın!", en = "🏅 50 lines — you're on fire!", it = "🏅 50 linee — sei in fiamme!", fr = "🏅 50 lignes — tu es en feu!", es = "🏅 50 líneas — ¡estás que ardes!"))
            }

            val clearedIndices = mutableSetOf<Int>()
            rowsToClear.forEach { r -> for (c in 0 until gridSize) clearedIndices.add(r * gridSize + c) }
            colsToClear.forEach { c -> for (r in 0 until gridSize) clearedIndices.add(r * gridSize + c) }

            // Once hucreler HALA DOLUYKEN kisa bir neon glow gosterilir ("patlamaya
            // kilitlendi" hissi, Block Blast referansi — kullanici geri bildirimi:
            // "3'lü patlamak üzereyken glow efekti falan ekliyor"). Gercek temizleme
            // (board sifirlama + parcacik patlamasi + ses) bu glow'dan HEMEN SONRA olur.
            // Faz 106 (TODO 12): bu patlamanin rengi = o hamlede yerlestirilen
            // parcanin rengi. board'dan degil `shape`'ten okunuyor — patlayan
            // satirdaki hucreler karisik renkte, tetikleyen parca ise tek renk.
            clearAccentColor = BLOCK_COLORS.getOrElse((shape.colorIndex - 1).coerceAtLeast(0)) { NeonGold }
            glowingClearCells = clearedIndices
            // Faz 50: hucre-hucre glow'a ek olarak tum satir/sutunun etrafinda
            // TEK bir cerceve (rakip oyun referansi) — render tarafinda ayri
            // bir Canvas katmaninda cizilecek.
            glowingRows = rowsToClear.toSet()
            glowingCols = colsToClear.toSet()
            // Faz 107 (Kademe A — ritim): referans klibi kare kare olculdu ve
            // ritmimizin TAM TERS oldugu ortaya cikti:
            //   Block Blast : ~640ms beklenti -> ~60ms yok olus
            //   Bizim (eski): ~250ms beklenti -> ~350ms flash sonmesi
            // Yani onlar uzun bekletip ANI kesiyor, biz az bekletip uzun
            // sonduruyorduk — "patlama" degil "eriyip gitme" hissi.
            //
            // Beklenti artik UZUN ve NABIZLI (tek rampa degil, 2 cevrim), yok
            // olus ANI. Ama beklentiyi her patlamada 620ms yapmak Sonsuz Mod'da
            // hizli oynayan biri icin fark edilir bir yavaslama olurdu ve tekli
            // patlama vakaların cogunlugu — bu yuzden IKI ayri ritim:
            //   tek satir & seri yok    -> 400ms (2 hizli nabiz, akici kalir)
            //   coklu satir VEYA 2x+    -> 620ms (referansa yakin, "buyuk an")
            // Boylece ritmin uzunlugu olayin buyuklugunu ANLATIR.
            val isBigClear = totalLinesCleared >= 2 || comboCount >= 2
            // Faz 107c: 400/620 -> 320/480. Kullanici cihazda "patlama suresi hala
            // biraz daha kisaltilabilir" dedi. Beklenti oyuncuyu FIILEN bekletir
            // (hucreler hala dolu, tahta guncellenmemis), o yuzden asil kesilecek
            // yer burasi — huzme/parcacik ise tahta zaten temizken oynuyor.
            // Nabiz yapisi korunuyor: keyframe'lerin hepsi bu degere oranli.
            val anticipationMs = if (isBigClear) 480 else 320
            dragCoroutineScope.launch {
                glowPulse.snapTo(0f)
                // 2 nabiz: parla -> kis -> daha parla -> kis -> patlamadan hemen
                // once overshoot ile "sarj ol". Segment easing'leri kasitli
                // asimetrik (cikis easeOutCubic = ani tutusma, inis easeInCubic
                // = yumusak birakma); duz FastOutSlowIn her asamayi ayni
                // hissettiriyordu. Son segmentteki easeOutBack overshoot'u
                // 0.52 -> 0.95 araliginda kaldigi icin tepe ~0.99'da kalir,
                // alpha 1.0'i asmaz (yine de okuma yerlerinde coerce ediliyor).
                glowPulse.animateTo(
                    targetValue = 0.95f,
                    animationSpec = keyframes {
                        durationMillis = anticipationMs
                        0f at 0 using EaseOutCubicBoom
                        0.85f at (anticipationMs * 0.20f).toInt() using EaseInCubicBoom
                        0.40f at (anticipationMs * 0.42f).toInt() using EaseOutCubicBoom
                        1.0f at (anticipationMs * 0.64f).toInt() using EaseInCubicBoom
                        0.52f at (anticipationMs * 0.80f).toInt() using EaseOutBackBoom
                        0.95f at anticipationMs
                    }
                )
                // Onceden burada `delay(90)` vardi — rampanin KENDISI artik
                // beklenti oldugu icin ek gecikme gereksiz (ve patlamayi
                // olu bir bosluktan sonra tetikliyordu).

                // Faz 33: ayni anda 2+ satir/sutun temizlenince (gercek kombo)
                // tekli patlama yerine daha uzun/coskulu "kombo patlamasi" calar.
                if (totalLinesCleared >= 2) {
                    SoundManager.playComboBlast(soundEnabled, comboCount)
                } else {
                    SoundManager.playBlast(soundEnabled)
                }
                // Faz 105: titresim tam patlama aninda, ses ile AYNI karede —
                // glow bittikten sonra. Kendi icinde 2 satir esigini kontrol
                // ediyor (tek satirda titresim yok), bu yuzden burada kosulsuz
                // cagriliyor: ses/titresim ayrimi tek bir yerde, HapticManager'da.
                HapticManager.playClearHaptic(hapticsEnabled, totalLinesCleared, comboCount)
                glowingClearCells = emptySet()
                glowingRows = emptySet()
                glowingCols = emptySet()
                recentlyClearedCells = clearedIndices
                // Faz 107: 350ms -> 110ms. Yok olus ANI olmali; 350ms'lik sonme
                // patlamayi "eriyip gitme"ye ceviriyordu. Kalici gorsel gövde
                // artik asagidaki isik huzmesi (690ms).
                dragCoroutineScope.launch {
                    clearFlashAlpha.snapTo(1f)
                    clearFlashAlpha.animateTo(0f, animationSpec = tween(110, easing = EaseOutQuartBoom))
                }
                // Faz 4: Radyal isin flash tetikleme (patlama aninda merkezden disa)
                if (effectIntensity != EffectIntensity.LOW && radialBeamCount > 0) {
                    val centerX = gridOriginPx.x + (gridSize * cellSizePx) / 2f
                    val centerY = gridOriginPx.y + (gridSize * cellSizePx) / 2f
                    radialFlash = RadialFlashState(
                        centerX = centerX,
                        centerY = centerY,
                        progress = 0f,
                        beamCount = radialBeamCount,
                        rotationOffset = Random.nextFloat() * 360f
                    )
                }
                // Faz 107 (Kademe B-1): ISIK HUZMESI. Hucreler gidince patlayan
                // satir/sutun boyunca parlak bir serit kalir. Olcum: ~690ms ve
                // IKI asamali — 1) dar/keskin beyaz cekirdek (~1/3 hucre),
                // 2) genisleyip yumusayan yari saydam altin bant. Tek bir sonen
                // dikdortgen DEGIL. Ilerleme LINEER: cizim tarafi bunu zaman
                // olarak kullaniyor.
                beamRows = rowsToClear.toSet()
                beamCols = colsToClear.toSet()
                dragCoroutineScope.launch {
                    beamProgress.snapTo(0f)
                    // Faz 107b: 690ms -> 360ms. Referans olcumu 690ms diyordu ama
                    // kullanici cihazda "oyun takilmis gibi his veriyor" dedi.
                    // Referansta huzme oyuncuyu BEKLETMIYOR (tahta zaten temiz,
                    // oynamaya devam edilebiliyor); bizde ritmin gorsel agirligi
                    // daha fazla hissediliyor. Sure yariya indirildi.
                    // EASING LINEAR KALMALI: `bp` burada zaman tabani olarak
                    // kullaniliyor (cizim tarafinda cekirdek ilk %30'da soner,
                    // bant %18'den acilir). Easing degistirmek faz matematigini bozar.
                    beamProgress.animateTo(1f, animationSpec = tween(280, easing = LinearEasing))
                    beamRows = emptySet()
                    beamCols = emptySet()
                }
                // Faz 106 (TODO 12): patlama animasyonu tek bir flash'ti — hucreler
                // parlayip yok oluyordu, "patlama" hissi yalnizca parcaciklardan
                // geliyordu. Artik patlayan satirin/sutunun boyunca disari acilip
                // sonen bir sok dalgasi da ciziliyor: flash 350ms'de sonerken dalga
                // 420ms boyunca genisliyor, ikisi ust uste binince cizgi "itiliyor"
                // gibi gorunuyor.
                shockRows = rowsToClear.toSet()
                shockCols = colsToClear.toSet()
                dragCoroutineScope.launch {
                    shockProgress.snapTo(0f)
                    // Faz 107: 420ms/FastOutSlowIn -> 300ms/easeOutQuart. Yeni
                    // ritimde dalga da "ani cikis, yavas sonme" olmali; eski
                    // egri baslangicta yavastı ve vurus anini yumusatiyordu.
                    shockProgress.animateTo(1f, animationSpec = tween(300, easing = EaseOutQuartBoom))
                    shockRows = emptySet()
                    shockCols = emptySet()
                }

                // Faz 50: patlama noktasinda yuzen "+N" puan yazisi — her temizlenen
                // satir/sutunun kendi orta noktasinda, kombo/coklu-satir carpanindan
                // BAGIMSIZ olarak esit paylastirilmis (sadece gorsel, gercek toplam
                // zaten `score`'a tam olarak eklendi).
                val lineCount = (rowsToClear.size + colsToClear.size).coerceAtLeast(1)
                val perLineScore = lineBonus / lineCount
                val popupColor = if (totalLinesCleared >= 2) NeonGold else NeonGreen
                val rowPopups = rowsToClear.map { r ->
                    ScorePopup(row = r + 0.5f, col = gridSize / 2f, text = "+$perLineScore", color = popupColor)
                }
                val colPopups = colsToClear.map { c ->
                    ScorePopup(row = gridSize / 2f, col = c + 0.5f, text = "+$perLineScore", color = popupColor)
                }
                scorePopups = rowPopups + colPopups

                // Parcacik patlamasi: temizlenen hucrelerden bir kismini orneklendirip
                // rastgele acı/mesafeyle disari firlat — renkler board SIFIRLANMADAN
                // ONCE okunuyor, aksi halde tum parcaciklar ayni "bos hucre" rengine duser.
                // Faz 50: buyuk anlarda (kombo/coklu-satir) parcacik sayisi da artiyor —
                // rakip oyunun kayittaki "Perfect!" anindaki yogun konfeti referansi.
                // clearedIndices'ten BUYUK sayida parcacik istenebiliyor (kucuk bir
                // tekli satir sadece 8 hucre saglar) — yerine koyarak (with replacement)
                // ornekleme yapiliyor ki yogunluk gercekten combo/coklu-satir ile artsin.
                // Faz 107 (Kademe B-2): parcacik SAYISI degil KALITESI degisti.
                // Referans olcumu bir sutun temizlemesinde ~15 kup + 2-3 yildiz
                // gosteriyor, yani mevcut 20-80 araligi zaten dogru — sayiyi
                // artirmak yanlis olurdu. Degisen: tur cesitliligi (esk. hepsi
                // daire), yukari yonelim, yercekimi, surtunme, parcacik basina
                // omur/gecikme/boyut, ve donme.
                // Tur dagilimi referanstan: ~%51 eskenar dortgen, %15 daire,
                // %9 serit, %15 kare zerre, %10 dort-uclu yildiz.
                // Faz 5: ParticlePool.acquire() ile yeni havuz sistemi.
                particlePool.reset()
                val clearedList = clearedIndices.toList()
                val burstCount = (20 + (comboCount - 1) * 8 + (totalLinesCleared - 1) * 10)
                    .coerceIn(20, MAX_BLAST_PARTICLES)
                repeat(burstCount) {
                    val index = clearedList[Random.nextInt(clearedList.size)]
                    val p = particlePool.acquire() ?: return@repeat
                    val cellColor = BLOCK_COLORS.getOrElse((board[index] - 1).coerceAtLeast(0)) { NeonCyan }
                    val roll = Random.nextFloat()
                    p.kind = when {
                        roll < 0.51f -> PK_DIAMOND
                        roll < 0.66f -> PK_CIRCLE
                        roll < 0.75f -> PK_STREAK
                        roll < 0.90f -> PK_MOTE
                        else -> PK_STAR
                    }
                    // Hucre icinde rastgele bir noktadan dogar (tam merkezden
                    // degil) — hepsi ayni noktadan ciktiginda "sacilma" degil
                    // "yildiz patlamasi" gorunuyordu.
                    p.x = (index % gridSize) + 0.15f + Random.nextFloat() * 0.70f
                    p.y = (index / gridSize) + 0.15f + Random.nextFloat() * 0.70f
                    // Yatay menzil bilerek dar: efekt katmani grid Card'inin
                    // ICINDE, yani tahta sinirinda KIRPILIYOR. Referansta konfeti
                    // tahtanin ~0.6 hucre disina tasiyor ama bizde tasan parcacik
                    // gorunmeyecegi icin "kesilmis" gorunurdu (bkz. rapor notu).
                    p.vx = (Random.nextFloat() - 0.5f) * 2.4f
                    // Yukari yonelim (negatif = yukari).
                    p.vy = -(2.5f + Random.nextFloat() * 2.2f)
                    p.rot = Random.nextFloat() * 360f
                    // Donme sadece dortgen ve seritlerde anlamli; daire/zerrede
                    // gorsel karsiligi yok, bosuna hesap.
                    p.spin = if (p.kind == PK_DIAMOND || p.kind == PK_STREAK) {
                        (Random.nextFloat() - 0.5f) * 520f
                    } else 0f
                    p.size = when (p.kind) {
                        PK_DIAMOND -> 0.20f
                        PK_CIRCLE -> 0.13f
                        PK_STREAK -> 0.10f
                        PK_MOTE -> 0.11f
                        else -> 0.26f
                    }
                    // Hepsi ayni anda dogmaz: 0-300ms rastgele gecikme.
                    p.delay = Random.nextFloat() * 0.30f
                    // Kiymik omru 300-600ms.
                    p.life = 0.30f + Random.nextFloat() * 0.30f
                    // Faz 106: cogunluk patlamayi TETIKLEYEN parcanin renginde,
                    // %30 hucrenin kendi renginde (doku korunuyor). Yildizlar
                    // additive ciziliyor, bu yuzden beyaz.
                    p.color = when {
                        p.kind == PK_STAR -> Color.White
                        Random.nextFloat() < 0.7f -> clearAccentColor
                        else -> cellColor
                    }
                }
                // Faz 5: ParticlePool kendi state'ini yönetiyor, activeParticles atıyor yok

                // Clear cells
                rowsToClear.forEach { r ->
                    for (c in 0 until gridSize) {
                        board[r * gridSize + c] = 0
                    }
                }
                colsToClear.forEach { c ->
                    for (r in 0 until gridSize) {
                        board[r * gridSize + c] = 0
                    }
                }

                // Faz 41: checkGameOver() ONCEDEN placeShape'in sonunda, SENKRON
                // cagriliyordu — ama satir/sutun temizligi (yukaridaki board sifirlama)
                // bu async coroutine icinde ~250ms GECIKMEYLE gerceklesiyor (glow efekti
                // icin). Sonuc: buyuk bir parca o an tahtada yer bulamiyor olsa bile,
                // ayni hamlede baska bir parca satir/sutun temizleyip o parcaya yer
                // acacak olsa DAHI, checkGameOver() henuz TEMIZLENMEMIS eski tahtaya
                // bakip yanlislikla "oyun bitti" diyordu (kullanici geri bildirimi:
                // "büyük parçaya yer açılacak olmasına rağmen oyun bitti diyor").
                // Artik temizleme SATIRLARI GERCEKTEN calistiktan SONRA, guncel tahtaya
                // gore kontrol ediliyor (asagidaki totalLinesCleared==0 dalindaki
                // senkron cagri artik SADECE temizlenen satir/sutun olmadigi durumda calisiyor).
                // isLevelComplete/isGameOver korumasi: bu coroutine placeShape'in geri
                // donusunden (ve olasi erken "return"unden) BAGIMSIZ calisir — seviye zaten
                // TAMAMLANMIS veya oyun zaten BITMISSE tekrar bir "oyun bitti" tetiklemesin.
                if (!isLevelComplete && !isGameOver) checkGameOver()

                dragCoroutineScope.launch {
                    particleProgress.snapTo(0f)
                    // KRITIK: LinearEasing sart. Cizim tarafi bu ilerlemeyi
                    // GERCEK ZAMAN olarak kullanip fizigi (v*t + 0.5*g*t^2)
                    // hesapliyor; egrili bir tween zamani bukup yercekimini
                    // yanlis gosterirdi. Eski kod tween(500) ile varsayilan
                    // FastOutSlowIn kullaniyordu.
                    particleProgress.animateTo(
                        1f,
                        animationSpec = tween(PARTICLE_WINDOW_MS, easing = LinearEasing)
                    )
                }

                // Kombo metni: olculen referans girisi ~100ms overshoot'lu.
                // Eskiden 1.5x'ten yaya birakiliyordu (sadece kuculme). Artik
                // kucukten overshoot ile GIRIYOR, sonra elastik oturuyor.
                // Yay: Compose varsayilani stiffness=1500 kutlama icin fazla
                // keskin — 400/0.5 elastic.easeOut'a cok daha yakin.
                dragCoroutineScope.launch {
                    comboTextScale.snapTo(0.62f)
                    comboTextScale.animateTo(1.12f, animationSpec = tween(100, easing = EaseOutBackBoom))
                    comboTextScale.animateTo(
                        1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
                    )
                }

                // Faz 107 (urun karari): ekran sarsintisi artik SADECE gercekten
                // buyuk anlarda. Referansta (Block Blast) sarsinti SIFIR — tahta
                // paneli 6x zoom'da birebir sabit — ama tamamen kaldirmak yerine
                // karakterimizi koruyup esigi yukselttik: eskiden 2 satir bile
                // (totalLinesCleared > 1) sarsiyordu, yani neredeyse her kombo
                // ekrani titretiyordu. Artik 3+ satir VEYA 3x+ kombo.
                if (totalLinesCleared >= 3 || comboCount >= 3) {
                    dragCoroutineScope.launch {
                        // Eiserloh modeli: siddet DOGRUSAL degil, trauma^2 ile
                        // artar — kucuk anlarda neredeyse hissedilmez, buyuk anda
                        // vurur. Tavan 24f -> 20f'ye cekildi (urun karari: buyuk
                        // an icin bile "bir tik azalt").
                        val shakeSteps = maxOf(totalLinesCleared - 2, comboCount - 2).coerceIn(1, 4)
                        val trauma = shakeSteps / 4f
                        val amplitude = 20f * (0.35f + 0.65f * trauma * trauma)
                        shakeOffset.snapTo(0f)
                        shakeOffset.animateTo(amplitude, animationSpec = tween(40))
                        shakeOffset.animateTo(-amplitude, animationSpec = tween(60))
                        shakeOffset.animateTo(amplitude * 0.6f, animationSpec = tween(60))
                        shakeOffset.animateTo(-amplitude * 0.6f, animationSpec = tween(60))
                        shakeOffset.animateTo(0f, animationSpec = tween(60))
                    }
                }
            }
        } else {
            comboCount = 0
            lastClearedText = ""
        }

        // Remove used shape from tray
        trayShapes[shapeIndex] = null

        // Faz 72: sadece hedef puani gecmek yetmiyor artik — kullanici geri
        // bildirimi: "98 puandayken 1x2 koydun ama patlama yoksa devam etmeli."
        // totalLinesCleared>0 sarti eklendi: hedef, SADECE bu hamlede en az bir
        // satir/sutun gercekten patladiysa tamamlaniyor (hedefi onceki, patlamasiz
        // bir hamleyle gecmis olmak tek basina yetmiyor, bir sonraki patlamaya kadar
        // oyun devam ediyor).
        if (!isEndless && !isLevelComplete && score >= targetScore && totalLinesCleared > 0) {
            isLevelComplete = true
            // Ses artik yukaridaki LaunchedEffect'te, ritual bitince calar
            // (bkz. Faz 107b notu) — patlama sesinin uzerine binmesin.
            onLevelComplete(score, computeStars(score, targetScore))
            return
        }

        // If all 3 shapes used, spawn 3 new
        if (trayShapes.all { it == null }) {
            generateNewTray()
        }

        // Faz 41: satir/sutun temizlendiginde checkGameOver() artik yukarida
        // (temizleme coroutine'inin SONUNDA, gercek tahta durumuna gore) cagriliyor.
        // Burada SADECE temizlenen bir satir/sutun YOKSA (async gecikme de yok)
        // senkron kontrol yapiliyor.
        if (totalLinesCleared == 0) {
            checkGameOver()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackgroundBrush)
            .onGloballyPositioned { rootOriginPx = it.positionInRoot() }
            .padding(16.dp)
    ) {
        // Faz 22: kok zemin onceden tamamen duz/statik renkti (tasarim onerisi:
        // "hafif atmosferik arka plan hareketi"). Cok yavas kayan, dusuk kontrastli
        // iki blob — pil/performans dostu olmasi icin tek paylasilan ambientPhase'e bagli.
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Zaten cizim lambda'si icinde okunuyordu (dogru desen) — sadece isim
            // degisti, davranis ayni.
            val ambientPhase = ambientPhaseState.value
            val twoPi = (2.0 * Math.PI).toFloat()
            val cx1 = size.width * (0.3f + 0.2f * sin(ambientPhase * twoPi))
            val cy1 = size.height * (0.2f + 0.15f * cos(ambientPhase * twoPi * 0.7f))
            drawCircle(
                color = uiSkin.accentGradient[0].copy(alpha = 0.09f),
                radius = size.minDimension * 0.55f,
                center = Offset(cx1, cy1)
            )
            val cx2 = size.width * (0.7f - 0.2f * cos(ambientPhase * twoPi * 0.6f))
            val cy2 = size.height * (0.8f - 0.15f * sin(ambientPhase * twoPi * 0.9f))
            drawCircle(
                color = uiSkin.accentGradient.getOrElse(1) { uiSkin.accentGradient[0] }.copy(alpha = 0.08f),
                radius = size.minDimension * 0.5f,
                center = Offset(cx2, cy2)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar Header
            // Faz 95c: kullanici "üst nav bar çok geniş, alttaki oyun alanını
            // daraltıyor" dedi — dikey padding daha da sikilastirildi.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (!isGameOver && !isLevelComplete) {
                            showExitConfirmDialog = true
                        } else {
                            exitGame()
                        }
                    },
                    modifier = Modifier.testTag("block_blast_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                // Faz 110b: Header yeniden duzenlemeldi — Level/Endless metni orta konuma,
                // TEMA butonu Settings menüsüne tasindi. [Back] [LEVEL] [Settings]
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isEndless) {
                            language.pick(tr = "SONSUZ", en = "ENDLESS", it = "INFINITA", fr = "INFINI", es = "INFINITO")
                        } else {
                            language.pick(tr = "SEVİYE $levelNumber", en = "LEVEL $levelNumber", it = "LIVELLO $levelNumber", fr = "NIVEAU $levelNumber", es = "NIVEL $levelNumber")
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // Settings button — oyun icinde Ayarlar'a (ses/tema/dil) erisim
                IconButton(
                    onClick = { showSettingsDialog = true },
                    modifier = Modifier.size(36.dp).testTag("block_blast_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Faz 112: HUD 2×3 Grid Redesign
            // Grid layout: invisible 2×3
            //   A1 (bomb x1)  | B1/B2 (SKOR)    | C1 (empty)
            //   A2 (plus x1)  |                 | C2 (/100)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                // Left Column A1/A2: Power-ups (bomb, plus) — vertical stack
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    var isFirstBooster = true
                    BoosterType.entries.forEach { type ->
                        val owned = availableBoosterCounts[type] ?: 0
                        // Faz 115f: emoji yerine gercek asset ikonlari
                        // (drawable-nodpi/icon_bomb.webp, icon_line_clear.webp).
                        val boosterIconRes = when (type) {
                            BoosterType.BOMB -> R.drawable.icon_bomb
                            BoosterType.LINE_CLEAR -> R.drawable.icon_line_clear
                        }

                        // Add 8dp spacing between booster items
                        if (!isFirstBooster) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (owned > 0) {
                            isFirstBooster = false
                            val isArmed = armedBooster == type
                            Surface(
                                color = if (isArmed) NeonGreen.copy(alpha = 0.3f) else palette.card,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isArmed) 2.dp else 1.dp,
                                        color = if (isArmed) NeonGreen else palette.cardBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        armedBooster = if (isArmed) null else type
                                    }
                                    .testTag("booster_button_${type.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(boosterIconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "x$owned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                                }
                            }
                        } else if (onWatchAdForBooster != null) {
                            Surface(
                                color = palette.card,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, palette.cardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        onWatchAdForBooster(type) {
                                            availableBoosterCounts[type] = (availableBoosterCounts[type] ?: 0) + 1
                                        }
                                    }
                                    .testTag("booster_watch_ad_${type.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(boosterIconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "▶️+1", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
                                }
                            }
                        }
                    }
                }

                // Faz 115: SKOR — v11 gorsel yonu.
                //
                // Faz 112'nin matematiksel merkezlemesi KORUNDU (Box fillMaxWidth +
                // contentAlignment = Center). O, booster sayisi ya da hedef metni
                // uzadikca skorun kaymasi sorununu cozmustu; buradaki degisiklik
                // yalnizca skorun GORUNUMU — konumlandirma mantigina dokunulmadi.
                //
                // Degisen: duz beyaz sayi -> altin dikey gradyan + yumusak isima +
                // altinda kucuk "SCORE" etiketi. Mockup'ta skoru "ekranin kahramani"
                // yapan sey buydu; ayni sayi, ayni yer, farkli agirlik.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer {
                                val s = scoreScale.value
                                scaleX = s
                                scaleY = s
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$animatedScore",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.ExtraBold,
                            // Faz 115k — HATA DUZELTMESI (kullanici: "açık modda skor
                            // okunmuyor rahat bir sekilde"). Kok sebep: en ust gradyan
                            // durağı neredeyse beyaza yakindi (#FFF3C4) ve golge SICAK/
                            // ALTIN renkteydi (#FFB300) — koyu lacivert zeminde iyi
                            // calisiyordu ama oyun ici acik-pastel gradyan zeminlerde
                            // (bu ekran gorunumunun sebebi) acik-uzerine-acik'e cok
                            // yakindi. Gradyan durağı doygunlastirildi (hicbir durak
                            // beyaza yakin degil) ve golge SICAK'tan KOYU/kontrast
                            // saglayan bir tona cevrildi, kucuk bir offset'le "gercek"
                            // bir golge gibi zemine oturuyor — hem acik hem koyu
                            // arka planda calisir.
                            style = androidx.compose.ui.text.TextStyle(
                                // Faz 115p — HATA DUZELTMESI (kullanici: "mode secim
                                // kartlarini guncellememissin" — ayni hata burada da
                                // vardi). Kok sebep: `Text()`e DOGRUDAN bir `TextStyle(...)`
                                // verildiginde bu, `LocalTextStyle.current`in (Fredoka'nin
                                // aktigi yer) YERINE geciyor, ustune merge OLMUYOR — o
                                // yuzden fontFamily alani bos kalip Android'in sistem
                                // varsayilanina (Roboto) duşuyordu. Tum uygulamada boyle
                                // dort yer bulundu, hepsine acikca fontFamily verildi.
                                fontFamily = AppFontFamily,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFD54F),
                                        Color(0xFFFFB300),
                                        Color(0xFFE65100)
                                    )
                                ),
                                shadow = Shadow(
                                    color = Color(0xFF4A2600).copy(alpha = 0.55f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        Text(
                            text = language.pick(tr = "SKOR", en = "SCORE", it = "PUNTI", fr = "SCORE", es = "PUNTOS"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = NeonCyan.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                // Faz 115: HEDEF — ciplak "/100" yerine gercek bir rozet.
                // Mockup'ta hedef, skorun rakibi olarak ayri bir kart halinde
                // duruyor; oyuncu "kac kaldi" bilgisini tek bakista aliyor.
                if (!isEndless) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        NeonPurple.copy(alpha = 0.42f),
                                        NeonPurple.copy(alpha = 0.16f)
                                    )
                                )
                            )
                            .border(1.dp, NeonPurple.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = language.pick(tr = "HEDEF", en = "TARGET", it = "OBIETTIVO", fr = "OBJECTIF", es = "OBJETIVO"),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$targetScore",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Faz 22: COMBO (sonsuz mod'da) — positioned at right side
                if (isEndless) {
                    val isOnStreak = comboCount >= 3
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOnStreak) Color(0xFFFF6B35).copy(alpha = 0.16f) else palette.card
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 16.dp, top = 8.dp)
                            .width(80.dp)
                            .then(
                                if (isOnStreak) {
                                    Modifier.drawPhaseRoundedBorder(
                                        width = 1.5.dp,
                                        radius = 12.dp,
                                        brush = streakCardBorderBrush
                                    ) { sharedPulseState.value }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = language.pick(tr = "🔥 KOMBO", en = "🔥 COMBO", it = "🔥 COMBO", fr = "🔥 COMBO", es = "🔥 COMBO"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (comboCount >= 2) NeonGold else NeonCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (comboCount > 0) "${comboCount}x" else "—",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (comboCount >= 2) NeonGold else palette.textPrimary
                            )
                        }
                    }
                }
            }  // Close outer Box (HUD container)


            // Faz 66: "SUPER! +16 / 2x KOMBO" yazisi burada (Column'un normal akisinda)
            // duruyordu — gorununce yer kapliyor, grid `weight(1f)` ile kalan alani
            // kullandigi icin metin her belirdiginde grid GORUNUR SEKILDE kucaliyor/
            // sicriyordu (kullanici ekran goruntusuyle bildirdi). Metin artik asagida,
            // grid'in KENDI BoxWithConstraints'i icinde bir overlay katmani olarak
            // (mevcut "+N" puan yazilari/parcacik efektleriyle AYNI desen) TopCenter'a
            // hizalanmis sekilde ciziliyor — grid'in olcumunu hic etkilemiyor, gorunse
            // de gorunmese de grid boyutu SABIT kaliyor.

            // 8x8 Main Grid
            // Faz 40: onceden .fillMaxWidth().aspectRatio(1f) ile SADECE genislige gore
            // kare boyutlaniyordu — banner reklam altta yer kaplayinca Column'un toplam
            // yuksekligi asiliyor, en sonda kalan tepsi (sonraki 3 parca) ekran disina
            // tasip goze neredeyse gorunmez ince seritler halinde kaliyordu (kullanici
            // geri bildirimi: "sıradaki bloklar gözükmüyor reklam çıkınca"). Artik grid
            // Column icinde weight(1f) ile SADECE kalan (header/skor/tepsi cikarilmis)
            // dikey alani aliyor, BoxWithConstraints ile o alanin hem genislik hem
            // yukseklik siniri arasindaki KUCUK olan kare kenari olarak kullaniliyor —
            // boylece reklam yer actikca daralan tek eleman grid oluyor, tepsi sabit
            // boyutunu (110dp) hep koruyor.
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
            val gridSide = minOf(maxWidth, maxHeight)

            // Faz 3: BoardGeometry hesapla (satır offset + padding hisset)
            val boardGeometry = remember(gridSide, density) {
                val sidePx = with(density) { gridSide.toPx() }
                // Card 6.dp padding icindedir, ama geometry tahta camiasinin (Card'dan sonra)
                // icindeki hucreler icin hesaplanmalidir — topLeft = Card padding sonrasi.
                val paddingPx = with(density) { 6.dp.toPx() }
                val availableSidePx = sidePx - 2 * paddingPx
                createBoardGeometry(
                    screenWidthPx = availableSidePx,
                    screenHeightPx = availableSidePx,
                    headerHeightPx = 0f, // Tahta Card'in icinde, header yok
                    gridRowCount = gridSize,
                    gridColCount = gridSize,
                    horizontalPaddingPx = 0f, // Padding Card'ta, burada yok
                    safeInsetTop = 0f,
                    safeInsetBottom = 0f
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = animatedCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(gridSide)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .border(2.dp, Brush.linearGradient(uiSkin.accentGradient), RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
              Box(
                modifier = Modifier.fillMaxSize()
              ) {
                // Faz 3: Tahta Canvas'i — 64 hucre + glowingRows/Cols + clear glow + sparkles
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { coords ->
                            gridOriginPx = coords.positionInRoot()
                            cellSizePx = coords.size.width / gridSize.toFloat()
                        }
                        // Faz 115d — HATA DUZELTMESI (kullanici cihazda buldu:
                        // "aldigim guclendiriciyi secsem de kullanamiyorum").
                        //
                        // Kok sebep: `applyBoosterAt()` tanimliydi ve dogru
                        // calisiyordu, ama TUM DOSYADA HIC CAGRILMIYORDU — tek
                        // gecisi kendi tanimiydi. Yani guclendirici butonu
                        // `armedBooster`i set ediyor, sonra hicbir sey olmuyordu.
                        //
                        // Ne zaman kirildi: `c8e6cf2` "Faz 3: Tahta rendering 64
                        // Compose Box -> Canvas.drawPath tasimasi". O gecis her
                        // hucrenin ayri Composable Box'ini tek bir Canvas'a
                        // indirdi; hucrelerin uzerindeki tiklama isleyicileri de
                        // onlarla birlikte gitti ve cagri yeniden baglanmadi.
                        // Cizim performansi kazanildi, ozellik sessizce oldu.
                        //
                        // pointerInput anahtari `armedBooster`: hicbir
                        // guclendirici secili degilken bu isleyici dokunmalari
                        // HIC yakalamaz, yani normal surukleme akisi etkilenmez.
                        .pointerInput(armedBooster) {
                            if (armedBooster == null) return@pointerInput
                            detectTapGestures { offset ->
                                val cell = size.width / gridSize.toFloat()
                                if (cell <= 0f) return@detectTapGestures
                                val col = (offset.x / cell).toInt().coerceIn(0, gridSize - 1)
                                val row = (offset.y / cell).toInt().coerceIn(0, gridSize - 1)
                                applyBoosterAt(row, col)
                            }
                        }
                ) {
                    val cellSize = size.width / gridSize
                    val cellPaddingPx = with(drawContext.density) { 0.5.dp.toPx() }
                    val cornerRadiusPx = with(drawContext.density) { 6.dp.toPx() }
                    // Faz 115: trapez bevel'ler kaldirildi (bkz. asagida "FASETLI /
                    // PARLAK BLOK"), bevelRatio artik kullanilmiyor.
                    // Faz 115s — HATA DUZELTMESI (kullanici ekran goruntusuyle
                    // gosterdi: Meyve/Sekerleme temasi secilince emoji'ler DEV
                    // boyutta, birden fazla hucreyi kaplayip capraz tasiyordu).
                    // Kok sebep: bu Canvas TUM TAHTAYI kapliyor (`size` = butun
                    // izgaranin boyutu, tek hucrenin degil) — `size.minDimension`
                    // burada tahtanin kenar uzunlugu demek, tek hucrenin degil.
                    // Onceden bu satir `size.minDimension * 0.6f` idi, yani
                    // "tahtanin %60'i" buyuklugunde font — TEPSI onizlemesindeki
                    // (EmbossedBlockCell) AYNI GORUNEN satirla karistirilmis
                    // olmali, orada Canvas gercekten TEK hucreye ozgu oldugu icin
                    // dogru calisiyordu. Burada dogrusu hucre boyutunun (`cellSize`,
                    // hemen yukarida) %60'i.
                    val fontSizePx = cellSize * 0.6f

                    // Faz 3: Tum 64 hucre dongusu
                    for (r in 0 until gridSize) {
                        for (c in 0 until gridSize) {
                            val cellIndex = r * gridSize + c
                            val cellVal = board[cellIndex]
                            val topLeftX = c * cellSize + cellPaddingPx
                            val topLeftY = r * cellSize + cellPaddingPx
                            val cellDrawSize = cellSize - 2 * cellPaddingPx

                            val inDragFootprint = isCellInDragFootprint(r, c)
                            val dropValid = if (inDragFootprint) isCurrentDropValid() else false

                            // 1. Empty cell background (hucre bos ve drag degil)
                            if (cellVal == 0 && !inDragFootprint) {
                                drawRect(
                                    color = palette.emptyCell,
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize)
                                )
                            }

                            // 2. Faz 115: FASETLI / PARLAK BLOK (v11 gorsel yonu)
                            //
                            // Onceki hal: kare `drawRect` govde + DORT trapez `Path`
                            // (ust/sol/sag/alt bevel) + ic yuz + shimmer. Bloklar
                            // BITISIKTI ve koseleri keskindi; mockup'ta ise her blok
                            // kendi basina duran, yuvarlatilmis, camsi bir nesne gibi
                            // okunuyor. Fark tarif edilebilir dort kuraldan ibaret:
                            //   a) bloklar arasinda gorunur bosluk (inset)
                            //   b) govdede dikey gradyan: ust acik -> alt koyu (hacim)
                            //   c) ust yarida yumusak parlaklik (cam/seker)
                            //   d) ic kenar ustte isik, altta golge (fasetli kenar)
                            //   + disarida hafif renkli glow (neon)
                            //
                            // PERFORMANS: eskisi hucre basina 7 cizim ve HER KAREDE 4
                            // adet `Path` nesnesi allocate ediyordu (64 hucre x 60 fps
                            // = kare basina 256 Path). Yeni hal 4 cizim, SIFIR Path
                            // allocation. Yani bu degisiklik daha ucuz — Faz 112'de
                            // performans icin FloatingScoreManager kaldirilmisti, o
                            // karara ters dusmuyor.
                            if (cellVal > 0) {
                                val baseColor = BLOCK_COLORS.getOrElse((cellVal - 1).coerceAtLeast(0)) { NeonCyan }

                                val inset = cellDrawSize * 0.055f
                                val bx = topLeftX + inset
                                val by = topLeftY + inset
                                val bs = (cellDrawSize - 2 * inset).coerceAtLeast(1f)
                                val br = androidx.compose.ui.geometry.CornerRadius(bs * 0.26f, bs * 0.26f)

                                val faceTop = lerp(baseColor, Color.White, 0.30f)
                                val faceBottom = lerp(baseColor, Color.Black, 0.34f)

                                // a) dis glow — tek halka. Gercek blur (RenderEffect)
                                // bu hucre sayisinda pahali; genisletilmis yumusak
                                // kontur ayni izlenimi bedelsiz veriyor.
                                drawRoundRect(
                                    color = baseColor.copy(alpha = 0.18f),
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(br.x + inset),
                                    style = Stroke(width = with(drawContext.density) { 2.dp.toPx() })
                                )

                                // b) govde — dikey gradyan
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(faceTop, baseColor, faceBottom),
                                        startY = by,
                                        endY = by + bs
                                    ),
                                    topLeft = Offset(bx, by),
                                    size = Size(bs, bs),
                                    cornerRadius = br
                                )

                                // c) ust parlaklik. Mevcut shimmer animasyonu KORUNDU
                                // (Faz'lardan beri suren "canlilik"); yalnizca duz ic
                                // dikdortgen yerine yuvarlatilmis ve daralmis halde.
                                val shimmerAlpha = 0.26f + 0.10f * sin(shimmerPhaseState.value + (r + c) * 0.4f)
                                val glossH = bs * 0.46f
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = shimmerAlpha.coerceIn(0.14f, 0.40f)),
                                            Color.Transparent
                                        ),
                                        startY = by,
                                        endY = by + glossH
                                    ),
                                    topLeft = Offset(bx + bs * 0.08f, by + bs * 0.06f),
                                    size = Size(bs * 0.84f, glossH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(br.x * 0.8f)
                                )

                                // d) ic kenar — ustte isik, altta golge
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.55f),
                                            Color.White.copy(alpha = 0.05f),
                                            Color.Black.copy(alpha = 0.30f)
                                        ),
                                        startY = by,
                                        endY = by + bs
                                    ),
                                    topLeft = Offset(bx, by),
                                    size = Size(bs, bs),
                                    cornerRadius = br,
                                    style = Stroke(width = with(drawContext.density) { 1.2.dp.toPx() })
                                )

                                // Tema emoji (FRUIT/SWEETS)
                                val emoji = when (currentTheme.uppercase()) {
                                    "FRUIT" -> when (cellVal % 6) {
                                        1 -> "🍉"
                                        2 -> "🍊"
                                        3 -> "🥝"
                                        4 -> "🍓"
                                        5 -> "🧀"
                                        0 -> "🍇"
                                        else -> ""
                                    }
                                    "SWEETS" -> when (cellVal % 6) {
                                        1 -> "🍩"
                                        2 -> "🍫"
                                        3 -> "🍪"
                                        4 -> "🧁"
                                        5 -> "🍬"
                                        0 -> "🧇"
                                        else -> ""
                                    }
                                    else -> ""
                                }

                                if (emoji.isNotEmpty()) {
                                    val paint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = fontSizePx
                                    }
                                    val fm = paint.fontMetrics
                                    val textY = topLeftY + cellDrawSize / 2f - (fm.ascent + fm.descent) / 2f
                                    drawContext.canvas.nativeCanvas.drawText(emoji, topLeftX + cellDrawSize / 2f, textY, paint)
                                }
                            }

                            // 3. Near-miss pulse (nabiz) — satir/sutun 1 hucre eksik
                            if (cellVal > 0 && cellIndex in nearMissCells && cellIndex !in glowingClearCells) {
                                drawRoundRect(
                                    color = NeonGold.copy(alpha = sharedPulseState.value),
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = with(drawContext.density) { 1.5.dp.toPx() })
                                )
                            }

                            // 4. Clear glow (patlamaya hazir satir/sutun)
                            if (cellIndex in glowingClearCells) {
                                // Inner dolgu
                                drawRect(
                                    color = clearAccentColor.copy(alpha = (glowPulse.value * 0.8f).coerceIn(0f, 1f)),
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize)
                                )

                                // Border
                                drawRoundRect(
                                    color = Color.White.copy(alpha = (glowPulse.value * 0.9f).coerceIn(0f, 1f)),
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = with(drawContext.density) { 2.dp.toPx() })
                                )

                                // Sparkle (her 3. hucrede)
                                if ((r + c) % 3 == 0) {
                                    val sparkText = "✨"
                                    val sparkPaint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        textSize = with(drawContext.density) { 12.sp.toPx() }
                                        alpha = (glowPulse.value.coerceIn(0f, 1f) * 255).toInt()
                                    }
                                    val fm = sparkPaint.fontMetrics
                                    val sparkY = topLeftY + 4.dp.toPx() - (fm.ascent + fm.descent) / 2f
                                    drawContext.canvas.nativeCanvas.drawText(sparkText, topLeftX + cellDrawSize - 4.dp.toPx(), sparkY, sparkPaint)
                                }
                            }

                            // 5. Drag footprint tint (suruklenirken gosterilen alan)
                            if (inDragFootprint) {
                                val tint = if (dropValid) NeonGreen else Color(0xFFF87171)
                                drawRect(
                                    color = tint.copy(alpha = 0.55f),
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize)
                                )
                                drawRoundRect(
                                    color = tint,
                                    topLeft = Offset(topLeftX, topLeftY),
                                    size = Size(cellDrawSize, cellDrawSize),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                    style = Stroke(width = with(drawContext.density) { 1.dp.toPx() })
                                )
                            }

                            // 6. Cell border (Compose border simulasyonu)
                            val borderColor = if (cellVal > 0) palette.background.copy(alpha = 0.35f) else palette.cardBorder
                            drawRoundRect(
                                color = borderColor,
                                topLeft = Offset(topLeftX, topLeftY),
                                size = Size(cellDrawSize, cellDrawSize),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx, cornerRadiusPx),
                                style = Stroke(width = with(drawContext.density) { 0.5.dp.toPx() })
                            )
                        }
                    }

                    // Faz 50 + Faz 3: Glowing row/col cerceveleri (patlama beklentisi)
                    if (glowingRows.isNotEmpty() || glowingCols.isNotEmpty()) {
                        val glowStrokeWidth = with(drawContext.density) { 3.dp.toPx() }
                        val glowColor = clearAccentColor.copy(alpha = glowPulse.value.coerceIn(0f, 1f))

                        glowingRows.forEach { r ->
                            drawRoundRect(
                                color = glowColor,
                                topLeft = Offset(glowStrokeWidth / 2, r * cellSize + glowStrokeWidth / 2),
                                size = androidx.compose.ui.geometry.Size(size.width - glowStrokeWidth, cellSize - glowStrokeWidth),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(with(drawContext.density) { 8.dp.toPx() }),
                                style = Stroke(width = glowStrokeWidth)
                            )
                        }
                        glowingCols.forEach { c ->
                            drawRoundRect(
                                color = glowColor,
                                topLeft = Offset(c * cellSize + glowStrokeWidth / 2, glowStrokeWidth / 2),
                                size = androidx.compose.ui.geometry.Size(cellSize - glowStrokeWidth, size.height - glowStrokeWidth),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(with(drawContext.density) { 8.dp.toPx() }),
                                style = Stroke(width = glowStrokeWidth)
                            )
                        }
                    }
                }

                // Faz 106 (TODO 12): patlama sok dalgasi — patlayan satirin/sutunun
                // boyunca disa acilip sonen bir halka. Faz 50 cercevesiyle ayni
                // geometri, ama patlamadan SONRA calisiyor: genisliyor, inceliyor,
                // soneriyor. Flash (350ms) sonerken dalga (420ms) hala aciliyor,
                // ust uste binince cizgi "itiliyor" gibi gorunuyor.
                if (shockRows.isNotEmpty() || shockCols.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cell = size.width / gridSize
                        val p = shockProgress.value
                        val spread = p * cell * 0.55f
                        val stroke = (4.dp.toPx() * (1f - p)).coerceAtLeast(0.5f)
                        val waveColor = clearAccentColor.copy(alpha = (1f - p) * 0.9f)
                        val waveCorner = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                        shockRows.forEach { r ->
                            drawRoundRect(
                                color = waveColor,
                                topLeft = Offset(-spread, r * cell - spread),
                                size = androidx.compose.ui.geometry.Size(size.width + spread * 2, cell + spread * 2),
                                cornerRadius = waveCorner,
                                style = Stroke(width = stroke)
                            )
                        }
                        shockCols.forEach { c ->
                            drawRoundRect(
                                color = waveColor,
                                topLeft = Offset(c * cell - spread, -spread),
                                size = androidx.compose.ui.geometry.Size(cell + spread * 2, size.height + spread * 2),
                                cornerRadius = waveCorner,
                                style = Stroke(width = stroke)
                            )
                        }
                    }
                }

                // Faz 107 (Kademe B-1): ISIK HUZMESI + temizlenen hucre flash'i.
                // Referans olcumu: hucreler tek karede yok oluyor, yerlerinde
                // ~690ms yasayan bir isik seridi kaliyor — patlamanin asil
                // gorsel govdesi bu. Iki asama:
                //   1) ~0-205ms  : dar (~1/3 hucre), keskin, BEYAZ cekirdek (additive)
                //   2) ~140-690ms: tam hucre genisligine acilan yumusak altin bant
                // Sifir varlik: sadece transparent->renk->transparent gradyan
                // dikdortgenler. Modifier.blur KULLANILMIYOR — API 31+ ve
                // minSdk 24'te sessizce yok sayiliyor; yumusaklik gradyandan geliyor.
                if (beamRows.isNotEmpty() || beamCols.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // TUM animasyon degerleri draw lambda'si icinde okunuyor:
                        // boylece her karede sadece YENIDEN CIZIM olur, recomposition
                        // olmaz.
                        val bp = beamProgress.value
                        if (bp >= 1f) return@Canvas
                        val cell = size.width / gridSize
                        // Cekirdek ilk %30'da soner (690ms * 0.30 = ~207ms).
                        val core = (1f - bp / 0.30f).coerceIn(0f, 1f)
                        // Bant %18'den itibaren acilir ve sona kadar soner.
                        val wide = ((bp - 0.18f) / 0.82f).coerceIn(0f, 1f)
                        val fade = 1f - wide
                        val bandHalf = cell * (0.34f + 0.66f * wide) * 0.5f
                        val bandAlpha = fade * fade * 0.55f
                        val coreHalf = cell * 0.30f * 0.5f
                        val bandColor = clearAccentColor.copy(alpha = bandAlpha)
                        val coreColor = Color.White.copy(alpha = core)
                        val clear = Color.Transparent

                        // Patlama anindaki hucre flash'i (110ms) — eskiden hucre
                        // basina bir Box'ti, artik burada.
                        val fa = clearFlashAlpha.value
                        if (fa > 0f) {
                            val flashColor = clearAccentColor.copy(alpha = (fa * 0.85f).coerceIn(0f, 1f))
                            val inset = 1.5.dp.toPx()
                            val corner = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                            recentlyClearedCells.forEach { index ->
                                val fr = index / gridSize
                                val fc = index % gridSize
                                drawRoundRect(
                                    color = flashColor,
                                    topLeft = Offset(fc * cell + inset, fr * cell + inset),
                                    size = Size(cell - inset * 2, cell - inset * 2),
                                    cornerRadius = corner
                                )
                            }
                        }

                        beamRows.forEach { r ->
                            val cy = (r + 0.5f) * cell
                            if (bandAlpha > 0.004f) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(clear, bandColor, clear),
                                        startY = cy - bandHalf,
                                        endY = cy + bandHalf
                                    ),
                                    topLeft = Offset(0f, cy - bandHalf),
                                    size = Size(size.width, bandHalf * 2f)
                                )
                            }
                            if (core > 0.004f) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(clear, coreColor, clear),
                                        startY = cy - coreHalf,
                                        endY = cy + coreHalf
                                    ),
                                    topLeft = Offset(0f, cy - coreHalf),
                                    size = Size(size.width, coreHalf * 2f),
                                    blendMode = BlendMode.Plus
                                )
                            }
                        }
                        beamCols.forEach { c ->
                            val cx = (c + 0.5f) * cell
                            if (bandAlpha > 0.004f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(clear, bandColor, clear),
                                        startX = cx - bandHalf,
                                        endX = cx + bandHalf
                                    ),
                                    topLeft = Offset(cx - bandHalf, 0f),
                                    size = Size(bandHalf * 2f, size.height)
                                )
                            }
                            if (core > 0.004f) {
                                drawRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(clear, coreColor, clear),
                                        startX = cx - coreHalf,
                                        endX = cx + coreHalf
                                    ),
                                    topLeft = Offset(cx - coreHalf, 0f),
                                    size = Size(coreHalf * 2f, size.height),
                                    blendMode = BlendMode.Plus
                                )
                            }
                        }
                    }
                }

                // Faz 4: Ambient toz + Radyal isin overlay. OLAY: her karede sadece
                // redraw (recomposition yok). drawLambda icinden canvas.size.width/height
                // kullaniyoruz — piksel, hucre degil. Faz 3'ten farkli olarak burada
                // tahta grid'i yoktur, sadece tam ekran FX.
                if (radialFlash != null) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val screenWidth = size.width
                        val screenHeight = size.height


                        // RADYAL ISIN: patlama tetiklemesinde gosteriliyor (300-400ms)
                        if (radialFlash != null) {
                            val flash = radialFlash!!
                            val p = flash.progress
                            // progress >= 1 ise artik gorunmez, update loop tarafinda kaldiriliyor
                            if (p < 1f) {
                                val alpha = (1f - p).coerceIn(0f, 1f)
                                val baseLength = maxOf(screenWidth, screenHeight)
                                val rayLength = baseLength * p

                                repeat(flash.beamCount) { i ->
                                    val angle = (360f / flash.beamCount) * i + flash.rotationOffset
                                    val theta = Math.toRadians(angle.toDouble()).toFloat()
                                    val endX = flash.centerX + (rayLength * cos(theta.toDouble())).toFloat()
                                    val endY = flash.centerY + (rayLength * sin(theta.toDouble())).toFloat()

                                    drawLine(
                                        color = NeonGold.copy(alpha = alpha.coerceIn(0f, 1f)),
                                        strokeWidth = with(drawContext.density) { 3.dp.toPx() },
                                        start = Offset(flash.centerX, flash.centerY),
                                        end = Offset(endX, endY)
                                    )
                                }
                            }
                        }
                    }
                }

                // Faz 51 + Faz 106 (TODO 11): surukleme ONIZLEMESI. Faz 51'de
                // tamamlanacak satir/sutunun etrafina yalnizca ince bir CERCEVE
                // ciziliyordu; kullanici Block Blast'ta satirin TAMAMININ, o parcanin
                // rengiyle DOLGULU vurgulandigini bildirdi ("daha senlikli"). Artik
                // cerceveye ek olarak o satir/sutundaki her hucre surukledigin parcanin
                // renginde yari saydam doluyor — ve renk artik sabit yesil degil,
                // parcanin kendi rengi. Boylece "bu parca burayi patlatacak" baglantisi
                // patlamadan ONCE kuruluyor; patlama anindaki karsiligi TODO 12.
                if (draggedTrayIndex != -1) {
                    val (previewRows, previewCols) = previewClearedLines()
                    if (previewRows.isNotEmpty() || previewCols.isNotEmpty()) {
                        val previewBase = activeDragShape()?.let { dragged ->
                            BLOCK_COLORS.getOrElse((dragged.colorIndex - 1).coerceAtLeast(0)) { NeonGreen }
                        } ?: NeonGreen
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cell = size.width / gridSize
                            val strokeWidth = 3.dp.toPx()
                            // Zaten cizim lambda'si icinde okunuyordu (dogru desen).
                            val sharedPulse = sharedPulseState.value
                            val previewColor = previewBase.copy(alpha = sharedPulse)
                            val fillColor = previewBase.copy(alpha = 0.20f + sharedPulse * 0.22f)
                            val fillInset = 1.5.dp.toPx()
                            val fillCorner = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                            // Satir + sutun ayni anda tamamlaniyorsa kesisen hucre iki kez
                            // boyanip iki kat koyu cikardi — once tekil indeks kumesi.
                            val fillCells = mutableSetOf<Int>()
                            previewRows.forEach { r -> for (c in 0 until gridSize) fillCells.add(r * gridSize + c) }
                            previewCols.forEach { c -> for (r in 0 until gridSize) fillCells.add(r * gridSize + c) }
                            fillCells.forEach { index ->
                                val r = index / gridSize
                                val c = index % gridSize
                                // Parcanin kendi ayak izi HARIC — orada zaten yesil/kirmizi
                                // "buraya birakilir mi" gostergesi var, ust uste binince iki
                                // bilgi birden okunamaz hale geliyordu.
                                if (isCellInDragFootprint(r, c)) return@forEach
                                drawRoundRect(
                                    color = fillColor,
                                    topLeft = Offset(c * cell + fillInset, r * cell + fillInset),
                                    size = androidx.compose.ui.geometry.Size(cell - fillInset * 2, cell - fillInset * 2),
                                    cornerRadius = fillCorner
                                )
                            }
                            previewRows.forEach { r ->
                                drawRoundRect(
                                    color = previewColor,
                                    topLeft = Offset(strokeWidth / 2, r * cell + strokeWidth / 2),
                                    size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, cell - strokeWidth),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                            previewCols.forEach { c ->
                                drawRoundRect(
                                    color = previewColor,
                                    topLeft = Offset(c * cell + strokeWidth / 2, strokeWidth / 2),
                                    size = androidx.compose.ui.geometry.Size(cell - strokeWidth, size.height - strokeWidth),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }
                }

                // Faz 50: patlama noktasinda yuzen "+N" puan yazisi.
                //
                // Faz 1 (performans) — BILEREK DOKUNULMADI. `popupProgress` burada
                // hala kompozisyon fazinda okunuyor ve popup yasarken (~700ms) bu
                // scope her karede yeniden deriliyor. Cizim fazina tasimak GORSEL
                // DEGISIKLIK yaratirdi:
                //   - `color = popup.color.copy(alpha = popupAlpha)` sadece GLYPH'i
                //     soldurur; `style.shadow` (siyah, sabit %60) solmaz. Ayni
                //     solmayi `Modifier.graphicsLayer { alpha = ... }` ile yapsaydik
                //     golge de birlikte solardi — mevcut gorunum degisirdi.
                //   - Sadece `Modifier.offset`i lambda'li surume cevirmek kazanc
                //     saglamaz; renk okumasi scope'u zaten her karede gecersiz kilar.
                // Dogru cozumu ayri bir is: popup'lari asagidaki parcacik Canvas'i
                // gibi tek bir cizim katmaninda (nativeCanvas + golge) cizmek.
                // Bu, gorsel-esitlik ispati gerektiren bir yeniden yazimdir.
                if (scorePopups.isNotEmpty()) {
                    val cellDp = if (cellSizePx > 0f) with(density) { cellSizePx.toDp() } else 26.dp
                    val riseFraction = popupProgress.value
                    val popupAlpha = (1f - riseFraction).coerceIn(0f, 1f)
                    scorePopups.forEach { popup ->
                        Text(
                            text = popup.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = popup.color.copy(alpha = popupAlpha),
                            // Faz 115p: bkz. yukaridaki SCORE duzeltmesi yorumu — dogrudan
                            // TextStyle() LocalTextStyle'in yerine geciyor, fontFamily
                            // acikca verilmezse Roboto'ya duşuyordu.
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                shadow = Shadow(color = Color.Black.copy(alpha = 0.6f), offset = Offset(1f, 2f), blurRadius = 3f)
                            ),
                            modifier = Modifier.offset(
                                x = cellDp * popup.col - 14.dp,
                                y = cellDp * popup.row - 10.dp - (16.dp * riseFraction)
                            )
                        )
                    }
                }

                // Faz 107 (Kademe B-2): fizikli parcacik katmani.
                // Onceki hali: `drawCircle(radius = 4f + 3f*(1f-progress))` —
                // (a) HAM PIKSEL yariçap, S8'in 3.0 yogunlugunda ~1.3-2.3dp,
                //     yani parcaciklar neredeyse gorunmuyordu (birim hatasi,
                //     "parcaciklarimiz ilkel" hissinin muhtemelen tek en buyuk
                //     sebebi); (b) hepsi TEK `progress` paylasiyordu — ayni anda
                //     dogup ayni anda oluyorlardi; (c) duz cizgi, yercekimi yok,
                //     donme yok; (d) hepsi daireydi.
                // Simdi: hucre birimli boyut/hiz, parcacik basina gecikme/omur,
                // yukari yonelim + yercekimi + surtunme, 5 farkli tur, donme.
                //
                // Canvas KOSULSUZ var: eskiden `if (... && particleProgress.value < 1f)`
                // ile sarilmisti, yani animasyon degeri KOMPOZISYONDA okunuyordu ve
                // burst boyunca bu Box her karede yeniden derleniyordu. Artik erken
                // cikis draw lambda'sinin ICINDE.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Faz 5: ParticlePool.getActiveCount() ile aktif parçacık sayısı
                    val n = particlePool.getActiveCount()
                    if (n == 0) return@Canvas
                    val t = particleProgress.value * PARTICLE_WINDOW_S
                    if (t >= PARTICLE_WINDOW_S) return@Canvas
                    val cell = size.width / gridSize
                    for (i in 0 until n) {
                        val p = particlePool[i]
                        val lt = t - p.delay
                        if (lt <= 0f || lt >= p.life) continue
                        val f = lt / p.life
                        // Ucgen olcek rampasi: t=0.5'te tam boy, t=1'de sifir.
                        // AYRI bir alpha fade YOK — duz alpha inisinden belirgin
                        // sekilde daha canli duruyor (juice referansi).
                        val grow = if (f < 0.5f) f * 2f else (1f - f) * 2f
                        if (grow <= 0.02f) continue
                        // Surtunme: yatay hiz omur boyunca sonuyor.
                        val cx = (p.x + p.vx * lt * (1f - 0.30f * f)) * cell
                        // Yercekimi: yukari cikip yavaslar, sonra duser.
                        val cy = (p.y + p.vy * lt + 0.5f * PARTICLE_GRAVITY * lt * lt) * cell
                        val sz = p.size * grow * cell
                        if (sz < 0.6f) continue
                        val center = Offset(cx, cy)
                        when (p.kind) {
                            PK_CIRCLE -> drawCircle(p.color, sz * 0.5f, center)
                            PK_MOTE -> drawRect(
                                color = p.color,
                                topLeft = Offset(cx - sz * 0.5f, cy - sz * 0.5f),
                                size = Size(sz, sz)
                            )
                            PK_DIAMOND -> rotate(p.rot + p.spin * lt, center) {
                                drawRect(
                                    color = p.color,
                                    topLeft = Offset(cx - sz * 0.5f, cy - sz * 0.5f),
                                    size = Size(sz, sz)
                                )
                            }
                            PK_STREAK -> rotate(p.rot + p.spin * lt, center) {
                                drawRect(
                                    color = p.color,
                                    topLeft = Offset(cx - sz * 0.5f, cy - sz * 1.1f),
                                    size = Size(sz, sz * 2.2f)
                                )
                            }
                            else -> withTransform({
                                translate(cx, cy)
                                rotate(p.rot, Offset.Zero)
                                scale(sz, sz, Offset.Zero)
                            }) {
                                // Additive: kivilcim hizla beyaza patlar.
                                drawPath(sparkStarPath, p.color, blendMode = BlendMode.Plus)
                            }
                        }
                    }
                }

                // Faz 107: tahta kenari MARQUEE (2x+ komboda tahtanin cevresinde
                // yuruyen sari isik noktalari) burada bulunuyordu ve cihazda
                // dogrulandi — ancak URUN KARARI ile kaldirildi: "gereksiz, goz
                // yoruyor". Referansta (Block Blast) mevcut olmasi tek basina
                // gerekce degil; kullanici karari ustundur.
                // NOT: bunu `nearMissCells` ile KARISTIRMA — o da NeonGold'dur
                // ama tahta cevresinde DONMEZ, satir/sutun tam 1 hucre eksikken
                // YERINDE nabiz atar ve dekorasyon degil OYNANIS ipucudur
                // (bkz. yukaridaki hucre dongusu, Faz 22). O korunuyor.

                // Faz 66: Combo Text Banner — kombo arttıkça büyüyor ve renk değiştiriyor.
                // Grid'in olcumunu etkilememesi icin (bkz. yukaridaki not) burada, grid'in
                // KENDI Box'i icinde bir overlay olarak, TopCenter'a hizalanmis sekilde
                // ciziliyor — hicbir zaman grid'i sikistirmiyor/kaydirmiyor.
                if (lastClearedText.isNotEmpty()) {
                    // Bu oyunda "patlama" HER ZAMAN olumlu bir olay — kirmizi/magenta
                    // gibi "hata/tehlike" hissi veren bir renk asla kullanilmamali
                    // (kullanici geri bildirimi: tekli patlama "kotu bir sey olmus gibi"
                    // gorunuyordu). Taban renk artik yesil, kombo yukseldikce turuncu/altina donuyor.
                    val comboColor = when {
                        comboCount >= 4 -> NeonGold
                        comboCount >= 2 || lastClearWasCelebration -> Color(0xFFFF6B35)
                        else -> NeonGreen
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            // Faz 1 (performans): `Modifier.scale(v)` degeri
                            // kompozisyonda okuyordu — kombo banner'inin
                            // "pop" animasyonu (100ms + geri donus) boyunca
                            // grid Card'inin TUM content'i her karede yeniden
                            // derileniyordu. `scale(x, y)` zaten
                            // `graphicsLayer(scaleX, scaleY, clip = false)`.
                            .graphicsLayer {
                                val s = comboTextScale.value
                                scaleX = s
                                scaleY = s
                                alpha = comboTextAlpha.value
                            }
                    ) {
                        // Faz 50: rakip oyunun kayittaki "Perfect!" yazisinda kalin bir
                        // golge vardi, bizimki duz renkti — daha "punchy" hissetmesi icin
                        // golge eklendi (kombo yukseldikce golge de belirginlesiyor).
                        Text(
                            text = lastClearedText,
                            fontSize = (13 + comboCount.coerceAtMost(5) * 2).sp,
                            fontWeight = FontWeight.Bold,
                            color = comboColor,
                            // Faz 115p: bkz. SCORE duzeltmesi yorumu.
                            style = TextStyle(
                                fontFamily = AppFontFamily,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 3f),
                                    blurRadius = 4f
                                )
                            )
                        )
                        // "Block Blast!" referansindaki sari COMBO rozeti — sadece gercek
                        // bir kombo (art arda ikinci+ temizleme) oldugunda gosterilir.
                        if (comboCount >= 2) {
                            Text(
                                text = "${comboCount}x ${language.pick(tr = "KOMBO", en = "COMBO", it = "COMBO", fr = "COMBO", es = "COMBO")}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGold
                            )
                        }
                    }
                }
              }
            }
            }

            // Faz 95: kullanici "bir bloğu sürükleyip ızgaraya bırakın" ipucu
            // metninin gereksiz/kalabalik oldugunu, sabit durup hicbir sey
            // katmadigini bildirdi — tum modlardan (Level/Endless/Pro paylasilan
            // bu tek nokta) kaldirildi.
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Tray with 3 Shapes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 3) {
                    val shape = trayShapes.getOrNull(i)
                    val isBeingDragged = draggedTrayIndex == i
                    var itemCoords by remember(i) { mutableStateOf<LayoutCoordinates?>(null) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .padding(4.dp)
                            .onGloballyPositioned { itemCoords = it }
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isBeingDragged) palette.cardAlt else palette.card)
                            .border(
                                width = 1.dp,
                                color = palette.cardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .pointerInput(shape?.id) {
                                if (shape == null) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        val coords = itemCoords ?: return@detectDragGestures
                                        draggedTrayIndex = i
                                        dragPointerStartGlobal = coords.positionInRoot() + startOffset
                                        dragCoroutineScope.launch { dragOffset.snapTo(Offset.Zero) }
                                        SoundManager.playPickup(soundEnabled)
                                    },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragCoroutineScope.launch { dragOffset.snapTo(dragOffset.value + amount) }
                                    },
                                    onDragEnd = {
                                        val hover = currentHoverCell()
                                        if (isCurrentDropValid() && hover != null) {
                                            placeShape(i, shape, hover.first, hover.second)
                                            draggedTrayIndex = -1
                                        } else {
                                            dragCoroutineScope.launch {
                                                dragOffset.animateTo(Offset.Zero, animationSpec = spring())
                                                draggedTrayIndex = -1
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        dragCoroutineScope.launch {
                                            dragOffset.animateTo(Offset.Zero, animationSpec = spring())
                                            draggedTrayIndex = -1
                                        }
                                    }
                                )
                            }
                            .testTag("tray_shape_$i"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (shape != null && !isBeingDragged) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                for (r in shape.pattern.indices) {
                                    Row(horizontalArrangement = Arrangement.Center) {
                                        for (c in shape.pattern[r].indices) {
                                            val active = shape.pattern[r][c]
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(0.5.dp)
                                            ) {
                                                if (active) {
                                                    EmbossedBlockCell(
                                                        colorIndex = shape.colorIndex,
                                                        theme = currentTheme,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (shape == null) {
                            // Onceden tamamen bos/karanlik bir kart kaliyordu, "kirik/eksik"
                            // gibi goruntyordu (tasarim onerisi). Noktali cerceve + soluk "+"
                            // ile "burada yeni parca gelecek" hissi verilir.
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, palette.cardBorder.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", fontSize = 14.sp, color = palette.textSecondary.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // Sürüklenen parçanın parmağı takip eden önizlemesi (tam şekil, geçerliyse yeşil/geçersizse kırmızı)
        // Faz 22: oturum ici basari rozeti — kisa sureli, ekranin ustunde beliren bir toast.
        AnimatedVisibility(
            visible = activeAchievementText != null,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
                .zIndex(25f)
        ) {
            Surface(
                color = NeonGold,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = activeAchievementText ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        // Faz 95: kullanici ilk-hamle rehber ipucunu da (Faz 22) kalabalik
        // bulup kaldirmamizi istedi — showFirstMoveHint state'i ve
        // onFirstMoveMade() cagrisi (asagida, checkGameOver/placeShape akisinda)
        // zararsizca kaliyor (DataStore'a "ilk hamle yapildi" yazmaya devam
        // ediyor), sadece GORUNUR ipucu kaldirildi.

        activeDragShape()?.let { draggedShape ->
            val liftPx = with(density) { dragLiftDp.toPx() }
            // Ghost'un kendi hucre boyutu, gercek grid hucre boyutuyla (cellSizePx) ayni olmali —
            // aksi halde onizleme, gercekte yerlesecegi alandan farkli buyuklukte gorunur.
            val ghostCellDp = if (cellSizePx > 0f) with(density) { cellSizePx.toDp() } else 26.dp
            val ghostCellPx = with(density) { ghostCellDp.toPx() }
            val shapeWidthPx = draggedShape.pattern[0].size * ghostCellPx
            val shapeHeightPx = draggedShape.pattern.size * ghostCellPx
            // Faz 1 (performans) — BILEREK DOKUNULMADI. `dragOffset` surukleme
            // boyunca kompozisyon fazinda okunuyor, ama bu ZORUNLU: ayni deger
            // `currentHoverCell()` -> `isCellInDragFootprint()` -> hangi hucrenin
            // yesil/kirmizi vurgulanacagini ve `isCurrentDropValid()` -> ghost'un
            // hangi composable'a donecegini belirliyor. Yani surukleme sirasinda
            // recomposition bir israf degil, oynanisin kendisi. Ayrica surukleme
            // parmak hiziyla sinirli ve bos tahtada hic calismiyor — baseline'daki
            // "bos/hareketsiz tahtada 60 fps recomposition" sorunuyla ilgisi yok.
            val pointerAbs = dragPointerStartGlobal + dragOffset.value
            val left = pointerAbs.x - rootOriginPx.x - shapeWidthPx / 2f
            // Faz 95c: kullanici "yatay parçalar için yetiyor ama dikeyler için
            // yetmiyor" dedi — kok neden bulundu: asagidaki eski formul
            // (- shapeHeightPx/2f) parcanin MERKEZINI parmaktan sabit liftPx
            // mesafede tutuyordu, ALT KENARINI degil. Uzun/dikey parcalarda
            // (buyuk shapeHeightPx) bu, alt kenarin parmaga DAHA YAKIN kalmasina
            // yol aciyordu (merkez sabit -> parca ne kadar uzunsa alt kenar o
            // kadar asagida/parmaga yakin). Simdi TAM shapeHeightPx cikariliyor,
            // boylece alt kenar HER ZAMAN (parca boyutundan bagimsiz) parmaktan
            // tam liftPx mesafede kaliyor.
            val top = (pointerAbs.y - liftPx) - rootOriginPx.y - shapeHeightPx
            val dropValid = isCurrentDropValid()

            Box(
                modifier = Modifier
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .zIndex(10f)
            ) {
                Column {
                    draggedShape.pattern.forEach { rowPattern ->
                        Row {
                            rowPattern.forEach { active ->
                                Box(modifier = Modifier.size(ghostCellDp).padding(1.dp)) {
                                    if (active) {
                                        if (dropValid) {
                                            EmbossedBlockCell(
                                                colorIndex = draggedShape.colorIndex,
                                                theme = currentTheme,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFF87171).copy(alpha = 0.85f))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .clickable { showThemeDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(2.dp, NeonPurple, RoundedCornerShape(20.dp))
                        .clickable(enabled = false) {} // Prevent dismiss on card click
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Theme", tint = NeonPurple)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = language.pick(tr = "BLOK TEMASI SEÇİN", en = "SELECT BLOCK THEME", it = "SELEZIONA TEMA BLOCCHI", fr = "CHOISIR LE THÈME DES BLOCS", es = "SELECCIONA TEMA DE BLOQUES"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BLOCK_THEMES.forEach { theme ->
                                val isSelected = currentTheme == theme.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) NeonPurple.copy(alpha = 0.25f) else palette.background
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) NeonPurple else palette.cardBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            SoundManager.playBeep(soundEnabled)
                                            onSelectTheme(theme.id)
                                            showThemeDialog = false
                                        }
                                        .testTag("select_block_theme_${theme.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = theme.icon, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = theme.title(language),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = theme.description(language),
                                                fontSize = 11.sp,
                                                color = palette.textSecondary
                                            )
                                        }
                                        if (isSelected) {
                                            Surface(
                                                color = NeonGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = language.pick(tr = "SEÇİLİ", en = "ACTIVE", it = "ATTIVO", fr = "ACTIF", es = "ACTIVO"),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showThemeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.cardAlt),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(language.pick(tr = "KAPAT", en = "CLOSE", it = "CHIUDI", fr = "FERMER", es = "CERRAR"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }
                    }
                }
            }
        }

        // Ayarlar overlay — ayri bir navigasyon hedefi degil, ayni composable
        // icinde tam ekran bir katman (bkz. yukaridaki parametre yorumu).
        if (showSettingsDialog) {
            Box(modifier = Modifier.fillMaxSize().zIndex(30f)) {
                SettingsScreen(
                    soundEnabled = soundEnabled,
                    soundVolume = soundVolume,
                    musicEnabled = musicEnabled,
                    hapticsEnabled = hapticsEnabled,
                    darkMode = darkMode,
                    language = language,
                    skin = uiSkin,
                    // Faz 115q — HATA DUZELTMESI (kullanici: "ayarlarda dropdownda
                    // gozukuyor ama seçilmiyor"). Kok sebep: bu OYUN ICI Ayarlar
                    // overlay'i `SettingsScreen`'i cagirirken `currentTheme` ve
                    // `onSelectTheme`'i HIC GECIRMIYORDU — ikisi de sessizce
                    // varsayilana duşuyordu: `currentTheme` hep "CLASSIC" (baslik
                    // asla degismiyordu), `onSelectTheme` ise BOS bir no-op
                    // lambda `{}` (tiklama gercekten hicbir sey yapmiyordu).
                    // `BoomBlocksGame`'in KENDI parametreleri olarak zaten mevcuttu
                    // (tahta cizimi zaten currentTheme kullaniyor) — sadece ic ice
                    // cagriya aktarilmamis.
                    currentTheme = currentTheme,
                    onSelectTheme = onSelectTheme,
                    onToggleSound = onToggleSound,
                    onSoundVolumeChange = onSoundVolumeChange,
                    onToggleMusic = onToggleMusic,
                    onToggleHaptics = onToggleHaptics,
                    onToggleDarkMode = onToggleDarkMode,
                    onSelectLanguage = onSelectLanguage,
                    onSelectSkin = onSelectSkin,
                    notificationsEnabled = notificationsEnabled,
                    onToggleNotifications = onToggleNotifications,
                    onBack = { showSettingsDialog = false }
                )
            }
        }

        // Sonsuz Mod — "Devam Et?" diyalogu (oturum basina bir kez)
        AnimatedVisibility(
            visible = showContinueDialog,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                        .border(2.dp, NeonGreen, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = language.pick(tr = "DEVAM ETMEK İSTER MİSİN?", en = "WANT TO CONTINUE?", it = "VUOI CONTINUARE?", fr = "VOULEZ-VOUS CONTINUER?", es = "¿QUIERES CONTINUAR?"),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = language.pick(tr = "Reklam izleyip son 3 hamleni geri alarak devam edebilirsin", en = "Watch an ad to undo your last 3 moves and keep playing", it = "Guarda un annuncio per annullare le ultime 3 mosse e continuare", fr = "Regardez une pub pour annuler vos 3 derniers coups et continuer", es = "Mira un anuncio para deshacer tus últimos 3 movimientos y seguir jugando"),
                            fontSize = 13.sp,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Faz 90: kullanici oturum basina 4 hakka kadar izin
                        // verilmesini istedi — Pro Mod'daki "Kalan: N" deseniyle
                        // tutarli bir gosterge.
                        Text(
                            text = language.pick(
                                tr = "Kalan hak: ${maxEndlessContinues - endlessContinuesUsed}",
                                en = "Remaining: ${maxEndlessContinues - endlessContinuesUsed}",
                                it = "Rimanenti: ${maxEndlessContinues - endlessContinuesUsed}",
                                fr = "Restant : ${maxEndlessContinues - endlessContinuesUsed}",
                                es = "Restante: ${maxEndlessContinues - endlessContinuesUsed}"
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { handleContinueWithAd() },
                            enabled = !isRequestingContinueAd,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("continue_watch_ad_button")
                        ) {
                            // Faz 48: reklam yuklemesi (3-8sn) hicbir gorsel geri bildirim
                            // vermiyordu — sadece Button'un varsayilan "disabled" soluklugu
                            // pek fark edilmiyordu (ayni Loadout'taki WATCH sorunuyla ayni
                            // kok neden). Artik acikca donen bir gosterge var.
                            if (isRequestingContinueAd) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = language.pick(tr = "REKLAM İZLE VE DEVAM ET", en = "WATCH AD TO CONTINUE", it = "GUARDA ANNUNCIO E CONTINUA", fr = "REGARDER PUB ET CONTINUER", es = "VER ANUNCIO Y CONTINUAR"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { handleEndSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.cardAlt),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("continue_end_session_button")
                        ) {
                            Text(language.pick(tr = "BİTİR", en = "END", it = "FINE", fr = "FIN", es = "FIN"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }
                    }
                }
            }
        }

        // Faz 38: Sonsuz Mod'da "cikmak istedigine emin misin" onay ekrani —
        // hem sistem geri tusu/jesti (BackHandler) hem oyun ici geri oku
        // buraya yonleniyor, kullanicinin yanlislikla mevcut skoru/serisini
        // kaybetmesini onlemek icin.
        AnimatedVisibility(
            visible = showExitConfirmDialog,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                        .border(2.dp, NeonMagenta, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = language.pick(tr = "ÇIKMAK İSTEDİĞİNE EMİN MİSİN?", en = "ARE YOU SURE YOU WANT TO EXIT?", it = "SEI SICURO DI VOLER USCIRE?", fr = "ÊTES-VOUS SÛR DE VOULOIR QUITTER ?", es = "¿SEGURO QUE QUIERES SALIR?"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonMagenta,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isEndless) {
                                language.pick(tr = "Mevcut skorun ve serin kaybolacak", en = "Your current score and streak will be lost", it = "Il tuo punteggio e la tua serie attuali andranno persi", fr = "Votre score et votre série actuels seront perdus", es = "Se perderán tu puntuación y racha actuales")
                            } else {
                                language.pick(tr = "Bu seviyedeki ilerlemen kaybolacak", en = "Your progress in this level will be lost", it = "I tuoi progressi in questo livello andranno persi", fr = "Votre progression dans ce niveau sera perdue", es = "Se perderá tu progreso en este nivel")
                            },
                            fontSize = 13.sp,
                            color = palette.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showExitConfirmDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("exit_confirm_cancel_button")
                        ) {
                            Text(
                                text = language.pick(tr = "OYUNA DEVAM ET", en = "KEEP PLAYING", it = "CONTINUA A GIOCARE", fr = "CONTINUER À JOUER", es = "SEGUIR JUGANDO"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showExitConfirmDialog = false
                                exitGame()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.cardAlt),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("exit_confirm_exit_button")
                        ) {
                            Text(
                                text = language.pick(tr = "ÇIK", en = "EXIT", it = "ESCI", fr = "QUITTER", es = "SALIR"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Game Over Modal
        AnimatedVisibility(
            visible = isGameOver,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                // Faz 95d: kullanici "yazılar kutulara sığmamış, skor yazısını sil,
                // reklam kutularını büyüt" dedi. Kullanicinin kendi onerisi
                // ("kalan can sayısını sağ üst köşede göstermek gibi") benimsendi:
                // butonlardaki uzun "(-1 ❤ · Kalan: N)" metni kaldirildi, yerine bu
                // Box'in disina (Card'in SAG UST kosesine bindirilen) kompakt bir
                // rozet eklendi (asagida). "Skor" etiketi + buyuk rakam TAMAMEN
                // kaldirildi — zaten arka planda (dialog acilmadan once) SKOR/
                // İLERLEME/HEDEF kartlari ayni bilgiyi gosteriyor, dialog icinde
                // TEKRARI gereksiz yer kaplıyordu.
                Box(contentAlignment = Alignment.TopEnd) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.card),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp)
                            .border(2.dp, NeonMagenta, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isEndless) {
                                    language.pick(tr = "OYUN BİTTİ", en = "GAME OVER", it = "GIOCO FINITO", fr = "PARTIE TERMINÉE", es = "JUEGO TERMINADO")
                                } else {
                                    language.pick(tr = "SEVİYE BAŞARISIZ", en = "LEVEL FAILED", it = "LIVELLO FALLITO", fr = "NIVEAU ÉCHOUÉ", es = "NIVEL FALLIDO")
                                },
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonMagenta
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Faz 103: Pro Mode'da rewarded devam haklari (3) bitince bu
                            // buton "TEKRAR DENE"ye donuyordu ve tam sifirlama yapiyordu —
                            // ama hemen altindaki turuncu "YENİDEN BAŞLAT" da AYNI seyi
                            // yapiyor. Iki buton, ayni sonuc, farkli bedel: oyuncu birkac
                            // denemede ucuz olani ogrenip digerine bir daha basmiyordu.
                            // Kullanici karari: Pro'da haklar bitince bu buton hic
                            // GORUNMESIN — geriye net isi olan iki buton kalir
                            // ("YENİDEN BAŞLAT" = sifirla, "HARİTAYA DÖN" = cik).
                            // Seviyeli Mod'da turuncu buton YOK, orada "TEKRAR DENE"
                            // kalmaya devam ediyor; Sonsuz Mod'da da kaliyor.
                            val hideRetryButton = isChallengeMode && continuesUsedInAttempt >= maxRetryContinues

                            if (!hideRetryButton) {
                            Button(
                            // Faz 103: Sonsuz Mod'da "TEKRAR DENE" dogrudan resetGame()
                            // cagiriyordu — hic reklam yok, bedava sifirlama. Seviyeli/Pro'da
                            // ayni durumda zorunlu interstitial vardi, yani asimetrikti ve
                            // Sonsuz en cok oynanan mod oldugu icin en cok kaybettiren
                            // bosluk burasiydi. Artik o da her sifirlamada interstitial
                            // gosteriyor (bkz. AppNavigation.kt Routes.ENDLESS_GAME'de
                            // onRetryInterstitial baglandi). No-fill'de yine gecer.
                            onClick = { if (isEndless) onRetryInterstitial { resetGame() } else handleRetryWithAd() },
                            enabled = !isRequestingContinueAd,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            // Faz 95d: kullanici "kutu esnek olmasın, farklı boylarda kutular
                            // olur, puntoyu küçült ve kutu yüksekliğini SABİT artır" dedi —
                            // 3 buton (bu + asagidaki YENİDEN BAŞLA + HARİTAYA DÖN) artik
                            // AYNI sabit 52dp yukseklikte, tutarli.
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("block_blast_restart_confirm")
                        ) {
                            if (isRequestingContinueAd) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (isEndless || continuesUsedInAttempt >= maxRetryContinues) {
                                        // Faz 97: Seviyeli/Pro Mode'da bu denemedeki 3 reklamli
                                        // devam hakki da tukendiyse, buton "TEKRAR DENE"ye
                                        // (zorunlu interstitial'a sarili, reklamsiz degil)
                                        // donuyor — oyuncuya yanlislikla tekrar rewarded reklam
                                        // vaat etmiyoruz.
                                        language.pick(tr = "TEKRAR DENE", en = "RETRY", it = "RIPROVA", fr = "RÉESSAYER", es = "REINTENTAR")
                                    } else {
                                        // Faz 61: kullanici istegiyle metin "reklam izle,
                                        // devam et"e cevrildi — artik gercekten tam sifirlama
                                        // degil, 3 hamle geri alip DEVAM ediyor (Endless'teki
                                        // gibi), metin bunu dogru yansitmali.
                                        language.pick(tr = "REKLAM İZLE, DEVAM ET", en = "WATCH AD, CONTINUE", it = "GUARDA ANNUNCIO, CONTINUA", fr = "REGARDER PUB, CONTINUER", es = "VER ANUNCIO, CONTINUAR")
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        }

                        // Faz 96: can sistemi kaldirildi (bypass edilebiliyordu) —
                        // Pro Mode'a ozel bu buton HER ZAMAN tam sifirlama yapar,
                        // interstitial reklam (onProModeRestart) arkasinda.
                        // Faz 103: reklam artik HER yeniden baslatmada gosteriliyor
                        // (onceden "her 2 kayipta 1"di). Kullanicinin gerekcesi: bu buton
                        // zaten "cik-gir yaparak bedava sifirlama" bypass'ini kapatmak icin
                        // var; cikistan (HARİTAYA DÖN, her seferinde reklam) ucuz olursa
                        // kimse cikmaz, herkes yeniden baslatir ve arbitraj olusur.
                        // Ikisi de ayni bedele gelince kafa karisikligi da bitiyor.
                        if (isChallengeMode) {
                            if (!hideRetryButton) Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onProModeRestart { resetGame() } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text(
                                    text = language.pick(tr = "YENİDEN BAŞLAT", en = "RESTART", it = "RICOMINCIA", fr = "RECOMMENCER", es = "REINICIAR"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = palette.cardAlt),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text(language.pick(tr = "HARİTAYA DÖN", en = "BACK TO MAP", it = "TORNA ALLA MAPPA", fr = "RETOUR À LA CARTE", es = "VOLVER AL MAPA"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        }
                    }
                }
                }
            }
        }

        // Level Complete Modal
        AnimatedVisibility(
            // Faz 107b: `isLevelComplete` degil `levelCompleteRevealed` — diyalog
            // patlama rituali bitene kadar beklemeli (bkz. yukaridaki not).
            visible = levelCompleteRevealed,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                // Faz 1 (performans): kosul eskiden `confettiProgress.value < 1f`
                // iceriyordu — animasyon degeri KOMPOZISYONDA okunuyordu ve
                // konfeti suresince (1600ms) bu diyalogun tamami her karede
                // yeniden derileniyordu. Erken cikis artik draw lambda'sinin
                // ICINDE (Faz 107'de parcacik katmaninda uygulanan ayni desen);
                // progress >= 1 iken her iki halde de HICBIR SEY cizilmiyor.
                if (confettiPieces.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val progress = confettiProgress.value
                        if (progress >= 1f) return@Canvas
                        confettiPieces.forEach { piece ->
                            val local = ((progress - piece.startDelay) / (1f - piece.startDelay)).coerceIn(0f, 1f)
                            if (local <= 0f) return@forEach
                            val y = size.height * local
                            val x = size.width * piece.xFraction + sin(local * piece.rotationSpeed) * piece.drift
                            val alpha = (1f - local).coerceIn(0f, 1f)
                            drawCircle(
                                color = piece.color.copy(alpha = alpha),
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp)
                        .border(2.dp, NeonGreen, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = language.pick(tr = "SEVİYE TAMAMLANDI!", en = "LEVEL COMPLETE!", it = "LIVELLO COMPLETATO!", fr = "NIVEAU TERMINÉ!", es = "¡NIVEL COMPLETADO!"),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Faz 45: kullanici "yıldız mantığı yok, hedef başarma mantığı
                        // var" dedi — bu modal zaten SADECE hedefe ulasilinca aciliyor
                        // (yani her zaman "basari"), dereceli bir yildiz puanlamasi
                        // anlamsizdi. 3 yildizlik satir yerine tek bir kupa gosteriliyor.
                        Text(text = "🏆", fontSize = 48.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(language.pick(tr = "Skor", en = "Score", it = "Punteggio", fr = "Score", es = "Puntuación"), fontSize = 14.sp, color = palette.textSecondary)
                        Text("$score", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onLevelCompleteContinue,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("level_complete_continue_button")
                        ) {
                            Text(language.pick(tr = "DEVAM ET", en = "CONTINUE", it = "CONTINUA", fr = "CONTINUER", es = "CONTINUAR"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmbossedBlockCell(
    colorIndex: Int,
    theme: String,
    modifier: Modifier = Modifier,
    isHover: Boolean = false,
    // Faz 22: bloklar yerlesince tamamen donuk kaliyordu (kullanici geri bildirimi
    // kaynakli tasarim onerisi: "ince bir canlilik"). Paylasilan TEK bir faz degeri
    // (ustteki BlastTheBlocksGame'de hesaplanan) her hucreye row/col'a gore kaydirilmis
    // olarak gecirilir, boylece 64 ayri animasyon dongusu acilmadan hafif bir parlaklik
    // dalgasi elde edilir.
    //
    // Faz 1 (performans): tip `Float` degil `() -> Float`. Duz Float olarak
    // gecirildiginde cagiran taraf animasyon degerini KOMPOZISYON fazinda okumak
    // zorunda kaliyordu; `infiniteRepeatable` hic durmadigi icin bu, tahta boşken
    // bile grid'in tamaminin saniyede 60 kez yeniden derilmesi demekti. Lambda ile
    // deger asagidaki Canvas'in draw lambda'sinda — yani cizim fazinda — okunuyor:
    // ayni piksel, recomposition yok.
    shimmerPhase: () -> Float = { 0f }
) {
    val baseColor = if (isHover) {
        NeonCyan.copy(alpha = 0.35f)
    } else {
        BLOCK_COLORS.getOrElse((colorIndex - 1).coerceAtLeast(0)) { NeonCyan }
    }

    val emoji = if (isHover) "" else when (theme.uppercase()) {
        "FRUIT" -> when (colorIndex % 6) {
            1 -> "🍉"
            2 -> "🍊"
            3 -> "🥝"
            4 -> "🍓"
            5 -> "🧀"
            0 -> "🍇"
            else -> ""
        }
        "SWEETS" -> when (colorIndex % 6) {
            1 -> "🍩"
            2 -> "🍫"
            3 -> "🍪"
            4 -> "🧁"
            5 -> "🍬"
            0 -> "🧇"
            else -> ""
        }
        else -> ""
    }

    Box(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (isHover) {
                drawRect(color = baseColor)
            } else {
                // Faz 115: izgara hucresiyle AYNI recete (bkz. "FASETLI / PARLAK
                // BLOK"). Tepsi ve tahta ayri kod yollarindan cizildigi icin biri
                // guncellenip digeri unutulursa parca tepsiden tahtaya
                // suruklendiginde GORUNUM DEGISIR — Faz 106'da tam bu tur bir
                // kopukluk kullanici tarafindan yakalanmisti. Ikisi birlikte
                // degistirildi.
                val inset = w * 0.055f
                val bs = (minOf(w, h) - 2 * inset).coerceAtLeast(1f)
                val bw = (w - 2 * inset).coerceAtLeast(1f)
                val bh = (h - 2 * inset).coerceAtLeast(1f)
                val br = androidx.compose.ui.geometry.CornerRadius(bs * 0.26f, bs * 0.26f)

                val faceTop = lerp(baseColor, Color.White, 0.30f)
                val faceBottom = lerp(baseColor, Color.Black, 0.34f)

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(faceTop, baseColor, faceBottom),
                        startY = inset,
                        endY = inset + bh
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(bw, bh),
                    cornerRadius = br
                )

                val shimmerAlpha = 0.26f + 0.10f * sin(shimmerPhase())
                val glossH = bh * 0.46f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = shimmerAlpha.coerceIn(0.14f, 0.40f)),
                            Color.Transparent
                        ),
                        startY = inset,
                        endY = inset + glossH
                    ),
                    topLeft = Offset(inset + bw * 0.08f, inset + bh * 0.06f),
                    size = Size(bw * 0.84f, glossH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(br.x * 0.8f)
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.30f)
                        ),
                        startY = inset,
                        endY = inset + bh
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(bw, bh),
                    cornerRadius = br,
                    style = Stroke(width = with(drawContext.density) { 1.2.dp.toPx() })
                )
            }

            // Tema emojisi burada, ayni Canvas icinde, gercek piksel boyutuna gore
            // ciziliyor — ayri bir Compose Text kullanmiyoruz çünkü Dp/Sp donusumu
            // ic ice yerlesik tepsi onizlemesi gibi bazi baglamlarda kutunun disina
            // tasan/kaybolan sonuçlar veriyordu (kullanici geri bildirimi). Canvas'in
            // kendi `size` piksel degeri her baglamda dogru oldugu icin bu yontem
            // ana ızgara hucresinde hem de tepsi onizlemesinde tutarli calisir.
            if (emoji.isNotEmpty() && !isHover) {
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = size.minDimension * 0.6f
                }
                val fm = paint.fontMetrics
                val textY = h / 2f - (fm.ascent + fm.descent) / 2f
                drawContext.canvas.nativeCanvas.drawText(emoji, w / 2f, textY, paint)
            }
        }
    }
}
