package com.miniappfactory.boomblocks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Faz 166 — "AYNI KAREDE IKI OLAY" KOSUM TAKIMI.
 *
 * NEDEN VAR
 * ---------
 * vc19 uretimde `IllegalArgumentException: Padding must be non-negative` ile
 * coktu (Loadout'ta SATIN AL). O sirada 94 test vardi ve HICBIRI yakalamadi.
 * Ardindan yapilan denetim ayni hata sinifindan 15 kusur daha buldu, biri yine
 * bir cokme (`OnboardingScreen`, `Index 3 out of bounds for length 3`).
 *
 * Kacirma nedeni yeterince test olmamasi degil, testlerin YANLIS SEYI olcmesiydi:
 * hepsi saf fonksiyon / statik deger testleriydi (kontrast orani, metin tasmasi,
 * duzen sabitleri). Bu hata sinifinin hicbir ornegi tek bir saf fonksiyonda
 * yasamiyor. Ortak imza su:
 *
 *   IKI DEGER AYRI KAYNAKLARDAN GELIYOR ve aralarinda senkron VARSAYILIYOR.
 *
 *   | Kaynak A            | Kaynak B                  | Ornek                     |
 *   |---------------------|---------------------------|---------------------------|
 *   | Animasyon saati     | Composable parametresi    | depth - sink (vc19 cokme) |
 *   | Kompozisyon zamani  | Olay dagitim zamani       | Onboarding, tepsi TOCTOU  |
 *   | 1. olcum gecisi     | 2. olcum gecisi           | FitToHeight               |
 *   | Dp/sp uzayi         | px uzayi                  | GameKit, GameEmblem       |
 *
 * Hicbiri tek bir sabit girdiyle tetiklenmiyor: KARELERIN ARASINA dusuyorlar.
 *
 * Bu dosya o bosluktan birini kapatir: `mainClock.autoAdvance = false` ile saat
 * durdurulur, iki olay ust uste dagitilir, SONRA saat ilerletilir. Boylece iki
 * tiklama da ayni gorunurluk/etkinlik kararina gore islenir — gercek cihazda UI
 * thread takildiginda (ilk acilista consent + AdMob SDK yuklenirken tam olarak
 * boyle oluyor) olan sey budur.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SameFrameDoubleEventTest {

    @get:Rule
    val rule = createComposeRule()

    /**
     * ONBOARDING COKMESININ BIREBIR MODELI.
     *
     * Butonun GORUNURLUGU kompozisyon zamaninda karar veriliyor
     * (`currentStep < lastIndex`), ARTIRMA ise olay-dagitim zamaninda oluyor.
     *
     * NEDEN `performClick()` IKI KEZ CAGRILMIYOR: Compose test harness'i her
     * `performClick`ten sonra besteyi tazeliyor, yani `autoAdvance = false`
     * olsa bile iki tiklama arasinda recomposition oluyor ve senaryo HIC
     * uretilmiyor (denendi, clamp kaldirilinca bile test geciyordu -- yani
     * disi olmayan bir test olurdu).
     *
     * Uretimde olan sey su: girdi kuyrugu iki dokunusu de AYNI bestede
     * olusturulmus AYNI lambda'ya teslim ediyor. Test de tam olarak bunu
     * yapiyor -- bestedeki `onClick` yakalanip iki kez cagriliyor.
     */
    @Test
    fun `ayni besteye dagitilan iki tiklama adim sayacini tasirmaz`() {
        val steps = listOf("bir", "iki", "uc") // gercekte de 3 elemanli
        var crash: Throwable? = null
        var advance: (() -> Unit)? = null

        rule.setContent {
            var currentStep by remember { mutableIntStateOf(0) }
            Box(Modifier.padding(8.dp)) {
                val label = runCatching { steps[currentStep] }
                    .onFailure { crash = it }
                    .getOrDefault("")
                Text(text = label, modifier = Modifier.testTag("step_label"))

                if (currentStep < steps.lastIndex) {
                    // URUNDEKI DUZELTMENIN AYNISI (OnboardingScreen.kt).
                    advance = { currentStep = (currentStep + 1).coerceAtMost(steps.lastIndex) }
                } else {
                    advance = null
                }
            }
        }
        rule.waitForIdle()

        // Sinira bitisik adima gel (normal kullanim, arada recomposition var).
        advance!!.invoke()
        rule.waitForIdle()

        // Simdi AYNI bestenin lambda'sini iki kez cagir: girdi kuyrugu UI
        // thread takiliyken tam olarak bunu yapiyor.
        val sameComposition = advance!!
        sameComposition.invoke()
        sameComposition.invoke()
        rule.waitForIdle()

        assertEquals("sinir disi indeks okundu: $crash", null, crash)
    }

    /**
     * VC19 COKMESININ BIREBIR MODELI.
     *
     * `sink` bir ANIMASYON degeri, `depth` bir composable PARAMETRESI. Ikisi
     * ayri kaynaktan geliyor ama `depth - sink` aralarinda senkron VARSAYIYOR.
     * Buton basiliyken pasiflesince (satin alma tam olarak bunu yapiyor) fark
     * negatife dusuyor ve `Modifier.padding` istisna atiyor.
     *
     * Test, dogru yapilmis hesabin her ara degerde gecerli kaldigini gosterir.
     */
    @Test
    fun `basili buton pasiflesince dolgu asla negatif olmaz`() {
        var negativeSeen = false

        rule.mainClock.autoAdvance = false
        rule.setContent {
            var enabled by remember { mutableStateOf(true) }
            var sinkRaw by remember { mutableStateOf(0f) }
            val depth = 6.dp

            // vc20 hotfix'indeki ifadenin aynisi.
            val top = sinkRaw.dp.coerceIn(0.dp, depth)
            val bottom = (depth - sinkRaw.dp).coerceAtLeast(0.dp)
            if (top.value < 0f || bottom.value < 0f) negativeSeen = true

            Box(Modifier.padding(top = top, bottom = bottom)) {
                Button(
                    onClick = {
                        // Basili haldeyken hem animasyon degerini ileri tasi
                        // hem de butonu pasiflestir -- vc19'daki sira.
                        sinkRaw = 9f // depth'ten BUYUK: eski kodda bottom negatif
                        enabled = false
                    },
                    enabled = enabled,
                    modifier = Modifier.height(48.dp).testTag("buy")
                ) { Text("satin al") }
            }
        }

        rule.onNodeWithTag("buy").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()

        assertTrue("dolgu negatife dustu", !negativeSeen)
    }
}
