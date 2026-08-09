package com.example.ui.modeselect

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
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
    onOpenLevels: () -> Unit,
    onOpenEndless: () -> Unit,
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
            ModeSelectHeader(
                language = language,
                tokens = tokens,
                palette = palette,
                onOpenMissions = onOpenMissions,
                onOpenSettings = onOpenSettings
            )

            // Onceden basliktan sonraki tum icerik ekranin ustune sabitti ve
            // alt yarida buyuk bos alan kaliyordu (kullanici geri bildirimi:
            // "oyun tum ekrani kaplamıyor"). Kalan dikey alanda ortalanarak
            // ekranin tamamini kullaniyor.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Marka kimligi icin logo rozeti — onceden sadece duz yazi vardi,
                // rakip oyunlarin hepsinde basligin yaninda bir ikon/logo var (UI/UX
                // karsilastirma bulgusu).
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.card)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Blast the Blocks",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = palette.textPrimary
                        )
                        Text(
                            text = language.pick(tr = "Bir oyun modu seç", en = "Choose a game mode", it = "Scegli una modalità di gioco", fr = "Choisissez un mode de jeu", es = "Elige un modo de juego"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ModeCard(
                    title = language.pick(tr = "SONSUZ MOD", en = "ENDLESS MODE", it = "MODALITÀ INFINITA", fr = "MODE INFINI", es = "MODO INFINITO"),
                    subtitle = language.pick(tr = "Hedefsiz oyna, yüksek skor kovala", en = "Play freely, chase a high score", it = "Gioca liberamente, punta al record", fr = "Jouez librement, visez le meilleur score", es = "Juega libremente, persigue una puntuación alta"),
                    statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                    statValue = "$endlessBestScore",
                    icon = Icons.Default.AllInclusive,
                    accent = NeonGreen,
                    palette = palette,
                    onClick = onOpenEndless,
                    testTag = "mode_select_endless_button",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                ModeCard(
                    title = language.pick(tr = "SEVİYELİ MOD", en = "LEVEL MODE", it = "MODALITÀ LIVELLI", fr = "MODE NIVEAUX", es = "MODO NIVELES"),
                    subtitle = language.pick(tr = "Seviye seviye ilerle, yıldız topla", en = "Progress level by level, earn stars", it = "Avanza livello dopo livello, guadagna stelle", fr = "Progressez niveau par niveau, gagnez des étoiles", es = "Avanza nivel a nivel, gana estrellas"),
                    statLabel = language.pick(tr = "EN YÜKSEK SEVİYE", en = "HIGHEST LEVEL", it = "LIVELLO PIÙ ALTO", fr = "NIVEAU LE PLUS HAUT", es = "NIVEL MÁS ALTO"),
                    statValue = "$highestUnlockedLevel",
                    icon = Icons.Default.Extension,
                    accent = NeonCyan,
                    palette = palette,
                    onClick = onOpenLevels,
                    testTag = "mode_select_levels_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeSelectHeader(
    language: AppLanguage,
    tokens: Int,
    palette: BlastPalette,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🪙", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$tokens",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGold
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onOpenMissions,
            modifier = Modifier.testTag("mode_select_missions_button")
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                tint = NeonPurple
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("mode_select_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                tint = palette.textPrimary
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    statLabel: String,
    statValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    palette: BlastPalette,
    onClick: () -> Unit,
    testTag: String,
    // Faz 23: kart sabit 176dp yukseklikteydi, dikey ortalama (Faz 21) bosluğu
    // yeniden dağıtsa da TOPLAM bos alani azaltmiyordu — kullanici hala "ekranı
    // kaplamıyor" dedi. Artik cagiran taraf Modifier.weight(1f) verip karti kalan
    // TUM dikey alani doldurmaya zorluyor, bosluk gercekten ortadan kalkiyor.
    modifier: Modifier = Modifier.height(176.dp)
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .border(2.dp, Brush.linearGradient(listOf(accent, NeonGold)), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = statLabel,
                    fontSize = 9.sp,
                    color = palette.textSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                )
                Text(
                    text = statValue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGold,
                    maxLines = 1
                )
            }
        }
    }
}
