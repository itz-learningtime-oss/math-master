package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat

data class ApkInfo(
    val appName: String = "Math Master",
    val packageName: String,
    val versionName: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val exists: Boolean
)

object ApkShareUtil {

    fun getApkInfo(context: Context): ApkInfo {
        return try {
            val sourceDir = context.applicationInfo.sourceDir
            val file = if (sourceDir != null) File(sourceDir) else null
            val size = if (file != null && file.exists()) file.length() else 0L
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vName = pInfo.versionName ?: "1.0"
            val formatted = formatFileSize(size)

            ApkInfo(
                appName = "Math Master",
                packageName = context.packageName,
                versionName = vName,
                sizeBytes = size,
                formattedSize = formatted,
                exists = file?.exists() == true
            )
        } catch (e: Exception) {
            ApkInfo(
                appName = "Math Master",
                packageName = context.packageName,
                versionName = "1.0",
                sizeBytes = 0L,
                formattedSize = "Unknown",
                exists = false
            )
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes.toDouble() / (1024 * 1024)
        return DecimalFormat("#,##0.0 MB").format(mb)
    }

    suspend fun shareApkFile(
        context: Context,
        onStart: () -> Unit = {},
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        withContext(Dispatchers.Main) {
            onStart()
        }

        try {
            val (apkUri, fileName) = withContext(Dispatchers.IO) {
                val sourcePath = context.applicationInfo.sourceDir
                if (sourcePath.isNullOrEmpty()) {
                    throw IllegalStateException("Application APK source file not found.")
                }
                val sourceFile = File(sourcePath)
                if (!sourceFile.exists()) {
                    throw IllegalStateException("APK file does not exist on device.")
                }

                // Destination in cache folder
                val cacheApksDir = File(context.cacheDir, "apks")
                if (!cacheApksDir.exists()) {
                    cacheApksDir.mkdirs()
                }

                val outApkName = "MathMaster_v1.0.apk"
                val destFile = File(cacheApksDir, outApkName)

                // Copy APK bytes
                sourceFile.copyTo(destFile, overwrite = true)

                // Generate Content Uri via FileProvider
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, destFile)
                Pair(uri, outApkName)
            }

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Math Master APK - Speed Math Training App")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Here is the Math Master APK ($fileName)! Install this app to master speed calculations, multiplication tables, factor pairs, squares, roots, and mental arithmetic."
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val chooser = Intent.createChooser(shareIntent, "Share Math Master APK via").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                onComplete(true, null)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val errorMsg = e.localizedMessage ?: "Failed to prepare APK file"
                Toast.makeText(context, "Error sharing APK: $errorMsg", Toast.LENGTH_LONG).show()
                onComplete(false, errorMsg)
            }
        }
    }

    fun shareAppInvite(context: Context) {
        try {
            val shareText = """
                🧮 Math Master - Speed Math & Mental Arithmetic Trainer
                
                Boost your calculation speed with tailored practice modes:
                • Multiplication Tables (2 to 99) with Flashcard Study
                • Factors Practice: Find factor pairs A × B = N (≤99)
                • Speed Addition & Subtraction (2 to 5 operands)
                • Division with Exact Quotients & Decimal precision
                • Squares (up to 50²) & Cubes (up to 20³)
                • Square Roots (up to √100) & Cube Roots (up to ∛20)
                • Complex Analysis: Difference between Sum & Average
                • 5×5 Matrix Grid Addition Speed Runs with Timer
                • Daily Goals, Streaks & Performance Analytics
                
                Practice daily and become a mental math champion!
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Math Master - Speed Math Trainer App")
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share Math Master with Friends").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share invite: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
