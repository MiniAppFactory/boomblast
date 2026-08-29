package com.miniappfactory.boomblocks.ui.shop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AD_TOKEN_REWARD
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.BoosterType
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.components.GameBackButton
import com.miniappfactory.boomblocks.ui.components.GameButton
import com.miniappfactory.boomblocks.ui.components.FitToHeight
import com.miniappfactory.boomblocks.ui.components.GameCoinPill
import com.miniappfactory.boomblocks.ui.components.GameIconTile
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.components.GameScrollHint
import com.miniappfactory.boomblocks.ui.components.GameTitle
import com.miniappfactory.boomblocks.ui.components.NeonCard
import com.miniappfactory.boomblocks.ui.components.gameButtonColors
import com.miniappfactory.boomblocks.ui.components.mutedGameButtonColors
import com.miniappfactory.boomblocks.ui.components.primaryGameButtonColors
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.readableOn
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces

/**
 * Pre-level loadout screen: lets the player review the level target, spend
 * Game Tokens on boosters, watch an ad for bonus tokens, then start the level.
 *
 * Faz 158 — gorsel yenileme: duz zemin -> `GameScreenBackground`; duz Material
 * Button -> kabartmali `GameButton`; "20 🪙" emoji -> `GameCoinPill`;
 * emoji ikon -> `GameIconTile`. Fiyat, etki ve KARSILANABILIRLIK artik ayni
 * kartta: karsilanamayan booster GIZLENMIYOR, sebebi yaziliyor.
 *
 * Ekonomi/denge DEGISMEDI: fiyatlar `BoosterType.tokenPrice`ten, odul
 * `AD_TOKEN_REWARD`tan geliyor.
 */
@Composable
fun LoadoutScreen(
    levelNumber: Int,
    targetScore: Int,
    // Faz 94: dar parametreler — cagiran taraf kendi booster envanterini gecirir.
    tokens: Int,
    ownedBoosters: Map<BoosterType, Int>,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onBuyBooster: (BoosterType) -> Unit,
    onWatchAdForTokens: () -> Unit,
    // Faz 43: reklam yuklemesi 3-8 sn surebiliyor; gorsel geri bildirim sart.
    isWatchAdLoading: Boolean = false,
    // Faz 166: dogrulanmis internet erisimi. Odullu reklam teklifini gizler;
    // baska hicbir seyi etkilemez. Gerekce: `ConnectivityGate`.
    adsReachable: Boolean = true,
    onStartLevel: () -> Unit,
    onBack: () -> Unit
) {
    val surfaces = rememberGameSurfaces(skin, darkMode)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameBackButton(
                    onClick = onBack,
                    surfaces = surfaces,
                    contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                    modifier = Modifier.testTag("loadout_back_button")
                )

                // Faz 25: 3-parcali SpaceBetween Row dar ekranlarda cakisiyordu —
                // orta sutun kalan alani paylasip gerekirse metni kisaltiyor,
                // jeton kapsulunden asla alan calmiyor.
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    GameTitle(
                        text = language.pick(tr = "SEVİYE $levelNumber", en = "LEVEL $levelNumber", it = "LIVELLO $levelNumber", fr = "NIVEAU $levelNumber", es = "NIVEL $levelNumber"),
                        surfaces = surfaces,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                    Text(
                        text = language.pick(tr = "HEDEF: $targetScore", en = "TARGET: $targetScore", it = "OBIETTIVO: $targetScore", fr = "OBJECTIF : $targetScore", es = "OBJETIVO: $targetScore"),
                        fontSize = 11.sp,
                        color = surfaces.hairline,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                GameCoinPill(amount = "$tokens", surfaces = surfaces, fontSize = 14.sp)
            }

            // Faz 159: reklam / "Ucretsiz Jeton" bolumu S8'de ekranin disinda
            // kaliyor, gormek icin kaydirmak gerekiyordu — yani jeton KAZANMA
            // yolu gorunmezdi. Ustelik oyuncunun bakiyesi yetmedigi anda tam
            // da o bolumu gormesi gerekiyor. Dikey bosluklar kisaldi.
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = language.pick(tr = "TAKIMINI HAZIRLA", en = "PREPARE YOUR LOADOUT", it = "PREPARA IL TUO EQUIPAGGIAMENTO", fr = "PRÉPARE TON ÉQUIPEMENT", es = "PREPARA TU EQUIPO"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = surfaces.accentText,
                // TASMA: "PREPARA IL TUO EQUIPAGGIAMENTO" (IT) dar ekranda tek
                // satira sigmaz — iki satira taser, kirpilmaz.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ==== FAZ 160 — OTOMATIK SIGDIRMA ====
            //
            // CIHAZDA YAKALANAN HATA: icerik kullanilabilir yuksekligi BIRKAC
            // PIKSEL asiyordu. Kucuk tasma, orantisiz cirkin sonuc: kaydirma
            // devreye giriyor ve kaydirma ipucu chevron'u "REKLAM IZLE"
            // butonunun UZERINE binip onu kismen kapatiyordu.
            //
            // Elle 12dp bosluk kismak YALNIZCA bu cihazi cozerdi. Gezinme
            // cubugu olan bir telefon, baska bir ekran orani, buyuk sistem
            // yazi tipi ya da daha uzun bir ceviri geldigi anda tasma geri
            // gelirdi. `FitToHeight` sorunu sinifi olarak cozuyor: icerik
            // once dogal boyutunda olculur, sigmiyorsa orantili kucultulur.
            //
            // Kartlar TEK yerde tanimli: iki varyant (sigan / kaydirilan)
            // ayni icerigi paylasiyor, ikisi ayrisamaz.
            val cards: @Composable ColumnScope.() -> Unit = {
                BoosterType.entries.forEach { type ->
                    BoosterCard(
                        type = type,
                        owned = ownedBoosters[type] ?: 0,
                        canAfford = tokens >= type.tokenPrice,
                        language = language,
                        surfaces = surfaces,
                        onBuy = { onBuyBooster(type) }
                    )
                }

                // Faz 166: ag yokken bu kart hic cizilmez — basildiginda kesin
                // basarisiz olacak bir jeton teklifi sunmak yerine.
                if (adsReachable) {
                    WatchAdCard(
                        language = language,
                        surfaces = surfaces,
                        isLoading = isWatchAdLoading,
                        onWatchAdForTokens = onWatchAdForTokens
                    )
                }
            }

            FitToHeight(modifier = Modifier.weight(1f).fillMaxWidth()) { fits ->
                if (fits) {
                    // SIGDI: kaydirma da chevron da YOK. Chevron'un butona
                    // binmesi tam olarak burada bitiyor — "gerekmiyorsa hic
                    // cizilmesin" kurali duzen seviyesinde uygulaniyor.
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        content = cards
                    )
                } else {
                    // Alt sinirda (0.85) bile sigmadi — ESKI davranisa donuluyor.
                    // Ozellik sigdiramadiginda zarar vermez, kaydirmaya duser.
                    //
                    // CHEVRON ARTIK ICERIGIN USTUNE BINMIYOR.
                    //
                    // Kullanicinin sikayeti "kaydirma cikiyor" degil, chevron'un
                    // "REKLAM IZLE" butonunun UZERINE binip onu kapatmasiydi.
                    // `GameScrollHint` bir Box'in BottomCenter'ina cizilen 34dp'lik
                    // OPAK bir daire; kaydirma kutusunun uzerine kondugunda o an
                    // alt kenarda ne varsa onu orter — ve orasi cogu zaman bir
                    // BUTON oluyordu. 1080x1920'de bu birebir tekrar uretildi.
                    //
                    // Icerige alt dolgu eklemek YETMEZ: kaydirma 0 konumundayken
                    // alt kenarda yine bir buton durur. Tek garantili cozum
                    // bindirmeyi BIRAKMAK — chevron kendi seridinde, kaydirma
                    // penceresinin ALTINDA duruyor. 34dp'lik bedel yalnizca
                    // kaydirmaya zaten dusulmus durumda odeniyor; sigan
                    // ekranlarda serit hic olusmuyor.
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            cards()
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            GameScrollHint(
                                visible = scrollState.canScrollForward,
                                surfaces = surfaces
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Birincil aksiyon: ekranin en agir butonu. Sabit yukseklik YOK —
            // uzun ceviriler butonu uzatir.
            //
            // NEDEN skin accent'i DEGIL de sabit YESIL:
            // Kullanici cihazda "basla tusu algisini kaybetti, ustundekiler o
            // kadar buyuk ki ben bile zor buldum" dedi. Hiyerarsi ters donmustu:
            // booster butonlari (ham turuncu/altin, doygun) ile reklam butonu
            // (NeonGold) ekranin en gur ogeleriydi; BASLA ise skin accent'inden
            // (DEFAULT'ta camgobegi-mor) besleniyordu ve o mavi aile, yanindaki
            // sicak doygun butonlarin yaninda GERI CEKILIYOR.
            //
            // Yesil bu ekranda baska hicbir yerde kullanilmiyor, yani rengin
            // kendisi "birincil eylem" sinyali tasiyor; ayrica "devam/basla"
            // evrensel olarak yesil. Punto/yukseklik/derinlik de booster
            // butonlarinin USTUNE cikarildi ki renk tek basina tasimasin.
            GameButton(
                text = language.pick(tr = "BAŞLA", en = "START", it = "INIZIA", fr = "COMMENCER", es = "EMPEZAR"),
                onClick = onStartLevel,
                // DESIGN_SPEC'teki SONSUZ modu yesili (#7CFF3A). Onceki
                // `NeonGreen` (#4ADE80) kullanicinin sozuyle "cok mat"
                // kaliyordu — bu ton daha parlak ve oyunun kendi mod
                // renkleriyle ayni aileden, yani ekranda yabanci durmuyor.
                colors = gameButtonColors(Color(0xFF7CFF3A)),
                fontSize = 20.sp,
                minHeight = 62.dp,
                depth = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loadout_start_button")
            )
        }
    }
}

@Composable
private fun BoosterCard(
    type: BoosterType,
    owned: Int,
    canAfford: Boolean,
    language: AppLanguage,
    surfaces: GameSurfaces,
    onBuy: () -> Unit
) {
    // Her tur kendi vurgu rengini tasiyor, ikonun anlamiyla eslesecek sekilde
    // (bomba -> turuncu/kirmizi, simsek -> altin).
    val role = when (type) {
        BoosterType.BOMB -> Color(0xFFFF6B35)
        BoosterType.LINE_CLEAR -> NeonGold
    }
    // ==== FAZ 160 — DOLGU ROL RENGININ KENDISI, SKIN'E CEKILMIS HALI DEGIL ====
    //
    // CIHAZDA YAKALANAN HATA: "SATIN AL" butonlari pastele yikanmis cikiyordu
    // (BOMBA soluk somon, SATIR TEMIZLE haki/zeytin), ama AYNI ekrandaki
    // "REKLAM IZLE" butonu doygun altin duruyordu. Ikisi de ayni
    // `gameButtonColors` bilesenini kullaniyor — yani bilesen saglamdi,
    // BESLENEN RENK bozuktu.
    //
    // Fark tek satirdi: reklam butonu rol rengini HAM veriyordu
    // (`gameButtonColors(NeonGold)`), booster ise once `roleTint`ten
    // geciriyordu. `roleTint` rengi %35 skin accent'ine cekiyor; DEFAULT
    // skin'de accent CAMGOBEGI, yani rol renginin TAMAMLAYICISI. Tamamlayici
    // iki renk karisinca sonuc griye dogru coker:
    //     altin  #FACC15 (H=48 S=92%) -> #BFC87F (H=68 S=36%)  = zeytin
    //     turuncu #FF6B35 (H=16 S=79%) -> #CD9079 (H=16 S=41%) = soluk somon
    // Ekran goruntusunden olculen piksel bunu birebir dogruladi (H=67.9,
    // S=32% — simulasyon H=67.7, S=36.8%).
    //
    // ONEMLI: bir onceki tur `gameButtonColors` icindeki parlatma dongusunu
    // ters cevirerek duzeltmeye calisti ama duzelmedi, cunku HATA ORADA
    // DEGILDI: altin icin o dongu SIFIR adim calisiyor. Zarar dongunun
    // ONUNDE, bu satirda olusuyordu.
    //
    // `roleTint` YERINDE KALIYOR — dekoratif ikonlari skin'e uydurmak icin
    // doğru arac (Ayarlar/Gorevler onu kullanmaya devam ediyor). Yanlis olan,
    // onu bir DOLGU uretmek icin kullanmakti. Booster karti artik reklam
    // kartiyla ayni receteyi izliyor: rol rengi ham gider, doygunlugu ve
    // hue'su korunur. Skin kimligi zaten panel/zemin/parlamadan geliyor.
    val accent = role
    // Faz 159 — OYUNUN ICINDEKI VARLIKLARIN AYNISI.
    //
    // Bu ekran Material vektor muadillerini (Whatshot alevi / Bolt simsegi)
    // kullaniyordu; oyunun icinde ise (`BoomBlocksGame`, booster tepsisi)
    // gercek illustrasyonlar var. Ayni booster iki farkli gorselle
    // gorununce oyuncu ikisini AYNI SEY olarak baglayamiyordu.
    //
    // KURAL: bir varlik oyunun icinde zaten kullaniliyorsa, menude onun
    // vektor muadili KONMAZ.
    //
    // Bu illustrasyonlar kendi golgesini/hacmini tasiyor — `GameIconTile`in
    // drawable asiri yuklemesi bilerek `ColorFilter.tint` UYGULAMIYOR,
    // aksi halde duzlesirlerdi.
    val iconRes = when (type) {
        BoosterType.BOMB -> R.drawable.icon_bomb
        BoosterType.LINE_CLEAR -> R.drawable.icon_line_clear
    }
    val name = when (type) {
        BoosterType.BOMB -> language.pick(tr = "BOMBA", en = "BOMB", it = "BOMBA", fr = "BOMBE", es = "BOMBA")
        BoosterType.LINE_CLEAR -> language.pick(tr = "SATIR TEMİZLE", en = "LINE CLEAR", it = "ELIMINA LINEA", fr = "EFFACER LIGNE", es = "LIMPIAR LÍNEA")
    }
    // Faz 159 — ACIKLAMALAR KODDAN DOGRULANDI.
    //
    // `BoomBlocksGame.applyBoosterAt` (salt okunur incelendi):
    //   BOMB       -> dr/dc = -1..1, yani dokunulan hucrenin cevresindeki
    //                 3x3 alani siler. Satir sayaci ARTMAZ.
    //   LINE_CLEAR -> `for (c2 in 0 until gridSize) board[row * gridSize + c2] = 0`
    //                 yani SADECE dokunulan SATIR silinir; SUTUN SILINMEZ.
    //                 Ayrica `onLinesCleared(1)` cagriliyor, yani temizlenen
    //                 satir sayacina 1 ekleniyor.
    //
    // ONCEKI METIN YANLISTI: bes dilde de "bir satır/sütunu" (row/column)
    // diyordu — uygulamada olmayan bir yetenek vaat ediyordu. Bu projede daha
    // once de magaza metnine olmayan ozellikler yazilmisti; metin artik
    // davranisla birebir.
    val description = when (type) {
        BoosterType.BOMB -> language.pick(
            tr = "Dokunduğun yerin çevresindeki 3x3 alanı siler",
            en = "Clears the 3x3 area around the cell you tap",
            it = "Elimina l'area 3x3 attorno alla cella che tocchi",
            fr = "Efface la zone 3x3 autour de la case touchée",
            es = "Borra el área 3x3 alrededor de la casilla que tocas"
        )
        BoosterType.LINE_CLEAR -> language.pick(
            tr = "Dokunduğun satırın tamamını siler",
            en = "Clears the entire row you tap",
            it = "Elimina l'intera riga che tocchi",
            fr = "Efface toute la ligne touchée",
            es = "Borra toda la fila que tocas"
        )
    }

    NeonCard(
        surfaces = surfaces,
        accent = accent,
        glow = if (canAfford) 0.9f else 0.35f,
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = 10.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ikon + etki semasi alt alta: "bu nedir" ve "ne yapar" yan yana
            // degil, TEK bir gorsel sutunda.
            // Faz 159 — "Sahip:" ETIKETI KALDIRILDI.
            //
            // Kullanicinin sozu: "Sahip yazisi sacma oldu sanki." Haklıydi:
            // "Sahip" tek basina TR'de eksik bir cumle gibi okunuyor ("sahip"
            // neye?) — "Owned" kaliginin dogrudan cevirisiydi ve TR'de
            // calismiyordu. Ayni gariplik bes dilde birden vardi.
            //
            // Yerine ikonun uzerinde METINSIZ bir adet rozeti: "x2". Ceviri
            // gerektirmiyor, bir satir dikey alan kazandiriyor ve bu projede
            // zaten tercih edilen metinsiz anlatimla tutarli.
            //
            // Sifirken rozet HIC cizilmiyor: "x0" gurultu olurdu, elinde bir
            // sey olmamasi zaten varsayilan durum.
            Box {
                GameIconTile(iconRes = iconRes, tint = accent, size = 44.dp)
                if (owned > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 5.dp, y = 5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(lerp(accent, Color.Black, 0.25f))
                            .border(1.5.dp, lerp(accent, Color.White, 0.55f), RoundedCornerShape(50))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "×$owned",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = readableOn(lerp(accent, Color.Black, 0.25f)),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Faz 79: "Sahip: N" rozeti ADIN ALTINDA kendi satirinda —
                // isim tam genisligi kullanabiliyor.
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (surfaces.isLightSurface) Color(0xFF12161F) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Etki semasi ACIKLAMANIN YANINDA. Onceden ikonun altinda
                // ayri bir satirdaydi ve kartin yuksekligini ~50dp
                // buyutuyordu; reklam/ucretsiz jeton bolumu bu yuzden ekranin
                // disina tasip kaydirma gerektiriyordu.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BoosterEffectDiagram(
                        type = type,
                        accent = accent,
                        surfaces = surfaces,
                        size = 30.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        // Faz 159: `hairline` bir KENARLIK tonu (yari saydam
                        // koyu gri) — aciklama metni olarak kart uzerinde
                        // neredeyse okunmuyordu. Ikincil metin tonu artik
                        // zeminin parlakligina gore.
                        color = if (surfaces.isLightSurface) Color(0xFF2A3242) else Color.White.copy(alpha = 0.78f),
                        // TASMA: aciklamalar 5 dilde uzun — iki satira taser,
                        // kart uzar, kirpilmaz.
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!canAfford) {
                    // Karsilanamayan secenek GIZLENMEZ, sebebiyle gosterilir.
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = surfaces.hairline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = language.pick(
                                tr = "Yetersiz jeton",
                                en = "Not enough tokens",
                                it = "Token insufficienti",
                                fr = "Jetons insuffisants",
                                es = "Fichas insuficientes"
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = surfaces.hairline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

        }

        // ==== FAZ 159 — FIYAT ARTIK BUTONUN ICINDE ====
        //
        // CIHAZDA YAKALANAN HATA: fiyat kapsulu ve "SATIN AL" butonu satirin
        // SAGINDA, agirliksiz bir sutundaydi. `GameButton`in ic Row'u
        // `fillMaxWidth()` kullaniyor ve Compose'da bir Row once AGIRLIKSIZ
        // cocuklari olcup onlara kalan TUM genisligi verir — buton satirin
        // tamamini yutuyor, `weight(1f)` olan metin sutununa SIFIR genislik
        // kaliyordu. Booster'in adi ve aciklamasi bu yuzden ekranda hic
        // gorunmuyordu; kullanicinin "ne satin aldigin anlasilmiyor"
        // sikayetinin kok nedeni buydu. (Regresyon testi:
        // MenuOverflowTest."booster name keeps its width next to a buy button")
        //
        // Yeni duzen aynı zamanda kullanicinin istedigi simetriyi kuruyor:
        // NE ALDIGIN ustte, NE ODEDIGIN butonun icinde — reklam kartiyla
        // birebir ayni format.
        Spacer(modifier = Modifier.height(8.dp))
        GameButton(
            onClick = onBuy,
            colors = if (canAfford) {
                gameButtonColors(accent)
            } else {
                mutedGameButtonColors(surfaces, surfaces.hairline)
            },
            enabled = canAfford,
            // Faz 160: 46dp -> 48dp. Dokunma hedefi alt siniri 48dp; eski
            // deger kurali 2dp ihlal ediyordu. `FitToHeight` bu yuksekligi
            // kucultmez (bkz. GameButton/LocalFitScale), yani sigdirma
            // devreye girse bile hedef 48dp kalir.
            minHeight = 48.dp,
            depth = if (canAfford) 4.dp else 2.dp,
            cornerRadius = 12.dp,
            horizontalPadding = 12.dp,
            modifier = Modifier.fillMaxWidth().testTag("buy_booster_${type.name}")
        ) {
            val labelColors = if (canAfford) {
                gameButtonColors(accent)
            } else {
                mutedGameButtonColors(surfaces, surfaces.hairline)
            }
            Text(
                text = language.pick(tr = "SATIN AL", en = "BUY", it = "ACQUISTA", fr = "ACHETER", es = "COMPRAR"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = labelColors.content,
                // TASMA: "ACQUISTA" / "ACHETER" uzun — buton sabit yukseklikli
                // DEGIL, gerekirse iki satira taser.
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.icon_coin),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "${type.tokenPrice}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = labelColors.content,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WatchAdCard(
    language: AppLanguage,
    surfaces: GameSurfaces,
    isLoading: Boolean,
    onWatchAdForTokens: () -> Unit
) {
    // Tamami altin/odul temasi: hediye ikonu + altin renk, odulle dogrudan
    // gorsel baglanti.
    val accent = NeonGold
    NeonCard(
        surfaces = surfaces,
        accent = accent,
        glow = 1f,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watch_ad_tokens"),
        cornerRadius = 16.dp,
        contentPadding = 10.dp,
        onClick = if (isLoading) null else onWatchAdForTokens
    ) {
        // Faz 159 — "izle butonu var ama ne ise yaradigi yok" (kullanici).
        //
        // TESHIS: odul miktari AYRI bir baslik satirindaydi, butonda ise
        // yalniz "IZLE" yaziyordu. Goz butona gidiyor, buton hicbir sey
        // soylemiyordu. Ustelik buton dar bir sutunda sikisikti.
        //
        // COZUM: buton ARTIK KENDI KENDINI ANLATIYOR — tam genislikte, uzerinde
        // jeton ikonu ve kazanilacak miktar. Baslik da kisaldi, cunku miktari
        // artik buton soyluyor.
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameIconTile(icon = Icons.Filled.CardGiftcard, tint = accent, size = 44.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.pick(
                        tr = "Ücretsiz Jeton",
                        en = "Free Tokens",
                        it = "Token Gratis",
                        fr = "Jetons Gratuits",
                        es = "Fichas Gratis"
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    // Faz 147: sabit NeonGold acik temada okunmuyordu — metin
                    // rengi zeminin parlakligina gore.
                    color = if (surfaces.isLightSurface) Color(0xFF12161F) else Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    // Ne olacagi acikca yaziyor: KISA BIR VIDEO acilacak ve
                    // SONUNDA jeton kazanilacak. Mevcut onay diyaloguyla
                    // (Faz 145) tutarli.
                    text = language.pick(
                        // CIHAZDA GORULEN (FR): iki satira tasinca sayfa
                        // birkac piksel kayiyor ve "asagida devami var"
                        // chevron'u butonun UZERINE biniyordu. Metin bes dilde
                        // birden kisaltildi — "kisa video" ve "jeton kazanma"
                        // bilgisi korundu.
                        tr = "Kısa bir video izle, $AD_TOKEN_REWARD jeton kazan",
                        en = "Watch a short video, earn $AD_TOKEN_REWARD tokens",
                        it = "Guarda un breve video, ottieni $AD_TOKEN_REWARD token",
                        fr = "Regardez une courte vidéo, gagnez $AD_TOKEN_REWARD jetons",
                        es = "Mira un video corto, gana $AD_TOKEN_REWARD fichas"
                    ),
                    fontSize = 11.sp,
                    // Booster aciklamasiyla AYNI ikincil metin tonu; `hairline`
                    // bir kenarlik rengiydi ve kart uzerinde okunmuyordu.
                    color = if (surfaces.isLightSurface) Color(0xFF2A3242) else Color.White.copy(alpha = 0.78f),
                    // TASMA: FR/IT en uzun hali — uc satira kadar uzayabilir,
                    // kirpilmaz.
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isLoading) {
                Spacer(modifier = Modifier.width(10.dp))
                CircularProgressIndicator(
                    color = if (surfaces.isLightSurface) lerp(accent, Color.Black, 0.4f) else accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (!isLoading) {
            Spacer(modifier = Modifier.height(10.dp))
            // Tam genislikte, kendi kendini anlatan buton: eylem + jeton
            // ikonu + kazanc. Ayri bir baslik okumaya gerek kalmiyor.
            GameButton(
                onClick = onWatchAdForTokens,
                colors = gameButtonColors(accent),
                // Faz 160: 46dp -> 48dp. Dokunma hedefi alt siniri 48dp; eski
            // deger kurali 2dp ihlal ediyordu. `FitToHeight` bu yuksekligi
            // kucultmez (bkz. GameButton/LocalFitScale), yani sigdirma
            // devreye girse bile hedef 48dp kalir.
            minHeight = 48.dp,
                depth = 4.dp,
                cornerRadius = 12.dp,
                horizontalPadding = 12.dp,
                modifier = Modifier.fillMaxWidth().testTag("watch_ad_button")
            ) {
                val labelColors = gameButtonColors(accent)
                Text(
                    text = language.pick(
                        tr = "REKLAM İZLE",
                        en = "WATCH AD",
                        it = "GUARDA ANNUNCIO",
                        fr = "REGARDER LA PUB",
                        es = "VER ANUNCIO"
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = labelColors.content,
                    // TASMA: "GUARDA ANNUNCIO" / "REGARDER LA PUB" uzun —
                    // buton sabit yukseklikli DEGIL, iki satira taser.
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(R.drawable.icon_coin),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "+$AD_TOKEN_REWARD",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = labelColors.content,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Booster etki semasi (Faz 159)
// ---------------------------------------------------------------------------

/**
 * Booster'in tahtada NEREYI sildigini gosteren minik izgara semasi.
 *
 * Neden sema: bu projede metinsiz anlatim tercih ediliyor (5 dil var, uzun
 * ceviriler dar ekranda taşiyor). Sema DIL BAGIMSIZ — "3x3" ya da "satir"
 * kelimelerini okumadan da etki alani bir bakista anlasiliyor.
 *
 * Cizim KODDAN dogrulandi (`BoomBlocksGame.applyBoosterAt`, salt okunur):
 *   BOMB       -> dokunulan hucre + cevresindeki 8 hucre (3x3).
 *   LINE_CLEAR -> dokunulan hucrenin SATIRININ tamami (sutun DEGIL).
 *
 * Dokunulan hucre daha parlak ciziliyor: "nereye basarsam" sorusunun cevabi
 * da semada. Renk TEK ayrim kanali degil — silinen hucreler ayrica DOLU,
 * silinmeyenler bos/cerceveli.
 */
@Composable
private fun BoosterEffectDiagram(
    type: BoosterType,
    accent: Color,
    surfaces: GameSurfaces,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    // 5x5 tahta parcasi, merkez (2,2) dokunulan hucre.
    val grid = 5
    val center = 2
    Canvas(modifier = modifier.size(size)) {
        val cell = this.size.minDimension / grid
        val gap = cell * 0.14f
        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val affected = when (type) {
                    // 3x3: merkezin bir hucre cevresi.
                    BoosterType.BOMB -> (r in center - 1..center + 1) && (c in center - 1..center + 1)
                    // Sadece SATIR — sutun dahil degil.
                    BoosterType.LINE_CLEAR -> r == center
                }
                val isTapped = r == center && c == center
                val topLeft = Offset(c * cell + gap / 2f, r * cell + gap / 2f)
                val cellSize = Size(cell - gap, cell - gap)
                val radius = CornerRadius(cell * 0.22f)
                if (affected) {
                    drawRoundRect(
                        color = if (isTapped) accent else accent.copy(alpha = 0.55f),
                        topLeft = topLeft,
                        size = cellSize,
                        cornerRadius = radius
                    )
                } else {
                    // Etkilenmeyen hucre: bos + ince cerceve. Ikinci ayrim
                    // kanali (dolu/bos), renk korlugunde de okunur.
                    drawRoundRect(
                        color = surfaces.hairline.copy(alpha = 0.55f),
                        topLeft = topLeft,
                        size = cellSize,
                        cornerRadius = radius,
                        style = Stroke(width = 1f * density)
                    )
                }
            }
        }
    }
}
