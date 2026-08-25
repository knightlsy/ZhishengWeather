package com.zhisheng.weather.ui

import android.content.Context
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.WidgetCache
import com.zhisheng.weather.data.WidgetDay
import com.zhisheng.weather.data.WidgetHour
import com.zhisheng.weather.data.WidgetSnapshot
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.widget.ZhishengWidgetProvider
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// 小组件快照构建：从 WeatherData 组装 WidgetSnapshot 并落盘 + 刷新桌面。
// v0.0.4 从 WeatherViewModel 提取为共享逻辑——后台刷新 Worker 与主 App 抓取共用同一份实现。
object WidgetSnapshotBuilder {

    suspend fun save(context: Context, city: City, data: WeatherData) {
        val unit = SettingsRepository.tempUnit.first()
        val windUnit = SettingsRepository.windUnit.first()
        fun t(v: Double?): Int? = v?.let {
            (if (unit == "f") it * 9.0 / 5.0 + 32.0 else it).roundToInt()
        }
        val today = data.daily.firstOrNull()
        val hi = if (today?.high != null && today.low != null) maxOf(today.high, today.low) else today?.high
        val lo = if (today?.high != null && today.low != null) minOf(today.high, today.low) else today?.low
        val hourFmt = DateTimeFormatter.ofPattern("H时")
        val zone = ZoneId.systemDefault()

        WidgetCache.save(
            context,
            WidgetSnapshot(
                city = city.name,
                temp = t(data.current?.temperature),
                high = t(hi),
                low = t(lo),
                feelsLike = t(data.current?.feelsLike),
                humidity = data.current?.humidity?.roundToInt(),
                windText = Fmt.wind(data.current?.windSpeed, windUnit).orEmpty(),
                rainChance = (
                    data.hourly.firstOrNull()?.precipProb
                        ?: data.daily.firstOrNull()?.precipProbability
                    )?.takeIf { it in 1..100 },
                text = data.current?.weatherText ?: data.current?.condition?.label.orEmpty(),
                conditionName = data.current?.condition?.name.orEmpty(),
                aqi = data.aqi?.value,
                aqiLevel = data.aqi?.level.orEmpty(),
                updateMillis = data.updateTime ?: System.currentTimeMillis(),
                source = data.dataSource.orEmpty(),
                // 跳过"现在"那格，小组件右侧展示接下来的四小时
                hours = data.hourly.drop(1).take(4).map { h ->
                    WidgetHour(
                        label = hourFmt.format(Instant.ofEpochMilli(h.timeMillis).atZone(zone)),
                        temp = t(h.temperature),
                        conditionName = h.condition?.name.orEmpty(),
                    )
                },
                days = data.daily.take(3).mapIndexed { i, d ->
                    val dh = if (d.high != null && d.low != null) maxOf(d.high, d.low) else d.high
                    val dl = if (d.high != null && d.low != null) minOf(d.high, d.low) else d.low
                    WidgetDay(
                        label = if (i == 0) "今天" else weekdayZh(d.dateMillis, zone),
                        high = t(dh),
                        low = t(dl),
                        conditionName = d.condition?.name.orEmpty(),
                    )
                },
            ),
        )
        ZhishengWidgetProvider.refreshAll(context)
    }

    private fun weekdayZh(millis: Long, zone: ZoneId): String =
        when (Instant.ofEpochMilli(millis).atZone(zone).dayOfWeek.value) {
            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
            5 -> "周五"; 6 -> "周六"; else -> "周日"
        }
}
