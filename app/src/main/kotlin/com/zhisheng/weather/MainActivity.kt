package com.zhisheng.weather

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zhisheng.weather.data.SettingsRepository
import com.zhisheng.weather.model.City
import com.zhisheng.weather.ui.SearchScreen
import com.zhisheng.weather.ui.WeatherViewModel
import com.zhisheng.weather.ui.home.HomeScreen
import com.zhisheng.weather.ui.SettingsScreen
import com.zhisheng.weather.ui.theme.ZhishengWeatherTheme
import kotlinx.coroutines.flow.MutableStateFlow

private enum class Screen { HOME, SEARCH, SETTINGS }
private data class ShortcutCommand(val action: String? = null, val sequence: Long = 0L)

class MainActivity : ComponentActivity() {
    private val shortcutCommand = MutableStateFlow(ShortcutCommand())
    private var shortcutSequence = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dispatchShortcut(intent)
        // 部分厂商系统在新接口下仍依赖旧窗口标志；两者并用可兼容 Android 8.0 和定制 ROM。
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        enableEdgeToEdge()
        setContent {
            ZhishengWeatherTheme {
                val vm: WeatherViewModel = viewModel()
                // rememberSaveable：旋转/进程重建后仍停在原来那屏（v0.0.2）
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                val uiState by vm.uiState.collectAsState()
                val command by shortcutCommand.collectAsState()

                LaunchedEffect(command.sequence) {
                    when (command.action) {
                        ACTION_SEARCH -> screen = Screen.SEARCH
                        ACTION_SETTINGS -> screen = Screen.SETTINGS
                        ACTION_REFRESH -> {
                            screen = Screen.HOME
                            vm.refresh(force = true)
                        }
                    }
                }

                // 常亮屏幕（设置项）
                val keepOn by SettingsRepository.keepScreenOn.collectAsState(initial = false)
                val view = LocalView.current
                DisposableEffect(keepOn) {
                    view.keepScreenOn = keepOn
                    onDispose { view.keepScreenOn = false }
                }

                // 系统返回键：搜索/设置页退回主屏，而不是直接退出 App（v0.0.2）
                BackHandler(enabled = screen != Screen.HOME) {
                    screen = Screen.HOME
                }

                // 每次打开 / 回到前台都拉最新天气（10 分钟内同城不重复拉）
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    vm.refresh(force = false)
                    vm.autoLocateIfEnabled()
                }

                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (targetState == Screen.SEARCH) {
                            (fadeIn(tween(260)) + slideInHorizontally { it / 3 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally { -it / 3 })
                        } else {
                            (fadeIn(tween(260)) + slideInHorizontally { -it / 3 }) togetherWith
                                (fadeOut(tween(180)) + slideOutHorizontally { it / 3 })
                        }
                    },
                    label = "screen",
                ) { current ->
                    when (current) {
                        Screen.SETTINGS -> SettingsScreen(
                            onBack = { screen = Screen.HOME },
                            onLocate = { vm.locateCurrentCity() },
                            locating = uiState.locating,
                            locateMessage = uiState.locateMessage,
                            onClearLocateMessage = { vm.clearLocateMessage() },
                            activeSource = uiState.weather?.dataSource,
                            activeCityName = uiState.selectedCity?.name,
                            sourceLoading = uiState.loading,
                        )
                        Screen.SEARCH -> SearchScreen(
                            onCityPicked = { city: City ->
                                vm.addCityAndSelect(city)
                                screen = Screen.HOME
                            },
                            onBack = { screen = Screen.HOME },
                        )
                        Screen.HOME -> HomeScreen(
                            viewModel = vm,
                            onSearchClick = { screen = Screen.SEARCH },
                            onSettingsClick = { screen = Screen.SETTINGS },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dispatchShortcut(intent)
    }

    private fun dispatchShortcut(intent: Intent?) {
        val action = intent?.action
        if (action == ACTION_SEARCH || action == ACTION_SETTINGS || action == ACTION_REFRESH) {
            shortcutCommand.value = ShortcutCommand(action, ++shortcutSequence)
        }
    }

    private companion object {
        const val ACTION_REFRESH = "com.zhisheng.weather.action.REFRESH"
        const val ACTION_SEARCH = "com.zhisheng.weather.action.SEARCH"
        const val ACTION_SETTINGS = "com.zhisheng.weather.action.SETTINGS"
    }
}
