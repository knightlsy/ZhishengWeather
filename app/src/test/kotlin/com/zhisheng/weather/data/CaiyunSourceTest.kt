package com.zhisheng.weather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherIntensity

class CaiyunSourceTest {
    @Test
    fun hailAndIntensityRemainDistinct() {
        assertEquals(WeatherCondition.HAIL, CaiyunSource.skycon("LIGHT_HAIL"))
        assertEquals(WeatherIntensity.HEAVY, CaiyunSource.skyconProfile("HEAVY_RAIN")?.intensity)
        assertEquals(WeatherCondition.UNKNOWN, CaiyunSource.skycon("FUTURE_CODE"))
    }
    @Test
    fun mapsEveryLifeIndexReturnedByCurrentAccount() {
        val item = { desc: String -> listOf(CaiyunLifeIndexItem(desc = desc)) }
        val indices = CaiyunSource.mapLifeIndices(
            CaiyunLifeIndex(
                ultraviolet = item("最弱"),
                carWashing = item("适宜"),
                dressing = item("温暖"),
                comfort = item("舒适"),
                coldRisk = item("少发"),
            ),
        )

        assertEquals(listOf("紫外线", "洗车", "穿衣", "舒适", "感冒"), indices.map { it.name })
        assertEquals(listOf("最弱", "适宜", "温暖", "舒适", "少发"), indices.map { it.category })
    }

    @Test
    fun absentPaidBlockDoesNotCreateFakeIndices() {
        assertTrue(CaiyunSource.mapLifeIndices(null).isEmpty())
    }

    @Test
    fun probabilityAcceptsBothCaiyunValueConventionsWithoutDoubleScaling() {
        assertEquals(60, CaiyunSource.normalizeProbability(0.60))
        assertEquals(60, CaiyunSource.normalizeProbability(60.0))
        assertEquals(100, CaiyunSource.normalizeProbability(1.0))
        assertEquals(86, CaiyunSource.normalizeProbability(0.86))
        assertNull(CaiyunSource.normalizeProbability(6000.0))
        assertNull(CaiyunSource.normalizeProbability(Double.NaN))
    }
}
