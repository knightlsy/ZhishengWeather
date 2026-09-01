package com.tianqi.weather.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tianqi.weather.ui.theme.TianQiBg
import com.tianqi.weather.ui.theme.TianQiCard
import com.tianqi.weather.ui.theme.TianQiCardBorder
import com.tianqi.weather.ui.theme.TianQiCyan
import com.tianqi.weather.ui.theme.TianQiMint
import com.tianqi.weather.ui.theme.TianQiOrange
import com.tianqi.weather.ui.theme.TianQiSurface
import com.tianqi.weather.ui.theme.TianQiText
import com.tianqi.weather.ui.theme.TianQiTextSecondary
import com.tianqi.weather.ui.theme.TianQiTextTertiary

internal const val WhatsNewVersion = "1.0.0"
internal const val WhatsNewPreferenceFile = "tianqi_whats_new"
internal const val WhatsNewSeenKey = "last_seen_version"

@Composable
fun WhatsNewDialog(onClose: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pageCount = 2

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(TianQiBg.copy(alpha = 0.90f))
                .safeDrawingPadding()
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val panelHeight = minOf(maxHeight - 24.dp, 700.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = panelHeight)
                    .background(TianQiSurface, RectangleShape)
                    .border(1.dp, TianQiCyan.copy(alpha = 0.54f), RectangleShape),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
                        Text(
                            "TIANQI WEATHER / 1.0.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = TianQiCyan,
                            letterSpacing = 1.3.sp,
                        )
                        Text(
                            "更新说明",
                            style = MaterialTheme.typography.titleLarge,
                            color = TianQiText,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        modifier = Modifier.size(52.dp).clickable(role = Role.Button, onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = MaterialTheme.typography.headlineSmall, color = TianQiTextSecondary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    repeat(pageCount) { index ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (index <= page) TianQiCyan else TianQiCardBorder),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = TianQiCardBorder)

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        val enter = fadeIn(tween(140, easing = LinearOutSlowInEasing)) +
                            slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { it * direction / 6 }
                        val exit = fadeOut(tween(110, easing = FastOutLinearInEasing)) +
                            slideOutHorizontally(tween(180, easing = FastOutSlowInEasing)) { -it * direction / 8 }
                        enter togetherWith exit
                    },
                    label = "whats-new-page",
                    modifier = Modifier.weight(1f),
                ) { currentPage ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when (currentPage) {
                            0 -> forecastPage()
                            else -> sourcePage()
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = TianQiCardBorder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = page > 0, role = Role.Button) { page-- }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                    ) {
                        Text(
                            if (page > 0) "[ 上一步 ]" else "更新说明",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (page > 0) TianQiTextSecondary else TianQiTextTertiary,
                        )
                    }
                    Text(
                        "0${page + 1}/0$pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = TianQiTextTertiary,
                        letterSpacing = 1.sp,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) {
                                if (page < pageCount - 1) page++ else onClose()
                            }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Text(
                            if (page < pageCount - 1) "[ 下一步 ]" else "[ 进入 1.0.0 ]",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (page < pageCount - 1) TianQiCyan else TianQiMint,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.forecastPage() {
    item {
        UpdateTitle("01//", "天气体验升级", "WHAT'S CHANGED", TianQiOrange)
    }
    item {
        EmphasisBlock(
            "横屏、定位、预报阅读和数据展示全面升级，日常查看更准确、更清楚。",
        )
    }
    item { FeatureBlock("横屏待机", "横放手机进入独立气象时钟；也可以在设置中关闭横屏，保持竖屏使用。") }
    item { FeatureBlock("定位更精确", "可选择精确位置并尽量显示街道；无法识别时会稳妥回退到城市。") }
    item { FeatureBlock("逐时与逐日修复", "修复预报缺失和温度曲线错位；逐日增加日号，跨月时显示月份分隔。") }
    item { FeatureBlock("短时降水重做", "直接说明何时开始或停止，显示峰值雨势和两小时时间轴；无雨时也保持完整布局。") }
    item { FeatureBlock("同一时刻对齐", "实况、逐时「现在」和短时降水按城市当地时间说话；正在下雨时不会再出现晴窗。") }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sourcePage() {
    item { UpdateTitle("02//", "数据与显示升级", "DATA + DISPLAY", TianQiCyan) }
    item { FeatureBlock("和风接入更可靠", "保存凭据前会验证真实天气权限；补齐昼夜预报、降水雨强和空气质量解析，手动选择后不再混入其他天气源。") }
    item { FeatureBlock("自动优选更稳定", "自动优选以小米天气为主，只在缺少数据时补充其他来源；手动选择时保持单一来源，并标出真实时间粒度和更新时间。") }
    item { FeatureBlock("套餐能力不浪费", "数据源返回了更长逐时预报或更多生活指数时会完整展示；没有返回的内容不会用占位信息冒充。") }
    item { FeatureBlock("数据口径校准", "气压按地面观测口径显示，统一一小时降水与空气污染物单位，跨源对照更直观。") }
    item { FeatureBlock("遥测可以自选", "开发者模式可自由选择显示项目，项目数量变化时会自动排满，不再留下突兀空栏。") }
    item { FeatureBlock("生活指数更整齐", "生活指数会按返回数量自动排满；开发者模式也可以自由选择要显示的项目。") }
    item { FeatureBlock("夜间氛围增强", "增加更明显的氛围档位，并改善夜间模式下天气效果不易看见的问题。") }
    item { FeatureBlock("透明小组件", "桌面小组件换成分层半透明玻璃外壳，重新整理边框、字号和对齐，并补充逐时趋势与生活信息。") }
    item { FeatureBlock("天气娘图标", "默认启用新的天气娘头像；喜欢原版时，可在设置的界面选项中随时切回经典图标。") }
    item { FeatureBlock("真正铺满屏幕", "背景和天气氛围延伸到手势导航区域，底部不再出现割裂的黑边。") }
    item {
        Text(
            "以后想再看：设置 → 关于 → 点击版本号。",
            style = MaterialTheme.typography.bodyMedium,
            color = TianQiOrange,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun UpdateTitle(index: String, title: String, en: String, accent: androidx.compose.ui.graphics.Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(index, style = MaterialTheme.typography.titleSmall, color = accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(8.dp))
            Text(en, style = MaterialTheme.typography.labelSmall, color = TianQiTextTertiary, letterSpacing = 1.4.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = TianQiText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmphasisBlock(text: String, accent: androidx.compose.ui.graphics.Color = TianQiOrange) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TianQiCard)
            .border(1.dp, accent.copy(alpha = 0.58f), RectangleShape)
            .padding(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TianQiText, lineHeight = 21.sp)
    }
}

@Composable
private fun FeatureBlock(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("> $title", style = MaterialTheme.typography.titleSmall, color = TianQiCyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = TianQiTextSecondary, lineHeight = 20.sp)
    }
}
