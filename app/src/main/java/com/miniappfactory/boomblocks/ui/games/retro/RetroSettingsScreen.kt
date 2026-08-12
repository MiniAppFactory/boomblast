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
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.data.retro.ControlStyle
import com.miniappfactory.boomblocks.data.retro.GameSettings
import com.miniappfactory.boomblocks.data.retro.RetroThemeStyle
import com.miniappfactory.boomblocks.data.retro.label
import com.miniappfactory.boomblocks.ui.theme.retro.ThemeColorPalette
import kotlin.math.roundToInt

@Composable
fun RetroSettingsScreen(
    settings: GameSettings,
    palette: ThemeColorPalette,
    language: AppLanguage,
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
                        contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                        tint = palette.textColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = language.pick(tr = "AYARLAR & TEMALAR", en = "SETTINGS & THEMES", it = "IMPOSTAZIONI & TEMI", fr = "PARAMÈTRES & THÈMES", es = "AJUSTES Y TEMAS"),
                    color = palette.accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Visual Theme Style Selector
            SettingsSectionTitle(language.pick(tr = "GÖRSEL TEMA", en = "VISUAL THEME PRESET", it = "TEMA VISIVO", fr = "THÈME VISUEL", es = "TEMA VISUAL"), palette)
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
                    val themeTitle = style.label(language)

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
            SettingsSectionTitle(language.pick(tr = "KONTROL ŞEMASI", en = "CONTROL SCHEME", it = "SCHEMA CONTROLLI", fr = "SCHÉMA DE CONTRÔLE", es = "ESQUEMA DE CONTROL"), palette)
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
                    val label = ctrl.label(language)

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
            SettingsSectionTitle(
                language.pick(
                    tr = "BAŞLANGIÇ SEVİYESİ: ${settings.startingLevel}", en = "STARTING LEVEL: ${settings.startingLevel}",
                    it = "LIVELLO INIZIALE: ${settings.startingLevel}", fr = "NIVEAU DE DÉPART : ${settings.startingLevel}", es = "NIVEL INICIAL: ${settings.startingLevel}"
                ),
                palette
            )
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
            SettingsSectionTitle(language.pick(tr = "OYUN TERCİHLERİ", en = "GAMEPLAY PREFERENCES", it = "PREFERENZE DI GIOCO", fr = "PRÉFÉRENCES DE JEU", es = "PREFERENCIAS DE JUEGO"), palette)
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
                    label = language.pick(tr = "RETRO SES EFEKTLERİ", en = "RETRO SOUND EFFECTS", it = "EFFETTI SONORI RETRO", fr = "EFFETS SONORES RÉTRO", es = "EFECTOS DE SONIDO RETRO"),
                    checked = settings.soundEnabled,
                    palette = palette,
                    onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                )

                SettingSwitchRow(
                    label = language.pick(tr = "DOKUNSAL GERİ BİLDİRİM", en = "HAPTIC TOUCH FEEDBACK", it = "FEEDBACK APTICO", fr = "RETOUR HAPTIQUE", es = "RETROALIMENTACIÓN HÁPTICA"),
                    checked = settings.hapticsEnabled,
                    palette = palette,
                    onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) }
                )

                SettingSwitchRow(
                    label = language.pick(tr = "HAYALET PARÇA ÖNİZLEME", en = "GHOST PIECE PREVIEW", it = "ANTEPRIMA PEZZO FANTASMA", fr = "APERÇU PIÈCE FANTÔME", es = "VISTA PREVIA PIEZA FANTASMA"),
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
