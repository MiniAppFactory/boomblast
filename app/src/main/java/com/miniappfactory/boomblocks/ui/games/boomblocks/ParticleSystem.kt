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
     * Render döngüsünde cizilecek aktif parçacıkları döndür.
     */
    fun getActiveParticles(): List<BlastParticle> = particles.slice(0 until activeCount)

    /**
     * Aktif parçacık sayısını döndür (Canvas loop'ta kullanılır).
     */
    fun getActiveCount(): Int = activeCount

    /**
     * Index ile parçacık erişimi (Canvas loop'ta kullanılır).
     */
    operator fun get(index: Int): BlastParticle = particles[index]

    /**
     * Physics güncelleme — her karede bir kez çağrılır.
     * Hücre birimi + yerçekim simülasyonu.
     *
     * @param deltaTimeMs kare süresi (millisaniye)
     */
    fun update(deltaTimeMs: Float) {
        val deltaS = deltaTimeMs / 1000f
        particles.slice(0 until activeCount).forEach { p ->
            if (!p.active) return@forEach

            // Yaş artır; maxAge aşılırsa deaktif et
            p.life -= deltaS
            if (p.life <= 0f) {
                p.active = false
                return@forEach
            }

            // Yerçekim + hava direnci
            p.vy += PARTICLE_GRAVITY * deltaS  // aşağı ivme
            p.vx *= 0.96f  // hava direnci (her frame'de %4 azalma)

            // Konum güncelle
            p.x += p.vx * deltaS
            p.y += p.vy * deltaS

            // Dönme
            p.rot += p.spin * deltaS
        }
    }

    /**
     * Havuzun doldurulma yüzdesini döndür (debug için).
     */
    fun getUtilization(): Float = activeCount.toFloat() / particles.size
}

/**
 * Faz 5: Floating Score — "+N" animasyonu.
 *
 * Hücre temizlendiğinde merkez noktasından başlayarak "+10/50/100" yazıları
 * yukarı uçuyor, 1 saniye içinde solup kayboluyor. Kombo bonusu ("+x2", "+x3")
 * farklı renk ve ayrı path ile.
 */
data class FloatingScore(
    val x: Float,           // hücre X koordinatı
    val y: Float,           // hücre Y koordinatı
    val text: String,       // "+50", "+x3", vb.
    val color: Color,       // renk (puanlara göre)
    var age: Float = 0f,    // geçen zaman (ms)
    val maxAge: Float = 1000f  // 1 saniye
) {
    fun isAlive(): Boolean = age < maxAge

    fun getAlpha(): Float {
        // Son 200ms fade-out
        if (age < maxAge - 200f) return 1f
        return (maxAge - age) / 200f
    }

    fun getOffset(): Float {
        // Ease-out: hızlı başla, yavaşla
        val progress = age / maxAge
        return -progress * 100f  // 100 hücre yukarı
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
