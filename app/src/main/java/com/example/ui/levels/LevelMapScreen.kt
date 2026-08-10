package com.example.ui.levels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.PlayerProgress
import com.example.data.pick
import com.example.game.LevelGenerator
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.blastPalette
import kotlin.math.sin

@Composable
fun LevelMapScreen(
    progress: PlayerProgress,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onSelectLevel: (Int) -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val lastLevel = progress.highestUnlockedLevel + 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LevelMapHeader(
                progress = progress,
                language = language,
                palette = palette,
                onOpenMissions = onOpenMissions,
                onOpenSettings = onOpenSettings,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Faz 42: onceden duz bir LazyColumn liste kartlari alt alta diziyordu
            // (kullanici: "yıldızların anlamı yok, düz liste gibi... slalomlu eğlenceli
            // bir harita olsun"). Her ogenin x konumu index'e gore bir sinus dalgasiyla
            // hesaplaniyor (saf formul, olculmus komsu-oge pozisyonuna ihtiyac yok —
            // LazyColumn sanallastirmasiyla tam uyumlu), aralarindaki kesikli çizgi de
            // ayni formulle her ogenin KENDI Canvas'inda (onceki->bu ogenin x'i) ciziliyor.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_map_list"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
            ) {
                items(count = lastLevel, key = { it + 1 }) { index ->
                    val levelNumber = index + 1
                    val unlocked = levelNumber <= progress.highestUnlockedLevel
                    val stars = progress.levelStars[levelNumber]
                    LevelPathNode(
                        levelNumber = levelNumber,
                        unlocked = unlocked,
                        stars = stars,
                        prevXFraction = if (index == 0) null else pathXFraction(index - 1),
                        currXFraction = pathXFraction(index),
                        isLastItem = index == lastLevel - 1,
                        language = language,
                        palette = palette,
                        onClick = { if (unlocked) onSelectLevel(levelNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelMapHeader(
    progress: PlayerProgress,
    language: AppLanguage,
    palette: BlastPalette,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Faz 25: "SEVİYELER" basligi ile sag taraftaki jeton kapsulu arasinda
        // hicbir esneme payi yoktu — dar ekranlarda ikisi "dip dibe" cakisiyordu
        // (kullanici geri bildirimi, ekran goruntusuyle dogrulandi). Sol grup artik
        // `weight(1f, fill=false)` ile sinirli ve basligin kendisi tek satirda
        // gerekirse ellipsis ile kisaliyor, sag gruptan asla alan calmiyor.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("level_map_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                    tint = palette.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = language.pick(tr = "SEVİYELER", en = "LEVELS", it = "LIVELLI", fr = "NIVEAUX", es = "NIVELES"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Token balance pill
            Surface(
                color = NeonGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("level_map_token_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 14.sp) // 🪙
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.tokens}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onOpenMissions,
                modifier = Modifier.testTag("level_map_missions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                    tint = NeonPurple
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("level_map_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                    tint = palette.textPrimary
                )
            }
        }
    }
}

// Faz 42: slalom/zigzag harita — index'e gore 0f..1f arasinda salinan bir x
// konumu. Komsu ogelerin gercek olculmus pozisyonuna bagli DEGIL (LazyColumn
// sanallastirmasi hala calisir), sadece index'in kendisinden turetiliyor.
private fun pathXFraction(index: Int): Float =
    0.5f + 0.30f * sin(index * 1.05f)

private val LEVEL_PATH_ITEM_HEIGHT = 132.dp
private val LEVEL_NODE_SIZE = 56.dp
// Dugumun DUSEY merkezi ogenin tepesinden bu kadar asagida — sabit bir dp
// degeri olarak tutuluyor ki Canvas'taki cizgi ile Column'daki gercek dugum
// konumu HER ZAMAN birebir ortussun (BiasAlignment ile tahmin degil).
private val LEVEL_NODE_CENTER_Y = 40.dp

// Faz 42: baglanti cizgisi iki parcaya bolundu — (1) bu dugumden asagi, oge
// sinirina kadar duz bir "cikis" izi, (2) bir onceki dugumun x'inden gelip BU
// dugume varan capraz "varis" izi. Boylece her oge SADECE kendi bilgisiyle
// (prevX, currX, sabit nodeY) cizim yapiyor ama komsu ogelerle tam piksel
// hassasiyetinde birlesiyor — ilk denemede (sadece "varis" izi, oge tepesinden
// baslayan) dugum ile cizginin bittigi nokta arasinda gozle gorulur bir bosluk
// vardi (kullanici: "slalomlu harita" istegi sonrasi ilk halde cizgi dugume
// hic degmiyordu), kok neden buydu.
@Composable
private fun LevelPathNode(
    levelNumber: Int,
    unlocked: Boolean,
    stars: Int?,
    prevXFraction: Float?,
    currXFraction: Float,
    isLastItem: Boolean,
    language: AppLanguage,
    palette: BlastPalette,
    onClick: () -> Unit
) {
    val targetScore = LevelGenerator.forLevel(levelNumber).targetScore
    val borderBrush = if (unlocked) {
        Brush.linearGradient(listOf(NeonCyan, NeonPurple))
    } else {
        Brush.linearGradient(listOf(palette.cardBorder, palette.cardBorder))
    }
    val pathColor = if (unlocked) NeonCyan.copy(alpha = 0.45f) else palette.cardBorder.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LEVEL_PATH_ITEM_HEIGHT)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nodeY = LEVEL_NODE_CENTER_Y.toPx()
            if (prevXFraction != null) {
                drawLine(
                    color = pathColor,
                    start = Offset(prevXFraction * size.width, 0f),
                    end = Offset(currXFraction * size.width, nodeY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                )
            }
            if (!isLastItem) {
                drawLine(
                    color = pathColor,
                    start = Offset(currXFraction * size.width, nodeY),
                    end = Offset(currXFraction * size.width, size.height),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(BiasAlignment(currXFraction * 2f - 1f, -1f))
                .offset(y = LEVEL_NODE_CENTER_Y - LEVEL_NODE_SIZE / 2)
                .alpha(if (unlocked) 1f else 0.5f)
                .clickable(enabled = unlocked, onClick = onClick)
                .testTag("level_card_$levelNumber")
        ) {
            // Seviye numarasi/kilit rozeti — dugumun kendisi
            Box(
                modifier = Modifier
                    .size(LEVEL_NODE_SIZE)
                    .clip(CircleShape)
                    .background(if (unlocked) palette.card else palette.cardAlt)
                    .border(2.5.dp, borderBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Text(
                        text = "$levelNumber",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = language.pick(tr = "Kilitli", en = "Locked", it = "Bloccato", fr = "Verrouillé", es = "Bloqueado"),
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = language.pick(tr = "HEDEF: $targetScore", en = "TARGET: $targetScore", it = "OBIETTIVO: $targetScore", fr = "OBJECTIF : $targetScore", es = "OBJETIVO: $targetScore"),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center
            )

            if (unlocked) {
                Spacer(modifier = Modifier.height(2.dp))
                StarRow(stars = stars ?: 0)
            }
        }
    }
}

@Composable
private fun StarRow(stars: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { i ->
            val filled = i < stars
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (filled) NeonGold else Color.DarkGray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
