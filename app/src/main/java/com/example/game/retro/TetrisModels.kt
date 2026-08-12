package com.example.game.retro

enum class TetrominoType(val id: Int) {
    I(1),
    J(2),
    L(3),
    O(4),
    S(5),
    T(6),
    Z(7);

    companion object {
        fun fromId(id: Int): TetrominoType? = entries.find { it.id == id }
    }
}

/**
 * 4x4 matrix representation of pieces in their default orientation (0 degrees).
 */
val TETROMINO_SHAPES = mapOf(
    TetrominoType.I to arrayOf(
        intArrayOf(0, 0, 0, 0),
        intArrayOf(1, 1, 1, 1),
        intArrayOf(0, 0, 0, 0),
        intArrayOf(0, 0, 0, 0)
    ),
    TetrominoType.J to arrayOf(
        intArrayOf(1, 0, 0),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    ),
    TetrominoType.L to arrayOf(
        intArrayOf(0, 0, 1),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    ),
    TetrominoType.O to arrayOf(
        intArrayOf(1, 1),
        intArrayOf(1, 1)
    ),
    TetrominoType.S to arrayOf(
        intArrayOf(0, 1, 1),
        intArrayOf(1, 1, 0),
        intArrayOf(0, 0, 0)
    ),
    TetrominoType.T to arrayOf(
        intArrayOf(0, 1, 0),
        intArrayOf(1, 1, 1),
        intArrayOf(0, 0, 0)
    ),
    TetrominoType.Z to arrayOf(
        intArrayOf(1, 1, 0),
        intArrayOf(0, 1, 1),
        intArrayOf(0, 0, 0)
    )
)

data class ActivePiece(
    val type: TetrominoType,
    val shape: Array<IntArray>,
    val x: Int,
    val y: Int,
    val rotation: Int = 0 // 0, 1, 2, 3
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActivePiece) return false
        if (type != other.type) return false
        if (x != other.x || y != other.y || rotation != other.rotation) return false
        return shape.contentDeepEquals(other.shape)
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + shape.contentDeepHashCode()
        result = 31 * result + x
        result = 31 * result + y
        result = 31 * result + rotation
        return result
    }
}

enum class GameStatus {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class LineClearEvent(
    val linesCleared: Int,
    val isTetris: Boolean,
    val isBackToBack: Boolean,
    val comboCount: Int,
    val scoreGained: Int
)

data class GameState(
    val status: GameStatus = GameStatus.READY,
    val boardMatrix: Array<IntArray> = Array(BOARD_HEIGHT) { IntArray(BOARD_WIDTH) },
    val activePiece: ActivePiece? = null,
    val ghostY: Int = 0,
    val nextPieces: List<TetrominoType> = emptyList(),
    val holdPiece: TetrominoType? = null,
    val canHold: Boolean = true,
    val score: Int = 0,
    val lines: Int = 0,
    val level: Int = 1,
    val comboCount: Int = 0,
    val isBackToBack: Boolean = false,
    val lastLineClearEvent: LineClearEvent? = null,
    val gameDurationSeconds: Int = 0,
    val isNewHighScore: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        if (status != other.status || score != other.score || lines != other.lines || level != other.level) return false
        if (activePiece != other.activePiece || ghostY != other.ghostY || holdPiece != other.holdPiece || canHold != other.canHold) return false
        if (nextPieces != other.nextPieces || comboCount != other.comboCount || isBackToBack != other.isBackToBack) return false
        if (gameDurationSeconds != other.gameDurationSeconds || isNewHighScore != other.isNewHighScore) return false
        return boardMatrix.contentDeepEquals(other.boardMatrix)
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + boardMatrix.contentDeepHashCode()
        result = 31 * result + (activePiece?.hashCode() ?: 0)
        result = 31 * result + ghostY
        result = 31 * result + nextPieces.hashCode()
        result = 31 * result + (holdPiece?.hashCode() ?: 0)
        result = 31 * result + canHold.hashCode()
        result = 31 * result + score
        result = 31 * result + lines
        result = 31 * result + level
        return result
    }
}

const val BOARD_WIDTH = 10
const val BOARD_HEIGHT = 22 // 20 visible + 2 top buffer
const val VISIBLE_BOARD_HEIGHT = 20
