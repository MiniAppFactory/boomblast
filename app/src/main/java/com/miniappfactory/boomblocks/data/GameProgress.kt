package com.miniappfactory.boomblocks.data

// Faz 47: kullanici "güçlendiriciler daha pahalı olmalı, reklam izlemeye
// teşvik etmeli, bomba hepsi 100 olmalı, shuffle olmasın, bomba ve line
// clear olsun sadece" dedi — SHUFFLE turu tamamen kaldirildi (35/50/20 ->
// sadece BOMB/LINE_CLEAR, ikisi de 100).
// Faz 129: guclendirici fiyati 150 -> 80 (kullanici karari). Yeni denge:
// bir reklam 40 jeton (AD_TOKEN_REWARD), yani bir guclendirici artik TAM 2
// reklam ediyor — onceden ~4 reklamdi. Baslangic bakiyesi 150 jeton oldugu
// icin oyuncu ilk bolume girmeden bir guclendirici alip ustune 70 jeton
// birakabiliyor (onceden bakiye tam tamina bir guclendiriciye yetiyordu).
enum class BoosterType(val tokenPrice: Int) {
    BOMB(80),
    LINE_CLEAR(80)
}

// Faz 109: patlama efektleri (isik huzmesi, fizikli parcaciklar, ileride ambient
// toz ve radyal isinlar) zayif cihazlarda kare suresini yiyebiliyor, ayrica
// "ne kadar sölen" bir zevk meselesi. hapticsEnabled (Faz 105) ile AYNI zincir
// (alan -> DataStore anahtari -> repository setter -> ViewModel -> Ayarlar satiri),
// tek farki iki degil UC degerli olmasi.
//
// Neden Boolean/Int degil de enum + String kod:
//  - Boolean yetmiyor (uc kademe), Int ise DataStore'da anlamsiz bir sayi olarak
//    kalir ve ileride araya bir kademe eklenince (ör. OFF) eski kayitlar kayar.
//  - String kod, AppLanguage'in ("tr"/"en"/...) ve BoosterType'in (enum adi)
//    zaten kullandigi desen; bilinmeyen/bozuk kod okununca fromCode() sessizce
//    varsayilana duser, yani ileri/geri surum uyumu bedava geliyor.
// Varsayilan NORMAL — bugunku (Faz 106) efekt yogunlugu "normal" kabul ediliyor,
// mevcut oyuncunun hissi guncellemeyle degismesin.
enum class EffectIntensity(val code: String) {
    LOW("low"),
    NORMAL("normal"),
    HIGH("high");

    companion object {
        fun fromCode(code: String?): EffectIntensity = entries.find { it.code == code } ?: NORMAL
    }
}

// AppLanguage.label() ile ayni desen — gorsel metin enum'un yaninda duruyor,
// cagiran ekranin when/if zinciri yazmasi gerekmiyor.
fun EffectIntensity.label(language: AppLanguage): String = when (this) {
    EffectIntensity.LOW -> language.pick(tr = "Düşük", en = "Low", it = "Basso", fr = "Faible", es = "Bajo")
    EffectIntensity.NORMAL -> language.pick(tr = "Normal", en = "Normal", it = "Normale", fr = "Normal", es = "Normal")
    EffectIntensity.HIGH -> language.pick(tr = "Yüksek", en = "High", it = "Alto", fr = "Élevé", es = "Alto")
}

data class PlayerProgress(
    val tokens: Int = 150,
    val highestUnlockedLevel: Int = 1,
    val levelStars: Map<Int, Int> = emptyMap(),
    val ownedBoosters: Map<BoosterType, Int> = emptyMap(),
    // Faz 94: kullanici "seviyelideki bomba/satir temizle boosterlari
    // sonsuzlukta da geçerli oluyor, bu dogru degil" dedi — her mod artik
    // kendi AYRI booster envanterine sahip. `ownedBoosters` (yukarida)
    // Seviyeli Mod icin "legacy/varsayilan" adiyla aynen kaliyor (geriye
    // donuk uyumluluk, mevcut kullanici verisi kaybolmasin).
    val challengeOwnedBoosters: Map<BoosterType, Int> = emptyMap(),
    val endlessOwnedBoosters: Map<BoosterType, Int> = emptyMap(),
    // Faz 131: Kolay Mod da KENDI envanterini kullaniyor. Faz 128'de Kariyer'den
    // kopyalandigi icin ikisi ayni kovayi (ownedBoosters) paylasiyordu; Kolay Mod
    // cok daha hizli bolum gectirdigi icin orada biriktirip Kariyer'de harcama
    // yolu aciktı. Kullanici karari: "her bir guclendirici kendi bucket'ini
    // kullansin, ortak kullanan hicbir mod kalmasin." Artik dort modun dordu ayri.
    val comfortOwnedBoosters: Map<BoosterType, Int> = emptyMap(),
    // Faz 137: jetonla acilan blok temalari. Klasik her zaman ucretsiz (fiyat 0),
    // Meyve ve Sekerleme 100 jeton, Faz 132-134'te gelen 7 yeni tema 300 jeton.
    // NOT: Meyve/Sekerleme v1.0.8'de ucretsizdi. Kullanici karari: uygulama
    // henuz kapali testte oldugu icin eski oyuncu korumasi (tek seferlik
    // migrasyon) EKLENMEDI. Acik yayindan sonra bir tema paraliya cevrilirse
    // o koruma gerekli olur.
    val unlockedThemes: Set<String> = emptySet(),
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.5f,
    val musicEnabled: Boolean = true,
    // Faz 105: coklu patlama titresimi (Kariyer/Pro/Sonsuz). Varsayilan ACIK —
    // ozelligin kendisi ancak hissedilirse ise yariyor; rahatsiz olan kapatir.
    // Retro Modu'nun kendi ayri anahtari var (kendi ses anahtari gibi).
    val hapticsEnabled: Boolean = true,
    // Faz 109: patlama efekti yogunlugu (Düşük/Normal/Yüksek). Bkz. EffectIntensity.
    val effectIntensity: EffectIntensity = EffectIntensity.NORMAL,
    val darkMode: Boolean = true,
    val language: AppLanguage = AppLanguage.TR,
    val blockTheme: String = "CLASSIC",
    val uiSkin: String = "DEFAULT",
    val hasSeenOnboarding: Boolean = false,
    val endlessHighScore: Int = 0,
    val hasAcceptedTerms: Boolean = false,
    val hasMadeFirstMove: Boolean = false,
    val notificationsEnabled: Boolean = true,
    // Faz 39/42: Seviye Modu'nda son zorunlu gecis reklamindan bu yana kac
    // bolum tamamlandi — esige (Faz 42: her bolum) ulasinca sifirlanir ve bir interstitial gosterilir.
    val levelsCompletedSinceInterstitial: Int = 0,
    // Faz 77: Pro Mode ("Challenge") — Seviyeli Mod'dan AYRI ilerleme.
    val challengeHighestUnlockedLevel: Int = 1,
    val challengeLevelStars: Map<Int, Int> = emptyMap(),
    // Faz 96: can sistemi (bypass edilebiliyordu, bkz. eski challengeLives)
    // yerini "Yeniden Başlat"a bagli esikli interstitial reklama birakti —
    // levelsCompletedSinceInterstitial ile AYNI desen, ayri bir sayac.
    val proRestartsSinceInterstitial: Int = 0,
    // Faz 128: Comfort Mode (TR "KOLAY MOD") — Retro'nun yerini aldi. Kariyer'in
    // AYNI ekranlarini kullanir ama kendi ilerlemesi, kendi (cok yumusak) hedef
    // egrisi ve tahta-farkindali pozitif onyargisi vardir. Kariyer/Pro ile
    // AYRI ilerleme: oyuncu Kolay Mod'da ilerleyince Kariyer acilmaz.
    val comfortHighestUnlockedLevel: Int = 1,
    val comfortLevelStars: Map<Int, Int> = emptyMap()
)

// Faz 130: odul 40 -> 80. Gerekce kullanicinin kendi ifadesi: "uyumluluk
// acisindan". Artik oyunun HER yerinde bir reklam = bir guclendirici:
//   - Loadout ekrani: 1 reklam = 80 jeton = tam 1 guclendirici (fiyat 80)
//   - Oyun ici (dort modda da): 1 reklam = dogrudan +1 guclendirici
// Onceden loadout'ta 2 reklam gerekiyordu, oyun icinde 1 — oyuncu ayni seyin
// iki farkli fiyatini goruyordu. Miktar TEK yerde tanimli: hem odulu veren kod
// (BlastViewModel.watchAdForTokens) hem etiket (LoadoutScreen) buradan okuyor.
const val AD_TOKEN_REWARD = 80

enum class MissionType { COMPLETE_LEVELS, CLEAR_LINES, USE_BOOSTERS, SCORE_POINTS, MULTI_CLEARS }

// Faz 73: her gorev artik TEK hedef/odul yerine kademeli 3 milestone'luk bir
// merdiven — kullanici: "5 kullan = 20 jeton, 10 kullan = 30 jeton daha,
// 15 kullan = 50 jeton daha" gibi. target'lar KUMULATIF (tier[1].target,
// tier[0]'dan itibaren degil, bastan itibaren sayilir).
data class MissionTier(val target: Int, val rewardTokens: Int)

data class WeeklyMissionDef(
    val id: String,
    val titleTr: String,
    val titleEn: String,
    val titleIt: String,
    val titleFr: String,
    val titleEs: String,
    val tiers: List<MissionTier>,
    val type: MissionType
) {
    // Faz 73 duzeltmesi: kullanici "güçlendirici kullan ama kaç tane belli
    // olmuyor" dedi — baslik artik aktif tier'in hedefini {count} yerine
    // koyarak somutlastiriyor (ornek: "5 Güçlendirici Kullan").
    fun title(language: AppLanguage, count: Int): String =
        language.pick(tr = titleTr, en = titleEn, it = titleIt, fr = titleFr, es = titleEs)
            .replace("{count}", "$count")
}

data class WeeklyMissionProgress(
    val weekId: String,
    val missions: List<WeeklyMissionDef>,
    // Ham kumulatif sayac (mission.id -> deger), en yuksek tier'in target'ina coerce edilir.
    val progress: Map<String, Int> = emptyMap(),
    // "missionId#tierIndex" formatinda claim edilmis tier'ler.
    val claimed: Set<String> = emptySet()
)
