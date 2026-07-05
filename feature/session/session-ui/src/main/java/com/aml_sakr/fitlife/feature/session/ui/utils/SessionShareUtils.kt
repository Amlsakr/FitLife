package com.aml_sakr.fitlife.feature.session.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.aml_sakr.fitlife.feature.session.domain.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object SessionShareUtils {

    suspend fun shareToWhatsApp(context: Context, session: Session, calories: Int) {
        val shareText = """
            🔥 I just finished a workout on FitLife!
            
            ⏱ Duration: ${session.durationSeconds?.let { it / 60 } ?: 0} min
            💪 Reps: ${session.totalReps}
            🚨 Fatigue detected: ${session.fatigueEventCount} times
            ⚡ Calories: $calories kcal
            
            Download FitLife to start your AI-guided fitness journey!
        """.trimIndent()

        withContext(Dispatchers.IO) {
            val bitmap = generateShareBadge(context, session, calories)
            if (bitmap == null) return@withContext

            val imageUri = saveBitmapToCache(context, bitmap)
            if (imageUri == null) return@withContext

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "Share your achievement"))
            }
        }
    }

    private fun generateShareBadge(context: Context, session: Session, calories: Int): Bitmap? {
        return try {
            val width = 1080
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            val paint = Paint().apply {
                color = Color.parseColor("#1A1C1E") // Dark theme background
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Branding
            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 80f
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("FitLife", 100f, 200f, textPaint)

            // Metrics
            textPaint.textSize = 60f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            var yPos = 400f
            canvas.drawText("Workout Summary", 100f, yPos, textPaint)
            
            textPaint.textSize = 100f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            yPos += 150f
            canvas.drawText("${session.totalReps} Reps", 100f, yPos, textPaint)
            
            yPos += 150f
            canvas.drawText("${session.durationSeconds?.let { it / 60 } ?: 0} Min", 100f, yPos, textPaint)
            
            yPos += 150f
            canvas.drawText("$calories kcal", 100f, yPos, textPaint)

            bitmap
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            // Unique filename to avoid race conditions and cache issues
            val filename = "session_badge_${System.currentTimeMillis()}.png"
            val file = File(cachePath, filename)
            
            // Cleanup old files in the directory
            cachePath.listFiles()?.forEach { if (it.name.startsWith("session_badge_")) it.delete() }

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }
}
