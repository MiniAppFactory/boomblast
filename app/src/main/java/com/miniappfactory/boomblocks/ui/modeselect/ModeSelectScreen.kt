package com.miniappfactory.boomblocks.ui.modeselect

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AD_TOKEN_REWARD
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.common.DEFAULT_WANDERING_PIECES
import com.miniappfactory.boomblocks.ui.common.WanderingPiecesBackground
import com.miniappfactory.boomblocks.ui.components.FitToHeight
import com.miniappfactory.boomblocks.ui.components.GamePill
import com.miniappfactory.boomblocks.ui.components.screenBodyTextColor
import com.miniappfactory.boomblocks.ui.components.GoldPillAccent
import com.miniappfactory.boomblocks.ui.components.ChromeAssetButton
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.components.IconMedallion
import com.miniappfactory.boomblocks.ui.components.NeonCard
import com.miniappfactory.boomblocks.ui.theme.AppFontFamily
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.ComfortTeal
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces

// Oyunun asil giris ekrani: kullanici once "Sonsuz Mod" mu "Seviyeli Mod" mu
// oynayacagina karar veriyor.
//
// Faz 158 — GORSEL YENILEME (kullanici: "hic oyun menusu gibi degil",
// "biz sanki websitesi hissi veriyoruz"). Degisen SADECE gorsel katman;
// modlar, istatistikler, navigasyon ve reklam akisi aynen korundu.
//   - duz `palette.background` -> `GameScreenBackground` (gok + kose bloklari
//     + zemin bandi)
//   - baslik satiri -> uygulama ikonu kutucugu + wordmark + jeton pill'i +
//     kupa/dişli TUSLARI (seffaf IconButton yerine gorunur tuslar)
//   - mod kartlari -> `NeonCard` + `IconMedallion`: her mod KENDI rengini
//     kartin her katmaninda tasiyor (kenarlik, zemin tonu, madalyon, baslik)
//
// 6 SKIN: kartlarin mod renkleri (yesil/camgobegi/mor/turkuaz) mod KIMLIGI
// oldugu icin sabit; kart zemini/kenarligi ise `GameSurfaces` uzerinden
// skin'in kendi paletinden turuyor, yani hicbir skinde yabanci durmuyor.
@Composable
fun ModeSelectScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    tokens: Int,
    endlessBestScore: Int,
    highestUnlockedLevel: Int,
    // Faz 77: Pro Mode (eski "Challenge") artik oynanabilir.
    highestChallengeLevel: Int,
    // Faz 128: Comfort Mode (TR "KOLAY MOD") — Retro Modu'nun yerini aldi.
    comfortHighestLevel: Int,
    onOpenLevels: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenChallenge: () -> Unit,
    onOpenComfort: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    // Faz 145: jeton rozetine basinca odullu reklam.
    isWatchAdLoading: Boolean = false,
    onWatchAdForTokens: () -> Unit = {},
    // Faz 166: dogrulanmis internet erisimi. Odullu reklam teklifini gizler;
    // baska hicbir seyi etkilemez. Gerekce: `ConnectivityGate`.
    adsReachable: Boolean = true
) {
    val palette = blastPalette(skin, darkMode)
    val surfaces = rememberGameSurfaces(skin, darkMode)
    // Faz 145: rozete dokununca once onay — reklam dogrudan acilmiyor.
    var showWatchAdDialog by remember { mutableStateOf(false) }

    if (showWatchAdDialog) {
        AlertDialog(
            onDismissRequest = { showWatchAdDialog = false },
            containerColor = palette.card,
            title = {
                Text(
                    text = language.pick(
                        tr = "Reklam izle, jeton kazan",
                        en = "Watch an ad, earn tokens",
                        it = "Guarda un annuncio, guadagna token",
                        fr = "Regarde une pub, gagne des jetons",
                        es = "Mira un anuncio, gana fichas"
                    ),
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            },
            text = {
                Text(
                    // Miktar AD_TOKEN_REWARD'dan geliyor — odul degisirse metin de degisir.
                    text = language.pick(
                        tr = "Kısa bir video açılacak. Sonunda $AD_TOKEN_REWARD jeton kazanacaksın.",
                        en = "A short video will open. You'll earn $AD_TOKEN_REWARD tokens at the end.",
                        it = "Si aprirà un breve video. Alla fine guadagnerai $AD_TOKEN_REWARD token.",
                        fr = "Une courte vidéo va s'ouvrir. Tu gagneras $AD_TOKEN_REWARD jetons à la fin.",
                        es = "Se abrirá un vídeo corto. Ganarás $AD_TOKEN_REWARD fichas al final."
                    ),
                    color = palette.textSecondary
                )
            },
            // Faz 166: ag yoksa "IZLE" hic sunulmaz. Jeton rozeti tiklanabilir
            // kalir (jeton sayisini gostermesi ise yariyor) ama diyalog bu kez
            // sunamayacagi bir odulu vaat etmek yerine nedenini soyluyor.
            confirmButton = {
                if (!adsReachable) {
                    Text(
                        text = language.pick(
                            tr = "Bunun için internet bağlantısı gerekiyor.",
                            en = "This needs an internet connection.",
                            it = "Serve una connessione a internet.",
                            fr = "Une connexion internet est nécessaire.",
                            es = "Se necesita conexión a internet."
                        ),
                        color = palette.textSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                } else
                TextButton(onClick = {
                    showWatchAdDialog = false
                    onWatchAdForTokens()
                }) {
                    Text(
                        text = language.pick(tr = "İZLE", en = "WATCH", it = "GUARDA", fr = "REGARDER", es = "VER"),
                        color = NeonGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWatchAdDialog = false }) {
                    Text(
                        text = language.pick(tr = "VAZGEÇ", en = "CANCEL", it = "ANNULLA", fr = "ANNULER", es = "CANCELAR"),
                        color = palette.textSecondary
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            modifier = Modifier.matchParentSize()
        )
        // Faz 115h/120-124: gezinen oyun parcalari katmani — zemin bloklarinin
        // ONUNDE, icerigin ARKASINDA duruyor.
        //
        // Faz 161 — DEKORASYON METNIN UZERINE GELMEZ. Cihazda turuncu tek kup
        // "Bir oyun modu seç" yazisinin icine giriyordu. Alfa dusurmek bunu
        // COZMEZ: parca gezindigi icin bazen tam harfin uzerinde durur ve
        // okunurluk ANA ekranda kumara birakilamaz. Cozum konum: ust serit
        // (wordmark + alt baslik) dekorasyona KAPALI.
        WanderingPiecesBackground(
            pieces = ModeSelectWanderingPieces,
            modifier = Modifier.matchParentSize()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                // Faz 161: dikey dolgu 16 -> 10dp, yatay 16 -> 14dp. Kazanilan
                // her dp izgaraya gidiyor. Sistem cubuklari zaten
                // MainActivity'de windowInsetsPadding ile disarida.
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Olculer docs/UI_TARGET.md bolum 5'ten (referans cerce 941x1672).
            // ORANLAR kullaniliyor, piksel degil.
            val boxWidth = maxWidth
            val boxHeight = maxHeight

            // UST BAR BUTCESI — bu ekranin en onemli sayisi.
            //
            // Cihazda olculdu: wordmark ortaya alinip buyutuldugunde ust serit
            // ekran yuksekliginin ~%25'ini yiyordu ve izgara eziliyordu.
            // Hedefte ust bar yuksekligin ~%9'u (y 88-240 / 1672). Punto hem
            // genislige hem yuksekliğe bagli; iki satir BITISIK oldugu icin
            // (`tightLines`) blok yuksekligi ~2 x punto.
            //
            // Wordmark VARLIGI 720x511 (en-boy 1.41). Yuksekligi hem ust bar
            // butcesinden hem de GENISLIK payindan turemek zorunda: yalnizca
            // yukseklige baglansaydi dar bir ekranda logo jeton kapsulunun
            // uzerine binerdi. Genislik payi = ekranin ~%30'u.
            // Logo artik ust barin TEK marka ogesi (ayri uygulama ikonu yok),
            // o yuzden genislik payi %30 -> %38.
            val wordmarkHeight = minOf(
                boxHeight.value * 0.135f,
                (boxWidth.value * 0.38f) / KB_LOGO_ASPECT
            ).coerceIn(36f, 96f).dp

            // Mod adlari 4 kart icin TEK punto ile olcekleniyor: en uzun
            // ceviri hangisiyse hepsi ona gore kuculur. Kart kart farkli
            // punto "bozuk dizgi" gibi okunurdu.
            val modeTitles = listOf(
                language.pick(tr = "SONSUZ", en = "ENDLESS", it = "INFINITA", fr = "INFINI", es = "INFINITO"),
                language.pick(tr = "KARİYER", en = "CAREER", it = "CARRIERA", fr = "CARRIÈRE", es = "CARRERA"),
                language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO"),
                language.pick(tr = "KOLAY MOD", en = "COMFORT MODE", it = "MODALITÀ COMFORT", fr = "MODE CONFORT", es = "MODO CONFORT")
            )
            val statLabelLevel = language.pick(
                tr = "EN YÜKSEK SEVİYE",
                en = "HIGHEST LEVEL",
                it = "LIVELLO PIÙ ALTO",
                fr = "NIVEAU LE PLUS HAUT",
                es = "NIVEL MÁS ALTO"
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ---------------------------------------------------------------
                // 1) UST BAR — TEK SATIR: [ikon][wordmark] ... [jeton][kupa][disli]
                //
                //    Ikon ve wordmark TEK MARKA BLOGU: ayri satirlara
                //    bolundugunde ikon oksuz kaliyor, wordmark da ekranin
                //    kimligi olmak yerine basibos bir baslik gibi duruyordu.
                //    Sag taraf arac tuslari; ikisinin arasindaki `weight`
                //    bosluğu wordmark'in tasmasini da engelliyor.
                // ---------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Faz 161 — WORDMARK ARTIK METIN DEGIL, VARLIK.
                    //
                    // "Kaboom Blocks" bir MARKA ADI: bes dilde de ayni, yani
                    // basliklarin metin olma gerekcesi (ceviri) burada gecerli
                    // degil. Varlik gradyani, konturu, kabartmayi ve satir
                    // araligini KENDI ICINDE tasiyor — "renklerinin alakasi
                    // yok" ve "aralarinda kocaman bosluk var" sikayetlerinin
                    // ikisini birden kapatiyor.
                    //
                    // AYRI UYGULAMA IKONU YOK: yeni logo patlayan "B" blogunu
                    // ve cevresindeki renkli bloklari zaten iceriyor. Yanina
                    // bir de `kb_app_icon` koymak ayni amblemi iki kez
                    // gostermek olurdu.
                    //
                    // Ekran BASLIKLARI (AYARLAR, HAFTALIK GOREVLER) metin
                    // tabanli kalmaya devam ediyor; onlar CEVRILIYOR.
                    Image(
                        painter = painterResource(R.drawable.kb_logo),
                        contentDescription = "Kaboom Blocks",
                        modifier = Modifier
                            .height(wordmarkHeight)
                            .aspectRatio(KB_LOGO_ASPECT)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(6.dp))

                    ModeSelectHeaderActions(
                        language = language,
                        tokens = tokens,
                        isWatchAdLoading = isWatchAdLoading,
                        onTokenPillClick = { showWatchAdDialog = true },
                        surfaces = surfaces,
                        onOpenMissions = onOpenMissions,
                        onOpenSettings = onOpenSettings
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Ortalanmis alt baslik, iki yaninda kucuk eskenar dortgen parilti.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubtitleSparkle(surfaces)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = language.pick(
                            tr = "Bir oyun modu seç",
                            en = "Choose a game mode",
                            it = "Scegli una modalità di gioco",
                            fr = "Choisissez un mode de jeu",
                            es = "Elige un modo de juego"
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        // Faz 161: `accentText` (camgobegi) DEGIL. Zemin de ayni
                        // renk ailesinden geldigi icin metin zemine gomuluyordu.
                        // Vurgu rengi kimlik tasir, OKUNAN metin notr kalir.
                        // Parilti kareleri accent'te kaldi — dekorasyon orada.
                        color = screenBodyTextColor(surfaces),
                        textAlign = TextAlign.Center,
                        // TASMA: "Scegli una modalità di gioco" / "Choisissez un
                        // mode de jeu" dar ekranda tek satira sigmaz — iki satira
                        // taser, kirpilmaz. weight ile parilti simgeleri sikismaz.
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SubtitleSparkle(surfaces)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ---------------------------------------------------------------
                // 3) 2x2 mod izgarasi — KALAN ALANIN TAMAMI
                // ---------------------------------------------------------------
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Faz 161 — OLU BOSLUK. Kartlar `aspectRatio(1f)` ile KARE
                    // idi ve kenar SADECE genislikten cikiyordu; dikeyde artan
                    // her sey bosluga gidiyordu. Cihazda olculdu: alt basligin
                    // altinda ~270px, izgaranin altinda ~250px — ekranin dortte
                    // biri bos, kartlar da bu yuzden kucuk.
                    //
                    // Kart artik hucrenin TAMAMINI kapliyor (`weight` ile
                    // yukseklik paylasimi). Tablet YATAY korumasi KAYBOLMADI,
                    // aksine guclendi: kart yuksekligi hucre yuksekliginin
                    // uzerine CIKAMAZ, yani iki sira her zaman tam siger ve
                    // etkilesilebilir hicbir kart ekran disina tasmaz.
                    // Tek ek sinir asiri oran (bkz. `modeGridMetrics`).
                    val metrics = modeGridMetrics(maxWidth, maxHeight)
                    // Mod adi puntosu 4 kart icin TEK sefer, EN UZUN ceviriye
                    // gore ve GERCEK olcumle bulunuyor (bkz. fonksiyonun
                    // basindaki not — karakter butcesi tahmini cihazda
                    // "MODE CONF..." olarak kirpildi).
                    val titleSp = rememberModeTitleFontSize(
                        titles = modeTitles,
                        // Hedefte mod adi ~40px / 480px kart yuksekligi = %8.3.
                        maxFontSize = (metrics.cardHeight.value * 0.095f)
                            .coerceIn(11f, 22f).sp,
                        availableWidth = metrics.cardWidth - ModeCardTextInset
                    )
                    Column(
                        modifier = Modifier
                            .width(metrics.cardWidth * 2 + ModeGridColumnGap)
                            .height(metrics.cardHeight * 2 + ModeGridRowGap)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            ModeCard(
                                title = modeTitles[0],
                                titleFontSize = titleSp,
                                statLabel = language.pick(tr = "EN YÜKSEK SKOR", en = "BEST SCORE", it = "MIGLIOR PUNTEGGIO", fr = "MEILLEUR SCORE", es = "MEJOR PUNTUACIÓN"),
                                statValue = "$endlessBestScore",
                                accent = NeonGreen,
                                surfaces = surfaces,
                                onClick = onOpenEndless,
                                testTag = "mode_select_endless_button",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                iconRes = R.drawable.kb_mode_endless
                            )
                            Spacer(modifier = Modifier.width(ModeGridColumnGap))
                            ModeCard(
                                // Faz 104: "SEVİYELİ" -> "KARİYER".
                                title = modeTitles[1],
                                titleFontSize = titleSp,
                                statLabel = statLabelLevel,
                                statValue = "$highestUnlockedLevel",
                                accent = NeonCyan,
                                surfaces = surfaces,
                                onClick = onOpenLevels,
                                testTag = "mode_select_levels_button",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                iconRes = R.drawable.kb_mode_career
                            )
                        }

                        Spacer(modifier = Modifier.height(ModeGridRowGap))

                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            ModeCard(
                                title = modeTitles[2],
                                titleFontSize = titleSp,
                                statLabel = statLabelLevel,
                                statValue = "$highestChallengeLevel",
                                // Faz 117: Pro mor, Retro turuncuydu (ikon kontrasti).
                                accent = NeonPurple,
                                surfaces = surfaces,
                                onClick = onOpenChallenge,
                                testTag = "mode_select_challenge_button",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                iconRes = R.drawable.kb_mode_pro
                            )
                            Spacer(modifier = Modifier.width(ModeGridColumnGap))
                            // Faz 128: RETRO kartinin yerini COMFORT MODE (TR "KOLAY MOD") aldi.
                            ModeCard(
                                title = modeTitles[3],
                                titleFontSize = titleSp,
                                statLabel = statLabelLevel,
                                statValue = "$comfortHighestLevel",
                                accent = ComfortTeal,
                                surfaces = surfaces,
                                onClick = onOpenComfort,
                                testTag = "mode_select_comfort_button",
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                iconRes = R.drawable.kb_mode_easy
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Faz 161 — IZGARA OLCULERI (saf fonksiyon, testten dogrudan cagriliyor)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Hazir varliklarin en-boy oranlari (genislik / yukseklik).
//
// Varliklar KARE DEGIL ve kendi golge/parlama paylarini tasiyor. Oran elle
// yazilmazsa `Image` kendi ic olcusunu dayatir ve ust bar satirinda tasma
// uretir; `aspectRatio` ile yukseklikten guvenle turetiliyor.
// ---------------------------------------------------------------------------

/**
 * kb_logo — 535 x 380. Ikon + wordmark TEK varlik: patlayan "B" blogu ve
 * cevresindeki renkli bloklar logonun icinde, ayrica uygulama ikonu konmuyor.
 */
internal const val KB_LOGO_ASPECT = 535f / 380f

/** Sutunlar arasi bosluk — hedefte ~32/941 = genisligin %3.4'u. */
internal val ModeGridColumnGap: Dp = 12.dp

/** Satirlar arasi bosluk — hedefte ~50/1672 = yuksekligin %3'u. */
internal val ModeGridRowGap: Dp = 16.dp

/**
 * Hedef mockup'taki kart en-boy orani (genislik / yukseklik).
 * 390 x 480 = 0.8125 — kart KARE DEGIL, enden uzun.
 */
internal const val MODE_CARD_ASPECT = 0.8125f

internal data class ModeGridMetrics(val cardWidth: Dp, val cardHeight: Dp)

/**
 * 2x2 izgaranin kart olculeri.
 *
 * ONCE SEKIL, SONRA SIGDIRMA:
 *  1. Kart hedefteki orani (0.81) alir ve genisligi hucrenin tamamidir.
 *  2. Bu yukseklik alana sigmiyorsa kart YUKSEKLIKTEN turer ve genisligi
 *     ORANI KORUYARAK kuculur.
 *
 * Iki dalda da `cardWidth <= cellWidth` ve `cardHeight <= cellHeight`, yani
 * `2*kart + bosluk <= kullanilabilir olcu`. Kart ekran disina TASAMAZ —
 * eski `aspectRatio(1f)` cozumunun tablet-yatay korumasi burada da var
 * (kisa bir pencerede kartlar oranini koruyarak kuculur), ustelik kartlar
 * artik kareye kilitli olmadigi icin dikeyde olu bosluk birakmiyor.
 *
 * KAYDIRMA YOK: bu ekran hicbir zaman kaydirilmaz, sigdirma tamamen bu
 * fonksiyonun isi.
 */
internal fun modeGridMetrics(
    maxWidth: Dp,
    maxHeight: Dp,
    columnGap: Dp = ModeGridColumnGap,
    rowGap: Dp = ModeGridRowGap
): ModeGridMetrics {
    val cellWidth = ((maxWidth - columnGap) / 2).coerceAtLeast(0.dp)
    val cellHeight = ((maxHeight - rowGap) / 2).coerceAtLeast(0.dp)

    var width = cellWidth
    var height = width / MODE_CARD_ASPECT
    if (height > cellHeight) {
        height = cellHeight
        width = minOf(cellWidth, height * MODE_CARD_ASPECT)
    }
    return ModeGridMetrics(width, height)
}

/** Mod adinin iki yanindaki dolgu (kart ic dolgusu 8dp x 2). */
internal val ModeCardTextInset: Dp = 18.dp

/** Mod adi puntosunun alt siniri — bunun altinda ad okunmaz olurdu. */
internal const val MODE_TITLE_MIN_SP = 10f

/**
 * Verilen kosula uyan EN BUYUK punto.
 *
 * Arama saf tutuldu (olcum disaridan `fits` ile geliyor) ki testten
 * dogrudan cagrilabilsin; gercek cagrida `fits` bir `TextMeasurer` ile
 * GERCEK metin genisligini olcer.
 *
 * NEDEN TAHMIN DEGIL OLCUM: ilk denemede karakter BUTCESI kullanilmisti
 * (12 karakter). Fransizca "MODE CONFORT" tam 12 karakter oldugu icin
 * kucultulmedi ve cihazda "MODE CONF..." diye KIRPILDI. Karakter sayisi
 * genisligin vekili degil: harf genisligi, yazi tipi ve `letterSpacing`
 * hesaba girmiyordu.
 */
internal fun fitFontSize(maxSp: Float, minSp: Float, fits: (Float) -> Boolean): Float {
    var size = maxSp
    while (size > minSp) {
        if (fits(size)) return size
        size -= 1f
    }
    return minSp
}

/**
 * Mod adlarinin ORTAK puntosu: 4 kart icin TEK sefer, EN UZUN cevirisine
 * gore. Kart kart farkli punto "bozuk dizgi" gibi okunurdu.
 */
@Composable
private fun rememberModeTitleFontSize(
    titles: List<String>,
    maxFontSize: TextUnit,
    availableWidth: Dp
): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(titles, maxFontSize, availableWidth, density.density, density.fontScale) {
        val limitPx = with(density) { availableWidth.toPx() }
        if (limitPx <= 0f) return@remember maxFontSize
        fitFontSize(maxFontSize.value, MODE_TITLE_MIN_SP) { candidate ->
            titles.all { title ->
                measurer.measure(
                    text = AnnotatedString(title),
                    style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = candidate.sp,
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 1,
                    softWrap = false
                ).size.width <= limitPx
            }
        }.sp
    }
}

/** Ust serit: wordmark ve alt basligin durdugu bolge (ekranin ust %42'si). */
internal const val HEADER_BAND_BOTTOM = 0.42f

/** Ust seritte parcalarin GIREMEYECEGI orta sutun (metin burada). */
internal const val HEADER_TEXT_LEFT = 0.18f
internal const val HEADER_TEXT_RIGHT = 0.82f

/**
 * Ust seritteki parcalarin kenara pimlendigi konum ve kisitli gezinme yaricapi.
 *
 * FAZ 162 — MADDE 3 ILE BIRLIKTE YARICAP KISILDI (22dp -> 16dp).
 * Pim konumu (`EDGE`) KASITLI OLARAK 0.045'te BIRAKILDI.
 *
 * Parcalar artik duz kare degil 3B kup VARLIGI ve okunabilmeleri icin daha
 * genis ciziliyorlar (bkz. `WanderingPiece.widthDp`). Eski degerlerle en dar
 * desteklenen ekranda (292dp) 4'lu parcanin SAG KENARI x=0.194'e ulasiyordu —
 * yani `HEADER_TEXT_LEFT` (0.18) sinirini asip "Bir oyun modu seç" yazisinin
 * sutununa giriyordu. Merkez sinirda kaliyordu, TASAN sey cizim genisligiydi.
 *
 * ILK DENEMEDE PIM 0.02'YE CEKILDI VE CIHAZDA GERI ALINDI: fy=0.38'deki
 * parcalar (ki bunlar alt basligin cok altinda, karta kadar olan bos
 * seritte duruyorlar) ekran disina kayip kenarda birer "dilim" olarak
 * okunuyordu. Sadece YARICAPI kismak ayni tasma guvencesini veriyor ve
 * parcalarin yerini degistirmiyor:
 *   0.045 + 16/292 + 21.6/292 = 0.174  <  0.18  ✓
 *
 * Yani Faz 161'de konan kural (ust serit dekorasyona kapali) aynen gecerli;
 * olcu buyudugu icin yalnizca salinim genligi daraltildi.
 * ModeSelectLayoutTest artik cizim genisligini de hesaba katiyor.
 */
private const val HEADER_PIECE_EDGE = 0.045f
private const val HEADER_PIECE_RANGE_DP = 16f

/**
 * Ana menunun gezinen parca listesi.
 *
 * Hedef gorselde (docs/ui_mockups/hedef_modsecim.png) UST KOSELERDE de blok
 * var — mavi sag ustte, mor sagda, turuncu solda. Yani parcalari ust seritten
 * TAMAMEN silmek hedeften uzaklastirirdi.
 *
 * Ama cihazda yakalanan hata gercek: turuncu tek kup "Bir oyun modu seç"
 * yazisinin icine giriyordu. Sebep gezinme yaricapiydi — parca fx = 0.08'de
 * duruyor ama +-55dp gezindigi icin metnin uzerine kadar suruklenebiliyordu.
 * Alfa dusurmek bunu COZMEZ: parca hareketli oldugu icin er ya da gec tam
 * harfin uzerinde durur ve okunurluk kumara birakilamaz.
 *
 * Cozum hedefle uyumlu: ust serittteki parcalar EN KENARA pimleniyor ve
 * gezinme yaricaplari kisiliyor; boylece kosede kaliyorlar, metin sutununa
 * (x %18-%82) hicbir salinimda giremiyorlar. Alt seritteki parcalar aynen
 * kaliyor.
 */
internal val ModeSelectWanderingPieces = DEFAULT_WANDERING_PIECES.map { piece ->
    if (piece.fy >= HEADER_BAND_BOTTOM) {
        piece
    } else {
        piece.copy(
            fx = if (piece.fx < 0.5f) HEADER_PIECE_EDGE else 1f - HEADER_PIECE_EDGE,
            rangeDp = minOf(piece.rangeDp, HEADER_PIECE_RANGE_DP)
        )
    }
}

// Alt basligin iki yanindaki kucuk parilti.
//
// Hedef gorselde bunlar EKSENE PARALEL kare degil, 45 derece dondurulmus
// ELMAS. Kare hali "eksik yuklenmis bir ikon" gibi duruyordu.
@Composable
private fun SubtitleSparkle(surfaces: GameSurfaces) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .rotate(45f)
            .clip(RoundedCornerShape(2.dp))
            .background(surfaces.accentPrimary)
    )
}


/**
 * FAZ 186 — ANA MENU CHROME'U HARITALARLA AYNI VARLIKLARI KULLANIYOR.
 *
 * Kullanici: "main menudeki kupa, coin, ayarlar ikonu modlarin icindeki ile
 * ayni olsun."
 *
 * Ana menude jeton kapsulu `GamePill`, iki tus da `GameImageIconButton`
 * ile COMPOSE'DA CIZILIYORDU (koyu gövde + accent kenarlik) ve icine yalnizca
 * kucuk bir ikon (`kb_coin`, `kb_trophy`, `kb_settings`) konuyordu. Harita
 * ekranlari ise Faz 174'te tamamen RASTER varliklara gecmisti
 * (`kb_*_coin`, `kb_*_trophybtn`, `kb_*_setbtn`) -- yani ayni uc oge iki
 * ekranda iki farkli malzemeyle ciziliyordu.
 *
 * Ana menu moda bagli olmadigi icin Kariyer (camgobegi) seti kullaniliyor;
 * menunun kendi lacivert zeminiyle ayni ailede.
 *
 * Olcu: varliklarin govde/bitmap orani 0.806 (bkz.
 * tools/wordart/normalize_chrome.py). Cagiran taraf GOVDE olcusunu verir,
 * isima payi buradan eklenir -- boylece dokunma hedegi ve gorsel boy
 * haritalardakiyle birebir ayni kaliyor.
 */
@Composable
private fun ModeSelectHeaderActions(
    language: AppLanguage,
    tokens: Int,
    surfaces: GameSurfaces,
    isWatchAdLoading: Boolean = false,
    onTokenPillClick: () -> Unit = {},
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Dokunma hedefleri arasi >= 6dp bosluk (48dp kural: tuslar 40dp
        // gorsel, cevrelerindeki bosluklarla birlikte rahat hedef).
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Jeton kapsulu ayni zamanda bir BUTON (Faz 145: odullu reklam).
        // Yukleme sirasinda spinner gosteriyor, tekrar basilamiyor.
        ChromeAssetButton(
            resId = R.drawable.kb_car_coin,
            bodyWidth = 66.dp,
            aspect = 0.510f,
            contentDescription = null,
            onClick = if (isWatchAdLoading) null else onTokenPillClick,
            modifier = Modifier.testTag("mode_select_token_pill")
        ) {
            // Sayi, kapsulun BOS ALANINA oturuyor: madeni para cizimin
            // solunda, govdenin ilk %42'sinde. Bos alanin merkezi bitmap
            // merkezinden +0.195 x govde kadar sagda (haritadaki olcumun
            // aynisi, bkz. ProgressionMapScreen.Ref.COIN_TEXT_X).
            Box(
                modifier = Modifier.offset(x = 66.dp * 0.195f),
                contentAlignment = Alignment.Center
            ) {
                if (isWatchAdLoading) {
                    // Faz 43 dersi: reklam yuklemesi 3-8 sn surebiliyor,
                    // sessiz bekleme "buton bozuk" hissi veriyor.
                    CircularProgressIndicator(
                        color = NeonGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    // FAZ 188: sayi RASTER bir kapsulun icinde duruyor, o
                    // yuzden punto dp'ye kilitli. `sp` birakilirsa sistem
                    // yazi boyutu ayari sayiyi buyutur ama kapsulu buyutmez
                    // ve sayi disari tasar (haritada tabletten gelen hata
                    // tam olarak buydu; telefonda font_scale=1.3 ile uretildi).
                    Text(
                        text = "$tokens",
                        fontSize = with(LocalDensity.current) { 13.dp.toSp() },
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

        ChromeAssetButton(
            resId = R.drawable.kb_car_trophybtn,
            bodyWidth = 40.dp,
            aspect = 0.970f,
            contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
            onClick = onOpenMissions,
            modifier = Modifier.testTag("mode_select_missions_button")
        )

        ChromeAssetButton(
            resId = R.drawable.kb_car_setbtn,
            bodyWidth = 40.dp,
            aspect = 1.000f,
            contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
            onClick = onOpenSettings,
            modifier = Modifier.testTag("mode_select_settings_button")
        )
    }
}

// Mod karti: madalyon + baslik + istatistik kapsulu.
//
// TASMA (2026-08-16'da cihazda yakalanmisti): kart aspectRatio(1f) yani
// ekran genisligine BAGLI bir kare. 320dp bir ekranda kart ~136dp'ye duser
// ve sabit olculer tasar — o zaman istatistik DEGERI sessizce kirpilmisti.
// Bu yuzden olculer artik SABIT DEGIL: madalyon ve yazi boyutlari
// BoxWithConstraints ile kartin gercek yuksekligine gore olcekleniyor.
// Boylece 320dp telefonda da, tablet yatay modunda da tasma olmuyor.
@Composable
private fun ModeCard(
    title: String,
    statLabel: String,
    statValue: String,
    accent: Color,
    surfaces: GameSurfaces,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    // Punto DISARIDAN geliyor: 4 kart ayni puntoyu paylasmak zorunda ve
    // en uzun ceviriye gore GERCEK olcumle bulunuyor (bkz. fitFontSize).
    titleFontSize: TextUnit = 16.sp,
    locked: Boolean = false,
    @androidx.annotation.DrawableRes iconRes: Int? = null
) {
    val effectiveAccent = if (locked) surfaces.panelBorder else accent
    NeonCard(
        surfaces = surfaces,
        accent = effectiveAccent,
        glow = if (locked) 0f else 1f,
        cornerRadius = 20.dp,
        contentPadding = 0.dp,
        onClick = if (locked) null else onClick,
        modifier = modifier.testTag(testTag)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Ic olculer docs/UI_TARGET.md bolum 5.4'ten, kart 390x480 kabul
            // edilerek. Madalyon kart GENISLIGINDEN, yazilar kart
            // YUKSEKLIGINDEN turuyor — hedefte oyle olculdu.
            val cardHeight = maxHeight
            val cardWidth = maxWidth
            // Hedef: cap ~195 / 390 genislik = %50.
            val medallion = (cardWidth * 0.54f).coerceIn(36.dp, 128.dp)
            val titleSp = titleFontSize
            // Hedef: deger ~48 / 480 = %10, etiket ~22 / 480 = %4.6.
            val valueSp = (cardHeight.value * 0.100f).coerceIn(13f, 26f).sp
            val labelSp = (cardHeight.value * 0.050f).coerceIn(8.5f, 12f).sp

            // TASMA SINIFI OLARAK COZULUYOR: yukaridaki oranlar 1.0 fontScale
            // icin dengeli, ama sistemde buyuk yazi tipi secili bir cihazda
            // (fontScale 1.3) ya da cok kisa bir kartta toplam icerik kart
            // yuksekligini asabilir. `FitToHeight` icerigi kartin icine
            // sigdirir; tek tek deger kirpmaya gerek yok.
            FitToHeight(modifier = Modifier.fillMaxSize(), minScale = 0.62f) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Faz 161 — MADALYON ARTIK VARLIGIN KENDISI.
                //
                // Yeni mod varliklari TAM BIR MADALYON: 360x360 tuval, disk
                // merkezi (180,180), cap 292 — dordu de birebir ayni. (Onceki
                // surumde tuval yukseklikleri 350/350/315/315 idi ve kullanici
                // "Kolay Mod kaymis" diye yakalamisti.)
                //
                // Bu yuzden `IconMedallion` artik yalnizca KILITLI durumun
                // yer tutucusu: varligin ustune bir de kod ciziminden disk +
                // halka koymak ayni halkayi iki kez gostermek olurdu.
                //
                // HIZA KURALI: dordu de AYNI kutuya konuyor. Farkli boyut
                // verilirse varliklarin sagladigi hiza yeniden bozulur.
                if (!locked && iconRes != null) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(medallion)
                    )
                } else {
                    IconMedallion(
                        accent = effectiveAccent,
                        size = medallion,
                        dimmed = locked
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = surfaces.panelBorder,
                            modifier = Modifier.size(medallion * 0.45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Faz 115g dersi: baslik rengi ZEMININ parlakligina gore
                // secilir, sabit degil — acik temada accent+beyaz okunmuyordu.
                val titleColor = when {
                    locked -> surfaces.hairline
                    surfaces.isLightSurface -> lerp(accent, Color.Black, 0.45f)
                    else -> lerp(accent, Color.White, 0.55f)
                }
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = AppFontFamily,
                        fontSize = titleSp,
                        fontWeight = FontWeight.Black,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = accent.copy(alpha = if (locked || surfaces.isLightSurface) 0f else 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 0f),
                            blurRadius = 22f
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Istatistik kapsulu: koyu ic kutu + kucuk etiket + buyuk sayi.
                // Zemin SABIT koyu degil, `surfaces.sunken` — yani acik temada
                // da koyu-uzerine-beyaz degil, temaya uygun bir oyuk yuzey.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaces.sunken.copy(alpha = 0.85f))
                        .border(
                            1.dp,
                            effectiveAccent.copy(alpha = 0.45f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 5.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-2).dp)
                ) {
                    // Faz 161: etiket OKUNMASI gereken bir metin, dekorasyon
                    // degil — alfa 0.72 -> 0.88. Renk yine zeminden turuyor
                    // (`sunken` acik temada acik), sabit beyaz gomulmuyor.
                    val onWell = if (surfaces.sunken.luminance() > 0.45f) {
                        Color(0xFF12161F).copy(alpha = 0.88f)
                    } else {
                        Color.White.copy(alpha = 0.88f)
                    }
                    Text(
                        text = statLabel,
                        fontSize = labelSp,
                        color = onWell,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (statValue.isNotEmpty()) {
                        Text(
                            text = statValue,
                            fontSize = valueSp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (locked) NeonGold else lerp(accent, Color.White, 0.6f),
                            maxLines = 1
                        )
                    }
                }
            }
            }
        }
    }
}
