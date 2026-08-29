package com.miniappfactory.boomblocks.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.flag
import com.miniappfactory.boomblocks.data.label
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.common.WanderingPiecesBackground
import com.miniappfactory.boomblocks.ui.components.gameOuterGlow
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import androidx.compose.ui.platform.LocalDensity

private data class OnboardingStep(
    val icon: ImageVector,
    // Faz 115i: Line Clear adimi icin gercek asset (icon_line_clear.webp) —
    // bu adim uygulamadaki Line Clear guclendiricisiyle AYNI kavram, o
    // guclendirici icin zaten gercek bir asset var. Diger iki adim (PARÇALARI
    // SÜRÜKLE / HEDEFE ULAŞ) icin uygun asset YOK, onlar Icons.Default.* ile
    // kaliyor — bu yuzden alan opsiyonel (null = jenerik vektor ikon kullanilir).
    @param:androidx.annotation.DrawableRes val iconRes: Int? = null,
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
        icon = Icons.Default.Extension,
        iconRes = R.drawable.icon_line_clear,
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
            // v11 gorsel dilinin gezinen oyun parcalari (ModeSelectScreen.kt
            // ile AYNI ortak composable, bkz. `ui/common/WanderingPiecesBackground.kt`;
            // Faz 124: kullanici "bu ekranda da farklı parçalar + hareket
            // olsun" dedi). Kart fillMaxWidth(0.85f) oldugu icin 4 kosede
            // yeterince bosluk var, parcalar kartin DISINDaki koyu zeminde
            // gezini yor.
            WanderingPiecesBackground(modifier = Modifier.matchParentSize())

            Card(
                colors = CardDefaults.cardColors(containerColor = palette.card),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
                    // FAZ 162 — MADDE 4. Kullanici: "dil seciminden sonraki 3
                    // ekranin kenar cizgileri daha neon efekti alabilir, cok
                    // tekduze hissediliyor."
                    //
                    // CIHAZDA GORULEN: kenarlik TEK, sabit parlaklikta ince
                    // bir camgobegi cizgiydi — cam tup degil, cizilmis bir
                    // cerceve. Hedef mockup'larda kenarlik disa dogru yayilan
                    // gercek bir hale tasiyor.
                    //
                    // `Modifier.blur` KULLANILAMAZ (API 31+, minSdk 24);
                    // NeonCard/GamePanel ile AYNI paylasilan katmanli cozum
                    // kullaniliyor.
                    //
                    // CIHAZDA GORULEN VE DUZELTILEN: ilk denemede 7 katman x
                    // 2.6dp verildi ve hale SUREKLI degil AYRI AYRI HALKALAR
                    // olarak okundu (katmanlar arasi ~7.8px, kontur kalinligi
                    // 6px — aralarinda bosluk kaliyordu). Katman sayisi
                    // artirilip aralik daraltildi: 14 x 1.3dp = ayni ~18dp
                    // yayilim, ama komsu konturlar artik ORTUSUYOR ve
                    // gercek bir bulanik hale gibi okunuyor.
                    // Kart `padding(16.dp)` icinde durdugu icin hale
                    // kirpilmadan disariya tasabiliyor.
                    .gameOuterGlow(
                        accent = NeonCyan,
                        cornerRadius = 20.dp,
                        intensity = 1f,
                        layers = 14,
                        spreadStepDp = 1.3f,
                        coreAlpha = 0.52f
                    )
                    .border(
                        2.dp,
                        Brush.verticalGradient(
                            listOf(
                                lerp(NeonCyan, Color.White, 0.55f),
                                NeonCyan,
                                lerp(NeonCyan, Color.White, 0.20f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    // Ic kontur: parlak dis kenarligin hemen icinde daha
                    // sonuk ikinci bir cizgi — "cift cizgi" hissini veren
                    // ve kenarligi CIZGI olmaktan cikarip CERCEVE yapan oge.
                    .drawBehind {
                        val inset = 3.5f * density
                        drawRoundRect(
                            color = lerp(NeonCyan, Color.White, 0.35f)
                                .copy(alpha = 0.30f),
                            topLeft = Offset(inset, inset),
                            size = Size(
                                size.width - inset * 2f,
                                size.height - inset * 2f
                            ),
                            cornerRadius = CornerRadius(20.dp.toPx() - inset),
                            style = Stroke(width = 1.2f * density)
                        )
                    }
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

                    // Ikinci savunma hatti: yukaridaki clamp kok nedeni cozuyor,
                    // bu satir ise ileride baska bir yol `currentStep`i bozarsa
                    // cokme yerine son adimi gostersin diye duruyor.
                    val step = onboardingSteps[currentStep.coerceIn(0, onboardingSteps.lastIndex)]

                    Box(
                        modifier = Modifier.size(116.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Faz 159 — CIHAZDA GORULEN: bu rozet YASSI idi. Duz
                        // bir alfa halkasi + merkezden baslayan lineer gradyan;
                        // kullanicinin "madalyonlar yassi, mat disk + gri
                        // halka" teshisi tam olarak buydu.
                        //
                        // Artik GameKit'teki `IconMedallion` ile AYNI receteyi
                        // kullaniyor:
                        //   1. isik kaynagi UST-SOLDA (merkezde degil) — disk
                        //      kure gibi okunuyor,
                        //   2. halka tek renk degil, ustte parlak altta koyu,
                        //   3. halkanin disinda KATMANLI parlama (Modifier.blur
                        //      API 31+ oldugu icin kullanilamiyor, minSdk 24).
                        val medallionPx = with(LocalDensity.current) { 88.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .drawBehind {
                                    val r = size.minDimension / 2f
                                    for (layer in 1..3) {
                                        val spread = layer * 3.2f * density
                                        drawCircle(
                                            color = step.accent.copy(alpha = 0.30f / layer),
                                            radius = r + spread,
                                            style = Stroke(width = 2.5f * density)
                                        )
                                    }
                                }
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            lerp(step.accent, Color.White, 0.45f),
                                            step.accent,
                                            lerp(step.accent, NeonGold, 0.55f),
                                            lerp(step.accent, Color.Black, 0.35f)
                                        ),
                                        // Faz 166: bu uc sayi PIKSELDI ama kutu
                                        // 88.dp, yani cihaz yogunluguyla buyuyor —
                                        // isik kaynagi yogunluk arttikca kutunun
                                        // ust-sol kosesine dogru kaciyordu.
                                        // GameKit.IconMedallion'daki ayni hatanin
                                        // elle kopyalanmis hali.
                                        //
                                        // Oranlar tasarimin yapildigi xxhdpi'deki
                                        // (density 3, 88dp = 264px) gorunumu birebir
                                        // korur: 30/264, 24/264, 150/264.
                                        // Isik ust-solda.
                                        center = Offset(medallionPx * 0.1136f, medallionPx * 0.0909f),
                                        radius = medallionPx * 0.5682f
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            lerp(step.accent, Color.White, 0.70f),
                                            lerp(step.accent, Color.White, 0.15f),
                                            lerp(step.accent, Color.Black, 0.45f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (step.iconRes != null) {
                                Image(
                                    painter = painterResource(step.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
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
                            // 🔴 Faz 165: sinirsiz artirma COKME uretiyordu.
                            // Butonun GORUNURLUGU kompozisyon zamaninda karar
                            // veriliyor (yukarida `currentStep < lastIndex`), ama
                            // ARTIRMA olay-dagitim zamaninda oluyor. UI thread
                            // takiliyken (ilk acilista consent + AdMob SDK yuklenirken
                            // tam olarak boyle oluyor) ayni kareye iki dokunus
                            // sigabiliyor: ikisi de eski gorunurluk kararina gore
                            // dagitiliyor, `currentStep` son indeksi asiyor ve
                            // asagidaki `onboardingSteps[currentStep]` patliyor.
                            //   IndexOutOfBoundsException: Index 3 out of bounds
                            //   for length 3
                            // Her YENI KURULUM bu ekrandan geciyor, yani en pahali
                            // yerdeki cokme.
                            onClick = { currentStep = (currentStep + 1).coerceAtMost(onboardingSteps.lastIndex) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            lerp(NeonCyan, Color.White, 0.30f),
                                            NeonCyan,
                                            lerp(NeonCyan, Color.Black, 0.25f)
                                        )
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            lerp(NeonGold, Color.White, 0.30f),
                                            NeonGold,
                                            lerp(NeonGold, Color.Black, 0.25f)
                                        )
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
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
