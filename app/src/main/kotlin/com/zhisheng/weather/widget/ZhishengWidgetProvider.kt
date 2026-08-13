package com.zhisheng.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.zhisheng.weather.MainActivity
import com.zhisheng.weather.R
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.ThemeMode
import com.zhisheng.weather.data.WidgetCache
import com.zhisheng.weather.data.WidgetSnapshot
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.conditionIconRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 磷光天气仪表桌面小组件（v0.0.4）
// 三个 Provider = 桌面选择器里三个独立条目（2x2 / 4x2 / 4x4）；
// 每个仍可拉伸，布局按实际尺寸自适应。
// 数据来自 WidgetCache（主 App 抓取后写入），小组件本身不发网络请求。
open class ZhishengWidgetProvider : AppWidgetProvider() {

    // 子类固定档位；null = 按实际尺寸自适应
    protected open val forcedLayout: Int? = null

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        renderAsync(context, manager, ids)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        newOptions: Bundle?,
    ) {
        renderAsync(context, manager, intArrayOf(id))
    }

    private fun renderAsync(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pending = goAsync()
        scope.launch {
            try {
                val snap = runCatching { WidgetCache.load(context) }
                    .onFailure { android.util.Log.e(TAG, "读取小组件缓存失败", it) }
                    .getOrNull()
                // 小组件双主题（v0.0.5）：跟随 App 主题模式；SYSTEM 时按当前系统夜间态判定
                val light = when (SettingsRepository.themeMode.first()) {
                    ThemeMode.LIGHT -> true
                    ThemeMode.DARK -> false
                    ThemeMode.SYSTEM ->
                        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) !=
                            Configuration.UI_MODE_NIGHT_YES
                }
                ids.forEach { id ->
                    runCatching {
                        val views = build(context, manager, id, snap, light)
                        manager.updateAppWidget(id, views)
                    }.onFailure {
                        // 单个实例失败不阻断其他尺寸，同时留下可诊断日志。
                        android.util.Log.e(TAG, "小组件渲染失败 id=$id", it)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun build(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        snap: WidgetSnapshot?,
        light: Boolean,
    ): RemoteViews {
        val opts = manager.getAppWidgetOptions(id)
        val minW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val minH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)

        val layout = forcedLayout ?: when {
            minW >= 250 && minH >= 250 -> R.layout.widget_large
            minW >= 250 -> R.layout.widget_medium
            else -> R.layout.widget_small
        }
        val v = RemoteViews(context.packageName, layout)
        if (light) applyLightSkin(context, v) // XML 默认深色磷光，浅色按资源表整体换肤（v0.0.5）

        // 整块点击进 App
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        v.setOnClickPendingIntent(
            R.id.widget_root,
            PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )

        v.setTextViewText(R.id.w_date, dateLabel())
        // 2x2 布局已移除 w_upd 控件（主动舍弃更新时间，v0.0.4）；其余档位正常显示
        if (layout != R.layout.widget_small) v.setViewVisibility(R.id.w_upd, View.VISIBLE)

        if (snap == null || snap.temp == null) {
            // 空态兜底文案资源化（v0.0.4）
            v.setTextViewText(R.id.w_city, context.getString(R.string.widget_name))
            v.setTextViewText(R.id.w_temp, context.getString(R.string.widget_value_placeholder))
            v.setTextViewText(R.id.w_range, context.getString(R.string.widget_sync_hint))
            v.setTextViewText(R.id.w_details, context.getString(R.string.widget_details_placeholder))
            if (layout != R.layout.widget_small) {
                v.setTextViewText(R.id.w_upd, context.getString(R.string.widget_update_placeholder))
            }
            if (layout != R.layout.widget_small) {
                listOf(R.id.h1_i, R.id.h2_i, R.id.h3_i, R.id.h4_i)
                    .forEach { v.setViewVisibility(it, View.INVISIBLE) }
            }
            if (layout == R.layout.widget_large) {
                listOf(R.id.d1_i, R.id.d2_i, R.id.d3_i)
                    .forEach { v.setViewVisibility(it, View.INVISIBLE) }
                v.setViewVisibility(R.id.w_aqi, View.GONE)
            }
            return v
        }

        v.setTextViewText(R.id.w_city, snap.city.ifBlank { context.getString(R.string.widget_name) })
        v.setTextViewText(R.id.w_temp, "${snap.temp}°")
        v.setTextViewText(
            R.id.w_range,
            buildString {
                if (snap.text.isNotBlank()) append(snap.text)
                if (snap.high != null && snap.low != null) {
                    if (isNotEmpty()) append("  ·  ")
                    append("${snap.high}° / ${snap.low}°")
                }
            },
        )
        v.setImageViewResource(R.id.w_icon, iconRes(snap.conditionName))
        v.setTextViewText(
            R.id.w_details,
            buildList {
                snap.feelsLike?.let { add(if (layout == R.layout.widget_large) "体感 $it°" else "体感$it°") }
                snap.humidity?.let { add(if (layout == R.layout.widget_large) "湿度 $it%" else "湿度$it%") }
                if (layout == R.layout.widget_large && snap.windText.isNotBlank()) add("风 ${snap.windText}")
            }
                .joinToString(if (layout == R.layout.widget_large) "  ·  " else " · ")
                .ifBlank { "体感 --  ·  湿度 --" },
        )
        if (layout != R.layout.widget_small) {
            v.setTextViewText(
                R.id.w_upd,
                listOfNotNull(sourceShort(snap.source), timeLabel(context, snap))
                    .joinToString("  ")
                    .ifBlank { context.getString(R.string.widget_update_placeholder) },
            )
        }

        if (layout != R.layout.widget_small) {
            val hourIds = listOf(
                Triple(R.id.h1_t, R.id.h1_i, R.id.h1_v),
                Triple(R.id.h2_t, R.id.h2_i, R.id.h2_v),
                Triple(R.id.h3_t, R.id.h3_i, R.id.h3_v),
                Triple(R.id.h4_t, R.id.h4_i, R.id.h4_v),
            )
            hourIds.forEachIndexed { i, (tId, iId, vId) ->
                val h = snap.hours.getOrNull(i)
                if (h == null) {
                    v.setTextViewText(tId, "")
                    v.setTextViewText(vId, "")
                    v.setViewVisibility(iId, View.INVISIBLE)
                } else {
                    v.setTextViewText(tId, h.label)
                    v.setTextViewText(vId, h.temp?.let { "$it°" } ?: "--")
                    v.setImageViewResource(iId, iconRes(h.conditionName))
                    v.setViewVisibility(iId, View.VISIBLE)
                }
            }
        }

        if (layout == R.layout.widget_large) {
            val dayIds = listOf(
                Triple(R.id.d1_t, R.id.d1_i, R.id.d1_v),
                Triple(R.id.d2_t, R.id.d2_i, R.id.d2_v),
                Triple(R.id.d3_t, R.id.d3_i, R.id.d3_v),
            )
            dayIds.forEachIndexed { i, (tId, iId, vId) ->
                val d = snap.days.getOrNull(i)
                if (d == null) {
                    v.setTextViewText(tId, "")
                    v.setTextViewText(vId, "")
                    v.setViewVisibility(iId, View.INVISIBLE)
                } else {
                    v.setTextViewText(tId, d.label)
                    v.setTextViewText(
                        vId,
                        if (d.high != null && d.low != null) "${d.low}° ~ ${d.high}°" else "--",
                    )
                    v.setImageViewResource(iId, iconRes(d.conditionName))
                    v.setViewVisibility(iId, View.VISIBLE)
                }
            }
            val status = buildList {
                snap.aqi?.let { add("AQI $it ${snap.aqiLevel}".trim()) }
                snap.rainChance?.let { add("降水 $it%") }
            }.joinToString("  ·  ")
            if (status.isNotBlank()) {
                v.setViewVisibility(R.id.w_aqi, View.VISIBLE)
                v.setTextViewText(R.id.w_aqi, status)
            } else {
                v.setViewVisibility(R.id.w_aqi, View.GONE)
            }
        }
        return v
    }

    // 浅色换肤（v0.0.5）：文本色/背景/装饰条整体切换到纸面终端资源；
    // 三种布局 id 并集一次应用，缺失 id 的动作会被 RemoteViews 静默跳过
    private fun applyLightSkin(context: Context, v: RemoteViews) {
        fun color(res: Int) = context.getColor(res)
        v.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_light)
        v.setTextColor(R.id.w_city, color(R.color.widget_light_accent_orange))
        v.setTextColor(R.id.w_date, color(R.color.widget_light_accent_cyan))
        v.setTextColor(R.id.w_temp, color(R.color.widget_light_text_primary))
        v.setTextColor(R.id.w_range, color(R.color.widget_light_text_secondary))
        v.setTextColor(R.id.w_details, color(R.color.widget_light_text_tertiary))
        v.setTextColor(R.id.w_upd, color(R.color.widget_light_border))
        v.setTextColor(R.id.w_aqi, color(R.color.widget_light_accent_cyan))
        listOf(R.id.h1_t, R.id.h2_t, R.id.h3_t, R.id.h4_t)
            .forEach { v.setTextColor(it, color(R.color.widget_light_text_tertiary)) }
        listOf(R.id.h1_v, R.id.h2_v, R.id.h3_v, R.id.h4_v)
            .forEach { v.setTextColor(it, color(R.color.widget_light_text_primary)) }
        listOf(R.id.d1_t, R.id.d2_t, R.id.d3_t)
            .forEach { v.setTextColor(it, color(R.color.widget_light_text_secondary)) }
        listOf(R.id.d1_v, R.id.d2_v, R.id.d3_v)
            .forEach { v.setTextColor(it, color(R.color.widget_light_text_primary)) }
        v.setInt(R.id.widget_accent_bar, "setBackgroundResource", R.drawable.widget_accent_light)
        v.setInt(R.id.widget_rule_bar, "setBackgroundResource", R.drawable.widget_rule_light)
        v.setInt(R.id.widget_rule_bar_2, "setBackgroundResource", R.drawable.widget_rule_light)
        v.setInt(R.id.widget_live_dot, "setBackgroundResource", R.drawable.widget_live_dot_light)
    }

    private fun clock(ms: Long): String =
        if (ms <= 0) "--" else java.text.SimpleDateFormat("HH:mm", java.util.Locale.US)
            .format(java.util.Date(ms))

    // 快照新鲜度（v0.0.4）：超过 3 小时显示「x小时前」，超过 24 小时提示过期，
    // 不再让几天前的旧数据伪装成「今天 HH:mm」。负龄为设备时钟回拨，退回显示时刻。
    private fun timeLabel(context: Context, snap: WidgetSnapshot): String {
        val ageMs = System.currentTimeMillis() - snap.updateMillis
        return when {
            ageMs < 3 * 3_600_000L ->
                clock(snap.updateMillis).takeUnless { it == "--" }
                    ?: context.getString(R.string.widget_update_placeholder)
            ageMs < 24 * 3_600_000L -> context.getString(R.string.widget_stale_hours, ageMs / 3_600_000L)
            else -> context.getString(R.string.widget_stale_expired)
        }
    }

    private fun dateLabel(): String =
        java.text.SimpleDateFormat("M月d日 E", java.util.Locale.CHINA)
            .format(java.util.Date())

    private fun sourceShort(source: String): String? = when (source) {
        "QWEATHER" -> "和风"
        "XIAOMI" -> "小米"
        "OPEN-METEO" -> "公共源"
        else -> source.takeIf { it.isNotBlank() }
    }

    // 图标资源映射收敛在 model/ConditionIcons.kt（与 Compose 侧共用同一真源，v0.0.4）
    private fun iconRes(name: String): Int = conditionIconRes(
        runCatching { WeatherCondition.valueOf(name) }.getOrNull()
    ) ?: R.drawable.weather_cloud

    companion object {
        private const val TAG = "ZhishengWidget"
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        // 主 App 抓到新数据后调用，立即刷新所有已放置的小组件（三个规格都刷）
        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            listOf(
                ZhishengWidgetSmall::class.java,
                ZhishengWidgetMedium::class.java,
                ZhishengWidgetLarge::class.java,
            ).forEach { cls ->
                val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
                if (ids.isNotEmpty()) {
                    context.sendBroadcast(
                        Intent(context, cls)
                            .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    )
                }
            }
        }
    }
}

class ZhishengWidgetSmall : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_small
}

class ZhishengWidgetMedium : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_medium
}

class ZhishengWidgetLarge : ZhishengWidgetProvider() {
    override val forcedLayout = R.layout.widget_large
}
