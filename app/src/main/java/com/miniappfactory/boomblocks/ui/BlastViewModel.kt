package com.miniappfactory.boomblocks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miniappfactory.boomblocks.data.AD_TOKEN_REWARD
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.BoosterType
import com.miniappfactory.boomblocks.data.EffectIntensity
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

    // Faz 129: yalnizca GERCEK rewarded odulunde cagriliyor — uc cagri noktasinin
    // (Kariyer / Pro / Kolay Mod loadout ekranlari) hepsi RewardedAdManager'in
    // onRewardEarned callback'ine bagli, erken kapatan odul almiyor. Miktar
    // AD_TOKEN_REWARD'dan geliyor (bkz. GameProgress.kt) — etiketle ayni kaynak.
    // NOT: eski yorum "Faz 4'te gercek rewarded ad'e baglanacak, simdilik test
    // odulu" diyordu; coktan baglanmisti, yorum bayattı.
    fun watchAdForTokens() {
        viewModelScope.launch { repository.addTokens(AD_TOKEN_REWARD) }
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

    // Faz 130: ayni "oyun ici reklam izle, +1 guclendirici" firsati Kariyer/Kolay
    // ve Pro modlarina da acildi. Uc ayri envanter var, o yuzden uc ayri fonksiyon:
    //   grantBoosterFromAd          -> ownedBoosters          (Kariyer + Kolay Mod, ORTAK)
    //   grantChallengeBoosterFromAd -> challengeOwnedBoosters (Pro)
    //   grantEndlessBoosterFromAd   -> endlessOwnedBoosters   (Sonsuz)
    // Hepsi SATIN ALINMAZ, sadece gercek rewarded odulunde cagrilir.
    fun grantBoosterFromAd(type: BoosterType) {
        viewModelScope.launch { repository.addBooster(type) }
    }

    fun grantChallengeBoosterFromAd(type: BoosterType) {
        viewModelScope.launch { repository.addChallengeBooster(type) }
    }

    // Faz 131: Kolay Mod kendi envanterini kullanir — Pro'nunkiyle AYNI desen.
    fun buyComfortBooster(type: BoosterType) {
        viewModelScope.launch {
            if (repository.spendTokens(type.tokenPrice)) {
                repository.addComfortBooster(type)
            }
        }
    }

    fun consumeComfortBoosterFromInventory(type: BoosterType) {
        viewModelScope.launch {
            repository.consumeComfortBooster(type)
            repository.incrementMissionProgress(MissionType.USE_BOOSTERS, 1)
        }
    }

    fun grantComfortBoosterFromAd(type: BoosterType) {
        viewModelScope.launch { repository.addComfortBooster(type) }
    }

    // Faz 137: tema satin alma. Tema ancak jeton GERCEKTEN dustuyse secili hale
    // geliyor (repository.unlockTheme atomik, bkz. oradaki not) — yetersiz
    // bakiyede hicbir sey olmuyor, arayuz zaten fiyat rozetini soluk gosteriyor.
    fun unlockTheme(themeId: String, price: Int) {
        viewModelScope.launch {
            if (repository.unlockTheme(themeId, price)) {
                repository.setBlockTheme(themeId)
            }
        }
    }

    fun resetEndlessBoosters() {
        viewModelScope.launch { repository.resetEndlessBoosters() }
    }

    fun recordLinesCleared(count: Int) {
        viewModelScope.launch { repository.incrementMissionProgress(MissionType.CLEAR_LINES, count) }
    }

    fun recordLevelComplete(level: Int, score: Int, stars: Int) {
        viewModelScope.launch {
            // Faz 139: jeton SADECE ilk gecerde (bkz. repository notu).
            // Faz 149: 5 -> 10 (kullanici karari, ekonomi yeniden dengelendi).
            if (repository.recordLevelResult(level, stars)) repository.addTokens(10)
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
            // Faz 139: jeton SADECE ilk gecerde (bkz. repository notu).
            val firstClear = repository.recordChallengeLevelResult(level, stars)
            // Faz 138: Pro odulu 5 -> 10. Gerekce: bir Pro bolumu Kariyer/Kolay
            // bolumunden cok daha uzun suruyor (hedef puan 250+ ve +50/bolum,
            // 1x1 parca yok, tam havuz bolum 9'da acik) — ayni 5 jetonu odemek
            // dakika basina en dusuk odul demekti. 10'da bile Pro, dakika basina
            // Kolay Mod'dan daha az kazandiriyor, yani "en verimli jeton yolu"
            // haline GELMIYOR; sadece adil oluyor.
            // Faz 149: 10 -> 25. Pro bolumu digerlerinden cok daha uzun
            // suruyor (hedef 250 ve +50/bolum, 1x1 yok, tam havuz bolum 9).
            if (firstClear) repository.addTokens(25)
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

    // --- Comfort Mode (Kolay Mod) ---

    // Faz 128: recordChallengeLevelComplete ile ayni desen. Jeton odulu Kariyer/
    // Pro ile AYNI (5) tutuldu — Kolay Mod bolumleri daha hizli gectigi icin
    // jeton kazanimi zaten dogal olarak daha yuksek, ustune carpan verilmedi.
    fun recordComfortLevelComplete(level: Int, score: Int, stars: Int) {
        viewModelScope.launch {
            // Faz 139: jeton SADECE ilk gecerde (bkz. repository notu).
            // Faz 149: 5 -> 10, Kariyer ile ayni.
            if (repository.recordComfortLevelResult(level, stars)) repository.addTokens(10)
            repository.incrementMissionProgress(MissionType.COMPLETE_LEVELS, 1)
            repository.incrementMissionProgress(MissionType.SCORE_POINTS, score)
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

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    // Faz 109: patlama efekti yogunlugu (Düşük/Normal/Yüksek) — setHapticsEnabled
    // ile ayni desen, sadece Boolean yerine uc degerli enum.
    fun setEffectIntensity(intensity: EffectIntensity) {
        viewModelScope.launch { repository.setEffectIntensity(intensity) }
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
