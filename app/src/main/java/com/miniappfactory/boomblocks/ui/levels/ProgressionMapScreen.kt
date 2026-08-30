package com.miniappfactory.boomblocks.ui.levels

import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.components.AutoFitLabel
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import kotlin.math.roundToInt

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

/**
 * FAZ 188 — RASTER KABIN ICINDEKI METIN SISTEM YAZI OLCEGINDEN BAGIMSIZ.
 *
 * Kullanici tabletten ekran goruntusu gonderdi: hedef haplarinin yazisi
 * hapin disina tasiyordu.
 *
 * Kok neden EKRAN GENISLIGI DEGILDI (720dp'de yeniden uretmeye calistim,
 * duzgun ciziliyor). Sorun BIRIM KARISIMI: bu ekrandaki her GORSEL olcu
 * `px(...)` ile dp cinsinden ve `scale` ile oranlaniyor, ama METIN `.sp`
 * ile veriliyordu. `sp`, dp'den farkli olarak KULLANICININ SISTEM YAZI
 * BOYUTU ayariyla da carpilir. Ayar 1.0'in ustundeyse (tabletlerde sik)
 * yazi buyur, ARDINDAKI RASTER HAP BUYUMEZ -- yazi disari tasar.
 * Telefonda font_scale'i 1.3 yapinca hata birebir uretildi.
 *
 * Cozum: hapin/dugumun/panelin ICINDEKI metin dp'den sp'ye cevriliyor,
 * yani cizimle AYNI birime kilitleniyor. Bu, erisilebilirlik acisindan
 * bilincli bir odun: sabit olculu bir varligin icine sigmasi gereken metin
 * buyuyemez. Ekrandaki SERBEST metinler (oyun ici basliklar, diyaloglar)
 * `sp` olarak kaliyor ve kullanicinin ayarina uymaya devam ediyor.
 */
@Composable
private fun fixedSp(refUnits: Float, scale: Float): TextUnit =
    with(LocalDensity.current) { (refUnits * scale).dp.toSp() }

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
    /**
     * Mod basligi TURKCE surumu.
     *
     * FAZ 184 — kullanici: "basliklar dil degisince degismiyor, turkce
     * kaliyor." Haritadaki her metin `language.pick`ten geciyordu ama BASLIK
     * bir RASTER varlikti ve yazi varligin icine gomuluydu. Diger dillerin
     * surumleri `tools/wordart/make_word.py` ile, ayni stille uretildi;
     * Turkce varliklar degistirilmedi.
     */
    val word: Int,
    val wordEn: Int,
    val wordIt: Int,
    val wordFr: Int,
    val wordEs: Int,
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

/** Basligin, secili dildeki varligi. */
fun MapTheme.wordFor(language: AppLanguage): Int = when (language) {
    AppLanguage.TR -> word
    AppLanguage.EN -> wordEn
    AppLanguage.IT -> wordIt
    AppLanguage.FR -> wordFr
    AppLanguage.ES -> wordEs
}

val CareerMapTheme = MapTheme(
    back = R.drawable.kb_car_back,
    word = R.drawable.kb_car_word,
    wordEn = R.drawable.kb_car_word_en,
    wordIt = R.drawable.kb_car_word_it,
    wordFr = R.drawable.kb_car_word_fr,
    wordEs = R.drawable.kb_car_word_es,
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
    wordEn = R.drawable.kb_esy_word_en,
    wordIt = R.drawable.kb_esy_word_it,
    wordFr = R.drawable.kb_esy_word_fr,
    wordEs = R.drawable.kb_esy_word_es,
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
    wordEn = R.drawable.kb_pro_word_en,
    wordIt = R.drawable.kb_pro_word_it,
    wordFr = R.drawable.kb_pro_word_fr,
    wordEs = R.drawable.kb_pro_word_es,
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
    // Geri butonu da ayni dilimleme hatasindan payini almisti: bitmap'inin
    // sag ucunda komsu butondan bir DILIM vardi ve ekranda kelime sanatinin
    // arkasinda soluk bir dikey cizgi olarak goruluyordu. Varlik normalize
    // edildi; govde genisligi (129) ve sol payi (20) KORUNDU, degisen sadece
    // govde/bitmap ve en-boy orani.
    val BACK = floatArrayOf(85f, 119f, 122f * CHROME_SCALE, 0.806f, 1.000f)
    // Kullanici iki kez "kolay mod yazisini asagi cek" dedi. Merkez 111 -> 128.
    // Varlik icindeki kelime blogu bitmap yuksekliginin %52'sinde durdugu icin
    // blogun ekrandaki merkezi 115 -> 132'ye cikiyor; yazi artik dugme
    // sirasinin (119) belirgin sekilde ALTINDA oturuyor.
    val WORD = floatArrayOf(327f, 128f, 326f * CHROME_SCALE, 0.85f, 0.402f)
    // FAZ 183 — SAG GRUP OLCULEREK YENIDEN HIZALANDI.
    //
    // Kullanici: "sagdaki gostergeler olmamis." Uc ayri kok neden vardi ve
    // ucu de VARLIK DILIMLEME hatasiydi (bkz. tools/wordart/normalize_chrome.py):
    //   1. `setbtn` bitmap'i IKI buton iceriyordu; ikincisi ekranin sag
    //      kenarinda "yarim kalmis kutu" olarak goruluyordu.
    //   2. Govdeler bitmap icinde ORTALI DEGILDI; `AssetImage` bitmap'i Ref
    //      merkezine oturttugu icin dugmeler kayiyordu (kupa ile disli arasi
    //      dengesiz bosluk).
    //   3. Govde/bitmap ve en-boy oranlari uc temada FARKLIYDI, oysa Ref her
    //      tema icin TEK oran kullaniyor -- en az iki temada olcu yanlisti.
    //
    // Varliklar normalize edildi (govde ortali, oran 0.806, tip basina tek
    // en-boy). Konumlar da yeniden hesaplandi: grup, geri butonunun SOL
    // payiyla (20) simetrik olacak sekilde SAGA yaslandi ve dugmeler esit
    // boya (106) + esit araliga (14) cekildi.
    //   disli : 815..921   kupa : 695..801   jeton : 509..681
    val COIN = floatArrayOf(595f, 119f, 162f * CHROME_SCALE, 0.806f, 0.510f)
    val TROPHY_BTN = floatArrayOf(748f, 119f, 100f * CHROME_SCALE, 0.806f, 0.970f)
    val SET_BTN = floatArrayOf(868f, 119f, 100f * CHROME_SCALE, 0.806f, 1.000f)

    /**
     * Jeton sayisinin oturdugu BOS ALAN (hapin icinde, madeni paranin sagi).
     *
     * Once sabit bir kaydirmayla (`COIN[0] + 14`) yaziliyordu; sayi hapin
     * icinde ortalanmiyor, sag kenarina dayaniyordu ve dort haneli bir
     * degerde tasacakti. Alan olculdu: madeni para govdenin ilk %42'sini
     * kapliyor, geri kalani bos.
     */
    const val COIN_TEXT_X = 584f
    const val COIN_TEXT_W = 88f
    const val COIN_TEXT_H = 60f
    val PANEL = floatArrayOf(467f, 329f, 811f, 0.94f, 0.739f)
    val TROPHY_BIG = floatArrayOf(162f, 338f, 118f * CHROME_SCALE, 0.89f, 0.943f)

    /** Dugum GOVDE capi ekranin ~%15.5'i (spec: %13-15, bloom haric). */
    const val NODE_BODY = 146f * MAP_SCALE
    const val NODE_OPEN_RATIO = 0.84f
    const val NODE_LOCK_RATIO = 0.79f

    // FAZ 183b: 300 -> 277. Kuyruk geri gelince hap GENISLEDI ve iki yandan da
    // ekran kenarina dayaniyordu (olculdu: sol kenar 7px, sag kenar 10px).
    // 277 ile iki tarafta da ~40px pay kaliyor.
    const val PILL_BODY = 277f * MAP_SCALE

    // FAZ 183b: 0.95 -> 0.806. Iki hap varligi da normalize edildi (govde
    // ortali, ayni pay). Onceden SAG haplar kuyruksuz `pill_l`i kullaniyordu
    // ve govde/bitmap orani 0.833'tu; yani sag ve sol haplar AYNI `PILL_BODY`
    // degerine ragmen FARKLI boyda ciziliyordu.
    const val PILL_RATIO = 0.806f

    /** Normalize edilmis hap bitmap'inin en-boy orani (iki taraf icin ayni). */
    const val PILL_ASPECT = 0.55f

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
        // FAZ 183c — kullanici: "bilgi alanindaki yazi cok asagi yapismis, olc."
        // OLCULDU (cihaz ekran goruntusu, 1080x2220): panelin gorunur kutusu
        // y=361..551 (yukseklik 190), yazi blogu y=429..531 -- ust bosluk 68,
        // alt bosluk 20. Ortalamak icin blok 24 cihaz pikseli yukari alinmali;
        // 1 referans birimi = ekranGenisligi/941 = 1.148 px oldugundan bu 21
        // referans birimi eder: 288 -> 267.
        Column(modifier = Modifier.offset(x = px(238f, scale), y = px(267f, scale))) {
            Text(
                text = modeLabel,
                color = theme.label,
                fontSize = fixedSp(26f, scale),
                fontWeight = FontWeight.Bold,
                letterSpacing = fixedSp(1.2f, scale)
            )
            Text(
                text = language.pick(
                    tr = "SEVİYE $highestUnlockedLevel", en = "LEVEL $highestUnlockedLevel",
                    it = "LIVELLO $highestUnlockedLevel", fr = "NIVEAU $highestUnlockedLevel",
                    es = "NIVEL $highestUnlockedLevel"
                ),
                color = Color.White,
                fontSize = fixedSp(46f, scale),
                fontWeight = FontWeight.Black
            )
        }

        // 7) Fixed header. Existing handlers are reused.
        AssetImage(theme.back, Ref.BACK, scale, onBack)
        AssetImage(theme.wordFor(language), Ref.WORD, scale)
        Box {
            AssetImage(theme.coin, Ref.COIN, scale)
            // Sayi artik sabit kaydirmayla degil, hapin BOS ALANINA ortalanarak
            // yaziliyor; `AutoFitLabel` sigmayan uzun degerleri (10000+)
            // olcerek kuculttugu icin tasma da kapaniyor.
            Box(
                modifier = Modifier
                    .offset(
                        x = px(Ref.COIN_TEXT_X, scale),
                        y = px(Ref.COIN[1] - Ref.COIN_TEXT_H / 2f, scale)
                    )
                    .width(px(Ref.COIN_TEXT_W, scale))
                    .height(px(Ref.COIN_TEXT_H, scale)),
                contentAlignment = Alignment.Center
            ) {
                // AutoFitLabel `Float` sp bekliyor; degerler yine dp'den
                // cevriliyor ki sistem yazi olcegi hapin icindeki sayiyi
                // sisirmesin (bkz. `fixedSp` notu).
                val density = LocalDensity.current
                AutoFitLabel(
                    text = "${progress.tokens}",
                    color = Color.White,
                    maxSizeSp = with(density) { (34f * scale).dp.toSp().value },
                    minSizeSp = with(density) { (15f * scale).dp.toSp().value },
                    maxLines = 1
                )
            }
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
    val listState = rememberLazyListState()
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

    LaunchedEffect(highestUnlockedLevel) {
        // Oyuncunun bulundugu seviyeyi gorunur kil, iki seviye pay birak.
        // Oge tabanli oldugu icin piksel hesabi gerekmiyor.
        listState.scrollToItem((highestUnlockedLevel - 2).coerceAtLeast(0))
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topInset)
            .testTag("progression_map_list")
    ) {
        // Ilk dugumun ust bosluğu.
        item { Box(Modifier.height(px(nodeStartRef, scale))) }

        // FAZ 180: her oge BIR SEVIYE ARALIGI = karonun bir CEYREGI.
        //
        // ChatGPT'nin surekli-yol cozumu gorsel olarak dogruydu ama
        // `verticalScroll` kullaniyordu: ekranda olmayan seviyeler de
        // besteleniyordu (seviye 200'de 212 dugum + 212 hap). LazyColumn geri
        // geldi; yol hala AYNI tek karodan geliyor, sadece ceyrek ceyrek
        // ciziliyor. Gorsel degismiyor, maliyet ekranla sinirli.
        items(lastLevel) { idx ->
            val level = idx + 1
            val quarter = idx % 4
            PathQuarterRow(
                theme = theme,
                level = level,
                quarter = quarter,
                unlocked = level <= highestUnlockedLevel,
                scale = scale,
                spacingRef = levelSpacingRef,
                pathLeftRef = pathLeftRef,
                pathWRef = pathWRef,
                nodeCenterRef = nodeCenterRef,
                targetScoreForLevel = targetScoreForLevel,
                language = language,
                onSelectLevel = onSelectLevel
            )
        }
        item { Box(Modifier.height(px(260f, scale))) }
    }
}

/**
 * Tek seviye satiri: karonun ilgili CEYREGI + hedef hapi + dugum.
 *
 * Karo dikeyde dort esit ceyrege bolunuyor ve ceyrek sinirlari, merkez
 * cizginin x~%50'ye dondugu noktalar -- yani dugum capalari. Ceyrekler alt
 * alta gelince yol kesintisiz akiyor.
 */
@Composable
private fun PathQuarterRow(
    theme: MapTheme,
    level: Int,
    quarter: Int,
    unlocked: Boolean,
    scale: Float,
    spacingRef: Float,
    pathLeftRef: Float,
    pathWRef: Float,
    nodeCenterRef: Float,
    targetScoreForLevel: (Int) -> Int,
    language: AppLanguage,
    onSelectLevel: (Int) -> Unit
) {
    val tile = ImageBitmap.imageResource(theme.path)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(px(spacingRef, scale))
    ) {
        // Z=3: yolun bu ceyregi.
        Canvas(
            modifier = Modifier
                .offset(x = px(pathLeftRef, scale))
                .width(px(pathWRef, scale))
                .height(px(spacingRef, scale))
        ) {
            val qh = tile.height / 4
            drawImage(
                image = tile,
                srcOffset = IntOffset(0, quarter * qh),
                srcSize = IntSize(tile.width, qh),
                dstOffset = IntOffset(0, 0),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }

        // Z=4: hedef hapi
        TargetPillContinuous(
            theme = theme,
            level = level,
            nodeCx = nodeCenterRef,
            nodeCy = 0f,
            scale = scale,
            // FAZ 181: hedef artik TEK satirda degil IKI satirda. Puan ust
            // satirda (goz once ona bakiyor), "+ 1 satir" kurali altinda.
            topText = language.pick(
                tr = "HEDEF: ${targetScoreForLevel(level)}",
                en = "TARGET: ${targetScoreForLevel(level)}",
                it = "OBIETTIVO: ${targetScoreForLevel(level)}",
                fr = "OBJECTIF : ${targetScoreForLevel(level)}",
                es = "OBJETIVO: ${targetScoreForLevel(level)}"
            ),
            bottomText = language.pick(
                tr = "+ 1 satır",
                en = "+ 1 row",
                it = "+ 1 riga",
                fr = "+ 1 ligne",
                es = "+ 1 fila"
            )
        )

        // Z=5: dugum -- ceyrek sinirinda, yani yolun uzerinde.
        ProgressionNode(
            theme = theme,
            level = level,
            unlocked = unlocked,
            nodeCx = nodeCenterRef,
            nodeCy = 0f,
            scale = scale,
            onSelectLevel = onSelectLevel
        )
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
            // FAZ 183e — kullanici: "1 rakamini yuvarlak icinde ortala."
            // Varligin govdesi bitmap'e ortalandiktan SONRA da kucuk bir
            // kaciklik kaliyordu; olculdu (cihaz, Kolay mod): rakamin murekkep
            // merkezi halkanin merkezinden 5px SOLDA ve 4px ASAGIDA. Kalan
            // fark yazi tipinin satir kutusu metriginden geliyor (Text
            // MURekkebi degil ILERLEMEYI ortaliyor), o yuzden duzeltme burada.
            Text(
                text = "$level",
                color = Color.White,
                fontSize = fixedSp(62f, scale),
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(
                    x = px(4.4f, scale),
                    y = px(-3.2f, scale)
                )
            )
        }
    }
}

/**
 * Hedef etiketi dugumun YANINDA, dugumle AYNI HIZADA duruyor; taraf her
 * seviyede degisiyor (tek seviye sol, cift seviye sag), yol ise dugumun
 * ALTINDAN kesintisiz gecmeye devam ediyor.
 *
 * FAZ 181 - 1. SEVIYE ARTIK ISTISNA DEGIL.
 *
 * Once 1. seviyenin hapi dugumun ALTINDA (yukari bakan kuyruklu `pillUp`
 * varligiyla) duruyordu. Kullanici: "1. levelin hedefi de digerleri gibi kendi
 * hizasinda gostersin." Tek seviye oldugu icin dogal yeri SOL taraf -- boylece
 * 1-2-3-4 sirasi sol/sag/sol/sag olarak duzgun alterniyor ve haritanin ilk
 * ekraninda "farkli duran" bir oge kalmiyor.
 */
/**
 * Haplarin yola dogru iceri alinma miktari (referans birimi).
 *
 * 1 referans birimi = ekranGenisligi / 941; kullanicinin cihazinda
 * 1080 / 941 = 1.148 cihaz pikseli. Kullanici "4px daha yanassin" dedi:
 * 4 / 1.148 = 3.5 birim, yani 22 -> 25.5.
 */
private const val PILL_INSET = 25.5f

@Composable
private fun TargetPillContinuous(
    theme: MapTheme,
    level: Int,
    nodeCx: Float,
    nodeCy: Float,
    scale: Float,
    topText: String,
    bottomText: String
) {
    // Reference alternates labels around an almost-vertical node chain.
    val labelOnRight = level % 2 == 0
    val w = bitmapW(Ref.PILL_BODY, Ref.PILL_RATIO)
    // tail points LEFT when the pill is on the RIGHT, and vice versa.
    val res = if (labelOnRight) theme.pillTailLeft else theme.pillTailRight
    // FAZ 183b: iki taraf ayni cizimin aynasi oldugu icin en-boy da ayni.
    val pillAspect = Ref.PILL_ASPECT
    // FAZ 183g — haplar yola YAKLASTIRILDI.
    //
    // Kullanici: "sag ve soldaki bilgi yazilari harita cizgisine biraz
    // yaklasmali." Onceki tur 2 referans birimiydi (~2px) ve fark edilmiyordu.
    // OLCULDU (cihaz, 1080 genislik): sag hapin kuyruk ucu ile dugum halkasi
    // arasi 81px, ekran kenari payi 34px. 22 referans birimi (~25 cihaz
    // pikseli) iceri alinca bosluk ~56px'e iniyor, kenar payi ~59px'e cikiyor
    // -- hap yola yaklasiyor ama dugume degmiyor.
    val cx = if (labelOnRight) nodeCx + Ref.NODE_BODY / 2f + w / 2f - 10f - PILL_INSET
             else nodeCx - Ref.NODE_BODY / 2f - w / 2f + 10f + PILL_INSET
    val cy = nodeCy + 18f + if (labelOnRight) 2f else 0f

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
        // FAZ 183f — kullanici: "sag sol chrome assetlerin metinleri de
        // ortalansin." Kutu bitmap'in TAMAMINI kapsiyor, bitmap ise
        // dikdortgen + KUYRUK. Metin kutuya ortalaninca dikdortgenin degil
        // "dikdortgen+kuyruk"un merkezine oturuyor, yani kuyrugun ters
        // yonune kayiyor. Olculdu (uc tema): dikdortgenin merkezi bitmap
        // merkezinden 0.039 x genislik kadar kuyruk YONUNUN TERSINDE.
        val textShift = w * 0.039f * (if (labelOnRight) 1f else -1f)
        // FAZ 181: punto 25 -> 23 (kullanici: "hedeflerin yazilari 2px
        // azalsin"). Referans birimi oldugu icin ekranda ~2px'e denk geliyor.
        // Satir yuksekligi puntonun ~1.05'i: iki satir hapin govdesinde
        // kaliyor, varsayilan (~1.4x) bosluk hapin disina tasiyordu.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = px(textShift, scale))
        ) {
            Text(
                text = topText,
                color = Color.White,
                fontSize = fixedSp(23f, scale),
                lineHeight = fixedSp(24f, scale),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = bottomText,
                color = Color.White,
                fontSize = fixedSp(23f, scale),
                lineHeight = fixedSp(24f, scale),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * 9-slice raster blit for the empty progression panel. Corners stay native;
 * only edges/center stretch. No vector redraw is introduced.
 *
 * FAZ 183c — DIKISLER KAPATILDI.
 *
 * Kullanici: "bilgi kutusunun zemini kirli." Nokta deseni varliktan silindikten
 * SONRA bile panelde ince dikey/yatay CIZGILER kaliyordu (cihaz ekran
 * goruntusunde olculdu: panelin sag tarafinda iki dikey, ortasinda bir yatay).
 *
 * Kok neden bu fonksiyondaydi, varlikta degil: dokuz parcanin hedef konumu ve
 * boyu AYRI AYRI `toInt()` ile KIRPILIYORDU. Kirpma yon degistirdiginde iki
 * komsu parca arasinda 1 piksellik bosluk (ya da bindirme) olusuyor; duz bir
 * zemin uzerinde bu, gorunur bir cizgi demek.
 *
 * Duzeltme: parcalarin hedef SINIRLARI once tek tek yuvarlanip ORTAK sinir
 * dizisine yaziliyor, genislikler o sinirlardan cikariliyor. Boylece komsu
 * parcalar tanim geregi ayni pikselde bitip basliyor -- bosluk imkansiz.
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
        val dx = cx * k
        val dy = cy * k

        // ORTAK sinirlar: her parca bu dizideki iki komsu degerin arasini
        // doldurur, yani aralarinda bosluk kalamaz.
        val xs = intArrayOf(
            0,
            dx.roundToInt().coerceIn(0, size.width.toInt()),
            (size.width - dx).roundToInt().coerceIn(0, size.width.toInt()),
            size.width.roundToInt()
        )
        val ys = intArrayOf(
            0,
            dy.roundToInt().coerceIn(0, size.height.toInt()),
            (size.height - dy).roundToInt().coerceIn(0, size.height.toInt()),
            size.height.roundToInt()
        )
        val sxs = intArrayOf(0, cx, iw - cx, iw)
        val sys = intArrayOf(0, cy, ih - cy, ih)

        for (r in 0..2) {
            for (c in 0..2) {
                val sw = sxs[c + 1] - sxs[c]
                val sh = sys[r + 1] - sys[r]
                val ow = xs[c + 1] - xs[c]
                val oh = ys[r + 1] - ys[r]
                if (sw <= 0 || sh <= 0 || ow <= 0 || oh <= 0) continue
                drawImage(
                    image = img,
                    srcOffset = IntOffset(sxs[c], sys[r]),
                    srcSize = IntSize(sw, sh),
                    dstOffset = IntOffset(xs[c], ys[r]),
                    dstSize = IntSize(ow, oh)
                )
            }
        }
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
