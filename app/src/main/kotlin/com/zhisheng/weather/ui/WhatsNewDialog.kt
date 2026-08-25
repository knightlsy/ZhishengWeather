package com.zhisheng.weather.ui

import androidx.compose.animation.AnimatedContent
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
import com.zhisheng.weather.ui.theme.ZhishengBg
import com.zhisheng.weather.ui.theme.ZhishengCard
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.ZhishengMint
import com.zhisheng.weather.ui.theme.ZhishengOrange
import com.zhisheng.weather.ui.theme.ZhishengSurface
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary

internal const val WhatsNewVersion = "0.1.0"
internal const val WhatsNewPreferenceFile = "zhisheng_whats_new"
internal const val WhatsNewSeenKey = "last_seen_version"

@Composable
fun WhatsNewDialog(onClose: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pageCount = 3

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ZhishengBg.copy(alpha = 0.90f))
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
                    .background(ZhishengSurface, RectangleShape)
                    .border(1.dp, ZhishengCyan.copy(alpha = 0.54f), RectangleShape),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
                        Text(
                            "ZHISHENG WEATHER / 0.1.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengCyan,
                            letterSpacing = 1.3.sp,
                        )
                        Text(
                            "更新说明",
                            style = MaterialTheme.typography.titleLarge,
                            color = ZhishengText,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        modifier = Modifier.size(52.dp).clickable(role = Role.Button, onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = MaterialTheme.typography.headlineSmall, color = ZhishengTextSecondary)
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
                                .background(if (index <= page) ZhishengCyan else ZhishengCardBorder),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (fadeIn(tween(180)) + slideInHorizontally(tween(240)) { it * direction / 4 }) togetherWith
                            (fadeOut(tween(130)) + slideOutHorizontally(tween(190)) { -it * direction / 4 })
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
                            0 -> cityDeckPage()
                            1 -> interfacePage()
                            else -> dataAndWidgetPage()
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
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
                            color = if (page > 0) ZhishengTextSecondary else ZhishengTextTertiary,
                        )
                    }
                    Text(
                        "0${page + 1}/0$pageCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZhishengTextTertiary,
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
                            if (page < pageCount - 1) "[ 下一步 ]" else "[ 进入 0.1.0 ]",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (page < pageCount - 1) ZhishengCyan else ZhishengMint,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.cityDeckPage() {
    item {
        UpdateTitle("01//", "长按快速选择城市", "CITY SWITCH", ZhishengOrange)
    }
    item {
        EmphasisBlock(
            "先保存至少 2 个城市。上下滑动天气页时，屏幕底部会有一条若隐若现的呼吸光——那里就是城市传感器。",
        )
    }
    item { GestureStep("01", "按住", "拇指按住屏幕底部的呼吸光，先不要松手。") }
    item { GestureStep("02", "等震动", "手机震动、卡牌弹出后，保持按住并左右滑到目标城市。") }
    item { GestureStep("03", "松手切换", "让目标卡牌停在中央，再松手；主页会切换到该城市。") }
    item {
        EmphasisBlock(
            "想松手慢慢选：卡牌出现后明显向上推。第二次震动代表卡组已固定，这时可以松手，再左右滑动并点选城市。",
            accent = ZhishengMint,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.interfacePage() {
    item { UpdateTitle("02//", "界面与天气效果", "INTERFACE", ZhishengCyan) }
    item { FeatureBlock("模块顺序", "设置 → 主屏显示中可重新排列天气模块，也能一键恢复默认顺序。") }
    item { FeatureBlock("全天气氛围", "晴昼、晴夜、多云、阴、雨、雪、雷暴、雾、霾、沙尘和大风等都有自己的终端背景；下雨时会出现灵动的数据雨。") }
    item { FeatureBlock("天气效果预览", "开启开发者模式后，可用模拟天气逐项查看氛围效果，不会改变主页正在显示的真实天气。") }
    item { FeatureBlock("设置更清楚", "设置页重新整理，更容易找到需要的选项；新增亮度更低的绿、蓝强调色，浅色模式天气图标也有了合适的颜色。") }
}

private fun androidx.compose.foundation.lazy.LazyListScope.dataAndWidgetPage() {
    item { UpdateTitle("03//", "天气数据、小组件与社区", "SYSTEM", ZhishengMint) }
    item { FeatureBlock("和风与彩云", "想使用和风或彩云时，可在设置中开启开发者模式，跟随步骤填写凭据并测试连接。可用内容取决于对应账号的权限。") }
    item { FeatureBlock("天气信息更准确", "高低温会随时段切换；分钟降水、雨渐停、降雨概率与风向的显示更加一致，没雨时不再保留空的降水卡。") }
    item { FeatureBlock("五种终端小组件", "新增 4×1、2×4 等尺寸，升级为带呼吸状态灯的 3D 终端设备外观，并修复浅色模式图标配色。") }
    item { FeatureBlock("社区入口", "关于页新增贡献者名单和用户交流 QQ 群，可直接查看二维码或复制群号。") }
    item {
        Text(
            "以后想再看：设置 → 关于 → 点击版本号。",
            style = MaterialTheme.typography.bodyMedium,
            color = ZhishengOrange,
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
            Text(en, style = MaterialTheme.typography.labelSmall, color = ZhishengTextTertiary, letterSpacing = 1.4.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = ZhishengText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmphasisBlock(text: String, accent: androidx.compose.ui.graphics.Color = ZhishengOrange) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(ZhishengCard)
            .border(1.dp, accent.copy(alpha = 0.58f), RectangleShape)
            .padding(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = ZhishengText, lineHeight = 21.sp)
    }
}

@Composable
private fun GestureStep(index: String, action: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().border(1.dp, ZhishengCardBorder, RectangleShape).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("[$index]", style = MaterialTheme.typography.labelMedium, color = ZhishengCyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))
        Column {
            Text(action, style = MaterialTheme.typography.titleSmall, color = ZhishengText, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = ZhishengTextSecondary, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun FeatureBlock(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("> $title", style = MaterialTheme.typography.titleSmall, color = ZhishengCyan, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = ZhishengTextSecondary, lineHeight = 20.sp)
    }
}
