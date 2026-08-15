package com.miniappfactory.boomblocks.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Sadece menu/level-select gibi oyun-disi ekranlarda kullanilir (bkz. plan
// "Banner ads only in places where they do not damage gameplay or controls").
@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adWidthDp = configuration.screenWidthDp

    // Faz 108: reklam alani ONCEDEN rezerve ediliyor.
    //
    // Sorun: AndroidView'e hicbir yukseklik verilmiyordu. Reklam gelene kadar
    // AdView 0dp kaliyor, fill gelince bir anda ~50-60dp'ye siciyor ve
    // ustundeki HER SEYI (oynanis ekranlarinda surukleme tepsisi dahil)
    // yukari itiyordu. Parmagin altinda olusan bu duzen kaymasi, kazara
    // reklam tiklamasinin en bilinen uretim yoludur.
    //
    // Bu projede ozellikle kritik: olculmus 36x NO_FILL nedeniyle bircok
    // oturumda banner hic dolmuyor (0dp), sonra gecikmeli bir fill tam
    // oyuncu parca suruklerken her seyi kaydiriyor.
    //
    // Cozum: adaptive banner yuksekligi kadar alan bastan ayrilir; reklam
    // gelse de gelmese de duzen HIC oynamaz.
    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        factory = {
            AdView(context).apply {
                setAdSize(adSize)
                adUnitId = AdIds.bannerAdUnitId()
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
