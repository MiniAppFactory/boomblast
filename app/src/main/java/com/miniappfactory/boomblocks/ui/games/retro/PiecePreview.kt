package com.miniappfactory.boomblocks.ui.games.retro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.game.retro.TETROMINO_SHAPES
import com.miniappfactory.boomblocks.game.retro.TetrominoType
import com.miniappfactory.boomblocks.ui.theme.retro.ThemeColorPalette

@Composable
fun PiecePreviewBox(
    title: String,
    pieceType: TetrominoType?,
    palette: ThemeColorPalette,
    modifier: Modifier = Modifier,
    boxSize: Dp = 72.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            color = palette.textColor.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(boxSize)
                .background(palette.containerBackground, RoundedCornerShape(6.dp))
                .border(1.5.dp, palette.bezelBorderColor, RoundedCornerShape(6.dp))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pieceType != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawPiecePreview(pieceType, palette)
                }
            }
        }
    }
}

private fun DrawScope.drawPiecePreview(
    type: TetrominoType,
    palette: ThemeColorPalette
) {
    val shape = TETROMINO_SHAPES[type] ?: return
    val color = palette.pieceColors[type] ?: palette.accentColor

    val shapeRows = shape.size
    val shapeCols = shape[0].size

    var maxOccupiedR = 0
    var minOccupiedR = shapeRows
    var maxOccupiedC = 0
    var minOccupiedC = shapeCols

    for (r in 0 until shapeRows) {
        for (c in 0 until shapeCols) {
            if (shape[r][c] != 0) {
                if (r < minOccupiedR) minOccupiedR = r
                if (r > maxOccupiedR) maxOccupiedR = r
                if (c < minOccupiedC) minOccupiedC = c
                if (c > maxOccupiedC) maxOccupiedC = c
            }
        }
    }

    val occRows = maxOccupiedR - minOccupiedR + 1
    val occCols = maxOccupiedC - minOccupiedC + 1

    val cellSize = minOf(
        size.width / (occCols + 0.6f),
        size.height / (occRows + 0.6f)
    )

    val startX = (size.width - (occCols * cellSize)) / 2f
    val startY = (size.height - (occRows * cellSize)) / 2f

    for (r in minOccupiedR..maxOccupiedR) {
        for (c in minOccupiedC..maxOccupiedC) {
            if (shape[r][c] != 0) {
                val left = startX + (c - minOccupiedC) * cellSize
                val top = startY + (r - minOccupiedR) * cellSize

                drawRect(
                    color = color,
                    topLeft = Offset(left + 1f, top + 1f),
                    size = Size(cellSize - 2f, cellSize - 2f)
                )

                // Bevel / inner border for retro 3D feel
                drawRect(
                    color = Color.White.copy(alpha = 0.35f),
                    topLeft = Offset(left + 2f, top + 2f),
                    size = Size(cellSize - 4f, cellSize - 4f),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}
