package io.github.shreyasskdev.tiledeck.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.shreyasskdev.tiledeck.ui.refreshAttendanceWidgets
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

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

// ─────────────────────────────────────────────────────────────────────────────
//  Timetable clock-driven refresh
//
//  The timetable widget's look depends on the current time (current-period
//  highlight, ✓ marks, date header), and Glance doesn't observe the clock.
//  So we schedule a refresh at each moment the appearance changes.
// ─────────────────────────────────────────────────────────────────────────────

internal const val TIMETABLE_TICK_WORK_NAME = "timetable_tick"

// Each period start, and 1 minute after each period end
// (isCurrentPeriod is end-inclusive), plus just after midnight.
// Keep in sync with PERIOD_START_TIMES / PERIOD_END_TIMES in TimetableWidget.kt.
private val TIMETABLE_BOUNDARIES = listOf(
    LocalTime.of(9, 30), LocalTime.of(10, 21),
    LocalTime.of(10, 30), LocalTime.of(11, 21),
    LocalTime.of(11, 30), LocalTime.of(12, 21),
    LocalTime.of(13, 30), LocalTime.of(14, 21),
    LocalTime.of(14, 30), LocalTime.of(15, 21),
    LocalTime.of(15, 30), LocalTime.of(16, 21),
    LocalTime.of(0, 0, 5), // new day -> header + weekday/weekend switch
)

/** Bumps the state key and redraws only the timetable widgets. */
suspend fun refreshTimetableWidgets(context: Context) {
    val appContext = context.applicationContext
    val now = System.currentTimeMillis()
    val manager = GlanceAppWidgetManager(appContext)
    manager.getGlanceIds(TimetableWidget::class.java).forEach { id ->
        runCatching {
            updateAppWidgetState(appContext, PreferencesGlanceStateDefinition, id) { prefs: Preferences ->
                prefs.toMutablePreferences().apply { this[TIMETABLE_WIDGET_UPDATE_KEY] = now }
            }
            TimetableWidget().update(appContext, id)
        }
    }
}

class TimetableTickWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        refreshTimetableWidgets(applicationContext)
        scheduleNextTimetableTick(applicationContext)
        return Result.success()
    }
}

/** Enqueues a one-shot worker for the next boundary; the worker re-schedules itself. */
fun scheduleNextTimetableTick(context: Context) {
    val now = LocalDateTime.now()
    val next = TIMETABLE_BOUNDARIES
        .map { now.toLocalDate().atTime(it) }
        .filter { it.isAfter(now) }
        .minOrNull()
        ?: now.toLocalDate().plusDays(1).atTime(LocalTime.of(0, 0, 5))

    val delayMs = Duration.between(now, next).toMillis().coerceAtLeast(1_000L)

    val request = OneTimeWorkRequestBuilder<TimetableTickWorker>()
        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context.applicationContext)
        .enqueueUniqueWork(TIMETABLE_TICK_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
}