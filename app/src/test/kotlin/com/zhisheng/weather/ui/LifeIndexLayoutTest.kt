package com.zhisheng.weather.ui

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeIndexLayoutTest {
    @Test
    fun indexGridUsesContentDrivenRowsWithoutBlankPlaceholderColumn() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val home = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/home/HomeScreen.kt").readText()

        assertTrue(home.contains("items.chunked(2)"))
        assertTrue(home.contains("height(IntrinsicSize.Max)"))
        assertTrue(home.contains("Modifier.weight(1f).fillMaxHeight()"))
        assertFalse(home.contains("if (rowItems.size == 1) Spacer(Modifier.weight(1f))"))
    }

    @Test
    fun longProviderNamesCannotTurnIntoVerticalColumns() {
        val projectDir = File(requireNotNull(System.getProperty("user.dir")))
        val home = File(projectDir, "src/main/kotlin/com/zhisheng/weather/ui/home/HomeScreen.kt").readText()
        val repository = File(projectDir, "src/main/kotlin/com/zhisheng/weather/data/WeatherRepository.kt").readText()

        assertTrue(home.contains("maxLines = 1"))
        assertTrue(home.contains("overflow = TextOverflow.Ellipsis"))
        assertTrue(repository.contains("\"10\" -> \"空气扩散\" to \"AIR\""))
        assertFalse(repository.contains("val name = item.name"))
    }

    @Test
    fun unmappedPaidIndexStillAppearsInsteadOfDisappearing() {
        val data = com.zhisheng.weather.model.WeatherData(
            extraIndices = listOf(
                com.zhisheng.weather.model.LifeIndexExtra("路况", "INDEX 21", "较好"),
            ),
        )
        val items = com.zhisheng.weather.ui.home.lifeIndexItems(
            data,
            com.zhisheng.weather.data.LifeIndexMetric.defaultSelection,
        )
        assertEquals(listOf("路况"), items.map { it.name })
        assertEquals(listOf("较好"), items.map { it.value })
    }

    @Test
    fun cityDeckPositionDoesNotCrashWhenCityListIsEmpty() {
        assertEquals(0f, com.zhisheng.weather.ui.home.clampCityDeckPosition(2f, 0), 0.0001f)
        assertEquals(0f, com.zhisheng.weather.ui.home.clampCityDeckPosition(-1f, 3), 0.0001f)
        assertEquals(2f, com.zhisheng.weather.ui.home.clampCityDeckPosition(9f, 3), 0.0001f)
    }
}
