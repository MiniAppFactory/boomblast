package com.miniappfactory.boomblocks.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 166. Kullanicinin buldugu istismari kilitleyen testler:
 *
 *   "mobil veriyi kapatip o tusa basarsa hic reklam izlemeden gecer"
 *
 * Kritik ayrim -- ikisi de "reklam gosterilemedi" ama SONUCLARI farkli olmali:
 *   ag VAR + reklam gelmedi -> gercek no-fill, oyuncu cezalandirilmaz (Faz 95c/98)
 *   ag YOK                  -> teklif hic sunulmaz
 */
class ConnectivityGateTest {

    @Test
    fun `ag ve riza varken odullu reklam sunulur`() {
        assertTrue(ConnectivityGate.canOfferRewardedAd(hasNetwork = true, consentGiven = true))
    }

    @Test
    fun `ag yokken odullu reklam SUNULMAZ`() {
        assertFalse(ConnectivityGate.canOfferRewardedAd(hasNetwork = false, consentGiven = true))
    }

    @Test
    fun `riza yokken odullu reklam sunulmaz`() {
        assertFalse(ConnectivityGate.canOfferRewardedAd(hasNetwork = true, consentGiven = false))
    }

    @Test
    fun `ag varken yukleme denenir`() {
        assertTrue(
            ConnectivityGate.shouldAttemptLoad(
                hasNetwork = true, consentGiven = true, loadInFlight = false
            )
        )
    }

    /**
     * AdMob politika riski: ayni anda ikinci bir yukleme, arka arkaya iki tam
     * ekran reklam demek. Denetim bunu 9 cagri noktasi icinde korumasiz tek yer
     * olan `watchAdForBooster`da buldu; kapi artik RewardedAdManager'da, yani
     * dokuzunu birden kapsiyor.
     */
    @Test
    fun `yukleme ucusdayken ikinci yukleme baslatilmaz`() {
        assertFalse(
            ConnectivityGate.shouldAttemptLoad(
                hasNetwork = true, consentGiven = true, loadInFlight = true
            )
        )
    }

    @Test
    fun `ag yoksa in-flight olmasa bile yukleme denenmez`() {
        assertFalse(
            ConnectivityGate.shouldAttemptLoad(
                hasNetwork = false, consentGiven = true, loadInFlight = false
            )
        )
    }

    /**
     * ASIRI DUZELTME KONTROLU. Faz 95c/98'deki comertlik bilincli bir karardi:
     * gercek bir no-fill oyuncunun hatasi degil. Bu kapi SADECE "ag yok" halini
     * ayirmali; ag varken karar her zaman "sun" olmali ki o comertlik korunsun.
     */
    @Test
    fun `ag varken karar reklamin dolup dolmamasindan bagimsizdir`() {
        assertTrue(ConnectivityGate.canOfferRewardedAd(hasNetwork = true, consentGiven = true))
    }
}
