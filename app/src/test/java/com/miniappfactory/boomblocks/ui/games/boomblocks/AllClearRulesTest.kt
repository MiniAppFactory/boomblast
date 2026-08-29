package com.miniappfactory.boomblocks.ui.games.boomblocks

import com.miniappfactory.boomblocks.data.ALL_CLEAR_BONUS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Faz 161 (madde 3): ALL CLEAR kurallarinin birim testleri.
 *
 * Neden bu testler var: ALL CLEAR, oyunun tek yeni skor kaynagi. Yanlis
 * tetiklenirse oyuncu hak etmedigi puan alir ve bolum dengesi bozulur; hic
 * tetiklenmezse ozellik sessizce olur (bu dosyada daha once benzer bir vaka
 * yasandi — Faz 115d'de `applyBoosterAt` tanimliydi ama HIC cagrilmiyordu).
 */
class AllClearRulesTest {

    private val gridSize = 8

    private fun emptyBoard() = MutableList(gridSize * gridSize) { 0 }

    private fun rowIndices(r: Int): Set<Int> =
        (0 until gridSize).map { c -> r * gridSize + c }.toSet()

    private fun colIndices(c: Int): Set<Int> =
        (0 until gridSize).map { r -> r * gridSize + c }.toSet()

    @Test
    fun `tek dolu satir temizlenince tahta bosalir`() {
        val board = emptyBoard()
        for (c in 0 until gridSize) board[3 * gridSize + c] = 1
        assertTrue(willBoardBeAllClear(board, rowIndices(3)))
    }

    @Test
    fun `temizlenen kumenin disinda tek bir dolu hucre varsa ALL CLEAR yok`() {
        val board = emptyBoard()
        for (c in 0 until gridSize) board[3 * gridSize + c] = 1
        // Tahtanin bambaska bir kosesinde tek bir blok kaliyor.
        board[7 * gridSize + 0] = 2
        assertFalse(willBoardBeAllClear(board, rowIndices(3)))
    }

    @Test
    fun `kesisen satir ve sutun birlikte tahtayi bosaltabilir`() {
        val board = emptyBoard()
        for (c in 0 until gridSize) board[2 * gridSize + c] = 1
        for (r in 0 until gridSize) board[r * gridSize + 5] = 3
        val cleared = rowIndices(2) + colIndices(5)
        assertTrue(willBoardBeAllClear(board, cleared))
    }

    @Test
    fun `hicbir sey temizlenmiyorsa bos tahtada bile ALL CLEAR yok`() {
        // Onemli: "reklamla devam" akisi ve guclendiriciler tahtayi patlama
        // ritueli calistirmadan bosaltabiliyor. Kural, temizlenen bir kume
        // olmadan ASLA true donmemeli.
        assertFalse(willBoardBeAllClear(emptyBoard(), emptySet()))
    }

    @Test
    fun `dolu tahtada tek satir temizlemek ALL CLEAR degildir`() {
        val board = MutableList(gridSize * gridSize) { 1 }
        assertFalse(willBoardBeAllClear(board, rowIndices(0)))
    }

    @Test
    fun `bonus varsayilan modda taban degerdir`() {
        assertEquals(ALL_CLEAR_BONUS, allClearBonusFor(1f))
    }

    @Test
    fun `bonus Pro Mode carpanina tabidir`() {
        // Pro Mode carpani 1.5f (bkz. LevelGenerator.generateProLevel).
        assertEquals((ALL_CLEAR_BONUS * 1.5f).toInt(), allClearBonusFor(1.5f))
    }

    @Test
    fun `bonus bolum dengesini tek hamlede bozacak kadar buyuk degildir`() {
        // Seviye 1 hedefi 100 (LevelGenerator.targetScoreForLevel = 100 + (n-1)*5).
        // ALL CLEAR bonusu TEK BASINA bu hedefi gecmemeli — gecseydi oyuncu ilk
        // bolumu tek bir hamlede kapatabilirdi.
        assertTrue(allClearBonusFor(1f) < 100)
        // Ayni zamanda anlamsiz derecede kucuk de olmamali: en az bir tam
        // satirin (gridSize = 8 puan) birkac katı olmali ki "jackpot" hissetsin.
        assertTrue(allClearBonusFor(1f) > gridSize * 4)
    }
}
