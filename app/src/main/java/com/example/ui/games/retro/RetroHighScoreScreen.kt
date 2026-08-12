package com.example.ui.games.retro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.retro.HighScore
import com.example.ui.theme.retro.ThemeColorPalette
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun RetroHighScoreScreen(
    scores: List<HighScore>,
    totalGamesPlayed: Int,
    totalLinesCleared: Int,
    highestScoreEver: Int,
    selectedDifficulty: String?,
    palette: ThemeColorPalette,
    onSelectDifficultyFilter: (String?) -> Unit,
    onBackToMenu: () -> Unit
) {
    val filteredScores = remember(scores, selectedDifficulty) {
        if (selectedDifficulty == null || selectedDifficulty == "ALL") {
            scores
        } else {
            scores.filter { it.difficultyMode == selectedDifficulty }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .background(palette.containerBackground, RoundedCornerShape(10.dp))
                        .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "HALL OF FAME",
                    color = palette.accentColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Career Stats Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.containerBackground, RoundedCornerShape(12.dp))
                    .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CareerStatItem(label = "CAREER BEST", value = "$highestScoreEver", palette = palette)
                CareerStatItem(label = "GAMES PLAYED", value = "$totalGamesPlayed", palette = palette)
                CareerStatItem(label = "TOTAL LINES", value = "$totalLinesCleared", palette = palette)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Difficulty Filter Tabs
            val difficultyTabs = listOf("ALL", "EASY", "NORMAL", "HARD", "CUSTOM")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.gridBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                difficultyTabs.forEach { tab ->
                    val isSelected = (selectedDifficulty == tab) || (tab == "ALL" && selectedDifficulty == null)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) palette.accentColor else Color.Transparent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = { onSelectDifficultyFilter(if (tab == "ALL") null else tab) }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else palette.textColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // High Scores List
            if (filteredScores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(palette.containerBackground, RoundedCornerShape(12.dp))
                        .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideogameAsset,
                            contentDescription = "No Scores",
                            tint = palette.textColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO HIGH SCORES YET!",
                            color = palette.textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Play a game to set the first record.",
                            color = palette.textColor.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filteredScores) { index, scoreItem ->
                        ScoreRowItem(
                            rank = index + 1,
                            score = scoreItem,
                            palette = palette
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerStatItem(
    label: String,
    value: String,
    palette: ThemeColorPalette
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = palette.textColor.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = palette.textColor,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ScoreRowItem(
    rank: Int,
    score: HighScore,
    palette: ThemeColorPalette
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> palette.buttonBackground
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.containerBackground, RoundedCornerShape(10.dp))
            .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(rankBadgeColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = if (rank <= 3) Color.Black else palette.buttonContentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = score.playerName,
                    color = palette.textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${score.linesCleared} Lines • Lvl ${score.levelReached} • ${score.difficultyMode}",
                    color = palette.textColor.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = "${score.score}",
            color = palette.accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
