package com.tianqi.weather.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.tianqi.weather.R
import com.tianqi.weather.data.AppIconCustom
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

/**
 * 图标上传面板：展示当前预览 + 相册入口 + 清除按钮。
 * launcher 由调用方（SettingsScreen）创建并传入 onPickClick，面板只负责展示与回调。
 */
@Composable
fun UploadSheetDialog(
    onPickClick: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val preview = AppIconCustom.previewDrawable(context)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TianQiSurface, RoundedCornerShape(16.dp))
                .border(1.dp, TianQiCardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "自定义桌面图标",
                style = MaterialTheme.typography.titleMedium,
                color = TianQiOrange,
                fontWeight = FontWeight.Bold,
            )
            if (preview != null) {
                AsyncImage(
                    model = AppIconCustom.customIconFile(context),
                    contentDescription = "当前自定义图标预览",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .background(TianQiSurface, RoundedCornerShape(24.dp))
                        .border(2.dp, TianQiCardBorder, RoundedCornerShape(24.dp)),
                )
                Text(
                    "已保存到应用私有目录，不上传任何服务器",
                    style = MaterialTheme.typography.labelSmall,
                    color = TianQiTextTertiary,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .background(TianQiSurface, RoundedCornerShape(24.dp))
                        .border(2.dp, TianQiCardBorder, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_character),
                        contentDescription = null,
                        tint = TianQiTextSecondary,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Text(
                if (preview != null) "点击下方按钮更换或清除" else "从相册选择一张本地图片",
                style = MaterialTheme.typography.labelMedium,
                color = TianQiText,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onPickClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TianQiMint,
                        contentColor = TianQiBg,
                    ),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_character),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("从相册选择")
                }
                if (preview != null) {
                    OutlinedButton(
                        onClick = onClear,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TianQiOrange,
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.verticalGradient(0f to TianQiOrange, 1f to TianQiOrange),
                            width = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                        ),
                    ) {
                        Text("清除")
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_character),
                    contentDescription = "关闭",
                    tint = TianQiTextTertiary,
                )
            }
        }
    }
}