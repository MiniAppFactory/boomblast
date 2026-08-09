package com.example.game

// Parametrik, sonsuz level üretimi — elle yüzlerce level tasarlamak yerine
// zorluk seviyeye göre kademeli artıyor (bkz. docs/GENRE_RESEARCH_NOTES.md #5).
data class LevelDefinition(
    val number: Int,
    val targetScore: Int,
    // shapePoolTier, BlastTheBlocksGame.kt'deki generateNewTray()'in SHAPE_PATTERNS.take(n)
    // mantığıyla aynı kademelendirmeyi kullanır: 1=basit parçalar, 3=tüm parçalar.
    val shapePoolTier: Int
)

object LevelGenerator {
    fun forLevel(number: Int): LevelDefinition {
        val safeNumber = number.coerceAtLeast(1)
        val targetScore = 500 + (safeNumber - 1) * 250
        val shapePoolTier = when {
            safeNumber <= 3 -> 1
            safeNumber <= 8 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, targetScore, shapePoolTier)
    }
}
