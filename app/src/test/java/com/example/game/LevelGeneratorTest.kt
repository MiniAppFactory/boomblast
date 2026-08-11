package com.example.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

    @Test
    fun `level 1 has base target score and easiest shape pool`() {
        val def = LevelGenerator.forLevel(1)
        assertEquals(1, def.number)
        assertEquals(100, def.targetScore)
        assertEquals(1, def.shapePoolTier)
    }

    @Test
    fun `target score increases by a flat 5 points per level`() {
        // Faz 64: kademeli egri terk edildi, artik her yerde sabit +5.
        assertEquals(5, LevelGenerator.forLevel(3).targetScore - LevelGenerator.forLevel(2).targetScore)
        assertEquals(5, LevelGenerator.forLevel(8).targetScore - LevelGenerator.forLevel(7).targetScore)
        assertEquals(5, LevelGenerator.forLevel(20).targetScore - LevelGenerator.forLevel(19).targetScore)
        assertEquals(5, LevelGenerator.forLevel(35).targetScore - LevelGenerator.forLevel(34).targetScore)
        assertEquals(345, LevelGenerator.forLevel(50).targetScore)
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
        assertEquals(100, def.targetScore)
    }

    @Test
    fun `target score never decreases as levels progress`() {
        var previous = LevelGenerator.forLevel(1).targetScore
        for (level in 2..50) {
            val current = LevelGenerator.forLevel(level).targetScore
            assertTrue("level $level target should exceed previous", current > previous)
            previous = current
        }
    }
}
