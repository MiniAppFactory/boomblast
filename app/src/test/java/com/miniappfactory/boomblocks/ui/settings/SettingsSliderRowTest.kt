package com.miniappfactory.boomblocks.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.miniappfactory.boomblocks.ui.components.GameSlider
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.gameSurfaces
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Faz 161 — SES SIDDETI SATIRI.
 *
 * Kullanicinin sikayeti iki basliktaydi:
 *   1. "ses siddeti cok genis yer kapliyor, bar daralip tek satira sigabilir"
 *   2. "50% yazisi iki yerde gereksiz cunku" (hem sagdaki kapsulde hem de
 *      kaydiricinin altindaki 0%/50%/100% kademe satirinin ORTASINDA)
 *
 * Tek satira indirmenin bedeli bu projede tekrar eden hatadir: uzun ceviri
 * ("Volumen de Sonido" / "Volume Sonore" / "Volume Suoni") satiri yer, geriye
 * kullanilamaz genislikte bir kaydirici kalir. Bu testler tam olarak ONU
 * kovaliyor.
 *
 * NOT — Robolectric gercek font olcumu yapmaz; `SettingsSliderRow` icindeki
 * `rememberTextMeasurer` burada sahte (cok kucuk) genislikler dondurur, yani
 * bilesen her zaman YAN YANA dala girer. Testin degeri de zaten orada: yan
 * yana dalin yapisal garantilerini (kaydirici genisligi, 48dp dokunma hedefi,
 * kapsulun kirpilmamasi, degerin TEK kez yazilmasi) dogruluyor. Alt alta
 * geri dusus dali cihazda goz ile dogrulanir.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w360dp-h740dp-mdpi")
class SettingsSliderRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val surfaces = gameSurfaces(
        blastPalette(BlastSkin.DEFAULT, true),
        BlastSkin.DEFAULT.accentGradient
    )

    /** Koddaki `language.pick(...)` cagrisindan alindi, uydurulmadi. */
    private val longestVolumeLabels = listOf(
        "Ses Şiddeti",
        "Sound Volume",
        "Volume Suoni",
        "Volume Sonore",
        "Volumen de Sonido"
    )

    /**
     * Kaydiricinin altina inmesine izin verilen mutlak taban. `SLIDER_MIN_TRACK`
     * 76dp; buradaki 72dp yuvarlama payi birakiyor. Bu deger BILEREK sabit
     * yazildi: birisi asgari genisligi dusururse test kirilsin, yani karar
     * bilincli alinsin.
     */
    private val minUsableTrack = 72f

    @Test
    fun `volume row keeps a usable slider next to the longest translations`() {
        val labelState = mutableStateOf(longestVolumeLabels.first())
        composeRule.setContent {
            val label by labelState
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                SettingsSliderRow(
                    icon = Icons.Default.GraphicEq,
                    tint = surfaces.accentPrimary,
                    label = label,
                    value = 0.5f,
                    onValueChange = {},
                    enabled = true,
                    surfaces = surfaces,
                    sliderTestTag = "vol_slider"
                )
            }
        }
        longestVolumeLabels.forEach { label ->
            composeRule.runOnIdle { labelState.value = label }
            composeRule.waitForIdle()

            composeRule.onNodeWithTag("vol_slider").assertIsDisplayed()
            val bounds = composeRule.onNodeWithTag("vol_slider").getBoundsInRoot()
            val unclipped = composeRule.onNodeWithTag("vol_slider").getUnclippedBoundsInRoot()

            // ASIL KORUMA: etiket agirligi alirsa burasi 0dp'ye coker.
            assertTrue(
                "'$label' satirinda kaydirici kullanilamaz genislikte: ${bounds.width}",
                bounds.width.value >= minUsableTrack
            )
            // Dokunma hedefi 48dp'nin altina inmemeli.
            assertTrue(
                "'$label' satirinda kaydirici 48dp dokunma hedefinin altinda: ${bounds.height}",
                bounds.height.value >= 48f
            )
            // Kirpilma yok.
            assertTrue(
                "'$label' satirinda kaydirici kirpiliyor: ${bounds.width} < ${unclipped.width}",
                bounds.width.value >= unclipped.width.value - 0.5f
            )
        }
    }

    /**
     * Kullanicinin ikinci sikayetinin dogrudan karsiligi: deger ekranda BIR kez
     * yazmali. Degisiklikten once "50%" hem kapsulde hem kademe satirinin
     * ortasinda goruntuleniyordu.
     */
    @Test
    fun `volume row prints the percentage exactly once`() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                SettingsSliderRow(
                    icon = Icons.Default.GraphicEq,
                    tint = surfaces.accentPrimary,
                    label = "Ses Şiddeti",
                    value = 0.5f,
                    onValueChange = {},
                    enabled = true,
                    surfaces = surfaces,
                    sliderTestTag = "vol_slider"
                )
            }
        }
        assertEquals(
            "Deger ekranda birden fazla yerde yaziyor",
            1,
            composeRule.onAllNodesWithText("50%").fetchSemanticsNodes().size
        )
        // Kademe etiketleri tamamen kalkti.
        assertEquals(
            "0% kademe etiketi hala ciziliyor",
            0,
            composeRule.onAllNodesWithText("0%").fetchSemanticsNodes().size
        )
        assertEquals(
            "100% kademe etiketi hala ciziliyor",
            0,
            composeRule.onAllNodesWithText("100%").fetchSemanticsNodes().size
        )
    }

    /**
     * `showTicks` eklendi ama VARSAYILANI mevcut davranis olmali — `GameSlider`
     * baska bir yerde kullanilirsa kademe etiketleri kendiliginden kaybolmasin.
     */
    @Test
    fun `GameSlider still shows tick labels by default`() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                GameSlider(
                    value = 0.5f,
                    onValueChange = {},
                    surfaces = surfaces
                )
            }
        }
        assertEquals(
            "Varsayilan davranis degisti: 0% kademe etiketi kayboldu",
            1,
            composeRule.onAllNodesWithText("0%").fetchSemanticsNodes().size
        )
        assertEquals(
            "Varsayilan davranis degisti: 100% kademe etiketi kayboldu",
            1,
            composeRule.onAllNodesWithText("100%").fetchSemanticsNodes().size
        )
    }
}
