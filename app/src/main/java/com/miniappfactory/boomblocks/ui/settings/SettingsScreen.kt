package com.miniappfactory.boomblocks.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.EffectIntensity
import com.miniappfactory.boomblocks.data.flag
import com.miniappfactory.boomblocks.data.label
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.components.GameDropdownField
import com.miniappfactory.boomblocks.ui.components.GameIconTile
import com.miniappfactory.boomblocks.ui.components.GamePanel
import com.miniappfactory.boomblocks.ui.components.GamePill
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.components.GameScreenHeader
import com.miniappfactory.boomblocks.ui.components.GameScrollHint
import com.miniappfactory.boomblocks.ui.components.GameSectionHeader
import com.miniappfactory.boomblocks.ui.components.GameSegmented
import com.miniappfactory.boomblocks.ui.components.GameSelectableRow
import com.miniappfactory.boomblocks.ui.components.GameSlider
import com.miniappfactory.boomblocks.ui.components.GameToggle
import com.miniappfactory.boomblocks.ui.components.roleTint
import com.miniappfactory.boomblocks.ui.games.boomblocks.BLOCK_THEMES
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces

// Faz 158 — AYARLAR GORSEL YENILEMESI.
//
// Kullanicinin teshisi: "ayarlar menusu gorevler menusu hepsi cok tekduze,
// sanki kurumsal bir sirketin pptx'i gibi", "biz sanki websitesi hissi
// veriyoruz". Kok sebepler ve karsiliklari:
//
//   1. STOK MATERIAL KONTROLLERI (Switch / Slider / chevron'lu dropdown) —
//      bunlar web/form kontrolleri. Yerine `GameToggle`, `GameSlider`,
//      `GameDropdownField`, `GameSegmented` (bkz. ui/components/GameControls.kt).
//   2. TEKDUZE KARTLAR — hepsi ayni yukseklik/dolgu/aralik. Yerine gruplama
//      basliklari (`GameSectionHeader`) + farkli yukseklikte paneller: goz
//      artik bir ritim yakaliyor.
//   3. EMOJI IKONLAR (isletim sistemi emojisi, uslup birligi yok) — yerine
//      gradyan dolgulu `GameIconTile` + Material vektor glifi. Her satirin
//      ikonu kendi rolunun renginde (ses mor, titresim yesil, ...) ama
//      `roleTint` ile skin accent'ine cekiliyor.
//   4. DUZ ZEMIN — yerine `GameScreenBackground`.
//   5. KAYDIRMA GORUNMUYORDU — liste dibinde `GameScrollHint` chevron'u.
//
// DEGISMEYENLER: tum parametreler, geri cagirmalar ve test etiketleri aynen
// korundu; hicbir ayarin davranisi degismedi.
@Composable
fun SettingsScreen(
    soundEnabled: Boolean,
    soundVolume: Float = 0.5f,
    musicEnabled: Boolean,
    // Faz 105: coklu patlama titresimi.
    hapticsEnabled: Boolean = true,
    // Faz 109: patlama efekti yogunlugu (Düşük/Normal/Yüksek).
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
    // Faz 137: tema dukkani.
    tokens: Int = 0,
    unlockedThemes: Set<String> = emptySet(),
    onUnlockTheme: (themeId: String, price: Int) -> Unit = { _, _ -> },
    onSelectLanguage: (AppLanguage) -> Unit,
    onSelectSkin: (BlastSkin) -> Unit = {},
    notificationsEnabled: Boolean = true,
    onToggleNotifications: (Boolean) -> Unit = {},
    onOpenHowToPlay: () -> Unit = {},
    // Faz 108: UMP gizlilik secenekleri girisi.
    showPrivacyOptions: Boolean = false,
    onShowPrivacyOptions: () -> Unit = {},
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val surfaces = rememberGameSurfaces(skin, darkMode)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            modifier = Modifier.matchParentSize()
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            GameScreenHeader(
                title = language.pick(tr = "AYARLAR", en = "SETTINGS", it = "IMPOSTAZIONI", fr = "PARAMÈTRES", es = "AJUSTES"),
                surfaces = surfaces,
                onBack = onBack,
                backDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                backTestTag = "settings_back_button"
            )

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // -------------------------------------------------------
                    // 1. SES VE GERI BILDIRIM
                    // -------------------------------------------------------
                    GameSectionHeader(
                        text = language.pick(
                            tr = "SES VE GERİ BİLDİRİM",
                            en = "SOUND & FEEDBACK",
                            it = "AUDIO E FEEDBACK",
                            fr = "SON ET RETOUR",
                            es = "SONIDO Y RESPUESTA"
                        ),
                        surfaces = surfaces
                    )

                    SettingsToggleRow(
                        iconRes = R.drawable.kb_ic_speaker,
                        tint = roleTint(NeonPurple, surfaces),
                        label = language.pick(tr = "Ses Efektleri", en = "Sound Effects", it = "Effetti Sonori", fr = "Effets Sonores", es = "Efectos de Sonido"),
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        testTag = "settings_sound_switch",
                        surfaces = surfaces
                    )

                    // Faz 161 — CIHAZDA GORULEN: bu kart komsu satirlarin UC
                    // KATI yuksekligindeydi (etiket+kapsul / tam genislik
                    // kaydirici / 0%-50%-100% kademe satiri) ve "50%" AYNI
                    // ANDA iki yerde yaziyordu. Artik tek satir; deger yalnizca
                    // sagdaki kapsulde. Sigmazsa `SettingsSliderRow` kendisi
                    // alt alta duzene duser (bkz. oradaki olcum).
                    // Faz 161: gorunen etiket "Ses Siddeti" -> "Ses".
                    // Olcum (onceki tur, cihazda): satir butcesi 308dp; ES
                    // "Volumen de Sonido" 331dp gerektiriyordu ve TEK dil olarak
                    // alt alta duzene dusuyordu. Kisa etiketle bes dil de tek
                    // satira giriyor. Ekran okuyucuya giden metin KISALMIYOR —
                    // `accessibilityLabel` aciklayici halini tasiyor.
                    SettingsSliderRow(
                        icon = Icons.Default.GraphicEq,
                        tint = roleTint(surfaces.accentPrimary, surfaces),
                        label = language.pick(tr = "Ses", en = "Volume", it = "Volume", fr = "Volume", es = "Volumen"),
                        accessibilityLabel = language.pick(tr = "Ses şiddeti", en = "Sound volume", it = "Volume suoni", fr = "Volume sonore", es = "Volumen de sonido"),
                        value = soundVolume,
                        onValueChange = onSoundVolumeChange,
                        enabled = soundEnabled,
                        surfaces = surfaces,
                        sliderTestTag = "settings_sound_volume_slider"
                    )

                    SettingsToggleRow(
                        iconRes = R.drawable.kb_ic_vibrate,
                        tint = roleTint(NeonGreen, surfaces),
                        label = language.pick(tr = "Titreşim", en = "Vibration", it = "Vibrazione", fr = "Vibration", es = "Vibración"),
                        checked = hapticsEnabled,
                        onCheckedChange = onToggleHaptics,
                        testTag = "settings_haptics_switch",
                        surfaces = surfaces
                    )

                    // -------------------------------------------------------
                    // 2. GORUNUM
                    // -------------------------------------------------------
                    GameSectionHeader(
                        text = language.pick(tr = "GÖRÜNÜM", en = "APPEARANCE", it = "ASPETTO", fr = "APPARENCE", es = "APARIENCIA"),
                        surfaces = surfaces
                    )

                    // Tema (blok gorunusu) secimi — Faz 160: etiket ve kontrol
                    // ARTIK AYNI SATIRDA (bkz. SettingsInlineField).
                    GamePanel(surfaces = surfaces, modifier = Modifier.fillMaxWidth()) {
                        val selectedTheme = BLOCK_THEMES.find { it.id == currentTheme }
                        // Kontrolde gorunecek metin, sigar/sigmaz olcumune de
                        // AYNEN girsin diye tek yerde tutuluyor.
                        val themeValue = selectedTheme?.title(language) ?: "Classic"
                        var themeMenuExpanded by remember { mutableStateOf(false) }
                        SettingsInlineField(
                            icon = Icons.Default.Palette,
                            tint = roleTint(NeonPurple, surfaces),
                            label = language.pick(tr = "Tema", en = "Theme", it = "Tema", fr = "Thème", es = "Tema"),
                            valueTexts = listOf(themeValue),
                            // Emoji leading (20sp ~ 24dp) + compact dolgu/chevron.
                            controlChrome = DROPDOWN_CHROME_EMOJI,
                            surfaces = surfaces
                        ) { controlModifier ->
                            // DIKKAT (Faz 159 dersi): `GameDropdownField` icinde
                            // `fillMaxWidth()` var. Compose'da Row ONCE agirliksiz
                            // cocuklari olcer ve onlara kalan TUM genisligi verir —
                            // Loadout'ta metin sutunu tam bu yuzden 0dp'ye
                            // cokmustu. Genisligi (weight ya da fillMaxWidth)
                            // TASIYAN sey bu `Box`: alani disaridan aliyor,
                            // kontrol de onu dolduruyor. Agirliksiz birakilirsa
                            // ayni hata tekrarlanir.
                            Box(modifier = controlModifier) {
                                GameDropdownField(
                                    label = themeValue,
                                    surfaces = surfaces,
                                    onClick = { themeMenuExpanded = true },
                                    // Yan yana duzende metnin disindaki olu yuk
                                    // 12dp kisaliyor; yukseklik 52dp kaliyor.
                                    compact = true,
                                    modifier = Modifier.testTag("settings_theme_dropdown_trigger"),
                                    leading = {
                                        Text(text = selectedTheme?.icon ?: "❓", fontSize = 20.sp)
                                    }
                                )
                                DropdownMenu(
                                    expanded = themeMenuExpanded,
                                    onDismissRequest = { themeMenuExpanded = false },
                                    modifier = Modifier
                                        .background(surfaces.panel)
                                        // Faz 142/144: 10 tema listesi ekrani
                                        // kaplamasin, son satir yarim gorunsun.
                                        .heightIn(max = 320.dp)
                                ) {
                                BLOCK_THEMES.forEach { theme ->
                                    val isSelected = theme.id == currentTheme
                                    // Faz 137: oyun ici dialogla AYNI kural.
                                    val isLocked = theme.tokenPrice > 0 && theme.id !in unlockedThemes
                                    val canAfford = tokens >= theme.tokenPrice
                                    DropdownMenuItem(
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        text = {
                                            GameSelectableRow(
                                                selected = isSelected,
                                                onClick = {
                                                    if (!isLocked) {
                                                        onSelectTheme(theme.id)
                                                        themeMenuExpanded = false
                                                    }
                                                },
                                                surfaces = surfaces,
                                                accent = NeonPurple,
                                                enabled = !isLocked,
                                                modifier = Modifier.width(300.dp),
                                                leading = { Text(text = theme.icon, fontSize = 18.sp) }
                                            ) {
                                                Text(
                                                    text = theme.title(language),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = when {
                                                        isLocked -> surfaces.hairline
                                                        isSelected -> surfaces.accentText
                                                        else -> rowTextColor(surfaces)
                                                    },
                                                    // Faz 142: tek satir — aciklamalar listeyi uzatiyordu.
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isLocked) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    // Karsilanamayan secenek GIZLENMEZ, sebebiyle
                                                    // gosterilir: fiyat gorunur, karsilanabiliyorsa
                                                    // "AC" yazar, degilse kilit isareti.
                                                    ThemeUnlockPill(
                                                        price = theme.tokenPrice,
                                                        canAfford = canAfford,
                                                        label = language.pick(tr = "AÇ", en = "UNLOCK", it = "SBLOCCA", fr = "OUVRIR", es = "ABRIR"),
                                                        surfaces = surfaces,
                                                        onClick = {
                                                            onUnlockTheme(theme.id, theme.tokenPrice)
                                                            themeMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (!isLocked) {
                                                onSelectTheme(theme.id)
                                                themeMenuExpanded = false
                                            }
                                        },
                                        modifier = Modifier.testTag("settings_theme_${theme.id}")
                                    )
                                }
                                }
                            }
                        }
                    }

                    // Gorunum (skin) secimi — ayni tek-satir duzeni.
                    //
                    // BU SATIR EN DAR OLANI: etiketin en uzun cevirileri burada
                    // ("Appearance" / "Apariencia" / "Apparence"), yani sol sutun
                    // en genis, kontrole kalan pay en dar. ES'de deger
                    // "Predeterminado" TEK KELIME — bolunemiyor. Olcum bunu
                    // gorup gerektiginde alt alta duzene dusuyor.
                    GamePanel(surfaces = surfaces, modifier = Modifier.fillMaxWidth()) {
                        val skinValue = skin.label(language)
                        var skinMenuExpanded by remember { mutableStateOf(false) }
                        SettingsInlineField(
                            iconRes = R.drawable.kb_ic_eye,
                            tint = roleTint(NeonPurple, surfaces),
                            label = language.pick(tr = "Görünüm", en = "Appearance", it = "Aspetto", fr = "Apparence", es = "Apariencia"),
                            valueTexts = listOf(skinValue),
                            controlChrome = DROPDOWN_CHROME_SWATCH,
                            surfaces = surfaces
                        ) { controlModifier ->
                            Box(modifier = controlModifier) {
                            GameDropdownField(
                                label = skinValue,
                                surfaces = surfaces,
                                onClick = { skinMenuExpanded = true },
                                compact = true,
                                modifier = Modifier.testTag("settings_skin_dropdown_trigger"),
                                leading = { SkinSwatch(skin.swatch, 22.dp) }
                            )
                            DropdownMenu(
                                expanded = skinMenuExpanded,
                                onDismissRequest = { skinMenuExpanded = false },
                                modifier = Modifier.background(surfaces.panel)
                            ) {
                                BlastSkin.entries.forEach { candidate ->
                                    val isSelected = candidate == skin
                                    DropdownMenuItem(
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        text = {
                                            GameSelectableRow(
                                                selected = isSelected,
                                                onClick = {
                                                    onSelectSkin(candidate)
                                                    skinMenuExpanded = false
                                                },
                                                surfaces = surfaces,
                                                accent = NeonPurple,
                                                modifier = Modifier.width(240.dp),
                                                leading = { SkinSwatch(candidate.swatch, 20.dp) }
                                            ) {
                                                Text(
                                                    text = candidate.label(language),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isSelected) surfaces.accentText else rowTextColor(surfaces),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
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

                    // Koyu / Acik mod
                    GamePanel(surfaces = surfaces, modifier = Modifier.fillMaxWidth()) {
                        // Etiketler artik lambda ICINDE uretilmiyor: ayni iki
                        // metin hem kontrole hem TASMA OLCUMUNE gidiyor. Secenek,
                        // sira ve geri cagirmalar aynen duruyor; degisen tek sey
                        // metnin tek kaynaktan okunmasi.
                        val darkLabel = language.pick(tr = "Koyu", en = "Dark", it = "Scura", fr = "Sombre", es = "Oscuro")
                        val lightLabel = language.pick(tr = "Açık", en = "Light", it = "Chiara", fr = "Clair", es = "Claro")
                        SettingsInlineField(
                            iconRes = if (darkMode) R.drawable.kb_ic_moon else R.drawable.kb_ic_sun,
                            tint = roleTint(NeonPurple, surfaces),
                            label = language.pick(tr = "Mod", en = "Mode", it = "Modalità", fr = "Mode", es = "Modo"),
                            // IKI segment AYNI ANDA gorunur, ikisinin de sigmasi
                            // gerekir: liste iki elemanli veriliyor ve gereken
                            // genislikler TOPLANIYOR. IT'de hem etiket uzun
                            // ("Modalità") hem de degerler uzun ("Modalità Scura /
                            // Chiara"); 360dp'de toplam sigmiyor ve BU SATIR
                            // kendiliginden alt alta duzene dusuyor.
                            valueTexts = listOf(darkLabel, lightLabel),
                            controlChrome = SEGMENTED_CHROME_TWO,
                            surfaces = surfaces
                        ) { controlModifier ->
                            // TASMA NOTU (bu projede tekrar eden hata kaynagi):
                            // segment genisligi weight(1f), yukseklik SABIT DEGIL
                            // (defaultMinSize alt sinir 48dp) ve metin maxLines = 2.
                            // "Modalità Scura" / "Modo Oscuro" gibi uzun ceviriler
                            // ikinci satira taser, kontrol uzar — kirpilmaz.
                            GameSegmented(
                                options = listOf(true, false),
                                selected = darkMode,
                                onSelect = onToggleDarkMode,
                                optionLabel = { isDark -> if (isDark) darkLabel else lightLabel },
                                optionIcon = { isDark ->
                                    if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode
                                },
                                optionTestTag = { isDark -> "settings_mode_${if (isDark) "dark" else "light"}" },
                                surfaces = surfaces,
                                accent = NeonPurple,
                                // Genisligi TASIYAN modifier — GameSegmented de
                                // icinde fillMaxWidth() kullaniyor, agirliksiz
                                // birakilirsa satiri yutar.
                                modifier = controlModifier
                            )
                        }
                    }

                    // -------------------------------------------------------
                    // 3. GENEL
                    // -------------------------------------------------------
                    GameSectionHeader(
                        text = language.pick(tr = "GENEL", en = "GENERAL", it = "GENERALE", fr = "GÉNÉRAL", es = "GENERAL"),
                        surfaces = surfaces
                    )

                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        tint = roleTint(NeonGold, surfaces),
                        label = language.pick(tr = "Hatırlatma Bildirimleri", en = "Reminder Notifications", it = "Notifiche di Promemoria", fr = "Notifications de Rappel", es = "Notificaciones de Recordatorio"),
                        checked = notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        testTag = "settings_notifications_switch",
                        surfaces = surfaces
                    )

                    // Faz 81: "Nasil Oynanir / Modlar" sayfasi.
                    SettingsNavRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        tint = roleTint(surfaces.accentSecondary, surfaces),
                        label = language.pick(tr = "Nasıl Oynanır / Modlar", en = "How to Play / Modes", it = "Come Giocare / Modalità", fr = "Comment Jouer / Modes", es = "Cómo Jugar / Modos"),
                        onClick = onOpenHowToPlay,
                        testTag = "settings_how_to_play_row",
                        surfaces = surfaces
                    )

                    // Faz 108: UMP "Gizlilik Secenekleri" girisi — yalnizca
                    // UMP gerekli dedigi bolgelerde gorunur.
                    if (showPrivacyOptions) {
                        SettingsNavRow(
                            icon = Icons.Default.Lock,
                            tint = roleTint(NeonGreen, surfaces),
                            label = language.pick(
                                tr = "Gizlilik Seçenekleri",
                                en = "Privacy Options",
                                it = "Opzioni Privacy",
                                fr = "Options de Confidentialité",
                                es = "Opciones de Privacidad"
                            ),
                            onClick = onShowPrivacyOptions,
                            testTag = "settings_privacy_options_row",
                            surfaces = surfaces
                        )
                    }

                    // Dil secimi
                    GamePanel(surfaces = surfaces, modifier = Modifier.fillMaxWidth()) {
                        SettingsFieldLabel(
                            icon = Icons.Default.Language,
                            tint = roleTint(surfaces.accentPrimary, surfaces),
                            text = language.pick(tr = "Dil", en = "Language", it = "Lingua", fr = "Langue", es = "Idioma"),
                            surfaces = surfaces
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var languageMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            GameDropdownField(
                                label = language.label(),
                                surfaces = surfaces,
                                onClick = { languageMenuExpanded = true },
                                modifier = Modifier.testTag("settings_lang_dropdown_trigger"),
                                leading = { Text(text = language.flag(), fontSize = 20.sp) }
                            )
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false },
                                modifier = Modifier.background(surfaces.panel)
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    val isSelected = lang == language
                                    DropdownMenuItem(
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        text = {
                                            // Dil satirlari: secili olan parlayan
                                            // kenarlik + onay ikonu aliyor (renk
                                            // TEK ayrim kanali degil).
                                            GameSelectableRow(
                                                selected = isSelected,
                                                onClick = {
                                                    onSelectLanguage(lang)
                                                    languageMenuExpanded = false
                                                },
                                                surfaces = surfaces,
                                                accent = NeonPurple,
                                                modifier = Modifier.width(240.dp),
                                                leading = { Text(text = lang.flag(), fontSize = 18.sp) }
                                            ) {
                                                Text(
                                                    text = lang.label(),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isSelected) surfaces.accentText else rowTextColor(surfaces),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
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

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Faz 25 dersi: bu ekran kaydirilabilir ama kaydirilabildigi
                // GORUNMUYORDU. Chevron sadece daha fazla icerik varken cikar.
                GameScrollHint(
                    visible = scrollState.canScrollForward,
                    surfaces = surfaces
                )
            }
        }
    }
}

// Satir metni: zeminin parlakligina gore. Sabit beyaz/siyah yazmak acik
// temayi (ya da ileride eklenecek acik bir skini) bozardi.
private fun rowTextColor(surfaces: GameSurfaces): Color =
    if (surfaces.isLightSurface) Color(0xFF12161F) else Color.White

@Composable
private fun SkinSwatch(color: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

// Panel icindeki alan basligi: ikon kutucugu + etiket.
@Composable
private fun SettingsFieldLabel(
    tint: Color,
    text: String,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    // Cizilmis ikon varligi geldiginde vektor glifin YERINE gecer.
    @DrawableRes iconRes: Int? = null,
    maxLines: Int = 2,
    // Kapali bir ayarin etiketi sonuklasabilsin diye disaridan verilebiliyor;
    // varsayilan mevcut davranis.
    color: Color = rowTextColor(surfaces)
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (iconRes != null) {
            GameIconTile(iconRes = iconRes, tint = tint, size = INLINE_ICON_SIZE)
        } else if (icon != null) {
            GameIconTile(icon = icon, tint = tint, size = INLINE_ICON_SIZE)
        }
        Spacer(modifier = Modifier.width(INLINE_ICON_GAP))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Faz 160 — TEK SATIR AYAR ALANI
// ---------------------------------------------------------------------------
//
// Kullanicinin teshisi: Tema / Gorunum / Mod ayarlarinin her biri IKI satir
// kapliyordu (ustte ikon + etiket, ALTINDA kontrol). Ekran sisiyor, goz her
// ayar icin iki kez asagi iniyordu. Ayni ekrandaki "Ses Efektleri" ve
// "Titresim" satirlari zaten "etiket solda, kontrol sagda" duzenindeydi —
// uc ayar da onlara uyunca ekran tek ritim kazaniyor ve ~120dp kaydirma
// eksiliyor (panel basina 40dp: 32dp etiket + 8dp bosluk satiri kalkiyor).
//
// ASIL RISK GENISLIKTIR, bu ekipte tekrar eden hata kaynagi budur. Yan yana
// duzende kontrole satirin yarisindan azi kaliyor ve ceviriler uzun:
// "Fiaba della Principessa" (IT tema), "Predeterminado" (ES gorunum, TEK
// KELIME, bolunemez), "Modalità Chiara" (IT mod).
//
// Bu yuzden esik SABIT BIR EKRAN GENISLIGI DEGIL. Karar her satirda,
// o an gosterilen GERCEK metin `TextMeasurer` ile olculerek veriliyor:
//
//   sol sutun  = ikon(32) + bosluk(10) + olculen etiket genisligi
//   sag sutun  = kontrolun olu yuku (controlChrome) + metne gereken pay
//   yan yana MI? -> sol + 12dp ara + sag <= kullanilabilir genislik
//                   VE sol sutun kullanilabilir genisligin yarisindan az
//
// Metne gereken pay uc kisittan EN BUYUGU:
//   * en uzun KELIMENIN %78'i  -> kelime ortasindan ellipsis'i onler
//   * tam metnin %58'i         -> iki satira bolunme payi (maxLines = 2)
//   * mutlak taban             -> kontrol hicbir zaman ince bir serit olmaz
//
// Sigmiyorsa satir ESKI alt alta duzenine GERI DUSER. Olcum
// `LocalDensity`nin fontScale'ini de kullandigi icin buyuk yazi tipi ayari
// olan cihazlar da kendiliginden alt alta duzene duser — ayrica bir kural
// yazmaya gerek yok.
private val INLINE_ICON_SIZE = 32.dp
private val INLINE_ICON_GAP = 10.dp

// Sol ve sag sutun arasindaki nefes.
private val INLINE_COLUMN_GAP = 12.dp

// Olculen etikete eklenen yuvarlama payi: genislik TAM olculen degere
// esitlenirse 1px'lik yuvarlama farki metni ikinci satira dusurebiliyor.
private val INLINE_LABEL_SLACK = 3.dp

// KONTROLUN OLU YUKU — metnin disinda kalan her sey. Rakamlar
// `GameControls.kt`teki olculerden geliyor, orada bir olcu degisirse
// burasi da guncellenmeli.
//
//   compact GameDropdownField: yatay dolgu 10x2=20, leading bosluk 8,
//   sag bosluk 8 + chevron kutusu 22 = 30.
//   + emoji leading (20sp ~ 24dp)      -> 20+24+8+30 = 82
//   + renk yuvarlagi leading (22dp)    -> 20+22+8+30 = 80
private val DROPDOWN_CHROME_EMOJI = 82.dp
private val DROPDOWN_CHROME_SWATCH = 80.dp

//   GameSegmented (2 secenek): dis dolgu 4x2=8, seceneklerin arasi 4,
//   secenek basina yatay dolgu 8x2=16 + ikon 18 + ikon boslugu 6 = 40.
//   8 + 4 + 2x40 = 92
private val SEGMENTED_CHROME_TWO = 92.dp

// En uzun kelimenin gorunmesi gereken orani. 1.0 yapilirsa cok daha erken
// alt alta duzene duser; 0.78 "Predetermina…" gibi hala TANINABILIR bir
// kisalmaya izin veriyor, "Predet…" gibi okunmaz bir kisalmaya izin vermiyor.
private const val INLINE_WORD_FIT_RATIO = 0.78f

// Tam metnin iki satira bolununce satir basina dusen payi. Tam yari (0.50)
// degil, cunku bolunme kelime sinirindan olur ve satirlar esit cikmaz.
private const val INLINE_WRAP_RATIO = 0.58f

// Kontrol metninin mutlak taban genisligi (tek degerli kontroller icin;
// segmented'te secenek sayisina bolunur).
private val INLINE_MIN_VALUE_TEXT = 76.dp

// Sol sutun satirin yarisini gecemez: gecerse kontrol okunmaz bir seride
// doner, o durumda alt alta duzen her zaman daha iyidir.
private const val INLINE_MAX_LABEL_SHARE = 0.5f

// ---------------------------------------------------------------------------
// Ses siddeti satiri (Faz 161) -- TEK SATIR olculeri
// ---------------------------------------------------------------------------

// KAYDIRICININ ASGARI GENISLIGI. Tutamak 26dp; ray bunun altina inerse tutamak
// rayin buyuk kismini kaplar ve deger secilemez hale gelir. Kullanicinin
// istedigi sey "dar bir bar" degil, "tek satir" -- o yuzden bu sinirin altina
// SIKISTIRMAK yerine alt alta duzene dusuyoruz (bkz. `fitsInline`).
//
// 76dp CIHAZDA OLCULEREK secildi (SM-G950F, 360dp genislik). Ayni olcumde
// satirin toplam butcesi 308dp ve en uzun SIGAN ceviri "Volume Sonore" (FR)
// 150.7dp etiket blogu istiyor; 84dp'de 4.3dp ile sigmiyordu. 76dp'de FR
// yaklasik 82dp, TR yaklasik 98dp ray aliyor.
private val SLIDER_MIN_TRACK = 76.dp

// Etiket sutunu ile kaydirici arasi.
private val SLIDER_COLUMN_GAP = 8.dp

// Kaydirici ile yuzde kapsulu arasi.
private val SLIDER_PILL_GAP = 8.dp

// `GamePill`in metin disi yuku: yatay dolgu 10x2 + 1.5dp kenarlik x2.
// `GameComponents.kt`teki olculerden geliyor; orasi degisirse burasi da.
private val SLIDER_PILL_CHROME = 23.dp

@Composable
private fun SettingsInlineField(
    label: String,
    // Kontrolde AYNI ANDA gorunecek metinler. Acilir listede tek eleman
    // (secili deger), segmented'te her secenek icin bir eleman — cunku
    // segmented'te iki metin ayni anda yer kaplar.
    valueTexts: List<String>,
    controlChrome: Dp,
    tint: Color,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    // Genisligi kontrole DISARIDAN veriyoruz. Cagiran taraf bu modifier'i
    // kontrolun en dis kabugona koymak ZORUNDA: `GameDropdownField` ve
    // `GameSegmented` icinde `fillMaxWidth()` var ve Row agirliksiz cocugu
    // ONCE olcup ona tum genisligi verdigi icin, agirliksiz birakilirsa
    // etiket sutunu 0dp'ye coker (Faz 159'da Loadout'ta yasanan hata).
    control: @Composable (Modifier) -> Unit
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // Olcum, cizimde kullanilan stille AYNI olmali: tema kendi yazi tipini
    // veriyorsa varsayilan fontla olcmek yanlis sonuc verir.
    val baseStyle = LocalTextStyle.current
    val labelStyle = baseStyle.merge(TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black))
    val valueStyle = baseStyle.merge(TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val available = maxWidth

        fun widthOf(text: String, style: TextStyle): Dp = with(density) {
            measurer.measure(
                text = AnnotatedString(text),
                style = style,
                maxLines = 1,
                softWrap = false
            ).size.width.toDp()
        }

        val labelWidth = widthOf(label, labelStyle) + INLINE_LABEL_SLACK
        val labelBlock = INLINE_ICON_SIZE + INLINE_ICON_GAP + labelWidth

        val perItemFloor = INLINE_MIN_VALUE_TEXT / valueTexts.size.coerceAtLeast(1)
        val neededText = valueTexts.fold(0.dp) { running, value ->
            val full = widthOf(value, valueStyle)
            // "Predeterminado" gibi TEK KELIMELIK degerler bolunemez; en uzun
            // kelime hesabi tam olarak bunu yakalar.
            val widestWord = value.split(' ')
                .filter { it.isNotBlank() }
                .fold(0.dp) { widest, word -> maxOf(widest, widthOf(word, valueStyle)) }
            running + maxOf(
                widestWord * INLINE_WORD_FIT_RATIO,
                full * INLINE_WRAP_RATIO,
                perItemFloor
            )
        }

        val fitsSideBySide =
            labelBlock + INLINE_COLUMN_GAP + controlChrome + neededText <= available &&
                labelBlock <= available * INLINE_MAX_LABEL_SHARE

        if (fitsSideBySide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SABIT genislik, agirlik YOK: Row once bunu olcer, kalani
                // agirlikli kontrole gider. Olculen genislik oldugu icin
                // etiket kirpilmaz; `fitsSideBySide` zaten kontrole gereken
                // payin kaldigini garanti ediyor.
                SettingsFieldLabel(
                    icon = icon,
                    iconRes = iconRes,
                    tint = tint,
                    text = label,
                    surfaces = surfaces,
                    modifier = Modifier.width(labelBlock),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(INLINE_COLUMN_GAP))
                control(Modifier.weight(1f))
            }
        } else {
            // GERI DUSUS: cok dar ekran, cok uzun ceviri ya da buyuk yazi tipi
            // ayari. Eski (alt alta) duzen aynen korunuyor.
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsFieldLabel(
                    icon = icon,
                    iconRes = iconRes,
                    tint = tint,
                    text = label,
                    surfaces = surfaces
                )
                Spacer(modifier = Modifier.height(8.dp))
                control(Modifier.fillMaxWidth())
            }
        }
    }
}

// Kaydirici satiri: ikon + etiket + kaydirici + yuzde kapsulu, TEK SATIRDA.
//
// Faz 161 -- CIHAZDA GORULEN. Onceki hali UC KATLIYDI: (1) ikon+etiket+kapsul,
// (2) tam genislik kaydirici, (3) altinda 0%/50%/100% kademe etiketleri.
// Kullanicinin teshisi: "ses siddeti cok genis yer kapliyor, bar daralip tek
// satira sigabilir. 50% yazisi iki yerde gereksiz cunku." Iki sikayet de
// dogruydu: kart komsu satirlarin uc kati yuksekligindeydi ve ayni deger hem
// kapsulde hem ORTADAKI kademe etiketinde yaziyordu.
//
// GENISLIK RISKI -- asil zorluk bu. "Volumen de Sonido" (ES), "Volume Sonore"
// (FR), "Volume Suoni" (IT) tek satirda etiket + kaydirici + kapsulu ayni anda
// tasimak zorunda. Iki kural:
//
//   1. AGIRLIK ETIKETTE DEGIL KAYDIRICIDA. Etiket olculen SABIT genisligini
//      alir, kalan yer kaydiricinin olur. Tersi yapilirsa etiket satiri yer ve
//      kaydiriciya sifir kalir (Faz 159'da Loadout'ta yasanan hata).
//   2. SIKISTIRMA YOK. Etiket + `SLIDER_MIN_TRACK` + kapsul sigmiyorsa 40px'lik
//      kullanilamaz bir kaydirici uretmek yerine alt alta duzene duselim.
@Composable
internal fun SettingsSliderRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    surfaces: GameSurfaces,
    sliderTestTag: String,
    // Gorunen etiket dar satira sigsin diye kisaltilabilir; ekran okuyucunun
    // duyacagi metin bundan BAGIMSIZ ve aciklayici kalir. Verilmezse gorunen
    // etiket kullanilir.
    accessibilityLabel: String = label
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // Olcum cizimle AYNI stille yapilmali; tema kendi yazi tipini veriyorsa
    // varsayilan fontla olcmek yanlis sonuc verir.
    val baseStyle = LocalTextStyle.current
    val labelStyle = baseStyle.merge(TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black))
    val valueStyle = baseStyle.merge(TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black))
    val percent = "${(value * 100).toInt()}%"
    val labelColor = if (enabled) rowTextColor(surfaces) else surfaces.hairline

    GamePanel(
        surfaces = surfaces,
        modifier = Modifier.fillMaxWidth(),
        // Komsu anahtar satirlariyla AYNI dolgu -- ritim buradan geliyor.
        contentPadding = 10.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val available = maxWidth

            fun widthOf(text: String, style: TextStyle): Dp = with(density) {
                measurer.measure(
                    text = AnnotatedString(text),
                    style = style,
                    maxLines = 1,
                    softWrap = false
                ).size.width.toDp()
            }

            val labelBlock =
                INLINE_ICON_SIZE + INLINE_ICON_GAP + widthOf(label, labelStyle) + INLINE_LABEL_SLACK
            // Kapsul HER ZAMAN en genis degere ("100%") gore olculuyor. Aksi
            // halde satir %50'de sigar, kullanici %100'e cekince tasardi -- ve
            // kaydiricinin genisligi surukleme sirasinda oynardi.
            val pillBlock = widthOf("100%", valueStyle) + SLIDER_PILL_CHROME

            val fitsInline =
                labelBlock + SLIDER_COLUMN_GAP + SLIDER_MIN_TRACK +
                    SLIDER_PILL_GAP + pillBlock <= available

            val pill: @Composable () -> Unit = {
                GamePill(
                    surfaces = surfaces,
                    accent = surfaces.accentPrimary,
                    modifier = Modifier.width(pillBlock)
                ) {
                    Text(
                        text = percent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = surfaces.accentText,
                        maxLines = 1
                    )
                }
            }
            val slider: @Composable (Modifier) -> Unit = { sliderModifier ->
                GameSlider(
                    value = value,
                    onValueChange = onValueChange,
                    surfaces = surfaces,
                    enabled = enabled,
                    // Kademe etiketleri KAPALI: deger zaten sagdaki kapsulde
                    // yaziyor, 0%/50%/100% satiri ayni sayiyi ikinci kez
                    // gosteriyordu. Dokunma alani yine 48dp (GameSlider ici).
                    showTicks = false,
                    accessibilityLabel = accessibilityLabel,
                    modifier = sliderModifier.testTag(sliderTestTag)
                )
            }

            if (fitsInline) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SABIT genislik, agirlik YOK. Row once agirliksiz cocuklari
                    // olcer; olculen genislik verildigi icin etiket kirpilmaz ve
                    // `fitsInline` kaydiriciya gereken payin kaldigini garanti
                    // eder.
                    SettingsFieldLabel(
                        icon = icon,
                        tint = tint,
                        text = label,
                        surfaces = surfaces,
                        modifier = Modifier.width(labelBlock),
                        maxLines = 1,
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.width(SLIDER_COLUMN_GAP))
                    slider(Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(SLIDER_PILL_GAP))
                    pill()
                }
            } else {
                // GERI DUSUS: cok uzun ceviri, cok dar ekran ya da buyutulmus
                // yazi tipi. Etiket + kapsul ustte, kaydirici altta -- deger yine
                // TEK yerde yaziyor, kademe etiketleri geri gelmiyor.
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsFieldLabel(
                            icon = icon,
                            tint = tint,
                            text = label,
                            surfaces = surfaces,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            color = labelColor
                        )
                        Spacer(modifier = Modifier.width(SLIDER_PILL_GAP))
                        pill()
                    }
                    slider(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// Anahtar satiri: ikon kutucugu + etiket + OZEL toggle.
@Composable
private fun SettingsToggleRow(
    tint: Color,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    surfaces: GameSurfaces,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null
) {
    GamePanel(
        surfaces = surfaces,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                GameIconTile(iconRes = iconRes, tint = tint, size = 38.dp)
            } else if (icon != null) {
                GameIconTile(icon = icon, tint = tint, size = 38.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // TASMA: "Notificaciones de Recordatorio" (ES) ve "Notifiche di
            // Promemoria" (IT) tek satira sigmiyor — weight(1f) + iki satir
            // ile satir uzuyor, metin kirpilmiyor, toggle yerinde kaliyor.
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = rowTextColor(surfaces),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            GameToggle(
                checked = checked,
                onCheckedChange = onCheckedChange,
                surfaces = surfaces,
                accessibilityLabel = label,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

// Navigasyon satiri: ikon kutucugu + etiket + chevron.
@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit,
    testTag: String,
    surfaces: GameSurfaces
) {
    GamePanel(
        surfaces = surfaces,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickableRow(onClick),
        contentPadding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameIconTile(icon = icon, tint = tint, size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = rowTextColor(surfaces),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = surfaces.accentText,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))

// Kilitli tema kapsulu: fiyat + karsilanabilirlik AYNI kartta.
@Composable
private fun ThemeUnlockPill(
    price: Int,
    canAfford: Boolean,
    label: String,
    surfaces: GameSurfaces,
    onClick: () -> Unit
) {
    GamePill(
        surfaces = surfaces,
        accent = if (canAfford) NeonGreen else surfaces.panelBorder,
        onClick = if (canAfford) onClick else null
    ) {
        if (!canAfford) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = surfaces.hairline,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = if (canAfford) "$label $price" else "$price",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (canAfford) {
                if (surfaces.isLightSurface) lerp(NeonGreen, Color.Black, 0.45f) else NeonGreen
            } else {
                surfaces.hairline
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
