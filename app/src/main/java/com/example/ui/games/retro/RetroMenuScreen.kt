package com.example.ui.games.retro

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
import com.example.data.retro.DifficultyPreset
import com.example.data.retro.GameSettings
import com.example.ui.theme.retro.ThemeColorPalette

@Composable
fun RetroMenuScreen(
    settings: GameSettings,
    highestScoreEver: Int,
    palette: ThemeColorPalette,
    onStartGame: () -> Unit,
    onSelectDifficulty: (DifficultyPreset) -> Unit,
    onOpenHighScores: () -> Unit,
    onOpenSettings: () -> Unit
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
                        contentDescription = "High Score",
                        tint = palette.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOP RECORD: $highestScoreEver",
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
                            contentDescription = "Start Game",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START GAME",
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
                    text = "SELECT DIFFICULTY",
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
                                text = preset.name,
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
                    text = "HIGH SCORES & STATS",
                    icon = Icons.Default.EmojiEvents,
                    palette = palette,
                    onClick = onOpenHighScores
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuNavButton(
                    text = "SETTINGS & THEMES",
                    icon = Icons.Default.Settings,
                    palette = palette,
                    onClick = onOpenSettings
                )

                Spacer(modifier = Modifier.height(8.dp))

                MenuNavButton(
                    text = "HOW TO PLAY",
                    icon = Icons.Default.HelpOutline,
                    palette = palette,
                    onClick = { showHowToPlay = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showHowToPlay) {
            HowToPlayDialog(palette = palette, onDismiss = { showHowToPlay = false })
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
                    text = "HOW TO PLAY",
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
                    InstructionItem("🕹️ D-Pad Left/Right", "Move falling block sideways", palette)
                    InstructionItem("⬇️ Soft Drop", "Speed up falling block (+1 pt/cell)", palette)
                    InstructionItem("⚡ Hard Drop", "Instantly drop block to bottom (+2 pt/cell)", palette)
                    InstructionItem("🔄 Rotate A / B", "Rotate piece clockwise / counter-clockwise", palette)
                    InstructionItem("📦 Hold Button", "Swap current piece with held piece", palette)
                    InstructionItem("🧱 Clear Lines", "Fill full rows to clear blocks & score points", palette)
                    InstructionItem("🏆 Back-To-Back", "Perform consecutive Tetris 4-line clears for bonus!", palette)
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
                    Text(text = "GOT IT!", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
