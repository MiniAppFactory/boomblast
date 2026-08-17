package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.miniappfactory.boomblocks.R

// Faz 115o — kullanici: "yazı fontumuz çok şirket yazısı gibi". Kok sebep:
// hicbir yerde ozel font tanimlanmamisti, tum uygulama FontFamily.Default
// (Roboto — Android'in sistem/ayarlar menusuyle BIREBIR ayni font) kullaniyordu.
//
// Faz 115w — FONT DEGISTIRILDI: Fredoka -> Baloo 2. Kullanicinin ilk sectigi
// Fredoka'nin Bold statik dosyasinda TURKCE KARAKTERLER EKSIKTI (Ş/ş, İ, Ğ/ğ
// yoktu — fontTools ile dogrulandi). Bu eksik karakterler icin Android
// sessizce SISTEM YEDEK FONTUNA (Roboto/Noto) duşuyordu — "BAŞLA" gibi
// kelimelerde Ş harfi diger harflerle uyumsuz gorunuyordu (kullanici
// ekran goruntusuyle yakaladi). Baloo 2'nin TUM statik agirliklarinda
// (Regular/Medium/SemiBold/Bold/ExtraBold) Turkce karakter seti TAM —
// fontTools ile tek tek dogrulandi.
//
// Baloo 2'de Fredoka'dan FARKLI olarak GERCEK bir ExtraBold (800) agirligi
// VAR — FontWeight.Black (900) icin ExtraBold dosyasi kullaniliyor (Bold'u
// iki kez baglayip sentetik kalinlatirma riskine girmeye gerek yok, ama
// yine de en yakin GERCEK agirlik bu, ExtraBold ile Black arasinda GERCEK
// bir dosya farki olmadigi icin Android muhtemelen ExtraBold'u dogrudan
// kullanacak, ekstra sentez gerekmez).
val AppFontFamily = FontFamily(
    Font(R.font.baloo2_regular, FontWeight.Normal),
    Font(R.font.baloo2_medium, FontWeight.Medium),
    Font(R.font.baloo2_semibold, FontWeight.SemiBold),
    Font(R.font.baloo2_bold, FontWeight.Bold),
    Font(R.font.baloo2_extrabold, FontWeight.ExtraBold),
    Font(R.font.baloo2_extrabold, FontWeight.Black)
)

// Material3'un varsayilan tip olcegi (boyut/satir yuksekligi/harf araligi)
// KORUNUYOR — degisen tek sey her seviyede fontFamily. Uygulamadaki Text()
// cagrilarinin neredeyse tamami fontSize/fontWeight'i DOGRUDAN parametre
// olarak veriyor (fontFamily vermiyor), bu yuzden LocalTextStyle uzerinden
// (bodyLarge varsayilani) MIRAS alinan fontFamily tum ekranlara otomatik
// yayiliyor — tek tek her Text() cagrisini degistirmeye gerek yok.
//
// ISTISNA: `Text()`e DOGRUDAN bir `style = TextStyle(...)` verilen yerler bu
// mirasi ATLAR (Faz 115p'de kesfedildi ve 4 yerde duzeltildi — bkz.
// ModeSelectScreen.kt/BoomBlocksGame.kt'deki "fontFamily = AppFontFamily"
// satirlari). Yeni bir yerde `style = TextStyle(...)` yazarsan fontFamily'yi
// ACIKCA ver, aksi halde sessizce Roboto'ya duşer.
private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = AppFontFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = AppFontFamily)
)
