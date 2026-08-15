package com.miniappfactory.boomblocks.ui.games.boomblocks

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Faz 4: Ambient toz parcacigi. Havuz mimarisinin aksine (BlastParticle),
 * bu tam ekranli FX katmaninda talimatsiz hareket ediyor.
 *
 * - Yukaridan asagiya (yavaş) + X'te sinus dalgasi
 * - Yasam oyuzde 50'den sonra fade out
 * - Beyaz/parlak, seffaf
 */
data class AmbientParticle(
    val x: Float,           // ekran yuzde (0..1)
    val y: Float,           // ekran yuzde (0..1)
    val vx: Float,          // ekran yuzde / saniye
    val vy: Float,          // ekran yuzde / saniye
    val age: Float,         // saniye
    val maxAge: Float,      // saniye
    val sinePhase: Float,   // sinus dalgasi baslangic fazi
    val size: Float         // dp cinsinden (yaklasik 2-6)
)

/**
 * Radyal isin flash parametreleri. Patlamada tetikleniyor,
 * hizli fade (~300-400ms).
 */
data class RadialFlashState(
    val centerX: Float,     // ekran X pixel
    val centerY: Float,     // ekran Y pixel
    val progress: Float,    // 0..1 (animatyon)
    val beamCount: Int,     // 8-12
    val rotationOffset: Float // random
)
