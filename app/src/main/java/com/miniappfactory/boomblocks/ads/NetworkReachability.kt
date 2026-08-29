package com.miniappfactory.boomblocks.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Faz 166: `ConnectivityGate`'in ihtiyaci olan tek olcum — "su anda gercek
 * internet erisimi var mi".
 *
 * NET_CAPABILITY_VALIDATED bilerek secildi: sadece "bir aga bagli" olmak yetmez,
 * o agin internete GERCEKTEN cikabildigi dogrulanmis olmali. Aksi halde captive
 * portal (otel/kafe girisi) veya internetsiz bir hotspot "cevrimici" sayilir ve
 * istismar kapisi acik kalir.
 *
 * `ACCESS_NETWORK_STATE` izni manifest'te ZATEN var, yeni izin gerekmiyor.
 */
object NetworkReachability {

    /** Anlik olcum. Callback henuz kurulmadan onceki ilk deger icin kullanilir. */
    fun hasValidatedInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

/**
 * Ag durumunu CANLI izler. Oyuncu oyun sirasinda ucak modunu acip kapatirsa
 * odullu reklam butonlari aninda kapanir/acilir — ekrandan cikip girmek
 * gerekmez.
 *
 * DisposableEffect ile kayit silindigi icin ekran birakildiginda callback
 * sizmaz (bkz. ayni denetimde bulunan MainActivity/Choreographer sizintilari).
 */
@Composable
fun rememberAdsReachable(): State<Boolean> {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val state = remember { mutableStateOf(NetworkReachability.hasValidatedInternet(appContext)) }

    DisposableEffect(appContext) {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            onDispose { }
        } else {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    state.value =
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }

                override fun onLost(network: Network) {
                    // Baska bir ag hala ayakta olabilir (Wi-Fi dusup mobil veri
                    // devralmasi gibi) — bu yuzden korukorune false yazmak yerine
                    // yeniden olculuyor.
                    state.value = NetworkReachability.hasValidatedInternet(appContext)
                }

                override fun onUnavailable() {
                    state.value = false
                }
            }
            // API 24+ (minSdk 24) — varsayilan agi izlemek icin ek NetworkRequest
            // kurmaya gerek yok.
            runCatching { cm.registerDefaultNetworkCallback(callback) }
            onDispose { runCatching { cm.unregisterNetworkCallback(callback) } }
        }
    }
    return state
}
