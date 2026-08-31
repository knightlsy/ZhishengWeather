package com.tianqi.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryMetricTest {
    @Test
    fun newUsersSeeEveryTelemetryMetric() {
        assertEquals(TelemetryMetric.entries.toSet(), TelemetryMetric.selectionFrom(null))
    }

    @Test
    fun selectionRoundTripsInStableDisplayOrder() {
        val selected = setOf(TelemetryMetric.WIND_GUST, TelemetryMetric.HUMIDITY, TelemetryMetric.LUMINARY)
        val stored = TelemetryMetric.selectionKey(selected)

        assertEquals(selected, TelemetryMetric.selectionFrom(stored))
        assertTrue(stored.startsWith(TelemetryMetric.HUMIDITY.key))
    }

    @Test
    fun emptySelectionIsPreservedInsteadOfResettingToDefault() {
        val stored = TelemetryMetric.selectionKey(emptySet())

        assertEquals(emptySet<TelemetryMetric>(), TelemetryMetric.selectionFrom(stored))
    }
}
