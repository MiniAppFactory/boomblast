package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Faz 5: acik/koyu mod icin ekranlarin okudugu ortak "token" paleti.
// Neon vurgu renkleri (NeonCyan/NeonGold/NeonPurple/vb.) her iki modda da
// ayni kalir, degisen sadece zemin/kart/metin katmanlari.
data class BlastPalette(
    val background: Color,
    val card: Color,
    val cardAlt: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val emptyCell: Color
)

val BlastDarkPalette = BlastPalette(
    background = Color(0xFF0F172A),
    card = Color(0xFF1E293B),
    cardAlt = Color(0xFF334155),
    cardBorder = Color(0xFF334155),
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    emptyCell = Color(0xFF0F172A)
)

val BlastLightPalette = BlastPalette(
    background = KronLightBg,
    card = KronLightCard,
    cardAlt = Color(0xFFE2E8F0),
    cardBorder = Color(0xFFCBD5E1),
    textPrimary = KronLightText,
    textSecondary = KronLightMuted,
    emptyCell = Color(0xFFE2E8F0)
)

fun blastPalette(darkMode: Boolean): BlastPalette = if (darkMode) BlastDarkPalette else BlastLightPalette
