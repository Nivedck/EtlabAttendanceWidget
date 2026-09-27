package io.github.shreyasskdev.tiledeck.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.shreyasskdev.tiledeck.ui.refreshAttendanceWidgets

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            refreshAttendanceWidgets(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

/**
 * Queue a widget refresh via WorkManager instead of a raw coroutine.
 * This survives the app being backgrounded or the process being killed,
 * which a plain CoroutineScope (even an app-scoped one) does not.
 */
fun enqueueWidgetRefresh(context: Context) {
    val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build()
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(
            "widget_refresh",
            ExistingWorkPolicy.REPLACE,
            request,
        )
}