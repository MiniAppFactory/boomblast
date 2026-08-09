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

// Faz 21: "skin" sistemi — rakip oyunlardaki gibi tum arayuzun zemin/kart
// tonunu birlikte degistiren, acik/koyu mod ayarindan bagimsiz sabit paletler
// (kullanici geri bildirimi: "Block Blast'ta skin degistirebiliyorsun").
// Vurgu renkleri (NeonCyan/NeonGold/vb.) marka tutarliligi icin tum skinlerde
// sabit kalir, sadece zemin/kart/metin katmanlari degisir.
val BlastForestPalette = BlastPalette(
    background = Color(0xFF14231A),
    card = Color(0xFF1F3327),
    cardAlt = Color(0xFF2A4433),
    cardBorder = Color(0xFF3B5942),
    textPrimary = Color(0xFFF1F5F0),
    textSecondary = Color(0xFFA8BFAE),
    emptyCell = Color(0xFF14231A)
)

val BlastOceanPalette = BlastPalette(
    background = Color(0xFF0B1F33),
    card = Color(0xFF123049),
    cardAlt = Color(0xFF1B4566),
    cardBorder = Color(0xFF2C5C82),
    textPrimary = Color.White,
    textSecondary = Color(0xFF9FC1DB),
    emptyCell = Color(0xFF0B1F33)
)

val BlastSunsetPalette = BlastPalette(
    background = Color(0xFF2E1A2E),
    card = Color(0xFF3E2440),
    cardAlt = Color(0xFF54305A),
    cardBorder = Color(0xFF6B3F63),
    textPrimary = Color(0xFFFFF3E8),
    textSecondary = Color(0xFFE0B08C),
    emptyCell = Color(0xFF2E1A2E)
)

val BlastPurpleNightPalette = BlastPalette(
    background = Color(0xFF160B2E),
    card = Color(0xFF221244),
    cardAlt = Color(0xFF33195E),
    cardBorder = Color(0xFF452380),
    textPrimary = Color.White,
    textSecondary = Color(0xFFC3A8E8),
    emptyCell = Color(0xFF160B2E)
)

enum class BlastSkin(val swatch: Color, val labelTr: String, val labelEn: String) {
    DEFAULT(Color(0xFF1E293B), "Varsayılan", "Default"),
    FOREST(Color(0xFF2A4433), "Orman", "Forest"),
    OCEAN(Color(0xFF1B4566), "Okyanus", "Ocean"),
    SUNSET(Color(0xFF54305A), "Gün Batımı", "Sunset"),
    PURPLE_NIGHT(Color(0xFF33195E), "Mor Gece", "Purple Night");

    companion object {
        fun fromId(id: String): BlastSkin = entries.find { it.name == id } ?: DEFAULT
    }
}

fun blastPalette(skin: BlastSkin, darkMode: Boolean): BlastPalette = when (skin) {
    BlastSkin.DEFAULT -> if (darkMode) BlastDarkPalette else BlastLightPalette
    BlastSkin.FOREST -> BlastForestPalette
    BlastSkin.OCEAN -> BlastOceanPalette
    BlastSkin.SUNSET -> BlastSunsetPalette
    BlastSkin.PURPLE_NIGHT -> BlastPurpleNightPalette
}
