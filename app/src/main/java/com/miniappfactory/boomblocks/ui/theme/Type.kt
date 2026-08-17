package com.miniappfactory.boomblocks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.miniappfactory.boomblocks.R

// Faz 115o — kullanici: "yazı fontumuz çok şirket yazısı gibi". Kok sebep:
// hicbir yerde ozel font tanimlanmamisti, tum uygulama FontFamily.Default
// (Roboto — Android'in sistem/ayarlar menusuyle BIREBIR ayni font) kullaniyordu.
// Kullanicinin kendi sectigi Fredoka (Google Fonts, OFL lisansli, cevrimdisi
// gomulu — internet/Play Services bagimliligi yok) buraya baglandi.
//
// Fredoka'da GERCEK bir "Black" agirlik YOK (en kalini Bold). Uygulama hemen
// her yerde FontWeight.Black kullaniyor (bkz. onlarca `fontWeight =
// FontWeight.Black` cagrisi) — eger Black icin ayri bir dosya vermeseydik,
// Android o agirligi SENTETIK (faux-bold, glifleri yatay gerip kalinlastiran
// sahte bir teknik) uretirdi ve harfler bozuk/kalin-degil-kalin gorunurdu.
// Bold dosyasi hem Bold hem Black agirligina baglanarak bu onleniyor — ikisi
// de ayni GERCEK kalin glifleri kullanir, sentez yok.
val FredokaFamily = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal),
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
    Font(R.font.fredoka_bold, FontWeight.Black)
)

// Material3'un varsayilan tip olcegi (boyut/satir yuksekligi/harf araligi)
// KORUNUYOR — degisen tek sey her seviyede fontFamily. Uygulamadaki Text()
// cagrilarinin neredeyse tamami fontSize/fontWeight'i DOGRUDAN parametre
// olarak veriyor (fontFamily vermiyor), bu yuzden LocalTextStyle uzerinden
// (bodyLarge varsayilani) MIRAS alinan fontFamily tum ekranlara otomatik
// yayiliyor — tek tek her Text() cagrisini degistirmeye gerek yok.
private val defaultTypography = Typography()

val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = FredokaFamily),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = FredokaFamily),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = FredokaFamily),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = FredokaFamily),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = FredokaFamily),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = FredokaFamily),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = FredokaFamily),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = FredokaFamily),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = FredokaFamily),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = FredokaFamily),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = FredokaFamily),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = FredokaFamily),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = FredokaFamily),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = FredokaFamily),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = FredokaFamily)
)
