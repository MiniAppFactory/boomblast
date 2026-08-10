package com.example.data

import kotlin.random.Random

// Hafta numarasına göre deterministik seed'lenen görev seçimi — aynı hafta içinde
// her çağrıda aynı 4 görev üretilir, yeni haftada otomatik değişir (bkz. GameStateRepository.currentWeekId).
object WeeklyMissionGenerator {

    private val POOL = listOf(
        WeeklyMissionDef("complete_5_levels", "5 Seviye Tamamla", "Complete 5 Levels", "Completa 5 Livelli", "Terminer 5 Niveaux", "Completa 5 Niveles", target = 5, rewardTokens = 25, type = MissionType.COMPLETE_LEVELS),
        WeeklyMissionDef("complete_15_levels", "15 Seviye Tamamla", "Complete 15 Levels", "Completa 15 Livelli", "Terminer 15 Niveaux", "Completa 15 Niveles", target = 15, rewardTokens = 40, type = MissionType.COMPLETE_LEVELS),
        WeeklyMissionDef("clear_100_lines", "100 Satır/Sütun Patlat", "Clear 100 Lines", "Elimina 100 Linee", "Effacer 100 Lignes", "Elimina 100 Líneas", target = 100, rewardTokens = 30, type = MissionType.CLEAR_LINES),
        WeeklyMissionDef("clear_300_lines", "300 Satır/Sütun Patlat", "Clear 300 Lines", "Elimina 300 Linee", "Effacer 300 Lignes", "Elimina 300 Líneas", target = 300, rewardTokens = 70, type = MissionType.CLEAR_LINES),
        WeeklyMissionDef("use_5_boosters", "5 Güçlendirici Kullan", "Use 5 Boosters", "Usa 5 Potenziamenti", "Utiliser 5 Boosts", "Usa 5 Potenciadores", target = 5, rewardTokens = 20, type = MissionType.USE_BOOSTERS),
        // Faz 45: puanlama 10x kucultuldu (bkz. BlastTheBlocksGame.placeShape) —
        // bu hedefler de orantili kuculdu (5000->500, 15000->1500), aksi halde
        // bu gorevler ONCEKINE gore 10 kat daha uzun surerdi.
        WeeklyMissionDef("score_500", "Toplamda 500 Puan Topla", "Score 500 Total Points", "Totalizza 500 Punti", "Marquer 500 Points au Total", "Consigue 500 Puntos en Total", target = 500, rewardTokens = 35, type = MissionType.SCORE_POINTS),
        WeeklyMissionDef("score_1500", "Toplamda 1500 Puan Topla", "Score 1500 Total Points", "Totalizza 1500 Punti", "Marquer 1500 Points au Total", "Consigue 1500 Puntos en Total", target = 1500, rewardTokens = 80, type = MissionType.SCORE_POINTS)
    )

    fun forWeek(weekId: String): List<WeeklyMissionDef> {
        val seed = weekId.hashCode().toLong()
        val shuffled = POOL.shuffled(Random(seed))
        return shuffled.take(4).sortedBy { it.type }
    }
}
