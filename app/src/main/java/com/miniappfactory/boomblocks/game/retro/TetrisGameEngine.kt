package com.miniappfactory.boomblocks.game.retro

import com.miniappfactory.boomblocks.data.retro.DifficultyPreset
import com.miniappfactory.boomblocks.data.retro.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TetrisGameEngine {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val bag = mutableListOf<TetrominoType>()
    private val nextQueue = mutableListOf<TetrominoType>()
    private var highScoresThreshold = 0

    // Callback event listener for audio/haptics sound effects
    var onSoundEvent: ((SoundEffect) -> Unit)? = null

    enum class SoundEffect {
        MOVE,
        ROTATE,
        SOFT_DROP,
        HARD_DROP,
        HOLD,
        SINGLE_CLEAR,
        DOUBLE_CLEAR,
        TRIPLE_CLEAR,
        TETRIS_CLEAR,
        GAME_OVER,
        LEVEL_UP
    }

    fun startGame(settings: GameSettings, highestScoreEver: Int = 0) {
        highScoresThreshold = highestScoreEver
        val matrix = Array(BOARD_HEIGHT) { IntArray(BOARD_WIDTH) }
        bag.clear()
        nextQueue.clear()

        // Fill initial queue with 5 pieces
        fillNextQueue()

        val firstPieceType = pullFromQueue()
        val firstPiece = createPiece(firstPieceType)

        val initialLevel = when (settings.difficultyPreset) {
            DifficultyPreset.EASY -> settings.startingLevel.coerceAtMost(3)
            DifficultyPreset.NORMAL -> settings.startingLevel
            DifficultyPreset.HARD -> maxOf(5, settings.startingLevel)
        }

        val initialState = GameState(
            status = GameStatus.PLAYING,
            boardMatrix = matrix,
            activePiece = firstPiece,
            ghostY = calculateGhostY(firstPiece, matrix),
            nextPieces = nextQueue.toList(),
            holdPiece = null,
            canHold = true,
            score = 0,
            lines = 0,
            level = initialLevel,
            comboCount = 0,
            isBackToBack = false,
            lastLineClearEvent = null,
            gameDurationSeconds = 0,
            isNewHighScore = false
        )

        _gameState.value = initialState
    }

    fun pauseGame() {
        val current = _gameState.value
        if (current.status == GameStatus.PLAYING) {
            _gameState.value = current.copy(status = GameStatus.PAUSED)
        }
    }

    fun resumeGame() {
        val current = _gameState.value
        if (current.status == GameStatus.PAUSED) {
            _gameState.value = current.copy(status = GameStatus.PLAYING)
        }
    }

    fun tickDuration() {
        val current = _gameState.value
        if (current.status == GameStatus.PLAYING) {
            _gameState.value = current.copy(
                gameDurationSeconds = current.gameDurationSeconds + 1
            )
        }
    }

    fun tickDrop(): Boolean {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return false

        val piece = current.activePiece
        val nextY = piece.y + 1

        if (canPositionPiece(piece.shape, piece.x, nextY, current.boardMatrix)) {
            val updatedPiece = piece.copy(y = nextY)
            _gameState.value = current.copy(
                activePiece = updatedPiece,
                ghostY = calculateGhostY(updatedPiece, current.boardMatrix)
            )
            return true
        } else {
            // Lock piece in place
            lockPiece()
            return false
        }
    }

    fun moveLeft() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return
        val piece = current.activePiece
        if (canPositionPiece(piece.shape, piece.x - 1, piece.y, current.boardMatrix)) {
            val updated = piece.copy(x = piece.x - 1)
            _gameState.value = current.copy(
                activePiece = updated,
                ghostY = calculateGhostY(updated, current.boardMatrix)
            )
            onSoundEvent?.invoke(SoundEffect.MOVE)
        }
    }

    fun moveRight() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return
        val piece = current.activePiece
        if (canPositionPiece(piece.shape, piece.x + 1, piece.y, current.boardMatrix)) {
            val updated = piece.copy(x = piece.x + 1)
            _gameState.value = current.copy(
                activePiece = updated,
                ghostY = calculateGhostY(updated, current.boardMatrix)
            )
            onSoundEvent?.invoke(SoundEffect.MOVE)
        }
    }

    fun softDrop() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return
        val piece = current.activePiece
        if (canPositionPiece(piece.shape, piece.x, piece.y + 1, current.boardMatrix)) {
            val updated = piece.copy(y = piece.y + 1)
            _gameState.value = current.copy(
                activePiece = updated,
                score = current.score + 1,
                ghostY = calculateGhostY(updated, current.boardMatrix)
            )
            onSoundEvent?.invoke(SoundEffect.SOFT_DROP)
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return
        val piece = current.activePiece
        val ghostY = calculateGhostY(piece, current.boardMatrix)
        val dropDistance = ghostY - piece.y
        val dropScoreBonus = dropDistance * 2

        val droppedPiece = piece.copy(y = ghostY)
        _gameState.value = current.copy(
            activePiece = droppedPiece,
            score = current.score + dropScoreBonus
        )
        onSoundEvent?.invoke(SoundEffect.HARD_DROP)
        lockPiece()
    }

    fun rotateClockwise() {
        rotatePiece(clockwise = true)
    }

    fun rotateCounterClockwise() {
        rotatePiece(clockwise = false)
    }

    private fun rotatePiece(clockwise: Boolean) {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null) return
        val piece = current.activePiece

        // Square O pieces don't need rotation calculations
        if (piece.type == TetrominoType.O) return

        val rotatedShape = rotateMatrix(piece.shape, clockwise)
        val newRotation = if (clockwise) (piece.rotation + 1) % 4 else (piece.rotation + 3) % 4

        // Wall kick offset candidates
        val kicks = arrayOf(
            Pair(0, 0),
            Pair(-1, 0),
            Pair(1, 0),
            Pair(0, -1),
            Pair(-2, 0),
            Pair(2, 0),
            Pair(0, 1)
        )

        for ((dx, dy) in kicks) {
            val testX = piece.x + dx
            val testY = piece.y + dy
            if (canPositionPiece(rotatedShape, testX, testY, current.boardMatrix)) {
                val updatedPiece = piece.copy(
                    shape = rotatedShape,
                    x = testX,
                    y = testY,
                    rotation = newRotation
                )
                _gameState.value = current.copy(
                    activePiece = updatedPiece,
                    ghostY = calculateGhostY(updatedPiece, current.boardMatrix)
                )
                onSoundEvent?.invoke(SoundEffect.ROTATE)
                return
            }
        }
    }

    fun holdPiece() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || current.activePiece == null || !current.canHold) return

        val activeType = current.activePiece.type
        val heldType = current.holdPiece

        val newActiveType = heldType ?: pullFromQueue()
        val newActivePiece = createPiece(newActiveType)

        if (!canPositionPiece(newActivePiece.shape, newActivePiece.x, newActivePiece.y, current.boardMatrix)) {
            triggerGameOver()
            return
        }

        _gameState.value = current.copy(
            activePiece = newActivePiece,
            ghostY = calculateGhostY(newActivePiece, current.boardMatrix),
            holdPiece = activeType,
            canHold = false,
            nextPieces = nextQueue.toList()
        )
        onSoundEvent?.invoke(SoundEffect.HOLD)
    }

    private fun lockPiece() {
        val current = _gameState.value
        val piece = current.activePiece ?: return
        val matrix = copyMatrix(current.boardMatrix)

        // Write piece blocks to matrix
        var toppedOut = false
        for (r in piece.shape.indices) {
            for (c in piece.shape[r].indices) {
                if (piece.shape[r][c] != 0) {
                    val boardR = piece.y + r
                    val boardC = piece.x + c
                    if (boardR in 0 until BOARD_HEIGHT && boardC in 0 until BOARD_WIDTH) {
                        matrix[boardR][boardC] = piece.type.id
                        if (boardR < 2) {
                            toppedOut = true
                        }
                    } else {
                        toppedOut = true
                    }
                }
            }
        }

        if (toppedOut) {
            _gameState.value = current.copy(boardMatrix = matrix)
            triggerGameOver()
            return
        }

        // Check full rows
        val completedRows = mutableListOf<Int>()
        for (r in 0 until BOARD_HEIGHT) {
            var isFull = true
            for (c in 0 until BOARD_WIDTH) {
                if (matrix[r][c] == 0) {
                    isFull = false
                    break
                }
            }
            if (isFull) {
                completedRows.add(r)
            }
        }

        val linesClearedCount = completedRows.size
        var newScore = current.score
        var newLines = current.lines
        var newLevel = current.level
        var newCombo = current.comboCount
        var newIsB2B = current.isBackToBack
        var lineClearEvent: LineClearEvent? = null

        if (linesClearedCount > 0) {
            // Remove full rows
            for (r in completedRows) {
                for (rowToMove in r downTo 1) {
                    matrix[rowToMove] = matrix[rowToMove - 1].copyOf()
                }
                matrix[0] = IntArray(BOARD_WIDTH)
            }

            newLines += linesClearedCount
            newCombo += 1

            val isTetris = linesClearedCount == 4
            val isBackToBack = isTetris && newIsB2B

            // Score formula
            val baseScore = when (linesClearedCount) {
                1 -> 100 * current.level
                2 -> 300 * current.level
                3 -> 500 * current.level
                4 -> 800 * current.level
                else -> 0
            }

            val b2bBonus = if (isBackToBack) (baseScore * 0.5).toInt() else 0
            val comboBonus = if (newCombo > 1) 50 * (newCombo - 1) * current.level else 0

            val roundScoreGained = baseScore + b2bBonus + comboBonus
            newScore += roundScoreGained

            newIsB2B = isTetris

            // Level up every 10 lines
            val calculatedLevel = (newLines / 10) + current.level
            if (calculatedLevel > newLevel) {
                newLevel = calculatedLevel
                onSoundEvent?.invoke(SoundEffect.LEVEL_UP)
            }

            // Play sound event
            val sound = when (linesClearedCount) {
                1 -> SoundEffect.SINGLE_CLEAR
                2 -> SoundEffect.DOUBLE_CLEAR
                3 -> SoundEffect.TRIPLE_CLEAR
                4 -> SoundEffect.TETRIS_CLEAR
                else -> SoundEffect.SINGLE_CLEAR
            }
            onSoundEvent?.invoke(sound)

            lineClearEvent = LineClearEvent(
                linesCleared = linesClearedCount,
                isTetris = isTetris,
                isBackToBack = isBackToBack,
                comboCount = newCombo,
                scoreGained = roundScoreGained
            )
        } else {
            newCombo = 0
        }

        // Spawn next piece
        val nextType = pullFromQueue()
        val nextPiece = createPiece(nextType)

        if (!canPositionPiece(nextPiece.shape, nextPiece.x, nextPiece.y, matrix)) {
            _gameState.value = current.copy(boardMatrix = matrix)
            triggerGameOver()
            return
        }

        val checkNewHigh = newScore > highScoresThreshold && newScore > 0

        _gameState.value = current.copy(
            boardMatrix = matrix,
            activePiece = nextPiece,
            ghostY = calculateGhostY(nextPiece, matrix),
            nextPieces = nextQueue.toList(),
            canHold = true,
            score = newScore,
            lines = newLines,
            level = newLevel,
            comboCount = newCombo,
            isBackToBack = newIsB2B,
            lastLineClearEvent = lineClearEvent,
            isNewHighScore = checkNewHigh
        )
    }

    private fun triggerGameOver() {
        val current = _gameState.value
        _gameState.value = current.copy(
            status = GameStatus.GAME_OVER,
            activePiece = null
        )
        onSoundEvent?.invoke(SoundEffect.GAME_OVER)
    }

    private fun fillNextQueue() {
        while (nextQueue.size < 5) {
            if (bag.isEmpty()) {
                bag.addAll(TetrominoType.entries.shuffled())
            }
            nextQueue.add(bag.removeAt(0))
        }
    }

    private fun pullFromQueue(): TetrominoType {
        fillNextQueue()
        val next = nextQueue.removeAt(0)
        fillNextQueue()
        return next
    }

    private fun createPiece(type: TetrominoType): ActivePiece {
        val rawShape = TETROMINO_SHAPES[type] ?: TETROMINO_SHAPES[TetrominoType.I]!!
        val shape = copy2DArray(rawShape)
        val startX = (BOARD_WIDTH - shape[0].size) / 2
        val startY = 0
        return ActivePiece(type = type, shape = shape, x = startX, y = startY)
    }

    private fun calculateGhostY(piece: ActivePiece, matrix: Array<IntArray>): Int {
        var ghostY = piece.y
        while (canPositionPiece(piece.shape, piece.x, ghostY + 1, matrix)) {
            ghostY++
        }
        return ghostY
    }

    private fun canPositionPiece(
        shape: Array<IntArray>,
        posX: Int,
        posY: Int,
        matrix: Array<IntArray>
    ): Boolean {
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val boardR = posY + r
                    val boardC = posX + c

                    // Wall boundaries
                    if (boardC < 0 || boardC >= BOARD_WIDTH || boardR >= BOARD_HEIGHT) {
                        return false
                    }

                    // Collision with placed blocks
                    if (boardR >= 0 && matrix[boardR][boardC] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun rotateMatrix(shape: Array<IntArray>, clockwise: Boolean): Array<IntArray> {
        val rows = shape.size
        val cols = shape[0].size
        val result = Array(cols) { IntArray(rows) }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (clockwise) {
                    result[c][rows - 1 - r] = shape[r][c]
                } else {
                    result[cols - 1 - c][r] = shape[r][c]
                }
            }
        }
        return result
    }

    private fun copyMatrix(matrix: Array<IntArray>): Array<IntArray> {
        return Array(matrix.size) { r -> matrix[r].copyOf() }
    }

    private fun copy2DArray(arr: Array<IntArray>): Array<IntArray> {
        return Array(arr.size) { r -> arr[r].copyOf() }
    }
}
