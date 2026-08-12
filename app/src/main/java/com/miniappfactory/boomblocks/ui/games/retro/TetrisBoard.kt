package com.miniappfactory.boomblocks.ui.games.retro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.miniappfactory.boomblocks.data.retro.ControlStyle
import com.miniappfactory.boomblocks.game.retro.BOARD_WIDTH
import com.miniappfactory.boomblocks.game.retro.GameState
import com.miniappfactory.boomblocks.game.retro.TetrominoType
import com.miniappfactory.boomblocks.game.retro.VISIBLE_BOARD_HEIGHT
import com.miniappfactory.boomblocks.ui.theme.retro.ThemeColorPalette
import kotlin.math.abs

@Composable
fun TetrisBoard(
    gameState: GameState,
    palette: ThemeColorPalette,
    ghostEnabled: Boolean,
    controlStyle: ControlStyle,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accumulatedDragX by remember { mutableFloatStateOf(0f) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .aspectRatio(0.5f) // 10:20 ratio
            .background(palette.gridBackground, RoundedCornerShape(8.dp))
            .border(2.5.dp, palette.bezelBorderColor, RoundedCornerShape(8.dp))
            .padding(4.dp)
            .then(
                if (controlStyle == ControlStyle.TOUCH_GESTURES || controlStyle == ControlStyle.HYBRID) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onRotate() }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    accumulatedDragX = 0f
                                    accumulatedDragY = 0f
                                },
                                onDragEnd = {
                                    if (accumulatedDragY > 120f) {
                                        onHardDrop()
                                    }
                                    accumulatedDragX = 0f
                                    accumulatedDragY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedDragX += dragAmount.x
                                    accumulatedDragY += dragAmount.y

                                    val thresholdX = 35f
                                    val thresholdY = 35f

                                    if (abs(accumulatedDragX) > thresholdX) {
                                        if (accumulatedDragX > 0) {
                                            onMoveRight()
                                        } else {
                                            onMoveLeft()
                                        }
                                        accumulatedDragX = 0f
                                    } else if (accumulatedDragY > thresholdY) {
                                        onSoftDrop()
                                        accumulatedDragY = 0f
                                    }
                                }
                            )
                        }
                } else Modifier
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / BOARD_WIDTH
            val cellHeight = size.height / VISIBLE_BOARD_HEIGHT

            // 1. Draw Grid Lines
            drawGridLines(cellWidth, cellHeight, palette.gridLineColor)

            // 2. Draw Fixed Board Matrix (rows 2 to 21)
            for (boardR in 2 until 22) {
                val visibleR = boardR - 2
                for (c in 0 until BOARD_WIDTH) {
                    val cellVal = gameState.boardMatrix[boardR][c]
                    if (cellVal != 0) {
                        val pieceType = TetrominoType.fromId(cellVal)
                        val color = pieceType?.let { palette.pieceColors[it] } ?: palette.accentColor
                        drawCellBlock(
                            row = visibleR,
                            col = c,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            color = color
                        )
                    }
                }
            }

            // 3. Draw Ghost Piece Preview (if enabled and piece exists)
            val active = gameState.activePiece
            if (ghostEnabled && active != null && gameState.ghostY >= 2) {
                val ghostVisibleR = gameState.ghostY - 2
                for (r in active.shape.indices) {
                    for (c in active.shape[r].indices) {
                        if (active.shape[r][c] != 0) {
                            val targetR = ghostVisibleR + r
                            val targetC = active.x + c
                            if (targetR in 0 until VISIBLE_BOARD_HEIGHT && targetC in 0 until BOARD_WIDTH) {
                                drawGhostBlock(
                                    row = targetR,
                                    col = targetC,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    color = palette.ghostColor
                                )
                            }
                        }
                    }
                }
            }

            // 4. Draw Active Falling Piece
            if (active != null) {
                val pieceVisibleR = active.y - 2
                val color = palette.pieceColors[active.type] ?: palette.accentColor
                for (r in active.shape.indices) {
                    for (c in active.shape[r].indices) {
                        if (active.shape[r][c] != 0) {
                            val targetR = pieceVisibleR + r
                            val targetC = active.x + c
                            if (targetR in 0 until VISIBLE_BOARD_HEIGHT && targetC in 0 until BOARD_WIDTH) {
                                drawCellBlock(
                                    row = targetR,
                                    col = targetC,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // 5. Draw Subtle Retro Scanline Overlay
            drawScanlines(palette.gridLineColor.copy(alpha = 0.2f))
        }
    }
}

private fun DrawScope.drawGridLines(
    cellWidth: Float,
    cellHeight: Float,
    color: Color
) {
    // Vertical lines
    for (i in 1 until BOARD_WIDTH) {
        val x = i * cellWidth
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }
    // Horizontal lines
    for (j in 1 until VISIBLE_BOARD_HEIGHT) {
        val y = j * cellHeight
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }
}

private fun DrawScope.drawCellBlock(
    row: Int,
    col: Int,
    cellWidth: Float,
    cellHeight: Float,
    color: Color
) {
    val left = col * cellWidth
    val top = row * cellHeight

    // Fill main block
    drawRect(
        color = color,
        topLeft = Offset(left + 1f, top + 1f),
        size = Size(cellWidth - 2f, cellHeight - 2f)
    )

    // Inner 3D Highlight
    drawRect(
        color = Color.White.copy(alpha = 0.35f),
        topLeft = Offset(left + 2f, top + 2f),
        size = Size(cellWidth - 4f, cellHeight - 4f),
        style = Stroke(width = 2f)
    )

    // Shadow border bottom/right
    drawLine(
        color = Color.Black.copy(alpha = 0.4f),
        start = Offset(left + 1f, top + cellHeight - 2f),
        end = Offset(left + cellWidth - 2f, top + cellHeight - 2f),
        strokeWidth = 2f
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.4f),
        start = Offset(left + cellWidth - 2f, top + 1f),
        end = Offset(left + cellWidth - 2f, top + cellHeight - 2f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawGhostBlock(
    row: Int,
    col: Int,
    cellWidth: Float,
    cellHeight: Float,
    color: Color
) {
    val left = col * cellWidth
    val top = row * cellHeight

    drawRect(
        color = color,
        topLeft = Offset(left + 2f, top + 2f),
        size = Size(cellWidth - 4f, cellHeight - 4f),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawScanlines(color: Color) {
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += 4f
    }
}
