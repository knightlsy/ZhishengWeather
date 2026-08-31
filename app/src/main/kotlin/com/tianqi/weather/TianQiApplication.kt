package com.tianqi.weather

import android.app.Application
import com.tianqi.weather.data.CityRepository
import com.tianqi.weather.data.SecretStore
import com.tianqi.weather.data.SettingsRepository
import com.tianqi.weather.widget.WidgetSyncWorker

class TianQiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecretStore.init(this)
        SettingsRepository.init(this)
        CityRepository.init(this)
        // 小组件后台刷新周期任务（v0.0.4）：KEEP 策略保证不因每次启动重置周期
        WidgetSyncWorker.schedule(this)
    }
}
