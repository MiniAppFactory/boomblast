package com.example.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGeneratorTest {

    @Test
    fun `level 1 has base target score and easiest shape pool`() {
        val def = LevelGenerator.forLevel(1)
        assertEquals(1, def.number)
        assertEquals(500, def.targetScore)
        assertEquals(1, def.shapePoolTier)
    }

    @Test
    fun `target score increases by 250 per level`() {
        val level5 = LevelGenerator.forLevel(5)
        val level6 = LevelGenerator.forLevel(6)
        assertEquals(250, level6.targetScore - level5.targetScore)
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
        assertEquals(500, def.targetScore)
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
