package com.example.ui.modeselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.pick
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.blastPalette

// Oyunun asil giris ekrani: kullanici once "Sonsuz Mod" mu "Seviyeli Mod" mu
// oynayacagina karar veriyor. Onceden Sonsuz Mod, seviye listesinin icine
// sikistirilmis bir kart olarak gosteriliyordu — bu iki esdeger oyun modunu
// birbirinden ayirip her ikisine de esit agirlik veriyor.
@Composable
fun ModeSelectScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    tokens: Int,
    endlessBestScore: Int,
    highestUnlockedLevel: Int,
    // Faz 77: Pro Mode (eski "Challenge") artik oynanabilir — kart YAKINDA/
    // kilitli degil, kendi ilerlemesini gosteriyor.
    highestChallengeLevel: Int,
    // Faz 78: Retro Modu artik oynanabilir — kart YAKINDA/kilitli degil.
    retroHighScore: Int,
    onOpenLevels: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenChallenge: () -> Unit,
    onOpenRetro: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Faz 44: logo+isim ve jeton/görevler/ayarlar oncede İKİ AYRI satirdi —
            // kullanici "logo ve isim üste taşınmalı ki altta yer açılsın, böyle
            // tasarım kötü gözüküyor" dedi. Artik TEK satirda: logo+isim sola,
            // jeton/görevler/ayarlar saga (LevelMapHeader/oyun ekrani basligindaki
            // ayni desen) — bir satir yuksekliginde alan mod kartlarina geri
            // kazandiriliyor.
            // Faz 60: kullanici ekran goruntusuyle "sagdaki jeton/kupa/ayarlar
            // kumesi sag kenara yapisik degil, bosluk var" dedi — kok neden
            // `weight(1f, fill = false)` idi: fill=false, bu Row'un icerigi
            // (ikon+baslik) kadar KUCULMESINE izin veriyordu, bu yuzden Row
            // kendi payinin tamamini KAPLAMIYOR, hemen ardindan gelen
            // jeton/ayarlar kumesi de sag kenara degil, kucuk Row'un hemen
            // sagina (ekranin ortalarina yakin bir yere) yerlesiyordu.
            // fill=true (varsayilan) ile Row artik kalan TUM genisligi
            // kapliyor, boylece sagdaki kume gercekten sag kenara yapisiyor.
            // "Boom Blocks" sabit/kisa bir metin oldugu icin ellipsis riski yok.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.card)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Boom Blocks",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                ModeSelectHeaderActions(
                    language = language,
                    tokens = tokens,
                    palette = palette,
                    onOpenMissions = onOpenMissions,
                    onOpenSettings = onOpenSettings
                )
            }

            Text(
                text = language.pick(tr = "Bir oyun modu seç", en = "Choose a game mode", it = "Scegli una modalità di gioco", fr = "Choisissez un mode de jeu", es = "Elige un modo de juego"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary,
                modifier = Modifier.padding(start = 50.dp, top = 2.dp)
            )

            // Faz 71: kullanici "oyun tipi secimi 2x2 olsun, asagi dogru
            // (kaydirma) olmasin" dedi — 4 moda (Sonsuz/Seviyeli/Challenge/
            // Retro) hazirlik olarak dikey tek-sutunluk liste yerine 2x2 grid.
            // Butun modlar kaydirmadan tek bakista goruluyor. Challenge ve
            // Retro henuz oynanabilir olmadigi icin "YAKINDA" kilitli
            // tasarim olarak eklendi — grid'in tamamlanmis gorunmesi ve
            // gelecek modlarin onizlemesi icin.
            // Faz 72: kullanici "kareler olsun, gridde ortalansinlar alttan
            // ustten bosluk esit kalacak sekilde" dedi — eskiden bu Column
            // weight(1f) ile kalan TUM dikey alani zorla dolduruyor, kartlar
            // da fillMaxHeight() ile o alana GERiliyordu (genis dikdortgen).
            // Artik disaridaki Box weight(1f) alani aliyor ama iceride kartlar
            // aspectRatio(1f) ile KARE kalip, Box'un contentAlignment=Center'i
            // sayesinde ust/alt bosluk otomatik esitleniyor.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = language.pick(tr = "SONSUZ", en = "ENDLESS", it = "INFINITA", fr = "INFINI", es = "INFINITO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                            statValue = "$endlessBestScore",
                            icon = Icons.Default.AllInclusive,
                            accent = NeonGreen,
                            palette = palette,
                            onClick = onOpenEndless,
                            testTag = "mode_select_endless_button",
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ModeCard(
                            title = language.pick(tr = "SEVİYELİ", en = "LEVEL", it = "LIVELLI", fr = "NIVEAUX", es = "NIVELES"),
                            statLabel = language.pick(tr = "EN YÜKSEK SEVİYE", en = "HIGHEST LEVEL", it = "LIVELLO PIÙ ALTO", fr = "NIVEAU LE PLUS HAUT", es = "NIVEL MÁS ALTO"),
                            statValue = "$highestUnlockedLevel",
                            icon = Icons.Default.Extension,
                            accent = NeonCyan,
                            palette = palette,
                            onClick = onOpenLevels,
                            testTag = "mode_select_levels_button",
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SEVİYE", en = "HIGHEST LEVEL", it = "LIVELLO PIÙ ALTO", fr = "NIVEAU LE PLUS HAUT", es = "NIVEL MÁS ALTO"),
                            statValue = "$highestChallengeLevel",
                            icon = Icons.Default.LocalFireDepartment,
                            accent = androidx.compose.ui.graphics.Color(0xFFFF6B35),
                            palette = palette,
                            onClick = onOpenChallenge,
                            testTag = "mode_select_challenge_button",
                            locked = false,
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ModeCard(
                            title = language.pick(tr = "RETRO", en = "RETRO", it = "RETRO", fr = "RÉTRO", es = "RETRO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                            statValue = "$retroHighScore",
                            icon = Icons.Default.ViewModule,
                            accent = NeonPurple,
                            palette = palette,
                            onClick = onOpenRetro,
                            testTag = "mode_select_retro_button",
                            locked = false,
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectHeaderActions(
    language: AppLanguage,
    tokens: Int,
    palette: BlastPalette,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = NeonGold.copy(alpha = 0.18f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .testTag("mode_select_token_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🪙", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$tokens",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGold
                )
            }
        }

        // Faz 44: tek satira sigdirmak icin ikon dugmeleri de (oyun ekrani basligindaki
        // Faz 28 desenine benzer) 48dp varsayilan dokunma alani yerine 36dp'ye daraltildi.
        IconButton(
            onClick = onOpenMissions,
            modifier = Modifier.size(36.dp).testTag("mode_select_missions_button")
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                tint = NeonPurple,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(36.dp).testTag("mode_select_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                tint = palette.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Faz 71: 2x2 grid'e gecince kart artik genis/yatay degil, kareye yakin bir
// tile — eski yatay Row (ikon+baslik+alt yazi solda, istatistik sagda) dar
// tile'da sikisip taniz duruyordu. Ikon ortada ustte, baslik altinda, istatistik
// en altta — dikey, ortalanmis bir duzen. `subtitle` kaldirildi (dar tile'da
// yer yok, zaten baslik + ikon modu yeterince anlatiyor). `locked` (Faz 71,
// Challenge/Retro icin) true oldugunda kart soluk gorunuyor, kucuk bir kilit
// rozeti + "YAKINDA" etiketi gosteriyor, tiklama hicbir sey yapmiyor.
@Composable
private fun ModeCard(
    title: String,
    statLabel: String,
    statValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    palette: BlastPalette,
    onClick: () -> Unit,
    testTag: String,
    locked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val effectiveAccent = if (locked) palette.textSecondary else accent
    // Faz 72: kullanici "transparan cerceve uzerinde durmasinlar, canli
    // renklerle dolu olsun, zeminleri buton gibi olsun" dedi — kilit acik
    // kartlar artik notr palette.card yerine kendi accent renginin
    // gradyaniyla DOLU (gercek bir buton gibi). Kilitli (Challenge/Retro)
    // kartlar eskisi gibi notr/soluk kaliyor, sadece unlocked kartlar
    // canli dolgu aliyor.
    val fillBrush = if (locked) {
        SolidColor(palette.card)
    } else {
        Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.72f)))
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .background(fillBrush, RoundedCornerShape(20.dp))
            .border(
                2.dp,
                if (locked) {
                    SolidColor(palette.cardBorder)
                } else {
                    Brush.linearGradient(listOf(accent, NeonGold))
                },
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !locked, onClick = onClick)
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (locked) effectiveAccent.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (locked) Icons.Default.Lock else icon,
                        contentDescription = null,
                        tint = if (locked) effectiveAccent else Color.White,
                        modifier = Modifier.size(if (locked) 22.dp else 26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (locked) palette.textSecondary else Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = statLabel,
                    fontSize = 9.sp,
                    color = if (locked) palette.textSecondary else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (statValue.isNotEmpty()) {
                    Text(
                        text = statValue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (locked) NeonGold else Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
