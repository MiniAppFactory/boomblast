package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
 * NEDEN IKI SATIR: tek satir ("SEVIYE BASARISIZ", "LIVELLO COMPLETATO!") dar
 * ekranda zaten sariyordu -- ama KENDILIGINDEN, yani iki esit agirlikta satir
 * olarak. Kullanicinin "gozumu yoran bir sey var" dedigi buydu. Simdi bolme
 * KASITLI ve hiyerarsik: ust satir kucuk ve sakin (baglam), alt satir buyuk
 * (olayin kendisi).
 *
 * ---------------------------------------------------------------------------
 * FAZ 170 — PUNTO ARTIK OLCULEREK SECILIYOR, TAHMIN EDILMIYOR.
 *
 * Kullanici cihazda gordu: "bu olmamis, sigmiyor SEVIYE TAMAMLANDI yazisi."
 * Ekranda "TAMAML..." cikiyordu.
 *
 * Ilk yazdigimda punto SABIT 38sp'ydi ve yorumda "GameEmblemLine kendi icinde
 * karakter sayisina gore kuculuyor (bkz. emblemFontScale)" yaziyordu. BU
 * YANLISTI: `emblemFontScale` yalnizca `GameEmblem` icinde cagriliyor,
 * `GameEmblemLine` icinde DEGIL. Yani satir hic kuculmuyor, sadece kirpiliyordu.
 * Kendi render'imda 411dp'de sigdigi icin de fark etmemistim -- daha dar bir
 * yerlesimde ilk denemede patladi.
 *
 * Cozum karakter saymak DEGIL (o da bir tahmin ve dile gore yanilir): metin
 * gercek stiliyle OLCULUP sigan en buyuk punto araniyor. "TAMAMLANDI!",
 * "COMPLETATO!", "TERMINADO", "BAŞARISIZ" -- hepsi ayni yoldan geciyor.
 * ---------------------------------------------------------------------------
 */
@Composable
fun ResultDialogTitle(
    topLine: String,
    bottomLine: String,
    colors: EmblemColors,
    modifier: Modifier = Modifier,
    // Secilen alt satir puntosu. Test bunu okuyup TABANA DAYANMIS mi diye
    // bakiyor: arama yalnizca sigmadigi surece kuculttugu icin tabana dayanmak
    // "sigdiramadim" demektir (bkz. ResultDialogOverflowTest).
    onBottomSizeChosen: (Float) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        // %92: amblem malzemesi metnin cevresine kontur + 3B govde + isima
        // ekliyor, yani cizilen genislik olculen metinden BIRAZ genis. Pay
        // birakilmazsa kenarda tiras olusuyor.
        val budgetPx = with(density) { maxWidth.toPx() } * 0.92f

        fun widthOf(text: String, sizeSp: Float): Float =
            measurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = sizeSp.sp,
                    // GameEmblemLine ile AYNI harf araligi; yoksa olcum
                    // gercekte cizilenden dar cikar.
                    letterSpacing = (sizeSp * 0.035f).sp
                ),
                maxLines = 1
            ).size.width.toFloat()

        fun fitSize(text: String, max: Float, min: Float): Float {
            var size = max
            while (size > min && widthOf(text, size) > budgetPx) size -= 1f
            return size
        }

        val bottomSp = fitSize(bottomLine, max = 38f, min = 15f)
        onBottomSizeChosen(bottomSp)
        // Ust satir alt satirdan buyuk olmamali: hiyerarsi bozulmasin.
        val topSp = minOf(fitSize(topLine, max = 27f, min = 15f), bottomSp * 0.78f)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameEmblemLine(
                text = topLine,
                colors = colors,
                fontSize = topSp.sp,
                textAlign = TextAlign.Center,
                tightLines = true
            )
            GameEmblemLine(
                text = bottomLine,
                colors = colors,
                fontSize = bottomSp.sp,
                textAlign = TextAlign.Center,
                tightLines = true
            )
        }
    }
}


/**
 * FAZ 170 — SIGAN EN BUYUK PUNTOYU OLCEREK BULAN ETIKET.
 *
 * Kullanici: "reklam izle devam et yazisi sigmamis burada da... APK yapip bana
 * test yaptirmak yerine APK yapmadan once render et, ben senin testcin
 * degilim." Hakli: bu benim isimdi.
 *
 * KOK NEDEN: etiket puntosu KARAKTER SAYISINA gore seciliyordu
 * (`text.length <= 18 -> 14.sp` gibi). Bu bir tahmin ve iki yerden yanilir:
 *   - harf genisligi dile gore degisir ("REKLAM IZLE, DEVAM ET" ile ayni
 *     uzunluktaki Ispanyolca metin ayni yeri kaplamaz),
 *   - butonun ICINDEKI diger seyleri bilmez. Faz 169'da sol ikonu eklediğimde
 *     kalan genislik 38dp daralinca ayni karakter sayisi artik sigmiyordu ama
 *     formul bunu goremedi.
 *
 * Burada tahmin yok: metin GERCEK stiliyle ve GERCEK kalan genislikle olculup
 * `hasVisualOverflow` false olana kadar punto dusuruluyor. Iki satira da izin
 * var (varlik paketindeki mavi buton da iki satir).
 */
@Composable
fun AutoFitLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxSizeSp: Float = 16f,
    minSizeSp: Float = 10f,
    maxLines: Int = 2,
    // Cizilen metnin son olcum sonucu. Testler `hasVisualOverflow`u buradan
    // okuyup TASMA OLURSA BUILD'I DUSURUYOR (bkz. ResultDialogOverflowTest) --
    // kullanicinin cihazda bulmasi gereken bir sey degil bu.
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }.toInt().coerceAtLeast(1)

        fun styleFor(sizeSp: Float) = TextStyle(
            fontFamily = AppFontFamily,
            fontSize = sizeSp.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.6.sp,
            color = color,
            textAlign = TextAlign.Center
        )

        var size = maxSizeSp
        while (size > minSizeSp) {
            val overflow = measurer.measure(
                text = AnnotatedString(text),
                style = styleFor(size),
                maxLines = maxLines,
                constraints = Constraints(maxWidth = widthPx)
            ).hasVisualOverflow
            if (!overflow) break
            size -= 0.5f
        }

        Text(
            text = text,
            style = styleFor(size),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = onTextLayout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * FAZ 170: sonuc diyaloglarinin TEK buton yolu.
 *
 * Uc buton da (reklam izle / yeniden baslat / haritaya don) buradan geciyor ki
 * ikon boyutu, aralik ve sigdirma kurali tek yerde dursun. Faz 169'da ikonu
 * elle uc ayri yere eklemek, etiketin sigmadigini fark etmemenin de sebebiydi.
 */
@Composable
fun ResultActionButton(
    text: String,
    onClick: () -> Unit,
    colors: GameButtonColors,
    iconRes: Int?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    maxSizeSp: Float = 16f,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    GameButton(
        onClick = onClick,
        colors = colors,
        enabled = enabled,
        minHeight = 54.dp,
        horizontalPadding = 10.dp,
        modifier = modifier
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = colors.content,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            if (iconRes != null) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            AutoFitLabel(
                text = text,
                color = colors.content,
                maxSizeSp = maxSizeSp,
                onTextLayout = onTextLayout,
                // Ikon ve bosluk sol tarafta yer kapladi; kalan genislik
                // BURADAN olculuyor, tahmin edilmiyor.
                modifier = Modifier.weight(1f)
            )
        }
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
            // FAZ 181 — PANEL ALCALDI.
            //
            // Kullanici (cihaz ekran goruntusuyle): "kalp emojisi altindaki
            // skor ve hedef blogu height daralmali ki asagidaki haritaya don
            // butonu gozuksun." Teshis: diyalog Column'u ekrandan uzundu ve
            // Compose kalan yeri SON cocuga veriyor -- "HARITAYA DON" butonu
            // 54dp yerine ~37dp olcuup yazisi ORTADAN kesiliyordu. Panel
            // (dolgu 24dp + 12sp etiket + 30sp rakam) blogun en yuksek
            // parcasiydi; burada ~24dp geri kazaniliyor.
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatColumn(leftLabel, leftValue, leftColor, Modifier.weight(1f))
        if (rightLabel != null && rightValue != null) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
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
            fontSize = 11.sp,
            // Satir yuksekligi puntoya kelepcelendi: varsayilan yazi tipi
            // metrigi etiket+rakam icin ~14dp fazladan bosluk ekliyordu ve
            // kazanilmak istenen yer tam orasi.
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = Color(0xFF8FA0BF)
        )
        Text(
            text = value,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}
