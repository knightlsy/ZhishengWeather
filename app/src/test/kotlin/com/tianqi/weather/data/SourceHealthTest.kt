package com.tianqi.weather.data

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SourceHealthTest {

    @Before
    fun reset() = SourceHealth.resetForTest()

    @After
    fun cleanup() = SourceHealth.resetForTest()

    @Test
    fun tripsCircuitAfterTwoConsecutiveFailures() {
        assertFalse(SourceHealth.isDown("test-source"))
        SourceHealth.recordFailure("test-source")
        assertFalse(SourceHealth.isDown("test-source"))
        SourceHealth.recordFailure("test-source")
        assertTrue(SourceHealth.isDown("test-source"))
    }

    @Test
    fun successClearsPendingFailures() {
        SourceHealth.recordFailure("test-source")
        SourceHealth.recordSuccess("test-source")
        SourceHealth.recordFailure("test-source")
        // 只有一次连续失败，不应熔断
        assertFalse(SourceHealth.isDown("test-source"))
    }

    @Test
    fun successRecoversFromTrippedCircuit() {
        repeat(3) { SourceHealth.recordFailure("test-source") }
        assertTrue(SourceHealth.isDown("test-source"))
        SourceHealth.recordSuccess("test-source")
        assertFalse(SourceHealth.isDown("test-source"))
    }

    @Test
    fun circuitExpiresAfterCooldown() {
        var now = 1_000_000L
        SourceHealth.nowProvider = { now }
        SourceHealth.recordFailure("test-source")
        SourceHealth.recordFailure("test-source")
        assertTrue(SourceHealth.isDown("test-source"))
        // 5 分钟冷却 + 1ms 后恢复可用
        now += 5 * 60_000L + 1
        assertFalse(SourceHealth.isDown("test-source"))
    }

    @Test
    fun sourcesAreIndependent() {
        SourceHealth.recordFailure("a")
        SourceHealth.recordFailure("a")
        assertTrue(SourceHealth.isDown("a"))
        assertFalse(SourceHealth.isDown("b"))
    }
}
