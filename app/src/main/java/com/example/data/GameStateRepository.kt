package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

private val Context.gameDataStore by preferencesDataStore(name = "blast_the_blocks_progress")

// DataStore Preferences dışında bir bağımlılık (Room/Moshi/kotlinx.serialization) eklememek için
// Map/Set alanları basit "anahtar:değer,anahtar:değer" string'i olarak kodlanıyor.
class GameStateRepository(private val context: Context) {

    private object Keys {
        val TOKENS = intPreferencesKey("tokens")
        val HIGHEST_LEVEL = intPreferencesKey("highest_unlocked_level")
        val LEVEL_STARS = stringPreferencesKey("level_stars")
        val OWNED_BOOSTERS = stringPreferencesKey("owned_boosters")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val IS_TR = booleanPreferencesKey("is_tr")
        val BLOCK_THEME = stringPreferencesKey("block_theme")

        val MISSION_WEEK_ID = stringPreferencesKey("mission_week_id")
        val MISSION_PROGRESS = stringPreferencesKey("mission_progress")
        val MISSION_CLAIMED = stringPreferencesKey("mission_claimed")
    }

    val playerProgress: Flow<PlayerProgress> = context.gameDataStore.data.map { prefs ->
        PlayerProgress(
            tokens = prefs[Keys.TOKENS] ?: 150,
            highestUnlockedLevel = prefs[Keys.HIGHEST_LEVEL] ?: 1,
            levelStars = decodeIntMap(prefs[Keys.LEVEL_STARS]),
            ownedBoosters = decodeBoosterMap(prefs[Keys.OWNED_BOOSTERS]),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            darkMode = prefs[Keys.DARK_MODE] ?: true,
            isTr = prefs[Keys.IS_TR] ?: true,
            blockTheme = prefs[Keys.BLOCK_THEME] ?: "CLASSIC"
        )
    }

    val weeklyMissionProgress: Flow<WeeklyMissionProgress> = context.gameDataStore.data.map { prefs ->
        val weekId = prefs[Keys.MISSION_WEEK_ID] ?: currentWeekId()
        WeeklyMissionProgress(
            weekId = weekId,
            missions = WeeklyMissionGenerator.forWeek(weekId),
            progress = decodeStringIntMap(prefs[Keys.MISSION_PROGRESS]),
            claimed = decodeStringSet(prefs[Keys.MISSION_CLAIMED])
        )
    }

    suspend fun addTokens(amount: Int) {
        context.gameDataStore.edit { prefs ->
            val current = prefs[Keys.TOKENS] ?: 150
            prefs[Keys.TOKENS] = current + amount
        }
    }

    suspend fun spendTokens(amount: Int): Boolean {
        var success = false
        context.gameDataStore.edit { prefs ->
            val current = prefs[Keys.TOKENS] ?: 150
            if (current >= amount) {
                prefs[Keys.TOKENS] = current - amount
                success = true
            }
        }
        return success
    }

    suspend fun recordLevelResult(level: Int, stars: Int) {
        context.gameDataStore.edit { prefs ->
            val currentHighest = prefs[Keys.HIGHEST_LEVEL] ?: 1
            if (level >= currentHighest) {
                prefs[Keys.HIGHEST_LEVEL] = level + 1
            }
            val starsMap = decodeIntMap(prefs[Keys.LEVEL_STARS]).toMutableMap()
            val best = maxOf(starsMap[level] ?: 0, stars)
            starsMap[level] = best
            prefs[Keys.LEVEL_STARS] = encodeIntMap(starsMap)
        }
    }

    suspend fun addBooster(type: BoosterType, count: Int = 1) {
        context.gameDataStore.edit { prefs ->
            val boosters = decodeBoosterMap(prefs[Keys.OWNED_BOOSTERS]).toMutableMap()
            boosters[type] = (boosters[type] ?: 0) + count
            prefs[Keys.OWNED_BOOSTERS] = encodeBoosterMap(boosters)
        }
    }

    suspend fun consumeBooster(type: BoosterType): Boolean {
        var success = false
        context.gameDataStore.edit { prefs ->
            val boosters = decodeBoosterMap(prefs[Keys.OWNED_BOOSTERS]).toMutableMap()
            val owned = boosters[type] ?: 0
            if (owned > 0) {
                boosters[type] = owned - 1
                prefs[Keys.OWNED_BOOSTERS] = encodeBoosterMap(boosters)
                success = true
            }
        }
        return success
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(isTr: Boolean) {
        context.gameDataStore.edit { it[Keys.IS_TR] = isTr }
    }

    suspend fun setBlockTheme(theme: String) {
        context.gameDataStore.edit { it[Keys.BLOCK_THEME] = theme }
    }

    // --- Haftalık görevler ---

    suspend fun incrementMissionProgress(type: MissionType, amount: Int) {
        context.gameDataStore.edit { prefs ->
            val weekId = currentWeekId()
            val storedWeekId = prefs[Keys.MISSION_WEEK_ID]
            val missions = WeeklyMissionGenerator.forWeek(weekId)
            val progress = if (storedWeekId == weekId) {
                decodeStringIntMap(prefs[Keys.MISSION_PROGRESS]).toMutableMap()
            } else {
                mutableMapOf()
            }
            missions.filter { it.type == type }.forEach { mission ->
                val current = progress[mission.id] ?: 0
                progress[mission.id] = (current + amount).coerceAtMost(mission.target)
            }
            prefs[Keys.MISSION_WEEK_ID] = weekId
            prefs[Keys.MISSION_PROGRESS] = encodeStringIntMap(progress)
            if (storedWeekId != weekId) {
                prefs[Keys.MISSION_CLAIMED] = ""
            }
        }
    }

    suspend fun claimMission(missionId: String, rewardTokens: Int) {
        context.gameDataStore.edit { prefs ->
            val claimed = decodeStringSet(prefs[Keys.MISSION_CLAIMED]).toMutableSet()
            if (claimed.add(missionId)) {
                prefs[Keys.MISSION_CLAIMED] = claimed.joinToString(",")
                val current = prefs[Keys.TOKENS] ?: 150
                prefs[Keys.TOKENS] = current + rewardTokens
            }
        }
    }

    companion object {
        fun currentWeekId(): String {
            val calendar = Calendar.getInstance()
            val week = calendar.get(Calendar.WEEK_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            return "$year-W$week"
        }

        private fun encodeIntMap(map: Map<Int, Int>): String =
            map.entries.joinToString(",") { "${it.key}:${it.value}" }

        private fun decodeIntMap(raw: String?): Map<Int, Int> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) parts[0].toIntOrNull()?.let { k -> parts[1].toIntOrNull()?.let { v -> k to v } } else null
            }.toMap()
        }

        private fun encodeBoosterMap(map: Map<BoosterType, Int>): String =
            map.entries.joinToString(",") { "${it.key.name}:${it.value}" }

        private fun decodeBoosterMap(raw: String?): Map<BoosterType, Int> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val type = runCatching { BoosterType.valueOf(parts[0]) }.getOrNull()
                    val count = parts[1].toIntOrNull()
                    if (type != null && count != null) type to count else null
                } else null
            }.toMap()
        }

        private fun encodeStringIntMap(map: Map<String, Int>): String =
            map.entries.joinToString(",") { "${it.key}:${it.value}" }

        private fun decodeStringIntMap(raw: String?): Map<String, Int> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) parts[1].toIntOrNull()?.let { v -> parts[0] to v } else null
            }.toMap()
        }

        private fun decodeStringSet(raw: String?): Set<String> {
            if (raw.isNullOrBlank()) return emptySet()
            return raw.split(",").filter { it.isNotBlank() }.toSet()
        }
    }
}
