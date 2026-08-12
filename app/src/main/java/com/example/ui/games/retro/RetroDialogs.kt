package com.example.ui.games.retro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppLanguage
import com.example.data.pick
import com.example.game.retro.GameState
import com.example.ui.theme.retro.ThemeColorPalette

@Composable
fun GameOverDialog(
    gameState: GameState,
    defaultPlayerName: String,
    palette: ThemeColorPalette,
    language: AppLanguage,
    onSaveScore: (playerName: String) -> Unit,
    onPlayAgain: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    var playerName by remember { mutableStateOf(defaultPlayerName) }

    Dialog(onDismissRequest = { }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.containerBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, palette.accentColor, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = language.pick(tr = "OYUN BİTTİ", en = "GAME OVER", it = "PARTITA FINITA", fr = "PARTIE TERMINÉE", es = "FIN DEL JUEGO"),
                    color = palette.accentColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                if (gameState.isNewHighScore) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = language.pick(tr = "🏆 YENİ REKOR! 🏆", en = "🏆 NEW HIGH SCORE! 🏆", it = "🏆 NUOVO RECORD! 🏆", fr = "🏆 NOUVEAU RECORD ! 🏆", es = "🏆 ¡NUEVO RÉCORD! 🏆"),
                        color = palette.textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Stats Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.gridBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    StatRow(language.pick(tr = "SONUÇ SKOR", en = "FINAL SCORE", it = "PUNTEGGIO FINALE", fr = "SCORE FINAL", es = "PUNTUACIÓN FINAL"), "${gameState.score}", palette)
                    StatRow(language.pick(tr = "SİLİNEN SATIR", en = "LINES CLEARED", it = "RIGHE ELIMINATE", fr = "LIGNES EFFACÉES", es = "LÍNEAS ELIMINADAS"), "${gameState.lines}", palette)
                    StatRow(language.pick(tr = "MAKS SEVİYE", en = "MAX LEVEL", it = "LIVELLO MAX", fr = "NIVEAU MAX", es = "NIVEL MÁX"), "${gameState.level}", palette)
                    StatRow(language.pick(tr = "SÜRE", en = "DURATION", it = "DURATA", fr = "DURÉE", es = "DURACIÓN"), formatDuration(gameState.gameDurationSeconds), palette)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name Input for High Score
                Text(
                    text = language.pick(
                        tr = "OYUNCU ADI/RUMUZU GİR:", en = "ENTER PLAYER INITIALS / NAME:",
                        it = "INSERISCI NOME GIOCATORE:", fr = "ENTREZ VOTRE NOM :", es = "INTRODUCE TU NOMBRE:"
                    ),
                    color = palette.textColor.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { if (it.length <= 12) playerName = it.uppercase() },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.textColor,
                        unfocusedTextColor = palette.textColor,
                        focusedBorderColor = palette.accentColor,
                        unfocusedBorderColor = palette.bezelBorderColor,
                        focusedContainerColor = palette.background,
                        unfocusedContainerColor = palette.background
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Button(
                    onClick = {
                        onSaveScore(playerName.ifBlank { "PLAYER 1" })
                        onPlayAgain()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = language.pick(tr = "KAYDET & TEKRAR OYNA", en = "SAVE & PLAY AGAIN", it = "SALVA & GIOCA ANCORA", fr = "SAUVER & REJOUER", es = "GUARDAR Y JUGAR DE NUEVO"),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSaveScore(playerName.ifBlank { "PLAYER 1" })
                        onReturnToMenu()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.buttonBackground,
                        contentColor = palette.buttonContentColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = language.pick(tr = "ANA MENÜ", en = "MAIN MENU", it = "MENU PRINCIPALE", fr = "MENU PRINCIPAL", es = "MENÚ PRINCIPAL"),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    palette: ThemeColorPalette,
    language: AppLanguage,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onOpenSettings: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.containerBackground,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, palette.bezelBorderColor, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = language.pick(tr = "DURAKLATILDI", en = "PAUSED", it = "IN PAUSA", fr = "EN PAUSE", es = "PAUSADO"),
                    color = palette.textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                DialogActionButton(language.pick(tr = "OYUNA DEVAM ET", en = "RESUME GAME", it = "RIPRENDI PARTITA", fr = "REPRENDRE", es = "REANUDAR JUEGO"), palette.accentColor, Color.White, onResume)
                Spacer(modifier = Modifier.height(10.dp))
                DialogActionButton(language.pick(tr = "YENİDEN BAŞLAT", en = "RESTART", it = "RICOMINCIA", fr = "RECOMMENCER", es = "REINICIAR"), palette.buttonBackground, palette.buttonContentColor, onRestart)
                Spacer(modifier = Modifier.height(10.dp))
                DialogActionButton(language.pick(tr = "AYARLAR", en = "SETTINGS", it = "IMPOSTAZIONI", fr = "PARAMÈTRES", es = "AJUSTES"), palette.buttonBackground, palette.buttonContentColor, onOpenSettings)
                Spacer(modifier = Modifier.height(10.dp))
                DialogActionButton(language.pick(tr = "MENÜYE DÖN", en = "QUIT TO MENU", it = "TORNA AL MENU", fr = "RETOUR AU MENU", es = "VOLVER AL MENÚ"), palette.buttonBackground, palette.textColor.copy(alpha = 0.7f), onReturnToMenu)
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    palette: ThemeColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = palette.textColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = palette.textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
