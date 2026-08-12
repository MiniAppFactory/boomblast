package com.example.ui.games.retro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.retro.ThemeColorPalette

@Composable
fun ArcadeControlPanel(
    palette: ThemeColorPalette,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotateClockwise: () -> Unit,
    onRotateCounterClockwise: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    canHold: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.containerBackground, RoundedCornerShape(16.dp))
            .border(2.dp, palette.bezelBorderColor, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Top utility row: Hold button & Hard Drop button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArcadeRectButton(
                text = "HOLD",
                icon = Icons.Default.SwapHoriz,
                enabled = canHold,
                palette = palette,
                onClick = onHold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            ArcadeRectButton(
                text = "HARD DROP",
                icon = Icons.Default.KeyboardDoubleArrowDown,
                accent = true,
                palette = palette,
                onClick = onHardDrop,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Controls Row: D-Pad on Left, Action Buttons on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cross D-Pad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Up blank / placeholder for symmetrical alignment
                Box(modifier = Modifier.size(52.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ArcadeCircularButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Move Left",
                        palette = palette,
                        onClick = onMoveLeft,
                        sizeDp = 54
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    ArcadeCircularButton(
                        icon = Icons.Default.ArrowDownward,
                        contentDescription = "Soft Drop",
                        palette = palette,
                        onClick = onSoftDrop,
                        sizeDp = 54
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    ArcadeCircularButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Move Right",
                        palette = palette,
                        onClick = onMoveRight,
                        sizeDp = 54
                    )
                }
            }

            // Action Buttons (Rotate CCW, Rotate CW)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArcadeCircularButton(
                        icon = Icons.Default.RotateLeft,
                        contentDescription = "Rotate CCW",
                        palette = palette,
                        onClick = onRotateCounterClockwise,
                        sizeDp = 58,
                        buttonColor = palette.accentColor,
                        iconColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROTATE L",
                        fontSize = 10.sp,
                        color = palette.textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ArcadeCircularButton(
                        icon = Icons.Default.RotateRight,
                        contentDescription = "Rotate CW",
                        palette = palette,
                        onClick = onRotateClockwise,
                        sizeDp = 64,
                        buttonColor = palette.buttonContentColor,
                        iconColor = palette.background
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROTATE R",
                        fontSize = 10.sp,
                        color = palette.textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcadeCircularButton(
    icon: ImageVector,
    contentDescription: String,
    palette: ThemeColorPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 54,
    buttonColor: Color = palette.buttonBackground,
    iconColor: Color = palette.buttonContentColor
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(buttonColor)
            .border(2.dp, palette.bezelBorderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size((sizeDp * 0.55f).dp)
        )
    }
}

@Composable
private fun ArcadeRectButton(
    text: String,
    icon: ImageVector,
    palette: ThemeColorPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = false
) {
    val bgColor = if (!enabled) palette.buttonBackground.copy(alpha = 0.4f)
    else if (accent) palette.accentColor
    else palette.buttonBackground

    val textColor = if (!enabled) palette.textColor.copy(alpha = 0.3f)
    else if (accent) Color.White
    else palette.buttonContentColor

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.5.dp, if (accent) Color.White.copy(alpha = 0.5f) else palette.bezelBorderColor, RoundedCornerShape(8.dp))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
