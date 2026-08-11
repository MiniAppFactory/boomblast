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
    fun `target score increment matches the tiered curve`() {
        // levels 2-6: +40/level
        assertEquals(40, LevelGenerator.forLevel(3).targetScore - LevelGenerator.forLevel(2).targetScore)
        // levels 7-15: +10/level (Faz 61 - onceden 20'ydi)
        assertEquals(10, LevelGenerator.forLevel(8).targetScore - LevelGenerator.forLevel(7).targetScore)
        // levels 16-30: +8/level
        assertEquals(8, LevelGenerator.forLevel(20).targetScore - LevelGenerator.forLevel(19).targetScore)
        // levels 31+: +2/level
        assertEquals(2, LevelGenerator.forLevel(35).targetScore - LevelGenerator.forLevel(34).targetScore)
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
