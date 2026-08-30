import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  // Faz 82: ic Kotlin namespace de applicationId ile eslesecek sekilde
  // com.example -> com.miniappfactory.boomblocks olarak yeniden adlandirildi
  // (Gorev #27, uzun suredir ertelenmisti). applicationId zaten Faz 68'de
  // bu degerdeydi, hicbir Play Store/uygulama-kimligi riski yok.
  namespace = "com.miniappfactory.boomblocks"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    // Faz 68: kullanici istegiyle "aistudio"/"blasttheblocks" kalintilarindan
    // temizlendi — uygulama henuz Play Store'a yayinlanmadigi icin bu son
    // firsat (yayindan sonra applicationId degistirilemez). Yeni marka adiyla
    // (MiniAppFactory) tutarli.
    applicationId = "com.miniappfactory.boomblocks"
    minSdk = 24
    targetSdk = 36
    // Faz 88: closed testing release'i internal testing'in kullandigi
    // versionCode=1 ile cakisti ("Version code 1 has already been used").
    // Play Console tum track'lerde tekil/artan versionCode istiyor.
    // Faz 95c: Faz 90-95 arasi tum degisiklikleri (booster mimarisi, drag-lift
    // duzeltmesi, UI sikilastirma vb.) test kullanicilarina ulastirmak icin.
    // Faz 97: Faz 96-97 (can sistemi kaldirildi, reklam mekanizmasi yeniden
    // duzenlendi) degisikliklerini Play Console'a yuklemek icin.
    // Faz 100: Faz 98-99 (reklam no-fill'de oyuncu kilitleniyordu, bedava
    // sifirla butonu kaldirildi) testcilere hic ulasmamisti + S8 gelistirici
    // test cihazi listesinden cikarildi (artik gercek testci cihazi).
    // Faz 102: kullanici "garanti olsun" diye 5'i atlayip 6'yi sectti (Play'de
    // yayindaki release hala 4/1.0.3; 5 uretildi ama hic yuklenmedi).
    // versionName 1.0.4 kaliyor — o da hic yayinlanmadi.
    // Faz 108 (release): 6 -> 7. Faz 103-107 arasi HICBIR degisiklik testcilere
    // ulasmadi — reklam ekonomisi bosluklari (103), "Kariyer" yeniden adlandirma
    // + kilavuz duzeltmesi (104), coklu patlama titresimi + Ayarlar'da Titresim
    // anahtari (105/105b), patlama soleni: surukleme onizlemesi + patlama rengi
    // + sok dalgasi (106), 16 eksik donme yonu: 18 -> 34 parca (107).
    // versionName 1.0.4 -> 1.0.5: 1.0.4 Alpha kanalina yuklendi, ayni ada FARKLI
    // bir build vermek karisiklik yaratir.
    // Faz 108 (release): 7 -> 8. vc7 AAB'si uretildi ama yayina alinmadi;
    // uzerine reklam/riza uyum duzeltmeleri geldi (interstitial cift dokunma
    // korumasi, banner yukseklik rezervasyonu + tepsiden uzaklastirma, UMP
    // gizlilik secenekleri girisi, canRequestAds kapisi). Ayni versionCode ile
    // FARKLI bir ikili uretmemek icin numara artirildi.
    // Faz 112 (closed testing): 8 -> 9. HUD tam yeniden tasarimi (2x3 harita, ortada
    // SKOR math-based, saga TARGET, soiunda power-ups). Grid kenari 1.5 -> 0.5 dp.
    // Puan sartlari static 100+(n-1)*5. FloatingScoreManager (performance) kaldirilan.
    // Faz 114 (release): 9 -> 10. vc9/1.0.7 Play'e YUKLENDI (kullanici teyidi;
    // handover'daki "vc9 hic yuklenmedi" notu yanlisti). Uzerine iki Play
    // POLITIKA duzeltmesi geldi:
    //   - gecis reklami siklik siniri (60sn min aralik + 45sn oturum grace).
    //     Oncesinde zaman bazli hicbir ust sinir yoktu; Pro Mod'da ust uste
    //     kaybeden oyuncu ~30-60 sn'de bir tam ekran reklam goruyordu
    //     ("disruptive ads" yaptirim alani).
    //   - bildirim metinlerinden biri bes dilde de reklam izlemeye cagiriyordu;
    //     Play bildirimlerin promosyon araci olarak kullanilmasini yasaklar.
    // versionName 1.0.7 -> 1.0.8: 1.0.7 yayinlandi, ayni ada FARKLI bir build
    // vermek karisiklik yaratir (bkz. yukarida 1.0.4 -> 1.0.5 ayni gerekce).
    // Faz 124 (release): 10 -> 11. vc10/1.0.8 Play Console'da Closed testing ->
    // Alpha'da CANLI oldugu Play Console ekran goruntusuyle DOGRULANDI (Son
    // guncelleme: 16 Agu 2026) — Faz 117-124'un tum degisikliklerini (mod karti
    // renk/boyut duzeltmeleri, interstitial oturum grace 45sn -> 10sn, gercek
    // geri-tusu-baypas duzeltmesi, gezinen oyun parcalari 4 ekranda) tasiyan bu
    // build AYNI versionCode ile yuklenemez (Play tekil/artan versionCode ister).
    // Faz 135 (release): 11 -> 12, 1.0.9 -> 1.1.0.
    // NEDEN vc11 TEKRAR KULLANILMADI: vc11/1.0.9 AAB'si 17 Agu 2026'da uretildi
    // ama Play Console'a HIC YUKLENMEDI (handover kaydi). Yani teknik olarak
    // vc11 hala bos olabilir. Yine de artirildi, iki gerekceyle:
    //   1. Play Console durumu canli olarak DOGRULANMADI. Bu projede "Play'de ne
    //      var" varsayimi daha once IKI KEZ yanlis cikti (bkz. handover §2).
    //      vc12 her iki senaryoda da calisir, vc11 yalnizca biri dogruysa.
    //   2. O AAB'nin icerigi ile bu build arasinda Faz 125-134 var — ayni
    //      versionName altinda iki farkli build dolasmasi karisiklik yaratir
    //      (bkz. yukarida 1.0.4 -> 1.0.5 ve 1.0.7 -> 1.0.8 ayni gerekce).
    // versionName 1.0.9 -> 1.1.0 (yama degil MINOR): yeni bir oyun modu
    // (Kolay Mod, Retro'nun yerine), 7 yeni blok temasi, yeni gorunum
    // (Seker Pembesi), Pro Mode zorluk egrisi duzeltmesi ve reklam/jeton
    // ekonomisinin yeniden dengelenmesi.
    // Faz 143 (release): 12 -> 13. Play Console vc12'yi "zaten kullanilmis"
    // olarak reddetti — vc12 AAB'si yuklenmeye baslanmis, yukleme yarida
    // durdurulmus olsa bile Play o versionCode'u tuketilmis sayiyor.
    // versionName 1.1.0 AYNEN KALIYOR: icerik ayni surum, sadece yeniden
    // yuklenebilir bir versionCode gerekiyordu (1.0.4->1.0.5 gibi bir "farkli
    // build, ayni ad" durumu YOK, bu gercekten ayni surum).
    //
    // Faz 148: 13 -> 14. vc13 de yuklenmeye calisildi ve tukendi.
    //
    // KURAL (bu projede DORDUNCU kez ayni hata yapildi, artik varsayim degil):
    // Play'e YUKLEMESI DENENEN her versionCode tukenir — yukleme yarida
    // kesilse, iptal edilse, taslakta kalsa bile. "Yayinlanmadi" ile
    // "kullanilmadi" ayni sey DEGIL.
    // Bu yuzden yeni bir release AAB'si uretmeden ONCE kullaniciya sorulmali:
    // "bu versionCode'u Play'e yuklemeyi denedin mi?" Emin olunamiyorsa
    // dogrudan bir sonraki sayiya cikilmali — versionCode ucuz, reddedilen
    // yukleme pahali.
    // Faz 150 (release): 14 -> 15, versionName 1.1.0 -> 1.1.1.
    // vc14 de Play'e yuklenmeye calisildi ve tukendi (bkz. yukaridaki KURAL).
    // Bu sefer versionName de artti cunku ICERIK degisti: jeton ekonomisi
    // yeniden dengelendi (baslangic 150 -> 100, Kariyer/Kolay bolum odulu
    // 5 -> 10, Pro 10 -> 25). Ayni ada farkli bir build vermek karisiklik
    // yaratirdi — bkz. 1.0.4 -> 1.0.5 ve 1.0.7 -> 1.0.8 ayni gerekce.
    //
    // Faz 151 (release): 15 -> 16, versionName 1.1.1 -> 1.1.2.
    // Kullanici teyidi: "su anda Google'daki vc15 (1.1.1)" — yani vc15
    // TUKENDI (yayinda), bir sonraki sayiya cikildi.
    // versionName de artti cunku ICERIK degisti: her patlamada ekran
    // sarsintisi (eskiden 3+ satir/kombo), daha yogun parcacik, Pro Mode
    // hedef egrisi 250 +50/bolum -> 200 +20/bolum (L40 sonrasi +10) ve buna
    // bagli Pro bolum odulu 25 -> 15. Ayni ada farkli bir build vermek
    // karisiklik yaratirdi — bkz. 1.0.4 -> 1.0.5, 1.0.7 -> 1.0.8, 1.1.0 -> 1.1.1.
    //
    // Faz 154 (release): 16 -> 17, versionName 1.1.2 -> 1.1.3.
    //
    // Faz 160 (release): 17 -> 18, versionName 1.1.3 -> 1.1.4.
    // Kullanici vc17'yi Play'e YUKLEDIGINI bildirdi ("vc17 yuklendi"), yani vc17
    // TUKENDI — bkz. yukaridaki KURAL. Icerik: tablet titresim duzeltmesi —
    // Galaxy Tab S7'de oyun titresimleri hic hissedilmiyordu; kok neden vibrate
    // cagrisinda AudioAttributes (usage) eksikligiydi, ayrica darbe sureleri
    // uzatildi (tablet motorunun donebilmesi icin).
    // Faz 162 (release): versionCode 18 -> 19. vc18 (1.1.4) production'da
    // CANLI, yani tukendi.
    //
    // versionName 1.1.4 -> 1.2.0 (yama degil MINOR): bu surumde arayuz dili
    // bastan yazildi (ortak bilesen kiti, yeni varliklar, doygunlastirilmis
    // palet) ve ALL CLEAR diye YENI bir mekanik eklendi. Yama numarasi bunu
    // anlatmaz; 1.0.9 -> 1.1.0 ile ayni gerekce.
    // 🔴 Faz 163 (ACIL HOTFIX): versionCode 19 -> 20, versionName 1.2.0 -> 1.2.1.
    // vc19 URETIMDE COKUYORDU: yeterli jetonla "SATIN AL"a basinca
    // IllegalArgumentException: Padding must be non-negative (GameButton'un
    // basilma animasyonunda depth - sink negatife dusuyordu). Yama surumu,
    // cunku icerik degismedi — yalnizca cokme giderildi.
    // Faz 183-186 (release): versionCode 20 -> 21, versionName 1.2.1 -> 1.2.2.
    // Kullanici teyidi: vc20 (1.2.1 HOTFIX) Play'e yuklendi, yani TUKENDI --
    // bkz. yukaridaki KURAL.
    // Yama surumu: yeni mekanik yok. Icerik, varlik setindeki DILIMLEME
    // hatalarinin duzeltilmesi (setbtn icinde ikinci buton, pill_l'in kirpilmis
    // kuyrugu, node_open'in kaymis govdesi), 9 dilim dikisleri, panel/yol
    // dokusunun temizlenmesi, mod basliklarinin 5 dile acilmasi ve
    // landing/onboarding ekranlarinin oyunun buton malzemesine gecirilmesi.
    versionCode = 21
    versionName = "1.2.2"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // Onceden SADECE ortam degiskeninden okunuyordu — kullanicinin her release
      // build oncesi `!` ile STORE_PASSWORD/KEY_PASSWORD set etmesi gerekiyordu,
      // ama her `!` komutu ve her Gradle cagrisi AYRI bir shell surecinde calisti-
      // gindan (export edilen deger bir sonraki komuta miras kalmiyor) bu her
      // seferinde tekrarlanan bir surtunmeye donusmustu. Artik git'e HIC girmeyen
      // (bkz. .gitignore: "signing.properties") yerel bir dosyadan da okunabiliyor,
      // ortam degiskeni varsa o ONCELIKLI kalir (CI/farkli makine senaryosu icin).
      val signingPropsFile = rootProject.file("signing.properties")
      val signingProps = Properties().apply {
        if (signingPropsFile.exists()) signingPropsFile.inputStream().use { load(it) }
      }
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: signingProps.getProperty("storePassword")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: signingProps.getProperty("keyPassword")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // GECICI (gorsel dogrulama turu): cihazdaki Play-imzali surum kaldirilmadan
      // yan yana kurulabilmek icin. Is bitince GERI ALINACAK.
      applicationIdSuffix = ".dev"
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  lint {
    disable.add("NewApi")
    disable.add("PropertyEscape")
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.play.services.ads)
  implementation(libs.user.messaging.platform)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.fragment.ktx)
  // Faz 109: `androidTest/` klasoru YOK, yani bugune kadar hicbir UI testi
  // calismiyordu — ui-test-junit4 zaten testImplementation'daydi ama JVM'de
  // Android runtime'i saglayan bir sey olmadigi icin kullanilamiyordu.
  // Robolectric bu bosluğu dolduruyor: Compose UI testleri artik cihazsiz,
  // `testDebugUnitTest` ile calisiyor (bkz. testOptions.unitTests.
  // isIncludeAndroidResources, zaten aciktı).
  //
  // BOM platform'u testImplementation'a da veriliyor: ui-test-junit4 surumsuz
  // tanimli ve bugune kadar SADECE ana kaynak setinin gecisli bagimliliklariyla
  // (tesadufen 1.7.2) hizalaniyordu. Artik ayni BOM'a acikca bagli.
  testImplementation(platform(libs.androidx.compose.bom))
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
