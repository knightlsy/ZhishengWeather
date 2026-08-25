package com.zhisheng.weather.widget

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutTest {

    @Test
    fun everyWidgetLayoutUsesOnlyRemoteViewsSupportedElements() {
        val layoutDir = sequenceOf(
            File("app/src/main/res/layout"),
            File("src/main/res/layout"),
        ).first { it.isDirectory }
        val widgetLayouts = layoutDir.listFiles()
            .orEmpty()
            .filter { it.name.startsWith("widget_") && it.extension == "xml" }

        assertTrue("Expected widget XML layouts", widgetLayouts.isNotEmpty())
        val allowed = setOf("LinearLayout", "TextView", "ImageView", "ViewFlipper", "include")
        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        widgetLayouts.forEach { file ->
            val document = parser.parse(file)
            val nodes = document.getElementsByTagName("*")
            val unsupported = (0 until nodes.length)
                .map { nodes.item(it).nodeName.substringAfterLast('.') }
                .filterNot { it in allowed }
                .distinct()
            assertTrue(
                "${file.name} contains RemoteViews-unsupported elements: $unsupported",
                unsupported.isEmpty(),
            )
        }
    }

    @Test
    fun primaryWidgetsExposeReadableDateAndDetailFields() {
        val layoutDir = sequenceOf(
            File("app/src/main/res/layout"),
            File("src/main/res/layout"),
        ).first { it.isDirectory }
        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        listOf("widget_small.xml", "widget_medium.xml", "widget_large.xml").forEach { name ->
            val document = parser.parse(File(layoutDir, name))
            val xml = File(layoutDir, name).readText()
            assertTrue("$name must show a date", xml.contains("@+id/w_date"))
            assertTrue("$name must show current details", xml.contains("@+id/w_details"))

            val nodes = document.getElementsByTagName("*")
            val temp = (0 until nodes.length).map { nodes.item(it) }
                .first { it.attributes?.getNamedItem("android:id")?.nodeValue == "@+id/w_temp" }
            val icon = (0 until nodes.length).map { nodes.item(it) }
                .first { it.attributes?.getNamedItem("android:id")?.nodeValue == "@+id/w_icon" }
            val tempSize = temp.attributes.getNamedItem("android:textSize").nodeValue.removeSuffix("sp").toFloat()
            val iconSize = icon.attributes.getNamedItem("android:layout_width").nodeValue.removeSuffix("dp").toFloat()
            assertTrue("$name temperature is too small: $tempSize", tempSize >= 44f)
            assertTrue("$name icon is too small: $iconSize", iconSize >= 44f)
        }
    }

    @Test
    fun widgetPanelUsesAVisibleCornerRadius() {
        val drawable = sequenceOf(
            File("app/src/main/res/drawable/widget_bg.xml"),
            File("src/main/res/drawable/widget_bg.xml"),
        ).first { it.isFile }.readText()
        val radius = Regex("android:radius=\"([0-9.]+)dp\"")
            .find(drawable)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
        assertTrue("Widget corner radius must be visibly rounded", radius >= 16f)
    }

    @Test
    fun everyWidgetProviderHasLauncherCompatiblePreviewImage() {
        val resDir = sequenceOf(
            File("app/src/main/res"),
            File("src/main/res"),
        ).first { it.isDirectory }

        listOf("small", "medium", "large").forEach { size ->
            val provider = File(resDir, "xml/widget_info_$size.xml").readText()
            assertTrue(
                "widget_info_$size.xml must declare a static previewImage",
                provider.contains("android:previewImage=\"@drawable/widget_preview_$size\""),
            )
            assertTrue(
                "widget_preview_$size.png is missing",
                File(resDir, "drawable-nodpi/widget_preview_$size.png").isFile,
            )
        }
        listOf("nano", "tower").forEach { size ->
            val provider = File(resDir, "xml/widget_info_$size.xml").readText()
            assertTrue(provider.contains("android:previewLayout=\"@layout/widget_$size\""))
            assertTrue(File(resDir, "layout/widget_$size.xml").isFile)
        }
    }

    @Test
    fun widgetDetailsAreSizedPerLayoutInsteadOfBeingEllipsized() {
        val source = sequenceOf(
            File("app/src/main/kotlin/com/zhisheng/weather/widget/ZhishengWidgetProvider.kt"),
            File("src/main/kotlin/com/zhisheng/weather/widget/ZhishengWidgetProvider.kt"),
        ).first { it.isFile }.readText()

        assertTrue(source.contains("spacious && snap.windText.isNotBlank()"))
        // v0.0.4：2x2 主动舍弃更新时间——布局不再保留恒 GONE 的 w_upd 占位，
        // Provider 仅对非 small 档位写 w_upd（原断言检查 GONE 分支，随死控件移除更新）
        assertTrue(source.contains("if (hasUpdate) v.setViewVisibility(R.id.w_upd, View.VISIBLE)"))
        assertTrue(source.contains("snap.rainChance?.let { add(\"降水 ${'$'}it%\") }"))
        assertTrue("small and medium details must not include wind", !source.contains("layout != R.layout.widget_small && snap.windText"))

        val layoutDir = sequenceOf(
            File("app/src/main/res/layout"),
            File("src/main/res/layout"),
        ).first { it.isDirectory }
        val smallXml = File(layoutDir, "widget_small.xml").readText()
        assertTrue("widget_small must not carry the hidden w_upd placeholder", !smallXml.contains("@+id/w_upd"))
    }

    @Test
    fun widgetFamilyHasFiveHardwareFormFactorsAndLightIconTones() {
        val source = sequenceOf(
            File("app/src/main/kotlin/com/zhisheng/weather/widget/ZhishengWidgetProvider.kt"),
            File("src/main/kotlin/com/zhisheng/weather/widget/ZhishengWidgetProvider.kt"),
        ).first { it.isFile }.readText()
        val manifest = sequenceOf(
            File("app/src/main/AndroidManifest.xml"),
            File("src/main/AndroidManifest.xml"),
        ).first { it.isFile }.readText()

        listOf("Small", "Medium", "Large", "Nano", "Tower").forEach { size ->
            assertTrue("Missing $size widget provider", manifest.contains("ZhishengWidget$size"))
        }
        assertTrue(source.contains("widget_light_icon_sun"))
        assertTrue(source.contains("widget_light_icon_rain"))
        assertTrue(source.contains("setColorFilter"))
    }
}
