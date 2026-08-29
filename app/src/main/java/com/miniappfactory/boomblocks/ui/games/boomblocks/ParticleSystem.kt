package com.miniappfactory.boomblocks.ui.games.boomblocks

import androidx.compose.ui.graphics.Color

// Faz 5: ParticlePool güncelleme — BlastParticle boomblocks oyun dosyasında tanımlı,
// burada pool yönetimini yapıyoruz.

/**
 * Faz 5: ParticlePool — garbage collection engelleme.
 *
 * Parçacık havuzu (object pool) pattern'i. Oyun döngüsünde her patlama 50-100
 * parçacık emit edebilir. `data class` + `List.map` yapısıyla her karede yeni
 * nesne ayrılması GC duraklaması ve jank'a neden oluyor.
 *
 * Çözüm: sabit boyutlu havuz (200 parçacık), `acquire()` havuzu boşalt, physics
 * loop'ta state'i güncelle, render döngüsünde parçacık listesini çiz.
 *
 * **Birim:** tüm konum/hız/boyut değerleri HÜCREdir (1.0 = bir grid hücresi).
 * Piksel değil, çünkü DrawScope'ta 3.0 yogunluklu cihazda (S8) piksel = 1/3 dp
 * eder — parçacıklar görünmezdi. Hücre birimi tablet/telefon ve banner kaymalarında
 * tutarlı hissettiriyor.
 */

// ObjectPool: maksimum eşzamanlı parçacık sayısı
private const val MAX_POOL_PARTICLES = 200

class ParticlePool {
    private val particles = Array(MAX_POOL_PARTICLES) { BlastParticle() }
    private var activeCount = 0

    /**
     * Havuzdan boş bir parçacık al. Burada "boş" = state atanmamış, render
     * döngüsüne henüz dahil değil.
     */
    fun acquire(): BlastParticle? {
        if (activeCount >= particles.size) return null
        return particles[activeCount++].also { it.active = true }
    }

    /**
     * Tüm parçacıkları temizle. Yeni bölüm başladığında veya clear animasyonu
     * tamamlandığında çağrılır.
     */
    fun reset() {
        activeCount = 0
        particles.forEach { it.active = false }
    }

    /**
     * Aktif parçacık sayısını döndür (Canvas loop'ta kullanılır).
     */
    fun getActiveCount(): Int = activeCount

    /**
     * Index ile parçacık erişimi (Canvas loop'ta kullanılır).
     */
    operator fun get(index: Int): BlastParticle = particles[index]

    // Faz 166 — KALDIRILDI: `update(deltaTimeMs)`.
    //
    // Ayni parcacigi IKI bagimsiz integrator suruyordu:
    //
    //   1) burasi, her karede p.x/p.y/p.vx/p.vy/p.rot/p.life'i ILERLETIYORDU;
    //   2) BoomBlocksGame'deki cizim katmani ise ayni alanlari BASLANGIC degeri
    //      kabul edip `lt` (parcacigin kendi gecen suresi) ile kapali formda
    //      YENIDEN entegre ediyordu:
    //        cx = p.x + p.vx * lt * (1 - 0.30f)
    //        cy = p.y + p.vy * lt + 0.5f * g * lt * lt
    //
    // Sonuclari (denetimde dogrulandi):
    //   - OMUR TAM YARIYA iniyordu: burasi `p.life`i azaltirken cizim onu sabit
    //     toplam omur sanip `lt >= p.life` ile kesiyordu; ikisi ortada bulusuyor.
    //   - Dikey hareket ~2x, donus 2x.
    //   - `p.vx *= 0.96f` KARE BASINA oldugu icin sacilma EKRAN TAZELEME HIZINA
    //     bagliydi: 120Hz'te 60Hz'in iki kati sonuyordu, yani ayni patlama S22
    //     Ultra'da ve eski bir cihazda farkli goruunuyordu.
    //
    // Tek sahip olarak CIZIM secildi, cunku modeli zaten daha zengin ve dogru:
    // parcacik basina `delay`, ucgen olcek rampasi, kapali-form yercekimi ve
    // surtunme iceriyor; `particleProgress` (Animatable) tabanli oldugu icin de
    // kare hizindan BAGIMSIZ. Havuz artik yalnizca depolama: her karede
    // `particles.slice(...)` ile liste tahsis etmiyor.

    /**
     * Havuzun doldurulma yüzdesini döndür (debug için).
     */
    fun getUtilization(): Float = activeCount.toFloat() / particles.size
}

/**
 * Faz 5/111: Floating Score — "+N" animasyonu.
 *
 * Hücre temizlendiğinde merkez noktasından başlayarak "+10/50/100" yazıları
 * SKOR kartına doğru uçuyor, 1 saniye içinde solup kayboluyor. Kombo bonusu
 * ("+x2", "+x3") farklı renk ve ayrı path ile.
 *
 * Faz 111: Mutable targetX/targetY — animasyon başlangıç noktasından (grid cell)
 * hedef (SKOR card merkez) konumuna bezier eğrisi boyunca hareket eder.
 * Hedef koordinatlar render döngüsünde set edilir.
 */
class FloatingScore(
    val x: Float,           // başlangıç X koordinatı (hücre)
    val y: Float,           // başlangıç Y koordinatı (hücre)
    val text: String,       // "+50", "+x3", vb.
    val color: Color,       // renk (puanlara göre)
    var age: Float = 0f,    // geçen zaman (ms)
    val maxAge: Float = 1000f  // 1 saniye
) {
    var targetX: Float = x    // hedef X (SKOR card merkez, hücre)
    var targetY: Float = y    // hedef Y (SKOR card merkez, hücre)

    fun isAlive(): Boolean = age < maxAge

    fun getAlpha(): Float {
        // Son 200ms fade-out
        if (age < maxAge - 200f) return 1f
        return (maxAge - age) / 200f
    }

    fun getEaseProgress(): Float {
        // Ease-out-cubic: hızlı başla, yavaşla
        val progress = age / maxAge
        return 1f - (1f - progress) * (1f - progress) * (1f - progress)
    }

    fun getPositionAtTime(): Pair<Float, Float> {
        // Faz 111: bezier interpolasyon — başlangıçtan hedefe doğru hareket
        val easeProgress = getEaseProgress()

        // Lineer interpolasyon başlangıç ve hedef arasında
        val currentX = x + (targetX - x) * easeProgress
        val currentY = y + (targetY - y) * easeProgress

        return Pair(currentX, currentY)
    }

    fun getOffset(): Float {
        // Eski sadece dikey uçuş (geriye uyumluluk)
        val progress = age / maxAge
        return -progress * 100f  // 100 hücre yukarı (kullanılmıyor, getPositionAtTime kullan)
    }

    fun update(deltaTimeMs: Float) {
        age += deltaTimeMs
    }
}

/**
 * Oyun mantığı hook'ları. BoomBlocksGame.kt'de `recordClear` ve `recordCombo`
 * çağrıldığında bu sınıf şu verileri emit eder.
 */
class FloatingScoreManager {
    private val scores = mutableListOf<FloatingScore>()

    fun recordClear(value: Int, x: Float, y: Float) {
        val (color, text) = when {
            value >= 100 -> Color(0xFFFF1744) to "+$value"  // kırmızı
            value >= 50 -> Color(0xFFFF9800) to "+$value"   // turuncu
            else -> Color(0xFFFDD835) to "+$value"          // sarı
        }
        scores.add(FloatingScore(x, y, text, color))
    }

    fun recordCombo(multiplier: Int, x: Float, y: Float) {
        val color = when (multiplier) {
            2 -> Color(0xFF4CAF50)  // yeşil
            3 -> Color(0xFFE91E63)  // pembe
            else -> Color(0xFF2196F3)  // mavi
        }
        scores.add(FloatingScore(x, y, "+x$multiplier", color))
    }

    /**
     * Faz 111: Tüm FloatingScore'lara hedef koordinatı set et.
     * Çağrı zamanı: render döngüsünde, SKOR card pozisyonu belirlendikten sonra.
     */
    fun setTargetForAll(targetX: Float, targetY: Float) {
        scores.forEach { score ->
            score.targetX = targetX
            score.targetY = targetY
        }
    }

    fun getAliveScores(): List<FloatingScore> {
        return scores.filter { it.isAlive() }
    }

    fun update(deltaTimeMs: Float) {
        scores.forEach { it.update(deltaTimeMs) }
        scores.removeAll { !it.isAlive() }
    }

    fun clear() {
        scores.clear()
    }
}
