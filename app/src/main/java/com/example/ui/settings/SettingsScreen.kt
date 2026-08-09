package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.blastPalette

@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    darkMode: Boolean,
    isTr: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (Boolean) -> Unit,
    onSelectSkin: (BlastSkin) -> Unit = {},
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTr) "AYARLAR" else "SETTINGS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSwitchRow(
            icon = "🔊",
            label = if (isTr) "Ses Efektleri" else "Sound Effects",
            checked = soundEnabled,
            onCheckedChange = onToggleSound,
            testTag = "settings_sound_switch",
            palette = palette
        )
        SettingsSwitchRow(
            icon = "🎵",
            label = if (isTr) "Müzik" else "Music",
            checked = musicEnabled,
            onCheckedChange = onToggleMusic,
            testTag = "settings_music_switch",
            palette = palette
        )
        SettingsSwitchRow(
            icon = "🌙",
            label = if (isTr) "Koyu Mod" else "Dark Mode",
            checked = darkMode,
            onCheckedChange = onToggleDarkMode,
            testTag = "settings_dark_mode_switch",
            palette = palette
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTr) "Dil" else "Language",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LanguageOption(
                        label = "Türkçe",
                        selected = isTr,
                        onClick = { onSelectLanguage(true) },
                        testTag = "settings_lang_tr",
                        palette = palette
                    )
                    LanguageOption(
                        label = "English",
                        selected = !isTr,
                        onClick = { onSelectLanguage(false) },
                        testTag = "settings_lang_en",
                        palette = palette
                    )
                }
            }
        }

        // Faz 21: gorunum ("skin") galerisi — Block Blast'ta oldugu gibi tum
        // arayuzun zemin/kart rengini birlikte degistiren, birbirinden bagimsiz
        // hazir paletler (kullanici geri bildirimi: "Block Blast'ta skin
        // değiştirebiliyorsun, ayarlardan default'a çevirebiliyorsun").
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isTr) "Görünüm" else "Appearance",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(BlastSkin.entries.toList()) { candidate ->
                        SkinOption(
                            skinOption = candidate,
                            label = if (isTr) candidate.labelTr else candidate.labelEn,
                            selected = candidate == skin,
                            onClick = { onSelectSkin(candidate) },
                            palette = palette
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkinOption(
    skinOption: BlastSkin,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: BlastPalette
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
            .testTag("settings_skin_${skinOption.name}")
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(skinOption.swatch)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) NeonGreen else palette.cardBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) palette.textPrimary else palette.textSecondary,
            maxLines = 1
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: String,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    palette: BlastPalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = NeonGreen),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    palette: BlastPalette
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NeonPurple.copy(alpha = 0.25f) else palette.background
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NeonPurple else palette.cardBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
