package com.miniappfactory.boomblocks.ui.games.boomblocks

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

/**
 * Faz 2: Tahta (grid) geometrisinin parametrik temsili. Koordinat sistemi,
 * hücre boyutlandırması, ekran-grid dönüşümleri ve fizik kuralları (near-miss,
 * shake, glow) burada tanımlanır.
 *
 * Hedef: Faz 3'te Canvas.drawPath döngüsü SADECE sayıları okuyacak; tüm
 * geometri mantığı bu veri yapısında.
 */
data class BoardGeometry(
    /**
     * Tahta genişliği ve yüksekliği (hücre sayısı). Tipik: 8×8.
     */
    val gridRows: Int,
    val gridCols: Int,

    /**
     * Bir hücrenin piksel boyutu. Sürükle-bırak hesaplamalarında kritik.
     * Ekran density'si ve safe inset'ler dikkate alınarak hesaplanır.
     */
    val gridCellSizePx: Float,

    /**
     * Tahta başlangıç konumu (sol üst köşe), ekran piksellerinde.
     * notch, gesture bar vb. safe inset'ler bu konuma yansır.
     */
    val gridStartXPx: Float,
    val gridStartYPx: Float,

    /**
     * Tahta toplam boyutları, piksellerinde.
     * gridTotalWidthPx = gridCols * gridCellSizePx
     * gridTotalHeightPx = gridRows * gridCellSizePx
     */
    val gridTotalWidthPx: Float,
    val gridTotalHeightPx: Float,

    /**
     * Near-miss (bir hücre eksik satır/sütun) nabzı çapı, piksellerinde.
     * Tipik: 12-24px, banner yüksekliğiyle ölçeklenebilir.
     */
    val nearMissRadiusPx: Float,

    /**
     * Shake (sarsıntı) genliği sınırları. Trauma² eğrisinden türetilen
     * amplitude, bu aralıkta kalmalı.
     * Tipik: min=7.8px, max=20px
     */
    val shakeMinAmplitudePx: Float,
    val shakeMaxAmplitudePx: Float
) {
    /**
     * Ekran piksel konumunu grid (row, col) hücresine çevir.
     * Out-of-bounds → null dönüş.
     *
     * Koordinat sistemi: Hücre (r, c) screen X aralığında [gridStartXPx + c*cellSize, gridStartXPx + (c+1)*cellSize)
     * ve Y aralığında [gridStartYPx + r*cellSize, gridStartYPx + (r+1)*cellSize) kaplar.
     * screenToCell basitçe floor(localX / cellSize) ve floor(localY / cellSize) yapıyor.
     * (Drag preview'da +0.5f kullanıldığı görülebilir — ama burası pure geometric conversion.)
     */
    fun screenToCell(screenXPx: Float, screenYPx: Float): Pair<Int, Int>? {
        val localX = screenXPx - gridStartXPx
        val localY = screenYPx - gridStartYPx

        if (localX < 0f || localY < 0f || localX >= gridTotalWidthPx || localY >= gridTotalHeightPx) {
            return null
        }

        val row = (localY / gridCellSizePx).toInt()
        val col = (localX / gridCellSizePx).toInt()

        // Bounds kontrol (redundant ama defensif)
        if (row !in 0 until gridRows || col !in 0 until gridCols) {
            return null
        }

        return row to col
    }

    /**
     * Grid (row, col) hücresini ekran piksel konumuna çevir.
     * Hücrenin SOL ÜST köşesinin konumu döner (hücrenin başlangıcı).
     */
    fun cellToScreenTopLeft(row: Int, col: Int): Pair<Float, Float>? {
        if (row !in 0 until gridRows || col !in 0 until gridCols) {
            return null
        }
        val screenX = gridStartXPx + col * gridCellSizePx
        val screenY = gridStartYPx + row * gridCellSizePx
        return screenX to screenY
    }

    /**
     * Grid (row, col) hücresinin MERKEZİ ekran piksel konumu.
     */
    fun cellToScreenCenter(row: Int, col: Int): Pair<Float, Float>? {
        val (topX, topY) = cellToScreenTopLeft(row, col) ?: return null
        val centerX = topX + gridCellSizePx / 2f
        val centerY = topY + gridCellSizePx / 2f
        return centerX to centerY
    }

    /**
     * Tahta toplam boyutları (pixel olarak), padding dahil.
     */
    val boundingBoxRightPx: Float
        get() = gridStartXPx + gridTotalWidthPx

    val boundingBoxBottomPx: Float
        get() = gridStartYPx + gridTotalHeightPx

    /**
     * Tahta grid'inin ekran dışında kalıp kalmadığını kontrol et.
     * (örn. banner reklam yüklenmesiyle tahta küçüldü mü?)
     */
    fun isCompletelyVisible(): Boolean {
        return gridStartXPx >= 0f && gridStartYPx >= 0f &&
               boundingBoxRightPx >= gridTotalWidthPx &&
               boundingBoxBottomPx >= gridTotalHeightPx
    }
}

/**
 * BoardGeometry oluşturmacı factory fonksiyonu.
 *
 * @param screenWidthPx Ekran genişliği
 * @param screenHeightPx Ekran yüksekliği
 * @param headerHeightPx Header/banner yüksekliği (reklam dahil)
 * @param gridRowCount Grid satır sayısı (tipik: 8)
 * @param gridColCount Grid sütun sayısı (tipik: 8)
 * @param horizontalPaddingPx Ekranın sol/sağ tarafındaki padding
 * @param safeInsetTop Notch/gesture bar üst inset
 * @param safeInsetBottom Gesture bar alt inset
 */
fun createBoardGeometry(
    screenWidthPx: Float,
    screenHeightPx: Float,
    headerHeightPx: Float,
    gridRowCount: Int = 8,
    gridColCount: Int = 8,
    horizontalPaddingPx: Float = 8f,
    safeInsetTop: Float = 0f,
    safeInsetBottom: Float = 0f
): BoardGeometry {
    // Tahta için kullanılabilir alan: header altından gesture bar üstüne kadar
    val availableTopPx = headerHeightPx + safeInsetTop
    val availableHeightPx = screenHeightPx - availableTopPx - safeInsetBottom

    // Tahta için yatay alan: padding'i göz önüne alarak
    val availableWidthPx = screenWidthPx - 2 * horizontalPaddingPx

    // Hücre boyutu: square grid, mevcut alana sığan en büyük boyut
    val maxCellSizeByWidth = availableWidthPx / gridColCount
    val maxCellSizeByHeight = availableHeightPx / gridRowCount
    val cellSizePx = minOf(maxCellSizeByWidth, maxCellSizeByHeight)

    // Tahta toplam boyutu
    val gridTotalWidthPx = cellSizePx * gridColCount
    val gridTotalHeightPx = cellSizePx * gridRowCount

    // Tahta konumu (merkezlenmiş yatay, header altında yerleştirilmiş dikey)
    val gridStartXPx = (screenWidthPx - gridTotalWidthPx) / 2f
    val gridStartYPx = availableTopPx + (availableHeightPx - gridTotalHeightPx) / 2f

    // Near-miss radius: hücre boyutunun ~1-1.5 katı, ama piksel cinsinden
    // Tipik: 40x40 hücre (~360px) → radius ~15px; 25x25 (~225px) → radius ~10px
    val nearMissRadiusPx = (cellSizePx * 0.4f).coerceIn(8f, 24f)

    // Shake amplitude sınırları: trauma² eğrisinden, hücre boyutunun türevi
    val shakeMinAmplitudePx = cellSizePx * 0.15f  // ~7.8px for 52px cell
    val shakeMaxAmplitudePx = cellSizePx * 0.38f  // ~20px for 52px cell

    return BoardGeometry(
        gridRows = gridRowCount,
        gridCols = gridColCount,
        gridCellSizePx = cellSizePx,
        gridStartXPx = gridStartXPx,
        gridStartYPx = gridStartYPx,
        gridTotalWidthPx = gridTotalWidthPx,
        gridTotalHeightPx = gridTotalHeightPx,
        nearMissRadiusPx = nearMissRadiusPx,
        shakeMinAmplitudePx = shakeMinAmplitudePx,
        shakeMaxAmplitudePx = shakeMaxAmplitudePx
    )
}
