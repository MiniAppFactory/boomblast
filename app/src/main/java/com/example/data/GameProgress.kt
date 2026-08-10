package com.example.data

// Faz 47: kullanici "güçlendiriciler daha pahalı olmalı, reklam izlemeye
// teşvik etmeli, bomba hepsi 100 olmalı, shuffle olmasın, bomba ve line
// clear olsun sadece" dedi — SHUFFLE turu tamamen kaldirildi (35/50/20 ->
// sadece BOMB/LINE_CLEAR, ikisi de 100).
enum class BoosterType(val tokenPrice: Int) {
    BOMB(100),
    LINE_CLEAR(100)
}

data class PlayerProgress(
    val tokens: Int = 150,
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val ownedBoosters: Map<BoosterType, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.5f,
    val musicEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.TR,
    val blockTheme: String = "CLASSIC",
    val uiSkin: String = "DEFAULT",
    val hasSeenOnboarding: Boolean = false,
    val endlessHighScore: Int = 0,
    val hasAcceptedTerms: Boolean = false,
    val hasMadeFirstMove: Boolean = false,
    val notificationsEnabled: Boolean = true,
    // Faz 39/42: Seviye Modu'nda son zorunlu gecis reklamindan bu yana kac
    // bolum tamamlandi — esige (Faz 42: her bolum) ulasinca sifirlanir ve bir interstitial gosterilir.
    val levelsCompletedSinceInterstitial: Int = 0
)

enum class MissionType { COMPLETE_LEVELS, CLEAR_LINES, USE_BOOSTERS, SCORE_POINTS }

data class WeeklyMissionDef(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val titleIt: String,
    val titleFr: String,
    val titleEs: String,
    val target: Int,
    val rewardTokens: Int,
    val type: MissionType
) {
    fun title(language: AppLanguage): String =
        language.pick(tr = titleTr, en = titleEn, it = titleIt, fr = titleFr, es = titleEs)
}

data class WeeklyMissionProgress(
    val weekId: String,
    val missions: List<WeeklyMissionDef>,
    val progress: Map<String, Int> = emptyMap(),
    val claimed: Set<String> = emptySet()
)
