package com.example.ui.games.retro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppLanguage
import com.example.data.pick
import com.example.data.retro.GameSettings
import com.example.game.retro.GameStatus
import com.example.game.retro.TetrisGameEngine
import com.example.ui.theme.retro.ThemeColorPalette

@Composable
fun RetroGameScreen(
    gameEngine: TetrisGameEngine,
    settings: GameSettings,
    palette: ThemeColorPalette,
    highestScoreEver: Int,
    language: AppLanguage,
    onSaveHighScore: (String) -> Unit,
    onRestartGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onReturnToMenu: () -> Unit,
    // Faz 80 bug fix: gameEngine.resumeGame() dogrudan cagirmak sadece motorun
    // durumunu PLAYING'e ceviriyor ama viewModel'in dropJob/timerJob
    // coroutine'lerini yeniden BASLATMIYOR (bunlar pause'da status PAUSED
    // olunca dogal olarak sona eriyor) — sonuc: resume sonrasi parcalar
    // otomatik dusmeyi birakiyor. onResume artik RetroViewModel.resumeGame()'e
    // baglaniyor (o hem gameEngine.resumeGame() hem startGameLoops() cagiriyor).
    onResume: () -> Unit
) {
    val gameState by gameEngine.gameState.collectAsStateWithLifecycle()

    var showPauseModal by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        gameEngine.pauseGame()
                        showPauseModal = true
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(palette.buttonBackground, CircleShape)
                        .border(1.dp, palette.bezelBorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = language.pick(tr = "Duraklat", en = "Pause", it = "Pausa", fr = "Pause", es = "Pausa"),
                        tint = palette.buttonContentColor
                    )
                }

                // Stats Header
                HudItem(label = language.pick(tr = "SKOR", en = "SCORE", it = "PUNTI", fr = "SCORE", es = "PUNTOS"), value = "${gameState.score}", palette = palette)
                HudItem(label = language.pick(tr = "SATIR", en = "LINES", it = "RIGHE", fr = "LIGNES", es = "LÍNEAS"), value = "${gameState.lines}", palette = palette)
                HudItem(label = language.pick(tr = "SEVİYE", en = "LEVEL", it = "LIVELLO", fr = "NIVEAU", es = "NIVEL"), value = "${gameState.level}", palette = palette)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Middle Section: Left Side Stats, Center Tetris Grid, Right Next Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Panel: Hold Box & Level Stats
                Column(
                    modifier = Modifier.width(76.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    PiecePreviewBox(
                        title = language.pick(tr = "TUT", en = "HOLD", it = "TIENI", fr = "GARDER", es = "GUARDAR"),
                        pieceType = gameState.holdPiece,
                        palette = palette,
                        boxSize = 68.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Line Clear Notification Banner
                    gameState.lastLineClearEvent?.let { event ->
                        if (event.isTetris || event.isBackToBack || event.comboCount > 1) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(palette.accentColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .border(1.dp, palette.accentColor, RoundedCornerShape(6.dp))
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (event.isTetris) {
                                    Text(
                                        text = "TETRIS!",
                                        color = palette.accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (event.isBackToBack) {
                                    Text(
                                        text = "B2B!",
                                        color = palette.textColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (event.comboCount > 1) {
                                    Text(
                                        text = language.pick(
                                            tr = "${event.comboCount}x KOMBO", en = "${event.comboCount}x COMBO",
                                            it = "${event.comboCount}x COMBO", fr = "${event.comboCount}x COMBO", es = "${event.comboCount}x COMBO"
                                        ),
                                        color = palette.textColor,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Center Tetris Board
                TetrisBoard(
                    gameState = gameState,
                    palette = palette,
                    ghostEnabled = settings.ghostPieceEnabled,
                    controlStyle = settings.controlStyle,
                    onMoveLeft = { gameEngine.moveLeft() },
                    onMoveRight = { gameEngine.moveRight() },
                    onRotate = { gameEngine.rotateClockwise() },
                    onSoftDrop = { gameEngine.softDrop() },
                    onHardDrop = { gameEngine.hardDrop() },
                    modifier = Modifier.weight(1f)
                )

                // Right Panel: Next Piece Queue
                Column(
                    modifier = Modifier.width(76.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val next1 = gameState.nextPieces.getOrNull(0)
                    PiecePreviewBox(
                        title = language.pick(tr = "SIRADAKİ", en = "NEXT", it = "PROSSIMO", fr = "SUIVANT", es = "SIGUIENTE"),
                        pieceType = next1,
                        palette = palette,
                        boxSize = 68.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val next2 = gameState.nextPieces.getOrNull(1)
                    if (next2 != null) {
                        PiecePreviewBox(
                            title = "",
                            pieceType = next2,
                            palette = palette,
                            boxSize = 56.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Arcade Controls
            ArcadeControlPanel(
                palette = palette,
                language = language,
                onMoveLeft = { gameEngine.moveLeft() },
                onMoveRight = { gameEngine.moveRight() },
                onRotateClockwise = { gameEngine.rotateClockwise() },
                onRotateCounterClockwise = { gameEngine.rotateCounterClockwise() },
                onSoftDrop = { gameEngine.softDrop() },
                onHardDrop = { gameEngine.hardDrop() },
                onHold = { gameEngine.holdPiece() },
                canHold = gameState.canHold
            )
        }

        // Pause Modal
        if (showPauseModal || gameState.status == GameStatus.PAUSED) {
            PauseDialog(
                palette = palette,
                language = language,
                onResume = {
                    showPauseModal = false
                    onResume()
                },
                onRestart = {
                    showPauseModal = false
                    onRestartGame()
                },
                onOpenSettings = {
                    showPauseModal = false
                    onOpenSettings()
                },
                onReturnToMenu = {
                    showPauseModal = false
                    onReturnToMenu()
                }
            )
        }

        // Game Over Dialog
        if (gameState.status == GameStatus.GAME_OVER) {
            GameOverDialog(
                gameState = gameState,
                defaultPlayerName = settings.lastPlayerName,
                palette = palette,
                language = language,
                onSaveScore = { playerName ->
                    onSaveHighScore(playerName)
                },
                onPlayAgain = {
                    onRestartGame()
                },
                onReturnToMenu = {
                    onReturnToMenu()
                }
            )
        }
    }
}

@Composable
private fun HudItem(
    label: String,
    value: String,
    palette: ThemeColorPalette
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = palette.textColor.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = palette.textColor,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
