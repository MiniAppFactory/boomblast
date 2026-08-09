package com.example.ui.consent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.pick
import com.example.ui.theme.BlastSkin
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.blastPalette

private const val PRIVACY_POLICY_URL = "https://whatsthisapp.github.io/blasttheblocks/"

// Ilk acilista, onboarding tutorial'dan ONCE, atlanamaz bir kabul ekrani —
// Play Store'a gonderilecek her uygulamada beklenen standart bir uygulama
// (bkz. referans: "Block Blast!"in kendi "Accept Terms of Use and Privacy Policy" ekrani).
@Composable
fun TermsAcceptScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onAccept: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val context = LocalContext.current

    fun openPolicy() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = palette.card),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                .testTag("terms_accept_card")
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🧩💥", fontSize = 40.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = language.pick(tr = "Blast the Blocks'a Hoş Geldin!", en = "Welcome to Blast the Blocks!", it = "Benvenuto in Blast the Blocks!", fr = "Bienvenue dans Blast the Blocks !", es = "¡Bienvenido a Blast the Blocks!"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = language.pick(
                        tr = "Devam etmeden önce lütfen Gizlilik Politikamızı ve Kullanım Şartlarımızı oku ve kabul et.",
                        en = "Before you continue, please read and accept our Privacy Policy and Terms of Use.",
                        it = "Prima di continuare, leggi e accetta la nostra Informativa sulla Privacy e i Termini di Utilizzo.",
                        fr = "Avant de continuer, veuillez lire et accepter notre Politique de Confidentialité et nos Conditions d'Utilisation.",
                        es = "Antes de continuar, lee y acepta nuestra Política de Privacidad y Términos de Uso."
                    ),
                    fontSize = 14.sp,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { openPolicy() },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.cardAlt),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terms_accept_open_policy_button")
                ) {
                    Text(
                        text = language.pick(tr = "Gizlilik Politikası ve Kullanım Şartları", en = "Privacy Policy & Terms of Use", it = "Informativa sulla Privacy e Termini di Utilizzo", fr = "Politique de Confidentialité et Conditions d'Utilisation", es = "Política de Privacidad y Términos de Uso"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("terms_accept_button")
                ) {
                    Text(
                        text = language.pick(tr = "KABUL ET", en = "ACCEPT", it = "ACCETTA", fr = "ACCEPTER", es = "ACEPTAR"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}
