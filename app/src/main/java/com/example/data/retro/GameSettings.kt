package com.example.data.retro

import android.content.Context
import android.content.SharedPreferences

enum class RetroThemeStyle {
    NEON_CYBERPUNK,
    GAME_BOY_LCD,
    ARCADE_CRT,
    MONOCHROME_8BIT,
    NES_SYNTH
}

enum class ControlStyle {
    DPAD_BUTTONS,
    TOUCH_GESTURES,
    HYBRID
}

// Faz 79: kullanici "custom'i cikart, easy/normal/hard kalsin" dedi.
enum class DifficultyPreset {
    EASY,
    NORMAL,
    HARD
}

data class GameSettings(
    val theme: RetroThemeStyle = RetroThemeStyle.NEON_CYBERPUNK,
    val controlStyle: ControlStyle = ControlStyle.HYBRID,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val ghostPieceEnabled: Boolean = true,
    val startingLevel: Int = 1,
    val difficultyPreset: DifficultyPreset = DifficultyPreset.NORMAL,
    val lastPlayerName: String = "RETRO_ACE"
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("retro_tetris_prefs", Context.MODE_PRIVATE)

    fun getSettings(): GameSettings {
        val themeName = prefs.getString("theme", RetroThemeStyle.NEON_CYBERPUNK.name) ?: RetroThemeStyle.NEON_CYBERPUNK.name
        val theme = runCatching { RetroThemeStyle.valueOf(themeName) }.getOrDefault(RetroThemeStyle.NEON_CYBERPUNK)

        val controlName = prefs.getString("controlStyle", ControlStyle.HYBRID.name) ?: ControlStyle.HYBRID.name
        val controlStyle = runCatching { ControlStyle.valueOf(controlName) }.getOrDefault(ControlStyle.HYBRID)

        val diffName = prefs.getString("difficultyPreset", DifficultyPreset.NORMAL.name) ?: DifficultyPreset.NORMAL.name
        val difficultyPreset = runCatching { DifficultyPreset.valueOf(diffName) }.getOrDefault(DifficultyPreset.NORMAL)

        return GameSettings(
            theme = theme,
            controlStyle = controlStyle,
            soundEnabled = prefs.getBoolean("soundEnabled", true),
            hapticsEnabled = prefs.getBoolean("hapticsEnabled", true),
            ghostPieceEnabled = prefs.getBoolean("ghostPieceEnabled", true),
            startingLevel = prefs.getInt("startingLevel", 1).coerceIn(1, 15),
            difficultyPreset = difficultyPreset,
            lastPlayerName = prefs.getString("lastPlayerName", "RETRO_ACE") ?: "RETRO_ACE"
        )
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit()
            .putString("theme", settings.theme.name)
            .putString("controlStyle", settings.controlStyle.name)
            .putBoolean("soundEnabled", settings.soundEnabled)
            .putBoolean("hapticsEnabled", settings.hapticsEnabled)
            .putBoolean("ghostPieceEnabled", settings.ghostPieceEnabled)
            .putInt("startingLevel", settings.startingLevel)
            .putString("difficultyPreset", settings.difficultyPreset.name)
            .putString("lastPlayerName", settings.lastPlayerName)
            .apply()
    }
}
