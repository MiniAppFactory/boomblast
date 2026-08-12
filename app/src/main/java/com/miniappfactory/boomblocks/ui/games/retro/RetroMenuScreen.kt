package com.miniappfactory.boomblocks.ui.games.retro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.data.retro.DifficultyPreset
import com.miniappfactory.boomblocks.data.retro.GameSettings
import com.miniappfactory.boomblocks.data.retro.label
import com.miniappfactory.boomblocks.ui.theme.retro.ThemeColorPalette

@Composable
fun RetroMenuScreen(
    settings: GameSettings,
    highestScoreEver: Int,
    palette: ThemeColorPalette,
    language: AppLanguage,
    onStartGame: () -> Unit,
    onSelectDifficulty: (DifficultyPreset) -> Unit,
    onOpenHighScores: () -> Unit,
    onOpenSettings: () -> Unit,
    // Faz 79: bu ekranin hic geri/cikis yolu yoktu (baslik banner'inda geri
    // oku yok, sistem geri tusu disinda Boom Blocks ana menusune donmenin
    // bir yolu yoktu) — kullanici "quit to main koyman lazim" dedi.
    onQuitToMain: () -> Unit
) {
    var showHowToPlay by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title Banner
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(palette.containerBackground, RoundedCornerShape(12.dp))
                        .border(3.dp, palette.accentColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "RETRO",
                            color = palette.textColor.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "TETRIS",
                            color = palette.accentColor,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Highest Score Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(palette.gridBackground, RoundedCornerShape(20.dp))
                        .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = language.pick(tr = "En Yüksek Skor", en = "High Score", it = "Punteggio Massimo", fr = "Meilleur Score", es = "Puntuación Máxima"),
                        tint = palette.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = language.pick(
                            tr = "EN İYİ REKOR: $highestScoreEver", en = "TOP RECORD: $highestScoreEver",
                            it = "RECORD: $highestScoreEver", fr = "MEILLEUR SCORE : $highestScoreEver", es = "RÉCORD: $highestScoreEver"
                        ),
                        color = palette.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Play & Difficulty Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large START GAME Button
                Button(
                    onClick = onStartGame,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = language.pick(tr = "Oyunu Başlat", en = "Start Game", it = "Inizia Partita", fr = "Démarrer", es = "Iniciar Juego"),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language.pick(tr = "OYUNU BAŞLAT", en = "START GAME", it = "INIZIA PARTITA", fr = "DÉMARRER", es = "INICIAR JUEGO"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Difficulty Mode Selector Label
                Text(
                    text = language.pick(tr = "ZORLUK SEÇ", en = "SELECT DIFFICULTY", it = "SELEZIONA DIFFICOLTÀ", fr = "CHOISIR DIFFICULTÉ", es = "SELECCIONAR DIFICULTAD"),
                    color = palette.textColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Difficulty Preset Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.containerBackground, RoundedCornerShape(10.dp))
                        .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DifficultyPreset.entries.forEach { preset ->
                        val isSelected = settings.difficultyPreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) palette.accentColor else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = { onSelectDifficulty(preset) }
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label(language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else palette.textColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Nav Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                MenuNavButton(
                    text = language.pick(tr = "YÜKSEK SKORLAR & İSTATİSTİK", en = "HIGH SCORES & STATS", it = "PUNTEGGI & STATISTICHE", fr = "SCORES & STATS", es = "PUNTUACIONES Y ESTADÍSTICAS"),
                    icon = Icons.Default.EmojiEvents,
                    palette = palette,
                    onClick = onOpenHighScores
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuNavButton(
                    text = language.pick(tr = "AYARLAR & TEMALAR", en = "SETTINGS & THEMES", it = "IMPOSTAZIONI & TEMI", fr = "PARAMÈTRES & THÈMES", es = "AJUSTES Y TEMAS"),
                    icon = Icons.Default.Settings,
                    palette = palette,
                    onClick = onOpenSettings
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuNavButton(
                    text = language.pick(tr = "NASIL OYNANIR", en = "HOW TO PLAY", it = "COME GIOCARE", fr = "COMMENT JOUER", es = "CÓMO JUGAR"),
                    icon = Icons.Default.HelpOutline,
                    palette = palette,
                    onClick = { showHowToPlay = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuNavButton(
                    text = language.pick(tr = "ANA MENÜYE DÖN", en = "QUIT TO MAIN", it = "TORNA AL MENU", fr = "RETOUR AU MENU", es = "VOLVER AL MENÚ"),
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    palette = palette,
                    onClick = onQuitToMain
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showHowToPlay) {
            HowToPlayDialog(palette = palette, language = language, onDismiss = { showHowToPlay = false })
        }
    }
}

@Composable
private fun MenuNavButton(
    text: String,
    icon: ImageVector,
    palette: ThemeColorPalette,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.containerBackground)
            .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = palette.buttonContentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = palette.buttonContentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HowToPlayDialog(
    palette: ThemeColorPalette,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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
                    text = language.pick(tr = "NASIL OYNANIR", en = "HOW TO PLAY", it = "COME GIOCARE", fr = "COMMENT JOUER", es = "CÓMO JUGAR"),
                    color = palette.textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.gridBackground, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    InstructionItem(
                        "🕹️ " + language.pick(tr = "Yön Tuşu Sol/Sağ", en = "D-Pad Left/Right", it = "D-Pad Sinistra/Destra", fr = "D-Pad Gauche/Droite", es = "D-Pad Izquierda/Derecha"),
                        language.pick(tr = "Düşen parçayı yana kaydır", en = "Move falling block sideways", it = "Sposta il blocco lateralmente", fr = "Déplace le bloc sur le côté", es = "Mueve el bloque hacia los lados"),
                        palette
                    )
                    InstructionItem(
                        "⬇️ " + language.pick(tr = "Yumuşak Düşüş", en = "Soft Drop", it = "Discesa Lenta", fr = "Chute Douce", es = "Caída Suave"),
                        language.pick(tr = "Düşen parçayı hızlandır (+1 puan/hücre)", en = "Speed up falling block (+1 pt/cell)", it = "Velocizza la caduta (+1 pt/cella)", fr = "Accélère la chute (+1 pt/case)", es = "Acelera la caída (+1 pt/celda)"),
                        palette
                    )
                    InstructionItem(
                        "⚡ " + language.pick(tr = "Sert Düşüş", en = "Hard Drop", it = "Discesa Rapida", fr = "Chute Rapide", es = "Caída Rápida"),
                        language.pick(tr = "Parçayı anında en alta düşür (+2 puan/hücre)", en = "Instantly drop block to bottom (+2 pt/cell)", it = "Fa cadere subito il blocco (+2 pt/cella)", fr = "Fait tomber le bloc instantanément (+2 pt/case)", es = "Hace caer el bloque al instante (+2 pt/celda)"),
                        palette
                    )
                    InstructionItem(
                        "🔄 " + language.pick(tr = "Döndür A / B", en = "Rotate A / B", it = "Ruota A / B", fr = "Pivoter A / B", es = "Girar A / B"),
                        language.pick(tr = "Parçayı saat yönünde / tersine döndür", en = "Rotate piece clockwise / counter-clockwise", it = "Ruota in senso orario / antiorario", fr = "Pivote dans le sens horaire / antihoraire", es = "Gira en sentido horario / antihorario"),
                        palette
                    )
                    InstructionItem(
                        "📦 " + language.pick(tr = "Tut Düğmesi", en = "Hold Button", it = "Pulsante Tieni", fr = "Bouton Garder", es = "Botón Guardar"),
                        language.pick(tr = "Mevcut parçayı tutulan parçayla değiştir", en = "Swap current piece with held piece", it = "Scambia il pezzo con quello tenuto", fr = "Échange la pièce avec celle gardée", es = "Cambia la pieza actual con la guardada"),
                        palette
                    )
                    InstructionItem(
                        "🧱 " + language.pick(tr = "Satır Temizle", en = "Clear Lines", it = "Elimina Righe", fr = "Effacer Lignes", es = "Eliminar Líneas"),
                        language.pick(tr = "Satırları tam doldurup blokları patlat, puan kazan", en = "Fill full rows to clear blocks & score points", it = "Riempi le righe per eliminare blocchi e punti", fr = "Remplis les lignes pour effacer et marquer", es = "Llena filas para eliminar bloques y ganar puntos"),
                        palette
                    )
                    InstructionItem(
                        "🏆 " + language.pick(tr = "Ardışık Bonus", en = "Back-To-Back", it = "Back-To-Back", fr = "Back-To-Back", es = "Back-To-Back"),
                        language.pick(tr = "Art arda 4'lü satır temizleyerek bonus kazan!", en = "Perform consecutive Tetris 4-line clears for bonus!", it = "Fai Tetris consecutivi (4 righe) per un bonus!", fr = "Enchaîne des Tetris (4 lignes) pour un bonus !", es = "¡Encadena Tetris (4 líneas) para un bono!"),
                        palette
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = language.pick(tr = "ANLADIM!", en = "GOT IT!", it = "OK!", fr = "COMPRIS !", es = "¡ENTENDIDO!"),
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun InstructionItem(
    title: String,
    description: String,
    palette: ThemeColorPalette
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            color = palette.accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = description,
            color = palette.textColor.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
