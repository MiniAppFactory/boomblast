package com.example.ui.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.WeeklyMissionDef
import com.example.data.WeeklyMissionProgress
import com.example.data.pick
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.blastPalette

@Composable
fun MissionsScreen(
    missionProgress: WeeklyMissionProgress,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onClaim: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val allClaimed = missionProgress.missions.isNotEmpty() &&
        missionProgress.missions.all { mission ->
            mission.tiers.indices.all { tierIndex -> "${mission.id}#$tierIndex" in missionProgress.claimed }
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("missions_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                Text(
                    text = language.pick(tr = "HAFTALIK GÖREVLER", en = "WEEKLY MISSIONS", it = "MISSIONI SETTIMANALI", fr = "MISSIONS HEBDOMADAIRES", es = "MISIONES SEMANALES"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Spacer to balance the back button so the title stays centered
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (allClaimed) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = NeonGreen.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("missions_all_claimed_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = NeonGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = language.pick(
                                tr = "Bu haftaki tüm görevler tamamlandı!",
                                en = "All missions completed this week!",
                                it = "Tutte le missioni di questa settimana completate!",
                                fr = "Toutes les missions de la semaine sont terminées !",
                                es = "¡Todas las misiones de esta semana completadas!"
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missionProgress.missions) { mission ->
                    MissionCard(
                        mission = mission,
                        currentCount = missionProgress.progress[mission.id] ?: 0,
                        claimedTiers = mission.tiers.indices.filter { "${mission.id}#$it" in missionProgress.claimed }.toSet(),
                        language = language,
                        palette = palette,
                        onClaim = { tierIndex -> onClaim(mission.id, tierIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: WeeklyMissionDef,
    currentCount: Int,
    claimedTiers: Set<Int>,
    language: AppLanguage,
    palette: BlastPalette,
    onClaim: (tierIndex: Int) -> Unit
) {
    // Faz 73: her gorev artik 3 milestone'li bir merdiven — kart HER ZAMAN
    // bir sonraki claim edilmemis tier'i "aktif" olarak gosterir (progress
    // bar/hedef/odul o tier'e gore), ustte 3 kucuk nokta ile hangi tier'lerin
    // claim edildigini/ulasildigini/henuz ulasilmadigini ozetler.
    val activeTierIndex = mission.tiers.indices.firstOrNull { it !in claimedTiers }
    val allTiersClaimed = activeTierIndex == null
    val activeTier = activeTierIndex?.let { mission.tiers[it] } ?: mission.tiers.last()
    val isComplete = currentCount >= activeTier.target
    // Faz 76: kullanici "milestone kriterleri hicbir yerde yazmiyor" dedi —
    // karttaki baslik/ilerleme sadece AKTIF tier'i gosteriyordu, 3 tier'in
    // tamamini gormenin bir yolu yoktu. Kucuk bir "i" ikonu tiklayinca
    // 3 tier'in tamamini (hedef+odul) listeleyen kisa bir overlay aciliyor.
    var showInfo by remember { mutableStateOf(false) }
    val isClaimable = !allTiersClaimed && isComplete
    val isClaimed = allTiersClaimed

    // Faz 70: kullanici "haftalik gorevler sayfasinin gorseli cirkin, kabartmali
    // gibi olsun" dedi. Duz Material3 Card yerine — CFO-Catch projesindeki
    // EmbossedCard deseninden esinlenerek — golge + dikey gradyan arka plan +
    // ust kenari acik/alt kenari koyu bir gradyan kenarlik ile "kabartilmis
    // panel" hissi verildi. Tamamlanan gorevlerde kenarlik altin renge donuyor,
    // kucuk bir "basarildi" isareti.
    val borderTopColor = if (isComplete) NeonGold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f)
    val borderBottomColor = if (isComplete) NeonGold.copy(alpha = 0.35f) else palette.cardBorder
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.card, palette.card.copy(alpha = 0.88f))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(colors = listOf(borderTopColor, borderBottomColor)),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("mission_card_${mission.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mission.title(language, if (allTiersClaimed) mission.tiers.last().target else activeTier.target),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(palette.cardAlt)
                            .clickable { showInfo = true }
                            .testTag("mission_info_${mission.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = language.pick(tr = "Görev detayı", en = "Mission details", it = "Dettagli missione", fr = "Détails de la mission", es = "Detalles de la misión"),
                            tint = palette.textSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activeTier.rewardTokens}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Faz 75: kullanici "3 ayri yuvarlak + ayri bir ilerleme cubugu
            // yerine, TEK bir cubuk olsun, milestone'lar o cubugun UZERINDE
            // duran noktalar olsun" dedi (elle cizilmis bir referans gonderdi:
            // tek çizgi + üzerinde 2 nokta). Tek TieredProgressBar: tum gorevin
            // 0..sonTier.target araligini kaplayan bir cubuk, 2 ara-tier
            // sinirinda (3 tier = 2 sinir) ustune binen yuvarlak isaretlerle.
            TieredProgressBar(
                mission = mission,
                currentCount = currentCount,
                claimedTiers = claimedTiers,
                palette = palette
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (allTiersClaimed) {
                    "${mission.tiers.last().target} / ${mission.tiers.last().target}"
                } else {
                    "${currentCount.coerceAtMost(activeTier.target)} / ${activeTier.target}"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isClaimed -> {
                    Surface(
                        color = palette.cardAlt,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_mission_${mission.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = palette.textSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.pick(tr = "Alındı", en = "Claimed", it = "Riscosso", fr = "Réclamé", es = "Reclamado"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textSecondary
                            )
                        }
                    }
                }

                isClaimable -> {
                    Button(
                        onClick = { activeTierIndex?.let(onClaim) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_mission_${mission.id}")
                    ) {
                        Text(
                            text = language.pick(tr = "TOPLA", en = "CLAIM", it = "RISCUOTI", fr = "RÉCLAMER", es = "RECLAMAR"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.cardAlt,
                            disabledContainerColor = palette.cardAlt
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_mission_${mission.id}")
                    ) {
                        Text(
                            text = language.pick(tr = "TOPLA", en = "CLAIM", it = "RISCUOTI", fr = "RÉCLAMER", es = "RECLAMAR"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                Button(onClick = { showInfo = false }) {
                    Text(text = language.pick(tr = "Tamam", en = "OK", it = "OK", fr = "OK", es = "OK"))
                }
            },
            title = {
                Text(
                    text = language.pick(tr = "Görev Basamakları", en = "Mission Tiers", it = "Livelli Missione", fr = "Paliers de Mission", es = "Niveles de Misión"),
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    mission.tiers.forEachIndexed { tierIndex, tier ->
                        if (tierIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                        val tierStatus = when {
                            tierIndex in claimedTiers -> language.pick(tr = "✅ Alındı", en = "✅ Claimed", it = "✅ Riscosso", fr = "✅ Réclamé", es = "✅ Reclamado")
                            currentCount >= tier.target -> language.pick(tr = "🟢 Hazır", en = "🟢 Ready", it = "🟢 Pronto", fr = "🟢 Prêt", es = "🟢 Listo")
                            else -> language.pick(tr = "🔒 Kilitli", en = "🔒 Locked", it = "🔒 Bloccato", fr = "🔒 Verrouillé", es = "🔒 Bloqueado")
                        }
                        Text(
                            text = "${tierIndex + 1}. ${mission.title(language, tier.target)} — ${tier.rewardTokens} 🪙",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tierStatus,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        )
    }
}

private enum class TierDotState { CLAIMED, READY, LOCKED }

// Faz 75: kullanici elle cizilmis bir referans gonderdi — TEK bir cubuk,
// uzerinde ara-tier sinirlarini isaretleyen yuvarlaklar (3 dot + ayri
// progress bar yerine). Cubugun 0..son-tier.target araligi doluyor,
// ara-tier sinirlarinda (3 tier = 2 sinir) TierDot'lar cubugun UZERINE
// biniyor.
@Composable
private fun TieredProgressBar(
    mission: WeeklyMissionDef,
    currentCount: Int,
    claimedTiers: Set<Int>,
    palette: BlastPalette
) {
    val maxTarget = mission.tiers.last().target.coerceAtLeast(1)
    val fillFraction = (currentCount.toFloat() / maxTarget.toFloat()).coerceIn(0f, 1f)
    val barColor = if (currentCount >= maxTarget) NeonGreen else NeonCyan

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(palette.cardAlt)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(fillFraction)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barColor)
        )
        mission.tiers.dropLast(1).forEachIndexed { tierIndex, tier ->
            val markerFraction = (tier.target.toFloat() / maxTarget.toFloat()).coerceIn(0f, 1f)
            val dotState = when {
                tierIndex in claimedTiers -> TierDotState.CLAIMED
                currentCount >= tier.target -> TierDotState.READY
                else -> TierDotState.LOCKED
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = maxWidth * markerFraction - 8.dp)
            ) {
                TierDot(state = dotState, palette = palette)
            }
        }
    }
}

@Composable
private fun TierDot(state: TierDotState, palette: BlastPalette) {
    val fill = when (state) {
        TierDotState.CLAIMED -> NeonGold
        TierDotState.READY -> NeonGreen
        TierDotState.LOCKED -> palette.cardAlt
    }
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(fill)
            .border(2.dp, palette.card, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (state == TierDotState.CLAIMED) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}
