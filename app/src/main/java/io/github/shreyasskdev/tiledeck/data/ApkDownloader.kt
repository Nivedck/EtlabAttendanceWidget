package io.github.shreyasskdev.tiledeck.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ApkDownloader {

    private const val TAG = "ApkDownloader"
    private const val UPDATE_DIR = "updates"
    private const val APK_NAME = "tiledeck-update.apk"

    /** 64 KB buffer — much faster than the previous 8 KB. */
    private const val BUFFER_SIZE = 64 * 1024

    /** UI update throttle: report progress at most every 200 ms. */
    private const val PROGRESS_INTERVAL_MS = 200L

    data class Progress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val bytesPerSecond: Long,
    ) {
        val percent: Int
            get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0

        /** True when the server did not send a Content-Length header. */
        val isTotalUnknown: Boolean
            get() = totalBytes <= 0
    }

    suspend fun downloadAndInstall(
        context: Context,
        downloadUrl: String,
        onProgress: (Progress) -> Unit = {},
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.cacheDir, UPDATE_DIR).apply { mkdirs() }

            // Nuke any previous download so we don't install a stale APK.
            dir.listFiles()?.forEach { it.delete() }

            val apkFile = File(dir, APK_NAME)
            Log.d(TAG, "Downloading $downloadUrl → ${apkFile.absolutePath}")

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .callTimeout(0, TimeUnit.MILLISECONDS)   // no overall cap
                .build()

            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP ${response.code} ${response.message}")
                return@withContext false
            }

            val body = response.body ?: run {
                Log.e(TAG, "Empty response body")
                return@withContext false
            }

            val total = body.contentLength()

            var downloaded = 0L
            val startTime = System.currentTimeMillis()
            var lastReportTime = startTime
            var lastReportBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buf = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (input.read(buf).also { read = it } != -1) {
                        output.write(buf, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        if (now - lastReportTime >= PROGRESS_INTERVAL_MS) {
                            val elapsed = now - lastReportTime
                            val deltaBytes = downloaded - lastReportBytes
                            val bps = if (elapsed > 0) (deltaBytes * 1000) / elapsed else 0L

                            onProgress(Progress(downloaded, total, bps))

                            lastReportTime = now
                            lastReportBytes = downloaded
                        }
                    }
                    output.flush()
                }
            }

            // Final progress report with the overall average speed.
            val elapsed = System.currentTimeMillis() - startTime
            val avgBps = if (elapsed > 0) (downloaded * 1000) / elapsed else 0L
            onProgress(Progress(downloaded, total, avgBps))

            Log.d(TAG, "Downloaded ${apkFile.length()} bytes in ${elapsed}ms (${avgBps / 1024} KB/s)")

            withContext(Dispatchers.Main) {
                launchInstaller(context, apkFile)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Download/install failed", e)
            false
        }
    }

    private fun launchInstaller(context: Context, apkFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}