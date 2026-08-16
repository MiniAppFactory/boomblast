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
    versionCode = 8
    versionName = "1.0.6"

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
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
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
