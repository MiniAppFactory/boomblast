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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

/** Iki dugum merkezi arasi dikey mesafe (referans px). */
private const val LEVEL_SPACING = 250f

/** Dugum sutununun merkezi ve hafif zikzak genligi (referans px). */
private const val NODE_CENTER_X = 452f
private const val NODE_SWING = 26f

/** Baglayici kavisin yatay genisligi: dugum sapmasindan GENIS, yol saliniyor. */
private const val SEG_W = 232f


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
    val BACK = floatArrayOf(85f, 118f, 122f, 0.69f, 0.900f)
    val WORD = floatArrayOf(327f, 111f, 326f, 0.85f, 0.402f)
    val COIN = floatArrayOf(615f, 121f, 162f, 0.95f, 0.584f)
    val TROPHY_BTN = floatArrayOf(754f, 119f, 104f, 0.82f, 1.048f)
    val SET_BTN = floatArrayOf(863f, 119f, 98f, 0.94f, 0.681f)
    val PANEL = floatArrayOf(467f, 329f, 811f, 0.94f, 0.739f)
    val TROPHY_BIG = floatArrayOf(159f, 335f, 118f, 0.89f, 0.943f)

    /** Dugum GOVDE capi ekranin ~%15.5'i (spec: %13-15, bloom haric). */
    const val NODE_BODY = 146f
    const val NODE_OPEN_RATIO = 0.84f
    const val NODE_LOCK_RATIO = 0.79f

    const val PILL_BODY = 278f
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
        val listState = rememberLazyListState()

        // --- 1) mod zemini -------------------------------------------------
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            accentOverride = listOf(theme.accent, theme.accent),
            skyDarken = 0.45f,
            showSparkles = true,
            modifier = Modifier.matchParentSize()
        )

        // --- 2-5) yol + haplar + dugumler (kaydirilabilir) ------------------
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("progression_map_list")
        ) {
            item { Box(Modifier.height(px(Ref.MAP_TOP, scale))) }
            // Faz 152 karari: ulasilanin 12 otesi cizilir -- ufuk gorunsun ama
            // kilitli bir duvar gibi durmasin.
            items(highestUnlockedLevel + 12) { idx ->
                LevelRow(
                    theme = theme,
                    level = idx + 1,
                    scale = scale,
                    highestUnlockedLevel = highestUnlockedLevel,
                    targetScoreForLevel = targetScoreForLevel,
                    language = language,
                    onSelectLevel = onSelectLevel
                )
            }
            item { Box(Modifier.height(px(140, scale))) }
        }

        // --- 6) ilerleme paneli (sabit) ------------------------------------
        // 9-dilim: referanstaki genis/alcak kutuya varlik BOZULMADAN oturuyor.
        NinePatchImage(
            resId = theme.panel,
            capFrac = 0.30f,
            modifier = Modifier
                .offset(x = px(62f, scale), y = px(222f, scale))
                .width(px(811f, scale))
                .height(px(214f, scale))
        )
        AssetImage(theme.trophy, Ref.TROPHY_BIG, scale)
        Column(
            modifier = Modifier.offset(x = px(238f, scale), y = px(288f, scale))
        ) {
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

        // --- 7) ust bar: tiklama davranislari mevcut kancalardan ------------
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
 * 9-DILIM cizim.
 *
 * NEDEN GEREKLI: V3 panel varliginin govdesi 296x192 (en-boy 0.65), oysa
 * referanstaki panel 811x214 (0.26). Varligi tek parca esnetmek kose
 * yaricapini ve kenarlik kalinligini bozar -- spec bunu acikca yasakliyor
 * ("asset stretching"). Panel bir CERCEVE oldugu icin dogru cozum 9-dilim:
 * dort kose DOGAL oranda kalir, yalnizca kenarlar ve orta uzar.
 *
 * Bu bir YENIDEN CIZIM DEGIL: ayni bitmap dokuz parca halinde blit ediliyor,
 * tek piksel uretilmiyor.
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
        // Kose olcegi YUKSEKLIGE gore: cerceve kalinligi hedefin yuksekligiyle
        // orantili kalsin, yatayda uzarken incelmesin.
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
        // ust sira
        part(0, 0, cx, cy, 0f, 0f, dx, dy)
        part(cx, 0, mw, cy, dx, 0f, midW, dy)
        part(iw - cx, 0, cx, cy, dx + midW, 0f, dx, dy)
        // orta sira
        part(0, cy, cx, mh, 0f, dy, dx, midH)
        part(cx, cy, mw, mh, dx, dy, midW, midH)
        part(iw - cx, cy, cx, mh, dx + midW, dy, dx, midH)
        // alt sira
        part(0, ih - cy, cx, cy, 0f, dy + midH, dx, dy)
        part(cx, ih - cy, mw, cy, dx, dy + midH, midW, dy)
        part(iw - cx, ih - cy, cx, cy, dx + midW, dy + midH, dx, dy)
    }
}

/**
 * Varligi MERKEZINE gore konumlandirir.
 *
 * Referans olcumleri GOVDE kutusuydu; bitmap isima payiyla daha genis, o
 * yuzden bitmap genisligi orandan geri hesaplanip merkez hizalaniyor. Boylece
 * ekranda gorunen govde tam olculen yere oturuyor.
 */
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
        // Dikey merkezleme YUKSEKLIGE gore: genislikle yapmak geniş-kisa
        // varliklari ekran disina tasiyordu (bkz. Ref notu).
        .offset(x = px(cx - w / 2f, scale), y = px(cy - h / 2f, scale))
        .width(px(w, scale))
    if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        m = m.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    }
    Image(painter = painterResource(resId), contentDescription = null, modifier = m)
}

/**
 * TEK SEVIYE satiri: baglayici kavis + hedef hapi + dugum.
 *
 * Z-sirasi satir icinde de korunuyor: once KAVIS, sonra HAP, en uste DUGUM --
 * yani yol dugumun ARKASINDAN geciyor.
 *
 * Kavis sette gelen iki yonlu S parcasi; yonu seviyeye gore degisiyor.
 * Kutusu dugum sapmasindan GENIS tutuluyor, boylece yol dugumun etrafinda
 * saliniyor (referanstaki gorunum).
 */
@Composable
private fun LevelRow(
    theme: MapTheme,
    level: Int,
    scale: Float,
    highestUnlockedLevel: Int,
    targetScoreForLevel: (Int) -> Int,
    language: AppLanguage,
    onSelectLevel: (Int) -> Unit
) {
    val unlocked = level <= highestUnlockedLevel
    val swingRight = level % 2 == 1
    val nodeCx = NODE_CENTER_X + if (swingRight) -NODE_SWING else NODE_SWING
    val nodeCy = LEVEL_SPACING / 2f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(px(LEVEL_SPACING, scale))
    ) {
        // --- 3) baglayici kavis (bu dugumden BIR SONRAKINE) ---
        Image(
            painter = painterResource(
                if (swingRight) theme.segLeftToRight else theme.segRightToLeft
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset(x = px(NODE_CENTER_X - SEG_W / 2f, scale), y = px(nodeCy, scale))
                .width(px(SEG_W, scale))
                .height(px(LEVEL_SPACING, scale))
        )

        // --- 4) hedef hapi ---
        TargetPill(
            theme = theme,
            isFirst = level == 1,
            onRight = !swingRight,
            nodeCx = nodeCx,
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

        // --- 5) dugum ---
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
                // Kilit gorseli KILITLI varligin icinde zaten var; burada
                // yalnizca DINAMIK seviye numarasi yaziliyor.
                Text(
                    text = "$level",
                    color = Color.White,
                    fontSize = (62 * scale).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Hedef hapi.
 *
 * Referanstaki yerlesim: ILK dugumun hapi ALTINDA (kuyruk yukari), digerleri
 * dugumun YANINDA ve kuyruk dugume BAKIYOR. Sagdaki dugumun hapi sola gider,
 * yani kuyruk sagda.
 */
@Composable
private fun TargetPill(
    theme: MapTheme,
    isFirst: Boolean,
    onRight: Boolean,
    nodeCx: Float,
    nodeCy: Float,
    scale: Float,
    text: String
) {
    val w = bitmapW(Ref.PILL_BODY, Ref.PILL_RATIO)
    val res = when {
        isFirst -> theme.pillUp
        onRight -> theme.pillTailRight
        else -> theme.pillTailLeft
    }
    val cx: Float
    val cy: Float
    if (isFirst) {
        cx = nodeCx
        cy = nodeCy + Ref.NODE_BODY / 2f + 46f
    } else if (onRight) {
        cx = nodeCx - Ref.NODE_BODY / 2f - w / 2f + 6f
        cy = nodeCy + 18f
    } else {
        cx = nodeCx + Ref.NODE_BODY / 2f + w / 2f - 6f
        cy = nodeCy + 18f
    }

    // Hap en-boy oranlari: kuyruk-yukari 0.50, kuyruk-sol 0.64, kuyruk-sag 0.58
    val pillAspect = when {
        isFirst -> 0.501f
        onRight -> 0.580f
        else -> 0.638f
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
