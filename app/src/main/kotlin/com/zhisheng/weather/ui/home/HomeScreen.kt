/* Hallmark · pre-emit critique: P5 H5 E5 S5 R5 V4 */
/* Hallmark · component: minute precipitation + wind compass · genre: atmospheric
 * theme: existing Zhisheng terminal · contrast: pass
 */
package com.zhisheng.weather.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhisheng.weather.model.AlertInfo
import com.zhisheng.weather.model.AqiInfo
import com.zhisheng.weather.model.CurrentWeather
import com.zhisheng.weather.model.DailyWeather
import com.zhisheng.weather.model.HourlyWeather
import com.zhisheng.weather.model.Nowcast
import com.zhisheng.weather.model.HeroTemps
import com.zhisheng.weather.model.TyphoonInfo
import com.zhisheng.weather.model.WeatherCondition
import com.zhisheng.weather.model.WeatherData
import com.zhisheng.weather.model.YesterdayInfo
import com.zhisheng.weather.R
import com.zhisheng.weather.ui.Fmt
import com.zhisheng.weather.ui.HomeUiState
import com.zhisheng.weather.ui.WeatherViewModel
import com.zhisheng.weather.ui.components.WeatherIcon
import com.zhisheng.weather.ui.components.WeatherAmbience
import com.zhisheng.weather.ui.components.isNightAt
import com.zhisheng.weather.ui.theme.ZhishengBg
import com.zhisheng.weather.ui.theme.ZhishengCard
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.LocalZhishengPalette
import com.zhisheng.weather.ui.theme.ZhishengMint
import androidx.compose.ui.graphics.lerp as colorLerp
import com.zhisheng.weather.ui.theme.ZhishengOrange
import com.zhisheng.weather.ui.theme.ZhishengRed
import com.zhisheng.weather.ui.theme.ZhishengSurface
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary
import com.zhisheng.weather.ui.theme.ZhishengWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════
// 枳生天气 · 磷光数据终端主屏
// 布局序：状态行 → Hero → 预警 → 逐时(曲线) → 分钟降水 → 逐日(归一化温度条)
//        → 遥测卡格 → 空气质量 → 生活指数 → 昨日复盘 → 台风 → 枳生页脚
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WeatherViewModel,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 氛围层要知道现在是不是夜里：国标现象码（小米 weathercn）没有昼夜变体，
    // 只看 condition 的话夜里的晴天也会走白天那套。每分钟对一次表，
    // 日落之后主屏立刻换成星点，不必等下一次天气刷新（v0.0.9）。
    var epochMinute by remember { mutableStateOf(System.currentTimeMillis() / 60_000L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            epochMinute = System.currentTimeMillis() / 60_000L
        }
    }
    val nowMinutes = uiState.weather?.utcOffsetSeconds?.let { offset ->
        Math.floorMod(epochMinute + offset / 60L, 24L * 60L).toInt()
    } ?: java.time.LocalTime.now().run { hour * 60 + minute }
    val todayAstro = uiState.weather?.daily?.firstOrNull()
    val night = isNightAt(todayAstro?.sunrise, todayAstro?.sunset, nowMinutes)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CityDrawer(
                uiState = uiState,
                onSelect = { key ->
                    viewModel.selectCity(key)
                    scope.launch { drawerState.close() }
                },
                onRemove = viewModel::removeCity,
                onAddCity = {
                    scope.launch { drawerState.close() }
                    onSearchClick()
                },
            )
        },
    ) {
        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }
        Box(modifier = Modifier.fillMaxSize().background(ZhishengBg)) {
            WeatherAmbience(
                weather = uiState.weather,
                level = uiState.prefs.ambience,
                night = night,
            )
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    cityName = uiState.selectedCity?.name ?: "枳生天气",
                    loading = uiState.loading,
                    onMenu = { scope.launch { drawerState.open() } },
                    onRefresh = { viewModel.refresh() },
                    onSettings = onSettingsClick,
                )
                PullToRefreshBox(
                    isRefreshing = uiState.loading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 0.0.9-debug：cities 占位期（citiesLoaded=false）不判空态，
                        // 渲染 loading——否则已存城市的用户每次冷启动闪一屏"未接入城市"。
                        val contentKey = when {
                            uiState.citiesLoaded && uiState.cities.isEmpty() -> "empty"
                            uiState.loading && uiState.weather == null -> "loading"
                            uiState.weather?.error != null && uiState.weather?.current == null -> "error"
                            uiState.weather != null -> "data"
                            else -> "loading"
                        }
                        Crossfade(targetState = contentKey, animationSpec = tween(280), label = "content") { key ->
                            when (key) {
                                "empty" -> EmptyState(onSearchClick)
                                "error" -> ErrorState(uiState.weather?.error.orEmpty(), onSearchClick)
                                // 0.0.9-debug 修复：按城市 key 包一层。换城市时 contentKey 恒为
                                // "data"，WeatherContent 不重建，原城市停在半截的滚动深度、
                                // 逐日展开行、预警展开态全部原样带进新城市。key 换城市即
                                // 整个子树重建：列表回顶、展开态清零（entered 交错动画随
                                // 重建重放一次，语义正确——这就是新城市首次入场）。
                                "data" -> {
                                    // Crossfade 退出动画仍会组合旧的 "data" 分支。切城市时
                                    // weather 已被清空，不能对当前 uiState.weather 做 !!。
                                    val weather = uiState.weather
                                    if (weather != null) {
                                        androidx.compose.runtime.key(uiState.selectedCity?.locationKey) {
                                            val weatherListState = rememberLazyListState()
                                            WeatherContent(
                                                data = weather,
                                                city = uiState.selectedCity,
                                                unit = uiState.tempUnit,
                                                showTyphoon = uiState.showTyphoon,
                                                prefs = uiState.prefs,
                                                staleAgeMillis = uiState.staleAgeMillis,
                                                listState = weatherListState,
                                            )
                                        }
                                    }
                                }
                                else -> BootState(uiState.prefs.bootAnim)
                            }
                        }
                    }
                }
            }
            if (uiState.prefs.scanlines) Scanlines()
        }
    }
}

/**
 * 开发者氛围实验室复用的真实首页表面。
 * data/city/prefs 全由调用方以内存值传入，不持有 ViewModel，也不会写入缓存或城市选择。
 */
@Composable
fun SimulatedWeatherSurface(
    data: WeatherData,
    city: com.zhisheng.weather.model.City,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    unit: String = "c",
    night: Boolean = false,
    header: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(ZhishengBg)) {
        WeatherAmbience(weather = data, level = prefs.ambience, night = night)
        Column(Modifier.fillMaxSize()) {
            header()
            Box(Modifier.weight(1f)) {
                androidx.compose.runtime.key(data.current?.condition, data.current?.profile?.intensity) {
                    val listState = rememberLazyListState()
                    WeatherContent(
                        data = data,
                        city = city,
                        unit = unit,
                        showTyphoon = false,
                        prefs = prefs,
                        staleAgeMillis = null,
                        listState = listState,
                    )
                }
            }
        }
        if (prefs.scanlines) Scanlines()
    }
}

// —— 扫描线氛围层（3dp 周期，不拦截触摸）——
// 深色 = CRT 扫描线（白 2.5%）；浅色 = 纸面细纹（墨线 2%，v0.0.5）
@Composable
private fun Scanlines() {
    val lineColor = LocalZhishengPalette.current.run {
        if (isLight) text.copy(alpha = 0.02f) else Color.White.copy(alpha = 0.025f)
    }
    Box(modifier = Modifier.fillMaxSize().drawWithCache {
        val step = 3.dp.toPx()
        val scanPath = Path()
        var y = 0f
        while (y < size.height) {
            scanPath.moveTo(0f, y)
            scanPath.lineTo(size.width, y)
            y += step
        }
        onDrawBehind { drawPath(scanPath, lineColor, style = Stroke(width = 1f)) }
    })
}

@Composable
private fun TopBar(
    cityName: String,
    loading: Boolean,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Menu, contentDescription = "城市列表", tint = ZhishengTextSecondary, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(
                text = cityName,
                style = MaterialTheme.typography.titleMedium,
                color = ZhishengOrange,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "ZHISHENG WEATHER TERMINAL",
                style = MaterialTheme.typography.labelSmall,
                color = ZhishengTextTertiary,
                letterSpacing = 1.5.sp,
            )
        }
        // remember 必须无条件调用：loading 在刷新起止之间翻转，
        // 不能把 InfiniteTransition 放进 if 里。
        val spin = rememberInfiniteTransition(label = "spin")
        val animatedAngle by spin.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
            label = "angle",
        )
        val angle = if (loading) animatedAngle else 0f
        IconButton(onClick = onRefresh, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = if (loading) "正在刷新" else "刷新",
                tint = if (loading) ZhishengMint else ZhishengOrange,
                modifier = Modifier.size(20.dp).rotate(if (loading) angle else 0f),
            )
        }
        IconButton(onClick = onSettings, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = "设置", tint = ZhishengTextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

// —— 交错入场动画容器（50ms 步进，300ms，M3 标准缓动） ——
// entered 由 WeatherContent 统一持有：只在数据首次入场时播放一次交错动画。
// 开关不能 remember 在 item 内部——LazyColumn 快滑时新入屏的 item 才现场组合，
// 逐项重置开关会重放淡入（还有 index*50ms 延迟），表现为快滑时卡片空白、停下才冒出来。
// 状态提升后，滚动中/回收后重组的卡片读到 entered=true，animateFloatAsState 初值即 1f，直接可见。
@Composable
private fun Stagger(index: Int, entered: Boolean, content: @Composable (Modifier) -> Unit) {
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        if (entered) 1f else 0f, tween(300, delayMillis = index * 50, easing = FastOutSlowInEasing), label = "sa",
    )
    val dy by androidx.compose.animation.core.animateFloatAsState(
        if (entered) 0f else 20f, tween(300, delayMillis = index * 50, easing = FastOutSlowInEasing), label = "sd",
    )
    content(
        Modifier.graphicsLayerAlpha(alpha, dy)
    )
}

private fun Modifier.graphicsLayerAlpha(a: Float, t: Float) =
    this.then(Modifier.graphicsLayer { alpha = a; translationY = t })

@Composable
private fun WeatherContent(
    data: WeatherData,
    city: com.zhisheng.weather.model.City?,
    unit: String,
    showTyphoon: Boolean,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    staleAgeMillis: Long?,
    listState: LazyListState,
) {
    // 入场动画总开关：状态提升到 LazyColumn 之上，只驱动一次交错入场（v0.0.1 修复快滑闪卡）
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    // 区块编号改为渲染时现算：原来靠一个与渲染顺序不一致的 visible 数组预推，
    // 某些区块缺失时编号会跳号/错位（v0.0.2）
    var seq = 0
    var stagger = 0
    val nextIndex = { ++seq }
    val nextStagger = { stagger++ }

    val zone = HeroTemps.zoneOf(data.utcOffsetSeconds)
    val showHourly = data.hourly.isNotEmpty()
    val showPrecip = prefs.showPrecip && Nowcast.shouldShowPrecipModule(data, System.currentTimeMillis())
    val showDaily = data.daily.isNotEmpty()
    val showTele = prefs.showTelemetry && data.current != null
    val showAqi = prefs.showAqi && data.aqi != null
    val showIndices = prefs.showIndices &&
        (data.carWashOk != null || data.sportsOk != null || data.extraIndices.isNotEmpty())
    val showYesterday = prefs.showYesterday && data.yesterday != null
    val showTy = showTyphoon && data.typhoons.isNotEmpty()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item { StatusLine(city, data, staleAgeMillis, zone) }
        data.current?.let { cur ->
            item { Stagger(nextStagger(), entered) { m -> HeroSection(cur, data, unit, prefs, zone, m) } }
        }
        if (data.alerts.isNotEmpty()) {
            item { Stagger(nextStagger(), entered) { m -> AlertSection(data.alerts.take(3), m) } }
        }
        if (showHourly) {
            val n = nextIndex()
            item { SectionTitle(n, "逐时预报", "HOURLY") }
            item { Stagger(nextStagger(), entered) { m -> HourlySection(data.hourly, unit, prefs.windUnit, zone, m) } }
        }
        if (showPrecip) {
            val n = nextIndex()
            item { SectionTitle(n, "分钟降水", "PRECIP") }
            item { Stagger(nextStagger(), entered) { m -> PrecipCard(data, m) } }
        }
        if (showDaily) {
            val n = nextIndex()
            item { SectionTitle(n, "逐日预报", "FORECAST") }
            item { Stagger(nextStagger(), entered) { m -> DailySection(data.daily, unit, prefs.windUnit, zone, m) } }
        }
        if (showTele) {
            val n = nextIndex()
            item { SectionTitle(n, "遥测数据", "TELEMETRY") }
            item {
                Stagger(nextStagger(), entered) { m ->
                    TelemetryGrid(data.current!!, data.daily.firstOrNull(), unit, prefs, m)
                }
            }
        }
        if (showAqi) {
            val n = nextIndex()
            item { SectionTitle(n, "空气质量", "AIR QUALITY") }
            item { Stagger(nextStagger(), entered) { m -> AqiCard(data.aqi!!, m) } }
        }
        if (showIndices) {
            val n = nextIndex()
            item { SectionTitle(n, "生活指数", "INDICES") }
            item { Stagger(nextStagger(), entered) { m -> IndicesRow(data.carWashOk, data.sportsOk, data.extraIndices, m) } }
        }
        if (showYesterday) {
            val n = nextIndex()
            item { SectionTitle(n, "昨日复盘", "RETRO") }
            item { Stagger(nextStagger(), entered) { m -> YesterdayCard(data.yesterday!!, data.daily.firstOrNull(), unit, m) } }
        }
        if (showTy) {
            val n = nextIndex()
            item { SectionTitle(n, "台风关注", "TYPHOON") }
            item { Stagger(nextStagger(), entered) { m -> TyphoonCard(data.typhoons, m) } }
        }
        item { Stagger(nextStagger(), entered) { m -> Footer(data, m) } }
    }
}

// —— 状态行：坐标 / 更新时间 / 数据源 ——
@Composable
private fun StatusLine(
    city: com.zhisheng.weather.model.City?,
    data: WeatherData,
    staleAgeMillis: Long?,
    zone: ZoneId,
) {
    val coord = city?.let {
        // 负坐标按 S/W 显示，避免出现 "-33.90N" 这种矛盾写法（v0.0.1）
        String.format(
            Locale.US, "%.2f%s %.2f%s",
            Math.abs(it.latitude), if (it.latitude >= 0) "N" else "S",
            Math.abs(it.longitude), if (it.longitude >= 0) "E" else "W",
        )
    } ?: "----"
    // 离线缓存兜底时标注缓存年龄（<10 分钟不打扰，只给正常更新时间）
    val updText = if (staleAgeMillis != null && staleAgeMillis >= 10 * 60_000L) {
        "UPD ${staleAgeMillis / 60_000L}分钟前 · 缓存"
    } else {
        "UPD ${data.updateTime?.let { formatTime(it, zone) } ?: "--"}"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = coord,
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
            letterSpacing = 1.sp,
            maxLines = 1,
            softWrap = false,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "$updText // SRC ${dataSourceShortLabel(data.dataSource)}${supplementShortLabel(data)}",
            style = MaterialTheme.typography.labelSmall,
            color = if (staleAgeMillis != null && staleAgeMillis >= 10 * 60_000L) ZhishengOrange else ZhishengTextTertiary,
            letterSpacing = 1.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}

// —— Hero：大温度 + 数字滚动 + 大图标 ——
@Composable
private fun HeroSection(
    cur: CurrentWeather,
    data: WeatherData,
    unit: String,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    zone: ZoneId,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = cur.weatherText ?: cur.condition?.label ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZhishengOrange,
                    fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.Top) {
                    AnimatedTemp(cur.temperature, unit)
                    Text(
                        text = "°",
                        style = MaterialTheme.typography.displayLarge,
                        color = ZhishengOrange,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(2.dp))
                val range = HeroTemps.range(data.daily, data.yesterday, System.currentTimeMillis(), zone)
                Text(
                    text = buildString {
                        if (HeroTemps.showFeelsLike(cur.temperature, cur.feelsLike)) {
                            append("体感${Fmt.temp(cur.feelsLike, unit)}°")
                        }
                        if (range.hasAny) {
                            if (isNotEmpty()) append("  ")
                            range.left?.let { append("${range.leftLabel}${Fmt.temp(it, unit)}°") }
                            range.right?.let {
                                if (range.left != null) append(" ")
                                append("${range.rightLabel}${Fmt.temp(it, unit)}°")
                            }
                        }
                        if (isEmpty()) append("—")
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = ZhishengTextSecondary,
                    maxLines = 1,
                )
                // 风况直接进 Hero：最常看的一项，不用再往下滚到遥测区
                windLabel(cur, prefs.windUnit)?.let { w ->
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "风 $w",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZhishengTextTertiary,
                        maxLines = 1,
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                // 六边形 AT 力场底纹（Canvas lambda 非 composable 上下文，颜色提前取值）
                val hexOuter = ZhishengOrange.copy(alpha = 0.22f)
                val hexInner = ZhishengCyan.copy(alpha = 0.12f)
                Canvas(modifier = Modifier.size(116.dp)) {
                    val c = center
                    val r = size.minDimension / 2f
                    val path = Path().apply {
                        for (i in 0 until 6) {
                            val a = Math.toRadians(60.0 * i - 30.0)
                            val p = Offset(c.x + r * Math.cos(a).toFloat(), c.y + r * Math.sin(a).toFloat())
                            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                        }
                        close()
                    }
                    drawPath(path, hexOuter, style = Stroke(1.5f))
                    drawPath(
                        androidx.compose.ui.graphics.Path().apply {
                            val r2 = r * 0.82f
                            for (i in 0 until 6) {
                                val a = Math.toRadians(60.0 * i - 30.0)
                                val p = Offset(c.x + r2 * Math.cos(a).toFloat(), c.y + r2 * Math.sin(a).toFloat())
                                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                            }
                            close()
                        },
                        hexInner,
                        style = Stroke(1f),
                    )
                }
                WeatherIcon(cur.condition, Modifier.size(76.dp))
            }
        }
        Nowcast.briefingLine(data, unit, System.currentTimeMillis())?.let { raw ->
            val line = Nowcast.tidyCopy(raw)
            val rain = Nowcast.rainTiming(
                data.rainMinutes,
                System.currentTimeMillis(),
                currentPrecip = cur.condition?.isPrecipitation == true || (cur.precipMm ?: 0.0) > 0.05,
            )
            val color = if (rain.hasRain || Nowcast.looksLikeIncomingRain(line)) {
                ZhishengOrange
            } else {
                ZhishengMint
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = line,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// 温度数字滚动（400ms，emphasizedDecelerate 近似）
@Composable
private fun AnimatedTemp(celsius: Double?, unit: String) {
    val target = celsius?.let { if (unit == "f") it * 9.0 / 5.0 + 32.0 else it }
    // remember / LaunchedEffect 必须无条件调用：温度从有到无（或缺测补上）时
    // 不能提前 return，否则会打乱 hook 顺序。
    val anim = remember { Animatable((target ?: 0.0).toFloat()) }
    var hasValue by remember { mutableStateOf(celsius != null) }
    LaunchedEffect(target) {
        if (target == null) {
            hasValue = false
            return@LaunchedEffect
        }
        if (!hasValue) {
            anim.snapTo(target.toFloat())
            hasValue = true
        } else {
            anim.animateTo(target.toFloat(), tween(400))
        }
    }
    Text(
        text = if (!hasValue) "--" else anim.value.roundToInt().toString(),
        style = MaterialTheme.typography.displayLarge,
        color = ZhishengText,
        fontWeight = FontWeight.Bold,
    )
}

// —— 预警横幅：警示斜纹 + 按等级着色边框 ——
@Composable
private fun AlertSection(alerts: List<AlertInfo>, modifier: Modifier) {
    // 展开态按标题记忆：原来按列表位置 remember，预警条数变化时展开态会错位到别条（v0.0.2）
    val expandedTitles = remember { mutableStateListOf<String>() }
    // 单一闪烁时钟：原来每条预警各起一个 while(true)，多条预警时多个协程各自计时（v0.0.2）
    val blinkOn = rememberBlink()
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        alerts.forEach { alert ->
            val expanded = alert.title in expandedTitles
            // v0.0.4：三源等级归一后按国标四档着色，未识别档退回警报红
            val c = alertColor(alert.severity)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RectangleShape)
                    .background(ZhishengCard)
                    .border(1.dp, c.copy(alpha = 0.7f), RectangleShape)
                    .clickable {
                        if (expanded) expandedTitles.remove(alert.title)
                        else expandedTitles.add(alert.title)
                    }
                    .padding(0.dp),
            ) {
                // 顶部警示斜纹
                Canvas(modifier = Modifier.fillMaxWidth().height(5.dp)) {
                    hazardStripes(this, c.copy(alpha = 0.75f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BlinkDot(blinkOn, c)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(alert.title, style = MaterialTheme.typography.titleSmall, color = c, fontWeight = FontWeight.Bold)
                        alert.pubTime?.let {
                            Text(formatAlertTime(it), style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                        }
                    }
                    Text(
                        if (expanded) "[-]" else "[+]",
                        style = MaterialTheme.typography.labelMedium,
                        color = c,
                    )
                }
                if (expanded && !alert.detail.isNullOrBlank()) {
                    HorizontalDivider(color = c.copy(alpha = 0.3f), thickness = 1.dp)
                    Text(
                        alert.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = ZhishengTextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

// 预警等级 → 着色（国标蓝/黄/橙/红；黄色按主题取色板 warning：深色荧光黄 / 浅色油墨黄）
@Composable
private fun alertColor(level: com.zhisheng.weather.model.AlertLevel): Color = when (level) {
    com.zhisheng.weather.model.AlertLevel.RED -> ZhishengRed
    com.zhisheng.weather.model.AlertLevel.ORANGE -> ZhishengOrange
    com.zhisheng.weather.model.AlertLevel.YELLOW -> ZhishengWarning
    com.zhisheng.weather.model.AlertLevel.BLUE -> ZhishengCyan
    com.zhisheng.weather.model.AlertLevel.UNKNOWN -> ZhishengRed
}

private fun hazardStripes(scope: DrawScope, color: Color) {
    with(scope) {
        val w = 10f
        var x = -size.height
        while (x < size.width) {
            val path = Path().apply {
                moveTo(x, size.height)
                lineTo(x + size.height, 0f)
                lineTo(x + size.height + w, 0f)
                lineTo(x + w, size.height)
                close()
            }
            drawPath(path, color)
            x += w * 2.4f
        }
    }
}

// 1Hz 闪烁时钟：整个预警区共用一个，随 composable 离开屏幕自动停
@Composable
private fun rememberBlink(): Boolean {
    var on by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            on = !on
        }
    }
    return on
}

@Composable
private fun BlinkDot(on: Boolean, color: Color? = null) {
    // 默认取主题警报红（composable getter 不能出现在默认参数表达式里，v0.0.5）
    val c = color ?: ZhishengRed
    Box(
        Modifier
            .size(8.dp)
            .background(if (on) c else c.copy(alpha = 0.25f)),
    )
}

@Composable
private fun SectionTitle(index: Int, title: String, en: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("%02d//".format(index), style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = ZhishengTextSecondary, letterSpacing = 2.sp)
        Spacer(Modifier.width(8.dp))
        Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.5.sp)
        Spacer(Modifier.weight(1f))
        Text("─".repeat(6), style = MaterialTheme.typography.labelSmall, color = ZhishengCardBorder)
    }
}

// —— 角括号 HUD 卡片 ——
@Composable
private fun HudCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .then(Modifier.hudBorder())
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        content()
    }
}

@Composable
private fun Modifier.hudBorder() = this
    .border(1.dp, ZhishengCardBorder, RectangleShape)
    .padding(0.dp)
    .then(
        Modifier.drawCornerBrackets(ZhishengOrange)
    )

private fun Modifier.drawCornerBrackets(color: Color) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val len = 7.dp.toPx()
        val w = 1.6.dp.toPx()
        // 四角 L 形
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), w)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), w)
        drawLine(color, Offset(size.width, 0f), Offset(size.width - len, 0f), w)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, len), w)
        drawLine(color, Offset(0f, size.height), Offset(len, size.height), w)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - len), w)
        drawLine(color, Offset(size.width, size.height), Offset(size.width - len, size.height), w)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - len), w)
    }
)

// —— 逐时：横向滚动 + 连续温度曲线 + 降水概率 ——
// v0.0.2 重做：原实现每格各画「本格中心→本格右边」的半段贝塞尔，格与格首尾不相接，
// 视觉上是一串断开的小弧线（用户反馈"那个线很丑"）。现改为每格画
// 「左邻中点→本格中心→右邻中点」的连续折线 + 渐隐面积填充，跨格严丝合缝。
@Composable
private fun HourlySection(
    hourly: List<HourlyWeather>,
    unit: String,
    windUnit: String,
    zone: ZoneId,
    modifier: Modifier,
) {
    val temps = hourly.mapNotNull { h -> conv(h.temperature, unit) }
    val minT = temps.minOrNull() ?: 0.0
    val maxT = temps.maxOrNull() ?: 1.0
    // 0.0.9-debug 修复：原实现每格独立用 ±40 分钟双向容差判「现在」，
    // :20-:40 之间上一整点与下一整点同时命中，两格都标「现在」并高亮。
    // 改为在父层算唯一「现在」格：优先取包含当前时刻的小时格（10:50 属于
    // 10:00 格），找不到（该格已被 dropPastHourly 裁掉）再退回 40 分钟
    // 窗口内最近的一格；均无则不标。
    val nowMs = System.currentTimeMillis()
    val nowIdx = hourly.indexOfFirst { h ->
        h.timeMillis <= nowMs && nowMs < h.timeMillis + 3_600_000L
    }.takeIf { it >= 0 } ?: hourly.indices
        .filter { kotlin.math.abs(hourly[it].timeMillis - nowMs) <= 40 * 60_000L }
        .minByOrNull { kotlin.math.abs(hourly[it].timeMillis - nowMs) } ?: -1
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            // key=时间戳：数据刷新时按身份复用 item，不整列重绑（v0.0.1）
            LazyRow(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                itemsIndexed(hourly, key = { _, h -> h.timeMillis }) { i, h ->
                    HourlyItem(
                        h = h,
                        prev = hourly.getOrNull(i - 1),
                        next = hourly.getOrNull(i + 1),
                        unit = unit,
                        minT = minT,
                        maxT = maxT,
                        isNow = i == nowIdx,
                        windUnit = windUnit,
                        zone = zone,
                    )
                }
            }
            // 图例：底部两行数字分别是降水概率与风速，去掉每格的 km/h 后在此说明一次
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (hourly.any { it.precipProb != null && it.precipProb > 0 }) {
                    Box(Modifier.size(width = 6.dp, height = 2.dp).background(ZhishengCyan))
                    Spacer(Modifier.width(5.dp))
                    Text("降水概率", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                    Spacer(Modifier.width(14.dp))
                }
                Box(Modifier.size(width = 6.dp, height = 2.dp).background(ZhishengTextTertiary))
                Spacer(Modifier.width(5.dp))
                Text(Fmt.windUnitLabel(windUnit), style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                Spacer(Modifier.weight(1f))
                Text(
                    "${hourly.size}H",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZhishengTextTertiary,
                )
            }
        }
    }
}

private fun conv(c: Double?, unit: String): Double? =
    c?.let { if (unit == "f") it * 9.0 / 5.0 + 32.0 else it }

// 归一化温度条参数：返回 (lo, hi, widthFraction)，均限制在 [0,1]。
// low/high 为数据源原始摄氏度；weekMin/weekMax 为已按 unit 换算的显示温度
// （与 DailySection 调用约定一致：weekMin/weekMax 由 lows/highs 经 conv 预算）。
// 提取为纯函数以便对 lo 接近 1 的极端温度分布做回归（v0.0.3）。
// 原内联写法 (hi-lo).coerceIn(0.03f, 1f-lo) 当 lo>0.97 时下界大于上界，
// Float.coerceIn 会抛 IllegalArgumentException，致逐日区域整体崩溃。
internal fun tempBarParams(
    low: Double?,
    high: Double?,
    weekMin: Double,
    weekMax: Double,
    unit: String,
): Triple<Float, Float, Float> {
    val range = (weekMax - weekMin).coerceAtLeast(1.0)
    val a = (((conv(low, unit) ?: weekMin) - weekMin) / range).toFloat()
    val b = (((conv(high, unit) ?: weekMax) - weekMin) / range).toFloat()
    val lo = minOf(a, b).coerceIn(0f, 1f)
    val hi = maxOf(a, b).coerceIn(0f, 1f)
    // 空间允许时保底 0.03f 可见；lo 接近 1 时收缩宽度，避免下界超过上界且不溢出右边界。
    val maxW = (1f - lo).coerceAtLeast(0f)
    val minW = minOf(0.03f, maxW)
    val w = (hi - lo).coerceIn(minW, maxW)
    return Triple(lo, hi, w)
}

// 昨日温差：按当前显示单位换算后取整再相减，保证 ΔT 与高低温读数一致（v0.0.3）。
// 原代码直接用原始摄氏度相减，华氏度模式下 ΔT 会和高低温读数对不上。
internal fun tempDelta(todayHigh: Double?, yesterdayHigh: Double?, unit: String): Int? {
    if (todayHigh == null || yesterdayHigh == null) return null
    return (conv(todayHigh, unit) ?: todayHigh).roundToInt() -
        (conv(yesterdayHigh, unit) ?: yesterdayHigh).roundToInt()
}

@Composable
private fun HourlyItem(
    h: HourlyWeather,
    prev: HourlyWeather?,
    next: HourlyWeather?,
    unit: String,
    minT: Double,
    maxT: Double,
    isNow: Boolean,
    windUnit: String,
    zone: ZoneId,
) {
    val itemW = 54.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(itemW),
    ) {
        Text(
            text = if (isNow) "现在" else formatHour(h.timeMillis, zone),
            style = MaterialTheme.typography.labelSmall,
            color = if (isNow) ZhishengMint else ZhishengTextTertiary,
        )
        Spacer(Modifier.height(6.dp))
        WeatherIcon(h.condition, Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        // 温度读数放在曲线正上方，视线不用来回跳
        Text(
            text = Fmt.temp(h.temperature, unit)?.let { "$it°" } ?: "--",
            style = MaterialTheme.typography.titleSmall,
            color = ZhishengText,
        )
        Spacer(Modifier.height(3.dp))
        // 连续曲线：左半段接上一格中点，右半段接下一格中点（颜色提前取值，Canvas lambda 非 composable）
        val curveMint = ZhishengMint
        val curveBg = ZhishengBg
        Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
            val range = (maxT - minT).coerceAtLeast(1.0).toFloat()
            val top = 4f
            val usable = size.height - top - 4f
            fun yOf(v: Double?): Float? = v?.let {
                size.height - 4f - ((it - minT).toFloat() / range) * usable
            }

            val cx = size.width / 2f
            val yCur = yOf(conv(h.temperature, unit)) ?: return@Canvas
            val yPrev = yOf(prev?.let { conv(it.temperature, unit) })
            val yNext = yOf(next?.let { conv(it.temperature, unit) })

            // 左右邻的中点：与相邻格画出的同一点重合，所以跨格连续
            val pLeft = yPrev?.let { Offset(0f, (it + yCur) / 2f) }
            val pRight = yNext?.let { Offset(size.width, (it + yCur) / 2f) }
            val pCur = Offset(cx, yCur)

            // 面积填充（曲线到底边），极淡，给折线一点体积感
            val fill = Path().apply {
                moveTo(pLeft?.x ?: cx, pLeft?.y ?: yCur)
                lineTo(pCur.x, pCur.y)
                pRight?.let { lineTo(it.x, it.y) }
                lineTo(pRight?.x ?: cx, size.height)
                lineTo(pLeft?.x ?: cx, size.height)
                close()
            }
            drawPath(fill, curveMint.copy(alpha = 0.07f))

            // 折线本体
            val line = Path().apply {
                moveTo(pLeft?.x ?: cx, pLeft?.y ?: yCur)
                lineTo(pCur.x, pCur.y)
                pRight?.let { lineTo(it.x, it.y) }
            }
            drawPath(line, curveMint.copy(alpha = 0.75f), style = Stroke(1.6f))

            // 「现在」格用实心亮点强调，其余用小空心点
            if (isNow) {
                drawCircle(curveMint, 3.2f, pCur)
            } else {
                drawCircle(curveBg, 2.6f, pCur)
                drawCircle(curveMint.copy(alpha = 0.85f), 2.6f, pCur, style = Stroke(1.2f))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = Fmt.probability(h.precipProb) ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengCyan,
        )
        Text(
            text = Fmt.windValue(h.windSpeed, windUnit) ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
        )
    }
}

// —— 分钟降水：柱状雷达图 ——
@Composable
private fun PrecipCard(data: WeatherData, modifier: Modifier) {
    // 0.0.9-debug 修复：离线缓存兜底时（staleAgeMillis 可 ≥10 分钟），分钟序列
    // 仍从抓取时刻起画——已过去的柱被画在紧贴 "NOW" 标签的位置，像是正在下。
    // 绘制前裁掉 2 分钟窗口之前的历史柱；全裁空就保持空，绝不把过期雨柱复活成“现在”。
    val minutes = data.rainMinutes
        .filter { it.timeMillis >= System.currentTimeMillis() - Nowcast.NOW_WINDOW_MS }
    val rainDistanceKm = data.rainDistanceKm
    val precipNow = data.current.let { cur ->
        cur != null && (cur.condition?.isPrecipitation == true || (cur.precipMm ?: 0.0) > 0.05)
    }
    val chartCeiling = Nowcast.precipChartCeiling(minutes)
    val dry = chartCeiling <= 0f
    val timingLabel = Nowcast.rainTimingLabel(
        Nowcast.rainTiming(minutes, System.currentTimeMillis(), currentPrecip = precipNow),
    )
    val horizonLabel = Nowcast.horizonLabel(minutes)
    val peak = minutes.maxOfOrNull { it.precip }?.coerceAtLeast(0f) ?: 0f
    val distanceLabel = rainDistanceKm?.takeIf { it > 0.0 }?.let { km ->
        if (km == Math.floor(km)) km.toInt().toString() else String.format(Locale.US, "%.1f", km)
    }
    val statusText = timingLabel ?: when {
        !dry -> data.rainNowcast?.trim()?.takeIf { it.isNotEmpty() } ?: "未来 2 小时有降水"
        distanceLabel != null -> "近处无雨 · 雨区距此 $distanceLabel km"
        else -> "未来 2 小时无降水"
    }
    // Canvas lambda 非 composable，颜色提前取值。
    val barCyan = ZhishengCyan.copy(alpha = 0.85f)
    val barBorder = ZhishengCardBorder
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .semantics {
                    contentDescription = if (dry) statusText
                    else "$statusText，峰值 ${String.format(Locale.US, "%.2f", peak)} 毫米每小时"
                },
        ) {
            Row(Modifier.fillMaxWidth().height(38.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(if (dry) ZhishengMint else ZhishengOrange),
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (dry) "CLEAR WINDOW" else "PRECIP WINDOW",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZhishengTextTertiary,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (dry) ZhishengMint else ZhishengOrange,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        String.format(Locale.US, "%.2f", peak),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (dry) ZhishengTextSecondary else ZhishengCyan,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("mm/h", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("NOW", style = MaterialTheme.typography.labelSmall, color = ZhishengOrange, letterSpacing = 0.8.sp)
                Spacer(Modifier.width(8.dp))
                Canvas(Modifier.weight(1f).fillMaxHeight()) {
                    val baseline = size.height - 1.dp.toPx()
                    drawLine(barBorder, Offset(0f, baseline), Offset(size.width, baseline), 1.dp.toPx())
                    if (!dry && minutes.isNotEmpty()) {
                        val bw = size.width / minutes.size
                        val minWetHeight = 2.dp.toPx()
                        minutes.forEachIndexed { i, minute ->
                            if (minute.precip > 0f) {
                                val scaled = (minute.precip / chartCeiling).coerceIn(0f, 1f)
                                val hgt = (scaled * (size.height - 2.dp.toPx())).coerceAtLeast(minWetHeight)
                                drawRect(
                                    color = barCyan,
                                    topLeft = Offset(i * bw + bw * 0.14f, baseline - hgt),
                                    size = androidx.compose.ui.geometry.Size((bw * 0.72f).coerceAtLeast(1f), hgt),
                                )
                            }
                        }
                    } else {
                        val dotRadius = 1.dp.toPx()
                        listOf(0.25f, 0.5f, 0.75f).forEach { x ->
                            drawCircle(barBorder, dotRadius, Offset(size.width * x, baseline))
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(horizonLabel, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
            }
        }
    }
}

// —— 逐日：全周归一化温度区间条 ——
@Composable
private fun DailySection(
    daily: List<DailyWeather>,
    unit: String,
    windUnit: String,
    zone: ZoneId,
    modifier: Modifier,
) {
    val lows = daily.mapNotNull { conv(it.low, unit) }
    val highs = daily.mapNotNull { conv(it.high, unit) }
    val weekMin = lows.minOrNull() ?: 0.0
    val weekMax = highs.maxOrNull() ?: 1.0
    var expandedMillis by remember { mutableStateOf<Long?>(null) }

    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            daily.forEachIndexed { index, d ->
                val expanded = expandedMillis == d.dateMillis
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { expandedMillis = if (expanded) null else d.dateMillis }
                        .padding(vertical = 6.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatWeekday(d.dateMillis, index, zone),
                            modifier = Modifier.width(44.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (index == 0) ZhishengMint else ZhishengText,
                        )
                        WeatherIcon(d.condition, Modifier.size(22.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = Fmt.probability(d.precipProbability) ?: "  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengCyan,
                            modifier = Modifier.width(30.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                        Text(
                            Fmt.temp(d.low, unit)?.let { "$it°" } ?: "--",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZhishengTextTertiary,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.End,
                        )
                        // 归一化温度条
                        BoxWithConstraints(
                            Modifier.padding(horizontal = 8.dp).weight(1f).height(4.dp)
                                .background(ZhishengTextTertiary.copy(alpha = 0.3f), RectangleShape)
                        ) {
                            // lo/hi/w 经 tempBarParams 统一归一：源数据偶发把高低温写反（小米 from/to
                            // 语义不定），且 lo 接近 1 时需收缩宽度避免 coerceIn 下界超过上界（v0.0.3）
                            val (lo, _, w) = tempBarParams(d.low, d.high, weekMin, weekMax, unit)
                            Box(
                                Modifier
                                    .offset(x = maxWidth * lo)
                                    .width(maxWidth * w)
                                    .fillMaxHeight()
                                    .background(tempColor(d.low), RectangleShape),
                            )
                        }
                        Text(
                            Fmt.temp(d.high, unit)?.let { "$it°" } ?: "--",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZhishengText,
                            modifier = Modifier.width(34.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                    if (expanded) {
                        DailyExpanded(d, windUnit)
                    }
                }
                if (index < daily.size - 1) {
                    HorizontalDivider(color = ZhishengCardBorder.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun DailyExpanded(d: DailyWeather, windUnit: String) {
    Column(Modifier.padding(start = 50.dp, top = 6.dp, end = 4.dp)) {
        d.weatherText?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = ZhishengMint)
            Spacer(Modifier.height(4.dp))
        }
        d.windSpeed?.let {
            Text("风 ${Fmt.wind(it, windUnit)}", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
            Spacer(Modifier.height(4.dp))
        }
        Row {
            d.sunrise?.let {
                Text("日出 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengOrange)
                Spacer(Modifier.width(14.dp))
            }
            d.sunset?.let {
                Text("日落 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengOrange)
            }
        }
        d.precipMm?.takeIf { it > 0.0 }?.let { mm ->
            Spacer(Modifier.height(4.dp))
            Text(
                "降水 ${if (mm == Math.floor(mm)) mm.toInt().toString() else String.format(java.util.Locale.US, "%.1f", mm)} mm",
                style = MaterialTheme.typography.labelSmall,
                color = ZhishengCyan,
            )
        }
        if (d.moonPhase != null || d.moonrise != null || d.moonset != null) {
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    "月相 ${Fmt.moonPhaseZh(d.moonPhase) ?: "--"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZhishengCyan,
                )
                Spacer(Modifier.width(14.dp))
                d.moonrise?.let {
                    Text("月出 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengCyan)
                    Spacer(Modifier.width(14.dp))
                }
                d.moonset?.let {
                    Text("月落 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengCyan)
                }
            }
        }
    }
}

// 温度色：两段插值 钢青 → 翡翠 → 琥珀（单段青→橙的中点会发灰发脏，v0.0.5 盘查）
@Composable
private fun tempColor(low: Double?): Color {
    val t = (((low ?: 10.0) + 10.0) / 45.0).toFloat().coerceIn(0f, 1f)
    return if (t < 0.5f) {
        colorLerp(ZhishengCyan, ZhishengMint, t * 2f)
    } else {
        colorLerp(ZhishengMint, ZhishengOrange, (t - 0.5f) * 2f)
    }
}

// —— 遥测卡格：2 列 HUD 小卡 ——
@Composable
private fun TelemetryGrid(
    cur: CurrentWeather,
    today: DailyWeather?,
    unit: String,
    prefs: com.zhisheng.weather.ui.DisplayPrefs,
    modifier: Modifier,
) {
    // 没数的格不画：小米实况没有 1 时降水，硬留第九格会 -- 还在右侧留空（v0.0.7）。
    val items = listOf(
        Triple("湿度", "HUMIDITY", cur.humidity?.let { "${it.roundToInt()}%" }),
        Triple("风向风速", "WIND", windLabel(cur, prefs.windUnit)),
        Triple("气压", "PRESS", Fmt.pressure(cur.pressure, prefs.pressureUnit)),
        Triple("紫外线", "UV", cur.uvIndex?.let { uvText(it) }),
        Triple("能见度", "VIS", cur.visibility?.let { "${it.roundToInt()} km" }),
        Triple("露点", "DEW", cur.dewPoint?.let { "${Fmt.temp(it, unit)}°" }),
        Triple("云量", "CLOUD", cur.cloudCover?.let { "${it.roundToInt()}%" }),
        Triple("阵风", "GUST", Fmt.wind(cur.windGust, prefs.windUnit)),
        Triple("1时降水", "PRECIP", cur.precipMm?.let { String.format(Locale.US, "%.1f mm", it) }),
    ).mapNotNull { (cn, en, value) -> value?.let { Triple(cn, en, it) } }
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (rowItems.size == 1) {
                    val (cn, en, value) = rowItems[0]
                    TeleCell(cn, en, value, cur, Modifier.fillMaxWidth())
                } else {
                    rowItems.forEach { (cn, en, value) ->
                        TeleCell(cn, en, value, cur, Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        // 日月宽卡：公共源不提供月出月落时由本地天文计算补齐。
        if (today != null && (
                today.sunrise != null || today.sunset != null || today.moonPhase != null ||
                    today.moonrise != null || today.moonset != null
                )
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RectangleShape)
                    .background(ZhishengSurface)
                    .border(1.dp, ZhishengCardBorder, RectangleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column {
                    TeleLabel("日月", "LUMINARY")
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        today?.sunrise?.let {
                            Text("日出 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                            Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(18.dp))
                        today?.sunset?.let {
                            Text("日落 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                            Text(it, style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("月相 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                        Text(
                            Fmt.moonPhaseZh(today.moonPhase) ?: "--",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZhishengCyan,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("月出 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                        Text(today.moonrise ?: "--", style = MaterialTheme.typography.titleSmall, color = ZhishengCyan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(18.dp))
                        Text("月落 ", style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
                        Text(today.moonset ?: "--", style = MaterialTheme.typography.titleSmall, color = ZhishengCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeleCell(
    cn: String,
    en: String,
    value: String,
    cur: CurrentWeather,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .border(1.dp, ZhishengCardBorder, RectangleShape)
            .drawCornerBrackets(ZhishengOrange.copy(alpha = 0.8f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column {
            TeleLabel(cn, en)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (en == "WIND" && cur.windDirectionDeg != null) {
                    WindCompass(cur.windDirectionDeg)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    color = ZhishengText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun WindCompass(degrees: Double) {
    val ring = ZhishengCardBorder
    val north = ZhishengOrange
    val vector = ZhishengCyan
    val surface = ZhishengSurface
    val shadow = ZhishengBg.copy(alpha = 0.92f)
    val rimHighlight = ZhishengTextTertiary.copy(alpha = 0.65f)
    val needleHighlight = ZhishengText.copy(alpha = 0.55f)
    val hubHighlight = ZhishengText.copy(alpha = 0.7f)
    Box(
        Modifier
            .size(38.dp)
            .semantics { contentDescription = "风向 ${degrees.roundToInt()} 度" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val ovalLeft = 2.dp.toPx()
            val ovalTop = 10.dp.toPx()
            val ovalSize = androidx.compose.ui.geometry.Size(size.width - 4.dp.toPx(), 22.dp.toPx())
            // 扁椭圆底座 + 上缘高光 / 下缘阴影，制造悬浮罗盘的纵深。
            drawOval(shadow, Offset(ovalLeft, ovalTop + 2.dp.toPx()), ovalSize)
            drawOval(surface, Offset(ovalLeft, ovalTop), ovalSize)
            drawOval(ring, Offset(ovalLeft, ovalTop), ovalSize, style = Stroke(1.2.dp.toPx()))
            drawArc(
                color = rimHighlight,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(ovalLeft + 1.dp.toPx(), ovalTop + 1.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(ovalSize.width - 2.dp.toPx(), ovalSize.height - 2.dp.toPx()),
                style = Stroke(0.8.dp.toPx()),
            )
            val c = Offset(size.width / 2f, ovalTop + ovalSize.height / 2f)
            drawLine(north, Offset(c.x, ovalTop - 2.dp.toPx()), Offset(c.x, ovalTop + 3.dp.toPx()), 1.8.dp.toPx())
            drawCircle(ring, 3.8.dp.toPx(), c, style = Stroke(1.dp.toPx()))
        }
        // 指针与文字共用同一“来向”角度：北=0°向上、东=90°向右，不再额外翻转 180°。
        Canvas(Modifier.fillMaxSize().rotate(degrees.toFloat())) {
            val c = Offset(size.width / 2f, 21.dp.toPx())
            val headY = 3.dp.toPx()
            val tailY = 30.dp.toPx()
            val depth = Offset(1.4.dp.toPx(), 1.6.dp.toPx())
            drawLine(shadow, Offset(c.x, tailY) + depth, Offset(c.x, headY + 6.dp.toPx()) + depth, 4.4.dp.toPx())
            drawLine(vector.copy(alpha = 0.45f), Offset(c.x, tailY), Offset(c.x, headY + 6.dp.toPx()), 4.dp.toPx())
            drawLine(vector, Offset(c.x - 0.7.dp.toPx(), tailY), Offset(c.x - 0.7.dp.toPx(), headY + 6.dp.toPx()), 1.7.dp.toPx())
            val headShadow = Path().apply {
                moveTo(c.x + depth.x, headY + depth.y)
                lineTo(c.x - 5.dp.toPx() + depth.x, headY + 8.dp.toPx() + depth.y)
                lineTo(c.x + 5.dp.toPx() + depth.x, headY + 8.dp.toPx() + depth.y)
                close()
            }
            drawPath(headShadow, shadow)
            val head = Path().apply {
                moveTo(c.x, headY)
                lineTo(c.x - 5.dp.toPx(), headY + 8.dp.toPx())
                lineTo(c.x + 5.dp.toPx(), headY + 8.dp.toPx())
                close()
            }
            drawPath(head, vector)
            drawLine(
                needleHighlight,
                Offset(c.x - 1.5.dp.toPx(), headY + 2.dp.toPx()),
                Offset(c.x - 3.6.dp.toPx(), headY + 6.5.dp.toPx()),
                0.8.dp.toPx(),
            )
            drawCircle(shadow, 3.7.dp.toPx(), Offset(c.x, tailY) + depth)
            drawCircle(north, 3.2.dp.toPx(), Offset(c.x, tailY))
            drawCircle(shadow, 3.8.dp.toPx(), c)
            drawCircle(vector, 3.2.dp.toPx(), c)
            drawCircle(hubHighlight, 1.dp.toPx(), Offset(c.x - 0.8.dp.toPx(), c.y - 0.8.dp.toPx()))
        }
    }
}

@Composable
private fun TeleLabel(cn: String, en: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 3.dp, height = 8.dp).background(ZhishengOrange))
        Spacer(Modifier.width(6.dp))
        Text(cn, style = MaterialTheme.typography.labelMedium, color = ZhishengTextSecondary)
        Spacer(Modifier.width(6.dp))
        Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
    }
}

private fun windLabel(cur: CurrentWeather, windUnit: String): String? {
    val dir = com.zhisheng.weather.data.WeatherRepository.windDirection(cur.windDirectionDeg)
    val speed = Fmt.wind(cur.windSpeed, windUnit)
    return when {
        dir != null && speed != null -> "$dir $speed"
        dir != null -> dir
        speed != null -> speed
        else -> null
    }
}

private fun uvText(uv: Int): String = when {
    uv <= 2 -> "$uv 弱"
    uv <= 5 -> "$uv 中等"
    uv <= 7 -> "$uv 强"
    uv <= 10 -> "$uv 很强"
    else -> "$uv 极强"
}

// —— AQI ——
@Composable
private fun AqiCard(aqi: AqiInfo, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = aqi.value?.toString() ?: "--",
                    style = MaterialTheme.typography.displaySmall,
                    color = aqiColor(aqi.value),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(aqi.level ?: "空气质量", style = MaterialTheme.typography.titleMedium, color = aqiColor(aqi.value), fontWeight = FontWeight.Bold)
                    Text("AQI // AIR QUALITY INDEX", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                aqi.primary?.let {
                    Text("首要污染物 $it", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                }
            }
            Spacer(Modifier.height(10.dp))
            // 刻度尺 + 游标
            Box(Modifier.fillMaxWidth().height(4.dp).background(ZhishengCardBorder, RectangleShape)) {
                Box(
                    Modifier
                        .fillMaxWidth((aqi.value?.toFloat() ?: 0f).coerceIn(0f, 500f) / 500f)
                        .height(4.dp)
                        .background(aqiColor(aqi.value), RectangleShape),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PollutantChip("PM2.5", aqi.pm25, Modifier.weight(1f))
                PollutantChip("PM10", aqi.pm10, Modifier.weight(1f))
                PollutantChip("O3", aqi.o3, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PollutantChip("NO2", aqi.no2, Modifier.weight(1f))
                PollutantChip("SO2", aqi.so2, Modifier.weight(1f))
                PollutantChip("CO", aqi.co, Modifier.weight(1f))
            }
            // 健康建议（v0.0.4：小米 suggest 接入，其余源无此行）
            aqi.suggest?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
            }
        }
    }
}

@Composable
private fun PollutantChip(name: String, value: String?, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RectangleShape)
            .background(ZhishengCard)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
        Spacer(Modifier.weight(1f))
        Text(value ?: "--", style = MaterialTheme.typography.titleSmall, color = ZhishengText)
    }
}

@Composable
private fun aqiColor(value: Int?): Color = when {
    value == null -> ZhishengTextTertiary
    value <= 50 -> ZhishengMint
    value <= 100 -> ZhishengMint.copy(alpha = 0.8f)
    value <= 150 -> ZhishengOrange
    value <= 200 -> ZhishengOrange.copy(alpha = 0.85f)
    value <= 300 -> ZhishengRed
    else -> ZhishengRed.copy(alpha = 0.8f)
}

// —— 生活指数 ——
@Composable
private fun IndicesRow(carWashOk: Boolean?, sportsOk: Boolean?, extra: List<com.zhisheng.weather.model.LifeIndexExtra>, modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            carWashOk?.let { IndexChip("洗车", "CAR WASH", it, Modifier.weight(1f)) }
            sportsOk?.let { IndexChip("运动", "SPORTS", it, Modifier.weight(1f)) }
        }
        if (extra.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            extra.chunked(2).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { ix ->
                        Row(
                            Modifier.weight(1f)
                                .clip(RectangleShape)
                                .background(ZhishengSurface)
                                .border(1.dp, ZhishengCardBorder, RectangleShape)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                TeleLabel(ix.name, ix.en)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    ix.category.ifBlank { "--" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ZhishengText,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun IndexChip(cn: String, en: String, ok: Boolean, modifier: Modifier = Modifier) {
    val c = if (ok) ZhishengMint else ZhishengOrange
    Row(
        modifier
            .clip(RectangleShape)
            .background(ZhishengSurface)
            .border(1.dp, c.copy(alpha = 0.5f), RectangleShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            TeleLabel(cn, en)
            Spacer(Modifier.height(4.dp))
            Text(
                if (ok) "适宜" else "不适宜",
                style = MaterialTheme.typography.titleMedium,
                color = c,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(if (ok) "[OK]" else "[NG]", style = MaterialTheme.typography.labelMedium, color = c)
    }
}

// —— 昨日复盘 ——
@Composable
private fun YesterdayCard(y: YesterdayInfo, today: DailyWeather?, unit: String, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (y.condition != null) {
                WeatherIcon(y.condition, Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
            }
            if (y.high != null && y.low != null) {
                Text(
                    "${Fmt.temp(y.high, unit)}° / ${Fmt.temp(y.low, unit)}°",
                    style = MaterialTheme.typography.titleMedium,
                    color = ZhishengText,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(10.dp))
            }
            y.aqi?.let {
                Text("AQI $it", style = MaterialTheme.typography.labelMedium, color = aqiColor(it))
            }
            Spacer(Modifier.weight(1f))
            tempDelta(today?.high, y.high, unit)?.let { diff ->
                Text(
                    "ΔT ${if (diff >= 0) "+" else ""}$diff°",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (diff > 0) ZhishengOrange else ZhishengMint,
                )
            }
        }
    }
}

// —— 台风 ——
@Composable
private fun TyphoonCard(typhoons: List<TyphoonInfo>, modifier: Modifier) {
    HudCard(modifier = modifier.fillMaxWidth()) {
        Column {
            typhoons.forEachIndexed { i, t ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        t.type ?: "TY",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZhishengOrange,
                        modifier = Modifier.width(34.dp),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(t.name ?: "", style = MaterialTheme.typography.titleSmall, color = ZhishengText)
                    Spacer(Modifier.width(8.dp))
                    t.ename?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary)
                    }
                    Spacer(Modifier.weight(1f))
                    t.windSpeed?.let {
                        Text("${it.roundToInt()}m/s", style = MaterialTheme.typography.labelMedium, color = ZhishengCyan)
                    }
                }
            }
        }
    }
}

// —— 枳生页脚 ——
@Composable
private fun Footer(data: WeatherData, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "ZHISHENG CORE // SENSOR-1 · FORECAST-2 · DISPLAY-3",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary,
            letterSpacing = 1.5.sp,
        )
        Text(
            "${dataSourceSummary(data)} · 枳生天气 v${com.zhisheng.weather.BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = ZhishengTextTertiary.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
        )
    }
}

private fun dataSourceLabel(source: String?): String = when (source) {
    "QWEATHER" -> "数据来自和风天气"
    "CAIYUN" -> "数据来自彩云天气"
    "XIAOMI" -> "数据来自小米天气"
    "OPEN-METEO" -> "数据来自 Open-Meteo"
    else -> "DATA ${source ?: "--"}"
}

private fun dataSourceSummary(data: WeatherData): String {
    val supplements = data.blockSources.values
        .filter { it != data.dataSource }
        .distinct()
        .map(::dataSourceShortLabel)
    return if (supplements.isEmpty()) dataSourceLabel(data.dataSource)
    else "${dataSourceLabel(data.dataSource)} · 部分预报由 ${supplements.joinToString("/")} 补全"
}

private fun supplementShortLabel(data: WeatherData): String {
    val extras = data.blockSources.values.filter { it != data.dataSource }.distinct()
    return if (extras.isEmpty()) "" else extras.joinToString(prefix = "+", separator = "+") { dataSourceShortLabel(it) }
}

private fun dataSourceShortLabel(source: String?): String = when (source) {
    "QWEATHER" -> "和风"
    "CAIYUN" -> "彩云"
    "XIAOMI" -> "小米"
    "OPEN-METEO" -> "OPEN-METEO"
    else -> source ?: "--"
}

// —— 启动加载：枳生终端自检序列 ——
@Composable
private fun BootState(bootAnim: Boolean = true) {
    val lines = listOf(
        "ZHISHENG WEATHER TERMINAL v${com.zhisheng.weather.BuildConfig.VERSION_NAME}",
        "ZHISHENG CORE ... ONLINE",
        "SYNC ATMOSPHERIC DATA ...",
    )
    var count by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        // 关闭开机动画时直接全部显示，不逐行打字延迟（v0.0.3：bootAnim 设置项此前无人读取）
        if (!bootAnim) {
            count = lines.size
            return@LaunchedEffect
        }
        lines.indices.forEach { i ->
            kotlinx.coroutines.delay(260)
            count = i + 1
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            lines.take(count).forEach { l ->
                Text(
                    "> $l",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                "█",
                style = MaterialTheme.typography.bodySmall,
                color = ZhishengMint,
            )
        }
    }
}

@Composable
private fun EmptyState(onSearchClick: () -> Unit) {
    // 终端打字序列：与开屏 BootState 同款，逐字敲出 + █ 光标；文案不点名任何具体城市
    val lines = listOf(
        "NO CITY // 未接入城市",
        "SEARCH ANY CITY // 输入任意城市名",
        "AWAITING INPUT ...",
    )
    var doneCount by remember { mutableIntStateOf(0) }
    var chars by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        lines.forEachIndexed { i, l ->
            chars = 0
            l.indices.forEach { c ->
                kotlinx.coroutines.delay(26)
                chars = c + 1
            }
            kotlinx.coroutines.delay(240)
            doneCount = i + 1
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WeatherIcon(WeatherCondition.CLEAR, Modifier.size(64.dp).alpha(0.6f))
        Spacer(Modifier.height(24.dp))
        Column(Modifier.align(Alignment.Start)) {
            lines.take(doneCount).forEach { l ->
                Text(
                    "> $l",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(6.dp))
            }
            if (doneCount < lines.size) {
                Text(
                    "> " + lines[doneCount].take(chars),
                    style = MaterialTheme.typography.bodySmall,
                    color = ZhishengMint,
                    letterSpacing = 1.sp,
                )
            }
            Text(
                "█",
                style = MaterialTheme.typography.bodySmall,
                color = ZhishengMint,
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .clip(RectangleShape)
                .background(ZhishengSurface)
                .border(1.dp, ZhishengMint.copy(alpha = 0.6f), RectangleShape)
                .drawCornerBrackets(ZhishengMint)
                .clickable(role = Role.Button, onClickLabel = "添加城市") { onSearchClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text("[ + ADD CITY ]", style = MaterialTheme.typography.titleSmall, color = ZhishengMint, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ErrorState(message: String, onSearchClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("!! LINK FAILURE", style = MaterialTheme.typography.titleMedium, color = ZhishengRed, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = ZhishengTextSecondary)
        Spacer(Modifier.height(16.dp))
        Text(
            "[ 换一个城市试试 ]",
            style = MaterialTheme.typography.bodyMedium,
            color = ZhishengMint,
            modifier = Modifier
                .clickable(role = Role.Button, onClickLabel = "换一个城市") { onSearchClick() }
                .padding(8.dp),
        )
    }
}

// —— 城市抽屉 ——
@Composable
private fun CityDrawer(
    uiState: HomeUiState,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onAddCity: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZhishengSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
            Text("00//", style = MaterialTheme.typography.titleSmall, color = ZhishengOrange, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("城市", style = MaterialTheme.typography.titleMedium, color = ZhishengText, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text("CITY LIST", style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.5.sp)
        }
        if (uiState.cities.isEmpty()) {
            Text("还没有保存的城市", style = MaterialTheme.typography.bodySmall, color = ZhishengTextTertiary)
        }
        uiState.cities.forEachIndexed { i, city ->
            val selected = city.locationKey == uiState.selectedCity?.locationKey
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RectangleShape)
                    .background(if (selected) ZhishengCard else Color.Transparent)
                    .clickable { onSelect(city.locationKey) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "%02d".format(i + 1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) ZhishengOrange else ZhishengTextTertiary,
                )
                Spacer(Modifier.width(10.dp))
                if (selected) {
                    Box(Modifier.size(width = 3.dp, height = 14.dp).background(ZhishengMint))
                    Spacer(Modifier.width(8.dp))
                }
                // 城市名 + 归属地：同名城市（金川区@金昌 vs 金川县@阿坝）必须可区分（v0.0.1）
                Column(Modifier.weight(1f)) {
                    Text(
                        city.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) ZhishengMint else ZhishengText,
                    )
                    if (city.affiliation.isNotBlank()) {
                        Text(
                            city.affiliation,
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengTextTertiary,
                        )
                    }
                }
                IconButton(onClick = { onRemove(city.locationKey) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "删除${city.name}", tint = ZhishengTextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape)
                .background(ZhishengCard)
                .border(1.dp, ZhishengMint.copy(alpha = 0.5f), RectangleShape)
                .clickable { onAddCity() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = ZhishengMint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("添加城市", style = MaterialTheme.typography.titleSmall, color = ZhishengMint, letterSpacing = 1.sp)
        }
    }
}

private val hourFmt = DateTimeFormatter.ofPattern("H时")
private val timeFmt = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private fun formatHour(epoch: Long, zone: ZoneId): String {
    val zoned = Instant.ofEpochMilli(epoch).atZone(zone)
    return hourFmt.format(zoned)
}

private fun formatWeekday(epoch: Long, index: Int, zone: ZoneId): String {
    val zoned = Instant.ofEpochMilli(epoch).atZone(zone)
    if (index == 0) return "今天"
    return when (zoned.dayOfWeek.value) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"; 5 -> "周五"; 6 -> "周六"; else -> "周日"
    }
}

private fun formatTime(epoch: Long, zone: ZoneId): String =
    timeFmt.format(Instant.ofEpochMilli(epoch).atZone(zone))

private fun formatAlertTime(s: String): String = try {
    s.substring(0, minOf(16, s.length)).replace("T", " ")
} catch (_: Exception) {
    s
}
