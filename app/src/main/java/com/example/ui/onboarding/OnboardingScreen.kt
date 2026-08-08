package com.example.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.blastPalette

private data class OnboardingStep(
    val icon: String,
    val titleTr: String,
    val titleEn: String,
    val descriptionTr: String,
    val descriptionEn: String
)

private val onboardingSteps = listOf(
    OnboardingStep(
        icon = "🧩",
        titleTr = "PARÇALARI SÜRÜKLE",
        titleEn = "DRAG THE PIECES",
        descriptionTr = "Tepsideki bir bloğu seçip ızgaraya sürükle",
        descriptionEn = "Drag a block from the tray onto the grid"
    ),
    OnboardingStep(
        icon = "💥",
        titleTr = "SATIRI PATLAT",
        titleEn = "CLEAR THE LINE",
        descriptionTr = "Bir satırı veya sütunu tamamen doldurunca anında patlar",
        descriptionEn = "Fill an entire row or column and it instantly clears"
    ),
    OnboardingStep(
        icon = "🎯",
        titleTr = "HEDEFE ULAŞ",
        titleEn = "REACH THE TARGET",
        descriptionTr = "Her seviyenin bir hedef skoru var, ulaşınca seviye tamamlanır",
        descriptionEn = "Each level has a target score — reach it to complete the level"
    )
)

@Composable
fun OnboardingScreen(
    isTr: Boolean,
    darkMode: Boolean,
    onFinish: () -> Unit
) {
    val palette = blastPalette(darkMode)
    var currentStep by remember { mutableIntStateOf(0) }

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = palette.card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
                    .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onFinish,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = if (isTr) "Atla" else "Skip",
                                fontSize = 13.sp,
                                color = palette.textSecondary
                            )
                        }
                    }

                    val step = onboardingSteps[currentStep]

                    Text(text = step.icon, fontSize = 56.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isTr) step.titleTr else step.titleEn,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isTr) step.descriptionTr else step.descriptionEn,
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        onboardingSteps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (index == currentStep) NeonCyan else palette.cardAlt,
                                        shape = CircleShape
                                    )
                                    .testTag("onboarding_step_dot_$index")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentStep < onboardingSteps.lastIndex) {
                        Button(
                            onClick = { currentStep += 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text(
                                text = if (isTr) "İLERİ" else "NEXT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_start_button")
                        ) {
                            Text(
                                text = if (isTr) "BAŞLA" else "START",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
