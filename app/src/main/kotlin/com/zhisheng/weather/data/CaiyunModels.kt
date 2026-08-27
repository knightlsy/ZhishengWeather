package com.zhisheng.weather.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaiyunWeatherResponse(
    val status: String? = null,
    val result: CaiyunResult? = null,
)

@Serializable
data class CaiyunResult(
    val realtime: CaiyunRealtime? = null,
    val minutely: CaiyunMinutely? = null,
    val hourly: CaiyunHourly? = null,
    val daily: CaiyunDaily? = null,
    val alert: CaiyunAlertBlock? = null,
    @SerialName("forecast_keypoint") val forecastKeypoint: String? = null,
)

@Serializable
data class CaiyunRealtime(
    val temperature: Double? = null,
    val humidity: Double? = null,
    val cloudrate: Double? = null,
    val skycon: String? = null,
    val visibility: Double? = null,
    val pressure: Double? = null,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    val wind: CaiyunWind? = null,
    @SerialName("air_quality") val airQuality: CaiyunAir? = null,
    val precipitation: CaiyunRealtimePrecip? = null,
)

@Serializable
data class CaiyunRealtimePrecip(val local: CaiyunLocalPrecip? = null)

@Serializable
data class CaiyunLocalPrecip(
    val status: String? = null,
    val intensity: Double? = null,
)

@Serializable
data class CaiyunWind(
    val speed: Double? = null,
    val direction: Double? = null,
)

@Serializable
data class CaiyunAir(
    val pm25: Double? = null,
    val pm10: Double? = null,
    val o3: Double? = null,
    val no2: Double? = null,
    val so2: Double? = null,
    val co: Double? = null,
    val aqi: CaiyunAqiChn? = null,
    val description: CaiyunChnUsa? = null,
)

@Serializable
data class CaiyunAqiChn(
    val chn: Int? = null,
    val usa: Int? = null,
)

@Serializable
data class CaiyunChnUsa(
    val chn: String? = null,
    val usa: String? = null,
)

@Serializable
data class CaiyunMinutely(
    val status: String? = null,
    val description: String? = null,
    @SerialName("precipitation_2h") val precipitation2h: List<Double>? = null,
    val precipitation: List<Double>? = null,
)

@Serializable
data class CaiyunHourly(
    val status: String? = null,
    val temperature: List<CaiyunTimed>? = null,
    val skycon: List<CaiyunTimedStr>? = null,
    val wind: List<CaiyunTimedWind>? = null,
    val precipitation: List<CaiyunTimed>? = null,
)

@Serializable
data class CaiyunTimed(
    val datetime: String? = null,
    val value: Double? = null,
    val probability: Double? = null,
)

@Serializable
data class CaiyunTimedStr(
    val datetime: String? = null,
    val date: String? = null,
    val value: String? = null,
)

@Serializable
data class CaiyunTimedWind(
    val datetime: String? = null,
    val speed: Double? = null,
    val direction: Double? = null,
)

@Serializable
data class CaiyunDaily(
    val status: String? = null,
    val temperature: List<CaiyunDailyTemp>? = null,
    val skycon: List<CaiyunTimedStr>? = null,
    @SerialName("skycon_08h_20h") val skyconDay: List<CaiyunTimedStr>? = null,
    @SerialName("skycon_20h_32h") val skyconNight: List<CaiyunTimedStr>? = null,
    val astro: List<CaiyunAstro>? = null,
    val precipitation: List<CaiyunDailyPrecip>? = null,
    @SerialName("life_index") val lifeIndex: CaiyunLifeIndex? = null,
)

@Serializable
data class CaiyunLifeIndex(
    val ultraviolet: List<CaiyunLifeIndexItem>? = null,
    val carWashing: List<CaiyunLifeIndexItem>? = null,
    val dressing: List<CaiyunLifeIndexItem>? = null,
    val comfort: List<CaiyunLifeIndexItem>? = null,
    val coldRisk: List<CaiyunLifeIndexItem>? = null,
)

@Serializable
data class CaiyunLifeIndexItem(
    val date: String? = null,
    val index: String? = null,
    val desc: String? = null,
)

@Serializable
data class CaiyunDailyTemp(
    val date: String? = null,
    val max: Double? = null,
    val min: Double? = null,
)

@Serializable
data class CaiyunDailyPrecip(
    val date: String? = null,
    val max: Double? = null,
    val probability: Double? = null,
)

@Serializable
data class CaiyunAstro(
    val date: String? = null,
    val sunrise: CaiyunClock? = null,
    val sunset: CaiyunClock? = null,
)

@Serializable
data class CaiyunClock(val time: String? = null)

@Serializable
data class CaiyunAlertBlock(
    val status: String? = null,
    val content: List<CaiyunAlert>? = null,
)

@Serializable
data class CaiyunAlert(
    val title: String? = null,
    val description: String? = null,
    val code: String? = null,
    val status: String? = null,
)
