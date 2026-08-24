package com.miniappfactory.boomblocks.game

// Parametrik, sonsuz level üretimi — elle yüzlerce level tasarlamak yerine
// zorluk seviyeye göre kademeli artıyor (bkz. docs/GENRE_RESEARCH_NOTES.md #5).
data class LevelDefinition(
    val number: Int,
    val targetScore: Int,
    // shapePoolTier, BlastTheBlocksGame.kt'deki generateNewTray()'in SHAPE_PATTERNS.take(n)
    // mantığıyla aynı kademelendirmeyi kullanır: 1=basit parçalar, 3=tüm parçalar.
    val shapePoolTier: Int,
    // Faz 77: Pro Mode (Challenge) icin — 1f=degisiklik yok (Level Modu),
    // Pro Mode'da bu carpan BlastTheBlocksGame'in TUM puan artislarina
    // uygulaniyor ("daha yuksek puan carpani").
    val scoreMultiplier: Float = 1f
)

object LevelGenerator {
    // Faz 151: Pro Mode hedef egrisi -- tek yerden yonetilir.
    const val PRO_BASE_TARGET = 200
    const val PRO_TARGET_STEP = 20
    // Faz 151b: yumusak tavan. Duz +20 hic kirilmadigi icin cok ileri
    // seviyelerde bolum suresi kontrolsuz uzuyordu (L100'de 2180 puan).
    // L40'tan sonra adim yariya iniyor -- egri hala MONOTON artiyor, sadece
    // egimi dusuyor; "artik zorlasmiyor" hissi olusmuyor.
    const val PRO_TARGET_SOFT_CAP_LEVEL = 40
    const val PRO_TARGET_STEP_AFTER_CAP = 10

    fun forLevel(number: Int): LevelDefinition {
        val safeNumber = number.coerceAtLeast(1)
        val targetScore = targetScoreForLevel(safeNumber)
        val shapePoolTier = when {
            safeNumber <= 3 -> 1
            safeNumber <= 8 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, targetScore, shapePoolTier)
    }

    // Faz 64: kullanici "amacımız çok bölüm geçsin, çok reklam izlesin —
    // challenge ile değil bölümü geçtim başarma isteğiyle oynatmalıyız" dedi.
    // Faz 45/61'deki kademeli (40/10/8/2) egri tamamen terk edildi — DUZ,
    // sabit +5/seviye artis (100, 105, 110, 115...). Amac bilerek "hicbir
    // zaman zorlasmiyor gibi hissettiren" bir egri: her seviye bir oncekine
    // neredeyse ozdes, oyuncu skill/challenge yerine sadece "bir tane daha
    // gectim" tekrarindan motive oluyor.
    //
    // Faz 110b: Reklam sikligi azaltildi (iki levelda bir, ilk 10 level).
    // Puan şartları eski modele geri döndürüldü: 100 + (n-1)*5.
    private fun targetScoreForLevel(n: Int): Int = 100 + (n - 1) * 5

    // Faz 77: Pro Mode — "daha zor zorluk eğrisi, daha yüksek puan çarpanı"
    // (handover 6.1). Faz 79'da zorluk agirligi PUANDAN PARCA HAVUZUNA
    // kaydirildi (bkz. BoomBlocksGame.kt generateNewTray isChallengeMode dali,
    // 1x1 hic gelmiyor, havuz cok daha hizli aciliyor) — bu yuzden hedef puan
    // egrisi de +20/seviyeden +10/seviyeye YUMUSATILDI (kullanicinin kendi
    // onerisi: "zorlugu parcadan yonetirsek puanı o kadar dik tutmaya gerek yok").
    // 1.5x puan carpani ve shapePoolTier (dead/kullanilmiyor, bkz. generateNewTray)
    // aynen kaliyor.
    //
    // Faz 110: Pro Mode de Career Mode stratejisini takip et — Level 1-10
    // binding phase: daha yiksekcek puan (250 base), Level 11+ ise seamless
    // (Level 10 = 700 + (n-10)*5).
    //
    // Faz 151: iki kademeli egri (250 +50/lv, sonra 700 +5/lv) TERK EDILDI.
    // Kullanici geri bildirimi: "pro mode da puanlar cok fazla hizli artiyor,
    // asiri reklam izlemek zorunda kalmaktan izlemezler; pro 200 puanla
    // baslayip 20 20 artmali her levelda, zaten pronun zorlugu aslinda board
    // bias olmamasindan geliyor."
    //
    // Teshis dogru: Pro'da tahta-farkindali pozitif onyargi YOK (bkz.
    // BoomBlocksGame.generateNewTray -- helpfulIndices SADECE isComfortMode
    // dalinda dolduruluyor), 1x1 agirligi 0 oldugu icin hic gelmiyor ve havuz
    // seviye 6'da tamamen aciliyor. Yani Pro'nun zorlugu zaten PARCADAN geliyor;
    // ustune hedef puani +50/seviye tirmandirmak ayni zorlugu ikinci kez
    // fiyatlandiriyordu. Oyuncu Level 10'da 700 puana takilip ya birakiyor ya da
    // rewarded "devam" reklamina MECBUR kaliyordu -- mecburi reklam izlenmez,
    // oyun birakilir (bkz. Faz 64'un ayni yondeki karari).
    //
    // Yeni egri: 200 + (n-1)*20, L40'tan sonra adim +10 (yumusak tavan).
    //   L1  250 -> 200      L10 700 -> 380      L20 750 -> 580
    //   L30 800 -> 780 (kesisim ~L31)           L40 850 -> 980
    //   L50 900 -> 1080     L100 1150 -> 1580
    // Erken bolumler belirgin sekilde nefes aliyor. Faz 151'in ilk halinde adim
    // hic kirilmiyordu ve L100'de hedef 2180'e cikiyordu -- oyuncu modu coktan
    // benimsemis olsa bile tek bir bolum orada dakikalarca suruyordu. Yumusak
    // tavanla egri hala her seviyede artiyor (yani "zorlasmayi birakti" hissi
    // yok) ama ust uc 2180 yerine 1580'de kaliyor.
    fun forChallengeLevel(number: Int): LevelDefinition {
        val safeNumber = number.coerceAtLeast(1)
        val targetScore = if (safeNumber <= PRO_TARGET_SOFT_CAP_LEVEL) {
            PRO_BASE_TARGET + (safeNumber - 1) * PRO_TARGET_STEP
        } else {
            PRO_BASE_TARGET + (PRO_TARGET_SOFT_CAP_LEVEL - 1) * PRO_TARGET_STEP +
                (safeNumber - PRO_TARGET_SOFT_CAP_LEVEL) * PRO_TARGET_STEP_AFTER_CAP
        }
        val shapePoolTier = when {
            safeNumber <= 2 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, targetScore, shapePoolTier, scoreMultiplier = 1.5f)
    }

    // Faz 128: Comfort Mode (TR "KOLAY MOD") — Retro'nun yerini aldi.
    // Kullanici tanimi: "100 puanla baslayacak ve her bolumde hedef 1 puan
    // artacak. daha cok reklam izleme daha cok bolum gecme motivasyonu
    // yaratacak." Yani Kariyer'in +5/seviye egrisinin BESTE BIRI: bolum 100'de
    // hedef hala sadece 199 puan (Kariyer'de 595 olurdu). Parca havuzu ve
    // agirlik tablosu Kariyer'in AYNISI (BoomBlocksGame'de isChallengeMode ve
    // isEndless false oldugu icin otomatik olarak Kariyer dalina duser);
    // farki hedef egrisi + tahta-farkindali pozitif onyargi (bkz. generateNewTray).
    fun forComfortLevel(number: Int): LevelDefinition {
        val safeNumber = number.coerceAtLeast(1)
        val shapePoolTier = when {
            safeNumber <= 3 -> 1
            safeNumber <= 8 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, 100 + (safeNumber - 1), shapePoolTier)
    }

    // Faz 128: Comfort Mode reklam temposu — kullanici karari "ilk 3 bolum
    // reklamsiz, sonra her bolum". Gerekce: mod cok hizli bolum gectirdigi icin
    // ilk oturumda reklam yorgunlugu riski var; oyuncu modu taniyana kadar
    // tamamen temiz kaliyor, 4. bolumden itibaren her gecis reklamli.
    // NOT: InterstitialFrequencyPolicy (60sn min aralik) yine ustte devrede,
    // yani pratikte cok hizli ust uste gecislerde hepsi gosterilmez.
    fun shouldShowInterstitialAfterComfortLevel(level: Int): Boolean = level > 3

    // Faz 110: Career Mode reklam sinlifi. Level 1-10'de binding phase'i
    // destelemek icin her 2 levelda 1 reklam (2, 4, 6, 8, 10); Level 11+'da
    // standard "her level" gosterimine gecis.
    fun shouldShowInterstitialAfterLevel(level: Int): Boolean = when {
        level <= 10 -> level % 2 == 0  // çift seviyelerde (2, 4, 6, 8, 10)
        else -> true  // Level 11+ her level sonrası
    }
}
