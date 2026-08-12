package com.miniappfactory.boomblocks.ui.theme.retro

import androidx.compose.ui.graphics.Color
import com.miniappfactory.boomblocks.data.retro.RetroThemeStyle
import com.miniappfactory.boomblocks.game.retro.TetrominoType

data class ThemeColorPalette(
    val background: Color,
    val containerBackground: Color,
    val gridBackground: Color,
    val gridLineColor: Color,
    val ghostColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val bezelBorderColor: Color,
    val buttonBackground: Color,
    val buttonContentColor: Color,
    val pieceColors: Map<TetrominoType, Color>
)

val NeonCyberpunkPalette = ThemeColorPalette(
    background = Color(0xFF080911),
    containerBackground = Color(0xFF121424),
    gridBackground = Color(0xFF0D0E1A),
    gridLineColor = Color(0xFF22263E),
    ghostColor = Color(0x5500E5FF),
    textColor = Color(0xFFE0E6FF),
    accentColor = Color(0xFFFF007A),
    bezelBorderColor = Color(0xFF2D3352),
    buttonBackground = Color(0xFF1E233C),
    buttonContentColor = Color(0xFF00E5FF),
    pieceColors = mapOf(
        TetrominoType.I to Color(0xFF00E5FF), // Cyan
        TetrominoType.J to Color(0xFF2979FF), // Blue
        TetrominoType.L to Color(0xFFFF9100), // Orange
        TetrominoType.O to Color(0xFFFFEA00), // Yellow
        TetrominoType.S to Color(0xFF00E676), // Green
        TetrominoType.T to Color(0xFFD500F9), // Purple / Magenta
        TetrominoType.Z to Color(0xFFFF1744)  // Red
    )
)

val GameBoyLcdPalette = ThemeColorPalette(
    background = Color(0xFF8BAC0F),
    containerBackground = Color(0xFF9BBC0F),
    gridBackground = Color(0xFF9BBC0F),
    gridLineColor = Color(0xFF8BAC0F),
    ghostColor = Color(0x66306230),
    textColor = Color(0xFF0F380F),
    accentColor = Color(0xFF0F380F),
    bezelBorderColor = Color(0xFF306230),
    buttonBackground = Color(0xFF306230),
    buttonContentColor = Color(0xFF9BBC0F),
    pieceColors = mapOf(
        TetrominoType.I to Color(0xFF0F380F),
        TetrominoType.J to Color(0xFF306230),
        TetrominoType.L to Color(0xFF0F380F),
        TetrominoType.O to Color(0xFF306230),
        TetrominoType.S to Color(0xFF0F380F),
        TetrominoType.T to Color(0xFF306230),
        TetrominoType.Z to Color(0xFF0F380F)
    )
)

val ArcadeCrtPalette = ThemeColorPalette(
    background = Color(0xFF101018),
    containerBackground = Color(0xFF1A1A28),
    gridBackground = Color(0xFF0C0C14),
    gridLineColor = Color(0xFF28283E),
    ghostColor = Color(0x44FFFFFF),
    textColor = Color(0xFF00FF66),
    accentColor = Color(0xFFFFD700),
    bezelBorderColor = Color(0xFF3A3A5A),
    buttonBackground = Color(0xFF222236),
    buttonContentColor = Color(0xFF00FF66),
    pieceColors = mapOf(
        TetrominoType.I to Color(0xFF00FFFF),
        TetrominoType.J to Color(0xFF0000FF),
        TetrominoType.L to Color(0xFFFFA500),
        TetrominoType.O to Color(0xFFFFFF00),
        TetrominoType.S to Color(0xFF00FF00),
        TetrominoType.T to Color(0xFFA020F0),
        TetrominoType.Z to Color(0xFFFF0000)
    )
)

val Monochrome8BitPalette = ThemeColorPalette(
    background = Color(0xFF000000),
    containerBackground = Color(0xFF121212),
    gridBackground = Color(0xFF000000),
    gridLineColor = Color(0xFF333333),
    ghostColor = Color(0x44FFFFFF),
    textColor = Color(0xFFFFFFFF),
    accentColor = Color(0xFFCCCCCC),
    bezelBorderColor = Color(0xFF555555),
    buttonBackground = Color(0xFF222222),
    buttonContentColor = Color(0xFFFFFFFF),
    pieceColors = mapOf(
        TetrominoType.I to Color(0xFFFFFFFF),
        TetrominoType.J to Color(0xFFDDDDDD),
        TetrominoType.L to Color(0xFFCCCCCC),
        TetrominoType.O to Color(0xFFFFFFFF),
        TetrominoType.S to Color(0xFFBBBBBB),
        TetrominoType.T to Color(0xFFEEEEEE),
        TetrominoType.Z to Color(0xFFAAAAAA)
    )
)

val NesSynthPalette = ThemeColorPalette(
    background = Color(0xFF180A28),
    containerBackground = Color(0xFF2D1245),
    gridBackground = Color(0xFF120520),
    gridLineColor = Color(0xFF4A206B),
    ghostColor = Color(0x55FF77A9),
    textColor = Color(0xFFFFCC00),
    accentColor = Color(0xFFFF77A9),
    bezelBorderColor = Color(0xFF6B2B90),
    buttonBackground = Color(0xFF3E185D),
    buttonContentColor = Color(0xFFFFCC00),
    pieceColors = mapOf(
        TetrominoType.I to Color(0xFF00F0F0),
        TetrominoType.J to Color(0xFF0000F0),
        TetrominoType.L to Color(0xFFF0A000),
        TetrominoType.O to Color(0xFFF0F000),
        TetrominoType.S to Color(0xFF00F000),
        TetrominoType.T to Color(0xFFA000F0),
        TetrominoType.Z to Color(0xFFF00000)
    )
)

fun getThemePalette(style: RetroThemeStyle): ThemeColorPalette {
    return when (style) {
        RetroThemeStyle.NEON_CYBERPUNK -> NeonCyberpunkPalette
        RetroThemeStyle.GAME_BOY_LCD -> GameBoyLcdPalette
        RetroThemeStyle.ARCADE_CRT -> ArcadeCrtPalette
        RetroThemeStyle.MONOCHROME_8BIT -> Monochrome8BitPalette
        RetroThemeStyle.NES_SYNTH -> NesSynthPalette
    }
}
