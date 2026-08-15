package com.miniappfactory.boomblocks.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform

// Faz 108: UMP rizasinin TEK kaynagi.
//
// Neden gerekti: consent durumu bu tarihe kadar yalnizca MainActivity'deki
// `adsConsentResolved` bayragindan ibaretti ve o bayrak SADECE banner'i
// kapiliyordu — interstitial ve rewarded cagrilari riza durumundan tamamen
// bagimsiz calisiyordu. Ustelik 4 saniyelik guvenlik agi bayragi kosulsuz
// true yapiyordu, yani `canRequestAds()` false olsa bile (sistemin "reklam
// isteme" dedigi tek durum) reklam yine aciliyordu. Google'in AB Kullanici
// Rizasi Politikasi acisindan bu bir ihlal; yaptirim AdMob hesap seviyesinde
// (ad serving limited) gelir.
//
// Cozum: `canRequestAds` tek yerde tutulur ve UC reklam turunun de yukleme
// oncesi kapisi olur. `adsConsentResolved` bayragi ise ARTIK yalnizca "UI'i
// serbest birak" anlamina gelir — reklam gosterme yetkisi degil.
object AdsConsent {

    // Varsayilan false: riza durumu okunana kadar hicbir reklam istenmez.
    // MainActivity acilista refresh() cagirir, bu birkac yuz milisaniye surer.
    @Volatile
    var canRequestAds: Boolean = false
        private set

    // UMP, form gerektiginde uygulamanin kullaniciya rizayi SONRADAN
    // degistirebilecegi kalici bir giris noktasi sunmasini zorunlu kilar.
    // Bu bayrak Ayarlar'daki "Gizlilik Seçenekleri" satirinin gorunurlugunu
    // belirler — gerekmedigi bolgelerde (AB disi) satir hic gosterilmez.
    @Volatile
    var privacyOptionsRequired: Boolean = false
        private set

    /** MainActivity'den, consent akisinin her sonucunda cagrilir. */
    fun refresh(context: Context) {
        val info = UserMessagingPlatform.getConsentInformation(context)
        canRequestAds = info.canRequestAds()
        privacyOptionsRequired =
            info.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Ayarlar'daki "Gizlilik Seçenekleri" satiri buraya baglanir.
     * Form kapandiktan sonra durum yeniden okunur — kullanici rizasini geri
     * cekmisse `canRequestAds` aninda false olur ve sonraki reklam istekleri
     * kapida durur.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            refresh(activity)
            onDismissed()
        }
    }
}
