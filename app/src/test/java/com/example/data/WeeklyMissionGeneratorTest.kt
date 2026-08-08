package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyMissionGeneratorTest {

    @Test
    fun `always returns exactly 4 missions`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W32")
        assertEquals(4, missions.size)
    }

    @Test
    fun `missions have no duplicate ids within a week`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W32")
        assertEquals(missions.size, missions.map { it.id }.toSet().size)
    }

    @Test
    fun `same week id always produces the same mission set`() {
        val first = WeeklyMissionGenerator.forWeek("2026-W32")
        val second = WeeklyMissionGenerator.forWeek("2026-W32")
        assertEquals(first.map { it.id }, second.map { it.id })
    }

    @Test
    fun `every mission has a positive target and reward`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W40")
        missions.forEach { mission ->
            assertTrue("target must be positive for ${mission.id}", mission.target > 0)
            assertTrue("reward must be positive for ${mission.id}", mission.rewardTokens > 0)
        }
    }
}
