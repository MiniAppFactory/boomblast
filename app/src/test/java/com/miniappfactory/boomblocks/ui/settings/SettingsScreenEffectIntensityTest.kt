package com.miniappfactory.boomblocks.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.EffectIntensity
import com.miniappfactory.boomblocks.data.label
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Faz 109 — projedeki ILK cihazsiz Compose UI testi.
 *
 * Bugune kadar `app/src/androidTest/` yoktu, yani hicbir ekran otomatik olarak
 * dogrulanmiyordu. Robolectric ile ayni testler `./gradlew testDebugUnitTest`
 * icinde, emulator/cihaz olmadan calisiyor.
 *
 * GraphicsMode.NATIVE sart: varsayilan LEGACY modda Robolectric metin olcumunu
 * taklit eder (her karakter ~1px), o yuzden "uzun ceviri sigiyor mu" sorusu
 * anlamsiz sekilde HEP "evet" cikardi. NATIVE modda gercek metin duzeni
 * (TextLayoutResult) hesaplandigi icin tasma kontrolu gercekten bir sey kanitliyor.
 *
 * Ekran boyutu bilerek dar (320dp) seciliyor — desteklenen en dar yaygin
 * telefon genisligi; genis ekranda sigan metin darda sigmayabiliyor.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w320dp-h640dp")
class SettingsScreenEffectIntensityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setSettingsContent(
        initialLanguage: AppLanguage = AppLanguage.TR,
        initialIntensity: EffectIntensity = EffectIntensity.NORMAL,
        onSelectEffectIntensity: (EffectIntensity) -> Unit = {}
    ): (AppLanguage) -> Unit {
        var language by mutableStateOf(initialLanguage)
        composeRule.setContent {
            SettingsScreen(
                soundEnabled = true,
                musicEnabled = true,
                darkMode = true,
                language = language,
                effectIntensity = initialIntensity,
                onToggleSound = {},
                onToggleMusic = {},
                onSelectEffectIntensity = onSelectEffectIntensity,
                onToggleDarkMode = {},
                onSelectLanguage = {},
                onBack = {}
            )
        }
        return { newLanguage -> composeRule.runOnUiThread { language = newLanguage } }
    }

    private fun tagOf(option: EffectIntensity) = "settings_effect_intensity_${option.code}"

    @Test
    fun allThreeIntensityOptionsAreVisible() {
        setSettingsContent()

        EffectIntensity.entries.forEach { option ->
            composeRule.onNodeWithTag(tagOf(option)).assertIsDisplayed()
        }
    }

    /** Varsayilan NORMAL, ve secim durumu gercekten tek secenekte. */
    @Test
    fun currentIntensityIsMarkedSelected() {
        setSettingsContent(initialIntensity = EffectIntensity.NORMAL)

        composeRule.onNodeWithTag(tagOf(EffectIntensity.NORMAL)).assertIsSelected()
        composeRule.onNodeWithTag(tagOf(EffectIntensity.LOW)).assertIsNotSelected()
        composeRule.onNodeWithTag(tagOf(EffectIntensity.HIGH)).assertIsNotSelected()
    }

    @Test
    fun tappingHighInvokesCallbackWithHigh() {
        val selections = mutableListOf<EffectIntensity>()
        setSettingsContent(
            initialIntensity = EffectIntensity.NORMAL,
            onSelectEffectIntensity = { selections += it }
        )

        composeRule.onNodeWithTag(tagOf(EffectIntensity.HIGH)).performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(EffectIntensity.HIGH), selections)
    }

    @Test
    fun tappingLowInvokesCallbackWithLow() {
        val selections = mutableListOf<EffectIntensity>()
        setSettingsContent(
            initialIntensity = EffectIntensity.NORMAL,
            onSelectEffectIntensity = { selections += it }
        )

        composeRule.onNodeWithTag(tagOf(EffectIntensity.LOW)).performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(EffectIntensity.LOW), selections)
    }

    /**
     * Projede tekrar eden hata: uzun TR/IT/FR ceviriler sabit olculu kontrollere
     * sigmiyor ve SESSIZCE kirpiliyor. Bu test 5 dil x 3 secenegin tamamini
     * 320dp genislikte gercekten OLCUP kirpilmadigini dogruluyor.
     *
     * NOT: `TextLayoutResult.hasVisualOverflow` burada kullanilamiyor — Compose
     * 1.7.2'de bu bayrak, metin kutusu kendisine verilen genislikten DAR
     * olculdugunde de true donuyor (olculen 15 etiketin tamami, 69px'lik alanda
     * 23px yer kaplayan "Alto" dahil, true veriyordu). O yuzden dogrudan olcum
     * karsilastiriliyor: metnin fiili genisligi <= kendisine verilen genislik
     * ve satir sayisi maxLines sinirinda.
     */
    @Test
    fun intensityLabelsFitWithoutClippingInAllLanguages() {
        assertLabelsFit(maxLines = 1)
    }

    /**
     * Erisilebilirlik: sistem yazi tipi olcegi buyutuldugunde de kirpilma olmamali.
     * Segment yuksekligi sabit DEGIL (defaultMinSize + maxLines=2), yani metin
     * gerekirse ikinci satira tasiyor ve kart uzuyor — kirpilmiyor.
     */
    @Test
    @Config(qualifiers = "w320dp-h640dp", fontScale = 1.3f)
    fun intensityLabelsFitWithLargeSystemFont() {
        assertLabelsFit(maxLines = 2)
    }

    private fun assertLabelsFit(maxLines: Int) {
        val switchLanguage = setSettingsContent()

        AppLanguage.entries.forEach { language ->
            switchLanguage(language)
            composeRule.waitForIdle()

            EffectIntensity.entries.forEach { option ->
                val expected = option.label(language)
                val textNode = composeRule.onNode(
                    hasText(expected) and hasAnyAncestor(hasTestTag(tagOf(option))),
                    useUnmergedTree = true
                )
                textNode.assertExists(
                    "${language.code}/${option.code}: \"$expected\" etiketi bulunamadi"
                )

                val layouts = mutableListOf<TextLayoutResult>()
                textNode.fetchSemanticsNode()
                    .config[SemanticsActions.GetTextLayoutResult]
                    .action
                    ?.invoke(layouts)

                val layout = layouts.firstOrNull()
                    ?: error("${language.code}/${option.code}: TextLayoutResult alinamadi")

                val available = layout.layoutInput.constraints.maxWidth
                assertTrue(
                    "${language.code}/${option.code}: \"$expected\" segmente sigmiyor — " +
                        "metin ${layout.size.width}px, kullanilabilir ${available}px",
                    layout.size.width <= available
                )
                assertTrue(
                    "${language.code}/${option.code}: \"$expected\" ${layout.lineCount} satira " +
                        "bolundu (izin verilen en fazla $maxLines) — 320dp'de segment dar kaliyor",
                    layout.lineCount <= maxLines
                )
            }
        }
    }
}
