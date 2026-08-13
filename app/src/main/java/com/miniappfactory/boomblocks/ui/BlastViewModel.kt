package com.miniappfactory.boomblocks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.BoosterType
import com.miniappfactory.boomblocks.data.GameStateRepository
import com.miniappfactory.boomblocks.data.MissionType
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.data.WeeklyMissionGenerator
import com.miniappfactory.boomblocks.data.WeeklyMissionProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BlastViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameStateRepository(application)

    val playerProgress: StateFlow<PlayerProgress> = repository.playerProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProgress())

    val weeklyMissions: StateFlow<WeeklyMissionProgress> = repository.weeklyMissionProgress
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            run {
                val weekId = GameStateRepository.currentWeekId()
                WeeklyMissionProgress(weekId, WeeklyMissionGenerator.forWeek(weekId))
            }
        )

    fun buyBooster(type: BoosterType) {
        viewModelScope.launch {
            if (repository.spendTokens(type.tokenPrice)) {
                repository.addBooster(type)
            }
        }
    }

    fun watchAdForTokens() {
        // Faz 4'te gerçek rewarded ad sonucuna bağlanacak; şimdilik sabit bir test ödülü.
        viewModelScope.launch { repository.addTokens(15) }
    }

    fun consumeBoosterFromInventory(type: BoosterType) {
        viewModelScope.launch {
            repository.consumeBooster(type)
            repository.incrementMissionProgress(MissionType.USE_BOOSTERS, 1)
        }
    }

    // Faz 94: Pro Mode kendi ayri booster envanterini kullanir — buyBooster/
    // consumeBoosterFromInventory ile AYNI desen, sadece repository'nin
    // Challenge-ozel fonksiyonlarina yonlendiriyor.
    fun buyChallengeBooster(type: BoosterType) {
        viewModelScope.launch {
            if (repository.spendTokens(type.tokenPrice)) {
                repository.addChallengeBooster(type)
            }
        }
    }

    fun consumeChallengeBoosterFromInventory(type: BoosterType) {
        viewModelScope.launch {
            repository.consumeChallengeBooster(type)
            repository.incrementMissionProgress(MissionType.USE_BOOSTERS, 1)
        }
    }

    // Faz 94: Sonsuz Mod kendi ayri booster envanterini kullanir — coin ile
    // SATIN ALINMAZ (buyBooster/buyChallengeBooster'in aksine), sadece reklam
    // odulu sonrasi grantEndlessBoosterFromAd cagrilir.
    fun consumeEndlessBoosterFromInventory(type: BoosterType) {
        viewModelScope.launch {
            repository.consumeEndlessBooster(type)
            repository.incrementMissionProgress(MissionType.USE_BOOSTERS, 1)
        }
    }

    fun grantEndlessBoosterFromAd(type: BoosterType) {
        viewModelScope.launch { repository.addEndlessBooster(type) }
    }

    fun resetEndlessBoosters() {
        viewModelScope.launch { repository.resetEndlessBoosters() }
    }

    fun recordLinesCleared(count: Int) {
        viewModelScope.launch { repository.incrementMissionProgress(MissionType.CLEAR_LINES, count) }
    }

    fun recordLevelComplete(level: Int, score: Int, stars: Int) {
        viewModelScope.launch {
            repository.recordLevelResult(level, stars)
            repository.incrementMissionProgress(MissionType.COMPLETE_LEVELS, 1)
            repository.incrementMissionProgress(MissionType.SCORE_POINTS, score)
            repository.incrementLevelsSinceInterstitial()
        }
    }

    fun resetLevelsSinceInterstitial() {
        viewModelScope.launch { repository.resetLevelsSinceInterstitial() }
    }

    // --- Pro Mode (Challenge) ---

    fun recordChallengeLevelComplete(level: Int, score: Int, stars: Int) {
        viewModelScope.launch {
            repository.recordChallengeLevelResult(level, stars)
            repository.incrementMissionProgress(MissionType.COMPLETE_LEVELS, 1)
            repository.incrementMissionProgress(MissionType.SCORE_POINTS, score)
            // recordLevelComplete'teki ile ayni sayaci artiriyor — AppNavigation'daki
            // CHALLENGE_GAME'in onLevelCompleteContinue'su bu sayaci okuyordu ama
            // hicbir yerde artirmiyordu, yani Pro Mode'da bolum sonrasi reklam
            // ASLA tetiklenmiyordu (kullanici bildirdi: "pro modda da her bölüm
            // sonrası reklam olacak").
            repository.incrementLevelsSinceInterstitial()
        }
    }

    // Faz 96: can sistemi kaldirildi — "Yeniden Başlat" artik bu esikli
    // sayacla interstitial reklama bagli (resetLevelsSinceInterstitial ile
    // AYNI desen, ayri bir sayac).
    fun incrementProRestartsSinceInterstitial() {
        viewModelScope.launch { repository.incrementProRestartsSinceInterstitial() }
    }

    fun resetProRestartsSinceInterstitial() {
        viewModelScope.launch { repository.resetProRestartsSinceInterstitial() }
    }

    fun claimMission(missionId: String, tierIndex: Int) {
        viewModelScope.launch {
            val mission = weeklyMissions.value.missions.find { it.id == missionId } ?: return@launch
            val tier = mission.tiers.getOrNull(tierIndex) ?: return@launch
            repository.claimMission(missionId, tierIndex, tier.rewardTokens)
        }
    }

    fun recordMultiClear() {
        viewModelScope.launch { repository.incrementMissionProgress(MissionType.MULTI_CLEARS, 1) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setMusicEnabled(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setBlockTheme(theme: String) {
        viewModelScope.launch { repository.setBlockTheme(theme) }
    }

    fun setUiSkin(skin: String) {
        viewModelScope.launch { repository.setUiSkin(skin) }
    }

    fun markFirstMoveMade() {
        viewModelScope.launch { repository.markFirstMoveMade() }
    }

    fun setSoundVolume(volume: Float) {
        viewModelScope.launch { repository.setSoundVolume(volume) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun markOnboardingSeen() {
        viewModelScope.launch { repository.markOnboardingSeen() }
    }

    fun markTermsAccepted() {
        viewModelScope.launch { repository.markTermsAccepted() }
    }

    fun recordEndlessScore(score: Int) {
        viewModelScope.launch { repository.recordEndlessScore(score) }
    }
}
