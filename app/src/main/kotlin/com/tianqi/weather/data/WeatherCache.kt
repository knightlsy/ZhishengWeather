package com.tianqi.weather.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tianqi.weather.model.WeatherData
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.weatherCacheStore: DataStore<Preferences> by preferencesDataStore(name = "weather_cache")

// 离线缓存：每城最近一次抓取成功的 WeatherData。
// 断网 / 全部数据源失败 / 全局超时时的兜底展示源（v0.0.4）。
// 与小组件的 WidgetCache 职责不同：这里缓存完整数据、按城市分键。
@Serializable
data class CachedWeather(
    val data: WeatherData,
    val savedAtMillis: Long,
)

object WeatherCache {

    private val json = Json { ignoreUnknownKeys = true }

    private fun key(locationKey: String) = stringPreferencesKey("cached_$locationKey")

    suspend fun save(context: Context, locationKey: String, data: WeatherData) {
        val entry = CachedWeather(data = data, savedAtMillis = System.currentTimeMillis())
        context.applicationContext.weatherCacheStore.edit {
            it[key(locationKey)] = json.encodeToString(CachedWeather.serializer(), entry)
        }
    }

    suspend fun load(context: Context, locationKey: String): CachedWeather? {
        val raw = context.applicationContext.weatherCacheStore.data.first()[key(locationKey)] ?: return null
        return try {
            json.decodeFromString(CachedWeather.serializer(), raw)
        } catch (_: Exception) {
            null
        }
    }
}
