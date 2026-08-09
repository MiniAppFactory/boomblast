package com.example.ui.missions

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import com.example.data.WeeklyMissionDef
import com.example.data.WeeklyMissionProgress
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.blastPalette

@Composable
fun MissionsScreen(
    missionProgress: WeeklyMissionProgress,
    isTr: Boolean,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onClaim: (String) -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val allClaimed = missionProgress.missions.isNotEmpty() &&
        missionProgress.missions.all { it.id in missionProgress.claimed }

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
                    text = if (isTr) "HAFTALIK GÖREVLER" else "WEEKLY MISSIONS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan
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
                            text = if (isTr) {
                                "Bu haftaki tüm görevler tamamlandı!"
                            } else {
                                "All missions completed this week!"
                            },
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
                        isClaimed = mission.id in missionProgress.claimed,
                        isTr = isTr,
                        palette = palette,
                        onClaim = { onClaim(mission.id) }
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
    isClaimed: Boolean,
    isTr: Boolean,
    palette: BlastPalette,
    onClaim: () -> Unit
) {
    val target = if (mission.target > 0) mission.target else 1
    val progressFraction = (currentCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val isComplete = currentCount >= mission.target
    val isClaimable = isComplete && !isClaimed

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${mission.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTr) mission.titleTr else mission.titleEn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = palette.textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mission.rewardTokens}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isComplete) NeonGreen else NeonCyan,
                trackColor = palette.cardAlt
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${currentCount.coerceAtMost(mission.target)} / ${mission.target}",
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
                                text = if (isTr) "Alındı" else "Claimed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textSecondary
                            )
                        }
                    }
                }

                isClaimable -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_mission_${mission.id}")
                    ) {
                        Text(
                            text = if (isTr) "TOPLA" else "CLAIM",
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
                            text = if (isTr) "TOPLA" else "CLAIM",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}
