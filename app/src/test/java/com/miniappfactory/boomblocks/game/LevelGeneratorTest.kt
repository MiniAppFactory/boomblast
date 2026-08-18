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
        // Challenge target growth (Level 1-10): +50/level (250, 300, 350, ...)
        val challengeGrowth = LevelGenerator.forChallengeLevel(6).targetScore - LevelGenerator.forChallengeLevel(5).targetScore
        assertEquals(50, challengeGrowth)
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
