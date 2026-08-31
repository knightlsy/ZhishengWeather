package com.tianqi.weather.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AccentToneTest {
    @Test
    fun standardIsDefaultAndSoftRoundTrips() {
        assertEquals(AccentTone.STANDARD, AccentTone.from(null))
        assertEquals(AccentTone.STANDARD, AccentTone.from("unknown"))
        assertEquals(AccentTone.SOFT, AccentTone.from("soft"))
    }
}
