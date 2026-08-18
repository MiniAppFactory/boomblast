package com.miniappfactory.boomblocks.game

import com.miniappfactory.boomblocks.ui.games.boomblocks.SHAPE_PATTERNS
import com.miniappfactory.boomblocks.ui.games.boomblocks.SHAPE_WEIGHTS
import com.miniappfactory.boomblocks.ui.games.boomblocks.SHAPE_WEIGHTS_CHALLENGE
import com.miniappfactory.boomblocks.ui.games.boomblocks.SHAPE_WEIGHTS_ENDLESS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 107: parca havuzunun yapisal garantileri.
 *
 * Bu dosyanin varlik sebebi: SHAPE_PATTERNS ile uc agirlik tablosu PARALEL
 * dizilerdir — birine parca eklenip digerine eklenmezse derleyici uyarmaz,
 * oyun da patlamaz; sadece son parcalar sessizce hic gelmez ya da yanlis
 * agirlikla gelir. Ayrica Faz 55'te elle ayarlanmis zorluk dengesi (hangi
 * ailenin ne kadar pay aldigi) yeni yon eklendiginde farkinda olmadan
 * kayabilir. Ikisi de gozle fark edilmesi cok zor hatalar, bu yuzden test.
 */
class ShapePoolTest {

    /** Deseni karsilastirilabilir bir anahtara cevirir. */
    private fun key(m: List<List<Boolean>>): String =
        m.joinToString("/") { row -> row.joinToString("") { if (it) "1" else "0" } }

    /** 90 derece saat yonunde dondurur. */
    private fun rotate(m: List<List<Boolean>>): List<List<Boolean>> {
        val rows = m.size
        val cols = m[0].size
        return List(cols) { c -> List(rows) { r -> m[rows - 1 - r][c] } }
    }

    /** Bir desenin tum benzersiz donme varyantlari (1, 2 veya 4 tane). */
    private fun variants(m: List<List<Boolean>>): List<List<List<Boolean>>> {
        val seen = LinkedHashMap<String, List<List<Boolean>>>()
        var cur = m
        repeat(4) {
            seen.putIfAbsent(key(cur), cur)
            cur = rotate(cur)
        }
        return seen.values.toList()
    }

    /** Donme denkligine gore aileler, listede gorulme sirasiyla. */
    private fun families(): List<List<Int>> {
        val assigned = mutableSetOf<Int>()
        val result = mutableListOf<List<Int>>()
        SHAPE_PATTERNS.indices.forEach { i ->
            if (i in assigned) return@forEach
            val keys = variants(SHAPE_PATTERNS[i]).map { key(it) }.toSet()
            val members = SHAPE_PATTERNS.indices.filter { key(SHAPE_PATTERNS[it]) in keys }
            assigned.addAll(members)
            result.add(members)
        }
        return result
    }

    @Test
    fun `agirlik tablolari SHAPE_PATTERNS ile ayni boyutta`() {
        assertEquals("Kariyer agirlik tablosu eksik/fazla", SHAPE_PATTERNS.size, SHAPE_WEIGHTS.size)
        assertEquals("Sonsuz agirlik tablosu eksik/fazla", SHAPE_PATTERNS.size, SHAPE_WEIGHTS_ENDLESS.size)
        assertEquals("Pro agirlik tablosu eksik/fazla", SHAPE_PATTERNS.size, SHAPE_WEIGHTS_CHALLENGE.size)
    }

    @Test
    fun `ayni parca listede iki kez tanimli degil`() {
        val keys = SHAPE_PATTERNS.map { key(it) }
        val duplicates = keys.groupBy { it }.filterValues { it.size > 1 }.keys
        assertTrue("Tekrar eden desen(ler): $duplicates", duplicates.isEmpty())
    }

    @Test
    fun `her parcanin tum donme yonleri tanimli`() {
        // Faz 107'nin asil kazanimi: oyunda dondurme yok, bu yuzden listede
        // olmayan bir yon oyunda HIC cikmaz. Once T, Buyuk L, S, Z ve L/J
        // tetrominolari tek yonle sinirliydi.
        val defined = SHAPE_PATTERNS.map { key(it) }.toSet()
        val missing = SHAPE_PATTERNS.flatMap { variants(it) }
            .map { key(it) }
            .filter { it !in defined }
            .distinct()
        assertTrue("Tanimsiz donme yonu var: $missing", missing.isEmpty())
    }

    @Test
    fun `bir ailenin tum yonleri listede yan yana duruyor`() {
        // Havuz "ilk N parca" olarak aciliyor. Aile dagilirsa oyuncuya bir
        // ailenin bazi yonleri acilip bazilari acilmamis olur — keyfi ve
        // aciklanamaz bir durum ("neden ters T var da yan T yok?").
        families().forEach { members ->
            val expected = (members.first()..members.last()).toList()
            assertEquals(
                "Aile listede parcali duruyor (indeksler: $members)",
                expected,
                members
            )
        }
    }

    @Test
    fun `aile paylari Faz 55 dengesiyle ayni`() {
        // Faz 107'de her yon eklenirken agirliklar x4 olceklenip aile ICINDE
        // bolundu. Aile TOPLAMLARI degismedigi surece zorluk dengesi sabittir.
        // Sira: 1x1, ikili duz, uclu duz, 2x2, kose, T, dortlu duz, Buyuk L,
        //       dikdortgen, S, Z, L-tetromino, J-tetromino
        val fams = families()

        fun totals(weights: List<Int>) = fams.map { it.sumOf { i -> weights[i] } }

        assertEquals(
            listOf(4, 16, 24, 12, 24, 4, 8, 4, 8, 4, 4, 4, 4),
            totals(SHAPE_WEIGHTS)
        )
        assertEquals(
            listOf(4, 16, 24, 12, 24, 8, 16, 8, 16, 8, 8, 8, 8),
            totals(SHAPE_WEIGHTS_ENDLESS)
        )
        // Faz 125: kacis parcasi (ikili duz) 3 kat arttirildi, en pahali iki
        // aile (dortlu duz, dikdortgen) kucultuldu — bkz. BoomBlocksGame.kt
        // SHAPE_WEIGHTS_CHALLENGE yorumu. Diger aileler DEGISMEDI.
        assertEquals(
            listOf(0, 24, 16, 8, 16, 12, 16, 12, 8, 12, 12, 12, 12),
            totals(SHAPE_WEIGHTS_CHALLENGE)
        )
    }

    @Test
    fun `Pro Modda 1x1 asla secilemez`() {
        val singleCell = SHAPE_PATTERNS.indexOfFirst { it.size == 1 && it[0].size == 1 }
        assertTrue("1x1 parca listede bulunamadi", singleCell >= 0)
        assertEquals(
            "Pro Mod'da 1x1'in agirligi 0 olmali (kullanici karari, Faz 79)",
            0,
            SHAPE_WEIGHTS_CHALLENGE[singleCell]
        )
    }

    @Test
    fun `hicbir agirlik negatif degil ve her tabloda en az bir pozitif var`() {
        listOf(SHAPE_WEIGHTS, SHAPE_WEIGHTS_ENDLESS, SHAPE_WEIGHTS_CHALLENGE).forEach { w ->
            assertTrue("Negatif agirlik var", w.all { it >= 0 })
            assertTrue("Tum agirliklar 0 — hicbir parca secilemez", w.sum() > 0)
        }
    }
}
