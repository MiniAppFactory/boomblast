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
    // Faz 110: Career Mode (Level 1-10) — binding phase: reklam sikligi
    // azaltildi (her 2 levelda 1) oyuncuyu takip etmek amaciyla. Puan serti
    // de dinamik: Level 1-10'da hedef 45-60 sn oynanis (1 star) vermek icin
    // +40/seviye (200, 240, 280, ..., 560). Level 11+ ise Level 10'dan
    // seamless devam: 560 + (n-10)*5.
    private fun targetScoreForLevel(n: Int): Int = when {
        n <= 10 -> 200 + (n - 1) * 40
        else -> 560 + (n - 10) * 5
    }

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
    fun forChallengeLevel(number: Int): LevelDefinition {
        val safeNumber = number.coerceAtLeast(1)
        val targetScore = when {
            safeNumber <= 10 -> 250 + (safeNumber - 1) * 50
            else -> 700 + (safeNumber - 10) * 5
        }
        val shapePoolTier = when {
            safeNumber <= 2 -> 2
            else -> 3
        }
        return LevelDefinition(safeNumber, targetScore, shapePoolTier, scoreMultiplier = 1.5f)
    }

    // Faz 110: Career Mode reklam sinlifi. Level 1-10'de binding phase'i
    // destelemek icin her 2 levelda 1 reklam (2, 4, 6, 8, 10); Level 11+'da
    // standard "her level" gosterimine gecis.
    fun shouldShowInterstitialAfterLevel(level: Int): Boolean = when {
        level <= 10 -> level % 2 == 0  // çift seviyelerde (2, 4, 6, 8, 10)
        else -> true  // Level 11+ her level sonrası
    }
}
