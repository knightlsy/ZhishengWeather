package com.tianqi.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeIndexMetricTest {
    @Test
    fun newUsersSeeEveryLifeIndexMetric() {
        assertEquals(LifeIndexMetric.entries.toSet(), LifeIndexMetric.selectionFrom(null))
    }

    @Test
    fun selectionRoundTripsInStableDisplayOrder() {
        val selected = setOf(LifeIndexMetric.SUNSCREEN, LifeIndexMetric.CAR_WASH, LifeIndexMetric.COMFORT)
        val stored = LifeIndexMetric.selectionKey(selected)

        assertEquals(selected, LifeIndexMetric.selectionFrom(stored))
        assertTrue(stored.startsWith(LifeIndexMetric.CAR_WASH.key))
    }

    @Test
    fun emptySelectionIsPreserved() {
        assertEquals(
            emptySet<LifeIndexMetric>(),
            LifeIndexMetric.selectionFrom(LifeIndexMetric.selectionKey(emptySet())),
        )
    }

    @Test
    fun providerLabelsResolveToSharedMetrics() {
        assertEquals(LifeIndexMetric.AIR_POLLUTION, LifeIndexMetric.fromEnglish("AIR POLLUTION"))
        assertEquals(LifeIndexMetric.AIR_POLLUTION, LifeIndexMetric.fromEnglish("AIR"))
        assertEquals(LifeIndexMetric.SUNGLASSES, LifeIndexMetric.fromEnglish("SUNGLASSES"))
        assertEquals(LifeIndexMetric.SUNGLASSES, LifeIndexMetric.fromEnglish("GLASSES"))
    }
}
