package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.AppTheme
import java.io.File
import java.io.FileOutputStream

private data class ShareCardPalette(
    val bgStart: Int,
    val bgEnd: Int,
    val primaryAccent: Int,
    val goldAccent: Int,
    val bodyColor: Int,
    val mutedColor: Int
)

object ShareCardGenerator {

    fun shareStyledCard(
        context: Context,
        headerLabel: String,
        bodyText: String,
        footerLabel: String,
        appTheme: AppTheme = AppTheme.SEPIA,
        isNightStyle: Boolean = appTheme == AppTheme.DARK
    ) {
        val effectiveTheme = if (isNightStyle) AppTheme.DARK else appTheme
        val file = runCatching {
            val bitmap = renderCardBitmap(context, headerLabel, bodyText, footerLabel, effectiveTheme)
            val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
            val outFile = File(dir, "share_card_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 96, stream)
            }
            bitmap.recycle()
            outFile
        }.getOrNull() ?: return

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$headerLabel\n\n$bodyText\n\n— $footerLabel")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة البطاقة"))
    }

    private fun renderCardBitmap(
        context: Context,
        headerLabel: String,
        bodyText: String,
        footerLabel: String,
        theme: AppTheme
    ): Bitmap {
        val width = 1080
        val padding = 96
        val contentWidth = width - padding * 2
        val bodyTypeface = ResourcesCompat.getFont(context, R.font.alyamama_medium)
            ?: ResourcesCompat.getFont(context, R.font.amiri_quran)
            ?: Typeface.DEFAULT
        val boldTypeface = ResourcesCompat.getFont(context, R.font.alyamama_bold) ?: Typeface.DEFAULT_BOLD

        val palette = when (theme) {
            AppTheme.DARK -> ShareCardPalette(
                bgStart = Color.parseColor("#1A1612"),
                bgEnd = Color.parseColor("#241F19"),
                primaryAccent = Color.parseColor("#8FAE8B"),
                goldAccent = Color.parseColor("#D4A857"),
                bodyColor = Color.parseColor("#F3EBDA"),
                mutedColor = Color.parseColor("#B5A78F")
            )
            AppTheme.SEPIA, AppTheme.SYSTEM -> ShareCardPalette(
                bgStart = Color.parseColor("#E6D5B2"),
                bgEnd = Color.parseColor("#D8C194"),
                primaryAccent = Color.parseColor("#6B472B"),
                goldAccent = Color.parseColor("#936120"),
                bodyColor = Color.parseColor("#2B1E14"),
                mutedColor = Color.parseColor("#5A4632")
            )
        }
        val (bgStart, bgEnd, primaryAccent, goldAccent, bodyColor, mutedColor) = palette

        val bodySizePx = when {
            bodyText.length > 360 -> 40f
            bodyText.length > 200 -> 48f
            else -> 56f
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bodyColor
            textSize = bodySizePx
            typeface = bodyTypeface
        }
        val bodyLayout = StaticLayout.Builder
            .obtain(bodyText, 0, bodyText.length, bodyPaint, contentWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(14f, 1.38f)
            .build()

        val headerHeight = 148
        val footerHeight = 136
        val height = (headerHeight + bodyLayout.height + footerHeight + padding * 2).coerceIn(720, 2200)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), bgStart, bgEnd, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = goldAccent
            alpha = 140
        }
        canvas.drawRoundRect(RectF(42f, 42f, width - 42f, height - 42f), 48f, 48f, borderPaint)

        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = primaryAccent
            alpha = 85
        }
        canvas.drawRoundRect(RectF(60f, 60f, width - 60f, height - 60f), 38f, 38f, innerBorderPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryAccent
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = boldTypeface
        }
        canvas.drawText(headerLabel, width / 2f, padding + 64f, headerPaint)

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldAccent
            alpha = 125
            strokeWidth = 2.5f
        }
        val midX = width / 2f
        val dividerY = padding + 104f
        canvas.drawLine(width * 0.24f, dividerY, midX - 24f, dividerY, dividerPaint)
        canvas.drawLine(midX + 24f, dividerY, width * 0.76f, dividerY, dividerPaint)

        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = goldAccent
            alpha = 175
        }
        val diamondPath = android.graphics.Path().apply {
            moveTo(midX, dividerY - 12f)
            lineTo(midX + 12f, dividerY)
            lineTo(midX, dividerY + 12f)
            lineTo(midX - 12f, dividerY)
            close()
        }
        canvas.drawPath(diamondPath, diamondPaint)

        canvas.save()
        canvas.translate(padding.toFloat(), padding + headerHeight.toFloat())
        bodyLayout.draw(canvas)
        canvas.restore()

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = mutedColor
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = boldTypeface
        }
        canvas.drawText(footerLabel, width / 2f, height - padding - 18f, footerPaint)
        return bitmap
    }
}
