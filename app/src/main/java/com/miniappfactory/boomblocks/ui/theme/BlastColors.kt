package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.ui.graphics.Color
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick

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

// Faz 157: Varsayilan (aktif) skin'in tahtasi Block Blast referansina gore
// cok koyu lacivertti (neredeyse siyah), bloklar zeminden "patlamiyordu".
// Referansta tahta ORTA-acik bir mavi ve izgara cizgileri gorunuyor. Tahta
// karti + hucre ici acildi; DIS `background` (tahta disi alan) kasitli olarak
// biraz daha koyu tutuldu ki tahta ondan AYRISIP one ciksin. Blok renkleri
// ayri (BLOCK_COLORS) — burada degismedi.
//   card      1E293B -> 25344F  (tahta cercevesi, daha acik mavi-gri)
//   emptyCell 0F172A -> 1C2B48  (hucre ici; izgara artik gorunur)
//   cardAlt   334155 -> 3B4E70  (hucre kenari/kabartma ust tonu)
val BlastDarkPalette = BlastPalette(
    background = Color(0xFF101B30),
    card = Color(0xFF25344F),
    cardAlt = Color(0xFF3B4E70),
    cardBorder = Color(0xFF3B4E70),
    textPrimary = Color.White,
    textSecondary = Color.Gray,
    emptyCell = Color(0xFF1C2B48)
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

// Faz 130: "Şeker Pembesi" gorunumu (kullanici istegi). Oyunun tum gorunumleri
// KOYU zeminli — acik pembe bir zemin butun arayuzu (beyaz metin, neon vurgular,
// blok kontrastı) bozardi. O yuzden zemin koyu gul/bordo, "seker pembesi" ise
// vurgu gradyaninda ve metin renklerinde tasiniyor: SUNSET ile ayni mantik,
// ama mor yerine tamamen pembe eksende.
val BlastCandyPinkPalette = BlastPalette(
    background = Color(0xFF2B0F1E),
    card = Color(0xFF3D1730),
    cardAlt = Color(0xFF5A2140),
    cardBorder = Color(0xFF7A2E56),
    textPrimary = Color(0xFFFFF0F6),
    textSecondary = Color(0xFFF2A8C8),
    emptyCell = Color(0xFF2B0F1E)
)

enum class BlastSkin(
    val swatch: Color,
    val labelTr: String,
    val labelEn: String,
    val labelIt: String,
    val labelFr: String,
    val labelEs: String,
    // Faz 22: grid cercevesi ve ambient zemin ısıması onceden TUM skinlerde sabit
    // cyan-mor kalıyordu — Orman/Okyanus gibi skinlerde bile ayni cercevenin kalmasi
    // skin'in "yarim" hissettirmesine yol aciyordu. Her skin artik kendi vurgu
    // gradyanini tasiyor.
    val accentGradient: List<Color>
) {
    DEFAULT(Color(0xFF1E293B), "Varsayılan", "Default", "Predefinito", "Par défaut", "Predeterminado", listOf(NeonCyan, NeonPurple)),
    FOREST(Color(0xFF2A4433), "Orman", "Forest", "Foresta", "Forêt", "Bosque", listOf(Color(0xFF34D399), Color(0xFF059669))),
    OCEAN(Color(0xFF1B4566), "Okyanus", "Ocean", "Oceano", "Océan", "Océano", listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9))),
    SUNSET(Color(0xFF54305A), "Gün Batımı", "Sunset", "Tramonto", "Coucher de Soleil", "Atardecer", listOf(Color(0xFFF97316), Color(0xFFEC4899))),
    PURPLE_NIGHT(Color(0xFF33195E), "Mor Gece", "Purple Night", "Notte Viola", "Nuit Violette", "Noche Púrpura", listOf(Color(0xFFA78BFA), Color(0xFF7C3AED))),
    // Faz 130: swatch her zaman o paletin cardAlt'i (digerlerinde de oyle).
    CANDY_PINK(Color(0xFF5A2140), "Şeker Pembesi", "Candy Pink", "Rosa Caramella", "Rose Bonbon", "Rosa Caramelo", listOf(Color(0xFFFF8FC7), Color(0xFFEC4899)));

    fun label(language: AppLanguage): String =
        language.pick(tr = labelTr, en = labelEn, it = labelIt, fr = labelFr, es = labelEs)

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
    BlastSkin.CANDY_PINK -> BlastCandyPinkPalette
}
