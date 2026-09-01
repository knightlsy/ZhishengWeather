package com.tianqi.weather.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * 桌面图标切换：在预声明的 activity-alias 之间互斥启用/禁用。
 * 三种风格：天气娘（CHARACTER）、经典（CLASSIC）、自定义（CUSTOM）。
 * 自定义风格需要用户先从相册上传图片（AppIconCustom），未上传时不可用。
 */
object AppIconManager {
    private const val CHARACTER_ALIAS = "com.tianqi.weather.IconCharacter"
    private const val CLASSIC_ALIAS = "com.tianqi.weather.IconClassic"
    private const val CUSTOM_ALIAS = "com.tianqi.weather.IconCustom"

    private val aliases = mapOf(
        AppIconStyle.CHARACTER to CHARACTER_ALIAS,
        AppIconStyle.CLASSIC to CLASSIC_ALIAS,
        AppIconStyle.CUSTOM to CUSTOM_ALIAS,
    )

    fun apply(context: Context, style: AppIconStyle): Boolean = runCatching {
        val appContext = context.applicationContext
        val packageManager = appContext.packageManager
        val selected = ComponentName(appContext.packageName, aliases[style] ?: return false)
        for ((s, alias) in aliases) {
            if (s == style) continue
            val other = ComponentName(appContext.packageName, alias)
            setState(packageManager, other, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
        }
        setState(
            packageManager = packageManager,
            component = selected,
            state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        )
    }.isSuccess

    private fun setState(
        packageManager: PackageManager,
        component: ComponentName,
        state: Int,
    ) {
        if (packageManager.getComponentEnabledSetting(component) == state) return
        packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP,
        )
    }
}