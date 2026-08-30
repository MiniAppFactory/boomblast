package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Faz 167 — SONUC DIYALOGLARININ MALZEMESI.
 *
 * Kullanicinin iki sikayeti vardi ve ikisi de aynı koke iniyordu:
 *
 *   "Seviye Basarisiz yazisinda ikinci satir olmasinda falan gozumu yoran
 *    bir sey var. Ayrica butonlarin tasarimi oyunun yeni tasarimindan uzak."
 *   "Bu kazandiniz ekrani da eski renk tasarimli, daha guzel kabartmali
 *    olabilir, kupa main menudeki gibi olabilir."
 *
 * Kok neden: arayuz Faz 158-162'de bastan yazilirken (GameButton, NeonCard,
 * GameEmblem) SONUC DIYALOGLARI atlanmisti. Menuler kabartmali, konturlu ve
 * isimali; bu iki diyalog ise hala duz Material `Button` ve duz `Text`
 * kullaniyordu. Oyunun en duygusal iki aninda oyuncu birden baska bir
 * uygulamaya gecmis gibi oluyordu.
 *
 * Renkler kullanicinin gonderdigi mockup'tan (Kaboom_Result_Dialog_Assets)
 * ORNEKLENDI, tahmin edilmedi.
 */

/** Diyalog zemini — mockup'taki koyu lacivert panel. */
val ResultDialogSurface = Color(0xFF1B2436)

/** Basari yesili. */
val ResultSuccessAccent = Color(0xFF3ED66A)

/** Basarisizlik kirmizisi. */
val ResultFailAccent = Color(0xFFFF4D6D)

/** BIRINCIL eylem (oyuncuyu oyunda tutan): "reklam izle, devam et". */
val ResultPrimaryBlue = Color(0xFF17B4F0)

/** IKINCIL eylem: "yeniden baslat". */
val ResultSecondaryOrange = Color(0xFFF98D17)

/** UCUNCUL eylem: "haritaya don" — notr, dikkat cekmemeli. */
val ResultTertiarySlate = Color(0xFF55658A)

/** Kupanin arkasindaki sicak isima. */
val ResultTrophyGlow = Color(0xFFFFC53D)

/** Panel ici (skor/hedef kutusu) — zeminden bir tik acik. */
private val ResultPanelFill = Color(0xFF232E44)
private val ResultPanelBorder = Color(0xFF37455F)

/**
 * Vurgu renginden amblem malzemesi uretir: yuz gradyani, 3B govde, kontur
 * ve isima. `GameEmblem`in altin recetesiyle AYNI katmanlar — sadece ton
 * farkli, boylece baslik menu basliklariyla ayni aileden okunuyor.
 */
fun resultEmblemColors(accent: Color): EmblemColors = EmblemColors(
    // Ust yuz: vurgunun beyaza cekilmis hali — isik ustten geliyor.
    fillTop = lerp(accent, Color.White, 0.34f),
    // Alt yuz: koyulastirilmis vurgu. Ustle arasindaki fark harfin
    // hacmini veren sey; ikisi cok yakin olursa yazi yassilasir
    // (Faz 162'de altin icin ogrenildi).
    fillBottom = lerp(accent, Color(0xFF6B0018), 0.30f),
    // Kontur: koyu ama NOTR degil — vurgunun kendi ailesinden, yoksa
    // yazi zeminde "soguk" duruyor (yine Faz 162 dersi).
    outline = lerp(accent, Color(0xFF120A2E), 0.80f),
    glow = accent,
    // 3B govde: yuzun altinda kalan yan yuzey.
    extrude = lerp(accent, Color.Black, 0.55f)
)

/**
 * Sonuc diyalogu butonlarinin renkleri.
 *
 * NEDEN `gameButtonColors` KULLANILMIYOR: o fonksiyon once yazi rengini secip
 * sonra dolguyu kontrast esigi (3.2) saglanana kadar o renkten UZAKLASTIRIYOR.
 * Menulerde dogru davranis, ama buradaki mavi/turuncu gibi orta parlaklikta
 * renklerde dongu birkac kez calisip dolguyu pastele ceviriyor -- render
 * alindiginda mockup'taki doygun butonlar yerine soluk kutular cikti.
 *
 * Burada dolgu ve yazi rengi mockup'tan ORNEKLENIP dogrudan veriliyor, yani
 * dongu hic devreye girmiyor. Kabartma/isima/basma cokmesi yine `GameButton`in
 * kendi malzemesi -- degisen tek sey renk secimi.
 */
fun resultButtonColors(base: Color, content: Color): GameButtonColors =
    GameButtonColors(
        // Ust yuz: cam gibi parlak vurgu (mockup'taki butonlarin ust yarisi).
        top = lerp(base, Color.White, 0.22f),
        // Alt yuz: govdenin koyu tarafi -- hacmi veren fark.
        bottom = lerp(base, Color.Black, 0.20f),
        rim = lerp(base, Color.White, 0.50f),
        shade = lerp(base, Color.Black, 0.52f),
        content = content
    )

/** Mavi birincil buton uzerindeki koyu yazi (mockup'ta da koyu). */
val ResultOnPrimary = Color(0xFF06263D)

/** Turuncu ikincil buton uzerindeki koyu kahve yazi. */
val ResultOnSecondary = Color(0xFF3B1B00)

/** Yesil butonun koyu yesil yazisi. */
val ResultOnSuccess = Color(0xFF07301A)

/**
 * Iki satirli sonuc basligi.
 *
 * NEDEN IKI SATIR: tek satir ("SEVIYE BASARISIZ", "LIVELLO COMPLETATO!")
 * dar ekranda zaten sariyordu — ama KENDILIGINDEN, yani iki esit agirlikta
 * satir olarak. Kullanicinin "gozumu yoran bir sey var" dedigi buydu.
 * Simdi bolme KASITLI ve hiyerarsik: ust satir kucuk ve sakin (baglam),
 * alt satir buyuk (olayin kendisi). Mockup da tam olarak boyle.
 */
@Composable
fun ResultDialogTitle(
    topLine: String,
    bottomLine: String,
    colors: EmblemColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameEmblemLine(
            text = topLine,
            colors = colors,
            fontSize = 27.sp,
            textAlign = TextAlign.Center,
            tightLines = true
        )
        GameEmblemLine(
            text = bottomLine,
            colors = colors,
            // Alt satir uzun cevirilerde ("COMPLETATO!", "TERMINADO")
            // tasabilir; `GameEmblemLine` kendi icinde karakter sayisina
            // gore kuculuyor (bkz. emblemFontScale), o yuzden sabit ve
            // buyuk verilebiliyor.
            fontSize = 38.sp,
            textAlign = TextAlign.Center,
            tightLines = true
        )
    }
}

/**
 * Skor (ve varsa hedef) paneli.
 *
 * Faz 95d'de "arka planda zaten ayni bilgi var" gerekcesiyle kaldirilmisti.
 * O gerekce artik gecerli degil: diyalog acilinca arka plan %85 siyahla
 * perdeleniyor ve arkadaki kartlar okunmuyor. Bu panel eski "Skor + dev
 * rakam" blogundan cok daha kompakt — tek satir, en fazla iki sutun.
 */
@Composable
fun ResultStatPanel(
    leftLabel: String,
    leftValue: String,
    leftColor: Color,
    rightLabel: String?,
    rightValue: String?,
    rightColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ResultPanelFill, RoundedCornerShape(18.dp))
            .border(1.dp, ResultPanelBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatColumn(leftLabel, leftValue, leftColor, Modifier.weight(1f))
        if (rightLabel != null && rightValue != null) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(38.dp)
                    .background(ResultPanelBorder)
            )
            StatColumn(rightLabel, rightValue, rightColor, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFF8FA0BF)
        )
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}
