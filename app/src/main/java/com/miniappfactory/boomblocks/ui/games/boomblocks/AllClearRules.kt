package com.miniappfactory.boomblocks.ui.games.boomblocks

import com.miniappfactory.boomblocks.data.ALL_CLEAR_BONUS
import kotlin.math.roundToInt

/**
 * Faz 161 (madde 3): ALL CLEAR kurallari.
 *
 * Bu iki fonksiyon BILEREK saf (pure) ve Compose'dan bagimsiz. Sebep: ALL CLEAR
 * oyunun TEK yeni skor kaynagi ve bir skor kaynagi "cihazda denedim, guzel
 * gorunuyor" ile dogrulanamaz. Mantik `BoomBlocksGame` composable'inin icinde
 * kalsaydi hicbir birim testi ona ulasamazdi (dosyanin geri kalani icin durum
 * zaten bu). Burada durunca `AllClearRulesTest` her ikisini de dogrudan test
 * edebiliyor.
 */

/**
 * Bu hamleden sonra tahta TAMAMEN bosalacak mi?
 *
 * Kritik nokta: cagri ani, hucrelerin GERCEKTEN sifirlanmasindan ONCEdir —
 * temizleme, patlama animasyonu icin ~150-210ms geciken bir coroutine'de olur.
 * Ama sonucu simdiden biliyoruz: dolu kalan tek bir hucre bile temizlenecekler
 * kumesinin DISINDA degilse tahta bosalacak demektir.
 *
 * Bunu erken bilmek zorunlu, cunku bonus puan `placeShape` govdesinde SENKRON
 * eklenmeli: seviye tamamlama kontrolu (`score >= targetScore`) orada calisiyor.
 * Bonusu coroutine'e ertelemek, ALL CLEAR ile hedefi gecen oyuncunun bolumu
 * O hamlede bitirememesine yol acardi.
 *
 * @param board satir-oncelikli tahta; 0 = bos, >0 = blok rengi indeksi
 * @param clearedIndices bu hamlede temizlenecek hucrelerin duz indeksleri
 */
fun willBoardBeAllClear(board: List<Int>, clearedIndices: Set<Int>): Boolean {
    // Hicbir sey temizlenmiyorsa ALL CLEAR de olamaz. (Cagri noktasi zaten
    // `totalLinesCleared > 0` dalinda ama fonksiyon tek basina da dogru olmali:
    // bos bir tahtaya "hicbir sey temizlenmedi" deyip true donmek yaniltici olurdu.)
    if (clearedIndices.isEmpty()) return false
    for (i in board.indices) {
        if (board[i] != 0 && i !in clearedIndices) return false
    }
    return true
}

/**
 * ALL CLEAR bonusunun bu moddaki puan karsiligi.
 *
 * Taban deger `ALL_CLEAR_BONUS` (bkz. GameProgress.kt: 1 hucre = 1 puan
 * olceginde 8x8 tahtanin tam degeri = 64). Carpan, oyunun DIGER TUM puan
 * kaynaklariyla ayni desende uygulanir — Pro Mode 1.5x, Seviyeli/Sonsuz 1.0x.
 * Yuvarlama da ayni: `roundToInt` (yerlestirme ve satir bonusuyla birebir).
 */
fun allClearBonusFor(scoreMultiplier: Float): Int =
    (ALL_CLEAR_BONUS * scoreMultiplier).roundToInt()
