package com.miniappfactory.boomblocks.ui.levels

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.R
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.common.WanderingPiecesBackground
import com.miniappfactory.boomblocks.ui.components.GameBackButton
import com.miniappfactory.boomblocks.ui.components.GameTitle
import com.miniappfactory.boomblocks.ui.components.GameCoinPill
import com.miniappfactory.boomblocks.ui.components.GameIconButton
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.theme.GameSurfaces
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
import com.miniappfactory.boomblocks.ui.theme.ComfortTeal
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGold
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.NeonPurple
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import kotlin.math.sin

// ModeSelectScreen'deki Challenge kart aksanniyla (Color(0xFFFF6B35)) ayni —
// Pro Mode'un her yerde tutarli bir "marka rengi" olmasi icin.
private val ProModeOrange = Color(0xFFFF6B35)

// Faz 152: haritanin oyuncunun ULASTIGI seviyenin otesinde kac kilitli dugum
// gosterecegi. Bkz. LevelMapScreen icindeki gerekce.
private const val LEVEL_MAP_LOOKAHEAD = 12

// Faz 77: Pro Mode kendi haritasi icin bu ekrani AYNEN yeniden kullaniyor —
// eskiden `progress.highestUnlockedLevel`/`progress.levelStars`/`LevelGenerator.
// forLevel` DOGRUDAN icerde okunuyordu (Seviyeli Mod'a kilitli). Artik cagiran
// taraf (AppNavigation) hangi ilerleme/hedef egrisinin kullanilacagini
// parametre olarak veriyor, bu ekran mod-agnostik.
@Composable
fun LevelMapScreen(
    progress: PlayerProgress,
    highestUnlockedLevel: Int,
    levelStars: Map<Int, Int>,
    targetScoreForLevel: (Int) -> Int,
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    isChallengeMode: Boolean = false,
    // Faz 128: Comfort Mode (TR "KOLAY MOD") — ayni harita, kendi basligi/rengi.
    isComfortMode: Boolean = false,
    onSelectLevel: (Int) -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    // Kariyer/Pro/Kolay prosedurel ve SINIRSIZ (bkz. LevelGenerator) — sabit bir
    // "son bolum" yok, o yuzden harita "ulasilan + N" dugum cizer. N Faz 3'ten
    // beri 3'tu.
    //
    // Faz 152: N 3 -> 12. Kullanici once "haritalar sadece 4 level gozukuyor,
    // onu +10 level yapalim" dedi, sonra "hep +12 ciz" ile netlestirdi.
    // Sorun: yeni oyuncu haritayi actiginda 4 dugum goruyordu, oyun kucuk/
    // bitmis gibi duruyordu ve kaydirilacak bir sey olmadigi icin "devami var"
    // hissi hic olusmuyordu. 12, ekrana sigandan fazla dugum birakiyor (liste
    // gercekten kaydiriliyor) ama hala ULASILABILIR bir ufuk gosteriyor —
    // 50 dugum cizmek kilitli bir duvar gibi durup tersine caydirici olurdu.
    //
    // Maliyet yok: liste LazyColumn, yani sanallastirilmis; ekranda olmayan
    // dugum bestelenmiyor. Otomatik kaydirma da (asagidaki LaunchedEffect)
    // oyuncunun seviyesini hedefliyor, ek dugumler ONUN ALTINDA kaliyor —
    // acilis gorunumu degismiyor.
    val lastLevel = highestUnlockedLevel + LEVEL_MAP_LOOKAHEAD
    val accentColor = when {
        isChallengeMode -> ProModeOrange
        isComfortMode -> ComfortTeal
        else -> NeonCyan
    }

    // Faz 115l — HATA DUZELTMESI (kullanici gercek cihazda, dark mode'da
    // gorup "haritanın kotu tasarımı" dedi). Onceki (Faz 115) gecisi kilit
    // ikonunu ve dugum gradyanini duzeltmisti ama BASKA bir sorunu
    // KACIRMISTI: `palette.cardAlt` (kilitli dugum dolgusu) ve
    // `palette.cardBorder` (kilitli dugum KENARLIGI) dark temada AYNI RENK
    // (#334155) — yani kilitli dugumun kenarligi kendi dolgusunun icinde
    // KAYBOLUYORDU, ekranda neredeyse gorunmez bir leke olarak kaliyordu.
    // Ayni sorun kilitli yol cizgisinde de vardi (`cardBorder` alpha 0.35,
    // zaten koyu bir zeminde neredeyse hicbir sey). Ayrica ekran tamamen
    // duz `palette.background` — hicbir doku/derinlik yok, dugumler arasi
    // buyuk bosluklarla birlikte "bos/bitmemis" hissi yaratiyordu.
    // Faz 158: elle kurulan gradyan + iki radyal leke, ortak
    // `GameScreenBackground` bilesenine devredildi (gok gradyani + kose
    // bloklari + parilti + zemin bandi). ONEMLI: `accentOverride` ile MODUN
    // rengi geciliyor — Kariyer camgobegi, Pro turuncu, Kolay nane kaliyor;
    // zemin bandi/gok tonu skin'in kendi paletinden turuyor. Yani hem mod
    // kimligi hem skin kimligi korunuyor.
    val surfaces = rememberGameSurfaces(skin, darkMode, listOf(accentColor, accentColor))
    Box(modifier = Modifier.fillMaxSize()) {
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            accentOverride = listOf(accentColor, accentColor),
            modifier = Modifier.matchParentSize()
        )
        // Faz 124: kullanici "aynısı Pro ve Kariyer modları için de geçerli"
        // dedi — ModeSelectScreen/TermsAcceptScreen/OnboardingScreen'deki AYNI
        // gezinen-oyun-parcasi katmani (bkz. `ui/common/WanderingPiecesBackground.kt`).
        // Bu TEK ekran-seviyesi Canvas, LazyColumn'un ARKASINDA sabit duruyor —
        // liste kaydikca parcalar ekranda sabit kalir, eski per-dugum statik
        // tek-kup cizimi (drawMapConfettiCube) buyuzden kaldirildi.
        WanderingPiecesBackground(modifier = Modifier.fillMaxSize())
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            LevelMapHeader(
                surfaces = surfaces,
                progress = progress,
                language = language,
                palette = palette,
                isChallengeMode = isChallengeMode,
                isComfortMode = isComfortMode,
                accentColor = accentColor,
                onOpenMissions = onOpenMissions,
                onOpenSettings = onOpenSettings,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Faz 115n: "X/Y tamamlandi" kesri kaldirildi — Y (lastLevel) gercek
            // bir toplam degildi, sadece render-zamani dugum sayisiydi (bkz.
            // CareerProgressCard yorumu). Panel artik sadece ulasilan seviyeyi
            // gosteriyor.
            CareerProgressCard(
                language = language,
                palette = palette,
                accentColor = accentColor,
                currentLevel = highestUnlockedLevel
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Faz 42: onceden duz bir LazyColumn liste kartlari alt alta diziyordu
            // (kullanici: "yıldızların anlamı yok, düz liste gibi... slalomlu eğlenceli
            // bir harita olsun"). Her ogenin x konumu index'e gore bir sinus dalgasiyla
            // hesaplaniyor (saf formul, olculmus komsu-oge pozisyonuna ihtiyac yok —
            // LazyColumn sanallastirmasiyla tam uyumlu), aralarindaki kesikli çizgi de
            // ayni formulle her ogenin KENDI Canvas'inda (onceki->bu ogenin x'i) ciziliyor.
            // Faz 62: kullanici "haritada direkt kaldığın basamağa otomatik
            // kaydırmıyor" dedi — liste her acilista level 1'den (en ustten)
            // basliyordu, oyuncu her seferinde elle asagi kaydirmak zorunda
            // kaliyordu. Ekran acilir acilmaz, ekstra bir kaydirma animasyonu
            // gostermeden (`scrollToItem`, `animateScrollToItem` DEGIL),
            // oyuncunun oynayabilecegi bir sonraki seviyeye ataniyor —
            // birkac tamamlanmis seviyeyi de baglam icin ustte gorebilsin diye
            // 2 dugum kadar yukarisina.
            val listState = rememberLazyListState()
            LaunchedEffect(Unit) {
                val targetIndex = (highestUnlockedLevel - 1).coerceIn(0, lastLevel - 1)
                listState.scrollToItem((targetIndex - 2).coerceAtLeast(0))
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_map_list"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
            ) {
                items(count = lastLevel, key = { it + 1 }) { index ->
                    val levelNumber = index + 1
                    val unlocked = levelNumber <= highestUnlockedLevel
                    // Faz 43: kullanici "yıldızların anlamı yok, gitsin, tamamlanan level
                    // yeşil olsun" dedi — yildizlar kaldirildi, tamamlanma durumu artik
                    // dugumun rengiyle gosteriliyor. levelStars'ta kayit varsa (recordLevelResult
                    // sadece seviye bitirilince yaziyor) o seviye tamamlanmis demektir.
                    val completed = levelStars[levelNumber] != null
                    val isLastItem = index == lastLevel - 1
                    LevelPathNode(
                        levelNumber = levelNumber,
                        targetScore = targetScoreForLevel(levelNumber),
                        unlocked = unlocked,
                        completed = completed,
                        accentColor = accentColor,
                        hasPreviousNode = index != 0,
                        // Faz 115u: bir sonraki dugumun x'i — S-kavisi artik BU
                        // ogenin KENDI dugumunden bir sonraki ogenin dugumune
                        // dogru, tum oge yuksekligini kullanarak ciziliyor (bkz.
                        // LevelPathNode ici aciklama).
                        nextXFraction = if (isLastItem) null else pathXFraction(index + 1),
                        currXFraction = pathXFraction(index),
                        isLastItem = isLastItem,
                        language = language,
                        palette = palette,
                        onClick = { if (unlocked) onSelectLevel(levelNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelMapHeader(
    surfaces: GameSurfaces,
    progress: PlayerProgress,
    language: AppLanguage,
    palette: BlastPalette,
    isChallengeMode: Boolean = false,
    isComfortMode: Boolean = false,
    accentColor: Color = NeonCyan,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Faz 25: "SEVİYELER" basligi ile sag taraftaki jeton kapsulu arasinda
        // hicbir esneme payi yoktu — dar ekranlarda ikisi "dip dibe" cakisiyordu
        // (kullanici geri bildirimi, ekran goruntusuyle dogrulandi). Sol grup artik
        // `weight(1f, fill=false)` ile sinirli ve basligin kendisi tek satirda
        // gerekirse ellipsis ile kisaliyor, sag gruptan asla alan calmiyor.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Faz 158: seffaf IconButton yerine kabartmali oyun tusu.
            GameBackButton(
                onClick = onBack,
                surfaces = surfaces,
                contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                size = 40.dp,
                modifier = Modifier.testTag("level_map_back_button")
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Faz 161 — kullanici istegi: "modlari sectikten sonra gelen
            // haritalardaki PRO MOD / KARIYER / KOLAY MOD yazilari menu
            // ekranindaki gibi kabartmali olsun."
            //
            // Duz `Text` yerine `GameTitle`: menudeki baslik malzemesinin
            // AYNISI (kalin kontur + gradyan dolgu + ic isik + golge). Boylece
            // oyuncu mod kartindan haritaya gecerken ayni gorsel dile devam
            // ediyor. `surfaces.accentText` ile saglanan acik/koyu tema
            // kontrast guvencesi GameEmblem'in kendi icinde yasiyor.
            GameTitle(
                surfaces = surfaces,
                text = if (isChallengeMode) {
                    language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO")
                } else if (isComfortMode) {
                    // Faz 128: mod kartindaki adla birebir ayni — oyuncu "KOLAY MOD"
                    // yazan karta basip baska baslikli bir ekrana dusmesin.
                    language.pick(tr = "KOLAY MOD", en = "COMFORT MODE", it = "MODALITÀ COMFORT", fr = "MODE CONFORT", es = "MODO CONFORT")
                } else {
                    // Faz 104: mod adi "SEVİYELİ" -> "KARİYER" olunca bu harita basligi da
                    // ("SEVİYELER") modun adiyla hizalandi — oyuncu mod kartinda "KARİYER"
                    // yazan butona basip "SEVİYELER" baslikli bir ekrana dusmesin.
                    language.pick(tr = "KARİYER", en = "CAREER", it = "CARRIERA", fr = "CARRIÈRE", es = "CARRERA")
                },
                fontSize = 22.sp,
                // KIRPMA DEGIL SARMA: tek satirda "KOLAY M..." diye kesiliyordu.
                // Kullanici: "boyle durumlarda kirpmak degil wrap yapsak daha
                // iyi." En uzun cevirilerde (MODALITA COMFORT, MODE CONFORT)
                // ikinci satira insin, bilgi kaybolmasin.
                maxLines = 2,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Faz 72: sag gruptaki IconButton'lar varsayilan 48dp dokunma alaniyla
        // aralarinda gorsel bir bosluk birakiyordu (kullanici defalarca bildirdi:
        // "kupa ile disli arasinda cok bosluk var"). Dokunma hedefi 40dp'ye
        // dusuruldu ve Spacer'lar kaldirildi/daraltildi — grup artik saga
        // yapisik, sikistirilmis tek blok halinde duruyor.
        // Faz 158: jeton kapsulu ve iki tus artik ortak kitten
        // (`GameCoinPill` / `GameIconButton`) — mod secim ekranindaki ust
        // barla AYNI malzeme. Faz 72'deki "kupa ile disli arasinda cok
        // bosluk var" sikayeti korunuyor: aralar 4dp, grup saga yapisik.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GameCoinPill(
                amount = "${progress.tokens}",
                surfaces = surfaces,
                fontSize = 13.sp,
                iconSize = 16.dp,
                modifier = Modifier.testTag("level_map_token_pill")
            )

            GameIconButton(
                icon = Icons.Default.EmojiEvents,
                contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                onClick = onOpenMissions,
                surfaces = surfaces,
                accent = NeonPurple,
                size = 38.dp,
                modifier = Modifier.testTag("level_map_missions_button")
            )

            GameIconButton(
                icon = Icons.Default.Settings,
                contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                onClick = onOpenSettings,
                surfaces = surfaces,
                size = 38.dp,
                modifier = Modifier.testTag("level_map_settings_button")
            )
        }
    }
}

// Faz 115n — HATA DUZELTMESI (kullanici: "0/4 gözükmesi anlamlı degil").
// Kok sebep: "Y" olarak kullandigim `lastLevel = highestUnlockedLevel + N`
// gercek bir toplam DEGIL — sadece "kac dugum render edilsin" render-zamani
// sayisi (Kariyer prosedurel/sinirsiz, sabit bir toplam bolum sayisi yok).
// Onu "toplam" gibi gostermek yanlis veri sunmakti. Kullanicinin istegiyle
// panel sadelestirildi: tek bilgi, oyuncunun ULASTIGI seviye — kesir yok,
// ayrica "MEVCUT" sag sutunu da kaldirildi (ayni bilginin tekrariydi).
@Composable
private fun CareerProgressCard(
    language: AppLanguage,
    palette: BlastPalette,
    accentColor: Color,
    currentLevel: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        lerp(palette.card, accentColor, 0.16f),
                        palette.card
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(lerp(accentColor, Color.White, 0.3f), accentColor.copy(alpha = 0.4f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.icon_career),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = language.pick(
                        tr = "KARİYER İLERLEMESİ", en = "CAREER PROGRESS", it = "PROGRESSO CARRIERA",
                        fr = "PROGRESSION DE CARRIÈRE", es = "PROGRESO DE CARRERA"
                    ),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = language.pick(
                        tr = "SEVİYE $currentLevel",
                        en = "LEVEL $currentLevel",
                        it = "LIVELLO $currentLevel",
                        fr = "NIVEAU $currentLevel",
                        es = "NIVEL $currentLevel"
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = palette.textPrimary
                )
            }
        }
    }
}

// Faz 115n — HATA DUZELTMESI (kullanici: "büyük olmak zorunda degil, yuvarlaklar
// dümdüz inmek zorunda degil"). Faz 115m'de genlik 0.30, frekans 1.05 idi —
// matematiksel olarak genis (ekranin %60'i) ama sadece ~4-5 gorunur dugumluk
// bir pencerede (sin periyodu ~6 index) dalga tam bir "S" turu atamiyor,
// EGIMLI DUZ BIR CIZGI gibi hissettiriyordu (kullanicinin sikayeti dogru).
// Frekans arttirilinca (1.05 -> 1.7) ayni gorunur pencerede DAHA FAZLA
// salinim tamamlaniyor — mockup'taki gibi belirgin zigzag.
// Genlik 0.30'da kaldi (glow ile birlikte kenar guvenlik payi tutuyor);
// canlilik SADECE frekanstan (1.05 -> 1.7) geliyor.
private fun pathXFraction(index: Int): Float =
    0.5f + 0.30f * sin(index * 1.7f)

// Faz 115n: dugum 72dp -> 60dp (kullanici: "bu kadar büyük olmak zorunda
// degil"). Oge yuksekligi de orantili daraltildi (124dp -> 96dp) — hem daha
// fazla dugum ekranda gorunuyor hem yukaridaki daha sik dalga formuluyle
// birlikte gercekten kivrilan bir yol hissi olusuyor.
private val LEVEL_PATH_ITEM_HEIGHT = 96.dp
private val LEVEL_NODE_SIZE = 60.dp
// Dugumun DUSEY merkezi ogenin tepesinden bu kadar asagida — sabit bir dp
// degeri olarak tutuluyor ki Canvas'taki cizgi ile Column'daki gercek dugum
// konumu HER ZAMAN birebir ortussun (BiasAlignment ile tahmin degil).
private val LEVEL_NODE_CENTER_Y = 40.dp

// Faz 115m: 3 durumlu harita rengi. Kullanicinin istegiyle "tamamlandi"
// YESIL'den ALTIN'a tasindi (mockup'taki gibi), aktif dugum camgobegi,
// kilitli dugum mor — ChatGPT promptunun istedigi doğrudan bu esleme.
private val CompletedRouteColor = NeonGold
private val LockedRouteColor = NeonPurple

// Faz 115m: harita zemininde dagilmis, dondurulmus mini kupler kullanilmisti;
// Faz 124'te kullanici "aynısı Pro ve Kariyer modları için de geçerli" dedi
// (ModeSelectScreen/TermsAcceptScreen/OnboardingScreen'deki "farklı parçalar +
// gezinme" istegi) — per-dugum statik tek-kup ciziimi kaldirildi, yerine
// ekran seviyesinde ortak `WanderingPiecesBackground` eklendi (bkz.
// LevelMapScreen composable'inin govdesi).

// Faz 115m: yol tek renk+kalinlik degil, 3 katmanli (dis isima / koyu taban /
// parlak ic hat) — ChatGPT promptunun istedigi "kalin, parlak, mockup gibi
// yol" hissi. Kesikli cizgi KALDIRILDI (dolu/solid), promptun acikca
// istedigi "Do not use the current thin dashed line" talebi.
//
// Faz 115t — HATA DUZELTMESI (kullanici: "cizgiler cok dik, grafik gibi,
// kivrimli olsa iyi olmaz mi"). Kok sebep: duz `drawLine(start, end)` —
// dugume giren "varis" izi capraz, dugumden cikan "cikis" izi dikeydi, ikisi
// KESKIN bir kose ile birlesiyordu (cizgi grafigi/finans grafigi gibi
// duruyordu, mockup'taki yumusak yol gibi degil).
//
// Duzeltme: duz cizgi yerine kubik Bezier (S-kavis). Kontrol noktalari
// start/end ile AYNI X'te, dikey ortada — bu, egrinin start'tan TAM DIKEY
// cikip end'e TAM DIKEY varmasini saglar. Onemi: bu egri, HEMEN ONCESINDE ve
// SONRASINDA gelen dikey "cikis" saplarinin (bkz. asagidaki `!isLastItem`
// cagrisinin cizdigi dumduz dikey parca) yonuyle TAM ORTUSUYOR — yani kesin
// bir kose yerine kesintisiz, akan bir S harfi olusuyor. `start.x == end.x`
// oldugunda (dikey cikis izi icin) kontrol noktalari da ayni x'te kalir,
// egri kendiliginden duz cizgiye doner — fonksiyon iki cagriya da (capraz
// varis + dikey cikis) DEGISIKLIKSIZ uygulanabilir.
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGlowRoute(
    start: Offset,
    end: Offset,
    color: Color
) {
    val midY = (start.y + end.y) / 2f
    val curve = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(start.x, midY, end.x, midY, end.x, end.y)
    }
    val outerGlow = androidx.compose.ui.graphics.drawscope.Stroke(width = 26f, cap = StrokeCap.Round)
    val darkBase = androidx.compose.ui.graphics.drawscope.Stroke(width = 13f, cap = StrokeCap.Round)
    val brightCore = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = StrokeCap.Round)
    val gloss = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f, cap = StrokeCap.Round)
    // 1) dis isima — genis, dusuk alfa
    drawPath(curve, color = color.copy(alpha = 0.22f), style = outerGlow)
    // 2) koyu taban — yolun "govdesi"
    drawPath(curve, color = lerp(color, Color.Black, 0.45f).copy(alpha = 0.85f), style = darkBase)
    // 3) parlak ic hat
    drawPath(curve, color = color.copy(alpha = 0.95f), style = brightCore)
    // 4) ince parlaklik — yolun ustunde cam hissi
    drawPath(curve, color = lerp(color, Color.White, 0.6f).copy(alpha = 0.5f), style = gloss)
}

// Faz 42: baglanti cizgisi iki parcaya bolunuyor. Faz 115u'ya kadar bolunme
// sekli "duz dikey cikis + capraz varis"ti; capraz kismin TUMU sadece
// LEVEL_NODE_CENTER_Y (40dp) kadarlik dar bir dikey alana sikisiyordu.
//
// Faz 115u — HATA DUZELTMESI (kullanici ekran goruntusuyle gosterdi: yol
// "ilmek/dugum" gibi kabarip tangled gorunuyordu, ChatGPT mockup'indaki gibi
// temiz genis bir S degil). Kok sebep MATEMATIKSEL: kubik Bezier'de
// baslangic/bitis teget YONU dikey olacak sekilde zorlaniyordu (S-kavis icin
// gerekli) ama bunun icin sadece 40dp dikey alan varken YATAYDA ~genislik*0.30
// kadar (bazen 90dp+) kaymasi gerekiyordu — bu oran (dar dikey alan + genis
// yatay kayma + zorunlu dikey teget) kacinilmaz olarak kavis/ilmek yaratiyor.
//
// Duzeltme: bolunme noktasi degisti. Artik her oge KENDI dugumunden BIR
// SONRAKI ogenin dugumune dogru TUM oge yuksekligini (LEVEL_PATH_ITEM_HEIGHT,
// ~96dp) kullanarak TEK bir S-kavisi ciziyor ("inis" — bkz. asagida). Bu,
// eskisinden ~2.4 kat fazla dikey alan demek, kavis cok daha yumusak/genis
// oluyor. Bir SONRAKI oge, kendi tepesinden (bu inis'in TAM ULASTIGI x'e —
// yani KENDI dugumunun x'ine, cunku nextXFraction = o ogenin currXFraction'i)
// kendi dugumune kadar KISA, DUZ bir "giris sapi" ciziyor — hizalama
// `prevXFraction`i eslestirmeye degil, TASARIM GEREGI garantili (inis nereye
// varirsa, giris sapi zaten oradan baslar).
@Composable
private fun LevelPathNode(
    levelNumber: Int,
    targetScore: Int,
    unlocked: Boolean,
    completed: Boolean,
    accentColor: Color,
    hasPreviousNode: Boolean,
    nextXFraction: Float?,
    currXFraction: Float,
    isLastItem: Boolean,
    language: AppLanguage,
    palette: BlastPalette,
    onClick: () -> Unit
) {
    // Faz 115m: 3 durumlu renk — tamamlandi=ALTIN, aktif/oynanabilir=aksan
    // (camgobegi Kariyer'de, turuncu Pro'da), kilitli=MOR. Kullanicinin
    // ChatGPT promptuyla istedigi eslemenin birebir uygulanmasi.
    val nodeAccent = when {
        completed -> CompletedRouteColor
        unlocked -> accentColor
        else -> LockedRouteColor
    }
    val routeColor = nodeAccent
    val borderBrush = Brush.verticalGradient(
        listOf(lerp(nodeAccent, Color.White, 0.35f), nodeAccent, nodeAccent.copy(alpha = 0.55f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LEVEL_PATH_ITEM_HEIGHT)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nodeY = LEVEL_NODE_CENTER_Y.toPx()
            // Faz 115u: "giris sapi" — bir onceki ogenin inis kavisi TAM BURAYA
            // (kendi x'imize) ulasti, o yuzden burasi HER ZAMAN kisa ve duz
            // (ayni x, sadece dikey). Yol artik 3 katmanli/dolu (bkz.
            // drawGlowRoute), kesikli cizgi degil.
            if (hasPreviousNode) {
                drawGlowRoute(
                    start = Offset(currXFraction * size.width, 0f),
                    end = Offset(currXFraction * size.width, nodeY),
                    color = routeColor
                )
            }
            // Faz 115u: "inis" — KENDI dugumumuzden bir SONRAKI ogenin
            // dugumune, TUM oge yuksekligini kullanan genis/yumusak S-kavisi.
            if (!isLastItem && nextXFraction != null) {
                drawGlowRoute(
                    start = Offset(currXFraction * size.width, nodeY),
                    end = Offset(nextXFraction * size.width, size.height),
                    color = routeColor
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(BiasAlignment(currXFraction * 2f - 1f, -1f))
                .offset(y = LEVEL_NODE_CENTER_Y - LEVEL_NODE_SIZE / 2)
                // Faz 115v — HATA DUZELTMESI (kullanici ekran goruntusuyle
                // gosterdi: "cizgiler yuvarlaklarin icinde de devam ediyor").
                // Kok sebep cizgide DEGIL, dugumdeydi: kilitli dugumun TUM
                // Column'una `.alpha(0.7f)` uygulaniyordu — daire dolgusu +
                // kenarligi dahil TUM govde SAYDAM composite ediliyordu, bu
                // yuzden arkasindaki Canvas'taki parlak yol cizgisi saydamligin
                // ARDINDAN goruluyordu (cizginin kendisi yanlis degildi, onu
                // ortmesi gereken daire yeterince opak degildi). Alfa
                // kaldirildi — "kilitli" hissi artik SADECE rengin kendisinden
                // (soluk/mor-arka plan karisimi gradyan, bkz. `nodeFill`)
                // geliyor, bu da ChatGPT promptunun "kilitli dugumler hala
                // premium gorunmeli, neredeyse gorunmez OLMAMALI" istegiyle de
                // ortusuyor.
                .clickable(enabled = unlocked, onClick = onClick)
                .testTag("level_card_$levelNumber")
        ) {
            val isCurrent = unlocked && !completed

            // Faz 115m: aktif/oynanabilir dugum yavasca "nefes alir" — mockup'in
            // "subtle breathing pulse" istegi, sadece TEK dugum (frontier) icin.
            val pulse = if (isCurrent) {
                val infinite = rememberInfiniteTransition(label = "nodePulse")
                infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "nodePulseValue"
                ).value
            } else 1f

            val nodeFill = when {
                completed -> Brush.verticalGradient(
                    listOf(lerp(nodeAccent, Color.White, 0.55f), nodeAccent, lerp(nodeAccent, Color.Black, 0.45f))
                )
                unlocked -> Brush.verticalGradient(
                    listOf(lerp(nodeAccent, Color.White, 0.30f), palette.card, lerp(nodeAccent, Color.Black, 0.20f))
                )
                // Faz 115l/m: duz tek renk yerine hafif dikey gradyan — ama mor
                // aksanla, ChatGPT promptunun "hala premium/cazip gorunmeli,
                // neredeyse gorunmez olmamali" istegi.
                else -> Brush.verticalGradient(
                    listOf(
                        lerp(LockedRouteColor, palette.background, 0.55f),
                        lerp(LockedRouteColor, palette.background, 0.72f),
                        lerp(LockedRouteColor, Color.Black, 0.35f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(LEVEL_NODE_SIZE)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    // Glow, dugumun OLCULEN boyutunu (LEVEL_NODE_SIZE) DEGISTIRMEZ —
                    // drawBehind ile kutu sinirlarinin disina, layout'u etkilemeden
                    // ciziliyor. Aksi halde Column'daki dugum LEVEL_NODE_CENTER_Y'nin
                    // varsaydigi konumdan kayar ve Canvas'taki yolla hizasi bozulur.
                    .drawBehind {
                        val glowAlpha = if (isCurrent) 0.45f else 0.28f
                        val glowPad = if (isCurrent) 14.dp.toPx() else 8.dp.toPx()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(nodeAccent.copy(alpha = glowAlpha), Color.Transparent)
                            ),
                            radius = size.minDimension / 2f + glowPad
                        )
                    }
                    .clip(CircleShape)
                    .background(nodeFill)
                    .border(3.dp, borderBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Text(
                        text = "$levelNumber",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = if (completed) Color(0xFF3D2300) else Color.White
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.icon_lock),
                        contentDescription = language.pick(tr = "Kilitli", en = "Locked", it = "Bloccato", fr = "Verrouillé", es = "Bloqueado"),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Faz 115m: tamamlanma rozeti — ChatGPT promptu "stars YOK,
                // completion checkmark" diyor (bkz. yukaridaki dosya-basi not:
                // Faz 43'te yildiz sistemi TAMAMEN kaldirilmisti, geri
                // getirilmiyor). Kucuk, kosede, beyaz zeminde koyu tik.
                if (completed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 1.dp, y = 1.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, lerp(nodeAccent, Color.Black, 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = lerp(nodeAccent, Color.Black, 0.35f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Faz 115m: "HEDEF: X + 1 satır" artik ciplak metin degil, kucuk
            // koyu-lacivert bir levha (mockup'taki "small target plaque"),
            // dugumun renginde ince bir kenarligi var.
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                    .border(1.dp, nodeAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    // Faz 72: oynanis kurali artik "hedef puan + en az 1 satir/sutun
                    // patlatma" — etiket bu kurali acikca yansitiyor, aksi halde
                    // kullanici hedefi gecince neden bitmedigini anlamaz.
                    text = language.pick(
                        tr = "HEDEF: $targetScore + 1 satır",
                        en = "TARGET: $targetScore + 1 row",
                        it = "OBIETTIVO: $targetScore + 1 riga",
                        fr = "OBJECTIF : $targetScore + 1 ligne",
                        es = "OBJETIVO: $targetScore + 1 fila"
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
