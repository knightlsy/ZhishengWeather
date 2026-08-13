package com.zhisheng.weather.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.conditionIconRes
import com.zhisheng.weather.ui.theme.LocalZhishengPalette

// 天气图标组件。资源映射收敛在 model/ConditionIcons.kt（与小组件共用同一真源，v0.0.4）。
// 图标为平面双色调 PNG，颜色已内置于位图，不统一染色。
// v0.0.5 浅色模式：位图是深底亮线设计，直接放纸白底会发白看不清——
// 用 SrcIn 把线条统一染成钢青，呈单色青线（清冷翡翠质感；此前染成墨黑太硬）。
@Composable
fun WeatherIcon(
    condition: WeatherCondition?,
    modifier: Modifier = Modifier,
) {
    val res = conditionIconRes(condition)
    if (res != null) {
        val palette = LocalZhishengPalette.current
        Image(
            painter = painterResource(res),
            contentDescription = condition?.label,
            modifier = modifier,
            colorFilter = if (palette.isLight) {
                ColorFilter.tint(palette.cyan, BlendMode.SrcIn)
            } else {
                null
            },
        )
    }
}
