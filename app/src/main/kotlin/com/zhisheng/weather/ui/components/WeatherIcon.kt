package com.zhisheng.weather.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.conditionIconRes

// 天气图标组件。资源映射收敛在 model/ConditionIcons.kt（与小组件共用同一真源，v0.0.4）。
// 图标为平面双色调 PNG，颜色已内置于位图，不统一染色。
@Composable
fun WeatherIcon(
    condition: WeatherCondition?,
    modifier: Modifier = Modifier,
) {
    val res = conditionIconRes(condition)
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = condition?.label,
            modifier = modifier,
        )
    }
}
