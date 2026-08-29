package com.miniappfactory.boomblocks.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 164 — "TEKRAR DENE" reklaminin kaldirilmasi.
 *
 * Iki seyi birden kilitler:
 *  (a) Haklarini TUKETEN oyuncuya reklam GOSTERILMEZ (bildirilen sorun).
 *  (b) Hakki KALAN oyuncuya reklam HALA gosterilir (asiri duzeltme kontrolu).
 * Ikincisi olmadan test yesil yanar ama duzeltme gereginden fazlasini susturmus
 * olabilir; bu repoda daha once tam bu tur bir bosluk yasandi.
 */
class RetryAdPolicyTest {

    @Test
    fun `sonsuz modda dort hak da kullanilmissa reklam gosterilmez`() {
        assertFalse(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 4, maxContinues = 4))
    }

    @Test
    fun `kariyer ve pro modda uc hak da kullanilmissa reklam gosterilmez`() {
        assertFalse(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 3, maxContinues = 3))
    }

    @Test
    fun `hakki kalan oyuncuya reklam hala gosterilir`() {
        // ASIRI DUZELTME KONTROLU: devam teklifini reddedip oyunu bitiren
        // oyuncu haklarini tuketmedi; onun akisi degismemeli.
        assertTrue(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 0, maxContinues = 4))
        assertTrue(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 1, maxContinues = 4))
        assertTrue(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 3, maxContinues = 4))
    }

    @Test
    fun `hak sayisinin ustune cikan bir sayac da muafiyete girer`() {
        // Savunmaci: sayac herhangi bir sebeple max'i asarsa muafiyet yine gecerli.
        assertFalse(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 9, maxContinues = 4))
    }

    @Test
    fun `hak sayisi sifirsa muafiyet devreye girmez`() {
        // maxContinues = 0 anlamsiz bir yapilandirma; boyle bir durumda tum
        // reklamlari susturmak yanlis olur, normal davranis surer.
        assertTrue(RetryAdPolicy.shouldShowInterstitialOnRetry(continuesUsed = 0, maxContinues = 0))
    }
}
