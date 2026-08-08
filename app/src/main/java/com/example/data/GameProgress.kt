package com.example.data

enum class BoosterType(val tokenPrice: Int) {
    BOMB(35),
    LINE_CLEAR(50),
    SHUFFLE(20)
}

data class PlayerProgress(
    val tokens: Int = 150,
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val ownedBoosters: Map<BoosterType, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val isTr: Boolean = true,
    val blockTheme: String = "CLASSIC"
)

enum class MissionType { COMPLETE_LEVELS, CLEAR_LINES, USE_BOOSTERS, SCORE_POINTS }

data class WeeklyMissionDef(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val target: Int,
    val rewardTokens: Int,
    val type: MissionType
)

data class WeeklyMissionProgress(
    val weekId: String,
    val missions: List<WeeklyMissionDef>,
    val progress: Map<String, Int> = emptyMap(),
    val claimed: Set<String> = emptySet()
)
