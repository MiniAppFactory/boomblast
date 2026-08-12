package com.miniappfactory.boomblocks.ui.levels

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.PlayerProgress
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.theme.BlastPalette
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
    onSelectLevel: (Int) -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val lastLevel = highestUnlockedLevel + 3
    val accentColor = if (isChallengeMode) ProModeOrange else NeonCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LevelMapHeader(
                progress = progress,
                language = language,
                palette = palette,
                isChallengeMode = isChallengeMode,
                accentColor = accentColor,
                onOpenMissions = onOpenMissions,
                onOpenSettings = onOpenSettings,
                onBack = onBack
            )

            // Faz 77: can rozeti onceden basligin YANINDA (ayni satirda) idi —
            // "PRO MOD" + kalp rozeti birlikte sikisip baslik "PRO ..." diye
            // kirpiliyordu (Faz 72'nin tam duzelttigi "Seviyel..." sorununun
            // ayni tekrari). Artik TAMAMEN AYRI, kendi satirinda — basligin
            // genisligiyle asla yarismiyor.
            if (isChallengeMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = language.pick(tr = "Canlar", en = "Lives", it = "Vite", fr = "Vies", es = "Vidas"),
                        tint = Color(0xFFE53E3E),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = language.pick(
                            tr = "${progress.challengeLives} Can",
                            en = "${progress.challengeLives} Lives",
                            it = "${progress.challengeLives} Vite",
                            fr = "${progress.challengeLives} Vies",
                            es = "${progress.challengeLives} Vidas"
                        ),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53E3E),
                        modifier = Modifier.testTag("level_map_lives_pill")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    LevelPathNode(
                        levelNumber = levelNumber,
                        targetScore = targetScoreForLevel(levelNumber),
                        unlocked = unlocked,
                        completed = completed,
                        accentColor = accentColor,
                        prevXFraction = if (index == 0) null else pathXFraction(index - 1),
                        currXFraction = pathXFraction(index),
                        isLastItem = index == lastLevel - 1,
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
    progress: PlayerProgress,
    language: AppLanguage,
    palette: BlastPalette,
    isChallengeMode: Boolean = false,
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("level_map_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = language.pick(tr = "Geri", en = "Back", it = "Indietro", fr = "Retour", es = "Atrás"),
                    tint = palette.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isChallengeMode) {
                    language.pick(tr = "PRO MOD", en = "PRO MODE", it = "MODALITÀ PRO", fr = "MODE PRO", es = "MODO PRO")
                } else {
                    language.pick(tr = "SEVİYELER", en = "LEVELS", it = "LIVELLI", fr = "NIVEAUX", es = "NIVELES")
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Faz 72: sag gruptaki IconButton'lar varsayilan 48dp dokunma alaniyla
        // aralarinda gorsel bir bosluk birakiyordu (kullanici defalarca bildirdi:
        // "kupa ile disli arasinda cok bosluk var"). Dokunma hedefi 40dp'ye
        // dusuruldu ve Spacer'lar kaldirildi/daraltildi — grup artik saga
        // yapisik, sikistirilmis tek blok halinde duruyor.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Token balance pill
            Surface(
                color = NeonGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("level_map_token_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🪙", fontSize = 14.sp) // 🪙
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.tokens}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGold
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onOpenMissions,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("level_map_missions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = language.pick(tr = "Görevler", en = "Missions", it = "Missioni", fr = "Missions", es = "Misiones"),
                    tint = NeonPurple
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("level_map_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = language.pick(tr = "Ayarlar", en = "Settings", it = "Impostazioni", fr = "Paramètres", es = "Ajustes"),
                    tint = palette.textPrimary
                )
            }
        }
    }
}

// Faz 42: slalom/zigzag harita — index'e gore 0f..1f arasinda salinan bir x
// konumu. Komsu ogelerin gercek olculmus pozisyonuna bagli DEGIL (LazyColumn
// sanallastirmasi hala calisir), sadece index'in kendisinden turetiliyor.
private fun pathXFraction(index: Int): Float =
    0.5f + 0.30f * sin(index * 1.05f)

private val LEVEL_PATH_ITEM_HEIGHT = 132.dp
private val LEVEL_NODE_SIZE = 56.dp
// Dugumun DUSEY merkezi ogenin tepesinden bu kadar asagida — sabit bir dp
// degeri olarak tutuluyor ki Canvas'taki cizgi ile Column'daki gercek dugum
// konumu HER ZAMAN birebir ortussun (BiasAlignment ile tahmin degil).
private val LEVEL_NODE_CENTER_Y = 40.dp

// Faz 42: baglanti cizgisi iki parcaya bolundu — (1) bu dugumden asagi, oge
// sinirina kadar duz bir "cikis" izi, (2) bir onceki dugumun x'inden gelip BU
// dugume varan capraz "varis" izi. Boylece her oge SADECE kendi bilgisiyle
// (prevX, currX, sabit nodeY) cizim yapiyor ama komsu ogelerle tam piksel
// hassasiyetinde birlesiyor — ilk denemede (sadece "varis" izi, oge tepesinden
// baslayan) dugum ile cizginin bittigi nokta arasinda gozle gorulur bir bosluk
// vardi (kullanici: "slalomlu harita" istegi sonrasi ilk halde cizgi dugume
// hic degmiyordu), kok neden buydu.
@Composable
private fun LevelPathNode(
    levelNumber: Int,
    targetScore: Int,
    unlocked: Boolean,
    completed: Boolean,
    accentColor: Color,
    prevXFraction: Float?,
    currXFraction: Float,
    isLastItem: Boolean,
    language: AppLanguage,
    palette: BlastPalette,
    onClick: () -> Unit
) {
    val nodeAccent = if (completed) NeonGreen else accentColor
    val borderBrush = when {
        completed -> Brush.linearGradient(listOf(NeonGreen, NeonGreen))
        unlocked -> Brush.linearGradient(listOf(accentColor, NeonPurple))
        else -> Brush.linearGradient(listOf(palette.cardBorder, palette.cardBorder))
    }
    val pathColor = if (unlocked) nodeAccent.copy(alpha = 0.45f) else palette.cardBorder.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LEVEL_PATH_ITEM_HEIGHT)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nodeY = LEVEL_NODE_CENTER_Y.toPx()
            if (prevXFraction != null) {
                drawLine(
                    color = pathColor,
                    start = Offset(prevXFraction * size.width, 0f),
                    end = Offset(currXFraction * size.width, nodeY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                )
            }
            if (!isLastItem) {
                drawLine(
                    color = pathColor,
                    start = Offset(currXFraction * size.width, nodeY),
                    end = Offset(currXFraction * size.width, size.height),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(BiasAlignment(currXFraction * 2f - 1f, -1f))
                .offset(y = LEVEL_NODE_CENTER_Y - LEVEL_NODE_SIZE / 2)
                .alpha(if (unlocked) 1f else 0.5f)
                .clickable(enabled = unlocked, onClick = onClick)
                .testTag("level_card_$levelNumber")
        ) {
            // Seviye numarasi/kilit rozeti — dugumun kendisi
            // Faz 72: tamamlanmis seviyeler eskiden sadece yesil KENARLIKLI, ici
            // transparan bir daireydi — kullanici bunu "tamamlandi" gibi
            // okumuyordu. Artik tamamlanan daireler ici tamamen yesil DOLU
            // (kabartma/embossed hissi icin hafif ust-sol acik / alt-sag koyu
            // radial gradient) ve rakam beyaza donuyor.
            val completedFill = Brush.verticalGradient(
                colors = listOf(NeonGreen, NeonGreen.copy(alpha = 0.8f))
            )
            Box(
                modifier = Modifier
                    .size(LEVEL_NODE_SIZE)
                    .clip(CircleShape)
                    .then(
                        if (completed) {
                            Modifier.background(completedFill)
                        } else {
                            Modifier.background(if (unlocked) palette.card else palette.cardAlt)
                        }
                    )
                    .border(2.5.dp, borderBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Text(
                        text = "$levelNumber",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (completed) Color.White else nodeAccent
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = language.pick(tr = "Kilitli", en = "Locked", it = "Bloccato", fr = "Verrouillé", es = "Bloqueado"),
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                // Faz 72: oynanis kurali artik "hedef puan + en az 1 satir/sutun
                // patlatma" — haritadaki etiket bu kurali acikca yansitiyor,
                // aksi halde kullanici hedefi gecince neden bitmedigini anlamaz.
                text = language.pick(
                    tr = "HEDEF: $targetScore + 1 satır",
                    en = "TARGET: $targetScore + 1 row",
                    it = "OBIETTIVO: $targetScore + 1 riga",
                    fr = "OBJECTIF : $targetScore + 1 ligne",
                    es = "OBJETIVO: $targetScore + 1 fila"
                ),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
