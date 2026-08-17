package com.miniappfactory.boomblocks.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import com.miniappfactory.boomblocks.ui.theme.BlockBlue
import com.miniappfactory.boomblocks.ui.theme.BlockGreen
import com.miniappfactory.boomblocks.ui.theme.BlockOrange
import com.miniappfactory.boomblocks.ui.theme.BlockPink
import com.miniappfactory.boomblocks.ui.theme.BlockYellow
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Faz 124: ModeSelectScreen/TermsAcceptScreen/OnboardingScreen'de UCUNDE de
// AYNI "dondurulmus konfeti kupu" deseni ayri ayri kopyalanmisti (Faz 115h).
// Kullanici ModeSelectScreen'de bu deseni gercek oyun parcasi geometrilerine
// (Faz 122) ve gezinme animasyonuna (Faz 120/121/123) cevirince, ayni istegi
// diger iki ekran icin de yaptı — uc kopyayi elle senkron tutmak yerine TEK
// paylasilan composable'a cikarildi. Degistirmek istersen sadece burasi.
data class WanderingPiece(
    val pattern: List<List<Boolean>>,
    val fx: Float,
    val fy: Float,
    val cellDp: Float,
    val color: Color,
    val alpha: Float,
    val seed: Float,
    val speed: Float,
    val rangeDp: Float
)

// SHAPE_PATTERNS'teki (BoomBlocksGame.kt) AYNI desenler, birebir kopyalandi —
// gameplay dosyasina bagimlilik eklememek icin sadece SEKIL kopyalandi,
// referans degil. fx/fy: gezinme yorungesinin MERKEZI (ekran fraksiyonu).
// seed/speed: her parca farkli fazdan baslar, farkli hizda gezinir — gercek
// rastgelelik degil ama gozle dagitik gorunur, sabit oldugu icin
// recomposition'lar arasi kararli. rangeDp: gezinme yarim-genligi.
val DEFAULT_WANDERING_PIECES = listOf(
    // Tek kup — SHAPE_PATTERNS[0]
    WanderingPiece(listOf(listOf(true)), 0.08f, 0.18f, 16f, BlockOrange, 0.80f, 0.05f, 1.0f, 55f),
    // 2'li duz parca — SHAPE_PATTERNS[1]
    WanderingPiece(listOf(listOf(true, true)), 0.90f, 0.16f, 12f, NeonCyan, 0.75f, 0.34f, 1.25f, 60f),
    // 3'lu duz parca — SHAPE_PATTERNS[3]
    WanderingPiece(listOf(listOf(true, true, true)), 0.06f, 0.60f, 10f, NeonGold, 0.60f, 0.61f, 0.9f, 65f),
    // Kucuk L (kose, 3 hucre) — SHAPE_PATTERNS[6]
    WanderingPiece(listOf(listOf(true, false), listOf(true, true)), 0.92f, 0.58f, 12f, NeonPurple, 0.70f, 0.80f, 1.15f, 58f),
    // T parcasi — SHAPE_PATTERNS[10]
    WanderingPiece(listOf(listOf(true, true, true), listOf(false, true, false)), 0.50f, 0.94f, 11f, BlockPink, 0.75f, 0.47f, 1.05f, 62f),
    // 2x2 Kare — SHAPE_PATTERNS[5]
    WanderingPiece(listOf(listOf(true, true), listOf(true, true)), 0.10f, 0.85f, 10f, BlockBlue, 0.65f, 0.22f, 1.1f, 60f),
    // S-tetromino — SHAPE_PATTERNS'teki S ailesi
    WanderingPiece(listOf(listOf(false, true, true), listOf(true, true, false)), 0.88f, 0.85f, 9f, BlockYellow, 0.60f, 0.71f, 0.95f, 63f),
    // Z-tetromino — SHAPE_PATTERNS'teki Z ailesi
    WanderingPiece(listOf(listOf(true, true, false), listOf(false, true, true)), 0.06f, 0.38f, 9f, BlockGreen, 0.65f, 0.88f, 1.2f, 57f),
    // 4'lu duz parca — SHAPE_PATTERNS[14]
    WanderingPiece(listOf(listOf(true, true, true, true)), 0.92f, 0.38f, 8f, NeonGreen, 0.55f, 0.39f, 0.85f, 65f)
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
    val pieceTime = rememberInfiniteTransition(label = "wanderingPieces").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "pieceTime"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val t = pieceTime.value
        for (piece in pieces) {
            val angle = (t + piece.seed) * piece.speed * 2f * PI.toFloat()
            val range = piece.rangeDp * density
            // Farkli X/Y frekanslari (0.9/1.3) — temiz bir daire/elips YERINE
            // duzensiz, "boslukta suzuluyor" hissi veren bir yol.
            val cx = w * piece.fx + sin(angle * 0.9f + piece.seed * 11f) * range
            val cy = h * piece.fy + cos(angle * 1.3f + piece.seed * 7f) * range
            val cell = piece.cellDp * density
            val gap = cell * 0.12f
            val rows = piece.pattern.size
            val cols = piece.pattern[0].size
            val originX = cx - (cols * cell) / 2f
            val originY = cy - (rows * cell) / 2f
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (!piece.pattern[r][c]) continue
                    val topLeft = Offset(originX + c * cell + gap / 2f, originY + r * cell + gap / 2f)
                    val cellSize = Size(cell - gap, cell - gap)
                    val corner = CornerRadius(cellSize.width * 0.24f)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                lerp(piece.color, Color.White, 0.35f),
                                piece.color,
                                lerp(piece.color, Color.Black, 0.35f)
                            ),
                            start = topLeft,
                            end = Offset(topLeft.x + cellSize.width, topLeft.y + cellSize.height)
                        ),
                        topLeft = topLeft,
                        size = cellSize,
                        cornerRadius = corner,
                        alpha = piece.alpha
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.5f * piece.alpha),
                        topLeft = topLeft,
                        size = cellSize,
                        cornerRadius = corner,
                        style = Stroke(width = cellSize.width * 0.09f)
                    )
                }
            }
        }
    }
}
