package com.example.data.retro

// AI Studio'nun uretimindeki Room @Entity anotasyonlari kaldirildi — Boom
// Blocks kurali "DataStore/SharedPreferences disinda bagimlilik ekleme"
// (bkz. GameStateRepository.kt yorumu). Ayni alan seti korunuyor, RetroHighScoreRepository
// bunu SharedPreferences uzerinde saklayip Room'un yerini alıyor.
data class HighScore(
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val linesCleared: Int,
    val levelReached: Int,
    val durationSeconds: Int,
    val difficultyMode: String, // "EASY", "NORMAL", "HARD"
    val timestamp: Long = System.currentTimeMillis()
)
