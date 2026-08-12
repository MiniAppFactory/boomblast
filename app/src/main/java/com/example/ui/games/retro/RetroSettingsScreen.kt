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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.retro.ControlStyle
import com.example.data.retro.GameSettings
import com.example.data.retro.RetroThemeStyle
import com.example.ui.theme.retro.ThemeColorPalette
import kotlin.math.roundToInt

@Composable
fun RetroSettingsScreen(
    settings: GameSettings,
    palette: ThemeColorPalette,
    onUpdateSettings: (GameSettings) -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .background(palette.containerBackground, RoundedCornerShape(10.dp))
                        .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "SETTINGS & THEMES",
                    color = palette.accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Visual Theme Style Selector
            SettingsSectionTitle("VISUAL THEME PRESET", palette)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RetroThemeStyle.entries.forEach { style ->
                    val isSelected = settings.theme == style
                    val themeTitle = when (style) {
                        RetroThemeStyle.NEON_CYBERPUNK -> "Neon Cyberpunk (Default)"
                        RetroThemeStyle.GAME_BOY_LCD -> "1989 Game Boy Green LCD"
                        RetroThemeStyle.ARCADE_CRT -> "Retro Arcade CRT"
                        RetroThemeStyle.MONOCHROME_8BIT -> "8-Bit Monochrome Matrix"
                        RetroThemeStyle.NES_SYNTH -> "80s Synthwave Sunset"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) palette.accentColor else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onUpdateSettings(settings.copy(theme = style)) }
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = themeTitle,
                            color = if (isSelected) Color.White else palette.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Control Scheme Selection
            SettingsSectionTitle("CONTROL SCHEME", palette)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ControlStyle.entries.forEach { ctrl ->
                    val isSelected = settings.controlStyle == ctrl
                    val label = when (ctrl) {
                        ControlStyle.DPAD_BUTTONS -> "D-PAD"
                        ControlStyle.TOUCH_GESTURES -> "GESTURES"
                        ControlStyle.HYBRID -> "HYBRID"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) palette.accentColor else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onUpdateSettings(settings.copy(controlStyle = ctrl)) }
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else palette.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Starting Level Slider
            SettingsSectionTitle("STARTING LEVEL: ${settings.startingLevel}", palette)
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Slider(
                    value = settings.startingLevel.toFloat(),
                    onValueChange = { onUpdateSettings(settings.copy(startingLevel = it.roundToInt())) },
                    valueRange = 1f..15f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.accentColor,
                        activeTrackColor = palette.accentColor,
                        inactiveTrackColor = palette.bezelBorderColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Audio, Haptics, Ghost Piece Switches
            SettingsSectionTitle("GAMEPLAY PREFERENCES", palette)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingSwitchRow(
                    label = "RETRO SOUND EFFECTS",
                    checked = settings.soundEnabled,
                    palette = palette,
                    onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                )

                SettingSwitchRow(
                    label = "HAPTIC TOUCH FEEDBACK",
                    checked = settings.hapticsEnabled,
                    palette = palette,
                    onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) }
                )

                SettingSwitchRow(
                    label = "GHOST PIECE PREVIEW",
                    checked = settings.ghostPieceEnabled,
                    palette = palette,
                    onCheckedChange = { onUpdateSettings(settings.copy(ghostPieceEnabled = it)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, palette: ThemeColorPalette) {
    Text(
        text = title,
        color = palette.textColor.copy(alpha = 0.7f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
    )
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    palette: ThemeColorPalette,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = palette.textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = palette.accentColor,
                uncheckedThumbColor = palette.textColor.copy(alpha = 0.5f),
                uncheckedTrackColor = palette.background
            )
        )
    }
}
