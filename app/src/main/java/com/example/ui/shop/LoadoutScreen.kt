package com.example.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BoosterType
import com.example.data.PlayerProgress
import com.example.ui.theme.BlastPalette
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.blastPalette

/**
 * Pre-level loadout screen: lets the player review the level target, spend
 * Game Tokens on boosters, watch an ad for bonus tokens, then start the level.
 */
@Composable
fun LoadoutScreen(
    levelNumber: Int,
    targetScore: Int,
    progress: PlayerProgress,
    isTr: Boolean,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onBuyBooster: (BoosterType) -> Unit,
    onWatchAdForTokens: () -> Unit,
    onStartLevel: () -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("loadout_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                // Faz 25: sabit-genislik varsayan 3-parcali SpaceBetween Row'lari dar
                // ekranlarda komsu elemanlarla cakisiyordu (Level Map'te ayni desen
                // bulundu ve duzeltildi) — orta sutun artik kalan alani paylasip
                // gerekirse metni kisaltiyor, jeton kapsulunden asla alan calmiyor.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isTr) "SEVİYE $levelNumber" else "LEVEL $levelNumber",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isTr) "HEDEF: $targetScore" else "TARGET: $targetScore",
                        fontSize = 11.sp,
                        color = palette.textSecondary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = NeonGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${progress.tokens}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isTr) "TAKIMINI HAZIRLA" else "PREPARE YOUR LOADOUT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BoosterType.entries.forEach { type ->
                    BoosterCard(
                        type = type,
                        owned = progress.ownedBoosters[type] ?: 0,
                        canAfford = progress.tokens >= type.tokenPrice,
                        isTr = isTr,
                        palette = palette,
                        onBuy = { onBuyBooster(type) }
                    )
                }

                WatchAdCard(isTr = isTr, palette = palette, onWatchAdForTokens = onWatchAdForTokens)

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Start Level Button
            Button(
                onClick = onStartLevel,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonPurple)), RoundedCornerShape(14.dp))
                    .testTag("loadout_start_button")
            ) {
                Text(
                    text = if (isTr) "BAŞLA" else "START",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun BoosterCard(
    type: BoosterType,
    owned: Int,
    canAfford: Boolean,
    isTr: Boolean,
    palette: BlastPalette,
    onBuy: () -> Unit
) {
    val icon = when (type) {
        BoosterType.BOMB -> "💣"
        BoosterType.LINE_CLEAR -> "⚡"
        BoosterType.SHUFFLE -> "🔀"
    }
    // Uc guclendirici butonu birbirinden ayirt edilemiyordu (hepsi ayni mavi) —
    // UI/UX karsilastirma bulgusu. Her tur artik kendi vurgu rengini tasiyor,
    // ikonun anlamiyla eslesecek sekilde (bomba->kirmizi/turuncu, simsek->altin,
    // karistir->mor).
    val accent = when (type) {
        BoosterType.BOMB -> Color(0xFFFF6B35)
        BoosterType.LINE_CLEAR -> NeonGold
        BoosterType.SHUFFLE -> NeonPurple
    }
    val name = when (type) {
        BoosterType.BOMB -> if (isTr) "BOMBA" else "BOMB"
        BoosterType.LINE_CLEAR -> if (isTr) "SATIR TEMİZLE" else "LINE CLEAR"
        BoosterType.SHUFFLE -> if (isTr) "KARIŞTIR" else "SHUFFLE"
    }
    val description = when (type) {
        BoosterType.BOMB -> if (isTr) "3x3 alanı yok eder" else "Destroys a 3x3 area"
        BoosterType.LINE_CLEAR -> if (isTr) "Bir satır/sütunu anında temizler" else "Instantly clears a row/column"
        BoosterType.SHUFFLE -> if (isTr) "Tepsiyi parça harcamadan yeniler" else "Refreshes the tray without using a piece"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, palette.cardBorder, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 26.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = palette.cardAlt,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = (if (isTr) "Sahip: " else "Owned: ") + owned,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) accent else palette.cardAlt,
                    disabledContainerColor = palette.cardAlt
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = canAfford,
                modifier = Modifier.testTag("buy_booster_${type.name}")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isTr) "SATIN AL" else "BUY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.Black else palette.textSecondary
                    )
                    Text(
                        text = "${type.tokenPrice} 🪙",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canAfford) Color.Black else palette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchAdCard(
    isTr: Boolean,
    palette: BlastPalette,
    onWatchAdForTokens: () -> Unit
) {
    // Onceden yesil vurgu + klaket ikonu kullaniliyordu — ikonun temsil ettigi
    // "video izle" eylemiyle asil odul olan altin token arasinda renk kopuklugu
    // vardi (UI/UX karsilastirma bulgusu). Artik tamami altin/odul temasi:
    // hediye ikonu + altin renk, odulle dogrudan gorsel baglanti kuruyor.
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.card),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onWatchAdForTokens() }
            .testTag("watch_ad_tokens")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = NeonGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🎁",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTr) "Reklam izle: +Token" else "Watch ad: +Tokens",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
                Text(
                    text = if (isTr) "Ücretsiz bonus token kazan" else "Earn free bonus tokens",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }

            Surface(
                color = NeonGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isTr) "İZLE" else "WATCH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
