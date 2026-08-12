package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// Faz 77: Pro Mode can regen formulu saf bir fonksiyon (Context/DataStore
// gerektirmiyor) — dogrudan unit test edilebiliyor.
class GameStateRepositoryTest {

    private val refillMs = GameStateRepository.CHALLENGE_LIFE_REFILL_MS
    private val maxLives = GameStateRepository.CHALLENGE_MAX_LIVES

    @Test
    fun `full lives never regenerate past the max`() {
        val (lives, _) = GameStateRepository.regenChallengeLives(maxLives, 0L, System.currentTimeMillis())
        assertEquals(maxLives, lives)
    }

    @Test
    fun `no time passed means no lives gained`() {
        val now = 10_000_000_000L
        val (lives, ts) = GameStateRepository.regenChallengeLives(2, now, now)
        assertEquals(2, lives)
        assertEquals(now, ts)
    }

    @Test
    fun `exactly one refill interval grants exactly one life`() {
        val now = 10_000_000_000L
        val (lives, ts) = GameStateRepository.regenChallengeLives(2, now - refillMs, now)
        assertEquals(3, lives)
        assertEquals(now, ts)
    }

    @Test
    fun `partial progress toward next life is preserved, not lost`() {
        val now = 10_000_000_000L
        val halfway = now - (refillMs / 2)
        val (lives, ts) = GameStateRepository.regenChallengeLives(2, halfway, now)
        assertEquals("no full interval elapsed yet, lives should not change", 2, lives)
        assertEquals("partial progress must be preserved (baseline untouched)", halfway, ts)
    }

    @Test
    fun `lives gained cap at max even with excess elapsed time`() {
        val now = 10_000_000_000L
        val (lives, ts) = GameStateRepository.regenChallengeLives(0, now - refillMs * 100, now)
        assertEquals(maxLives, lives)
        assertEquals("timestamp should snap to now once capped, not carry leftover", now, ts)
    }

    @Test
    fun `gained lives consume exactly their elapsed time, remainder carries over`() {
        val now = 10_000_000_000L
        val lastTs = now - (refillMs * 2 + refillMs / 3)
        val (lives, ts) = GameStateRepository.regenChallengeLives(0, lastTs, now)
        assertEquals(2, lives)
        assertEquals(lastTs + refillMs * 2, ts)
        assertTrue("remaining wait must be less than one full interval", now - ts < refillMs)
    }
}
