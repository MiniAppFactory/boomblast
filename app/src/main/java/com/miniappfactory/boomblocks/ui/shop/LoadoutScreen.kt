package com.miniappfactory.boomblocks.ui.shop

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AD_TOKEN_REWARD
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.BoosterType
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette

/**
 * Pre-level loadout screen: lets the player review the level target, spend
 * Game Tokens on boosters, watch an ad for bonus tokens, then start the level.
 */
@Composable
fun LoadoutScreen(
    levelNumber: Int,
    targetScore: Int,
    // Faz 94: `progress: PlayerProgress` yerine dar parametreler — bu ekran
    // zaten sadece bu iki alani okuyordu, boylece cagiran taraf (Seviyeli/Pro
    // Mode) kendi dogru booster envanterini gecirebiliyor.
    tokens: Int,
    ownedBoosters: Map<BoosterType, Int>,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onBuyBooster: (BoosterType) -> Unit,
    onWatchAdForTokens: () -> Unit,
    // Faz 43: reklam yuklemesi (RewardedAd.load) genelde 3-8 saniye surebiliyor
    // ama ONCEDEN butonda hicbir gorsel geri bildirim yoktu — kullanici "watch
    // butonuna tıklayamadım" dedi, aslinda tiklama calisiyordu ama sessiz bekleme
    // suresi butonun bozuk oldugu izlenimi veriyordu. Artik cagiran taraf (Ad-
    // Mob istegini baslatan/biten AppNavigation) bu durumu buraya bildiriyor.
    isWatchAdLoading: Boolean = false,
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
                        text = language.pick(tr = "SEVİYE $levelNumber", en = "LEVEL $levelNumber", it = "LIVELLO $levelNumber", fr = "NIVEAU $levelNumber", es = "NIVEL $levelNumber"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = language.pick(tr = "HEDEF: $targetScore", en = "TARGET: $targetScore", it = "OBIETTIVO: $targetScore", fr = "OBJECTIF : $targetScore", es = "OBJETIVO: $targetScore"),
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
                        androidx.compose.foundation.Image(
                            painter = painterResource(com.miniappfactory.boomblocks.R.drawable.icon_coin),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$tokens",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = language.pick(tr = "TAKIMINI HAZIRLA", en = "PREPARE YOUR LOADOUT", it = "PREPARA IL TUO EQUIPAGGIAMENTO", fr = "PRÉPARE TON ÉQUIPEMENT", es = "PREPARA TU EQUIPO"),
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
                        owned = ownedBoosters[type] ?: 0,
                        canAfford = tokens >= type.tokenPrice,
                        language = language,
                        palette = palette,
                        onBuy = { onBuyBooster(type) }
                    )
                }

                WatchAdCard(
                    language = language,
                    palette = palette,
                    isLoading = isWatchAdLoading,
                    onWatchAdForTokens = onWatchAdForTokens
                )

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
                    text = language.pick(tr = "BAŞLA", en = "START", it = "INIZIA", fr = "COMMENCER", es = "EMPEZAR"),
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
    language: AppLanguage,
    palette: BlastPalette,
    onBuy: () -> Unit
) {
    val icon = when (type) {
        BoosterType.BOMB -> "💣"
        BoosterType.LINE_CLEAR -> "⚡"
    }
    // Uc guclendirici butonu birbirinden ayirt edilemiyordu (hepsi ayni mavi) —
    // UI/UX karsilastirma bulgusu. Her tur artik kendi vurgu rengini tasiyor,
    // ikonun anlamiyla eslesecek sekilde (bomba->kirmizi/turuncu, simsek->altin).
    val accent = when (type) {
        BoosterType.BOMB -> Color(0xFFFF6B35)
        BoosterType.LINE_CLEAR -> NeonGold
    }
    val name = when (type) {
        BoosterType.BOMB -> language.pick(tr = "BOMBA", en = "BOMB", it = "BOMBA", fr = "BOMBE", es = "BOMBA")
        BoosterType.LINE_CLEAR -> language.pick(tr = "SATIR TEMİZLE", en = "LINE CLEAR", it = "ELIMINA LINEA", fr = "EFFACER LIGNE", es = "LIMPIAR LÍNEA")
    }
    val description = when (type) {
        BoosterType.BOMB -> language.pick(tr = "3x3 alanı yok eder", en = "Destroys a 3x3 area", it = "Distrugge un'area 3x3", fr = "Détruit une zone 3x3", es = "Destruye un área de 3x3")
        BoosterType.LINE_CLEAR -> language.pick(tr = "Bir satır/sütunu anında temizler", en = "Instantly clears a row/column", it = "Elimina istantaneamente una riga/colonna", fr = "Efface instantanément une ligne/colonne", es = "Limpia instantáneamente una fila/columna")
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
                // Faz 79: "Sahip: N" rozeti ADI ile AYNI satirdaydi, sabit
                // genislikte oldugu icin (weight yok) her zaman tam yer
                // kapliyordu — kalan dar alanda uzun isimler ("SATIR TEMİZLE")
                // "SATIR TE..." diye kirpiliyordu. Kullanici: "sahiplik sayisini
                // asagi indir". Rozet artik ADIN ALTINDA kendi satirinda,
                // isim artik TAM genislik kullanabiliyor.
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    color = palette.cardAlt,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = language.pick(tr = "Sahip: ", en = "Owned: ", it = "Posseduti: ", fr = "Possédés : ", es = "Poseídos: ") + owned,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
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
                        text = language.pick(tr = "SATIN AL", en = "BUY", it = "ACQUISTA", fr = "ACHETER", es = "COMPRAR"),
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
    language: AppLanguage,
    palette: BlastPalette,
    isLoading: Boolean,
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
            .clickable(enabled = !isLoading) { onWatchAdForTokens() }
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
                    // Faz 129: miktar artik etikette de yaziyor (kullanici istegi:
                    // "+100 Token yazsin"). Sayi AD_TOKEN_REWARD'dan interpolate
                    // ediliyor, elle yazilmiyor — odul degisirse etiket de degisir.
                    text = language.pick(
                        tr = "Reklam izle: +$AD_TOKEN_REWARD Token",
                        en = "Watch ad: +$AD_TOKEN_REWARD Tokens",
                        it = "Guarda annuncio: +$AD_TOKEN_REWARD Token",
                        fr = "Regarder pub : +$AD_TOKEN_REWARD Jetons",
                        es = "Ver anuncio: +$AD_TOKEN_REWARD Fichas"
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGold
                )
                Text(
                    text = language.pick(tr = "Ücretsiz bonus token kazan", en = "Earn free bonus tokens", it = "Ottieni token bonus gratis", fr = "Gagnez des jetons bonus gratuits", es = "Gana fichas bonus gratis"),
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }

            Surface(
                color = NeonGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = NeonGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .size(16.dp)
                    )
                } else {
                    Text(
                        text = language.pick(tr = "İZLE", en = "WATCH", it = "GUARDA", fr = "REGARDER", es = "VER"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
