package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.games.blockblast.BlockBlastGame
import com.example.ui.theme.BlastTheBlocksTheme
import java.util.Locale

// Faz 1 iskeleti: tek ekran, sabit parametreler, no-op callback'ler.
// Level/güçlendirici/token/persistence Faz 3'te bu Activity'nin yerini alacak navigasyona bağlanacak.
// Dil seçici (kalıcı, kullanıcı değiştirebilir) Faz 3'te eklenecek; şimdilik sistem diline göre otomatik seçiliyor.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val systemIsTr = Locale.getDefault().language.equals("tr", ignoreCase = true)
        setContent {
            BlastTheBlocksTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BlockBlastGame(
                        highScore = 0,
                        currentTheme = "CLASSIC",
                        isTr = systemIsTr,
                        soundEnabled = true,
                        onSelectTheme = {},
                        onBack = {},
                        onGameOver = { _, _ -> }
                    )
                }
            }
        }
    }
}
