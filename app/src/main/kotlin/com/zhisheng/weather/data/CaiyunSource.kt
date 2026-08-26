package com.zhisheng.weather.data

import com.zhisheng.weather.model.AlertInfo
import com.zhisheng.weather.model.AqiInfo
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.DailyWeather
import com.zhisheng.weather.model.HourlyWeather
import com.zhisheng.weather.model.LifeIndexExtra
import com.zhisheng.weather.model.MinutePrecip
import com.zhisheng.weather.model.RainMeta
import com.zhisheng.weather.model.Nowcast
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.model.WeatherIntensity
import com.zhisheng.weather.model.WeatherProfile
import com.zhisheng.weather.model.PrecipitationPhase
import com.zhisheng.weather.model.alertLevelOf
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

interface CaiyunService {
    @GET("v2.6/{token}/{lng},{lat}/weather")
    suspend fun weather(
        @Path("token") token: String,
        @Path("lng") lng: String,
        @Path("lat") lat: String,
        @Query("alert") alert: Boolean = true,
        @Query("dailysteps") dailySteps: Int = 15,
        @Query("hourlysteps") hourlySteps: Int = 48,
        @Query("unit") unit: String = "metric:v2",
    ): CaiyunWeatherResponse
}

object CaiyunApi {
    val enabled: Boolean get() = SecretStore.caiyunReady

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
    val service: CaiyunService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.caiyunapp.com/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CaiyunService::class.java)
    }
}

object CaiyunSource {

    suspend fun fetch(city: City): WeatherData {
        val token = SecretStore.caiyunRuntime.token
        if (token.isBlank()) return WeatherData(error = "未配置彩云天气 Token")
        return try {
            val lng = String.format(java.util.Locale.US, "%.4f", city.longitude)
            val lat = String.format(java.util.Locale.US, "%.4f", city.latitude)
            val body = CaiyunApi.service.weather(token, lng, lat)
            if (!body.status.equals("ok", true) || body.result == null) {
                WeatherData(error = "彩云天气请求失败")
            } else {
                map(body.result)
            }
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (e: Exception) {
            android.util.Log.w("ZhishengWeather", "彩云请求失败", e)
            WeatherData(error = "彩云天气请求失败（检查 Token 与网络）")
        }
    }

    suspend fun ping(): String {
        val token = SecretStore.currentCaiyun().token
        if (token.isBlank()) return "还没有填写 Token"
        return try {
            val body = CaiyunApi.service.weather(token, "116.4074", "39.9042")
            if (body.status.equals("ok", true) && body.result?.realtime != null) "连接成功，彩云已返回北京实况"
            else "服务没有返回有效天气，请核对 Token"
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (_: Exception) {
            "连接失败，请核对 Token 与网络"
        }
    }

    private fun map(r: CaiyunResult): WeatherData {
        val rt = r.realtime
        val now = System.currentTimeMillis()
        val precip2h = r.minutely?.precipitation2h ?: r.minutely?.precipitation
        val minutes = precip2h?.let { Nowcast.minuteSeries(it.map { v -> v.toFloat() }, now) }.orEmpty()
        return WeatherData(
            current = rt?.let {
                CurrentWeather(
                    temperature = it.temperature,
                    feelsLike = it.apparentTemperature,
                    condition = skycon(it.skycon),
                    weatherText = skyconLabel(it.skycon),
                    profile = skyconProfile(it.skycon),
                    humidity = it.humidity?.times(100.0),
                    // 0.0.9-debug 修复：官方单位制表 metric（默认）下 wind.speed 就是 km/h，
                    // 内部风速单位也是 km/h。原实现 ×3.6 把风速放大 3.6 倍
                    //（2 级微风显示成 7 级大风）。仅 unit=SI 时才是 m/s。
                    windSpeed = it.wind?.speed,
                    windDirectionDeg = it.wind?.direction,
                    pressure = it.pressure?.div(100.0),
                    visibility = it.visibility,
                    cloudCover = it.cloudrate?.times(100.0),
                    precipMm = it.precipitation?.local?.intensity,
                )
            },
            hourly = r.hourly?.let { h ->
                val n = minOf(h.temperature?.size ?: 0, h.skycon?.size ?: 0, 48)
                (0 until n).map { i ->
                    val slot = h.temperature!![i]
                    HourlyWeather(
                        timeMillis = parseTime(slot.datetime) ?: (now + i * 3_600_000L),
                        temperature = slot.value,
                        condition = skycon(h.skycon?.getOrNull(i)?.value),
                        profile = skyconProfile(h.skycon?.getOrNull(i)?.value),
                        windSpeed = h.wind?.getOrNull(i)?.speed,
                    )
                }
            }.orEmpty(),
            daily = r.daily?.temperature?.mapIndexed { i, d ->
                DailyWeather(
                    dateMillis = parseTime(d.date) ?: (now + i * 86_400_000L),
                    high = d.max,
                    low = d.min,
                    condition = skycon(r.daily.skycon?.getOrNull(i)?.value),
                    weatherText = skyconLabel(r.daily.skycon?.getOrNull(i)?.value),
                    profile = skyconProfile(r.daily.skycon?.getOrNull(i)?.value),
                    sunrise = r.daily.astro?.getOrNull(i)?.sunrise?.time,
                    sunset = r.daily.astro?.getOrNull(i)?.sunset?.time,
                    precipProbability = normalizeProbability(
                        r.daily.precipitation?.getOrNull(i)?.probability,
                    ),
                    precipMm = r.daily.precipitation?.getOrNull(i)?.max,
                )
            }.orEmpty(),
            aqi = rt?.airQuality?.let { a ->
                AqiInfo(
                    value = a.aqi?.chn,
                    level = a.description?.chn,
                    pm25 = a.pm25?.toInt()?.toString(),
                    pm10 = a.pm10?.toInt()?.toString(),
                    o3 = a.o3?.toInt()?.toString(),
                    no2 = a.no2?.toInt()?.toString(),
                    so2 = a.so2?.toInt()?.toString(),
                    co = a.co?.toString(),
                )
            },
            alerts = r.alert?.content.orEmpty().mapNotNull { a ->
                val title = a.title?.trim().orEmpty()
                if (title.isEmpty()) null
                else AlertInfo(
                    title = title,
                    detail = a.description,
                    level = a.code,
                    severity = alertLevelOf(a.code ?: a.title),
                )
            },
            updateTime = now,
            rainNowcast = r.minutely?.description ?: r.forecastKeypoint,
            rainMinutes = minutes,
            rainMeta = minutes.takeIf { it.isNotEmpty() }?.let { RainMeta("CAIYUN", 1, now) },
            extraIndices = mapLifeIndices(r.daily?.lifeIndex),
            dataSource = "CAIYUN",
            blockSources = mapOf("current" to "CAIYUN", "hourly" to "CAIYUN", "daily" to "CAIYUN", "minutely" to "CAIYUN"),
            utcOffsetSeconds = offsetSeconds(
                r.hourly?.temperature?.firstOrNull()?.datetime ?: r.daily?.temperature?.firstOrNull()?.date,
            ),
        )
    }

    internal fun mapLifeIndices(life: CaiyunLifeIndex?): List<LifeIndexExtra> {
        if (life == null) return emptyList()
        return listOfNotNull(
            life.ultraviolet.firstIndex("紫外线", "UV"),
            life.carWashing.firstIndex("洗车", "CAR WASH"),
            life.dressing.firstIndex("穿衣", "DRESS"),
            life.comfort.firstIndex("舒适", "COMFORT"),
            life.coldRisk.firstIndex("感冒", "COLD"),
        )
    }

    // 彩云历史/不同套餐响应中 probability 既出现过 0..1，也出现过 0..100。
    // 按值域归一，不能无条件乘 100，否则 60 会被错误显示为 6000%。
    internal fun normalizeProbability(value: Double?): Int? {
        if (value == null || !value.isFinite() || value < 0.0) return null
        return when {
            value <= 1.0 -> (value * 100.0).roundToInt()
            value <= 100.0 -> value.roundToInt()
            else -> null
        }
    }

    private fun List<CaiyunLifeIndexItem>?.firstIndex(name: String, en: String): LifeIndexExtra? {
        val item = this?.firstOrNull { !it.desc.isNullOrBlank() } ?: return null
        return LifeIndexExtra(name, en, item.desc!!.trim())
    }

    private fun parseTime(raw: String?): Long? = try {
        if (raw.isNullOrBlank()) null
        else OffsetDateTime.parse(raw).toInstant().toEpochMilli()
    } catch (_: Exception) {
        try {
            java.time.LocalDateTime.parse(raw!!.take(19)).atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    internal fun skycon(code: String?): WeatherCondition? = skyconProfile(code)?.condition

    internal fun skyconProfile(code: String?): WeatherProfile? {
        val raw = code?.uppercase()?.takeIf { it.isNotBlank() } ?: return null
        fun p(
            condition: WeatherCondition,
            intensity: WeatherIntensity? = null,
            phase: PrecipitationPhase = PrecipitationPhase.NONE,
        ) = WeatherProfile(condition, intensity, phase, source = "CAIYUN", rawCode = raw)
        return when (raw) {
            "CLEAR_DAY" -> p(WeatherCondition.CLEAR)
            "CLEAR_NIGHT" -> p(WeatherCondition.CLEAR_NIGHT)
            "PARTLY_CLOUDY_DAY" -> p(WeatherCondition.PARTLY_CLOUDY)
            "PARTLY_CLOUDY_NIGHT" -> p(WeatherCondition.PARTLY_CLOUDY_NIGHT)
            "CLOUDY" -> p(WeatherCondition.OVERCAST)
            "LIGHT_HAZE" -> p(WeatherCondition.HAZE, WeatherIntensity.LIGHT)
            "MODERATE_HAZE" -> p(WeatherCondition.HAZE, WeatherIntensity.MODERATE)
            "HEAVY_HAZE" -> p(WeatherCondition.HAZE, WeatherIntensity.HEAVY)
            "LIGHT_RAIN" -> p(WeatherCondition.DRIZZLE, WeatherIntensity.LIGHT, PrecipitationPhase.RAIN)
            "MODERATE_RAIN" -> p(WeatherCondition.RAIN, WeatherIntensity.MODERATE, PrecipitationPhase.RAIN)
            "HEAVY_RAIN" -> p(WeatherCondition.RAIN, WeatherIntensity.HEAVY, PrecipitationPhase.RAIN)
            "STORM_RAIN" -> p(WeatherCondition.RAIN, WeatherIntensity.EXTREME, PrecipitationPhase.RAIN)
            "FOG" -> p(WeatherCondition.FOG)
            "LIGHT_SNOW" -> p(WeatherCondition.SNOW, WeatherIntensity.LIGHT, PrecipitationPhase.SNOW)
            "MODERATE_SNOW" -> p(WeatherCondition.SNOW, WeatherIntensity.MODERATE, PrecipitationPhase.SNOW)
            "HEAVY_SNOW" -> p(WeatherCondition.SNOW, WeatherIntensity.HEAVY, PrecipitationPhase.SNOW)
            "STORM_SNOW" -> p(WeatherCondition.SNOW, WeatherIntensity.EXTREME, PrecipitationPhase.SNOW)
            "DUST", "SAND" -> p(WeatherCondition.SAND, WeatherIntensity.MODERATE)
            "WIND" -> p(WeatherCondition.WIND)
            "LIGHT_HAIL" -> p(WeatherCondition.HAIL, WeatherIntensity.LIGHT, PrecipitationPhase.HAIL)
            "MODERATE_HAIL" -> p(WeatherCondition.HAIL, WeatherIntensity.MODERATE, PrecipitationPhase.HAIL)
            "HEAVY_HAIL" -> p(WeatherCondition.HAIL, WeatherIntensity.HEAVY, PrecipitationPhase.HAIL)
            else -> WeatherProfile(WeatherCondition.UNKNOWN, source = "CAIYUN", rawCode = raw)
        }
    }

    private fun offsetSeconds(raw: String?): Int? = try {
        if (raw.isNullOrBlank()) null else OffsetDateTime.parse(raw).offset.totalSeconds
    } catch (_: Exception) {
        null
    }

    internal fun skyconLabel(code: String?): String? = when (code?.uppercase()) {
        "CLEAR_DAY", "CLEAR_NIGHT" -> "晴"
        "PARTLY_CLOUDY_DAY", "PARTLY_CLOUDY_NIGHT" -> "多云"
        "CLOUDY" -> "阴"
        "LIGHT_HAZE", "MODERATE_HAZE", "HEAVY_HAZE" -> "霾"
        "LIGHT_RAIN" -> "小雨"
        "MODERATE_RAIN" -> "中雨"
        "HEAVY_RAIN", "STORM_RAIN" -> "大雨"
        "FOG" -> "雾"
        "LIGHT_SNOW" -> "小雪"
        "MODERATE_SNOW", "HEAVY_SNOW", "STORM_SNOW" -> "雪"
        "DUST", "SAND" -> "沙尘"
        "WIND" -> "大风"
        "LIGHT_HAIL" -> "小冰雹"
        "MODERATE_HAIL" -> "冰雹"
        "HEAVY_HAIL" -> "强冰雹"
        else -> skycon(code)?.label
    }
}
