package com.miniappfactory.boomblocks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyMissionGeneratorTest {

    @Test
    fun `always returns exactly 5 missions`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W32")
        assertEquals(5, missions.size)
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
    fun `every mission has exactly 3 tiers with increasing targets and positive rewards`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W40")
        missions.forEach { mission ->
            assertEquals("mission ${mission.id} must have exactly 3 tiers", 3, mission.tiers.size)
            mission.tiers.forEach { tier ->
                assertTrue("target must be positive for ${mission.id}", tier.target > 0)
                assertTrue("reward must be positive for ${mission.id}", tier.rewardTokens > 0)
            }
            val targets = mission.tiers.map { it.target }
            assertEquals("tier targets must be strictly increasing for ${mission.id}", targets, targets.sorted().distinct())
        }
    }

    @Test
    fun `all 5 mission types are represented`() {
        val missions = WeeklyMissionGenerator.forWeek("2026-W40")
        assertEquals(MissionType.entries.toSet(), missions.map { it.type }.toSet())
    }
}
