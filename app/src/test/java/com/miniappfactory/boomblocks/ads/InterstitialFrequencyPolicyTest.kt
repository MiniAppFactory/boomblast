package com.miniappfactory.boomblocks.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 114. Bu testlerin amaci sadece "kod calisiyor mu" degil — TODO madde 1'de
 * tarif edilen somut kotu senaryonun (Pro Mod'da ust uste kaybederken hizli-ates
 * reklam) bir daha sessizce geri gelmemesini kilitlemek.
 */
class InterstitialFrequencyPolicyTest {

    private val grace = InterstitialFrequencyPolicy.SESSION_GRACE_MS
    private val interval = InterstitialFrequencyPolicy.MIN_INTERVAL_MS

    // Oturum basi = 0 kabul edilen sadelestirilmis eksen.
    private fun allow(now: Long, lastShown: Long = 0L, sessionStart: Long = 0L) =
        InterstitialFrequencyPolicy.allow(now, lastShown, sessionStart)

    @Test
    fun `uygulama acilir acilmaz reklam gosterilmez`() {
        assertFalse(allow(now = 0L))
        assertFalse(allow(now = grace - 1))
    }

    @Test
    fun `grace bitince ilk reklam serbest`() {
        assertTrue(allow(now = grace))
        assertTrue(allow(now = grace + 10_000))
    }

    @Test
    fun `grace penceresi oturum basina goredir, mutlak zamana degil`() {
        // Surec 5 dakikadir ayakta degil — oturum saati ileri bir noktada
        // baslamis olabilir. Pencere yine oturum basindan olculmeli.
        val sessionStart = 300_000L
        assertFalse(allow(now = sessionStart + grace - 1, sessionStart = sessionStart))
        assertTrue(allow(now = sessionStart + grace, sessionStart = sessionStart))
    }

    @Test
    fun `cooldown dolmadan ikinci reklam engellenir`() {
        val shownAt = 100_000L
        assertFalse(allow(now = shownAt + 1, lastShown = shownAt))
        assertFalse(allow(now = shownAt + interval - 1, lastShown = shownAt))
    }

    @Test
    fun `cooldown dolunca reklam yeniden serbest`() {
        val shownAt = 100_000L
        assertTrue(allow(now = shownAt + interval, lastShown = shownAt))
        assertTrue(allow(now = shownAt + interval * 2, lastShown = shownAt))
    }

    @Test
    fun `hic gosterilmemis reklam cooldown saymaz`() {
        // lastShown = 0 sentinel'i "epoch'ta gosterildi" gibi yorumlanmamali.
        assertTrue(allow(now = grace, lastShown = 0L))
    }

    @Test
    fun `TODO madde 1 senaryosu — Pro Modda ust uste kaybetme hizli ates edemez`() {
        // Oyuncu grace'i gecti ve bir reklam gordu (t = 60sn).
        val firstAd = 60_000L
        assertTrue(allow(now = firstAd, lastShown = 0L))

        // Kaybet -> YENIDEN BASLA -> 20 sn sonra tekrar kaybet -> tekrar deneme.
        // Sikayete konu olan tam bu: eskiden burada IKINCI tam ekran reklam
        // aciliyordu.
        assertFalse(allow(now = firstAd + 20_000, lastShown = firstAd))
        assertFalse(allow(now = firstAd + 40_000, lastShown = firstAd))
        assertFalse(allow(now = firstAd + 59_000, lastShown = firstAd))

        // Ancak gercek bir dakika gectiginde yeni gosterim hak edilir.
        assertTrue(allow(now = firstAd + interval, lastShown = firstAd))
    }

    @Test
    fun `grace ve cooldown kosullarinin ikisi birden gecerlidir`() {
        // Cooldown dolmus ama hala grace icindeyiz (uygulama yeni acildi,
        // lastShown onceki oturumdan tasinmis gibi bir durum) -> yine hayir.
        assertFalse(allow(now = 1_000L, lastShown = 1_000L - interval, sessionStart = 0L))
    }

    @Test
    fun `sinir degerleri kapali-acik araliktir`() {
        val shownAt = grace
        // Tam esikte serbest (>= interval), bir ms oncesinde degil.
        assertFalse(allow(now = shownAt + interval - 1, lastShown = shownAt))
        assertTrue(allow(now = shownAt + interval, lastShown = shownAt))
    }

    @Test
    fun `sabitler makul araliklarda`() {
        // Yanlislikla 0 veya sacma bir degere cekilirse koruma sessizce olur.
        assertTrue(interval >= 30_000L)
        assertTrue(grace >= 15_000L)
    }
}
