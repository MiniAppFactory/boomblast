package com.miniappfactory.boomblocks.ui.modeselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.BlockOrange
import com.miniappfactory.boomblocks.ui.theme.BlockPink
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.miniappfactory.boomblocks.ui.theme.blastPalette

// Oyunun asil giris ekrani: kullanici once "Sonsuz Mod" mu "Seviyeli Mod" mu
// oynayacagina karar veriyor. Onceden Sonsuz Mod, seviye listesinin icine
// sikistirilmis bir kart olarak gosteriliyordu — bu iki esdeger oyun modunu
// birbirinden ayirip her ikisine de esit agirlik veriyor.
@Composable
fun ModeSelectScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    tokens: Int,
    endlessBestScore: Int,
    highestUnlockedLevel: Int,
    // Faz 77: Pro Mode (eski "Challenge") artik oynanabilir — kart YAKINDA/
    // kilitli degil, kendi ilerlemesini gosteriyor.
    highestChallengeLevel: Int,
    // Faz 78: Retro Modu artik oynanabilir — kart YAKINDA/kilitli degil.
    retroHighScore: Int,
    onOpenLevels: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenChallenge: () -> Unit,
    onOpenRetro: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        // Faz 115h: v11 mockup'taki dagilmis, dondurulmus "konfeti kupleri".
        // Bunlar resim degil — blok hucrelerindeki AYNI fasetli/parlak recete
        // (dikey gradyan + isik/golge kenar), kucuk olcekte ve dondurulmus
        // olarak Canvas'a cizildi. Kartlarin ICINE degil ARKASINA, kenarlara
        // yakin yerlestirildi ki icerigi (baslik/deger) ortmesin — bu ayni
        // zamanda mockup'taki gercek kompozisyona da daha yakin: gemler
        // kartlarin uzerinde degil, ekranin kenar bosluklarinda duruyordu.
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            data class Confetti(val fx: Float, val fy: Float, val sizeDp: Float, val rotation: Float, val color: Color, val alpha: Float)
            // Faz 115h duzeltmesi: ilk denemede ust siradaki kupler header
            // satirindaki Ayarlar disliyle cakisiyordu (fy=0.03-0.045, header
            // yaklasik o yukseklikte). Baslik satirinin ("Choose a game mode")
            // ALTINA, kart grid'inin USTUNE dusen bosluga tasindi.
            val cubes = listOf(
                Confetti(0.05f, 0.155f, 20f, 24f, BlockOrange, 0.80f),
                Confetti(0.93f, 0.145f, 16f, -20f, NeonCyan, 0.75f),
                Confetti(0.02f, 0.235f, 13f, 40f, NeonPurple, 0.65f),
                Confetti(0.955f, 0.225f, 15f, -35f, BlockPink, 0.70f),
                Confetti(0.02f, 0.96f, 20f, -15f, NeonGreen, 0.80f),
                Confetti(0.95f, 0.965f, 24f, 18f, BlockOrange, 0.85f),
                Confetti(0.06f, 0.55f, 12f, 30f, NeonGold, 0.55f),
                Confetti(0.965f, 0.58f, 14f, -28f, NeonPurple, 0.60f)
            )
            for (cube in cubes) {
                val cx = w * cube.fx
                val cy = h * cube.fy
                val s = cube.sizeDp * density
                rotate(degrees = cube.rotation, pivot = Offset(cx, cy)) {
                    val topLeft = Offset(cx - s / 2f, cy - s / 2f)
                    val corner = androidx.compose.ui.geometry.CornerRadius(s * 0.24f)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                lerp(cube.color, Color.White, 0.35f),
                                cube.color,
                                lerp(cube.color, Color.Black, 0.35f)
                            ),
                            start = topLeft,
                            end = Offset(topLeft.x + s, topLeft.y + s)
                        ),
                        topLeft = topLeft,
                        size = Size(s, s),
                        cornerRadius = corner,
                        alpha = cube.alpha
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.5f * cube.alpha),
                        topLeft = topLeft,
                        size = Size(s, s),
                        cornerRadius = corner,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.07f)
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Faz 44: logo+isim ve jeton/görevler/ayarlar oncede İKİ AYRI satirdi —
            // kullanici "logo ve isim üste taşınmalı ki altta yer açılsın, böyle
            // tasarım kötü gözüküyor" dedi. Artik TEK satirda: logo+isim sola,
            // jeton/görevler/ayarlar saga (LevelMapHeader/oyun ekrani basligindaki
            // ayni desen) — bir satir yuksekliginde alan mod kartlarina geri
            // kazandiriliyor.
            // Faz 60: kullanici ekran goruntusuyle "sagdaki jeton/kupa/ayarlar
            // kumesi sag kenara yapisik degil, bosluk var" dedi — kok neden
            // `weight(1f, fill = false)` idi: fill=false, bu Row'un icerigi
            // (ikon+baslik) kadar KUCULMESINE izin veriyordu, bu yuzden Row
            // kendi payinin tamamini KAPLAMIYOR, hemen ardindan gelen
            // jeton/ayarlar kumesi de sag kenara degil, kucuk Row'un hemen
            // sagina (ekranin ortalarina yakin bir yere) yerlesiyordu.
            // fill=true (varsayilan) ile Row artik kalan TUM genisligi
            // kapliyor, boylece sagdaki kume gercekten sag kenara yapisiyor.
            // "Boom Blocks" sabit/kisa bir metin oldugu icin ellipsis riski yok.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.card)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kaboom Blocks",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                ModeSelectHeaderActions(
                    language = language,
                    tokens = tokens,
                    palette = palette,
                    onOpenMissions = onOpenMissions,
                    onOpenSettings = onOpenSettings
                )
            }

            Text(
                text = language.pick(tr = "Bir oyun modu seç", en = "Choose a game mode", it = "Scegli una modalità di gioco", fr = "Choisissez un mode de jeu", es = "Elige un modo de juego"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary,
                modifier = Modifier.padding(start = 50.dp, top = 2.dp)
            )

            // Faz 71: kullanici "oyun tipi secimi 2x2 olsun, asagi dogru
            // (kaydirma) olmasin" dedi — 4 moda (Sonsuz/Seviyeli/Challenge/
            // Retro) hazirlik olarak dikey tek-sutunluk liste yerine 2x2 grid.
            // Butun modlar kaydirmadan tek bakista goruluyor. Challenge ve
            // Retro henuz oynanabilir olmadigi icin "YAKINDA" kilitli
            // tasarim olarak eklendi — grid'in tamamlanmis gorunmesi ve
            // gelecek modlarin onizlemesi icin.
            // Faz 72: kullanici "kareler olsun, gridde ortalansinlar alttan
            // ustten bosluk esit kalacak sekilde" dedi — eskiden bu Column
            // weight(1f) ile kalan TUM dikey alani zorla dolduruyor, kartlar
            // da fillMaxHeight() ile o alana GERiliyordu (genis dikdortgen).
            // Artik disaridaki Box weight(1f) alani aliyor ama iceride kartlar
            // aspectRatio(1f) ile KARE kalip, Box'un contentAlignment=Center'i
            // sayesinde ust/alt bosluk otomatik esitleniyor.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = language.pick(tr = "SONSUZ", en = "ENDLESS", it = "INFINITA", fr = "INFINI", es = "INFINITO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                            statValue = "$endlessBestScore",
                            icon = Icons.Default.AllInclusive,
                            accent = NeonGreen,
                            palette = palette,
                            onClick = onOpenEndless,
                            testTag = "mode_select_endless_button",
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            iconRes = R.drawable.icon_endless
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ModeCard(
                            // Faz 104: "SEVİYELİ" -> "KARİYER". Eski ad yavan ve islevsel
                            // duruyordu. Kullanici "Rekabetçi/Competitive" onerdi ama o ad
                            // olmayan bir sey vaat ederdi (lider tablosu/rakip/siralama yok,
                            // mod tek kisilik bir bolum haritasi) ve zaten rekabet cagrisimi
                            // tasiyan PRO MOD ile cakisirdi. "Kariyer" ilerleme/yolculuk
                            // fikrini dogru veriyor, PRO ile net bir cift olusturuyor
                            // (KARİYER = normal yolculuk, PRO = zorlu versiyonu) ve 5 dilde
                            // de kisa. NOT: kod icindeki eski yorumlarda gecen "Seviyeli Mod"
                            // ifadesi bu moddur — sadece gorunen metinler degistirildi.
                            title = language.pick(tr = "KARİYER", en = "CAREER", it = "CARRIERA", fr = "CARRIÈRE", es = "CARRERA"),
                            statLabel = language.pick(tr = "EN YÜKSEK SEVİYE", en = "HIGHEST LEVEL", it = "LIVELLO PIÙ ALTO", fr = "NIVEAU LE PLUS HAUT", es = "NIVEL MÁS ALTO"),
                            statValue = "$highestUnlockedLevel",
                            icon = Icons.Default.Extension,
                            accent = NeonCyan,
                            palette = palette,
                            onClick = onOpenLevels,
                            testTag = "mode_select_levels_button",
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            iconRes = R.drawable.icon_career
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        ModeCard(
                            title = language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SEVİYE", en = "HIGHEST LEVEL", it = "LIVELLO PIÙ ALTO", fr = "NIVEAU LE PLUS HAUT", es = "NIVEL MÁS ALTO"),
                            statValue = "$highestChallengeLevel",
                            icon = Icons.Default.LocalFireDepartment,
                            accent = androidx.compose.ui.graphics.Color(0xFFFF6B35),
                            palette = palette,
                            onClick = onOpenChallenge,
                            testTag = "mode_select_challenge_button",
                            locked = false,
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            emoji = "🔥",
                            iconRes = R.drawable.icon_pro
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ModeCard(
                            title = language.pick(tr = "RETRO", en = "RETRO", it = "RETRO", fr = "RÉTRO", es = "RETRO"),
                            statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                            statValue = "$retroHighScore",
                            icon = Icons.Default.SportsEsports,
                            accent = NeonPurple,
                            palette = palette,
                            onClick = onOpenRetro,
                            testTag = "mode_select_retro_button",
                            locked = false,
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            emoji = "🕹️",
                            iconRes = R.drawable.icon_retro
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeSelectHeaderActions(
    language: AppLanguage,
    tokens: Int,
    palette: BlastPalette,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = NeonGold.copy(alpha = 0.18f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .testTag("mode_select_token_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Faz 115i: emoji yerine gercek asset (icon_coin.webp).
                androidx.compose.foundation.Image(
                    painter = painterResource(com.miniappfactory.boomblocks.R.drawable.icon_coin),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$tokens",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGold
                )
            }
        }

        // Faz 44: tek satira sigdirmak icin ikon dugmeleri de (oyun ekrani basligindaki
        // Faz 28 desenine benzer) 48dp varsayilan dokunma alani yerine 36dp'ye daraltildi.
        IconButton(
            onClick = onOpenMissions,
            modifier = Modifier.size(36.dp).testTag("mode_select_missions_button")
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                tint = NeonPurple,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.size(36.dp).testTag("mode_select_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                tint = palette.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Faz 71: 2x2 grid'e gecince kart artik genis/yatay degil, kareye yakin bir
// tile — eski yatay Row (ikon+baslik+alt yazi solda, istatistik sagda) dar
// tile'da sikisip taniz duruyordu. Ikon ortada ustte, baslik altinda, istatistik
// en altta — dikey, ortalanmis bir duzen. `subtitle` kaldirildi (dar tile'da
// yer yok, zaten baslik + ikon modu yeterince anlatiyor). `locked` (Faz 71,
// Challenge/Retro icin) true oldugunda kart soluk gorunuyor, kucuk bir kilit
// rozeti + "YAKINDA" etiketi gosteriyor, tiklama hicbir sey yapmiyor.
@Composable
private fun ModeCard(
    title: String,
    statLabel: String,
    statValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    palette: BlastPalette,
    onClick: () -> Unit,
    testTag: String,
    locked: Boolean = false,
    modifier: Modifier = Modifier,
    // Faz 89: kullanici Pro Mod/Retro icin "renkli" ikon istedi — Icon()
    // composable'i her zaman tek renkte (Color.White) tint ediyor, emoji ise
    // kendi dogal cok-renkli goruntusunu koruyor. Verilirse Icon yerine bu
    // emoji metni gosteriliyor (kilitliyken hala Icons.Default.Lock kullanilir).
    emoji: String? = null,
    // Faz 115f: gorsel yenileme — ChatGPT ile uretilen gercek ikon varliklari
    // (drawable-nodpi/icon_*.webp). Verilirse emoji/Icon'un YERINE gecer;
    // asset uretim istemi docs/asset_prompt_for_image_model.md'de. Kilitliyken
    // hala Icons.Default.Lock kullanilir (asset'lerde kilit hali yok).
    @androidx.annotation.DrawableRes iconRes: Int? = null
) {
    val effectiveAccent = if (locked) palette.textSecondary else accent
    // Faz 72: kullanici "transparan cerceve uzerinde durmasinlar, canli
    // renklerle dolu olsun, zeminleri buton gibi olsun" dedi — kilit acik
    // kartlar artik notr palette.card yerine kendi accent renginin
    // gradyaniyla DOLU (gercek bir buton gibi). Kilitli (Challenge/Retro)
    // kartlar eskisi gibi notr/soluk kaliyor, sadece unlocked kartlar
    // canli dolgu aliyor.
    // Faz 115: KOYU GOVDE + ACCENT KENARLIK/GLOW (v11 mockup yonu).
    //
    // Faz 72'de kullanici "transparan cerceve uzerinde durmasinlar, canli
    // renklerle dolu olsun, zeminleri buton gibi olsun" demisti. 2026-08-16'da
    // mockup'lari gorup "mod sectigimiz kartlara bak, onun yaptiklari mukemmel"
    // dedi — yani koyu govdeli yon ACIKCA secildi. Faz 72 sessizce degil,
    // kullanici talimatiyla geri alindi.
    //
    // Kazanci: doygun dolgu uzerinde beyaz metin ve altin deger birbirini
    // boguyordu; koyu zeminde accent renk kontrast kazaniyor.
    val fillBrush = if (locked) {
        SolidColor(palette.card)
    } else {
        Brush.verticalGradient(
            listOf(
                lerp(palette.card, accent, 0.34f),
                lerp(palette.card, accent, 0.12f),
                palette.card
            )
        )
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
            .background(fillBrush, RoundedCornerShape(20.dp))
            .border(
                2.dp,
                if (locked) {
                    SolidColor(palette.cardBorder)
                } else {
                    // Accent kenarlik: mockup'ta karti tanimlayan asil oge bu.
                    // Ustte parlak, altta soluk — isik yukaridan geliyor hissi.
                    Brush.verticalGradient(
                        listOf(
                            lerp(accent, Color.White, 0.35f),
                            accent,
                            accent.copy(alpha = 0.55f)
                        )
                    )
                },
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = !locked, onClick = onClick)
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Ikonun arkasindaki accent isima — mockup'ta ikon "kendi isigini
            // yayan" bir nesne gibi duruyor. Radyal gradyan, illustrasyon
            // olmadan o hissi veren en ucuz yol.
            if (!locked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = 0.30f), Color.Transparent)
                            )
                        )
                )
            }
            // TASMA UYARISI (2026-08-16'da cihazda yakalandi): kart aspectRatio(1f),
            // yani ~190dp kare. Ilk denemede ikon 58dp + bosluklar 9dp + baslik 16sp
            // + serit toplami ~189dp tutmustu ve istatistik DEGERI (0/1/5) sessizce
            // kirpildi — etiket goruniyor, sayi yok. Asagidaki olculer toplami
            // ~136dp'de tutuyor. Bu blogu buyutursen S8'de (1080x2220) TEKRAR
            // dogrula; ozellikle "EN YUKSEK SEVIYE" / "NIVEAU LE PLUS HAUT" gibi
            // uzun etiketlerle.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ikon kuyusu: accent halka + ic isima
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (locked) effectiveAccent.copy(alpha = 0.12f) else accent.copy(alpha = 0.42f),
                                    if (locked) effectiveAccent.copy(alpha = 0.05f) else accent.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (locked) effectiveAccent.copy(alpha = 0.30f) else accent.copy(alpha = 0.75f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!locked && iconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    } else if (!locked && emoji != null) {
                        Text(text = emoji, fontSize = 24.sp)
                    } else {
                        Icon(
                            imageVector = if (locked) Icons.Default.Lock else icon,
                            contentDescription = null,
                            // Faz 115g: emoji/iconRes yoksa buraya dusen yedek yol —
                            // ayni acik-zemin kontrast kurali burada da gecerli.
                            tint = when {
                                locked -> effectiveAccent
                                palette.card.luminance() > 0.5f -> lerp(accent, Color.Black, 0.35f)
                                else -> Color.White
                            },
                            modifier = Modifier.size(if (locked) 20.dp else 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Faz 115g — HATA DUZELTMESI (kullanici cihazda buldu: "light
                // mode'da kartların yazıları okunmuyor").
                //
                // Kok sebep: baslik rengi `lerp(accent, White, 0.45f)` — koyu
                // temadaki `palette.card` (#1E293B, lacivert) uzerinde parlak
                // duruyordu ama hic test edilmemis LIGHT temada `palette.card`
                // neredeyse beyaz (#EEF2F7) ve baslik da beyaza dogru cekilmis
                // acik bir renk: acik-uzerine-acik, pratikte gorunmuyordu.
                //
                // Duzeltme: zeminin PARLAKLIGINA gore (renk kodlanmis bir
                // "darkMode" bayragina degil — boylece Orman/Okyanus/Gun Batimi/
                // Mor Gece skin'lerinde de doğru davranir) baslik ya beyaza ya
                // siyaha dogru cekilir. Acik zeminde koyu, koyu zeminde acik.
                val isLightCard = palette.card.luminance() > 0.5f
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        locked -> palette.textSecondary
                        isLightCard -> lerp(accent, Color.Black, 0.35f)
                        else -> lerp(accent, Color.White, 0.45f)
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = accent.copy(alpha = if (locked || isLightCard) 0f else 0.75f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                            blurRadius = 20f
                        )
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Istatistik seridi: mockup'taki koyu ic pill. Faz 115g: zemin
                // artik `palette.card`'in tonuna gore DEGISEN bir siyah-alfa
                // karisimi degil, SABIT koyu lacivert — aynı hatanin burada da
                // tekrarlanmasini engelliyor. Acik temada `Color.Black alpha
                // 0.32` sadece orta gri veriyordu (kontrasti yetersiz); sabit
                // koyu zemin, uzerindeki beyaz metnin HER iki temada da ayni
                // kontrasti garanti etmesini sagliyor — light mode icin ayrica
                // dal acmaya gerek yok.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (locked) palette.cardAlt.copy(alpha = 0.55f)
                            else Color(0xFF0F172A).copy(alpha = 0.82f)
                        )
                        .border(
                            1.dp,
                            (if (locked) palette.cardBorder else accent).copy(alpha = 0.42f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statLabel,
                        fontSize = 9.sp,
                        color = if (locked) palette.textSecondary else Color.White.copy(alpha = 0.70f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (statValue.isNotEmpty()) {
                        Text(
                            text = statValue,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (locked) NeonGold else lerp(accent, Color.White, 0.55f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
