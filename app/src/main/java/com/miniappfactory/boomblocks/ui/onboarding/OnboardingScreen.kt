package com.miniappfactory.boomblocks.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.miniappfactory.boomblocks.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.flag
import com.miniappfactory.boomblocks.data.label
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.blastPalette

private data class OnboardingStep(
    val icon: ImageVector,
    val accent: Color,
    val titleTr: String,
    val titleEn: String,
    val titleIt: String,
    val titleFr: String,
    val titleEs: String,
    val descriptionTr: String,
    val descriptionEn: String,
    val descriptionIt: String,
    val descriptionFr: String,
    val descriptionEs: String
) {
    fun title(language: AppLanguage): String =
        language.pick(tr = titleTr, en = titleEn, it = titleIt, fr = titleFr, es = titleEs)

    fun description(language: AppLanguage): String =
        language.pick(tr = descriptionTr, en = descriptionEn, it = descriptionIt, fr = descriptionFr, es = descriptionEs)
}

// Faz 31: dev emoji ("🧩"/"💥"/"🎯") duz Text olarak ciziliyordu — emoji karakteri
// bazi cihazlarda/fontlarda dusuk cozunurluklu/pikselli render oluyordu, ucu de
// "hic emek harcanmamis duz ikon secimi" gibi duruyordu (kullanici geri bildirimi).
// Artik her adimin kendi vurgu rengiyle gradyanli, parlama halkali bir rozet
// icinde NET (vektor tabanli, hicbir cozunurlukte pikselenmeyen) bir Material
// ikonu var — oyunun geri kalanindaki ModeCard/logo rozeti dilini takip ediyor.
private val onboardingSteps = listOf(
    OnboardingStep(
        icon = Icons.Default.Extension,
        accent = NeonCyan,
        titleTr = "PARÇALARI SÜRÜKLE",
        titleEn = "DRAG THE PIECES",
        titleIt = "TRASCINA I PEZZI",
        titleFr = "GLISSEZ LES PIÈCES",
        titleEs = "ARRASTRA LAS PIEZAS",
        descriptionTr = "Tepsideki bir bloğu seçip ızgaraya sürükle",
        descriptionEn = "Drag a block from the tray onto the grid",
        descriptionIt = "Trascina un blocco dal vassoio sulla griglia",
        descriptionFr = "Faites glisser un bloc du plateau vers la grille",
        descriptionEs = "Arrastra un bloque de la bandeja a la cuadrícula"
    ),
    OnboardingStep(
        icon = Icons.Default.Bolt,
        accent = Color(0xFFFF6B35),
        titleTr = "SATIRI PATLAT",
        titleEn = "CLEAR THE LINE",
        titleIt = "ELIMINA LA LINEA",
        titleFr = "EFFACEZ LA LIGNE",
        titleEs = "LIMPIA LA LÍNEA",
        descriptionTr = "Bir satırı veya sütunu tamamen doldurunca anında patlar",
        descriptionEn = "Fill an entire row or column and it instantly clears",
        descriptionIt = "Riempi completamente una riga o colonna e si elimina all'istante",
        descriptionFr = "Remplissez entièrement une ligne ou une colonne et elle s'efface instantanément",
        descriptionEs = "Llena completamente una fila o columna y se elimina al instante"
    ),
    OnboardingStep(
        icon = Icons.Default.TrackChanges,
        accent = NeonGold,
        titleTr = "HEDEFE ULAŞ",
        titleEn = "REACH THE TARGET",
        titleIt = "RAGGIUNGI L'OBIETTIVO",
        titleFr = "ATTEIGNEZ L'OBJECTIF",
        titleEs = "ALCANZA EL OBJETIVO",
        descriptionTr = "Her seviyenin bir hedef skoru var, ulaşınca seviye tamamlanır",
        descriptionEn = "Each level has a target score — reach it to complete the level",
        descriptionIt = "Ogni livello ha un punteggio obiettivo — raggiungilo per completare il livello",
        descriptionFr = "Chaque niveau a un score cible — atteignez-le pour terminer le niveau",
        descriptionEs = "Cada nivel tiene una puntuación objetivo: alcánzala para completar el nivel"
    )
)

@Composable
fun OnboardingScreen(
    language: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onFinish: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    var currentStep by remember { mutableIntStateOf(0) }
    // Faz 37: kullanici "ilk onboarding sayfasında dil de seçtirelim" dedi —
    // cihaz dili GameStateRepository'de otomatik algilanip varsayilan
    // secildi (bkz. AppLanguage.fromSystemLocale), burada kullanici onu
    // onaylayabiliyor veya degistirebiliyor. Onaylanana kadar tur adimlari
    // (PARÇALARI SÜRÜKLE vb.) gosterilmiyor.
    var languageConfirmed by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = palette.card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
                    .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
            ) {
                Column(
                    // Faz 104: savunma amacli verticalScroll. Kart sabit yukseklikte degil,
                    // icerigi kadar buyuyor ve hicbir yerde kaydirilamiyordu — Faz 102'de
                    // dil secme adiminin basina 104dp'lik logo eklenince icerik yaklasik
                    // 55dp uzadi. S8'de (1080x2220) rahat sigiyor, ama minSdk 24 seviyesindeki
                    // kucuk ekranlarda 5 dil satiri + logo + baslik tasabilir ve tasan kisma
                    // ERISILEMEZ olurdu (dil secmek ilerlemenin TEK yolu, atlanamiyor).
                    // Scroll ile en kotu ihtimalde kaydirilarak ulasilabilir kaliyor.
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  if (!languageConfirmed) {
                    LanguagePickerStep(
                        language = language,
                        palette = palette,
                        onSelectLanguage = onSelectLanguage,
                        onConfirm = { languageConfirmed = true }
                    )
                  } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onFinish,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = language.pick(tr = "Atla", en = "Skip", it = "Salta", fr = "Passer", es = "Saltar"),
                                fontSize = 13.sp,
                                color = palette.textSecondary
                            )
                        }
                    }

                    val step = onboardingSteps[currentStep]

                    Box(
                        modifier = Modifier.size(116.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dis parlama halkasi — rozete derinlik ve "gerçek bir
                        // görsel" hissi katan yumusak glow.
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape)
                                .background(step.accent.copy(alpha = 0.16f))
                        )
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(step.accent, NeonGold)))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.title(language),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.description(language),
                        fontSize = 14.sp,
                        color = palette.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        onboardingSteps.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (index == currentStep) NeonCyan else palette.cardAlt,
                                        shape = CircleShape
                                    )
                                    .testTag("onboarding_step_dot_$index")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentStep < onboardingSteps.lastIndex) {
                        Button(
                            onClick = { currentStep += 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text(
                                text = language.pick(tr = "İLERİ", en = "NEXT", it = "AVANTI", fr = "SUIVANT", es = "SIGUIENTE"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_start_button")
                        ) {
                            Text(
                                text = language.pick(tr = "BAŞLA", en = "START", it = "INIZIA", fr = "COMMENCER", es = "EMPEZAR"),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                  }
                }
            }
        }
    }
}

// Faz 37: onboarding'in ilk adimi — kullanici "ilk onboarding sayfasında dil
// de seçtirelim" dedi. Cihaz dili GameStateRepository tarafindan otomatik
// algilanip varsayilan olarak zaten secilmis oluyor (bkz. AppLanguage.
// fromSystemLocale); burada kullanici sadece onaylıyor veya degistiriyor.
// Hangi dilde oldugu henuz belli olmadigindan basliktaki metin kasitli
// olarak COK DILLI (TR/EN) tutuldu, ama her secenegin kendi native adi zaten
// kendini acikliyor (bayrak + "Türkçe"/"English"/vb.).
@Composable
private fun LanguagePickerStep(
    language: AppLanguage,
    palette: BlastPalette,
    onSelectLanguage: (AppLanguage) -> Unit,
    onConfirm: () -> Unit
) {
    // Faz 102: kullanici "ilk acilistaki hos geldin ekraninda ikon yerine bizim
    // logomuz olsun" dedi. Onceden burada duz bir 🌐 emojisi vardi — hem marka
    // tasimiyordu hem de Faz 31'de tutorial adimlarindaki emojiler icin
    // verilen ayni geri bildirime ("emek harcanmamis duruyor") aciktı. Artik
    // uygulamanin kendi ikonu (docs/play_store_assets/icon_512.png ->
    // drawable-nodpi/logo_kaboom.png) gosteriliyor: kullanicinin uygulamayi
    // Play'de ve ana ekranda gordugu gorselle birebir ayni, yani ilk acilis
    // aninda marka tanınırlığı kuruluyor.
    Box(
        modifier = Modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        // ModeCard/rozet dilini takip eden yumusak dis parlama.
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.16f))
        )
        Image(
            painter = painterResource(R.drawable.logo_kaboom),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Dil Seç / Choose Language",
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = palette.textPrimary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppLanguage.entries.forEach { lang ->
            val selected = lang == language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) NeonCyan.copy(alpha = 0.18f) else palette.cardAlt)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) NeonCyan else palette.cardBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        onSelectLanguage(lang)
                        onConfirm()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("onboarding_lang_${lang.code}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = lang.flag(), fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = lang.label(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) NeonCyan else palette.textPrimary
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
