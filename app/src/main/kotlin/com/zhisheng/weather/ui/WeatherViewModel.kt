package com.zhisheng.weather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zhisheng.weather.data.AmbienceLevel
import com.zhisheng.weather.data.CityRepository
import com.zhisheng.weather.data.LocationSource
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.data.SourcePref
import com.zhisheng.weather.data.WeatherCache
import com.zhisheng.weather.data.WeatherRepository
import com.zhisheng.weather.model.City
import com.zhisheng.weather.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DisplayPrefs(
    val showAqi: Boolean = true,
    val showIndices: Boolean = true,
    val showYesterday: Boolean = true,
    val showPrecip: Boolean = true,
    val showTelemetry: Boolean = true,
    val windUnit: String = "kmh",
    val pressureUnit: String = "hpa",
    val scanlines: Boolean = true,
    val ambience: AmbienceLevel = AmbienceLevel.SUBTLE,
    val bootAnim: Boolean = true,
)

data class HomeUiState(
    val cities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val weather: WeatherData? = null,
    val loading: Boolean = false,
    val tempUnit: String = "c",
    val showTyphoon: Boolean = true,
    val sourcePref: SourcePref = SourcePref.AUTO,
    val prefs: DisplayPrefs = DisplayPrefs(),
    val locating: Boolean = false,
    val locateMessage: String? = null,
    // 非空 = 当前展示的是离线缓存兜底数据，值为缓存年龄（毫秒）
    val staleAgeMillis: Long? = null,
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _weather = MutableStateFlow<WeatherData?>(null)
    private val _loading = MutableStateFlow(false)
    private val _locating = MutableStateFlow(false)
    private val _locateMessage = MutableStateFlow<String?>(null)
    private val _staleAge = MutableStateFlow<Long?>(null)

    private var lastFetchedKey: String? = null
    private var lastFetchKey: String? = null
    private var lastFetchAt: Long = 0L
    private var lastAutoLocateAt: Long = 0L

    // 同一时间只保留一次抓取：换城市立即取消旧任务，
    // 避免新旧城市结果乱序覆盖（v0.0.1：切城市偶发数据错乱的修复）
    private var fetchJob: kotlinx.coroutines.Job? = null

    val cities: StateFlow<List<City>> = CityRepository.cities
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val selectedCity: StateFlow<City?> = CityRepository.selectedCity
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // 数据源偏好单独暴露：refresh 时要读当前值（combine 的 6 元上限已满）
    val sourcePref: StateFlow<SourcePref> = SettingsRepository.sourcePref
        .stateIn(viewModelScope, SharingStarted.Eagerly, SourcePref.AUTO)

    private val displayPrefs: StateFlow<DisplayPrefs> = combine(
        SettingsRepository.showAqi,
        SettingsRepository.showIndices,
        SettingsRepository.showYesterday,
        SettingsRepository.showPrecip,
        SettingsRepository.showTelemetry,
    ) { aqi, ix, y, p, tele ->
        DisplayPrefs(showAqi = aqi, showIndices = ix, showYesterday = y, showPrecip = p, showTelemetry = tele)
    }.combine(
        combine(
            SettingsRepository.windUnit,
            SettingsRepository.pressureUnit,
            SettingsRepository.scanlines,
            SettingsRepository.ambience,
            SettingsRepository.bootAnim,
        ) { w, pr, sl, amb, boot -> listOf(w, pr, sl, amb, boot) }
    ) { base, extra ->
        base.copy(
            windUnit = extra[0] as String,
            pressureUnit = extra[1] as String,
            scanlines = extra[2] as Boolean,
            ambience = extra[3] as AmbienceLevel,
            bootAnim = extra[4] as Boolean,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DisplayPrefs())

    private val baseState: StateFlow<HomeUiState> = combine(
        cities, selectedCity, _weather, _loading,
        SettingsRepository.tempUnit, SettingsRepository.showTyphoon,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        HomeUiState(
            cities = arr[0] as List<City>,
            selectedCity = arr[1] as City?,
            weather = arr[2] as WeatherData?,
            loading = arr[3] as Boolean,
            tempUnit = arr[4] as String,
            showTyphoon = arr[5] as Boolean,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    // combine 的 6 元没有具名 lambda 重载，与 baseState 一样用 Array 风格
    val uiState: StateFlow<HomeUiState> = combine(
        baseState, displayPrefs, sourcePref, _locating, _locateMessage, _staleAge,
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        (arr[0] as HomeUiState).copy(
            prefs = arr[1] as DisplayPrefs,
            sourcePref = arr[2] as SourcePref,
            locating = arr[3] as Boolean,
            locateMessage = arr[4] as String?,
            staleAgeMillis = arr[5] as Long?,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState())

    init {
        viewModelScope.launch { CityRepository.ensureDefaultCity() }
        viewModelScope.launch {
            selectedCity.collect { city ->
                if (city != null && city.locationKey != lastFetchedKey) {
                    lastFetchedKey = city.locationKey
                    refresh(city)
                }
            }
        }
        // 数据源改了就立即重拉当前城市，不用等用户手动下拉（v0.0.2）
        viewModelScope.launch {
            var first = true
            sourcePref.collect {
                if (first) { first = false; return@collect }
                refresh()
            }
        }
        viewModelScope.launch {
            SettingsRepository.purgeRetiredProviderData()
        }
    }

    // force=false 用于 ON_RESUME 自动刷新：同城 10 分钟内不重复拉，
    // 避免与启动时 selectedCity 首发射叠加成双份请求（v0.0.1）
    fun refresh(city: City? = null, force: Boolean = true) {
        val target = city ?: selectedCity.value ?: return
        val now = System.currentTimeMillis()
        if (!force && target.locationKey == lastFetchKey && now - lastFetchAt < 10 * 60_000L) return
        if (target.locationKey != lastFetchedKey) lastFetchedKey = target.locationKey
        lastFetchKey = target.locationKey
        lastFetchAt = now
        fetchJob?.cancel()
        var job: kotlinx.coroutines.Job? = null
        job = viewModelScope.launch {
            _loading.value = true
            try {
                // 全局超时兜底：三源降级链最坏可串行 60s+，超过 25s 直接判失败走离线缓存。
                // 注意 TimeoutCancellationException 是 CancellationException 子类，必须先于它 catch（v0.0.4）。
                var result = try {
                    kotlinx.coroutines.withTimeout(FETCH_TIMEOUT_MS) {
                        WeatherRepository.fetchWeather(target, sourcePref.value)
                    }
                } catch (te: kotlinx.coroutines.TimeoutCancellationException) {
                    android.util.Log.w("ZhishengWeather", "抓取超时 ${target.name}，走缓存兜底")
                    WeatherData(error = "请求超时")
                } catch (ce: kotlinx.coroutines.CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    android.util.Log.w("ZhishengWeather", "抓取异常 ${target.name}", e)
                    WeatherData(error = e.message ?: "网络错误")
                }
                if (result.current == null) {
                    // 失败兜底：展示该城最近一次成功数据，标注缓存年龄；无缓存才显示错误
                    val cached = WeatherCache.load(getApplication(), target.locationKey)
                    if (cached != null) {
                        result = cached.data
                        _staleAge.value = System.currentTimeMillis() - cached.savedAtMillis
                        android.util.Log.i("ZhishengWeather", "${target.name} 抓取失败，展示 ${_staleAge.value}ms 前的缓存")
                    } else {
                        _staleAge.value = null
                    }
                } else {
                    _staleAge.value = null
                    runCatching { WeatherCache.save(getApplication(), target.locationKey, result) }
                    // 抓到有效数据就写一份小组件快照并刷新桌面（失败不影响主流程）
                    runCatching { WidgetSnapshotBuilder.save(getApplication(), target, result) }
                }
                _weather.value = result
            } finally {
                // 仅当自己仍是当前任务时才清 loading：换城市取消旧任务时，
                // 旧任务的 finally 不应把新任务刚置的 loading 清掉（v0.0.3）
                if (fetchJob === job) _loading.value = false
            }
        }
        fetchJob = job
    }

    fun selectCity(locationKey: String) {
        _locateMessage.value = null
        viewModelScope.launch { CityRepository.selectCity(locationKey) }
    }

    fun addCityAndSelect(city: City) {
        _locateMessage.value = null
        viewModelScope.launch {
            CityRepository.addCity(city)
        }
        lastFetchedKey = city.locationKey
        refresh(city)
    }

    fun removeCity(locationKey: String) {
        viewModelScope.launch { CityRepository.removeCity(locationKey) }
    }

    // —— 定位（v0.0.2）——
    // 手动触发时总是优先取新位置；权限申请由 UI 层负责，这里假定已授权。
    fun locateCurrentCity() {
        beginLocate(automatic = false)
    }

    // 定位开关开启且已授权后，回到前台最多每 30 分钟复核一次城市；
    // 不申请后台位置，也不会在 App 未打开时持续跟踪。
    fun autoLocateIfEnabled() {
        if (_locating.value) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (now - lastAutoLocateAt < AUTO_LOCATE_INTERVAL_MS) return@launch
            if (!SettingsRepository.locationEnabled.first()) return@launch
            if (!LocationSource.hasPermission(getApplication())) return@launch
            lastAutoLocateAt = now
            beginLocate(automatic = true)
        }
    }

    private fun beginLocate(automatic: Boolean) {
        if (_locating.value) return
        viewModelScope.launch {
            _locating.value = true
            if (!automatic) _locateMessage.value = null
            try {
                when (val r = LocationSource.locate(getApplication())) {
                    is LocationSource.Result.Ok -> {
                        _locateMessage.value = if (automatic) {
                            "已自动更新定位：${r.city.name}"
                        } else {
                            "已定位：${r.city.name}"
                        }
                        lastFetchedKey = r.city.locationKey
                        CityRepository.addCity(r.city)
                        refresh(r.city)
                    }
                    is LocationSource.Result.Failed -> {
                        if (!automatic) _locateMessage.value = r.message
                    }
                }
            } finally {
                _locating.value = false
            }
        }
    }

    fun clearLocateMessage() {
        _locateMessage.value = null
    }

    private companion object {
        const val AUTO_LOCATE_INTERVAL_MS = 30 * 60_000L
        const val FETCH_TIMEOUT_MS = 25_000L
    }
}
