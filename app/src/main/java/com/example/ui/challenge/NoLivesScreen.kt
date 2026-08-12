package com.example.ui.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.GameStateRepository
import com.example.data.pick
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.blastPalette
import kotlinx.coroutines.delay

// Faz 77: Pro Mode can sistemi — 0 canken bir bolume baslamaya calisinca
// (BlastViewModel.consumeChallengeLife() false donunce) bu ekran gosteriliyor.
// Kullanicinin ya bekleyip dogal yenilenmeyi gormesi ya "reklam izle: +1 can"
// ile hemen devam etmesi icin.
@Composable
fun NoLivesScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    lastLifeTimestamp: Long,
    isWatchAdLoading: Boolean,
    onWatchAd: () -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val remainingMs = (GameStateRepository.CHALLENGE_LIFE_REFILL_MS - (now - lastLifeTimestamp)).coerceAtLeast(0)
    val minutes = (remainingMs / 60000L).toInt()
    val seconds = ((remainingMs / 1000L) % 60L).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("no_lives_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                tint = palette.textPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color(0xFFE53E3E),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = language.pick(tr = "Canların Bitti", en = "Out of Lives", it = "Vite Esaurite", fr = "Plus de Vies", es = "Sin Vidas"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = language.pick(
                    tr = "Sonraki can: %02d:%02d".format(minutes, seconds),
                    en = "Next life: %02d:%02d".format(minutes, seconds),
                    it = "Prossima vita: %02d:%02d".format(minutes, seconds),
                    fr = "Prochaine vie : %02d:%02d".format(minutes, seconds),
                    es = "Próxima vida: %02d:%02d".format(minutes, seconds)
                ),
                fontSize = 14.sp,
                color = palette.textSecondary,
                modifier = Modifier.testTag("no_lives_countdown")
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onWatchAd,
                enabled = !isWatchAdLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("no_lives_watch_ad_button")
            ) {
                if (isWatchAdLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = language.pick(
                            tr = "🎁 Reklam İzle: +1 Can",
                            en = "🎁 Watch Ad: +1 Life",
                            it = "🎁 Guarda Pubblicità: +1 Vita",
                            fr = "🎁 Regarder une Pub : +1 Vie",
                            es = "🎁 Ver Anuncio: +1 Vida"
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
