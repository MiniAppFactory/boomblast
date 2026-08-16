package com.miniappfactory.boomblocks.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.EffectIntensity
import com.miniappfactory.boomblocks.data.flag
import com.miniappfactory.boomblocks.data.label
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.games.boomblocks.BLOCK_THEMES
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
    // Faz 105: coklu patlama titresimi. Varsayilan degerli — bu ekran oyun ici
    // overlay olarak da cagriliyor, cagiran her yerin guncellenmesi sart olmasin.
    hapticsEnabled: Boolean = true,
    // Faz 109: patlama efekti yogunlugu (Düşük/Normal/Yüksek). hapticsEnabled ile
    // ayni gerekce ile varsayilan degerli — bu ekran oyun ici overlay olarak da
    // cagriliyor; ayrica deger henuz AppNavigation'a baglanmadi (sonraki faz),
    // varsayilan olmadan mevcut cagiranlar derlenmez.
    effectIntensity: EffectIntensity = EffectIntensity.NORMAL,
    darkMode: Boolean,
    language: AppLanguage,
    skin: BlastSkin = BlastSkin.DEFAULT,
    currentTheme: String = "CLASSIC",
    onToggleSound: (Boolean) -> Unit,
    onSoundVolumeChange: (Float) -> Unit = {},
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit = {},
    onSelectEffectIntensity: (EffectIntensity) -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectTheme: (String) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectSkin: (BlastSkin) -> Unit = {},
    notificationsEnabled: Boolean = true,
    onToggleNotifications: (Boolean) -> Unit = {},
    onOpenHowToPlay: () -> Unit = {},
    // Faz 108: UMP gizlilik secenekleri girisi — yalnizca UMP "gerekli" dedigi
    // bolgelerde gosterilir (bkz. AdsConsent.privacyOptionsRequired).
    showPrivacyOptions: Boolean = false,
    onShowPrivacyOptions: () -> Unit = {},
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

        // Faz 110c: Tema Dropdown (blok şekil/görünüş seçimi)
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = language.pick(tr = "Tema", en = "Theme", it = "Tema", fr = "Thème", es = "Tema"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                var themeMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        color = palette.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { themeMenuExpanded = true }
                            .testTag("settings_theme_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val selectedTheme = BLOCK_THEMES.find { it.id == currentTheme }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = selectedTheme?.icon ?: "❓", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = selectedTheme?.title(language) ?: "Classic",
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
                        expanded = themeMenuExpanded,
                        onDismissRequest = { themeMenuExpanded = false },
                        modifier = Modifier.background(palette.card)
                    ) {
                        BLOCK_THEMES.forEach { theme ->
                            val isSelected = theme.id == currentTheme
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = theme.icon, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = theme.title(language),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) NeonPurple else palette.textPrimary
                                            )
                                            Text(
                                                text = theme.description(language),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Light,
                                                color = palette.textSecondary
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectTheme(theme.id)
                                    themeMenuExpanded = false
                                },
                                modifier = Modifier.testTag("settings_theme_${theme.id}")
                            )
                        }
                    }
                }
            }
        }

        // Faz 110c: Görünüm (Skin) Dropdown
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
                var skinMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        color = palette.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { skinMenuExpanded = true }
                            .testTag("settings_skin_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(skin.swatch)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = skin.label(language),
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
                        expanded = skinMenuExpanded,
                        onDismissRequest = { skinMenuExpanded = false },
                        modifier = Modifier.background(palette.card)
                    ) {
                        BlastSkin.entries.forEach { candidate ->
                            val isSelected = candidate == skin
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(candidate.swatch)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = candidate.label(language),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) NeonPurple else palette.textPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectSkin(candidate)
                                    skinMenuExpanded = false
                                },
                                modifier = Modifier.testTag("settings_skin_${candidate.name}")
                            )
                        }
                    }
                }
            }
        }

        // Faz 110c: Dark Mode / Light Mode seçimi (segmented control)
        SettingsSegmentedRow(
            icon = "🌓",
            label = language.pick(tr = "Mod", en = "Mode", it = "Modalità", fr = "Mode", es = "Modo"),
            options = listOf(true, false),
            optionLabel = { isDark ->
                if (isDark) language.pick(tr = "Koyu Mod", en = "Dark Mode", it = "Modalità Scura", fr = "Mode Sombre", es = "Modo Oscuro")
                else language.pick(tr = "Açık Mod", en = "Light Mode", it = "Modalità Chiara", fr = "Mode Clair", es = "Modo Claro")
            },
            selected = darkMode,
            onSelect = onToggleDarkMode,
            optionTestTag = { isDark -> "settings_mode_${if (isDark) "dark" else "light"}" },
            palette = palette
        )

        // Faz 105: coklu patlama titresimi. Ses/muzik anahtarlarinin hemen
        // altinda — ucu de "oyunun sana verdigi geri bildirim" grubunu olusturuyor,
        // Koyu Mod'dan (gorunum) ve Bildirimler'den (sistem) once geliyor.
        SettingsSwitchRow(
            icon = "📳",
            label = language.pick(tr = "Titreşim", en = "Vibration", it = "Vibrazione", fr = "Vibration", es = "Vibración"),
            checked = hapticsEnabled,
            onCheckedChange = onToggleHaptics,
            testTag = "settings_haptics_switch",
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

        // Faz 108: UMP "Gizlilik Seçenekleri" girişi. Google'in AB Kullanici
        // Rizasi Politikasi, form gerektiginde uygulamanin kullaniciya rizayi
        // SONRADAN degistirebilecegi kalici bir giris noktasi sunmasini zorunlu
        // kiliyor — bugune kadar hic yoktu, yani ilk aciliste verilen cevap geri
        // alinamiyordu. Yaptirim Play incelemesinden cok AdMob hesap seviyesinde
        // (ad serving limited) gelir.
        //
        // Satir yalnizca UMP "gerekli" dedigi bolgelerde (fiilen AB/İngiltere)
        // gorunur; AB disindaki oyuncuya anlamsiz bir menu maddesi cikmaz.
        if (showPrivacyOptions) {
            SettingsNavRow(
                icon = "🔐",
                label = language.pick(
                    tr = "Gizlilik Seçenekleri",
                    en = "Privacy Options",
                    it = "Opzioni Privacy",
                    fr = "Options de Confidentialité",
                    es = "Opciones de Privacidad"
                ),
                onClick = onShowPrivacyOptions,
                testTag = "settings_privacy_options_row",
                palette = palette
            )
        }

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

// Faz 109: iki degerli anahtarlarin (SettingsSwitchRow) UC degerli karsiligi.
// Genel (<T>) tutuldu — ileride baska bir kademeli ayar geldiginde kopyalanmasin.
//
// TASMA NOTU: bu projede tekrar eden hata, uzun TR/IT/FR cevirilerin sabit
// yukseklikli/genislikli kontrollere sigmamasi. Bu yuzden burada:
//  - segmentlerin genisligi weight(1f) ile ekrana gore bolunuyor (sabit dp yok),
//  - yukseklik sabit DEGIL: defaultMinSize ile ALT sinir var, ust sinir yok,
//  - metin maxLines = 2 ve ortalanmis, yani buyuk yazi tipi olceginde (fontScale)
//    kirpilmak yerine ikinci satira tasiyor ve kart bir miktar uzuyor.
// "Yüksek" / "Alto" / "Élevé" / "Normale" hepsi 13sp'de tek satira siger;
// dogrulama SettingsScreenEffectIntensityTest icinde 5 dil x 3 secenek uzerinde
// gercek TextLayoutResult olcumuyle (320dp ekran + fontScale 1.3) yapiliyor.
//
// Erisilebilirlik: uc secenekten biri secildigi icin dogru anlambilim
// clickable degil selectable/selectableGroup (TalkBack "radio button,
// selected" diye okur, tek tek dugme gibi degil).
@Composable
private fun <T> SettingsSegmentedRow(
    icon: String,
    label: String,
    options: List<T>,
    optionLabel: (T) -> String,
    selected: T,
    onSelect: (T) -> Unit,
    optionTestTag: (T) -> String,
    palette: BlastPalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    Surface(
                        color = if (isSelected) NeonPurple.copy(alpha = 0.22f) else palette.background,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) NeonPurple else NeonPurple.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelect(option) }
                            )
                            .testTag(optionTestTag(option))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp)
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionLabel(option),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonPurple else palette.textPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
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
