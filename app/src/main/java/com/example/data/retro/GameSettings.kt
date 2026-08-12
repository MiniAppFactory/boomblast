package com.example.data.retro

import android.content.Context
import android.content.SharedPreferences
import com.example.data.AppLanguage
import com.example.data.pick

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

// Faz 80: Retro Modu 5 dil destegi - RetroMenuScreen/RetroHighScoreScreen/
// RetroSettingsScreen'deki tekrarlayan enum etiketleri icin merkezi cevirim.
fun RetroThemeStyle.label(language: AppLanguage): String = when (this) {
    RetroThemeStyle.NEON_CYBERPUNK -> language.pick(
        tr = "Neon Cyberpunk (Varsayılan)", en = "Neon Cyberpunk (Default)",
        it = "Neon Cyberpunk (Predefinito)", fr = "Neon Cyberpunk (Défaut)", es = "Neon Cyberpunk (Predeterminado)"
    )
    RetroThemeStyle.GAME_BOY_LCD -> language.pick(
        tr = "1989 Game Boy Yeşil LCD", en = "1989 Game Boy Green LCD",
        it = "1989 Game Boy LCD Verde", fr = "1989 Game Boy LCD Vert", es = "1989 Game Boy LCD Verde"
    )
    RetroThemeStyle.ARCADE_CRT -> language.pick(
        tr = "Retro Arcade CRT", en = "Retro Arcade CRT",
        it = "Retro Arcade CRT", fr = "Retro Arcade CRT", es = "Retro Arcade CRT"
    )
    RetroThemeStyle.MONOCHROME_8BIT -> language.pick(
        tr = "8-Bit Monokrom Matrix", en = "8-Bit Monochrome Matrix",
        it = "Matrice Monocromatica 8-Bit", fr = "Matrice Monochrome 8-Bit", es = "Matriz Monocromática 8-Bit"
    )
    RetroThemeStyle.NES_SYNTH -> language.pick(
        tr = "80'ler Synthwave Günbatımı", en = "80s Synthwave Sunset",
        it = "Tramonto Synthwave Anni '80", fr = "Coucher de Soleil Synthwave 80s", es = "Atardecer Synthwave de los 80"
    )
}

fun ControlStyle.label(language: AppLanguage): String = when (this) {
    ControlStyle.DPAD_BUTTONS -> language.pick(tr = "YÖN TUŞLARI", en = "D-PAD", it = "D-PAD", fr = "D-PAD", es = "D-PAD")
    ControlStyle.TOUCH_GESTURES -> language.pick(tr = "DOKUNMATİK", en = "GESTURES", it = "GESTI", fr = "GESTES", es = "GESTOS")
    ControlStyle.HYBRID -> language.pick(tr = "HİBRİT", en = "HYBRID", it = "IBRIDO", fr = "HYBRIDE", es = "HÍBRIDO")
}

fun DifficultyPreset.label(language: AppLanguage): String = when (this) {
    DifficultyPreset.EASY -> language.pick(tr = "KOLAY", en = "EASY", it = "FACILE", fr = "FACILE", es = "FÁCIL")
    DifficultyPreset.NORMAL -> language.pick(tr = "NORMAL", en = "NORMAL", it = "NORMALE", fr = "NORMAL", es = "NORMAL")
    DifficultyPreset.HARD -> language.pick(tr = "ZOR", en = "HARD", it = "DIFFICILE", fr = "DIFFICILE", es = "DIFÍCIL")
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
