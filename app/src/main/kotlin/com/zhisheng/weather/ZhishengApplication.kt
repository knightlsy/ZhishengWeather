package com.zhisheng.weather

import android.app.Application
import com.zhisheng.weather.data.CityRepository
import com.zhisheng.weather.data.SecretStore
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.widget.WidgetSyncWorker

class ZhishengApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecretStore.init(this)
        SettingsRepository.init(this)
        CityRepository.init(this)
        // 小组件后台刷新周期任务（v0.0.4）：KEEP 策略保证不因每次启动重置周期
        WidgetSyncWorker.schedule(this)
    }
}
