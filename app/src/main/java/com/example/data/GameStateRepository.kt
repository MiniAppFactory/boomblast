package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        // Faz 35: "is_tr" (Boolean, sadece TR/EN) yerini "language" (String kod,
        // 5 dil) alıyor. Eski key SILINMEDI — asagidaki okuma mantiginda geriye
        // donuk uyumluluk icin kullaniliyor (mevcut cihazdaki TR/EN tercihi kaybolmasin).
        val IS_TR = booleanPreferencesKey("is_tr")
        val LANGUAGE = stringPreferencesKey("language")
        val BLOCK_THEME = stringPreferencesKey("block_theme")
        val UI_SKIN = stringPreferencesKey("ui_skin")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val ENDLESS_HIGH_SCORE = intPreferencesKey("endless_high_score")
        val HAS_ACCEPTED_TERMS = booleanPreferencesKey("has_accepted_terms")
        val HAS_MADE_FIRST_MOVE = booleanPreferencesKey("has_made_first_move")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LEVELS_SINCE_INTERSTITIAL = intPreferencesKey("levels_since_interstitial")

        val MISSION_WEEK_ID = stringPreferencesKey("mission_week_id")
        val MISSION_PROGRESS = stringPreferencesKey("mission_progress")
        val MISSION_CLAIMED = stringPreferencesKey("mission_claimed")

        // Faz 77: Pro Mode (Challenge) — Seviyeli Mod'dan AYRI ilerleme + can sistemi.
        val CHALLENGE_HIGHEST_LEVEL = intPreferencesKey("challenge_highest_unlocked_level")
        val CHALLENGE_LEVEL_STARS = stringPreferencesKey("challenge_level_stars")
        val CHALLENGE_LIVES = intPreferencesKey("challenge_lives")
        val CHALLENGE_LAST_LIFE_TS = longPreferencesKey("challenge_last_life_timestamp")
    }

    val playerProgress: Flow<PlayerProgress> = context.gameDataStore.data.map { prefs ->
        // Faz 77: can sayisi OKUMA aninda turetiliyor (yazma yapmadan, Flow.map
        // saf bir donusum olmali) — gecen sureye gore kac can biriktigi hesaplanip
        // UI'a hep GUNCEL deger gosteriliyor, arka planda calisan bir zamanlayiciya
        // gerek yok. Gercek yazma (dusme/kazanma) sadece consumeChallengeLife/
        // grantChallengeLife SUSPEND fonksiyonlarinda, DataStore.edit icinde olur.
        val (challengeLives, challengeLastLifeTs) = regenChallengeLives(
            storedLives = prefs[Keys.CHALLENGE_LIVES] ?: CHALLENGE_MAX_LIVES,
            lastTimestamp = prefs[Keys.CHALLENGE_LAST_LIFE_TS] ?: 0L,
            now = System.currentTimeMillis()
        )
        PlayerProgress(
            tokens = prefs[Keys.TOKENS] ?: 150,
            highestUnlockedLevel = prefs[Keys.HIGHEST_LEVEL] ?: 1,
            levelStars = decodeIntMap(prefs[Keys.LEVEL_STARS]),
            ownedBoosters = decodeBoosterMap(prefs[Keys.OWNED_BOOSTERS]),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.5f,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            darkMode = prefs[Keys.DARK_MODE] ?: true,
            // Faz 37: gercekten ilk kurulumda (ne yeni "language" ne eski
            // "is_tr" kaydi varsa) cihazin sistem diline gore akilli bir
            // varsayilan seciliyor — onceden hep sabit TR'ye dusuyordu.
            language = when {
                prefs[Keys.LANGUAGE] != null -> AppLanguage.fromCode(prefs[Keys.LANGUAGE])
                prefs[Keys.IS_TR] != null -> if (prefs[Keys.IS_TR] == false) AppLanguage.EN else AppLanguage.TR
                else -> AppLanguage.fromSystemLocale()
            },
            blockTheme = prefs[Keys.BLOCK_THEME] ?: "CLASSIC",
            uiSkin = prefs[Keys.UI_SKIN] ?: "DEFAULT",
            hasSeenOnboarding = prefs[Keys.HAS_SEEN_ONBOARDING] ?: false,
            endlessHighScore = prefs[Keys.ENDLESS_HIGH_SCORE] ?: 0,
            hasAcceptedTerms = prefs[Keys.HAS_ACCEPTED_TERMS] ?: false,
            hasMadeFirstMove = prefs[Keys.HAS_MADE_FIRST_MOVE] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            levelsCompletedSinceInterstitial = prefs[Keys.LEVELS_SINCE_INTERSTITIAL] ?: 0,
            challengeHighestUnlockedLevel = prefs[Keys.CHALLENGE_HIGHEST_LEVEL] ?: 1,
            challengeLevelStars = decodeIntMap(prefs[Keys.CHALLENGE_LEVEL_STARS]),
            challengeLives = challengeLives,
            challengeLastLifeTimestamp = challengeLastLifeTs
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

    suspend fun setSoundVolume(volume: Float) {
        context.gameDataStore.edit { it[Keys.SOUND_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.MUSIC_ENABLED] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.gameDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setBlockTheme(theme: String) {
        context.gameDataStore.edit { it[Keys.BLOCK_THEME] = theme }
    }

    suspend fun setUiSkin(skin: String) {
        context.gameDataStore.edit { it[Keys.UI_SKIN] = skin }
    }

    suspend fun markOnboardingSeen() {
        context.gameDataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }
    }

    suspend fun markTermsAccepted() {
        context.gameDataStore.edit { it[Keys.HAS_ACCEPTED_TERMS] = true }
    }

    suspend fun markFirstMoveMade() {
        context.gameDataStore.edit { it[Keys.HAS_MADE_FIRST_MOVE] = true }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    // Faz 39: her seviye tamamlaninca 1 artar; AppNavigation 2'ye ulasinca
    // interstitial gosterip resetIntervalCounter() ile sifirlar.
    suspend fun incrementLevelsSinceInterstitial() {
        context.gameDataStore.edit { prefs ->
            val current = prefs[Keys.LEVELS_SINCE_INTERSTITIAL] ?: 0
            prefs[Keys.LEVELS_SINCE_INTERSTITIAL] = current + 1
        }
    }

    suspend fun resetLevelsSinceInterstitial() {
        context.gameDataStore.edit { it[Keys.LEVELS_SINCE_INTERSTITIAL] = 0 }
    }

    suspend fun recordEndlessScore(score: Int) {
        context.gameDataStore.edit { prefs ->
            val current = prefs[Keys.ENDLESS_HIGH_SCORE] ?: 0
            if (score > current) {
                prefs[Keys.ENDLESS_HIGH_SCORE] = score
            }
        }
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
                progress[mission.id] = (current + amount).coerceAtMost(mission.tiers.last().target)
            }
            prefs[Keys.MISSION_WEEK_ID] = weekId
            prefs[Keys.MISSION_PROGRESS] = encodeStringIntMap(progress)
            if (storedWeekId != weekId) {
                prefs[Keys.MISSION_CLAIMED] = ""
            }
        }
    }

    // Faz 73: gorevler artik 3 milestone'li — claim artik mission basina degil,
    // "$missionId#$tierIndex" anahtariyla tier basina.
    suspend fun claimMission(missionId: String, tierIndex: Int, rewardTokens: Int) {
        context.gameDataStore.edit { prefs ->
            val claimed = decodeStringSet(prefs[Keys.MISSION_CLAIMED]).toMutableSet()
            if (claimed.add("$missionId#$tierIndex")) {
                prefs[Keys.MISSION_CLAIMED] = claimed.joinToString(",")
                val current = prefs[Keys.TOKENS] ?: 150
                prefs[Keys.TOKENS] = current + rewardTokens
            }
        }
    }

    // --- Pro Mode (Challenge) ---

    suspend fun recordChallengeLevelResult(level: Int, stars: Int) {
        context.gameDataStore.edit { prefs ->
            val currentHighest = prefs[Keys.CHALLENGE_HIGHEST_LEVEL] ?: 1
            if (level >= currentHighest) {
                prefs[Keys.CHALLENGE_HIGHEST_LEVEL] = level + 1
            }
            val starsMap = decodeIntMap(prefs[Keys.CHALLENGE_LEVEL_STARS]).toMutableMap()
            val best = maxOf(starsMap[level] ?: 0, stars)
            starsMap[level] = best
            prefs[Keys.CHALLENGE_LEVEL_STARS] = encodeIntMap(starsMap)
        }
    }

    // Bir bolume BASLARKEN cagrilir (kazanilsin/kaybedilsin fark etmez, mobil
    // oyunlarda standart "attempt basina 1 can" deseni). Guncel (regen dahil)
    // can 0'sa false doner, cagiran taraf oyunu baslatmamali.
    suspend fun consumeChallengeLife(): Boolean {
        var success = false
        context.gameDataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val stored = prefs[Keys.CHALLENGE_LIVES] ?: CHALLENGE_MAX_LIVES
            val ts = prefs[Keys.CHALLENGE_LAST_LIFE_TS] ?: 0L
            val (current, adjustedTs) = regenChallengeLives(stored, ts, now)
            if (current > 0) {
                val wasFull = current >= CHALLENGE_MAX_LIVES
                prefs[Keys.CHALLENGE_LIVES] = current - 1
                // Dolu haldeyken ilk dususse yeni regen sayaci SIMDI baslar —
                // aksi halde eski (bos) zaman damgasindan sayilip oyuncuya
                // haksiz uzun bir bekleme cikar.
                prefs[Keys.CHALLENGE_LAST_LIFE_TS] = if (wasFull) now else adjustedTs
                success = true
            } else {
                prefs[Keys.CHALLENGE_LIVES] = current
                prefs[Keys.CHALLENGE_LAST_LIFE_TS] = adjustedTs
            }
        }
        return success
    }

    // "1 reklam = 1 can" akisi.
    suspend fun grantChallengeLife() {
        context.gameDataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val stored = prefs[Keys.CHALLENGE_LIVES] ?: CHALLENGE_MAX_LIVES
            val ts = prefs[Keys.CHALLENGE_LAST_LIFE_TS] ?: 0L
            val (current, adjustedTs) = regenChallengeLives(stored, ts, now)
            prefs[Keys.CHALLENGE_LIVES] = (current + 1).coerceAtMost(CHALLENGE_MAX_LIVES)
            prefs[Keys.CHALLENGE_LAST_LIFE_TS] = adjustedTs
        }
    }

    companion object {
        const val CHALLENGE_MAX_LIVES = 5
        const val CHALLENGE_LIFE_REFILL_MS = 30 * 60 * 1000L

        // Saf fonksiyon: depolanan can + son zaman damgasi + "simdi"den, GUNCEL
        // can sayisini ve yeni regen baz zamanini turetir. Dolu ise (>=MAX)
        // zaman damgasini "simdi"ye ilerletir (bos regen suresi birikmesin).
        fun regenChallengeLives(storedLives: Int, lastTimestamp: Long, now: Long): Pair<Int, Long> {
            if (storedLives >= CHALLENGE_MAX_LIVES) return storedLives to now
            if (lastTimestamp <= 0L) return storedLives to now
            val elapsed = now - lastTimestamp
            if (elapsed < CHALLENGE_LIFE_REFILL_MS) return storedLives to lastTimestamp
            val gained = (elapsed / CHALLENGE_LIFE_REFILL_MS).toInt()
            val newLives = (storedLives + gained).coerceAtMost(CHALLENGE_MAX_LIVES)
            val newTimestamp = if (newLives >= CHALLENGE_MAX_LIVES) now else lastTimestamp + gained * CHALLENGE_LIFE_REFILL_MS
            return newLives to newTimestamp
        }

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
