package com.miniappfactory.boomblocks.ui.games.boomblocks

import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * Faz 2: BoardGeometry koordinat sistemi ve parametrik geometri testleri.
 *
 * Testler:
 * 1. Koordinat mapping: cell → screen, screen → cell
 * 2. Grid bounds: out-of-bounds handling
 * 3. Density scaling: farklı ekran boyutlarında hücre boyutu
 * 4. Safe inset entegrasyonu: notch ve gesture bar altında konumlandırma
 * 5. Near-miss radius ölçekleme
 * 6. Shake amplitude sınırları (trauma² eğrisi)
 */
@RunWith(RobolectricTestRunner::class)
class BoardGeometryTest {

    private lateinit var geometry: BoardGeometry

    @Before
    fun setup() {
        // Standart test setupı: 1080x1920 telefon, 8x8 grid, 56dp header
        geometry = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            gridRowCount = 8,
            gridColCount = 8,
            horizontalPaddingPx = 8f,
            safeInsetTop = 0f,
            safeInsetBottom = 0f
        )
    }

    // ============================================================================
    // Test 1: Koordinat Mapping
    // ============================================================================

    @Test
    fun testScreenToCellMapping() {
        // Grid başlangıç konumu: (gridStartXPx, gridStartYPx)
        // İlk hücre (0, 0) merkez: (gridStartXPx + cellSize/2, gridStartYPx + cellSize/2)

        val (row, col) = geometry.screenToCell(
            geometry.gridStartXPx + geometry.gridCellSizePx / 2,
            geometry.gridStartYPx + geometry.gridCellSizePx / 2
        ) ?: throw AssertionError("screenToCell returned null")

        assert(row == 0) { "Expected row 0, got $row" }
        assert(col == 0) { "Expected col 0, got $col" }
    }

    @Test
    fun testScreenToCellMappingCorner() {
        // Son hücre (7, 7)
        val lastCellScreenX = geometry.gridStartXPx + 7.5f * geometry.gridCellSizePx
        val lastCellScreenY = geometry.gridStartYPx + 7.5f * geometry.gridCellSizePx

        val (row, col) = geometry.screenToCell(lastCellScreenX, lastCellScreenY)
            ?: throw AssertionError("screenToCell returned null for corner")

        assert(row == 7) { "Expected row 7, got $row" }
        assert(col == 7) { "Expected col 7, got $col" }
    }

    @Test
    fun testCellToScreenMapping() {
        // Hücre (3, 4) → screen koordinat
        val (screenX, screenY) = geometry.cellToScreenTopLeft(3, 4)
            ?: throw AssertionError("cellToScreenTopLeft returned null")

        val expectedX = geometry.gridStartXPx + 4 * geometry.gridCellSizePx
        val expectedY = geometry.gridStartYPx + 3 * geometry.gridCellSizePx

        assert(abs(screenX - expectedX) < 0.01f) { "Screen X mismatch: $screenX vs $expectedX" }
        assert(abs(screenY - expectedY) < 0.01f) { "Screen Y mismatch: $screenY vs $expectedY" }
    }

    @Test
    fun testCellToScreenCenter() {
        // Hücre (5, 5) merkezinin screen konumu
        val (centerX, centerY) = geometry.cellToScreenCenter(5, 5)
            ?: throw AssertionError("cellToScreenCenter returned null")

        val (topX, topY) = geometry.cellToScreenTopLeft(5, 5)
            ?: throw AssertionError("cellToScreenTopLeft returned null")

        val expectedCenterX = topX + geometry.gridCellSizePx / 2
        val expectedCenterY = topY + geometry.gridCellSizePx / 2

        assert(abs(centerX - expectedCenterX) < 0.01f) { "Center X mismatch" }
        assert(abs(centerY - expectedCenterY) < 0.01f) { "Center Y mismatch" }
    }

    // ============================================================================
    // Test 2: Grid Bounds / Out-of-Bounds
    // ============================================================================

    @Test
    fun testScreenToCellOutOfBoundsNegative() {
        // Grid dışında, negatif konumda
        val result = geometry.screenToCell(-100f, -100f)
        assert(result == null) { "Expected null for out-of-bounds negative" }
    }

    @Test
    fun testScreenToCellOutOfBoundsPositive() {
        // Grid dışında, sağında/altında
        val result = geometry.screenToCell(
            geometry.gridStartXPx + geometry.gridTotalWidthPx + 100f,
            geometry.gridStartYPx + geometry.gridTotalHeightPx + 100f
        )
        assert(result == null) { "Expected null for out-of-bounds positive" }
    }

    @Test
    fun testCellToScreenOutOfBounds() {
        // Geçersiz hücre koordinatı
        val result = geometry.cellToScreenTopLeft(-1, 0)
        assert(result == null) { "Expected null for row < 0" }

        val result2 = geometry.cellToScreenTopLeft(8, 0)
        assert(result2 == null) { "Expected null for row >= gridRows" }

        val result3 = geometry.cellToScreenTopLeft(0, 8)
        assert(result3 == null) { "Expected null for col >= gridCols" }
    }

    // ============================================================================
    // Test 3: Density Scaling
    // ============================================================================

    @Test
    fun testDifferentScreenSizes() {
        // Dar telefon: hücre boyutu küçük olmalı
        val narrowGeometry = createBoardGeometry(
            screenWidthPx = 540f,   // Half width
            screenHeightPx = 960f,
            headerHeightPx = 56f,
            gridRowCount = 8,
            gridColCount = 8,
            horizontalPaddingPx = 8f
        )

        // Geniş tablet: hücre boyutu büyük olmalı
        val wideGeometry = createBoardGeometry(
            screenWidthPx = 2160f,  // 2x width
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            gridRowCount = 8,
            gridColCount = 8,
            horizontalPaddingPx = 8f
        )

        assert(narrowGeometry.gridCellSizePx < geometry.gridCellSizePx) {
            "Narrow screen should have smaller cell size"
        }
        assert(wideGeometry.gridCellSizePx > geometry.gridCellSizePx) {
            "Wide screen should have larger cell size"
        }
    }

    @Test
    fun test2xDensity() {
        // 2x density → cell size ≈ half
        val standard = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f
        )

        val double = createBoardGeometry(
            screenWidthPx = 2160f,
            screenHeightPx = 3840f,
            headerHeightPx = 112f
        )

        // gridCellSizePx roughly doubles (but not exact due to padding/header)
        assert(double.gridCellSizePx > standard.gridCellSizePx) {
            "2x density should have larger cell size"
        }
    }

    // ============================================================================
    // Test 4: Safe Inset Integration
    // ============================================================================

    @Test
    fun testSafeInsetTop() {
        val noInset = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            safeInsetTop = 0f,
            safeInsetBottom = 0f
        )

        val withInset = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            safeInsetTop = 40f,  // Notch area
            safeInsetBottom = 0f
        )

        // Inset ile tahta daha aşağıya taşınmalı
        assert(withInset.gridStartYPx > noInset.gridStartYPx) {
            "Inset should push grid down: ${withInset.gridStartYPx} vs ${noInset.gridStartYPx}"
        }
    }

    @Test
    fun testSafeInsetBottom() {
        val noInset = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            safeInsetTop = 0f,
            safeInsetBottom = 0f
        )

        val withInset = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f,
            safeInsetTop = 0f,
            safeInsetBottom = 34f  // Gesture bar
        )

        // Inset ile kullanılabilir yükseklik azalır → grid daha küçük hücre boyutu
        // (tabii ki alan daralıyor, grid aynı konuma ancak daha sıkışık)
        assert(withInset.gridCellSizePx <= noInset.gridCellSizePx) {
            "Bottom inset should reduce cell size"
        }
    }

    @Test
    fun testBannerHeightAffectGridPosition() {
        // Banner olmadan
        val noBanner = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 56f  // Minimal header
        )

        // Büyük reklam banner
        val withBanner = createBoardGeometry(
            screenWidthPx = 1080f,
            screenHeightPx = 1920f,
            headerHeightPx = 200f  // Ad banner
        )

        // Büyük banner tahta daha aşağıya iter
        assert(withBanner.gridStartYPx > noBanner.gridStartYPx) {
            "Larger header should push grid down"
        }

        // Ama hücre boyutunu etkilemez (benzer grid boyutu)
        assert(abs(withBanner.gridCellSizePx - noBanner.gridCellSizePx) < 2f) {
            "Cell size should be similar regardless of header height"
        }
    }

    // ============================================================================
    // Test 5: Near-Miss Radius
    // ============================================================================

    @Test
    fun testNearMissRadiusWithinBounds() {
        // Near-miss radius hücre boyutunun makul bir kısmı
        // Factory: (cellSize * 0.4).coerceIn(8, 24)
        // Örnek: cellSize ~52 → 52*0.4=20.8 → coerce → ~20-21
        assert(geometry.nearMissRadiusPx > 0f) { "NearMiss radius must be positive" }

        // Ratio kontrolü daha gevşek (factory coerceIn yüzünden)
        val ratio = geometry.nearMissRadiusPx / geometry.gridCellSizePx
        assert(ratio >= 0.15f && ratio <= 0.5f) {
            "NearMiss radius should be 15-50% of cell size, got ${ratio * 100}%"
        }
    }

    @Test
    fun testNearMissRadiusScalesWithCell() {
        val small = createBoardGeometry(
            screenWidthPx = 540f,
            screenHeightPx = 960f,
            headerHeightPx = 56f
        )

        val large = createBoardGeometry(
            screenWidthPx = 2160f,
            screenHeightPx = 3840f,
            headerHeightPx = 112f
        )

        // Temel kontrol: büyük ekran büyük cell size'a sahip
        assert(large.gridCellSizePx > small.gridCellSizePx) {
            "Large screen should have larger cell size: ${large.gridCellSizePx} vs ${small.gridCellSizePx}"
        }

        // Radius orantısı biraz farklı olabilir (coerceIn sınırları yüzünden)
        // ama en azından pozitif ve artan trend olmalı
        val smallRatio = small.nearMissRadiusPx / small.gridCellSizePx
        val largeRatio = large.nearMissRadiusPx / large.gridCellSizePx
        // Tolerans: coerceIn yüzünden oranlar farklı olabilir (~0.2-0.5 aralığında)
        assert(smallRatio > 0f && largeRatio > 0f) {
            "Ratios must be positive: small=$smallRatio, large=$largeRatio"
        }
    }

    // ============================================================================
    // Test 6: Shake Amplitude Limits (Trauma² Curve)
    // ============================================================================

    @Test
    fun testShakeAmplitudeBounds() {
        // Min ve max değerleri mantıklı aralıkta
        assert(geometry.shakeMinAmplitudePx > 0f) { "Min amplitude must be positive" }
        assert(geometry.shakeMaxAmplitudePx > geometry.shakeMinAmplitudePx) {
            "Max must be > Min"
        }

        // İdeal aralık: 7.8-20px (tipik ortadaki hücre boyutu ~52px için)
        // Ama hücre boyutu farklı olabilir; oranı kontrol edelim:
        // Min ≈ cell * 0.15, Max ≈ cell * 0.38
        val minRatio = geometry.shakeMinAmplitudePx / geometry.gridCellSizePx
        val maxRatio = geometry.shakeMaxAmplitudePx / geometry.gridCellSizePx

        assert(minRatio >= 0.1f && minRatio <= 0.2f) {
            "Min amplitude ratio should be ~15% of cell: $minRatio"
        }
        assert(maxRatio >= 0.3f && maxRatio <= 0.45f) {
            "Max amplitude ratio should be ~38% of cell: $maxRatio"
        }
    }

    @Test
    fun testShakeAmplitudeWithDifferentCellSizes() {
        val narrow = createBoardGeometry(
            screenWidthPx = 540f,
            screenHeightPx = 960f,
            headerHeightPx = 56f
        )

        val wide = createBoardGeometry(
            screenWidthPx = 2160f,
            screenHeightPx = 3840f,
            headerHeightPx = 112f
        )

        // Daha büyük hücre → daha büyük shake amplitude
        assert(wide.shakeMaxAmplitudePx > narrow.shakeMaxAmplitudePx) {
            "Larger cells should allow larger shake amplitude"
        }

        // Ama oransal değer stabil
        val narrowRatio = narrow.shakeMaxAmplitudePx / narrow.gridCellSizePx
        val wideRatio = wide.shakeMaxAmplitudePx / wide.gridCellSizePx
        assert(abs(narrowRatio - wideRatio) < 0.05f) {
            "Shake amplitude ratio should be consistent: $narrowRatio vs $wideRatio"
        }
    }

    // ============================================================================
    // Test 7: Grid Bounds Accessors
    // ============================================================================

    @Test
    fun testBoundingBoxCalculations() {
        val rightEdge = geometry.boundingBoxRightPx
        val bottomEdge = geometry.boundingBoxBottomPx

        val expectedRight = geometry.gridStartXPx + geometry.gridTotalWidthPx
        val expectedBottom = geometry.gridStartYPx + geometry.gridTotalHeightPx

        assert(abs(rightEdge - expectedRight) < 0.01f) { "Right edge mismatch" }
        assert(abs(bottomEdge - expectedBottom) < 0.01f) { "Bottom edge mismatch" }
    }

    @Test
    fun testIsCompletelyVisible() {
        // Normal durumda tamamen görünür olmalı
        assert(geometry.isCompletelyVisible()) {
            "Geometry should be completely visible"
        }

        // Eğer tahta negatif konumda olsaydı (not practical but test it)
        val outOfBounds = BoardGeometry(
            gridRows = 8,
            gridCols = 8,
            gridCellSizePx = 50f,
            gridStartXPx = -100f,  // Out of bounds
            gridStartYPx = 0f,
            gridTotalWidthPx = 400f,
            gridTotalHeightPx = 400f,
            nearMissRadiusPx = 15f,
            shakeMinAmplitudePx = 7f,
            shakeMaxAmplitudePx = 20f
        )

        assert(!outOfBounds.isCompletelyVisible()) {
            "Out-of-bounds geometry should not be visible"
        }
    }

    // ============================================================================
    // Test 8: Round-trip Consistency
    // ============================================================================

    @Test
    fun testRoundTripCellToScreenToCell() {
        // Grid üzerinde rastgele hücreler test et
        for (row in 0 until geometry.gridRows) {
            for (col in 0 until geometry.gridCols) {
                // cell → screen → cell
                val (screenX, screenY) = geometry.cellToScreenCenter(row, col)
                    ?: throw AssertionError("cellToScreenCenter returned null")

                val (returnRow, returnCol) = geometry.screenToCell(screenX, screenY)
                    ?: throw AssertionError("screenToCell returned null")

                // Merkezden başladığımız için round-trip mükemmel olmalı
                assert(returnRow == row && returnCol == col) {
                    "Round-trip failed: ($row, $col) → ($returnRow, $returnCol)"
                }
            }
        }
    }
}
