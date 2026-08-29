package com.miniappfactory.boomblocks.ui.missions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.MissionType
import com.miniappfactory.boomblocks.data.WeeklyMissionDef
import com.miniappfactory.boomblocks.data.WeeklyMissionProgress
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.components.GameButton
import com.miniappfactory.boomblocks.ui.components.GameCoinPill
import com.miniappfactory.boomblocks.ui.components.GamePanel
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.components.GameScreenHeader
import com.miniappfactory.boomblocks.ui.components.GameScrollHint
import com.miniappfactory.boomblocks.ui.components.GameTierState
import com.miniappfactory.boomblocks.ui.components.GameTieredProgressBar
import com.miniappfactory.boomblocks.ui.components.GoldPillAccent
import com.miniappfactory.boomblocks.ui.components.IconMedallion
import com.miniappfactory.boomblocks.ui.components.NeonCard
import com.miniappfactory.boomblocks.ui.components.gameButtonColors
import com.miniappfactory.boomblocks.ui.components.mutedGameButtonColors
import com.miniappfactory.boomblocks.ui.components.roleTint
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces

// Faz 158 — GOREVLER: LISTEDEN PANOYA.
//
// Kullanicinin teshisi: "gorevler menusu cok tekduze, kurumsal bir sirketin
// pptx'i gibi". Ekran goruntusunde uc kart BIREBIR aynidiy: ayni yukseklik,
// ayni kenarlik, ayni gri "TOPLA" butonu. Yani TAMAMLANMIS gorev ile
// TAMAMLANMAMIS gorev arasinda gorsel fark neredeyse yoktu — odul ani
// tamamen kayipti.
//
// Yeni yapi:
//   1. USTTE OZET PANOSU — kac basamagin toplandigi, genel ilerleme cubugu ve
//      bu hafta kalan toplam odul. Liste bir "pano"ya donuyor.
//   2. HER GOREV KENDI KIMLIGINDE — tipine gore ikon madalyonu + renk
//      (`MissionType` uzerinden; uydurma degil, veri modelinden).
//   3. DURUM AYRIMI — kartin kenarlik PARLAKLIGI ilerlemeyle artiyor:
//      * hazir  -> canli yesil kenarlik + CANLI "TOPLA" butonu (kabartmali)
//      * devam  -> ilerleme kadar parlayan accent kenarlik + dolu cubuk
//      * alindi -> altin kenarlik + onay isareti, sonuk
//      Fark renk-disi kanallarda da var (dolgu miktari, ikon, buton kabartmasi)
//      — renk korlugunde de okunuyor.
//   4. ODUL — "20 🪙" emoji yerine gercek `icon_coin` varligi + altin sayi.
//   5. KAYDIRMA IPUCU — liste dibinde chevron.
//
// DEGISMEYEN: gorev tanimlari, kademeler, odul miktarlari, claim mantigi.
@Composable
fun MissionsScreen(
    missionProgress: WeeklyMissionProgress,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onClaim: (String, Int) -> Unit,
    onBack: () -> Unit
) {
    val surfaces = rememberGameSurfaces(skin, darkMode)
    val listState = rememberLazyListState()

    val totalTiers = missionProgress.missions.sumOf { it.tiers.size }
    val claimedTierCount = missionProgress.missions.sumOf { mission ->
        mission.tiers.indices.count { "${mission.id}#$it" in missionProgress.claimed }
    }
    val allClaimed = totalTiers > 0 && claimedTierCount == totalTiers
    // Bu hafta HENUZ toplanmamis odul toplami — sahte sayi degil, gercek
    // tier odullerinden hesaplaniyor.
    val remainingReward = missionProgress.missions.sumOf { mission ->
        mission.tiers.filterIndexed { index, _ ->
            "${mission.id}#$index" !in missionProgress.claimed
        }.sumOf { it.rewardTokens }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            modifier = Modifier.matchParentSize()
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            GameScreenHeader(
                title = language.pick(
                    tr = "HAFTALIK GÖREVLER",
                    en = "WEEKLY MISSIONS",
                    it = "MISSIONI SETTIMANALI",
                    fr = "MISSIONS HEBDOMADAIRES",
                    es = "MISIONES SEMANALES"
                ),
                surfaces = surfaces,
                onBack = onBack,
                backDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                backTestTag = "missions_back_button",
                // TASMA: "MISSIONS HEBDOMADAIRES" (FR) ve "MISIONES SEMANALES"
                // uzun — GameTitle maxLines = 2, baslik iki satira taser.
                titleFontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // -----------------------------------------------------------
            // Ozet panosu
            // -----------------------------------------------------------
            MissionsSummaryBoard(
                claimedTierCount = claimedTierCount,
                totalTiers = totalTiers,
                remainingReward = remainingReward,
                allClaimed = allClaimed,
                language = language,
                surfaces = surfaces
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(missionProgress.missions) { mission ->
                        MissionCard(
                            mission = mission,
                            currentCount = missionProgress.progress[mission.id] ?: 0,
                            claimedTiers = mission.tiers.indices
                                .filter { "${mission.id}#$it" in missionProgress.claimed }
                                .toSet(),
                            language = language,
                            surfaces = surfaces,
                            onClaim = { tierIndex -> onClaim(mission.id, tierIndex) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                GameScrollHint(
                    visible = listState.canScrollForward,
                    surfaces = surfaces
                )
            }
        }
    }
}

// Ozet panosu: kupa madalyonu + toplanan basamak + genel ilerleme +
// kalan odul. Hepsi gercek veriden.
@Composable
private fun MissionsSummaryBoard(
    claimedTierCount: Int,
    totalTiers: Int,
    remainingReward: Int,
    allClaimed: Boolean,
    language: AppLanguage,
    surfaces: GameSurfaces
) {
    val accent = if (allClaimed) NeonGreen else surfaces.accentPrimary
    val fraction = if (totalTiers == 0) 0f else claimedTierCount.toFloat() / totalTiers
    GamePanel(
        surfaces = surfaces,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (allClaimed) Modifier.testTag("missions_all_claimed_banner") else Modifier),
        emphasis = if (allClaimed) 1f else 0.5f,
        accent = accent,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconMedallion(accent = accent, size = 46.dp) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.35f) else lerp(accent, Color.White, 0.35f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (allClaimed) {
                        language.pick(
                            tr = "Bu haftaki tüm görevler tamamlandı!",
                            en = "All missions completed this week!",
                            it = "Tutte le missioni di questa settimana completate!",
                            fr = "Toutes les missions de la semaine sont terminées !",
                            es = "¡Todas las misiones de esta semana completadas!"
                        )
                    } else {
                        language.pick(
                            tr = "Toplanan basamak",
                            en = "Tiers claimed",
                            it = "Livelli riscossi",
                            fr = "Paliers réclamés",
                            es = "Niveles reclamados"
                        )
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = surfaces.accentText,
                    // TASMA: tamamlanma metni 5 dilde de uzun (IT/FR ozellikle)
                    // — iki satira taser, kart uzar, kirpilmaz.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$claimedTierCount / $totalTiers",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.4f) else lerp(accent, Color.White, 0.45f),
                    maxLines = 1
                )
            }
            if (remainingReward > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = language.pick(tr = "KALAN", en = "LEFT", it = "RESTA", fr = "RESTE", es = "QUEDA"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = surfaces.hairline,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    GameCoinPill(amount = "$remainingReward", surfaces = surfaces, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        GameTieredProgressBar(
            fraction = fraction,
            tierMarkers = emptyList(),
            surfaces = surfaces,
            accent = accent
        )
    }
}

// Gorev tipinin kimligi: ikon + renk. `MissionType` veri modelinden geliyor,
// gorev metinleri uydurulmuyor.
private fun missionIcon(type: MissionType): ImageVector = when (type) {
    MissionType.COMPLETE_LEVELS -> Icons.Filled.Flag
    MissionType.CLEAR_LINES -> Icons.Filled.Bolt
    MissionType.USE_BOOSTERS -> Icons.Filled.RocketLaunch
    MissionType.SCORE_POINTS -> Icons.Filled.Star
    MissionType.MULTI_CLEARS -> Icons.Filled.AutoAwesome
}

private fun missionRole(type: MissionType): Color = when (type) {
    MissionType.COMPLETE_LEVELS -> NeonCyan
    MissionType.CLEAR_LINES -> NeonGold
    MissionType.USE_BOOSTERS -> Color(0xFFFF6B35)
    MissionType.SCORE_POINTS -> NeonPurple
    MissionType.MULTI_CLEARS -> NeonGreen
}

@Composable
private fun MissionCard(
    mission: WeeklyMissionDef,
    currentCount: Int,
    claimedTiers: Set<Int>,
    language: AppLanguage,
    surfaces: GameSurfaces,
    onClaim: (tierIndex: Int) -> Unit
) {
    // Faz 73: her gorev 3 milestone'li bir merdiven; kart HER ZAMAN bir
    // sonraki claim edilmemis tier'i "aktif" gosterir.
    val activeTierIndex = mission.tiers.indices.firstOrNull { it !in claimedTiers }
    val allTiersClaimed = activeTierIndex == null
    val activeTier = activeTierIndex?.let { mission.tiers[it] } ?: mission.tiers.last()
    val isComplete = currentCount >= activeTier.target
    val isClaimable = !allTiersClaimed && isComplete
    // Faz 76: "i" ikonu 3 tier'in tamamini gosteren overlay aciyor.
    var showInfo by remember { mutableStateOf(false) }

    val role = roleTint(missionRole(mission.type), surfaces)
    val maxTarget = mission.tiers.last().target.coerceAtLeast(1)
    val overallFraction = (currentCount.toFloat() / maxTarget).coerceIn(0f, 1f)

    // KART DURUMU: kenarlik parlakligi (glow) ve accent rengi durumu tasiyor.
    val cardAccent = when {
        isClaimable -> NeonGreen
        allTiersClaimed -> NeonGold
        else -> role
    }
    val glow = when {
        isClaimable -> 1f
        allTiersClaimed -> 0.55f
        // Ilerleme yoksa kart bilerek SONUK — hedef mockup'ta da boyle.
        else -> (0.20f + overallFraction * 0.65f)
    }

    NeonCard(
        surfaces = surfaces,
        accent = cardAccent,
        glow = glow,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}"),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconMedallion(
                accent = cardAccent,
                size = 42.dp,
                dimmed = allTiersClaimed
            ) {
                Icon(
                    imageVector = if (allTiersClaimed) Icons.Filled.Check else missionIcon(mission.type),
                    contentDescription = null,
                    tint = if (surfaces.isLightSurface) {
                        lerp(cardAccent, Color.Black, 0.35f)
                    } else {
                        lerp(cardAccent, Color.White, 0.40f)
                    },
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mission.title(
                            language,
                            if (allTiersClaimed) mission.tiers.last().target else activeTier.target
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (surfaces.isLightSurface) Color(0xFF12161F) else Color.White,
                        // TASMA: "Esegui 15 Cancellazioni Multiple" (IT) ve
                        // "Marquer 1000 Points au Total" (FR) tek satira
                        // sigmaz — iki satira taser, kart uzar. "i" rozeti
                        // sabit genislikte oldugu icin baslik weight(1f) alir.
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(surfaces.sunken)
                            .clickable { showInfo = true }
                            .testTag("mission_info_${mission.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = language.pick(tr = "Görev detayı", en = "Mission details", it = "Dettagli missione", fr = "Détails de la mission", es = "Detalles de la misión"),
                            tint = surfaces.accentText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Odul: emoji degil, gercek jeton varligi + altin sayi.
            GameCoinPill(
                amount = "${activeTier.rewardTokens}",
                surfaces = surfaces,
                fontSize = 15.sp,
                iconSize = 18.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Faz 159 — KART YUKSEKLIGI DUSURULDU.
        //
        // Kullanicinin sikayeti: "gorevlerin kutu yukseklikleri cok fazla
        // buyuk, cok scroll gerekiyor." Kart dort katmandan olusuyordu:
        // baslik satiri / ilerleme cubugu / "0 / 3" KENDI satirinda / tam
        // genislikte TOPLA butonu.
        //
        // Birinci kazanc: ilerleme sayaci artik cubugun SAG UCUNDA, kendi
        // satirinda degil. Bir satir + iki bosluk kazanildi.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Faz 75: TEK cubuk, uzerinde ara-tier dugumleri.
            GameTieredProgressBar(
                fraction = overallFraction,
                tierMarkers = mission.tiers.dropLast(1).mapIndexed { tierIndex, tier ->
                    val position = (tier.target.toFloat() / maxTarget).coerceIn(0f, 1f)
                    val state = when {
                        tierIndex in claimedTiers -> GameTierState.CLAIMED
                        currentCount >= tier.target -> GameTierState.READY
                        else -> GameTierState.LOCKED
                    }
                    position to state
                },
                surfaces = surfaces,
                accent = role,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Ilerleme metni: ilerleme VARSA accent, yoksa sonuk. Hedef
            // mockup'ta "21 / 50" canli cyan, "0 / 3" gri.
            Text(
                text = if (allTiersClaimed) {
                    "${mission.tiers.last().target} / ${mission.tiers.last().target}"
                } else {
                    "${currentCount.coerceAtMost(activeTier.target)} / ${activeTier.target}"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = when {
                    allTiersClaimed -> surfaces.hairline
                    currentCount > 0 -> surfaces.accentText
                    else -> surfaces.hairline
                },
                maxLines = 1
            )
        }

        // Ikinci ve ASIL kazanc: TOPLA butonu artik SADECE toplanabilir
        // durumda ciziliyor.
        //
        // Onceden gorev devam ederken de tam genislikte, 46dp yuksekliginde
        // PASIF bir buton duruyordu — hicbir ise yaramiyor, sadece yer
        // kapliyordu. Bu ayni zamanda daha onceki bir sikayeti de kendiliginden
        // cozuyor ("TOPLA ucunde de ayni ve pasif, tamamlanmisla tamamlanmamis
        // ayirt edilmiyor"): artik BUTON VARSA toplanacak bir sey var demektir,
        // goz dogrudan oraya gidiyor.
        when {
            allTiersClaimed -> {
                // Alindi hali: buton DEGIL, kompakt bir rozet. Dokunulabilir
                // bir sey olmadigi icin buton gorunumu zaten yaniltiyordu.
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = GoldPillAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = language.pick(tr = "Alındı", en = "Claimed", it = "Riscosso", fr = "Réclamé", es = "Reclamado"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPillAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            isClaimable -> {
                // ODUL ANI: canli yesil, kabartmali, basinca cokuyor. Ekranda
                // TEK basina duran buton bu — hiyerarsi kendiliginden kuruluyor.
                Spacer(modifier = Modifier.height(10.dp))
                GameButton(
                    text = language.pick(tr = "TOPLA", en = "CLAIM", it = "RISCUOTI", fr = "RÉCLAMER", es = "RECLAMAR"),
                    onClick = { activeTierIndex?.let(onClaim) },
                    colors = gameButtonColors(NeonGreen),
                    fontSize = 15.sp,
                    minHeight = 48.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("claim_mission_${mission.id}")
                )
            }
            // Devam eden gorevde HICBIR SEY cizilmiyor: kart bir katman
            // eksiliyor, ekrana belirgin bicimde daha fazla gorev sigiyor.
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
                            text = "${tierIndex + 1}. ${mission.title(language, tier.target)} — ${tier.rewardTokens}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = tierStatus, fontSize = 12.sp)
                    }
                }
            }
        )
    }
}
