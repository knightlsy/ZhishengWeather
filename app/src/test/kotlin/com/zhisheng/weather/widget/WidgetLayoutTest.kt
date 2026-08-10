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
        val allowed = setOf("LinearLayout", "TextView", "ImageView", "include")
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
}
