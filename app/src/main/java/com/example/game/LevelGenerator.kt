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
        val targetScore = targetScoreForLevel(safeNumber)
        val shapePoolTier = when {
            safeNumber <= 3 -> 1
            safeNumber <= 8 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, targetScore, shapePoolTier)
    }

    // Faz 64: kullanici "amacımız çok bölüm geçsin, çok reklam izlesin —
    // challenge ile değil bölümü geçtim başarma isteğiyle oynatmalıyız" dedi.
    // Faz 45/61'deki kademeli (40/10/8/2) egri tamamen terk edildi — DUZ,
    // sabit +5/seviye artis (100, 105, 110, 115...). Amac bilerek "hicbir
    // zaman zorlasmiyor gibi hissettiren" bir egri: her seviye bir oncekine
    // neredeyse ozdes, oyuncu skill/challenge yerine sadece "bir tane daha
    // gectim" tekrarindan motive oluyor.
    private fun targetScoreForLevel(n: Int): Int = 100 + (n - 1) * 5
}
