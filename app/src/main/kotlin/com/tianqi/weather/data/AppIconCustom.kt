package com.tianqi.weather.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.graphics.PorterDuff
import android.net.Uri
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import java.io.File

/**
 * 自定义图标：用户从相册上传的本地图片，保存到应用私有目录后作为图标使用。
 *
 * 说明：Android 桌面图标只能在预声明的 activity-alias 之间切换，无法在运行时
 * 把任意图片直接设为 launcher 图标。因此上传的图片会同时用于两处：
 *  1. 应用内「自定义」风格的实际显示（设置页预览、应用图标管理）；
 *  2. 作为 IconCustom 别名的视觉载体——IconCustom 别名在安装时就已预声明，
 *     用户上传后只需启用该别名即可把桌面图标切到「自定义」入口。
 * 图片会裁剪为圆角正方形并压缩存储，不落外部存储，卸载即清。
 */
object AppIconCustom {

    private const val TAG = "AppIconCustom"
    private const val FILE_NAME = "custom_icon.png"
    private const val MAX_PX = 512
    private const val CORNER_DP = 22f

    fun filesDir(context: Context): File = context.filesDir

    fun customIconFile(context: Context): File = File(context.filesDir, FILE_NAME)

    fun hasCustomIcon(context: Context): Boolean = customIconFile(context).exists()

    fun clearCustomIcon(context: Context): Boolean {
        return runCatching { customIconFile(context).delete() }.onFailure {
            Log.e(TAG, "clearCustomIcon failed", it)
        }.getOrDefault(false)
    }

    /** 保存并裁剪用户选择的图片；返回是否成功。 */
    fun saveCustomIcon(context: Context, bitmap: Bitmap): Boolean {
        return runCatching {
            val scaled = scaleToMax(bitmap, MAX_PX)
            val rounded = roundCrop(scaled)
            customIconFile(context).outputStream().use { out ->
                rounded.compress(Bitmap.CompressFormat.PNG, 92, out)
            }
            if (scaled != bitmap) scaled.recycle()
            if (rounded != scaled) rounded.recycle()
            true
        }.onFailure { Log.e(TAG, "saveCustomIcon failed", it) }.getOrDefault(false)
    }

    /** 供 Compose 预览使用的 BitmapDrawable；未上传时返回 null。 */
    fun previewDrawable(context: Context): BitmapDrawable? {
        val f = customIconFile(context)
        if (!f.exists()) return null
        val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath) ?: return null
        return BitmapDrawable(context.resources, bmp)
    }

    /**
     * 从相册选择的 Uri 读取图片并保存为自定义图标。
     * 返回是否成功；失败时已清理残留文件。
     */
    fun saveFromUri(context: Context, uri: Uri): Boolean {
        val bmp = runCatching {
            val cr = context.contentResolver
            val input = cr.openInputStream(uri) ?: return false
            android.graphics.BitmapFactory.decodeStream(input).also { input.close() }
        }.getOrNull() ?: return false
        return saveCustomIcon(context, bmp)
    }

    private fun scaleToMax(src: Bitmap, maxPx: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxPx && h <= maxPx) return src
        val scale = maxPx.toFloat() / maxOf(w, h).toFloat()
        val nw = (w * scale).toInt().coerceAtLeast(1)
        val nh = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }

    private fun roundCrop(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = android.graphics.Paint().apply { isAntiAlias = true }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val radius = size * CORNER_DP / 2f
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.blendMode = PorterDuff.Mode.SRC_IN
        val srcRect = RectF(
            ((src.width - size) / 2).toFloat(),
            ((src.height - size) / 2).toFloat(),
            ((src.width - size) / 2 + size).toFloat(),
            ((src.height - size) / 2 + size).toFloat(),
        )
        canvas.drawBitmap(src, srcRect, rect, paint)
        return output
    }
}