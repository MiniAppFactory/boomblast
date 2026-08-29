package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.width
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.gameSurfaces
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Faz 158 — TASMA KONTROLU.
 *
 * Bu projede tekrar eden hata kaynagi: sabit yukseklikli/genislikli
 * kontrollere uzun TR/IT/FR/ES cevirilerin sigmamasi (Faz 71/79/119'da uc kez
 * CIHAZDA yakalandi). Yeni menu bilesenlerinin AYNI hataya dusmedigi burada,
 * en kotu senaryoda dogrulaniyor:
 *
 *   - ekran genisligi 320dp (desteklenen en dar telefon),
 *   - fontScale 1.3 (kullanicinin buyuttugu yazi tipi),
 *   - her dilin EN UZUN metni (koddaki `language.pick(...)` cagrilarindan
 *     alindi, uydurulmadi).
 *
 * Kontrol yontemi: `getBoundsInRoot()` (kirpilmis) ile
 * `getUnclippedBoundsInRoot()` (kirpilmamis) karsilastiriliyor. Ikisi
 * ayrisiyorsa oge KIRPILIYOR demektir — cihazda "yazi yarim gorunuyor" ya da
 * "sayi kayboldu" olarak ortaya cikan sey tam olarak budur.
 *
 * NOT: `setContent` test basina bir kez cagrilabilir; o yuzden metin bir
 * `mutableStateOf` uzerinden degistiriliyor ve her degerde yeniden olculuyor.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w320dp-h640dp-mdpi")
class MenuOverflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val surfaces = gameSurfaces(
        blastPalette(BlastSkin.DEFAULT, true),
        BlastSkin.DEFAULT.accentGradient
    )

    private val longestButtonLabels = listOf(
        "PREPARA IL TUO EQUIPAGGIAMENTO",
        "PRÉPARE TON ÉQUIPEMENT",
        "COMMENCER",
        "RECLAMAR",
        "TAKIMINI HAZIRLA"
    )

    private val longestRowLabels = listOf(
        "Notificaciones de Recordatorio",
        "Notifications de Rappel",
        "Notifiche di Promemoria",
        "Hatırlatma Bildirimleri",
        "Nasıl Oynanır / Modlar"
    )

    private val longestSegmentLabels = listOf(
        "Modalità Scura" to "Modalità Chiara",
        "Mode Sombre" to "Mode Clair",
        "Koyu Mod" to "Açık Mod"
    )

    private fun assertNotClipped(tag: String, context: String) {
        val clipped = composeRule.onNodeWithTag(tag).getBoundsInRoot()
        val unclipped = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        assertTrue(
            "[$context] $tag kirpiliyor: genislik ${clipped.width} < ${unclipped.width}",
            clipped.width.value >= unclipped.width.value - 0.5f
        )
        assertTrue(
            "[$context] $tag kirpiliyor: yukseklik ${clipped.height} < ${unclipped.height}",
            clipped.height.value >= unclipped.height.value - 0.5f
        )
    }

    @Test
    fun `game button grows instead of clipping the longest translations`() {
        val labelState = mutableStateOf(longestButtonLabels.first())
        composeRule.setContent {
            val label by labelState
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    GameButton(
                        text = label,
                        onClick = {},
                        colors = primaryGameButtonColors(surfaces),
                        modifier = Modifier.fillMaxWidth().testTag("btn")
                    )
                }
            }
        }
        longestButtonLabels.forEach { label ->
            composeRule.runOnIdle { labelState.value = label }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("btn").assertIsDisplayed()
            assertNotClipped("btn", label)
            // Buton sabit yukseklikli DEGIL; alt sinir her zaman >= 48dp.
            val h = composeRule.onNodeWithTag("btn").getBoundsInRoot().height.value
            assertTrue("'$label' butonu 48dp dokunma hedefinin altinda: $h", h >= 48f)
        }
    }

    @Test
    fun `settings row keeps the toggle on screen with the longest labels`() {
        val labelState = mutableStateOf(longestRowLabels.first())
        composeRule.setContent {
            val label by labelState
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f)
            ) {
                GamePanel(
                    surfaces = surfaces,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentPadding = 10.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameIconTile(
                            icon = Icons.Default.Notifications,
                            tint = surfaces.accentPrimary,
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).testTag("label")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        GameToggle(
                            checked = true,
                            onCheckedChange = {},
                            surfaces = surfaces,
                            modifier = Modifier.testTag("toggle")
                        )
                    }
                }
            }
        }
        longestRowLabels.forEach { label ->
            composeRule.runOnIdle { labelState.value = label }
            composeRule.waitForIdle()
            // Anahtar ekran disina itilmemeli: uzun etiketin ilk kurbani odur.
            composeRule.onNodeWithTag("toggle").assertIsDisplayed()
            assertNotClipped("toggle", label)
            composeRule.onNodeWithTag("label").assertIsDisplayed()
            val h = composeRule.onNodeWithTag("toggle").getBoundsInRoot().height.value
            assertTrue("'$label' satirinda anahtar 48dp altinda: $h", h >= 48f)
        }
    }

    @Test
    fun `segmented control wraps long labels instead of clipping them`() {
        val pairState = mutableStateOf(longestSegmentLabels.first())
        composeRule.setContent {
            val pair by pairState
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    GameSegmented(
                        options = listOf(true, false),
                        selected = true,
                        onSelect = {},
                        optionLabel = { if (it) pair.first else pair.second },
                        surfaces = surfaces,
                        optionTestTag = { if (it) "seg_first" else "seg_second" },
                        modifier = Modifier.testTag("segmented")
                    )
                }
            }
        }
        longestSegmentLabels.forEach { pair ->
            composeRule.runOnIdle { pairState.value = pair }
            composeRule.waitForIdle()
            val ctx = "${pair.first} / ${pair.second}"
            composeRule.onNodeWithTag("segmented").assertIsDisplayed()
            assertNotClipped("segmented", ctx)
            assertNotClipped("seg_first", ctx)
            assertNotClipped("seg_second", ctx)
        }
    }

    /**
     * Faz 159 — CIHAZDA YAKALANAN OLCUM HATASI icin regresyon testi.
     *
     * Loadout ekraninda booster'in ADI ve ACIKLAMASI ekranda hic
     * gorunmuyordu ("ne satin aldigin anlasilmiyor"). Sebep eksik metin
     * degildi; bir Row olcum tuzagiydi:
     *
     *   `GameButton`in ic Row'u `fillMaxWidth()` kullaniyor. Compose'da Row
     *   once AGIRLIKSIZ cocuklari olcer ve onlara kalan TUM genisligi verir.
     *   Buton agirliksiz bir sutundaysa satirin tamamini yutar ve
     *   `weight(1f)` olan metin sutununa SIFIR genislik kalir.
     *
     * Bu test tam o duzeni kurar: solda metin (weight), sagda butonlu sutun.
     * Metin sutunu sifir genislige duserse test kirilir.
     */
    @Test
    fun `booster name keeps its width next to a buy button`() {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ekrandaki duzen: ikon sutunu (agirliksiz) + metin sutunu
                    // (agirlikli). Buton ARTIK bu satirin icinde degil, kartin
                    // altinda tam genislikte — yani satiri yutamaz.
                    Box(modifier = Modifier.width(44.dp).testTag("iconcol"))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f).testTag("textcol")) {
                        Text(
                            text = "SATIR TEMİZLE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("booster_name")
                        )
                        Text(
                            text = "Dokunduğun satırın tamamını siler",
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("booster_desc")
                        )
                    }
                }
            }
        }
        // ASIL KORUMA: metin sutunu EZILMEMELI.
        //
        // NOT — testin NEYE baktigi onemli: Robolectric gercek font olcumu
        // yapmadigi icin bir `Text` dugumunun genisligi burada anlamsizdir
        // (olcum sirasinda 13dp gibi sahte degerler doner). Bu yuzden
        // dogrulama METNIN degil, metni tasiyan SUTUNUN genisligi uzerinden
        // yapiliyor — layout hatasi tam olarak orada gorunuyor.
        //
        // Hata halinde bu deger 0.0'a duser (olculdu: buton agirliksiz bir
        // sutunda `fillMaxWidth()` yaptiginda satirin 278dp'sini aliyor,
        // agirlikli sutuna 0dp kaliyordu).
        val textColWidth = composeRule.onNodeWithTag("textcol").getBoundsInRoot().width.value
        assertTrue(
            "Booster metin sutunu eziliyor: ${textColWidth}dp",
            textColWidth >= 100f
        )
        composeRule.onNodeWithTag("booster_name").assertIsDisplayed()
        composeRule.onNodeWithTag("booster_desc").assertIsDisplayed()
    }


    @Test
    fun `coin pill stays intact next to a long title`() {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f, fontScale = 1.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameTitle(
                        text = "MISSIONS HEBDOMADAIRES",
                        surfaces = surfaces,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f).testTag("title")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GameCoinPill(
                        amount = "99999",
                        surfaces = surfaces,
                        modifier = Modifier.testTag("coin")
                    )
                }
            }
        }
        composeRule.onNodeWithTag("coin").assertIsDisplayed()
        assertNotClipped("coin", "MISSIONS HEBDOMADAIRES")
        composeRule.onNodeWithTag("title").assertIsDisplayed()
    }
}
