package com.miniappfactory.boomblocks.ui.consent

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.miniappfactory.boomblocks.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miniappfactory.boomblocks.data.AppLanguage
import com.miniappfactory.boomblocks.data.pick
import com.miniappfactory.boomblocks.ui.common.WanderingPiecesBackground
import com.miniappfactory.boomblocks.ui.components.GameScreenBackground
import com.miniappfactory.boomblocks.ui.components.gameOuterGlow
import com.miniappfactory.boomblocks.ui.components.GameWordmark
import com.miniappfactory.boomblocks.ui.components.GameButton
import com.miniappfactory.boomblocks.ui.components.resultButtonColors
import com.miniappfactory.boomblocks.ui.components.GameButtonColors
import com.miniappfactory.boomblocks.ui.components.ResultSuccessAccent
import com.miniappfactory.boomblocks.ui.components.ResultOnSuccess
import com.miniappfactory.boomblocks.ui.theme.BlastSkin
import com.miniappfactory.boomblocks.ui.theme.NeonCyan
import com.miniappfactory.boomblocks.ui.theme.NeonGreen
import com.miniappfactory.boomblocks.ui.theme.blastPalette
import com.miniappfactory.boomblocks.ui.theme.rememberGameSurfaces

// Faz 63: GitHub hesabi whatsthisapp -> MiniAppFactory olarak yeniden
// adlandirildi, repo da blasttheblocks -> boomblast oldu (kullanici istegi).
private const val PRIVACY_POLICY_URL = "https://miniappfactory.github.io/boomblast/"

// Ilk acilista, onboarding tutorial'dan ONCE, atlanamaz bir kabul ekrani —
// Play Store'a gonderilecek her uygulamada beklenen standart bir uygulama
// (bkz. referans: "Block Blast!"in kendi "Accept Terms of Use and Privacy Policy" ekrani).
@Composable
fun TermsAcceptScreen(
    language: AppLanguage,
    darkMode: Boolean,
    skin: BlastSkin = BlastSkin.DEFAULT,
    onAccept: () -> Unit
) {
    val palette = blastPalette(skin, darkMode)
    val surfaces = rememberGameSurfaces(skin, darkMode)
    val context = LocalContext.current

    fun openPolicy() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Faz 159 — CIHAZDA GORULEN: bu ekran uygulamanin ILK ekrani, ama
        // Faz 158'in zemin yenilemesini hic almamisti: duz `palette.background`
        // uzerinde duruyordu ve diger tum menulerden KOPUK, yikanmis
        // gorunuyordu. Artik Ayarlar/Gorevler/Mod Secim ile AYNI zemini
        // kullaniyor (derin gradyan + kosedeki 3B kupler + vinyet).
        GameScreenBackground(
            skin = skin,
            darkMode = darkMode,
            modifier = Modifier.matchParentSize()
        )
        // ModeSelectScreen.kt'deki (Faz 115h -> 124) v11 "gezinen oyun parcasi"
        // deseninin AYNISI — kullanici bu ekranda da "farklı parçalar olsun,
        // hareket olabiliyorsa çok daha iyi" dedi (Faz 124). Ortak composable,
        // bkz. `ui/common/WanderingPiecesBackground.kt`.
        WanderingPiecesBackground(modifier = Modifier.matchParentSize())

        Card(
            // FAZ 162: ham `palette.card` yerine `surfaces.panel`.
            // Onceki hal doygunluk katmanini ATLIYORDU — zemin ve diger
            // ekranlar derin maviye cekilirken bu kartin govdesi GRI kaliyor,
            // yan yana gorulunce tutarsiz duruyordu.
            colors = CardDefaults.cardColors(containerColor = surfaces.panel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                // Kenar boslugu artik KARTIN uzerinde. Onceden dis Box'taydi,
                // ama zemin (GameScreenBackground) tam kenara dayanmali —
                // kose kupleri aksi halde 24dp iceriden baslardi.
                .padding(24.dp)
                .fillMaxWidth()
                // FAZ 162: onboarding ve dil secim kartlariyla AYNI katmanli
                // hale. Bu kart o gecisten disarida kalmisti (dosya baska bir
                // elde duzenleniyordu) ve yan yana gorulunce tek, duz, ince
                // cizgisiyle tutarsiz kaliyordu — oysa oyuncunun gordugu ILK
                // ekran burasi.
                //
                // `Modifier.blur` KULLANILAMAZ (API 31+, minSdk 24); ayni
                // paylasilan `gameOuterGlow` katmanli cozumu kullaniliyor.
                .gameOuterGlow(
                    accent = NeonCyan,
                    cornerRadius = 20.dp,
                    intensity = 1f,
                    layers = 14,
                    spreadStepDp = 1.3f,
                    coreAlpha = 0.52f
                )
                .border(
                    2.dp,
                    Brush.verticalGradient(
                        listOf(
                            lerp(NeonCyan, Color.White, 0.55f),
                            NeonCyan,
                            lerp(NeonCyan, Color.White, 0.20f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .testTag("terms_accept_card")
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Faz 102: burasi uygulamanin ILK acilista gordugu ilk ekran
                // ("Kaboom Blocks'a Hos Geldin!", dil seciminden de once).
                // Onceden basligin ustunde duz bir "🧩💥" emoji cifti vardi —
                // marka tasimiyordu ve emoji render'i cihazdan cihaza degisiyordu.
                // Artik uygulamanin kendi ikonu: kullanicinin Play sayfasinda ve
                // ana ekranda gordugu gorselle birebir ayni, yani ilk karsilasma
                // aninda "dogru uygulamayi actim" tanınırlığı kuruluyor.
                // Onboarding'deki dil secme ekraninda da ayni desen kullanildi,
                // orada 84dp; burasi asil karsilama oldugu icin biraz daha buyuk.
                // Faz 161 — kullanicinin istegi: "burada da logoyu kullanabilirsin
                // yazi ve appicon yerine."
                //
                // Onceki hal IKI ogeydi: yuvarlatilmis kutu icinde uygulama
                // ikonu (`logo_kaboom`) + altinda metinle cizilen wordmark
                // (`GameWordmark`). Yeni `kb_logo` varligi patlayan "B" blogunu
                // ZATEN iceriyor, yani ikon + wordmark tek gorsel. Ikisini birden
                // koymak ayni seyi iki kez gostermekti.
                //
                // Ana menu de ayni varligi kullaniyor — marka ilk ekranda ve
                // menude birebir ayni goruniyor, ikisi asla ayrisamaz.
                Image(
                    painter = painterResource(R.drawable.kb_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        // Not: bir ara 0.56'ya kucultulmustu ("K harfi kirpik"
                        // sanilmisti); olcum harfin TAM oldugunu gosterdi, kirpik
                        // olan yalnizca kenardaki hale payiydi. Kullanici geri
                        // buyutulmesini istedi.
                        .fillMaxWidth(0.72f)
                        .aspectRatio(535f / 380f)   // varligin kendi orani
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Karsilama sozcugu AYRI satirda ve sade: wordmark'la
                // yarismasin diye kalin/konturlu degil.
                Text(
                    text = language.pick(
                        tr = "Hoş Geldin",
                        en = "Welcome",
                        it = "Benvenuto",
                        fr = "Bienvenue",
                        es = "Bienvenido"
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    // Faz 159 — metin kisaltildi (uc satirdan iki satira).
                    //
                    // HUKUKI ICERIK, dikkatle kirpildi. Play politikasi geregi
                    // uc unsur KALDI:
                    //   (a) Gizlilik Politikasi ve Kullanim Sartlari'na ACIK
                    //       atif — asagidaki cumlede adlariyla geciyor,
                    //   (b) calisan BAGLANTI — hemen altindaki tiklanabilir
                    //       satir (openPolicy) degismedi,
                    //   (c) acik KABUL eylemi — "KABUL ET" butonu degismedi.
                    //
                    // Atilan sey yalnizca doldurma ifadeler: "lutfen",
                    // "devam etmeden once ... oku ve". Bes dilde birden
                    // kisaltildi; biri uzun kalsaydi o dilde yine uc satir
                    // olurdu.
                    text = language.pick(
                        tr = "Devam etmek için Gizlilik Politikası ve Kullanım Şartları'nı kabul et.",
                        en = "To continue, accept our Privacy Policy and Terms of Use.",
                        it = "Per continuare, accetta l'Informativa sulla Privacy e i Termini di Utilizzo.",
                        // Faz 160 — FR CIHAZDA OLCULDU: METIN UC SATIR, IKI DEGIL.
                        //
                        // Faz 159 bu metni 121 -> 89 karaktere indirip "iki
                        // satira indi" DEDI ama DOGRULAMADI: bu ekran dil
                        // seciminden ONCE render ediliyor, yani uygulama
                        // icinden Fransizcaya gecip bakmak mumkun degil.
                        // Faz 160'ta DataStore'a language=fr tohumlanarak
                        // cihazda BAKILDI (SM-G950F, 1080x2220): metin UC
                        // SATIR. Iddia yanlisti.
                        //
                        // METIN DAHA FAZLA KISALTILMADI — bilincli karar:
                        // 3 satir bir TASMA DEGIL. Kart rahat siğiyor, hicbir
                        // sey kirpilmiyor, ACCEPTER butonu tam gorunur.
                        // "Iki satir" kozmetik bir hedefti, kusur degil.
                        //
                        // Iki satira inmenin bedeli olculdu: cihazda satira
                        // ~34 karakter siğiyor, iki belge adi tek basina 55
                        // karakter ("Confidentialité" tek basina 15'lik
                        // bolunmez bir token). Iki satir ancak su uc yoldan
                        // biriyle olurdu: (a) bir belge adini atmak,
                        // (b) "CGU" kisaltmasina dusmek, (c) puntoyu 12sp'ye
                        // indirmek. Ucu de HUKUKI ACIKLIGI ya da OKUNURLUGU
                        // kozmetik bir satir icin takas ederdi — yapilmadi.
                        // "Pour continuer" de korundu: kullaniciya NEDEN
                        // kabul ettigini soyleyen kisim o.
                        fr = "Pour continuer, acceptez la Politique de Confidentialité et les Conditions d'Utilisation.",
                        es = "Para continuar, acepta la Política de Privacidad y los Términos de Uso."
                    ),
                    fontSize = 14.sp,
                    // `textSecondary` koyu temada duz `Color.Gray` -- doygunlugu
                    // sifir ve kontrasti dusuk. Zemin lacivert oldugu icin
                    // hafif camgobegine cekilmis acik bir ton hem okunur hem
                    // ekranin geri kalaniyla ayni aileden.
                    color = lerp(palette.textPrimary, NeonCyan, 0.25f).copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    // Faz 160: FR govde metninin GERCEKTEN iki satira indigi
                    // cihazda gorulemiyordu — bu ekran dil seciminden ONCE
                    // render ediliyor, yani uygulama icinden Fransizcaya
                    // gecip bakmak mumkun degil. Bu etiket sayesinde
                    // `TermsConsentOverflowTest` satir sayisini 5 dilde de
                    // OLCUYOR; "sigdi" iddiasi artik gozle degil sayiyla.
                    modifier = Modifier.testTag("terms_body")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // FAZ 185 — LANDING EKRANLARI OYUNUN DILINE GECTI.
                //
                // Kullanici: "su landing pageler hala cok pastel tonlu."
                // OLCULDU, kok neden renk secimiydi:
                //   - bu buton duz `palette.cardAlt` (#3B4E70) idi: doygunlugu
                //     dusuk, kabartmasi/konturu/isimasi olmayan bir levha,
                //   - govde metni `textSecondary` = duz GRI,
                //   - "KABUL ET" `NeonGreen` (#4ADE80, zaten acik) idi ve
                //     ustune `lerp(..., White, 0.30)` uygulaniyordu -> nane
                //     pastel.
                // Menuler ve sonuc diyaloglari coktan `GameButton`a gecmisti
                // (Faz 158-167); bu iki ekran atlanmisti. Artik ayni malzeme:
                // 3B govde, cam parlakligi, kontur ve dis isima.
                GameButton(
                    text = language.pick(tr = "Gizlilik Politikası ve Kullanım Şartları", en = "Privacy Policy & Terms of Use", it = "Informativa sulla Privacy e Termini di Utilizzo", fr = "Politique de Confidentialité et Conditions d'Utilisation", es = "Política de Privacidad y Términos de Uso"),
                    onClick = { openPolicy() },
                    // Koyu ve DOYGUN lacivert govde + camgobegi yazi. Ayni
                    // bilgi hiyerarsisi (ikincil eylem) korunuyor ama artik
                    // zeminde "duran" bir nesne.
                    // `resultButtonColors` konturu TABANDAN turetiyor
                    // (lerp(base, beyaz, 0.50)) -- lacivert bir tabanda bu
                    // GRI bir kontur veriyor ve buton yine soluk okunuyordu.
                    // Burada renkler dogrudan veriliyor: derin lacivert govde,
                    // TAM camgobegi kontur.
                    colors = GameButtonColors(
                        top = Color(0xFF17456F),
                        bottom = Color(0xFF0A2340),
                        rim = NeonCyan,
                        shade = Color(0xFF04121F),
                        content = lerp(NeonCyan, Color.White, 0.30f)
                    ),
                    fontSize = 13.sp,
                    minHeight = 48.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terms_accept_open_policy_button")
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Yesil artik `NeonGreen` (#4ADE80) degil sonuc diyaloglarinin
                // DOYGUN basari yesili (#3ED66A); ustelik beyaza cekme 0.30 ->
                // 0.22 (GameButton'in kendi receti), yani yikanmiyor.
                GameButton(
                    text = language.pick(tr = "KABUL ET", en = "ACCEPT", it = "ACCETTA", fr = "ACCEPTER", es = "ACEPTAR"),
                    onClick = onAccept,
                    // Taban `ResultSuccessAccent` (#3ED66A) idi; GameButton ust
                    // yuzu beyaza %22 cektigi icin ekranda #8FE0A0'a, yani yine
                    // NANE PASTELE dusuyordu. Taban doygun/koyu yesile
                    // (#16A34A) cekildi: ust yuz artik #45B571, alt yuz
                    // #12823B -- ayni kabartma, cok daha doygun okuma.
                    colors = resultButtonColors(Color(0xFF16A34A), Color(0xFF04220F)),
                    fontSize = 17.sp,
                    minHeight = 56.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terms_accept_button")
                )
            }
        }
    }
}
