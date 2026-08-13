package com.zhisheng.weather.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TempBarParamsTest {

    @Test
    fun normalizesWithinUnitRange() {
        // weekMin=10, weekMax=40, 当天 low=15 high=25
        val (lo, hi, w) = tempBarParams(15.0, 25.0, 10.0, 40.0, "c")
        assertTrue("lo in [0,1]", lo in 0f..1f)
        assertTrue("hi in [0,1]", hi in 0f..1f)
        assertTrue("lo <= hi", lo <= hi)
        assertTrue("bar fits within track", lo + w <= 1f + 1e-5f)
    }

    @Test
    fun doesNotCrashWhenLowApproachesWeekMax() {
        // 极端：当天低温 39.9 接近全周最高 40，lo 归一后 > 0.97。
        // 原内联 (hi-lo).coerceIn(0.03f, 1f-lo) 此处下界>上界会抛 IllegalArgumentException，逐日区域整体崩溃。
        val (lo, hi, w) = tempBarParams(39.9, 40.0, 10.0, 40.0, "c")
        assertTrue(lo in 0f..1f)
        assertTrue(hi in 0f..1f)
        assertTrue("width must not overflow right edge", lo + w <= 1f + 1e-5f)
        assertTrue("width non-negative", w >= 0f)
    }

    @Test
    fun doesNotCrashWhenLowEqualsWeekMax() {
        // 更极端：当天低温恰好等于全周最高温，lo 归一为 1.0，1f-lo=0
        val (lo, hi, w) = tempBarParams(40.0, 40.0, 10.0, 40.0, "c")
        assertTrue(lo in 0f..1f)
        assertTrue(hi in 0f..1f)
        assertTrue("width non-negative", w >= 0f)
    }

    @Test
    fun doesNotCrashWhenAllTemperaturesEqual() {
        // 全周温度相同：range 被 coerceAtLeast(1.0)，不除零
        val (lo, hi, w) = tempBarParams(20.0, 20.0, 20.0, 20.0, "c")
        assertTrue(lo in 0f..1f)
        assertTrue(hi in 0f..1f)
        assertTrue(w >= 0f)
    }

    @Test
    fun sortsSwappedHighLow() {
        // 源数据高低温写反：low=25 high=15，应排序为 lo<=hi，宽度非负
        val (lo, hi, w) = tempBarParams(25.0, 15.0, 10.0, 40.0, "c")
        assertTrue("lo should be the lower value", lo <= hi)
        assertTrue("width non-negative", w >= 0f)
    }

    @Test
    fun fahrenheitConversionMatchesCelsiusProportions() {
        // low/high 为原始摄氏度；weekMin/weekMax 为已按 unit 换算的显示温度
        // （与 DailySection 调用约定一致：weekMin/weekMax 由 lows/highs 经 conv 预算）
        val c = tempBarParams(15.0, 25.0, 10.0, 40.0, "c")
        val f = tempBarParams(15.0, 25.0, 50.0, 104.0, "f") // 10°C→50°F, 40°C→104°F
        assertEquals(c.first, f.first, 1e-3f)
        assertEquals(c.second, f.second, 1e-3f)
    }

    @Test
    fun nullValuesFallBackToWeekExtremes() {
        // null low/high 兜底为 weekMin/weekMax：lo=0, hi=1
        val (lo, hi, _) = tempBarParams(null, null, 10.0, 40.0, "c")
        assertEquals(0f, lo, 1e-3f)
        assertEquals(1f, hi, 1e-3f)
    }

    @Test
    fun coerceInThrowsWhenMinimumExceedsMaximum() {
        // 回归依据：Float.coerceIn 下界>上界时抛 IllegalArgumentException，
        // 这正是原 DailySection 内联归一在 lo>0.97 时崩溃的根因（v0.0.3 修复）。
        var threw = false
        try {
            0.5f.coerceIn(0.03f, 0.01f)
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assertTrue("Float.coerceIn must throw when min > max", threw)
    }

    @Test
    fun tempDeltaMatchesDisplayedUnit() {
        // 摄氏度：今日 25° - 昨日 20° = +5
        assertEquals(5, tempDelta(25.0, 20.0, "c"))
        // 华氏度：25°C=77°F，20°C=68°F，ΔT 应为 9（而非摄氏差的 5）
        assertEquals(9, tempDelta(25.0, 20.0, "f"))
    }

    @Test
    fun tempDeltaHandlesNegativeAndZero() {
        // 今天更冷
        assertEquals(-3, tempDelta(20.0, 23.0, "c"))
        // 温度相同
        assertEquals(0, tempDelta(20.0, 20.0, "c"))
    }

    @Test
    fun tempDeltaReturnsNullOnMissingValues() {
        assertNull(tempDelta(null, 20.0, "c"))
        assertNull(tempDelta(20.0, null, "c"))
        assertNull(tempDelta(null, null, "c"))
    }
}
