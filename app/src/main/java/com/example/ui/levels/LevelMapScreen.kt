package com.example.ui.levels

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerProgress
import com.example.game.LevelGenerator
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPurple

// Dark/neon palette shared with BlockBlastGame.kt so this screen reads as part of the same app.
private val ScreenBg = Color(0xFF0F172A)
private val CardBg = Color(0xFF1E293B)
private val CardBgLocked = Color(0xFF161F2E)

@Composable
fun LevelMapScreen(
    progress: PlayerProgress,
    isTr: Boolean,
    onSelectLevel: (Int) -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val lastLevel = progress.highestUnlockedLevel + 3

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LevelMapHeader(
                progress = progress,
                isTr = isTr,
                onOpenMissions = onOpenMissions,
                onOpenSettings = onOpenSettings
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_map_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(count = lastLevel, key = { it + 1 }) { index ->
                    val levelNumber = index + 1
                    val unlocked = levelNumber <= progress.highestUnlockedLevel
                    val stars = progress.levelStars[levelNumber]
                    LevelCard(
                        levelNumber = levelNumber,
                        unlocked = unlocked,
                        stars = stars,
                        isTr = isTr,
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
    isTr: Boolean,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isTr) "SEVİYELER" else "LEVELS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan
        )

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
                    contentDescription = if (isTr) "Görevler" else "Missions",
                    tint = NeonPurple
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("level_map_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = if (isTr) "Ayarlar" else "Settings",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    levelNumber: Int,
    unlocked: Boolean,
    stars: Int?,
    isTr: Boolean,
    onClick: () -> Unit
) {
    val targetScore = LevelGenerator.forLevel(levelNumber).targetScore
    val borderBrush = if (unlocked) {
        Brush.linearGradient(listOf(NeonCyan, NeonPurple))
    } else {
        Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF334155)))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) CardBg else CardBgLocked
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderBrush, RoundedCornerShape(14.dp))
            .clickable(enabled = unlocked, onClick = onClick)
            .testTag("level_card_$levelNumber")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Level number badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (unlocked) NeonCyan.copy(alpha = 0.18f) else Color(0xFF1E293B)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (unlocked) {
                        Text(
                            text = "$levelNumber",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = if (isTr) "Kilitli" else "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isTr) "SEVİYE $levelNumber" else "LEVEL $levelNumber",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (unlocked) Color.White else Color.Gray
                    )
                    Text(
                        text = if (isTr) "Hedef: $targetScore" else "Target: $targetScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (unlocked) Color.Gray else Color.DarkGray
                    )
                }
            }

            if (unlocked) {
                StarRow(stars = stars ?: 0)
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = if (isTr) "Kilitli" else "Locked",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(20.dp)
                )
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
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
