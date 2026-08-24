package com.miniappfactory.boomblocks.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

    @Test
    fun `level 1 has base target score and easiest shape pool`() {
        val def = LevelGenerator.forLevel(1)
        assertEquals(1, def.number)
        // Faz 110b: Career Mode Level 1, target score = 100 + (1-1)*5 = 100
        assertEquals(100, def.targetScore)
        assertEquals(1, def.shapePoolTier)
    }

    @Test
    fun `target score increases by expected increments per level`() {
        // Faz 110b: Puan şartları 100 + (n-1)*5 — +5/level artışı
        assertEquals(100, LevelGenerator.forLevel(1).targetScore)
        assertEquals(105, LevelGenerator.forLevel(2).targetScore)
        assertEquals(5, LevelGenerator.forLevel(3).targetScore - LevelGenerator.forLevel(2).targetScore)
        assertEquals(5, LevelGenerator.forLevel(50).targetScore - LevelGenerator.forLevel(49).targetScore)
    }

    @Test
    fun `shape pool tier escalates at expected thresholds`() {
        assertEquals(1, LevelGenerator.forLevel(3).shapePoolTier)
        assertEquals(2, LevelGenerator.forLevel(4).shapePoolTier)
        assertEquals(2, LevelGenerator.forLevel(8).shapePoolTier)
        assertEquals(3, LevelGenerator.forLevel(9).shapePoolTier)
    }

    @Test
    fun `non-positive level numbers are coerced to level 1`() {
        val def = LevelGenerator.forLevel(0)
        assertEquals(1, def.number)
        // Faz 110b: Level 1 = 100 (Career Mode puan şartı: 100 + (n-1)*5)
        assertEquals(100, def.targetScore)
    }


    @Test
    fun `challenge levels have a steeper curve and a score multiplier above 1`() {
        val level = LevelGenerator.forLevel(5)
        val challenge = LevelGenerator.forChallengeLevel(5)
        assertEquals(1f, level.scoreMultiplier)
        assertTrue("challenge multiplier must exceed normal level multiplier", challenge.scoreMultiplier > level.scoreMultiplier)
        // Faz 151: Pro hedef egrisi 200 + (n-1)*20; Faz 151b'de L40'tan sonra
        // adim +10'a dusuyor (yumusak tavan). Eski egrideki 10->11 kirilmasi
        // (50 -> 5) yok.
        assertEquals(200, LevelGenerator.forChallengeLevel(1).targetScore)
        assertEquals(380, LevelGenerator.forChallengeLevel(10).targetScore)
        assertEquals(980, LevelGenerator.forChallengeLevel(40).targetScore)
        assertEquals(1080, LevelGenerator.forChallengeLevel(50).targetScore)
        assertEquals(1580, LevelGenerator.forChallengeLevel(100).targetScore)
    }

    @Test
    fun `pro target grows by 20 up to the soft cap and by 10 after it`() {
        val cap = LevelGenerator.PRO_TARGET_SOFT_CAP_LEVEL
        for (n in 1 until cap) {
            assertEquals(
                "Pro target growth must be +${LevelGenerator.PRO_TARGET_STEP} at level $n",
                LevelGenerator.PRO_TARGET_STEP,
                LevelGenerator.forChallengeLevel(n + 1).targetScore - LevelGenerator.forChallengeLevel(n).targetScore
            )
        }
        for (n in cap..(cap + 60)) {
            assertEquals(
                "Pro target growth must be +${LevelGenerator.PRO_TARGET_STEP_AFTER_CAP} at level $n",
                LevelGenerator.PRO_TARGET_STEP_AFTER_CAP,
                LevelGenerator.forChallengeLevel(n + 1).targetScore - LevelGenerator.forChallengeLevel(n).targetScore
            )
        }
    }

    @Test
    fun `pro target never stops growing and never drops`() {
        // Yumusak tavan bir TAVAN degil, egim dususu: egri her seviyede artmali.
        for (n in 1..200) {
            val prev = LevelGenerator.forChallengeLevel(n).targetScore
            val next = LevelGenerator.forChallengeLevel(n + 1).targetScore
            assertTrue("Pro target must strictly increase at level $n", next > prev)
        }
    }

    // --- Faz 128: Comfort Mode (TR "KOLAY MOD") ---

    @Test
    fun `comfort levels start at 100 and grow by exactly one point`() {
        assertEquals(100, LevelGenerator.forComfortLevel(1).targetScore)
        assertEquals(101, LevelGenerator.forComfortLevel(2).targetScore)
        assertEquals(1, LevelGenerator.forComfortLevel(3).targetScore - LevelGenerator.forComfortLevel(2).targetScore)
        // Bolum 100'de hedef hala sadece 199 — Kariyer'de ayni bolumde 595 olurdu.
        assertEquals(199, LevelGenerator.forComfortLevel(100).targetScore)
    }

    @Test
    fun `comfort curve is far gentler than career at the same level`() {
        val career = LevelGenerator.forLevel(40).targetScore
        val comfort = LevelGenerator.forComfortLevel(40).targetScore
        assertTrue("comfort hedefi kariyerden dusuk olmali", comfort < career)
        // Ayni baslangic noktasi, besde bir egim: fark tam olarak (n-1)*4
        assertEquals((40 - 1) * 4, career - comfort)
    }

    @Test
    fun `comfort has no score multiplier and non-positive levels are coerced`() {
        assertEquals(1f, LevelGenerator.forComfortLevel(7).scoreMultiplier)
        val def = LevelGenerator.forComfortLevel(0)
        assertEquals(1, def.number)
        assertEquals(100, def.targetScore)
    }

    @Test
    fun `comfort shows no interstitial for the first three levels then every level`() {
        // Kullanici karari: ilk 3 bolum reklamsiz, 4. bolumden itibaren her gecis.
        assertEquals(false, LevelGenerator.shouldShowInterstitialAfterComfortLevel(1))
        assertEquals(false, LevelGenerator.shouldShowInterstitialAfterComfortLevel(2))
        assertEquals(false, LevelGenerator.shouldShowInterstitialAfterComfortLevel(3))
        assertEquals(true, LevelGenerator.shouldShowInterstitialAfterComfortLevel(4))
        assertEquals(true, LevelGenerator.shouldShowInterstitialAfterComfortLevel(5))
        assertEquals(true, LevelGenerator.shouldShowInterstitialAfterComfortLevel(120))
    }
}
