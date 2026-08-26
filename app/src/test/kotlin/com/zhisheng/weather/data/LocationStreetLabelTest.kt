package com.zhisheng.weather.data

import com.zhisheng.weather.model.City
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationStreetLabelTest {
    @Test
    fun combinesSubdistrictAndRoadWithoutRepeatingCity() {
        assertEquals(
            "新华路街道·北京路",
            streetLabel(
                subLocality = "新华路街道",
                thoroughfare = "北京路",
                locality = "金昌市",
                subAdminArea = "金川区",
                cityName = "金川区",
            ),
        )
    }

    @Test
    fun administrativeOnlyResultFallsBackToCity() {
        assertNull(
            streetLabel(
                subLocality = "金川区",
                thoroughfare = null,
                locality = "金昌市",
                subAdminArea = "金川区",
                cityName = "金川区",
            ),
        )
    }

    @Test
    fun cityDisplayKeepsStreetSeparateFromWeatherLookupName() {
        val city = City("金川区", "甘肃省·金昌市", 38.52, 102.19, "101161401", "新华路街道")

        assertEquals("金川区", city.name)
        assertEquals("金川区·新华路街道", city.displayName)
        assertEquals("甘肃省·金昌市 · 新华路街道", city.contextLabel)
    }
}
