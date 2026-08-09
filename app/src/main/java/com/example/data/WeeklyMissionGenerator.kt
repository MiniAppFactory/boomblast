package com.example.data

import kotlin.random.Random

// Hafta numarasına göre deterministik seed'lenen görev seçimi — aynı hafta içinde
// her çağrıda aynı 4 görev üretilir, yeni haftada otomatik değişir (bkz. GameStateRepository.currentWeekId).
object WeeklyMissionGenerator {

    private val POOL = listOf(
        WeeklyMissionDef("complete_5_levels", "5 Seviye Tamamla", "Complete 5 Levels", target = 5, rewardTokens = 25, type = MissionType.COMPLETE_LEVELS),
        WeeklyMissionDef("complete_15_levels", "15 Seviye Tamamla", "Complete 15 Levels", target = 15, rewardTokens = 40, type = MissionType.COMPLETE_LEVELS),
        WeeklyMissionDef("clear_100_lines", "100 Satır/Sütun Patlat", "Clear 100 Lines", target = 100, rewardTokens = 30, type = MissionType.CLEAR_LINES),
        WeeklyMissionDef("clear_300_lines", "300 Satır/Sütun Patlat", "Clear 300 Lines", target = 300, rewardTokens = 70, type = MissionType.CLEAR_LINES),
        WeeklyMissionDef("use_5_boosters", "5 Güçlendirici Kullan", "Use 5 Boosters", target = 5, rewardTokens = 20, type = MissionType.USE_BOOSTERS),
        WeeklyMissionDef("score_5000", "Toplamda 5000 Puan Topla", "Score 5000 Total Points", target = 5000, rewardTokens = 35, type = MissionType.SCORE_POINTS),
        WeeklyMissionDef("score_15000", "Toplamda 15000 Puan Topla", "Score 15000 Total Points", target = 15000, rewardTokens = 80, type = MissionType.SCORE_POINTS)
    )

    fun forWeek(weekId: String): List<WeeklyMissionDef> {
        val seed = weekId.hashCode().toLong()
        val shuffled = POOL.shuffled(Random(seed))
        return shuffled.take(4).sortedBy { it.type }
    }
}
