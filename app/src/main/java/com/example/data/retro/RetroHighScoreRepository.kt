package com.example.data.retro

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// AI Studio'nun Room tabanli HighScoreDao/HighScoreRepository/AppDatabase'inin
// yerine gecti — ayni public yuzey (topScores/highestScoreEver/totalGamesPlayed/
// totalLinesCleared Flow'lari + saveScore/clearScores), ama SharedPreferences
// uzerine kurulu (Boom Blocks'un "DataStore/SharedPreferences disinda bagimlilik
// ekleme" kurali). En yuksek 20 skor puana gore sirali saklanir (dusuk olanlar
// elenir) — "highestScoreEver" bu yuzden HER ZAMAN dogru kalir (en yuksek asla
// elenmez). Oyun sayisi/toplam satir gibi omur-boyu toplamlar AYRI sayaçlarda
// tutulur, top-20 listesinden bagimsiz olarak HER kayitta artar.
class RetroHighScoreRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("retro_tetris_prefs", Context.MODE_PRIVATE)

    private val _topScores = MutableStateFlow(loadScores())
    val topScores: Flow<List<HighScore>> = _topScores.asStateFlow()

    val highestScoreEver: Flow<Int?> = _topScores.map { it.maxOfOrNull { s -> s.score } }
    val totalGamesPlayed: Flow<Int> = _topScores.map { prefs.getInt(KEY_TOTAL_GAMES, 0) }
    val totalLinesCleared: Flow<Int?> = _topScores.map { prefs.getInt(KEY_TOTAL_LINES, 0) }

    fun getTopScoresByDifficulty(mode: String): Flow<List<HighScore>> =
        topScores.map { list -> list.filter { it.difficultyMode == mode } }

    suspend fun saveScore(score: HighScore): Long {
        val withId = score.copy(id = System.currentTimeMillis())
        val updated = (_topScores.value + withId)
            .sortedByDescending { it.score }
            .take(MAX_STORED_SCORES)
        _topScores.value = updated
        prefs.edit()
            .putString(KEY_SCORES, encodeScores(updated))
            .putInt(KEY_TOTAL_GAMES, prefs.getInt(KEY_TOTAL_GAMES, 0) + 1)
            .putInt(KEY_TOTAL_LINES, prefs.getInt(KEY_TOTAL_LINES, 0) + score.linesCleared)
            .apply()
        return withId.id
    }

    suspend fun clearScores() {
        _topScores.value = emptyList()
        prefs.edit()
            .remove(KEY_SCORES)
            .remove(KEY_TOTAL_GAMES)
            .remove(KEY_TOTAL_LINES)
            .apply()
    }

    private fun loadScores(): List<HighScore> = decodeScores(prefs.getString(KEY_SCORES, null))

    companion object {
        private const val MAX_STORED_SCORES = 20
        private const val KEY_SCORES = "high_scores"
        private const val KEY_TOTAL_GAMES = "total_games_played"
        private const val KEY_TOTAL_LINES = "total_lines_cleared"
        private const val FIELD_SEP = "|"
        private const val ENTRY_SEP = "\n"

        private fun encodeScores(scores: List<HighScore>): String =
            scores.joinToString(ENTRY_SEP) { s ->
                listOf(
                    s.id,
                    s.playerName.replace(FIELD_SEP, "").replace(ENTRY_SEP, ""),
                    s.score,
                    s.linesCleared,
                    s.levelReached,
                    s.durationSeconds,
                    s.difficultyMode,
                    s.timestamp
                ).joinToString(FIELD_SEP)
            }

        private fun decodeScores(raw: String?): List<HighScore> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(ENTRY_SEP).mapNotNull { line ->
                val parts = line.split(FIELD_SEP)
                if (parts.size != 8) return@mapNotNull null
                runCatching {
                    HighScore(
                        id = parts[0].toLong(),
                        playerName = parts[1],
                        score = parts[2].toInt(),
                        linesCleared = parts[3].toInt(),
                        levelReached = parts[4].toInt(),
                        durationSeconds = parts[5].toInt(),
                        difficultyMode = parts[6],
                        timestamp = parts[7].toLong()
                    )
                }.getOrNull()
            }
        }
    }
}
