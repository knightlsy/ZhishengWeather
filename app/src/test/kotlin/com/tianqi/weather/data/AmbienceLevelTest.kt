package com.tianqi.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbienceLevelTest {

    @Test
    fun vividLevelIsClearlyStrongerAndIsTheDefault() {
        assertEquals(AmbienceLevel.VIVID, AmbienceLevel.from(null))
        assertEquals(AmbienceLevel.VIVID, AmbienceLevel.from("vivid"))
        assertTrue(AmbienceLevel.VIVID.factor >= AmbienceLevel.SUBTLE.factor * 3f)
        assertTrue(AmbienceLevel.INTENSE.factor > AmbienceLevel.VIVID.factor)
        assertTrue(AmbienceLevel.INTENSE.motionScale > AmbienceLevel.VIVID.motionScale)
        assertTrue(AmbienceLevel.INTENSE.vivid)
    }
}
