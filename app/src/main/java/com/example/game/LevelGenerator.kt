package com.example.game

// Parametrik, sonsuz level üretimi — elle yüzlerce level tasarlamak yerine
// zorluk seviyeye göre kademeli artıyor (bkz. docs/GENRE_RESEARCH_NOTES.md #5).
data class LevelDefinition(
    val number: Int,
    val targetScore: Int,
    // shapePoolTier, BlastTheBlocksGame.kt'deki generateNewTray()'in SHAPE_PATTERNS.take(n)
    // mantığıyla aynı kademelendirmeyi kullanır: 1=basit parçalar, 3=tüm parçalar.
    val shapePoolTier: Int
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

    // Faz 45: eski hedef formulu `500 + (n-1)*250` idi — SINIRSIZ dogrusal
    // buyume, ama zorluk (shapePoolTier, yukarida) seviye 9'da SABITLENIYOR.
    // Sonuc: ileri seviyelerde hedef gitgide zorlasirken oyuncunun elindeki
    // parca cesitliligi hic artmiyor — kullanici "50. kademe oynanamaz hale
    // gelmemeli" diye acikca belirtti. Ayrica puanlama tamamen yeniden
    // olceklendi (bkz. BlastTheBlocksGame.placeShape — artik 1 hucre=1 puan,
    // onceden 10'du), o yuzden hedefler de bu yeni olcege gore YENIDEN
    // tasarlandi, eskisinin basit /10'u degil.
    //
    // Yeni egri: erken seviyelerde belirgin, sonra GITTIKCE KUCULEN artislarla
    // ilerliyor (40 -> 20 -> 8 -> 2 puan/seviye), seviye ~30 civarinda pratikte
    // duzlesiyor — tipki shapePoolTier'in seviye 9'da duzlesmesi gibi. Boylece
    // seviye 30 ile seviye 50 arasindaki fark kucuk kaliyor (600 -> 640),
    // "oynanamaz hale gelme" riski ortadan kalkiyor, ama uzun vadede (cok ileri
    // seviyelerde) hala hafif bir ilerleme hissi (+2/seviye) korunuyor.
    private fun targetScoreForLevel(n: Int): Int {
        var target = 100
        for (level in 2..n) {
            // Faz 61: kullanici "amacımız çok oynatmak" dedi, 7-15 arasi
            // seviye-basi artis 20 -> 10 puana dusuruldu (daha sik "seviye
            // tamamlandi" hissi, daha hizli ilerleme).
            val increment = when {
                level <= 6 -> 40
                level <= 15 -> 10
                level <= 30 -> 8
                else -> 2
            }
            target += increment
        }
        return target
    }
}
