package com.miniappfactory.boomblocks.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.miniappfactory.boomblocks.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Faz 124: ModeSelectScreen/TermsAcceptScreen/OnboardingScreen'de UCUNDE de
// AYNI "dondurulmus konfeti kupu" deseni ayri ayri kopyalanmisti (Faz 115h).
// Kullanici ModeSelectScreen'de bu deseni gercek oyun parcasi geometrilerine
// (Faz 122) ve gezinme animasyonuna (Faz 120/121/123) cevirince, ayni istegi
// diger iki ekran icin de yaptı — uc kopyayi elle senkron tutmak yerine TEK
// paylasilan composable'a cikarildi. Degistirmek istersen sadece burasi.

// FAZ 162 — MADDE 3: PARCALAR ARTIK DUZ DIKDORTGEN DEGIL, 3B KUP VARLIGI.
//
// Onceki hal: her hucre `drawRoundRect` ile yuvarlatilmis kare olarak
// ciziliyordu. Cihaz ekran goruntusunde bunlar "yassi renkli kareler" olarak
// okunuyordu — GameScreenBackground'un kose dekoru Faz 159'da gercek kuplere
// gecirilmisti ama bu katman geride kalmisti, yani ayni ekranda iki farkli
// blok dili vardi.
//
// ⚠️ HAREKET DAVRANISI DEGISMEDI. `fx/fy/seed/speed/rangeDp` ve asagidaki
// aci/salinim formulu Faz 122'den beri AYNI — kullanici bu gezinmeyi
// begendigi icin tek satiri bile degistirilmedi. Degisen SADECE cizilen sey.
//
// ⚠️ TINT YOK. Varliklar kendi golgesini ve yuz aydinlatmasini tasiyor;
// `ColorFilter.tint` uygulamak o hacmi duz bir siluete cevirirdi. Bu yuzden
// parcanin rengi artik bir `Color` alani DEGIL, secilen VARLIK.
data class WanderingPiece(
    val pattern: List<List<Boolean>>,
    val fx: Float,
    val fy: Float,
    val cellDp: Float,
    /** Cizilecek 3B kup varligi (`R.drawable.kb_block_*`). */
    @param:DrawableRes val asset: Int,
    val alpha: Float,
    val seed: Float,
    val speed: Float,
    val rangeDp: Float
) {
    /**
     * Varligin cizim genisligi (dp).
     *
     * Taban olcu desenin kapladigi kutu — yani parcalarin BOY SIRASI
     * (hangisi buyuk, hangisi kucuk) korunuyor. Uzerine iki duzeltme:
     *
     *  - `PIECE_ART_SCALE`: 3B kup, duz kareden daha fazla yer ister; ayni
     *    olcude cizilirse hacim okunmaz, gene "leke" gibi gorunur.
     *  - `MIN_PIECE_WIDTH_DP`: tek hucreli parca 16dp'de kalirdi ve kup
     *    degil toz zerresi gibi okunurdu.
     *
     * ⚠️ TASMA: bu genislik ModeSelectLayoutTest tarafindan da okunuyor —
     * ust serit parcalarinin GERCEK cizim genisligi hesaba katilmadan
     * "metin sutununa girmiyor" denemez (bkz. o testin aciklamasi).
     */
    val widthDp: Float
        get() = maxOf(pattern[0].size * cellDp * PIECE_ART_SCALE, MIN_PIECE_WIDTH_DP)
}

/** Bkz. `WanderingPiece.widthDp`. */
private const val PIECE_ART_SCALE = 1.35f
private const val MIN_PIECE_WIDTH_DP = 26f

// SHAPE_PATTERNS'teki (BoomBlocksGame.kt) AYNI desenler, birebir kopyalandi —
// gameplay dosyasina bagimlilik eklememek icin sadece SEKIL kopyalandi,
// referans degil. fx/fy: gezinme yorungesinin MERKEZI (ekran fraksiyonu).
// seed/speed: her parca farkli fazdan baslar, farkli hizda gezinir — gercek
// rastgelelik degil ama gozle dagitik gorunur, sabit oldugu icin
// recomposition'lar arasi kararli. rangeDp: gezinme yarim-genligi.
val DEFAULT_WANDERING_PIECES = listOf(
    // VARLIK ESLEMESI: her parcanin hucre SAYISI kadar kup tasiyan varlik
    // secildi (1 hucre -> tekil kup, 2 -> ikili, 3+ -> uclu kume) ve
    // mumkun oldugunca ESKI RENGI tasiyan varlik tercih edildi, boylece
    // ekrandaki renk dagilimi degismedi.
    // Tek kup — SHAPE_PATTERNS[0]  (turuncuydu -> turuncu kup)
    WanderingPiece(listOf(listOf(true)), 0.08f, 0.18f, 16f, R.drawable.kb_block_orange, 0.80f, 0.05f, 1.0f, 55f),
    // 2'li duz parca — SHAPE_PATTERNS[1]  (camgobegiydi -> mavi iceren yatay ikili)
    WanderingPiece(listOf(listOf(true, true)), 0.90f, 0.16f, 12f, R.drawable.kb_block_pair_a, 0.75f, 0.34f, 1.25f, 60f),
    // 3'lu duz parca — SHAPE_PATTERNS[3]  (altindi -> sari agirlikli uclu kume)
    WanderingPiece(listOf(listOf(true, true, true)), 0.06f, 0.60f, 10f, R.drawable.kb_block_trio_a, 0.60f, 0.61f, 0.9f, 65f),
    // Kucuk L (kose, 3 hucre) — SHAPE_PATTERNS[6]  (mordu -> mor iceren dikey ikili)
    WanderingPiece(listOf(listOf(true, false), listOf(true, true)), 0.92f, 0.58f, 12f, R.drawable.kb_block_pair_b, 0.70f, 0.80f, 1.15f, 58f),
    // T parcasi — SHAPE_PATTERNS[10]
    WanderingPiece(listOf(listOf(true, true, true), listOf(false, true, false)), 0.50f, 0.94f, 11f, R.drawable.kb_block_trio_b, 0.75f, 0.47f, 1.05f, 62f),
    // 2x2 Kare — SHAPE_PATTERNS[5]  (maviydi -> mavi kup)
    WanderingPiece(listOf(listOf(true, true), listOf(true, true)), 0.10f, 0.85f, 10f, R.drawable.kb_block_blue, 0.65f, 0.22f, 1.1f, 60f),
    // S-tetromino — SHAPE_PATTERNS'teki S ailesi  (sariydi -> sari kup)
    WanderingPiece(listOf(listOf(false, true, true), listOf(true, true, false)), 0.88f, 0.85f, 9f, R.drawable.kb_block_yellow, 0.60f, 0.71f, 0.95f, 63f),
    // Z-tetromino — SHAPE_PATTERNS'teki Z ailesi  (yesildi -> yesil kup)
    WanderingPiece(listOf(listOf(true, true, false), listOf(false, true, true)), 0.06f, 0.38f, 9f, R.drawable.kb_block_green, 0.65f, 0.88f, 1.2f, 57f),
    // 4'lu duz parca — SHAPE_PATTERNS[14]
    WanderingPiece(listOf(listOf(true, true, true, true)), 0.92f, 0.38f, 8f, R.drawable.kb_block_trio_b, 0.55f, 0.39f, 0.85f, 65f)
)

// Kupler yerine gercek oyun parcalari, ekranda HIC donmeden geziniyor
// (kullanici acikca "donmesinler" dedi). Tek paylasilan animasyon kaynagi
// (`pieceTime`) tum parcalari besliyor, `.value` SADECE Canvas'in draw
// lambda'sinda okunuyor — EmbossedBlockCell'deki shimmerPhase ile AYNI
// performans gerekcesi: recomposition degil, sadece bu Canvas'in yeniden
// cizilmesi tetiklenir.
@Composable
fun WanderingPiecesBackground(
    modifier: Modifier = Modifier,
    pieces: List<WanderingPiece> = DEFAULT_WANDERING_PIECES,
    durationMillis: Int = 9_000
) {
    // Varliklar bir KEZ cozulur. Ayni varlik birden cok parcada
    // kullanilabildigi icin once TEKILLESTIRILIYOR: liste 9 parca tasisa da
    // yalnizca kullanilan farkli varlik sayisi kadar cozme yapilir ve ayni
    // bitmap paylasilir. Compose'un kaynak onbellegi bunu ekranlar arasinda
    // da paylastigi icin GameScreenBackground'un cozdugu ayni varliklar
    // yeniden cozulmez.
    val assetIds = remember(pieces) { pieces.map { it.asset }.distinct() }
    val bitmaps: Map<Int, ImageBitmap> =
        assetIds.associateWith { ImageBitmap.imageResource(it) }

    val pieceTime = rememberInfiniteTransition(label = "wanderingPieces").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "pieceTime"
    )
    // Bu Canvas dokunma YAKALAMAZ: uzerinde `clickable`/`pointerInput` yok,
    // yani katman kartlarin tiklamasini yutmaz — DESIGN_SPEC'in "particle
    // layers must not intercept touch" kurali. Bilesen `matchParentSize` ile
    // kartlarin uzerine seriliyor, o yuzden bu ozellik kazara bozulmamali.
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val t = pieceTime.value
        for (piece in pieces) {
            val bmp = bitmaps[piece.asset] ?: continue
            // ---- HAREKET: Faz 122'den beri DEGISMEDI ----
            val angle = (t + piece.seed) * piece.speed * 2f * PI.toFloat()
            val range = piece.rangeDp * density
            // Farkli X/Y frekanslari (0.9/1.3) — temiz bir daire/elips YERINE
            // duzensiz, "boslukta suzuluyor" hissi veren bir yol.
            val cx = w * piece.fx + sin(angle * 0.9f + piece.seed * 11f) * range
            val cy = h * piece.fy + cos(angle * 1.3f + piece.seed * 7f) * range
            // ---- CIZIM: duz dikdortgen yerine 3B kup varligi ----
            val drawW = piece.widthDp * density
            // En-boy orani KORUNUYOR (varlik yukseklikleri 220-516px arasi
            // degisiyor); sabit bir kare kutuya sigdirmak kupleri ezerdi.
            val drawH = drawW * (bmp.height.toFloat() / bmp.width.toFloat())
            drawImage(
                image = bmp,
                dstOffset = IntOffset(
                    (cx - drawW / 2f).roundToInt(),
                    (cy - drawH / 2f).roundToInt()
                ),
                dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt()),
                // Dekoratif seffaflik. Bu bir TINT DEGIL: varligin kendi
                // golgesi ve yuz aydinlatmasi oldugu gibi kaliyor, sadece
                // katman butunuyle geri cekiliyor ki icerigin onune gecmesin.
                alpha = piece.alpha,
                filterQuality = FilterQuality.High
            )
        }
    }
}
