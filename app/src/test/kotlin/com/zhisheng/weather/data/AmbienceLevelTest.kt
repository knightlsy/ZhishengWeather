package com.zhisheng.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbienceLevelTest {

    @Test
    fun vividLevelIsClearlyStrongerWithoutChangingTheDefault() {
        assertEquals(AmbienceLevel.SUBTLE, AmbienceLevel.from(null))
        assertEquals(AmbienceLevel.VIVID, AmbienceLevel.from("vivid"))
        assertTrue(AmbienceLevel.VIVID.factor >= AmbienceLevel.SUBTLE.factor * 3f)
    }
}
