package com.miniappfactory.boomblocks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Faz 166e — `FitToHeight`in IKI YONLU sozlesmesi.
 *
 * `fits` sadece bir bilgi degil, bir SOZ: `true` ise "kaydirma da chevron da
 * GEREKMIYOR" demek ve cagiran taraf ona guvenip kaydirmasiz varyanti ciziyor
 * (bkz. LoadoutScreen). Iki yonde de yanlis olabilir, IKISININ DE bedeli var:
 *
 *  1. `fits = true` deyip icerik sigmiyorsa -> icerik KIRPILIR ve oyuncunun
 *     kaydirma sansi da olmaz. Loadout'ta bu "REKLAM IZLE" kartinin
 *     ulasilamaz olmasi, yani kapali bir jeton kazanma yolu demek.
 *
 *  2. `fits = false` deyip aslinda sigiyorsa -> gereksiz yere kaydirma ve
 *     34dp'lik chevron seridi acilir; ekrana sigan icerik sigmaz hale gelir.
 *
 * Faz 166'da once (1) icin "olc, sigmiyorsa kaydirmaya dus" yazdim ve aninda
 * (2)'yi urettim. Kullanici hemen gordu: "burasi bozulmus, 3'u de gozukuyordu,
 * simdi reklam izle icin scroll gerekiyor". Dogru cevap kaydirmaya dusmek
 * degil, SIGAN EN BUYUK olcegi ARAMAKTI.
 *
 * ---------------------------------------------------------------------------
 * 🔴 BU HARNESS'IN SINIRI — okumadan test eklemeyin
 *
 * Hatanin KOK NEDENI metnin duzenden daha az kuculmesiydi (duzen `scale`,
 * metin `(1 + scale) / 2`). Bu asimetri Robolectric'te URETILEMIYOR: font
 * metrikleri sabit oldugu icin yogunlugu 0.85'e cekmek metin yuksekligini
 * degistirmiyor. Olculdu: 170px icerik, 0.85 olcekte 167px -- azalan tek sey
 * dp cinsinden ARALIKLAR oldu, satirlar hic kuculmedi.
 *
 * Bu yuzden buradaki testler metinle degil SABIT YUKSEKLIKLI kutularla
 * kuruldu ve KARAR MEKANIZMASINI kilitliyor: gereksiz yere kaydirmaya
 * dusmemek, gercekten imkansizsa dusmek. Metin asimetrisinin kendisi ancak
 * gercek cihazda gorulebilir.
 * ---------------------------------------------------------------------------
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FitToHeightTest {

    @get:Rule
    val rule = createComposeRule()

    private companion object {
        /** 4 x 40dp kutu + 3 x 10dp aralik. */
        const val NATURAL_DP = 190f
    }

    private fun scene(available: Dp, onFits: (Boolean) -> Unit): @Composable () -> Unit = {
        Column(Modifier.height(available).width(300.dp)) {
            FitToHeight(modifier = Modifier.fillMaxWidth()) { fits ->
                onFits(fits)
                val inner = @Composable {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(4) { Box(Modifier.fillMaxWidth().height(40.dp)) }
                    }
                }
                if (fits) inner() else Column(Modifier.verticalScroll(rememberScrollState())) { inner() }
            }
        }
    }

    /**
     * SOZ 2 — kullanicinin bildirdigi regresyon.
     *
     * Kucuk bir tasma icin dogru cevap kaydirma acmak degil, biraz kucultmek.
     * `minScale` (0.85) bunu fazlasiyla karsiliyor.
     */
    @Test
    fun `kucuk tasma kaydirma acmaz`() {
        var reported: Boolean? = null
        rule.setContent { scene((NATURAL_DP * 0.95f).dp) { reported = it }() }
        rule.waitForIdle()

        assertEquals(
            "kucuk tasma icin kaydirmaya dusuldu — chevron alttaki butonu orter",
            true, reported
        )
    }

    /**
     * Hic kucultme gerekmiyorsa da kaydirma acilmamali.
     */
    @Test
    fun `rahatca sigan icerik icin kaydirma acilmaz`() {
        var reported: Boolean? = null
        rule.setContent { scene((NATURAL_DP * 1.5f).dp) { reported = it }() }
        rule.waitForIdle()

        assertEquals("sigan icerik icin kaydirmaya dusuldu", true, reported)
    }

    /**
     * SOZ 1 — alt sinirda BILE sigmiyorsa kaydirma ACILMALI. Ozellik
     * sigdiramadiginda yalan soylememeli; eski davranisa donmeli.
     */
    @Test
    fun `alt sinirda bile sigmiyorsa kaydirmaya dusulur`() {
        var reported: Boolean? = null
        rule.setContent { scene((NATURAL_DP * 0.5f).dp) { reported = it }() }
        rule.waitForIdle()

        assertEquals("sigmadigi halde fits=true dendi", false, reported)
    }
}
