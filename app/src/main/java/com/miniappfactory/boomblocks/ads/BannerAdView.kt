package com.miniappfactory.boomblocks.ads

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.delay

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
    //
    // Faz 109: bu rezervasyon simdi ikinci bir ise daha yariyor — banner'i
    // gecikmeli baglayabilmemizi ucretsiz kiliyor (bkz. asagidaki
    // LaunchedEffect). Alan zaten dogru boyutta oldugu icin 250 ms sonra
    // icine reklam oturmasi hicbir kayma yaratmaz.
    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    // Faz 109: `AndroidView`'in `factory`'si artik SADECE bos bir FrameLayout
    // uretiyor.
    //
    // Onceden burada `AdView(context)` kurulup `loadAd()` cagriliyordu.
    // `factory` kompozisyon sirasinda, UI thread'inde, KARENIN ICINDE calisir
    // — yani her ekran girisi bir AdView kurulumunu (ilkinde ustelik tum
    // WebView saglayicisinin yuklenmesini) o kareye yikiyordu. Olculdu:
    // soguk aciliste 200 ms'lik tek kare, GPU tarafi 5 ms; bir oturum sonunda
    // surecte 7 canli WebView.
    //
    // Artik AdView surec omurlu tek bir ornek (BannerAdPool) ve ekranlar
    // arasinda yalnizca ebeveyn degistiriyor: yeni WebView yok, yeni
    // `loadAd()` yok, kare icinde is yok.
    val container = remember { FrameLayout(context) }

    LaunchedEffect(container, adSize) {
        // Kullanicinin acik onceligi: "gerekirse oyun acilirken ceyrek saniye
        // gec gelsin ama gelince takilma hissi olmamali." Once ekran (ve oyun
        // izgarasinin ilk kompozisyonu) rahatca yerlessin, reklam sonra otursun.
        delay(BannerAdPool.ATTACH_DELAY_MS)
        BannerAdPool.attach(container, context, adSize)
    }

    DisposableEffect(container) {
        onDispose {
            // AdView yok EDILMEZ, sadece birakilir — bir sonraki ekran onu
            // dolu haliyle devralir. Gercek destroy() Activity onDestroy'da
            // (bkz. MainActivity) ve olculen WebView sizintisini orasi kapatir.
            BannerAdPool.release(container)
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
        factory = { container }
    )
}
