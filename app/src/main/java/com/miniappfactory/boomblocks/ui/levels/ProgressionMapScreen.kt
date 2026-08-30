package com.miniappfactory.boomblocks.ui.levels

import androidx.compose.foundation.Image
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.theme.BlastSkin

/**
 * FAZ 174 — ILERLEME HARITASI, TAMAMEN VERILEN GORSELLERLE.
 *
 * ---------------------------------------------------------------------------
 * NEDEN BU DOSYA VAR
 *
 * Onceki ekran REDDEDILDI: verilen tasarimi Compose ilkelleriyle (Canvas,
 * CircleShape, Brush, Card, BorderStroke) YENIDEN CIZIYORDU. Burada hicbir
 * gorsel yeniden cizilmiyor. Native taraf yalnizca konumlandirma, olcekleme,
 * kaydirma, tiklama, dinamik metin ve durumdan sorumlu.
 *
 * ---------------------------------------------------------------------------
 * VARLIK PAKETI: V3 (dogrulandi)
 *
 * V2 kullanilamiyordu ve sebebi olculdu: `path_overlay` disindaki TUM
 * bilesenler %0 seffafti (opak dikdortgen kesitler) ve panel/dugum/hap/jeton
 * SABIT METIN tasiyordu; ayrica `layout_positions.json` icindeki iki hap
 * kutusu BOS ZEMINI gosteriyordu. V3 uculuyu de duzeltti:
 *   - gercek alfa (bilesen basina %15-37 seffaflik)
 *   - metin yok
 *   - DIKISSIZ yol karosu + dugum capalari (path_tile_meta.json)
 *
 * V3'te tek kalan sorun tema klasorlerinin KARISIK olmasiydi (career/ icinde
 * Pro varliklari vb.); bu ice aktarim sirasinda duzeltildi, varliklara
 * dokunulmadi.
 *
 * ---------------------------------------------------------------------------
 * OLCEKLEME
 *
 * Tasarim 941 px referans genislige gore. SABIT dp YOK -- bir onceki deneme
 * tam bu yuzden patlamisti: 76dp'lik dugum benim render ettigim 411dp'lik
 * ekranda dogru gorunurken kullanicinin daha dar ekraninda devasa cikmisti.
 * Her olcu `scale = icerikGenisligi / 941` ile oranlaniyor.
 *
 * Bir incelik: varliklarin ISIMA PAYI var, yani bitmap govdeden buyuk.
 * Ekranda gorunmesini istedigim sey GOVDE oldugu icin, bitmap genisligi
 * `govde / (govde/bitmap orani)` ile hesaplandi. Oranlar varliklardan
 * OLCULDU (alfa > 200 cekirdek kutusu), tahmin edilmedi.
 */
private const val REF_W = 941f

/**
 * FAZ 178 — HARITA GRUBU CARPANI.
 *
 * Kullanici: "harita cok kucuk, dugum/yol/hap/dikey aralik BIRLIKTE
 * buyusun." Tek carpan kullaniliyor, boylece oranlar birbirine gore
 * bozulmuyor -- ogeleri tek tek buyutmek tam da bunu bozardi.
 *
 * Olcum (ekran genisligine oranli): panel referansta %86.6, bizde %82.3;
 * yol salinimi ve dugum capi da referansin altindaydi.
 */
private const val MAP_SCALE = 1.28f

/** Ust bar ve panel icin ayri, daha kucuk bir duzeltme. */
private const val CHROME_SCALE = 1.06f

private fun px(v: Int, scale: Float): Dp = (v * scale).dp
private fun px(v: Float, scale: Float): Dp = (v * scale).dp

/**
 * Bir temanin gorsel kimligi: tum bilesenler + zemin vurgusu.
 *
 * Uc mod AYNI yapiyi kullaniyor, yalnizca varlik kumesi degisiyor -- yeniden
 * kullanilabilir mimari istegi bu sekilde karsilaniyor, ama bilesenler yine
 * RASTER; "yeniden kullanilabilirlik" gorsel ilkellere donusmuyor.
 */
data class MapTheme(
    val back: Int,
    val word: Int,
    val coin: Int,
    val trophyBtn: Int,
    val settingsBtn: Int,
    val panel: Int,
    val trophy: Int,
    val nodeOpen: Int,
    val nodeLock: Int,
    val pillUp: Int,
    val pillTailLeft: Int,
    val pillTailRight: Int,
    /** Sag dugumden SOL dugume inen S kavisi. */
    val segRightToLeft: Int,
    /** Sol dugumden SAG dugume inen S kavisi (digerinin aynasi). */
    val segLeftToRight: Int,
    /** 1. seviye icin: kavisin ALT YARISI (yol burada BASLAR). */
    val segStart: Int,
    /** Seamless full S path tile. Nodes are placed on its quarter-period anchors. */
    val path: Int,
    val accent: Color,
    val label: Color,
)

val CareerMapTheme = MapTheme(
    back = R.drawable.kb_car_back,
    word = R.drawable.kb_car_word,
    coin = R.drawable.kb_car_coin,
    trophyBtn = R.drawable.kb_car_trophybtn,
    settingsBtn = R.drawable.kb_car_setbtn,
    panel = R.drawable.kb_car_panel,
    trophy = R.drawable.kb_car_trophy,
    nodeOpen = R.drawable.kb_car_node_open,
    nodeLock = R.drawable.kb_car_node_lock,
    pillUp = R.drawable.kb_car_pill_up,
    pillTailLeft = R.drawable.kb_car_pill_l,
    pillTailRight = R.drawable.kb_car_pill_r,
    segRightToLeft = R.drawable.kb_car_seg_rl,
    segLeftToRight = R.drawable.kb_car_seg_lr,
    segStart = R.drawable.kb_car_seg_start,
    path = R.drawable.kb_car_path,
    accent = Color(0xFF22D3EE),
    label = Color(0xFF4FC3F7)
)

// FAZ 175 — YERLESIM REFERANSA CEVRILDI.
//
// Once V3'un `path_tile_seamless` karosu kullaniliyordu; o karo dugumleri
// SAGA-SOLA ATLATIYOR ve KESKIN ZIGZAG uretiyordu. Kullanicinin referansinda
// ise dugumler neredeyse DIKEY siralı, YOL onlarin etrafinda saliniyor.
// Ayrica sette iki yonlu TEK BUKUMLU S baglayicilar zaten vardi -- kullanici
// hakli olarak "neden kullanmadin?" diye sordu.
//
// Artik karo yok: her seviye kendi satirinda, aralari sette gelen S kavisiyle
// baglaniyor. Kavis yonu her seviyede degisiyor (asagi-sag / asagi-sol).

val EasyMapTheme = MapTheme(
    back = R.drawable.kb_esy_back,
    word = R.drawable.kb_esy_word,
    coin = R.drawable.kb_esy_coin,
    trophyBtn = R.drawable.kb_esy_trophybtn,
    settingsBtn = R.drawable.kb_esy_setbtn,
    panel = R.drawable.kb_esy_panel,
    trophy = R.drawable.kb_esy_trophy,
    nodeOpen = R.drawable.kb_esy_node_open,
    nodeLock = R.drawable.kb_esy_node_lock,
    pillUp = R.drawable.kb_esy_pill_up,
    pillTailLeft = R.drawable.kb_esy_pill_l,
    pillTailRight = R.drawable.kb_esy_pill_r,
    segLeftToRight = R.drawable.kb_esy_seg_lr,
    segStart = R.drawable.kb_esy_seg_start,
    segRightToLeft = R.drawable.kb_esy_seg_rl,
    path = R.drawable.kb_esy_path,
    accent = Color(0xFF2DD4BF),
    label = Color(0xFF5EEAD4)
)

val ProMapTheme = MapTheme(
    back = R.drawable.kb_pro_back,
    word = R.drawable.kb_pro_word,
    coin = R.drawable.kb_pro_coin,
    trophyBtn = R.drawable.kb_pro_trophybtn,
    settingsBtn = R.drawable.kb_pro_setbtn,
    panel = R.drawable.kb_pro_panel,
    trophy = R.drawable.kb_pro_trophy,
    nodeOpen = R.drawable.kb_pro_node_open,
    nodeLock = R.drawable.kb_pro_node_lock,
    pillUp = R.drawable.kb_pro_pill_up,
    pillTailLeft = R.drawable.kb_pro_pill_l,
    pillTailRight = R.drawable.kb_pro_pill_r,
    // NOT: sette kavis yalnizca MAVI ve MOR ailesinde vardi; Pro'nun sicak
    // kimliginde bir kavis YOKTU. Mavi kavisin ton kaydirmasi kullaniliyor --
    // ayni cizimin renk donusumu, yeniden cizim degil. Pro'ya ozel kavis
    // gelirse bu iki satir degistirilerek dogrudan takilabilir.
    segLeftToRight = R.drawable.kb_pro_seg_lr,
    segStart = R.drawable.kb_pro_seg_start,
    segRightToLeft = R.drawable.kb_pro_seg_rl,
    path = R.drawable.kb_pro_path,
    accent = Color(0xFFFF7A2F),
    label = Color(0xFFFFA366)
)

// FAZ 175b — ZINCIR ARTIK GERCEKTEN BAGLANIYOR.
//
// Kullanici: "olmamis ki, bir sag bir sol kavis kullanman lazimdi ki baglansin
// harita." Yon alternasyonu zaten vardi; kirik olan sey UC NOKTALARIN
// HIZASIYDI.
//
// Olculdu: kavisin govdesi bitmap KOSESINDE degil, isima payi yuzunden
// iceride. `kb_car_seg_lr` (202x260) icin govde kutusu x=22..176, y=24..235;
// ust uc x=51, alt uc x=163. Yani oransal olarak:
//     ust uc  = (0.252 , 0.092)
//     alt uc  = (0.807 , 0.904)
// Kutuyu kose koseye yerlestirince uclar birbirine DEGMIYOR, aralarinda
// bosluk kaliyordu.
//
// Zincir bu oranlardan cozuluyor. Sag-sola inen kavis aynadir, yani onun
// uclari (0.748, 0.092) ve (0.193, 0.904).
//
// Ardisik iki kavisin uclari cakissin istiyorsak:
//   dikey adim  = (0.904 - 0.092) * SEG_H = 0.812 * SEG_H  ==> SEG_H = SPACING / 0.812
//   yatay kayma = (0.807 - 0.748) * SEG_W = 0.059 * SEG_W
// Kayma her satirda ISARET DEGISTIRDIGI icin iki satirda bir sifirlaniyor --
// yol yana kaymiyor.
private const val SEG_TOP_X = 0.252f
private const val SEG_TOP_Y = 0.092f
private const val SEG_BOT_X = 0.807f
private const val SEG_BOT_Y = 0.904f

/** Iki dugum merkezi arasi dikey mesafe (referans px). */
// Referanstaki dugum araliklari olculdu: 340, 234, 219 -> ortalama ~264.
// 285 seciliyor: referansin ferahligini veriyor ama ekrana sigan dugum
// sayisini gereksiz azaltmiyor.
private const val LEVEL_SPACING = 285f * MAP_SCALE

/** Kavisin yatay genisligi = yolun salinim genligi. */
// FAZ 177 — SALINIM GENISLIGI OLCULDU.
// Referanstaki yol ekranin ~%32'sini kapliyor. Ilk olcumum 604 (%64) cikmisti
// ama o maske hedef HAPLARININ camgobegi kenarligini da yol sanmisti; kavis
// o genislikte yatay bir supurme gibi duruyordu (yukseklik 351, genislik 604).
// Referansta bir dugum araligi neredeyse KARE: dikey ~264, yatay ~300.
private const val SEG_W = 330f * MAP_SCALE

/**
 * Kavisin DIKEY ORTA NOKTASINDA egrinin yatay konumu (bitmap orani).
 *
 * Olculdu: y = 0.50H iken egri x = 0.542. Dugumu kutu MERKEZINE koymak bu
 * yuzden yanlisti -- dugum yolun uzerine degil KENARINA oturuyordu. Kutu artik
 * dugume gore bu orandan konumlaniyor, yani yol dugumun TAM ALTINDAN geciyor.
 */
private const val SEG_MID_X = 0.542f

/**
 * Uclarin cakismasi icin gereken kavis yuksekligi -- ustune BINDIRME payi.
 *
 * Uclar tam cakissa bile birlesme noktasi GORUNUYOR, cunku kavisin uclari
 * sivrilerek soluyor. %14 fazla yukseklik komsu segmentlerin uclarini
 * ust uste bindiriyor ve yol kesintisiz tek bir serit gibi okunuyor.
 */
private const val SEG_OVERLAP = 1.14f
private const val SEG_H = LEVEL_SPACING / (SEG_BOT_Y - SEG_TOP_Y) * SEG_OVERLAP

/** Dugum sutunu merkezi ve uc hizasindan dogan kucuk yatay sapma. */
private const val NODE_CENTER_X = 470f

/**
 * Ardisik dugumler arasi kucuk yatay kayma.
 *
 * Kutu artik orta noktaya gore konumlandigi icin zincir sartı degisti:
 *   LtR alt ucu   = nodeX + (SEG_BOT_X - SEG_MID_X) * W
 *   RtL ust ucu   = nodeX' + ((1 - SEG_TOP_X) - (1 - SEG_MID_X)) * W
 * Esitlenince kayma = ((SEG_BOT_X - SEG_MID_X) - (SEG_MID_X - SEG_TOP_X)) * W.
 * Isaret her satirda degistigi icin iki satirda bir sifirlaniyor.
 */
private const val NODE_SWING =
    ((SEG_BOT_X - SEG_MID_X) - (SEG_MID_X - SEG_TOP_X)) * SEG_W / 2f


/**
 * Referans (941x1672) uzerinde ELLE OLCULEN GOVDE kutulari.
 *
 * Her satir: merkez x, merkez y, GOVDE genisligi, govde/bitmap orani, en-boy.
 *
 * Varliklarin ISIMA PAYI var, yani bitmap govdeden buyuk. Ekranda gorunmesi
 * istenen sey GOVDE oldugu icin bitmap genisligi `govde / oran` ile geri
 * hesaplaniyor; oranlar varliklardan OLCULDU (alfa > 200 cekirdek kutusu).
 *
 * En-boy oranlari da varliktan: ilk surumde dikey merkezleme `w / 2` ile
 * yapiliyordu (yukseklik yerine GENISLIK) ve geniş-kisa varliklar ekran
 * disina kaciyordu.
 */
private object Ref {
    val BACK = floatArrayOf(85f, 118f, 122f * CHROME_SCALE, 0.69f, 0.900f)
    val WORD = floatArrayOf(327f, 111f, 326f * CHROME_SCALE, 0.85f, 0.402f)
    val COIN = floatArrayOf(612f, 121f, 162f * CHROME_SCALE, 0.95f, 0.584f)
    val TROPHY_BTN = floatArrayOf(752f, 119f, 104f * CHROME_SCALE, 0.82f, 1.048f)
    val SET_BTN = floatArrayOf(858f, 119f, 98f * CHROME_SCALE, 0.94f, 0.681f)
    val PANEL = floatArrayOf(467f, 329f, 811f, 0.94f, 0.739f)
    val TROPHY_BIG = floatArrayOf(162f, 338f, 118f * CHROME_SCALE, 0.89f, 0.943f)

    /** Dugum GOVDE capi ekranin ~%15.5'i (spec: %13-15, bloom haric). */
    const val NODE_BODY = 146f * MAP_SCALE
    const val NODE_OPEN_RATIO = 0.84f
    const val NODE_LOCK_RATIO = 0.79f

    const val PILL_BODY = 300f * MAP_SCALE
    const val PILL_RATIO = 0.95f

    /** Harita, panelin altindan basliyor. */
    const val MAP_TOP = 470
}

/** Isima payi dahil bitmap genisligi. */
private fun bitmapW(body: Float, ratio: Float) = body / ratio


/**
 * Ilerleme haritasi ekrani.
 *
 * Parametreler eski `LevelMapScreen` ile ayni kaynaklardan besleniyor: seviye
 * kilidi, hedef puani, jeton, gezinme ve kaydirma mevcut oyun durumundan
 * geliyor, burada yeniden uretilmiyor.
 */
@Composable
fun ProgressionMapScreen(
    theme: MapTheme,
    modeLabel: String,
    progress: PlayerProgress,
    highestUnlockedLevel: Int,
    targetScoreForLevel: (Int) -> Int,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin,
    onSelectLevel: (Int) -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val scale = maxWidth.value / REF_W

        // 1) Mode background. Gameplay/progression state remains untouched.
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            accentOverride = listOf(theme.accent, theme.accent),
            skyDarken = 0.45f,
            showSparkles = true,
            modifier = Modifier.matchParentSize()
        )

        // 2-5) One CONTINUOUS path behind all nodes. This deliberately replaces
        // the previous per-row curve composition. The previous version exposed
        // curve caps next to the circles; here there are no segment caps at a
        // level at all. Nodes simply cover a continuous path anchor.
        ContinuousMapContent(
            theme = theme,
            scale = scale,
            highestUnlockedLevel = highestUnlockedLevel,
            targetScoreForLevel = targetScoreForLevel,
            language = language,
            onSelectLevel = onSelectLevel,
            topInset = px(Ref.MAP_TOP, scale)
        )

        // 6) Fixed progression panel.
        NinePatchImage(
            resId = theme.panel,
            capFrac = 0.30f,
            modifier = Modifier
                .offset(x = px(56f, scale), y = px(220f, scale))
                .width(px(829f, scale))
                .height(px(228f, scale))
        )
        AssetImage(theme.trophy, Ref.TROPHY_BIG, scale)
        Column(modifier = Modifier.offset(x = px(238f, scale), y = px(288f, scale))) {
            Text(
                text = modeLabel,
                color = theme.label,
                fontSize = (26 * scale).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (1.2f * scale).sp
            )
            Text(
                text = language.pick(
                    tr = "SEVİYE $highestUnlockedLevel", en = "LEVEL $highestUnlockedLevel",
                    it = "LIVELLO $highestUnlockedLevel", fr = "NIVEAU $highestUnlockedLevel",
                    es = "NIVEL $highestUnlockedLevel"
                ),
                color = Color.White,
                fontSize = (46 * scale).sp,
                fontWeight = FontWeight.Black
            )
        }

        // 7) Fixed header. Existing handlers are reused.
        AssetImage(theme.back, Ref.BACK, scale, onBack)
        AssetImage(theme.word, Ref.WORD, scale)
        Box {
            AssetImage(theme.coin, Ref.COIN, scale)
            Text(
                text = "${progress.tokens}",
                color = Color.White,
                fontSize = (34 * scale).sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(
                    x = px(Ref.COIN[0] + 14f, scale),
                    y = px(Ref.COIN[1] - 22f, scale)
                )
            )
        }
        AssetImage(theme.trophyBtn, Ref.TROPHY_BTN, scale, onOpenMissions)
        AssetImage(theme.settingsBtn, Ref.SET_BTN, scale, onOpenSettings)
    }
}

/**
 * Continuous infinite-looking progression route.
 *
 * kb_*_path.webp is a 512x1024 transparent tile. Its centerline returns to
 * almost exactly x=50% at y=0, 25%, 50%, 75%, 100%. Therefore each quarter
 * of the tile is one level-to-level connection. Repeating the tile vertically
 * makes a seamless route and every level node can sit directly on the route.
 *
 * Crucial visual rule: THE PATH NEVER ENDS AT A NODE. It continues underneath
 * the node. The node is drawn later in z-order and hides the junction, so the
 * eye reads: curve -> node -> curve -> next node, exactly as requested.
 */
@Composable
private fun ContinuousMapContent(
    theme: MapTheme,
    scale: Float,
    highestUnlockedLevel: Int,
    targetScoreForLevel: (Int) -> Int,
    language: AppLanguage,
    onSelectLevel: (Int) -> Unit,
    topInset: Dp
) {
    val lastLevel = highestUnlockedLevel + 12
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Reference geometry. Keeping the source tile's 1:2 aspect gives a
    // ~300-reference-px level interval and ~300px horizontal S swing, matching
    // the approved reference far better than composing isolated one-bend caps.
    val pathWRef = 600f
    val pathHRef = 1200f
    val levelSpacingRef = pathHRef / 4f // 300
    val pathLeftRef = NODE_CENTER_X - pathWRef / 2f
    val nodeStartRef = 95f
    val nodeCenterRef = NODE_CENTER_X
    val contentHeightRef = nodeStartRef + (lastLevel - 1) * levelSpacingRef + 260f
    val tileCount = kotlin.math.ceil(
        (nodeStartRef + (lastLevel - 1) * levelSpacingRef) / pathHRef
    ).toInt().coerceAtLeast(1)

    LaunchedEffect(highestUnlockedLevel, scale) {
        // Hedef seviyeyi ekranin TEPESINE degil, biraz asagisina getir.
        //
        // Duzeltme: pay olmadan 1. seviyede bile 95 ref px kaydiriliyordu ve
        // ilk dugum panelin altina giriyordu (render'da goruldu). Pay dusuldugu
        // icin bastaki seviyelerde hesap negatife dusuyor, yani HIC kaydirma
        // yapilmiyor ve dugum tam gorunuyor.
        val targetLevel = (highestUnlockedLevel - 2).coerceAtLeast(1)
        val targetRef = nodeStartRef + (targetLevel - 1) * levelSpacingRef - 150f
        val targetPx = with(density) { (targetRef * scale).dp.roundToPx() }
        scrollState.scrollTo(targetPx.coerceAtLeast(0))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topInset)
            .verticalScroll(scrollState)
            .testTag("progression_map_list")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(px(contentHeightRef, scale))
        ) {
            // Z=3: seamless path. Each tile meets the next at its centerline.
            repeat(tileCount + 1) { tileIndex ->
                Image(
                    painter = painterResource(theme.path),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset(
                            x = px(pathLeftRef, scale),
                            y = px(nodeStartRef + tileIndex * pathHRef, scale)
                        )
                        .width(px(pathWRef, scale))
                        .height(px(pathHRef, scale))
                )
            }

            // Z=4/5: labels and nodes. Node centers are placed exactly at each
            // quarter-tile anchor. Since path is behind, it disappears beneath
            // the orb and emerges continuously on the other side.
            for (level in 1..lastLevel) {
                val nodeCy = nodeStartRef + (level - 1) * levelSpacingRef
                val unlocked = level <= highestUnlockedLevel

                TargetPillContinuous(
                    theme = theme,
                    level = level,
                    nodeCx = nodeCenterRef,
                    nodeCy = nodeCy,
                    scale = scale,
                    text = language.pick(
                        tr = "HEDEF: ${targetScoreForLevel(level)} + 1 satır",
                        en = "TARGET: ${targetScoreForLevel(level)} + 1 row",
                        it = "OBIETTIVO: ${targetScoreForLevel(level)} + 1 riga",
                        fr = "OBJECTIF : ${targetScoreForLevel(level)} + 1 ligne",
                        es = "OBJETIVO: ${targetScoreForLevel(level)} + 1 fila"
                    )
                )

                ProgressionNode(
                    theme = theme,
                    level = level,
                    unlocked = unlocked,
                    nodeCx = nodeCenterRef,
                    nodeCy = nodeCy,
                    scale = scale,
                    onSelectLevel = onSelectLevel
                )
            }
        }
    }
}

@Composable
private fun ProgressionNode(
    theme: MapTheme,
    level: Int,
    unlocked: Boolean,
    nodeCx: Float,
    nodeCy: Float,
    scale: Float,
    onSelectLevel: (Int) -> Unit
) {
    val ratio = if (unlocked) Ref.NODE_OPEN_RATIO else Ref.NODE_LOCK_RATIO
    val w = bitmapW(Ref.NODE_BODY, ratio)
    Box(
        modifier = Modifier
            .offset(x = px(nodeCx - w / 2f, scale), y = px(nodeCy - w / 2f, scale))
            .size(px(w, scale))
            .clickable(enabled = unlocked) { onSelectLevel(level) }
            .testTag("level_card_$level"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(if (unlocked) theme.nodeOpen else theme.nodeLock),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        if (unlocked) {
            Text(
                text = "$level",
                color = Color.White,
                fontSize = (62 * scale).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * First target label sits below the first node. Later labels alternate left
 * and right, while the PATH itself stays continuous underneath the node.
 */
@Composable
private fun TargetPillContinuous(
    theme: MapTheme,
    level: Int,
    nodeCx: Float,
    nodeCy: Float,
    scale: Float,
    text: String
) {
    val isFirst = level == 1
    // Reference alternates labels around an almost-vertical node chain.
    val labelOnRight = level % 2 == 0
    val w = bitmapW(Ref.PILL_BODY, Ref.PILL_RATIO)
    val res = when {
        isFirst -> theme.pillUp
        labelOnRight -> theme.pillTailLeft   // tail points LEFT, toward node
        else -> theme.pillTailRight          // tail points RIGHT, toward node
    }
    val pillAspect = when {
        isFirst -> 0.501f
        labelOnRight -> 0.638f
        else -> 0.580f
    }
    val cx = when {
        isFirst -> nodeCx
        labelOnRight -> nodeCx + Ref.NODE_BODY / 2f + w / 2f - 10f
        else -> nodeCx - Ref.NODE_BODY / 2f - w / 2f + 10f
    }
    val cy = if (isFirst) {
        nodeCy + Ref.NODE_BODY / 2f + 48f
    } else {
        nodeCy + 18f
    }

    Box(
        modifier = Modifier
            .offset(x = px(cx - w / 2f, scale), y = px(cy - w * pillAspect / 2f, scale))
            .width(px(w, scale)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = (25 * scale).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * 9-slice raster blit for the empty progression panel. Corners stay native;
 * only edges/center stretch. No vector redraw is introduced.
 */
@Composable
private fun NinePatchImage(
    resId: Int,
    capFrac: Float,
    modifier: Modifier
) {
    val img = ImageBitmap.imageResource(resId)
    Canvas(modifier = modifier) {
        val iw = img.width
        val ih = img.height
        val cx = (iw * capFrac).toInt().coerceAtLeast(1)
        val cy = (ih * capFrac).toInt().coerceAtLeast(1)
        val k = size.height / ih
        val dx = (cx * k)
        val dy = (cy * k)
        val midW = (size.width - 2 * dx).coerceAtLeast(1f)
        val midH = (size.height - 2 * dy).coerceAtLeast(1f)

        fun part(sx: Int, sy: Int, sw: Int, sh: Int, ox: Float, oy: Float, ow: Float, oh: Float) {
            if (sw <= 0 || sh <= 0 || ow <= 0f || oh <= 0f) return
            drawImage(
                image = img,
                srcOffset = IntOffset(sx, sy),
                srcSize = IntSize(sw, sh),
                dstOffset = IntOffset(ox.toInt(), oy.toInt()),
                dstSize = IntSize(ow.toInt(), oh.toInt())
            )
        }

        val mw = iw - 2 * cx
        val mh = ih - 2 * cy
        part(0, 0, cx, cy, 0f, 0f, dx, dy)
        part(cx, 0, mw, cy, dx, 0f, midW, dy)
        part(iw - cx, 0, cx, cy, dx + midW, 0f, dx, dy)
        part(0, cy, cx, mh, 0f, dy, dx, midH)
        part(cx, cy, mw, mh, dx, dy, midW, midH)
        part(iw - cx, cy, cx, mh, dx + midW, dy, dx, midH)
        part(0, ih - cy, cx, cy, 0f, dy + midH, dx, dy)
        part(cx, ih - cy, mw, cy, dx, dy + midH, midW, dy)
        part(iw - cx, ih - cy, cx, cy, dx + midW, dy + midH, dx, dy)
    }
}

@Composable
private fun AssetImage(
    resId: Int,
    ref: FloatArray,
    scale: Float,
    onClick: (() -> Unit)? = null
) {
    val cx = ref[0]; val cy = ref[1]; val body = ref[2]; val ratio = ref[3]
    val aspect = ref[4]
    val w = bitmapW(body, ratio)
    val h = w * aspect
    var m = Modifier
        .offset(x = px(cx - w / 2f, scale), y = px(cy - h / 2f, scale))
        .width(px(w, scale))
    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        m = m.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    }
    Image(painter = painterResource(resId), contentDescription = null, modifier = m)
}
