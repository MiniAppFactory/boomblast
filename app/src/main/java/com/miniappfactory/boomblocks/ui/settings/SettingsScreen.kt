package com.miniappfactory.boomblocks.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.flag
import com.miniappfactory.boomblocks.data.label
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette

@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    soundVolume: Float = 0.5f,
    musicEnabled: Boolean,
    darkMode: Boolean,
    language: AppLanguage,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onToggleSound: (Boolean) -> Unit,
    onSoundVolumeChange: (Float) -> Unit = {},
    onToggleMusic: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectSkin: (BlastSkin) -> Unit = {},
    notificationsEnabled: Boolean = true,
    onToggleNotifications: (Boolean) -> Unit = {},
    onOpenHowToPlay: () -> Unit = {},
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    // Faz 25: "Görünüm" (skin galerisi) eklenince toplam icerik yukseklik
    // artti, Column kaydirilamadigi icin alt kisim (skin isimleri, hatta
    // svatch dairelerinin alt yarisi) ekran disinda SESSIZCE kirpiliyordu
    // (kullanici geri bildirimi, ekran goruntusuyle dogrulandi). Artik dikey
    // kaydirma var, hicbir icerik erisilmez kalmiyor.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Faz 30: "AYARLAR" basligi ustunde fazladan bir bosluk hissi vardi
        // (kullanici geri bildirimi, ekran goruntusuyle dogrulandi) — kok neden,
        // Column'un 16dp ust padding'i + Row'un kendi 4dp padding'i + IconButton'un
        // 48dp'lik varsayilan dokunma alaninin GERI dugmesini asagi kaydirmasiydi.
        // Ust bosluk daraltildi, geri dugmesinin dokunma alani da kucultuldu.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).testTag("settings_back_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.textPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = language.pick(tr = "AYARLAR", en = "SETTINGS", it = "IMPOSTAZIONI", fr = "PARAMÈTRES", es = "AJUSTES"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan
            )
        }

        // Faz 30: baslik ile ilk menu arasindaki bosluk fazla genisti, alt
        // kisimdaki "Görünüm" skin galerisi kaydirmadan gorunmuyordu (kullanici
        // istegi) — bosluk azaltildi, asagidaki tum kartlarin ic/dis padding'i
        // de hafifce daraltilip toplamda birkac satirlik yer kazanildi.
        Spacer(modifier = Modifier.height(8.dp))

        SettingsSwitchRow(
            icon = "🔊",
            label = language.pick(tr = "Ses Efektleri", en = "Sound Effects", it = "Effetti Sonori", fr = "Effets Sonores", es = "Efectos de Sonido"),
            checked = soundEnabled,
            onCheckedChange = onToggleSound,
            testTag = "settings_sound_switch",
            palette = palette
        )
        // Faz 27: sesler telefon sesi sonuna kadar acik olsa bile kisik
        // kaliyordu (kullanici geri bildirimi) — sentezlenen seslerin genligi
        // yukseltildi VE oyun ici bir ses siddeti kaydiricisi eklendi, boylece
        // kullanici istedigi kadar yukseltebiliyor.
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.pick(tr = "Ses Şiddeti", en = "Sound Volume", it = "Volume Suoni", fr = "Volume Sonore", es = "Volumen de Sonido"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (soundEnabled) palette.textPrimary else palette.textSecondary
                    )
                    Text(
                        text = "${(soundVolume * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (soundEnabled) NeonCyan else palette.textSecondary
                    )
                }
                Slider(
                    value = soundVolume,
                    onValueChange = onSoundVolumeChange,
                    enabled = soundEnabled,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth().testTag("settings_sound_volume_slider")
                )
            }
        }
        SettingsSwitchRow(
            icon = "🎵",
            label = language.pick(tr = "Müzik", en = "Music", it = "Musica", fr = "Musique", es = "Música"),
            checked = musicEnabled,
            onCheckedChange = onToggleMusic,
            testTag = "settings_music_switch",
            palette = palette
        )
        SettingsSwitchRow(
            icon = "🌙",
            label = language.pick(tr = "Koyu Mod", en = "Dark Mode", it = "Modalità Scura", fr = "Mode Sombre", es = "Modo Oscuro"),
            checked = darkMode,
            onCheckedChange = onToggleDarkMode,
            testTag = "settings_dark_mode_switch",
            palette = palette
        )
        SettingsSwitchRow(
            icon = "🔔",
            label = language.pick(tr = "Hatırlatma Bildirimleri", en = "Reminder Notifications", it = "Notifiche di Promemoria", fr = "Notifications de Rappel", es = "Notificaciones de Recordatorio"),
            checked = notificationsEnabled,
            onCheckedChange = onToggleNotifications,
            testTag = "settings_notifications_switch",
            palette = palette
        )

        // Faz 81: Ayarlar altina "Nasil Oynanir / Modlar" sayfasi (Gorev #21).
        // Toggle degil navigasyon oldugu icin SettingsSwitchRow yerine kendi
        // clickable Card'i (ok/chevron ile) — ayni gorsel dile (palette.card,
        // 12dp rounded) sadik kaliyor.
        SettingsNavRow(
            icon = "❓",
            label = language.pick(tr = "Nasıl Oynanır / Modlar", en = "How to Play / Modes", it = "Come Giocare / Modalità", fr = "Comment Jouer / Modes", es = "Cómo Jugar / Modos"),
            onClick = onOpenHowToPlay,
            testTag = "settings_how_to_play_row",
            palette = palette
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = language.pick(tr = "Dil", en = "Language", it = "Lingua", fr = "Langue", es = "Idioma"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Faz 36b: kaydirilabilir kart listesi (LazyRow) yerine acilir
                // menu (dropdown) — her secenek kendi bayragiyla gosteriliyor
                // (kullanici istegi).
                var languageMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        color = palette.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { languageMenuExpanded = true }
                            .testTag("settings_lang_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = language.flag(), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = language.label(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = palette.textSecondary
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        modifier = Modifier.background(palette.card)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == language
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = lang.flag(), fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = lang.label(),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NeonPurple else palette.textPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectLanguage(lang)
                                    languageMenuExpanded = false
                                },
                                modifier = Modifier.testTag("settings_lang_${lang.code}")
                            )
                        }
                    }
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = language.pick(tr = "Görünüm", en = "Appearance", it = "Aspetto", fr = "Apparence", es = "Apariencia"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(BlastSkin.entries.toList()) { candidate ->
                        SkinOption(
                            skinOption = candidate,
                            label = candidate.label(language),
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
private fun SettingsNavRow(
    icon: String,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    palette: BlastPalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).testTag(testTag),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.textSecondary
            )
        }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
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
