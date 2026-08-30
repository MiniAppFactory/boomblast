package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.lerp

// Faz 158 — menulerin ZEMIN katmani.
//
// Onceki hal: `.background(palette.background)` — tek duz renk. Kullanicinin
// teshisi: "biz sanki websitesi hissi veriyoruz". Hedef mockup'ta zemin bir
// DUNYA: derin gradyan, kenarlarda bulanik/sonuk 3B bloklar, altta koyulasan
// bir zemin bandi.
//
// Bloklar VARLIK DEGIL — prosedurel ciziliyor (yuvarlatilmis kare + ust yuz
// parlakligi, hafif dondurulmus, cok dusuk alfa). Blur yerine katmanli
// yumusatma kullanildi: Modifier.blur API 31+ istiyor, bu proje minSdk 24 —
// eski cihazlarda SESSIZCE keskin blok cizerdi.
//
// PARAMETRIK: painter verilirse boyali illustrasyon prosedurel katmanin
// USTUNE cizilir. Boyali varlik seti ayri bir is; bu bilesen o gun hazir
// olacak sekilde yazildi, cagiran taraf degismek zorunda kalmayacak.
//
// 6 SKIN GUVENCESI: burada TEK bir renk sabiti yok. Her sey
// rememberGameSurfaces uzerinden skin'in kendi accentGradient'inden ve
// palette'inden turuyor.
//
// MALIYET: tek Canvas, animasyon yok — bir kez cizilir, sadece boyut
// degisince yeniden cizilir. FPS/pil etkisi yok.
@Composable
fun GameScreenBackground(
    skin: BlastSkin,
    darkMode: Boolean,
    modifier: Modifier = Modifier.fillMaxSize(),
    // Mod kimligi tasiyan ekranlar icin (Kariyer/Pro/Kolay). Bkz. GameSurfaces.
    accentOverride: List<Color>? = null,
    // Zemin bandinin (ufuk cizgisinin) ekran yuksekligindeki orani.
    horizonFraction: Float = 0.62f,
    // Kenar bloklarinin cizilip cizilmeyecegi (cok dar yuzeylerde kapatilabilir).
    showBlocks: Boolean = true,
    // Ileride eklenecek boyali zemin illustrasyonu.
    painter: Painter? = null,
    painterAlpha: Float = 1f,
    // FAZ 172: gogu LACIVERTE cekme orani (0 = dokunma).
    //
    // Kullanicinin gonderdigi harita tasarimlarinda zemin bizimkinden belirgin
    // sekilde daha KOYU ve daha MAVI: olculdu, hedefte ust ~#004163 ve orta
    // ~#00163C iken bizde ust ~#015A77 ve genel ton turkuaza kaciyordu. Yolun
    // ve dugumlerin isimasi bu koyulukta okunuyor; acik turkuaz zeminde
    // hepsi birbirine giriyordu.
    //
    // Parametre olarak eklendi cunku ayni bilesen mod secim ekraninda da
    // kullaniliyor ve ORASI kullanicinin begendigi haliyle kalmali.
    skyDarken: Float = 0f,
    // Haritada zemine serpilen kucuk isik noktalari. Tasarimda var, mod secim
    // ekraninda yok.
    showSparkles: Boolean = false
) {
    val surfaces = rememberGameSurfaces(skin, darkMode, accentOverride)
    // Faz 159 — URETILEN 3B KUPLER ARTIK BAGLI.
    //
    // Onceki hal: `drawEdgeBlock` ile prosedurel cizilen DUZ yuvarlatilmis
    // kareler. Cihaz ekran goruntusunde bunlar kup degil "renkli hap" gibi
    // okunuyordu — kullanicinin "yassi renkli kareler" teshisi tam olarak
    // buydu. `res/drawable-nodpi/kb_block_*.webp` (9 dosya, parlak izometrik
    // kupler) uretilmis ama koda HIC baglanmamisti.
    //
    // Varliklar zaten EGIMLI uretildi — ek `rotate` YOK, aksi halde
    // izometrik acilari bozulurdu.
    val decorBitmaps = if (showBlocks) rememberBlockDecorBitmaps() else emptyList()
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGameWorld(surfaces, horizonFraction, showBlocks, skyDarken, showSparkles)
            if (showBlocks && decorBitmaps.isNotEmpty()) {
                drawBlockDecor(decorBitmaps, surfaces.isLightSurface)
                // Kuplerin USTUNE vinyet. Kupler boylece "yuzen cikartma"
                // olmaktan cikip zeminin DERINLIGINE oturuyor: kenarlara
                // dogru koyulasan bir perde arkalarindan geciyor. Ayrica
                // ekranin ortasindaki icerik alanini aydinlik birakarak
                // metin kontrastini korur.
                drawEdgeVignette(surfaces)
            }
        }
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = painterAlpha,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Parilti noktalari (mockup'taki kucuk isik lekeleri): x/y orani + yaricap
// orani. Blok kumelerinin yaninda duruyorlar, ekran ortasina girmiyorlar.
private val SPARKLES = listOf(
    // CIHAZDA GORULEN: ekranin ortasindaki bos alana dusen parildamalar
    // "olu piksel / toz" gibi okunuyordu. Amblem (GameEmblem) zaten kendi
    // pariltilarini cizdigi icin zemindekiler yalnizca KOSE kupleriyle ayni
    // bolgede birakildi.
    Triple(0.13f, 0.79f, 0.005f),
    Triple(0.86f, 0.84f, 0.004f),
    Triple(0.94f, 0.12f, 0.0035f),
    Triple(0.07f, 0.14f, 0.004f)
)

private fun DrawScope.drawGameWorld(
    s: GameSurfaces,
    horizonFraction: Float,
    showBlocks: Boolean,
    skyDarken: Float = 0f,
    showSparkles: Boolean = false
) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    // FAZ 172: gogu laciverte cekme. `skyDarken == 0f` iken ifade kimlik
    // fonksiyonu, yani bu parametreyi vermeyen ekranlar (mod secim) HIC
    // etkilenmiyor.
    val deep = Color(0xFF00102E)
    fun sky(c: Color) = if (skyDarken <= 0f) c else lerp(c, deep, skyDarken)

    // 1) Gok: yukarida accent'e dogru acilan dikey gradyan.
    drawRect(
        brush = Brush.verticalGradient(
            0f to sky(s.skyTop),
            0.30f to sky(s.skyMid),
            1f to sky(s.horizon)
        )
    )

    // 1b) Isik noktalari — tasarimdaki "yildiz tozu". Konumlar SABIT bir
    // formulden uretiliyor (rastgele degil): her karede ayni yerde duruyorlar,
    // yani titremiyorlar ve durum tutmuyorlar.
    if (showSparkles) {
        for (i in 0 until 46) {
            val fx = ((i * 37) % 100) / 100f
            val fy = ((i * 61) % 100) / 100f
            val r = (1.2f + (i % 3) * 0.9f) * density
            drawCircle(
                color = Color.White.copy(alpha = 0.10f + (i % 4) * 0.06f),
                radius = r,
                center = Offset(w * fx, h * fy)
            )
        }
    }

    // 2) Ambient isima — duz yuzeyi kirip hacim hissi veren en ucuz katman.
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(s.glowPrimary, Color.Transparent),
            center = Offset(w * 0.18f, h * 0.10f),
            radius = w * 0.80f
        ),
        radius = w * 0.80f,
        center = Offset(w * 0.18f, h * 0.10f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(s.glowSecondary, Color.Transparent),
            center = Offset(w * 0.92f, h * 0.38f),
            radius = w * 0.85f
        ),
        radius = w * 0.85f,
        center = Offset(w * 0.92f, h * 0.38f)
    )

    // 3) Parilti noktalari. Kupler artik gercek varlik olarak AYRI bir gecişte
    //    ciziliyor (bkz. drawBlockDecor); burada sadece isik lekeleri kaldi.
    if (showBlocks) {
        for ((sx, sy, sr) in SPARKLES) {
            val r = minOf(w, h) * sr
            val c = Offset(w * sx, h * sy)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (s.isLightSurface) 0.35f else 0.75f),
                        Color.Transparent
                    ),
                    center = c,
                    radius = r * 3.2f
                ),
                radius = r * 3.2f,
                center = c
            )
            drawCircle(
                color = Color.White.copy(alpha = if (s.isLightSurface) 0.45f else 0.9f),
                radius = r,
                center = c
            )
        }
    }

    // 4) Zemin bandi. Duz cizgi degil, hafif tepe kavisi — "panel" degil
    //    "dunya" hissini veren asil oge.
    val hy = h * horizonFraction
    val amp = h * 0.04f
    val ridge = Path().apply {
        moveTo(0f, hy + amp * 0.9f)
        cubicTo(w * 0.28f, hy - amp, w * 0.68f, hy + amp * 1.1f, w, hy - amp * 0.45f)
    }
    val ground = Path().apply {
        addPath(ridge)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = ground,
        brush = Brush.verticalGradient(
            colors = listOf(s.groundTop, s.groundBottom),
            startY = hy - amp,
            endY = h
        )
    )
    // 5) Ufukta ince isik kenari — zemini gokten AYIRAN detay.
    drawPath(
        path = ridge,
        // CIHAZDA GORULEN: bu cizgi ekranin ortasindan gecen bagimsiz bir
        // YAY gibi okunuyordu (mockup'ta boyle bir cizgi yok). Zemini gokten
        // ayirmaya devam etsin diye kaldirilmadi, sadece cok zayiflatildi.
        color = s.accentPrimary.copy(alpha = if (s.isLightSurface) 0.10f else 0.14f),
        style = Stroke(width = 1.5f * density)
    )
}

// ---------------------------------------------------------------------------
// 3B kup varliklari (Faz 159)
// ---------------------------------------------------------------------------

// Uretilen izometrik kupler. Tekil kupler kucuk (236px), ikili/uclu kumeler
// buyuk (400px) — yerlesimde bu fark boyut butcesiyle eslestiriliyor.
private val BLOCK_SINGLES = intArrayOf(
    R.drawable.kb_block_orange,
    R.drawable.kb_block_blue,
    R.drawable.kb_block_green,
    R.drawable.kb_block_yellow,
    R.drawable.kb_block_purple
)

private val BLOCK_CLUSTERS = intArrayOf(
    R.drawable.kb_block_pair_a,
    R.drawable.kb_block_pair_b,
    R.drawable.kb_block_trio_a,
    R.drawable.kb_block_trio_b
)

/**
 * Kup varliklarini bir kez cozup hatirlar. `remember` sayesinde her yeniden
 * bestede yeniden cozulmez; Compose kaynak onbellegi ayrica ekranlar arasi
 * paylasir. Toplam cozulmus boyut ~3.7 MB (9 varlik) — menu ekranlarinda
 * kabul edilebilir, oynanis ekraninda bu bilesen zaten kullanilmiyor.
 */
@Composable
private fun rememberBlockDecorBitmaps(): List<ImageBitmap> {
    val ids = BLOCK_SINGLES + BLOCK_CLUSTERS
    return ids.map { ImageBitmap.imageResource(it) }
}

/**
 * Bir dekor kupunun yerlesimi.
 *
 * @param fx,fy   merkezinin ekran orani cinsinden konumu
 * @param widthDp cizim genisligi (50-90dp araligi; brief'teki butce)
 * @param asset   `rememberBlockDecorBitmaps()` listesindeki indeks
 * @param alpha   dekoratif seffaflik (0.35-0.55)
 */
private data class CubeDecor(
    val fx: Float,
    val fy: Float,
    val widthDp: Float,
    val asset: Int,
    val alpha: Float
)

// KOSELER — mockup'ta kupler tam olarak burada duruyor ve ekranin ortasi
// (icerik alani) temiz kaliyor, yani metin kontrasti dusmuyor. Konumlar
// SABIT: rastgele degil, her cizimde ayni yerde.
//
// Indeksler: 0-4 tekil kup, 5-8 ikili/uclu kume.
// CIHAZDA GORULEN VE DUZELTILEN (Faz 159, ikinci gecis):
//   - Kenar-ortasi kupler (fy 0.46 / 0.56) mod kartlarinin kenarina
//     BINIYORDU. Kaldirildi: dekor artik yalnizca dort kosede.
//   - Kupler ekranin ust %10'una girip wordmark'in arkasinda gri lekeler
//     olusturuyordu. Ust kume asagi ve disari kaydirildi.
private val CUBE_DECOR = listOf(
    // Sol ust
    CubeDecor(-0.04f, 0.055f, 78f, 7, 0.42f),
    CubeDecor(0.10f, -0.035f, 52f, 1, 0.34f),
    // Sag ust — mockup'ta en yogun kose
    CubeDecor(1.03f, 0.035f, 84f, 5, 0.44f),
    CubeDecor(0.88f, -0.03f, 58f, 3, 0.36f),
    CubeDecor(1.06f, 0.145f, 62f, 6, 0.32f),
    // Sol alt
    CubeDecor(-0.03f, 0.94f, 80f, 8, 0.44f),
    CubeDecor(0.12f, 1.03f, 56f, 4, 0.36f),
    // Sag alt
    CubeDecor(1.01f, 0.955f, 84f, 6, 0.44f),
    CubeDecor(0.86f, 1.04f, 54f, 0, 0.36f)
)

/**
 * Kupleri cizer. Ek `rotate` YOK — varliklar zaten izometrik acilarla
 * uretildi, dondurmek o aciyi bozardi.
 *
 * Acik temada alfa dusuruluyor: parlak kupler acik zeminde koyu zemine gore
 * cok daha baskin cikiyor, dekoratif kalmalari icin geri cekiliyorlar.
 */
private fun DrawScope.drawBlockDecor(bitmaps: List<ImageBitmap>, isLight: Boolean) {
    if (bitmaps.isEmpty()) return
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return

    for (decor in CUBE_DECOR) {
        val bmp = bitmaps.getOrNull(decor.asset % bitmaps.size) ?: continue
        val drawW = decor.widthDp * density
        // En-boy orani KORUNUYOR: varlik yuksekligi genisligine gore
        // olcekleniyor, kupler ezilmiyor.
        val drawH = drawW * (bmp.height.toFloat() / bmp.width.toFloat())
        val left = w * decor.fx - drawW / 2f
        val top = h * decor.fy - drawH / 2f
        val alpha = if (isLight) decor.alpha * 0.55f else decor.alpha

        drawImage(
            image = bmp,
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()),
            alpha = alpha,
            filterQuality = FilterQuality.High
        )
    }
}

/**
 * Kenar vinyeti — kupleri derinlige oturtan perde.
 *
 * Merkez SEFFAF, kenarlar zeminin kendi koyu tonuna dogru kapaniyor. Yani
 * dekor kupleri kenarlarda soner, icerik alani (ekranin ortasi) hic
 * etkilenmez; metin kontrasti dusmez.
 *
 * Acik temada cok daha zayif: acik bir zeminde koyu vinyet kirli gorunur.
 */
private fun DrawScope.drawEdgeVignette(s: GameSurfaces) {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return
    val edge = if (s.isLightSurface) {
        Color.Black.copy(alpha = 0.10f)
    } else {
        deepenTowardShade(s.horizon).copy(alpha = 0.42f)
    }
    val radius = maxOf(w, h) * 0.78f
    drawRect(
        brush = Brush.radialGradient(
            0.0f to Color.Transparent,
            0.66f to Color.Transparent,
            1.0f to edge,
            center = Offset(w * 0.5f, h * 0.46f),
            radius = radius
        )
    )
}

/** Vinyet rengi: zemin tonunun daha koyu hali. */
private fun deepenTowardShade(base: Color): Color = Color(
    red = base.red * 0.45f,
    green = base.green * 0.45f,
    blue = base.blue * 0.55f,
    alpha = 1f
)
