package com.example.ui.retro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.retro.DifficultyPreset
import com.example.data.retro.GameSettings
import com.example.data.retro.HighScore
import com.example.data.retro.RetroHighScoreRepository
import com.example.data.retro.UserPreferencesRepository
import com.example.game.retro.GameStatus
import com.example.game.retro.RetroSoundManager
import com.example.game.retro.TetrisGameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// AI Studio'daki MainViewModel.kt'den adapte edildi. Faz 78: kendi ic
// ScreenState/Crossfade navigasyon state machine'i (MENU/GAME/HIGH_SCORES/
// SETTINGS) COMPLETELY CIKARILDI — Boom Blocks'un tek NavHost'u (AppNavigation.kt)
// artik bu rolu ustleniyor (Routes.RETRO_MENU/RETRO_GAME/RETRO_HIGH_SCORES/
// RETRO_SETTINGS). Room tabanli HighScoreRepository yerine SharedPreferences
// tabanli RetroHighScoreRepository kullaniliyor (bkz. o dosyadaki yorum).
class RetroViewModel(application: Application) : AndroidViewModel(application) {

    private val highScoreRepo = RetroHighScoreRepository(application)
    private val prefsRepo = UserPreferencesRepository(application)
    val soundManager = RetroSoundManager(application)

    val gameEngine = TetrisGameEngine()

    private val _settings = MutableStateFlow(prefsRepo.getSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _selectedLeaderboardDifficulty = MutableStateFlow<String?>("ALL")
    val selectedLeaderboardDifficulty: StateFlow<String?> = _selectedLeaderboardDifficulty.asStateFlow()

    val topScores: StateFlow<List<HighScore>> = highScoreRepo.topScores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val highestScoreEver: StateFlow<Int?> = highScoreRepo.highestScoreEver.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalGamesPlayed: StateFlow<Int> = highScoreRepo.totalGamesPlayed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalLinesCleared: StateFlow<Int?> = highScoreRepo.totalLinesCleared.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val gameState = gameEngine.gameState

    private var dropJob: Job? = null
    private var timerJob: Job? = null

    init {
        // Wire sound manager to engine events
        gameEngine.onSoundEvent = { effect ->
            soundManager.playSound(effect)
        }
        applySoundSettings(_settings.value)
    }

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
        prefsRepo.saveSettings(newSettings)
        applySoundSettings(newSettings)
    }

    private fun applySoundSettings(settings: GameSettings) {
        soundManager.soundEnabled = settings.soundEnabled
        soundManager.hapticsEnabled = settings.hapticsEnabled
    }

    fun startNewGame() {
        val highest = highestScoreEver.value ?: 0
        gameEngine.startGame(_settings.value, highest)
        startGameLoops()
    }

    fun pauseGame() {
        gameEngine.pauseGame()
    }

    fun resumeGame() {
        gameEngine.resumeGame()
        startGameLoops()
    }

    fun saveHighScore(playerName: String) {
        val state = gameState.value
        val currentDiff = _settings.value.difficultyPreset.name
        viewModelScope.launch {
            highScoreRepo.saveScore(
                HighScore(
                    playerName = playerName,
                    score = state.score,
                    linesCleared = state.lines,
                    levelReached = state.level,
                    durationSeconds = state.gameDurationSeconds,
                    difficultyMode = currentDiff
                )
            )
            // Update last used player name
            updateSettings(_settings.value.copy(lastPlayerName = playerName))
        }
    }

    fun selectLeaderboardDifficulty(difficulty: String?) {
        _selectedLeaderboardDifficulty.value = difficulty
    }

    private fun startGameLoops() {
        dropJob?.cancel()
        timerJob?.cancel()

        // Loop 1: Automatic Gravity Drop
        dropJob = viewModelScope.launch {
            while (gameState.value.status == GameStatus.PLAYING) {
                val interval = calculateDropInterval(gameState.value.level, _settings.value.difficultyPreset)
                delay(interval)
                if (gameState.value.status == GameStatus.PLAYING) {
                    gameEngine.tickDrop()
                }
            }
        }

        // Loop 2: Game Timer Duration
        timerJob = viewModelScope.launch {
            while (gameState.value.status == GameStatus.PLAYING) {
                delay(1000)
                if (gameState.value.status == GameStatus.PLAYING) {
                    gameEngine.tickDuration()
                }
            }
        }
    }

    private fun calculateDropInterval(level: Int, preset: DifficultyPreset): Long {
        val baseMs = when {
            level <= 1 -> 800L
            level == 2 -> 710L
            level == 3 -> 620L
            level == 4 -> 530L
            level == 5 -> 440L
            level == 6 -> 360L
            level == 7 -> 280L
            level == 8 -> 200L
            level == 9 -> 140L
            else -> maxOf(45L, 100L - (level - 10) * 8L)
        }

        val multiplier = when (preset) {
            DifficultyPreset.EASY -> 1.25f
            DifficultyPreset.NORMAL -> 1.0f
            DifficultyPreset.HARD -> 0.75f
            DifficultyPreset.CUSTOM -> 1.0f
        }

        return (baseMs * multiplier).toLong().coerceAtLeast(40L)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
