package com.tianqi.weather.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/** Switches between the two manifest launcher aliases without restarting the app. */
object AppIconManager {
    private const val CHARACTER_ALIAS = "com.tianqi.weather.IconCharacter"
    private const val CLASSIC_ALIAS = "com.tianqi.weather.IconClassic"

    fun apply(context: Context, style: AppIconStyle): Boolean = runCatching {
        val appContext = context.applicationContext
        val packageManager = appContext.packageManager
        val character = ComponentName(appContext.packageName, CHARACTER_ALIAS)
        val classic = ComponentName(appContext.packageName, CLASSIC_ALIAS)
        val selected = if (style == AppIconStyle.CHARACTER) character else classic
        val previous = if (style == AppIconStyle.CHARACTER) classic else character

        // Always expose the new launcher entry before hiding the old one. Some OEM
        // launchers otherwise briefly remove the app from the desktop/app drawer.
        setState(
            packageManager = packageManager,
            component = selected,
            state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        )
        setState(
            packageManager = packageManager,
            component = previous,
            state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
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
