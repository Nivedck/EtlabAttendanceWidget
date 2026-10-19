package io.github.shreyasskdev.tiledeck.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.WorkManager

class TimetableWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimetableWidget()

    // First timetable widget placed: start the period-boundary tick chain.
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextTimetableTick(context)
    }

    // Last timetable widget removed: stop ticking.
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context.applicationContext)
            .cancelUniqueWork(TIMETABLE_TICK_WORK_NAME)
    }
}