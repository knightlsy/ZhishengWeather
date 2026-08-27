/* Hallmark · pre-emit critique: P5 H5 E5 S5 R5 V4 */
package com.zhisheng.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.zhisheng.weather.ui.theme.ZhishengCardBorder
import com.zhisheng.weather.ui.theme.ZhishengCyan
import com.zhisheng.weather.ui.theme.ZhishengMint
import com.zhisheng.weather.ui.theme.ZhishengSurface
import com.zhisheng.weather.ui.theme.ZhishengText
import com.zhisheng.weather.ui.theme.ZhishengTextSecondary
import com.zhisheng.weather.ui.theme.ZhishengTextTertiary

internal val CommunityContributors = listOf(
    "PPQ1028",
    "Uinuan1",
    "KZzzzo",
    "睡觉了寂",
    "微生之最",
    "r1file",
    "vsqesy3721",
    "茉莉羽",
    "陈大橙",
    "飞667",
    "一杯冰美式、、",
    "M1ralce",
    "紅星照耀中國",
    "我爱跑步",
    "河鱼天雁",
    "你的心里没点高数吗",
    "周月星斗",
    "无敌战神暴王龙",
    "control3",
    "明珠有泪",
    "Gstar_",
    "伍拾两HZ",
    "寡欲老公猪",
)

@Composable
fun ContributorsDialog(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ZhishengBg.copy(alpha = 0.82f))
                .safeDrawingPadding()
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val panelMaxHeight = minOf(maxHeight - 24.dp, 620.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = panelMaxHeight)
                    .background(ZhishengSurface, RectangleShape)
                    .border(1.dp, ZhishengCardBorder, RectangleShape),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(vertical = 14.dp)) {
                        Text(
                            "COMMUNITY / ${com.zhisheng.weather.BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZhishengCyan,
                            letterSpacing = 1.4.sp,
                        )
                        Text(
                            "社区贡献者",
                            style = MaterialTheme.typography.titleLarge,
                            color = ZhishengText,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(role = Role.Button, onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", style = MaterialTheme.typography.headlineSmall, color = ZhishengTextSecondary)
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                Text(
                    "感谢你们在社区里留下名字，也感谢每一次试用、反馈与陪伴。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZhishengTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                )
                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(CommunityContributors, key = { _, id -> id }) { index, id ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "[${(index + 1).toString().padStart(2, '0')}]",
                                style = MaterialTheme.typography.labelMedium,
                                color = ZhishengMint,
                                letterSpacing = 0.8.sp,
                            )
                            Spacer(Modifier.size(14.dp))
                            Text(
                                id,
                                style = MaterialTheme.typography.titleMedium,
                                color = ZhishengText,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        if (index != CommunityContributors.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 1.dp,
                                color = ZhishengCardBorder.copy(alpha = 0.65f),
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = ZhishengCardBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onClose)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "名单按酷安留言顺序排列",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZhishengTextTertiary,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "[ 关闭 ]",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZhishengCyan,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
